/*
 * Copyright (c) 2023 Bernhard Haumacher et al. All Rights Reserved.
 */
package org.mjsip.time;

import org.zoolu.util.Configure;
import org.zoolu.util.Parser;

/**
 * Configuration options for the {@link Scheduler}.
 *
 * @author <a href="mailto:haui@haumacher.de">Bernhard Haumacher</a>
 */
public class SchedulerConfig extends Configure {

	private int _threadPoolSize = 3;

	private boolean _daemonThreads = true;

	@Override
	public void setOption(String attribute, Parser par) {
		switch (attribute) {
		case "thread_pool_size":
			_threadPoolSize = par.getInt();
			break;
		case "timer_daemon_mode":
			_daemonThreads = par.getString().toLowerCase().startsWith("y");
			break;
		}
	}

	/**
	 * The core pool size of the scheduler's thread pool.
	 */
	public int getThreadPoolSize() {
		return _threadPoolSize;
	}

	/**
	 * Whether the scheduler uses daemon threads.
	 */
	public boolean useDaemonThreads() {
		return _daemonThreads;
	}

	/**
	 * Creates a {@link SchedulerConfig}
	 */
	public static SchedulerConfig init() {
		return new SchedulerConfig();
	}

	/**
	 * Creates a {@link SchedulerConfig}
	 */
	public static SchedulerConfig init(String config_file) {
		SchedulerConfig result = init();
		result.loadFile(config_file);
		return result;
	}

}
