/*
 * Copyright (c) 2023 Bernhard Haumacher et al. All Rights Reserved.
 */
package org.mjsip.ua.streamer;

import org.mjsip.media.FlowSpec;
import org.mjsip.media.LoopbackMediaStreamer;
import org.mjsip.media.MediaStreamer;
import org.mjsip.ua.MediaConfig;

/**
 * {@link StreamerFactory} creating {@link LoopbackMediaStreamer}s.
 */
public final class LoopbackStreamerFactory implements StreamerFactory {
	@Override
	public MediaStreamer createMediaStreamer(FlowSpec flow_spec, MediaConfig mediaConfig) {
		return new LoopbackMediaStreamer(flow_spec);
	}
}