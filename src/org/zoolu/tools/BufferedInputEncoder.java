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

package org.zoolu.tools;


import java.io.*;


/** Buffered InputStream performing InputStream encoding.
  * It wraps an inner InputStream, reads chunks of data and performs encoding operation.
  */
public class BufferedInputEncoder extends InputStream
{  
   /** Inner InputStream */
   InputStream is;

   /** Encoder */
   Encoder encoder;

   /** Buffer */
   byte[] reading_buffer;

   /** Buffer */
   byte[] encoded_buffer;

   /** Buffer index */
   int buffer_index;

   /** Buffer size */
   int buffer_size;


   /** Creates a new BufferedInputEncoder. */
   public BufferedInputEncoder(InputStream is, Encoder encoder, int reading_size, int encoded_size)
   {  this.is=is;
      this.encoder=encoder;
      reading_buffer=new byte[reading_size];
      encoded_buffer=new byte[encoded_size];
      buffer_index=0;
      buffer_size=0;
   }

   /** Returns the number of bytes that can be read (or skipped over) from this input stream without blocking by the next caller of a method for this input stream. */
   public int available() throws IOException
   {  return is.available()+buffer_size-buffer_index;
   }

   /** Reads the next byte of data from the input stream. */
   public int read() throws IOException
   {  if (buffer_index==buffer_size)
      {  int len=is.read(reading_buffer,0,reading_buffer.length);
         buffer_size=encoder.encode(reading_buffer,0,len,encoded_buffer,0);
         buffer_index=0;
         if (buffer_size<=0) return -1;
      }
      return encoded_buffer[buffer_index++];
   }

   /** Reads some number of bytes from the input stream and stores them into the buffer array b. */
   public int read(byte[] buff) throws IOException
   {  return read(buff,0,buff.length);
   }

   /** Reads up to len bytes of data from the input stream into an array of bytes. */
   public int read(byte[] buff, int off, int len) throws IOException 
   {  int i=off;
      int end=off+len;
      int b=0;
      while (i<end && i<buff.length && (b=read())>=0) buff[i++]=(byte)b;
      return i;
   }

   /** Skips over and discards n bytes of data from this input stream. */
   public long skip(long n) throws IOException
   {  // to do
      return 0;
   }

   /** Tests if this input stream supports the mark and reset methods. */
   public boolean markSupported() 
   {  return false;
   }

   /** Marks the current position in this input stream. */
   public void mark(int readlimit) 
   {  
   }

   /** Repositions this stream to the position at the time the mark method was last called on this input stream. */
   public void reset() throws IOException
   {  
   }

   /** Closes this pipe stream. */
   public void close() throws IOException
   {  is.close();
   }
}
