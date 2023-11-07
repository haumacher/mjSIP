/*
 * Copyright (c) 2023 Bernhard Haumacher et al. All Rights Reserved.
 */
package org.mjsip.ua.registration;

/**
 * Options for a {@link RegistrationClient}.
 */
public interface RegistrationOptions extends AuthOptions {

	/**
	 * Additional routing information to reach the registrar.
	 */
	String getRoute();

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

	/** Expires time (in seconds). */
	int getExpires();

}
