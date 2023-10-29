/*
 * Copyright (c) 2023 Bernhard Haumacher et al. All Rights Reserved.
 */
package org.mjsip.up;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mjsip.ua.pool.PortPool;
import org.mjsip.ua.pool.PortPool.Exhausted;

/**
 * Test case for {@link PortPool}
 */
@SuppressWarnings("javadoc")
public class TestPortPool {
	
	@Test
	public void testSingletonPool() {
		PortPool pool = new PortPool(10, 1);
		
		Assertions.assertTrue(pool.isAvailable());
		Assertions.assertEquals(10, pool.allocate());
		Assertions.assertFalse(pool.isAvailable());
		assertFailExhausted(pool);
		pool.release(10);
		Assertions.assertTrue(pool.isAvailable());
	}

	@Test
	public void testPool() {
		PortPool pool = new PortPool(10, 3);
		
		Assertions.assertTrue(pool.isAvailable());
		Assertions.assertEquals(10, pool.allocate());
		Assertions.assertTrue(pool.isAvailable());
		Assertions.assertEquals(11, pool.allocate());
		Assertions.assertTrue(pool.isAvailable());
		pool.release(10);
		Assertions.assertEquals(10, pool.allocate());
		Assertions.assertTrue(pool.isAvailable());
	}

	private void assertFailExhausted(PortPool pool) {
		try {
			pool.allocate();
			Assertions.fail("Allocating from an exhausted pool must fail.");
		} catch (Exhausted ex) {
			// Expected.
		}
	}
	
}
