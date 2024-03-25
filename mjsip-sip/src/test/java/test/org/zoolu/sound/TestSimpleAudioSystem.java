/*
 * Copyright (c) 2023 Bernhard Haumacher et al. All Rights Reserved.
 */
package test.org.zoolu.sound;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mjsip.sound.AudioFile;
import org.mjsip.sound.ToneGenerator;
import org.mjsip.sound.ToneInputStream;
import org.mjsip.sound.ToneGenerator.Encoding;
import org.zoolu.sound.CodecType;
import org.zoolu.sound.SimpleAudioSystem;
import org.zoolu.sound.codec.G711;

/**
 * Test generating, and transforming WAV files.
 */
class TestSimpleAudioSystem {

	@Test
	void testToneWavLinearSigned() throws IOException, UnsupportedAudioFileException {
		int sampleRate = 16000;
		int sampleSize = 2;
		boolean bigEndian = false;
		int duration = 1;
		Encoding encoding = ToneGenerator.Encoding.PCM_LINEAR_SIGNED;
		try (InputStream in = new ToneInputStream(1000, 1, sampleRate, sampleSize, encoding, bigEndian)) {
			try (OutputStream out = AudioFile.getAudioFileOutputStream("./target/tone-linear.wav",
					new AudioFormat(sampleRate, sampleSize * 8, 1, encoding.isSigned(), bigEndian))) {
				for (int n = 0; n < sampleRate * sampleSize * duration; n++) {
					int s = in.read();
					out.write(s);
				}
			}
		}
	}

	@Test
	void testToneWavAlaw() throws IOException, UnsupportedAudioFileException {
		int sampleRate = 16000;
		int sampleSize = 2;
		int duration = 1;
		Encoding encoding = ToneGenerator.Encoding.PCM_LINEAR_SIGNED;

		ToneGenerator generator = new ToneGenerator(1000, 1, sampleRate, sampleSize, encoding);

		AudioFormat format = SimpleAudioSystem.getAudioFormat(CodecType.G711_ALAW, sampleRate);
		try (OutputStream out = AudioFile.getAudioFileOutputStream("./target/tone-alaw.wav", format)) {
			for (int n = 0; n < sampleRate * sampleSize * duration; n++) {
				long s = generator.nextSample();

				int code = G711.linear2alaw((int) s);

				out.write(code);
			}
		}

		decompress("./target/tone-alaw.wav");
	}

	void decompress(String alawWavFile) throws IOException, UnsupportedAudioFileException {
		int sampleSize = 2;
		boolean bigEndian = false;
		Encoding encoding = ToneGenerator.Encoding.PCM_LINEAR_SIGNED;
		try (AudioInputStream in = AudioFile.getAudioFileInputStream(alawWavFile)) {
			AudioFormat inputFormat = in.getFormat();
			float sampleRate = inputFormat.getSampleRate();
			AudioFormat format = new AudioFormat(sampleRate, sampleSize * 8, inputFormat.getChannels(),
					encoding.isSigned(), bigEndian);
			try (OutputStream out = AudioFile.getAudioFileOutputStream(
					alawWavFile.substring(0, alawWavFile.length() - 4) + "-decompressed.wav", format)) {
				int b;
				while ((b = in.read()) >= 0) {
					int code = b & 0xFF;

					int s = ALAW_DECOMPRESS_TABLE[code];
					int s1 = alawExpand(code);
					int s2 = G711.alaw2linear(code);

					Assertions.assertEquals(s, s1);
					Assertions.assertEquals(s, s2);

					out.write(s & 0xFF);
					out.write((s >>> 8) & 0xFF);
				}
			}
		}
	}

	private static final int[] ALAW_DECOMPRESS_TABLE =
		{
		     -5504, -5248, -6016, -5760, -4480, -4224, -4992, -4736,
		     -7552, -7296, -8064, -7808, -6528, -6272, -7040, -6784,
		     -2752, -2624, -3008, -2880, -2240, -2112, -2496, -2368,
		     -3776, -3648, -4032, -3904, -3264, -3136, -3520, -3392,
		     -22016,-20992,-24064,-23040,-17920,-16896,-19968,-18944,
		     -30208,-29184,-32256,-31232,-26112,-25088,-28160,-27136,
		     -11008,-10496,-12032,-11520,-8960, -8448, -9984, -9472,
		     -15104,-14592,-16128,-15616,-13056,-12544,-14080,-13568,
		     -344,  -328,  -376,  -360,  -280,  -264,  -312,  -296,
		     -472,  -456,  -504,  -488,  -408,  -392,  -440,  -424,
		     -88,   -72,   -120,  -104,  -24,   -8,    -56,   -40,
		     -216,  -200,  -248,  -232,  -152,  -136,  -184,  -168,
		     -1376, -1312, -1504, -1440, -1120, -1056, -1248, -1184,
		     -1888, -1824, -2016, -1952, -1632, -1568, -1760, -1696,
		     -688,  -656,  -752,  -720,  -560,  -528,  -624,  -592,
		     -944,  -912,  -1008, -976,  -816,  -784,  -880,  -848,
		      5504,  5248,  6016,  5760,  4480,  4224,  4992,  4736,
		      7552,  7296,  8064,  7808,  6528,  6272,  7040,  6784,
		      2752,  2624,  3008,  2880,  2240,  2112,  2496,  2368,
		      3776,  3648,  4032,  3904,  3264,  3136,  3520,  3392,
		      22016, 20992, 24064, 23040, 17920, 16896, 19968, 18944,
		      30208, 29184, 32256, 31232, 26112, 25088, 28160, 27136,
		      11008, 10496, 12032, 11520, 8960,  8448,  9984,  9472,
		      15104, 14592, 16128, 15616, 13056, 12544, 14080, 13568,
		      344,   328,   376,   360,   280,   264,   312,   296,
		      472,   456,   504,   488,   408,   392,   440,   424,
		      88,    72,   120,   104,    24,     8,    56,    40,
		      216,   200,   248,   232,   152,   136,   184,   168,
		      1376,  1312,  1504,  1440,  1120,  1056,  1248,  1184,
		      1888,  1824,  2016,  1952,  1632,  1568,  1760,  1696,
		      688,   656,   752,   720,   560,   528,   624,   592,
		      944,   912,  1008,   976,   816,   784,   880,   848
		};
	
	
	private static int alawExpand(int in) {
		int ix, mant, iexp;

		/* re-toggle toggled bits */
		ix = in ^ 0x55;

		/* remove sign bit */
		ix &= 0x7F;

		/* extract exponent */
		iexp = ix >> 4;

		/* get mantissa */
		mant = ix & 0x0F;

		if (iexp > 0) {
			/* add leading '1', if exponent > 0 */
			mant |= 0x10;
		}

		/* now mantissa left justified and 1/2 quantization step added */
		mant = (mant << 4) | 0x08;

		if (iexp > 1) {
			/* left shift according to exponent */
			mant = mant << (iexp - 1);
		}

		/* invert, if negative sample */
		int out = in > 127 ? mant : -mant;

		return out;
	}

}
