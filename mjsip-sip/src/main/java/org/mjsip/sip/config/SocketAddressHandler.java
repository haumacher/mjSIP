/*
 * Copyright (c) 2023 Bernhard Haumacher et al. All Rights Reserved.
 */
package org.mjsip.sip.config;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.OptionDef;
import org.kohsuke.args4j.spi.OptionHandler;
import org.kohsuke.args4j.spi.Parameters;
import org.kohsuke.args4j.spi.Setter;
import org.zoolu.net.SocketAddress;

/**
 * {@link OptionHandler} for {@link SocketAddress} instances.
 */
public class SocketAddressHandler extends OptionHandler<SocketAddress> {

	/**
	 * Creates a {@link SocketAddressHandler}.
	 */
	public SocketAddressHandler(CmdLineParser parser, OptionDef option, Setter<? super SocketAddress> setter) {
		super(parser, option, setter);
	}

	@Override
	public int parseArguments(Parameters params) throws CmdLineException {
		String value = params.getParameter(0);
		setter.addValue(new SocketAddress(value));
		return 1;
	}

	@Override
	public String getDefaultMetaVariable() {
		return "<addr>";
	}

}
