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



/** GSM-to-PCM Encoder. */
public class GsmToPcmEncoder implements Encoder {
	
	/** GSMDecoder costructor */
	java.lang.reflect.Constructor gsm_decoder_constructor=null;

	/** GSMDecoder */
	//Object gsm_decoder=null;

	/** GSMDecoder decode() method */
	java.lang.reflect.Method gsm_decoder_decode=null;
	
	/** Creates a new GsmToPcmEncoder */
	public GsmToPcmEncoder() {
		try {
			// begin of java reflection for org.tritonus.lowlevel.gsm.GSMDecoder.decode()
			Class gsm_decoder_class=Class.forName("org.tritonus.lowlevel.gsm.GSMDecoder");
			gsm_decoder_constructor=gsm_decoder_class.getConstructor((Class[])null);
			//gsm_decoder=gsm_decoder_constructor.newInstance((Object[])null);
			gsm_decoder_decode=gsm_decoder_class.getMethod("decode",new Class[]{ byte[].class, int.class, byte[].class, int.class, boolean.class });
			// end of java reflection
		}
		catch (Exception e) {  e.printStackTrace();  }
	}

	/** Encodes the input chunk in_buff and returns the encoded chuck into out_buff.
	  * It returns the actual size of the output data. */
	public int encode(byte[] in_buff, int in_offset, int in_len, byte[] out_buff, int out_offset) {
		int out_len=160;
		boolean big_endian=false;
		try {
			//(new org.tritonus.lowlevel.gsm.GSMDecoder()).decode(in_buff,in_offset,out_buff,out_offset,big_endian);   
			gsm_decoder_decode.invoke(gsm_decoder_constructor.newInstance((Object[])null),new Object[]{ in_buff, new Integer(in_offset), out_buff, new Integer(out_offset), new Boolean(big_endian) });
			//gsm_decoder_decode.invoke(gsm_decoder,new Object[]{ in_buff, new Integer(in_offset), out_buff, new Integer(out_offset), new Boolean(big_endian) });
			return out_len;
		}
		catch (Exception e) {}
		return 0;
	}
}
