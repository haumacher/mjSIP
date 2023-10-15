/*
 * Copyright (c) 2023 Bernhard Haumacher et al. All Rights Reserved.
 */
package org.mjsip.time;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * Service for scheduling tasks.
 *
 * @author <a href="mailto:haui@haumacher.de">Bernhard Haumacher</a>
 */
public class Scheduler {

	/**
	 * Inner non-daemon scheduler. The program terminates only when all non-daemon timers
	 * (associated to this scheduler) have ended (for timeout or explicitly halted)
	 */
	private ScheduledThreadPoolExecutor executor;

	/**
	 * Inner daemon scheduler. Daemon timers (associated to the this scheduler) silently terminate
	 * (without firing the corresponding timout callbacks) when all program threads end
	 */
	private ScheduledThreadPoolExecutor daemonExecutor;

	/** Whether the default mode is 'daemon', or not */
	public static boolean DEFAULT_DAEMON_MODE=false;

	private static Scheduler INSTANCE;

	/**
	 * The {@link Scheduler} singleton.
	 */
	public static synchronized Scheduler getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new Scheduler();
		}

		return INSTANCE;
	}

	/**
	 * Creates a {@link Scheduler}.
	 */
	public Scheduler() {
		executor = new ScheduledThreadPoolExecutor(3,
				Scheduler.DEFAULT_DAEMON_MODE ? new DaemonFactory() : Executors.defaultThreadFactory());
	}

	/**
	 * Executor for regular tasks.
	 */
	public ScheduledThreadPoolExecutor executor() {
		return executor;
	}

	/**
	 * Executor for background tasks that end if the program terminates.
	 */
	public ScheduledThreadPoolExecutor daemonExecutor() {
		return daemonExecutor;
	}

	/**
	 * Schedule a new task.
	 * 
	 * @param delay
	 *        the delay in milliseconds to wait before starting the given task
	 * @param task
	 *        the task to be scheduled
	 * 
	 * @return The {@link ScheduledFuture} to control the task.
	 */
	public static ScheduledFuture<?> scheduleTask(long delay, Runnable task) {
		return getInstance().executor().schedule(task, delay, TimeUnit.MILLISECONDS);
	}

	/**
	 * Schedules the given repeated task with a given fixed delay.
	 */
	public static ScheduledFuture<?> scheduleWithFixedDelay(long delay, Runnable task) {
		return getInstance().executor().scheduleWithFixedDelay(task, delay, delay, TimeUnit.MILLISECONDS);
	}

	private static class DaemonFactory implements ThreadFactory {

		private final ThreadFactory inner = Executors.defaultThreadFactory();

		@Override
		public Thread newThread(Runnable r) {
			Thread result = inner.newThread(r);
			result.setDaemon(true);
			return result;
		}

	}
}
