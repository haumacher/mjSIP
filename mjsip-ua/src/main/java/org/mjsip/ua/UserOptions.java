/*
 * Copyright (c) 2023 Bernhard Haumacher et al. All Rights Reserved.
 */
package org.mjsip.ua;

import org.mjsip.ua.registration.AuthOptions;

/**
 * Account-specific aspect of {@link ClientOptions}.
 */
public interface UserOptions extends AuthOptions {

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
	String getSipUser();

}
