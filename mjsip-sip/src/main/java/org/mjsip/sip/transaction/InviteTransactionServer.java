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
import org.mjsip.sip.message.SipMethods;
import org.mjsip.sip.message.SipResponses;
import org.mjsip.sip.provider.ConnectionId;
import org.mjsip.sip.provider.SipId;
import org.mjsip.sip.provider.SipProvider;
import org.slf4j.LoggerFactory;

/** INVITE server transaction as defined in RFC 3261 (Section 17.2.1).
  * <BR> An InviteTransactionServer is responsable to create a new SIP invite
  * transaction that starts with a INVITE message received by the SipProvider
  * and ends sending a final response.
  * <BR> The changes of the internal status and the received messages are fired
  * to the TransactionListener passed to the InviteTransactionServer object.
  * <BR> This implementation of InviteTransactionServer automatically generates
  * a "100 Trying" response when the INVITE message is received
  * (as suggested by RFC3261) 
  */
public class InviteTransactionServer extends TransactionServer {
	
	private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(InviteTransactionServer.class);

	/** Default behavior for automatically sending 100 Trying on INVITE. */
	//public static boolean AUTO_TRYING=true;


	/** the TransactionServerListener that captures the events fired by the InviteTransactionServer */
	InviteTransactionServerListener invite_ts_listener;

	/** last response message */
	//Message response=null;
	
	ScheduledFuture<?> retransmission_to;
	/** end timeout ("Timer H" in RFC 3261) */
	ScheduledFuture<?> end_to;
	/** clearing timeout ("Timer I" in RFC 3261) */
	//Timer clearing_to; 

	/** Whether automatically sending 100 Trying on INVITE. */
	boolean auto_trying;

	private long _retransmissionTimeout;

	// ************************** Costructors **************************

	/** Creates a new InviteTransactionServer. */
	public InviteTransactionServer(SipProvider sip_provider, InviteTransactionServerListener listener) {
		super(sip_provider);
		init(listener,SipId.createTransactionServerId(SipMethods.INVITE),null);
	}  
		
	/** Creates a new InviteTransactionServer for the already received INVITE request <i>invite</i>. */
	public InviteTransactionServer(SipProvider sip_provider, SipMessage invite, InviteTransactionServerListener listener) {
		super(sip_provider);
		request=new SipMessage(invite);
		init(listener,SipId.createTransactionServerId(request),request.getConnectionId());

		changeStatus(STATE_TRYING);
		sip_provider.addSelectiveListener(transaction_id,this);
		// automatically send "100 Tryng" response and go to STATE_PROCEEDING
		if (auto_trying) {
			SipMessage trying100 = sip_provider.messageFactory().createResponse(request, SipResponses.TRYING, null,
					null);
			respondWith(trying100); // this method makes it going automatically to STATE_PROCEEDING
		}
	}  

	/** Creates a new InviteTransactionServer for the already received INVITE request <i>invite</i>. */
	public InviteTransactionServer(SipProvider sip_provider, SipMessage invite, boolean auto_trying, InviteTransactionServerListener listener) {
		super(sip_provider);
		request=new SipMessage(invite);
		init(listener,SipId.createTransactionServerId(request),request.getConnectionId());      
		this.auto_trying=auto_trying;

		changeStatus(STATE_TRYING);
		sip_provider.addSelectiveListener(transaction_id,this);
		// automatically send "100 Tryng" response and go to STATE_PROCEEDING
		if (auto_trying) {
			SipMessage trying100 = sip_provider.messageFactory().createResponse(request, SipResponses.TRYING, null,
					null);
			respondWith(trying100); // this method makes it going automatically to STATE_PROCEEDING
		}
	}  

	/** Initializes it. */
	void init(InviteTransactionServerListener listener, SipId transaction_id, ConnectionId connection_id) {
		this.invite_ts_listener=listener;
		this.transaction_id=transaction_id;
		this.connection_id=connection_id;
		auto_trying=sip_provider.sipConfig().isAutoTrying();
		// init the timer just to set the timeout value and label, without listener (never started)
		if (LOG.isDebugEnabled()) {
			LOG.debug("new transaction-id: {}", transaction_id);
		}
	}   


	// ************************ Public methods *************************

	/** Whether automatically sending 100 Trying on INVITE. */
	public void setAutoTrying(boolean auto_trying) {
		this.auto_trying=auto_trying;
	}

	/** Starts the InviteTransactionServer. */
	@Override
	public void listen() {
		LOG.trace("start");
		if (statusIs(STATE_IDLE)) {
			changeStatus(STATE_WAITING);  
			//sip_provider.addSelectiveListener(new TransactionId(SipMethods.INVITE),this); 
			sip_provider.addSelectiveListener(transaction_id,this); 
		}
	}  

	/** Sends a response message */
	@Override
	public void respondWith(SipMessage resp) {
		response=resp;
		response.setConnectionId(connection_id);
		int code=response.getStatusLine().getCode();
		if (statusIs(STATE_TRYING) || statusIs(STATE_PROCEEDING)) sip_provider.sendMessage(response);         
		if (code>=100 && code<200 && statusIs(STATE_TRYING)) {
			changeStatus(STATE_PROCEEDING);
			return;
		}
		if (code>=200 && code<300 && (statusIs(STATE_TRYING) || statusIs(STATE_PROCEEDING))) {
			doTerminate();
			invite_ts_listener=null;
			return;
		}
		if (code>=300 && code<700 && (statusIs(STATE_TRYING) || statusIs(STATE_PROCEEDING))) {
			changeStatus(STATE_COMPLETED);
			// retransmission only in case of unreliable transport 
			if (connection_id==null) {
				scheduleRetransmission(sip_provider.sipConfig().getRetransmissionTimeout());
				//end_to=new Timer(end_to.getTime(),this);
				//end_to.start();
			}
			else {
				LOG.trace("No retransmissions for reliable transport ({})", connection_id);
				//onTimeout(end_to);
			}
			end_to = sip_provider.scheduler().schedule(sip_provider.sipConfig().getTransactionTimeout(),
					this::onEndTimeout);
		}
	}

	/** Method derived from interface SipListener.
	  * It's fired from the SipProvider when a new message is catch for to the present ServerTransaction. */
	@Override
	public void onReceivedMessage(SipProvider provider, SipMessage msg) {
		if (msg.isRequest()) {
			String req_method=msg.getRequestLine().getMethod();
			
			// invite received
			if (req_method.equals(SipMethods.INVITE)) {
				
				if (statusIs(STATE_WAITING)) {
					request=new SipMessage(msg);
					connection_id=request.getConnectionId();
					sip_provider.removeSelectiveListener(transaction_id);
					transaction_id=SipId.createTransactionServerId(request);
					sip_provider.addSelectiveListener(transaction_id,this); 
					changeStatus(STATE_TRYING);
					// automatically send "100 Tryng" response and go to STATE_PROCEEDING
					if (auto_trying) {
						SipMessage trying100 = sip_provider.messageFactory().createResponse(request,
								SipResponses.TRYING, null, null);
						respondWith(trying100); // this method makes it going automatically to STATE_PROCEEDING
					}
					if (invite_ts_listener!=null) invite_ts_listener.onTransRequest(this,msg);
					return;            
				}
				if (statusIs(STATE_PROCEEDING) || statusIs(STATE_COMPLETED)) {
					// retransmission of the last response
					sip_provider.sendMessage(response);
					return;
				}
			}
			// ack received
			if (req_method.equals(SipMethods.ACK) && statusIs(STATE_COMPLETED)) {
				if (retransmission_to != null)
					retransmission_to.cancel(false);
				end_to.cancel(false);
				changeStatus(STATE_CONFIRMED);
				if (invite_ts_listener!=null) invite_ts_listener.onTransFailureAck(this,msg);
				clearing_to = sip_provider.scheduler().schedule(sip_provider.sipConfig().getClearingTimeout(),
						this::onClearingTimeout);
				return;
			}
		}    
	}

	private void onRetransmissionTimeout() {
		if (statusIs(STATE_COMPLETED)) {
			LOG.debug("Retransmission timeout expired");
			scheduleRetransmission(sip_provider.retransmissionSlowdown(_retransmissionTimeout));
			sip_provider.sendMessage(response);
		}
	}

	private void scheduleRetransmission(long timeout) {
		_retransmissionTimeout = timeout;
		retransmission_to = sip_provider.scheduler().schedule(timeout, this::onRetransmissionTimeout);
	}

	private void onEndTimeout() {
		if (statusIs(STATE_COMPLETED)) {
			LOG.debug("End timeout expired");
			doTerminate();
			invite_ts_listener = null;
		}
	}   

	/** Method used to drop an active transaction */
	@Override
	public void terminate() {
		doTerminate();
		invite_ts_listener=null;
	}


	// *********************** Protected methods ***********************

	/** Moves to terminate state. */
	@Override
	protected void doTerminate() {
		if (!statusIs(STATE_TERMINATED)) {
			if (retransmission_to != null)
				retransmission_to.cancel(false);
			if (clearing_to != null)
				clearing_to.cancel(false);
			if (end_to != null)
				end_to.cancel(false);
			//if (statusIs(STATE_WAITING)) sip_provider.removeSelectiveListener(new TransactionId(SipMethods.INVITE));
			//else sip_provider.removeSelectiveListener(transaction_id);
			sip_provider.removeSelectiveListener(transaction_id);
			changeStatus(STATE_TERMINATED);
		}
	}

}
