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



/** Encodings used by the G711 audio decoder.
  */
public class G711Encoding extends AudioFormat.Encoding {
	
  /** Specifies G.711 ULAW 64 Kbps encoding. */
  public static final G711Encoding G711_ULAW=new G711Encoding("G711_ULAW");

  /** Specifies G.711 ALAW 64 Kbps encoding. */
  public static final G711Encoding G711_ALAW=new G711Encoding("G711_ALAW");


  /** Constructs a new encoding.
	 * @param name - Name of the G711 encoding. */
  public G711Encoding(final String name)
  {  super(name);
  }

}
