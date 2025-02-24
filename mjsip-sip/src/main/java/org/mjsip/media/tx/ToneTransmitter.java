/*
 * Copyright (c) 2023 Bernhard Haumacher et al. All Rights Reserved.
 */
package org.mjsip.media.tx;

import java.io.IOException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;

import org.mjsip.media.RtpStreamSender;
import org.mjsip.media.RtpStreamSenderListener;
import org.mjsip.rtp.RtpControl;
import org.mjsip.rtp.RtpPayloadFormat;
import org.mjsip.sound.ToneGenerator;
import org.mjsip.sound.ToneInputStream;
import org.slf4j.LoggerFactory;
import org.zoolu.net.UdpSocket;
import org.zoolu.sound.CodecType;
import org.zoolu.sound.ConverterAudioSystem;
import org.zoolu.util.Encoder;

/**
 * {@link AudioTransmitter} sending a sinus wave.
 */
public class ToneTransmitter implements AudioTransmitter {

	private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(ToneTransmitter.class);

	/** Test tone frequency [Hz] */
	public static int TONE_FREQ = 330;

	/** Test tone amplitude (from 0.0 to 1.0) */
	public static double TONE_AMPL = 0.8;

	/** By default whether using big-endian rule for byte ordering */
	public static final boolean DEFAULT_BIG_ENDIAN = false;

	private int _toneFreq;

	private double _toneAmpl;

	/**
	 * Creates a {@link ToneTransmitter} with a default tone.
	 */
	public ToneTransmitter() {
		this(TONE_FREQ, TONE_AMPL);
	}

	/**
	 * Creates a {@link ToneTransmitter}.
	 */
	public ToneTransmitter(int tone_freq, double tone_ampl) {
		_toneFreq = tone_freq;
		_toneAmpl = tone_ampl;
	}

	@Override
	public AudioTXHandle createSender(RtpSenderOptions options, UdpSocket udp_socket, AudioFormat audio_format,
			CodecType codec, int payload_type,
			RtpPayloadFormat payloadFormat, int sample_rate, int channels, Encoder additional_encoder, long packet_time,
			int packet_size, String remote_addr, int remote_port, RtpStreamSenderListener listener, RtpControl rtpControl) throws IOException {
		LOG.info("Tone generator: {} Hz", _toneFreq);

		int sampleSizeInBits = audio_format.getSampleSizeInBits();
		ToneInputStream sinWave = new ToneInputStream(_toneFreq, _toneAmpl, sample_rate,
				(sampleSizeInBits + 7) / 8,
				ToneGenerator.Encoding.PCM_LINEAR_SIGNED, DEFAULT_BIG_ENDIAN);

		AudioInputStream tone = new AudioInputStream(sinWave,
				new AudioFormat(sample_rate, sampleSizeInBits, 1, true, DEFAULT_BIG_ENDIAN), 65000);

		AudioInputStream converter = ConverterAudioSystem.convertAudioInputStream(codec, sample_rate, tone);

		RtpStreamSender sender = new RtpStreamSender(options, converter, true, payload_type, payloadFormat,
				sample_rate, channels, packet_time, packet_size, additional_encoder, udp_socket, remote_addr,
				remote_port, rtpControl, listener);
		return new RtpAudioTxHandle(sender);
	}

}
