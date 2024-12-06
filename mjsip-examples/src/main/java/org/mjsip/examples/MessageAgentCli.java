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

package org.mjsip.examples;


import org.mjsip.sip.address.NameAddress;
import org.mjsip.sip.provider.SipProvider;
import org.mjsip.ua.MessageAgent;
import org.mjsip.ua.MessageAgentListener;
import org.mjsip.ua.registration.RegistrationOptions;
import org.slf4j.LoggerFactory;


/** Simple command-line short-message UA.
  * It allows a user to send and receive short messages, using a command-line interface.
  */
public class MessageAgentCli implements MessageAgentListener {
	
	private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(MessageAgentCli.class);
	
	/** Message Agent */
	MessageAgent ma;

	/** Remote user. */
	NameAddress remote_user;
	
	
	/**
	 * Creates a new MA.
	 */
	public MessageAgentCli(SipProvider sip_provider, RegistrationOptions regOptions) {
		ma = new MessageAgent(sip_provider, regOptions,this);
		ma.receive();
	}

	
	/** Gets the remote peer of the last received/sent message. */
	public String getRemoteUser() {
		return remote_user.toString();
	}

	/** Sends a new message. */
	public void send(String recipient, String subject, String text) {
		ma.send(recipient,subject,text);
	}   


	// *********************** callback functions *********************
 
	/** When a new Message is received. */
	@Override
	public void onMaReceivedMessage(MessageAgent ma, NameAddress sender, NameAddress recipient, String subject, String content_type, String content) {
		remote_user=sender;
		LOG.info("NEW MESSAGE:");
		LOG.info("From: {}", sender);
		if (subject != null)
			LOG.info("Subject: {}", subject);
		LOG.info("Content: {}", content);
	}

	/** When a message delivery successes. */
	@Override
	public void onMaDeliverySuccess(MessageAgent ma, NameAddress recipient, String subject, String result) {
		//LOG.info("Delivery success: {}", result);
	}

	/** When a message delivery fails. */
	@Override
	public void onMaDeliveryFailure(MessageAgent ma, NameAddress recipient, String subject, String result) {
		//LOG.info("Delivery failure: {}", result);
	}

}
