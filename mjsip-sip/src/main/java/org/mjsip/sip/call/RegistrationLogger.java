/*
 * Copyright (c) 2023 Bernhard Haumacher et al. All Rights Reserved.
 */
package org.mjsip.sip.call;

import org.mjsip.sip.address.NameAddress;
import org.slf4j.LoggerFactory;

/**
 * {@link RegistrationClientListener} logging registration status.
 */
public class RegistrationLogger implements RegistrationClientListener {

	private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(RegistrationLogger.class);

	/** When a UA has been successfully (un)registered. */
	@Override
	public void onRegistrationSuccess(RegistrationClient rc, NameAddress target, NameAddress contact, int expires, String result) {
		LOG.info("Registration success: expires="+expires+": "+result);
	}

	/** When a UA failed on (un)registering. */
	@Override
	public void onRegistrationFailure(RegistrationClient rc, NameAddress target, NameAddress contact, String result) {
		LOG.info("Registration failure: "+result);
	}

}
