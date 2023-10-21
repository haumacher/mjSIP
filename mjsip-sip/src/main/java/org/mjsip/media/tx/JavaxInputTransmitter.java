/*
 * Copyright (c) 2023 Bernhard Haumacher et al. All Rights Reserved.
 */
package org.mjsip.media.tx;

import java.io.IOException;
import java.io.InputStream;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;

import org.mjsip.media.RtpStreamSender;
import org.mjsip.media.RtpStreamSenderListener;
import org.slf4j.LoggerFactory;
import org.zoolu.net.UdpSocket;
import org.zoolu.sound.CodecType;
import org.zoolu.sound.ConverterAudioSystem;
import org.zoolu.sound.SimpleAudioSystem;
import org.zoolu.util.Encoder;

/**
 * {@link AudioTransmitter} sending audio from system mirophone input.
 */
public class JavaxInputTransmitter implements AudioTransmitter {

	private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(JavaxInputTransmitter.class);

	private final boolean javaxSoundSync;

	private final boolean direct_convertion;

	/**
	 * Creates a {@link JavaxInputTransmitter}.
	 */
	public JavaxInputTransmitter(boolean javaxSoundSync, boolean direct_convertion) {
		super();
		this.javaxSoundSync = javaxSoundSync;
		this.direct_convertion = direct_convertion;
	}

	@Override
	public AudioTXHandle createSender(UdpSocket udp_socket, AudioFormat audio_format, CodecType codec, int payload_type,
			int sample_rate, int channels, Encoder additional_encoder, long packet_time, int packet_size,
			String remote_addr, int remote_port, RtpStreamSenderListener listener) throws IOException {

		InputStream audioIn;
		if (!direct_convertion || codec.equals(CodecType.G711_ULAW) || codec.equals(CodecType.G711_ALAW)) {
			// use standard java embedded conversion provider
			audioIn = SimpleAudioSystem.getInputStream(audio_format);
		} else {
			// use conversion provider
			final AudioFormat baseFormat = SimpleAudioSystem.getBaseAudioFormat(sample_rate, channels);
			AudioInputStream rawInput = SimpleAudioSystem.getInputStream(baseFormat);
			AudioInputStream converter = ConverterAudioSystem.convertAudioInputStream(codec, sample_rate, rawInput);
			LOG.info("send x-format: " + converter.getFormat());
			audioIn = converter;
		}

		return new RtpAudioTxHandle(new RtpStreamSender(audioIn, javaxSoundSync, payload_type, sample_rate, channels, packet_time,
				packet_size, additional_encoder, udp_socket, remote_addr, remote_port, listener)) {
			@Override
			public void start() {
				super.start();
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
