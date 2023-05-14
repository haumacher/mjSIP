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

import org.mjsip.sip.header.MultipleHeader;
import org.mjsip.sip.header.SipHeaders;
import org.mjsip.sip.message.SipMessage;
import org.mjsip.sip.message.SipMessageFactory;
import org.mjsip.sip.provider.SipProvider;
import org.mjsip.sip.provider.SipStack;
import org.zoolu.util.Flags;
import org.zoolu.util.LogLevel;


/** Class Redirect implement a SIP edirect server.
  * It extends class Registrar. A Redirect can work as simply SIP redirect,
  * or it can handle calls for registered users. 
  */
public class Redirect extends Registrar {
	
	/** Costructs a new Redirect that acts also as location server for registered users. */
	public Redirect(SipProvider provider, ServerProfile server_profile) {
		super(provider,server_profile);
	}
		
	/** When a new request message is received for a local user */
	public void processRequestToLocalUser(SipMessage msg) {
		log(LogLevel.DEBUG,"inside processRequestToLocalUser(msg)");
		
		// message targets
		Vector contacts=getTargets(msg);

		if (contacts.isEmpty()) {
			log(LogLevel.INFO,"No target found, message discarded");
			if (!msg.isAck()) sip_provider.sendMessage(SipMessageFactory.createResponse(msg,404,null,null));
			return;
		} 
					 
		log(LogLevel.DEBUG,"message will be redirect to all user's contacts");         
		// create the response with all contact URIs, and send it 
		MultipleHeader mc=new MultipleHeader(SipHeaders.Contact,contacts);
		mc.setCommaSeparated(true);
		SipMessage resp=SipMessageFactory.createResponse(msg,302,null,null);
		resp.setContacts(mc);
		sip_provider.sendMessage(resp);      
	}
	
	/** When a new request message is received for a remote UA */
	public void processRequestToRemoteUA(SipMessage msg) {
		log(LogLevel.DEBUG,"inside processRequestToRemoteUA(msg)");
		log(LogLevel.INFO,"request not for local server");
		if (!msg.isAck()) sip_provider.sendMessage(SipMessageFactory.createResponse(msg,404,null,null));
		else log(LogLevel.INFO,"message discarded");
	}   

	/** When a new response message is received */
	public void processResponse(SipMessage resp) {
		log(LogLevel.DEBUG,"inside processResponse(msg)");
		log(LogLevel.INFO,"request not for local server: message discarded");
	}
	

 
	// ****************************** Logs *****************************

	/** Adds a new string to the default Log. */
	private void log(LogLevel level, String str) {
		if (logger!=null) logger.log(level,"Redirect: "+str);  
	}


	// ****************************** MAIN *****************************

	/** The main method. */
	public static void main(String[] args) {
		
		Flags flags=new Flags(args);
		boolean help=flags.getBoolean("-h","prints this message");
		String file=flags.getString("-f","<file>",null,"loads configuration from the given file");
		
		if (help) {
			System.out.println(flags.toUsageString(Redirect.class.getName()));
			return;
		}
						
		SipStack.init(file);
		SipProvider sip_provider=new SipProvider(file);
		ServerProfile server_profile=new ServerProfile(file);

		new Redirect(sip_provider,server_profile);      
	}
  
}
