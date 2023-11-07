/*
 * Copyright (c) 2023 Bernhard Haumacher et al. All Rights Reserved.
 */
package org.mjsip.ua;

import org.mjsip.ua.registration.RegistrationOptions;

/**
 * Options for setting up a {@link UserAgent}
 */
public interface UAOptions extends ClientOptions, RegistrationOptions {

	/** Whether registering with a registrar server */
	boolean isRegister();

	/**
	 * Whether running the UAS (User Agent Server), or acting just as UAC (User Agent Client). In
	 * the latter case only outgoing calls are supported.
	 */
	boolean isUaServer();

	/** Whether running an Options Server, that automatically responds to OPTIONS requests. */
	boolean isOptionsServer();

	/** Whether running an Null Server, that automatically responds to not-implemented requests. */
	boolean isNullServer();

}
