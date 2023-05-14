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



import org.zoolu.sound.codec.AMR;
import org.zoolu.util.Encoder;



/** AMR-to-PCM Encoder. */
public class AmrToPcmEncoder implements Encoder {
	
	/** AMR codec */
	AMR amr;
	
	
	/** Creates a new AmrToPcmEncoder */
	public AmrToPcmEncoder() {
		amr=new AMR();
	}


	/** Encodes the input chunk in_buff and returns the encoded chuck into out_buff.
	  * @return the actual size of the output data */
	public int encode(byte[] in_buff, int in_offset, int in_len, byte[] out_buff, int out_offset) {
		//int mode=(in_buff[in_offset]>>3)&0xf;
		//int frame_size=AMR.frameSize(mode);
		//if (in_len!=frame_size) return 0;
		//try {
			if (in_offset!=0) {
				byte[] in_aux=new byte[in_len];
				System.arraycopy(in_buff,in_offset,in_aux,0,in_len);
				in_buff=in_aux;
			}
			short[] out_aux=new short[160];
			amr.decode(in_buff,out_aux);
			for (int i=0; i<160; i++) {
				short linear=out_aux[i];
				// convert signed short to little-endian byte array
				out_buff[out_offset++]=(byte)(linear&0xFF);
				out_buff[out_offset++]=(byte)(linear>>8);
			}
			return 320;
		//}
		//catch (Exception e) {}
		//return 0;
	}
}
