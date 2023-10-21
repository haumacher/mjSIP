/*
 * Copyright (c) 2023 Bernhard Haumacher et al. All Rights Reserved.
 */
package org.mjsip.media.tx;

import java.net.UnknownHostException;

import org.mjsip.media.RtpStreamSender;
import org.mjsip.rtp.RtpControl;
import org.mjsip.rtp.RtpPayloadFormat;
import org.zoolu.net.SocketAddress;

/**
 * Handle for controlling an audio transmission.
 * 
 * @see AudioTransmitter#createSender(org.zoolu.net.UdpSocket, javax.sound.sampled.AudioFormat,
 *      org.zoolu.sound.CodecType, int, int, int, org.zoolu.util.Encoder, long, int, String, int,
 *      org.mjsip.media.RtpStreamSenderListener)
 */
public interface AudioTXHandle {

	/**
	 * Starts the transmission.
	 */
	void start();

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
	 * @see RtpStreamSender#setSyncAdj(long)
	 */
	void setSyncAdj(long sync_adj);

	/**
	 * @see RtpStreamSender#setControl(RtpControl)
	 */
	void setControl(RtpControl rtp_control);

	/**
	 * @see RtpStreamSender#setRtpPayloadFormat(RtpPayloadFormat)
	 */
	void setRtpPayloadFormat(RtpPayloadFormat amr_payload_format);

}
