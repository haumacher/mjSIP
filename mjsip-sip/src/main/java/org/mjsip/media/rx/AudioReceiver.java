/*
 * Copyright (c) 2023 Bernhard Haumacher et al. All Rights Reserved.
 */
package org.mjsip.media.rx;

import java.io.IOException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.mjsip.media.RtpStreamReceiverListener;
import org.mjsip.rtp.RtpPayloadFormat;
import org.zoolu.net.UdpSocket;
import org.zoolu.sound.CodecType;
import org.zoolu.util.Encoder;

/**
 * TODO
 *
 * @author <a href="mailto:haui@haumacher.de">Bernhard Haumacher</a>
 */
public interface AudioReceiver {

	/**
	 * TODO
	 * 
	 * @param options
	 * @param udp_socket
	 *        TODO
	 * @param audio_format
	 * @param codec
	 * @param payload_type
	 *        TODO
	 * @param payloadFormat TODO
	 * @param sample_rate
	 * @param channels
	 *        TODO
	 * @param additional_decoder
	 */
	AudioRxHandle createReceiver(RtpReceiverOptions options, UdpSocket udp_socket, AudioFormat audio_format,
			CodecType codec,
			int payload_type, RtpPayloadFormat payloadFormat, int sample_rate, int channels, Encoder additional_decoder, RtpStreamReceiverListener listener)
			throws IOException, UnsupportedAudioFileException;

}
