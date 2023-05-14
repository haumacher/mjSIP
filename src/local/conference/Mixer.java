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


import java.io.*;
import java.util.Hashtable;
import java.util.Enumeration;


/** Mixer is a simple audio mixer with M input lines (OutputStreams)
  * and N output lines (InputStreams). 
  * <p/>
  * Each line has an identifier (Object) used as key when adding or
  * removing the line. 
  * <p/>
  * It works only with PCM ulaw 8bit input/output audio streams. 
  */
public class Mixer
{
   /** Whether working in debug mode. */
   public static boolean DEBUG=false;

   /** The splitter lines, as table of (Object)id-->(SplitterLine)line */
   Hashtable splitter_lines;
 
   /** The mixer lines, as table of (Object)id-->(MixerLine)line */
   Hashtable mixer_lines;

  
   /** Creates a new Mixer. */
   public Mixer()
   {  splitter_lines=new Hashtable();
      mixer_lines=new Hashtable();
   }


   /** Close the Mixer. */
   public void close() throws IOException
   {  for (Enumeration e=splitter_lines.elements(); e.hasMoreElements(); )
      {  ((SplitterLine)e.nextElement()).close();
      }
      for (Enumeration e=mixer_lines.elements(); e.hasMoreElements(); )
      {  ((MixerLine)e.nextElement()).close();
      }
      mixer_lines=null;
      splitter_lines=null;
   }


   /** Adds a new input line.
     * @return It returns the OutputStream where the new input-line can write incoming data. */
   public OutputStream newInputLine(Object id) throws IOException
   {  //System.err.println("Mixer: add input line: "+id);
      SplitterLine sl=newSplitterLine(id);
      for (Enumeration e=mixer_lines.keys(); e.hasMoreElements(); )
      {  Object mid=e.nextElement();
         if (!mid.equals(id))
         {  //PipedInputStream is=new PipedInputStream();
            //PipedOutputStream os=new PipedOutputStream(is);
            ExtendedPipedInputStream is=new ExtendedPipedInputStream();
            ExtendedPipedOutputStream os=new ExtendedPipedOutputStream(is);
            //System.err.println("Mixer: SL("+id+"): add line: "+mid);
            sl.addLine(mid,os);
            //System.err.println("Mixer: ML("+mid+"): add line: "+id);
            ((MixerLine)mixer_lines.get(mid)).addLine(id,is);
         }
      }
      splitter_lines.put(id,sl);
      return sl;
   }


   /** Gets a new SplitterLine.
     * This method is used by method newInputLine() and can be re-defined 
     * by a class that extends Mixer in order to implement new slitting mechanisms.
     * @return It returns the new SplitterLine. */
   protected SplitterLine newSplitterLine(Object id)
   {  return new SplitterLine(id);
   }


   /** Removes an input line. */
   public void removeInputLine(Object id)
   {  //System.err.println("Mixer: remove input line: "+id);
      SplitterLine sl=(SplitterLine)splitter_lines.get(id);
      splitter_lines.remove(id);
      /*for (Enumeration e=mixer_lines.elements(); e.hasMoreElements(); )
      {  System.err.println("Mixer: ML(?): remove line: "+id);
         ((MixerLine)e.nextElement()).removeLine(id);
      }*/
      for (Enumeration e=mixer_lines.keys(); e.hasMoreElements(); )
      {  Object mid=e.nextElement();
         if (!mid.equals(id))
         {  //System.err.println("Mixer: SL("+id+"): remove line: "+mid);
            sl.removeLine(mid);
            //System.err.println("Mixer: ML("+mid+"): remove line: "+id);
            ((MixerLine)mixer_lines.get(mid)).removeLine(id);
         }
      }
      try { sl.close(); } catch (Exception e) {}
   }


   /** Adds a new output line.
     * @return It returns the InputStream where the new output-line can read outgoing data. */
   public InputStream newOutputLine(Object id) throws IOException
   {  //System.err.println("Mixer: add output line: "+id);
      MixerLine ml=newMixerLine(id);    
      for (Enumeration e=splitter_lines.keys(); e.hasMoreElements(); )
      {  Object sid=e.nextElement();
         if (!sid.equals(id))
         {  //PipedInputStream is=new PipedInputStream();
            //PipedOutputStream os=new PipedOutputStream(is);
            ExtendedPipedInputStream is=new ExtendedPipedInputStream();
            ExtendedPipedOutputStream os=new ExtendedPipedOutputStream(is);
            //org.zoolu.tools.Pipe pipe=new org.zoolu.tools.Pipe(500);
            //InputStream is=new org.zoolu.tools.PipeInputStream(pipe);
            //OutputStream os=new org.zoolu.tools.PipeOutputStream(pipe);
            //System.err.println("Mixer: ML("+id+"): add line: "+sid);
            ml.addLine(sid,is);
            //System.err.println("Mixer: SL("+sid+"): add line: "+id);
            ((SplitterLine)splitter_lines.get(sid)).addLine(id,os);
         }
      }
      mixer_lines.put(id,ml);
      return ml;
   }


   /** Gets a new MixerLine.
     * This method is used by method newOutputLine() and can be re-defined 
     * by a class that extends Mixer in order to implement new mixing mechanisms.
     * @return It returns a new MixerLine. */
   protected MixerLine newMixerLine(Object id)
   {  return new MixerLine(id);
   }


   /** Removes an output line. */
   public void removeOutputLine(Object id)
   {  //System.err.println("Mixer: remove output line: "+id);
      MixerLine ml=(MixerLine)mixer_lines.get(id);
      mixer_lines.remove(id);
      /*for (Enumeration e=splitter_lines.elements(); e.hasMoreElements(); )
      {  System.err.println("Mixer: SL(?): remove line: "+id);
         ((SplitterLine)e.nextElement()).removeLine(id);
      }*/
      for (Enumeration e=splitter_lines.keys(); e.hasMoreElements(); )
      {  Object sid=e.nextElement();
         if (!sid.equals(id))
         {  //System.err.println("Mixer: ML("+id+"): remove line: "+sid);
            ml.removeLine(sid);
            //System.err.println("Mixer: SL("+sid+"): remove line: "+id);
            ((SplitterLine)splitter_lines.get(sid)).removeLine(id);
         }
      }
      try { ml.close(); } catch (Exception e) {}
   }


   /** Gets a String representation of the Object */
   public String toString()
   {  return "Mixer";
   }
}
