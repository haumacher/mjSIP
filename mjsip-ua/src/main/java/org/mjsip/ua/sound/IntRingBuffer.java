/*
 * Copyright (c) 2023 Bernhard Haumacher et al. All Rights Reserved.
 */
package org.mjsip.ua.sound;

/**
 * Buffer of <code>int</code> values that keeps the last size values.
 *
 * @author <a href="mailto:haui@haumacher.de">Bernhard Haumacher</a>
 */
public final class IntRingBuffer {
	
	private int _length;
	private int[] _buffer;
	
	/**
	 * Position of the next value to write.
	 */
	private int _in;
	
	/**
	 * Position of the next value to read.
	 */
	private int _out;

	/** 
	 * Creates a {@link IntRingBuffer}.
	 */
	public IntRingBuffer(int size) {
		_length = size + 1;
		_buffer = new int[size + 1];
		
		clear();
	}
	
	/**
	 * Removes all values from this buffer.
	 */
	public void clear() {
		_in = 0;
		_out = 0;
	}

	/**
	 * Whether there are no values in this buffer.
	 */
	public boolean empty() {
		return _out == _in;
	}

	/**
	 * Whether the maximum number of values are in this buffer.
	 * 
	 * <p>
	 * Writing another values drops the oldest value.
	 * </p>
	 */
	public boolean full() {
		return inc(_in) == _out;
	}

	private int inc(int pos) {
		int result = pos + 1;
		if (result == _length) {
			result = 0;
		}
		return result;
	}
	
	/**
	 * Writes a new value to this buffer.
	 */
	public void write(int value) {
		_buffer[_in] = value;
		_in = inc(_in);
		
		if (_in == _out) {
			// Drop overflow.
			_out = inc(_out);
		}
	}
	
	/**
	 * Reads and discards the oldest value from this buffer.
	 */
	public int read() {
		assert !empty();
		int value = _buffer[_out];
		_out = inc(_out);
		return value;
	}

	/**
	 * Drops the given number of values.
	 *
	 * @param cnt
	 *        The number of values to drop.
	 */
	public void skip(int cnt) {
		_out += Math.min(cnt, length());
		if (_out >= _length) {
			_out -= _length;
		}
	}

	/** 
	 * The number of values in the buffer.
	 */
	public int length() {
		if (_in >= _out) {
			return _in - _out;
		} else {
			return _in + _length - _out;
		}
	}
	
}
