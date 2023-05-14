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



/** G726ToPcmAudioInputStream transcoder.
  */
class G726ToPcmAudioInputStream extends BufferedAudioInputStream {
	
	/** G726 codec */
	G726 decoder;
	
	/** Auxiliar buffer */
	byte[] aux_buffer;


	/** Creates a new G726ToPcmAudioInputStream. */
	public G726ToPcmAudioInputStream(AudioInputStream input_stream/*, AudioFormat source_format*/) {
		super(input_stream,input_stream.getFormat(),16);
		printOut("G726ToPcmAudioInputStream()");
		AudioFormat.Encoding g726_encoding=input_stream.getFormat().getEncoding();
		
		int size=0;
		if (g726_encoding==G726Encoding.G726_32) {
			decoder=new G726_32(); 
			size=4;
		}
		else
		if (g726_encoding==G726Encoding.G726_24) {
			decoder=new G726_24();
			size=3;
		}
		else
		if (g726_encoding==G726Encoding.G726_40) {
			decoder=new G726_40();
			size=5;
		}
		else {
			System.err.println("ERROR: G726ToPcmAudioInputStream: unknown G726 encoding type: "+g726_encoding.toString());  
		}

		aux_buffer=new byte[size];
	}


	/** Reads a block of bytes from the inner input stream.
	  * @param buffer the buffer where the the bytes are read to
	  * @return the number of bytes that have been read */
	protected int innerRead(byte[] buffer) {
		try {
			int aux_len=input_stream.read(aux_buffer);
			int len=decoder.decode(aux_buffer,0,aux_len,G726.AUDIO_ENCODING_LINEAR,buffer,0);
			return len;
		}
		catch (java.io.IOException e) {
			return -1;
		}
	}


	/** Gets the number of bytes that are currently available in the inner input stream for reading.
	  * @return the number of bytes that can be still read from the inner input stream */
	protected int innerAvailable() throws java.io.IOException {
		if (decoder.getEncoding()==G726Encoding.G726_32) return input_stream.available()*4; // *8 /4 *2
		else
		if (decoder.getEncoding()==G726Encoding.G726_24) return input_stream.available()*16/3; // *8 /3 *2
		else
		if (decoder.getEncoding()==G726Encoding.G726_40) return input_stream.available()*16/5; // *8 /5 *2
		// otherwise
		return 0;
	}

}
