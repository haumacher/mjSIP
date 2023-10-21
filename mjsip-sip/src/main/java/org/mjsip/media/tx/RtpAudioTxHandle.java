/*
 * Copyright (c) 2023 Bernhard Haumacher et al. All Rights Reserved.
 */
package org.mjsip.media.tx;

import java.net.UnknownHostException;

import org.mjsip.media.RtpStreamSender;
import org.mjsip.rtp.RtpControl;
import org.mjsip.rtp.RtpPayloadFormat;
import org.zoolu.net.SocketAddress;

/**
 * {@link AudioTXHandle} implementation.
 */
class RtpAudioTxHandle implements AudioTXHandle {

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
	public void start() {
		_rtpSender.start();
	}

	@Override
	public void halt() {
		_rtpSender.halt();
	}

	@Override
	public void setRemoteSoAddress(SocketAddress remote_soaddr) throws UnknownHostException {
		getRtpSender().setRemoteSoAddress(remote_soaddr);
	}

	@Override
	public void setSyncAdj(long sync_adj) {
		getRtpSender().setSyncAdj(sync_adj);
	}

	@Override
	public void setControl(RtpControl rtp_control) {
		getRtpSender().setControl(rtp_control);
	}

	@Override
	public void setRtpPayloadFormat(RtpPayloadFormat rtp_payload_format) {
		getRtpSender().setRtpPayloadFormat(rtp_payload_format);
	}

}
