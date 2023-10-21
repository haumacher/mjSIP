/*
 * Copyright (c) 2023 Bernhard Haumacher et al. All Rights Reserved.
 */
package org.mjsip.media.rx;

import org.mjsip.rtp.RtpPayloadFormat;

/**
 * TODO
 *
 * @author <a href="mailto:haui@haumacher.de">Bernhard Haumacher</a>
 */
public interface AudioRxHandle {

	/**
	 * TODO
	 *
	 */
	void start();

	/**
	 * TODO
	 *
	 */
	void halt();

	/**
	 * TODO
	 *
	 * @param amr_payload_format
	 */
	void setRtpPayloadFormat(RtpPayloadFormat amr_payload_format);

	/**
	 * TODO
	 *
	 * @param sEQUENCE_CHECK
	 */
	void setSequenceCheck(boolean value);

	/**
	 * TODO
	 *
	 * @param sILENCE_PADDING
	 */
	void setSilencePadding(boolean value);

	/**
	 * TODO
	 *
	 * @param random_early_drop
	 */
	void setRED(int random_early_drop);

}
