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

/**
 * Source of samples producing a tone with a constant frequency.
 */
public class ToneGenerator {

	/**
	 * Encoding of tone data.
	 */
	public enum Encoding {
		/** Linear unsigned PCM (zero is at half the signal range, no negative values) */
		PCM_LINEAR_UNSIGNED,

		/**
		 * Linear signed PCM (zero is at <code>(int) 0</code>, the negative half wave has negative
		 * values).
		 */
		PCM_LINEAR_SIGNED;

		/**
		 * Whether this encoding used signed values.
		 */
		public boolean isSigned() {
			switch (this) {
			case PCM_LINEAR_SIGNED:
				return true;
			case PCM_LINEAR_UNSIGNED:
				return false;
			}
			throw new AssertionError("No such encoding: " + this);
		}
	}

	/** Tone frequency in Hz */
	private final int _frequency;

	/** Tone amplitude in the interval [0, 2^(n-1)] where n is the sample size in bits. */
	private final double _amplitude;

	/** Sample rate in samples per seconds. */
	private final int _sampleRate;

	private final int _sampleSize;

	/** Offset to be added in case of unsigned PCM output */
	private final double _zero;

	/** Sample sequence number */
	private double _k;

	/**
	 * Factor to muliply with {@link #_k} producing the radian input to {@link Math#sin(double)}: 2
	 * * {@link Math#PI} * {@link #_frequency} / {@link #_sampleRate}
	 */
	private final double _B;

	/**
	 * Creates a {@link ToneGenerator}.
	 *
	 * @param frequency
	 *        The tone frequency in Hz.
	 * @param amplitude
	 *        The signal amplitude between 0.0 and 1.0.
	 * @param sampleRate
	 *        The number of samples per second.
	 * @param sampleSize
	 *        The number of bytes per sample.
	 * @param codec
	 *        Either {@link Encoding#PCM_LINEAR_SIGNED} or {@link Encoding#PCM_LINEAR_UNSIGNED}
	 */
	public ToneGenerator(int frequency, double amplitude, int sampleRate, int sampleSize, Encoding codec) {
		_frequency = frequency;
		_sampleRate = sampleRate;
		_sampleSize = sampleSize;
		_B = (2 * Math.PI * _frequency) / _sampleRate;
	
		int bitsPerSample = sampleSize * 8;
		long range = (((long) 1) << (bitsPerSample - 1)) - 1;
		_amplitude = amplitude * range;
		_zero = codec == Encoding.PCM_LINEAR_SIGNED ? 0.0f : range / 2;
	
		_k = 0;
	}

	/** Reads the next sample. */
	public long nextSample() {
		return (long) (_amplitude * Math.sin(_B * (_k++)) + _zero);
	}

	/**
	 * The number of bytes per sample.
	 */
	public int getSampleSize() {
		return _sampleSize;
	}

}
