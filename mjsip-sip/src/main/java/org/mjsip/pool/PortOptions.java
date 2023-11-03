/*
 * Copyright (c) 2023 Bernhard Haumacher et al. All Rights Reserved.
 */
package org.mjsip.pool;

/**
 * Options for the {@link PortPool}.
 */
public interface PortOptions {

	/**
	 * The first port to use for media streaming.
	 * 
	 * @see #getPortCount()
	 */
	int getMediaPort();

	/**
	 * The number of ports starting with {@link #getMediaPort()} to use for media streaming.
	 * 
	 * <p>
	 * Each call handled in parallel requires as many ports as media streams are transmitted in that
	 * call. This setting indirectly limits the number of parallel calls a system can handle.
	 * </p>
	 * 
	 * @see #getMediaPort()
	 */
	int getPortCount();

}
