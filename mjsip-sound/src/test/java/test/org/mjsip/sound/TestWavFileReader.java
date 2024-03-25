package test.org.mjsip.sound;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;

import javax.sound.sampled.AudioFileFormat.Type;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioFormat.Encoding;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.junit.jupiter.api.Test;
import org.mjsip.sound.WavFileReader;

/**
 * Test case for {@link WavFileReader}.
 */
class TestWavFileReader {
	
	@Test
	void testRead() throws IOException, UnsupportedAudioFileException {
		float sampleRate = 8000;
		int sampleSizeInBits = 8;
		int channels = 1;
		int frameSize = 1;
		float frameRate = 8000;
		boolean bigEndian = false;
		AudioFormat format = new AudioFormat(Encoding.ALAW, sampleRate, sampleSizeInBits, channels, frameSize, frameRate, bigEndian);
		long length = 1;
		byte[] data = new byte[] {42};
		ByteArrayInputStream in = new ByteArrayInputStream(data);
		AudioInputStream stream = new AudioInputStream(in, format, length);
		Type fileType = Type.WAVE;
		File wavFile = File.createTempFile("test", ".wav");
		AudioSystem.write(stream, fileType, wavFile);
		
		AudioInputStream audioInputStream = WavFileReader.INSTANCE.getAudioInputStream(wavFile);
		assertFormat(format, audioInputStream);
		assertEquals(1, audioInputStream.getFrameLength());
		assertEquals(42, audioInputStream.read());
	}

	private static void assertFormat(AudioFormat expected, AudioInputStream given) {
		AudioFormat format = given.getFormat();
		assertAll(() -> assertEquals(expected.getChannels(), format.getChannels()),
				() -> assertEquals(expected.getEncoding(), format.getEncoding()),
				() -> assertEquals(expected.getFrameRate(), format.getFrameRate()),
				() -> assertEquals(expected.getFrameSize(), format.getFrameSize()),
				() -> assertEquals(expected.getSampleRate(), format.getSampleRate()),
				() -> assertEquals(expected.getSampleSizeInBits(), format.getSampleSizeInBits())
		);
	}

}
