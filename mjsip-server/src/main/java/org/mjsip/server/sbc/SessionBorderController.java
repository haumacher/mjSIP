/*
 * Copyright (C) 2005 Luca Veltri - University of Parma - Italy
 *
 * This source code is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This source code is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this source code; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * Author(s):
 * Luca Veltri (luca.veltri@unipr.it)
 */

package org.mjsip.server.sbc;



import java.util.Hashtable;
import java.util.Vector;

import org.mjsip.config.MetaConfig;
import org.mjsip.config.OptionParser;
import org.mjsip.pool.PortConfig;
import org.mjsip.pool.PortPool;
import org.mjsip.sdp.SdpMessage;
import org.mjsip.server.Proxy;
import org.mjsip.server.ServerProfile;
import org.mjsip.sip.address.GenericURI;
import org.mjsip.sip.address.NameAddress;
import org.mjsip.sip.address.SipURI;
import org.mjsip.sip.header.Header;
import org.mjsip.sip.header.ContactHeader;
import org.mjsip.sip.header.MultipleHeader;
import org.mjsip.sip.header.RouteHeader;
import org.mjsip.sip.header.ViaHeader;
import org.mjsip.sip.message.SipMessage;
import org.mjsip.sip.message.SipMethods;
import org.mjsip.sip.provider.SipConfig;
import org.mjsip.sip.provider.SipKeepAlive;
import org.mjsip.time.ConfiguredScheduler;
import org.mjsip.time.SchedulerConfig;
import org.slf4j.LoggerFactory;
import org.zoolu.net.SocketAddress;
import org.zoolu.util.Parser;



/** Class SessionBorderController implements a SIP proxy acting as simple Session Border Control (SBC).
  * <p>
  * The SBC forces both signaling and media flows to transit through it.
  * It do this by using Record-Route header field, by mangling contact and request-URI addresses,
  * and by mangling SDP content (actually the 'c' and 'm' fields).
  * <br> If the same message has spiralelly passed to the SBC more times,
  * the contact address is mangled recursively each time.
  * <p>
  * For media flows the SBC operates as media relay.
  * <br> Currenly no RTP transcoding is implemented, and just rough UDP relay is performed.
  * <p>
  * A SBC can be used for example to assist an UA (e.g. a softphone)
  * to communicate through NAT middle-boxes (using symmetric RTP).
  * <p>
  * It works dinamically and allows an UA to change its point of attachment
  * without loose the SBC connectivity and relay functinality.
  * <p>
  * A SBC is created by the <i>SessionBorderController()</i> costructor based on:
  * <br> - a ServerProfile containg general proxy configuration,
  * <br> - a SessionBorderControllerProfile containg the specific SBC configuration.
  * <p>
  * SBC uses a set of media ports assigned cyclically to new media relays
  * (when the last port is in-use, the first port is re-used).
  * The set of available media ports is also taken by the SessionBorderControllerProfile.
  */
public class SessionBorderController extends Proxy {
	
	private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(SessionBorderController.class);

	/** Keep-alive parameter that can be used in contact URIs to ask
	  * the UAS to activate keep-alive service, i.e. sending keep-alive null messages
	  * to the UAC SIP port. */
	//public static final String KEEP_ALIVE_PARAM="keep-alive";


	/** The ExtendedSipProvider. It overrides org.mjsip.sip.provier.SipProvider */
	protected ExtendedSipProvider sip_provider;

	/** The SBC configuration */
	protected SessionBorderControllerProfile sbc_profile;

	/** The media GW box */
	MediaGw media_gw;

	/** Sip keep-alive daemons for registered users. */
	Hashtable<String, SipKeepAlive> keepalive_daemons=null;

	/** Maximum time between two handovers (in milliseconds). */
	//long handover_time=5000;


	/** Costructs a new SessionBorderController.
	  * @param sip_provider is the SIP transport provider
	  * @param server_profile the ServerProfile cointaining basic server confiuguration
	  * @param sbc_profile the SessionBorderControllerProfile cointaining specific SBC network configuration. */
	public SessionBorderController(ExtendedSipProvider sip_provider, PortPool portPool,ServerProfile server_profile, SessionBorderControllerProfile sbc_profile) {
		super(sip_provider,server_profile);

		this.sip_provider=sip_provider;
		this.sbc_profile=sbc_profile;
		
		if (sbc_profile.keepaliveTime>0 && !sbc_profile.keepaliveAggressive) keepalive_daemons=new Hashtable<>();
		if (sbc_profile.mediaAddr==null || sbc_profile.mediaAddr.equals("0.0.0.0")) sbc_profile.mediaAddr=sip_provider.getViaAddress();
		
		media_gw = new MediaGw(sip_provider.scheduler(), portPool, sbc_profile);

		// be sure to stay on route
		//server_profile.on_route=true;
	}

	/** When a new request message is received for a local user. */
	@Override
	public void processRequestToLocalUser(SipMessage msg) {
		LOG.debug("inside processRequestToLocalUser(msg)");
		msg=SipMangler.unmangleRequestLine(msg);
		if (isResponsibleFor(msg) && !SipMangler.isRequestLineMangled(msg)) super.processRequestToLocalUser(msg);
		else super.processRequestToRemoteUA(msg);
	}


	/** When a new request message is received for a remote UA */
	/*public void processRequestToRemoteUA(SipMessage msg) {
		printLog("inside processRequestToRemoteUA(msg)",LogWriter.LEVEL_MEDIUM);
		msg=SipMangler.unmangleRequestLine(msg);
		super.processRequestToRemoteUA(msg);
	}*/


	/** Processes the Proxy headers of the request.
	  * Such headers are: Via, Record-Route, Route, Max-Forwards, etc. */
	@Override
	protected SipMessage updateProxyingRequest(SipMessage req) {
		LOG.debug("inside updateProxyingRequest(req)");
		
		// before doing anything, force the use of a backend proxy
		if (sbc_profile.backendProxy!=null) {
			ViaHeader via=req.getViaHeader();
			SocketAddress via_soaddr=new SocketAddress(via.getHost(),(via.hasPort())?via.getPort():sip_provider.sipConfig().getDefaultPort());
			// pass to the backend_proxy only requests that are not coming from it
			if (!via_soaddr.equals(sbc_profile.backendProxy)) {
				Vector<Header> route_list;
				if (req.hasRouteHeader()) route_list=req.getRoutes().getHeaders();
				else route_list=new Vector<>();
				int index=0; 
				// skip the route for the present SBC
				if (route_list.size()>0) {
					GenericURI route=(new RouteHeader(route_list.elementAt(0))).getNameAddress().getAddress();
					if (route.isSipURI()) {
						SipURI sip_route=route.toSipURI(); 
						if (isResponsibleFor(sip_route.getHost(),sip_route.getPort())) index++;
					}
				}
				// check if the backend_proxy is already the next hop
				boolean already_on_route=false;
				if (route_list.size()>index) {
					GenericURI route=(new RouteHeader(route_list.elementAt(index))).getNameAddress().getAddress();
					if (route.isSipURI()) {
						SipURI sip_route=route.toSipURI();
						SocketAddress route_soaddr=new SocketAddress(sip_route.getHost(),(sip_route.hasPort())?sip_route.getPort():sip_provider.sipConfig().getDefaultPort());
						already_on_route=route_soaddr.equals(sbc_profile.backendProxy);
					}
				}
				// force the route via the backend_proxy
				if (!already_on_route) {
					SipURI bp_route=new SipURI(sbc_profile.backendProxy.getAddress().toString(),sbc_profile.backendProxy.getPort());
					bp_route.addLr();
					route_list.insertElementAt(new RouteHeader(new NameAddress(bp_route)),index);
					req.setRoutes(new MultipleHeader(route_list));
				}           
			}
		}
		// update the standard proxy headers
		req=super.updateProxyingRequest(req);
		// mangle the sdp
		if (req.hasBody()) req=mangleBody(req);
		// mangle the Contact header field
		if (req.hasContactHeader()) req=SipMangler.mangleContact(req,sip_provider.getViaAddress(),sip_provider.getPort());   
		return req;
	}


	/** Processes the Proxy headers of the response.
	  * Such headers are: Via, .. */
	@Override
	protected SipMessage updateProxyingResponse(SipMessage resp) {
		LOG.debug("inside updateProxyingResponse(resp)");
		resp=super.updateProxyingResponse(resp);
		// mangle the sdp
		if (resp.hasBody()) resp=mangleBody(resp);
		// mangle Contact header field
		if (resp.hasContactHeader()) {
			if (resp.getCSeqHeader().getMethod().equalsIgnoreCase(SipMethods.REGISTER)) resp=SipMangler.unmangleContact(resp);
			else resp=SipMangler.mangleContact(resp,sip_provider.getViaAddress(),sip_provider.getPort());
		}
		// update the SIP keep alive daemons
		if (keepalive_daemons!=null && resp.getCSeqHeader().getMethod().equalsIgnoreCase(SipMethods.REGISTER)) updateKeepAlive(resp);
		return resp;
	}


	/** Updates the registration of a local user.
	  * This method is called only if the SBC works also as Registrar. */
	@Override
	protected SipMessage updateRegistration(SipMessage msg) {
		SipMessage resp=super.updateRegistration(msg);
		// update the SIP keep alive daemons
		if (keepalive_daemons!=null) updateKeepAlive(resp);
		return resp;
	}


	//** Updates the SIP keep alive daemons. */
	private SipMessage updateKeepAlive(SipMessage resp) {
		if (resp.hasContactHeader()) {
			Vector<Header> c_headers=resp.getContacts().getHeaders();
			for (int i=0; i<c_headers.size(); i++) {
				ContactHeader ch=new ContactHeader(c_headers.elementAt(i));
				GenericURI uri=ch.getNameAddress().getAddress();
				if (!uri.isSipURI()) continue;
				// else
				SipURI sip_uri=uri.toSipURI();
				String host=sip_uri.getHost();
				int port=sip_uri.getPort();
				if (port<=0) port=sip_provider.sipConfig().getDefaultPort();
				SocketAddress soaddr=new SocketAddress(host,port);
				int time=ch.getExpires();
				if (time>0) {
					SipKeepAlive keepalive;
					String key=soaddr.toString();
					if (keepalive_daemons.containsKey(key)) {
						keepalive=keepalive_daemons.get(key);
						if (!keepalive.isRunning()) {
							keepalive_daemons.remove(key);
							keepalive=new SipKeepAlive(sip_provider,soaddr,sbc_profile.keepaliveTime);
							keepalive_daemons.put(key,keepalive);
							LOG.debug("KeepAlive: restart: {} ({}secs)", soaddr, time);
						}
						else LOG.debug("KeepAlive: update: {} ({}secs)", soaddr, time);
					}
					else {
						keepalive=new SipKeepAlive(sip_provider,soaddr,sbc_profile.keepaliveTime);
						keepalive_daemons.put(key,keepalive);
						LOG.debug("KeepAlive: start: {} ({}secs)", soaddr, time);
					}
					keepalive.setExpirationTime(((long)time)*1000);
				}
				else {
					String key=soaddr.toString();
					if (keepalive_daemons.containsKey(key)) {
						SipKeepAlive keepalive=keepalive_daemons.get(key);
						keepalive_daemons.remove(key);
						keepalive.halt();
						LOG.debug("KeepAlive: halt: {}", soaddr);
					}
				}
			}
		}
		return resp;
	}


	/** Mangles the body */
	private SipMessage mangleBody(SipMessage msg) {
		LOG.debug("inside mangleBody()");

		String content_type=msg.getContentTypeHeader().getContentType();
		if (content_type.equalsIgnoreCase("application/sdp")) {
			SdpMessage sdp=msg.getSdpBody();
			String dest_addr=sdp.getConnection().getAddress();
			// substitute 0.0.0.0 with 127.0.0.1
			if (dest_addr.equals("0.0.0.0")) dest_addr="127.0.0.1";

			// checking whether acts as media gw
			if (doRelay(dest_addr)) msg=media_gw.processSessionDescriptor(msg);
		}
		return msg;
	}


	/** Unangles the request-URI. */
	/*private SipMessage unmangleRequestUri(SipMessage msg) {
		LOG.debug("inside mangleRequestUri(msg)");
		String username=msg.getRequestLine().getAddress().getUserName();
		if (username!=null && username.startsWith(SipMangler.magic_cookie)) {
			msg=SipMangler.unmangleRequestLine(msg);
		}
		return msg;
	}*/


	/** Mangles the Contact header filed. */
	/*private SipMessage mangleContact(SipMessage msg) {
		LOG.debug("inside mangleContact()");
		msg=SipMangler.mangleContact(msg,sip_provider.getViaAddress(),sip_provider.getPort());   
		return msg;
	}*/


	/** Unangles the Contact header filed. */
	/*private SipMessage unmangleContact(SipMessage msg) {
		LOG.debug("inside mangleContact()");
		msg=SipMangler.unmangleContact(msg);   
		return msg;
	}*/


	/** Whether acts as relay system for that remote host. */
	protected boolean doRelay(String remote_addr) {
		return true;
	}


	/** Whether <i>addr</i> belongs to subnet <i>net</i>/<i>mask</i> */
	private static boolean addressBelongsTo(String addr, String net, String mask) {
		return addressToLong(net)==(addressToLong(mask)&addressToLong(addr));
	}


	/** Converts a String address into a long value (binary represtation of the address) */
	private static long addressToLong(String addr) {
		Parser par=new Parser(addr);
		char[] separators={ '.' };
		long n=0;
		n+=Integer.parseInt(par.getWord(separators)); par.skipChar();
		n<<=8;
		n+=Integer.parseInt(par.getWord(separators)); par.skipChar();
		n<<=8;
		n+=Integer.parseInt(par.getWord(separators)); par.skipChar();
		n<<=8;
		n+=Integer.parseInt(par.getWord(separators));
		return n;
	}

	// ****************************** MAIN *****************************

	/** The main method. */
	public static void main(String[] args) {
		SipConfig sipConfig = new SipConfig();
		SchedulerConfig schedulerConfig = new SchedulerConfig();
		SessionBorderControllerProfile sbc_profile=new SessionBorderControllerProfile();
		PortConfig portConfig = new PortConfig();
		ServerProfile server_profile=new ServerProfile();

		MetaConfig metaConfig = OptionParser.parseOptions(args, ".mjsip-sbc", sipConfig, schedulerConfig, sbc_profile, portConfig, server_profile);
		
		sipConfig.normalize();
		server_profile.normalize();
		
		// remove outbound proxy in case of the presence of a backend proxy
		if (sbc_profile.backendProxy!=null) {
			sipConfig.setOutboundProxy(null);
		}
		
		// create a new ExtendedSipProvider
		long keepalive_aggressive_time=(sbc_profile.keepaliveAggressive)? sbc_profile.keepaliveTime : 0;
		ExtendedSipProvider extended_provider=new ExtendedSipProvider(sipConfig, new ConfiguredScheduler(schedulerConfig), sbc_profile.bindingTimeout,keepalive_aggressive_time);

		// create and start the SBC
		new SessionBorderController(extended_provider, portConfig.createPool(), server_profile,sbc_profile);
	}
}
