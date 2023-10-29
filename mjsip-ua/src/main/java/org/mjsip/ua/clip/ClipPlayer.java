/*
 * Copyright (c) 2023 Bernhard Haumacher et al. All Rights Reserved.
 */
package org.mjsip.ua.clip;

import org.mjsip.media.MediaDesc;
import org.mjsip.sip.address.NameAddress;
import org.mjsip.ua.UserAgent;
import org.mjsip.ua.UserAgentListener;
import org.mjsip.ua.UserAgentListenerAdapter;

/**
 * {@link UserAgentListener} playing sounds when certain {@link UserAgent} actions occur.
 */
public class ClipPlayer implements UserAgentListenerAdapter {
	
	/** On wav file */
	static final String CLIP_ON="on.wav";
	/** Off wav file */
	static final String CLIP_OFF="off.wav";
	/** Ring wav file */
	static final String CLIP_RING="ring.wav";
	/** Progress wav file */
	static final String CLIP_PROGRESS="progress.wav";

	/** On sound */
	private AudioClipPlayer clip_on;
	/** Off sound */
	private AudioClipPlayer clip_off;
	/** Ring sound */
	private AudioClipPlayer clip_ring;
	/** Progress sound */
	private AudioClipPlayer clip_progress;

	/** On volume gain */
	private float clip_on_volume_gain=(float)0.0; // not changed
	/** Off volume gain */
	private float clip_off_volume_gain=(float)0.0; // not changed
	/** Ring volume gain */
	private float clip_ring_volume_gain=(float)0.0; // not changed
	/** Progress volume gain */
	private float clip_progress_volume_gain=(float)0.0; // not changed
	
	/** 
	 * Creates a {@link ClipPlayer}.
	 */
	public ClipPlayer(String mediaPath) {
		clip_on=getAudioClip(mediaPath+"/"+CLIP_ON);
		clip_off=getAudioClip(mediaPath+"/"+CLIP_OFF);
		clip_ring=getAudioClip(mediaPath+"/"+CLIP_RING);
		clip_progress=getAudioClip(mediaPath+"/"+CLIP_PROGRESS);
		
		clip_ring.setLoop();
		clip_progress.setLoop();
		clip_on.setVolumeGain(clip_on_volume_gain);
		clip_off.setVolumeGain(clip_off_volume_gain);
		clip_ring.setVolumeGain(clip_ring_volume_gain);
		clip_progress.setVolumeGain(clip_progress_volume_gain);
	}
	
	@Override
	public void onUaIncomingCall(UserAgent ua, NameAddress callee, NameAddress caller, MediaDesc[] media_descs) {
		if (clip_ring!=null) clip_ring.play();
	}
	
	@Override
	public void onUaCallIncomingAccepted(UserAgent userAgent) {
		if (clip_ring!=null) clip_ring.stop();
	}
	
	@Override
	public void onUaIncomingCallTimeout(UserAgent userAgent) {
		if (clip_ring!=null) clip_ring.stop();
	}

	@Override
	public void onUaCallCancelled(UserAgent ua) {
		if (clip_ring!=null) clip_ring.stop();
		
		if (clip_off!=null) clip_off.play();
	}

	@Override
	public void onUaCallRedirected(UserAgent userAgent, NameAddress redirect_to) {
		if (clip_ring!=null) clip_ring.stop();
	}
	
	@Override
	public void onUaCallProgress(UserAgent ua) {
		if (clip_progress!=null) clip_progress.play();
	}

	@Override
	public void onUaCallRinging(UserAgent ua) {
		if (clip_progress!=null) clip_progress.play();
	}

	@Override
	public void onUaCallAccepted(UserAgent ua) {
		if (clip_progress!=null) clip_progress.stop();
		
		if (clip_on!=null) clip_on.play();
	}

	@Override
	public void onUaCallConfirmed(UserAgent userAgent) {
		if (clip_progress!=null) clip_progress.stop();
		
		if (clip_on!=null) clip_on.play();
	}
	
	@Override
	public void onUaCallFailed(UserAgent ua, String reason) {
		if (clip_progress!=null) clip_progress.stop();
		
		if (clip_off!=null) clip_off.play();
	}
	
	@Override
	public void onUaCallClosed(UserAgent ua) {
		if (clip_ring!=null) clip_ring.stop();
		if (clip_progress!=null) clip_progress.stop();
		
		if (clip_off!=null) clip_off.play();
	}
	
	private static AudioClipPlayer getAudioClip(String image_file) {
		return new AudioClipPlayer(UserAgent.class.getResource("/" + image_file), null);
	}
}
