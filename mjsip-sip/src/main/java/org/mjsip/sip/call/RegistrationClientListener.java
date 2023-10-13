package org.mjsip.sip.call;



import org.mjsip.sip.address.NameAddress;



/** Listener of RegistrationClient */
public interface RegistrationClientListener {
	
	/** When it has been successfully (un)registered. */
	public void onRegistrationSuccess(RegistrationClient regist, NameAddress target, NameAddress contact, int expires, String result);

	/** When it failed on (un)registering. */
	public void onRegistrationFailure(RegistrationClient regist, NameAddress target, NameAddress contact, String result);

}
