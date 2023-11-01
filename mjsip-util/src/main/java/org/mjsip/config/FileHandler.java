/*
 * Copyright (c) 2023 Bernhard Haumacher et al. All Rights Reserved.
 */
package org.mjsip.config;

import java.io.File;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.OptionDef;
import org.kohsuke.args4j.spi.OptionHandler;
import org.kohsuke.args4j.spi.Parameters;
import org.kohsuke.args4j.spi.Setter;

/**
 * {@link OptionHandler} accepting yes/no values for {@link Boolean} options.
 */
public class FileHandler extends OptionHandler<File> {

	/** 
	 * Creates a {@link FileHandler}.
	 */
	public FileHandler(CmdLineParser parser, OptionDef option, Setter<? super File> setter) {
		super(parser, option, setter);
	}

	@Override
	public int parseArguments(Parameters params) throws CmdLineException {
		String value = params.getParameter(0);
		setter.addValue(value.isBlank() ? null : new File(value));
		return 1;
	}

	@Override
	public String getDefaultMetaVariable() {
		return "<file>";
	}

}
