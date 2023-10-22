/*
 * Copyright (c) 2023 Bernhard Haumacher et al. All Rights Reserved.
 */
package org.mjsip.ua.streamer;

import org.mjsip.media.FlowSpec;
import org.mjsip.media.MediaStreamer;

/**
 * Factory for {@link MediaStreamer}s.
 *
 * @see DispatchingStreamerFactory#addFactory(String, StreamerFactory)
 */
public interface StreamerFactory {

	/** 
	 * Creates a {@link MediaStreamer} for the given flow.
	 */
	MediaStreamer createMediaStreamer(FlowSpec flow_spec);

}
