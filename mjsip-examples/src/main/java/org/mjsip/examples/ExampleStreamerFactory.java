/*
 * Copyright (c) 2023 Bernhard Haumacher et al. All Rights Reserved.
 */
package org.mjsip.examples;

import org.mjsip.media.FlowSpec.Direction;
import org.mjsip.media.StreamerOptions;
import org.mjsip.media.rx.AudioFileReceiver;
import org.mjsip.media.rx.AudioReceiver;
import org.mjsip.media.rx.JavaxAudioOutput;
import org.mjsip.media.tx.AudioFileTransmitter;
import org.mjsip.media.tx.AudioTransmitter;
import org.mjsip.media.tx.JavaxAudioInput;
import org.mjsip.media.tx.ToneTransmitter;
import org.mjsip.ua.StaticOptions;
import org.mjsip.ua.streamer.DefaultStreamerFactory;
import org.mjsip.ua.streamer.DispatchingStreamerFactory;
import org.mjsip.ua.streamer.LoopbackStreamerFactory;
import org.mjsip.ua.streamer.NativeStreamerFactory;
import org.mjsip.ua.streamer.StreamerFactory;

/**
 * Utility for crating a {@link StreamerFactory} for testing.
 */
public class ExampleStreamerFactory {

	/** 
	 *Creates a more or less configurable {@link StreamerFactory} for various test situations.
	 */
	public static StreamerFactory createStreamerFactory(ExampleMediaOptions mediaConfig, StaticOptions uaConfig) {
		if (mediaConfig.isLoopback()) {
			return new LoopbackStreamerFactory();
		} else {
			DispatchingStreamerFactory factory = new DispatchingStreamerFactory();
			if (mediaConfig.isAudio()) {
				if (mediaConfig.isUseRat()) {
					factory.addFactory("audio", new NativeStreamerFactory(mediaConfig.getAudioMcastSoAddr(), mediaConfig.getBinRat()));
				} else {
					Direction dir = uaConfig.getDirection();
		
					AudioTransmitter tx;
					if (dir.doSend()) {
						if (mediaConfig.isSendTone()) {
							tx=new ToneTransmitter();
						} else if (mediaConfig.getSendFile()!=null) {
							tx= new AudioFileTransmitter(mediaConfig.getSendFile());
						} else {
							tx = new JavaxAudioInput(true, mediaConfig.isJavaxSoundDirectConversion());
						}
					} else {
						tx = null;
					}
		
					// audio output
					String audio_out=null;
					if (mediaConfig.getRecvFile()!=null) audio_out=mediaConfig.getRecvFile();        
					
					AudioReceiver rx;
					if (dir.doReceive()) {
						if (audio_out == null) {
							rx = new JavaxAudioOutput(mediaConfig.isJavaxSoundDirectConversion());
						} else {
							rx = new AudioFileReceiver(audio_out);
						}
					} else {
						rx = null;
					}
		
					// standard javax-based audio streamer
					StreamerOptions options = StreamerOptions.builder()
							.setRandomEarlyDrop(mediaConfig.getRandomEarlyDropRate())
							.setSymmetricRtp(mediaConfig.isSymmetricRtp())
							.build();
					
					factory.addFactory("audio", new DefaultStreamerFactory(options, rx, tx));
				}
			}
			if (mediaConfig.isVideo()) {
				if (mediaConfig.isUseVic()) {
					factory.addFactory("video", new NativeStreamerFactory(mediaConfig.getVideoMcastSoAddr(), mediaConfig.getBinVic()));
				}
			}
			return factory;
		}
	}

}
