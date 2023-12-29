package org.mjsip.rtp;

/**
 * Utilities for accessing byte buffers.
 */
public class BufferUtil {

	/** Gets long value. */
	public static long getLong(byte[] data, int start, int stop) {
		long result = 0;
		for (int n = start; n < stop; n++) {
			result <<= 8;
			result |= data[n] & 0xFF;
		}
		return result;
	}

	/** Sets long value. */
	public static void setLong(long value, byte[] data, int start, int stop) {
		for (int n = stop - 1; n >= start; n--) {
			data[n] = (byte) (value & 0xFF);
			value >>>= 8;
		}
	}

	/** Gets Int value. */
	public static int getInt(byte[] data, int start, int stop) {
		int result = 0;
		for (int n = start; n < stop; n++) {
			result <<= 8;
			result |= data[n] & 0xFF;
		}
		return result;
	}

	/** Sets Int value. */
	public static void setInt(int value, byte[] data, int start, int stop) {
		for (int n = stop - 1; n >= start; n--) {
			data[n] = (byte) (value & 0xFF);
			value >>>= 8;
		}
	}

	/** Gets bit value. */
	public static boolean getBit(byte b, int bit) {
		return ((b >>> bit) & 0x01) == 1;
	}

	/** Sets bit value. */
	public static void setBit(boolean value, byte[] data, int offset, int bit) {
		byte b = data[offset];
		if (value) {
			b |= (1 << bit);
		} else {
			b &= 0xFF ^ (1 << bit);
		}
		data[offset] = b;
	}

}
