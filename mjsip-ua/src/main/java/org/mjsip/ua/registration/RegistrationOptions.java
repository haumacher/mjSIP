/*
 * Copyright (c) 2023 Bernhard Haumacher et al. All Rights Reserved.
 */
package org.mjsip.ua.registration;

import org.mjsip.sip.address.SipURI;

/**
 * Options for a {@link RegistrationClient}.
 */
public interface RegistrationOptions extends AuthOptions {

	/**
	 * Additional routing information to reach the {@link #getRegistrar() registrar}.
	 */
	SipURI getRoute();

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
	SipURI getRegistrar();

	/** Expires time (in seconds). */
	int getExpires();

}
