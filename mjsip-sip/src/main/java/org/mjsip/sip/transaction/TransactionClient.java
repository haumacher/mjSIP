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
import org.mjsip.sip.provider.SipId;
import org.mjsip.sip.provider.SipProvider;
import org.slf4j.LoggerFactory;



/** Generic client transaction as defined in RFC 3261 (Section 17.1.2).
  * A TransactionClient is responsable to create a new SIP transaction, starting with a request message sent through the SipProvider and ending with a final response.<BR>
  * The changes of the internal status and the received messages are fired to the TransactionListener passed to the TransactionClient object.<BR>
  */
public class TransactionClient extends Transaction {
	
	private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(TransactionClient.class);

	/** The TransactionClientListener that captures the events fired by the TransactionClient */
	TransactionClientListener transaction_listener;

	protected long retransmissionTimeout;

	/** Retransmission timeout ("Timer E" in RFC 3261) */
	ScheduledFuture<?> retransmission_to;

	/** Transaction timeout ("Timer F" in RFC 3261) */
	ScheduledFuture<?> transaction_to;

	/** Clearing timeout ("Timer K" in RFC 3261) */
	ScheduledFuture<?> clearing_to;

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
		init(listener,SipId.createTransactionClientId(request));
	}
	
	/** Initializes it. */
	void init(TransactionClientListener listener, SipId transaction_id) {
		this.transaction_listener=listener;
		this.transaction_id=transaction_id;
		LOG.debug("new transaction-id: {}", transaction_id);
	}


	// ************************ Public methods *************************

	/** Starts the TransactionClient and sends the transaction request. */
	public void request() {
		LOG.trace("start");
		changeStatus(STATE_TRYING);

		startTransactionTimeout();

		sip_provider.addSelectiveListener(transaction_id,this);
		connection_id=sip_provider.sendMessage(request);

		// Retransmission only for unreliable transport
		if (connection_id == null) {
			LOG.debug("Starting retransmission timeout.");
			scheduleRetransmission(sip_provider.sipConfig().getRetransmissionTimeout());
		}
	}
		
	/** Terminates the transaction. */
	@Override
	public void terminate() {
		doTerminate();
		transaction_listener=null;
	}


	// *********************** Callback methods ************************

	/** From SipListener. It's fired from the SipProvider when a new message is received for to the present TransactionClient. */
	@Override
	public void onReceivedMessage(SipProvider provider, SipMessage msg) {
		if (msg.isResponse()) {
			int code=msg.getStatusLine().getCode();
			if (code>=100 && code<200 && (statusIs(STATE_TRYING) || statusIs(STATE_PROCEEDING))) {
				if (statusIs(STATE_TRYING)) changeStatus(STATE_PROCEEDING);
				if (transaction_listener!=null) transaction_listener.onTransProvisionalResponse(this,msg);
				return;
			}
			if (code>=200 && code<700 && (statusIs(STATE_TRYING) || statusIs(STATE_PROCEEDING))) {
				stopRetransmissionTimeout();
				stopTransactionTimeout();
				changeStatus(STATE_COMPLETED);
				if (transaction_listener!=null) {
					if (code<300) transaction_listener.onTransSuccessResponse(this,msg);
					else transaction_listener.onTransFailureResponse(this,msg);
				}
				if (connection_id==null) {
					startClearingTimeout();
				}
				else {
					// There is no clearing timeout for reliable transport.
					doTerminate();
				}
				return;
			}
		}
	}

	/**
	 * Event handler for the retransmission timeout.
	 */
	protected void onRetransmissionTimeout() {
		if (statusIs(STATE_TRYING) || statusIs(STATE_PROCEEDING)) {
			LOG.debug("Retransmission timeout expired");

			sip_provider.sendMessage(request);

			scheduleRetransmission(sip_provider.retransmissionSlowdown(retransmissionTimeout));
		}
	}
	
	protected final void scheduleRetransmission(long timeout) {
		retransmissionTimeout = timeout;
		retransmission_to = sip_provider.scheduler().schedule(timeout, this::onRetransmissionTimeout);
	}

	private void startTransactionTimeout() {
		long timeout = sip_provider.sipConfig().getTransactionTimeout();
		LOG.debug("Starting transaction timeout: {}ms", timeout);

		transaction_to = sip_provider.scheduler().schedule(timeout, this::onTransactionTimeout);
	}

	/**
	 * Event handler for the transaction timeout.
	 */
	protected void onTransactionTimeout() {
		LOG.debug("Transaction timeout expired.");
		doTerminate();

		if (transaction_listener != null) {
			transaction_listener.onTransTimeout(this);
			transaction_listener = null;
		}
	}

	private void startClearingTimeout() {
		long timeout = sip_provider.sipConfig().getClearingTimeout();
		LOG.debug("Starting clearing timeout: {}ms", timeout);
		clearing_to = sip_provider.scheduler().schedule(timeout, this::onClearingTimeout);
	}

	/**
	 * Event handler for the clearing timeout.
	 */
	protected void onClearingTimeout() {
		LOG.debug("Clearing timeout expired.");
		doTerminate();
	}

	// *********************** Protected methods ***********************

	/** Moves to terminate state. */
	protected void doTerminate() {
		if (!statusIs(STATE_TERMINATED)) {
			stopRetransmissionTimeout();
			stopTransactionTimeout();     
			stopClearingTimeout();
			sip_provider.removeSelectiveListener(transaction_id);
			changeStatus(STATE_TERMINATED);
		}
	}

	private void stopTransactionTimeout() {
		if (transaction_to != null) {
			transaction_to.cancel(false);
			transaction_to = null;
		}
	}

	private void stopRetransmissionTimeout() {
		if (retransmission_to != null) {
			retransmission_to.cancel(false);
			retransmission_to = null;
		}
	}

	private void stopClearingTimeout() {
		if (clearing_to != null) {
			clearing_to.cancel(false);
			clearing_to = null;
		}
	}

}
