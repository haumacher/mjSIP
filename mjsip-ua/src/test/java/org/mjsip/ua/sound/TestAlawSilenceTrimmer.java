/*
 * Copyright (c) 2023 Bernhard Haumacher et al. All Rights Reserved.
 */
package org.mjsip.ua.sound;

import java.io.IOException;
import java.io.OutputStream;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mjsip.sound.AudioFile;
import org.mjsip.sound.ToneGenerator;

/**
 * Test for {@link AlawSilenceTrimmer}.
 *
 * @author <a href="mailto:haui@haumacher.de">Bernhard Haumacher</a>
 */
class TestAlawSilenceTrimmer {

	/**
	 * Test loudness analysis with synthetic tones. 
	 */
	@Test
	void testSinDbLimits() {
		int frequency = 1000;
		int sampleRate = 16000;
		
		// Test with sin waves over one complete oscillation
		int oscillations = 1;
		int bufferTime = 1000 * oscillations / frequency;
		int bufferSize = sampleRate * bufferTime / 1000;

		int bitsPerSample = 16;

		// A full-scale sin wave (amplitude 1.0) 
		long squareSum = squareSinSum(frequency, sampleRate, bufferSize, 1.0);
		
		System.out.println(squareSum);
		long db0 = AlawSilenceTrimmer.sqrSumLimit(bufferSize, bitsPerSample, 0.0);
		long db1 = AlawSilenceTrimmer.sqrSumLimit(bufferSize, bitsPerSample, -1.0);
		System.out.println(db0);
		System.out.println(db1);
		
		Assertions.assertTrue(squareSum <= db0, "Square sum of full scale sin wave must be not larger than the 0 db limit.");
		Assertions.assertTrue(squareSum > db1, "Square sum of full scale sin wave must be larger than the 1 db limit.");
		
		// Find the sin wave amplitude that has a db value less than -60.
		long db60 = AlawSilenceTrimmer.sqrSumLimit(bufferSize, bitsPerSample, -60.0);
		System.out.println(db60);
		
		double amplitude = 1.0;
		while (true) {
			long sum = squareSinSum(frequency, sampleRate, bufferSize, amplitude);
			if (sum < db60) {
				System.out.println(amplitude);
				System.out.println(amplitude * AlawSilenceTrimmer.range(bitsPerSample));
				break;
			}
			amplitude = amplitude * 0.8;
		}
	}

	private long squareSinSum(int frequency, int sampleRate, int bufferSize, double amplitude) {
		ToneGenerator toneGenerator = new ToneGenerator(frequency, amplitude, sampleRate, 2, ToneGenerator.Encoding.PCM_LINEAR_SIGNED);
		
		long squareSum = 0;
		for (int n = 0; n < bufferSize; n++) {
			long sample = toneGenerator.nextSample();
			squareSum += sample * sample;
		}
		return squareSum;
	}
	
	/**
	 * Test silence trimming with a real world recording.
	 */
	@Test
	void testWav() throws IOException, UnsupportedAudioFileException {
		try (AudioInputStream in = AudioFile.getAudioFileInputStream("./src/test/fixtures/test-alaw.wav")) {
			AudioFormat format = in.getFormat();
			try (OutputStream out = AudioFile.getAudioFileOutputStream("./target/test-alaw-trimmed.wav", format)) {
				int sampleRate = (int) format.getSampleRate();
				
				SilenceListener printer = new SilenceListener() {
					@Override
					public void onSilenceStarted(long clock) {
						// Trigger event.
						System.out.println("Silence starts at: " + clock);
					}
					
					@Override
					public void onSilenceEnded(long clock) {
						// Trigger event.
						System.out.println("Silence ends at: " + clock);
					}
				};
				
				try (AlawSilenceTrimmer trimmer = new AlawSilenceTrimmer(sampleRate, 20, 500, 100, -30, out, printer)) {
					int code;
					while ((code = in.read()) >= 0) {
						trimmer.write(code);
					}
				}
			}
		}
	}
	
}
