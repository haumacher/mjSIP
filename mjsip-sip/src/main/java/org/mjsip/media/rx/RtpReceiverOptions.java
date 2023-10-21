/*
 * Copyright (c) 2023 Bernhard Haumacher et al. All Rights Reserved.
 */
package org.mjsip.media.rx;

import org.mjsip.media.RtpStreamReceiver;

/**
 * Options for {@link RtpStreamReceiver}.
 */
public interface RtpReceiverOptions {

	/** Whether out-of-sequence and duplicated packets are discarded. */
	boolean sequenceCheck();

	/** Whether filling silence intervals with (silence-equivalent) void data. */
	boolean silencePadding();

	/**
	 * The packet random early drop (RED) value. The number of packets that separates two drops. A
	 * value of 0 means no drop. If greater than 0, it is the inverse of the packet drop rate.
	 */
	int randomEarlyDrop();

	/**
	 * In ssrc-check mode packets with a SSRC that differs from the one in the first received packet
	 * are discarded.
	 */
	boolean ssrcCheck();

}
