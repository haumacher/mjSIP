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
import org.mjsip.sip.provider.ConnectionId;
import org.mjsip.sip.provider.SipId;
import org.mjsip.sip.provider.SipProvider;
import org.slf4j.LoggerFactory;

/**
 * ACK server transaction should follow an INVITE server transaction within an INVITE Dialog in a
 * SIP UAC. The AckTransactionServer sends the final response message and retransmits it several
 * times until the method terminate() is called or the transaction timeout occurs.
 */ 
public class AckTransactionServer extends Transaction {
	
	private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(AckTransactionServer.class);

	/** the TransactionServerListener that captures the events fired by the AckTransactionServer */
	private AckTransactionServerListener transaction_listener;

	/** last response message */
	private final SipMessage response;
	
	/** retransmission timeout */
	private long retransmissionTimeout;

	private ScheduledFuture<?> retransmission_to;

	/** transaction timeout */
	private ScheduledFuture<?> transaction_to;


	/**
	 * Creates a {@link AckTransactionServer}.
	 * 
	 * <p>
	 * The AckTransactionServer starts sending a the response message <i>resp</i>.
	 * </p>
	 * <p>
	 * It periodically re-sends the response if no ACK request is received. The response is also
	 * sent each time a duplicate INVITE request is received.
	 * </p>
	 */
	public AckTransactionServer(SipProvider sip_provider, SipMessage invite, SipMessage resp, AckTransactionServerListener listener) {
		this(sip_provider, null, invite, resp, listener);
	}  

	/**
	 * Creates a {@link AckTransactionServer}.
	 * <p>
	 * The AckTransactionServer starts sending a the response message <i>resp</i>.
	 * </p>
	 * <p>
	 * It periodically re-sends the response if no ACK request is received. The response is also
	 * sent each time a duplicate INVITE request is received.
	 * </p>
	 * <p>
	 * The response is sent through the connection <i>conn_id</i>.
	 * </p>
	 */
	public AckTransactionServer(SipProvider sip_provider, ConnectionId connection_id, SipMessage invite, SipMessage resp, AckTransactionServerListener listener) {
		super(sip_provider);

		this.transaction_listener=listener;
		this.connection_id=connection_id;
		this.response=resp;
		response.setConnectionId(connection_id);
		transaction_id=SipId.createTransactionServerId(invite);
		// init the timer just to set the timeout value and label, without listener (never started)
		// (CHANGE-040905) now timeouts are started when method respond() is called
		//transaction_to=new Timer(transaction_to.getTime(),this);
		//transaction_to.start();
		//if (connection_id==null)
		//{  retransmission_to=new Timer(retransmission_to.getTime(),this);
		//   retransmission_to.start();
		//}
		LOG.info("new transaction-id: {}", transaction_id);
	}    

	/** Starts the AckTransactionServer. */
	public void respond() {
		LOG.trace("start");
		changeStatus(STATE_PROCEEDING); 
		// (CHANGE-071209) add sip provider listener
		sip_provider.addSelectiveListener(transaction_id,this);
		//transaction_id=null; // it is not required since no SipProviderListener is implemented 
		// (CHANGE-040905) now timeouts are started when method respond() is called
		transaction_to = sip_provider.scheduler().schedule(sip_provider.sipConfig().getTransactionTimeout(),
				this::onTransactionTimeout);

		if (connection_id == null) {
			scheduleRetransmission(sip_provider.sipConfig().getRetransmissionTimeout());
		}

		sip_provider.sendMessage(response); 
	}

	/** From SipProviderListener. When a new SipMessage is received by the SipProvider. */
	@Override
	public void onReceivedMessage(SipProvider sip_provider, SipMessage msg) {
		if (statusIs(STATE_PROCEEDING) && msg.isRequest()) {
			if (msg.isInvite()) {
				LOG.trace("response retransmission");
				sip_provider.sendMessage(response);
			}
			/*else
			if (msg.isAck()) {
				doTerminate();
				if (transaction_listener!=null) transaction_listener.onTransAck(this,msg);
				transaction_listener=null;
			}*/
			else
				LOG.warn("{} method erroneously passed to this trasaction", msg.getRequestLine().getMethod());
		}
	}

	private void onTransactionTimeout() {
		LOG.info("Transaction timeout expired");
		doTerminate();
		// retransmission_to=null;
		// transaction_to=null;
		if (transaction_listener != null) {
			transaction_listener.onTransAckTimeout(this);
		}
	}

	private void onRetransmissionTimeout() {
		LOG.info("Retransmission timeout expired");

		scheduleRetransmission(sip_provider.retransmissionSlowdown(retransmissionTimeout));

		sip_provider.sendMessage(response);
	}

	private void scheduleRetransmission(long timeout) {
		retransmissionTimeout = timeout;
		retransmission_to = sip_provider.scheduler().schedule(timeout, this::onRetransmissionTimeout);
	}

	/** Method used to drop an active transaction. */
	@Override
	public void terminate() {
		doTerminate();
		transaction_listener=null;
		//retransmission_to=null;
		//transaction_to=null;
  }


	// *********************** Protected methods ***********************

	/** Moves to terminate state. */
	protected void doTerminate() {
		if (!statusIs(STATE_TERMINATED)) {
			changeStatus(STATE_TERMINATED);
			if (retransmission_to != null) {
				retransmission_to.cancel(false);
			}
			if (transaction_to != null) {
				transaction_to.cancel(false);
			}
			//retransmission_to=null;
			//transaction_to=null;
			sip_provider.removeSelectiveListener(transaction_id);
		}
	}

}
