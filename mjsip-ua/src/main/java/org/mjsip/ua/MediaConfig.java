/*
 * Copyright (c) 2023 Bernhard Haumacher et al. All Rights Reserved.
 */
package org.mjsip.ua;

import org.kohsuke.args4j.Option;
import org.mjsip.config.YesNoHandler;
import org.mjsip.media.MediaDesc;
import org.mjsip.sip.config.MediaDescHandler;
import org.mjsip.sip.config.SocketAddressHandler;
import org.zoolu.net.SocketAddress;

/**
 * Definition of media streams.
 */
public class MediaConfig implements MediaOptions {

	@Option(name = "--audio", handler = YesNoHandler.class)
	private boolean _audio=true;

	@Option(name = "--video", handler = YesNoHandler.class)
	private boolean _video=false;

	@Option(name = "--send-video-file", usage = "Video is streamed from the specified file.")
	private String _sendVideoFile=null;
	
	@Option(name = "--recv-video-file", usage = "Received video is recorded to the specified file.")
	private String _recvVideoFile=null;

	@Option(name = "--sound-conversion", handler = YesNoHandler.class)
	private boolean _javaxSoundDirectConversion=false;

	@Option(name = "--use-rat", handler = YesNoHandler.class)
	private boolean _useRat=false;
	
	@Option(name = "--use-vic", handler = YesNoHandler.class)
	private boolean _useVic=false;
	
	@Option(name = "--rat-cmd")
	private String _binRat="rat";
	
	@Option(name = "--vic-cmd")
	private String _binVic="vic";
	
	@Option(name = "--audio-mcast-addr", handler = SocketAddressHandler.class)
	private SocketAddress _audioMcastSoAddr=null;
	
	@Option(name = "--video-mcast-addr", handler = SocketAddressHandler.class)
	private SocketAddress _videoMcastSoAddr=null;

	@Option(name = "--media", handler = MediaDescHandler.class)
	private MediaDesc[] _mediaDescs=new MediaDesc[]{};
	
	@Option(name = "--random-early-drop")
	private int _randomEarlyDropRate=20;

	@Option(name = "--symmetric-rtp", handler = YesNoHandler.class)
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
