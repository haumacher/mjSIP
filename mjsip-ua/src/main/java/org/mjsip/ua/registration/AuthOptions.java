/*
 * Copyright (c) 2023 Bernhard Haumacher et al. All Rights Reserved.
 */
package org.mjsip.ua.registration;

import org.mjsip.sip.address.NameAddress;
import org.mjsip.ua.UserOptions;

/**
 * User authentication common to {@link UserOptions} and {@link RegistrationOptions}.
 */
public interface AuthOptions {

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

}
