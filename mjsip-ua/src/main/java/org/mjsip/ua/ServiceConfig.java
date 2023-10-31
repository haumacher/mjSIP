/*
 * Copyright (c) 2023 Bernhard Haumacher et al. All Rights Reserved.
 */
package org.mjsip.ua;

import org.kohsuke.args4j.Option;

/**
 * Options for SIP a command-line tool or user interface.
 */
public class ServiceConfig implements ServiceOptions {

	@Option(name = "--hangup-time", usage = "Hang up after the given number of seconds (0 means manual hangup).")
	private int _hangupTime=-1;
	
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
