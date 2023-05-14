/*
 * Copyright (C) 2008 Luca Veltri - University of Parma - Italy
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
import java.util.Date;



/** Class Logger enriches the access to a Log.
  * It supports a log level offset and a log header tag,
  * and provides more printing methods.
  */
public class Logger
{
   /******************************* Attributes *******************************/

   /** The Log. */
   Log log;

   /** The <i>level_offset</i> added to the specified <i>log_level</i>. */
   int level_offset;

   /** A tag printed as header of each log messages. */
   String tag;



   /** Creates a new Logger. */
   public Logger(Log log)
   {  this.log=log;
      this.tag=null;
      this.level_offset=0;
   }

   /** Creates a new Logger. */
   public Logger(Log log, String tag, int level_offset)
   {  this.log=log;
      this.tag=tag;
      this.level_offset=level_offset;
   }

   /** Gets the Log. */
   public Log getLog()
   {  return log;
   }

   /** Logs the Exception. */
   public Logger printException(Exception e)
   {  return printException(e,1);
   }

   /** Logs the Exception. */
   public Logger printException(Exception e, int level)
   {  return println("Exception: "+ExceptionPrinter.getStackTraceOf(e),level);
   }

   /** Prints the <i>message</i> if the Log <i>verbose_level</i> is greater than 0. */
   public Logger println(String message)
   {  return println(message,1);
   }

   /** Prints the <i>message</i> if <i>level</i> is less than or equal to the <i>verbose_level</i>. */
   public Logger println(String message, int level)
   {  if (tag!=null) log.println(tag+message,level_offset+level);
      else log.println(message,level_offset+level);
      return this;
   }

   /** Prints the <i>message</i> if the Log <i>verbose_level</i> is greater than 0. */
   public Logger print(String message)
   {  return print(message,1);
   }

   /** Prints the <i>message</i> if <i>level</i> is less than or equal to the <i>verbose_level</i>. */
   public Logger print(String message, int level)
   {  if (tag!=null) log.print(tag+message,level_offset+level);
      else log.print(message,level_offset+level);
      return this;
   }

}
