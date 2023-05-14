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

package org.mjsip.sip.provider;



import org.mjsip.sip.message.SipMessage;



/** Dialog identifier.
  */
public class DialogId extends SipId {
	
	/** Creates a new DialogId.
	  * @param call_id the call-id
	  * @param local_tag the local tag
	  * @param remote_tag the remote tag. */
	public DialogId(String call_id, String local_tag, String remote_tag) {
		super(getDialodId(call_id,local_tag,remote_tag));
	}

	/** Creates a new DialogId.
	  * @param msg a SIP message. */
	public DialogId(SipMessage msg) {
		super();
		String call_id=msg.getCallIdHeader().getCallId();
		String local_tag, remote_tag;
		if (msg.isRequest()) {  local_tag=msg.getToHeader().getTag(); remote_tag=msg.getFromHeader().getTag(); }
		else {  local_tag=msg.getFromHeader().getTag(); remote_tag=msg.getToHeader().getTag(); }
		// set id
		this.id=getDialodId(call_id,local_tag,remote_tag);
	}   

	/** Creates a new DialogId.
	  * @param i a dialog id. */
	public DialogId(DialogId i) {
		super(i);
	}

	/** Gets the string value of the dialog identifier.
	  * @param call_id the call-id
	  * @param local_tag the local tag
	  * @param remote_tag the remote tag. */
	private static String getDialodId(String call_id, String local_tag, String remote_tag) {
		return call_id+"-"+local_tag+"-"+remote_tag;
	}
}
