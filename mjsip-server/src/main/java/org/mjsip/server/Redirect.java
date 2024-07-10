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

package org.mjsip.server;


import java.util.Vector;

import org.mjsip.config.MetaConfig;
import org.mjsip.config.OptionParser;
import org.mjsip.sip.header.MultipleHeader;
import org.mjsip.sip.header.SipHeaders;
import org.mjsip.sip.message.SipMessage;
import org.mjsip.sip.message.SipResponses;
import org.mjsip.sip.provider.SipConfig;
import org.mjsip.sip.provider.SipProvider;
import org.mjsip.time.ConfiguredScheduler;
import org.mjsip.time.SchedulerConfig;
import org.slf4j.LoggerFactory;
import org.zoolu.util.Flags;


/** Class Redirect implement a SIP edirect server.
  * It extends class Registrar. A Redirect can work as simply SIP redirect,
  * or it can handle calls for registered users. 
  */
public class Redirect extends Registrar {
	
	private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(Redirect.class);

	/** Costructs a new Redirect that acts also as location server for registered users. */
	public Redirect(SipProvider provider, ServerProfile server_profile) {
		super(provider,server_profile);
	}
		
	/** When a new request message is received for a local user */
	@Override
	public void processRequestToLocalUser(SipMessage msg) {
		LOG.debug("inside processRequestToLocalUser(msg)");
		
		// message targets
		Vector<String> contacts=getTargets(msg);

		if (contacts.isEmpty()) {
			LOG.info("No target found, message discarded");
			if (!msg.isAck()) sip_provider.sendMessage(sip_provider.messageFactory().createResponse(msg,SipResponses.NOT_FOUND,null,null));
			return;
		} 
					 
		LOG.debug("message will be redirect to all user's contacts");         
		// create the response with all contact URIs, and send it 
		MultipleHeader mc=new MultipleHeader(SipHeaders.Contact,contacts);
		mc.setCommaSeparated(true);
		SipMessage resp=sip_provider.messageFactory().createResponse(msg,SipResponses.MOVED_TEMPORARILY,null,null);
		resp.setContacts(mc);
		sip_provider.sendMessage(resp);      
	}
	
	/** When a new request message is received for a remote UA */
	@Override
	public void processRequestToRemoteUA(SipMessage msg) {
		LOG.debug("inside processRequestToRemoteUA(msg)");
		LOG.info("request not for local server");
		if (!msg.isAck()) sip_provider.sendMessage(sip_provider.messageFactory().createResponse(msg,SipResponses.NOT_FOUND,null,null));
		else LOG.info("message discarded");
	}   

	/** When a new response message is received */
	@Override
	public void processResponse(SipMessage resp) {
		LOG.debug("inside processResponse(msg)");
		LOG.info("request not for local server: message discarded");
	}
	
	// ****************************** MAIN *****************************

	/** The main method. */
	public static void main(String[] args) {
		Flags flags=new Flags(Redirect.class.getName(), args);
		
		SipConfig sipConfig = new SipConfig();
		SchedulerConfig schedulerConfig = new SchedulerConfig();
		ServerProfile server_profile=new ServerProfile();

		MetaConfig metaConfig = OptionParser.parseOptions(args, ".mjsip-redirect", sipConfig, schedulerConfig, server_profile);
		
		sipConfig.normalize();
		server_profile.normalize();
						
		SipProvider sip_provider=new SipProvider(sipConfig, new ConfiguredScheduler(schedulerConfig));

		new Redirect(sip_provider,server_profile);      
	}
  
}
