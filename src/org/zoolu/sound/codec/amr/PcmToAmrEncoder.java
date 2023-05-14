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

import org.zoolu.sound.codec.AMR;
import org.zoolu.util.Encoder;



/** PCM-to-AMR Encoder. */
public class PcmToAmrEncoder implements Encoder {
	
	/** Default AMR codec */
	public static int DEFAULT_AMR_MODE=AMR.M0_0475;


	/** AMR codec */
	AMR amr;
	
	/** AMR mode */
	int amr_mode=-1;


	
	/** Creates a new PcmToAmrEncoder. */
	public PcmToAmrEncoder() {
		amr=new AMR();
		amr_mode=DEFAULT_AMR_MODE;
	}


	/** Creates a new PcmToAmrEncoder.
	  * @param amr_mode AMR mode */
	public PcmToAmrEncoder(int amr_mode) {
		amr=new AMR();
		this.amr_mode=amr_mode;
	}


	/** Creates a new PcmToAmrEncoder.
	  * @param amr_encoding AMR mode */
	public PcmToAmrEncoder(AudioFormat.Encoding amr_encoding) {
		amr=new AMR();
		if (amr_encoding instanceof AmrEncoding) amr_mode=((AmrEncoding)amr_encoding).getMode();
		if (amr_mode<0) amr_mode=DEFAULT_AMR_MODE;
	}


	/** Gets AMR mode.
	  * @return the AMR mode */
	public int getAmrMode() {
		return amr_mode;
	}


	/** Gets AMR frame size.
	  * @return the AMR frame size for the current AMR mode */
	public int getAmrFrameSize() {
		return AMR.frameSize(amr_mode);
	}


	/** Encodes the input chunk in_buff and returns the encoded chuck into out_buff.
	  * @return the actual size of the output data */
	public int encode(byte[] in_buff, int in_offset, int in_len, byte[] out_buff, int out_offset) {
		if (in_len!=320) return 0;
		//try {
			short[] in_aux=new short[160];
			for (int i=0; i<160; i++) {
				// convert little-endian byte array to signed short
				in_aux[i]=(short)(((in_buff[in_offset+1]&0xFF)<<8)+(in_buff[in_offset]&0xFF));
				in_offset+=2;
			}
			int frame_size=AMR.frameSize(amr_mode);
			byte[] out_aux=(out_offset==0)? out_buff : new byte[frame_size];
			amr.encode(amr_mode,in_aux,out_aux);
			if (out_aux!=out_buff) System.arraycopy(out_aux,0,out_buff,out_offset,frame_size);
			return frame_size;
		//}
		//catch (Exception e) {}
		//return 0;
	}

}
