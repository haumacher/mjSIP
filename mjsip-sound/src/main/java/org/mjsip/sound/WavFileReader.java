package org.mjsip.sound;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.sound.sampled.spi.AudioFileReader;

/**
 * {@link AudioFileReader} for WAV files.
 */
public class WavFileReader extends AudioFileReader {
	
	/**
	 * Singleton instance of {@link WavFileReader}.
	 */
	public static final WavFileReader INSTANCE = new WavFileReader();

	@Override
	public AudioFileFormat getAudioFileFormat(InputStream stream) throws UnsupportedAudioFileException, IOException {
		return parse(stream).getAudioFileFormat();
	}

	@Override
	public AudioFileFormat getAudioFileFormat(URL url) throws UnsupportedAudioFileException, IOException {
		return getAudioFileFormat(open(url));
	}

	@Override
	public AudioFileFormat getAudioFileFormat(File file) throws UnsupportedAudioFileException, IOException {
		return getAudioFileFormat(open(file));
	}

	@Override
	public AudioInputStream getAudioInputStream(InputStream stream) throws UnsupportedAudioFileException, IOException {
		return parse(stream).getAudioInputStream();
	}

	private WavHeader parse(InputStream stream) throws UnsupportedAudioFileException, IOException {
		return new WavHeader(stream);
	}

	@Override
	public AudioInputStream getAudioInputStream(URL url) throws UnsupportedAudioFileException, IOException {
		return getAudioInputStream(open(url));
	}

	@Override
	public AudioInputStream getAudioInputStream(File file) throws UnsupportedAudioFileException, IOException {
		return getAudioInputStream(open(file));
	}

	private InputStream open(URL url) throws IOException {
		return url.openStream();
	}

	private FileInputStream open(File file) throws FileNotFoundException {
		return new FileInputStream(file);
	}

}
