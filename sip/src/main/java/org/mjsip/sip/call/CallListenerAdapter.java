/*
 * Copyright (C) 2005 Luca Veltri - University of Parma - Italy
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

package org.mjsip.sip.call;



//import java.util.Iterator;
import java.util.Vector;

import org.mjsip.sdp.OfferAnswerModel;
import org.mjsip.sdp.SdpMessage;
import org.mjsip.sip.address.NameAddress;
import org.mjsip.sip.message.SipMessage;



/** Class CallListenerAdapter implements CallListener interface
  * providing a dummy implementation of all Call callback functions used to capture Call events.
  * <p> CallListenerAdapter can be extended to manage basic SIP calls.
  * The callback methods defined in this class have basically a void implementation.
  * This class exists as convenience for creating call listener objects. 
  * <br> You can extend this class overriding only methods corresponding to events
  * you want to handle.
  * <p> <i>onCallInvite(NameAddress,String)</i> is the only non-empty method.
  * It signals the receiver the ring status (by using method Call.ring()),
  * adapts the sdp body and accepts the call (by using method Call.accept(sdp)). 
  */
public abstract class CallListenerAdapter implements ExtendedCallListener {
	
		
	// ************************** Costructors ***************************

	/** Creates a new dummy call listener */
	protected CallListenerAdapter() {
		
	}
	
	
	// ************************* Static methods *************************
  
	/** Changes the current session descriptor specifing the receiving RTP/UDP port number, the AVP format, the codec, and the clock rate */
	/*public static String audioSession(int port, int avp, String codec, int rate) {
		SessionDescriptor sdp=new SessionDescriptor();
		sdp.addMedia(new MediaField("audio ",port,0,"RTP/AVP",String.valueOf(avp)),new AttributeField("rtpmap",avp+" "+codec+"/"+rate));
		return sdp.toString();
	}*/

	/** Changes the current session descriptor specifing the receiving RTP/UDP port number, the AVP format, the codec, and the clock rate */
	/*public static String audioSession(int port) {
		return audioSession(port,0,"PCMU",8000);
	}*/


	// *********************** Callback functions ***********************

	/** Accepts an incoming call.
	  * Callback function called when arriving a new INVITE method (incoming call) */
	public void onCallInvite(Call call, NameAddress callee, NameAddress caller, String sdp, SipMessage invite) {
		//log("INCOMING");
		call.ring();
		// accept immediatly
		String local_session;
		if (sdp!=null && sdp.length()>0) {
			SdpMessage remote_sdp=new SdpMessage(sdp);     
			SdpMessage local_sdp=new SdpMessage(call.getLocalSessionDescriptor());
			SdpMessage new_sdp=new SdpMessage(local_sdp.getOrigin(),remote_sdp.getSessionName(),local_sdp.getConnection(),local_sdp.getTime());
			new_sdp.addMediaDescriptors(local_sdp.getMediaDescriptors());
			new_sdp=OfferAnswerModel.makeSessionDescriptorProduct(new_sdp,remote_sdp);
			local_session=new_sdp.toString();
		}
		else local_session=call.getLocalSessionDescriptor();
		call.accept(local_session);
	}

	/** Changes the call when remotly requested.
	  * Callback function called when arriving a new Re-INVITE method (re-inviting/call modify) */
	public void onCallModify(Call call, String sdp, SipMessage invite) {
		//log("RE-INVITE/MODIFY");
		String local_session;
		if (sdp!=null && sdp.length()>0) {
			SdpMessage remote_sdp=new SdpMessage(sdp);
			SdpMessage local_sdp=new SdpMessage(call.getLocalSessionDescriptor());
			SdpMessage new_sdp=new SdpMessage(local_sdp.getOrigin(),remote_sdp.getSessionName(),local_sdp.getConnection(),local_sdp.getTime());
			new_sdp.addMediaDescriptors(local_sdp.getMediaDescriptors());
			new_sdp=OfferAnswerModel.makeSessionDescriptorProduct(new_sdp,remote_sdp);
			local_session=new_sdp.toString();
		}
		else local_session=call.getLocalSessionDescriptor();
		// accept immediatly
		call.accept(local_session);
	}

	/** Does nothing.
	  * Callback function called when arriving a 183 Session Progress */
	public void onCallProgress(Call call, SipMessage resp) {
		//log("PROGRESS");
	}

	/** Does nothing.
	  * Callback function called when arriving a 180 Ringing */
	public void onCallRinging(Call call, SipMessage resp) {
		//log("RINGING");
	}

	/** Does nothing.
	  * Callback function called when arriving a 2xx (call accepted) */
	public void onCallAccepted(Call call, String sdp, SipMessage resp) {
		//log("ACCEPTED/CALL");
	}

	/** Does nothing.
	  * Callback function called when arriving a 4xx (call failure) */
	public void onCallRefused(Call call, String reason, SipMessage resp) {
		//log("REFUSED ("+reason+")");
	}

	/** Redirects the call when remotly requested.
	  * Callback function called when arriving a 3xx (call redirection) */
	public void onCallRedirected(Call call, String reason, Vector contact_list, SipMessage resp) {
		//log("REDIRECTION ("+reason+")");
		NameAddress first_contact=new NameAddress((String)contact_list.elementAt(0));
		call.call(first_contact); 
	}

	/** Does nothing.
	  * Callback function called when arriving an ACK method (call confirmed) */
	public void onCallConfirmed(Call call, String sdp, SipMessage ack) {
		//log("CONFIRMED/CALL");
	}

	/** Does nothing.
	  * Callback function called when arriving an  INFO method. */ 
	public void onCallInfo(Call call, String info_package, String content_type, byte[] body, SipMessage msg) {
		//log("INFO");
	}

	/** Does nothing.
	  * Callback function called when the invite expires */
	public void onCallTimeout(Call call) {
		//log("TIMEOUT/CLOSE");
	}   

	/** Does nothing.
	  * Callback function called when arriving a 2xx (re-invite/modify accepted) */
	public void onCallModifyAccepted(Call call, String sdp, SipMessage resp) {
		//log("RE-INVITE-ACCEPTED/CALL");
	}

	/** Does nothing.
	  * Callback function called when arriving a 4xx (re-invite/modify failure) */
	public void onCallModifyRefused(Call call, String reason, SipMessage resp) {
		//log("RE-INVITE-REFUSED ("+reason+")/CALL");
	}

	/** Does nothing.
	  * Callback function called when a re-invite expires */
	public void onCallModifyTimeout(Call call) {
		//log("RE-INVITE-TIMEOUT/CALL");
	}   

	/** Does nothing.
	  * Callback function called when arriving a CANCEL request */
	public void onCallCancel(Call call, SipMessage cancel) {
		//log("CANCELING");
	}

	/** Does nothing.
	  * Callback function that may be overloaded (extended). Called when arriving a BYE request */
	public void onCallBye(Call call, SipMessage bye) {
		//log("CLOSING");
	}

	/** Does nothing.
	  * Callback function that may be overloaded (extended). Called when arriving a response for a BYE request (call closed) */
	public void onCallClosed(Call call, SipMessage resp) {
		//log("CLOSED");
	}


	/** From ExtendedCallListener. Callback function called when arriving a new UPDATE method (update request). */
	public void onCallUpdate(Call call, String sdp, SipMessage update) {
		String local_session;
		if (sdp!=null && sdp.length()>0) {
			SdpMessage remote_sdp=new SdpMessage(sdp);
			SdpMessage local_sdp=new SdpMessage(call.getLocalSessionDescriptor());
			SdpMessage new_sdp=new SdpMessage(local_sdp.getOrigin(),remote_sdp.getSessionName(),local_sdp.getConnection(),local_sdp.getTime());
			new_sdp.addMediaDescriptors(local_sdp.getMediaDescriptors());
			new_sdp=OfferAnswerModel.makeSessionDescriptorProduct(new_sdp,remote_sdp);
			local_session=new_sdp.toString();
		}
		else local_session=call.getLocalSessionDescriptor();
		// accept immediatly
		call.acceptUpdate(local_session);
	}

	/** Callback function called when arriving a 2xx for an UPDATE request */
	public void onCallUpdateAccepted(Call call, String sdp, SipMessage resp) {
		
	}

	/** Callback function called when arriving a non 2xx for an UPDATE request */
	public void onCallUpdateRefused(Call call, String sdp, SipMessage resp) {
		
	}

	/** Does nothing.
	  * Callback function called when arriving a new REFER method (transfer request) */
	public void onCallTransfer(ExtendedCall call, NameAddress refer_to, NameAddress refered_by, SipMessage refer) {
		//log("REFER-TO/TRANSFER");
	}

	/** Callback function called when arriving a new REFER method (transfer request) with Replaces header, replacing an existing call. */
	public void onCallAttendedTransfer(ExtendedCall call, NameAddress refer_to, NameAddress refered_by, String replcall_id, SipMessage refer) {
		//log("REFER-TO/TRANSFER");
	}

	/** Does nothing.
	  * Callback function called when a call transfer is accepted. */
	public void onCallTransferAccepted(ExtendedCall call, SipMessage resp) {
		
	}

	/** Does nothing.
	  * Callback function called when a call transfer is refused. */
	public void onCallTransferRefused(ExtendedCall call, String reason, SipMessage resp) {
		
	}

	/** Does nothing.
	  * Callback function called when a call transfer is successfully completed */
	public void onCallTransferSuccess(ExtendedCall call, SipMessage notify) {
		//log("TRANSFER SUCCESS");
	}

	/** Does nothing.
	  * Callback function called when a call transfer is NOT sucessfully completed */
	public void onCallTransferFailure(ExtendedCall call, String reason, SipMessage notify) {
		//log("TRANSFER FAILURE");
	}

}

