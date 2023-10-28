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



/** Encodings used by the AMR audio decoder.
  */
public class AmrEncoding extends AudioFormat.Encoding {
	
	/** Specifies generic AMR narrow band (AMR-NB) encoding. */
	//public static final AmrEncoding AMR_NB=new AmrEncoding("AMR_NB",AMR.M0_0475); // default AMR 4.75
	public static final AmrEncoding AMR_NB=new AmrEncoding("AMR_NB",-1);

	/** Specifies AMR (Narrow Band) 4.75 kbps encoding. */
	public static final AmrEncoding AMR_0475=new AmrEncoding("AMR_0475",AMR.M0_0475);

	/** Specifies AMR (Narrow Band) 5.15 kbps encoding. */
	public static final AmrEncoding AMR_0515=new AmrEncoding("AMR_0515",AMR.M1_0515);

	/** Specifies AMR (Narrow Band) 5.9 kbps encoding. */
	public static final AmrEncoding AMR_0590=new AmrEncoding("AMR_0590",AMR.M2_0590);

	/** Specifies AMR (Narrow Band) 6.7 kbps encoding. */
	public static final AmrEncoding AMR_0670=new AmrEncoding("AMR_0670",AMR.M3_0670);

	/** Specifies AMR (Narrow Band) 7.4 kbps encoding. */
	public static final AmrEncoding AMR_0740=new AmrEncoding("AMR_0740",AMR.M4_0740);

	/** Specifies AMR (Narrow Band) 7.95 kbps encoding. */
	public static final AmrEncoding AMR_0795=new AmrEncoding("AMR_0795",AMR.M5_0795);

	/** Specifies AMR (Narrow Band) 10.2 kbps encoding. */
	public static final AmrEncoding AMR_1020=new AmrEncoding("AMR_1020",AMR.M6_1020);

	/** Specifies AMR (Narrow Band) 12.2 kbps encoding. */
	public static final AmrEncoding AMR_1220=new AmrEncoding("AMR_1220",AMR.M7_1220);

	/** Specifies AMR (Narrow Band) 4.75 kbps encoding. */
	//public static final AmrEncoding AMR=new AmrEncoding("AMR");

	/** Specifies AMR WB (Wide Band) encoding. */
	//public static final AmrEncoding AMR_WB=new AmrEncoding("AMR_WB");


	/** AMR mode */
	int mode;  



	/** Constructs a new encoding.
	  * @param name name of the AMR encoding
	  * @param mode AMR mode */
	protected AmrEncoding(final String name, int mode) {
		super(name);
		this.mode=mode;   
	}


	/** Gets the AMR mode.
	  * @return the AMR mode of this encoding (or -1 if the AMR mode is unspecified) */
	public int getMode() {
		return mode;
	}
}
