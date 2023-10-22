/*
 * Copyright (C) 2007 Luca Veltri - University of Parma - Italy
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

package org.mjsip.ua;



import java.io.File;

import org.mjsip.sip.address.NameAddress;
import org.mjsip.sip.provider.SipConfig;
import org.mjsip.sip.provider.SipProvider;
import org.mjsip.sip.provider.SipStack;
import org.mjsip.time.Scheduler;
import org.mjsip.time.SchedulerConfig;
import org.mjsip.ua.cli.UserAgentCli;
import org.slf4j.LoggerFactory;
import org.zoolu.util.Flags;



/** Jukebox is a simple audio server.
  * It automatically responds to incoming calls and sends the audio file
  * as selected by the caller through the request-line parameter 'audiofile'.
  * <p>
  * Note that it is sigle-call UA, that is it serves ONE call at a time.
  */
public class MiniJukebox extends UserAgentCli {
	
	private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(MiniJukebox.class);

	/** URI resource parameter */
	public static String PARAM_RESOURCE="resource";
	
	/** Maximum life time (call duration) in seconds */
	public static int MAX_LIFE_TIME=600;

	/** 
	 * Creates a new MiniJukebox. 
	 */
	public MiniJukebox(SipProvider sip_provider, UAConfig uaConfig, UIConfig uiConfig, MediaConfig mediaConfig) {
		super(sip_provider,uaConfig, uiConfig, mediaConfig);
	}

	/** From UserAgentListener. When a new call is incoming */
	public void onUaIncomingCall(UserAgent ua, NameAddress callee, NameAddress caller) {
		LOG.info("incoming call from "+caller.toString());
		String audio_file=callee.getAddress().getParameter(PARAM_RESOURCE);
		if (audio_file!=null) {
			if (new File(audio_file).isFile()) {
				_uaConfig.sendFile=audio_file;
			}
		}
		if (_uaConfig.sendFile!=null) ua.accept(_mediaConfig.mediaDescs);      
		else ua.hangup();
	}
	
	/** The main method. */
	public static void main(String[] args) {
		System.out.println("MiniJukebox"+SipStack.version);
		Flags flags=new Flags("MiniJukebox", args);
		String config_file=flags.getString("-f","<file>", System.getProperty("user.home") + "/.mjsip-ua" ,"loads configuration from the given file");
		SipConfig sipConfig = SipConfig.init(config_file, flags);
		UAConfig uaConfig = UAConfig.init(config_file, flags);
		SchedulerConfig schedulerConfig = SchedulerConfig.init(config_file);
		MediaConfig mediaConfig = MediaConfig.init(config_file, flags, uaConfig);
		UIConfig uiConfig=UIConfig.init(config_file, flags);         
		flags.close();

		uaConfig.audio=true;
		uaConfig.video=false;
		uaConfig.sendOnly=true;
		if (uaConfig.hangupTime<=0) uaConfig.hangupTime=MAX_LIFE_TIME;
		new MiniJukebox(new SipProvider(sipConfig, new Scheduler(schedulerConfig)),uaConfig, uiConfig, mediaConfig);
	}    
	
}
