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



/** Class Log allows the printing of log messages onto standard output
  * or files or any PrintStream.
  * <p>
  * Every Log has a <i>verbose_level</i> associated with it;
  * any log request with <i>logl_evel</i> less or equal
  * to the <i>verbose_level</i> is logged.
  * <br/>
  * Verbose level 0 indicates no log. The log levels should be greater than 0.
  */
public class Log
{
   /********************************* Statics ********************************/

   /** Default level for hight priority logs. */
   public static final int LEVEL_HIGH=1;
   /** Default level for medium priority logs. */
   public static final int LEVEL_MEDIUM=3;
   /** Default level for low priority logs. */
   public static final int LEVEL_LOW=5;  
   /** Default level for very low priority logs. */
   public static final int LEVEL_LOWER=9; 


   /******************************* Attributes *******************************/

   /** (static) Default maximum log file size (4MB) */
   //public static final long MAX_SIZE=4096*1024; // 4MB
   public static final long MAX_SIZE=1024*1024; // 1MB

   /** The log output stream */
   PrintStream out_stream;

   /** The <i>verbose_level</i>.
     * <p>Only messages with a level less or equal this <i>verbose_level</i>
     * are effectively logged */
   int verbose_level;
   
   /** The maximum size of the log stream/file [bytes]
     * Value -1 indicates no maximum size */
   long max_size;
     
   /** Whether messages are logged */
   boolean pause=false;

   /** Whether writing a timestamp header */
   boolean timestamp=true;

   /** The char counter of the already logged data */
   long counter=0;


   /****************************** Constructors ******************************/

   /** Associates a new Log to another Log.
     * The <i>verbose_level</i> is incremented of <i>verbose_offset</i>. */
   /*public Log(Log log, int verbose_offset)
   {  init(log.out_stream,log.verbose_level+verbose_offset,log.max_size);
   }*/

   /** Associates a new Log to the PrintStream <i>out_stream</i>.
     * Log size has no bound */
   public Log(PrintStream out_stream, int verbose_level)
   {  init(out_stream,verbose_level,-1);
   }

   /** Associates a new Log to the file <i>file_name</i>.
     * Log size is limited to the MAX_SIZE. */
   public Log(String file_name, int verbose_level)
   {  PrintStream os=null;
      if (verbose_level>0)
      {  try { os=new PrintStream(new FileOutputStream(file_name)); } catch (IOException e) { e.printStackTrace(); }
         init(os,verbose_level,MAX_SIZE);
      }
   }

   /** Associates a new Log to the file <i>file_name</i>.
     * Log size is limited to <i>max_size</i> [bytes]. */
   public Log(String file_name, int verbose_level, long max_size)
   {  PrintStream os=null;
      if (verbose_level>0)
      {  try { os=new PrintStream(new FileOutputStream(file_name)); } catch (IOException e) { e.printStackTrace(); }
         init(os,verbose_level,max_size);
      }
      else
      {  init(null,0,0);
      }
   }

   /** Associates a new Log to the file <i>filename</i>.
     * The file is opened in rewrite or append mode depending on
     * the <i>append</i> parameter value.
     * Log size is limited to <i>logsize</i> [bytes]. */
   public Log(String file_name, int verbose_level, long max_size, boolean append)
   {  PrintStream os=null;
      if (verbose_level>0)
      {  try { os=new PrintStream(new FileOutputStream(file_name,append)); } catch (IOException e) { e.printStackTrace(); }
         init(os,verbose_level,max_size);
      }
      else
      {  init(null,0,0);
      }
   }


   /**************************** Protected methods ****************************/

   /** Initializes the log */
   protected void init(PrintStream out_stream, int verbose_level, long max_size) 
   {  this.out_stream=out_stream;
      this.verbose_level=verbose_level;
      this.max_size=max_size;
   }

   /** Flushes */
   protected Log flush()
   {  if (verbose_level>0) out_stream.flush();
      return this;
   }


   /***************************** Public methods *****************************/

   /** Whether writing a timestamp header. */
   public void setTimestamp(boolean timestamp) 
   {  this.timestamp=timestamp;
   }

   /** Whether stopping logging new message. */
   public void setPause(boolean pause) 
   {  this.pause=!pause;
   }

   /** Closes the log. */
   public void close() 
   {  if (out_stream!=null) out_stream.close();
      out_stream=null;
   }

   /** Prints the <i>message</i> if the Log <i>verbose_level</i> is greater than 0. */
   public Log println(String message)
   {  return println(message,1);
   }

   /** Prints the <i>message</i> if <i>level</i> is less than or equal to the <i>verbose_level</i>. */
   public Log println(String message, int level)
   {  return print(message+"\r\n",level).flush();
   }

   /** Prints the <i>message</i> if the Log <i>verbose_level</i> is greater than 0. */
   public Log print(String message)
   {  return print(message,1);
   }

   /** Prints the <i>message</i> if <i>level</i> is less than or equal to the <i>verbose_level</i>. */
   synchronized public Log print(String message, int level)
   {  if (out_stream!=null && level<=verbose_level)
      {  if (timestamp) message=System.currentTimeMillis()+": "+message;
         out_stream.print(message);
         if (max_size>=0)
         {  counter+=message.length();
            if (counter>max_size)
            {  out_stream.println("\r\n----MAXIMUM LOG SIZE----\r\nSuccessive logs are lost.");
            }
         }
      }
      return this;
   }

}
