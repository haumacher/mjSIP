/*
 * Copyright (c) 2023 Bernhard Haumacher et al. All Rights Reserved.
 */
package org.mjsip.ua.streamer;

import org.mjsip.media.FlowSpec;
import org.mjsip.media.MediaStreamer;

/**
 * {@link StreamerFactory} that creates no streamers at all.
 */
public class NoStreamerFactory implements StreamerFactory {
	
	/**
	 * Singleton {@link NoStreamerFactory} instance.
	 */
	public static final NoStreamerFactory INSTANCE = new NoStreamerFactory();

	private NoStreamerFactory() {
		// Singleton constructor.
	}

	@Override
	public MediaStreamer createMediaStreamer(FlowSpec flow_spec) {
		return null;
	}

}
