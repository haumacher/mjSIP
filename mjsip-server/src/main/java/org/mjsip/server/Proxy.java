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

package org.mjsip.server;


import java.util.Vector;

import org.mjsip.config.MetaConfig;
import org.mjsip.config.OptionParser;
import org.mjsip.sip.address.GenericURI;
import org.mjsip.sip.address.NameAddress;
import org.mjsip.sip.address.SipURI;
import org.mjsip.sip.header.MaxForwardsHeader;
import org.mjsip.sip.header.MultipleHeader;
import org.mjsip.sip.header.RecordRouteHeader;
import org.mjsip.sip.header.RequestLine;
import org.mjsip.sip.header.RouteHeader;
import org.mjsip.sip.header.ViaHeader;
import org.mjsip.sip.message.SipMessage;
import org.mjsip.sip.message.SipResponses;
import org.mjsip.sip.provider.SipConfig;
import org.mjsip.sip.provider.SipProvider;
import org.mjsip.time.ConfiguredScheduler;
import org.mjsip.time.SchedulerConfig;
import org.slf4j.LoggerFactory;
import org.zoolu.util.Flags;


/** Class Proxy implement a Proxy SIP Server.
  * It extends class Registrar. A Proxy can work as simply SIP Proxy,
  * or it can handle calls for registered users. 
  */
public class Proxy extends Registrar {
	
	private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(Proxy.class);

	/** Costructs a void Proxy */
	protected Proxy() {}


	/** Costructs a new Proxy that acts also as location server for registered users. */
	public Proxy(SipProvider provider, ServerProfile server_profile) {
		super(provider,server_profile);
	}


	/** When a new request is received for the local server. */
	@Override
	public void processRequestToLocalServer(SipMessage msg) {
		LOG.debug("inside processRequestToLocalServer(msg)");
		if (msg.isRegister()) {
			super.processRequestToLocalServer(msg);
		}
		else
		if (!msg.isAck()) {
			// send a stateless error response
			//int result=501; // response code 501 ("Not Implemented")
			//int result=485; // response code 485 ("Ambiguous");
			SipMessage resp=sip_provider.messageFactory().createResponse(msg,SipResponses.ADDRESS_INCOMPLETE,null,null);
			sip_provider.sendMessage(resp);
		}
	}


	/** When a new request message is received for a local user */
	@Override
	public void processRequestToLocalUser(SipMessage msg) {
		LOG.debug("inside processRequestToLocalUser(msg)");

		// proxy authentication
		/*if (server_profile.do_proxy_authentication && !msg.isAck() && !msg.isCancel()) {
			SipMessage err_resp=as.authenticateProxyRequest(msg);  
			if (err_resp!=null) {
				sip_provider.sendMessage(err_resp);
				return;
			}
		}*/

		// message targets
		Vector<String> targets=getTargets(msg);
		
		if (targets.isEmpty()) {
			// prefix-based forwarding
			GenericURI request_uri=msg.getRequestLine().getAddress();
			SipURI new_target=null;
			if (isResponsibleFor(msg.getFromHeader().getNameAddress().getAddress())) new_target=getAuthPrefixBasedProxyingTarget(request_uri);
			if (new_target==null) new_target=getPrefixBasedProxyingTarget(request_uri);
			if (new_target!=null) targets.addElement(new_target.toString());
		}
		if (targets.isEmpty()) {
			LOG.info("No target found, message discarded");
			if (!msg.isAck()) sip_provider.sendMessage(sip_provider.messageFactory().createResponse(msg,SipResponses.NOT_FOUND,null,null));
			return;
		}           
		
		LOG.debug("message will be forwarded to {} user's contact(s)", targets.size()); 
		for (int i=0; i<targets.size(); i++)  {
			SipURI target_uri=SipURI.parseSipURI(targets.elementAt(i));
			SipMessage request=new SipMessage(msg);
			request.removeRequestLine();
			request.setRequestLine(new RequestLine(msg.getRequestLine().getMethod(),target_uri));
			
			updateProxyingRequest(request);
			sip_provider.sendMessage(request);
		}
	}

	
	/** When a new request message is received for a remote UA */
	@Override
	public void processRequestToRemoteUA(SipMessage msg) {
		LOG.debug("inside processRequestToRemoteUA(msg)");
	
		if (!server_profile.isOpenProxy) {
			// check whether the caller or callee is a local user 
			if (!isResponsibleFor(msg.getFromHeader().getNameAddress().getAddress()) && !isResponsibleFor(msg.getToHeader().getNameAddress().getAddress())) {
				LOG.info("both caller and callee are not registered with the local server: proxy denied.");
				sip_provider.sendMessage(sip_provider.messageFactory().createResponse(msg,SipResponses.SERVICE_UNAVAILABLE,null,null));
				return;
			}
		}
		
		// proxy authentication
		/*if (server_profile.do_proxy_authentication && !msg.isAck() && !msg.isCancel()) {
			SipMessage err_resp=as.authenticateProxyRequest(msg);  
			if (err_resp!=null) {
				sip_provider.sendMessage(err_resp);
				return;
			}
		}*/
		
		// domain-based forwarding
		RequestLine rl=msg.getRequestLine();
		GenericURI request_uri=rl.getAddress();
		SipURI nexthop=null;
		if (isResponsibleFor(msg.getFromHeader().getNameAddress().getAddress())) nexthop=getAuthDomainBasedProxyingTarget(request_uri);
		if (nexthop==null) nexthop=getDomainBasedProxyingTarget(request_uri);
		if (nexthop!=null) msg.setRequestLine(new RequestLine(rl.getMethod(),nexthop));
		
		updateProxyingRequest(msg); 
	  
		sip_provider.sendMessage(msg);
	}

	
	/** Processes the Proxy headers of the request.
	  * Such headers are: Via, Record-Route, Route, Max-Forwards, etc. */
	protected SipMessage updateProxyingRequest(SipMessage msg) {
		LOG.trace("inside updateProxyingRequest(msg)");

		// clear transport information
		msg.clearTransport();

		// remove Route if present
		//boolean is_on_route=false;  
		if (msg.hasRouteHeader()) {
			MultipleHeader mr=msg.getRoutes();
			GenericURI route=(new RouteHeader(mr.getTop())).getNameAddress().getAddress();
			if (route.isSipURI()) {
				SipURI sip_route=route.toSipURI();
				if (isResponsibleFor(sip_route.getHost(),sip_route.getPort())) {
					mr.removeTop();
					if (mr.size()>0) msg.setRoutes(mr);
					else msg.removeRoutes();
					//is_on_route=true;
				}
			}
		}
		// add Record-Route?
		if (server_profile.onRoute && msg.isInvite()/* && !is_on_route*/) {
			SipURI rr_uri;
			if (sip_provider.getPort()==sip_provider.sipConfig().getDefaultPort()) rr_uri=SipURI.parseSipURI(sip_provider.getViaAddress());
			else rr_uri=new SipURI(sip_provider.getViaAddress(),sip_provider.getPort());
			if (server_profile.looseRoute) rr_uri.addLr();
			RecordRouteHeader rrh=new RecordRouteHeader(new NameAddress(rr_uri));
			msg.addRecordRouteHeader(rrh);
		}
		// which protocol?
		String proto=null;
		if (msg.hasRouteHeader()) {
			GenericURI route=msg.getRouteHeader().getNameAddress().getAddress();
			if (route.isSipURI()) {
				SipURI sip_route=route.toSipURI();
				if (sip_route.hasTransport()) proto=sip_route.getTransport();
			}
		}
		else {
			GenericURI request_uri=msg.getRequestLine().getAddress();
			if (request_uri.isSipURI()) {
				SipURI request_sip_uri=request_uri.toSipURI();
				if (request_sip_uri.hasTransport()) proto=request_sip_uri.getTransport();
				else
				if (request_sip_uri.isSecure()) proto=SipProvider.PROTO_TLS;
			}
		}
		if (proto==null) proto=sip_provider.getDefaultTransport();
		
		// add Via
		ViaHeader via=new ViaHeader(proto,sip_provider.getViaAddress(),sip_provider.getPort());
		if (sip_provider.isRportSet()) via.setRport();
		String branch=sip_provider.pickBranch(msg);
		if (server_profile.loopDetection) {
			String loop_tag=msg.getHeader(Loop_Tag).getValue();
			if (loop_tag!=null) {
				msg.removeHeader(Loop_Tag);
				branch+=loop_tag;
			}
		}
		via.setBranch(branch);
		msg.addViaHeader(via);

		// decrement Max-Forwards
		MaxForwardsHeader maxfwd=msg.getMaxForwardsHeader();
		if (maxfwd!=null) maxfwd.decrement();
		else maxfwd=new MaxForwardsHeader(sip_provider.sipConfig().getMaxForwards());
		msg.setMaxForwardsHeader(maxfwd);      

		// check whether the next Route is formed according to RFC2543
		msg.rfc2543RouteAdapt();
				  
		return msg;                             
	}
	

	/** When a new response message is received */
	@Override
	public void processResponse(SipMessage resp) {
		LOG.debug("inside processResponse(msg)");
	
		updateProxyingResponse(resp);
		
		if (resp.hasViaHeader()) sip_provider.sendMessage(resp);
		else
			LOG.warn("no VIA header found: message discarded");            
	}
	
	
	/** Processes the Proxy headers of the response.
	  * Such headers are: Via, .. */
	protected SipMessage updateProxyingResponse(SipMessage resp) {
		LOG.debug("inside updateProxyingResponse(resp)");
		// clear transport information
		resp.clearTransport();
		// remove the top most via
		//ViaHeader vh=new ViaHeader((Header)resp.getVias().getHeaders().elementAt(0));
		//if (vh.getHost().equals(sip_provider.getViaAddress())) resp.removeViaHeader();
		// remove the top most via regardless the via has been insterted by this node or not (this prevents loops)
		resp.removeViaHeader();
		return resp;
	}
	

	/** Gets a new target according to the domain-based forwarding rules. */
	protected SipURI getAuthDomainBasedProxyingTarget(GenericURI request_uri) {
		LOG.trace("inside getAuthDomainBasedProxyingTarget(uri)");
		// authenticated rules
		for (int i=0; i<server_profile.authenticatedDomainProxyingRules.length; i++) {
			ProxyingRule rule=(ProxyingRule)server_profile.authenticatedDomainProxyingRules[i];
			SipURI nexthop=rule.getNexthop(request_uri);
			if (nexthop!=null) {
				LOG.debug("domain-based authenticated forwarding: {}; YES",rule);
				LOG.debug("target={}",nexthop);
				return nexthop;
			}
			else 
				LOG.debug("domain-based authenticated forwarding: {}:NO",rule);
		}
		return null;
	}


	/** Gets a new target according to the domain-based forwarding rules. */
	protected SipURI getDomainBasedProxyingTarget(GenericURI request_uri) {
		LOG.trace("inside getDomainBasedForwardingTarget(uri)");
		// non-authenticated rules
		for (int i=0; i<server_profile.domainProxyingRules.length; i++) {
			ProxyingRule rule=(ProxyingRule)server_profile.domainProxyingRules[i];
			SipURI nexthop=rule.getNexthop(request_uri);
			if (nexthop!=null) {
				LOG.debug("domain-based forwarding: {}: YES", rule);
				LOG.debug("target={}", nexthop);
				return nexthop;
			}
			else LOG.debug("domain-based forwarding: {}: NO", rule);
		}
		return null;
	}


	/** Gets a new target according to the authenticated prefix-based forwarding rules. */
	protected SipURI getAuthPrefixBasedProxyingTarget(GenericURI request_uri) {
		LOG.trace("inside getAuthPrefixBasedProxyingTarget(uri)");
		if (!request_uri.isSipURI())  return null;
		// else
		SipURI sip_uri=request_uri.toSipURI();
		String username=sip_uri.getUserName();
		if (username==null || !isPhoneNumber(username))  return null;
		// else
		// authenticated rules
		LOG.trace("authenticated prefix-based rules: {}", server_profile.authenticatedPhoneProxyingRules.length);
		for (int i=0; i<server_profile.authenticatedPhoneProxyingRules.length; i++) {
			ProxyingRule rule=(ProxyingRule)server_profile.authenticatedPhoneProxyingRules[i];
			SipURI nexthop=rule.getNexthop(request_uri);
			if (nexthop!=null) {
				LOG.debug("prefix-based authenticated forwarding: {}: YES", rule);
				LOG.debug("target={}", nexthop);
				return nexthop;
			}
			else LOG.debug("prefix-based authenticated forwarding: {}: NO", rule);
		}
		return null;
	}


	/** Gets a new target according to the prefix-based forwarding rules. */
	protected SipURI getPrefixBasedProxyingTarget(GenericURI request_uri) {
		LOG.trace("inside getPrefixBasedProxyingTarget(uri)");
		if (!request_uri.isSipURI())  return null;
		// else
		SipURI sip_uri=request_uri.toSipURI();
		String username=sip_uri.getUserName();
		if (username==null || !isPhoneNumber(username))  return null;
		// else
		// non-authenticated rules
		LOG.trace("prefix-based rules: {}", server_profile.phoneProxyingRules.length);
		for (int i=0; i<server_profile.phoneProxyingRules.length; i++) {
			ProxyingRule rule=(ProxyingRule)server_profile.phoneProxyingRules[i];
			SipURI nexthop=rule.getNexthop(request_uri);
			if (nexthop!=null) {
				LOG.debug("prefix-based forwarding: {}: YES", rule);
				LOG.debug("target= {}", nexthop.toString());
				return nexthop;
			}
			else LOG.debug("prefix-based forwarding: {}: NO", rule);
		}
		return null;
	}


	/** Whether the String is a phone number. */
	protected boolean isPhoneNumber(String str) {
		if (str==null || str.length()==0) return false;
		for (int i=0; i<str.length(); i++) {
			char c=str.charAt(i);
			if (c!='+' && c!='-' && c!='*' && c!='#' && (c<'0' || c>'9')) return false;
		}
		return true;
	}   


	// ****************************** MAIN *****************************

	/** The main method. */
	public static void main(String[] args) {
		Flags flags=new Flags(Proxy.class.getName(), args);
		
		SipConfig sipConfig = new SipConfig();
		SchedulerConfig schedulerConfig = new SchedulerConfig();
		ServerProfile server_profile=new ServerProfile();

		MetaConfig metaConfig = OptionParser.parseOptions(args, ".mjsip-proxy", sipConfig, schedulerConfig, server_profile);
		
		sipConfig.normalize();
		server_profile.normalize();
					
		SipProvider sip_provider=new SipProvider(sipConfig, new ConfiguredScheduler(schedulerConfig));

		new Proxy(sip_provider,server_profile);
	}
  
}
