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

package local.media;



import local.media.*;
import org.zoolu.sound.*;
import org.zoolu.tools.Log;
import org.zoolu.tools.ExceptionPrinter;
import org.zoolu.net.UdpSocket;
import org.zoolu.net.SocketAddress;

import java.io.*;
import javax.sound.sampled.*;
import javax.sound.sampled.spi.FormatConversionProvider;



/** Fullduplex audio streaming application based on javax.sound.
  */
public class AudioApp implements MediaApp, RtpStreamReceiverListener
{  
   /** Whether using symmetric RTP by default */
   static final boolean SYMMETRIC_RTP=false;

   /** Codec */
   static final String DEFAULT_CODEC="ULAW";
   /** Payload type */
   static final int DEFAULT_PAYLOAD_TYPE=0;
   /** Sample rate [samples/sec] */
   static final int DEFAULT_SAMPLE_RATE=8000;
   /** Codec frame size [bytes] */
   static final int DEFAULT_FRAME_SIZE=1;
   /** Codec frame rate [frames/sec] */
   static final int DEFAULT_FRAME_RATE=8000;
   /** Packet size [bytes] */
   static final int DEFAULT_PACKET_SIZE=160;
   /** whether using big-endian rule for byte ordering */
   static final boolean DEFAULT_BIG_ENDIAN=false;

   /** Test tone */
   public static final String TONE="TONE";
   /** Test tone frequency [Hz] */
   public static int TONE_FREQ=100;
   /** Test tone ampliture (from 0.0 to 1.0) */
   public static double TONE_AMP=1.0;
   /** Test tone sample size [bits] */
   public static int TONE_SAMPLE_SIZE=8;


   /** Log */
   Log log=null;

   /** Audio format */
   AudioFormat audio_format;

   /** Stream direction */
   FlowSpec.Direction dir;

   /** UDP socket */
   UdpSocket socket=null;

   /** RtpStreamSender */
   RtpStreamSender sender=null;
   /** RtpStreamReceiver */
   RtpStreamReceiver receiver=null;

   /** Whether using system audio capture */
   boolean audio_input=false;
   /** Whether using system audio playout */
   boolean audio_output=false;
   
   /** Whether using symmetric_rtp */
   boolean symmetric_rtp=SYMMETRIC_RTP;



   /** Creates a new AudioApp */
   public AudioApp(RtpStreamSender sender, RtpStreamReceiver receiver, boolean symmetric_rtp, Log log)
   {  this.log=log;
      this.sender=sender;
      this.receiver=receiver;
      this.symmetric_rtp=symmetric_rtp;
      printLog("codec: [unknown]",Log.LEVEL_MEDIUM);
   }


   /** Creates a new AudioApp */
   /*public AudioApp(int local_port, String remote_addr, int remote_port, MediaApp.MediaDirection direction, Log log)
   {  init(local_port,remote_addr,remote_port,direction,null,null,null,-1,0,0,true,symmetric_rtp,log);
   }*/


   /** Creates a new AudioApp */
   public AudioApp(FlowSpec flow_spec, String audiofile_in, String audiofile_out, boolean direct_convertion, boolean do_sync, int red_rate, boolean symmetric_rtp, Log log)
   {  MediaSpec audio_spec=flow_spec.getMediaSpec();
      init(flow_spec.getLocalPort(),flow_spec.getRemoteAddress(),flow_spec.getRemotePort(),flow_spec.getDirection(),audiofile_in,audiofile_out,audio_spec.getCodec(),audio_spec.getAVP(),audio_spec.getSampleRate(),audio_spec.getPacketSize(),direct_convertion,do_sync,red_rate,symmetric_rtp,log);
   }


   /** Inits the AudioApp */
   private void init(int local_port, String remote_addr, int remote_port, FlowSpec.Direction direction, String audiofile_in, String audiofile_out, String codec, int payload_type, int sample_rate, int packet_size, boolean direct_convertion, boolean do_sync, int red_rate, boolean symmetric_rtp, Log log)
   {  this.log=log;
      this.dir=direction;
      this.symmetric_rtp=symmetric_rtp;
      // 1) in case not defined, use default values
      if (codec==null) codec=DEFAULT_CODEC;
      if (payload_type<0) payload_type=DEFAULT_PAYLOAD_TYPE;
      if (sample_rate<=0) sample_rate=DEFAULT_SAMPLE_RATE;
      if (packet_size<=0) packet_size=DEFAULT_PACKET_SIZE;
      
      // 2) codec name translation
      codec=codec.toUpperCase();
      String codec_orig=codec;

      if (codec.equals("PCMU")) codec="ULAW";
      else
      if (codec.equals("PCMA")) codec="ALAW";
      else
      if (codec.equals("G711-ulaw")) codec="G711_ULAW";
      else
      if (codec.equals("G711-alaw")) codec="G711_ALAW";
      else
      if (codec.equals("G726-24")) codec="G726_24";
      else
      if (codec.equals("G726-32")) codec="G726_32";
      else
      if (codec.equals("G726-40")) codec="G726_40";
      else
      if (codec.equals("ADPCM24")) codec="G726_24";
      else
      if (codec.equals("ADPCM32")) codec="G726_32";
      else
      if (codec.equals("ADPCM40")) codec="G726_40";
      else
      if (codec.equals("GSM")) codec="GSM0610";

      printLog("codec: "+codec_orig,Log.LEVEL_MEDIUM);     
      if (!codec.equals(codec_orig)) printLog("codec mapped to: "+codec,Log.LEVEL_MEDIUM);

      // 3) frame_size, frame_rate, packet_rate
      int frame_size=DEFAULT_FRAME_SIZE;
      int frame_rate=DEFAULT_FRAME_RATE;

      if (codec.equals("ULAW") || codec.equals("G711_ULAW"))
      {  payload_type=0;
         frame_size=1;
         frame_rate=sample_rate;
      }
      else
      if (codec.equals("ALAW") || codec.equals("G711_ALAW"))
      {  payload_type=8;
         frame_size=1;
         frame_rate=sample_rate;
      }
      else
      if (codec.equals("G726_24"))
      {  payload_type=101;
         frame_size=3;
         frame_rate=sample_rate/8;
      }
      else
      if (codec.equals("G726_32"))
      {  payload_type=101;
         frame_size=4;
         frame_rate=sample_rate/8;
      }
      else
      if (codec.equals("G726_40"))
      {  payload_type=101;
         frame_size=5;
         frame_rate=sample_rate/8;
      }
      else
      if (codec.equals("GSM0610"))
      {  payload_type=3;
         frame_size=33;
         frame_rate=sample_rate/160; // = 50 frames/sec in case of sample rate = 8000 Hz
      }

      int packet_rate=frame_rate*frame_size/packet_size;
      printLog("packet size: "+packet_size,Log.LEVEL_LOW);
      printLog("packet rate: "+packet_rate,Log.LEVEL_LOW);
   
      // 4) find the proper supported AudioFormat
      printLog("base audio format: "+ExtendedAudioSystem.getBaseAudioFormat().toString(),Log.LEVEL_LOW);
      AudioFormat.Encoding encoding=null;
      AudioFormat.Encoding[] supported_encodings=AudioSystem.getTargetEncodings(ExtendedAudioSystem.getBaseAudioFormat());
      for (int i=0; i<supported_encodings.length ; i++)
      {  if (supported_encodings[i].toString().equalsIgnoreCase(codec)) 
         {  encoding=supported_encodings[i];
            break;
         }
      }
      if (encoding!=null)
      {  // get the first available target format
         AudioFormat[] available_formats=AudioSystem.getTargetFormats(encoding,ExtendedAudioSystem.getBaseAudioFormat());
         audio_format=available_formats[0];
         printLog("encoding audio format: "+audio_format,Log.LEVEL_LOW);
         //printLog("DEBUG: frame_size: "+audio_format.getFrameSize(),Log.LEVEL_LOW);
         //printLog("DEBUG: frame_rate: "+audio_format.getFrameRate(),Log.LEVEL_LOW);
         //printLog("DEBUG: big_endian: "+audio_format.isBigEndian(),Log.LEVEL_LOW);
      }
      else printLog("WARNING: codec '"+codec+"' not natively supported",Log.LEVEL_HIGH);

      try
      {  // 5) udp socket
         socket=new UdpSocket(local_port);
         
         // 6) sender
         if ((dir==FlowSpec.SEND_ONLY || dir==FlowSpec.FULL_DUPLEX))
         {  printLog("new audio sender to "+remote_addr+":"+remote_port,Log.LEVEL_MEDIUM);
            if (audiofile_in!=null && audiofile_in.equals(AudioApp.TONE))
            {  // tone generator
               printLog("Tone generator: "+TONE_FREQ+" Hz");
               ToneInputStream tone=new ToneInputStream(TONE_FREQ,TONE_AMP,sample_rate,TONE_SAMPLE_SIZE,ToneInputStream.PCM_LINEAR_UNSIGNED,DEFAULT_BIG_ENDIAN);
               // sender
               sender=new RtpStreamSender(tone,true,payload_type,packet_rate,packet_size,socket,remote_addr,remote_port);
            }
            else
            if (audiofile_in!=null)
            {  // input file
               File file=new File(audiofile_in);
               if (audiofile_in.indexOf(".wav")==(audiofile_in.length()-4))
               {  // known file format
                  printLog("File audio format: "+AudioSystem.getAudioFileFormat(file));
                  // get AudioInputStream
                  AudioInputStream audio_input_stream=AudioSystem.getAudioInputStream(file);
                  // apply audio conversion
                  if (audio_format!=null) audio_input_stream=AudioSystem.getAudioInputStream(audio_format,audio_input_stream);
                  // sender
                  sender=new RtpStreamSender(audio_input_stream,true,payload_type,packet_rate,packet_size,socket,remote_addr,remote_port);
               }
               else
               {  // sender
                  sender=new RtpStreamSender(new FileInputStream(file),true,payload_type,packet_rate,packet_size,socket,remote_addr,remote_port);
               }
            }
            else
            {  // javax sound
               AudioInputStream audio_input_stream=null;
               if (!direct_convertion || codec.equalsIgnoreCase("ULAW") || codec.equalsIgnoreCase("ALAW"))
               {  // use embedded conversion provider
                  audio_input_stream=ExtendedAudioSystem.getInputStream(audio_format);          
               }
               else
               {  // use explicit conversion provider
                  Class audio_system=Class.forName("com.zoopera.sound.ConverterAudioSystem");
                  java.lang.reflect.Method get_input_stream=audio_system.getMethod("convertAudioInputStream",new Class[]{ String.class, float.class, AudioInputStream.class });
                  audio_input_stream=(AudioInputStream)get_input_stream.invoke(null,new Object[]{ codec, new Integer(sample_rate), ExtendedAudioSystem.getInputStream(ExtendedAudioSystem.getBaseAudioFormat()) });
                  printLog("send x-format: "+audio_input_stream.getFormat());
               }
               // sender
               if (!do_sync) sender=new RtpStreamSender(audio_input_stream,false,payload_type,packet_rate,packet_size,socket,remote_addr,remote_port);
               else sender=new RtpStreamSender(audio_input_stream,true,payload_type,packet_rate,packet_size,socket,remote_addr,remote_port);
               //if (sync_adj>0) sender.setSyncAdj(sync_adj);
               audio_input=true;
            }
         }
         
         // 7) receiver
         if (dir==FlowSpec.RECV_ONLY || dir==FlowSpec.FULL_DUPLEX)
         {  printLog("new audio receiver on "+local_port,Log.LEVEL_MEDIUM);
            if (audiofile_out!=null)
            {  // output file
               File file=new File(audiofile_out);
               FileOutputStream output_stream=new FileOutputStream(file);
               // receiver
               receiver=new RtpStreamReceiver(output_stream,socket);
            }
            else
            {  // javax sound
               AudioOutputStream audio_output_stream=null;
               if (!direct_convertion || codec.equalsIgnoreCase("ULAW") || codec.equalsIgnoreCase("ALAW"))
               {  // use embedded conversion provider
                  audio_output_stream=ExtendedAudioSystem.getOutputStream(audio_format);
               }
               else
               {  // use explicit conversion provider
                  Class audio_system=Class.forName("com.zoopera.sound.ConverterAudioSystem");
                  java.lang.reflect.Method get_output_stream=audio_system.getMethod("convertAudioOutputStream",new Class[]{ String.class, float.class, AudioOutputStream.class });
                  audio_output_stream=(AudioOutputStream)get_output_stream.invoke(null,new Object[]{ codec, new Integer(sample_rate), ExtendedAudioSystem.getOutputStream(ExtendedAudioSystem.getBaseAudioFormat()) });
                  printLog("recv x-format: "+audio_output_stream.getFormat());
               }
               // receiver
               receiver=new RtpStreamReceiver(audio_output_stream,socket,this);
               receiver.setRED(red_rate);
               audio_output=true;
            }
         }
      }
      catch (Exception e) {  printException(e,Log.LEVEL_HIGH);  }
   }


   /** Starts media application */
   public boolean startApp()
   {  printLog("starting java audio",Log.LEVEL_HIGH);
      if (sender!=null)
      {  printLog("start sending",Log.LEVEL_LOW);
         if (audio_input) ExtendedAudioSystem.startAudioInputLine();
         sender.start();
      }
      if (receiver!=null)
      {  printLog("start receiving",Log.LEVEL_LOW);
         if (audio_output) ExtendedAudioSystem.startAudioOutputLine();
         receiver.start();
      }
      return true;      
   }


   /** Stops media application */
   public boolean stopApp()
   {  printLog("stopping java audio",Log.LEVEL_HIGH);    
      if (sender!=null)
      {  sender.halt();
         sender=null;
         printLog("sender halted",Log.LEVEL_LOW);
      }      
      if (audio_input) ExtendedAudioSystem.stopAudioInputLine();
    
      if (receiver!=null)
      {  receiver.halt();
         receiver=null;
         printLog("receiver halted",Log.LEVEL_LOW);
      }      
      if (audio_output) ExtendedAudioSystem.stopAudioOutputLine();

      // try to take into account the resilience of RtpStreamSender
      try { Thread.sleep(RtpStreamReceiver.SO_TIMEOUT); } catch (Exception e) {}
      socket.close();
      return true;
   }
   
   
   /** Sets symmetric RTP mode. */
   public void setSymmetricRtp(boolean symmetric_rtp)
   {  this.symmetric_rtp=symmetric_rtp;
   }


   /** whether symmetric RTP mode is set. */
   public boolean isSymmetricRtp()
   {  return symmetric_rtp;
   }


   /** From RtpStreamReceiverListener. When the remote socket address (source) is changed. */
   public void onRemoteSoAddressChanged(RtpStreamReceiver rr, SocketAddress remote_soaddr)
   {  if (symmetric_rtp && sender!=null) sender.setRemoteSoAddress(remote_soaddr);
   }


   // ****************************** Logs *****************************

   /** Adds a new string to the default Log */
   private void printLog(String str)
   {  printLog(str,Log.LEVEL_HIGH);
   }


   /** Adds a new string to the default Log */
   private void printLog(String str, int level)
   {  if (log!=null) log.println("AudioApp: "+str,LoopbackMediaApp.LOG_OFFSET+level);  
      if (level<=Log.LEVEL_HIGH) System.out.println("AudioApp: "+str);
   }

   /** Adds the Exception message to the default Log */
   private void printException(Exception e,int level)
   {  printLog("Exception: "+ExceptionPrinter.getStackTraceOf(e),level);
      if (level<=Log.LEVEL_HIGH) e.printStackTrace();
   }

}
