/*
 * Copyright (C) 2013 Luca Veltri - University of Parma - Italy
 * 
 * This source code is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This source code is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this source code; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 * 
 * Author(s):
 * Luca Veltri (luca.veltri@unipr.it)
 */

package org.mjsip.media;



import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioFormat.Encoding;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.mjsip.media.rx.AudioReceiver;
import org.mjsip.media.rx.AudioRxHandle;
import org.mjsip.media.tx.AudioTXHandle;
import org.mjsip.media.tx.AudioTransmitter;
import org.mjsip.rtp.AmrRtpPayloadFormat;
import org.mjsip.rtp.RtpControl;
import org.mjsip.sound.Codec;
import org.slf4j.LoggerFactory;
import org.zoolu.net.SocketAddress;
import org.zoolu.net.UdpSocket;
import org.zoolu.sound.CodecType;
import org.zoolu.sound.SimpleAudioSystem;
import org.zoolu.util.Encoder;



/** Full-duplex audio streamer based on javax.sound.
  */
public class AudioStreamer implements MediaStreamer, RtpStreamSenderListener, RtpStreamReceiverListener {
	
	private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(AudioStreamer.class);

	/** Internal buffer size. */
	public static final int INTERNAL_BUFFER_SIZE=40960;

	/** Whether using RTP payload format in Bandwidth-Efficient Mode */
	public static boolean RTP_BANDWIDTH_EFFICIENT_MODE=false;

	/** Whether using symmetric RTP by default */
	public static final boolean DEFAULT_SYMMETRIC_RTP=false;
	
	/** Unknown payload type */
	public static final int UNKNOWN_PAYLOAD_TYPE=111;

	/** Default codec */
	public static final String DEFAULT_CODEC_NAME="ULAW";

	/** Default payload type */
	public static final int DEFAULT_PAYLOAD_TYPE=0;

	/** Default sample rate [sample/sec] */
	public static final int DEFAULT_SAMPLE_RATE=8000;

	/** Default number of audio channels (1 for mono, 2 for stereo) */
	public static final int DEFAULT_CHANNELS=1;

	/** Default codec frame size [byte] */
	public static final int DEFAULT_FRAME_SIZE=DEFAULT_CHANNELS*1;

	/** Default codec frame rate [frame/sec] */
	public static final int DEFAULT_FRAME_RATE=8000;

	/** Default packet size [byte] */
	//public static final int DEFAULT_PACKET_SIZE=160;

	/** Default packet rate [pkt/sec] */
	//public static final int DEFAULT_PACKET_RATE=50;

	/** Default inter-packet time [millisecs] */
	public static final int DEFAULT_PACKET_TIME=20;

	/** Whether using symmetric_rtp */
	private final boolean _symmetricRtp;

	/** Stream direction */
	private final FlowSpec.Direction dir;

	/** UDP socket */
	private final UdpSocket udp_socket;

	private AudioTXHandle _txHandle;

	/** RtpStreamReceiver */
	private AudioRxHandle _rxHandle;

	/** RTCP */
	private final RtpControl rtp_control;

	private Executor _executor;

	/**
	 * Creates a new audio streamer.
	 * @param flow_spec
	 *        the flow specification
	 */
	public AudioStreamer(Executor executor, FlowSpec flow_spec, AudioTransmitter tx, AudioReceiver rx, StreamerOptions options) {
		_executor = executor;
		MediaSpec mediaSpec = flow_spec.getMediaSpec();

		int channels = mediaSpec.getChannels();
		int payload_type = mediaSpec.getAVP();

		this.dir = flow_spec.getDirection();
		_symmetricRtp = options.symmetricRtp();

		int sample_rate = mediaSpec.getSampleRate();
		if (sample_rate<=0) sample_rate=DEFAULT_SAMPLE_RATE;
		
		CodecType codec = mediaSpec.getCodecType();
		if (codec == null) {
			throw new RuntimeException("No codec found for: " + mediaSpec);
		}

		int frame_size = channels * codec.getFrameSize();
		int frame_rate = sample_rate / codec.getSamplesPerFrame();
		if (payload_type < 0) {
			payload_type = codec.getPayloadType();
		}

		int packet_size = mediaSpec.getPacketSize();
		//int packet_rate=(packet_size>0)? frame_rate*frame_size/packet_size : DEFAULT_PACKET_RATE;
		long packet_time = (packet_size > 0) ? (long) (packet_size * 1000 / (frame_rate * frame_size / channels))
				: DEFAULT_PACKET_TIME;
		if (packet_size <= 0) {
			packet_size = frame_rate * frame_size * DEFAULT_PACKET_TIME / 1000;
		}
	
		final AudioFormat baseFormat = SimpleAudioSystem.getBaseAudioFormat(sample_rate, channels);
		LOG.info("Base format: {}", baseFormat);

		AudioFormat.Encoding encoding = getEncoding(baseFormat, codec);
		if (encoding == null) {
			throw new RuntimeException("Encoding " + codec.getEncoding() + " not found for: " + baseFormat);
		}

		AudioFormat targetFormat = getTargetFormat(baseFormat, encoding);
		if (targetFormat == null) {
			throw new RuntimeException("No target fomat with encoding " + encoding + " found for: " + baseFormat);
		}
		LOG.info("Target format: {}", targetFormat);

		Encoder additional_encoder=null;
		Encoder additional_decoder=null;
		Codec additionalCodec = options.additionalCodec();
		if (additionalCodec != null) {
			additional_encoder = additionalCodec.getEncoder();
			additional_decoder = additionalCodec.getDecoder();
		}	
		
		try {
			// 5) udp socket
			udp_socket = new UdpSocket(flow_spec.getLocalPort());
			
			// 6) sender
			String remote_addr = flow_spec.getRemoteAddress();
			int remote_port = flow_spec.getRemotePort();

			// RTP AMR payload format
			AmrRtpPayloadFormat payloadFormat;
			if (codec.equals(CodecType.AMR_NB) || codec.equals(CodecType.AMR_0475) || codec.equals(CodecType.AMR_0515)
					|| codec.equals(CodecType.AMR_0590) || codec.equals(CodecType.AMR_0670)
					|| codec.equals(CodecType.AMR_0740) || codec.equals(CodecType.AMR_0795)
					|| codec.equals(CodecType.AMR_1020) || codec.equals(CodecType.AMR_1220)) {
				payloadFormat = new AmrRtpPayloadFormat(RTP_BANDWIDTH_EFFICIENT_MODE);
				LOG.debug("RTP format: {} in {} Mode", codec,
						((RTP_BANDWIDTH_EFFICIENT_MODE) ? "Bandwidth-Efficinet" : "Octect-Alignied"));
			} else {
				payloadFormat = null;
			}

			// RTCP
			if (options.rtp()) {
				rtp_control = new RtpControl(null, udp_socket.getLocalPort() + 1, remote_addr, remote_port + 1);
			} else {
				rtp_control = null;
			}

			if (tx != null) {
				_txHandle = tx.createSender(options, udp_socket, targetFormat, codec, payload_type, payloadFormat,
						sample_rate, channels, additional_encoder, packet_time, packet_size, remote_addr, remote_port,
						this, rtp_control);
			} else {
				_txHandle = null;
			}

			// 7) receiver
			if (dir.doReceive()) {
				_rxHandle = rx.createReceiver(options, udp_socket, targetFormat, codec, payload_type, payloadFormat,
						sample_rate, channels, additional_decoder, this);
			} else {
				_rxHandle = null;
			}
		}
		catch (IOException | UnsupportedAudioFileException ex) {
			throw new RuntimeException("Media streamer initialization failed.", ex);
		}

		LOG.debug("Codec:         {}", codec);
		LOG.debug("Base format:   {}", baseFormat);
		LOG.debug("Target format: {}", targetFormat);
		LOG.debug("Sample rate:   {} Hz", sample_rate);
		LOG.debug("Frame rate:    {} frame/s", frame_rate);
		LOG.debug("Frame size:    {} B", frame_size);
		LOG.debug("Packet time:   {} ms", packet_time);
		LOG.debug("Packet rate:   {} pkt/s", (1000 / packet_time));
		LOG.debug("Packet size:   {} B", packet_size);
	}

	private static AudioFormat getTargetFormat(final AudioFormat baseFormat, AudioFormat.Encoding encoding) {
		AudioFormat[] formats = AudioSystem.getTargetFormats(encoding, baseFormat);
		for (AudioFormat format : formats) {
			if (format.getEncoding().equals(encoding)) {
				return format;
			}
		}
		return null;
	}

	private static AudioFormat.Encoding getEncoding(final AudioFormat format, CodecType codec) {
		String encodingName = codec.getEncoding();
		// get the proper audio format encoding
		AudioFormat.Encoding[] supportedEncodings = AudioSystem.getTargetEncodings(format);
		LOG.info("Supported codecs: "
				+ Arrays.stream(supportedEncodings).map(Object::toString).collect(Collectors.joining(", ")));
		for (Encoding supportedEncoding : supportedEncodings) {
			if (supportedEncoding.toString().equalsIgnoreCase(encodingName)) {
				return supportedEncoding;
			}
		}
		return null;
	}

	/** Starts media streams. */
	@Override
	public boolean start() {
		LOG.info("starting java audio");
		if (_txHandle != null) {
			LOG.debug("start sending");
			_txHandle.start(_executor);
		}
		if (_rxHandle != null) {
			LOG.debug("start receiving");
			_rxHandle.start(_executor);
		}
		return true;      
	}


	/** Stops media streams. */
	@Override
	public boolean halt() {
		LOG.info("stopping java audio");    
		if (_txHandle != null) {
			_txHandle.halt();
			_txHandle = null;
			LOG.debug("sender halted");
		}      
	 
		if (_rxHandle != null) {
			_rxHandle.halt();
			_rxHandle = null;
			LOG.debug("receiver halted");
		}      

		// try to take into account the resilience of RtpStreamSender
		try {
			Thread.sleep(RtpStreamReceiver.SO_TIMEOUT);
		} catch (Exception e) {
			// Ignore.
		}
		udp_socket.close();
		if (rtp_control!=null) rtp_control.halt();
		return true;
	}

	/** From RtpStreamReceiverListener. When the remote socket address (source) is changed. */
	@Override
	public void onRemoteSoAddressChanged(RtpStreamReceiver rr, SocketAddress remote_soaddr) {
		try {
			if (_symmetricRtp && _txHandle != null)
				_txHandle.setRemoteSoAddress(remote_soaddr);
		}
		catch (Exception e) {
			LOG.info("Exception.", e);
		}
	}

	/** From RtpStreamReceiverListener. When the stream receiver terminated. */
	@Override
	public void onRtpStreamReceiverTerminated(RtpStreamReceiver rr, Exception error) {
		if (error != null)
			LOG.info("Exception.", error);
	}

	/** From RtpStreamSenderListener. When the stream sender terminated. */
	@Override
	public void onRtpStreamSenderTerminated(RtpStreamSender rs, Exception error) {
		if (error != null)
			LOG.info("Exception.", error);
	}

	/**
	 * Waits until the current transmission has completed.
	 */
	public void join() throws InterruptedException {
		_txHandle.join();
	}

}
