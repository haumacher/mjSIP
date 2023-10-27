/*
 * Copyright (c) 2023 Bernhard Haumacher et al. All Rights Reserved.
 */
package org.mjsip.ua.sound;

import javax.sound.sampled.LineListener;

/**
 * {@link LineListener} that implements all callbacks with a no-op.
 */
public interface SilenceListenerAdapter extends SilenceListener {

	@Override
	default void onSilenceStarted(long clock) {
		// Ignore.
	}

	@Override
	default void onSilenceEnded(long clock) {
		// Ignore.
	}

}
