/*
 * Copyright (c) 2023 Bernhard Haumacher et al. All Rights Reserved.
 */
package org.mjsip.ua.sound;

import java.util.Random;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Test case for {@link IntRingBuffer}.
 *
 * @author <a href="mailto:haui@haumacher.de">Bernhard Haumacher</a>
 */
class TestIntRingBuffer {
	
	@Test
	void testWrite() {
		IntRingBuffer buffer = new IntRingBuffer(3);
		
		Assertions.assertTrue(buffer.empty());
		Assertions.assertEquals(0, buffer.length());
		Assertions.assertFalse(buffer.full());
		
		buffer.write(10);
		Assertions.assertFalse(buffer.empty());
		Assertions.assertEquals(1, buffer.length());
		buffer.write(11);
		Assertions.assertEquals(2, buffer.length());
		Assertions.assertFalse(buffer.full());
		buffer.write(12);
		Assertions.assertTrue(buffer.full());
		Assertions.assertEquals(3, buffer.length());
		
		buffer.write(13);
		Assertions.assertEquals(3, buffer.length());
		
		Assertions.assertEquals(11, buffer.read());
		Assertions.assertEquals(12, buffer.read());
		Assertions.assertEquals(13, buffer.read());
		
		Assertions.assertTrue(buffer.empty());
		Assertions.assertEquals(0, buffer.length());
	}

	@Test
	void testRandom() {
		int limit = 20;
		IntRingBuffer buffer = new IntRingBuffer(limit);
		
		int size = 0;
		Random rnd = new Random(42);
		for (int n = 0; n < 500; n++) {
			switch (rnd.nextInt(9)) {
			case 0:
			case 1:
			case 2:
			case 3:
				buffer.write(n++);
				size = Math.min(limit, size + 1);
				Assertions.assertEquals(size, buffer.length());
				break;
				
			case 4:
			case 5:
			case 6:
			case 7:
				if (size > 0) {
					buffer.read();
					size--;
					Assertions.assertEquals(size, buffer.length());
				} else {
					Assertions.assertTrue(buffer.empty());
				}
				break;

			case 8:
				int cnt = rnd.nextInt(5);
				buffer.skip(cnt);
				size = Math.max(0, size - cnt);
				Assertions.assertEquals(size, buffer.length());
				break;
				
			default: 
				Assertions.fail("Unexpected.");
			}
		}
	}
	
}
