/*
 * Copyright (c) 2023 Bernhard Haumacher et al. All Rights Reserved.
 */
package org.mjsip.media.tx;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.Executor;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;

import org.mjsip.media.RtpStreamSender;
import org.mjsip.media.RtpStreamSenderListener;
import org.mjsip.rtp.RtpControl;
import org.mjsip.rtp.RtpPayloadFormat;
import org.slf4j.LoggerFactory;
import org.zoolu.net.UdpSocket;
import org.zoolu.sound.CodecType;
import org.zoolu.sound.ConverterAudioSystem;
import org.zoolu.sound.SimpleAudioSystem;
import org.zoolu.util.Encoder;

/**
 * {@link AudioTransmitter} sending audio from system mirophone input.
 */
public class JavaxAudioInput implements AudioTransmitter {

	private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(JavaxAudioInput.class);

	private final boolean _sync;

	private final boolean _noConvertion;

	/**
	 * Creates a {@link JavaxAudioInput}.
	 */
	public JavaxAudioInput(boolean sync, boolean noConvertion) {
		super();
		_sync = sync;
		_noConvertion = noConvertion;
	}

	@Override
	public AudioTXHandle createSender(RtpSenderOptions options, UdpSocket udp_socket, AudioFormat audio_format,
			CodecType codec, int payload_type,
			RtpPayloadFormat payloadFormat, int sample_rate, int channels, Encoder additional_encoder, long packet_time,
			int packet_size, String remote_addr, int remote_port, RtpStreamSenderListener listener, RtpControl rtpControl) throws IOException {

		SimpleAudioSystem.initAudioInputLine(sample_rate, channels);

		InputStream audioIn;
		if (!_noConvertion || codec.equals(CodecType.G711_ULAW) || codec.equals(CodecType.G711_ALAW)) {
			// use standard java embedded conversion provider
			audioIn = SimpleAudioSystem.getInputStream(audio_format);
		} else {
			// use conversion provider
			final AudioFormat baseFormat = SimpleAudioSystem.getBaseAudioFormat(sample_rate, channels);
			AudioInputStream rawInput = SimpleAudioSystem.getInputStream(baseFormat);
			AudioInputStream converter = ConverterAudioSystem.convertAudioInputStream(codec, sample_rate, rawInput);
			LOG.info("send x-format: {}", converter.getFormat());
			audioIn = converter;
		}

		RtpStreamSender sender = new RtpStreamSender(options, audioIn, _sync, payload_type, payloadFormat,
				sample_rate, channels, packet_time, packet_size, additional_encoder, udp_socket, remote_addr,
				remote_port, rtpControl, listener);
		return new RtpAudioTxHandle(sender) {
			@Override
			public void start(Executor executor) {
				super.start(executor);
				SimpleAudioSystem.startAudioInputLine();
			}

			@Override
			public void halt() {
				SimpleAudioSystem.stopAudioInputLine();
				super.halt();
			}
		};
	}

}
