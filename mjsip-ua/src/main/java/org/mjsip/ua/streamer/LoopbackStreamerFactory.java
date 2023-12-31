/*
 * Copyright (c) 2023 Bernhard Haumacher et al. All Rights Reserved.
 */
package org.mjsip.ua.streamer;

import java.util.concurrent.Executor;

import org.mjsip.media.FlowSpec;
import org.mjsip.media.LoopbackMediaStreamer;
import org.mjsip.media.MediaStreamer;

/**
 * {@link StreamerFactory} creating {@link LoopbackMediaStreamer}s.
 */
public final class LoopbackStreamerFactory implements StreamerFactory {
	@Override
	public MediaStreamer createMediaStreamer(Executor executor, FlowSpec flow_spec) {
		return new LoopbackMediaStreamer(flow_spec);
	}
}