/*
 * Copyright (C) 2006 Luca Veltri - University of Parma - Italy
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

package org.mjsip.examples;



import org.kohsuke.args4j.Option;
import org.mjsip.config.OptionParser;
import org.mjsip.sip.message.SipMessage;
import org.mjsip.sip.provider.SipConfig;
import org.mjsip.sip.provider.SipProvider;
import org.mjsip.sip.provider.SipProviderListener;
import org.mjsip.time.ConfiguredScheduler;
import org.mjsip.time.SchedulerConfig;



/** DummyUAS is a very trivial UAS that replies to all incoming SIP requests
  * with a default response code. 
  */
public class DummyUAS implements SipProviderListener {
	
	/** Response code */
	int code;

	/** Response reason */
	String reason;

	private SipProvider sip_provider;


	/** Constructs a new DummyUAS. */
	public DummyUAS(SipProvider sipProvider, int code, String reason) {
		this.sip_provider = sipProvider;
		this.code=code;
		this.reason=reason;
	}


	// *********************** SipProvider callback ***********************

	/** When a new Message is received by the SipProvider. */
	@Override
	public void onReceivedMessage(SipProvider sip_provider, SipMessage msg) {
		if (msg.isRequest() && !msg.isAck()) {
			SipMessage resp=sip_provider.messageFactory().createResponse(msg,code,reason,null);
		sip_provider.sendMessage(resp);
		}
	}

	public static class Config {
		
		@Option(name = "-c", usage = "Response code.")
		int code = 403;
		
		@Option(name = "-r", usage = "Response reason.")
		String reason;
		
	}
	
	/** The main method. */
	public static void main(String[] args) {
		SipConfig sipConfig = new SipConfig();
		SchedulerConfig schedulerConfig = new SchedulerConfig();
		
		Config config = new Config();

		OptionParser.parseOptions(args, ".mjsip-ua", sipConfig, schedulerConfig, config);
		
		sipConfig.normalize();
		
		SipProvider sipProvider = new SipProvider(sipConfig, new ConfiguredScheduler(schedulerConfig));
		
		new DummyUAS(sipProvider, config.code, config.reason);
	}    
	
}
