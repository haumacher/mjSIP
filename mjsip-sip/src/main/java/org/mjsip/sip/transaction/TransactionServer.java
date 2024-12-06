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

import java.util.concurrent.ScheduledFuture;

import org.mjsip.sip.message.SipMessage;
import org.mjsip.sip.message.SipResponses;
import org.mjsip.sip.provider.ConnectionId;
import org.mjsip.sip.provider.SipId;
import org.mjsip.sip.provider.SipProvider;
import org.slf4j.LoggerFactory;

/**
 * Generic server transaction as defined in RFC 3261 (Section 17.2.2).
 * 
 * <p>
 * A {@link TransactionServer} is responsible to create a new SIP transaction that starts with a
 * request message received by the SipProvider and ends sending a final response.<BR>
 * The changes of the internal status and the received messages are fired to the TransactionListener
 * passed to the TransactionServer object.<BR>
 * When constructing a new TransactionServer, the transaction type is passed as String parameter to
 * the constructor (e.g. "CANCEL", "BYE", etc..)
 * </p>
 */
public class TransactionServer extends Transaction {
	
	private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(TransactionServer.class);

	/** The TransactionServerListener that captures the events fired by the TransactionServer */
	TransactionServerListener transaction_listener;

	/** Last response message */
	SipMessage response;
	
	/** Clearing timeout ("Timer J" in RFC 3261) */
	ScheduledFuture<?> clearing_to;


	// ************************** Constructors **************************

	/** Creates a new TransactionServer. */
	protected TransactionServer(SipProvider sip_provider) {
		super(sip_provider);
		transaction_listener=null;
		response=null;
	} 

	/** Creates a new TransactionServer of type <i>method</i>. */
	public TransactionServer(SipProvider sip_provider, String method, TransactionServerListener listener) {
		super(sip_provider);
		init(listener,SipId.createTransactionServerId(method),null);
	}  

	/** Creates a new TransactionServer for the already received request <i>req</i>. */
	public TransactionServer(SipProvider provider, SipMessage req, TransactionServerListener listener) {
		super(provider);
		request=new SipMessage(req);
		init(listener,SipId.createTransactionServerId(request),request.getConnectionId());
		
		changeStatus(STATE_TRYING);
		sip_provider.addSelectiveListener(transaction_id,this); 
	}  

	/** Inits the transaction server. */
	protected void init(TransactionServerListener listener, SipId transaction_id, ConnectionId connection_id) {
		this.transaction_listener=listener;
		this.transaction_id=transaction_id;
		this.connection_id=connection_id;
		this.response=null;
		// init the timer just to set the timeout value and label, without listener (never started)
		if (LOG.isDebugEnabled()) {
			LOG.debug("Starting transaction {}:{}", transaction_id, request);
		}
	}  


	// ************************ Public methods *************************

	/** Starts the TransactionServer. */
	public void listen() {
		if (statusIs(STATE_IDLE)) {
			changeStatus(STATE_WAITING);  
			sip_provider.addSelectiveListener(transaction_id,this); 
		}
	}  

	/**
	 * Sends a response message
	 * 
	 * @param code
	 *        See {@link SipResponses}.
	 */
	public void respondWith(int code) {
		SipMessage resp=sip_provider.messageFactory().createResponse(request,code,null,null);
		respondWith(resp);
	}  

	/** Sends a response message */
	public void respondWith(SipMessage resp) {
		if (LOG.isDebugEnabled()) {
			LOG.debug("Transaction {} response: {}", transaction_id, resp);
		}

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
					clearing_to = sip_provider.scheduler().schedule(sip_provider.sipConfig().getRetransmissionTimeout(),
							this::onClearingTimeout);
				} else {
					// Reliable transport
					doTerminate();
				}
			}
		}
	}  

	/** Terminates the transaction. */
	@Override
	public void terminate() {
		doTerminate();
	}

	// *********************** Callback methods ************************

	/** From SipListener. It's fired from the SipProvider when a new message is received for to the present TransactionServer. */
	@Override
	public void onReceivedMessage(SipProvider provider, SipMessage msg) {
		if (LOG.isDebugEnabled()) {
			LOG.debug("Transaction {}:{}", transaction_id, msg);
		}

		if (msg.isRequest()) {
			if (statusIs(STATE_WAITING)) {
				request=new SipMessage(msg);
				connection_id=msg.getConnectionId();
				SipId new_transaction_id = SipId.createTransactionServerId(request);
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
				LOG.trace("response retransmission");
				sip_provider.sendMessage(response);
				return;
			}
		}
	}

	protected void onClearingTimeout() {
		if (LOG.isDebugEnabled()) {
			LOG.debug("Transaction timeout reached: {}", transaction_id);
		}
		doTerminate();
	}   

	// *********************** Protected methods ***********************

	/** Moves to terminate state. */
	protected void doTerminate() {
		if (!statusIs(STATE_TERMINATED)) {
			if (clearing_to != null)
				clearing_to.cancel(false);
			//clearing_to=null;
			sip_provider.removeSelectiveListener(transaction_id);
			transaction_listener = null;
			changeStatus(STATE_TERMINATED);

			if (LOG.isDebugEnabled()) {
				LOG.debug("Transaction terminated: {}", transaction_id);
			}
		}
	}

}

