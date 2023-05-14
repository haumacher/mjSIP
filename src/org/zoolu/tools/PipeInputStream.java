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


/** PipeInputStream is an InputStream that can be used to read from a Pipe.
  * <p/>
  * It is equivalend to java.io.PipedInputStream but it seems to be quite faster. 
  */
public class PipeInputStream extends InputStream
{  
   /** Pipe */
   Pipe pipe;


   /** Creates a new PipeInputStream. */
   public PipeInputStream(Pipe pipe)
   {  this.pipe=pipe;
   }

   /** Returns the number of bytes that can be read (or skipped over) from this input stream without blocking by the next caller of a method for this input stream. */
   public int available() throws IOException
   {  return pipe.length();
   }

   /** Reads the next byte of data from the input stream. */
   public int read() throws IOException
   {  return pipe.read();
   }

   /** Reads some number of bytes from the input stream and stores them into the buffer array b. */
   public int read(byte[] buff) throws IOException
   {  //int length=(b.length<pipe.length())? b.length : pipe.length();
      //for (int i=0; i<length; i++) b[i]=pipe.read();
      //return length;
      return pipe.read(buff);
   }

   /** Reads up to len bytes of data from the input stream into an array of bytes. */
   public int read(byte[] buff, int off, int len) throws IOException 
   {  //int length=(len<pipe.length())? len : pipe.length();
      //int end=off+length;
      //for (int i=off; i<end; i++) buff[i]=pipe.read();
      //return length;
      return pipe.read(buff,off,len);
   }

   /** Skips over and discards n bytes of data from this input stream. */
   public long skip(long n) throws IOException
   {  //int length=(n<pipe.length())? (int)n : pipe.length();
      //for (int i=0; i<length; i++) pipe.read();
      //return length;
      return pipe.skip((int)n);
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
   {  pipe=null;
   }
}
