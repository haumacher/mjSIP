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

package org.zoolu.sound;


import javax.sound.sampled.*;
import java.io.InputStream;
import java.io.OutputStream;


/** ExtendedAudioSystem is a static class that allows the access to system audio
  * input and output in pure-java style using the javax.sound.sampled library (package).
  * <p/>
  * ExtendedAudioSystem has the following main methods: <br/>
  * <ul>
  * <li> startAudioInputLine() - for starting the audio capturing
  *
  * <li> stopAudioInputLine() - for stopping the audio capturing
  *
  * <li> startAudioOutputLine() - for starting the audio playback
  *
  * <li> stopAudioOutputLine() - for stopping the audio playback
  *
  * <li> getInputStream(AudioFormat) - for obtaining the actual InputStream used to read captured
  *      audio data of a specified format; the default format is PCM ULAW 8kHz mono
  *
  * <li> getOutputStream(AudioFormat) - for obtaining the actual OutputStream used to write and play out
  *      audio data of a specified format; the default format is PCM ULAW 8kHz mono
  * </ul>
  */
public class ExtendedAudioSystem
{
   /** Internal buffer size. */
   public static final int INTERNAL_BUFFER_SIZE=40960;

   /** Whether printing debugging information on standard error output. */
   public static boolean DEBUG=true;

   
   /** Base system audio format (PCM 8000Hz, Linear, 16bit, Mono, Little endian). */
   private static final AudioFormat base_format=new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,8000.0F,16,1,2,8000.0F,false);

   /** TargetDataLine for audio capturing. */
   private static TargetDataLine target_line=null;
   
   /** SourceDataLine for audio playout. */
   private static SourceDataLine source_line;



   /** Inits the static system audio input line */
   public static void initAudioInputLine()
   {  if (DEBUG)
      {  AudioFormat.Encoding[] codecs=AudioSystem.getTargetEncodings(base_format);
         String codec_list="";
         for (int i=0; i<codecs.length; i++) codec_list+=" "+codecs[i].toString();
         printLog("Supported:"+codec_list);
      }

      DataLine.Info lineInfo=new DataLine.Info(TargetDataLine.class,base_format,INTERNAL_BUFFER_SIZE);
      
      if (!AudioSystem.isLineSupported(lineInfo))
      {  System.err.println("ERROR: AudioLine not supported by this system.");
      }

      try
      {  target_line=(TargetDataLine)AudioSystem.getLine(lineInfo);
         if (DEBUG) printLog("TargetDataLine: "+target_line.getFormat());
         target_line.open(base_format,INTERNAL_BUFFER_SIZE);
      }
      catch (Exception e)
      {  System.err.println("ERROR: Exception when trying to init audio input: "+e.getMessage());
         //e.printStackTrace();
      }
   }


   /** Closes the static system audio input line */
   static public void closeAudioInputLine()
   {  target_line.close();
   }


   /** Inits the static system audio output line */
   public static void initAudioOutputLine()
   {  if (DEBUG)
      {  AudioFormat.Encoding[] codecs=AudioSystem.getTargetEncodings(base_format);
         String codec_list=""; 
         for (int i=0; i<codecs.length; i++) codec_list+=" "+codecs[i].toString();
         printLog("Supported:"+codec_list);
      }

      DataLine.Info lineInfo=new DataLine.Info(SourceDataLine.class, base_format, INTERNAL_BUFFER_SIZE);

      if (!AudioSystem.isLineSupported(lineInfo))
      {  System.err.println("ERROR: AudioLine not supported by this System.");
      }

      try
      {  source_line=(SourceDataLine)AudioSystem.getLine(lineInfo);
         if (DEBUG) printLog("SourceDataLine: "+source_line.getFormat());
         source_line.open(base_format,INTERNAL_BUFFER_SIZE);
      }
      catch (Exception e)
      {  System.err.println("ERROR: Exception when trying to init audio output at ExtendedAudioSystem: "+e.getMessage());
         //e.printStackTrace();
      }      
   }


   /** Closes the static system audio output line */
   static public void closeAudioOutputLine()
   {  source_line.close();
   }


   /** Starts capturing system audio */
   public static void startAudioInputLine()
   {  if (target_line==null) initAudioInputLine(); 
      if (target_line.isOpen()) target_line.start();
      else
      {  System.err.println("WARNING: Audio play error: target line is not open.");
      }
   }


   /** Stops capturing system audio */
   public static void stopAudioInputLine()
   {  if (target_line.isOpen()) target_line.stop();
      else
      {  System.err.println("WARNING: Audio stop error: target line is not open.");
      }
      //target_line.close();
   }


   /** Starts playing system audio */
   public static void startAudioOutputLine()
   {  if (source_line==null) initAudioOutputLine();
      if (source_line.isOpen()) source_line.start();
      else
      {  System.err.println("WARNING: Audio play error: source line is not open.");
      }
   }


   /** Stops playing system audio */
   public static void stopAudioOutputLine()
   {  if (source_line.isOpen())
      {  source_line.drain();
         source_line.stop();
      }
      else
      {  System.err.println("WARNING: Audio stop error: source line is not open.");
      }
      //source_line.close();
   }


   /** Gets the base audio format */
   public static AudioFormat getBaseAudioFormat()
   {  return base_format;
   }


   /** Gets the base transcoded audio format. It differs from the base audio format just over the encoding type. */
   public static AudioFormat getBaseTranscodedAudioFormat(AudioFormat.Encoding encoding)
   {  AudioFormat format=new AudioFormat(encoding,base_format.getSampleRate(), base_format.getSampleSizeInBits(),base_format.getChannels(),base_format.getFrameSize(),base_format.getFrameRate(),base_format.isBigEndian());
      return format;
   }


   /** Gets the audio AudioInputStream */
   public static AudioInputStream getInputStream()
   {  return getInputStream((AudioFormat)null);
   }


   /** Gets the audio AudioInputStream */
   public static AudioInputStream getInputStream(AudioFormat.Encoding encoding)
   {  return getInputStream(getBaseTranscodedAudioFormat(encoding));
   }


   /** Gets the audio AudioInputStream */
   public static AudioInputStream getInputStream(AudioFormat format)
   {  if (target_line==null) initAudioInputLine();
      // by default use PCM ULAW 8000Hz mono
      if (format==null) format=new AudioFormat(AudioFormat.Encoding.ULAW,8000.0F,8,1,1,8000.0F,false);

      AudioInputStream audio_input_stream=null;
      if (target_line.isOpen())
      {  // create an AudioInputStream from the target_line
         audio_input_stream=new AudioInputStream(target_line);
         // convert the AudioInputStream to the selected format
         audio_input_stream=AudioSystem.getAudioInputStream(format,audio_input_stream);
      }
      else
      {  System.err.println("WARNING: Audio init error: target line is not open.");
      }

      return audio_input_stream;
   }


   /** Gets the audio AudioOutputStream */
   public static AudioOutputStream getOutputStream()
   {  return getOutputStream((AudioFormat)null);
   }
   
   
   /** Gets the audio AudioOutputStream */
   public static AudioOutputStream getOutputStream(AudioFormat.Encoding encoding)
   {  return getOutputStream(getBaseTranscodedAudioFormat(encoding));
   }


   /** Gets the audio AudioOutputStream */
   public static AudioOutputStream getOutputStream(AudioFormat format)
   {  if (source_line==null) initAudioOutputLine();
      // by default use PCM ULAW 8000Hz mono
      if (format==null) format=new AudioFormat(AudioFormat.Encoding.ULAW,8000.0F,8,1,1,8000.0F,false);
      
      AudioOutputStream audio_output_stream=null;
      if (source_line.isOpen())
      {  // convert the audio stream to the selected format
         try
         {  audio_output_stream=new SourceLineAudioOutputStream(format,source_line);
         }
         catch (Exception e)
         {  System.err.println("WARNING: Audio init error: impossible to get audio output stream from sorce line.");
            //e.printStackTrace();
         }
      }
      else
      {  System.err.println("WARNING: Audio init error: source line is not open.");
      }

      return audio_output_stream;
   } 


   /** Debug output */
   private static void printLog(String str)
   {  System.err.println("ExtendedAudioSystem: "+str);
   }

}