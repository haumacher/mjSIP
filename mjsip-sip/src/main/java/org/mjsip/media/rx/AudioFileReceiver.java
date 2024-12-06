/*
 * Copyright (c) 2023 Bernhard Haumacher et al. All Rights Reserved.
 */
package org.mjsip.media.rx;

import java.io.IOException;
import java.io.OutputStream;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.mjsip.media.RtpStreamReceiver;
import org.mjsip.media.RtpStreamReceiverListener;
import org.mjsip.rtp.RtpPayloadFormat;
import org.mjsip.sound.AudioFile;
import org.slf4j.LoggerFactory;
import org.zoolu.net.UdpSocket;
import org.zoolu.sound.CodecType;
import org.zoolu.util.Encoder;

/**
 * {@link AudioReceiver} saving received audio data to a file.
 */
public class AudioFileReceiver implements AudioReceiver {

	private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(AudioFileReceiver.class);

	private final String _audioFile;

	/**
	 * Creates a {@link AudioFileReceiver}.
	 */
	public AudioFileReceiver(String audiofile_out) {
		this._audioFile = audiofile_out;
	}

	@Override
	public AudioRxHandle createReceiver(RtpReceiverOptions options, UdpSocket socket, AudioFormat audio_format,
			CodecType codec,
			int payload_type, RtpPayloadFormat payloadFormat, int sample_rate, int channels, Encoder additional_decoder, RtpStreamReceiverListener listener)
			throws IOException, UnsupportedAudioFileException {
		LOG.info("Storing audio stream to file {} format: {}", _audioFile, audio_format);
		OutputStream output_stream = AudioFile.getAudioFileOutputStream(_audioFile, audio_format);
		RtpStreamReceiver receiver = new RtpStreamReceiver(options, output_stream, additional_decoder, payloadFormat,
				socket, listener) {
			@Override
			protected void onRtpStreamReceiverTerminated(Exception error) {
				super.onRtpStreamReceiverTerminated(error);

				try {
					output_stream.close();
				} catch (IOException ex) {
					LOG.error("Closing audio stream failed: {}", _audioFile, ex);
				}
			}
		};
		return new RtpAudioRxHandler(receiver);
	}

}
