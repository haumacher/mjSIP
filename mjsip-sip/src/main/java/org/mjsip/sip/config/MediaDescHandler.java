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
import org.mjsip.media.MediaDesc;

/**
 * {@link OptionHandler} for {@link MediaDesc} instances.
 */
public class MediaDescHandler extends OptionHandler<MediaDesc> {

	/**
	 * Creates a {@link MediaDescHandler}.
	 */
	public MediaDescHandler(CmdLineParser parser, OptionDef option, Setter<? super MediaDesc> setter) {
		super(parser, option, setter);
	}

	@Override
	public int parseArguments(Parameters params) throws CmdLineException {
		String value = params.getParameter(0);
		setter.addValue(MediaDesc.parseMediaDesc(value));
		return 1;
	}

	@Override
	public String getDefaultMetaVariable() {
		return "<media-descr>";
	}

}
