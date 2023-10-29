/*
 * Copyright (c) 2023 Bernhard Haumacher et al. All Rights Reserved.
 */
package org.mjsip.examples;

import org.mjsip.ua.MediaOptions;

/**
 * {@link MediaOptions} for test situations.
 */
public interface ExampleMediaOptions extends MediaOptions {

	/** Audio file to be recorded */
	String getRecvFile();

	/** Audio file to be streamed */
	String getSendFile();

	/** Whether looping the received media streams back to the sender. */
	boolean isLoopback();

	/** Whether playing a test tone in send only mode */
	boolean isSendTone();

}
