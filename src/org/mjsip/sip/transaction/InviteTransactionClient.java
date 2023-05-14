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

package org.mjsip.sip.transaction;



import org.mjsip.sip.message.SipMessage;
import org.mjsip.sip.message.SipMessageFactory;
import org.mjsip.sip.provider.SipProvider;
import org.mjsip.sip.provider.SipStack;
import org.mjsip.sip.provider.TransactionClientId;
import org.zoolu.util.LogLevel;
import org.zoolu.util.Timer;



/** INVITE  client transaction as defined in RFC 3261 (Section 17.2.1).
  * <BR> An InviteTransactionClient is responsable to create a new SIP invite
  * transaction, starting with a invite message sent through the SipProvider
  * and ending with a final response.
  * <BR> The changes of the internal status and the received messages are fired
  * to the TransactionListener passed to the InviteTransactionClient object.
  */
public class InviteTransactionClient extends TransactionClient {
	
	/** the TransactionClientListener that captures the events fired by the InviteTransactionClient */
	TransactionClientListener invite_tc_listener;

	/** ack message */
	SipMessage ack;

	/** retransmission timeout ("Timer A" in RFC 3261) */
	//Timer retransmission_to;
	/** transaction timeout ("Timer B" in RFC 3261) */
	//Timer transaction_to;
	/** end timeout for invite transactions ("Timer D" in RFC 3261)*/
	Timer end_to;


	// ************************** Costructors **************************

	/** Creates a new InviteTransactionClient */
	public InviteTransactionClient(SipProvider sip_provider, SipMessage req, TransactionClientListener listener) {
		super(sip_provider);
		request=new SipMessage(req);
		init(listener,new TransactionClientId(request));
	}  

	/** Initializes it. */
	void init(TransactionClientListener listener, TransactionClientId transaction_id) {
		this.invite_tc_listener=listener;
		this.transaction_id=transaction_id;
		this.ack=null;
		// init the timer just to set the timeout value and label, without listener (never started)
		retransmission_to=new Timer(SipStack.retransmission_timeout,null);
		transaction_to=new Timer(SipStack.transaction_timeout,null);
		end_to=new Timer(SipStack.transaction_timeout,null);
		log(LogLevel.INFO,"new transaction-id: "+transaction_id.toString());
	}   

	
	// ************************ Public methods *************************

	/** Starts the InviteTransactionClient and sends the invite request. */
	public void request() {
		log(LogLevel.TRACE,"start");
		changeStatus(STATE_TRYING); 
		transaction_to=new Timer(transaction_to.getTime(),this);
		transaction_to.start(); 
		sip_provider.addSelectiveListener(transaction_id,this); 
		connection_id=sip_provider.sendMessage(request);
		retransmission_to=new Timer(retransmission_to.getTime(),this);
		retransmission_to.start();
	}  
		
	/** Method derived from interface SipListener.
	  * It's fired from the SipProvider when a new message is catch for to the present ServerTransaction.
	  */
	public void onReceivedMessage(SipProvider provider, SipMessage msg) {
		if (msg.isResponse()) {
			int code=msg.getStatusLine().getCode();
			if (code>=100 && code<200 && (statusIs(STATE_TRYING) || statusIs(STATE_PROCEEDING))) {
				if (statusIs(STATE_TRYING)) {
					retransmission_to.halt();
					transaction_to.halt();
					changeStatus(STATE_PROCEEDING);
				}
				if (invite_tc_listener!=null) invite_tc_listener.onTransProvisionalResponse(this,msg);
				return;
			}
			if (code>=300 && code<700 && (statusIs(STATE_TRYING) || statusIs(STATE_PROCEEDING) || statusIs(STATE_COMPLETED))) {
				if (statusIs(STATE_TRYING) || statusIs(STATE_PROCEEDING)) {
					retransmission_to.halt();
					transaction_to.halt();
					ack=SipMessageFactory.createNon2xxAckRequest(request,msg);
					changeStatus(STATE_COMPLETED);
					connection_id=sip_provider.sendMessage(ack);
					if (invite_tc_listener!=null) invite_tc_listener.onTransFailureResponse(this,msg);
					invite_tc_listener=null;
					if (connection_id==null) {
						end_to=new Timer(end_to.getTime(),this);
						end_to.start();
					}
					else {
						log(LogLevel.TRACE,"end_to=0 for reliable transport");
						onTimeout(end_to);
					}
				}
				else {
					// retransmit ACK only in case of unreliable transport 
					if (connection_id==null) sip_provider.sendMessage(ack);
				}
				return;
			}
			if (code>=200 && code<300 && (statusIs(STATE_TRYING) || statusIs(STATE_PROCEEDING))) {
				doTerminate();
				if (invite_tc_listener!=null) invite_tc_listener.onTransSuccessResponse(this,msg);
				invite_tc_listener=null;
				return;
			}
		}
	}

	/** Method derived from interface TimerListener.
	  * It's fired from an active Timer. */
	public void onTimeout(Timer to) {
		try {
			if (to.equals(retransmission_to) && statusIs(STATE_TRYING)) {
				log(LogLevel.INFO,"Retransmission timeout expired");
				// retransmission only in case of unreliable transport 
				if (connection_id==null) {
					sip_provider.sendMessage(request);
					long timeout=2*retransmission_to.getTime();
					retransmission_to=new Timer(timeout,this);
					retransmission_to.start();
				}
				else log(LogLevel.TRACE,"No retransmissions for reliable transport ("+connection_id+")");
			} 
			if (to.equals(transaction_to)) {
				log(LogLevel.INFO,"Transaction timeout expired");
				doTerminate();
				if (invite_tc_listener!=null) invite_tc_listener.onTransTimeout(this);
				invite_tc_listener=null;
			}  
			if (to.equals(end_to)) {
				log(LogLevel.INFO,"End timeout expired");
				doTerminate();
				invite_tc_listener=null; // already null..
			}
		}
		catch (Exception e) {
			log(LogLevel.INFO,e);
		}
	}

	/** Terminates the transaction. */
	public void terminate() {
		doTerminate();
		invite_tc_listener=null;
	}


	// *********************** Protected methods ***********************

	/** Moves to terminate state. */
	protected void doTerminate() {
		if (!statusIs(STATE_TERMINATED)) {
			retransmission_to.halt();
			transaction_to.halt();     
			end_to.halt();
			sip_provider.removeSelectiveListener(transaction_id);
			changeStatus(STATE_TERMINATED);
		}
	}
}
