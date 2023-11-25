package org.mjsip.ua;



import org.mjsip.media.MediaDesc;
import org.mjsip.sdp.SdpMessage;
import org.mjsip.sip.address.NameAddress;
import org.mjsip.sip.call.CallListener;
import org.mjsip.sip.call.DTMFInfo;



/** Listener of UserAgent */
public interface UserAgentListener {
	
	/** When registration succeeded. */
	public void onUaRegistrationSucceeded(UserAgent ua, String result);

	/** When registration failed. */
	public void onUaRegistrationFailed(UserAgent ua, String result);

	
	/**
	 * An incoming call is received.
	 * 
	 * <p>
	 * Should be answered with either {@link UserAgent#accept(MediaAgent)} or
	 * {@link UserAgent#hangup()}.
	 * </p>
	 */
	public void onUaIncomingCall(UserAgent ua, NameAddress callee, NameAddress caller, MediaDesc[] media_descs);
	
	/** When an incoming call has been accepted. */
	public void onUaCallIncomingAccepted(UserAgent userAgent);

	/** 
	 * When an incoming call was not accepted in time.
	 */
	public void onUaIncomingCallTimeout(UserAgent userAgent);

	/** When an incoming call is cancelled */
	public void onUaCallCancelled(UserAgent ua);
	
	/**
	 * When an incoming call has been established. 
	 * 
	 * @see CallListener#onCallConfirmed(org.mjsip.sip.call.Call, SdpMessage, org.mjsip.sip.message.SipMessage)
	 */
	public void onUaCallConfirmed(UserAgent userAgent);

	
	/** When an outgoing call is stated to be in progress */
	public void onUaCallProgress(UserAgent ua);

	/** When an outgoing call is remotely ringing */
	public void onUaCallRinging(UserAgent ua);
	
	/** When an outgoing call has been accepted */
	public void onUaCallAccepted(UserAgent ua);
	
	/** When a call has been transferred */
	public void onUaCallTransferred(UserAgent ua);

	/** When an outgoing call has been refused or times out */
	public void onUaCallFailed(UserAgent ua, String reason);

	/** When a call has been locally or remotely closed */
	public void onUaCallClosed(UserAgent ua);

	/**
	 * When a call is redirected to a new address. 
	 */
	public void onUaCallRedirected(UserAgent userAgent, NameAddress redirect_to);

	/** When a new media session is started */
	public void onUaMediaSessionStarted(UserAgent ua, String type, String codec);

	/** When a media session is stopped */
	public void onUaMediaSessionStopped(UserAgent ua, String type);

	/** 
	 * When a DTMF info message is received.
	 * 
	 * @param ua The {@link UserAgent} that received the message.
	 * @param dtmf Description of the pressed key.
	 */
	default void onDtmfInfo(UserAgent ua, DTMFInfo dtmf) {
		// Hook for subclasses.
	}
	
	/**
	 * Builds a chain of {@link UserAgentListener}s
	 *
	 * @param other
	 *        The listener to inform, after this listener has been called.
	 * @return A chain that first informs this listener and then the given one.
	 */
	default UserAgentListener andThen(UserAgentListener other) {
		if (other == null) {
			return this;
		}
		return new UserAgentListenerChain(this, other);
	}

}
