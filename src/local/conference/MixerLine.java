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


import org.zoolu.sound.*;

import java.io.*;
import java.util.Hashtable;
import java.util.Enumeration;


/** MixerLine is a simple audio mixer with N input lines (InputStream)
  * and one output line (the MixerLine itself, that extends class InputStream). 
  * <p/>
  * Each input line has an identifier (Object) used as key when adding or
  * removing the line.
  * <p/>
  * MixerLine works with with PCM ulaw 8bit audio streams.
  */
public class MixerLine extends InputStream
{
   /** Whether working in debug mode. */
   public static boolean DEBUG=false;

   /** MixerLine identifier. */
   protected Object mixer_id;

   /** The input lines, as table of (Object)id-->(InputStream)line. */
   protected Hashtable input_lines;
 
  
   /** Creates a new MixerLine. */
   public MixerLine(Object mixer_id)
   {  this.mixer_id=mixer_id;
      input_lines=new Hashtable();
      if (DEBUG) System.err.println("MixerLine-"+mixer_id+": new");
   }

   /** Creates a new MixerLine. */
   public MixerLine(Object mixer_id, Hashtable input_lines)
   {  this.mixer_id=mixer_id;
      this.input_lines=input_lines;
      if (DEBUG) System.err.println("MixerLine-"+mixer_id+": new");
   }


   /** Adds a new input line. */
   public void addLine(Object id, InputStream is)
   {  if (DEBUG) System.err.println("MixerLine-"+mixer_id+": add: "+id+" "+is);
      input_lines.put(id,is);
   }

   /** Removes an input line. */
   public void removeLine(Object id)
   {  if (DEBUG) System.err.println("MixerLine-"+mixer_id+": remove: "+id);
      input_lines.remove(id);
   }


   /** Gets all input lines. */
   /*protected Enumeration getLines()
   {  return input_lines.elements();
   }*/


   /** Returns the number of bytes that can be read (or skipped over) from this input stream without blocking by the next caller of a method for this input stream. */
   public int available() throws IOException
   {  int max=0;
      for (Enumeration e=input_lines.elements(); e.hasMoreElements(); )
      {  int n=((InputStream)e.nextElement()).available();
         if (n>max) max=n;
      }
      return max;
   }
   
   /** Closes this input stream and releases any system resources associated with the stream. */
   public void close() throws IOException
   {  for (Enumeration e=input_lines.elements(); e.hasMoreElements(); )
      {  ((InputStream)e.nextElement()).close();
      }
      input_lines=null;
   }
   
   /** Marks the current position in this input stream. */
   public void mark(int readlimit)
   {  for (Enumeration e=input_lines.elements(); e.hasMoreElements(); )
      {  ((InputStream)e.nextElement()).mark(readlimit);
      }
   }
   
   /** Tests if this input stream supports the mark and reset methods. */
   public boolean markSupported()
   {  boolean supported=true;
      for (Enumeration e=input_lines.elements(); e.hasMoreElements(); )
      {  if (!((InputStream)e.nextElement()).markSupported()) supported=false;
      }
      return supported;
   }
   
   /** Reads the next byte of data from the input stream. */
   public int read() throws IOException
   {  int sum=0;
      int count=0;
      int err_code=0;
      for (Enumeration e=input_lines.elements(); e.hasMoreElements(); )
      {  InputStream is=(InputStream)e.nextElement();
         if (is.available()>0)
         {  int value=is.read();
            if (value>=0)
            {  count++;
               sum+=G711.ulaw2linear(value);
            }
            else err_code=value;
         }
      }
      if (DEBUG) if (err_code!=0) System.err.println("MixerLine-"+mixer_id+": error code "+err_code);
      if (count>0 || err_code==0) return G711.linear2ulaw(sum);
      else return err_code;
   }
   
   /** Reads some number of bytes from the input stream and stores them into the buffer array b. */
   public int read(byte[] b) throws IOException
   {  //System.err.print("o");
      int ret=super.read(b);
      //System.err.print(".");
      return ret;
   }
   
   /** Reads up to len bytes of data from the input stream into an array of bytes. */
   public int read(byte[] b, int off, int len) throws IOException
   {  //System.err.print("o");
      int ret=super.read(b,off,len);
      //System.err.print(".");
      return ret;
   }
   
   /** Repositions this stream to the position at the time the mark method was last called on this input stream. */
   public void reset() throws IOException
   {  for (Enumeration e=input_lines.elements(); e.hasMoreElements(); )
      {  ((InputStream)e.nextElement()).reset();
      }
   }
   
   /** Skips over and discards n bytes of data from this input stream. */
   public long skip(long n) throws IOException
   {  for (Enumeration e=input_lines.elements(); e.hasMoreElements(); )
      {  ((InputStream)e.nextElement()).skip(n);
      }
      return n;
   }

   /** Gets a String representation of the Object */
   public String toString()
   {  return "MixerLine-"+mixer_id;
   }
}