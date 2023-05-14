/*
 * Copyright (C) 2014 Luca Veltri - University of Parma - Italy
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



/** Transaction client identifier.
  */
public class TransactionClientId extends TransactionId {
	
	/** Creates a new TransactionClientId.
	  * @param method the method name */
	public TransactionClientId(String method) {
		super(method);
	}

	/** Creates a new TransactionClientId.
	  * @param call_id the call-id
	  * @param seqn the CSeq sequence number
	  * @param sent_by the Via sent-by address
	  * @param branch the Via branch */
	public TransactionClientId(String call_id, long seqn, String method, String sent_by, String branch) {
		super(true,call_id,seqn,method,sent_by,branch);
	}

	/** Creates a new TransactionClientId.
	  * @param msg a SIP message */
	public TransactionClientId(SipMessage msg) {
		super(true,msg);
	}

	/** Creates a new TransactionClientId.
	  * @param id a transaction client identifier */
	public TransactionClientId(TransactionClientId id) {
		super(id);
	}
}
