/*
 * Copyright (C) 2013 Luca Veltri - University of Parma - Italy
 * 
 * THE PUBLICATION, REDISTRIBUTION OR MODIFY, COMPLETE OR PARTIAL OF CONTENTS, 
 * CAN BE MADE ONLY AFTER AUTHORIZATION BY THE AFOREMENTIONED COPYRIGHT HOLDER.
 *
 * Author(s):
 * Luca Veltri (luca.veltri@unipr.it)
 */

package org.zoolu.sound.codec.g726;



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
public class G726FormatConversionProvider extends FormatConversionProvider {
	
	/** */
	public static final AudioFormat.Encoding[] NO_ENCODING={};
	/** */
	public static final AudioFormat.Encoding[] PCM_ENCODING={ AudioFormat.Encoding.PCM_SIGNED };
	/** */
	public static final AudioFormat.Encoding[] G726_ENCODING={ G726Encoding.G726_24, G726Encoding.G726_32, G726Encoding.G726_40 };
	/** */
	public static final AudioFormat.Encoding[] BOTH_ENCODINGS={ AudioFormat.Encoding.PCM_SIGNED, G726Encoding.G726_24, G726Encoding.G726_32, G726Encoding.G726_40 };
	/** */
	public static final AudioFormat[] NO_FORMAT={};

	/** Debug */
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
			return G726_ENCODING;
		}
		else
		if (source_format.getEncoding() instanceof G726Encoding) {
			return PCM_ENCODING;
		}
		else {
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
		printOut("getTargetFormats(): target_encoding="+target_encoding.toString());
		printOut("getTargetFormats(): source_format="+source_format.toString());

		/*if (source_format.getEncoding().equals(AudioFormat.Encoding.PCM_SIGNED) && target_encoding instanceof G726Encoding) {
			
			if (source_format.getChannels()>0 && source_format.getChannels()<=2 && !source_format.isBigEndian()) {
				AudioFormat[] formats= {
					// new AudioFormat(encoding, sample rate, sample size in bits, channels, frame size, frame rate, is big endian)
					//new AudioFormat(target_encoding,source_format.getSampleRate(),-1,source_format.getChannels(),-1,-1,false)
					new AudioFormat(target_encoding,source_format.getSampleRate(),-1,source_format.getChannels(),3,1000,false),
					new AudioFormat(target_encoding,source_format.getSampleRate(),-1,source_format.getChannels(),4,1000,false),
					new AudioFormat(target_encoding,source_format.getSampleRate(),-1,source_format.getChannels(),5,1000,false)
				};
				return formats;
			}
			else  {
				AudioFormat[] formats={};
				return formats;
			}
		}
		else*/
		if (source_format.getEncoding().equals(AudioFormat.Encoding.PCM_SIGNED) && target_encoding.equals(G726Encoding.G726_24)) {
			
			if (source_format.getChannels()>0 && source_format.getChannels()<=2 && !source_format.isBigEndian()) {
				AudioFormat[] formats={  new AudioFormat(target_encoding,source_format.getSampleRate(),-1,source_format.getChannels(),3,source_format.getSampleRate()/8,false)  };
				return formats;
			}
			else  {
				AudioFormat[] formats={};
				return formats;
			}
		}
		else
		if (source_format.getEncoding().equals(AudioFormat.Encoding.PCM_SIGNED) && target_encoding.equals(G726Encoding.G726_32)) {
			
			if (source_format.getChannels()>0 && source_format.getChannels()<=2 && !source_format.isBigEndian()) {
				AudioFormat[] formats={  new AudioFormat(target_encoding,source_format.getSampleRate(),-1,source_format.getChannels(),4,source_format.getSampleRate()/8,false)  };
				return formats;
			}
			else  {
				AudioFormat[] formats={};
				return formats;
			}
		}
		else
		if (source_format.getEncoding().equals(AudioFormat.Encoding.PCM_SIGNED) && target_encoding.equals(G726Encoding.G726_40)) {
			
			if (source_format.getChannels()>0 && source_format.getChannels()<=2 && !source_format.isBigEndian()) {
				AudioFormat[] formats={  new AudioFormat(target_encoding,source_format.getSampleRate(),-1,source_format.getChannels(),5,source_format.getSampleRate()/8,false)  };
				return formats;
			}
			else  {
				AudioFormat[] formats={};
				return formats;
			}
		}
		else {
			
			if (source_format.getEncoding() instanceof G726Encoding && target_encoding.equals(AudioFormat.Encoding.PCM_SIGNED)) {
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
		printOut("getAudioInputStream(): target_encoding="+target_encoding.toString());
		printOut("getAudioInputStream(): source format="+source_stream.getFormat().toString());

		if (isConversionSupported(target_encoding,source_stream.getFormat())) {
			AudioFormat[] formats=getTargetFormats(target_encoding,source_stream.getFormat());
			if (formats!=null && formats.length>0) {
				
				AudioFormat source_format=source_stream.getFormat();
				AudioFormat target_format=formats[0];
				if (source_format.equals(target_format)) {
					return source_stream;
				}
				else 
				if (source_format.getEncoding() instanceof G726Encoding && target_format.getEncoding().equals(AudioFormat.Encoding.PCM_SIGNED)) {
					return new G726ToPcmAudioInputStream(source_stream/*,target_format*/);
				}
				else
				if (source_format.getEncoding().equals(AudioFormat.Encoding.PCM_SIGNED) && target_format.getEncoding() instanceof G726Encoding) {
					return new PcmToG726AudioInputStream(source_stream,target_format);
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
		printOut("getAudioInputStream(): target_format="+target_format.toString());
		printOut("getAudioInputStream(): source format="+source_stream.getFormat().toString());
		
		if (isConversionSupported(target_format,source_stream.getFormat())) {
			AudioFormat[] formats=getTargetFormats(target_format.getEncoding(),source_stream.getFormat());
			if (formats!=null && formats.length>0) {
				AudioFormat source_format=source_stream.getFormat();
				if (source_format.equals(target_format)) {
					return source_stream;
				}
				else
				if (source_format.getEncoding() instanceof G726Encoding && target_format.getEncoding().equals(AudioFormat.Encoding.PCM_SIGNED)) {
					return new G726ToPcmAudioInputStream(source_stream/*,target_format*/);
				}
				else
				if (source_format.getEncoding().equals(AudioFormat.Encoding.PCM_SIGNED) && target_format.getEncoding() instanceof G726Encoding) {
					return new PcmToG726AudioInputStream(source_stream,target_format);
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
		if (DEBUG) System.err.println("DEBUG: G726: "+str);
	}
}
