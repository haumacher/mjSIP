/*
 * Copyright (c) 2023 Bernhard Haumacher et al. All Rights Reserved.
 */
package org.mjsip.pool;

import java.util.HashSet;
import java.util.Set;

/**
 * Pool of host ports to e.g. serve RTP media streams.
 */
public class PortPool {
	
	/**
	 * Exception thrown, when allocating from an exhausted/empty pool.
	 */
	public static class Exhausted extends RuntimeException {
		/** 
		 * Creates a {@link PortPool.Exhausted}.
		 */
		public Exhausted(String message) {
			super(message);
		}
	}
	
	/**
	 * All ports that are initially available from this pool.
	 * 
	 * <p>
	 * For book keeping in assertions-enabled mode only.
	 * </p>
	 */
	private final Set<Integer> _allPorts = new HashSet<>();

	/**
	 * Ports that are currently still available from this pool (modified in allocate and release).
	 * 
	 * <p>
	 * For book keeping in assertions-enabled mode only.
	 * </p>
	 */
	private final Set<Integer> _availablePorts = new HashSet<>();
	
	/**
	 * The buffer of ports.
	 */
	private final int[] _ports;
	
	/**
	 * The index in the {@link #_ports} buffer of the next port being allocated.
	 */
	private int _next;
	
	/** 
	 * Creates a {@link PortPool}.
	 */
	public PortPool(int firstPort, int portCnt) {
		_ports = new int[portCnt];
		for (int n = 0, p = firstPort, end = firstPort + portCnt; p < end; n++, p++) {
			Integer port = Integer.valueOf(p);
			
			_ports[n] = p;
			assert createPort(port);
		}
		_next = 0;
	}
	
	private boolean createPort(Integer port) {
		_allPorts.add(port);
		_availablePorts.add(port);
		return true;
	}

	/** 
	 * Whether there is some port available to allocate.
	 */
	public boolean isAvailable() {
		return _next < _ports.length;
	}

	/**
	 * Allocates a free port from this buffer.
	 *
	 * @return The newly allocated buffer.
	 * 
	 * @throws Exhausted If there are no more ports available.
	 * @see #isAvailable()
	 */
	public synchronized int allocate() {
		if (!isAvailable()) {
			throw new Exhausted("No more ports available.");
		}
		int port = _ports[_next];
		assert canAllocate(port);
		_next = _next + 1;
		return port;
	}

	private boolean canAllocate(int port) {
		Integer allocatedPort = Integer.valueOf(port);
		assert _allPorts.contains(allocatedPort) : "The allocated port is not among the ports of this pool: " + port;
		assert _availablePorts.contains(allocatedPort) : "The allocated port is not available from this pool: " + port;
		_availablePorts.remove(allocatedPort);
		return true;
	}

	/**
	 * Releases a formerly allocated port.
	 *
	 * @param port The port to release.
	 */
	public synchronized void release(int port) {
		int last = (_next - 1);
		if (last < 0) {
			throw new IllegalStateException("Pool has no allocated ports that could be released: " + port);
		}
		
		assert canRelease(port);
		_ports[last] = port;
		_next = last;
	}

	private boolean canRelease(int port) {
		Integer releasedPort = Integer.valueOf(port);
		assert _allPorts.contains(releasedPort) : "The released port is not among the ports of this pool: " + port;
		assert !_availablePorts.contains(releasedPort) : "The released port is available from this pool: " + port;
		_availablePorts.add(releasedPort);
		return true;
	}
	
}
