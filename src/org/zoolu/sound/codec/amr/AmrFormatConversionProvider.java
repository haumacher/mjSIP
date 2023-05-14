/*
 * Copyright (C) 2013 Luca Veltri - University of Parma - Italy
 * 
 * THE PUBLICATION, REDISTRIBUTION OR MODIFY, COMPLETE OR PARTIAL OF CONTENTS, 
 * CAN BE MADE ONLY AFTER AUTHORIZATION BY THE AFOREMENTIONED COPYRIGHT HOLDER.
 *
 * Author(s):
 * Luca Veltri (luca.veltri@unipr.it)
 */

package org.zoolu.sound.codec.amr;



import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.spi.FormatConversionProvider;

import org.zoolu.sound.codec.AMR;



/** A format conversion provider provides format conversion services from one or
  * more input formats to one or more output formats. Converters include codecs,
  * which encode and/or decode audio data, as well as transcoders, etc.
  * Format converters provide methods for determining what conversions are
  * supported and for obtaining an audio stream from which converted data can be
  * read.
  * 
  * The source format represents the format of the incoming audio data, which
  * will be converted.
  * 
  * The target format represents the format of the processed, converted audio
  * data. This is the format of the data that can be read from the stream
  * returned by one of the getAudioInputStream methods.
  */
public class AmrFormatConversionProvider extends FormatConversionProvider {
	
	/** No encodings */
	public static final AudioFormat.Encoding[] NO_ENCODINGS={};

	/** PCM encodings */
	public static final AudioFormat.Encoding[] PCM_ENCODINGS={ AudioFormat.Encoding.PCM_SIGNED };

	/** AMR encodings */
	public static final AudioFormat.Encoding[] AMR_ENCODINGS={ AmrEncoding.AMR_NB, AmrEncoding.AMR_0475, AmrEncoding.AMR_0515, AmrEncoding.AMR_0590, AmrEncoding.AMR_0670, AmrEncoding.AMR_0740, AmrEncoding.AMR_0795, AmrEncoding.AMR_1020, AmrEncoding.AMR_1220 };

	/** PCM and AMR encodings */
	public static final AudioFormat.Encoding[] BOTH_ENCODINGS={ AudioFormat.Encoding.PCM_SIGNED, AmrEncoding.AMR_NB, AmrEncoding.AMR_0475, AmrEncoding.AMR_0515, AmrEncoding.AMR_0590, AmrEncoding.AMR_0670, AmrEncoding.AMR_0740, AmrEncoding.AMR_0795, AmrEncoding.AMR_1020, AmrEncoding.AMR_1220 };

	/** */
	public static final AudioFormat[] NO_FORMAT={};

	/** Debug mode */
	//public static boolean DEBUG=true;
	public static boolean DEBUG=false;


	/** Obtains the set of source format encodings from which format conversion
	  * services are provided by this provider.
	  * @return array of source format encodings.
	  * The array will always have a length of at least 1. */
	public AudioFormat.Encoding[] getSourceEncodings() {
		AudioFormat.Encoding[] encodings=BOTH_ENCODINGS;
		return encodings;
	}
 
 
	/** Obtains the set of target format encodings to which format conversion
	  * services are provided by this provider.
	  * @return array of target format encodings.
	  * The array will always have a length of at least 1. */
	public AudioFormat.Encoding[] getTargetEncodings() {
		AudioFormat.Encoding[] encodings=BOTH_ENCODINGS;
		return encodings;
	}

  
	/** Obtains the set of target format encodings supported by the format
	  * converter given a particular source format. If no target format encodings
	  * are supported for this source format, an array of length 0 is returned.
	  * @param source_format format of the incoming data.
	  * @return array of supported target format encodings. */
	public AudioFormat.Encoding[] getTargetEncodings(final AudioFormat source_format) {
		printOut("getTargetEncodings(): source_format="+source_format.toString());

		if (source_format.getEncoding().equals(AudioFormat.Encoding.PCM_SIGNED)) {
			return AMR_ENCODINGS;
		}
		else
		if (source_format.getEncoding() instanceof AmrEncoding) {
			return PCM_ENCODINGS;
		}
		else {
			return NO_ENCODINGS;
		}
	}


	/** Obtains the set of target formats with the encoding specified supported by
	  * the format converter. If no target formats with the specified encoding are
	  * supported for this source format, an array of length 0 is returned.
	  * @param target_encoding desired encoding of the outgoing data.
	  * @param source_format format of the incoming data.
	  * @return array of supported target formats.
	  */
	public AudioFormat[] getTargetFormats(final AudioFormat.Encoding target_encoding, final AudioFormat source_format) {
		printOut("getTargetFormats(): source format="+source_format.toString());
		printOut("getTargetFormats(): target encoding="+target_encoding.toString());

		AudioFormat[] formats={};
		if (source_format.getEncoding().equals(AudioFormat.Encoding.PCM_SIGNED) && target_encoding instanceof AmrEncoding) {
			if (source_format.getChannels()>0 && source_format.getChannels()<=2 && !source_format.isBigEndian()) {
				int amr_mode=((AmrEncoding)target_encoding).getMode();
				int frame_size=(amr_mode>=0)? AMR.frameSize(amr_mode) : -1;
				formats=new AudioFormat[] {
					// new AudioFormat(encoding, sample rate, sample size in bits, channels, frame size, frame rate, is big endian)
					new AudioFormat(target_encoding,8000,-1,1,frame_size,50,false)
				};
			}
		}
		else {
			if (source_format.getEncoding() instanceof AmrEncoding && target_encoding.equals(AudioFormat.Encoding.PCM_SIGNED)) {
				formats=new AudioFormat[] {
					// new AudioFormat(sample rate, sample size in bits, channels, signed, is big endian)
					new AudioFormat(source_format.getSampleRate(),16,source_format.getChannels(),true,false)
				};
			}
		}
		return formats;
	}
  
	/** Obtains an audio input stream with the specified encoding from the given
	  * audio source stream.
	  * @param target_encoding - desired encoding of the stream after processing.
	  * @param source_stream - stream from which data to be processed should be read.
	  * @return stream from which processed data with the specified target
	  * encoding may be read.
	  * @exception IllegalArgumentException - if the format combination supplied
	  * is not supported. */
	public AudioInputStream getAudioInputStream(final AudioFormat.Encoding target_encoding, final AudioInputStream source_stream) {
		printOut("getAudioInputStream(Encoding,AudioInputStream): source format="+source_stream.getFormat().toString());
		printOut("getAudioInputStream(Encoding,AudioInputStream): target encoding="+target_encoding.toString());
		AudioFormat source_format=source_stream.getFormat();
		if (isConversionSupported(target_encoding,source_format)) {
			AudioFormat[] formats=getTargetFormats(target_encoding,source_format);
			return getAudioInputStream(formats,source_stream);
		}
		else {
			throw new IllegalArgumentException("Conversion not supported\n  target_encoding="+target_encoding.toString()+"\n  source_format="+source_format);
		}
	}
  
	/** Obtains an audio input stream with the specified format from the given
	  * audio source stream.
	  * @param target_format - desired data format of the stream after processing.
	  * @param source_stream - stream from which data to be processed should be read.
	  * @return stream from which processed data with the specified format may be
	  * read.
	  * @exception IllegalArgumentException - if the format combination supplied
	  * is not supported. */
	public AudioInputStream getAudioInputStream(final AudioFormat target_format, final AudioInputStream source_stream) {
		printOut("getAudioInputStream(AudioFormat,AudioInputStream): source format="+source_stream.getFormat().toString());
		printOut("getAudioInputStream(AudioFormat,AudioInputStream): target format="+target_format.toString());
		AudioFormat source_format=source_stream.getFormat();
		if (isConversionSupported(target_format,source_format)) {
			AudioFormat[] formats=getTargetFormats(target_format.getEncoding(),source_format);
			return getAudioInputStream(formats,source_stream);
		}
		else {
			throw new IllegalArgumentException("Conversion not supported\n  target_format="+target_format.toString()+"\n  source_format="+source_format);
		}
	}


	/** Obtains an audio input stream with one of the specified available target formats
	  * from the given audio source stream.
	  * @param available_target_formats available target formats after processing
	  * @param source_stream stream from which data to be processed should be read
	  * @return stream from which processed data with the specified format may be
	  * read
	  * @exception IllegalArgumentException - if the format combination supplied
	  * is not supported */
	private AudioInputStream getAudioInputStream(final AudioFormat[] available_target_formats, final AudioInputStream source_stream) {
		if (available_target_formats!=null && available_target_formats.length>0) {
			AudioFormat source_format=source_stream.getFormat();
			AudioFormat target_format=available_target_formats[0];
			if (source_format.equals(target_format)) {
				return source_stream;
			}
			else 
			if (source_format.getEncoding() instanceof AmrEncoding && target_format.getEncoding().equals(AudioFormat.Encoding.PCM_SIGNED)) {
				return new AmrToPcmAudioInputStream(source_stream/*,target_format*/);
			}
			else
			if (source_format.getEncoding().equals(AudioFormat.Encoding.PCM_SIGNED) && target_format.getEncoding() instanceof AmrEncoding) {
				return new PcmToAmrAudioInputStream(source_stream,target_format);
			}
			else {
				throw new IllegalArgumentException("Unable to convert "+source_format.toString()+" to "+target_format.toString());
			}
		}
		else  {
			throw new IllegalArgumentException("Target format not found");
		}
	}


	/** Prints debug information. */
	private void printOut(String str) {
		if (DEBUG) System.err.println("DEBUG: AMR codec: "+str);
	}
}
