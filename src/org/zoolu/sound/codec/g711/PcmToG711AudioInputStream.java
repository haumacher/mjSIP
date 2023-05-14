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

import org.zoolu.sound.BufferedAudioInputStream;
import org.zoolu.sound.codec.G711;



/** PcmToG711AudioInputStream transcoder.
  */
class PcmToG711AudioInputStream extends BufferedAudioInputStream {
	
	/** Buffer size */
	static final int BUFFER_SIZE=1;

	/** Auxiliar buffer */
	byte[] aux_buffer;

	/** G711 encoding */
	AudioFormat.Encoding g711_encoding;


	/** Creates a new PcmToG711AudioInputStream. */
	public PcmToG711AudioInputStream(AudioInputStream input_stream, AudioFormat target_format) {
		super(input_stream,target_format,BUFFER_SIZE);
		printOut("PcmToG711AudioInputStream()");
		g711_encoding=target_format.getEncoding();
		
		if (g711_encoding!=G711Encoding.G711_ULAW && g711_encoding!=G711Encoding.G711_ALAW) {
			System.err.println("ERROR: PcmToG711AudioInputStream: unknown G711 encoding type: "+g711_encoding.toString());  
		}

		aux_buffer=new byte[2*BUFFER_SIZE];
	}


	/** Reads a block of bytes from the inner input stream.
	  * @param buffer the buffer where the the bytes are read to
	  * @return the number of bytes that have been read */
	protected int innerRead(byte[] buffer) {
		try {
			int aux_len=input_stream.read(aux_buffer);
			int aux_len_div2=aux_len/2;
			for (int i=0; i<aux_len_div2; i++) {
				int i2=i*2;
				int linear=org.zoolu.sound.codec.G726.signedIntLittleEndian(aux_buffer[i2+1],aux_buffer[i2]);
				if (g711_encoding==G711Encoding.G711_ULAW) buffer[i]=(byte)G711.linear2ulaw(linear);
				else buffer[i]=(byte)G711.linear2alaw(linear);
			}
			return aux_len_div2;
		}
		catch (java.io.IOException e) {
			if (DEBUG) {  e.printStackTrace();  System.exit(0);  }
			return -1;
		}
	}


	/** Gets the number of bytes that are currently available in the inner input stream for reading.
	  * @return the number of bytes that can be still read from the inner input stream */
	protected int innerAvailable() throws java.io.IOException {
		return input_stream.available()/2;
	}

}
