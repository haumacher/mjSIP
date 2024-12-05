/*
 * Copyright (c) 2023 Bernhard Haumacher et al. All Rights Reserved.
 */
package org.mjsip.ua.registration;

import org.mjsip.sip.address.NameAddress;
import org.slf4j.LoggerFactory;

/**
 * {@link RegistrationClientListener} logging registration status.
 */
public class RegistrationLogger implements RegistrationClientListener {

	private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(RegistrationLogger.class);

	/** When a UA has been successfully (un)registered. */
	@Override
	public void onRegistrationSuccess(RegistrationClient rc, NameAddress target, NameAddress contact, int expires, int renewTime, String result) {
		LOG.info("Registration of '{}' {}, expires in {}s {}.", contact, result, expires, (renewTime > 0 ? ", renewing in " + renewTime + "s" : ""));
	}

	/** When a UA failed on (un)registering. */
	@Override
	public void onRegistrationFailure(RegistrationClient rc, NameAddress target, NameAddress contact, String result) {
		LOG.info("Registration of '{}' failed: {}", contact, result);
	}

}
