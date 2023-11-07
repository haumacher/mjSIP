/*
 * Copyright (c) 2023 Bernhard Haumacher et al. All Rights Reserved.
 */
package org.mjsip.ua;

import org.mjsip.media.FlowSpec.Direction;

/**
 * The non-user-specific aspects of {@link ClientOptions}.
 */
public interface StaticOptions {

	/** Media address (use it if you want to use a media address different from the via address) */
	String getMediaAddr();

	/** No offer in the invite */
	boolean getNoOffer();

	/**
	 * Response time in seconds; it is the maximum time the user can wait before responding to an
	 * incoming call; after such time the call is automatically declined (refused).
	 */
	int getRefuseTime();

	/** 
	 * The flow direction.
	 */
	Direction getDirection();
	
}
