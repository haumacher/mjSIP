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




/** GarbageCollectorWatcher periodically frees the heap (invoking the garbage collection)
  * and writes a dump onto a Log.
  */
public abstract class GarbageCollectorWatcher implements GarbageCollectorListener
{
   /** GarbageCollector */
   GarbageCollector gc;

   /** Log */
   Log log;

   /** Log level */
   int log_level;


   /** Creates a new GarbageCollectorWatcher.
     * @param time the time between two GC (in millisecs)
     * @param log the logger */
   public GarbageCollectorWatcher(long time, Log log)
   {  init(time,log,1);
   }


   /** Creates a new GarbageCollectorWatcher.
     * @param time the time between two GC (in millisecs)
     * @param log the logger
     * @param log_level the log level */
   public GarbageCollectorWatcher(long time, Log log, int log_level)
   {  init(time,log,log_level);
   }


   /** Inits the GarbageCollectorWatcher. */
   private void init(long time, Log log, int log_level)
   {  this.log=log;
      this.log_level=log_level;
      gc=new GarbageCollector(time,this);
   }


   /** From GarbageCollectorListener. Before GC is performed. */
   public void onBeforeGC(GarbageCollector gb)
   {  if (log!=null) log.print("--------------- BEFORE GC ---------------\r\n",log_level);
      doSomething();
   }


   /** From GarbageCollectorListener. After GC has been performed. */
   public void onAfterGC(GarbageCollector gb)
   {  if (log!=null) log.print("--------------- AFTER GC ----------------\r\n",log_level);
      doSomething();
   }


   /** Does something. */
   public abstract void doSomething();
   
}
      