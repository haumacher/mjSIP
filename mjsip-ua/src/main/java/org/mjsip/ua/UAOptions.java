/*
 * Copyright (c) 2023 Bernhard Haumacher et al. All Rights Reserved.
 */
package org.mjsip.ua;

import org.mjsip.media.FlowSpec.Direction;

/**
 * Options for setting up a {@link UserAgent}
 */
public interface UAOptions {

	/**
	 * Fully qualified domain name (or address) of the proxy server. It is part of the user's AOR
	 * registered to the registrar server and used as From URI.
	 * <p>
	 * If <i>proxy</i> is not defined, the <i>registrar</i> value is used in its place.
	 * </p>
	 * <p>
	 * If <i>registrar</i> is not defined, the <i>proxy</i> value is used in its place.
	 * </p>
	 */
	String getProxy();

	/**
	 * User's name. It is used to build the user's AOR registered to the registrar server and used
	 * as From URI.
	 */
	String getUser();

	/** 
	 * The flow direction.
	 */
	Direction getDirection();

	/** Media address (use it if you want to use a media address different from the via address) */
	String getMediaAddr();

	/** No offer in the invite */
	boolean getNoOffer();

	/**
	 * Response time in seconds; it is the maximum time the user can wait before responding to an
	 * incoming call; after such time the call is automatically declined (refused).
	 */
	int getRefuseTime();

}
