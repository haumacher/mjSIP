/*
 * Copyright (c) 2023 Bernhard Haumacher et al. All Rights Reserved.
 */
package org.mjsip.ua;

import java.util.HashMap;

import org.mjsip.media.FlowSpec;
import org.mjsip.media.MediaStreamer;
import org.mjsip.ua.streamer.StreamerFactory;
import org.slf4j.LoggerFactory;

/**
 * Management of {@link MediaStreamer}s during a session on behalf of a {@link UserAgent}.
 */
public class MediaAgent {

	private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(MediaAgent.class);
	
	/** Active media streamers, as table of: (String)media-->(MediaStreamer)media_streamer */
	private HashMap<String, MediaStreamer> media_streamers = new HashMap<>();

	private StreamerFactory _streamerFactory;

	/**
	 * Creates a {@link MediaAgent}.
	 *
	 * @param streamerFactory The {@link StreamerFactory} to create streamer implementations with.
	 */
	public MediaAgent(StreamerFactory streamerFactory) {
		_streamerFactory = streamerFactory;
	}
	
	/** Starts a media session */
	public boolean startMediaSession(FlowSpec flow_spec) {
		LOG.info("Starting media session: " + flow_spec.getMediaSpec());
		LOG.info("Flow: " + flow_spec.getLocalPort() + " " + flow_spec.getDirection().arrow() + " " + flow_spec.getRemoteAddress() + ":" + flow_spec.getRemotePort());
		
		String mediaType=flow_spec.getMediaType();
		
		// stop previous media streamer (just in case something was wrong..)
		MediaStreamer existing = media_streamers.remove(mediaType);
		if (existing != null) {
			existing.halt();
		}
		 
		// Create a new media streamer
		MediaStreamer media_streamer = createMediaStreamer(flow_spec);
		if (media_streamer == null) {
			LOG.warn("No media streamer found for type: " + mediaType);
			return false;
		}
		
		// Start the new stream.
		if (media_streamer.start()) {
			media_streamers.put(mediaType, media_streamer);
			return true;
		} else {
			return false;
		}
	}

	/** 
	 * Creates a {@link MediaStreamer} for a certain flow.
	 */
	protected MediaStreamer createMediaStreamer(FlowSpec flow_spec) {
		return _streamerFactory.createMediaStreamer(flow_spec);
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


}
