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

package org.zoolu.tools;




/** ContinuousPipe.
  * <p/>
  * A ContinuousPipe is a FIFO line, where bytes can be written in and read out.
  * <br/>
  * If a byte is written into a full pipe, the first byte is first removed.
  * <br/>
  * If a byte is read from an empty pipe, a zero-value byte is read.
  */
public class ContinuousPipe extends Pipe
{  
   /** auxiliary buffer */
   protected byte[] aux;



   /** Creates a new ContinuousPipe. */
   public ContinuousPipe(int size)
   {  super(size);
      aux=new byte[size];
   }

   /** Writes the specified byte to this pipe. */
   public synchronized void write(byte b)
   {  if (freespace()==0) read();
      super.write(b);
   }

   /** Writes <i>length</i> bytes into this pipe
     * from the <i>buff</i> array starting from position <i>offset</i>. */
   public synchronized void write(byte[] buff, int offset, int length)
   {  int freespace=freespace();
      if (freespace<length) read(aux,0,length-freespace);
      super.write(buff,offset,length);
   }

   /** Reads the next byte of data from the pipe. */
   public synchronized byte read()
   {  if (length()>0) return super.read();
      //else
      return 0;
   }

   /** Reads <i>length</i> bytes from the pipe and puts them
     * into the <i>buff</i> array starting from position <i>offset</i>. */
   public synchronized int read(byte[] buff, int offset, int length)
   {  int available=length();
      if (available>=length) return super.read(buff,offset,length);
      //else
      super.read(buff,offset,available);
      int diff=length-available;
      for (int i=0; i<diff; i++) buff[offset+available+i]=0;
      return length;
   }

}
