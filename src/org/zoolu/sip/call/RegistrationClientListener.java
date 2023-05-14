package org.zoolu.sip.call;


import org.zoolu.sip.address.NameAddress;
import org.zoolu.sip.message.Message;


/** Listener of RegistrationClient */
public interface RegistrationClientListener
{
   /** When it has been successfully (un)registered. */
   public void onRegistrationSuccess(RegistrationClient regist, NameAddress target, NameAddress contact, String result);

   /** When it failed on (un)registering. */
   public void onRegistrationFailure(RegistrationClient regist, NameAddress target, NameAddress contact, String result);

}
