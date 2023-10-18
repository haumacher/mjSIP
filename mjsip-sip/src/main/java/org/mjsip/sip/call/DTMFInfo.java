/*
 * Copyright (c) 2023 Bernhard Haumacher et al. All Rights Reserved.
 */
package org.mjsip.sip.call;

/**
 * Represents the contents of a <code>application/dtmf-relay</code> info message.
 *
 * @author <a href="mailto:haui@haumacher.de">Bernhard Haumacher</a>
 */
public class DTMFInfo {

	private static final int DEFAULT_DURATION = 100;

	/**
	 * Creates a DTMFInfo from a <code>application/dtmf-relay</code> key/value pair message.
	 */
	public static DTMFInfo parse(String body) {
		String signal = null;
		int duration = DEFAULT_DURATION;

		int lineStart = 0;
		int lineEnd;
		while ((lineEnd = body.indexOf('\n', lineStart)) >= 0) {
			if (lineEnd > lineStart) {
				// Non-empty.
				int sepIndex = body.indexOf("=", lineStart);
				if (sepIndex >= 0) {
					String key = body.substring(lineStart, sepIndex).trim();
					String value = body.substring(sepIndex + 1, lineEnd).trim();
					switch (key) {
					case "Signal":
						signal = value;
						break;
					case "Duration":
						duration = Integer.parseInt(value);
						break;

					}
				}
			}
			lineStart = lineEnd + 1;
		}

		return new DTMFInfo(signal, duration);
	}

	private final String _signal;

	private final int _duration;

	/**
	 * Creates a {@link DTMFInfo}.
	 *
	 * @param signal
	 *        See {@link #getSignal()}.
	 */
	public DTMFInfo(String signal) {
		this(signal, DEFAULT_DURATION);
	}

	/**
	 * Creates a {@link DTMFInfo}.
	 * 
	 * @param signal
	 *        See {@link #getSignal()}.
	 * @param duration
	 *        See {@link #getDuration()}.
	 */
	public DTMFInfo(String signal, int duration) {
		_signal = signal;
		_duration = duration;
	}

	/**
	 * The key pressed.
	 */
	public String getSignal() {
		return _signal;
	}

	/**
	 * The duration of the key press.
	 */
	public int getDuration() {
		return _duration;
	}

	@Override
	public String toString() {
		return "signal=" + _signal + ", duration=" + _duration;
	}

}
