/*
 * Copyright (C) 2013 Luca Veltri - University of Parma - Italy
 * 
 * THE PUBLICATION, REDISTRIBUTION OR MODIFY, COMPLETE OR PARTIAL OF CONTENTS, 
 * CAN BE MADE ONLY AFTER AUTHORIZATION BY THE AFOREMENTIONED COPYRIGHT HOLDER.
 *
 * Author(s):
 * Luca Veltri (luca.veltri@unipr.it)
 */

package org.zoolu.sound;



import java.io.IOException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.spi.FormatConversionProvider;

import org.zoolu.sound.codec.amr.AmrEncoding;
import org.zoolu.sound.codec.amr.AmrFormatConversionProvider;
import org.zoolu.sound.codec.g711.G711Encoding;
import org.zoolu.sound.codec.g711.G711FormatConversionProvider;
import org.zoolu.sound.codec.g726.G726Encoding;
import org.zoolu.sound.codec.g726.G726FormatConversionProvider;



/** ConverterAudioSystem extends class ExtendedAudioSystem providing audio conversion.
  * ConverterAudioSystem adds the the following static methods: <br>
  * <ul>
  * <li> convertAudioInputStream(AudioFormat,AudioInputStream,FormatConversionProvider)
  *       - for obtaining a new AudioInputStream of a specified format
  *       from a specified AudioInputStream using a specified FormatConversionProvider.</li>
  *
  * <li> convertAudioInputStream(String,AudioInputStream,FormatConversionProvider)
  *       - for obtaining a new AudioInputStream of a specified format
  *       from a specified AudioInputStream;
  *       the format is specified by codec type and sample rate.</li>
  *
  * <li> convertAudioOutputStream(AudioFormat,AudioOutputStream,FormatConversionProvider)
  *       - for obtaining a new AudioOutputStream of a specified format
  *       from a specified AudioOutputStream using a specified FormatConversionProvider.</li>
  *
  * <li> convertAudioOutputStream(String,AudioOutputStream,FormatConversionProvider)
  *       - for obtaining a new AudioOutputStream of a specified format
  *       from a specified AudioOutputStream;
  *       the format is specified by codec type and sample rate.</li>
  *
  * </ul>
  */
public class ConverterAudioSystem extends SimpleAudioSystem {
	

	/** Gets a new AudioInputStream of a specified format
	  * from a specified AudioInputStream using a specified FormatConversionProvider. */
	public static AudioInputStream convertAudioInputStream(AudioFormat format, AudioInputStream is, FormatConversionProvider converter) {	
		return converter.getAudioInputStream(format,is);
	}


	/** Gets a new AudioInputStream with a specified encoding
	  * from a specified AudioInputStream using a specified FormatConversionProvider. */
	/*public static AudioInputStream convertAudioInputStream(AudioFormat.Encoding encoding, AudioInputStream is, FormatConversionProvider converter) {
		
		return converter.getAudioInputStream(encoding,is);
	}*/


	/** Gets a new AudioInputStream of a specified format
	  * from a specified AudioInputStream.
	  * The format is specified by codec type and sample rate. */
	public static AudioInputStream convertAudioInputStream(CodecType codec, float sample_rate, AudioInputStream is) throws Exception {
		FormatConversionProvider converter=getFormatConversionProvider(codec);
		AudioFormat format=getAudioFormat(codec,sample_rate,is.getFormat().getChannels());
		System.out.println("DEBUG: ConverterAudioSystem: source format: "+is.getFormat());
		System.out.println("DEBUG: ConverterAudioSystem: target format: "+format);
		return convertAudioInputStream(format,is,converter);
	}


	/** Gets a new AudioOutputStream of a specified format
	  * from a specified AudioOutputStream using a specified FormatConversionProvider. */
	public static AudioOutputStream convertAudioOutputStream(AudioFormat format, AudioOutputStream os, FormatConversionProvider converter) {
		try {
			return new ConvertedAudioOutputStream(format,os,converter);
		}
		catch (IOException e) {  e.printStackTrace(); return null;  }
	} 


	/** Gets a new AudioOutputStream with a specified encoding
	  * from a specified AudioOutputStream using a specified FormatConversionProvider. */
	/*public static AudioOutputStream convertAudioOutputStream(AudioFormat.Encoding encoding, AudioOutputStream os, FormatConversionProvider converter) {
		try {
			return new ConvertedAudioOutputStream(encoding,os,converter);
		}
		catch (IOException e) {  e.printStackTrace(); return null;  }
	}*/


	/** Gets a new AudioOutputStream of a specified format
	  * from a specified AudioOutputStream.
	  * The format is specified by codec type and sample rate. */
	public static AudioOutputStream convertAudioOutputStream(CodecType codec, float sample_rate, AudioOutputStream os) throws Exception {
		FormatConversionProvider converter=getFormatConversionProvider(codec);
		AudioFormat format=getAudioFormat(codec,sample_rate,os.getFormat().getChannels());
		System.out.println("DEBUG: ConverterAudioSystem: source format: "+format);
		System.out.println("DEBUG: ConverterAudioSystem: target format: "+os.getFormat());
		return convertAudioOutputStream(format,os,converter);
	}


	/** Gets the AudioFormat with a specified codec type, sample rate, and number of channels. */
	private static AudioFormat getAudioFormat(CodecType codec, float sample_rate, int channels) throws Exception {
		AudioFormat.Encoding encoding=null;
		int sample_size=-1;
		int frame_size=-1;
		float frame_rate=-1;
		
		if (codec.equals(CodecType.G711_ULAW)) {
			encoding=G711Encoding.G711_ULAW;
			sample_size=8;
			frame_size=1;
			frame_rate=sample_rate;
		}
		else
		if (codec.equals(CodecType.G711_ALAW)) {
			encoding=G711Encoding.G711_ALAW;
			sample_size=8;
			frame_size=1;
			frame_rate=sample_rate;
		}
		else
		if (codec.equals(CodecType.G726_24)) {
			encoding=G726Encoding.G726_24;
			sample_size=-1;
			frame_size=3;
			frame_rate=sample_rate/8;
		}
		else
		if (codec.equals(CodecType.G726_32)) {
			encoding=G726Encoding.G726_32;
			sample_size=-1;
			frame_size=4;
			frame_rate=sample_rate/8;
		}
		else
		if (codec.equals(CodecType.G726_40)) {
			encoding=G726Encoding.G726_40;
			sample_size=-1;
			frame_size=5;
			frame_rate=sample_rate/8;
		}
		else
		if (codec.equals(CodecType.GSM0610)) {
			String encoding_class_name="org.tritonus.share.sampled.Encodings";
			encoding=(AudioFormat.Encoding)Class.forName(encoding_class_name).getMethod("getEncoding",new Class[]{ String.class }).invoke(null,new Object[]{ "GSM0610" });
			frame_size=33;
			frame_rate=sample_rate/160; // = 50 frames/sec in case of sample rate = 8000 Hz
		}
		else
		if (codec.equals(CodecType.AMR_NB)) {
			encoding=AmrEncoding.AMR_NB;
			frame_size=codec.getFrameSize();
			frame_rate=sample_rate/160; // = 50 frames/sec in case of sample rate = 8000 Hz
		}
		else
		if (codec.equals(CodecType.AMR_0475)) {
			encoding=AmrEncoding.AMR_0475;
			frame_size=codec.getFrameSize();
			frame_rate=sample_rate/160; // = 50 frames/sec in case of sample rate = 8000 Hz
		}
		else
		if (codec.equals(CodecType.AMR_0515)) {
			encoding=AmrEncoding.AMR_0515;
			frame_size=codec.getFrameSize();
			frame_rate=sample_rate/160; // = 50 frames/sec in case of sample rate = 8000 Hz
		}
		else
		if (codec.equals(CodecType.AMR_0590)) {
			encoding=AmrEncoding.AMR_0590;
			frame_size=codec.getFrameSize();
			frame_rate=sample_rate/160; // = 50 frames/sec in case of sample rate = 8000 Hz
		}
		else
		if (codec.equals(CodecType.AMR_0670)) {
			encoding=AmrEncoding.AMR_0670;
			frame_size=codec.getFrameSize();
			frame_rate=sample_rate/160; // = 50 frames/sec in case of sample rate = 8000 Hz
		}
		else
		if (codec.equals(CodecType.AMR_0740)) {
			encoding=AmrEncoding.AMR_0740;
			frame_size=codec.getFrameSize();
			frame_rate=sample_rate/160; // = 50 frames/sec in case of sample rate = 8000 Hz
		}
		else
		if (codec.equals(CodecType.AMR_0795)) {
			encoding=AmrEncoding.AMR_0795;
			frame_size=codec.getFrameSize();
			frame_rate=sample_rate/160; // = 50 frames/sec in case of sample rate = 8000 Hz
		}
		else
		if (codec.equals(CodecType.AMR_1020)) {
			encoding=AmrEncoding.AMR_1020;
			frame_size=codec.getFrameSize();
			frame_rate=sample_rate/160; // = 50 frames/sec in case of sample rate = 8000 Hz
		}
		else
		if (codec.equals(CodecType.AMR_1220)) {
			encoding=AmrEncoding.AMR_1220;
			frame_size=codec.getFrameSize();
			frame_rate=sample_rate/160; // = 50 frames/sec in case of sample rate = 8000 Hz
		}
		return new AudioFormat(encoding,sample_rate,sample_size,channels,frame_size,frame_rate,false);
	}


	/** Gets suitable converter for a specified codec type. */
	private static FormatConversionProvider getFormatConversionProvider(CodecType codec) throws Exception {
		FormatConversionProvider converter=null;
	
		if (codec.equals(CodecType.G711_ULAW)) converter=new G711FormatConversionProvider();
		else
		if (codec.equals(CodecType.G711_ALAW)) converter=new G711FormatConversionProvider();
		else
		if (codec.equals(CodecType.G726_24)) converter=new G726FormatConversionProvider();
		else
		if (codec.equals(CodecType.G726_32)) converter=new G726FormatConversionProvider();
		else
		if (codec.equals(CodecType.G726_40)) converter=new G726FormatConversionProvider();
		else
		if (codec.equals(CodecType.GSM0610)) {
			String converter_class_name="org.tritonus.sampled.convert.gsm.GSMFormatConversionProvider";
			converter=(FormatConversionProvider)Class.forName(converter_class_name).getConstructor((Class[])null).newInstance((Object[])null);
		}
		else
		if (codec.equals(CodecType.AMR_0475)) converter=new AmrFormatConversionProvider();
		else
		if (codec.equals(CodecType.AMR_0515)) converter=new AmrFormatConversionProvider();
		else
		if (codec.equals(CodecType.AMR_0590)) converter=new AmrFormatConversionProvider();
		else
		if (codec.equals(CodecType.AMR_0670)) converter=new AmrFormatConversionProvider();
		else
		if (codec.equals(CodecType.AMR_0740)) converter=new AmrFormatConversionProvider();
		else
		if (codec.equals(CodecType.AMR_0795)) converter=new AmrFormatConversionProvider();
		else
		if (codec.equals(CodecType.AMR_1020)) converter=new AmrFormatConversionProvider();
		else
		if (codec.equals(CodecType.AMR_1220)) converter=new AmrFormatConversionProvider();
		
		return converter;
	}
}
