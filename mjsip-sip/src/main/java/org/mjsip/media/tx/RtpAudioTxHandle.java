/*
 * Copyright (c) 2023 Bernhard Haumacher et al. All Rights Reserved.
 */
package org.mjsip.media.tx;

import java.net.UnknownHostException;
import java.util.concurrent.Executor;

import org.mjsip.media.RtpStreamSender;
import org.zoolu.net.SocketAddress;

/**
 * {@link AudioTXHandle} implementation.
 */
public class RtpAudioTxHandle implements AudioTXHandle {

	private RtpStreamSender _rtpSender;

	/**
	 * Creates a {@link RtpAudioTxHandle}.
	 */
	public RtpAudioTxHandle(RtpStreamSender rtpSender) {
		_rtpSender = rtpSender;
	}

	/**
	 * The underlying sender.
	 */
	public RtpStreamSender getRtpSender() {
		return _rtpSender;
	}

	@Override
	public void start(Executor executor) {
		executor.execute(_rtpSender);
	}

	@Override
	public void halt() {
		_rtpSender.halt();
	}

	@Override
	public void join() throws InterruptedException {
		_rtpSender.join();
	}

	@Override
	public void setRemoteSoAddress(SocketAddress remote_soaddr) throws UnknownHostException {
		getRtpSender().setRemoteSoAddress(remote_soaddr);
	}

}
