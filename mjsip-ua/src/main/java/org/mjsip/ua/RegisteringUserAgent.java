/*
 * Copyright (c) 2023 Bernhard Haumacher et al. All Rights Reserved.
 */
package org.mjsip.ua;

import org.mjsip.pool.PortPool;
import org.mjsip.sip.address.SipURI;
import org.mjsip.sip.call.NotImplementedServer;
import org.mjsip.sip.call.OptionsServer;
import org.mjsip.sip.message.SipMethods;
import org.mjsip.sip.provider.SipId;
import org.mjsip.sip.provider.SipKeepAlive;
import org.mjsip.sip.provider.SipProvider;
import org.mjsip.ua.registration.RegistrationClient;
import org.mjsip.ua.registration.RegistrationOptions;
import org.zoolu.net.SocketAddress;

/**
 * {@link UserAgent} with regitration suppport.
 */
public class RegisteringUserAgent extends UserAgent {

	/** RegistrationClient */
	private RegistrationClient rc=null;

	/** SipKeepAlive daemon */
	private SipKeepAlive keep_alive;

	private RegistrationOptions _registrationConfig;

	/** OptionsServer */
	private OptionsServer options_server;

	/** NotImplementedServer */
	private NotImplementedServer null_server;

	/** 
	 * Creates a {@link RegisteringUserAgent}.
	 */
	public RegisteringUserAgent(SipProvider sip_provider, PortPool portPool, UAOptions uaConfig, UserAgentListener listener) {
		super(sip_provider, portPool, uaConfig, listener);
		_registrationConfig = uaConfig;
		
		// start listening for INVITE requests (UAS)
		if (uaConfig.isUaServer()) sip_provider.addSelectiveListener(SipId.createMethodId(SipMethods.INVITE),this);
		
		// start OPTIONS server
		if (uaConfig.isOptionsServer()) options_server=new OptionsServer(sip_provider,"INVITE, ACK, CANCEL, OPTIONS, BYE","application/sdp");

		// start "Not Implemented" server
		if (uaConfig.isNullServer()) null_server=new NotImplementedServer(sip_provider);
	}

	/** Register with the registrar server
	  * @param expire_time expiration time in seconds */
	public void register(int expire_time) {
		rc.register(expire_time);
	}

	/** Periodically registers the contact address with the registrar server.
	  * @param expire_time expiration time in seconds
	  * @param renew_time renew time in seconds
	  * @param keepalive_time keep-alive packet rate (inter-arrival time) in milliseconds */
	public void loopRegister(int expire_time, int renew_time, long keepalive_time) {
		// create registration client
		if (rc==null) {
			initRegistrationClient();
		}
		
		// start registering
		rc.loopRegister(expire_time,renew_time);

		// keep-alive
		if (keepalive_time>0) {
			SipURI target_uri=(sip_provider.hasOutboundProxy())? sip_provider.getOutboundProxy() : rc.getTargetAOR().getAddress().toSipURI();
			String target_host=target_uri.getHost();
			int target_port=target_uri.getPort();
			if (target_port<0) target_port=sip_provider.sipConfig().getDefaultPort();
			SocketAddress target_soaddr=new SocketAddress(target_host,target_port);
			if (keep_alive!=null && keep_alive.isRunning()) keep_alive.halt();
			keep_alive=new SipKeepAlive(sip_provider,target_soaddr,null,keepalive_time);
		}
	}

	/** Unregisters with the registrar server */
	public void unregister() {
		// stop registering
		if (keep_alive!=null && keep_alive.isRunning()) keep_alive.halt();

		// unregister
		if (rc!=null) {
			rc.unregister();
			rc.halt();
			rc = null;
		}
	}

	/** Unregister all contacts with the registrar server */
	public void unregisterall() {
		// create registration client
		if (rc==null) initRegistrationClient();
		// stop registering
		if (keep_alive!=null && keep_alive.isRunning()) keep_alive.halt();
		// unregister
		rc.unregisterall();
	}
	
	/** Inits the RegistrationClient */
	private void initRegistrationClient() {
		rc = new RegistrationClient(sip_provider, _registrationConfig, this);
	}

	
}
