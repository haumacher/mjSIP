/*
 * Copyright (c) 2023 Bernhard Haumacher et al. All Rights Reserved.
 */
package org.mjsip.config;

import java.io.File;

import org.kohsuke.args4j.Option;

/**
 * Configuration with reference to configuration file.
 */
public class MetaConfig {
	
	@Option(name = "-h", aliases = "--help", usage = "Print this help message.", help = true)
	boolean help;
	
	/**
	 * The configuration file to read.
	 */
	@Option(name = "-f", aliases = "--config-file", metaVar = "<file>", usage = "File with configuration options, 'none' to prevent reading from the default location.")
	public String configFile;

	/** 
	 * The configuration file passed through the command line options.
	 */
	public File getConfigFile() {
		return configFile == null || configFile.isBlank() || "none".equals(configFile) ? null : new File(configFile);
	}

}
