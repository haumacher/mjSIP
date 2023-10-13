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



/** Transaction server identifier.
  */
public class TransactionServerId extends TransactionId {
	
	/** Creates a new TransactionServerId.
	  * @param method the method name */
	public TransactionServerId(String method) {
		super(method);
	}

	/** Creates a new TransactionServerId.
	  * @param call_id the call-id
	  * @param seqn the CSeq sequence number
	  * @param sent_by the Via sent-by address
	  * @param branch the Via branch */
	public TransactionServerId(String call_id, long seqn, String method, String sent_by, String branch) {
		super(false,call_id,seqn,method,sent_by,branch);
	}

	/** Creates a new TransactionServerId.
	  * @param msg a SIP message */
	public TransactionServerId(SipMessage msg) {
		super(false,msg);
	}

	/** Creates a new TransactionServerId.
	  * @param id a transaction server identifier */
	public TransactionServerId(TransactionId id) {
		super(id);
	}
}
