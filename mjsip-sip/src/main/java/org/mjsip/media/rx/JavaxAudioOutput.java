/*
 * Copyright (c) 2023 Bernhard Haumacher et al. All Rights Reserved.
 */
package org.mjsip.media.rx;

import java.io.IOException;
import java.util.concurrent.Executor;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.mjsip.media.RtpStreamReceiver;
import org.mjsip.media.RtpStreamReceiverListener;
import org.mjsip.rtp.RtpPayloadFormat;
import org.slf4j.LoggerFactory;
import org.zoolu.net.UdpSocket;
import org.zoolu.sound.AudioOutputStream;
import org.zoolu.sound.CodecType;
import org.zoolu.sound.ConverterAudioSystem;
import org.zoolu.sound.SimpleAudioSystem;
import org.zoolu.util.Encoder;

/**
 * TODO
 *
 * @author <a href="mailto:haui@haumacher.de">Bernhard Haumacher</a>
 */
public class JavaxAudioOutput implements AudioReceiver {

	private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(JavaxAudioOutput.class);

	private final boolean _noConversion;

	/**
	 * Creates a {@link JavaxAudioOutput}.
	 *
	 * @param noConvertion
	 *        Whether to use data directly read from the underlying audio system without additional
	 *        conversion.
	 */
	public JavaxAudioOutput(boolean noConvertion) {
		super();
		_noConversion = noConvertion;
	}

	@Override
	public AudioRxHandle createReceiver(RtpReceiverOptions options, UdpSocket socket, AudioFormat audio_format,
			CodecType codec,
			int payload_type, RtpPayloadFormat payloadFormat, int sample_rate, int channels, Encoder additional_decoder, RtpStreamReceiverListener listener)
			throws IOException, UnsupportedAudioFileException {

		SimpleAudioSystem.initAudioOutputLine(sample_rate, channels);

		// javax sound
		AudioOutputStream audio_output_stream = null;
		if (!_noConversion || codec.equals(CodecType.G711_ULAW) || codec.equals(CodecType.G711_ALAW)) {
			// use standard java embedded conversion provider
			audio_output_stream = SimpleAudioSystem.getOutputStream(audio_format);
		} else {
			// use conversion provider
			final AudioFormat baseFormat = SimpleAudioSystem.getBaseAudioFormat(sample_rate, channels);
			audio_output_stream = ConverterAudioSystem.convertAudioOutputStream(codec, sample_rate,
					SimpleAudioSystem.getOutputStream(baseFormat));
			LOG.info("recv x-format: {}", audio_output_stream.getFormat());
		}

		RtpStreamReceiver receiver = new RtpStreamReceiver(options, audio_output_stream, additional_decoder, payloadFormat, socket, listener);
		RtpAudioRxHandler handle = new RtpAudioRxHandler(receiver) {
			@Override
			public void start(Executor executor) {
				SimpleAudioSystem.startAudioOutputLine();
				super.start(executor);
			}

			@Override
			public void halt() {
				super.halt();
				SimpleAudioSystem.stopAudioOutputLine();
			}
		};
		return handle;
	}

}
