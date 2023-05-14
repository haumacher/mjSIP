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



import org.mjsip.sip.header.CSeqHeader;
import org.mjsip.sip.header.ViaHeader;
import org.mjsip.sip.message.SipMessage;
import org.mjsip.sip.message.SipMethods;



/** TransactionId is used to address specific transaction to the SipProvider.
  */
public class TransactionId extends SipId {
	
	/** Creates a new TransactionId.
	  * @param method the method name */
	protected TransactionId(String method) {
		super(method);
	}

	/** Creates a new TransactionId.
	  * @param uac whether it is a UAC side (true=UAC, false=UAS)
	  * @param msg a SIP message */
	protected TransactionId(boolean uac, SipMessage msg) {
		super();
		String call_id=msg.getCallIdHeader().getCallId();
		ViaHeader top_via=msg.getViaHeader();
		String branch=null;
		String sent_by=null;
		if (top_via!=null) {
			if (top_via.hasBranch()) branch=top_via.getBranch();
			sent_by=top_via.getSentBy();
		}
		CSeqHeader cseqh=msg.getCSeqHeader();
		long seqn=cseqh.getSequenceNumber();      
		String method=cseqh.getMethod();
		// set id
		id=getTransactionId(uac,call_id,seqn,method,sent_by,branch);
	}

	/** Creates a new TransactionId.
	  * @param uac whether it is a UAC side (true=UAC, false=UAS)
	  * @param call_id the call-id
	  * @param seqn the CSeq sequence number
	  * @param sent_by the Via sent-by address
	  * @param branch the Via branch */
	protected TransactionId(boolean uac, String call_id, long seqn, String method, String sent_by, String branch) {
		super(getTransactionId(uac,call_id,seqn,method,sent_by,branch));
	}

	/** Creates a new TransactionId.
	  * @param id a transaction identifier */
	protected TransactionId(TransactionId id) {
		super(id);
	}

	/** Gets the string value of the transaction identifier.
	  * @param uac whether it is a UAC side (true=UAC, false=UAS)
	  * @param call_id the call-id
	  * @param seqn the CSeq sequence number
	  * @param sent_by the Via sent-by address
	  * @param branch the Via branch
	  * @return the string value for a transaction identifier */
	private static String getTransactionId(boolean uac, String call_id, long seqn, String method, String sent_by, String branch) {
		if (method.equals(SipMethods.ACK)) method=SipMethods.INVITE;
		String type=(uac)? "client" : "server";
		if (branch==null) branch=sent_by;
		return call_id+"-"+seqn+"-"+method+"-"+type+"-"+branch;
	}
}
