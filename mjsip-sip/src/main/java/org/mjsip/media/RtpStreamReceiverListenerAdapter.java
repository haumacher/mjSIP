/*
 * Copyright (c) 2023 Bernhard Haumacher et al. All Rights Reserved.
 */
package org.mjsip.media;

import org.zoolu.net.SocketAddress;

/**
 * {@link RtpStreamReceiverListener} that implements all callback as no-op.
 */
public interface RtpStreamReceiverListenerAdapter extends RtpStreamReceiverListener {

	@Override
	default void onRemoteSoAddressChanged(RtpStreamReceiver rr, SocketAddress remote_soaddr) {
		// Ignore.
	}

	@Override
	default void onRtpStreamReceiverTerminated(RtpStreamReceiver rr, Exception error) {
		// Ignore.
	}

}
