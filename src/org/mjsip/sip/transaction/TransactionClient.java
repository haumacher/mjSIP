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
import org.mjsip.sip.provider.SipProvider;
import org.mjsip.sip.provider.SipStack;
import org.mjsip.sip.provider.TransactionClientId;
import org.zoolu.util.LogLevel;
import org.zoolu.util.Timer;



/** Generic client transaction as defined in RFC 3261 (Section 17.1.2).
  * A TransactionClient is responsable to create a new SIP transaction, starting with a request message sent through the SipProvider and ending with a final response.<BR>
  * The changes of the internal status and the received messages are fired to the TransactionListener passed to the TransactionClient object.<BR>
  */
public class TransactionClient extends Transaction {
	
	/** The TransactionClientListener that captures the events fired by the TransactionClient */
	TransactionClientListener transaction_listener;

	/** Retransmission timeout ("Timer E" in RFC 3261) */
	Timer retransmission_to;
	/** Transaction timeout ("Timer F" in RFC 3261) */
	Timer transaction_to;
	/** Clearing timeout ("Timer K" in RFC 3261) */
	Timer clearing_to;

 
	// ************************** Costructors **************************

	/** Creates a new TransactionClient. */
	protected TransactionClient(SipProvider sip_provider) {
		super(sip_provider);
		transaction_listener=null;
	} 

	/** Creates a new TransactionClient */
	public TransactionClient(SipProvider sip_provider, SipMessage req, TransactionClientListener listener) {
		super(sip_provider);
		request=new SipMessage(req);
		init(listener,new TransactionClientId(request));
	}
	
	/** Initializes it. */
	void init(TransactionClientListener listener, TransactionClientId transaction_id) {
		this.transaction_listener=listener;
		this.transaction_id=transaction_id;
		// init the timer just to set the timeout value and label, without listener (never started)
		retransmission_to=new Timer(SipStack.retransmission_timeout,null);
		transaction_to=new Timer(SipStack.transaction_timeout,null);
		clearing_to=new Timer(SipStack.clearing_timeout,null);
		log(LogLevel.INFO,"new transaction-id: "+transaction_id.toString());
	}


	// ************************ Public methods *************************

	/** Starts the TransactionClient and sends the transaction request. */
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
		
	/** Terminates the transaction. */
	public void terminate() {
		doTerminate();
		transaction_listener=null;
	}


	// *********************** Callback methods ************************

	/** From SipListener. It's fired from the SipProvider when a new message is received for to the present TransactionClient. */
	public void onReceivedMessage(SipProvider provider, SipMessage msg) {
		if (msg.isResponse()) {
			int code=msg.getStatusLine().getCode();
			if (code>=100 && code<200 && (statusIs(STATE_TRYING) || statusIs(STATE_PROCEEDING))) {
				if (statusIs(STATE_TRYING)) changeStatus(STATE_PROCEEDING);
				if (transaction_listener!=null) transaction_listener.onTransProvisionalResponse(this,msg);
				return;
			}
			if (code>=200 && code<700 && (statusIs(STATE_TRYING) || statusIs(STATE_PROCEEDING))) {
				retransmission_to.halt();
				transaction_to.halt();
				changeStatus(STATE_COMPLETED);
				if (transaction_listener!=null) {
					if (code<300) transaction_listener.onTransSuccessResponse(this,msg);
					else transaction_listener.onTransFailureResponse(this,msg);
				}
				if (connection_id==null) {
					clearing_to=new Timer(clearing_to.getTime(),this);
					clearing_to.start();
				}
				else {
					log(LogLevel.TRACE,"clearing_to=0 for reliable transport");
					onTimeout(clearing_to);
				}
				return;
			}
		}
	}

	/** From TimerListener. It's fired from an active Timer. */
	public void onTimeout(Timer to) {
		try {
			if (to.equals(retransmission_to) && (statusIs(STATE_TRYING) || statusIs(STATE_PROCEEDING))) {
				log(LogLevel.INFO,"Retransmission timeout expired");
				// retransmission only for unreliable transport 
				if (connection_id==null) {
					sip_provider.sendMessage(request);
					long timeout=2*retransmission_to.getTime();
					if (timeout>SipStack.max_retransmission_timeout || statusIs(STATE_PROCEEDING)) timeout=SipStack.max_retransmission_timeout;
					retransmission_to=new Timer(timeout,this);
					retransmission_to.start();
				}
				else log(LogLevel.TRACE,"No retransmissions for reliable transport ("+connection_id+")");
			} 
			if (to.equals(transaction_to)) {
				log(LogLevel.INFO,"Transaction timeout expired");
				doTerminate();
				if (transaction_listener!=null) transaction_listener.onTransTimeout(this);
				transaction_listener=null;
			}  
			if (to.equals(clearing_to)) {
				log(LogLevel.INFO,"Clearing timeout expired");
				doTerminate();
			}
		}
		catch (Exception e) {
			log(LogLevel.INFO,e);
		}
	}
	
	// *********************** Protected methods ***********************

	/** Moves to terminate state. */
	protected void doTerminate() {
		if (!statusIs(STATE_TERMINATED)) {
			retransmission_to.halt();
			transaction_to.halt();     
			clearing_to.halt();
			sip_provider.removeSelectiveListener(transaction_id);
			changeStatus(STATE_TERMINATED);
		}
	}


	// ****************************** Logs *****************************

	/** Adds a new string to the default log. */
	protected void log(LogLevel level, String str) {
		if (logger!=null) logger.log(level,"TransactionClient#"+transaction_sqn+": "+str);  
	}

}
