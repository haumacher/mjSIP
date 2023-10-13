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

import org.mjsip.sip.address.NameAddress;
import org.mjsip.sip.message.SipMessage;



/** Interface CallListener can be implemented to manage SIP calls (sipx.call.Call).
 *  <p> Objects of class Call use CallListener callback methods to signal
 *  specific call events.  
 */
public interface CallListener {
	
	/** Callback function called when arriving a new INVITE method (incoming call) */
	public void onCallInvite(Call call, NameAddress callee, NameAddress caller, String sdp, SipMessage invite);

	/** Callback function called when arriving a 183 Session Progress */
	public void onCallProgress(Call call, SipMessage resp);

	/** Callback function called when arriving a 180 Ringing */
	public void onCallRinging(Call call, SipMessage resp);

	/** Callback function called when arriving a 1xx response (e.g. 183 Session Progress) that has to be confirmed */
	public void onCallConfirmableProgress(Call call, SipMessage resp);

	/** Callback function called when arriving a PRACK for a reliable 1xx response, that had to be confirmed */
	public void onCallProgressConfirmed(Call call, SipMessage resp, SipMessage prack);

	/** Callback function called when arriving a 2xx (call accepted) */
	public void onCallAccepted(Call call, String sdp, SipMessage resp);

	/** Callback function called when arriving a 4xx (call failure) */
	public void onCallRefused(Call call, String reason, SipMessage resp);

	/** Callback function called when arriving a 3xx (call redirection) */
	public void onCallRedirected(Call call, String reason, Vector contact_list, SipMessage resp);

	/** Callback function called when arriving an ACK method (call confirmed) */
	public void onCallConfirmed(Call call, String sdp, SipMessage ack);

	/** Callback function called when the invite expires */
	public void onCallTimeout(Call call);

	/**  Callback function called when arriving an  INFO method. */ 
	public void onCallInfo(Call call, String info_package, String content_type, byte[] body, SipMessage msg);

	/** Callback function called when arriving a new Re-INVITE method (re-inviting/call modify) */
	public void onCallModify(Call call, String sdp, SipMessage invite);

	/** Callback function called when arriving a 2xx (re-invite/modify accepted) */
	public void onCallModifyAccepted(Call call, String sdp, SipMessage resp);

	/** Callback function called when arriving a 4xx (re-invite/modify failure) */
	public void onCallModifyRefused(Call call, String reason, SipMessage resp);

	/** Callback function called when a re-invite expires */
	public void onCallModifyTimeout(Call call);

	/** Callback function called when arriving a 3xx (call redirection) */
	//public void onCallModifyRedirection(Call call, String reason, Vector contact_list, SipMessage resp);

	/** Callback function called when arriving a CANCEL request */
	public void onCallCancel(Call call, SipMessage cancel);

	/** Callback function called when arriving a new UPDATE method (update request). */
	public void onCallUpdate(Call call, String sdp, SipMessage update);

	/** Callback function called when arriving a 2xx for an UPDATE request */
	public void onCallUpdateAccepted(Call call, String sdp, SipMessage resp);

	/** Callback function called when arriving a non 2xx for an UPDATE request */
	public void onCallUpdateRefused(Call call, String sdp, SipMessage resp);

	/** Callback function called when arriving a BYE request */
	public void onCallBye(Call call, SipMessage bye);

	/** Callback function called when arriving a response for the BYE request (call closed) */
	public void onCallClosed(Call call, SipMessage resp);
}

