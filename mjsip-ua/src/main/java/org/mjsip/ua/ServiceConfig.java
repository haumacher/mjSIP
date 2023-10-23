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
public class ServiceConfig extends Configure {

	/** Automatic hangup time (maximum call duartion) in seconds; time&lt;=0 means no automatic hangup. */
	public int hangupTime=-1;
	
	/** 
	 * Constructs a {@link UAConfig} from the given configuration file and program arguments.
	 */
	public static ServiceConfig init(String file, Flags flags) {
		ServiceConfig result=new ServiceConfig();
		result.loadFile(file);
		result.updateWith(flags);
		return result;
	}

	@Override
	public void setOption(String attribute, Parser par) {
		if (attribute.equals("hangup_time"))    {  hangupTime=par.getInt();  return;  } 
	}

	/**
	 * Adds settings read from command line arguments.
	 */
	protected void updateWith(Flags flags) {
		int hangup_time=flags.getInteger("-t","<secs>",-1,"auto hangups after given seconds (0 means manual hangup)");
		if (hangup_time>0) this.hangupTime=hangup_time;
	}		

}
