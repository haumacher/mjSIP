/*
 * Copyright (C) 2006 Luca Veltri - University of Parma - Italy
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



import org.zoolu.net.SocketAddress;
import org.zoolu.sip.address.*;
import org.zoolu.sip.provider.*;

import java.io.PrintStream;



/** Simple SIP UA (User Agent) that can be executed with a GUI (it runs a GraphicalUA)
  * or with a command-line interface (it runs a CommandLineUA).
  * <p/>
  * Class UA allows the user to set several configuration parameters
  * directly from comman-line before starting the proper UA (GraphicalUA or CommandLineUA).
  */
public class UA
{

   /** Whether using a GUI */ 
   public static boolean use_gui=true;

   /** Print stream */ 
   public static PrintStream stdout=System.out;

   /** Configuration file */ 
   public static String file=null;

   /** SipProvider */ 
   public static SipProvider sip_provider=null;

   /** UserAgentProfile */ 
   public static UserAgentProfile ua_profile=null;



   /** Parses command-line options and inits the SIP stack, a SIP provider and an UA profile. */
   public static boolean init(String program, String[] args)
   {
      Boolean opt_enable=Boolean.TRUE;
      Boolean opt_unregist=null;
      Boolean opt_unregist_all=null;
      int     opt_regist_time=-1;
      long    opt_keepalive_time=-1;
      Boolean opt_no_offer=null;
      String  opt_call_to=null;      
      int     opt_accept_time=-1;      
      int     opt_hangup_time=-1;
      String  opt_redirect_to=null;
      String  opt_transfer_to=null;
      int     opt_transfer_time=-1;
      int     opt_re_invite_time=-1;
      Boolean opt_audio=null;
      Boolean opt_video=null;
      int     opt_media_port=0;
      Boolean opt_loopback=null;
      Boolean opt_recv_only=null;
      Boolean opt_send_only=null;
      Boolean opt_send_tone=null;
      String  opt_send_file=null;
      String  opt_recv_file=null;
      String  opt_send_video_file=null;
      String  opt_recv_video_file=null;
      Boolean opt_no_prompt=null;

      String opt_from_url=null;
      String opt_contact_url=null;
      String opt_display_name=null;
      String opt_user=null;
      String opt_proxy=null;
      String opt_registrar=null;
      String opt_auth_user=null;
      String opt_auth_realm=null;
      String opt_auth_passwd=null;

      int opt_debug_level=-1;
      String opt_log_path=null;
      String opt_outbound_proxy=null;
      String opt_via_addr=SipProvider.AUTO_CONFIGURATION;
      int opt_host_port=SipStack.default_port;

      try
      {
         for (int i=0; i<args.length; i++)
         {
            if (args[i].equals("--skip")) // skip this option
            {  continue;
            }
            if (args[i].equals("--gui"))
            {  use_gui=opt_enable.booleanValue();
               opt_enable=Boolean.TRUE;
               continue;
            }
            if (args[i].equals("--no-gui"))
            {  use_gui=!opt_enable.booleanValue();
               opt_enable=Boolean.TRUE;
               continue;
            }
            if (args[i].equals("-f") && args.length>(i+1))
            {  file=args[++i];
               continue;
            }
            if (args[i].equals("!") || args[i].equals("--not"))
            {  opt_enable=Boolean.FALSE;
               continue;
            }
            if (args[i].equals("-u")) // unregister its contact address
            {  opt_unregist=opt_enable;
               opt_enable=Boolean.TRUE;
               continue;
            }
            if (args[i].equals("-z")) // unregister all its contact addresses
            {  opt_unregist_all=opt_enable;
               opt_enable=Boolean.TRUE;
               continue;
            }
            if (args[i].equals("-g") && args.length>(i+1)) // register its contact address with a registrar server
            {  String time=args[++i];
               if (time.charAt(time.length()-1)=='h') opt_regist_time=Integer.parseInt(time.substring(0,time.length()-1))*3600;
               else opt_regist_time=Integer.parseInt(time);
               continue;
            }
            if (args[i].equals("-n")) // no offer in the invite
            {  opt_no_offer=opt_enable;
               opt_enable=Boolean.TRUE;
               continue;
            }
            if (args[i].equals("-c") && args.length>(i+1)) // make a call with a remote user (url)
            {  opt_call_to=args[++i];
               continue;
            }
            if (args[i].equals("-y") && args.length>(i+1)) // set automatic accept time
            {  opt_accept_time=Integer.parseInt(args[++i]);
               continue;
            }
            if (args[i].equals("-t") && args.length>(i+1)) // set the call duration
            {  opt_hangup_time=Integer.parseInt(args[++i]);
               continue;
            }
            if (args[i].equals("-i") && args.length>(i+1)) // set the re-invite time
            {  opt_re_invite_time=Integer.parseInt(args[++i]);
               continue;
            }
            if (args[i].equals("-r") && args.length>(i+1)) // redirect the call to a new url
            {  opt_accept_time=0;
               opt_redirect_to=args[++i];
               continue;
            }
            if (args[i].equals("-q") && args.length>(i+1)) // transfers the call to a new user (REFER)
            {  opt_transfer_to=args[++i];
               opt_transfer_time=Integer.parseInt(args[++i]);
               continue;
            }
            if (args[i].equals("-a")) // use audio
            {  opt_audio=opt_enable;
               opt_enable=Boolean.TRUE;
               continue;
            }
            if (args[i].equals("-v")) // use video
            {  opt_video=opt_enable;
               opt_enable=Boolean.TRUE;
               continue;
            }
            if (args[i].equals("-m") && args.length>(i+1)) // set the local media port
            {  opt_media_port=Integer.parseInt(args[++i]);
               continue;
            }
            if (args[i].equals("-o") && args.length>(i+1)) // outbound proxy
            {  opt_outbound_proxy=args[++i];
               continue;
            }
            if (args[i].equals("--via-addr") && args.length>(i+1)) // via addr
            {  opt_via_addr=args[++i];
               continue;
            }
            if (args[i].equals("-p") && args.length>(i+1)) // set the local sip port
            {  opt_host_port=Integer.parseInt(args[++i]);
               continue;
            }
            if (args[i].equals("--keep-alive") && args.length>(i+1)) // keep-alive
            {  opt_keepalive_time=Integer.parseInt(args[++i]);
               continue;
            }
            if (args[i].equals("--from-url") && args.length>(i+1)) // user's AOR
            {  opt_from_url=args[++i];
               continue;
            }
            if (args[i].equals("--contact-url") && args.length>(i+1)) // user's contact_url
            {  opt_contact_url=args[++i];
               continue;
            }
            if (args[i].equals("--display-name") && args.length>(i+1)) // username
            {  opt_display_name=args[++i];
               continue;
            }
            if (args[i].equals("--user") && args.length>(i+1)) // username
            {  opt_user=args[++i];
               continue;
            }
            if (args[i].equals("--proxy") && args.length>(i+1)) // username
            {  opt_proxy=args[++i];
               continue;
            }
            if (args[i].equals("--registrar") && args.length>(i+1)) // username
            {  opt_registrar=args[++i];
               continue;
            }
            if (args[i].equals("--auth-user") && args.length>(i+1)) // username
            {  opt_auth_user=args[++i];
               continue;
            }
            if (args[i].equals("--auth-realm") && args.length>(i+1)) // realm
            {  opt_auth_realm=args[++i];
               continue;
            }
            if (args[i].equals("--auth-passwd") && args.length>(i+1)) // passwd
            {  opt_auth_passwd=args[++i];
               continue;
            }
            if (args[i].equals("--loopback")) // loopback mode
            {  opt_loopback=opt_enable;
               opt_enable=Boolean.TRUE;
               continue;
            }
            if (args[i].equals("--recv-only")) // receive only mode
            {  opt_recv_only=opt_enable;
               opt_enable=Boolean.TRUE;
               continue;
            }
            if (args[i].equals("--send-only")) // send only mode
            {  opt_send_only=opt_enable;
               opt_enable=Boolean.TRUE;
               continue;
            }
            if (args[i].equals("--send-tone"))
            {  opt_send_tone=opt_enable;
               opt_enable=Boolean.TRUE;
               continue;
            }
            if (args[i].equals("--send-file") && args.length>(i+1)) // send audio file
            {  opt_send_file=args[++i];
               continue;
            }
            if (args[i].equals("--recv-file") && args.length>(i+1)) // receive audio file
            {  opt_recv_file=args[++i];
               continue;
            }
            if ((args[i].equals("--send-video-file") || args[i].equals("--send-vfile")) && args.length>(i+1)) // send video file
            {  opt_send_video_file=args[++i];
               continue;
            }
            if ((args[i].equals("--recv-video-file") || args[i].equals("--recv-vfile")) && args.length>(i+1)) // receive video file
            {  opt_recv_video_file=args[++i];
               continue;
            }
            if (args[i].equals("--debug-level") && args.length>(i+1)) // debug level
            {  opt_debug_level=Integer.parseInt(args[++i]);
               continue;
            }
            if (args[i].equals("--log-path") && args.length>(i+1)) // log path
            {  opt_log_path=args[++i];
               continue;
            }
            if (args[i].equals("--no-prompt")) // do not prompt
            {  opt_no_prompt=opt_enable;
               opt_enable=Boolean.TRUE;
               continue;
            }
            // else, do:
            if (!args[i].equals("-h"))
               System.out.println("unrecognized param '"+args[i]+"' or missing next param\n");

            printOut("usage:\n   java "+program+" [options]");
            printOut("   options:");
            printOut("   -h              this help");
            printOut("   -f <file>       specifies a configuration file");
            printOut("    ! or --not     inverts the next option (e.g. ! -a means no audio)");
            printOut("   -g <time>       registers the contact address with the registrar server");
            printOut("                   where time is the duration of the registration, and can be");
            printOut("                   in seconds (default) or hours (-g 7200 is the same as -g 2h)");
            printOut("   -u              unregisters the contact address with the registrar server");
            printOut("                   (is the same as -g 0)");
            printOut("   -z              unregisters ALL contact addresses");
            printOut("   -n              no offer in invite (offer/answer in 2xx/ack)");
            printOut("   -c <call_to>    calls a remote user");
            printOut("   -y <secs>       auto answer time");
            printOut("   -t <secs>       auto hangup time (0 means manual hangup)");
            printOut("   -i <secs>       re-invite after <secs> seconds");
            printOut("   -r <url>        redirects the call to new user <url>");
            printOut("   -q <url> <secs> transfers the call to <url> after <secs> seconds");
            printOut("   -a              audio");
            printOut("   -v              video");
            printOut("   -m <port>       (first) local media port");
            printOut("   -p <port>       local SIP port, used ONLY without -f option");
            printOut("   -o <addr>[:<port>]  use the specified outbound proxy");
            printOut("   --via-addr <addr>   host via address, used ONLY without -f option");
            printOut("   --keep-alive <millisecs>   send keep-alive packets each <millisecs>");
            printOut("   --from-url <url>    user's address-of-record (AOR)");
            printOut("   --contact-url <url> user's contact URL");
            printOut("   --display-name <str>    display name");
            printOut("   --user <user>           user name");
            printOut("   --proxy <proxy>         proxy server");
            printOut("   --registrar <registrar> registrar server");
            printOut("   --auth-user <user>      user name used for authentication");
            printOut("   --auth-realm <realm>    realm used for authentication");
            printOut("   --auth-passwd <passwd>  passwd used for authentication");
            printOut("   --loopback          loopback mode, received media are sent back to the remote sender");
            printOut("   --recv-only         receive only mode, no media is sent");
            printOut("   --send-only         send only mode, no media is received");
            printOut("   --send-tone         send only mode, an audio test tone is generated");
            printOut("   --send-file <file>  audio is played from the specified file");
            printOut("   --recv-file <file>  audio is recorded to the specified file");
            printOut("   --debug-level <n>   debug level (level=0 means no log)");
            printOut("   --log-path <path>   base path for all logs (./log is the default value)");
            printOut("   --no-prompt         do not prompt");
            return false;
         }

         // init SipStack
         SipStack.init(file);
         if (opt_debug_level>=0) SipStack.debug_level=opt_debug_level;
         if (opt_log_path!=null) SipStack.log_path=opt_log_path;

         // init sip_provider
         if (file!=null) sip_provider=new SipProvider(file); else sip_provider=new SipProvider(opt_via_addr,opt_host_port);
         if (opt_outbound_proxy!=null) sip_provider.setOutboundProxy(new SipURL(opt_outbound_proxy));

         // init ua_profile
         ua_profile=new UserAgentProfile(file);
         if (opt_no_prompt!=null) ua_profile.no_prompt=opt_no_prompt.booleanValue();
         if (ua_profile.no_prompt) ;

         if (opt_unregist!=null) ua_profile.do_unregister=opt_unregist.booleanValue();
         if (opt_unregist_all!=null) ua_profile.do_unregister_all=opt_unregist_all.booleanValue();
         if (opt_regist_time>=0) {  ua_profile.do_register=true;  ua_profile.expires=opt_regist_time;  }
         if (opt_keepalive_time>=0) ua_profile.keepalive_time=opt_keepalive_time;
         if (opt_no_offer!=null) ua_profile.no_offer=opt_no_offer.booleanValue();
         if (opt_call_to!=null) ua_profile.call_to=new NameAddress(opt_call_to);
         if (opt_redirect_to!=null) ua_profile.redirect_to=new NameAddress(opt_redirect_to);
         if (opt_transfer_to!=null) ua_profile.transfer_to=new NameAddress(opt_transfer_to);
         if (opt_accept_time>=0) ua_profile.accept_time=opt_accept_time;
         if (opt_hangup_time>0) ua_profile.hangup_time=opt_hangup_time;
         if (opt_re_invite_time>0) ua_profile.re_invite_time=opt_re_invite_time;
         if (opt_transfer_time>0) ua_profile.transfer_time=opt_transfer_time;
         if (opt_audio!=null) ua_profile.audio=opt_audio.booleanValue();
         if (opt_video!=null) ua_profile.video=opt_video.booleanValue();
         if (opt_media_port>0) ua_profile.media_port=opt_media_port;

         if (opt_display_name!=null) ua_profile.display_name=opt_display_name;
         if (opt_user!=null) ua_profile.user=opt_user;
         if (opt_proxy!=null) ua_profile.proxy=opt_proxy;
         if (opt_registrar!=null) ua_profile.registrar=opt_registrar;   
         if (opt_auth_user!=null) ua_profile.auth_user=opt_auth_user;
         if (opt_auth_realm!=null) ua_profile.auth_realm=opt_auth_realm;
         if (opt_auth_passwd!=null) ua_profile.auth_passwd=opt_auth_passwd; 
         
         if (opt_loopback!=null) ua_profile.loopback=opt_loopback.booleanValue();
         if (opt_recv_only!=null) ua_profile.recv_only=opt_recv_only.booleanValue();
         if (opt_send_only!=null) ua_profile.send_only=opt_send_only.booleanValue();             
         if (opt_send_tone!=null) ua_profile.send_tone=opt_send_tone.booleanValue();
         if (opt_send_file!=null) ua_profile.send_file=opt_send_file;
         if (opt_recv_file!=null) ua_profile.recv_file=opt_recv_file;
         if (opt_send_video_file!=null) ua_profile.send_video_file=opt_send_video_file;
         if (opt_recv_video_file!=null) ua_profile.recv_video_file=opt_recv_video_file;
         
         // for backward compatibily
         if (opt_from_url!=null) ua_profile.setUserURI(new NameAddress(opt_from_url));

         // use audio as default media in case of..
         if ((opt_recv_only!=null || opt_send_only!=null || opt_send_tone!=null || opt_send_file!=null || opt_recv_file!=null) && opt_video==null) ua_profile.audio=true;

         return true;
      }
      catch (Exception e)
      {
         e.printStackTrace();
         return false;
      }
   }


   /** Prints out a message to stantard output. */
   static void printOut(String str)
   {  if (stdout!=null) stdout.println(str);
   }


   /** The main method. */
   public static void main(String[] args)
   {
      printOut("MJSIP UA "+SipStack.version);

      if (!init("local.ua.UA",args)) System.exit(0);
      // else
      if (use_gui) new GraphicalUA(sip_provider,ua_profile);
      else new CommandLineUA(sip_provider,ua_profile);
   }

}
