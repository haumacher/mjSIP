/*
 * Copyright (c) 2023 Bernhard Haumacher et al. All Rights Reserved.
 */
package org.mjsip.media.rx;

import java.io.IOException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.mjsip.media.RtpStreamReceiver;
import org.mjsip.media.RtpStreamReceiverListener;
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

	private final boolean _directConvertion;

	private final int _randomEarlyDrop;

	/**
	 * Creates a {@link JavaxAudioOutput}.
	 *
	 * @param directConvertion
	 * @param randomEarlyDrop
	 */
	public JavaxAudioOutput(boolean directConvertion, int randomEarlyDrop) {
		super();
		_directConvertion = directConvertion;
		_randomEarlyDrop = randomEarlyDrop;
	}

	@Override
	public AudioRxHandle createReceiver(UdpSocket udp_socket, AudioFormat audio_format, CodecType codec,
			int payload_type, int sample_rate, int channels, Encoder additional_decoder, RtpStreamReceiverListener listener)
			throws IOException, UnsupportedAudioFileException {
		// javax sound
		AudioOutputStream audio_output_stream = null;
		if (!_directConvertion || codec.equals(CodecType.G711_ULAW) || codec.equals(CodecType.G711_ALAW)) {
			// use standard java embedded conversion provider
			audio_output_stream = SimpleAudioSystem.getOutputStream(audio_format);
		} else {
			// use conversion provider
			final AudioFormat baseFormat = SimpleAudioSystem.getBaseAudioFormat(sample_rate, channels);
			audio_output_stream = ConverterAudioSystem.convertAudioOutputStream(codec, sample_rate,
					SimpleAudioSystem.getOutputStream(baseFormat));
			LOG.info("recv x-format: " + audio_output_stream.getFormat());
		}
		// receiver
		RtpAudioRxHandler handle = new RtpAudioRxHandler(
				new RtpStreamReceiver(audio_output_stream, additional_decoder, udp_socket, listener)) {
			@Override
			public void start() {
				SimpleAudioSystem.startAudioOutputLine();
				super.start();
			}

			@Override
			public void halt() {
				super.halt();
				SimpleAudioSystem.stopAudioOutputLine();
			}
		};
		if (_randomEarlyDrop > 0) {
			handle.setRED(_randomEarlyDrop);
			LOG.debug("Random early drop at receiver: 1 packet out of " + _randomEarlyDrop);
		}
		return handle;
	}

}
