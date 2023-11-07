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
	
}
