/*
 * Copyright (c) 2023 Bernhard Haumacher et al. All Rights Reserved.
 */
package org.mjsip.media.tx;

import java.io.IOException;

import javax.sound.sampled.AudioFormat;

import org.mjsip.media.RtpStreamSenderListener;
import org.mjsip.rtp.RtpControl;
import org.mjsip.rtp.RtpPayloadFormat;
import org.zoolu.net.UdpSocket;
import org.zoolu.sound.CodecType;
import org.zoolu.util.Encoder;

/**
 * An audio transmitter encapsulating the source of the audio data.
 */
public interface AudioTransmitter {

	/**
	 * Creates a transmission handle.
	 * @param payloadFormat TODO
	 * @param rtpControl TODO
	 * @see AudioTXHandle#start()
	 * @see AudioTXHandle#halt()
	 */
	AudioTXHandle createSender(RtpSenderOptions options, UdpSocket udp_socket, AudioFormat audio_format,
			CodecType codec, int payload_type, RtpPayloadFormat payloadFormat,
			int sample_rate, int channels, Encoder additional_encoder, long packet_time,
			int packet_size, String remote_addr, int remote_port, RtpStreamSenderListener listener, RtpControl rtpControl) throws IOException;
}
