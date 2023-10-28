/*
 * Copyright (c) 2023 Bernhard Haumacher et al. All Rights Reserved.
 */
package org.mjsip.ua;

import org.mjsip.media.FlowSpec.Direction;
import org.mjsip.sip.address.NameAddress;

/**
 * Options for setting up a {@link UserAgent}
 */
public interface UAOptions {

	/**
	 * Whether running the UAS (User Agent Server), or acting just as UAC (User Agent Client). In
	 * the latter case only outgoing calls are supported.
	 */
	boolean isUaServer();

	/** Whether running an Options Server, that automatically responds to OPTIONS requests. */
	boolean isOptionsServer();

	/** Whether running an Null Server, that automatically responds to not-implemented requests. */
	boolean isNullServer();

	/**
	 * Fully qualified domain name (or address) of the registrar server. It is used as recipient for
	 * REGISTER requests.
	 * <p>
	 * If <i>registrar</i> is not defined, the <i>proxy</i> value is used in its place.
	 * </p>
	 * <p>
	 * If <i>proxy</i> is not defined, the <i>registrar</i> value is used in its place.
	 * </p>
	 */
	String getRegistrar();

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
	 * Gets the user's AOR (Address Of Record) registered to the registrar server and used as From
	 * URI.
	 * <p>
	 * In case of <i>proxy</i> and <i>user</i> parameters have been defined it is formed as
	 * "<i>display_name</i>" &lt;sip:<i>user</i>@<i>proxy</i>&gt;, otherwhise the local UA address
	 * (obtained by the SipProvider) is used.
	 * 
	 * @return the user's name address
	 */
	NameAddress getUserURI();

	/** User's name used for server authentication. */
	String getAuthUser();

	/** User's passwd used for server authentication. */
	String getAuthPasswd();

	/** User's realm used for server authentication. */
	String getAuthRealm();

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
