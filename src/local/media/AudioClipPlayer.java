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


import java.net.URL;
import java.io.*;
import javax.sound.sampled.*;


/** Plays an audio file or AudioInputStream.
  */
public class AudioClipPlayer implements LineListener 
{
   /** The sound clip */
   Clip clip=null;

   /** Loop counter */
   int loop_count=1;

   /** The player listener */
   AudioClipPlayerListener listener=null; 


   /** Creates a new AudioClipPlayer.
     * param@ filename the name of a valid audio file,
     * param@ listener the AudioClipPlayer listener. */
   public AudioClipPlayer(String filename, AudioClipPlayerListener listener)
   {  try
      {  //FileInputStream inputstream=new FileInputStream(new File(filename));
         //AudioInputStream audio_inputstream=AudioSystem.getAudioInputStream(inputstream);
         // note: the following line should be the same as the previous two
         AudioInputStream audio_inputstream=AudioSystem.getAudioInputStream(new File(filename));
         init(audio_inputstream,listener);
      }
      catch (Exception e) { e.printStackTrace(); }
   }


   /** Creates a new AudioClipPlayer.
     * param@ file a valid audio file,
     * param@ listener the AudioClipPlayer listener. */
   public AudioClipPlayer(File file, AudioClipPlayerListener listener)
   {  try
      {  //AudioInputStream audio_inputstream=AudioSystem.getAudioInputStream(new FileInputStream(file));
         // note: the following line should be the same as the previous one
         AudioInputStream audio_inputstream=AudioSystem.getAudioInputStream(file);
         init(audio_inputstream,listener);
      }
      catch (Exception e) { e.printStackTrace(); }
   }


   /** Creates a new AudioClipPlayer.
     * param@ audio_inputstream an AudioInputStream,
     * param@ listener the AudioClipPlayer listener. */
   public AudioClipPlayer(URL url, AudioClipPlayerListener listener)
   {  try
      {  AudioInputStream audio_inputstream=AudioSystem.getAudioInputStream(url);
         init(audio_inputstream,listener);
      }
      catch (Exception e) { e.printStackTrace(); } 
   }


   /** Creates a new AudioClipPlayer.
     * param@ inputstream an InputStream pointing to valid audio file data,
     * param@ listener the AudioClipPlayer listener. */
   /*public AudioClipPlayer(InputStream inputstream, AudioClipPlayerListener listener)
   {  try
      {  AudioInputStream audio_inputstream=AudioSystem.getAudioInputStream(inputstream);
         init(audio_inputstream,listener);
      }
      catch (Exception e) { e.printStackTrace(); }
   }*/


   /** Creates a new AudioClipPlayer.
     * param@ audio_inputstream an AudioInputStream,
     * param@ listener the AudioClipPlayer listener. */
   public AudioClipPlayer(AudioInputStream audio_inputstream, AudioClipPlayerListener listener)
   {  init(audio_inputstream,listener);
   }


   /** Inits the AudioClipPlayer. */
   private void init(AudioInputStream audio_inputstream, AudioClipPlayerListener listener)
   {  this.listener=listener;
      if (audio_inputstream!=null)
      try
      {  AudioFormat format=audio_inputstream.getFormat();
         DataLine.Info info=new DataLine.Info(Clip.class,format);
         clip=(Clip)AudioSystem.getLine(info);
         clip.addLineListener(this);
         clip.open(audio_inputstream);
      }
      catch (Exception e) { e.printStackTrace(); }
   }


   /** Sets loop counter.
     * @param n The number of plays (0 for playing continuously). */
   public AudioClipPlayer setLoopCount(int n)
   {  loop_count=n;
      return this;
   }


   /** Loops the sound until stopped. */
   public AudioClipPlayer setLoop()
   {  return setLoopCount(0);
   }


   /** Sets the volume gain, between -1 (min) and +1 (max). Value 0 corrisponds to the original volume level. */
   public AudioClipPlayer setVolumeGain(float volume)
   {  //System.err.println("DEBUG: set clip volume gain (linear): "+volume);        
      // try to set the overall gain of the line
      try
      {  FloatControl vc=(FloatControl)clip.getControl(FloatControl.Type.MASTER_GAIN);
         if(vc!=null)
         {  double linear_max=Math.exp((vc.getMaximum()/10)*Math.log(10));
            double linear_min=Math.exp((vc.getMinimum()/10)*Math.log(10));   
            double linear_level=(volume>=0)? volume*(linear_max-1)+1 : 1+volume*(1-linear_min);
            double level=10*Math.log(linear_level)/Math.log(10);
            //System.err.println("DEBUG: set clip volume gain (dB): "+level);
            vc.setValue((float)level);
         }
      }
      catch (Exception e) // getControl may throw IllegalArgumentException
      {  //e.printStackTrace();
         //System.err.println("WARNING: AudioClipPlayer: failed while trying to set volume gain.");
      }
      return this;
   }


   /** Gets the volume gain. */
   public float getVolumeGain()
   {  float volume=0;
      // try to get the overall gain of the line
      try
      {  FloatControl vc=(FloatControl)clip.getControl(FloatControl.Type.MASTER_GAIN);
         if(vc!=null)
         {  float level=vc.getValue();
            //System.err.println("DEBUG: get clip volume (dB): "+level);
            double linear_max=Math.exp((vc.getMaximum()/10)*Math.log(10));
            double linear_min=Math.exp((vc.getMinimum()/10)*Math.log(10));
            double linear_level=Math.exp((level/10)*Math.log(10));
            volume=(float)((linear_level>=1)? (linear_level-1)/linear_max : (1-linear_level)/linear_min);
         }
      }
      catch (Exception e) // getControl may throw IllegalArgumentException
      {  //e.printStackTrace();
         //System.err.println("WARNING: AudioClipPlayer: failed while trying to get volume gain.");
      }
      //System.err.println("DEBUG: get clip volume gain (linear): "+volume);
      return volume;
   }


   /** Sets the volume level, between 0 (min) and 1 (max). */
   /*public AudioClipPlayer setVolume(float volume)
   {  // try to set volume of the line
      try
      {  FloatControl vc=(FloatControl)clip.getControl(FloatControl.Type.VOLUME);
         if(vc!=null) vc.setValue(volume);
      }
      catch (Exception e) // getControl may throw IllegalArgumentException
      {  //e.printStackTrace();
         System.err.println("WARNING: AudioClipPlayer: failed while trying to set volume level.");
      }
      return this;
   }*/


   /** Gets the volume level. */
   /*public float getVolume()
   {  return clip.getLevel();
   }*/


   /** Plays it. */
   public AudioClipPlayer play()
   {  if (clip!=null)
      {  if (loop_count==1) clip.start();
         else
         if (loop_count<=0) clip.loop(Clip.LOOP_CONTINUOUSLY);
         else
         if (loop_count>0) clip.loop(loop_count-1);
      }
      return this;
   }


   /** Plays it n times.
     * @param n The number of plays (0 for playing continuously). */
   public AudioClipPlayer play(int n)
   {  setLoopCount(n).play();
      return this;
   }


   /** Pauses it. */
   public AudioClipPlayer pause()
   {  if (clip!=null) clip.stop();
      return this;
   }


   /** Stops it. */
   public AudioClipPlayer stop()
   {  if (clip!=null)
      {  clip.stop();
         clip.setMicrosecondPosition(0);
      }
      return this;
   }


   /** Goes to a time position. */
   public AudioClipPlayer goTo(long millisec)
   {  if (clip!=null) clip.setMicrosecondPosition(millisec);
      return this;
   }


   /** Close it. */
   public void close()
   {  clip.close();
      clip=null;
   }


   /** From LineListener. Called when an event occurs. */
   public void update(LineEvent event)
   {  if (event.getType().equals(LineEvent.Type.STOP))
      {  //clip.close();
         if (listener!=null) listener.onAudioClipStopped(this);
      }
   }


   // ***************************** MAIN *****************************
   
   /** The main method. */
    public static void main(String[] args)
    {
        if (args.length<1)
        {
            System.out.println("AudioClipPlayer: usage:\n  java AudioClipPlayer <filename>");
            System.exit(0);
        }

        AudioClipPlayer p=new AudioClipPlayer(args[0],null);
        p.play();
        //try { Thread.sleep(3000); } catch (Exception e) { e.printStackTrace(); }
        //p.stop(); 
    }
}


