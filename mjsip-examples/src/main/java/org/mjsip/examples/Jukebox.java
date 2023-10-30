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
package org.mjsip.examples;

import java.io.File;

import org.mjsip.media.MediaDesc;
import org.mjsip.sip.address.NameAddress;
import org.mjsip.sip.message.SipMessage;
import org.mjsip.sip.provider.SipConfig;
import org.mjsip.sip.provider.SipOptions;
import org.mjsip.sip.provider.SipProvider;
import org.mjsip.sip.provider.SipStack;
import org.mjsip.time.Scheduler;
import org.mjsip.time.SchedulerConfig;
import org.mjsip.ua.MediaAgent;
import org.mjsip.ua.MultipleUAS;
import org.mjsip.ua.ServiceConfig;
import org.mjsip.ua.ServiceOptions;
import org.mjsip.ua.UAConfig;
import org.mjsip.ua.UserAgent;
import org.mjsip.ua.UserAgentListener;
import org.mjsip.ua.UserAgentListenerAdapter;
import org.mjsip.ua.pool.PortConfig;
import org.mjsip.ua.pool.PortPool;
import org.mjsip.ua.streamer.StreamerFactory;
import org.zoolu.util.Flags;

/** Jukebox is a simple audio server.
  * It automatically responds to incoming calls and sends the audio file
  * as selected by the caller through the request-line parameter 'audiofile'.
  */
public class Jukebox extends MultipleUAS {

	/** URI resource parameter */
	public static String PARAM_RESOURCE="resource";
	
	/** Maximum life time (call duration) in seconds */
	public static int MAX_LIFE_TIME=600;

	/** Media file path */
	public static String MEDIA_PATH=".";

	private ExampleMediaConfig _mediaConfig;

	/** 
	 * Creates a {@link Jukebox}. 
	 */
	public Jukebox(SipProvider sip_provider, UAConfig uaConfig,
			ExampleMediaConfig mediaConfig, PortPool portPool, ServiceOptions serviceConfig) {
		super(sip_provider,portPool, uaConfig, uaConfig, serviceConfig);
		_mediaConfig = mediaConfig;
	}
	
	@Override
	protected UserAgentListener createCallHandler(SipMessage msg) {
		return new UserAgentListenerAdapter() {
			/** From UserAgentListener. When a new call is incoming. */
			@Override
			public void onUaIncomingCall(UserAgent ua, NameAddress callee, NameAddress caller, MediaDesc[] media_descs) {
				String audio_file=MEDIA_PATH+"/"+callee.getAddress().getParameter(PARAM_RESOURCE);
				if (new File(audio_file).isFile()) {
					_mediaConfig.setSendFile(audio_file);
					StreamerFactory streamerFactory = ExampleStreamerFactory.createStreamerFactory(_mediaConfig, _uaConfig);					
					ua.accept(new MediaAgent(_mediaConfig.getMediaDescs(), streamerFactory));
				} else {
					ua.hangup();
				}
			}
		};
	}

	/** The main method. */
	public static void main(String[] args) {
		System.out.println("Jukebox "+SipStack.version);

		boolean prompt_exit=false;

		for (int i=0; i<args.length; i++) {
			if (args[i].equals("--mpath")) {
				MEDIA_PATH=args[i+1];
				args[i]="--skip";
				args[++i]="--skip";
			}
			else
			if (args[i].equals("--prompt")) {
				prompt_exit=true;
				args[i]="--skip";
			}
		}
		Flags flags=new Flags("Jukebox", args);
		String config_file=flags.getString("-f","<file>", System.getProperty("user.home") + "/.mjsip-ua" ,"loads configuration from the given file");
		SipOptions sipConfig = SipConfig.init(config_file, flags);
		UAConfig uaConfig = UAConfig.init(config_file, flags, sipConfig);
		SchedulerConfig schedulerConfig = SchedulerConfig.init(config_file);
		ExampleMediaConfig mediaConfig = ExampleMediaConfig.init(config_file, flags);
		PortConfig portConfig = PortConfig.init(config_file, flags);
		ServiceOptions serviceConfig=ServiceConfig.init(config_file, flags);         
		flags.close();
		
		mediaConfig.setAudio(true);
		mediaConfig.setVideo(false);
		uaConfig.setSendOnly(true);
		new Jukebox(new SipProvider(sipConfig, new Scheduler(schedulerConfig)),uaConfig, mediaConfig, portConfig.createPool(), serviceConfig);
		
		// promt before exit
		if (prompt_exit) 
		try {
			System.out.println("press 'enter' to exit");
			(new java.io.BufferedReader(new java.io.InputStreamReader(System.in))).readLine();
			System.exit(0);
		}
		catch (Exception e) {}
	}    

}
