/*
 * Copyright (c) 2023 Bernhard Haumacher et al. All Rights Reserved.
 */
package org.mjsip.ua;

import java.util.HashMap;
import java.util.Map;

import org.mjsip.media.FlowSpec.Direction;
import org.mjsip.media.MediaDesc;
import org.mjsip.media.MediaSpec;
import org.mjsip.media.StreamerOptions;
import org.mjsip.media.rx.AudioFileReceiver;
import org.mjsip.media.rx.AudioReceiver;
import org.mjsip.media.rx.JavaxAudioOutput;
import org.mjsip.media.tx.AudioFileTransmitter;
import org.mjsip.media.tx.AudioTransmitter;
import org.mjsip.media.tx.JavaxAudioInput;
import org.mjsip.media.tx.ToneTransmitter;
import org.mjsip.ua.streamer.DefaultStreamerFactory;
import org.mjsip.ua.streamer.DispatchingStreamerFactory;
import org.mjsip.ua.streamer.LoopbackStreamerFactory;
import org.mjsip.ua.streamer.NativeStreamerFactory;
import org.mjsip.ua.streamer.StreamerFactory;
import org.zoolu.net.SocketAddress;
import org.zoolu.util.Configure;
import org.zoolu.util.Flags;
import org.zoolu.util.Parser;

/**
 * Definition of static media files to serve.
 */
public class MediaConfig extends Configure {

	/** 
	 * Constructs a {@link UAConfig} from the given configuration file and program arguments.
	 */
	public static MediaConfig init(String file, Flags flags, UAConfig uaConfig) {
		MediaConfig result=new MediaConfig();
		result.loadFile(file);
		result.initFrom(flags);
		result.normalize(uaConfig);
		return result;
	}

	/** Whether using audio */
	public boolean audio=true;
	/** Whether using video */
	public boolean video=false;

	/** Whether looping the received media streams back to the sender. */
	public boolean loopback=false;
	/** Whether playing a test tone in send only mode */
	public boolean sendTone=false;
	/** Audio file to be streamed */
	public String sendFile=null;
	/** Audio file to be recorded */
	public String recvFile=null;
	/** Video file to be streamed */
	public String sendVideoFile=null;
	/** Video file to be recorded */
	public String recvVideoFile=null;

	/** Whether using explicit external converter (i.e. direct access to an external conversion provider)
	  * instead of that provided by javax.sound.sampled.spi.
	  * It applies only when javax sound is used, that is when no other audio apps (such as jmf or rat) are used. */
	public boolean javaxSoundDirectConversion=false;

	/** Whether using RAT (Robust Audio Tool) as audio sender/receiver */
	public boolean useRat=false;
	/** Whether using VIC (Video Conferencing Tool) as video sender/receiver */
	public boolean useVic=false;
	/** RAT command-line executable */
	public String binRat="rat";
	/** VIC command-line executable */
	public String binVic="vic";
	
	/** Fixed audio multicast socket address; if defined, it forces the use of this maddr+port for audio session */
	public SocketAddress audioMcastSoAddr=null;
	/** Fixed video multicast socket address; if defined, it forces the use of this maddr+port for video session */
	public SocketAddress videoMcastSoAddr=null;

	/** Array of media descriptions */
	public MediaDesc[] mediaDescs=new MediaDesc[]{};
	
	/** Temporary mapping of media type to {@link MediaDesc}. */
	private Map<String, MediaDesc> _descByType=new HashMap<>();

	/**
	 * Sets the transport port for each medium.
	 * 
	 * @param portPool
	 *        The pool to take free ports from.
	 */
	public void allocateMediaPorts(PortPool portPool) {
		for (int i=0; i<mediaDescs.length; i++) {
			MediaDesc md=mediaDescs[i];
			md.setPort(portPool.allocate());
		}
	}

	/**
	 * Releases ports previously allocated using {@link #allocateMediaPorts(PortPool)}.
	 * 
	 * @param portPool The pool to put ports back to.
	 */
	public void releaseMediaPorts(PortPool portPool) {
		for (int i=0; i<mediaDescs.length; i++) {
			MediaDesc md=mediaDescs[i];
			portPool.release(md.getPort());
			md.setPort(0);
		}
	}
	
	@Override
	public void setOption(String attribute, Parser par) {
		if (attribute.equals("audio"))          {  audio=(par.getString().toLowerCase().startsWith("y"));  return;  }
		if (attribute.equals("video"))          {  video=(par.getString().toLowerCase().startsWith("y"));  return;  }

		if (attribute.equals("loopback"))       {  loopback=(par.getString().toLowerCase().startsWith("y"));  return;  }
		if (attribute.equals("send_tone"))      {  sendTone=(par.getString().toLowerCase().startsWith("y"));  return;  }
		if (attribute.equals("send_file"))      {  sendFile=par.getRemainingString().trim();  return;  }
		if (attribute.equals("recv_file"))      {  recvFile=par.getRemainingString().trim();  return;  }
		if (attribute.equals("send_video_file")){  sendVideoFile=par.getRemainingString().trim();  return;  }
		if (attribute.equals("recv_video_file")){  recvVideoFile=par.getRemainingString().trim();  return;  }

		if (attribute.equals("javax_sound_direct_convertion")) {  javaxSoundDirectConversion=(par.getString().toLowerCase().startsWith("y"));  return;  }
		
		if (attribute.equals("use_rat"))        {  useRat=(par.getString().toLowerCase().startsWith("y"));  return;  }
		if (attribute.equals("bin_rat"))        {  binRat=par.getStringUnquoted();  return;  }
		if (attribute.equals("use_vic"))        {  useVic=(par.getString().toLowerCase().startsWith("y"));  return;  }
		if (attribute.equals("bin_vic"))        {  binVic=par.getStringUnquoted();  return;  }      

		if (attribute.equals("audio_mcast_soaddr")) {  audioMcastSoAddr=new SocketAddress(par.getString());  return;  } 
		if (attribute.equals("video_mcast_soaddr")) {  videoMcastSoAddr=new SocketAddress(par.getString());  return;  }

		if (attribute.equals("media") || attribute.equals("media_desc"))    {
			MediaDesc desc = MediaDesc.parseMediaDesc(par.getRemainingString().trim());
			_descByType.put(desc.getMedia(), desc);  
		}
	}
	
	public void initFrom(Flags flags) {
		Boolean audio=flags.getBoolean("-a",null,"audio");
		if (audio!=null) this.audio=audio.booleanValue();
		
		Boolean video=flags.getBoolean("-v",null,"video");
		if (video!=null) this.video=video.booleanValue();
		
		Boolean loopback=flags.getBoolean("--loopback",null,"loopback mode, received media are sent back to the remote sender");
		if (loopback!=null) this.loopback=loopback.booleanValue();
		
		Boolean send_tone=flags.getBoolean("--send-tone",null,"send only mode, an audio test tone is generated");
		if (send_tone!=null) this.sendTone=send_tone.booleanValue();
		
		String  send_file=flags.getString("--send-file","<file>",null,"audio is played from the specified file");
		if (send_file!=null) this.sendFile=send_file;
		
		String  recv_file=flags.getString("--recv-file","<file>",null,"audio is recorded to the specified file");
		if (recv_file!=null) this.recvFile=recv_file;
		
		String  send_video_file=flags.getString("--send-video-file","<file>",null,"video is played from the specified file");
		if (send_video_file!=null) this.sendVideoFile=send_video_file;
		
		String  recv_video_file=flags.getString("--recv-video-file","<file>",null,"video is recorded to the specified file");
		if (recv_video_file!=null) this.recvVideoFile=recv_video_file;
	}
	
	private void normalize(UAConfig uaConfig) {
		if (sendFile!=null && sendFile.equalsIgnoreCase(Configure.NONE)) sendFile=null;
		if (recvFile!=null && recvFile.equalsIgnoreCase(Configure.NONE)) recvFile=null;

		// use audio as default media in case of..
		if ((uaConfig.isRecvOnly() || uaConfig.isSendOnly() || sendTone || sendFile!=null || recvFile!=null) && !video) audio=true;

		// media descriptions
		if (_descByType.size()==0 && audio) {
			// add default auido support
			_descByType.put("audio",MediaDesc.parseMediaDesc("audio 4080 RTP/AVP { 0 PCMU 8000 160, 8 PCMA 8000 160 }"));
		}
		
		int i = 0;
		mediaDescs=new MediaDesc[_descByType.size()];
		for (MediaDesc md : _descByType.values()) {
			// Remove audio or video descriptors, if audio or video has been disabled.
			if (md.getMedia().equalsIgnoreCase("audio") && !audio) continue;
			if (md.getMedia().equalsIgnoreCase("video") && !video) continue;
			
			mediaDescs[i++]=new MediaDesc(md.getMedia(),md.getPort(),md.getTransport(),md.getMediaSpecs());
		}
	}

	/**
	 * Creates a {@link MediaConfig} by copying the given descriptors.
	 * 
	 * <p>
	 * The copy is not deep. Only the {@link MediaDesc} is copied, the {@link MediaSpec}s are
	 * reused.
	 * </p>
	 */
	public static MediaConfig from(MediaDesc[] descriptors) {
		MediaConfig result = new MediaConfig();
		result.mediaDescs = copyDescriptors(descriptors);
		return result;
	}

	private static MediaDesc[] copyDescriptors(MediaDesc[] descriptors) {
		MediaDesc[] result = new MediaDesc[descriptors.length];
		for (int n = 0, cnt = descriptors.length; n < cnt; n++) {
			MediaDesc descriptor = descriptors[n];
			MediaSpec[] specs = descriptor.getMediaSpecs();
			result[n] = descriptor.withSpecs(specs);
		}
		return result;
	}	

	/**
	 * Creates a {@link StreamerFactory} based on configuration options.
	 */
	public StreamerFactory createStreamerFactory(UAConfig uaConfig) {
		if (loopback) {
			return new LoopbackStreamerFactory();
		} else {
			DispatchingStreamerFactory factory = new DispatchingStreamerFactory();
			if (audio) {
				if (useRat) {
					factory.addFactory("audio", new NativeStreamerFactory(audioMcastSoAddr, binRat));
				} else {
					Direction dir = uaConfig.getDirection();

					AudioTransmitter tx;
					if (dir.doSend()) {
						if (sendTone) {
							tx=new ToneTransmitter();
						} else if (sendFile!=null) {
							tx= new AudioFileTransmitter(sendFile);
						} else {
							tx = new JavaxAudioInput(true, javaxSoundDirectConversion);
						}
					} else {
						tx = null;
					}

					// audio output
					String audio_out=null;
					if (recvFile!=null) audio_out=recvFile;        
					
					AudioReceiver rx;
					if (dir.doReceive()) {
						if (audio_out == null) {
							rx = new JavaxAudioOutput(javaxSoundDirectConversion);
						} else {
							rx = new AudioFileReceiver(audio_out);
						}
					} else {
						rx = null;
					}

					// standard javax-based audio streamer
					StreamerOptions options = StreamerOptions.builder()
							.setRandomEarlyDrop(uaConfig.getRandomEarlyDropRate())
							.setSymmetricRtp(uaConfig.isSymmetricRtp())
							.build();
					
					factory.addFactory("audio", new DefaultStreamerFactory(options, rx, tx));
				}
			}
			if (video) {
				if (useVic) {
					factory.addFactory("video", new NativeStreamerFactory(videoMcastSoAddr, binVic));
				}
			}
			return factory;
		}
	}

}
