/*
 * Copyright (c) 2023 Bernhard Haumacher et al. All Rights Reserved.
 */
package org.mjsip.ua.sound;

/**
 * Listener that is informed about events in {@link AlawSilenceTrimmer}.
 */
public interface SilenceListener {
	
	void onSilenceStarted(long clock);
	
	void onSilenceEnded(long clock);
	
	/**
	 * Creates a guard when the given listener is <code>null</code>.
	 */
	public static SilenceListener nonNull(SilenceListener listener) {
		if (listener == null) {
			return new SilenceListenerAdapter() {
				// No-op.
			};
		}
		return listener;
	}

}
