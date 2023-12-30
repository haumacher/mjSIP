package org.mjsip.sound;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFileFormat.Type;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioFormat.Encoding;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.UnsupportedAudioFileException;

public class WavHeader {
	
	private static final int RIFF = magic("RIFF");
	private static final int WAVE = magic("WAVE");
	private static final int FMT = magic("fmt ");
	private static final int DATA = magic("data");
	
	// PCM
	private static final int WAVE_FORMAT_PCM = 0x0001;
			
	// IEEE float
	private static final int WAVE_FORMAT_IEEE_FLOAT = 0x0003;
	
	// 8-bit ITU-T G.711 A-law
	private static final int WAVE_FORMAT_ALAW = 0x0006;
	
	// 8-bit ITU-T G.711 µ-law
	private static final int WAVE_FORMAT_MULAW = 0x0007;
	
	// Determined by SubFormat
	private static final int WAVE_FORMAT_EXTENSIBLE = 0xFFFE;
	
	private InputStream _in;
	private long _limit = Long.MAX_VALUE;
	
	// Format code
	private int _type;
	
	// Number of interleaved channels
	private int _channels;
	
	// Sampling rate (blocks per second)
	private int _sampleRate;
	
	// Data rate
	private int _bytePerSecond;
	
	// (BitsPerSample * Channels) / 8.
	// 1 - 8 bit mono
	// 2 - 8 bit stereo/16 bit mono
	// 4 - 16 bit stereo
	private int _bytesPerFrame;
	
	private int _bitsPerSample;
	private int _validBitsPerSample;
	
	// Speaker position mask
	private int _channelMask;
	
	// GUID, including the data format code
	private String _subFormat;
	private long _dataSize;

	private static int magic(String magic) {
		int result = 0;
		int shift = 0;
		for (int n = 0, cnt = magic.length(); n < cnt; n++, shift+=8) {
			result |= (magic.charAt(n) & 0xFF) << shift;
		}
		return result;
	}

	public WavHeader(InputStream in) throws UnsupportedAudioFileException, IOException {
		_in = in;
		
		int riff = readInt();
		if (riff != RIFF) {
			throw new UnsupportedAudioFileException("Invalid file format, missing header magic value.");
		}
		long contentSize = readUnsignedInt();
		
		long before = limit(contentSize);
		readContent();
		restore(before);
	}

	private void restore(long before) {
		_limit = before;
	}

	private long limit(long limit) {
		long before = _limit;
		_limit = limit;
		return before;
	}

	private void readContent() throws UnsupportedAudioFileException, IOException {
		int wave = readInt();
		if (wave != WAVE) {
			throw new UnsupportedAudioFileException("Invalid file format, missing wave magic value.");
		}

		while (true) {
			int chunkId = readInt();
			long chunkSize = readUnsignedInt();
			
			long before = limit(chunkSize);
			{
				if (chunkId == FMT) {
					readFormat(chunkSize);
				}
				else if (chunkId == DATA) {
					// Data is read in separate call.
					_dataSize = _limit;
					return;
				}
			}
			_in.skip(_limit);
			restore(before);
		}
	}

	private void readFormat(long fmtSize) throws IOException {
		_type = readShort();
		_channels = readShort();
		_sampleRate = readInt();
		_bytePerSecond = readInt();
		_bytesPerFrame = readShort();
		_bitsPerSample = readShort();
		
		if (fmtSize > 16) {
			int extensionSize = readShort();
			
			if (extensionSize > 0) {
				_validBitsPerSample = readShort();
				_channelMask = readInt();
				_subFormat = readString(16);
			}
		}
	}

	private String readString(int cnt) throws IOException {
		StringBuilder buffer = new StringBuilder(cnt);
		for (int n = 0; n < cnt; n++) {
			int ch = read();
			if (ch < 0) {
				throw new EOFException();
			}
			
			buffer.append((char) ch);
		}
		return buffer.toString();
	}

	private long readUnsignedInt() throws IOException {
		return readInt() & 0xFFFFFFFFL;
	}

	private int readInt() throws IOException {
		return read(4);
	}

	private int readShort() throws IOException {
		return read(2);
	}

	private int read(int cnt) throws IOException, EOFException {
		// Little-endian byte order (least significant byte first).
		int result = 0;
		int shift = 0;
		for (int n = 0; n < cnt; n++, shift+=8) {
			int data = read();
			if (data < 0) {
				throw new EOFException();
			}
			
			result |= data << shift;
		}
		return result;
	}

	private int read() throws IOException {
		if (_limit > 0) {
			_limit--;
			return _in.read();
		} else {
			return -1;
		}
	}
	
	public AudioFileFormat getAudioFileFormat() throws UnsupportedAudioFileException {
		AudioFormat audioFormat = getAudioFormat();
		return new AudioFileFormat(Type.WAVE, audioFormat, getSampleCount());
	}

	private int getSampleCount() {
		return (int) (_dataSize / _bytesPerFrame);
	}

	private AudioFormat getAudioFormat() throws UnsupportedAudioFileException {
		Encoding encoding = encoding();
		int sampleSizeInBits = _bitsPerSample;
		float sampleRate = _sampleRate;
		int frameSize = _bytesPerFrame;
		float frameRate = _sampleRate;
		AudioFormat audio = new AudioFormat(encoding, sampleRate, sampleSizeInBits, _channels, frameSize, frameRate, false);
		return audio;
	}

	private Encoding encoding() throws UnsupportedAudioFileException {
		switch (_type) {
		case WAVE_FORMAT_PCM: return Encoding.PCM_SIGNED;
		case WAVE_FORMAT_IEEE_FLOAT: return Encoding.PCM_FLOAT;
		case WAVE_FORMAT_ALAW: return Encoding.ALAW;
		case WAVE_FORMAT_MULAW: return Encoding.ULAW;
		case WAVE_FORMAT_EXTENSIBLE: return new Encoding(_subFormat);
		default: throw new UnsupportedAudioFileException("Unknown type: " + _type);
		}
	}

	public AudioInputStream getAudioInputStream() throws UnsupportedAudioFileException {
		return new AudioInputStream(_in, getAudioFormat(), getSampleCount());
	}

}
