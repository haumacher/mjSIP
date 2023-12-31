/*
 * Copyright (c) 2023 Bernhard Haumacher et al. All Rights Reserved.
 */
package org.mjsip.media.rx;

import java.util.concurrent.Executor;

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
	void start(Executor executor);

	/**
	 * TODO
	 *
	 */
	void halt();

}
