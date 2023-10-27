/*
 * Copyright (c) 2023 Bernhard Haumacher et al. All Rights Reserved.
 */
package org.mjsip.ua.sound;

import static java.lang.Math.*;

import java.io.IOException;
import java.io.OutputStream;

import org.zoolu.sound.codec.G711;

/**
 * Filter for ALAW encoded audio streams that cuts out regions of silence.
 *
 * @author <a href="mailto:haui@haumacher.de">Bernhard Haumacher</a>
 */
public class AlawSilenceTrimmer extends OutputStream {
	
	/**
	 * Lookup table with signal square values for a given alaw code.
	 */
	private static final int[] ALAW_SQUARE;
	
	private final int _sampleRate;
	private final IntRingBuffer _buffer;
	
	private final int _bufferSize;
	private final long _maxSilenceSum;
	private final int _bufferTime;
	private final int _minSilenceTime;
	
	private long _squareSum;
	
	/**
	 * Number of samples summed.
	 */
	private int _cnt;
	
	private int _silenceTime;
	
	/**
	 * Whether the input is currently silent.
	 * 
	 * <p>
	 * Start in silence mode to buffer values in case there is really silence. In noisy mode, input
	 * is not buffered but directly passed to the output.
	 * </p>
	 */
	private boolean _silence = true;
	
	private long _clock;

	private OutputStream _out;

	private final SilenceListener _listener;
	
	static {
		ALAW_SQUARE = new int[256];
		for (int code = 0; code < 256; code++) {
			int value = G711.alaw2linear(code);
			ALAW_SQUARE[code] = value * value;
		}
	}

	/**
	 * Creates a {@link AlawSilenceTrimmer}.
	 * 
	 * @param sampleRate
	 *        Number of samples per second in the source line.
	 * @param bufferTime
	 *        Length of the silence detection window in milliseconds.
	 * @param minSilenceTime
	 *        Minimum time silence must be detected before pausing the recording.
	 * @param paddingTime
	 *        The time of silence passed to the output surrounding non-silence segments.
	 * @param silenceDb
	 *        Decibel value that is considered silence. 0 db corresponds to a full-scale sin wave.
	 * @param out
	 *        The output stream to pass non-silence regions of the input signal to.
	 */
	public AlawSilenceTrimmer(int sampleRate, int bufferTime, int minSilenceTime, int paddingTime, double silenceDb, OutputStream out, SilenceListener listener) {
		_sampleRate = sampleRate;
		_bufferTime = bufferTime;
		_minSilenceTime = minSilenceTime;
		_out = out;
		_listener = SilenceListener.nonNull(listener);
		int bufferSize = _sampleRate * bufferTime / 1000;
		_cnt = 0;
		_squareSum = 0L;
		
		int paddingSamples = sampleRate * paddingTime / 1000;
		
		_buffer = new IntRingBuffer(bufferTime + paddingSamples);
		_bufferSize = bufferSize;
		
		
		// db(_buffer) = Decibel value of buffered samples _buffer[i] where i in [0,.., _bufferSize-1] in comparison 
		// to a full-scale sin wave:
		
		// => db(_buffer) = 20 * log10(rms(_buffer) * sqrt(2)/range)
		//    where the root mean square rms(_buffer)
		//      = sqrt(sum(_buffer[i]^2, i=0..bufferSize) / bufferSize)
		//      = sqrt(_squareSum / bufferSize)
		// => db(_buffer) = 20 * log10(sqrt(_squareSum / bufferSize)/range * sqrt(2))
		//                = 20 * ( 1/2 * (log10(_squareSum) - log10(bufferSize)) - log10(range) + 1/2 log10(2))
		//                = 10 * (log10(_squareSum) - log10(bufferSize) + log10(2) - 2*log10(range))
		
		// Test for silence:
		// db(_buffer) <= silenceDb
		// 10 * (log10(_squareSum) - log10(bufferSize) + log10(2) - 2*log10(range)) <= silenceDb
		// log10(_squareSum) - log10(bufferSize) + log10(2) - 2*log10(range)  <= silenceDb / 10
		// log10(_squareSum) <= silenceDb / 10 + log10(bufferSize) - log10(2) + 2*log10(range)
		// _squareSum <= pow(10, silenceDb / 10 + log10(bufferSize) - log10(2) + 2*log10(range)) = _maxSilenceSum
		// _maxSilenceSum = pow(10, silenceDb / 10 + log10(bufferSize / 2)  + 2*log10(range));
		// _maxSilenceSum = pow(10, silenceDb / 10 + log10(bufferSize / 2)) * range^2;
		
		_maxSilenceSum = sqrSumLimit(bufferSize, 16, silenceDb);
	}
	
	/**
	 * Switches the underlying {@link OutputStream} where non-silent audio is forwarded to.
	 * 
	 * @return The previously used output.
	 */
	public OutputStream setOut(OutputStream out) {
		OutputStream before = _out;
		_out = out;
		return before;
	}

	/**
	 * The limit of the square sum of a signal to be less than the given db value.
	 *
	 * @param bufferSize
	 *        The buffer size of the signal.
	 * @param bitsPerSample
	 *        The bits per sample defining the signal range.
	 * @param db
	 *        The decibel value.
	 * @return Limit for the signal's square sum.
	 */
	public static long sqrSumLimit(int bufferSize, int bitsPerSample, double db) {
		long range = range(bitsPerSample);
		return (long) (pow(10, db / 10 + log10(bufferSize / 2)) * (range * range));
	}

	/**
	 * The signal range of a signal with the given bits per sample value.
	 */
	public static long range(int bitsPerSample) {
		return (1L << (bitsPerSample - 1)) - 1;
	}

	@Override
	public void write(int code) throws IOException {
		int square = ALAW_SQUARE[code & 0xFF];
		_squareSum += square;

		_buffer.write(code);
		_cnt++;
		
		if (_cnt == _bufferSize) {
			// A complete range has been read.
			if (_squareSum <= _maxSilenceSum) {
				// The last samples were silence.
				_silenceTime += _bufferTime;
				if (_silenceTime >= _minSilenceTime && !_silence) {
					_silence = true;
					
					// Flush buffer to output (skip last _bufferSize silent samples).
					for (int n = Math.max(0,  _buffer.length() - _bufferSize); n > 0; n--) {
						_out.write(_buffer.read());
					}
					_buffer.clear();
					
					_listener.onSilenceStarted(_clock);
				}
			} else {
				// Noise was read.
				if (_silence) {
					// There was silence before.
					_silence = false;
					_listener.onSilenceEnded(_clock);
				}

				// Flush buffer to output.
				forwardBuffer();
				
				_silenceTime = 0;
			}
			
			_cnt = 0;
			_squareSum = 0;
		}
		
		_clock++;
	}

	private void forwardBuffer() throws IOException {
		for (int n = _buffer.length(); n > 0; n--) {
			_out.write(_buffer.read());
		}
	}
	
	@Override
	public void close() throws IOException {
		if (!_silence) {
			forwardBuffer();
		}
		super.close();
	}

}
