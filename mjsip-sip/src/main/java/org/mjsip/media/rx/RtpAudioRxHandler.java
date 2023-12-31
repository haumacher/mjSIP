/*
 * Copyright (c) 2023 Bernhard Haumacher et al. All Rights Reserved.
 */
package org.mjsip.media.rx;

import java.util.concurrent.Executor;

import org.mjsip.media.RtpStreamReceiver;

/**
 * {@link AudioRxHandle} default implementation.
 */
public class RtpAudioRxHandler implements AudioRxHandle {

	private final RtpStreamReceiver _rtpReceiver;

	/**
	 * Creates a {@link RtpAudioRxHandler}.
	 */
	public RtpAudioRxHandler(RtpStreamReceiver rtpReceiver) {
		_rtpReceiver = rtpReceiver;
	}

	@Override
	public void start(Executor executor) {
		executor.execute(_rtpReceiver);
	}

	@Override
	public void halt() {
		_rtpReceiver.halt();
	}

}
