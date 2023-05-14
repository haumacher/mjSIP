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



/** PCM-to-G711 Encoder. */
public class PcmToG711Encoder implements Encoder {
	
	/** G711 encoding (PCM ULAW or ALAW) */
	AudioFormat.Encoding g711_encoding;


	/** Creates a new PcmToG711Encoder. */
	public PcmToG711Encoder(AudioFormat.Encoding g711_encoding) {
		this.g711_encoding=g711_encoding;
	}


	/** Encodes the input chunk in_buff and returns the encoded chuck into out_buff.
	  * It returns the actual size of the output data. */
	public int encode(byte[] in_buff, int in_offset, int in_len, byte[] out_buff, int out_offset) {
		int out_len=in_len/2;
		int out_end=out_offset+out_len;
		int j=in_offset;
		for (int i=out_offset; i<out_end; i++,j+=2) {
			// convert little-endian byte array to signed int
			//int linear=org.zoolu.ext.sound.codec.G726.signedIntLittleEndian(in_buff[j+1],in_buff[j]);
			int linear=((in_buff[j+1]&0xFF)<<8)+(in_buff[j]&0xFF);
			if (g711_encoding==G711Encoding.G711_ULAW) out_buff[i]=(byte)G711.linear2ulaw(linear);
			else out_buff[i]=(byte)G711.linear2alaw(linear);
		}
		return out_len;
	}


	/** PCM-to-G711.ALAW Encoder. */
	public static class ALAW extends PcmToG711Encoder {
		
		/** Creates a new PcmToG711alawEncoder. */
		public ALAW() {
			super(G711Encoding.G711_ALAW);
		}
	
	}
	
	
	/** PCM-to-G711.ULAW Encoder. */
	public static class ULAW extends PcmToG711Encoder {
		
		/** Creates a new PcmToG711ulawEncoder. */
		public ULAW() {
			super(G711Encoding.G711_ULAW);
		}
	
	}

}
