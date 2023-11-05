/*
 * Copyright (c) 2023 Bernhard Haumacher et al. All Rights Reserved.
 */
package org.zoolu.util;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * A buffer of key value pairs.
 *
 * @author <a href="mailto:haui@haumacher.de">Bernhard Haumacher</a>
 */
public class ConfigFile extends Configure {
	
	private final Map<String, String> _options = new LinkedHashMap<>();

	/** 
	 * Creates a {@link ConfigFile}.
	 */
	public ConfigFile(File file) {
		loadFile(file);
	}

	@Override
	public void setOption(String attribute, Parser par) {
		_options.put(attribute, par.getRemainingString().trim());
	}
	
	/**
	 * Sets an option.
	 */
	public void setOption(String key, String value) {
		_options.put(key, value);
	}
	
	/**
	 * Transfers all buffered options to the given {@link Configure}.
	 */
	public void configure(Configure other) {
		for (Entry<String, String> entry : _options.entrySet()) {
			other.setOption(entry.getKey(), new Parser(entry.getValue()));
		}
	}

	/**
	 * Converts the parsed options to an arguments list.
	 */
	public Collection<String> toArguments() {
		return _options.entrySet().stream().flatMap(e -> Arrays.asList("--" + e.getKey().replace('_', '-'), e.getValue()).stream()).collect(Collectors.toList());
	}

}
