/*
 * Copyright (c) 2023 Bernhard Haumacher et al. All Rights Reserved.
 */
package org.mjsip.ua;

import org.mjsip.media.MediaDesc;
import org.mjsip.sip.address.NameAddress;

/**
 * Adapter implementation for {@link UserAgentListener} with all method implemented empty.
 */
public abstract class UserAgentListenerAdapter implements UserAgentListener {

	@Override
	public void onUaRegistrationSucceeded(UserAgent ua, String result) {
		// Hook for subclasses.
	}

	@Override
	public void onUaRegistrationFailed(UserAgent ua, String result) {
		// Hook for subclasses.
	}

	@Override
	public void onUaIncomingCall(UserAgent ua, NameAddress callee, NameAddress caller, MediaDesc[] media_descs) {
		// Hook for subclasses.
	}
	
	@Override
	public void onUaCallCancelled(UserAgent ua) {
		// Hook for subclasses.
	}

	@Override
	public void onUaCallProgress(UserAgent ua) {
		// Hook for subclasses.
	}

	@Override
	public void onUaCallRinging(UserAgent ua) {
		// Hook for subclasses.
	}

	@Override
	public void onUaCallAccepted(UserAgent ua) {
		// Hook for subclasses.
	}

	@Override
	public void onUaCallTransferred(UserAgent ua) {
		// Hook for subclasses.
	}

	@Override
	public void onUaCallFailed(UserAgent ua, String reason) {
		// Hook for subclasses.
	}

	@Override
	public void onUaCallClosed(UserAgent ua) {
		// Hook for subclasses.
	}

	@Override
	public void onUaMediaSessionStarted(UserAgent ua, String type, String codec) {
		// Hook for subclasses.
	}

	@Override
	public void onUaMediaSessionStopped(UserAgent ua, String type) {
		// Hook for subclasses.
	}
}
