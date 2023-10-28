/*
 * Copyright (c) 2023 Bernhard Haumacher et al. All Rights Reserved.
 */
package org.mjsip.ua;

/**
 * Options for services automatically answering calls.
 */
public interface ServiceOptions {

	/** Automatic hangup time (maximum call duartion) in seconds; time&lt;=0 means no automatic hangup. */
	int getHangupTime();

}
