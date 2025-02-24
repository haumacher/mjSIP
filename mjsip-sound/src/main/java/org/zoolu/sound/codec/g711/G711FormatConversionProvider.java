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

import org.slf4j.LoggerFactory;



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
	
	private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(G711FormatConversionProvider.class);

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
	public static final boolean DEBUG = LOG.isDebugEnabled();
	//public static final boolean DEBUG=false;


	/** Obtains the set of source format encodings from which format conversion
	  * services are provided by this provider.
	  * @return array of source format encodings.
	  * The array will always have a length of at least 1. */
	@Override
	public AudioFormat.Encoding[] getSourceEncodings() {
		AudioFormat.Encoding[] encodings=BOTH_ENCODINGS;
		return encodings;
	}
 
 
	/** Obtains the set of target format encodings to which format conversion
	  * services are provided by this provider.
	  * @return array of target format encodings.
	  * The array will always have a length of at least 1. */
	@Override
	public AudioFormat.Encoding[] getTargetEncodings() {
		AudioFormat.Encoding[] encodings=BOTH_ENCODINGS;
		return encodings;
	}

  
	/** Obtains the set of target format encodings supported by the format
	  * converter given a particular source format. If no target format encodings
	  * are supported for this source format, an array of length 0 is returned.
	  * @param source_format format of the incoming data.
	  * @return array of supported target format encodings. */
	@Override
	public AudioFormat.Encoding[] getTargetEncodings(final AudioFormat source_format) {
		if (DEBUG)
			LOG.debug("getTargetEncodings(): source_format={}", source_format);

		if (source_format.getEncoding().equals(AudioFormat.Encoding.PCM_SIGNED)) {
			if (DEBUG)
				LOG.debug("getTargetEncodings(): target encoding: G711_ENCODING");
			return G711_ENCODING;
		}
		else
		if (source_format.getEncoding() instanceof G711Encoding) {
			if (DEBUG)
				LOG.debug("getTargetEncodings(): target encoding: PCM_ENCODING");
			return PCM_ENCODING;
		}
		else {
			if (DEBUG)
				LOG.debug("getTargetEncodings(): target encoding: NO_ENCODING");
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
	@Override
	public AudioFormat[] getTargetFormats(final AudioFormat.Encoding target_encoding, final AudioFormat source_format) {
		if (DEBUG)
			LOG.debug("getTargetFormats(): source format={}", source_format);
		if (DEBUG)
			LOG.debug("getTargetFormats(): target encoding={}", target_encoding);

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
	@Override
	public AudioInputStream getAudioInputStream(final AudioFormat.Encoding target_encoding, final AudioInputStream source_stream) {
		LOG.debug("getAudioInputStream(Encoding,AudioInputStream): source format={}", source_stream.getFormat());
		LOG.debug("getAudioInputStream(Encoding,AudioInputStream): target encoding={}", target_encoding);
		if(DEBUG) // TODO I don't know how komplex getTargetFormats is, so I keep this if(DEBUG)
			LOG.debug("getAudioInputStream(Encoding,AudioInputStream): size: {}", getTargetFormats(target_encoding,source_stream.getFormat()).length);
		
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
					throw new IllegalArgumentException("Unable to convert "+source_format+" to "+target_format);
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
	@Override
	public AudioInputStream getAudioInputStream(final AudioFormat target_format, final AudioInputStream source_stream) {
		AudioFormat sourceFormat = source_stream.getFormat();
		
		LOG.debug("getAudioInputStream(AudioFormat,AudioInputStream): source format={}", sourceFormat);
		LOG.debug("getAudioInputStream(AudioFormat,AudioInputStream): target format={}", target_format);
		
		if (isConversionSupported(target_format,sourceFormat)) {
			AudioFormat[] formats=getTargetFormats(target_format.getEncoding(),sourceFormat);
			if (formats!=null && formats.length>0) {
				AudioFormat source_format=sourceFormat;
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
					throw new IllegalArgumentException("Unable to convert "+source_format+" to "+target_format);
				}
			}
			else {
				throw new IllegalArgumentException("Target format not found: " + target_format);
			}
		}
		else {
			throw new IllegalArgumentException("Conversion not supported: " + sourceFormat + " -> " + target_format);
		}
	}
}
