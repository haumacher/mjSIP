/*
 * Copyright (C) 2006 Luca Veltri - University of Parma - Italy
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




/** MonitoredObjectWatcher writes a dump of all monitored class onto a Log.
  * It may do this periodically, if created with a <i>time</i> value greater than 0).
  */
public class MonitoredObjectWatcher implements TimerListener//, GarbageCollectorListener
{
   /** Time between two dumps (in millisecs) */
   long time;
   
   /** Log */
   Log log;
   
   /** Log level */
   int log_level;


   /** Creates a new MonitoredObjectWatcher.
     * @param time the time between two dumps (in millisecs)
     * @param log the logger */
   public MonitoredObjectWatcher(long time, Log log)
   {  init(time,log,1);
   }


   /** Creates a new MonitoredObjectWatcher.
     * @param time the time between two dumps (in millisecs)
     * @param log the logger
     * @param log_level the log level */
   public MonitoredObjectWatcher(long time, Log log, int log_level)
   {  init(time,log,log_level);
   }


   /** Inits the MonitoredObjectWatcher. */
   private void init(long time, Log log, int log_level)
   {  this.time=time;
      this.log=log;
      this.log_level=log_level;
      if (time>0) (new Timer(time,this)).start();
   }


   /** Logs a dump. */
   public void dump()
   {  if (log!=null)
      {  log.print("Memory dump:\r\n"+getDump()+"\r\n",log_level);
      }
   }


   /** Gets a dump. */
   private String getDump()
   {  StringBuffer sb=new StringBuffer();
      sb.append(MonitoredObject.getDump());
      sb.append("Threads: "+Thread.activeCount()+"\r\n");
      long ec=MonitoredObject.getExceptionCounter();
      if (ec>0) sb.append("Exceptions: "+ec+"\r\n");
      return sb.toString();
   }


   /** From TimerListener. When the Timer exceeds. */
   public void onTimeout(Timer t)
   {  dump();
      if (time>0) (new Timer(time,this)).start();
   }

}
      