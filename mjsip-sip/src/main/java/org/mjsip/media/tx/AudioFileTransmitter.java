/*
 * Copyright (c) 2023 Bernhard Haumacher et al. All Rights Reserved.
 */
package org.mjsip.media.tx;

import java.io.IOException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.mjsip.media.RtpStreamSender;
import org.mjsip.media.RtpStreamSenderListener;
import org.mjsip.rtp.RtpControl;
import org.mjsip.rtp.RtpPayloadFormat;
import org.mjsip.sound.AudioFile;
import org.slf4j.LoggerFactory;
import org.zoolu.net.UdpSocket;
import org.zoolu.sound.CodecType;
import org.zoolu.util.Encoder;

/**
 * {@link AudioTransmitter} sending an audio stream from a file.
 */
public class AudioFileTransmitter implements AudioTransmitter {

	private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(AudioFileTransmitter.class);

	private final String _audioFile;

	/**
	 * Creates a {@link AudioFileTransmitter}.
	 */
	public AudioFileTransmitter(String audiofile_in) {
		this._audioFile = audiofile_in;
	}

	@Override
	public AudioTXHandle createSender(RtpSenderOptions options, UdpSocket udp_socket, AudioFormat audio_format,
			CodecType codec, int payload_type,
			RtpPayloadFormat payloadFormat, int sample_rate, int channels, Encoder additional_encoder, long packet_time,
			int packet_size, String remote_addr, int remote_port, RtpStreamSenderListener listener, RtpControl rtpControl) throws IOException {
		try {
			LOG.info("Streaming audio from file {} format: {}", _audioFile, audio_format);
			AudioInputStream audioIn = AudioFile.getAudioFileInputStream(_audioFile, audio_format);
			RtpStreamSender sender = new RtpStreamSender(options, audioIn, true, payload_type, payloadFormat, sample_rate,
					channels, packet_time, packet_size, additional_encoder, udp_socket, remote_addr, remote_port, rtpControl, listener);
			return new RtpAudioTxHandle(sender);
		} catch (UnsupportedAudioFileException ex) {
			throw new IOException("Cannot read audio file: " + _audioFile, ex);
		}
	}

}
