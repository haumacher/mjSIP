package org.mjsip.media;

import org.zoolu.util.Encoder;


/** Couples a data encoder with a data decoder.
 */ 
public class Codec {

	/** Encoder */
	Encoder encoder;
	
	/** Decoder */
	Encoder decoder;
	
	
	/** Creates a new codec.
	 * @param encoder the encoder
	 * @param decoder the decoder */
	public Codec(Encoder encoder, Encoder decoder) {
		this.encoder=encoder;
		this.decoder=decoder;
	}
	
	
	/** Gets encoder */
	Encoder getEncoder() {
		return encoder;
	}

	/** Gets decoder */
	Encoder getDecoder() {
		return decoder;
	}

}
