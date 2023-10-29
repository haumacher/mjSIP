/*
 * Copyright (c) 2023 Bernhard Haumacher et al. All Rights Reserved.
 */
package org.mjsip.examples;

import org.mjsip.ua.MediaConfig;
import org.mjsip.ua.UAConfig;
import org.zoolu.util.Configure;
import org.zoolu.util.Flags;
import org.zoolu.util.Parser;

/**
 * {@link MediaConfig} for test situations.
 */
public class ExampleMediaConfig extends MediaConfig implements ExampleMediaOptions {

	/** 
	 * Constructs a {@link UAConfig} from the given configuration file and program arguments.
	 */
	public static ExampleMediaConfig init(String file, Flags flags) {
		ExampleMediaConfig result=new ExampleMediaConfig();
		result.loadFile(file);
		result.initFrom(flags);
		result.normalize();
		return result;
	}

	private boolean _loopback=false;
	private boolean _sendTone=false;
	private String _sendFile=null;
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

	@Override
	public void setOption(String attribute, Parser par) {
		super.setOption(attribute, par);
		
		if (attribute.equals("loopback"))       {  setLoopback((par.getString().toLowerCase().startsWith("y")));  return;  }
		if (attribute.equals("send_tone"))      {  setSendTone((par.getString().toLowerCase().startsWith("y")));  return;  }
		if (attribute.equals("send_file"))      {  setSendFile(par.getRemainingString().trim());  return;  }
		if (attribute.equals("recv_file"))      {  setRecvFile(par.getRemainingString().trim());  return;  }
	}
	
	@Override
	public void initFrom(Flags flags) {
		super.initFrom(flags);
		
		Boolean loopback=flags.getBoolean("--loopback",null,"loopback mode, received media are sent back to the remote sender");
		if (loopback!=null) this.setLoopback(loopback.booleanValue());
		
		Boolean send_tone=flags.getBoolean("--send-tone",null,"send only mode, an audio test tone is generated");
		if (send_tone!=null) this.setSendTone(send_tone.booleanValue());
		
		String  send_file=flags.getString("--send-file","<file>",null,"audio is played from the specified file");
		if (send_file!=null) this.setSendFile(send_file);
		
		String  recv_file=flags.getString("--recv-file","<file>",null,"audio is recorded to the specified file");
		if (recv_file!=null) this.setRecvFile(recv_file);
	}
	
	@Override
	protected void normalize() {
		if (getSendFile()!=null && getSendFile().equalsIgnoreCase(Configure.NONE)) setSendFile(null);
		if (getRecvFile()!=null && getRecvFile().equalsIgnoreCase(Configure.NONE)) setRecvFile(null);

		// use audio as default media in case of..
		if ((isSendTone() || getSendFile()!=null || getRecvFile()!=null) && !isVideo()) setAudio(true);

		super.normalize();
	}
}
