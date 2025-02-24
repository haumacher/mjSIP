/*
 * Copyright (c) 2023 Bernhard Haumacher et al. All Rights Reserved.
 */
package org.mjsip.config;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.kohsuke.args4j.ClassParser;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.NamedOptionDef;
import org.kohsuke.args4j.OptionDef;
import org.kohsuke.args4j.ParserProperties;
import org.kohsuke.args4j.spi.OptionHandler;
import org.slf4j.LoggerFactory;
import org.zoolu.util.ConfigFile;

/**
 * Utility for parsing command line options to multiple beans using args4j.
 */
public class OptionParser {

	private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(OptionParser.class);
	
	/**
	 * Parses the given command line arguments and fills the given bean objects.
	 */
	public static MetaConfig parseOptions(String[] args, String defaultConfigFile, Object...beans) {
		MetaConfig metaConfig = new MetaConfig();
		ParserProperties options = ParserProperties.defaults().withUsageWidth(120);
		CmdLineParser parser = new CmdLineParser(metaConfig, options);
		for (Object bean : beans) {
			new ClassParser().parse(bean, parser);
		}
		
		try {
			List<String> envArgs = new ArrayList<>();
			for (OptionHandler<?> option : parser.getOptions()) {
				OptionDef optionDef = option.option;
				if (optionDef instanceof NamedOptionDef) {
					NamedOptionDef namedOption = (NamedOptionDef) optionDef;
					
					String name = namedOption.name();
					String value = getValueFromEnvironment(name);
					if (value == null) {
						for (String alias : namedOption.aliases()) {
							value = getValueFromEnvironment(alias);
							if (value != null) {
								break;
							}
						}
					}
					
					if (value != null) {
						envArgs.add(name);
						envArgs.add(value);
					}
				}
			}

			CmdLineException problem = null;
			boolean invalidValueGiven = false;
			try {
				// First try, use options form environment and command line. 
				Collection<String> arguments = new ArrayList<>(envArgs);
				arguments.addAll(Arrays.asList(args));
				parser.parseArgument(arguments);
			} catch (CmdLineException ex) {
				// Happens, when required configurations are not given on the command line.
				problem = ex;
				String message = ex.getMessage();
				invalidValueGiven = message != null && message.contains(" is not a valid value for ");
				if (invalidValueGiven) {
					LOG.warn("{}. Reading from environment/command line aborted", message);
				}
			}
			
			String argFile = metaConfig.configFile;
			
			File file;
			if (argFile != null && !argFile.isBlank()) {
				if ("none".equalsIgnoreCase(argFile)) {
					file = null;
				} else {
					file = new File(argFile);
					if (!file.exists()) {
						System.err.println("Configuration file does not exits: " + file.getAbsolutePath());
						System.exit(1);
					}
				}
			} else if (defaultConfigFile != null) {
				file = new File(".", defaultConfigFile);
				if (!file.exists()) {
					file = new File(System.getProperty("user.home"), defaultConfigFile);
					if (!file.exists()) {
						file = null;
					}
				}
			} else {
				file = null;
			}
			
			if (file!= null) {
				if (invalidValueGiven) {
					LOG.warn("Reading options from: {}", file.getAbsolutePath());
				} else {
					LOG.debug("Reading options from: {}", file.getAbsolutePath());
				}
				
				ConfigFile configFile = new ConfigFile(file);
				
				// Parse all arguments again to check for required arguments not given and to
				// allow overwriting configuration file arguments with environment and command
				// line.
				// Use the following precedence order: Arguments in the configuration file (lowest),
				// arguments from the environment, arguments given on the command line (highest).
				Collection<String> arguments = new ArrayList<>(configFile.toArguments());
				arguments.addAll(envArgs);
				arguments.addAll(Arrays.asList(args));
				
				parser.parseArgument(arguments);
			} else {
				if (problem != null) {
					// The first parse attempt should have been successful, report problem.
					throw problem;
				}
			}
	
			if (metaConfig.help) {
				parser.printUsage(System.err);
				System.exit(1);
			}
		} catch (CmdLineException ex) {
			parser.printUsage(System.err);
			System.out.println(ex.getMessage());
			System.exit(1);
		}
		
		return metaConfig;
	}

	public static String getValueFromEnvironment(String optionName) {
		if (!optionName.startsWith("--")) {
			return null;
		}

		String env = optionName.substring(2).replace('-', '_').toUpperCase();
		String value = System.getenv(env);
		if (value != null) {
			LOG.info("Using '{}' from environment: {}={}", optionName, env, value);
		}
		return value;
	}

}
