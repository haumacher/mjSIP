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
import org.slf4j.LoggerFactory;
import org.zoolu.util.ScheduledWork;


/** MultipleUAS is a simple UA that automatically responds to incoming calls.
  * <br>
  * At start up it may register with a registrar server (if properly configured).
  */
public abstract class MultipleUAS implements UserAgentListener, RegistrationClientListener, SipProviderListener {
	
	private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(MultipleUAS.class);

	/** UserAgentProfile */
	protected UAConfig uaConfig;
			
	/** SipProvider */
	protected SipProvider sip_provider;

	/** Creates a new MultipleUAS. */
	public MultipleUAS(SipProvider sip_provider, UAConfig uaConfig) {
		this.uaConfig=uaConfig;
		this.sip_provider=sip_provider;

		// init UA profile
		uaConfig.setUnconfiguredAttributes(sip_provider);

		// registration client
		if (uaConfig.doRegister || uaConfig.doUnregister || uaConfig.doUnregisterAll) {
			RegistrationClient rc=new RegistrationClient(sip_provider,new SipURI(uaConfig.registrar),uaConfig.getUserURI(),uaConfig.authUser,uaConfig.authRealm,uaConfig.authPasswd,this);
			rc.loopRegister(uaConfig.expires,uaConfig.expires/2);
		}
		
		// set strict receive-only profile, since this configuration is re-used for spawning multiple user agents when calls are received.
		uaConfig.uaServer=false;
		uaConfig.optionsServer=false;
		uaConfig.nullServer=false;
		uaConfig.doRegister=false;
		
		// start UAS     
		sip_provider.addSelectiveListener(new MethodId(SipMethods.INVITE),this); 
	} 


	// ******************* SipProviderListener methods  ******************

	/** From SipProviderListener. When a new Message is received by the SipProvider. */
	@Override
	public void onReceivedMessage(SipProvider sip_provider, SipMessage msg) {
		LOG.debug("onReceivedMessage()");
		if (msg.isRequest() && msg.isInvite()) {
			LOG.info("received new INVITE request");
			
			// create new user agent
			final UserAgent ua=new UserAgent(sip_provider,uaConfig,this);
			
			// since there is still no proper method to init the UA with an incoming call, trick it by using the onNewIncomingCall() callback method
			new ExtendedCall(sip_provider,msg,ua);
			
			// automatic hang-up after the maximum time
			if (uaConfig.hangupTime>0) {
				new ScheduledWork(uaConfig.hangupTime*1000) {  
					@Override
					public void doWork() {  
						ua.hangup();  
					}  
				};
			}
		}
	}


	// ******************** UserAgentListener methods ********************

	/** From UserAgentListener. When registration succeeded. */
	@Override
	public void onUaRegistrationSucceeded(UserAgent ua, String result) {
		
	}

	/** From UserAgentListener. When registration failed. */
	@Override
	public void onUaRegistrationFailed(UserAgent ua, String result) {
		
	}
	
	/** From UserAgentListener. When an ougoing call is stated to be in progress. */
	@Override
	public void onUaCallProgress(UserAgent ua) {
		
	}

	/** From UserAgentListener. When an ougoing call is remotly ringing. */
	@Override
	public void onUaCallRinging(UserAgent ua) {
		
	}

	/** From UserAgentListener. When an ougoing call has been accepted. */
	@Override
	public void onUaCallAccepted(UserAgent ua) {
		
	}
	
	/** From UserAgentListener. When a call has been transferred. */
	@Override
	public void onUaCallTransferred(UserAgent ua) {
		
	}

	/** From UserAgentListener. When an incoming call has been cancelled. */
	@Override
	public void onUaCallCancelled(UserAgent ua) {
		
	}

	/** From UserAgentListener. When an ougoing call has been refused or timeout. */
	@Override
	public void onUaCallFailed(UserAgent ua, String reason) {
		
	}

	/** From UserAgentListener. When a call has been locally or remotely closed. */
	@Override
	public void onUaCallClosed(UserAgent ua) {
		
	}

	/** From UserAgentListener. When a new media session is started. */
	@Override
	public void onUaMediaSessionStarted(UserAgent ua, String type, String codec) {
		
	}

	/** From UserAgentListener. When a media session is stopped. */
	@Override
	public void onUaMediaSessionStopped(UserAgent ua, String type) {
		
	}


	// *************** RegistrationClientListener methods ****************

	/** From RegistrationClientListener. When a UA has been successfully (un)registered. */
	@Override
	public void onRegistrationSuccess(RegistrationClient rc, NameAddress target, NameAddress contact, int expires, String result) {
		LOG.info("Registration success: expires="+expires+": "+result);
	}

	/** From RegistrationClientListener. When a UA failed on (un)registering. */
	@Override
	public void onRegistrationFailure(RegistrationClient rc, NameAddress target, NameAddress contact, String result) {
		LOG.info("Registration failure: "+result);
	}

}
