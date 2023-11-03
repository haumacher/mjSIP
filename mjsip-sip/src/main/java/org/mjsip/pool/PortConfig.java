/*
 * Copyright (c) 2023 Bernhard Haumacher et al. All Rights Reserved.
 */
package org.mjsip.pool;

import org.kohsuke.args4j.Option;

/**
 * Configuration options for specifying a port range to serve RTP streams.
 */
public class PortConfig implements PortOptions {
	
	@Option(name = "--media-port", usage = "The first port used for RTP media streams.")
	private int _mediaPort = 50000;
	
	@Option(name = "--port-count", usage = "The number of ports used for RTP media streaming.")
	private int _portCount = 100;

	@Override
	public int getMediaPort() {
		return _mediaPort;
	}

	/** @see #getMediaPort() */
	public void setMediaPort(int mediaPort) {
		_mediaPort = mediaPort;
	}

	@Override
	public int getPortCount() {
		return _portCount;
	}

	/** @see #getPortCount() */
	public void setPortCount(int portCount) {
		_portCount = portCount;
	}

	/** 
	 * Creates a {@link PortPool} with this configuration.
	 */
	public PortPool createPool() {
		return new PortPool(getMediaPort(), getPortCount());
	}

}
