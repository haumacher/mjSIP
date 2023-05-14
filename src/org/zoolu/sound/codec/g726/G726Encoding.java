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



/** Encodings used by the G726 audio decoder.
  */
public class G726Encoding extends AudioFormat.Encoding {
	
  /** Specifies G.721 32 Kbps encoding. */
  public static final G726Encoding G726_32=new G726Encoding("G726_32");

  /** Specifies G.723 24 Kbps encoding. */
  public static final G726Encoding G726_24=new G726Encoding("G726_24");

  /** Specifies G.723 40 Kbps encoding. */
  public static final G726Encoding G726_40=new G726Encoding("G726_40");


  /** Constructs a new encoding.
	 * @param name - Name of the G726 encoding. */
  public G726Encoding(final String name)
  {  super(name);
  }

}
