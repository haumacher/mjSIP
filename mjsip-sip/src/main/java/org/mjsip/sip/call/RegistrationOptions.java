/*
 * Copyright (c) 2023 Bernhard Haumacher et al. All Rights Reserved.
 */
package org.mjsip.sip.call;

import org.mjsip.sip.address.NameAddress;

/**
 * Options for a {@link RegistrationClient}.
 */
public interface RegistrationOptions {

	/** Whether registering with the registrar server */
	boolean isRegister();

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

	/** Expires time (in seconds). */
	int getExpires();

	/** 
	 * This options without active registration.
	 */
	default RegistrationOptions noRegistration() {
		return new RegistrationOptions() {
			@Override
			public boolean isUaServer() {
				return false;
			}
			
			@Override
			public boolean isRegister() {
				return false;
			}
			
			@Override
			public boolean isOptionsServer() {
				return false;
			}
			
			@Override
			public boolean isNullServer() {
				return false;
			}
			
			@Override
			public NameAddress getUserURI() {
				return RegistrationOptions.this.getUserURI();
			}
			
			@Override
			public String getRegistrar() {
				return RegistrationOptions.this.getRegistrar();
			}
			
			@Override
			public String getRoute() {
				return RegistrationOptions.this.getRoute();
			}

			@Override
			public int getExpires() {
				return RegistrationOptions.this.getExpires();
			}
			
			@Override
			public String getAuthUser() {
				return RegistrationOptions.this.getAuthUser();
			}
			
			@Override
			public String getAuthRealm() {
				return RegistrationOptions.this.getAuthRealm();
			}
			
			@Override
			public String getAuthPasswd() {
				return RegistrationOptions.this.getAuthPasswd();
			}
		};
	}

}
