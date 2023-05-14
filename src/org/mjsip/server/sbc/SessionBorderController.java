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

import org.mjsip.sdp.SdpMessage;
import org.mjsip.server.Proxy;
import org.mjsip.server.ServerProfile;
import org.mjsip.server.StatefulProxy;
import org.mjsip.sip.address.GenericURI;
import org.mjsip.sip.address.NameAddress;
import org.mjsip.sip.address.SipURI;
import org.mjsip.sip.header.ContactHeader;
import org.mjsip.sip.header.Header;
import org.mjsip.sip.header.MultipleHeader;
import org.mjsip.sip.header.RouteHeader;
import org.mjsip.sip.header.ViaHeader;
import org.mjsip.sip.message.SipMessage;
import org.mjsip.sip.message.SipMethods;
import org.mjsip.sip.provider.SipKeepAlive;
import org.mjsip.sip.provider.SipStack;
import org.zoolu.net.SocketAddress;
import org.zoolu.util.ExceptionPrinter;
import org.zoolu.util.Flags;
import org.zoolu.util.LogLevel;
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
	Hashtable keepalive_daemons=null;

	/** Maximum time between two handovers (in milliseconds). */
	//long handover_time=5000;


	/** Costructs a new SessionBorderController.
	  * @param sip_provider is the SIP transport provider
	  * @param server_profile the ServerProfile cointaining basic server confiuguration
	  * @param sbc_profile the SessionBorderControllerProfile cointaining specific SBC network configuration. */
	public SessionBorderController(ExtendedSipProvider sip_provider, ServerProfile server_profile, SessionBorderControllerProfile sbc_profile) {
		super(sip_provider,server_profile);
		init(sip_provider,server_profile,sbc_profile);
	}


	/** Inits the SessionBorderController. */
	private void init(ExtendedSipProvider sip_provider, ServerProfile server_profile, SessionBorderControllerProfile sbc_profile) {
		// init
		this.sip_provider=sip_provider;
		this.sbc_profile=sbc_profile;
		
		// remove outbound proxy in case of the presence of a backend proxy
		if (sbc_profile.backend_proxy!=null) sip_provider.setOutboundProxy(null);

		if (sbc_profile.keepalive_time>0 && !sbc_profile.keepalive_aggressive) keepalive_daemons=new Hashtable();
		if (sbc_profile.media_addr==null || sbc_profile.media_addr.equals("0.0.0.0")) sbc_profile.media_addr=sip_provider.getViaAddress();
		
		media_gw=new MediaGw(sbc_profile,logger);

		// be sure to stay on route
		//server_profile.on_route=true;

		log(LogLevel.INFO,"Available media ports: ["+sbc_profile.media_ports.elementAt(0)+":"+sbc_profile.media_ports.elementAt(sbc_profile.media_ports.size()-1)+"] ("+sbc_profile.media_ports.size()+")");
	}


	/** When a new request message is received for a local user. */
	public void processRequestToLocalUser(SipMessage msg) {
		log(LogLevel.DEBUG,"inside processRequestToLocalUser(msg)");
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
	protected SipMessage updateProxyingRequest(SipMessage req) {
		log(LogLevel.DEBUG,"inside updateProxyingRequest(req)");
		
		// before doing anything, force the use of a backend proxy
		if (sbc_profile.backend_proxy!=null) {
			ViaHeader via=req.getViaHeader();
			SocketAddress via_soaddr=new SocketAddress(via.getHost(),(via.hasPort())?via.getPort():SipStack.default_port);
			// pass to the backend_proxy only requests that are not coming from it
			if (!via_soaddr.equals(sbc_profile.backend_proxy)) {
				Vector route_list;
				if (req.hasRouteHeader()) route_list=req.getRoutes().getHeaders();
				else route_list=new Vector();
				int index=0; 
				// skip the route for the present SBC
				if (route_list.size()>0) {
					GenericURI route=(new RouteHeader((Header)route_list.elementAt(0))).getNameAddress().getAddress();
					if (route.isSipURI()) {
						SipURI sip_route=new SipURI(route); 
						if (isResponsibleFor(sip_route.getHost(),sip_route.getPort())) index++;
					}
				}
				// check if the backend_proxy is already the next hop
				boolean already_on_route=false;
				if (route_list.size()>index) {
					GenericURI route=(new RouteHeader((Header)route_list.elementAt(index))).getNameAddress().getAddress();
					if (route.isSipURI()) {
						SipURI sip_route=new SipURI(route);
						SocketAddress route_soaddr=new SocketAddress(sip_route.getHost(),(sip_route.hasPort())?sip_route.getPort():SipStack.default_port);
						already_on_route=route_soaddr.equals(sbc_profile.backend_proxy);
					}
				}
				// force the route via the backend_proxy
				if (!already_on_route) {
					SipURI bp_route=new SipURI(sbc_profile.backend_proxy.getAddress().toString(),sbc_profile.backend_proxy.getPort());
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
	protected SipMessage updateProxyingResponse(SipMessage resp) {
		log(LogLevel.DEBUG,"inside updateProxyingResponse(resp)");
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
	protected SipMessage updateRegistration(SipMessage msg) {
		SipMessage resp=super.updateRegistration(msg);
		// update the SIP keep alive daemons
		if (keepalive_daemons!=null) updateKeepAlive(resp);
		return resp;
	}


	//** Updates the SIP keep alive daemons. */
	private SipMessage updateKeepAlive(SipMessage resp) {
		if (resp.hasContactHeader()) {
			Vector c_headers=resp.getContacts().getHeaders();
			for (int i=0; i<c_headers.size(); i++) {
				ContactHeader ch=new ContactHeader((Header)c_headers.elementAt(i));
				GenericURI uri=ch.getNameAddress().getAddress();
				if (!uri.isSipURI()) continue;
				// else
				SipURI sip_uri=new SipURI(uri);
				String host=sip_uri.getHost();
				int port=sip_uri.getPort();
				if (port<=0) port=SipStack.default_port;
				SocketAddress soaddr=new SocketAddress(host,port);
				int time=ch.getExpires();
				if (time>0) {
					SipKeepAlive keepalive;
					String key=soaddr.toString();
					if (keepalive_daemons.containsKey(key)) {
						keepalive=(SipKeepAlive)keepalive_daemons.get(key);
						if (!keepalive.isRunning()) {
							keepalive_daemons.remove(key);
							keepalive=new SipKeepAlive(sip_provider,soaddr,sbc_profile.keepalive_time);
							keepalive_daemons.put(key,keepalive);
							log(LogLevel.DEBUG,"KeepAlive: restart: "+soaddr+" ("+time+"secs)");
						}
						else log(LogLevel.DEBUG,"KeepAlive: update: "+soaddr+" ("+time+"secs)");
					}
					else {
						keepalive=new SipKeepAlive(sip_provider,soaddr,sbc_profile.keepalive_time);
						keepalive_daemons.put(key,keepalive);
						log(LogLevel.DEBUG,"KeepAlive: start: "+soaddr+" ("+time+"secs)");
					}
					keepalive.setExpirationTime(((long)time)*1000);
				}
				else {
					String key=soaddr.toString();
					if (keepalive_daemons.containsKey(key)) {
						SipKeepAlive keepalive=(SipKeepAlive)keepalive_daemons.get(key);
						keepalive_daemons.remove(key);
						keepalive.halt();
						log(LogLevel.DEBUG,"KeepAlive: halt: "+soaddr);
					}
				}
			}
		}
		return resp;
	}


	/** Mangles the body */
	private SipMessage mangleBody(SipMessage msg) {
		log(LogLevel.DEBUG,"inside mangleBody()");

		String content_type=msg.getContentTypeHeader().getContentType();
		if (content_type.equalsIgnoreCase("application/sdp")) {
			SdpMessage sdp=new SdpMessage(msg.getStringBody());
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
		log(LogLevel.DEBUG,"inside mangleRequestUri(msg)");
		String username=msg.getRequestLine().getAddress().getUserName();
		if (username!=null && username.startsWith(SipMangler.magic_cookie)) {
			msg=SipMangler.unmangleRequestLine(msg);
		}
		return msg;
	}*/


	/** Mangles the Contact header filed. */
	/*private SipMessage mangleContact(SipMessage msg) {
		log(LogLevel.DEBUG,"inside mangleContact()");
		msg=SipMangler.mangleContact(msg,sip_provider.getViaAddress(),sip_provider.getPort());   
		return msg;
	}*/


	/** Unangles the Contact header filed. */
	/*private SipMessage unmangleContact(SipMessage msg) {
		log(LogLevel.DEBUG,"inside mangleContact()");
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


	// ****************************** Logs *****************************

	/** Adds a new string to the default Log. */
	private void log(LogLevel level, String str) {
		if (logger!=null) logger.log(level,"SBC: "+str);
	}

	/** Adds the Exception message to the default Log. */
	private void log(LogLevel level, Exception e) {
		log(level,"Exception: "+ExceptionPrinter.getStackTraceOf(e));
	}


	// ****************************** MAIN *****************************

	/** The main method. */
	public static void main(String[] args) {
		
		Flags flags=new Flags(args);
		boolean help=flags.getBoolean("--prompt","prompt for exit");
		boolean memory_debugging=flags.getBoolean("-d",null);
		String file=flags.getString("-f","<file>",null,"loads configuration from the given file");
		String[] ports=flags.getStringTuple("-m",2,"<fist_port> <last_port>",null,"interval of media ports");
		int first_port=ports!=null? Integer.parseInt(ports[0]) : 0;
		int last_port=ports!=null? Integer.parseInt(ports[1]) : 0;
		
		if (help) {
			System.out.println(flags.toUsageString(SessionBorderController.class.getName()));
			return;
		}

		SipStack.init(file);
		ServerProfile server_profile=new ServerProfile(file);
		SessionBorderControllerProfile sbc_profile=new SessionBorderControllerProfile(file);

		if (first_port>0 && last_port>=first_port) {
			Vector media_ports=new Vector();
			for (int i=first_port; i<=last_port; i+=2) media_ports.addElement(new Integer(i));
			sbc_profile.media_ports=media_ports;
		}
		// create a new ExtendedSipProvider
		long keepalive_aggressive_time=(sbc_profile.keepalive_aggressive)? sbc_profile.keepalive_time : 0;
		ExtendedSipProvider extended_provider=new ExtendedSipProvider(file,sbc_profile.binding_timeout,keepalive_aggressive_time);

		// create and start the SBC
		new SessionBorderController(extended_provider,server_profile,sbc_profile);
	}
}
