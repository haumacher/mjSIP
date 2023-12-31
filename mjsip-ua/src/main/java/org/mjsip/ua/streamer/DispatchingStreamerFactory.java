/*
 * Copyright (c) 2023 Bernhard Haumacher et al. All Rights Reserved.
 */
package org.mjsip.ua.streamer;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;

import org.mjsip.media.FlowSpec;
import org.mjsip.media.MediaStreamer;

/**
 * {@link StreamerFactory} that dispatches based on the media type to other {@link StreamerFactory}
 * implementations.
 */
public class DispatchingStreamerFactory implements StreamerFactory {
	
	private final StreamerFactory _defaultStreamerFactory;
	private final Map<String, StreamerFactory> _factoryByType = new HashMap<>();
	
	/**
	 * Creates a {@link DispatchingStreamerFactory} with no default {@link StreamerFactory}.
	 */
	public DispatchingStreamerFactory() {
		this(NoStreamerFactory.INSTANCE);
	}
	
	/**
	 * Creates a {@link DispatchingStreamerFactory}.
	 *
	 * @param defaultStreamerFactory
	 *        The {@link StreamerFactory} to use, if no factory was added for a certain media type.
	 */
	public DispatchingStreamerFactory(StreamerFactory defaultStreamerFactory) {
		_defaultStreamerFactory = defaultStreamerFactory;
	}

	/**
	 * Adds a new {@link StreamerFactory} for the given media type.
	 * 
	 * @see FlowSpec#getMediaType()
	 */
	public DispatchingStreamerFactory addFactory(String mediaType, StreamerFactory factory) {
		_factoryByType.put(mediaType, factory);
		return this;
	}

	@Override
	public MediaStreamer createMediaStreamer(Executor executor, FlowSpec flow_spec) {
		StreamerFactory factory = _factoryByType.get(flow_spec.getMediaType());
		if (factory == null) {
			return _defaultStreamerFactory.createMediaStreamer(executor, flow_spec);
		}
		
		return factory.createMediaStreamer(executor, flow_spec);
	}

}
