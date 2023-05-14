package local.ua;



import local.media.MediaDesc;
import org.zoolu.sip.address.NameAddress;

import java.util.Vector;



/** Listener of UserAgent */
public interface UserAgentListener
{
   /** When registration succeeded. */
   public void onUaRegistrationSucceeded(UserAgent ua, String result);

   /** When registration failed. */
   public void onUaRegistrationFailed(UserAgent ua, String result);

   /** When a new call is incoming */
   public void onUaIncomingCall(UserAgent ua, NameAddress callee, NameAddress caller, Vector media_descs);
   
   /** When an incoming call is cancelled */
   public void onUaCallCancelled(UserAgent ua);

   /** When an ougoing call is stated to be in progress */
   public void onUaCallProgress(UserAgent ua);

   /** When an ougoing call is remotly ringing */
   public void onUaCallRinging(UserAgent ua);
   
   /** When an ougoing call has been accepted */
   public void onUaCallAccepted(UserAgent ua);
   
   /** When a call has been transferred */
   public void onUaCallTransferred(UserAgent ua);

   /** When an ougoing call has been refused or timeout */
   public void onUaCallFailed(UserAgent ua, String reason);

   /** When a call has been locally or remotly closed */
   public void onUaCallClosed(UserAgent ua);


   /** When a new media session is started */
   public void onUaMediaSessionStarted(UserAgent ua, String type, String codec);

   /** When a media session is stopped */
   public void onUaMediaSessionStopped(UserAgent ua, String type);

}