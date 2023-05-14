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

package local.conference;


import org.zoolu.sip.call.*;
import org.zoolu.sip.address.*;
import org.zoolu.sip.provider.*;
import org.zoolu.sip.message.*;
import org.zoolu.sip.call.*;
import org.zoolu.sdp.*;
import org.zoolu.tools.Log;
import org.zoolu.tools.Parser;

import java.util.Hashtable;
import java.util.HashSet;
import java.util.Enumeration;
import java.util.Vector;


/** The conference Focus can be used used to create and control SIP conferences.
  * <br>It acts as SIP MCU (Multipoint Control Unit).
  * It includes the MC (Multipoint Controller) -
  * i.e. the 3GPP-MRFC (Multimedia Resource Function Controller) -
  * <br>and creates and controls the MP (Multipoint Processor) - 
  * i.e. the 3GPP-MRFP (Multimedia Resource Function Processor). */
public class Focus extends CallListenerAdapter 
{  
   /** default audio avp */
   private static final int AUDIO_AVP=0;
   /** default audio codec */
   private static final String AUDIO_CODEC="PCMU";
   /** default audio rate */
   private static final int AUDIO_RATE=8000; 
   // default video avp
   private static final int video_avp=17;

   /** Log */
   protected Log log=null;

   /** The audio Multipoint Processor Unit (MPU) (i.e. audio part of the 3GPP Multimedia Resource Function Processor - MRFP) */
   protected MultimediaResourceProcessor audio_mrfp;

   /** The video Multipoint Processor Unit (MPU) (i.e. video part of 3GPP Multimedia Resource Function Processor - MRFP) */
   protected MultimediaResourceProcessor video_mrfp;

   /** The audio  Processor Unit (PU) */
   protected AudioMixer audio_mixer;

   /** Active calls and corresponding local ports */   
   Hashtable calls;

   /** Available media ports */
   HashSet media_ports;
   
   /** Whether to use the same caller media port */
   boolean port_mirroring=false;

   /** Whether mixing audio streams. */
   boolean audio_mixing=false;

   /** Whether using symmetric_rtp. */
   boolean symmetric_rtp=false;

   /** Focus contact url */
   String focus_contact;

   /** SipProvider */
   SipProvider sip_provider;

   /** Focus SDP without media descriptor */
   SessionDescriptor focus_sdp;
   /** Focus SDP connection field */
   ConnectionField focus_conn;
   /** Focus SDP audio */
   protected MediaDescriptor focus_audio=null;
   /** Focus SDP video */
   protected MediaDescriptor focus_video=null;



   /** Creates a new Focus specifing the via address, host port, tranpsort protocols, and the set of available media ports. */
   public Focus(String address, int port, String[] protocols, HashSet media_ports, boolean symmetric_rtp)
   {  SipProvider sip_provider=new SipProvider(address,port,protocols,null);
      init(sip_provider,media_ports,symmetric_rtp);
   }  


   /** Creates a new Focus specifing the SipProvider and the set of available media ports. */
   public Focus(SipProvider sip_provider, HashSet media_ports, boolean symmetric_rtp)
   {  init(sip_provider,media_ports,symmetric_rtp);
   }  


   /** Inits the Focus. */
   private void init(SipProvider sip_provider, HashSet media_ports, boolean symmetric_rtp)
   {  this.sip_provider=sip_provider;
      log=sip_provider.getLog();
      printLog("creating a new focus",Log.LEVEL_HIGH);
      // create a new SipProvider on server_port
      focus_contact="sip:focus@"+sip_provider.getViaAddress()+":"+sip_provider.getPort();
      calls=new Hashtable();
      this.media_ports=media_ports;
      this.symmetric_rtp=symmetric_rtp;
      printLog("available media ports: "+media_ports.size(),Log.LEVEL_HIGH); 
      printLog("audio mixing: "+audio_mixing,Log.LEVEL_HIGH); 
      printLog("port mirroring: "+port_mirroring,Log.LEVEL_HIGH); 
      printLog("symmetric RTP: "+symmetric_rtp,Log.LEVEL_HIGH); 
      audio_mrfp=new MultimediaResourceProcessor(symmetric_rtp,log); 
      video_mrfp=new MultimediaResourceProcessor(symmetric_rtp,log); 
      audio_mixer=new AudioMixer(log);     
      // create the focus sdp without audio and video descriptors
      focus_sdp=new SessionDescriptor(focus_contact,sip_provider.getViaAddress());
      focus_conn=new ConnectionField("IP4",sip_provider.getViaAddress());
      // create the first UAS
      newUAS();
   }  


   /** Gets the Focus contact URL. */
   public NameAddress getContactURL()
   {  return new NameAddress(new SipURL("focus",sip_provider.getViaAddress(),sip_provider.getPort()));
   }


   /** Gets the Focus SIP port. */
   public int getPort()
   {  return sip_provider.getPort();
   }


   /** Sets media ports equal to that used by peers. */
   public void setPortMirroring(boolean mirroring)
   {  printLog("port mirroring: "+mirroring,Log.LEVEL_HIGH); 
      port_mirroring=mirroring;
   }


   /** Sets the mixer mode for audio streams. */
   public void setAudioMixing(boolean audio_mixing)
   {  printLog("audio mixing: "+audio_mixing,Log.LEVEL_HIGH); 
      this.audio_mixing=audio_mixing;
      // force PCMU as supported audio type
      if (audio_mixing) focus_audio=new MediaDescriptor(new MediaField("audio",0,0,"RTP/AVP","0"),null,new AttributeField("rtpmap","0 PCMU/8000"));
      else focus_audio=null;
   }


   /** Sets the symmetric RTP mode. */
   /*public void setSymmetricRtp(boolean symmetric_rtp)
   {  printLog("symmetric RTP: "+symmetric_rtp,Log.LEVEL_HIGH); 
      this.symmetric_rtp=symmetric_rtp;
   }*/


   /** Tears down any active calls. */
   public void hangup()
   {  printLog("tearing down any active calls",Log.LEVEL_HIGH);
      for (Enumeration e=calls.keys(); e.hasMoreElements(); )
      {  Call call=(Call)e.nextElement();
         call.hangup(); 
         closeMediaSessions(call); 
      }
   }


   /** Stops the Focus. */
   public void halt()
   {  for (Enumeration e=calls.keys(); e.hasMoreElements(); )
      {  Call call=(Call)e.nextElement();
         calls.remove(call); 
      }
      calls=null;
      audio_mixer.close();
      audio_mixer=null;
      sip_provider.halt();
      sip_provider=null;
      media_ports=null;
      printLog("focus halted.",Log.LEVEL_HIGH);
   }


   /** Creates a new UAS (User Agent Server). */
   private void newUAS()
   {  printLog("new Focus-UAS",Log.LEVEL_MEDIUM);

      NameAddress focus_url=new NameAddress(new SipURL("focus",sip_provider.getViaAddress(),sip_provider.getPort()));
      Call call=new Call(sip_provider,focus_url,this);
      call.listen();
   }


   /** Callback function called when arriving a new INVITE method (incoming call). */
   public void onCallInvite(Call call, NameAddress callee, NameAddress caller, String sdp, Message invite)
   {  printLog("onCallInvite()",Log.LEVEL_MEDIUM);
     
      // remote audio and video
      MediaDescriptor remote_audio=null;  
      MediaDescriptor remote_video=null;  
      if (sdp!=null && sdp.length()>0)
      {  SessionDescriptor remote_sdp=new SessionDescriptor(sdp);
         remote_audio=remote_sdp.getMediaDescriptor("audio");
         remote_video=remote_sdp.getMediaDescriptor("video");
      }             
      // create local sdp without media
      SessionDescriptor local_sdp;
      if (sdp!=null && sdp.length()>0)
      {  local_sdp=new SessionDescriptor(sdp);
         local_sdp.setConnection(focus_conn);
         local_sdp.removeMediaDescriptors();
      }
      else
      {  local_sdp=new SessionDescriptor(focus_sdp);
      }
      
      // select audio and video ports
      int audio_port=0, video_port=0;
      if (!port_mirroring)
      {  Integer iport=(Integer)media_ports.iterator().next();
         media_ports.remove(iport);
         int refer_port=iport.intValue();      
         audio_port=refer_port;
         video_port=refer_port+2;
         printLog("used media ports: audio="+audio_port+", video="+video_port,Log.LEVEL_MEDIUM);
         printLog("remaining unused media ports: "+media_ports.size(),Log.LEVEL_MEDIUM); 
      }
      else
      {  // WARNING: Currently if no offer is present, no media port is set!!
         // WARNING: No check is done on port availability!!
         if (remote_audio!=null) audio_port=remote_audio.getMedia().getPort();        
         if (remote_video!=null) video_port=remote_video.getMedia().getPort();
         printLog("MEDIA PORT MIRRORING: used media ports: audio="+audio_port+", video="+video_port,Log.LEVEL_MEDIUM);
      }

      // set the audio
      MediaDescriptor audio;
      if (focus_audio!=null)
      {  MediaField audio_field=new MediaField("audio",audio_port,0,focus_audio.getMedia().getTransport(),focus_audio.getMedia().getFormats());
         Vector attributes=focus_audio.getAttributes();
         audio=new MediaDescriptor(audio_field,null,attributes);
      }
      else
      if (remote_audio!=null)
      {  MediaField audio_field=new MediaField("audio",audio_port,0,remote_audio.getMedia().getTransport(),remote_audio.getMedia().getFormats());
         Vector attributes=remote_audio.getAttributes();
         audio=new MediaDescriptor(audio_field,null,attributes);
         focus_audio=audio;
      }       
      else
      {  MediaField audio_field=new MediaField("audio",audio_port,0,"RTP/AVP",String.valueOf(AUDIO_AVP));
         AttributeField attribute=new AttributeField("rtpmap",AUDIO_AVP+" "+AUDIO_CODEC+"/"+AUDIO_RATE);
         audio=new MediaDescriptor(audio_field,null,attribute);
      }
      if (audio!=null) local_sdp.addMediaDescriptor(audio);  
           
      // set the video        
      MediaDescriptor video;
      if (focus_video!=null)
      {  MediaField video_field=new MediaField("video",video_port,0,focus_video.getMedia().getTransport(),focus_video.getMedia().getFormats());
         Vector attributes=focus_video.getAttributes();
         video=new MediaDescriptor(video_field,null,attributes);
         focus_video=video;
      }
      else
      if (remote_video!=null)
      {  MediaField video_field=new MediaField("video",video_port,0,remote_video.getMedia().getTransport(),remote_video.getMedia().getFormats());
         Vector attributes=remote_video.getAttributes();
         video=new MediaDescriptor(video_field,null,attributes);
      }       
      else 
      {  MediaField video_field=new MediaField("video",video_port,0,"RTP/AVP",String.valueOf(video_avp));
         AttributeField attribute=new AttributeField("rtpmap",String.valueOf(video_avp));
         video=new MediaDescriptor(video_field,null,attribute);
      }
      if (video!=null) local_sdp.addMediaDescriptor(video);

      // set the call local sdp
      call.setLocalSessionDescriptor(local_sdp.toString());
      
      printLog("local sdp:\r\n"+local_sdp.toString(),Log.LEVEL_LOWER);
      //Vector v=((MediaDescriptor)local_sdp.getMediaDescriptors().elementAt(0)).getMedia().getFormatList();
      //printLog("DEBUG:",5);
      //for (int i=0; i<v.size(); i++) printLog("rtpmap: "+(String)v.elementAt(i),5);
      
      // automatically accept the call and form the local sdp
      super.onCallInvite(call,callee,caller,sdp,invite);
      newUAS();
   }


   /** Callback function called when arriving an ACK method (call confirmed). */
   public void onCallConfirmed(Call call, String sdp, Message ack)
   {  printLog("CONFIRMED/CALL",Log.LEVEL_MEDIUM);
           
      SessionDescriptor local_sdp=new SessionDescriptor(call.getLocalSessionDescriptor());
      String local_media_address=(new Parser(local_sdp.getConnection().toString())).skipString().skipString().getString();
      int local_audio_port=0;  
      int local_video_port=0;  
        
      for (Enumeration e=local_sdp.getMediaDescriptors().elements(); e.hasMoreElements(); )
      {  MediaDescriptor media_desc=((MediaDescriptor)e.nextElement());
         MediaField media_field=media_desc.getMedia();
         String media_type=media_field.getMedia();
         int media_port=media_field.getPort();
         if (media_type.equals("audio"))
         {  local_audio_port=media_port;
         }
         else
         if (media_type.equals("video"))
         {  local_video_port=media_port;
         }      
      }

      SessionDescriptor remote_sdp=new SessionDescriptor(call.getRemoteSessionDescriptor());
      String remote_media_address=(new Parser(remote_sdp.getConnection().toString())).skipString().skipString().getString();
      int remote_audio_port=0;              
      int remote_video_port=0;              

      for (Enumeration e=remote_sdp.getMediaDescriptors().elements(); e.hasMoreElements(); )
      {  MediaDescriptor media_desc=((MediaDescriptor)e.nextElement());
         MediaField media_field=media_desc.getMedia();
         String media_type=media_field.getMedia();
         int media_port=media_field.getPort();
         if (media_type.equals("audio"))
         {  remote_audio_port=media_port;
         }
         else
         if (media_type.equals("video"))
         {  remote_video_port=media_port;
         }      
      }
      
      int refer_port=local_audio_port;
      if (refer_port==0) refer_port=local_video_port-2;

      //if (local_audio_port!=0) mrfp.addRtpRtcp(local_audio_port,remote_media_address,remote_audio_port);
      if (local_audio_port!=0)
      {  if (audio_mixing) audio_mixer.addEndpoint(local_audio_port,remote_media_address,remote_audio_port);
         else audio_mrfp.addRtpRtcp(local_audio_port,remote_media_address,remote_audio_port);
      }
      if (local_video_port!=0) video_mrfp.addRtpRtcp(local_video_port,remote_media_address,remote_video_port);
      calls.put(call,new Integer(refer_port));
   }


   /** Callback function called when arriving a BYE method (close request). */
   public void onCallBye(Call call, Message bye)
   {  printLog("CLOSING",Log.LEVEL_MEDIUM);
      closeMediaSessions(call);
   }


   /** Closes media sessions for a specific call. */
   private void closeMediaSessions(Call call)
   {  // get local ports
      SessionDescriptor local_sdp=new SessionDescriptor(call.getLocalSessionDescriptor());
      int local_audio_port=0;
      int local_video_port=0;
      for (Enumeration e=local_sdp.getMediaDescriptors().elements(); e.hasMoreElements(); )
      {  MediaField media=((MediaDescriptor)e.nextElement()).getMedia();
         if (media.getMedia().equals("audio")) 
         {  local_audio_port=media.getPort();
         }
         if (media.getMedia().equals("video")) 
         {  local_video_port=media.getPort();
         }
      }
      // remove media sessions from MPU
      //if (local_audio_port!=0) mrfp.removeRtpRtcp(local_audio_port);
      if (local_audio_port!=0)
      {  if (audio_mixing) audio_mixer.removeEndpoint(local_audio_port);
         else audio_mrfp.removeRtpRtcp(local_audio_port);
      }
      if (local_video_port!=0) video_mrfp.removeRtpRtcp(local_video_port);
      calls.remove(call);  
         
      // free media ports
      if (!port_mirroring)
      {  int refer_port=local_audio_port;
         if (refer_port==0) refer_port=local_video_port-2;
         media_ports.add(new Integer(refer_port));
         printLog("new available media ports: "+local_audio_port+", "+local_video_port,Log.LEVEL_MEDIUM);
         printLog("total available media ports: "+media_ports.size(),Log.LEVEL_MEDIUM);
      }
      else printLog("MEDIA PORT MIRRORING: new available media ports: "+local_audio_port+", "+local_video_port,Log.LEVEL_HIGH);
   }

   
   // ****************************** Logs *****************************

   /** Adds a new string to the default Log. */
   private void printLog(String str, int level)
   {  if (log!=null) log.println("Focus: "+str,ConferenceServer.LOG_OFFSET+level);  
   }


   // ****************************** MAIN *****************************   
   
   /** Default first available media port */
   static final int default_first_media_port=37100;
   /** Default last available media port */
   static final int default_last_media_port=37399;


   /** The main method. */
   public static void main(String[] args)
   {        
      try
      {  int first_media_port=default_first_media_port;
         int last_media_port=default_last_media_port;
         String file=null;
         boolean port_mirroring=false;
         boolean audio_mixing=false;
         boolean symmetric_rtp=false;

         for (int i=0; i<args.length; i++)
         {  if (args[i].equals("-p"))
            {  first_media_port=Integer.parseInt(args[++i]);
               last_media_port=Integer.parseInt(args[++i]);
               continue;
            }
            if (args[i].equals("-f") && args.length>(i+1))
            {  file=args[++i];
               continue;
            }
            if (args[i].equals("--port-mirror"))
            {  port_mirroring=true;
               continue;
            }
            if (args[i].equals("--audio-mixer"))
            {  audio_mixing=true;
               continue;
            }
            if (args[i].equals("--symmetric-rtp"))
            {  symmetric_rtp=true;
               continue;
            }
            
            // else, do:
            //if (args[i].equals("-h"))
            System.out.println("usage:\n   java Focus [options]");
            System.out.println("   options:");
            System.out.println("   -f <config_file>           specifies a configuration file");
            System.out.println("   -p <fist_port> <last_port> interval of media ports");
            System.out.println("   --port-mirror              uses media port equals to the peer");
            System.out.println("   --audio-mixer              mixes audio streams");
            System.out.println("   --symmetric-rtp            uses symmetric rtp");
            System.exit(0);
         }
                     
         SipStack.init(file);
         SipProvider sip_provider=new SipProvider(file);
         
         HashSet media_ports=new HashSet();
         for (int i=first_media_port; i<=last_media_port; i+=4) media_ports.add(new Integer(i)); 

         Focus focus=new Focus(sip_provider,media_ports,symmetric_rtp);
         if (port_mirroring) focus.setPortMirroring(true);
         if (audio_mixing) focus.setAudioMixing(true);
         //if (symmetric_rtp) focus.setSymmetricRtp(true);
               
      }
      catch (Exception e) { e.printStackTrace(); }

   } 
   
}