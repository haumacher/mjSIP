/*
 * Copyright (C) 2007 Luca Veltri - University of Parma - Italy
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



import org.zoolu.sip.address.NameAddress;
import org.zoolu.sip.provider.SipStack;
import org.zoolu.sip.provider.SipProvider;
import java.io.File;



/** Jukebox is a simple audio server.
  * It automatically responds to incoming calls and sends the audio file
  * as selected by the caller through the request-line parameter 'audiofile'.
  * <p/>
  * Note that it is sigle-call UA, that is it serves ONE call at a time.
  */
public class MiniJukebox extends CommandLineUA
{           

   /** URI resource parameter */
   public static String PARAM_RESOURCE="resource";
   
   /** Maximum life time (call duration) in seconds */
   public static int MAX_LIFE_TIME=600;



   /** Creates a new MiniJukebox. */
   public MiniJukebox(SipProvider sip_provider, UserAgentProfile user_profile)
   {  super(sip_provider,user_profile);
   }


   /** From UserAgentListener. When a new call is incoming */
   public void onUaIncomingCall(UserAgent ua, NameAddress callee, NameAddress caller)
   {  printOut("incoming call from "+caller.toString());
      String audio_file=callee.getAddress().getParameter(PARAM_RESOURCE);
      if (audio_file!=null)
      {  if (new File(audio_file).isFile())
         {  ua_profile.send_file=audio_file;
         }
      }
      if (ua_profile.send_file!=null) ua.accept();      
      else ua.hangup();
   }

   
   /** The main method. */
   public static void main(String[] args)
   {         
      System.out.println("MiniJukebox"+SipStack.version);

      if (!UA.init("MiniJukebox",args)) return;
      // else
      UA.ua_profile.audio=true;
      UA.ua_profile.video=false;
      UA.ua_profile.send_only=true;
      if (UA.ua_profile.hangup_time<=0) UA.ua_profile.hangup_time=MAX_LIFE_TIME;
      new MiniJukebox(UA.sip_provider,UA.ua_profile);
   }    
   
}
