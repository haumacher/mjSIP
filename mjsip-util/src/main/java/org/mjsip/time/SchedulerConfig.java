/*
 * Copyright (c) 2023 Bernhard Haumacher et al. All Rights Reserved.
 */
package org.mjsip.time;

import org.kohsuke.args4j.Option;
import org.mjsip.config.YesNoHandler;

/**
 * Configuration options for the {@link Scheduler}.
 *
 * @author <a href="mailto:haui@haumacher.de">Bernhard Haumacher</a>
 */
public class SchedulerConfig {

	@Option(name = "--thread-pool-size")
	private int _threadPoolSize = 5;

	@Option(name = "--use-daemon-treads", handler = YesNoHandler.class)
	private boolean _daemonThreads = true;

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

}
