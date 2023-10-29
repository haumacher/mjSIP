/*
 * Copyright (c) 2023 Bernhard Haumacher et al. All Rights Reserved.
 */
package org.mjsip.ua;

import java.util.HashMap;
import java.util.Map;

import org.mjsip.media.MediaDesc;
import org.mjsip.media.MediaSpec;
import org.mjsip.ua.pool.PortPool;
import org.zoolu.net.SocketAddress;
import org.zoolu.util.Configure;
import org.zoolu.util.Flags;
import org.zoolu.util.Parser;

/**
 * Definition of media streams.
 */
public class MediaConfig extends Configure implements MediaOptions {

	/** 
	 * Constructs a {@link UAConfig} from the given configuration file and program arguments.
	 */
	public static MediaConfig init(String file, Flags flags) {
		MediaConfig result=new MediaConfig();
		result.loadFile(file);
		result.initFrom(flags);
		result.normalize();
		return result;
	}

	private boolean _audio=true;
	private boolean _video=false;

	private String _sendVideoFile=null;
	private String _recvVideoFile=null;

	private boolean _javaxSoundDirectConversion=false;

	private boolean _useRat=false;
	private boolean _useVic=false;
	private String _binRat="rat";
	private String _binVic="vic";
	
	private SocketAddress _audioMcastSoAddr=null;
	private SocketAddress _videoMcastSoAddr=null;

	private MediaDesc[] _mediaDescs=new MediaDesc[]{};
	
	/** Temporary mapping of media type to {@link MediaDesc}. */
	private Map<String, MediaDesc> _descByType=new HashMap<>();

	private int _randomEarlyDropRate=20;

	private boolean _symmetricRtp=false;

	@Override
	public int getRandomEarlyDropRate() {
		return _randomEarlyDropRate;
	}

	/** @see #getRandomEarlyDropRate() */
	public void setRandomEarlyDropRate(int randomEarlyDropRate) {
		_randomEarlyDropRate = randomEarlyDropRate;
	}

	@Override
	public boolean isSymmetricRtp() {
		return _symmetricRtp;
	}

	/** @see #isSymmetricRtp() */
	public void setSymmetricRtp(boolean symmetricRtp) {
		_symmetricRtp = symmetricRtp;
	}


	/**
	 * Sets the transport port for each medium.
	 * 
	 * @param portPool
	 *        The pool to take free ports from.
	 */
	public void allocateMediaPorts(PortPool portPool) {
		for (int i=0; i<getMediaDescs().length; i++) {
			MediaDesc md=getMediaDescs()[i];
			md.setPort(portPool.allocate());
		}
	}

	/**
	 * Releases ports previously allocated using {@link #allocateMediaPorts(PortPool)}.
	 * 
	 * @param portPool The pool to put ports back to.
	 */
	public void releaseMediaPorts(PortPool portPool) {
		for (int i=0; i<getMediaDescs().length; i++) {
			MediaDesc md=getMediaDescs()[i];
			portPool.release(md.getPort());
			md.setPort(0);
		}
	}
	
	@Override
	public void setOption(String attribute, Parser par) {
		if (attribute.equals("audio"))          {  setAudio((par.getString().toLowerCase().startsWith("y")));  return;  }
		if (attribute.equals("video"))          {  setVideo((par.getString().toLowerCase().startsWith("y")));  return;  }

		if (attribute.equals("send_video_file")){  setSendVideoFile(par.getRemainingString().trim());  return;  }
		if (attribute.equals("recv_video_file")){  setRecvVideoFile(par.getRemainingString().trim());  return;  }

		if (attribute.equals("javax_sound_direct_convertion")) {  setJavaxSoundDirectConversion((par.getString().toLowerCase().startsWith("y")));  return;  }
		
		if (attribute.equals("use_rat"))        {  setUseRat((par.getString().toLowerCase().startsWith("y")));  return;  }
		if (attribute.equals("bin_rat"))        {  setBinRat(par.getStringUnquoted());  return;  }
		if (attribute.equals("use_vic"))        {  setUseVic((par.getString().toLowerCase().startsWith("y")));  return;  }
		if (attribute.equals("bin_vic"))        {  setBinVic(par.getStringUnquoted());  return;  }      

		if (attribute.equals("audio_mcast_soaddr")) {  setAudioMcastSoAddr(new SocketAddress(par.getString()));  return;  } 
		if (attribute.equals("video_mcast_soaddr")) {  setVideoMcastSoAddr(new SocketAddress(par.getString()));  return;  }

		if (attribute.equals("random_early_drop_rate")) {  setRandomEarlyDropRate(par.getInt());  return;  }
		if (attribute.equals("symmetric_rtp"))  {  setSymmetricRtp((par.getString().toLowerCase().startsWith("y")));  return;  } 

		if (attribute.equals("media") || attribute.equals("media_desc"))    {
			MediaDesc desc = MediaDesc.parseMediaDesc(par.getRemainingString().trim());
			_descByType.put(desc.getMediaType(), desc);  
		}
	}
	
	/**
	 * Reads command-line arguments.
	 */
	public void initFrom(Flags flags) {
		Boolean audio=flags.getBoolean("-a",null,"audio");
		if (audio!=null) this.setAudio(audio.booleanValue());
		
		Boolean video=flags.getBoolean("-v",null,"video");
		if (video!=null) this.setVideo(video.booleanValue());
		
		String  send_video_file=flags.getString("--send-video-file","<file>",null,"video is played from the specified file");
		if (send_video_file!=null) this.setSendVideoFile(send_video_file);
		
		String  recv_video_file=flags.getString("--recv-video-file","<file>",null,"video is recorded to the specified file");
		if (recv_video_file!=null) this.setRecvVideoFile(recv_video_file);
	}
	
	protected void normalize() {
		// media descriptions
		if (_descByType.size()==0 && isAudio()) {
			// add default auido support
			_descByType.put("audio",MediaDesc.parseMediaDesc("audio 4080 RTP/AVP { 0 PCMU 8000 160, 8 PCMA 8000 160 }"));
		}
		
		int i = 0;
		setMediaDescs(new MediaDesc[_descByType.size()]);
		for (MediaDesc md : _descByType.values()) {
			// Remove audio or video descriptors, if audio or video has been disabled.
			if (md.getMediaType().equalsIgnoreCase("audio") && !isAudio()) continue;
			if (md.getMediaType().equalsIgnoreCase("video") && !isVideo()) continue;
			
			getMediaDescs()[i++]=new MediaDesc(md.getMediaType(),md.getPort(),md.getTransport(),md.getMediaSpecs());
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
		result.setMediaDescs(copyDescriptors(descriptors));
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

	@Override
	public boolean isAudio() {
		return _audio;
	}

	/** @see #isAudio() */
	public void setAudio(boolean audio) {
		_audio = audio;
	}

	@Override
	public boolean isVideo() {
		return _video;
	}

	/** @see #isVideo() */
	public void setVideo(boolean video) {
		_video = video;
	}

	/** Video file to be streamed */
	public String getSendVideoFile() {
		return _sendVideoFile;
	}

	/** @see #getSendVideoFile() */
	public void setSendVideoFile(String sendVideoFile) {
		_sendVideoFile = sendVideoFile;
	}

	/** Video file to be recorded */
	public String getRecvVideoFile() {
		return _recvVideoFile;
	}

	/** @see #getRecvVideoFile() */
	public void setRecvVideoFile(String recvVideoFile) {
		_recvVideoFile = recvVideoFile;
	}

	@Override
	public boolean isJavaxSoundDirectConversion() {
		return _javaxSoundDirectConversion;
	}

	/** @see #isJavaxSoundDirectConversion() */
	public void setJavaxSoundDirectConversion(boolean javaxSoundDirectConversion) {
		_javaxSoundDirectConversion = javaxSoundDirectConversion;
	}

	@Override
	public boolean isUseRat() {
		return _useRat;
	}

	/** @see #isUseRat() */
	public void setUseRat(boolean useRat) {
		_useRat = useRat;
	}

	@Override
	public boolean isUseVic() {
		return _useVic;
	}

	/** @see #isUseVic() */
	public void setUseVic(boolean useVic) {
		_useVic = useVic;
	}

	@Override
	public String getBinRat() {
		return _binRat;
	}

	/** @see #getBinRat() */
	public void setBinRat(String binRat) {
		_binRat = binRat;
	}

	@Override
	public String getBinVic() {
		return _binVic;
	}

	/** @see #getBinVic() */
	public void setBinVic(String binVic) {
		_binVic = binVic;
	}

	@Override
	public SocketAddress getAudioMcastSoAddr() {
		return _audioMcastSoAddr;
	}

	/** @see #getAudioMcastSoAddr() */
	public void setAudioMcastSoAddr(SocketAddress audioMcastSoAddr) {
		_audioMcastSoAddr = audioMcastSoAddr;
	}

	@Override
	public SocketAddress getVideoMcastSoAddr() {
		return _videoMcastSoAddr;
	}

	/** @see #getVideoMcastSoAddr() */
	public void setVideoMcastSoAddr(SocketAddress videoMcastSoAddr) {
		_videoMcastSoAddr = videoMcastSoAddr;
	}

	@Override
	public MediaDesc[] getMediaDescs() {
		return _mediaDescs;
	}

	/** @see #getMediaDescs() */
	public void setMediaDescs(MediaDesc[] mediaDescs) {
		_mediaDescs = mediaDescs;
	}

}
