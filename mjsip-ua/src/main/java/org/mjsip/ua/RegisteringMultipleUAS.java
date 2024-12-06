/*
 * Copyright (c) 2023 Bernhard Haumacher et al. All Rights Reserved.
 */
package org.mjsip.ua;

import java.util.concurrent.ScheduledFuture;

import org.mjsip.pool.PortPool;
import org.mjsip.sip.address.NameAddress;
import org.mjsip.sip.call.ExtendedCall;
import org.mjsip.sip.message.SipMessage;
import org.mjsip.sip.provider.SipProvider;
import org.mjsip.ua.registration.RegistrationClient;
import org.mjsip.ua.registration.RegistrationClientListener;
import org.slf4j.LoggerFactory;

/**
 * {@link MultipleUAS} with built-in support for registering at a registrar.
 */
public abstract class RegisteringMultipleUAS extends MultipleUAS implements RegistrationClientListener {

	private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(RegisteringMultipleUAS.class);

	private RegistrationClient _rc;
	private final UAOptions _uaConfig;
	private final int _hangupTime;

	/** 
	 * Creates a {@link RegisteringMultipleUAS}.
	 */
	public RegisteringMultipleUAS(SipProvider sip_provider, PortPool portPool, UAOptions uaConfig,
			ServiceOptions serviceConfig) {
		super(sip_provider, portPool, uaConfig);
		_uaConfig = uaConfig;
		_hangupTime = serviceConfig.getHangupTime();
		
		register();
	}

	/**
	 * Registers at the registrar.
	 */
	public void register() {
		if (_uaConfig.isRegister()) {
			_rc = new RegistrationClient(sip_provider, _uaConfig, this);
			_rc.loopRegister(_uaConfig);
		}
	}

	/**
	 * Cancels registration.
	 */
	public void unregister() {
		if (_rc != null) {
			_rc.unregister();
			_rc.halt();
			_rc = null;
		}
	}
	
	/**
	 * Handles an SIP invite.
	 * 
	 * <p>
	 * By default the call is accepted and a {@link UserAgent} created for handling the new call.
	 * </p>
	 * @param msg
	 *        The invite message.
	 *
	 * @see #createCallHandler(SipMessage)
	 */
	@Override
	protected void onInviteReceived(SipMessage msg) {
		LOG.info("Received INVITE from: {}", msg.getFromHeader().getNameAddress());
		
		AutoHangup autoHangup;
		UserAgentListener listener = createCallHandler(msg);
		if (_hangupTime > 0) {
			autoHangup = new AutoHangup();
			listener = listener.andThen(autoHangup);
		} else {
			autoHangup = null;
		}
		
		final UserAgent ua = new UserAgent(sip_provider, _portPool, _uaConfig, listener);
		
		// since there is still no proper method to init the UA with an incoming call, trick it by using the onNewIncomingCall() callback method
		new ExtendedCall(sip_provider,msg,ua);
		
		if (autoHangup != null) {
			autoHangup.start(ua);
		}
	}

	// *************** RegistrationClientListener methods ****************

	/** From RegistrationClientListener. When a UA has been successfully (un)registered. */
	@Override
	public void onRegistrationSuccess(RegistrationClient rc, NameAddress target, NameAddress contact, int expires, int renewTime, String result) {
		LOG.info("Registration success: expires= {}:{}", expires, result);
	}

	/** From RegistrationClientListener. When a UA failed on (un)registering. */
	@Override
	public void onRegistrationFailure(RegistrationClient rc, NameAddress target, NameAddress contact, String result) {
		LOG.info("Registration failure: {}", result);
	}

	/**
	 * Creates a handler for controlling the call started with the given invite message.
	 * 
	 * @param msg
	 *        The message that represents the invite to the new call.
	 *
	 * @return The handler for controlling the user agent that handles the new call.
	 */
	protected abstract UserAgentListener createCallHandler(SipMessage msg);
	
	protected final class AutoHangup implements UserAgentListenerAdapter {
		private ScheduledFuture<?> _hangupTimer;
	
		@Override
		public void onUaCallClosed(UserAgent ua) {
			if (_hangupTimer != null) _hangupTimer.cancel(false);
		}
	
		public void start(UserAgent ua) {
			_hangupTimer = sip_provider.scheduler().schedule(_hangupTime*1000, () -> ua.hangup());			
		}
	}
	
}
