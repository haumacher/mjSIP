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


//PersonalJava
//import java.util.HashSet;
//import java.util.Iterator;
import org.zoolu.tools.HashSet;
import org.zoolu.tools.Iterator;



/** A Timer is a simple object that fires an onTimeout() method to its TimerListener when the time expires.
  * A Timer have to be explicitely started, and can be halted before it expires.
  */
public class Timer extends org.zoolu.tools.MonitoredObject implements InnerTimerListener
{
   /** Whether using single thread for all timer instances. */
   public static boolean SINGLE_THREAD=true; 

   //HashSet listener_list=null;

   /** Timer listener */
   TimerListener listener;

   /** Timeout value */
   long time;
     
   /** Start time */
   long start_time=0;
     
   /** Timer label (optional) */
   String label;
   
   /** Whether the Timer is (still) active */
   boolean active;


   /** Inits the Timer. */
   void init(long time, String label, TimerListener listener)
   {  //listener_list=new HashSet();
      //if (t_listener!=null) addTimerListener(listener);
      this.listener=listener;
      this.time=time;
      this.label=label;
      this.active=false;
   }  

   /** Creates a new Timer of <i>t_msec</i> milliseconds.
     * The Timer is not started. You need to fire the start() method. */
   /*public Timer(long t_msec)
   {  init(t_msec,null,null);
   }*/ 

   /** Creates a new Timer of <i>t_msec</i> milliseconds, with a label <i>t_event</i>.
     * The Timer is not started. You need to fire the start() method. */
   /*public Timer(long t_msec, String t_label)
   {  init(t_msec,t_label,null);
   }*/  
   
   /** Creates a new Timer of <i>t_msec</i> milliseconds with TimerListener <i>listener</i>
     * The Timer is not started. You need to fire the start() method. */
   public Timer(long t_msec, TimerListener t_listener)
   {  init(t_msec,null,t_listener);
   }  

   /** Creates a new Timer of <i>t_msec</i> milliseconds, with a label <i>t_event</i>, and with TimerListener <i>listener</i>
     * The Timer is not started. You need to fire the start() method. */
   public Timer(long t_msec, String t_label, TimerListener t_listener)
   {  init(t_msec,t_label,t_listener);
   }  

   /** Gets the Timer label. */
   public String getLabel()
   {  return label;
   }

   /** Gets the initial time (in milliseconds). */
   public long getTime()
   {  return time;
   }
   
   /** Gets the remaining time (in milliseconds). */
   public long getExpirationTime()
   {  if (active)
      {  long expire=start_time+time-System.currentTimeMillis();
         return (expire>0)? expire : 0;
      }
      else return time;
   }
   
   /** Adds a new listener (TimerListener). */
   /*public void addTimerListener(TimerListener listener)
   {  listener_list.add(listener);
   }*/

   /** Removes the specific listener (TimerListener). */
   /*public void removeTimerListener(TimerListener listener)
   {  listener_list.remove(listener);
   }*/
   
   /** Stops the Timer. The onTimeout() method will not be fired. */
   public synchronized void halt()
   {  active=false;
      // (CHANGE-040421) now it can free the link to Timer listeners
      //listener_list=null;
      listener=null;
   }

   /** Starts the timer. */
   public synchronized void start()
   {  if (time<0) return;
      // else
      start_time=System.currentTimeMillis();
      active=true;
      if (time>0)
      {  if (SINGLE_THREAD) new InnerTimerST(time,this);
         else new InnerTimerMT(time,this);
      }
      else
      {  // fire now!
         onInnerTimeout();  
      }
   }   


   /** Whether the timer is running. */
   public boolean isRunning()
   {  return active;
   }   


   /** From InnerTimerListener. When the Timeout fires. */
   public synchronized void onInnerTimeout()
   {  //if (active && !listener_list.isEmpty())
      //{  for (Iterator i=listener_list.iterator(); i.hasNext(); )
      //   {  ((TimerListener)i.next()).onTimeout(this);
      //   }
      //}   
      if (active && listener!=null) listener.onTimeout(this);  
      active=false;
      // (CHANGE-040421) now it can free the link to Timer listeners
      //listener_list=null;
      listener=null;
   }   
}

