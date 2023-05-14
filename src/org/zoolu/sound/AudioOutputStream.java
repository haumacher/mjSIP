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


import javax.sound.sampled.AudioFormat;
import java.io.OutputStream;
import java.io.IOException;


/** AudioOutputStream has been created to provide a useful API for audio output
  * equivalent to javax.sound.sampled.AudioInputStream used for audio input.
  */
public class AudioOutputStream extends OutputStream
{
   /** Audio format */
   AudioFormat format;
   
   /** OutputStream */
   OutputStream os;

  
   /** Creates a new AudioOutputStream. */
   protected AudioOutputStream(AudioFormat format)
   {  this.format=format;
      this.os=null;
   }


   /** Creates a new AudioOutputStream. */
   public AudioOutputStream(OutputStream os, AudioFormat format)
   {  this.format=format;
      this.os=os;
   }


   /** Gets the AudioFormat. */
   public AudioFormat getFormat()
   {  return format;
   }


   /** Closes this output stream and releases any system resources associated with this stream. */
   public void close() throws IOException
   {  os.close();
   }

  
   /** Flushes this output stream and forces any buffered output bytes to be written out. */
   public void flush() throws IOException
   {  os.flush();
   }

   
   /** Writes b.length bytes from the specified byte array to this output stream. */
   public void write(byte[] buf) throws IOException
   {  os.write(buf);
   }

   
   /** Writes len bytes from the specified byte array starting at offset off to this output stream. */
   public void write(byte[] buf, int off, int len) throws IOException
   {  os.write(buf,off,len);
   }

  
   /** Writes the specified byte to this output stream. */
   public void write(int b) throws IOException
   {  os.write(b);
   }

}
