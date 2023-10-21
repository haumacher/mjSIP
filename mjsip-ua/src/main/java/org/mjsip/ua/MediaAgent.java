/*
 * Copyright (C) 2008 Luca Veltri - University of Parma - Italy
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



import java.util.Hashtable;

import org.mjsip.media.AudioStreamer;
import org.mjsip.media.FlowSpec;
import org.mjsip.media.FlowSpec.Direction;
import org.mjsip.media.LoopbackMediaStreamer;
import org.mjsip.media.MediaDesc;
import org.mjsip.media.MediaSpec;
import org.mjsip.media.MediaStreamer;
import org.mjsip.media.NativeMediaStreamer;
import org.mjsip.media.rx.AudioFileReceiver;
import org.mjsip.media.rx.AudioReceiver;
import org.mjsip.media.rx.JavaxAudioOutput;
import org.mjsip.media.tx.AudioFileTransmitter;
import org.mjsip.media.tx.AudioTransmitter;
import org.mjsip.media.tx.ToneTransmitter;
import org.slf4j.LoggerFactory;
import org.zoolu.sound.SimpleAudioSystem;
import org.zoolu.util.Archive;



/**
 * A {@link MediaAgent} is used to start and stop multimedia sessions (e.g. audio and/or video), by
 * means of {@link MediaStreamer}s.
 */
public class MediaAgent {
	
	private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(MediaAgent.class);
	
	/** Audio application */
	UAConfig uaConfig;

	/** Active media streamers, as table of: (String)media-->(MediaStreamer)media_streamer */
	Hashtable<String, MediaStreamer> media_streamers=new Hashtable<>();

	/** Creates a new MediaAgent. */
	public MediaAgent(UAConfig uaConfig) {
		this.uaConfig=uaConfig;
	}

	/** Starts a media session */
	public boolean startMediaSession(FlowSpec flow_spec, MediaConfig mediaConfig) {
		LOG.info("Starting media session: " + flow_spec.getMediaSpec());
		LOG.info("Flow: " + flow_spec.getLocalPort() + " " + flow_spec.getDirection().arrow() + " " + flow_spec.getRemoteAddress() + ":" + flow_spec.getRemotePort());
		
		initMedia(mediaConfig);
		String media=flow_spec.getMediaType();
		
		// stop previous media streamer (just in case something was wrong..)
		MediaStreamer existing = media_streamers.remove(media);
		if (existing != null) {
			existing.halt();
		}
		 
		// start new media streamer
		MediaStreamer media_streamer;
		if (uaConfig.loopback) {
			media_streamer = new LoopbackMediaStreamer(flow_spec);
		} else {
			if (media.equals("audio")) media_streamer=newAudioStreamer(flow_spec);
			else if (media.equals("video")) media_streamer=newVideoStreamer(flow_spec);
			else if (media.equals("ptt")) media_streamer=newPttStreamer(flow_spec);
			else {
				LOG.warn("No media streamer found for: " + media);
				return false;
			}
		}
		
		if (media_streamer.start()) {
			media_streamers.put(media, media_streamer);
			return true;
		} else {
			return false;
		}
	}

	private void initMedia(MediaConfig mediaConfig) {
		// ################# Patch to make audio working with javax.sound.. #################
		// Currently ExtendedAudioSystem must be initialized before any AudioClipPlayer is initialized.
		// This is caused by a problem with the definition of the audio format
		// BEGIN PATCH
		if (!uaConfig.useRat && !uaConfig.useJmfAudio) {
			float audio_sample_rate=SimpleAudioSystem.DEFAULT_AUDIO_FORMAT.getSampleRate();
			int channels=SimpleAudioSystem.DEFAULT_AUDIO_FORMAT.getChannels();
			for (int i=0; i<mediaConfig.mediaDescs.length; i++) {
				MediaDesc media_desc=mediaConfig.mediaDescs[i];
				if (media_desc.getMedia().equalsIgnoreCase("audio")) {
					MediaSpec ms=media_desc.getMediaSpecs()[0];
					audio_sample_rate=ms.getSampleRate();
					channels=ms.getChannels();
				}
			}
			if (uaConfig.audio && !uaConfig.loopback) {
				if (uaConfig.sendFile==null && !uaConfig.recvOnly && !uaConfig.sendTone)SimpleAudioSystem.initAudioInputLine(audio_sample_rate,channels);
				if (uaConfig.recvFile==null && !uaConfig.sendOnly) SimpleAudioSystem.initAudioOutputLine(audio_sample_rate,channels);
			}
		}
		// END PATCH
	}
 
	
	/** Stops a media session.  */
	public void stopMediaSession(String media) {
		LOG.info("stop("+media+")");

		if (media_streamers.containsKey(media)) {
			media_streamers.get(media).halt();
			media_streamers.remove(media);
		}
		else {
			LOG.warn("No running "+media+" streamer has been found.");
		}
	}


	// ********************** media streamers *********************

	/** Creates a new audio streamer. */
	private MediaStreamer newAudioStreamer(FlowSpec audio_flow) {
		
		MediaStreamer audio_streamer=null;
		
		if (uaConfig.useRat) {
			// use a native audio streamer (e.g. RAT)
			String remote_addr=(uaConfig.audioMcastSoAddr!=null)? uaConfig.audioMcastSoAddr.getAddress().toString() : audio_flow.getRemoteAddress();
			int remote_port=(uaConfig.audioMcastSoAddr!=null)? uaConfig.audioMcastSoAddr.getPort() : audio_flow.getRemotePort();
			int local_port=(uaConfig.audioMcastSoAddr!=null)? uaConfig.audioMcastSoAddr.getPort() : audio_flow.getLocalPort();
			String[] args=new String[]{(remote_addr+"/"+remote_port)};
			audio_streamer = new NativeMediaStreamer(uaConfig.binRat, args, local_port, remote_port);
		} else  if (uaConfig.useJmfAudio) {
			// use JMF audio streamer
			try {
				String audio_source=(uaConfig.sendFile!=null)? Archive.getFileURL(uaConfig.sendFile).toString() : null;
				if (uaConfig.recvFile!=null) LOG.warn("File destination is not supported with JMF audio");
				Class media_streamer_class=Class.forName("local.ext.media.jmf.JmfMediaStreamer");
				Class[] param_types={ FlowSpec.class, String.class};
				Object[] param_values={ audio_flow, audio_source};
				java.lang.reflect.Constructor media_streamer_constructor=media_streamer_class.getConstructor(param_types);
				audio_streamer=(MediaStreamer)media_streamer_constructor.newInstance(param_values);
			}
			catch (Exception e) {
				LOG.error("Error trying to create the JmfMediaApp", e);
			}
		}

		if (audio_streamer==null) {
			// use embedded javax-based audio streamer
		
			// javax-based audio streamer
			if (uaConfig.javaxSoundStreamer==null) {
				// audio input
				AudioTransmitter tx;
				if (uaConfig.sendTone) {
					tx=new ToneTransmitter();
				} else if (uaConfig.sendFile!=null) {
					tx= new AudioFileTransmitter(uaConfig.sendFile);
				} else {
					tx = null;
				}
				
				// audio output
				String audio_out=null;
				if (uaConfig.recvFile!=null) audio_out=uaConfig.recvFile;        

				AudioReceiver rx;
				Direction dir = audio_flow.getDirection();
				if (dir == Direction.RECV_ONLY || dir == Direction.FULL_DUPLEX) {
					if (audio_out == null) {
						rx = new JavaxAudioOutput(uaConfig.javaxSoundDirectConversion, uaConfig.randomEarlyDropRate);
					} else {
						rx = new AudioFileReceiver(audio_out);
					}
				} else {
					rx = null;
				}

				// standard javax-based audio streamer
				audio_streamer = new AudioStreamer(audio_flow, tx, rx,
						null, uaConfig.symmetricRtp);
			} else {
				// alternative audio streamer (just for experimental uses)
				try {
					Class media_streamer_class=Class.forName(uaConfig.javaxSoundStreamer);
					Class[] param_types = { FlowSpec.class };
					Object[] param_values = { audio_flow };
					java.lang.reflect.Constructor media_streamer_constructor=media_streamer_class.getConstructor(param_types);
					audio_streamer=(MediaStreamer)media_streamer_constructor.newInstance(param_values);
				} catch (Exception e) {
					LOG.error("Error trying to create audio streamer '"+uaConfig.javaxSoundStreamer+"'", e);
				}
			}
		}
		return audio_streamer;
	}


	/** Creates a new video streamer. */
	private MediaStreamer newVideoStreamer(FlowSpec video_flow) {
		
		MediaStreamer video_streamer=null;

		if (uaConfig.useVic) {
			// use a native audio streamer (e.g. VIC)
			String remote_addr=(uaConfig.videoMcastSoAddr!=null)? uaConfig.videoMcastSoAddr.getAddress().toString() : video_flow.getRemoteAddress();
			int remote_port=(uaConfig.videoMcastSoAddr!=null)? uaConfig.videoMcastSoAddr.getPort() : video_flow.getRemotePort();
			int local_port=(uaConfig.videoMcastSoAddr!=null)? uaConfig.videoMcastSoAddr.getPort() : video_flow.getLocalPort();
			String[] args=new String[]{(remote_addr+"/"+remote_port)};
			video_streamer = new NativeMediaStreamer(uaConfig.binVic, args, local_port, remote_port);
		}
		else 
		if (uaConfig.useJmfVideo) {
			// use JMF video streamer
			try {
				String video_source=(uaConfig.sendVideoFile!=null)? Archive.getFileURL(uaConfig.sendVideoFile).toString() : null;
				if (uaConfig.recvVideoFile!=null) LOG.warn("File destination is not supported with JMF video");
				Class media_streamer_class=Class.forName("local.ext.media.jmf.JmfMediaApp");
				Class[] param_types = { FlowSpec.class, String.class };
				Object[] param_values = { video_flow, video_source };
				java.lang.reflect.Constructor media_streamer_constructor=media_streamer_class.getConstructor(param_types);
				video_streamer=(MediaStreamer)media_streamer_constructor.newInstance(param_values);
			}
			catch (Exception e) {
				LOG.error("Error trying to create the JmfMediaApp", e);
			}
		}
		return video_streamer;
	}


	/** Creates a new ptt streamer. */
	private MediaStreamer newPttStreamer(FlowSpec flow_spec) {
		
		MediaStreamer ptt_streamer=null;
		try {
			Class media_streamer_class=Class.forName("local.ext.media.push2talk.Push2TalkApp");
			Class[] param_types = { FlowSpec.class };
			Object[] param_values = { flow_spec };
			java.lang.reflect.Constructor media_streamer_constructor=media_streamer_class.getConstructor(param_types);
			ptt_streamer=(MediaStreamer)media_streamer_constructor.newInstance(param_values);
		}
		catch (Exception e) {
			LOG.error("Error trying to create the Push2TalkApp", e);
		}
		return ptt_streamer;
	}

}
