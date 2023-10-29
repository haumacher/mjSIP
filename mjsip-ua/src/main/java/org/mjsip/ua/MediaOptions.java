/*
 * Copyright (c) 2023 Bernhard Haumacher et al. All Rights Reserved.
 */
package org.mjsip.ua;

import org.mjsip.media.MediaDesc;
import org.zoolu.net.SocketAddress;

/**
 * Tool-options for interactive media streaming.
 */
public interface MediaOptions {

	/** Whether using audio */
	boolean isAudio();

	/**
	 * Whether using explicit external converter (i.e. direct access to an external conversion
	 * provider) instead of that provided by javax.sound.sampled.spi. It applies only when javax
	 * sound is used, that is when no other audio apps (such as jmf or rat) are used.
	 */
	boolean isJavaxSoundDirectConversion();

	/** Whether using symmetric_rtp */
	boolean isSymmetricRtp();

	/**
	 * Receiver random early drop (RED) rate. Actually it is the inverse of packet drop rate. It can
	 * used to prevent long play back delay. A value less or equal to 0 means that no packet
	 * dropping is explicitly performed at the RTP receiver.
	 */
	int getRandomEarlyDropRate();

	/** Whether using RAT (Robust Audio Tool) as audio sender/receiver */
	boolean isUseRat();

	/** Media descriptions that are used in calls. */
	MediaDesc[] getMediaDescs();

	/** Fixed audio multicast socket address; if defined, it forces the use of this maddr+port for audio session */
	SocketAddress getAudioMcastSoAddr();

	/** RAT command-line executable */
	String getBinRat();

	/** Whether using video */
	boolean isVideo();

	/** Whether using VIC (Video Conferencing Tool) as video sender/receiver */
	boolean isUseVic();

	/** VIC command-line executable */
	String getBinVic();

	/** Fixed video multicast socket address; if defined, it forces the use of this maddr+port for video session */
	SocketAddress getVideoMcastSoAddr();

}
