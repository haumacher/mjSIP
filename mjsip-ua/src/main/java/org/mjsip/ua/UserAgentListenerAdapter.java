/*
 * Copyright (c) 2023 Bernhard Haumacher et al. All Rights Reserved.
 */
package org.mjsip.ua;

import org.mjsip.media.MediaDesc;
import org.mjsip.sip.address.NameAddress;

/**
 * Adapter implementation for {@link UserAgentListener} with all method implemented empty.
 */
public interface UserAgentListenerAdapter extends UserAgentListener {

	@Override
	default void onUaRegistrationSucceeded(UserAgent ua, String result) {
		// Hook for subclasses.
	}

	@Override
	default void onUaRegistrationFailed(UserAgent ua, String result) {
		// Hook for subclasses.
	}

	@Override
	default void onUaIncomingCall(UserAgent ua, NameAddress callee, NameAddress caller, MediaDesc[] media_descs) {
		// Hook for subclasses.
	}
	
	@Override
	default void onUaCallIncomingAccepted(UserAgent userAgent) {
		// Hook for subclasses.
	}
	
	@Override
	default void onUaIncomingCallTimeout(UserAgent userAgent) {
		// Hook for subclasses.
	}
	
	@Override
	default void onUaCallCancelled(UserAgent ua) {
		// Hook for subclasses.
	}
	
	@Override
	default void onUaCallConfirmed(UserAgent userAgent) {
		// Hook for subclasses.
	}

	@Override
	default void onUaCallProgress(UserAgent ua) {
		// Hook for subclasses.
	}

	@Override
	default void onUaCallRinging(UserAgent ua) {
		// Hook for subclasses.
	}

	@Override
	default void onUaCallAccepted(UserAgent ua) {
		// Hook for subclasses.
	}

	@Override
	default void onUaCallTransferred(UserAgent ua) {
		// Hook for subclasses.
	}

	@Override
	default void onUaCallFailed(UserAgent ua, String reason) {
		// Hook for subclasses.
	}

	@Override
	default void onUaCallClosed(UserAgent ua) {
		// Hook for subclasses.
	}

	@Override
	default void onUaCallRedirected(UserAgent userAgent, NameAddress redirect_to) {
		// Hook for subclasses.
	}
	
	@Override
	default void onUaMediaSessionStarted(UserAgent ua, String type, String codec) {
		// Hook for subclasses.
	}

	@Override
	default void onUaMediaSessionStopped(UserAgent ua, String type) {
		// Hook for subclasses.
	}
}
