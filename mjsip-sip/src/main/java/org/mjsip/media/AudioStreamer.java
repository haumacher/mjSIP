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



import java.io.OutputStream;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

import org.mjsip.rtp.AmrRtpPayloadFormat;
import org.mjsip.rtp.RtpControl;
import org.slf4j.LoggerFactory;
import org.zoolu.net.SocketAddress;
import org.zoolu.net.UdpSocket;
import org.zoolu.sound.AudioOutputStream;
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
	
	/** Whether discarding out-of-sequence and duplicated packets */
	public static boolean SEQUENCE_CHECK=false;

	/** Whether filling silence intervals with (silence-equivalent) void data */
	public static boolean SILENCE_PADDING=false;

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

	/** By default whether using big-endian rule for byte ordering */
	public static final boolean DEFAULT_BIG_ENDIAN=false;


	/** Test tone */
	public static final String TONE="TONE";

	/** Test tone frequency [Hz] */
	public static int TONE_FREQ=100;

	/** Test tone amplitude (from 0.0 to 1.0) */
	public static double TONE_AMPL=1.0;

	/** Test tone sample size [bits] */
	public static int TONE_SAMPLE_SIZE=8;


	/** Whether using symmetric_rtp */
	boolean symmetric_rtp=DEFAULT_SYMMETRIC_RTP;

	/** Audio format */
	//AudioFormat audio_format;

	/** Stream direction */
	FlowSpec.Direction dir;

	/** UDP socket */
	UdpSocket udp_socket=null;

	/** RtpStreamSender */
	protected RtpStreamSender rtp_sender=null;

	/** RtpStreamReceiver */
	protected RtpStreamReceiver rtp_receiver=null;

	/** Whether using system audio capture */
	boolean audio_input=false;

	/** Whether using system audio playout */
	boolean audio_output=false;
	
	/** RTCP */
	RtpControl rtp_control=null;



	/** Creates a new audio streamer. */
	public AudioStreamer(RtpStreamSender rtp_sender, RtpStreamReceiver rtp_receiver, boolean symmetric_rtp) {
		this.rtp_sender=rtp_sender;
		this.rtp_receiver=rtp_receiver;
		this.symmetric_rtp=symmetric_rtp;
		LOG.debug("codec: [unknown]");
	}


	/** Creates a new audio streamer. */
	/*public AudioStreamer(int local_port, String remote_addr, int remote_port, MediaApp.MediaDirection direction) {
		init(local_port,remote_addr,remote_port,direction,null,null,null,-1,0,0,true,symmetric_rtp);
	}*/


	/** Creates a new audio streamer.
	  * @param flow_spec the flow specification
	  * @param direct_convertion whether using explicit external converter (i.e. direct access to an external conversion provider)
	  *    instead of that provided by javax.sound.sampled.spi.
	  *    It applies only when javax sound is used, that is when no other audio streamers (such as jmf or rat) are used
	  * @param additional_encoding additional audio encoder/decoder (optional)
	  * @param do_sync whether enforcing time synchronization to RTP source stream.
	  *    If synchronization is explicitly performed, the departure time of each RTP packet is equal to its nominal time.
	  *    Note that when using audio capturing, synchronization with the sample rate
	  *    is implicitly performed by the audio capture device and frames are read at constant bit rate.
	  *    However, an explicit re-synchronization is suggested
	  *    in order to let the read() method be non-blocking (in the other case
	  *    the UA audio performance seems decreasing
	  * @param random_early_drop receiver random early drop (RED) rate.
	  *    Actually it is the inverse of packet drop rate.
	  *    It can used to prevent long play back delay. 
	  *    A value less or equal to 0 means that no packet dropping is explicitly
	  *    performed at the RTP receiver
	  * @param symmetric_rtp whether using symmetric_rtp
	  * @param logger a logger for recording log messages (or <i>null</i>) */
	public AudioStreamer(FlowSpec flow_spec, String audiofile_in, String audiofile_out, boolean direct_convertion, Codec additional_encoding, boolean do_sync, int random_early_drop, boolean symmetric_rtp) {
		MediaSpec audio_spec=flow_spec.getMediaSpec();
		LOG.debug("audio_spec: " + audio_spec.toString());
		init(flow_spec.getLocalPort(),flow_spec.getRemoteAddress(),flow_spec.getRemotePort(),flow_spec.getDirection(),audiofile_in,audiofile_out,audio_spec.getCodec(),audio_spec.getAVP(),audio_spec.getSampleRate(),audio_spec.getChannels(),audio_spec.getPacketSize(),direct_convertion,additional_encoding,do_sync,random_early_drop,symmetric_rtp,false);
	}


	/** Creates a new audio streamer.
	  * @param flow_spec the flow specification
	  * @param direct_convertion whether using explicit external converter (i.e. direct access to an external conversion provider)
	  *    instead of that provided by javax.sound.sampled.spi.
	  *    It applies only when javax sound is used, that is when no other audio streamers (such as jmf or rat) are used
	  * @param additional_encoding additional audio encoder/decoder (optional)
	  * @param do_sync whether enforcing time synchronization to RTP source stream.
	  *    If synchronization is explicitly performed, the departure time of each RTP packet is equal to its nominal time.
	  *    Note that when using audio capturing, synchronization with the sample rate
	  *    is implicitly performed by the audio capture device and frames are read at constant bit rate.
	  *    However, an explicit re-synchronization is suggested
	  *    in order to let the read() method be non-blocking (in the other case
	  *    the UA audio performance seems decreasing
	  * @param random_early_drop receiver random early drop (RED) rate.
	  *    Actually it is the inverse of packet drop rate.
	  *    It can used to prevent long play back delay. 
	  *    A value less or equal to 0 means that no packet dropping is explicitly
	  *    performed at the RTP receiver
	  * @param symmetric_rtp whether using symmetric_rtp
	  * @param rtcp whether using rtcp
	  * @param logger a logger for recording log messages (or <i>null</i>) */
	public AudioStreamer(FlowSpec flow_spec, String audiofile_in, String audiofile_out, boolean direct_convertion, Codec additional_encoding, boolean do_sync, int random_early_drop, boolean symmetric_rtp, boolean rtcp) {
		MediaSpec audio_spec=flow_spec.getMediaSpec();
		LOG.debug("audio_spec: " + audio_spec.toString());
		init(flow_spec.getLocalPort(),flow_spec.getRemoteAddress(),flow_spec.getRemotePort(),flow_spec.getDirection(),audiofile_in,audiofile_out,audio_spec.getCodec(),audio_spec.getAVP(),audio_spec.getSampleRate(),audio_spec.getChannels(),audio_spec.getPacketSize(),direct_convertion,additional_encoding,do_sync,random_early_drop,symmetric_rtp,rtcp);
	}


	/** Initializes the audio streamer. */
	private void init(int local_port, String remote_addr, int remote_port, FlowSpec.Direction direction, String audiofile_in, String audiofile_out, String codec_name, int payload_type, int sample_rate, int channels, int packet_size, boolean direct_convertion, Codec additional_encoding, boolean do_sync, int random_early_drop, boolean symmetric_rtp, boolean rtcp) {
		LOG.debug("initalization");
		this.dir=direction;
		this.symmetric_rtp=symmetric_rtp;
		// 1) in case not defined, use default values
		if (codec_name==null) codec_name=DEFAULT_CODEC_NAME;
		//if (payload_type<0) payload_type=DEFAULT_PAYLOAD_TYPE;
		if (sample_rate<=0) sample_rate=DEFAULT_SAMPLE_RATE;
		//if (packet_size<=0) packet_size=DEFAULT_PACKET_SIZE;
		
		// 2) codec name translation
		codec_name=codec_name.toUpperCase();
		CodecType codec=CodecType.getByName(codec_name);
		LOG.debug("codec: "+codec_name);     
		if (!codec_name.equals(codec.toString())) LOG.debug("codec mapped to: "+codec);

		// 3) payload_type, frame_size, frame_rate, packet_size, packet_time
		int frame_size=channels*((codec!=null)? codec.getFrameSize() : DEFAULT_FRAME_SIZE);
		int frame_rate=(codec!=null)? sample_rate/codec.getSamplesPerFrame() : DEFAULT_FRAME_RATE;
		if (payload_type<0) payload_type=(codec!=null)? codec.getPayloadType() : UNKNOWN_PAYLOAD_TYPE;
		//int packet_rate=(packet_size>0)? frame_rate*frame_size/packet_size : DEFAULT_PACKET_RATE;
		long packet_time=(packet_size>0)? (long)(packet_size*1000/(frame_rate*frame_size/channels)) : DEFAULT_PACKET_TIME;
		if (packet_size<=0) packet_size=(int)(frame_rate*frame_size*DEFAULT_PACKET_TIME/1000);
		LOG.debug("packet size: "+packet_size+ "B");
		LOG.debug("packet time: "+packet_time+ "ms");
		LOG.debug("packet rate: "+(1000/packet_time)+ "pkt/s");
	
		// 4) find the proper supported AudioFormat
		LOG.debug("base audio format: "+SimpleAudioSystem.getBaseAudioFormat(sample_rate,channels).toString());
		AudioFormat audio_format=null;
		AudioFormat.Encoding encoding=null;
		// get the proper audio format encoding
		AudioFormat.Encoding[] supported_encodings=AudioSystem.getTargetEncodings(SimpleAudioSystem.getBaseAudioFormat(sample_rate,channels));
		StringBuffer supported_list=new StringBuffer();
		for (int i=0; i<supported_encodings.length; i++) supported_list.append(supported_encodings[i].toString()).append(", ");
		LOG.info("Supported codecs: "+supported_list.toString());
		String codec_str=codec.toString();
		if (codec_str.equalsIgnoreCase("G711_ULAW")) codec_str="ULAW";
		else
		if (codec_str.equalsIgnoreCase("G711_ALAW")) codec_str="ALAW";
		else
		if (codec_str.equalsIgnoreCase("PCM_LINEAR")) codec_str="PCM_SIGNED";
		for (int i=0; i<supported_encodings.length ; i++) {
			// printLOG.info("supported_encoding["+i+"]: "+supported_encodings[i],LogWriter.LEVEL_HIGH);
			if (supported_encodings[i].toString().equalsIgnoreCase(codec_str))  {
				encoding=supported_encodings[i];
				// printLOG.info("supported_encoding["+i+"]: OK",LogWriter.LEVEL_HIGH);
				break;
			}
		}
		if (encoding!=null) {
			// get the target audio format
			LOG.debug("base audio format: " + SimpleAudioSystem.getBaseAudioFormat(sample_rate, channels));
			AudioFormat[] available_formats=AudioSystem.getTargetFormats(encoding,SimpleAudioSystem.getBaseAudioFormat(sample_rate,channels));
			for (int i=0; i<available_formats.length ; i++) {
				if (available_formats[i].getEncoding().equals(encoding)) {
					audio_format=available_formats[i];
					LOG.debug("audio format: " + audio_format);
					break;
				}
			}
			LOG.info("target audio format: "+audio_format);
			//LOG.debug("frame_size: "+audio_format.getFrameSize());
			//LOG.debug("frame_rate: "+audio_format.getFrameRate());
			//LOG.debug("big_endian: "+audio_format.isBigEndian());
		}
		else LOG.warn("LOG.warn(pported");

		// RTP AMR payload format
		if (codec.equals(CodecType.AMR_NB) || codec.equals(CodecType.AMR_0475) || codec.equals(CodecType.AMR_0515) || codec.equals(CodecType.AMR_0590) || codec.equals(CodecType.AMR_0670) || codec.equals(CodecType.AMR_0740) || codec.equals(CodecType.AMR_0795) || codec.equals(CodecType.AMR_1020) || codec.equals(CodecType.AMR_1220)) {
			AmrRtpPayloadFormat amr_payload_format=new AmrRtpPayloadFormat(RTP_BANDWIDTH_EFFICIENT_MODE);
			if (rtp_sender!=null) rtp_sender.setRtpPayloadFormat(amr_payload_format);
			if (rtp_receiver!=null) rtp_receiver.setRtpPayloadFormat(amr_payload_format);
			LOG.debug("RTP format: " + codec + " in "
					+ ((RTP_BANDWIDTH_EFFICIENT_MODE) ? "Bandwidth-Efficinet" : "Octect-Alignied") + " Mode");
		}

		Encoder additional_encoder=null;
		Encoder additional_decoder=null;
		if (additional_encoding!=null) {
			additional_encoder=additional_encoding.getEncoder();
		    additional_decoder=additional_encoding.getDecoder();
		}	
		
		LOG.debug("sample rate: " + sample_rate + "Hz");
		LOG.debug("packet size: " + packet_size + "B");
		LOG.debug("packet time: " + packet_time + "ms");
		LOG.debug("packet rate: " + (1000 / packet_time) + "pkt/s");
		LOG.debug("audio format: " + audio_format);

		try {
			// 5) udp socket
			udp_socket=new UdpSocket(local_port);
			
			// 6) sender
			if ((dir==FlowSpec.SEND_ONLY || dir==FlowSpec.FULL_DUPLEX)) {
				LOG.debug("new audio sender to "+remote_addr+":"+remote_port);
				if (audiofile_in!=null && audiofile_in.equals(AudioStreamer.TONE)) {
					// tone generator
					LOG.info("Tone generator: " + TONE_FREQ + " Hz");
					ToneInputStream tone=new ToneInputStream(TONE_FREQ,TONE_AMPL,sample_rate,TONE_SAMPLE_SIZE,ToneInputStream.PCM_LINEAR_UNSIGNED,DEFAULT_BIG_ENDIAN);
					// sender
					rtp_sender=new RtpStreamSender(tone,true,payload_type,sample_rate,channels,packet_time,packet_size,additional_encoder,udp_socket,remote_addr,remote_port,this);
				}
				else
				if (audiofile_in!=null) {
					AudioInputStream audio_input_stream=AudioFile.getAudioFileInputStream(audiofile_in,audio_format);
					rtp_sender=new RtpStreamSender(audio_input_stream,true,payload_type,sample_rate,channels,packet_time,packet_size,additional_encoder,udp_socket,remote_addr,remote_port,this);
				}
				else {
					// javax sound
					AudioInputStream audio_input_stream=null;
					if (!direct_convertion || codec.equals(CodecType.G711_ULAW) || codec.equals(CodecType.G711_ALAW)) {
						// use standard java embedded conversion provider
						audio_input_stream=SimpleAudioSystem.getInputStream(audio_format);          
					}
					else {
						// use my explicit conversion provider
						Class audio_system=Class.forName("org.zoolu.ext.sound.ConverterAudioSystem");
						java.lang.reflect.Method get_input_stream=audio_system.getMethod("convertAudioInputStream",new Class[]{ String.class, int.class, AudioInputStream.class });
						audio_input_stream=(AudioInputStream)get_input_stream.invoke(null,new Object[]{ codec, new Integer(sample_rate), SimpleAudioSystem.getInputStream(SimpleAudioSystem.getBaseAudioFormat(sample_rate,channels)) });
						LOG.info("send x-format: " + audio_input_stream.getFormat());
					}
					// sender
					rtp_sender=new RtpStreamSender(audio_input_stream,do_sync,payload_type,sample_rate,channels,packet_time,packet_size,additional_encoder,udp_socket,remote_addr,remote_port,this);
					//if (sync_adj>0) sender.setSyncAdj(sync_adj);
					audio_input=true;
				}
			}
			
			// 7) receiver
			if (dir==FlowSpec.RECV_ONLY || dir==FlowSpec.FULL_DUPLEX) {
				LOG.debug("new audio receiver on "+local_port);
				if (audiofile_out!=null) {
					OutputStream output_stream=AudioFile.getAudioFileOutputStream(audiofile_out,codec,sample_rate);
					rtp_receiver=new RtpStreamReceiver(output_stream,additional_decoder,udp_socket);
				}
				else {
					// javax sound
					AudioOutputStream audio_output_stream=null;
					if (!direct_convertion || codec.equals(CodecType.G711_ULAW) || codec.equals(CodecType.G711_ALAW)) {
						// use standard java embedded conversion provider
						audio_output_stream=SimpleAudioSystem.getOutputStream(audio_format);
					}
					else {
						// use my explicit conversion provider
						Class audio_system=Class.forName("org.zoolu.ext.sound.ConverterAudioSystem");
						java.lang.reflect.Method get_output_stream=audio_system.getMethod("convertAudioOutputStream",new Class[]{ String.class, int.class, AudioOutputStream.class });
						audio_output_stream=(AudioOutputStream)get_output_stream.invoke(null,new Object[]{ codec, new Integer(sample_rate), SimpleAudioSystem.getOutputStream(SimpleAudioSystem.getBaseAudioFormat(sample_rate,channels)) });
						LOG.info("recv x-format: " + audio_output_stream.getFormat());
					}
					// receiver
					rtp_receiver=new RtpStreamReceiver(audio_output_stream,additional_decoder,udp_socket,this);
					if (random_early_drop>0) rtp_receiver.setRED(random_early_drop);
					audio_output=true;
				}
			}
			// RTCP
			if (rtcp) {
				rtp_control=new RtpControl(null,udp_socket.getLocalPort()+1,remote_addr,remote_port+1);
				if (rtp_sender!=null) rtp_sender.setControl(rtp_control);
			}
			// SEQUENCE CHECK
			if (rtp_receiver!=null) rtp_receiver.setSequenceCheck(SEQUENCE_CHECK);
			
			// SILENCE PADDING
			if (rtp_receiver!=null) rtp_receiver.setSilencePadding(SILENCE_PADDING);
		}
		catch (Exception e) {
			LOG.info("Exception.", e);
		}
		LOG.debug("DEBUG: Codec: " + codec);
		LOG.debug("DEBUG: Frame rate: " + frame_rate + " frame/s");
		LOG.debug("DEBUG: Frame size: " + frame_size + " B");
		LOG.debug("DEBUG: Packet time: " + packet_time + " ms");
		LOG.debug("DEBUG: Packet rate: " + (1000 / packet_time) + " pkt/s");
		LOG.debug("DEBUG: Packet size: " + packet_size + " B");
		if (random_early_drop > 0)
			LOG.debug("DEBUG: Random early drop at receiver: 1 packet out of " + random_early_drop);
	}


	/** Starts media streams. */
	@Override
	public boolean start() {
		LOG.info("starting java audio");
		if (rtp_sender!=null) {
			LOG.debug("start sending");
			if (audio_input) SimpleAudioSystem.startAudioInputLine();
			rtp_sender.start();
		}
		if (rtp_receiver!=null) {
			LOG.debug("start receiving");
			if (audio_output) SimpleAudioSystem.startAudioOutputLine();
			rtp_receiver.start();
		}
		return true;      
	}


	/** Stops media streams. */
	@Override
	public boolean halt() {
		LOG.info("stopping java audio");    
		if (rtp_sender!=null) {
			rtp_sender.halt();
			rtp_sender=null;
			LOG.debug("sender halted");
		}      
		if (audio_input) SimpleAudioSystem.stopAudioInputLine();
	 
		if (rtp_receiver!=null) {
			rtp_receiver.halt();
			rtp_receiver=null;
			LOG.debug("receiver halted");
		}      
		if (audio_output) SimpleAudioSystem.stopAudioOutputLine();

		// try to take into account the resilience of RtpStreamSender
		try { Thread.sleep(RtpStreamReceiver.SO_TIMEOUT); } catch (Exception e) {}
		udp_socket.close();
		if (rtp_control!=null) rtp_control.halt();
		return true;
	}
	
	
	/** Sets symmetric RTP mode. */
	public void setSymmetricRtp(boolean symmetric_rtp) {
		this.symmetric_rtp=symmetric_rtp;
	}


	/** whether symmetric RTP mode is set. */
	public boolean isSymmetricRtp() {
		return symmetric_rtp;
	}


	/** From RtpStreamReceiverListener. When the remote socket address (source) is changed. */
	@Override
	public void onRemoteSoAddressChanged(RtpStreamReceiver rr, SocketAddress remote_soaddr) {
		try {
			if (symmetric_rtp && rtp_sender!=null) rtp_sender.setRemoteSoAddress(remote_soaddr);
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


	/** Sets the synchronization adjustment time (in milliseconds). 
	  * It accelerates (sync_adj &lt; 0) or reduces (sync_adj &gt; 0) the sending rate respect to the nominal value.
	  * @param sync_adj the difference between the actual inter-packet sending time respect to the nominal value (in milliseconds). */
	public void setSyncAdj(long sync_adj) {
		if (rtp_sender!=null) rtp_sender.setSyncAdj(sync_adj);
		LOG.debug("Inter-packet time adjustment at sender: " + sync_adj + " ms every packet");
	}


	/** Sets the receiver packet random early drop (RED) value; if greater than 0, it is the inverse of the packet drop rate.
	  * @param random_early_drop the number of packets that separates two drops at receiver; a value of 0 means no drop. */
	public void setRED(int random_early_drop) {
		if (rtp_receiver!=null) rtp_receiver.setRED(random_early_drop);
	}

}