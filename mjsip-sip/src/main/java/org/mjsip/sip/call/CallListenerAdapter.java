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

import java.util.Vector;

import org.mjsip.sdp.OfferAnswerModel;
import org.mjsip.sdp.SdpMessage;
import org.mjsip.sip.address.NameAddress;
import org.mjsip.sip.message.SipMessage;

/**
 * {@link CallListenerAdapter} implements {@link CallListener} providing a dummy implementation of
 * all {@link Call} callback functions used to capture call events.
 * 
 * <p>
 * {@link CallListenerAdapter} can be extended to manage basic SIP calls. The callback methods
 * defined in this class have basically empty implementation. This class exists as convenience for
 * creating call listener implementations.
 * </p>
 * <p>
 * You can extend this class overriding only methods corresponding to events you want to handle.
 * </p>
 * <p>
 * {@link #onCallInvite(Call, NameAddress, NameAddress, SdpMessage, SipMessage)} is the only non-empty
 * method. It signals the receiver the ring status (by using method Call.ring()), adapts the sdp
 * body and accepts the call (by using method Call.accept(sdp)).
 * </p>
 */
public class CallListenerAdapter implements ExtendedCallListener {
	
	// ************************** Constructors ***************************

	/** Creates a new dummy call listener */
	protected CallListenerAdapter() {
		super();
	}
	
	// *********************** Callback functions ***********************

	/** Accepts an incoming call.
	  * Callback function called when arriving a new INVITE method (incoming call) */
	@Override
	public void onCallInvite(Call call, NameAddress callee, NameAddress caller, SdpMessage remoteSdp,
			SipMessage invite) {
		call.ring();
		// accept immediatly
		SdpMessage acceptSdp;
		if (remoteSdp != null) {
			acceptSdp = OfferAnswerModel.matchSdp(call.getLocalSessionDescriptor(), remoteSdp);
		} else {
			acceptSdp = call.getLocalSessionDescriptor();
		}
		call.accept(acceptSdp);
	}

	/** Changes the call when remotly requested.
	  * Callback function called when arriving a new Re-INVITE method (re-inviting/call modify) */
	@Override
	public void onCallModify(Call call, SdpMessage remoteSdp, SipMessage invite) {
		SdpMessage acceptSdp;
		if (remoteSdp != null) {
			acceptSdp = OfferAnswerModel.matchSdp(call.getLocalSessionDescriptor(), remoteSdp);
		} else {
			acceptSdp = call.getLocalSessionDescriptor();
		}
		call.accept(acceptSdp);
	}

	/** Does nothing.
	  * Callback function called when arriving a 183 Session Progress */
	@Override
	public void onCallProgress(Call call, SipMessage resp) {
		// Hook for subclasses.
	}

	@Override
	public void onCallProgressConfirmed(Call call, SipMessage resp, SipMessage prack) {
		// Hook for subclasses.
	}

	@Override
	public void onCallConfirmableProgress(Call call, SipMessage resp) {
		// Hook for subclasses.
	}

	/** Does nothing.
	  * Callback function called when arriving a 180 Ringing */
	@Override
	public void onCallRinging(Call call, SipMessage resp) {
		// Hook for subclasses.
	}

	/** Does nothing.
	  * Callback function called when arriving a 2xx (call accepted) */
	@Override
	public void onCallAccepted(Call call, SdpMessage remoteSdp, SipMessage resp) {
		// Hook for subclasses.
	}

	/** Does nothing.
	  * Callback function called when arriving a 4xx (call failure) */
	@Override
	public void onCallRefused(Call call, String reason, SipMessage resp) {
		// Hook for subclasses.
	}

	/** Redirects the call when remotly requested.
	  * Callback function called when arriving a 3xx (call redirection) */
	@Override
	public void onCallRedirected(Call call, String reason, Vector contact_list, SipMessage resp) {
		NameAddress first_contact=NameAddress.parse((String)contact_list.elementAt(0));
		call.call(first_contact); 
	}

	/** Does nothing.
	  * Callback function called when arriving an ACK method (call confirmed) */
	@Override
	public void onCallConfirmed(Call call, SdpMessage sdp, SipMessage ack) {
		// Hook for subclasses.
	}

	/** Does nothing.
	  * Callback function called when arriving an  INFO method. */ 
	@Override
	public void onCallInfo(Call call, String info_package, String content_type, byte[] body, SipMessage msg) {
		switch (content_type) {
		case "application/dtmf-relay": {
			DTMFInfo dtmf = DTMFInfo.parse(msg.getStringBody());
			onDtmfInfo(call, msg, dtmf);
			break;
		}
		case "application/dtmf": {
			DTMFInfo dtmf = new DTMFInfo(msg.getStringBody());
			onDtmfInfo(call, msg, dtmf);
			break;
		}
		}
		// Hook for subclasses.
	}

	/**
	 * Informs about a DTMF info message has been received.
	 * 
	 * @param msg
	 *        The current {@link Call}.
	 * @param call
	 *        The source {@link SipMessage}.
	 * @param dtmf
	 *        The parsed DTMF information.
	 */
	protected void onDtmfInfo(Call call, SipMessage msg, DTMFInfo dtmf) {
		// Hook for subclasses.
	}

	/** Does nothing.
	  * Callback function called when the invite expires */
	@Override
	public void onCallTimeout(Call call) {
		// Hook for subclasses.
	}   

	/** Does nothing.
	  * Callback function called when arriving a 2xx (re-invite/modify accepted) */
	@Override
	public void onCallModifyAccepted(Call call, SdpMessage sdp, SipMessage resp) {
		// Hook for subclasses.
	}

	/** Does nothing.
	  * Callback function called when arriving a 4xx (re-invite/modify failure) */
	@Override
	public void onCallModifyRefused(Call call, String reason, SipMessage resp) {
		// Hook for subclasses.
	}

	/** Does nothing.
	  * Callback function called when a re-invite expires */
	@Override
	public void onCallModifyTimeout(Call call) {
		// Hook for subclasses.
	}   

	/** Does nothing.
	  * Callback function called when arriving a CANCEL request */
	@Override
	public void onCallCancel(Call call, SipMessage cancel) {
		// Hook for subclasses.
	}

	/** Does nothing.
	  * Callback function that may be overloaded (extended). Called when arriving a BYE request */
	@Override
	public void onCallBye(Call call, SipMessage bye) {
		// Hook for subclasses.
	}

	/** Does nothing.
	  * Callback function that may be overloaded (extended). Called when arriving a response for a BYE request (call closed) */
	@Override
	public void onCallClosed(Call call, SipMessage resp) {
		// Hook for subclasses.
	}

	/** From ExtendedCallListener. Callback function called when arriving a new UPDATE method (update request). */
	@Override
	public void onCallUpdate(Call call, SdpMessage newSdp, SipMessage update) {
		SdpMessage acceptSdp;
		if (newSdp != null) {
			acceptSdp = OfferAnswerModel.matchSdp(call.getLocalSessionDescriptor(), newSdp);
		} else {
			acceptSdp = call.getLocalSessionDescriptor();
		}

		// accept immediatly
		call.acceptUpdate(update, acceptSdp);
	}

	/** Callback function called when arriving a 2xx for an UPDATE request */
	@Override
	public void onCallUpdateAccepted(Call call, String sdp, SipMessage resp) {
		// Hook for subclasses.
	}

	/** Callback function called when arriving a non 2xx for an UPDATE request */
	@Override
	public void onCallUpdateRefused(Call call, String sdp, SipMessage resp) {
		// Hook for subclasses.
	}

	/** Does nothing.
	  * Callback function called when arriving a new REFER method (transfer request) */
	@Override
	public void onCallTransfer(ExtendedCall call, NameAddress refer_to, NameAddress refered_by, SipMessage refer) {
		// Hook for subclasses.
	}

	/** Callback function called when arriving a new REFER method (transfer request) with Replaces header, replacing an existing call. */
	@Override
	public void onCallAttendedTransfer(ExtendedCall call, NameAddress refer_to, NameAddress refered_by, String replcall_id, SipMessage refer) {
		// Hook for subclasses.
	}

	/** Does nothing.
	  * Callback function called when a call transfer is accepted. */
	@Override
	public void onCallTransferAccepted(ExtendedCall call, SipMessage resp) {
		// Hook for subclasses.
	}

	/** Does nothing.
	  * Callback function called when a call transfer is refused. */
	@Override
	public void onCallTransferRefused(ExtendedCall call, String reason, SipMessage resp) {
		// Hook for subclasses.
	}

	/** Does nothing.
	  * Callback function called when a call transfer is successfully completed */
	@Override
	public void onCallTransferSuccess(ExtendedCall call, SipMessage notify) {
		// Hook for subclasses.
	}

	/** Does nothing.
	  * Callback function called when a call transfer is NOT sucessfully completed */
	@Override
	public void onCallTransferFailure(ExtendedCall call, String reason, SipMessage notify) {
		// Hook for subclasses.
	}

}

