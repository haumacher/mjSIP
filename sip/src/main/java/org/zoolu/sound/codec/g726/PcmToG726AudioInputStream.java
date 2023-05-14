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

import org.zoolu.sound.BufferedAudioInputStream;
import org.zoolu.sound.codec.G726;
import org.zoolu.sound.codec.G726_24;
import org.zoolu.sound.codec.G726_32;
import org.zoolu.sound.codec.G726_40;



/** PcmToG726AudioInputStream transcoder.
  */
class PcmToG726AudioInputStream extends BufferedAudioInputStream {
	
	/** G726 codec */
	G726 encoder;
	
	/** Auxiliar buffer */
	byte[] aux_buffer;


	/** Creates a new PcmToG726AudioInputStream. */
	public PcmToG726AudioInputStream(AudioInputStream input_stream, AudioFormat target_format) {
		super(input_stream,target_format,60);
		printOut("PcmToG726AudioInputStream()");
		AudioFormat.Encoding g726_encoding=target_format.getEncoding();
		
		int size=0;
		if (g726_encoding==G726Encoding.G726_32) {
			encoder=new G726_32();
			size=240;
		}
		else
		if (g726_encoding==G726Encoding.G726_24) {
			encoder=new G726_24();
			size=320;
		}
		else
		if (g726_encoding==G726Encoding.G726_40) {
			encoder=new G726_40();
			size=192;
		}
		else {
			System.err.println("ERROR: PcmToG726AudioInputStream: unknown G726 encoding type: "+g726_encoding.toString());  
		}

		aux_buffer=new byte[size];
	}


	/** Reads a block of bytes from the inner input stream.
	  * @param buffer the buffer where the the bytes are read to
	  * @return the number of bytes that have been read */
	protected int innerRead(byte[] buffer) {
		try {
			int aux_len=input_stream.read(aux_buffer);
			int len=encoder.encode(aux_buffer,0,aux_len,G726.AUDIO_ENCODING_LINEAR,buffer,0);
			return len;
		}
		catch (java.io.IOException e) {
			return -1;
		}
	}


	/** Gets the number of bytes that are currently available in the inner input stream for reading.
	  * @return the number of bytes that can be still read from the inner input stream */
	protected int innerAvailable() throws java.io.IOException {
		if (encoder.getEncoding()==G726Encoding.G726_32) return input_stream.available()/4; // *4 /8 /2
		else
		if (encoder.getEncoding()==G726Encoding.G726_24) return input_stream.available()*3/16; // *3 /8 /2
		else
		if (encoder.getEncoding()==G726Encoding.G726_40) return input_stream.available()*5/16; // *5 /8 /2
		// otherwise
		return 0;
	}

}
