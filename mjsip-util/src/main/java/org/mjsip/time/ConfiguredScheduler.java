/*
 * Copyright (c) 2023 Bernhard Haumacher et al. All Rights Reserved.
 */
package org.mjsip.time;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;

/**
 * Service for scheduling tasks.
 *
 * @author <a href="mailto:haui@haumacher.de">Bernhard Haumacher</a>
 */
public class ConfiguredScheduler implements Scheduler {

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

	/**
	 * Creates a {@link ConfiguredScheduler}.
	 */
	public ConfiguredScheduler(SchedulerConfig config) {
		executor = new ScheduledThreadPoolExecutor(config.getThreadPoolSize(),
				config.useDaemonThreads() ? new DaemonFactory() : Executors.defaultThreadFactory());
	}
	
	@Override
	public void execute(Runnable command) {
		executor.execute(command);
	}
	
	@Override
	public ScheduledExecutorService scheduler() {
		return executor;
	}

	/**
	 * Executor for background tasks that end if the program terminates.
	 */
	public ScheduledThreadPoolExecutor daemonExecutor() {
		return daemonExecutor;
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
