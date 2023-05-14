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




/** Class GarbageCollector can be used to free the heap periodically.
  */
public class GarbageCollector implements TimerListener
{
   /** Time between two dumps (in millisecs) */
   long time;
   
   /** Timer */
   //Timer timer;
   
   /** GarbageCollector listener */
   GarbageCollectorListener listener;

   
   /** Creates a new GarbageCollector.
     * @param time the time between two GC (in millisecs) */
   public GarbageCollector(long time)
   {  init(time,null);
   }


   /** Creates a new GarbageCollector.
     * @param time the time between two GC (in millisecs) 
     * @param listener GarbageCollector listener */
   public GarbageCollector(long time, GarbageCollectorListener listener)
   {  init(time,listener);
   }


   /** Inits the GarbageCollector. */
   private void init(long time, GarbageCollectorListener listener)
   {  this.time=time;
      this.listener=listener;
      //timer=new Timer(time,this);
      //timer.start();
      (new Timer(time,this)).start();
   }


   /** Starts the GC. */
   public void gc()
   {  if (listener!=null) listener.onBeforeGC(this);
      System.gc();
      if (listener!=null) listener.onAfterGC(this);
   }


   /** From TimerListener. When the Timer exceeds. */
   public void onTimeout(Timer t)
   {  gc();
      //timer=new Timer(time,this);
      //timer.start();
      (new Timer(time,this)).start();
   }

}
