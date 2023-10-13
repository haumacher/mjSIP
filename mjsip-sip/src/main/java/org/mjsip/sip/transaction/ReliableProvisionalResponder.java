/*
 * Copyright (C) 2012 Luca Veltri - University of Parma - Italy
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



import java.util.Vector;

import org.mjsip.sip.header.CSeqHeader;
import org.mjsip.sip.header.RAckHeader;
import org.mjsip.sip.header.RSeqHeader;
import org.mjsip.sip.header.RequireHeader;
import org.mjsip.sip.message.SipMessage;
import org.mjsip.sip.provider.SipConfig;
import org.mjsip.sip.provider.SipStack;
import org.slf4j.LoggerFactory;
import org.zoolu.util.Random;
import org.zoolu.util.Timer;
import org.zoolu.util.TimerListener;



/** Reliable responder for provisional responses.
  * Provisional responses (with response code &gt; 100 and &lt; 200) are retransmitted
  * with an exponential backoff until a corrensponding PRACK message is received (RFC 3262).
  * <p>
  * Ougoing reliable provisional (1xx) response should be passed to this ReliableProvisionalResponder through the method {@link ReliableProvisionalResponder#respond(SipMessage)}.
  * Incoming PRACK messages should be passed to this ReliableProvisionalResponder through the method {@link ReliableProvisionalResponder#processPrack(SipMessage)}.
  */ 
public class ReliableProvisionalResponder {
	
	private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(ReliableProvisionalResponder.class);

	/** Whether initializing RSeq = 1; note that RFC 3262 specifies that the initial value of RSeq should be chosen uniformly between 1 and 2^31 - 1 */
	public static final boolean DEBUG_RSEQ_INIT1=true;

	/** Invite transaction server */
	InviteTransactionServer invite_ts;
	
	/** Queue of response messages to be sent */
	Vector responses=new Vector();
	
	/** Retransmission timeout */
	Timer retransmission_to=null;
	/** Transaction timeout */
	Timer transaction_to=null;

	/** ReliableProvisionalResponderListener that captures the ReliableProvisionalResponder events */
	ReliableProvisionalResponderListener listener;

	/** RSeq counter */
	long rseq_counter;
	
	/** Timer listener */
	TimerListener this_timer_listener;

	private final SipConfig sipConfig;

	/** Creates a new ReliableProvisionalResponder.*/
	public ReliableProvisionalResponder(SipConfig sipConfig, InviteTransactionServer invite_ts, ReliableProvisionalResponderListener listener) {
		this.sipConfig = sipConfig;
		this.invite_ts=invite_ts;
		this.listener=listener;
		this.rseq_counter=(DEBUG_RSEQ_INIT1)? 1 : Random.nextLong(2147483648L); // chosen uniformly between 1 and 2^31 - 1
		this_timer_listener=new TimerListener() {
			@Override
			public void onTimeout(Timer t) {
				processTimeout(t);
			}
		};
		LOG.info("new ReliableProvisionalResponder has been created");
	}    


	/** Sends a new 1xx response.
	  * It actually adds the response to the output queue, waiting for the confirmation of the previously queued responses. 
	  * @param resp the 1xx resp */
	public void respond(SipMessage resp) {
		LOG.debug("respond()");
		if (resp.hasRequireHeader()) {
			RequireHeader reqh=resp.getRequireHeader();
			if (!reqh.hasOptionTag(SipStack.OTAG_100rel)) {
				reqh.addOptionTag(SipStack.OTAG_100rel);
				resp.setRequireHeader(reqh);
			}
		}
		else {
			resp.setRequireHeader(new RequireHeader(SipStack.OTAG_100rel));
		}
		resp.setRSeqHeader(new RSeqHeader(rseq_counter++));
		scheduleResponse(resp);
	}  


	/** Processes a new received PRACK message.
	  * @param prack the received PRACK */
	public synchronized void processPrack(SipMessage prack) {
		LOG.debug("processPrack()");
		if (responses.size()>0) {
			SipMessage resp=(SipMessage)responses.elementAt(0);
			CSeqHeader sh=resp.getCSeqHeader();
			RAckHeader rh=prack.getRAckHeader();
			if (rh!=null && rh.getCSeqSequenceNumber()==sh.getSequenceNumber() && rh.getCSeqMethod().equals(sh.getMethod()) && rh.getRAckSequenceNumber()==resp.getRSeqHeader().getSequenceNumber()) {
				stopResponseRetransmission();
				responses.removeElementAt(0);
				(new TransactionServer(invite_ts.getSipProvider(),prack,null)).respondWith(200);
				if (listener!=null) listener.onReliableProvisionalResponseConfirmation(this,resp,prack);
				if (retransmission_to==null && hasPendingResponses()) sendNextResponse();
			}
			else LOG.warn(prack.getRequestLine().getMethod()+" confirmation received for past response?");
		}
		else LOG.warn(prack.getRequestLine().getMethod()+" no provisional response waiting for confirmation has been found");
	}


	/** Whether there are some responses that have not been confirmed yet though a PRACK message.
	  * @return true if one or more responses are still waiting for a confirmation */
	public boolean hasPendingResponses() {
		return responses.size()>0;
	}


	/** Terminates any retransmission. */
	public void terminate() {
		stopResponseRetransmission();
		listener=null;
	}


	/** Puts a message in the output queue. */
	private synchronized void scheduleResponse(SipMessage resp) {
		responses.addElement(resp);
		if (retransmission_to==null) sendNextResponse();
	}  


	/** Sends the head-of-line response. */
	private synchronized void sendNextResponse() {
		transaction_to=new Timer(sipConfig.transaction_timeout,this_timer_listener);
		transaction_to.start();
		retransmission_to=new Timer(sipConfig.retransmission_timeout,this_timer_listener);
		retransmission_to.start();
		SipMessage resp=(SipMessage)responses.elementAt(0);
		invite_ts.respondWith(resp); 
	}  


	/** When an active timer expires. */
	private synchronized void processTimeout(Timer to) {
		try {
			if (to.equals(retransmission_to)) {
				LOG.info("Retransmission timeout expired");
				long timeout=2*retransmission_to.getTime();
				if (timeout>sipConfig.max_retransmission_timeout) timeout=sipConfig.max_retransmission_timeout;
				retransmission_to=new Timer(timeout,this_timer_listener);
				retransmission_to.start();
				SipMessage resp=(SipMessage)responses.elementAt(0);
				invite_ts.respondWith(resp); 
			}
			else
			if (to.equals(transaction_to)) {
				LOG.info("Transaction timeout expired");
				stopResponseRetransmission();
				SipMessage resp=(SipMessage)responses.elementAt(0);
				responses.removeElementAt(0);
				if (listener!=null) listener.onReliableProvisionalResponseTimeout(this,resp);
				if (responses.size()>0) sendNextResponse();
			}  
		}
		catch (Exception e) {
			LOG.info("Exception.", e);
		}
	}   


	/** Stops current response retransmission. */
	private synchronized void stopResponseRetransmission() {
		if (retransmission_to!=null) retransmission_to.halt();
		if (transaction_to!=null) transaction_to.halt();  
		retransmission_to=null;
		transaction_to=null;
	}

}
