/*
 * Copyright (C) 2013 Luca Veltri - University of Parma - Italy
 * 
 * THE PUBLICATION, REDISTRIBUTION OR MODIFY, COMPLETE OR PARTIAL OF CONTENTS, 
 * CAN BE MADE ONLY AFTER AUTHORIZATION BY THE AFOREMENTIONED COPYRIGHT HOLDER.
 *
 * Author(s):
 * Luca Veltri (luca.veltri@unipr.it)
 */

package org.zoolu.sound.codec;




/** Adaptive Multi-Rate (AMR-NB) CODEC. 
  */
public class AMR {
	

	/* Mode AMR 4.75 kbps */
	public static final int M0_0475=0;

	/* Mode AMR 5.15 kbps */
	public static final int M1_0515=1;

	/* Mode AMR 5.9 kbps */
	public static final int M2_0590=2;

	/* Mode AMR 6.7 kbps */
	public static final int M3_0670=3;

	/* Mode AMR 7.4 kbps */
	public static final int M4_0740=4;

	/* Mode AMR 7.95 kbps */
	public static final int M5_0795=5;

	/* Mode AMR 10.2 kbps */
	public static final int M6_1020=6;

	/* Mode AMR 12.2 kbps */
	public static final int M7_1220=7;

	/* AMR SID (comfort noise) */
	public static final int M8_SID=8;
	
	/* No Data */
	public static final int M15_NO_DATA=15;



	/** AMR file header */
	public static final byte[] AMR_HEADER=("#!AMR\n").getBytes();



	/** Gets frame size (in bytes) for a given AMR mode including one byte of AMR header (i.e. ToC entry, specifing the frame mode).
	  * @param mode the AMR mode
	  * @return the frame size (in bytes) including Table of Contents (ToC) entry (1 byte) */
	public static int frameSize(int mode) {
		//return (framePayloadBitSize(mode)+7)/8+1;
		// i.e.:
		switch (mode) {
			case M0_0475 : return 13; // 95 bits
			case M1_0515 : return 14; // 103 bits
			case M2_0590 : return 16; // 118 bits
			case M3_0670 : return 18; // 134 bits
			case M4_0740 : return 20; // 148 bits
			case M5_0795 : return 21; // 159 bits
			case M6_1020 : return 27; // 204 bits
			case M7_1220 : return 32; // 244 bits
			case M8_SID : return 6; // 39 bits AMR SID (comfort noise)
			case M15_NO_DATA : return 1; // 39 bits AMR SID (comfort noise)
			default : return 0;
		}
	}


	/** Gets frame payload size, in bits, for a given AMR mode, without considering any AMR header.
	  * @param mode the AMR mode
	  * @return the size in bits of the frame payload (without AMR header) for a given AMR mode */
	public static int framePayloadBitSize(int mode) {
		switch (mode) {
			case M0_0475 : return 95;
			case M1_0515 : return 103;
			case M2_0590 : return 118;
			case M3_0670 : return 134;
			case M4_0740 : return 148;
			case M5_0795 : return 159;
			case M6_1020 : return 204;
			case M7_1220 : return 244;
			case M8_SID : return 39; // AMR SID (comfort noise)
			case M15_NO_DATA : return 0;
			default : return 0;
		}
	}


	/** Whether the AMR native library is already loaded */
	private static boolean library_loaded=false;


	/** Loads the amr library */
	private static synchronized void loadLibrary() {
		if (library_loaded) return;
		// else
		// BEGIN PATCH FOR LINUX
		// manually load "libopencore-amrnb.so" since it is not automatically loaded by "libamr.so" on linux OS
		try { System.loadLibrary("opencore-amrnb"); } catch (UnsatisfiedLinkError e) {}
		// END
		try {
			//System.loadLibrary("cygopencore-amrnb-0");
			System.loadLibrary("amr-64");
		}
		catch (Error e_64) {
			System.err.println(e_64.getMessage());
			try { System.loadLibrary("amr-32"); }
			catch (Error e_32) {
				System.err.println(e_32.getMessage());
				System.loadLibrary("amr");
			}
		}
		library_loaded=true;
	}


	/** Encoder */
	int encoder=0;

	/** Decoder */
	int decoder=0;



	// ***************************** Costructors: ****************************


	/** Creates a new AMR. */
	public AMR() {
		if (!library_loaded) loadLibrary();
	}


	// *************************** Public methods: ***************************

	/** Encodes an AMR frame (160 audio samples) using AMR 4.75 mode.
	  * @param frame input PCM frame
	  * @param outbuf output buffer for the AMR frame
	  * @return the length of the the encoded AMR frame */
	/*public int encode(short[] frame, byte[] outbuf) {
		return encode(M_0475,frame,outbuf);
	}*/

	/** Encodes an AMR frame (160 audio samples) using the given AMR mode.
	  * @param mode AMR mode
	  * @param frame input PCM frame
	  * @param outbuf output buffer for the AMR frame
	  * @return the length of the the encoded AMR frame */
	public int encode(int mode, short[] frame, byte[] outbuf) {
		if (encoder==0) encoder=initEncoder(0);
		return encode(encoder,mode,frame,outbuf,0);
	}


	/** Decodes an AMR frame (160 audio samples).
	  * @param inbuf input AMR frame
	  * @param frame output buffer for the PCM frame */
	public void decode(byte[] inbuf, short[] frame) {
		try {
			if (decoder==0) decoder=initDecoder();
			if (decoder!=0 && inbuf!=null && frame!=null) decode(decoder,inbuf,frame,0);
			else System.err.println("DEBUG: org.zoolu.ext.sound.codec.AMR: decode("+decoder+","+inbuf+","+frame+",0): Invalid parameter(s).");
		}
		catch (Throwable t) {  t.printStackTrace();  }
	}


	// *************************** Native methods: ***************************


	/** void* Encoder_Interface_init(int dtx) */
	private native int initEncoder(int dtx);  
	
	/** void Encoder_Interface_exit(void* state) */
	private native void exitEncoder(int state_ptr);

	/** int Encoder_Interface_Encode(void* state, enum Mode mode, const short* speech, unsigned char* out, int forceSpeech) */
	private native int encode(int state_ptr, int mode, short[] speech, byte[] out, int forceSpeech);



	/** void* Decoder_Interface_init(void) */
	private native int initDecoder();  
	
	/** void Decoder_Interface_exit(void* state) */
	private native void exitDecoder(int state_ptr);

	/** void Decoder_Interface_Decode(void* state, const unsigned char* in, short* out, int bfi) */
	private native void decode(int state_ptr, byte[] in, short[] out, int bfi);
	//private native void decode(int state_ptr, int[] in, short[] out, int bfi);

}
