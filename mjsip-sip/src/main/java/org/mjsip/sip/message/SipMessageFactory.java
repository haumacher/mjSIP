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
import org.mjsip.sip.address.SipURI;
import org.mjsip.sip.address.UnexpectedUriSchemeException;
import org.mjsip.sip.dialog.Dialog;
import org.mjsip.sip.dialog.DialogInfo;
import org.mjsip.sip.header.CSeqHeader;
import org.mjsip.sip.header.ContactHeader;
import org.mjsip.sip.header.EventHeader;
import org.mjsip.sip.header.ExpiresHeader;
import org.mjsip.sip.header.FromHeader;
import org.mjsip.sip.header.Header;
import org.mjsip.sip.header.MultipleHeader;
import org.mjsip.sip.header.RAckHeader;
import org.mjsip.sip.header.RecordRouteHeader;
import org.mjsip.sip.header.ReferToHeader;
import org.mjsip.sip.header.ReferredByHeader;
import org.mjsip.sip.header.SipHeaders;
import org.mjsip.sip.header.SubjectHeader;
import org.mjsip.sip.header.ToHeader;
import org.mjsip.sip.header.ViaHeader;
import org.mjsip.sip.provider.SipProvider;
import org.mjsip.sip.provider.SipStack;



/** Class SipMessageFactory extends class BasicSipMessageFactory.
  * <br>
  * SipMessageFactory can be used to create both method-independent
  * and method-specific SIP requests and SIP responses.
  * <p>
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
public class SipMessageFactory extends BasicSipMessageFactory {
	

	//*************************** Basic (RFC 3261) ****************************

	/** Creates a new INVITE request out of any pre-existing dialogs.
	  * @param request_uri the request URI
	  * @param to name address in the <i>To</i> header field
	  * @param from name address in the <i>From</i> header field
	  * @param call_id the <i>Call-ID</i> value
	  * @param contact contact address, that is the name address in the <i>Contact</i> header field, or <i>null</i>
	  * @param content_type content type, or <i>null</i> in case of no message body
	  * @param body message content (body), or <i>null</i>
	  * @return the new INVITE message */
	public static SipMessage createInviteRequest(/*SipProvider sip_provider, */GenericURI request_uri, NameAddress to, NameAddress from, NameAddress contact, String call_id, String content_type, byte[] body) {
		//String call_id=sip_provider.pickCallId();
		int cseq=SipProvider.pickInitialCSeq();
		String local_tag=SipProvider.pickTag();
		//String branch=SipStack.pickBranch();
		//if (contact==null) contact=from;
		//if (contact==null) contact=new NameAddress(new SipURI(sip_provider.getViaAddress(),sip_provider.getPort()));
		return createRequest(/*sip_provider,*/SipMethods.INVITE,request_uri,to,from,call_id,cseq,local_tag,null,contact,content_type,body);
	}


	/** Creates a new INVITE request within a dialog (re-invite). 
	  * @param dialog the current dialog
	  * @param content_type content type, or <i>null</i> in case of no message body
	  * @param body message content (body), or <i>null</i>
	  * @return the new INVITE message */
	public static SipMessage createInviteRequest(Dialog dialog, String content_type, byte[] body) {
		return createRequest(dialog,SipMethods.INVITE,content_type,body);
	}


	/** Creates an ACK request for a 2xx response.
	  * @see #createRequest(Dialog,String,String) */
	//public static SipMessage create2xxAckRequest(Dialog dialog, String content_type, byte[] body)
	//{  return createRequest(dialog,SipMethods.ACK,content_type,body);
	//}


	/** Creates an ACK request for a 2xx response.
	  * @param dialog the current dialog
	  * @param resp the response message to be confirmed
	  * @param content_type content type, or <i>null</i> in case of no message body
	  * @param body message content (body), or <i>null</i>
	  * @return the new ACK message */
	public static SipMessage create2xxAckRequest(/*SipProvider sip_provider, */DialogInfo dialog, SipMessage resp, String content_type, byte[] body) {
		SipMessage ack=createRequest(/*sip_provider,*/dialog,SipMethods.ACK,content_type,body);
		ack.setCSeqHeader(new CSeqHeader(resp.getCSeqHeader().getSequenceNumber(),SipMethods.ACK));
		return ack;
	}


	/** Creates an ACK request for a 2xx response.
	  * @param method the initial request message, accepted by the 2xx response
	  * @param resp the response message to be confirmed
	  * @param content_type content type, or <i>null</i> in case of no message body
	  * @param body message content (body), or <i>null</i>
	  * @return the new ACK message */
	public static SipMessage create2xxAckRequest(/*SipProvider sip_provider, */SipMessage method, SipMessage resp, String content_type, byte[] body) {
		GenericURI request_uri=(resp.hasContactHeader())? resp.getContactHeader().getNameAddress().getAddress() : method.getRequestLine().getAddress();
		FromHeader from=method.getFromHeader();
		ToHeader to=resp.getToHeader();
		//String via_addr=sip_provider.getViaAddress();
		//int host_port=sip_provider.getPort();
		//boolean rport=sip_provider.isRportSet();
		//String proto=(request_uri.isSecure())? SipMessage.PROTO_TLS : ((request_uri.hasTransport())? request_uri.getTransport() : sip_provider.getDefaultTransport());    
		String call_id=method.getCallIdHeader().getCallId();
		long cseq=method.getCSeqHeader().getSequenceNumber();
		//NameAddress contact_naddr=(method.hasContactHeader())? method.getContactHeader().getNameAddress() : new NameAddress(new SipURI(sip_provider.getViaAddress(),sip_provider.getPort()));   
		NameAddress contact_naddr=(method.hasContactHeader())? method.getContactHeader().getNameAddress() : null;
		SipMessage ack=createRequest(SipMethods.ACK,request_uri,to.getNameAddress(),from.getNameAddress()/*,proto,via_addr,host_port,rport*/,call_id,cseq,from.getTag(),to.getTag(),contact_naddr,null,null);
		if (resp.hasRecordRouteHeader()) {
			Vector rr=resp.getRecordRoutes().getHeaders();
			int size=rr.size();
			Vector route=new Vector();
			for (int i=0; i<size; i++) route.insertElementAt((new RecordRouteHeader((Header)rr.elementAt(size-1-i))).getNameAddress(),i);
			ack.addRoutes(new MultipleHeader(SipHeaders.Route,route));
		}
		else
		if (method.hasRouteHeader()) ack.setRoutes(method.getRoutes());
		ack.rfc2543RouteAdapt();
		return ack;
	}


	/** Creates an ACK request for a non-2xx response.
	  * @param method the initial request message, refused  by the non-2xx response
	  * @param resp the response message to be confirmed
	  * @return the new ACK message */
	public static SipMessage createNon2xxAckRequest(/*SipProvider sip_provider, */SipMessage method, SipMessage resp) {
		GenericURI request_uri=method.getRequestLine().getAddress();
		FromHeader from=method.getFromHeader();
		ToHeader to=resp.getToHeader();
		//String via_addr=sip_provider.getViaAddress();
		//int host_port=sip_provider.getPort();
		//boolean rport=sip_provider.isRportSet();
		//String proto;
		//if (request_uri.isSecure()) proto=SipMessage.PROTO_TLS;
		//else if (request_uri.hasTransport()) proto=request_uri.getTransport();
		//else proto=sip_provider.getDefaultTransport();    
		String call_id=method.getCallIdHeader().getCallId();
		long cseq=method.getCSeqHeader().getSequenceNumber();
		NameAddress contact=null;
		SipMessage ack=createRequest(SipMethods.ACK,request_uri,to.getNameAddress(),from.getNameAddress()/*,proto,via_addr,host_port,rport*/,call_id,cseq,from.getParameter("tag"),to.getParameter("tag"),contact,null,null);
		ack.removeExpiresHeader();
		// via
		if (ack.hasViaHeader()) ack.removeViaHeader();
		ViaHeader via=resp.getViaHeader();
		String branch=via.getBranch();
		via=new ViaHeader(via.getProtocol(),via.getHost(),via.getPort());
		via.setBranch(branch);
		ack.addViaHeader(via);
		// route
		if (method.hasRouteHeader()) ack.setRoutes(method.getRoutes());
		return ack;
	}


	/** Creates an ACK request for a non-2xx response.
	  * @param dialog the current dialog
	  * @param resp the response message to be confirmed
	  * @return the new ACK message */
	public static SipMessage createNon2xxAckRequest(DialogInfo dialog, SipMessage resp) {
		FromHeader from=resp.getFromHeader();
		ToHeader to=resp.getToHeader();
		GenericURI request_uri=to.getNameAddress().getAddress();
		String call_id=dialog.getCallID();
		//long cseq=dialog.getLocalCSeq();
		long cseq=resp.getCSeqHeader().getSequenceNumber();
		NameAddress contact=null;
		SipMessage ack=createRequest(SipMethods.ACK,request_uri,to.getNameAddress(),from.getNameAddress()/*,proto,via_addr,host_port,rport*/,call_id,cseq,from.getParameter("tag"),to.getParameter("tag"),contact,null,null);
		ack.removeExpiresHeader();
		// via
		ViaHeader via=resp.getViaHeader();
		String branch=via.getBranch();
		via=new ViaHeader(via.getProtocol(),via.getHost(),via.getPort());
		via.setBranch(branch);
		ack.addViaHeader(via);
		// route
		Vector route=dialog.getRoute();
		if (route!=null && route.size()>0) ack.addRoutes(new MultipleHeader(SipHeaders.Route,route));
		ack.rfc2543RouteAdapt();
		return ack;
	}


	/** Creates a CANCEL request.
	  * @param req the request message that is going to be cancelled
	  * @return the new CANCEL message */
	public static SipMessage createCancelRequest(SipMessage req) {
		ToHeader to=req.getToHeader();
		FromHeader from=req.getFromHeader();
		GenericURI request_uri=req.getRequestLine().getAddress();
		NameAddress contact=req.getContactHeader().getNameAddress();
		//ViaHeader via=req.getViaHeader();
		//String host_addr=via.getHost();
		//int host_port=via.getPort();
		//boolean rport=via.hasRport();
		//String proto=via.getProtocol();
		//return createRequest(SipMethods.CANCEL,request_uri,to.getNameAddress(),from.getNameAddress(),contact,proto,host_addr,host_port,rport,req.getCallIdHeader().getCallId(),req.getCSeqHeader().getSequenceNumber(),from.getParameter("tag"),to.getParameter("tag"),branch,"");
		SipMessage cancel=createRequest(SipMethods.CANCEL,request_uri,to.getNameAddress(),from.getNameAddress()/*,proto,host_addr,host_port,rport*/,req.getCallIdHeader().getCallId(),req.getCSeqHeader().getSequenceNumber(),from.getParameter("tag"),to.getParameter("tag"),contact,null,null);
		// via
		cancel.addViaHeader(req.getViaHeader());
		// authorization
		if (req.hasAuthorizationHeader()) cancel.setAuthorizationHeader(req.getAuthorizationHeader());
		if (req.hasProxyAuthorizationHeader()) cancel.setProxyAuthorizationHeader(req.getProxyAuthorizationHeader());
		return cancel;
	}


	/** Creates a BYE request. */
	//public static SipMessage createByeRequest(Dialog dialog)
	//{  return createByeRequest(dialog.getSipProvider(),dialog);
	//}


	/** Creates a BYE request.
	  * @param dialog dialog to be closed
	  * @return the new BYE message */
	public static SipMessage createByeRequest(/*SipProvider sip_provider, */DialogInfo dialog) {
		SipMessage msg=createRequest(/*sip_provider,*/dialog,SipMethods.BYE,null,null);
		msg.removeExpiresHeader();
		msg.removeContacts();
		return msg;
	}


	/** Creates a new REGISTER request.
	  * @param registrar the address of the registrar server
	  * @param to name address in the <i>To</i> header field
	  * @param from name address in the <i>From</i> header field
	  * @param contact contact address, that is the name address in the <i>Contact</i> header field, or <i>null</i>
	  * @param call_id the <i>Call-ID</i> value
	  * @return the new REGISTER message */
	public static SipMessage createRegisterRequest(/*SipProvider sip_provider, */GenericURI registrar, NameAddress to, NameAddress from, NameAddress contact, String call_id) {
		if (registrar==null) {
			GenericURI to_uri=to.getAddress();
			if (to_uri.isSipURI()) {
				SipURI sip_uri=new SipURI(to_uri);
				registrar=new SipURI(sip_uri.getHost(),sip_uri.getPort());
			}
			else throw new UnexpectedUriSchemeException(to_uri.getScheme());
		}
		//String via_addr=sip_provider.getViaAddress();
		//int host_port=sip_provider.getPort();
		//boolean rport=sip_provider.isRportSet();
		//String proto;
		//if (to_url.isSecure()) proto=SipMessage.PROTO_TLS;
		//else if (to_url.hasTransport()) proto=to_url.getTransport();
		//else proto=sip_provider.getDefaultTransport();    
		//String call_id=sip_provider.pickCallId();
		int cseq=SipProvider.pickInitialCSeq();
		String local_tag=SipProvider.pickTag();
		//String branch=SipStack.pickBranch();
		SipMessage req=createRequest(SipMethods.REGISTER,registrar,to,from/*,proto,via_addr,host_port,rport*/,call_id,cseq,local_tag,null,contact,null,null);
		// if no contact, deregister all
		if (contact==null) {
			ContactHeader star=new ContactHeader(); // contact is *
			req.setContactHeader(star);
			req.setExpiresHeader(new ExpiresHeader(String.valueOf(SipStack.default_expires)));
		}
		return req;
	}


	//################ Can be removed? ################
	/** Creates a new REGISTER request.
	  * <p> If contact is null, set contact as star * (register all) */
	/*public static SipMessage createRegisterRequest(SipProvider sip_provider, NameAddress to, NameAddress contact) {
		return createRegisterRequest(sip_provider,to,to,contact);
	}*/


	//****************************** Extensions *******************************

	/** Creates a new PRACK request (RFC3262).
	  * @param dialog the current dialog
	  * @param resp_1xx the reliable 1xx response the has to be confirmed by the PRACK
	  * @param content_type the type of the content to be included within the PRACK (or <i>null</i> in case of no message body)
	  * @param body the message body to be included within the PRACK, or <i>null</i>. In an offer/answer model, this body represents the answer to the offer contained in the 1xx response message
	  * @return the new PRACK message */
	public static SipMessage createPrackRequest(Dialog dialog, SipMessage resp_1xx, String content_type, byte[] body) {
		SipMessage prack=SipMessageFactory.createRequest(dialog,SipMethods.PRACK,content_type,body);
		CSeqHeader csh=resp_1xx.getCSeqHeader();
		long rseq=resp_1xx.getRSeqHeader().getSequenceNumber();
		prack.setRAckHeader(new RAckHeader(rseq,csh.getSequenceNumber(),csh.getMethod()));
		return prack;
	}

	/** Creates a new MESSAGE request (RFC3428).
	  * @param recipient the recipient address
	  * @param from name address in the <i>From</i> header field
	  * @param call_id the <i>Call-ID</i> value
	  * @param subject the subject of the message (for the <i>Subject</i> header field), or <i>null</i>
	  * @param content_type content type, or <i>null</i> in case of no message body
	  * @param body message content (body), or <i>null</i>
	  * @return the new MESSAGE message */
	public static SipMessage createMessageRequest(/*SipProvider sip_provider, */NameAddress recipient, NameAddress from, String call_id, String subject, String content_type, byte[] body) {
		GenericURI request_uri=recipient.getAddress();
		//String callid=sip_provider.pickCallId();
		int cseq=SipProvider.pickInitialCSeq();
		String local_tag=SipProvider.pickTag();
		//String branch=SipStack.pickBranch();
		SipMessage req=createRequest(/*sip_provider,*/SipMethods.MESSAGE,request_uri,recipient,from,call_id,cseq,local_tag,null,null,content_type,body);
		if (subject!=null) req.setSubjectHeader(new SubjectHeader(subject));
		return req;
	}

	/** Creates a new REFER request (RFC3515).
	  * @param recipient the recipient address
	  * @param from name address in the <i>From</i> header field
	  * @param contact contact address, that is the name address in the <i>Contact</i> header field, or <i>null</i>
	  * @param call_id the <i>Call-ID</i> value
	  * @param refer_to the name address in the <i>Refer-To</i> header field
	  * @return the new REFER message */
	public static SipMessage createReferRequest(/*SipProvider sip_provider, */NameAddress recipient, NameAddress from, NameAddress contact, String call_id, NameAddress refer_to/*, NameAddress referred_by*/) {
		GenericURI request_uri=recipient.getAddress();
		//String callid=sip_provider.pickCallId();
		int cseq=SipProvider.pickInitialCSeq();
		String local_tag=SipProvider.pickTag();
		//String branch=SipStack.pickBranch();
		SipMessage req=createRequest(/*sip_provider,*/SipMethods.REFER,request_uri,recipient,from,call_id,cseq,local_tag,null,contact,null,null);
		req.setReferToHeader(new ReferToHeader(refer_to));
		//if (referred_by!=null) req.setReferredByHeader(new ReferredByHeader(referred_by));
		req.setReferredByHeader(new ReferredByHeader(from));
		return req;
	}

	/** Creates a new REFER request (RFC3515) within a dialog
	  * @param dialog the current dialog
	  * @param refer_to the name address in the <i>Refer-To</i> header field
	  * @param referred_by the name address in the <i>Refer-By</i> header field, or <i>null</i>
	  * @return the new REFER message */
	public static SipMessage createReferRequest(Dialog dialog, NameAddress refer_to, NameAddress referred_by) {
		SipMessage req=createRequest(dialog,SipMethods.REFER,null,null);
		req.setReferToHeader(new ReferToHeader(refer_to));
		if (referred_by!=null) req.setReferredByHeader(new ReferredByHeader(referred_by));
		else req.setReferredByHeader(new ReferredByHeader(dialog.getLocalName()));
		return req;
	}

	/** Creates a new SUBSCRIBE request (RFC3265) out of any pre-existing dialogs.
	  * @param recipient the recipient address
	  * @param to name address in the <i>To</i> header field
	  * @param from name address in the <i>From</i> header field
	  * @param contact contact address, that is the name address in the <i>Contact</i> header field, or <i>null</i>
	  * @param call_id the <i>Call-ID</i> value
	  * @param event event package
	  * @param id the event id
	  * @param content_type content type, or <i>null</i> in case of no message body
	  * @param body message content (body), or <i>null</i>
	  * @return the new SUBSCRIBE message */
	public static SipMessage createSubscribeRequest(/*SipProvider sip_provider, */GenericURI recipient, NameAddress to, NameAddress from, NameAddress contact, String call_id, String event, String id, String content_type, byte[] body) {
		SipMessage req=createRequest(/*sip_provider,*/SipMethods.SUBSCRIBE,recipient,to,from,call_id,contact,content_type,body);
		req.setEventHeader(new EventHeader(event,id));
		return req;
	}
	

	/** Creates a new SUBSCRIBE request (RFC3265) within a dialog (re-subscribe).
	  * @param dialog the current dialog
	  * @param event event package
	  * @param id the event id
	  * @param content_type content type, or <i>null</i> in case of no message body
	  * @param body message content (body), or <i>null</i>
	  * @return the new SUBSCRIBE message */
	public static SipMessage createSubscribeRequest(Dialog dialog, String event, String id, String content_type, byte[] body) {
		SipMessage req=createRequest(dialog,SipMethods.SUBSCRIBE,content_type,body);
		req.setEventHeader(new EventHeader(event,id));
		return req;
	}
	

	/** Creates a new NOTIFY request (RFC3265) within a dialog.
	  * @param dialog the current dialog
	  * @param event event package
	  * @param id the event id
	  * @param content_type content type, or <i>null</i> in case of no message body
	  * @param body message content (body), or <i>null</i>
	  * @return the new NOTIFY message */
	public static SipMessage createNotifyRequest(Dialog dialog, String event, String id, String content_type, byte[] body) {
		SipMessage req=createRequest(dialog,SipMethods.NOTIFY,content_type,body);
		req.removeExpiresHeader();
		req.setEventHeader(new EventHeader(event,id));
		return req;
	}


	/** Creates a new NOTIFY request (RFC3265) within a dialog, with a SIP fragment ("message/sipfrag") as message body.
	  * @param dialog the current dialog
	  * @param event event package
	  * @param id the event id
	  * @param sipfragment a fragment of a SIP message to be included as message body
	  * @return the new NOTIFY message */
	public static SipMessage createNotifyRequest(Dialog dialog, String event, String id, String sipfragment) {
		SipMessage req=createRequest(dialog,SipMethods.NOTIFY,"message/sipfrag;version=2.0",sipfragment.getBytes());
		req.removeExpiresHeader();
		req.setEventHeader(new EventHeader(event,id));
		return req;
	}

}  
