/*
 * Copyright (c) 2023 Bernhard Haumacher et al. All Rights Reserved.
 */
package org.mjsip.config;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.OptionDef;
import org.kohsuke.args4j.spi.OptionHandler;
import org.kohsuke.args4j.spi.Parameters;
import org.kohsuke.args4j.spi.Setter;

/**
 * {@link OptionHandler} accepting yes/no values for {@link Boolean} options.
 */
public class YesNoHandler extends OptionHandler<Boolean> {

	/** 
	 * Creates a {@link YesNoHandler}.
	 */
	public YesNoHandler(CmdLineParser parser, OptionDef option, Setter<? super Boolean> setter) {
		super(parser, option, setter);
	}

	@Override
	public int parseArguments(Parameters params) throws CmdLineException {
		setter.addValue(Boolean.valueOf(params.getParameter(0).toLowerCase().startsWith("y")));
		return 1;
	}

	@Override
	public String getDefaultMetaVariable() {
		return "yes/no";
	}

}
