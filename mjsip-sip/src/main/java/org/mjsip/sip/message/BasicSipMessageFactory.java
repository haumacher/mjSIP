/*
 * Copyright (C) 2012 Luca Veltri - University of Parma - Italy
 * 
 * This file is part of MjSip (http://www.mjsip.org)
 * 
 * MjSip is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * MjSip is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with MjSip; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 * 
 * Author(s):
 * Luca Veltri (luca.veltri@unipr.it)
 */

package org.mjsip.sip.message;



import java.util.Vector;

import org.mjsip.sip.address.GenericURI;
import org.mjsip.sip.address.NameAddress;
import org.mjsip.sip.dialog.DialogInfo;
import org.mjsip.sip.header.CSeqHeader;
import org.mjsip.sip.header.CallIdHeader;
import org.mjsip.sip.header.ContactHeader;
import org.mjsip.sip.header.ExpiresHeader;
import org.mjsip.sip.header.FromHeader;
import org.mjsip.sip.header.MaxForwardsHeader;
import org.mjsip.sip.header.MultipleHeader;
import org.mjsip.sip.header.RequestLine;
import org.mjsip.sip.header.ServerHeader;
import org.mjsip.sip.header.SipHeaders;
import org.mjsip.sip.header.StatusLine;
import org.mjsip.sip.header.ToHeader;
import org.mjsip.sip.header.UserAgentHeader;
import org.mjsip.sip.header.ViaHeader;
import org.mjsip.sip.provider.SipProvider;
import org.mjsip.sip.provider.SipStack;



/** BasicSipMessageFactory creates SIP requests and SIP responses
  * by means of two static methods: createRequest() and createResponse().
  * <br>
  * A valid SIP request sent by a UAC MUST, at least, contain
  * the following header fields: To, From, CSeq, Call-ID, Max-Forwards,
  * and Via; all of these header fields are mandatory in all SIP
  * requests.  These sip header fields are the fundamental building
  * blocks of a SIP message, as they jointly provide for most of the
  * critical message routing services including the addressing of
  * messages, the routing of responses, limiting message propagation,
  * ordering of messages, and the unique identification of transactions.
  * These header fields are in addition to the mandatory request line,
  * which contains the method, Request-URI, and SIP version.
  */
public abstract class BasicSipMessageFactory {
	
	/** Default (unknown) via address */
	static String DEFAULT_VIA_ADDRESS="0.0.0.0";

 
	/** Creates a SIP request message.
	  * @param method method name
	  * @param request_uri the request URI
	  * @param to name address in the <i>To</i> header field
	  * @param from name address in the <i>From</i> header field
	  * @param call_id the <i>Call-ID</i> value
	  * @param cseq the <i>CSeq</i> value
	  * @param local_tag tag in the <i>From</i> header field
	  * @param remote_tag tag in the <i>To</i> header field, or <i>null</i>, in case there is no tag
	  * @param contact contact address, that is the name address in the <i>Contact</i> header field, or null
	  * @param content_type content type, or <i>null</i> in case of no message body
	  * @param body message content (body), or <i>null</i>
	  * @return the new request message */
	public static SipMessage createRequest(String method, GenericURI request_uri, NameAddress to, NameAddress from/*, String proto, String via_addr, int host_port, boolean rport*/, String call_id, long cseq, String local_tag, String remote_tag/*, String branch*/, NameAddress contact, String content_type, byte[] body) {
		SipMessage req=new SipMessage();
		//mandatory headers first (To, From, Via, Max-Forwards, Call-ID, CSeq):
		req.setRequestLine(new RequestLine(method,request_uri));
		ViaHeader via=new ViaHeader(SipStack.default_transport_protocols[0],DEFAULT_VIA_ADDRESS,0);
		//if (rport) via.setRport();
		//if (branch==null) branch=SipProvider.pickBranch();
		String branch=SipProvider.pickBranch();
		via.setBranch(branch);
		req.addViaHeader(via);
		req.setMaxForwardsHeader(new MaxForwardsHeader(70));
		//if (remote_tag==null) req.setToHeader(new ToHeader(to)); else req.setToHeader(new ToHeader(to,remote_tag));
		req.setToHeader(new ToHeader(to,remote_tag));
		req.setFromHeader(new FromHeader(from,local_tag));
		req.setCallIdHeader(new CallIdHeader(call_id));
		req.setCSeqHeader(new CSeqHeader(cseq,method));
		//optional headers:
		if (contact!=null) req.addContactHeader(new ContactHeader(contact));
		req.setExpiresHeader(new ExpiresHeader(String.valueOf(SipStack.default_expires)));
		// add User-Agent header field
		if (SipStack.ua_info!=null) req.setUserAgentHeader(new UserAgentHeader(SipStack.ua_info));
		//if (body!=null) req.setBody(body); else req.setBody("");
		req.setBody(content_type,body);
		return req;
	}


	/** Creates a SIP request message.
	  * @see #createRequest(String,SipURI,NameAddress,NameAddress,NameAddress,String,String,int,String,long,String,String,String,String) */
	//public static SipMessage createRequest(String method, GenericURI request_uri, NameAddress to, NameAddress from, NameAddress contact, String call_id, long cseq, String local_tag, String remote_tag/*, String branch*/, String content_type, byte[] body)
	//{  //String via_addr=sip_provider.getViaAddress();
		//int host_port=sip_provider.getPort();
		//boolean rport=sip_provider.isRportSet();
		//String proto;
		//if (request_uri.isSecure()) proto=SipMessage.PROTO_TLS;
		//else if (request_uri.hasTransport()) proto=request_uri.getTransport();
		//else proto=sip_provider.getDefaultTransport();    
	//   return createRequest(method,request_uri,to,from,contact/*,proto,via_addr,host_port,rport*/,call_id,cseq,local_tag,remote_tag/*,branch*/,content_type,body);
	//}


	/** Creates a SIP request message.
	  * @param method method name
	  * @param request_uri the request URI
	  * @param to name address in the <i>To</i> header field
	  * @param from name address in the <i>From</i> header field
	  * @param contact contact address, that is the name address in the <i>Contact</i> header field, or <i>null</i>
	  * @param content_type content type, or <i>null</i> in case of no message body
	  * @param body message content (body), or <i>null</i>
	  * @return the new request message */
	public static SipMessage createRequest(/*SipProvider sip_provider, */String method, GenericURI request_uri, NameAddress to, NameAddress from, String call_id, NameAddress contact, String content_type, byte[] body) {
		//SipURI request_uri=to.getAddress();
		//String call_id=sip_provider.pickCallId();
		int cseq=SipProvider.pickInitialCSeq();
		String local_tag=SipProvider.pickTag();
		//String branch=SipStack.pickBranch();
		return createRequest(/*sip_provider,*/method,request_uri,to,from,call_id,cseq,local_tag,null/*,null*/,contact,content_type,body);
	}


	/** Creates a SIP request message.
	  * @see #createRequest(SipProvider,String,NameAddress,NameAddress,NameAddress,String) */
	/*public static SipMessage createRequest(SipProvider sip_provider, String method, NameAddress to, NameAddress from, String content_type, byte[] body) {
		String contact_user=from.getAddress().getUserName();
		NameAddress contact=new NameAddress(new SipURI(contact_user,sip_provider.getViaAddress(),sip_provider.getPort()));
		return createRequest(method,to.getAddress(),to,from,contact,sip_provider.pickCallId(),content_type,body);
	}*/


	/** Creates a SIP request message within a dialog.
	  * @param dialog the current dialog
	  * @param method method name
	  * @param content_type content type, or <i>null</i> in case of no message body
	  * @param body message content (body), or <i>null</i>
	  * @return the new request message */
	public static SipMessage createRequest(/*SipProvider sip_provider, */DialogInfo dialog, String method, String content_type, byte[] body) {
		NameAddress to=dialog.getRemoteName();
		NameAddress from=dialog.getLocalName();
		NameAddress target=dialog.getRemoteContact();
		if (target==null) target=to;
		GenericURI request_uri=target.getAddress();
		if (request_uri==null) request_uri=dialog.getRemoteName().getAddress();
		//SipProvider sip_provider=dialog.getSipProvider();
		//String via_addr=sip_provider.getViaAddress();
		//int host_port=sip_provider.getPort();
		//boolean rport=sip_provider.isRportSet();
		//String proto;
		//if (dialog.isSecure()) proto=SipMessage.PROTO_TLS;
		//else if (target.getAddress().isSecure()) proto=SipMessage.PROTO_TLS;
		//else if (target.getAddress().hasTransport()) proto=target.getAddress().getTransport();
		//else proto=sip_provider.getDefaultTransport();    
		NameAddress contact=dialog.getLocalContact();
		//if (contact==null) contact=from;
		//if (contact==null) contact=new NameAddress(new SipURI(sip_provider.getViaAddress(),sip_provider.getPort()));
		// increment the CSeq, if method is not ACK nor CANCEL
		if (!SipMethods.isAck(method) && !SipMethods.isCancel(method)) dialog.incLocalCSeq();
		String call_id=dialog.getCallID();
		long cseq=dialog.getLocalCSeq();
		String local_tag=dialog.getLocalTag();
		String remote_tag=dialog.getRemoteTag();
		//String branch=SipStack.pickBranch();
		SipMessage req=createRequest(method,request_uri,to,from/*,proto,via_addr,host_port,rport*/,call_id,cseq,local_tag,remote_tag/*,null*/,contact,content_type,body);
		Vector route=dialog.getRoute();
		if (route!=null && route.size()>0) req.addRoutes(new MultipleHeader(SipHeaders.Route,route));
		req.rfc2543RouteAdapt();
		return req;
	}


	/** Creates a SIP request message within a dialog.
	  * @param dialog the current dialog
	  * @param method method name
	  * @param content_type content type, or <i>null</i> in case of no message body
	  * @param body message content (body), or <i>null</i>
	  * @return the new request message */
	//public static SipMessage createRequest(Dialog dialog, String method, String content_type, byte[] body)
	//{  return createRequest(dialog.getSipProvider(),dialog,method,content_type,body);
	//}


	/** Creates a SIP response message.
	  * @param req the request message
	  * @param code the response code
	  * @param reason the response reason
	  * @param local_tag tag in the <i>To</i> header field, or <i>null</i>
	  * @param contact contact address, that is the name address in the <i>Contact</i> header field, or <i>null</i>
	  * @param content_type content type, or <i>null</i> in case of no message body
	  * @param body message content (body), or <i>null</i>
	  * @return the new response message */
	public static SipMessage createResponse(SipMessage req, int code, String reason, String local_tag, NameAddress contact, String content_type, byte[] body) {
		SipMessage resp=new SipMessage();
		if (reason==null) reason=SipResponses.reasonOf(code);
		resp.setStatusLine(new StatusLine(code,reason));
		resp.setVias(req.getVias());
		if (code>=180 && code<300 && req.hasRecordRouteHeader()) resp.setRecordRoutes(req.getRecordRoutes());
		ToHeader toh=req.getToHeader();
		if (local_tag!=null) toh.setTag(local_tag);
		resp.setToHeader(toh);
		resp.setFromHeader(req.getFromHeader());
		resp.setCallIdHeader(req.getCallIdHeader());
		resp.setCSeqHeader(req.getCSeqHeader());
		if (contact!=null) resp.setContactHeader(new ContactHeader(contact));
		// add Server header field
		if (SipStack.server_info!=null) resp.setServerHeader(new ServerHeader(SipStack.server_info));
		//if (body!=null) resp.setBody(body); else resp.setBody("");
		resp.setBody(content_type,body);
		return resp;
	}


	/** Creates a SIP response message.
	  * For 2xx responses the local tag is created by using the {@link SipProvider#pickTag(SipMessage req)} method.
	  * @param req the request message
	  * @param code the response code
	  * @param reason the response reason
	  * @param contact contact address, that is the name address in the <i>Contact</i> header field, or <i>null</i>
	  * @return the new response message */
	public static SipMessage createResponse(SipMessage req, int code, String reason, NameAddress contact) {
		//String reason=SipResponses.reasonOf(code);
		String localtag=null;
		if (req.createsDialog() && !req.getToHeader().hasTag()) {
			if ((SipStack.early_dialog && code!=100) || (code>=200 && code<300)) localtag=SipProvider.pickTag(req);
		}
		return createResponse(req,code,reason,localtag,contact,null,null);
	}

}  
