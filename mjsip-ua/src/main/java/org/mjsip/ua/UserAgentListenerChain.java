/*
 * Copyright (c) 2023 Bernhard Haumacher et al. All Rights Reserved.
 */
package org.mjsip.ua;

import org.mjsip.media.MediaDesc;
import org.mjsip.sip.address.NameAddress;

/**
 * {@link UserAgentListener} that informs two listeners about {@link UserAgent} events.
 */
class UserAgentListenerChain implements UserAgentListener {
	
	private final UserAgentListener _fst;
	private final UserAgentListener _snd;

	/** 
	 * Creates a {@link UserAgentListenerChain}.
	 * 
	 * @param fst The first listener to inform.
	 * @param snd The second listener to inform.
	 */
	public UserAgentListenerChain(UserAgentListener fst, UserAgentListener snd) {
		_fst = fst;
		_snd = snd;
	}

	@Override
	public void onUaRegistrationSucceeded(UserAgent ua, String result) {
		try {
			_fst.onUaRegistrationSucceeded(ua, result);
		} finally {
			_snd.onUaRegistrationSucceeded(ua, result);
		}
	}

	@Override
	public void onUaRegistrationFailed(UserAgent ua, String result) {
		try {
			_fst.onUaRegistrationFailed(ua, result);
		} finally {
			_snd.onUaRegistrationFailed(ua, result);
		}
	}

	@Override
	public void onUaIncomingCall(UserAgent ua, NameAddress callee, NameAddress caller, MediaDesc[] media_descs) {
		try {
			_fst.onUaIncomingCall(ua, callee, caller, media_descs);
		} finally {
			_snd.onUaIncomingCall(ua, callee, caller, media_descs);
		}
	}

	@Override
	public void onUaCallIncomingAccepted(UserAgent userAgent) {
		try {
			_fst.onUaCallIncomingAccepted(userAgent);
		} finally {
			_snd.onUaCallIncomingAccepted(userAgent);
		}
	}

	@Override
	public void onUaIncomingCallTimeout(UserAgent userAgent) {
		try {
			_fst.onUaIncomingCallTimeout(userAgent);
		} finally {
			_snd.onUaIncomingCallTimeout(userAgent);
		}
	}

	@Override
	public void onUaCallCancelled(UserAgent ua) {
		try {
			_fst.onUaCallCancelled(ua);
		} finally {
			_snd.onUaCallCancelled(ua);
		}
	}

	@Override
	public void onUaCallConfirmed(UserAgent userAgent) {
		try {
			_fst.onUaCallConfirmed(userAgent);
		} finally {
			_snd.onUaCallConfirmed(userAgent);
		}
	}

	@Override
	public void onUaCallProgress(UserAgent ua) {
		try {
			_fst.onUaCallProgress(ua);
		} finally {
			_snd.onUaCallProgress(ua);
		}
	}

	@Override
	public void onUaCallRinging(UserAgent ua) {
		try {
			_fst.onUaCallRinging(ua);
		} finally {
			_snd.onUaCallRinging(ua);
		}
	}

	@Override
	public void onUaCallAccepted(UserAgent ua) {
		try {
			_fst.onUaCallAccepted(ua);
		} finally {
			_snd.onUaCallAccepted(ua);
		}
	}

	@Override
	public void onUaCallTransferred(UserAgent ua) {
		try {
			_fst.onUaCallTransferred(ua);
		} finally {
			_snd.onUaCallTransferred(ua);
		}
	}

	@Override
	public void onUaCallFailed(UserAgent ua, String reason) {
		try {
			_fst.onUaCallFailed(ua, reason);
		} finally {
			_snd.onUaCallFailed(ua, reason);
		}
	}

	@Override
	public void onUaCallClosed(UserAgent ua) {
		try {
			_fst.onUaCallClosed(ua);
		} finally {
			_snd.onUaCallClosed(ua);
		}
	}

	@Override
	public void onUaCallRedirected(UserAgent userAgent, NameAddress redirect_to) {
		try {
			_fst.onUaCallRedirected(userAgent, redirect_to);
		} finally {
			_snd.onUaCallRedirected(userAgent, redirect_to);
		}
	}

	@Override
	public void onUaMediaSessionStarted(UserAgent ua, String type, String codec) {
		try {
			_fst.onUaMediaSessionStarted(ua, type, codec);
		} finally {
			_snd.onUaMediaSessionStarted(ua, type, codec);
		}
	}

	@Override
	public void onUaMediaSessionStopped(UserAgent ua, String type) {
		try {
			_fst.onUaMediaSessionStopped(ua, type);
		} finally {
			_snd.onUaMediaSessionStopped(ua, type);
		}
		
	}

}
