/*
 * Copyright (C) 2013 Luca Veltri - University of Parma - Italy
 * 
 * THE PUBLICATION, REDISTRIBUTION OR MODIFY, COMPLETE OR PARTIAL OF CONTENTS, 
 * CAN BE MADE ONLY AFTER AUTHORIZATION BY THE AFOREMENTIONED COPYRIGHT HOLDER.
 *
 * Author(s):
 * Luca Veltri (luca.veltri@unipr.it)
 */

package org.zoolu.sound.codec.gsm;



import org.zoolu.util.Encoder;



/** PCM-to-GSM Encoder. */
public class PcmToGsmEncoder implements Encoder {
	
	/** Encoder constructor */
	java.lang.reflect.Constructor gsm_encoder_constructor=null;

	/** Encoder */
	//Object gsm_encoder=null;

	/** Encoder encode() method */
	java.lang.reflect.Method gsm_encoder_encode=null;

	/** Creates a new PcmToGsmEncoder */
	public PcmToGsmEncoder() {
		try {
			// begin of java reflection for org.tritonus.lowlevel.gsm.Encoder.encode()
			Class gsm_encoder_class=Class.forName("org.tritonus.lowlevel.gsm.Encoder");
			gsm_encoder_constructor=gsm_encoder_class.getConstructor((Class[])null);
			//gsm_encoder=gsm_encoder_constructor.newInstance((Object[])null);
			gsm_encoder_encode=gsm_encoder_class.getMethod("encode",new Class[]{ short[].class, byte[].class });
			// end of java reflection
		}
		catch (Exception e) {  e.printStackTrace();  }
	}

	/** Encodes the input chunk in_buff and returns the encoded chuck into out_buff.
	  * It returns the actual size of the output data. */
	public int encode(byte[] in_buff, int in_offset, int in_len, byte[] out_buff, int out_offset) {
		int out_len=33;
		short[] asBuffer=new short[in_len/2];
		int j=in_offset;
		for (int i=0; i<asBuffer.length; i++,j+=2) asBuffer[i]=(short)org.zoolu.sound.codec.G726.signedIntLittleEndian(in_buff[j+1],in_buff[j]);
		byte[] abFrame=new byte[out_len];
		try {
			//(new org.tritonus.lowlevel.gsm.Encoder()).encode(asBuffer,abFrame);
			gsm_encoder_encode.invoke(gsm_encoder_constructor.newInstance((Object[])null),new Object[]{ asBuffer, abFrame });
			//gsm_encoder_encode.invoke(gsm_encoder,new Object[]{ asBuffer, abFrame });
			int k=out_offset;
			for (int i=0; i<out_len; i++) out_buff[k++]=abFrame[i];
			return out_len;
		}
		catch (Exception e) {}
		return 0;
	}

}
