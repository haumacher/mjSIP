/*
 * Copyright (C) 2005 Luca Veltri - University of Parma - Italy
 * 
 * This source code is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This source code is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this source code; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 * 
 * Author(s):
 * Luca Veltri (luca.veltri@unipr.it)
 */

package org.mjsip.ua;



import org.mjsip.sip.address.NameAddress;
import org.mjsip.sip.message.SipMessage;
import org.mjsip.sip.message.SipMethods;
import org.mjsip.sip.message.SipResponses;
import org.mjsip.sip.provider.SipId;
import org.mjsip.sip.provider.SipProvider;
import org.mjsip.sip.provider.SipProviderListener;
import org.mjsip.sip.transaction.TransactionClient;
import org.mjsip.sip.transaction.TransactionClientListener;
import org.mjsip.sip.transaction.TransactionServer;
import org.mjsip.ua.registration.RegistrationOptions;
import org.slf4j.LoggerFactory;


/** Simple Message Agent (MA).
  * <br>
  * It allows a user to send and receive short messages.
  */
public class MessageAgent implements SipProviderListener, TransactionClientListener {
	
	private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(MessageAgent.class);

	/** UserProfile */
	protected final RegistrationOptions _regOptions;

	/** SipProvider */
	protected final SipProvider sip_provider;

	/** Message listener */
	protected final MessageAgentListener listener;

	
	/** Costructs a new MessageAgent. */
	public MessageAgent(SipProvider sip_provider, RegistrationOptions regOptions, MessageAgentListener listener) {
		this.sip_provider=sip_provider;
		this.listener=listener;
		this._regOptions=regOptions;
	}   

	
	/** Sends a new text message. */
	public void send(String recipient, String subject, String content) {
		send(recipient,subject,"application/text",content.getBytes());
	}   


	/** Sends a new message. */
	public void send(String recipient, String subject, String content_type, byte[] content) {
		NameAddress to_uri=NameAddress.parse(recipient);
		NameAddress from_uri=_regOptions.getUserURI();
		SipMessage req=sip_provider.messageFactory().createMessageRequest(to_uri,from_uri,sip_provider.pickCallId(),subject,content_type,content);
		TransactionClient t=new TransactionClient(sip_provider,req,this);
		t.request();
	}


	/** Waits for incoming message. */
	public void receive() {
		sip_provider.addSelectiveListener(SipId.createMethodId(SipMethods.MESSAGE),this);
	} 
	

	/** Stops receiving messages. */
	public void halt() {
		sip_provider.removeSelectiveListener(SipId.createMethodId(SipMethods.MESSAGE));  
	} 


	// ******************* Callback functions implementation ********************

	/** When a new Message is received by the SipProvider. */
	@Override
	public void onReceivedMessage(SipProvider provider, SipMessage msg) {
		//printLog("Message received: "+msg.getFirstLine().substring(0,msg.toString().indexOf('\r')));
		if (msg.isRequest()) {
			(new TransactionServer(sip_provider,msg,null)).respondWith(sip_provider.messageFactory().createResponse(msg,SipResponses.OK,null,null));
			NameAddress sender=msg.getFromHeader().getNameAddress();
			NameAddress recipient=msg.getToHeader().getNameAddress();
			String subject=null;
			if (msg.hasSubjectHeader()) subject=msg.getSubjectHeader().getSubject();
			String content_type=null;
			if (msg.hasContentTypeHeader()) content_type=msg.getContentTypeHeader().getContentType();
			String content=msg.getStringBody();
			if (listener!=null) listener.onMaReceivedMessage(this,sender,recipient,subject,content_type,content);
		}
	}
 

	/** When the TransactionClient goes into the "Completed" state receiving a 2xx response */
	@Override
	public void onTransSuccessResponse(TransactionClient tc, SipMessage resp)  {
		onDeliverySuccess(tc,resp.getStatusLine().getReason());
	}

	/** When the TransactionClient goes into the "Completed" state receiving a 300-699 response */
	@Override
	public void onTransFailureResponse(TransactionClient tc, SipMessage resp)  {
		onDeliveryFailure(tc,resp.getStatusLine().getReason());
	}
	 
	/** When the TransactionClient is (or goes) in "Proceeding" state and receives a new 1xx provisional response */
	@Override
	public void onTransProvisionalResponse(TransactionClient tc, SipMessage resp) {
		// do nothing.
	}
		
	/** When the TransactionClient goes into the "Terminated" state, caused by transaction timeout */
	@Override
	public void onTransTimeout(TransactionClient tc) {
		onDeliveryFailure(tc,"Timeout");
	}
 
	/** When the delivery successes. */
	private void onDeliverySuccess(TransactionClient tc, String result) {
		LOG.info("Message successfully delivered ({}).", result);
		SipMessage req=tc.getRequestMessage();
		NameAddress recipient=req.getToHeader().getNameAddress();
		String subject=null;
		if (req.hasSubjectHeader()) subject=req.getSubjectHeader().getSubject();
		if (listener!=null) listener.onMaDeliverySuccess(this,recipient,subject,result);
	}

	/** When the delivery fails. */
	private void onDeliveryFailure(TransactionClient tc, String result) {
		LOG.info("Message delivery failed ({}).", result);
		SipMessage req=tc.getRequestMessage();
		NameAddress recipient=req.getToHeader().getNameAddress();
		String subject=null;
		if (req.hasSubjectHeader()) subject=req.getSubjectHeader().getSubject();
		if (listener!=null) listener.onMaDeliveryFailure(this,recipient,subject,result);
	}

}
