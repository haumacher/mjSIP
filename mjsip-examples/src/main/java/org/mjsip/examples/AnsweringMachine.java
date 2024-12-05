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
import java.io.IOException;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

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
import org.mjsip.ua.MediaConfig;
import org.mjsip.ua.RegisteringMultipleUAS;
import org.mjsip.ua.ServiceConfig;
import org.mjsip.ua.ServiceOptions;
import org.mjsip.ua.UAConfig;
import org.mjsip.ua.UAOptions;
import org.mjsip.ua.UserAgent;
import org.mjsip.ua.UserAgentListener;
import org.mjsip.ua.UserAgentListenerAdapter;
import org.mjsip.ua.streamer.StreamerFactory;
import org.slf4j.LoggerFactory;

/**
 * {@link AnsweringMachine} is a VOIP server that automatically accepts incoming calls, sends an
 * audio file and records input received from the remote end.
 */
public class AnsweringMachine extends RegisteringMultipleUAS {

	private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(AnsweringMachine.class);

	/** Media file to play when answering the call. */
	public static String DEFAULT_ANNOUNCEMENT_FILE = "./announcement-8000hz-mono-a-law.wav";

	private final MediaConfig _mediaConfig;

	private final StreamerFactory _streamerFactory;

	private final PortPool _portPool;


	/**
	 * Creates an {@link AnsweringMachine}.
	 * @param streamerFactory 
	 */
	public AnsweringMachine(SipProvider sip_provider, UAOptions uaConfig,
			MediaConfig mediaConfig, StreamerFactory streamerFactory, PortPool portPool, ServiceOptions serviceConfig) {
		super(sip_provider, portPool, uaConfig, serviceConfig);
		_mediaConfig = mediaConfig;
		_streamerFactory = streamerFactory;
		_portPool = portPool;
	}

	@Override
	protected UserAgentListener createCallHandler(SipMessage msg) {
		return new UserAgentListenerAdapter() {
			@Override
			public void onUaIncomingCall(UserAgent ua, NameAddress callee, NameAddress caller,
					MediaDesc[] media_descs) {
				LOG.info("Incomming call from: {}", callee.getAddress());
				ua.accept(new MediaAgent(_mediaConfig.getMediaDescs(), _streamerFactory));
			}
		};
	}

	/**
	 * The main entry point.
	 */
	public static void main(String[] args) throws UnsupportedAudioFileException, IOException {
		LOG.info("{} {}", AnsweringMachine.class.getSimpleName(), SipStack.version);

		SipConfig sipConfig = new SipConfig();
		UAConfig uaConfig = new UAConfig();
		SchedulerConfig schedulerConfig = new SchedulerConfig();
		ExampleMediaConfig mediaConfig = new ExampleMediaConfig();
		PortConfig portConfig = new PortConfig();
		ServiceConfig serviceConfig = new ServiceConfig();

		OptionParser.parseOptions(args, ".mjsip-ua", sipConfig, uaConfig, schedulerConfig, mediaConfig, portConfig, serviceConfig);
		
		sipConfig.normalize();
		uaConfig.normalize(sipConfig);
		mediaConfig.normalize();

		if (mediaConfig.getSendFile() != null) {
			AudioFileFormat audioFormat = AudioSystem.getAudioFileFormat(new File(mediaConfig.getSendFile()));
			LOG.info("Announcement file format: {}", audioFormat);
		}

		StreamerFactory streamerFactory = ExampleStreamerFactory.createStreamerFactory(mediaConfig, uaConfig);
		SipProvider sipProvider = new SipProvider(sipConfig, new ConfiguredScheduler(schedulerConfig));
		new AnsweringMachine(sipProvider, uaConfig, mediaConfig, streamerFactory, portConfig.createPool(), serviceConfig);
	}

}
