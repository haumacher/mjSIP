/*
 * Copyright (C) 2017 Luca Veltri - University of Parma - Italy
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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

import org.zoolu.sound.CodecType;
import org.zoolu.sound.SimpleAudioSystem;
import org.zoolu.sound.codec.AMR;
import org.zoolu.sound.codec.amr.AmrEncoding;
import org.zoolu.sound.codec.amr.AmrFormatConversionProvider;
import org.zoolu.sound.codec.g711.G711Encoding;
import org.zoolu.sound.codec.g711.G711FormatConversionProvider;

/** Methods for reading from and writing to audio files.
 * <p>
 * Current supported audio formats are: WAV, AU, AMR, RAW (raw data).
 */
public class AudioFile {

	/** Gets audio input stream from a file.
	  * No audio format conversion is applied. 
	  * <p>
	  * Supported audio file formats are: WAV (wave), AU, and AMR.
	  * @param file_name the file name */
	 public static AudioInputStream getAudioFileInputStream(String file_name) throws FileNotFoundException, IOException, javax.sound.sampled.UnsupportedAudioFileException {
		 return getAudioFileInputStream(file_name,null);
	 }

		 
	/** Gets audio input stream from a file.
	  * If necessary, it also provides audio format conversion from the file format to the given target audio format.
	  * <p>
	  * Supported audio file formats are: WAV (wave), AU, AMR, and RAW.
	  * Raw audio files contain only audio frames without any description header.
	  * @param fileName the file name
	  * @param targetFormat the target audio format. If not <i>null</i>, audio conversion is performed.
	  * Note: In case of AU file, this is the audio format assigned to raw audio */
	public static AudioInputStream getAudioFileInputStream(String fileName, AudioFormat targetFormat) throws FileNotFoundException, IOException, javax.sound.sampled.UnsupportedAudioFileException {
		File file = new File(fileName);
		String lowerCaseFileName = fileName.toLowerCase();
		AudioInputStream audio;
		if (lowerCaseFileName.endsWith(".wav")) {
			 audio = WavFileReader.INSTANCE.getAudioInputStream(file);
		} else if (lowerCaseFileName.endsWith(".au")) {
			// AU
			InputStream file_input_stream=new FileInputStream(file);
			AuFileHeader au=new AuFileHeader(file_input_stream);
			int sample_rate=au.getSampleRate();
			int encoding_format=au.getEncodingFormat();
			AudioFormat.Encoding encoding=null;
			switch (encoding_format) {
				case 1 : encoding=G711Encoding.G711_ULAW; break;
				case 27 : encoding=G711Encoding.G711_ALAW; break;
			}
			AudioFormat sourceFormat=new AudioFormat(encoding,sample_rate,8,1,1,sample_rate,false);
			audio=new AudioInputStream(file_input_stream,sourceFormat,-1);
			if (targetFormat==null) {
				return audio;		
			}
			return new G711FormatConversionProvider().getAudioInputStream(targetFormat,audio);
		} else if (lowerCaseFileName.endsWith(".amr")) {
			// AMR
			InputStream file_input_stream=new FileInputStream(file);
			file_input_stream.skip(("#!AMR\n").getBytes().length);
			//source_audio_format=new AudioFormat(AmrEncoding.AMR_NB,8000.0F,16,1,-1,50.0F,false);
			//source_audio_format=new AudioFormat(AmrEncoding.AMR_0590,8000.0F,16,1,AMR.frameSize(AMR.M_0590),50.0F,false);
			AudioFormat sourceFormat = new AudioFormat(AmrEncoding.AMR_0475,8000.0F,16,1,AMR.frameSize(AMR.M0_0475),50.0F,false);
			audio=new AudioInputStream(file_input_stream,sourceFormat,-1);
			if (targetFormat == null) {
				return audio;
			}
			return new AmrFormatConversionProvider().getAudioInputStream(targetFormat,audio);
		} else {
			audio = AudioSystem.getAudioInputStream(file);
		}

		if (targetFormat==null) {
			return audio;
		}
		return AudioSystem.getAudioInputStream(targetFormat,audio);
	}

	/** Creates an audio file output stream.
	  * It can be used to write audio data to a file. Audio format is not converted.
	  * @param file_name the file name
	  * @param audio_format the audio format */
	 public static OutputStream getAudioFileOutputStream(String file_name, AudioFormat audio_format) throws FileNotFoundException, IOException {
		if (file_name.toLowerCase().endsWith(".wav")) {
			final File tmp = new File(file_name + ".tmp");
			if (tmp.exists()) {
				tmp.delete();
			}
			return new FileOutputStream(tmp, true) {
		    	@Override
				public void close() throws IOException {
					super.close();

					AudioInputStream audioInputStream = new AudioInputStream(new FileInputStream(tmp), audio_format,
							tmp.length());
					AudioSystem.write(audioInputStream, AudioFileFormat.Type.WAVE, new File(file_name));
					audioInputStream.close();

					tmp.delete();
		    	}
		    };
		} else {
			FileOutputStream output_stream=new FileOutputStream(new File(file_name));
			if (file_name.toLowerCase().endsWith(".au")) {
				// AU
				int encoding_format;
				if (audio_format.getEncoding().equals(AudioFormat.Encoding.ULAW)) encoding_format=1;
				else
				if (audio_format.getEncoding().equals(AudioFormat.Encoding.ALAW)) encoding_format=27;
				else encoding_format=100;
				(new AuFileHeader(encoding_format,(int)audio_format.getSampleRate())).writeTo(output_stream);
			}
			else
			if (file_name.toLowerCase().endsWith(".amr")) {
				// AMR
				output_stream.write(("#!AMR\n").getBytes());
			}
			else {
				// RAW
			}
			return output_stream;
		}
	}

	 
	/** Creates an output stream for writing audio data to a file.
	  * <p>
	  * No Audio format conversion is performed.
	  * @param file_name the file name
	  * @param codec the audio codec
	  * @param sample_rate the audio sample rate */
	 public static OutputStream getAudioFileOutputStream(String file_name, CodecType codec, int sample_rate) throws FileNotFoundException, IOException, javax.sound.sampled.UnsupportedAudioFileException {
		AudioFormat audio_format=SimpleAudioSystem.getAudioFormat(codec,sample_rate);
		return getAudioFileOutputStream(file_name,audio_format);  
	}
	
}
