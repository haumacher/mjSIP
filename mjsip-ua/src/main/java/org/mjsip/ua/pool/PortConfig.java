/*
 * Copyright (c) 2023 Bernhard Haumacher et al. All Rights Reserved.
 */
package org.mjsip.ua.pool;

import org.zoolu.util.Configure;
import org.zoolu.util.Flags;
import org.zoolu.util.Parser;

/**
 * Configuration options for specifying a port range to serve RTP streams.
 */
public class PortConfig extends Configure implements PortOptions {
	
	private int _mediaPort = 50000;
	
	private int _portCount = 100;

	@Override
	public void setOption(String attribute, Parser par) {
		switch (attribute) {
		case "media_port": setMediaPort(par.getInt()); break;
		case "port_count": setPortCount(par.getInt()); break;
		}
	}

	/** 
	 * Creates a {@link PortConfig} from configuration.
	 */
	public static PortConfig init(String config_file, Flags flags) {
		PortConfig result = new PortConfig();
		result.loadFile(config_file);
		flags.getInteger("media_port", "<first port>", 50000, "The first port used for RTP media streams.");
		flags.getInteger("port_count", "<cnt>", 100, "The number of ports used for RTP media streaming.");
		return result;
	}

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

}
