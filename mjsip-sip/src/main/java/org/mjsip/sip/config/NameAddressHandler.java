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
import org.mjsip.sip.address.NameAddress;

/**
 * {@link OptionHandler} for {@link NameAddress} values.
 */
public class NameAddressHandler extends OptionHandler<NameAddress> {

	/** 
	 * Creates a {@link NameAddressHandler}.
	 */
	public NameAddressHandler(CmdLineParser parser, OptionDef option, Setter<? super NameAddress> setter) {
		super(parser, option, setter);
	}

	@Override
	public int parseArguments(Parameters params) throws CmdLineException {
		setter.addValue(NameAddress.parse(params.getParameter(0)));
		return 1;
	}

	@Override
	public String getDefaultMetaVariable() {
		return "<sip-uri>";
	}

}
