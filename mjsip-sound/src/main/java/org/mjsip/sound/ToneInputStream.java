/*
 * Copyright (C) 2005 Luca Veltri - University of Parma - Italy
 * 
 * This source code is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This source code is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this source code; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 * 
 * Author(s):
 * Luca Veltri (luca.veltri@unipr.it)
 */

package org.mjsip.sound;

import java.io.InputStream;

/**
 * {@link InputStream} providing synthetic audio data generated from a sin wave.
 */
public class ToneInputStream extends java.io.InputStream {
	
	/** The number of bytes that are notified as available */
	static int MAX_AVAILABLE_BYTES=65536;

	private final ToneGenerator _generator;

	private final int _sampleSize;

	private final int _bitsPerSample;

	/** Buffer containing the current sample */
	private final int[] _sBuff;

	/** Index within {@link #_sBuff} */
	private int _sIndex;

	/**
	 * Buffer position where to place the least significant byte of a sample.
	 */
	private int _startPos;

	/**
	 * Increment of buffer position while storing sample data.
	 */
	private int _inc;

	/**
	 * Creates a {@link ToneInputStream}
	 * 
	 * @param frequency
	 *        The tone frequency in Hz.
	 * @param amplitude
	 *        The signal amplitude between 0.0 and 1.0.
	 * @param sampleRate
	 *        The number of samples per second.
	 * @param sampleSize
	 *        The number of bytes per sample.
	 * @param encoding
	 *        Value range of tone samples.
	 * @param bigEndian
	 *        Whether to place the most-significant byte first in the stream.
	 */
	public ToneInputStream(int frequency, double amplitude, int sampleRate, int sampleSize,
			ToneGenerator.Encoding encoding, boolean bigEndian) {
		this(new ToneGenerator(frequency, amplitude, sampleRate, sampleSize, encoding), bigEndian);
	}
	  
	/**
	 * Creates a {@link ToneInputStream}.
	 *
	 * @param toneGenerator
	 *        The source of samples.
	 * @param bigEndian
	 *        Whether to place the most-significant byte first in the stream.
	 */
	public ToneInputStream(ToneGenerator toneGenerator, boolean bigEndian) {
		_generator = toneGenerator;

		int sampleSize = toneGenerator.getSampleSize();

		_sampleSize = sampleSize;
		_bitsPerSample = sampleSize * 8;

		_startPos = bigEndian ? sampleSize - 1 : 0;
		_inc = bigEndian ? -1 : 1;
		_sIndex = 0;
		_sBuff = new int[sampleSize];
	}

	/** Returns the number of bytes that can be read (or skipped over) from this input stream without blocking by the next caller of a method for this input stream. */
	@Override
	public int available()  {
		return MAX_AVAILABLE_BYTES;
	}

	/** Reads the next byte of data from the input stream. */
	@Override
	public int read()  {
		if (_sIndex == 0) {
			produceSample();
		}
		int result = _sBuff[_sIndex++];
		if (_sIndex == _sampleSize) {
			_sIndex = 0;
		}
		return result;
	}

	/**
	 * Fills the buffer with the next sample.
	 */
	private void produceSample() {
		long sample = _generator.nextSample();
		for (int pos = _startPos, shift = 0; shift < _bitsPerSample; pos += _inc, shift += 8) {
			_sBuff[pos] = (int) ((sample >>> shift) & 0xFF);
		}
	}

}


