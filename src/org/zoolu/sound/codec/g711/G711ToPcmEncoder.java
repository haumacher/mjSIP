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

import org.zoolu.sound.codec.G711;
import org.zoolu.util.Encoder;



/** G711-to-PCM Encoder. */
public class G711ToPcmEncoder implements Encoder {
	
	/** G711 encoding (PCM ULAW or ALAW) */
	AudioFormat.Encoding g711_encoding;


	/** Creates a new G711ToPcmEncoder */
	public G711ToPcmEncoder(AudioFormat.Encoding g711_encoding) {
		this.g711_encoding=g711_encoding;
	}


	/** Encodes the input chunk in_buff and returns the encoded chuck into out_buff.
	  * It returns the actual size of the output data. */
	public int encode(byte[] in_buff, int in_offset, int in_len, byte[] out_buff, int out_offset) {
		int in_end=in_offset+in_len;
		int j=out_offset;      
		for (int i=in_offset; i<in_end; i++) {
			int linear=0;
			if (g711_encoding==G711Encoding.G711_ULAW) linear=G711.ulaw2linear(in_buff[i]);
			else linear=G711.alaw2linear(in_buff[i]);
			// convert signed int to little-endian byte array
			out_buff[j++]=(byte)(linear&0xFF);
			out_buff[j++]=(byte)(linear>>8);
		}
		return in_len*2;
	}


	/** G711.ALAW-to-PCM Encoder. */
	public static class ALAW extends G711ToPcmEncoder {
		
		/** Creates a new G711alawToPcmEncoder */
		public ALAW() {
			super(G711Encoding.G711_ALAW);
		}
	
	}

	/** G711.ULAW-to-PCM Encoder. */
	public static class ULAW extends G711ToPcmEncoder {
		
		/** Creates a new G711ulawToPcmEncoder */
		public ULAW() {
			super(G711Encoding.G711_ULAW);
		}
	
	}

}
