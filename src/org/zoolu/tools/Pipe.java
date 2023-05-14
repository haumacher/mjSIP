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


//import java.io.InputStream;
//import java.io.OutputStream;


/** Pipe.
  * <p/>
  * A Pipe is a FIFO line, where bytes can be written in and read out.
  * The size of the Pipe is the maximum number of bytes that can be written without read,
  * that is the maximum number of bytes that can be starved within the Pipe.
  * <p/>
  * If a byte is written into a full Pipe, an ArrayIndexOutOfBoundsException is thrown.
  * <br/>
  * If a byte is read from an empty Pipe, an ArrayIndexOutOfBoundsException is thrown.
  */
public class Pipe
{  
   /** Buffer */
   protected byte[] buffer;

   /** Index of the first byte */
   protected int top;

   /** Number of bytes */
   protected int len;

   /** Pipe's InputStream */
   //protected InputStream is=null;

   /** Pipe's InputStream */
   //protected OutputStream os=null;


   /** Creates a new Pipe. */
   public Pipe(int size)
   {  buffer=new byte[size];
      top=0;
      len=0;
   }

   /** Creates a new Pipe. */
   /*public Pipe(byte[] buffer)
   {  this.buffer=buffer;
      top=0;
      len=0;
   }*/

   /** Gets the Pipe's InputStream. */
   /*public InputStream getInputStream()
   {  if (is==null) is=new PipeInputStream(this);
      return is;
   }*/

   /** Gets the Pipe's OutputStream. */
   /*public OutputStream getOutputStream()
   {  if (os==null) os=new PipeOutputStream(this);
      return os;
   }*/

   /** Returns the number of bytes that can be read from this pipe. */
   public int length()
   {  return len;
   }

   /** Returns the number of bytes that can be write to this pipe. */
   public int freespace()
   {  return buffer.length-len;
   }

   /** Writes the specified byte to this pipe. */
   public synchronized void write(byte b)
   {  if (len<buffer.length)
      {  buffer[(top+(len++))%buffer.length]=b;
      }
      else
      {  throw new ArrayIndexOutOfBoundsException("Pipe full ("+length()+")");
      }
   }

   /** Writes the specified array of bytes to this pipe. */
   public void write(byte[] buff)
   {  write(buff,0,buff.length);
   }

   /** Writes <i>length</i> bytes into this pipe
     * from the <i>buff</i> array starting from position <i>offset</i>. */
   public synchronized void write(byte[] buff, int offset, int length)
   {  if ((len+length)<buffer.length)
      {  int cnt=top+len;
         int pos=(cnt)%buffer.length;
         int end=top+len+length;
         for (; cnt<end; cnt++)
         {  buffer[pos++]=buff[offset++];
            if (pos==buffer.length) pos=0;
         }
         len+=length;
      }
      else
      {  throw new ArrayIndexOutOfBoundsException("Pipe full ("+length()+")");
      }
   }

   /** Reads the next byte of data from the pipe. */
   public synchronized byte read()
   {  if (len>0)
      {  byte b=buffer[top];
         if ((++top)==buffer.length) top=0;
         len--;
         return b;
      }
      else
      {  throw new ArrayIndexOutOfBoundsException("Pipe empty ("+length()+")");
      }
   }

   /** Reads an array of bytes from the pipe. */
   public int read(byte[] buff)
   {  return read(buff,0,buff.length);
   }

   /** Reads <i>length</i> bytes from the pipe and puts them
     * into the <i>buff</i> array starting from position <i>offset</i>. */
   public synchronized int read(byte[] buff, int offset, int length)
   {  if (len-length>=0)
      {  int cnt=top;
         int end=top+length;
         for (; cnt<end; cnt++)
         {  buff[offset++]=buffer[top++];
            if (top==buffer.length) top=0;
         }
         len-=length;
         return length;
      }
      else
      {  throw new ArrayIndexOutOfBoundsException("Pipe empty ("+length()+")");
      }
   }

   /** Skips over and discards n bytes of data from this input stream. */
   public long skip(int n)
   {  if (len-n>=0)
      {  len-=n;
         top=(top+n)%buffer.length;
         return n;
      }
      else
      {  throw new ArrayIndexOutOfBoundsException("Pipe empty ("+length()+")");
      }
   }

   /** Closes the Pipe.
     * It actually closes the Pipe's input and output streams. */
   /*public void close()
   {  try {  if (is!=null) is.close(); } catch (Exception e) {}
      try {  if (os!=null) os.close(); } catch (Exception e) {}
   }*/

}
