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


/** Class InnerTimerST implements a single-thread timer.
  * InnerTimerST uses the java.util.Timer in order to share the same thread
  * with all other InnerTimerST instances (that are simply viewed as java.util.Timer's tasks).
  * <p/>
  * If an error occurs while scheduling the InnerTimerST,
  * a new Timer is istatiated and the InnerTimerST is re-scheduled.
  */
class InnerTimerST extends java.util.TimerTask
{
   /** Whether running as separate daemon */
   static final boolean IS_DAEMON=true;

   /** Maximum number of attempts to schedule an instance of InnerTimerST */
   static final int MAX_ATTEPTS=2;

   /** Timer used to schedule all InnerTimerST instances */
   static java.util.Timer single_timer=new java.util.Timer(IS_DAEMON);

  
   /** Timer listener */
   InnerTimerListener listener;

   
   /** Creates a new InnerTimerST. */
   public InnerTimerST(long timeout, InnerTimerListener listener)
   {  this.listener=listener;
      int attempts=0;
      boolean success=false;
      while (!success && attempts<MAX_ATTEPTS)
      {  try 
         {  single_timer.schedule(this,timeout);
            success=true;
         }
         catch (IllegalStateException e)
         {  attempts++;
            single_timer=new java.util.Timer(IS_DAEMON);
         }
      }
   }  


   /** From TimerTask. The action to be performed by this timer task. */
   public void run()
   {  if (listener!=null)
      {  listener.onInnerTimeout();
         listener=null;
      }
   }   
}

