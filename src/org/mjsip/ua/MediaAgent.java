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
import org.mjsip.media.LoopbackMediaStreamer;
import org.mjsip.media.MediaDesc;
import org.mjsip.media.MediaSpec;
import org.mjsip.media.MediaStreamer;
import org.mjsip.media.NativeMediaStreamer;
import org.zoolu.sound.SimpleAudioSystem;
import org.zoolu.util.Archive;
import org.zoolu.util.ExceptionPrinter;
import org.zoolu.util.LogLevel;
import org.zoolu.util.Logger;



/** Media agent.
  * A media agent is used to start and stop multimedia sessions
  * (e.g. audio and/or video), by means of proper media streamers.
  */
public class MediaAgent {
	
	/** Logger */
	Logger logger;
	
	/** Audio application */
	UserAgentProfile ua_profile;

	/** Active media streamers, as table of: (String)media-->(MediaStreamer)media_streamer */
	Hashtable media_streamers=new Hashtable();



	/** Creates a new MediaAgent. */
	public MediaAgent(UserAgentProfile ua_profile, Logger logger) {
		this.ua_profile=ua_profile;
		this.logger=logger;

		// ################# Patch to make audio working with javax.sound.. #################
		// Currently ExtendedAudioSystem must be initialized before any AudioClipPlayer is initialized.
		// This is caused by a problem with the definition of the audio format
		// BEGIN PATCH
		if (!ua_profile.use_rat && !ua_profile.use_jmf_audio) {
			float audio_sample_rate=SimpleAudioSystem.DEFAULT_AUDIO_FORMAT.getSampleRate();
			int channels=SimpleAudioSystem.DEFAULT_AUDIO_FORMAT.getChannels();
			for (int i=0; i<ua_profile.media_descs.length; i++) {
				MediaDesc media_desc=ua_profile.media_descs[i];
				if (media_desc.getMedia().equalsIgnoreCase("audio")) {
					MediaSpec ms=(MediaSpec)media_desc.getMediaSpecs()[0];
					audio_sample_rate=ms.getSampleRate();
					channels=ms.getChannels();
				}
			}
			if (ua_profile.audio && !ua_profile.loopback) {
				if (ua_profile.send_file==null && !ua_profile.recv_only && !ua_profile.send_tone)SimpleAudioSystem.initAudioInputLine(audio_sample_rate,channels);
				if (ua_profile.recv_file==null && !ua_profile.send_only) SimpleAudioSystem.initAudioOutputLine(audio_sample_rate,channels);
			}
		}
		// END PATCH
	}

	
	/** Starts a media session */
	public boolean startMediaSession(FlowSpec flow_spec) {
		log("start("+flow_spec.getMediaSpec()+")");
		log("new flow: "+flow_spec.getLocalPort()+((flow_spec.getDirection()==FlowSpec.SEND_ONLY)? "=-->" : ((flow_spec.getDirection()==FlowSpec.RECV_ONLY)? "<--=" : "<-->" ))+flow_spec.getRemoteAddress()+":"+flow_spec.getRemotePort());

		String media=flow_spec.getMediaSpec().getType();
		
		// stop previous media streamer (just in case something was wrong..)
		if (media_streamers.containsKey(media)) {
			((MediaStreamer)media_streamers.get(media)).halt();
			media_streamers.remove(media);
		}
		 
		// start new media streamer
		MediaStreamer media_streamer=null;

		if (ua_profile.loopback) media_streamer=new LoopbackMediaStreamer(flow_spec,logger);
		else
		if (flow_spec.getMediaSpec().getType().equals("audio")) media_streamer=newAudioStreamer(flow_spec);
		else
		if (flow_spec.getMediaSpec().getType().equals("video")) media_streamer=newVideoStreamer(flow_spec);
		else
		if (flow_spec.getMediaSpec().getType().equals("ptt")) media_streamer=newPttStreamer(flow_spec);

		if (media_streamer!=null) {
			if (media_streamer.start()) {
				media_streamers.put(media,media_streamer);
				return true;
			}
			else return false;
		}
		else {
			log(LogLevel.WARNING,"No "+media+" streamer has been found: "+media+" not started");
			return false;
		}
	}
 
	
	/** Stops a media session.  */
	public void stopMediaSession(String media) {
		log("stop("+media+")");

		if (media_streamers.containsKey(media)) {
			((MediaStreamer)media_streamers.get(media)).halt();
			media_streamers.remove(media);
		}
		else {
			log(LogLevel.WARNING,"No running "+media+" streamer has been found.");
		}
	}


	// ********************** media streamers *********************

	/** Creates a new audio streamer. */
	private MediaStreamer newAudioStreamer(FlowSpec audio_flow) {
		
		MediaStreamer audio_streamer=null;
		
		if (ua_profile.use_rat) {
			// use a native audio streamer (e.g. RAT)
			//if (ua_profile.audio_mcast_soaddr!=null) audio_flow=new FlowSpec(audio_flow.getMediaSpec(),ua_profile.audio_mcast_soaddr.getPort(),ua_profile.audio_mcast_soaddr.getAddress().toString(),ua_profile.audio_mcast_soaddr.getPort(),audio_flow.getDirection());
			String remote_addr=(ua_profile.audio_mcast_soaddr!=null)? ua_profile.audio_mcast_soaddr.getAddress().toString() : audio_flow.getRemoteAddress();
			int remote_port=(ua_profile.audio_mcast_soaddr!=null)? ua_profile.audio_mcast_soaddr.getPort() : audio_flow.getRemotePort();
			int local_port=(ua_profile.audio_mcast_soaddr!=null)? ua_profile.audio_mcast_soaddr.getPort() : audio_flow.getLocalPort();
			String[] args=new String[]{(remote_addr+"/"+remote_port)};
			audio_streamer=new NativeMediaStreamer(ua_profile.bin_rat,args,local_port,remote_port,logger);
		}
		else 
		if (ua_profile.use_jmf_audio) {
			// use JMF audio streamer
			try {
				String audio_source=(ua_profile.send_file!=null)? Archive.getFileURL(ua_profile.send_file).toString() : null;
				if (ua_profile.recv_file!=null) log(LogLevel.WARNING,"File destination is not supported with JMF audio");
				Class media_streamer_class=Class.forName("local.ext.media.jmf.JmfMediaStreamer");
				Class[] param_types={ FlowSpec.class, String.class, Logger.class };
				Object[] param_values={ audio_flow, audio_source, logger };
				java.lang.reflect.Constructor media_streamer_constructor=media_streamer_class.getConstructor(param_types);
				audio_streamer=(MediaStreamer)media_streamer_constructor.newInstance(param_values);
			}
			catch (Exception e) {
				log(LogLevel.WARNING,e);
				log(LogLevel.SEVERE,"Error trying to create the JmfMediaApp");
			}
		}
		// else
		if (audio_streamer==null) {
			// use embedded javax-based audio streamer
		
			// audio input
			String audio_in=null;
			if (ua_profile.send_tone) audio_in=AudioStreamer.TONE;
			else
			if (ua_profile.send_file!=null) audio_in=ua_profile.send_file;
			// audio output
			String audio_out=null;
			if (ua_profile.recv_file!=null) audio_out=ua_profile.recv_file;        

			// javax-based audio streamer
			if (ua_profile.javax_sound_streamer==null) {
				// standard javax-based audio streamer
				audio_streamer=new AudioStreamer(audio_flow,audio_in,audio_out,ua_profile.javax_sound_direct_convertion,null,ua_profile.javax_sound_sync,ua_profile.random_early_drop_rate,ua_profile.symmetric_rtp,logger);
			}
			else {
				// alternative audio streamer (just for experimental uses)
				try {
					Class media_streamer_class=Class.forName(ua_profile.javax_sound_streamer);
					Class[] param_types={ FlowSpec.class, Logger.class };
					Object[] param_values={ audio_flow, logger };
					java.lang.reflect.Constructor media_streamer_constructor=media_streamer_class.getConstructor(param_types);
					audio_streamer=(MediaStreamer)media_streamer_constructor.newInstance(param_values);
				}
				catch (Exception e) {
					log(LogLevel.WARNING,e);
					log(LogLevel.SEVERE,"Error trying to create audio streamer '"+ua_profile.javax_sound_streamer+"'");
				}
			}
		}
		return audio_streamer;
	}


	/** Creates a new video streamer. */
	private MediaStreamer newVideoStreamer(FlowSpec video_flow) {
		
		MediaStreamer video_streamer=null;

		if (ua_profile.use_vic) {
			// use a native audio streamer (e.g. VIC)
			//if (ua_profile.video_mcast_soaddr!=null) video_flow=new FlowSpec(video_flow.getMediaSpec(),ua_profile.video_mcast_soaddr.getPort(),ua_profile.video_mcast_soaddr.getAddress().toString(),ua_profile.video_mcast_soaddr.getPort(),video_flow.getDirection());
			String remote_addr=(ua_profile.video_mcast_soaddr!=null)? ua_profile.video_mcast_soaddr.getAddress().toString() : video_flow.getRemoteAddress();
			int remote_port=(ua_profile.video_mcast_soaddr!=null)? ua_profile.video_mcast_soaddr.getPort() : video_flow.getRemotePort();
			int local_port=(ua_profile.video_mcast_soaddr!=null)? ua_profile.video_mcast_soaddr.getPort() : video_flow.getLocalPort();
			String[] args=new String[]{(remote_addr+"/"+remote_port)};
			video_streamer=new NativeMediaStreamer(ua_profile.bin_vic,args,local_port,remote_port,logger);
		}
		else 
		if (ua_profile.use_jmf_video) {
			// use JMF video streamer
			try {
				String video_source=(ua_profile.send_video_file!=null)? Archive.getFileURL(ua_profile.send_video_file).toString() : null;
				if (ua_profile.recv_video_file!=null) log(LogLevel.WARNING,"File destination is not supported with JMF video");
				Class media_streamer_class=Class.forName("local.ext.media.jmf.JmfMediaApp");
				Class[] param_types={ FlowSpec.class, String.class, Logger.class };
				Object[] param_values={ video_flow, video_source, logger };
				java.lang.reflect.Constructor media_streamer_constructor=media_streamer_class.getConstructor(param_types);
				video_streamer=(MediaStreamer)media_streamer_constructor.newInstance(param_values);
			}
			catch (Exception e) {
				log(LogLevel.WARNING,e);
				log(LogLevel.SEVERE,"Error trying to create the JmfMediaApp");
			}
		}
		return video_streamer;
	}


	/** Creates a new ptt streamer. */
	private MediaStreamer newPttStreamer(FlowSpec flow_spec) {
		
		MediaStreamer ptt_streamer=null;
		try {
			Class media_streamer_class=Class.forName("local.ext.media.push2talk.Push2TalkApp");
			Class[] param_types={ FlowSpec.class, Logger.class };
			Object[] param_values={ flow_spec, logger };
			java.lang.reflect.Constructor media_streamer_constructor=media_streamer_class.getConstructor(param_types);
			ptt_streamer=(MediaStreamer)media_streamer_constructor.newInstance(param_values);
		}
		catch (Exception e) {
			log(LogLevel.WARNING,e);
			log(LogLevel.SEVERE,"Error trying to create the Push2TalkApp");
		}
		return ptt_streamer;
	}


	// ***************************** logs ****************************

	/** Adds a new string to the default Log. */
	private void log(String str) {
		log(LogLevel.INFO,str);
	}

	/** Adds a new string to the default Log. */
	private void log(LogLevel level, String str) {
		if (logger!=null) logger.log(level,"MediaAgent: "+str);
	}

	/** Adds the Exception message to the default Log. */
	private final void log(LogLevel level, Exception e) {
		log(level,"Exception: "+ExceptionPrinter.getStackTraceOf(e));
	}


}
