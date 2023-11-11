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

import org.kohsuke.args4j.Option;
import org.mjsip.config.OptionParser;
import org.mjsip.media.MediaDesc;
import org.mjsip.pool.PortConfig;
import org.mjsip.pool.PortPool;
import org.mjsip.sip.address.NameAddress;
import org.mjsip.sip.message.SipMessage;
import org.mjsip.sip.provider.SipConfig;
import org.mjsip.sip.provider.SipProvider;
import org.mjsip.sip.provider.SipStack;
import org.mjsip.time.ConfiguredScheduler;
import org.mjsip.time.SchedulerConfig;
import org.mjsip.ua.MediaAgent;
import org.mjsip.ua.RegisteringMultipleUAS;
import org.mjsip.ua.ServiceConfig;
import org.mjsip.ua.ServiceOptions;
import org.mjsip.ua.UAConfig;
import org.mjsip.ua.UserAgent;
import org.mjsip.ua.UserAgentListener;
import org.mjsip.ua.UserAgentListenerAdapter;
import org.mjsip.ua.streamer.StreamerFactory;

/** Jukebox is a simple audio server.
  * It automatically responds to incoming calls and sends the audio file
  * as selected by the caller through the request-line parameter 'audiofile'.
  */
public class Jukebox extends RegisteringMultipleUAS {

	/** URI resource parameter */
	public static String PARAM_RESOURCE="resource";
	
	/** Maximum life time (call duration) in seconds */
	public static int MAX_LIFE_TIME=600;

	private ExampleMediaConfig _mediaConfig;

	private final String _mediaPath;

	/** 
	 * Creates a {@link Jukebox}. 
	 * @param mediaPath 
	 */
	public Jukebox(SipProvider sip_provider, UAConfig uaConfig,
			ExampleMediaConfig mediaConfig, PortPool portPool, ServiceOptions serviceConfig, String mediaPath) {
		super(sip_provider,portPool, uaConfig, serviceConfig);
		_mediaConfig = mediaConfig;
		_mediaPath = mediaPath;
	}
	
	@Override
	protected UserAgentListener createCallHandler(SipMessage msg) {
		return new UserAgentListenerAdapter() {
			/** From UserAgentListener. When a new call is incoming. */
			@Override
			public void onUaIncomingCall(UserAgent ua, NameAddress callee, NameAddress caller, MediaDesc[] media_descs) {
				String audio_file=_mediaPath+"/"+callee.getAddress().getParameter(PARAM_RESOURCE);
				if (new File(audio_file).isFile()) {
					_mediaConfig.setSendFile(audio_file);
					StreamerFactory streamerFactory = ExampleStreamerFactory.createStreamerFactory(_mediaConfig, _config);					
					ua.accept(new MediaAgent(_mediaConfig.getMediaDescs(), streamerFactory));
				} else {
					ua.hangup();
				}
			}
		};
	}
	
	public static class Config {
		
		@Option(name = "--media-path")
		String mediaPath;
		
		@Option(name = "--prompt")
		boolean prompt;
		
	}

	/** The main method. */
	public static void main(String[] args) {
		System.out.println("Jukebox "+SipStack.version);

		SipConfig sipConfig = new SipConfig();
		UAConfig uaConfig = new UAConfig();
		SchedulerConfig schedulerConfig = new SchedulerConfig();
		ExampleMediaConfig mediaConfig = new ExampleMediaConfig();
		PortConfig portConfig = new PortConfig();
		ServiceConfig serviceConfig = new ServiceConfig();
		
		Config config = new Config();

		OptionParser.parseOptions(args, ".mjsip-ua", sipConfig, uaConfig, schedulerConfig, mediaConfig, portConfig, serviceConfig, config);
		
		sipConfig.normalize();
		uaConfig.normalize(sipConfig);
		mediaConfig.normalize();

		mediaConfig.setAudio(true);
		mediaConfig.setVideo(false);
		uaConfig.setSendOnly(true);
		new Jukebox(new SipProvider(sipConfig, new ConfiguredScheduler(schedulerConfig)),uaConfig, mediaConfig, portConfig.createPool(), serviceConfig, config.mediaPath);
		
		// Prompt before exit
		if (config.prompt) {
			try {
				System.out.println("press 'enter' to exit");
				(new java.io.BufferedReader(new java.io.InputStreamReader(System.in))).readLine();
				System.exit(0);
			}
			catch (Exception e) {}
		}
	}    

}
