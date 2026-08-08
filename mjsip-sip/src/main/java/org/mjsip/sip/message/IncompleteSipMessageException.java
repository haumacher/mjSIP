/*
 * Copyright (c) 2026 Bernhard Haumacher et al. All Rights Reserved.
 */
package org.mjsip.sip.message;

/**
 * Signals that a message could not be parsed because it has not been completely received yet.
 *
 * <p>
 * In contrast to a plain {@link MalformedSipMessageException}, this exception is recoverable: When
 * more data of a stream-oriented transport becomes available, parsing the message can be retried.
 * Only messages received over a stream-oriented transport can be incomplete, since a message
 * received over a datagram-oriented transport is either contained in a single transport packet, or
 * malformed (see RFC 3261, 18.3).
 * </p>
 */
public class IncompleteSipMessageException extends MalformedSipMessageException {

	/**
	 * Creates a {@link IncompleteSipMessageException}.
	 *
	 * @param error
	 *        the error message
	 */
	public IncompleteSipMessageException(String error) {
		super(error);
	}

}
