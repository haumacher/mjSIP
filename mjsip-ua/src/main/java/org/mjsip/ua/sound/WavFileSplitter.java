/*
 * Copyright (c) 2023 Bernhard Haumacher et al. All Rights Reserved.
 */
package org.mjsip.ua.sound;

import java.io.File;
import java.io.IOError;
import java.io.IOException;
import java.io.OutputStream;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.mjsip.sound.AudioFile;

/**
 * Tool splitting an input WAV file into parts where the input is separated by regions of silence.
 */
public class WavFileSplitter implements SilenceListenerAdapter {
	
	private File _file;
	private AlawSilenceTrimmer _trimmer;
	private AudioFormat _format;
	private OutputStream _out;
	
	/**
	 * The number of the current part created.
	 */
	private File _outputDir;
	private int _bufferTime = 20;
	private int _minSilenceTime = 1000;
	private int _paddingTime = 500;
	private int _silenceDb = -30;

	private int _part = 1;
	
	/** 
	 * Creates a {@link WavFileSplitter}.
	 *
	 * @param file The WAV input file.
	 */
	public WavFileSplitter(File file) {
		_file = file;
	}
	
	/**
	 * Sets the output directory.
	 */
	public WavFileSplitter setOutputDir(File outputDir) {
		_outputDir = outputDir;
		return this;
	}

	/**
	 * Starts the splitting.
	 */
	public void run() throws IOException, UnsupportedAudioFileException {
		try (AudioInputStream in = AudioFile.getAudioFileInputStream(_file.getAbsolutePath())) {
			_format = in.getFormat();
			int sampleRate = (int) _format.getSampleRate();
			_trimmer = new AlawSilenceTrimmer(sampleRate, _bufferTime, _minSilenceTime, _paddingTime, _silenceDb, null, this);
			byte[] buffer = new byte[sampleRate * _bufferTime / 1000];
			int direct;
			while ((direct = in.read(buffer)) >= 0) {
				_trimmer.write(buffer, 0, direct);
			}
			_trimmer.close();
			flushOut();
		}
	}
	
	/**
	 * The number of output files produced so far.
	 */
	public int getPartCntCreated() {
		return _part - 1;
	}
	
	@Override
	public void onSilenceEnded(long clock) {
		try {
			flushOut();
			_out = AudioFile.getAudioFileOutputStream(toFile(getOutputFileName(_part++)).getAbsolutePath(), _format);
			_trimmer.setOut(_out);
		} catch (IOException ex) {
			throw new IOError(ex);
		}
	}

	/** 
	 * Creates an output file name for the part with the given ID.
	 */
	protected String getOutputFileName(int partId) {
		String fileName = _file.getName();
		String partName = fileName.substring(0, fileName.length() - 4) + "-part" + partId + ".wav";
		return partName;
	}

	private File toFile(String partName) {
		return new File(_outputDir == null ? _file.getParentFile() : _outputDir, partName);
	}
	
	@Override
	public void onSilenceStarted(long clock) {
		try {
			flushOut();
		} catch (IOException ex) {
			throw new IOError(ex);
		}
	}

	private void flushOut() throws IOException {
		if (_out != null) {
			_out.close();
			_out = null;
		}
	}

	/**
	 * Main entry point of the tool.
	 *
	 * @param args The file name of the WAV file to split.
	 */
	public static void main(String[] args) throws Exception {
		String fileName = args[0];
		new WavFileSplitter(new File(fileName)).run();
	}

}
