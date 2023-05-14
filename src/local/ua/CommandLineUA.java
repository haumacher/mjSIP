/*
 * Copyright (C) 2005 Luca Veltri - University of Parma - Italy
 * 
 * This source code is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This source code is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this source code; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 * 
 * Author(s):
 * Luca Veltri (luca.veltri@unipr.it)
 */

package local.ua;



import local.media.MediaDesc;
import org.zoolu.sip.address.*;
import org.zoolu.sip.provider.SipStack;
import org.zoolu.sip.provider.SipProvider;
import org.zoolu.tools.ScheduledWork;
import org.zoolu.tools.Log;
import org.zoolu.tools.ExceptionPrinter;

import java.util.Vector;
import java.io.*;



/** Simple command-line-based SIP user agent (UA).
  * It includes audio/video applications.
  * <p>It can use external audio/video tools as media applications.
  * Currently only RAT (Robust Audio Tool) and VIC are supported as external applications.
  */
public class CommandLineUA implements UserAgentListener
{           

   /** Log */
   protected Log log;
   
   /** SipProvider. */
   protected SipProvider sip_provider;

   /** User Agent */
   protected UserAgent ua;

   /** UserAgentProfile */
   protected UserAgentProfile ua_profile;
         
   /** Standard input */
   BufferedReader stdin=null; 
         
   /** Standard output */
   PrintStream stdout=null; 

        
   // ************************* UA internal state *************************
     
   /** UA_IDLE=0 */
   protected static final String UA_IDLE="IDLE";
   /** UA_INCOMING_CALL=1 */
   protected static final String UA_INCOMING_CALL="INCOMING_CALL";
   /** UA_OUTGOING_CALL=2 */
   protected static final String UA_OUTGOING_CALL="OUTGOING_CALL";
   /** UA_ONCALL=3 */
   protected static final String UA_ONCALL="ONCALL";
   
   /** Call state: <P>UA_IDLE=0, <BR>UA_INCOMING_CALL=1, <BR>UA_OUTGOING_CALL=2, <BR>UA_ONCALL=3 */
   String call_state=UA_IDLE;
   

   /** Changes the call state */
   protected void changeStatus(String state)
   {  call_state=state;
      printLog("state: "+call_state,Log.LEVEL_MEDIUM); 
   }

   /** Checks the call state */
   protected boolean statusIs(String state)
   {  return call_state.equals(state); 
   }

   /** Gets the call state */
   protected String getStatus()
   {  return call_state; 
   }


   // *************************** Public methods **************************

   /** Costructs a UA with a default media port */
   public CommandLineUA(SipProvider sip_provider, UserAgentProfile ua_profile)
   {  this.sip_provider=sip_provider;
      this.ua_profile=ua_profile;
      log=sip_provider.getLog();
      ua=new UserAgent(sip_provider,ua_profile,this);      
      if (!ua_profile.no_prompt) stdin=new BufferedReader(new InputStreamReader(System.in)); 
      if (!ua_profile.no_prompt) stdout=System.out;
      run();
   }


   /** Becomes ready for receive a new incoming call. */
   public void readyToReceive()
   {  ua.printLog("WAITING FOR INCOMING CALL");
      if (!ua.ua_profile.audio && !ua.ua_profile.video) ua.printLog("ONLY SIGNALING, NO MEDIA");       
      //ua.listen();
      changeStatus(UA_IDLE);
      printOut("digit the callee's URL to make a call or press 'enter' to exit");
   } 


   /** Makes a new call */
   public void call(String target_url)
   {  ua.hangup();
      ua.printLog("CALLING "+target_url);
      if (!ua.ua_profile.audio && !ua.ua_profile.video) ua.printLog("ONLY SIGNALING, NO MEDIA");       
      ua.call(target_url);
      changeStatus(UA_OUTGOING_CALL);
   } 


   /** Accepts an incoming call */
   public void accept()
   {  ua.accept();
      changeStatus(UA_ONCALL);
      if (ua_profile.hangup_time>0) automaticHangup(ua_profile.hangup_time); 
      printOut("press 'enter' to hangup"); 
   } 


   /** Terminates a call */
   public void hangup()
   {  ua.hangup();
      changeStatus(UA_IDLE);
      if (ua.ua_profile.call_to!=null) exit();
      else readyToReceive();
   } 
   

   /** Starts the UA */
   void run()
   {
      try
      {  // Set the re-invite
         if (ua_profile.re_invite_time>0)
         {  reInvite(ua_profile.re_invite_time);
         }

         // Set the transfer (REFER)
         if (ua_profile.transfer_to!=null && ua_profile.transfer_time>0)
         {  callTransfer(ua_profile.transfer_to,ua_profile.transfer_time);
         }

         if (ua_profile.do_unregister_all)
         // ########## unregisters ALL contact URLs
         {  ua.printLog("UNREGISTER ALL contact URLs");
            ua.unregisterall();
         } 

         if (ua_profile.do_unregister)
         // unregisters the contact URL
         {  ua.printLog("UNREGISTER the contact URL");
            ua.unregister();
         } 

         if (ua_profile.do_register)
         // ########## registers the contact URL with the registrar server
         {  ua.printLog("REGISTRATION");
            ua.loopRegister(ua_profile.expires,ua_profile.expires/2,ua_profile.keepalive_time);
         }         
         
         if (ua_profile.call_to!=null)
         {  // UAC
            call(ua_profile.call_to.toString()); 
            printOut("press 'enter' to hangup");
            readLine();
            hangup();
         }
         else
         {  // UAS + UAC
            if (ua_profile.accept_time>=0) ua.printLog("AUTO ACCEPT MODE");
            readyToReceive();
            while (stdin!=null)
            {  String line=readLine();
               if (statusIs(UA_INCOMING_CALL))
               {  if (line.toLowerCase().startsWith("n"))
                  {  hangup();
                  }
                  else
                  {  accept();
                  }
               }
               else
               if (statusIs(UA_IDLE))
               {  if (line!=null && line.length()>0)
                  {  call(line);
                  }
                  else
                  {  exit();
                  }
               }
               else
               if (statusIs(UA_ONCALL))
               {  hangup();
               }
            }
         }
      }
      catch (Exception e)  {  e.printStackTrace(); System.exit(0);  }
   }


   /** Exits */
   public void exit()
   {  try {  Thread.sleep(1000);  } catch (Exception e) {}
      System.exit(0);
   }


   // ******************* UserAgent callback functions ******************

   /** When a new call is incoming */
   public void onUaIncomingCall(UserAgent ua, NameAddress callee, NameAddress caller, Vector media_descs)
   {  if (ua.ua_profile.redirect_to!=null) // redirect the call
      {  ua.redirect(ua.ua_profile.redirect_to);
         printOut("call redirected to "+ua.ua_profile.redirect_to);
      }         
      else
      if (ua.ua_profile.accept_time>=0) // automatically accept the call
      {  //accept();
         automaticAccept(ua.ua_profile.accept_time);
      }
      else         
      {  changeStatus(UA_INCOMING_CALL);
         printOut("incoming call from "+caller.toString());
         printOut("accept? [yes/no]");
      }
   }
   
   /** When an ougoing call is stated to be in progress */
   public void onUaCallProgress(UserAgent ua)
   {
   }

   /** When an ougoing call is remotly ringing */
   public void onUaCallRinging(UserAgent ua)
   {  
   }

   /** When an ougoing call has been accepted */
   public void onUaCallAccepted(UserAgent ua)
   {  changeStatus(UA_ONCALL);
      if (ua_profile.hangup_time>0) automaticHangup(ua_profile.hangup_time); 
   }
   
   /** When a call has been transferred */
   public void onUaCallTransferred(UserAgent ua)
   {  
   }

   /** When an incoming call has been cancelled */
   public void onUaCallCancelled(UserAgent ua)
   {  readyToReceive();
   }

   /** When an ougoing call has been refused or timeout */
   public void onUaCallFailed(UserAgent ua, String reason)
   {  if (ua.ua_profile.call_to!=null) exit();
      else readyToReceive();
   }

   /** When a call has been locally or remotely closed */
   public void onUaCallClosed(UserAgent ua)
   {  if (ua.ua_profile.call_to!=null) exit();
      else readyToReceive();     
   }

   /** When a new media session is started. */
   public void onUaMediaSessionStarted(UserAgent ua, String type, String codec)
   {  //printLog(type+" started "+codec);
   }

   /** When a media session is stopped. */
   public void onUaMediaSessionStopped(UserAgent ua, String type)
   {  //printLog(type+" stopped");
   }


   /** When registration succeeded. */
   public void onUaRegistrationSucceeded(UserAgent ua, String result)
   {  printLog("REGISTRATION SUCCESS: "+result); 
      printOut("UA: REGISTRATION SUCCESS: "+result);
   }

   /** When registration failed. */
   public void onUaRegistrationFailed(UserAgent ua, String result)
   {  printLog("REGISTRATION FAILURE: "+result); 
      printOut("UA: REGISTRATION FAILURE: "+result);
   }
   

   // ************************ scheduled events ************************

   /** Schedules a re-inviting after <i>delay_time</i> secs. It simply changes the contact address. */
   /*void reInvite(final NameAddress contact, final int delay_time)
   {  new ScheduledWork(delay_time*1000)
      {  public void doWork()
         {  printLog("AUTOMATIC RE-INVITING/MODIFING");
            ua.modify(contact,null);
         }
      };
   }*/
   /** Schedules a re-inviting after <i>delay_time</i> secs. It simply changes the contact address. */
   void reInvite(final int delay_time)
   {  printLog("AUTOMATIC RE-INVITING/MODIFING: "+delay_time+" secs"); 
      if (delay_time==0) ua.modify(null);
      else new ScheduledWork(delay_time*1000) {  public void doWork() {  ua.modify(null);  }  };
   }


   /** Schedules a call-transfer after <i>delay_time</i> secs. */
   /*void callTransfer(final NameAddress transfer_to, final int delay_time)
   {  new ScheduledWork(delay_time*1000)
      {  public void doWork()
         {  printLog("AUTOMATIC REFER/TRANSFER");
            ua.transfer(transfer_to);
         }
      };
   }*/
   /** Schedules a call-transfer after <i>delay_time</i> secs. */
   void callTransfer(final NameAddress transfer_to, final int delay_time)
   {  printLog("AUTOMATIC REFER/TRANSFER: "+delay_time+" secs");
      if (delay_time==0) ua.transfer(transfer_to);
      else new ScheduledWork(delay_time*1000) {  public void doWork() {  ua.transfer(transfer_to);  }  };
   }

   /** Schedules an automatic answer after <i>delay_time</i> secs. */
   /*void automaticAccept(final int delay_time)
   {  new ScheduledWork(delay_time*1000)
      {  public void doWork()
         {  printLog("AUTOMATIC ANSWER");
            accept();
         }
      };
   }*/
   /** Schedules an automatic answer after <i>delay_time</i> secs. */
   void automaticAccept(final int delay_time)
   {  printLog("AUTOMATIC ANSWER: "+delay_time+" secs");
      if (delay_time==0) accept();
      else new ScheduledWork(delay_time*1000) {  public void doWork() {  accept();  }  };
   }

   /** Schedules an automatic hangup after <i>delay_time</i> secs. */
   /*void automaticHangup(final int delay_time)
   {  new ScheduledWork(delay_time*1000)
      {  public void doWork()
         {  printLog("AUTOMATIC HANGUP");
            hangup();
         }
      };
   }*/
   /** Schedules an automatic hangup after <i>delay_time</i> secs. */
   void automaticHangup(final int delay_time)
   {  printLog("AUTOMATIC HANGUP: "+delay_time+" secs");
      if (delay_time==0) hangup();
      else new ScheduledWork(delay_time*1000) {  public void doWork() {  hangup();  }  };
   }


   // ******************************* Main ******************************

   /** The main method - It runs UA.main() with GUI disabled. */
   public static void main(String[] args)
   {
      System.out.println("MJSIP UA "+SipStack.version);

      if (!UA.init("local.ua.CommandLineUA",args)) System.exit(0);
      // else
      new CommandLineUA(UA.sip_provider,UA.ua_profile);
   }


   // ******************************* Logs ******************************

   /** Read a new line from stantard input. */
   protected String readLine()
   {  try { if (stdin!=null) return stdin.readLine(); } catch (IOException e) {}
      return null;
   }

   /** Print to stantard output. */
   protected void printOut(String str)
   {  if (stdout!=null) System.out.println(str);
   }

   /** Adds a new string to the default Log */
   void printLog(String str)
   {  printLog(str,Log.LEVEL_HIGH);
   }

   /** Adds a new string to the default Log */
   void printLog(String str, int level)
   {  if (log!=null) log.println("CommandLineUA: "+str,UserAgent.LOG_OFFSET+level);
   }

   /** Adds the Exception message to the default Log */
   void printException(Exception e,int level)
   {  printLog("Exception: "+ExceptionPrinter.getStackTraceOf(e),level);
   }

}
