/*
 * Copyright (C) 2007 Luca Veltri - University of Parma - Italy
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


import java.io.PrintStream;

import org.mjsip.sip.address.NameAddress;
import org.mjsip.sip.address.SipURI;
import org.mjsip.sip.call.ExtendedCall;
import org.mjsip.sip.call.RegistrationClient;
import org.mjsip.sip.call.RegistrationClientListener;
import org.mjsip.sip.message.SipMessage;
import org.mjsip.sip.message.SipMethods;
import org.mjsip.sip.provider.MethodId;
import org.mjsip.sip.provider.SipProvider;
import org.mjsip.sip.provider.SipProviderListener;
import org.zoolu.util.LogLevel;
import org.zoolu.util.Logger;
import org.zoolu.util.ScheduledWork;


/** MultipleUAS is a simple UA that automatically responds to incoming calls.
  * <br>
  * At start up it may register with a registrar server (if proper configured).
  */
public abstract class MultipleUAS implements UserAgentListener, RegistrationClientListener, SipProviderListener {
	

	/** UserAgentProfile */
	protected UserAgentProfile ua_profile;
			
	/** SipProvider */
	protected SipProvider sip_provider;

	/** Logger */
	Logger logger;
	
	/** Standard output */
	PrintStream stdout=null;



	/** Creates a new MultipleUAS. */
	public MultipleUAS(SipProvider sip_provider, UserAgentProfile ua_profile) {
		logger=sip_provider.getLogger();
		this.ua_profile=ua_profile;
		this.sip_provider=sip_provider;

		// init UA profile
		ua_profile.setUnconfiguredAttributes(sip_provider);
		if (!ua_profile.no_prompt) stdout=System.out;
		// set strict UA profile only 
		ua_profile.ua_server=false;
		ua_profile.options_server=false;
		ua_profile.null_server=false;
		// registration client
		if (ua_profile.do_register || ua_profile.do_unregister || ua_profile.do_unregister_all) {
			RegistrationClient rc=new RegistrationClient(sip_provider,new SipURI(ua_profile.registrar),ua_profile.getUserURI(),ua_profile.auth_user,ua_profile.auth_realm,ua_profile.auth_passwd,this);
			rc.loopRegister(ua_profile.expires,ua_profile.expires/2);
		}
		ua_profile.do_register=false;
		// start UAS     
		sip_provider.addSelectiveListener(new MethodId(SipMethods.INVITE),this); 
	} 


	// ******************* SipProviderListener methods  ******************

	/** From SipProviderListener. When a new Message is received by the SipProvider. */
	public void onReceivedMessage(SipProvider sip_provider, SipMessage msg) {
		log(LogLevel.DEBUG,"onReceivedMessage()");
		if (msg.isRequest() && msg.isInvite()) {
			log(LogLevel.INFO,"received new INVITE request");
			// get caller, callee, sdp
			//NameAddress callee=msg.getToHeader().getNameAddress();
			//NameAddress caller=msg.getFromHeader().getNameAddress();
			String sdp=msg.getStringBody();
			// create new UA
			final UserAgent ua=new UserAgent(sip_provider,ua_profile,this);
			// since there is still no proper method to init the UA with an incoming call, trick it by using the onNewIncomingCall() callback method
			//printOut("Incoming call from "+caller.toString());
			new ExtendedCall(sip_provider,msg,ua);
			// automatic hangup after a maximum time
			if (ua_profile.hangup_time>0) new ScheduledWork(ua_profile.hangup_time*1000) {  public void doWork() {  ua.hangup();  }  };
		}
	}


	// ******************** UserAgentListener methods ********************

	/** From UserAgentListener. When registration succeeded. */
	public void onUaRegistrationSucceeded(UserAgent ua, String result) {
		
	}

	/** From UserAgentListener. When registration failed. */
	public void onUaRegistrationFailed(UserAgent ua, String result) {
		
	}
	
	/** From UserAgentListener. When an ougoing call is stated to be in progress. */
	public void onUaCallProgress(UserAgent ua) {
		
	}

	/** From UserAgentListener. When an ougoing call is remotly ringing. */
	public void onUaCallRinging(UserAgent ua) {
		
	}

	/** From UserAgentListener. When an ougoing call has been accepted. */
	public void onUaCallAccepted(UserAgent ua) {
		
	}
	
	/** From UserAgentListener. When a call has been transferred. */
	public void onUaCallTransferred(UserAgent ua) {
		
	}

	/** From UserAgentListener. When an incoming call has been cancelled. */
	public void onUaCallCancelled(UserAgent ua) {
		
	}

	/** From UserAgentListener. When an ougoing call has been refused or timeout. */
	public void onUaCallFailed(UserAgent ua, String reason) {
		
	}

	/** From UserAgentListener. When a call has been locally or remotely closed. */
	public void onUaCallClosed(UserAgent ua) {
		
	}

	/** From UserAgentListener. When a new media session is started. */
	public void onUaMediaSessionStarted(UserAgent ua, String type, String codec) {
		
	}

	/** From UserAgentListener. When a media session is stopped. */
	public void onUaMediaSessionStopped(UserAgent ua, String type) {
		
	}


	// *************** RegistrationClientListener methods ****************

	/** From RegistrationClientListener. When a UA has been successfully (un)registered. */
	public void onRegistrationSuccess(RegistrationClient rc, NameAddress target, NameAddress contact, int expires, String result) {
		log(LogLevel.INFO,"Registration success: expires="+expires+": "+result);
	}

	/** From RegistrationClientListener. When a UA failed on (un)registering. */
	public void onRegistrationFailure(RegistrationClient rc, NameAddress target, NameAddress contact, String result) {
		log(LogLevel.INFO,"Registration failure: "+result);
	}

	
	// ****************************** Logs *******************************

	/** Prints out a message to stantard output. */
	void printOut(String str) {
		if (stdout!=null) stdout.println(str);
	}


	/** Adds a new string to the default Log. */
	void log(String str) {
		log(LogLevel.INFO,str);
	}

	/** Adds a new string to the default Log. */
	void log(LogLevel level, String str) {
		if (logger!=null) logger.log(level,"MultipleUAS: "+str);  
	}

}
