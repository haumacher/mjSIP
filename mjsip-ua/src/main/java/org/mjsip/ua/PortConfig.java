/*
 * Copyright (c) 2023 Bernhard Haumacher et al. All Rights Reserved.
 */
package org.mjsip.ua;

import org.zoolu.util.Configure;
import org.zoolu.util.Flags;
import org.zoolu.util.Parser;

/**
 * Configuration options for specifying a port range to serve RTP streams.
 */
public class PortConfig extends Configure {
	
	public int mediaPort = 50000;
	
	public int portCount = 100;

	@Override
	public void setOption(String attribute, Parser par) {
		switch (attribute) {
		case "media_port": mediaPort = par.getInt(); break;
		case "port_count": portCount = par.getInt(); break;
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

}
