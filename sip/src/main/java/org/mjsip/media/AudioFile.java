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

package org.mjsip.media;



import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.spi.FormatConversionProvider;

import org.zoolu.sound.CodecType;
import org.zoolu.sound.SimpleAudioSystem;
import org.zoolu.sound.codec.AMR;
import org.zoolu.sound.codec.amr.AmrEncoding;
import org.zoolu.sound.codec.amr.AmrFormatConversionProvider;
import org.zoolu.sound.codec.g711.G711Encoding;
import org.zoolu.sound.codec.g711.G711FormatConversionProvider;
import org.zoolu.util.ByteUtils;
import org.zoolu.util.LogLevel;
import org.zoolu.util.Logger;
import org.zoolu.util.SystemUtils;



/** Methods for reading from and writing to audio files.
 * <p>
 * Current supported audio formats are: WAV, AU, AMR, RAW (raw data).
 */
public class AudioFile {

	/** Adds a new string to the default Log */
	private static void log(String str) {
		log(LogLevel.INFO,str);
	}


	/** Adds a new string to the default Log */
	private static void log(LogLevel level, String str) {
		Logger logger=SystemUtils.getDefaultLogger();
		if (logger!=null) logger.log(level,AudioFile.class.getName()+": "+str);
	}


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
	  * @param file_name the file name
	  * @param target_audio_format the target audio format. If not <i>null</i>, audio conversion is performed.
	  * Note: In case of AU file, this is the audio format assigned to raw audio */
	 public static AudioInputStream getAudioFileInputStream(String file_name, AudioFormat target_audio_format) throws FileNotFoundException, IOException, javax.sound.sampled.UnsupportedAudioFileException {
		log(LogLevel.INFO,"input from file "+file_name);
		if (file_name.toLowerCase().endsWith(".wav")) {
			// WAV
			File file=new File(file_name);
			log("File audio format: "+AudioSystem.getAudioFileFormat(file));
			// get AudioInputStream
			AudioInputStream audio_input_stream=AudioSystem.getAudioInputStream(file);
			log("current available bytes="+audio_input_stream.available());
			// apply audio conversion through AudioSystem
			if (target_audio_format!=null) audio_input_stream=AudioSystem.getAudioInputStream(target_audio_format,audio_input_stream);
			return audio_input_stream;
		}
		else {
			InputStream file_input_stream=new FileInputStream(new File(file_name));
			// source audio format
			AudioFormat source_audio_format=null;
			// audio format converter
			FormatConversionProvider converter=null;		
			if (file_name.toLowerCase().endsWith(".au")) {
				// AU
				AuFileHeader au=new AuFileHeader(file_input_stream);
				int sample_rate=au.getSampleRate();
				int encoding_format=au.getEncodingFormat();
				AudioFormat.Encoding encoding=null;
				switch (encoding_format) {
					case 1 : encoding=G711Encoding.G711_ULAW; break;
					case 27 : encoding=G711Encoding.G711_ALAW; break;
				}
				source_audio_format=new AudioFormat(encoding,sample_rate,8,1,1,sample_rate,false);
				if (target_audio_format!=null) converter=new G711FormatConversionProvider();
			}
			else
			if (file_name.toLowerCase().endsWith(".amr")) {
				// AMR
				file_input_stream.skip(("#!AMR\n").getBytes().length);
				//source_audio_format=new AudioFormat(AmrEncoding.AMR_NB,8000.0F,16,1,-1,50.0F,false);
				//source_audio_format=new AudioFormat(AmrEncoding.AMR_0590,8000.0F,16,1,AMR.frameSize(AMR.M_0590),50.0F,false);
				source_audio_format=new AudioFormat(AmrEncoding.AMR_0475,8000.0F,16,1,AMR.frameSize(AMR.M0_0475),50.0F,false);
				if (target_audio_format!=null) converter=new AmrFormatConversionProvider();
			}
			else {
				// RAW
				file_input_stream=new FileInputStream(new File(file_name));
				source_audio_format=target_audio_format;
			}
			// get the audio input stream
			AudioInputStream audio_input_stream=new AudioInputStream(file_input_stream,source_audio_format,-1);
			// apply audio conversion through FormatConversionProvider
			if (converter!=null) audio_input_stream=converter.getAudioInputStream(target_audio_format,audio_input_stream);
			return audio_input_stream;		
		}
	}


	/** Creates an audio file output stream.
	  * It can be used to write audio data to a file. Audio format is not converted.
	  * @param file_name the file name
	  * @param audio_format the audio format */
	 public static OutputStream getAudioFileOutputStream(String file_name, AudioFormat audio_format) throws FileNotFoundException, IOException, javax.sound.sampled.UnsupportedAudioFileException {
		log(LogLevel.INFO,"output to file "+file_name);
		if (file_name.toLowerCase().endsWith(".wav")) {
			// WAV
			/* Wave file header:
				Offset	Value
				0 - 3	"RIFF" (Beginning of the file)	
				4 - 7	File size minus 8
				8 -11	"WAVE" (WAVE format)
				12-15	"fmt "
				16-19	16 (Length of format data as listed above)
				20-21	1 (Type of format: 1 PCM)
				22-23	1 (Number of channels: 1 mono, 2 stereo)
				24-27	44100 (Sample rate)
				28-31	88200 (Sample rate * bits per sample * channels / 8)
				32-33	2 (Bytes per sample * channels)
				34-35	16 (Bits per sample)
				36-39	"data" (Beginning of the data section)
				40-43	Data size (Equal to file size minus 44)
				44-  	Sample values */
			final File file=new File(file_name);
		    AudioInputStream audioInputStream=new AudioInputStream(new ByteArrayInputStream(new byte[0]),audio_format,0);
		    AudioSystem.write(audioInputStream,AudioFileFormat.Type.WAVE,file);
		    return new FileOutputStream(file,true) {
		    	//J5:@Override
		    	public void close() throws IOException {
		    		super.close();  		
		    		// finalize the WAV file
		    		RandomAccessFile raf=new RandomAccessFile(file,"rw");
		    		long len=raf.length();
		    		raf.seek(4);
		    		raf.write(ByteUtils.intToFourBytesLittleEndian(len-8));
		    		raf.seek(16);
		    		int csize=raf.read();
		    		raf.seek(24+csize);
		    		raf.write(ByteUtils.intToFourBytesLittleEndian(len-24-csize-4));
		    		raf.close();
		    	}
		    };
		}
		else {
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
