/*
 * Copyright (c) 2023 Bernhard Haumacher et al. All Rights Reserved.
 */
package org.mjsip.time;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Service for scheduling tasks.
 */
public interface Scheduler extends Executor {

	/** 
	 * The executor.
	 */
	default ExecutorService executor() {
		return scheduler();
	}
	
	/** 
	 * The scheduler.
	 */
	ScheduledExecutorService scheduler();

	@Override
	default void execute(Runnable command) {
		executor().execute(command);
	}

	/**
	 * Schedules a new task.
	 * 
	 * @param delay
	 *        the delay in milliseconds to wait before starting the given task
	 * @param task
	 *        the task to be scheduled
	 * 
	 * @return The {@link ScheduledFuture} to control the task.
	 */
	default ScheduledFuture<?> schedule(long delay, Runnable task) {
		return scheduler().schedule(task, delay, TimeUnit.MILLISECONDS);
	}

	/**
	 * Schedules the given repeated task with a given fixed delay.
	 */
	default ScheduledFuture<?> schedulerWithFixedDelay(long delay, Runnable task) {
		return scheduler().scheduleWithFixedDelay(task, delay, delay, TimeUnit.MILLISECONDS);
	}
}
