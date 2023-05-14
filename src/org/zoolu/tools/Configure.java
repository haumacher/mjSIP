/*
 * Copyright (C) 2005 Luca Veltri - University of Parma - Italy
 * 
 * This file is part of MjSip (http://www.mjsip.org)
 * 
 * MjSip is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * MjSip is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with MjSip; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 * 
 * Author(s):
 * Luca Veltri (luca.veltri@unipr.it)
 */

package org.zoolu.tools;


import java.io.*;
import java.net.URL;


/** Configure helps the loading and saving of configuration data.
  */
public class Configure
{

   /** String 'NONE' used as undefined value (i.e. null). */
   public static String NONE="NONE";

   /** The object that should be configured */
   Configurable configurable;

   
   /** Parses a single text line (read from the config file) */
   protected void parseLine(String line)
   {  // parse the text line..
   }


   /** Converts the entire object into lines (to be saved into the config file) */
   protected String toLines()
   {  // convert the object into to one or more text line..
      return "";
   }


   /** Costructs a Configure container */
   protected Configure()
   {  this.configurable=null;
   }


   /** Costructs a Configure container */
   public Configure(Configurable configurable, String file)
   {  this.configurable=configurable;
      loadFile(file);
   }

       
   /** Loads Configure attributes from the specified <i>file</i> */
   protected void loadFile(String file)
   {  if (file==null)
      {  return;
      }
      //else
      try
      {  readFrom(new FileReader(file));
      }
      catch (Exception e)
      {  System.err.println("WARNING: error reading file \""+file+"\"");
         return;
      }
   }


   /** Loads Configure attributes from the specified URL <i>url</i> */
   protected void loadFile(URL url)
   {  if (url==null)
      {  return;
      }
      //else
      try
      {  readFrom(new InputStreamReader(url.openStream()));
      }
      catch (Exception e)
      {  System.err.println("WARNING: error reading from \""+url+"\"");
         return;
      }
   }


   /** Saves Configure attributes on the specified <i>file</i> */
   protected void saveFile(String file)
   {  if (file==null) return;
      //else
      try
      {  writeTo(new FileWriter(file));
      }
      catch (IOException e)
      {  System.err.println("ERROR writing on file \""+file+"\"");
      }         
   }

   
   /** Reads Configure attributes from the specified Reader <i>rd</i> */
   protected void readFrom(Reader rd) throws java.io.IOException
   {  BufferedReader in=new BufferedReader(rd);           
      while (true)
      {  String line=null;
         try { line=in.readLine(); } catch (Exception e) { e.printStackTrace(); System.exit(0); }
         if (line==null) break;
      
         if (!line.startsWith("#"))
         {  if (configurable==null) parseLine(line); else configurable.parseLine(line);
         }
      } 
      in.close();
   }


   /** Writes Configure attributes to the specified Writer <i>wr</i> */
   protected void writeTo(Writer wr) throws java.io.IOException
   {  BufferedWriter out=new BufferedWriter(wr);
      out.write(toLines());
      out.close();
   }

}
