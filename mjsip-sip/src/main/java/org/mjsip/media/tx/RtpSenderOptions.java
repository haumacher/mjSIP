/*
 * Copyright (c) 2023 Bernhard Haumacher et al. All Rights Reserved.
 */
package org.mjsip.media.tx;

import org.mjsip.media.RtpStreamSender;

/**
 * Options for the {@link RtpStreamSender}.
 */
public interface RtpSenderOptions {

	/**
	 * The synchronization adjustment time (in milliseconds).
	 * 
	 * <p>
	 * It accelerates (&lt; 0) or reduces (&gt; 0) the sending rate with respect to the nominal
	 * value.
	 * 
	 * @return The difference between the actual inter-packet sending time with respect to the
	 *         nominal value (in milliseconds).
	 */
	long syncAdjust();

}
