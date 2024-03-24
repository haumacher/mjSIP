/*
 * Copyright (c) 2023 Bernhard Haumacher et al. All Rights Reserved.
 */
package org.mjsip.media.tx;

import java.net.UnknownHostException;
import java.util.concurrent.Executor;

import org.mjsip.media.RtpStreamSender;
import org.mjsip.rtp.RtpControl;
import org.mjsip.rtp.RtpPayloadFormat;
import org.zoolu.net.SocketAddress;

/**
 * Handle for controlling an audio transmission.
 * 
 * @see AudioTransmitter#createSender(RtpSenderOptions, org.zoolu.net.UdpSocket,
 *      javax.sound.sampled.AudioFormat, org.zoolu.sound.CodecType, int, RtpPayloadFormat, int,
 *      int, org.zoolu.util.Encoder, long, int, String, int, org.mjsip.media.RtpStreamSenderListener, RtpControl)
 */
public interface AudioTXHandle {

	/**
	 * Starts the transmission.
	 */
	void start(Executor executor);

	/**
	 * Stops the transmission.
	 */
	void halt();

	/**
	 * Switches the remote socket address.
	 * 
	 * @see RtpStreamSender#setRemoteSoAddress(SocketAddress)
	 */
	void setRemoteSoAddress(SocketAddress remote_soaddr) throws UnknownHostException;

	/**
	 * Waits until the current transmission has completed.
	 */
	void join() throws InterruptedException;

}
