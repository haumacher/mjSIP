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
import org.mjsip.sip.provider.ConnectionId;
import org.mjsip.sip.provider.SipProvider;
import org.mjsip.sip.provider.SipStack;
import org.mjsip.sip.provider.TransactionServerId;
import org.zoolu.util.LogLevel;
import org.zoolu.util.Timer;



/** Generic server transaction as defined in RFC 3261 (Section 17.2.2).
  * A TransactionServer is responsable to create a new SIP transaction that starts with a request message received by the SipProvider and ends sending a final response.<BR>
  * The changes of the internal status and the received messages are fired to the TransactionListener passed to the TransactionServer object.<BR>
  * When costructing a new TransactionServer, the transaction type is passed as String parameter to the costructor (e.g. "CANCEL", "BYE", etc..)
  */
public class TransactionServer extends Transaction {
	
	/** The TransactionServerListener that captures the events fired by the TransactionServer */
	TransactionServerListener transaction_listener;

	/** Last response message */
	SipMessage response;
	
	/** Clearing timeout ("Timer J" in RFC 3261) */
	Timer clearing_to;


	// ************************** Costructors **************************

	/** Creates a new TransactionServer. */
	protected TransactionServer(SipProvider sip_provider) {
		super(sip_provider);
		transaction_listener=null;
		response=null;
	} 

	/** Creates a new TransactionServer of type <i>method</i>. */
	public TransactionServer(SipProvider sip_provider, String method, TransactionServerListener listener) {
		super(sip_provider);
		init(listener,new TransactionServerId(method),null);
	}  

	/** Creates a new TransactionServer for the already received request <i>req</i>. */
	public TransactionServer(SipProvider provider, SipMessage req, TransactionServerListener listener) {
		super(provider);
		request=new SipMessage(req);
		init(listener,new TransactionServerId(request),request.getConnectionId());
		
		log(LogLevel.TRACE,"start");
		changeStatus(STATE_TRYING);
		sip_provider.addSelectiveListener(transaction_id,this); 
	}  

	/** Inits the transaction server. */
	protected void init(TransactionServerListener listener, TransactionServerId transaction_id, ConnectionId connection_id) {
		this.transaction_listener=listener;
		this.transaction_id=transaction_id;
		this.connection_id=connection_id;
		this.response=null;
		// init the timer just to set the timeout value and label, without listener (never started)
		clearing_to=new Timer(SipStack.transaction_timeout,null);
		log(LogLevel.INFO,"new transaction-id: "+transaction_id.toString());
	}  


	// ************************ Public methods *************************

	/** Starts the TransactionServer. */
	public void listen() {
		if (statusIs(STATE_IDLE)) {
			log(LogLevel.TRACE,"start");
			changeStatus(STATE_WAITING);  
			sip_provider.addSelectiveListener(transaction_id,this); 
		}
	}  

	/** Sends a response message */
	public void respondWith(int code) {
		SipMessage resp=SipMessageFactory.createResponse(request,code,null,null);
		respondWith(resp);
	}  

	/** Sends a response message */
	public void respondWith(SipMessage resp) {
		response=resp;
		response.setConnectionId(connection_id);
		if (statusIs(STATE_TRYING) || statusIs(STATE_PROCEEDING)) {
			sip_provider.sendMessage(response);
			int code=response.getStatusLine().getCode();
			if (code>=100 && code<200 && statusIs(STATE_TRYING)) {
				changeStatus(STATE_PROCEEDING);
			}
			if (code>=200 && code<700) {
				changeStatus(STATE_COMPLETED);
				if (connection_id==null) {
					clearing_to=new Timer(clearing_to.getTime(),this);
					clearing_to.start();
				}
				else {
					log(LogLevel.TRACE,"clearing_to=0 for reliable transport");
					onTimeout(clearing_to);
				}
			}
		}
	}  

	/** Terminates the transaction. */
	public void terminate() {
		doTerminate();
		transaction_listener=null;
	}


	// *********************** Callback methods ************************

	/** From SipListener. It's fired from the SipProvider when a new message is received for to the present TransactionServer. */
	public void onReceivedMessage(SipProvider provider, SipMessage msg) {
		if (msg.isRequest()) {
			if (statusIs(STATE_WAITING)) {
				request=new SipMessage(msg);
				connection_id=msg.getConnectionId();
				TransactionServerId new_transaction_id=new TransactionServerId(request);
				if (!new_transaction_id.equals(transaction_id)) {
					sip_provider.removeSelectiveListener(transaction_id);
					sip_provider.addSelectiveListener(transaction_id=new_transaction_id,this); 
				}            
				changeStatus(STATE_TRYING);
				if (transaction_listener!=null) transaction_listener.onTransRequest(this,msg);
				return;
			}
			if (statusIs(STATE_PROCEEDING) || statusIs(STATE_COMPLETED)) {
				// retransmission of the last response
				log(LogLevel.TRACE,"response retransmission");
				sip_provider.sendMessage(response);
				return;
			}
		}
	}

	/** From TimerListener. It's fired from an active Timer. */
	public void onTimeout(Timer to) {
		try {
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
			clearing_to.halt();
			//clearing_to=null;
			sip_provider.removeSelectiveListener(transaction_id);
			changeStatus(STATE_TERMINATED);
		}
	}


	// ****************************** Logs *****************************

	/** Adds a new string to the default log. */
	protected void log(LogLevel level, String str) {
		if (logger!=null) logger.log(level,"TransactionServer#"+transaction_sqn+": "+str);  
	}

}

