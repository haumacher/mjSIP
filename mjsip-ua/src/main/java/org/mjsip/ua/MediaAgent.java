/*
 * Copyright (c) 2023 Bernhard Haumacher et al. All Rights Reserved.
 */
package org.mjsip.ua;

import java.util.concurrent.Executor;

import org.mjsip.media.FlowSpec;
import org.mjsip.media.MediaDesc;
import org.mjsip.media.MediaStreamer;
import org.mjsip.pool.PortPool;
import org.mjsip.ua.streamer.StreamerFactory;

/**
 * Media-definition of a call with corresponding {@link StreamerFactory} that can create
 * corresponding {@link MediaStreamer} implementations.
 */
public class MediaAgent {

	private final MediaDesc[] _media;
	
	private final StreamerFactory _streamerFactory;

	/**
	 * Creates a {@link MediaAgent}.
	 *
	 * @param media
	 *        See {@link #getCallMedia()}.
	 * @param streamerFactory
	 *        The {@link StreamerFactory} to create streamer implementations with.
	 */
	public MediaAgent(MediaDesc[] media, StreamerFactory streamerFactory) {
		_media = MediaDesc.copy(media);
		_streamerFactory = streamerFactory;
	}

	/**
	 * Sets the transport port for each medium.
	 * 
	 * @param portPool
	 *        The pool to take free ports from.
	 */
	public void allocateMediaPorts(PortPool portPool) {
		for (int i=0; i<_media.length; i++) {
			MediaDesc md=_media[i];
			md.setPort(portPool.allocate());
		}
	}

	/** 
	 * Description of media offered by this {@link MediaAgent}.
	 */
	public MediaDesc[] getCallMedia() {
		return _media;
	}

	/** 
	 * Starts a media session 
	 */
	public MediaStreamer startMediaSession(Executor executor, FlowSpec flow_spec) {
		return _streamerFactory.createMediaStreamer(executor, flow_spec);
	}

	/**
	 * Releases ports previously allocated using {@link #allocateMediaPorts(PortPool)}.
	 * 
	 * @param portPool The pool to put ports back to.
	 */
	public void releaseMediaPorts(PortPool portPool) {
		for (int i=0; i<_media.length; i++) {
			MediaDesc md=_media[i];
			portPool.release(md.getPort());
			md.setPort(0);
		}
	}

}
