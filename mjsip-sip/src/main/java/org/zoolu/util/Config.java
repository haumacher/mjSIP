/*
 * Copyright (c) 2023 Bernhard Haumacher et al. All Rights Reserved.
 */
package org.zoolu.util;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * A buffer of key value pairs.
 *
 * @author <a href="mailto:haui@haumacher.de">Bernhard Haumacher</a>
 */
public class Config extends Configure {
	
	private final Map<String, String> _options = new LinkedHashMap<>();

	/** 
	 * Creates a {@link Config}.
	 */
	public Config(String file) {
		loadFile(file);
	}

	@Override
	public void setOption(String attribute, Parser par) {
		_options.put(attribute, par.getRemainingString());
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

}
