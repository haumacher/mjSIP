/*
 * Copyright (c) 2023 Bernhard Haumacher et al. All Rights Reserved.
 */
package org.mjsip.ua;

import org.zoolu.util.Configure;
import org.zoolu.util.Flags;
import org.zoolu.util.Parser;

/**
 * Options for SIP a command-line tool or user interface.
 */
public class ServiceConfig extends Configure implements ServiceOptions {

	private int _hangupTime=-1;
	
	/** 
	 * Constructs a {@link UAConfig} from the given configuration file and program arguments.
	 */
	public static ServiceOptions init(String file, Flags flags) {
		ServiceConfig result=new ServiceConfig();
		result.loadFile(file);
		result.updateWith(flags);
		return result;
	}

	@Override
	public void setOption(String attribute, Parser par) {
		if (attribute.equals("hangup_time"))    {  setHangupTime(par.getInt());  return;  } 
	}

	/**
	 * Adds settings read from command line arguments.
	 */
	protected void updateWith(Flags flags) {
		int hangup_time=flags.getInteger("-t","<secs>",-1,"auto hangups after given seconds (0 means manual hangup)");
		if (hangup_time>0) this.setHangupTime(hangup_time);
	}

	@Override
	public int getHangupTime() {
		return _hangupTime;
	}

	/**
	 * @see #getHangupTime()
	 */
	public void setHangupTime(int hangupTime) {
		_hangupTime = hangupTime;
	}		

}
