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


/** PipeOutputStream is an OutputStream that can be used to write into a Pipe.
  * <p/>
  * It is equivalend to java.io.PipedOutputStream but it seems to be quite faster. 
  */
public class PipeOutputStream extends OutputStream
{  
   /** Pipe */
   Pipe pipe;


   /** Creates a new PipeOutputStream. */
   public PipeOutputStream(Pipe pipe)
   {  this.pipe=pipe;
   }

   /** Flushes this output stream. */
   public void flush() throws IOException
   {  
   }
   
   /** Writes b.length bytes from the specified byte array to this output stream. */
   public void write(byte[] b) throws IOException
   {  //for (int i=0; i< b.length; i++) pipe.write(b[i]);
      pipe.write(b);
   }
   
   /** Writes len bytes from the specified byte array starting at offset off to this output stream. */
   public void write(byte[] buff, int off, int len) throws IOException
   {  //int end=off+len;
      //for (int i=off; i<end; i++) pipe.write(buff[i]);
      pipe.write(buff,off,len);
   }
  
   /** Writes the specified byte to this output stream. */
   public void write(int b) throws IOException
   {  pipe.write((byte)b);
   }

   /** Closes this pipe stream. */
   public void close() throws IOException
   {  pipe=null;
   }

}
