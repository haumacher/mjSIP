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



import local.media.MediaDesc;
import org.zoolu.sip.address.NameAddress;
import org.zoolu.sip.provider.SipStack;
import org.zoolu.sip.provider.SipProvider;

import java.util.Vector;
import java.io.File;



/** Jukebox is a simple audio server.
  * It automatically responds to incoming calls and sends the audio file
  * as selected by the caller through the request-line parameter 'audiofile'.
  */
public class Jukebox extends MultipleUAS
{           

   /** URI resource parameter */
   public static String PARAM_RESOURCE="resource";
   
   /** Default available ports */
   public static int MEDIA_PORTS=20;

   /** Maximum life time (call duration) in seconds */
   public static int MAX_LIFE_TIME=600;

   /** Media file path */
   public static String MEDIA_PATH=".";

   /** First media port */
   int first_media_port;

   /** Last media port */
   int last_media_port;



   /** Creates a new Jukebox. */
   public Jukebox(SipProvider sip_provider, UserAgentProfile ua_profile, int media_ports)
   {  super(sip_provider,ua_profile);
      if (ua_profile.media_port<0) ua_profile.media_port=((MediaDesc)ua_profile.media_descs.elementAt(0)).getPort();
      first_media_port=ua_profile.media_port;
      last_media_port=first_media_port+media_ports-1;
   } 


   /** From UserAgentListener. When a new call is incoming. */
   public void onUaIncomingCall(UserAgent ua, NameAddress callee, NameAddress caller, Vector media_descs)
   {  String audio_file=MEDIA_PATH+"/"+callee.getAddress().getParameter(PARAM_RESOURCE);
      if (audio_file!=null) if (new File(audio_file).isFile()) ua_profile.send_file=audio_file;
      if (ua_profile.send_file!=null) ua.accept(); else ua.hangup();
      if ((++ua_profile.media_port)>last_media_port) ua_profile.media_port=first_media_port;
   }
   

   /** Prints out a message to stantard output. */
   void printOut(String str)
   {  if (stdout!=null) stdout.println(str);
   }


   /** The main method. */
   public static void main(String[] args)
   {         
      System.out.println("Jukebox "+SipStack.version);
      SipStack.debug_level=8;

      int media_ports=MEDIA_PORTS;
      boolean prompt_exit=false;

      for (int i=0; i<args.length; i++)
      {  if (args[i].equals("--mports"))
         {  try
            {  media_ports=Integer.parseInt(args[i+1]);
               args[i]="--skip";
               args[++i]="--skip";
            }
            catch (Exception e) {  e.printStackTrace();  }
         }
         else
         if (args[i].equals("--mpath"))
         {  MEDIA_PATH=args[i+1];
            args[i]="--skip";
            args[++i]="--skip";
         }
         else
         if (args[i].equals("--prompt"))
         {  prompt_exit=true;
            args[i]="--skip";
         }
      }
      if (!UA.init("Jukebox",args))
      {  UA.printOut("   --mports            number of available media ports");
         UA.printOut("   --mpath <path>      path of media folder");
         UA.printOut("   --prompt            prompt for exit");
         return;
      }
      // else
      UA.ua_profile.audio=true;
      UA.ua_profile.video=false;
      UA.ua_profile.send_only=true;
      if (UA.ua_profile.hangup_time<=0) UA.ua_profile.hangup_time=MAX_LIFE_TIME;
      new Jukebox(UA.sip_provider,UA.ua_profile,media_ports);
      
      // promt before exit
      if (prompt_exit) 
      try
      {  System.out.println("press 'enter' to exit");
         (new java.io.BufferedReader(new java.io.InputStreamReader(System.in))).readLine();
         System.exit(0);
      }
      catch (Exception e) {}
   }    

}
