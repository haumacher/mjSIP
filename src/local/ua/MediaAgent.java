/*
 * Copyright (C) 2008 Luca Veltri - University of Parma - Italy
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



import local.media.*;
import org.zoolu.tools.Archive;
import org.zoolu.tools.Log;
import org.zoolu.tools.ExceptionPrinter;

import java.util.Hashtable;



/** Media agent.
  * A media agent is used to start and stop multimedia sessions
  * (e.g. audio and/or video), by means of embedded media applications.
  */
public class MediaAgent
{           

   /** Log */
   Log log;
   
   /** Audio application */
   UserAgentProfile ua_profile;

   /** Active media applications, as table of: (String)media-->(MediaApp)media_app */
   Hashtable media_apps=new Hashtable();



   /** Creates a new MediaAgent. */
   public MediaAgent(UserAgentProfile ua_profile, Log log)
   {  this.ua_profile=ua_profile;
      this.log=log;

      // ################# patch to make audio working with javax.sound.. #################
      // currently ExtendedAudioSystem must be initialized before any AudioClipPlayer is initialized..
      // this is caused by a problem with the definition of the audio format
      if (!ua_profile.use_rat && !ua_profile.use_jmf_audio)
      {  if (ua_profile.audio && !ua_profile.loopback && ua_profile.send_file==null && !ua_profile.recv_only && !ua_profile.send_tone) org.zoolu.sound.ExtendedAudioSystem.initAudioInputLine();
         if (ua_profile.audio && !ua_profile.loopback && ua_profile.recv_file==null && !ua_profile.send_only) org.zoolu.sound.ExtendedAudioSystem.initAudioOutputLine();
      }
   }

   
   /** Starts a media session */
   public boolean startMediaSession(FlowSpec flow_spec)
   {  printLog("start("+flow_spec.getMediaSpec()+")");
      printLog("new flow: "+flow_spec.getLocalPort()+((flow_spec.getDirection()==FlowSpec.SEND_ONLY)? "=-->" : ((flow_spec.getDirection()==FlowSpec.RECV_ONLY)? "<--=" : "<-->" ))+flow_spec.getRemoteAddress()+":"+flow_spec.getRemotePort());

      String media=flow_spec.getMediaSpec().getType();
      
      // stop previous media_app (just in case something was wrong..)
      if (media_apps.containsKey(media))
      {  ((MediaApp)media_apps.get(media)).stopApp();
         media_apps.remove(media);
      }
       
      // start new media_app
      MediaApp media_app=null;

      if (ua_profile.loopback) media_app=new LoopbackMediaApp(flow_spec,log);
      else
      if (flow_spec.getMediaSpec().getType().equals("audio")) media_app=newAudioApp(flow_spec);
      else
      if (flow_spec.getMediaSpec().getType().equals("video")) media_app=newVideoApp(flow_spec);
      else
      if (flow_spec.getMediaSpec().getType().equals("ptt")) media_app=new Push2TalkApp(flow_spec,log);

      if (media_app!=null)
      {  if (media_app.startApp())
         {  media_apps.put(media,media_app);
            return true;
         }
         else return false;
      }
      else
      {  printLog("WARNING: no "+media+" application has been found: "+media+" not started",Log.LEVEL_HIGH);
         return false;
      }
   }
 
   
   /** Stops a media session.  */
   public void stopMediaSession(String media)
   {  printLog("stop("+media+")");

      if (media_apps.containsKey(media))
      {  ((MediaApp)media_apps.get(media)).stopApp();
         media_apps.remove(media);
      }
      else
      {  printLog("WARNING: no running "+media+" application has been found.",Log.LEVEL_HIGH);
      }
   }


   // ********************** media applications *********************

   /** Creates a new audio application. */
   private MediaApp newAudioApp(FlowSpec audio_flow)
   {
      MediaApp audio_app=null;
      
      if (ua_profile.use_rat)
      {  // use RAT
         if (ua_profile.audio_mcast_soaddr!=null) audio_flow=new FlowSpec(audio_flow.getMediaSpec(),ua_profile.audio_mcast_soaddr.getPort(),ua_profile.audio_mcast_soaddr.getAddress().toString(),ua_profile.audio_mcast_soaddr.getPort(),audio_flow.getDirection());
         audio_app=new RatAudioApp(ua_profile.bin_rat,audio_flow,log);
      }
      else 
      if (ua_profile.use_jmf_audio)
      {  // use JMF audio app
         try
         {  String audio_source=(ua_profile.send_file!=null)? Archive.getFileURL(ua_profile.send_file).toString() : null;
            if (ua_profile.recv_file!=null) printLog("WARNING: file destination is not supported with JMF audio",Log.LEVEL_HIGH);
            Class mediaapp_class=Class.forName("local.media.jmf.JmfMediaApp");
            Class[] param_types={ FlowSpec.class, String.class, Log.class };
            Object[] param_values={ audio_flow, audio_source, log };
            java.lang.reflect.Constructor mediaapp_constructor=mediaapp_class.getConstructor(param_types);
            audio_app=(MediaApp)mediaapp_constructor.newInstance(param_values);
         }
         catch (Exception e)
         {  printException(e,Log.LEVEL_HIGH);
            printLog("ERROR trying to create the JmfMediaApp",Log.LEVEL_HIGH);
         }
      }
      // else
      if (audio_app==null)
      {  // use embedded javax-based audio app
      
         // audio input
         String audio_in=null;
         if (ua_profile.send_tone) audio_in=AudioApp.TONE;
         else
         if (ua_profile.send_file!=null) audio_in=ua_profile.send_file;
         // audio output
         String audio_out=null;
         if (ua_profile.recv_file!=null) audio_out=ua_profile.recv_file;        

         // javax-based audio app
         if (ua_profile.javax_sound_app==null)
         {  // standard javax-based audio application
            audio_app=new AudioApp(audio_flow,audio_in,audio_out,ua_profile.javax_sound_direct_convertion,ua_profile.javax_sound_sync,ua_profile.random_early_drop_rate,ua_profile.symmetric_rtp,log);
         }
         else
         {  // alternative audio application (just for experimental uses)
            try
            {  Class mediaapp_class=Class.forName(ua_profile.javax_sound_app);
               Class[] param_types={ FlowSpec.class, Log.class };
               Object[] param_values={ audio_flow, log };
               java.lang.reflect.Constructor mediaapp_constructor=mediaapp_class.getConstructor(param_types);
               audio_app=(MediaApp)mediaapp_constructor.newInstance(param_values);
            }
            catch (Exception e)
            {  printException(e,Log.LEVEL_HIGH);
               printLog("ERROR trying to create audio application '"+ua_profile.javax_sound_app+"'",Log.LEVEL_HIGH);
            }
         }
      }
      return audio_app;
   }


   /** Creates a new video application. */
   private MediaApp newVideoApp(FlowSpec video_flow)
   {
      MediaApp video_app=null;

      if (ua_profile.use_vic)
      {  // use VIC
         if (ua_profile.video_mcast_soaddr!=null) video_flow=new FlowSpec(video_flow.getMediaSpec(),ua_profile.video_mcast_soaddr.getPort(),ua_profile.video_mcast_soaddr.getAddress().toString(),ua_profile.video_mcast_soaddr.getPort(),video_flow.getDirection());
         video_app=new VicVideoApp(ua_profile.bin_vic,video_flow,log);
      }
      else 
      if (ua_profile.use_jmf_video)
      {  // use JMF video app
         try
         {  String video_source=(ua_profile.send_video_file!=null)? Archive.getFileURL(ua_profile.send_video_file).toString() : null;
            if (ua_profile.recv_video_file!=null) printLog("WARNING: file destination is not supported with JMF video",Log.LEVEL_HIGH);
            Class mediaapp_class=Class.forName("local.media.jmf.JmfMediaApp");
            Class[] param_types={ FlowSpec.class, String.class, Log.class };
            Object[] param_values={ video_flow, video_source, log };
            java.lang.reflect.Constructor mediaapp_constructor=mediaapp_class.getConstructor(param_types);
            video_app=(MediaApp)mediaapp_constructor.newInstance(param_values);
         }
         catch (Exception e)
         {  printException(e,Log.LEVEL_HIGH);
            printLog("ERROR trying to create the JmfMediaApp",Log.LEVEL_HIGH);
         }
      }
      return video_app;
   }


   // ***************************** logs ****************************

   /** Adds a new string to the default Log */
   private void printLog(String str)
   {  printLog(str,Log.LEVEL_HIGH);
   }

   /** Adds a new string to the default Log */
   private void printLog(String str, int level)
   {  if (log!=null) log.println("MediaAgent: "+str,UserAgent.LOG_OFFSET+level);
   }

   /** Adds the Exception message to the default Log */
   private final void printException(Exception e, int level)
   {  printLog("Exception: "+ExceptionPrinter.getStackTraceOf(e),level);
   }


}
