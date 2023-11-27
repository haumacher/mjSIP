package org.mjsip.ua.registration;



import org.mjsip.sip.address.NameAddress;



/** Listener of RegistrationClient */
public interface RegistrationClientListener {
	
	/** When it has been successfully (un)registered. */
	public void onRegistrationSuccess(RegistrationClient registration, NameAddress target, NameAddress contact, int expires, int renewTime, String result);

	/** When it failed on (un)registering. */
	public void onRegistrationFailure(RegistrationClient registration, NameAddress target, NameAddress contact, String result);

}
