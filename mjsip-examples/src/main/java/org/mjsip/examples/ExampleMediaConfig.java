/*
 * Copyright (c) 2023 Bernhard Haumacher et al. All Rights Reserved.
 */
package org.mjsip.examples;

import org.kohsuke.args4j.Option;
import org.mjsip.config.YesNoHandler;
import org.mjsip.ua.MediaConfig;
import org.zoolu.util.Configure;

/**
 * {@link MediaConfig} for test situations.
 */
public class ExampleMediaConfig extends MediaConfig implements ExampleMediaOptions {

	@Option(name = "--loopback", usage = "Loopback mode, received media are sent back to the remote sender.", handler = YesNoHandler.class)
	private boolean _loopback=false;
	
	@Option(name = "--send-tone", usage = "Send only mode, an audio test tone is generated.", handler = YesNoHandler.class)
	private boolean _sendTone=false;
	
	@Option(name = "--send-file", usage = "Audio is played from the specified file.")
	private String _sendFile=null;
	
	@Option(name = "--recv-file", usage = "Received audio is recorded to the specified file.")
	private String _recvFile=null;

	@Override
	public boolean isLoopback() {
		return _loopback;
	}

	/** @see #isLoopback() */
	public void setLoopback(boolean loopback) {
		_loopback = loopback;
	}

	@Override
	public boolean isSendTone() {
		return _sendTone;
	}

	/** @see #isSendTone() */
	public void setSendTone(boolean sendTone) {
		_sendTone = sendTone;
	}

	@Override
	public String getSendFile() {
		return _sendFile;
	}

	/** @see #getSendFile() */
	public void setSendFile(String sendFile) {
		_sendFile = sendFile;
	}

	@Override
	public String getRecvFile() {
		return _recvFile;
	}

	/** @see #getRecvFile() */
	public void setRecvFile(String recvFile) {
		_recvFile = recvFile;
	}

	public void normalize() {
		if (getSendFile()!=null && getSendFile().equalsIgnoreCase(Configure.NONE)) setSendFile(null);
		if (getRecvFile()!=null && getRecvFile().equalsIgnoreCase(Configure.NONE)) setRecvFile(null);

		// use audio as default media in case of..
		if ((isSendTone() || getSendFile()!=null || getRecvFile()!=null) && !isVideo()) setAudio(true);
	}
}
