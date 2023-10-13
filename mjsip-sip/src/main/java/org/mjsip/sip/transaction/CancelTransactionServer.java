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

package org.mjsip.sip.transaction;



import org.mjsip.sip.header.ViaHeader;
import org.mjsip.sip.message.SipMessage;
import org.mjsip.sip.message.SipMethods;
import org.mjsip.sip.provider.SipProvider;
import org.mjsip.sip.provider.TransactionServerId;



/** CANCEL transaction server.
  */
public class CancelTransactionServer extends TransactionServer {
	

	/** Creates a new CancelTransactionServer for a received request.
	  * @param sip_provider the SIP provider
	  * @param req the request message that this transaction server is targeted to
	  * @param ts_listener the transaction server listener */
	public CancelTransactionServer(SipProvider sip_provider, SipMessage req, TransactionServerListener ts_listener) {
		super(sip_provider);
		String call_id=req.getCallIdHeader().getCallId();
		ViaHeader top_via=req.getViaHeader();
		String branch=(top_via!=null && top_via.hasBranch())? top_via.getBranch() : null;
		String sent_by=(top_via!=null)? top_via.getSentBy() : null;
		long seqn=req.getCSeqHeader().getSequenceNumber();      
		TransactionServerId transaction_id=new TransactionServerId(call_id,seqn,SipMethods.CANCEL,sent_by,branch);
		init(ts_listener,transaction_id,null);
	}  

}

