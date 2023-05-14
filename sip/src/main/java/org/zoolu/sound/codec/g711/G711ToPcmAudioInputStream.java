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



/** G711ToPcmAudioInputStream transcoder.
  */
class G711ToPcmAudioInputStream extends BufferedAudioInputStream {
	
	/** Auxiliar buffer size */
	static final int AUX_BUFFER_SIZE=1;

	/** Auxiliar buffer */
	byte[] aux_buffer;

	/** G711 encoding */
	AudioFormat.Encoding g711_encoding;


	/** Creates a new G711ToPcmAudioInputStream. */
	public G711ToPcmAudioInputStream(AudioInputStream input_stream/*, AudioFormat source_format*/) {
		super(input_stream,input_stream.getFormat(),2*AUX_BUFFER_SIZE);
		printOut("G711ToPcmAudioInputStream()");
		g711_encoding=input_stream.getFormat().getEncoding();
		
		if (g711_encoding!=G711Encoding.G711_ULAW && g711_encoding!=G711Encoding.G711_ALAW) {
			System.err.println("ERROR: G711ToPcmAudioInputStream: unknown G711 encoding type: "+g711_encoding.toString());  
		}
		
		aux_buffer=new byte[AUX_BUFFER_SIZE];
	}


	/** Reads a block of bytes from the inner input stream.
	  * @param buffer the buffer where the the bytes are read to
	  * @return the number of bytes that have been read */
	protected int innerRead(byte[] buffer) {
		try {
			int aux_len=input_stream.read(aux_buffer);
			for (int i=0; i<aux_len; i++) {
				int linear=0;
				if (g711_encoding==G711Encoding.G711_ULAW) linear=G711.ulaw2linear(aux_buffer[i]);
				else linear=G711.alaw2linear(aux_buffer[i]);
				int i2=i*2;
				buffer[i2]=(byte)(linear&0xFF);
				buffer[i2+1]=(byte)(linear>>8);
			}
			return aux_len*2;
		}
		catch (java.io.IOException e) {
			if (DEBUG) {  e.printStackTrace();  System.exit(0);  }
			return -1;
		}
	}


	/** Gets the number of bytes that are currently available in the inner input stream for reading.
	  * @return the number of bytes that can be still read from the inner input stream */
	protected int innerAvailable() throws java.io.IOException {
		return input_stream.available()*2;
	}

}
