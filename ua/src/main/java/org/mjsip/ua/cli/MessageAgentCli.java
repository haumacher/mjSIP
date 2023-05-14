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

package org.mjsip.ua.cli;


import org.mjsip.sip.address.NameAddress;
import org.mjsip.sip.address.SipURI;
import org.mjsip.sip.call.RegistrationClient;
import org.mjsip.sip.call.RegistrationClientListener;
import org.mjsip.sip.provider.SipProvider;
import org.mjsip.ua.MessageAgent;
import org.mjsip.ua.MessageAgentListener;
import org.mjsip.ua.UserAgentProfile;
import org.slf4j.LoggerFactory;


/** Simple command-line short-message UA.
  * It allows a user to send and receive short messages, using a command-line interface.
  */
public class MessageAgentCli implements RegistrationClientListener, MessageAgentListener {
	
	private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(MessageAgentCli.class);
	
	/** Message Agent */
	MessageAgent ma;

	/** RegistrationClient */
	RegistrationClient rc;

	/** Remote user. */
	NameAddress remote_user;
	
	
	/** Creates a new MA. */
	public MessageAgentCli(SipProvider sip_provider, UserAgentProfile user_profile) {
		ma=new MessageAgent(sip_provider,user_profile,this);
		ma.receive();
		if (user_profile.do_register || user_profile.do_unregister || user_profile.do_unregister_all) {
			rc=new RegistrationClient(sip_provider,new SipURI(user_profile.registrar),user_profile.getUserURI(),this);
		}
	}

	
	/** Gets the remote peer of the last received/sent message. */
	public String getRemoteUser() {
		return remote_user.toString();
	}


	/** Register with the registrar server. */
	public void register(int expire_time) {
		LOG.info("REGISTRATION");
		rc.register(expire_time);
	}


	/** Unregister with the registrar server */
	public void unregister() {
		LOG.info("UNREGISTER the contact URI");
		rc.unregister();
	}


	/** Unregister all contacts with the registrar server */
	public void unregisterall() {
		LOG.info("UNREGISTER ALL contact URIs");
		rc.unregisterall();
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
		LOG.info("From: " + sender);
		if (subject != null)
			LOG.info("Subject: " + subject);
		LOG.info("Content: " + content);
	}

	/** When a message delivery successes. */
	@Override
	public void onMaDeliverySuccess(MessageAgent ma, NameAddress recipient, String subject, String result) {
		//LOG.info("Delivery success: "+result);
	}

	/** When a message delivery fails. */
	@Override
	public void onMaDeliveryFailure(MessageAgent ma, NameAddress recipient, String subject, String result) {
		//LOG.info("Delivery failure: "+result);
	}

	/** When a UA has been successfully (un)registered. */
	@Override
	public void onRegistrationSuccess(RegistrationClient rc, NameAddress target, NameAddress contact, int expires, String result) {
		LOG.info("Registration success: expires="+expires+": "+result);
	}

	/** When a UA failed on (un)registering. */
	@Override
	public void onRegistrationFailure(RegistrationClient rc, NameAddress target, NameAddress contact, String result) {
		LOG.info("Registration failure: "+result);
	}

}
