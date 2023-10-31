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
import org.zoolu.net.IpAddress;

/**
 * {@link OptionHandler} for {@link IpAddress} instances.
 */
public class IpAddressHandler extends OptionHandler<IpAddress> {

	/**
	 * Creates a {@link IpAddressHandler}.
	 */
	public IpAddressHandler(CmdLineParser parser, OptionDef option, Setter<? super IpAddress> setter) {
		super(parser, option, setter);
	}

	@Override
	public int parseArguments(Parameters params) throws CmdLineException {
		String value = params.getParameter(0);
		setter.addValue(new IpAddress(value));
		return 1;
	}

	@Override
	public String getDefaultMetaVariable() {
		return "<addr>";
	}

}
