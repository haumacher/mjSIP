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

import org.slf4j.LoggerFactory;
import org.zoolu.sound.BufferedAudioInputStream;



/** PCM-to-AMR AudioInputStream transcoder.
  */
class PcmToAmrAudioInputStream extends BufferedAudioInputStream {
	
	private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(PcmToAmrAudioInputStream.class);

	/** PCM-to-AMR encoder */
	PcmToAmrEncoder amr_encoder;
	
	/** PCM buffer */
	byte[] pcm_buffer=new byte[320];



	/** Creates a new PcmToAmrAudioInputStream. */
	public PcmToAmrAudioInputStream(AudioInputStream input_stream, AudioFormat target_format) {
		super(input_stream,target_format,target_format.getFrameSize());
		amr_encoder=new PcmToAmrEncoder(target_format.getEncoding());
	}


	/** Reads a block of bytes from the inner input stream.
	  * @param buffer the buffer where the the bytes are read to
	  * @return the number of bytes that have been read */
	@Override
	protected int innerRead(byte[] buffer) {
		try {
			input_stream.read(pcm_buffer);
			return amr_encoder.encode(pcm_buffer,0,pcm_buffer.length,buffer,0);
		}
		catch (java.io.IOException e) {
			if (DEBUG) {
				/*
				 * 
				 * TODO looks a bit strange
				 * In DEBUG mode we want an error in this function to end the whole program,
				 * after some logging
				 * 
				 * but in any other mode we want this function silently return -1.    
				 */
				LOG.debug("innerRead()", e);
				System.exit(0);  
			}
			return -1;
		}
	}


	/** Gets the number of bytes that are currently available in the inner input stream for reading.
	  * @return the number of bytes that can be still read from the inner input stream */
	@Override
	protected int innerAvailable() throws java.io.IOException {
		return (input_stream.available()/320)*amr_encoder.getAmrFrameSize();
	}

}
