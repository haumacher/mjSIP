/*
 * Copyright (C) 2013 Luca Veltri - University of Parma - Italy
 * 
 * THE PUBLICATION, REDISTRIBUTION OR MODIFY, COMPLETE OR PARTIAL OF CONTENTS, 
 * CAN BE MADE ONLY AFTER AUTHORIZATION BY THE AFOREMENTIONED COPYRIGHT HOLDER.
 *
 * Author(s):
 * Luca Veltri (luca.veltri@unipr.it)
 */

package org.zoolu.sound.codec.g711;



import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.spi.FormatConversionProvider;



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
public class G711FormatConversionProvider extends FormatConversionProvider {
	
	/** */
	public static final AudioFormat.Encoding[] NO_ENCODING={};
	/** */
	public static final AudioFormat.Encoding[] PCM_ENCODING={ AudioFormat.Encoding.PCM_SIGNED };
	/** */
	public static final AudioFormat.Encoding[] G711_ENCODING={ G711Encoding.G711_ULAW, G711Encoding.G711_ALAW };
	/** */
	public static final AudioFormat.Encoding[] BOTH_ENCODINGS={ AudioFormat.Encoding.PCM_SIGNED, G711Encoding.G711_ULAW, G711Encoding.G711_ALAW };
	/** */
	public static final AudioFormat[] NO_FORMAT={};

	/** Debug */
	public static boolean DEBUG=true;
	//public static boolean DEBUG=false;


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
			printOut("getTargetEncodings(): target encoding: G711_ENCODING");
			return G711_ENCODING;
		}
		else
		if (source_format.getEncoding() instanceof G711Encoding) {
			printOut("getTargetEncodings(): target encoding: PCM_ENCODING");
			return PCM_ENCODING;
		}
		else {
			printOut("getTargetEncodings(): target encoding: NO_ENCODING");
			return NO_ENCODING;
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

		if (source_format.getEncoding().equals(AudioFormat.Encoding.PCM_SIGNED) && target_encoding instanceof G711Encoding) {
			
			if (source_format.getChannels()>0 && source_format.getChannels()<=2 && !source_format.isBigEndian()) {
				float sample_rate=source_format.getSampleRate();
				int channels=source_format.getChannels();
				AudioFormat[] formats= {
					// new AudioFormat(encoding, sample rate, sample size in bits, channels, frame size, frame rate, is big endian)
					new AudioFormat(G711Encoding.G711_ULAW,sample_rate,8,channels,1,sample_rate,false),
					new AudioFormat(G711Encoding.G711_ALAW,sample_rate,8,channels,1,sample_rate,false)
				};
				return formats;
			}
			else  {
				AudioFormat[] formats={};
				return formats;
			}
		}
		else {
			if (source_format.getEncoding() instanceof G711Encoding && target_encoding.equals(AudioFormat.Encoding.PCM_SIGNED)) {
				AudioFormat[] formats= {
					// new AudioFormat(sample rate, sample size in bits, channels, signed, is big endian)
					new AudioFormat(source_format.getSampleRate(),16,source_format.getChannels(),true,false)
				};
				return formats;
			}
			else {
				AudioFormat[] formats={};
				return formats;
			}
		}
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
		printOut("getAudioInputStream(Encoding,AudioInputStream): size: "+getTargetFormats(target_encoding,source_stream.getFormat()).length);
		
		if (isConversionSupported(target_encoding,source_stream.getFormat())) {
			AudioFormat[] formats=getTargetFormats(target_encoding,source_stream.getFormat());
			if (formats!=null && formats.length>0) {
				
				AudioFormat source_format=source_stream.getFormat();
				AudioFormat target_format=formats[0];
				if (source_format.equals(target_format)) {
					return source_stream;
				}
				else 
				if (source_format.getEncoding() instanceof G711Encoding && target_format.getEncoding().equals(AudioFormat.Encoding.PCM_SIGNED)) {
					return new G711ToPcmAudioInputStream(source_stream/*,target_format*/);
				}
				else
				if (source_format.getEncoding().equals(AudioFormat.Encoding.PCM_SIGNED) && target_format.getEncoding() instanceof G711Encoding) {
					return new PcmToG711AudioInputStream(source_stream,target_format);
				}
				else {
					throw new IllegalArgumentException("Unable to convert "+source_format.toString()+" to "+target_format.toString());
				}
			}
			else  {
				throw new IllegalArgumentException("Target format not found");
			}
		}
		else {
			throw new IllegalArgumentException("Conversion not supported");
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
		
		if (isConversionSupported(target_format,source_stream.getFormat())) {
			AudioFormat[] formats=getTargetFormats(target_format.getEncoding(),source_stream.getFormat());
			if (formats!=null && formats.length>0) {
				AudioFormat source_format=source_stream.getFormat();
				if (source_format.equals(target_format)) {
					return source_stream;
				}
				else
				if (source_format.getEncoding() instanceof AudioFormat.Encoding && target_format.getEncoding().equals(AudioFormat.Encoding.PCM_SIGNED)) {
					return new G711ToPcmAudioInputStream(source_stream/*,target_format*/);
				}
				else
				if (source_format.getEncoding().equals(AudioFormat.Encoding.PCM_SIGNED) && target_format.getEncoding() instanceof AudioFormat.Encoding) {
					return new PcmToG711AudioInputStream(source_stream,target_format);
				}
				else {
					throw new IllegalArgumentException("Unable to convert "+source_format.toString()+" to "+target_format.toString());
				}
			}
			else {
				throw new IllegalArgumentException("Target format not found");
			}
		}
		else {
			throw new IllegalArgumentException("Conversion not supported");
		}
	}


	/** Prints debugging information. */
	private void printOut(String str) {
		if (DEBUG) System.err.println("DEBUG: G711 codec: "+str);
	}
}
