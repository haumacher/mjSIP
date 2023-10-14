/*
 * Copyright (c) 2023 Bernhard Haumacher et al. All Rights Reserved.
 */
package org.mjsip.sip.provider;

import org.mjsip.sip.message.SipMessage;

/**
 * Description of a problem that occurred while processing a {@link SipMessage}.
 */
public class MessageProblem extends SipMessage {

	private final SipMessage _msg;

	private final Exception _exception;

	/**
	 * Creates a {@link MessageProblem}.
	 */
	public MessageProblem(SipMessage msg, Exception exception) {
		_msg = msg;
		_exception = exception;
	}

	/**
	 * The {@link SipMessage} that caused the problem.
	 */
	public SipMessage getMsg() {
		return _msg;
	}

	/**
	 * The {@link Exception} that occurred.
	 */
	public Exception getException() {
		return _exception;
	}

}
