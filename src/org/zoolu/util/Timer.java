/*
 * Copyright (C) 2016 Luca Veltri - University of Parma - Italy
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

package org.zoolu.util;



import java.util.TimerTask;



/** A Timer is a simple object that fires the {@link TimerListener#onTimeout(Timer)}
  * method when the time expires.
  * Timer has to be explicitly started, and can be halted before it expires.
  * <p>
  * A timer may run is 'daemon' mode or 'non-daemon' mode.
  * <br>
  * In 'daemon' mode, if all program threads terminate, the timer terminates silently,
  * without firing the corresponding timeout callback.
  * <br>
  * Conversely, in 'non-daemon' mode, the program terminates only when the timer
  * expires (or it is explicitly halted).
  */
public class Timer {
	
	/** Whether the default mode is 'daemon', or not */
	public static boolean DEFAULT_DAEMON_MODE=false;

	/** Maximum number of attempts to schedule the task */
	static final int MAX_ATTEPTS=2;
	
	// Non-daemon scheduled tasks:

	/** Current number of total non-daemon scheduled tasks */
	static int scheduled_tasks=0;

	/** Inner non-daemon scheduler. The program terminates only when all non-daemon timers (associated to this scheduler) have ended (for timeout or explicitly halted) */
	static java.util.Timer scheduler=null;

	/** Lock for the non-daemon scheduler */
	static Object scheduler_lock=new Object();

	// Daemon scheduled tasks:

	/** Inner daemon scheduler. Daemon timers (associated to the this scheduler) silently terminate (without firing the corresponding timout callbacks) when all program threads end */
	static java.util.Timer daemon_scheduler=null;


	
	/** Whether running in 'daemon' mode */
	boolean daemon_mode;

	/** Start time */
	long start_time=0;

	/** Timeout value */
	protected long time;
	  
	/** Whether this timer is running */
	protected boolean is_running=false;

	/** Timer listener */
	protected TimerListener listener;
	  


	/** Creates a new Timer.
	  * <p>
	  * The Timer is not automatically started. You need to call the {@link #start()} method.
	  * @param time the timer expiration time in milliseconds
	  * @param listener timer listener */
	public Timer(long time, TimerListener listener) {
		this.listener=listener;
		this.time=time;
	}  

	
	/** Gets the initial time.
	  * @return the initial time in milliseconds */
	public long getTime() {
		return time;
	}

	
	/** Gets the remaining time.
	  * @return the remaining time in milliseconds */
	public long getExpirationTime() {
		if (is_running) {
			long expire=start_time+time-System.currentTimeMillis();
			return (expire>0)? expire : 0;
		}
		else return time;
	}
	

	/** Starts the timer. */
	public void start() {
		start(DEFAULT_DAEMON_MODE);
	}


	/** Starts the timer.
	 * @param daemon_mode whether running in 'daemon' mode
	 * In 'daemon' mode, when all other threads terminate, the program also ends
	 * regardless the timer was still running, and no timeout callback is fired.
	 * In 'non-daemon' mode, the program ends only when all active timers have expired
	 * or explicitly halted. */
	public synchronized void start(boolean daemon_mode) {
		if (time<0 || is_running) return;
		// else
		this.daemon_mode=daemon_mode;
		start_time=System.currentTimeMillis();
		is_running=true;
		if (time>0) {
			TimerTask task=new TimerTask() {
				public void run() { processInnerTimeout(); }   
			};
			scheduleTask(task,time,daemon_mode);
		}
		else {
			// fire now!			
			processInnerTimeout();  
		}
	}
	
	
	/** Schedule a new task.
	 * @param task the task to be scheduled
	 * @param time the time
	 * @param daemon_mode whether running in 'daemon' mode */
	private synchronized static void scheduleTask(TimerTask task, long time, boolean daemon_mode) {
		for (int attempts=0; attempts<MAX_ATTEPTS; attempts++) {
			if (daemon_mode) {
				try  {
					if (daemon_scheduler==null) daemon_scheduler=new java.util.Timer(true); 
					daemon_scheduler.schedule(task,time);
					break;
				}
				catch (IllegalStateException e) { daemon_scheduler=null; }
			}
			else {
				synchronized (scheduler_lock) {
					try  {
						if (scheduler==null) scheduler=new java.util.Timer(false); 
						scheduler.schedule(task,time);
						scheduled_tasks++;
					    break;						
					}
					catch (IllegalStateException e) { scheduler=null; }	
				}
			}
		}
	}


	/** Whether the timer is running.
	  * @return <i>true</i> if it is running */
	public boolean isRunning() {
		return is_running;
	}   


	/** Stops the Timer.
	  * The method {@link TimerListener#onTimeout(Timer)} of the timer listener will not be fired. */
	public void halt() {
		terminate();
	}

	
	/** When the InnerTimer expires. */
	private synchronized void processInnerTimeout() {
		if (is_running && listener!=null) listener.onTimeout(this);  
		terminate();
	}

	
	/** Terminates the Timer. */
	private synchronized void terminate() {
		if (is_running) {
			is_running=false;
			listener=null;
			if (!daemon_mode && time>0) {
				// the timer has been scheduled in 'non-daemon' mode
				synchronized (scheduler_lock) {
					scheduled_tasks--;
					if (scheduled_tasks==0) {
						scheduler.cancel();
						scheduler.purge();
					}			
				}
			}		
		}
	}
	
}
