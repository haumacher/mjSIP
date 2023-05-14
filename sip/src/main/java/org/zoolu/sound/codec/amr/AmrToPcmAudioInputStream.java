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
import javax.sound.sampled.AudioInputStream;

import org.zoolu.sound.BufferedAudioInputStream;
import org.zoolu.sound.codec.AMR;



/** AMR-to-PCM AudioInputStream transcoder.
  */
class AmrToPcmAudioInputStream extends BufferedAudioInputStream {
	

	/** PCM (linear) audio format */
	private static final AudioFormat PCM_AudioFormat=new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,8000,16,1,2,8000,false);

	/** Maximum AMR frame size */
	private static final int MAX_AMR_FRAME_SIZE=32;

	/** AMR-to-PCM decoder */
	AmrToPcmEncoder amr_decoder=new AmrToPcmEncoder();
	
	/** AMR buffer */
	byte[] amr_buffer=new byte[MAX_AMR_FRAME_SIZE];

	/** AMR frame size from AudioFormat */
	int amr_format_frame_size=-1;



	/** Creates a new AmrToPcmAudioInputStream. */
	public AmrToPcmAudioInputStream(AudioInputStream input_stream/*, AudioFormat source_format*/) {
		super(input_stream,PCM_AudioFormat,320);
		amr_format_frame_size=input_stream.getFormat().getFrameSize();
	}


	/** Reads a block of bytes from the inner input stream.
	  * @param buffer the buffer where the the bytes are read to
	  * @return the number of bytes that have been read */
	protected int innerRead(byte[] buffer) {
		try {
			if (amr_format_frame_size>0) {
				//System.out.println("DEBUG-1: AmrToPcmAudioInputStream: innerRead(): amr_format_frame_size>0 : "+amr_format_frame_size);
				input_stream.read(amr_buffer,0,amr_format_frame_size);
				return amr_decoder.encode(amr_buffer,0,amr_format_frame_size,buffer,0);
			}
			else {
				//System.out.println("DEBUG-1: AmrToPcmAudioInputStream: available="+input_stream.available());
				amr_buffer[0]=(byte)input_stream.read();
				int amr_mode=(amr_buffer[0]>>3)&0xf;
				int amr_frame_size=AMR.frameSize(amr_mode);
				if (amr_frame_size<=0) return 0; // no valid AMR frame
				// else
				//System.out.println("DEBUG-1: AmrToPcmAudioInputStream: innerRead(): amr_frame_size="+amr_frame_size);
				input_stream.read(amr_buffer,1,amr_frame_size-1);
				// AMR NO_DATA
				if (amr_mode==AMR.M15_NO_DATA) {
					// do something?
					//java.util.Arrays.fill(buffer,0,320,(byte)0);
					//return 320;
				}
				// AMR SID
				if (amr_mode==AMR.M8_SID) {
					// do something?
					//java.util.Arrays.fill(buffer,0,320,(byte)0);
					//return 320;
				}
				return amr_decoder.encode(amr_buffer,0,amr_frame_size,buffer,0);
			}
		}
		catch (java.io.IOException e) {
			if (DEBUG) {  printOut("innerRead(): "+e);  e.printStackTrace();  System.exit(0);  }
			return -1;
		}
	}


	/** Gets the number of bytes that are currently available in the inner input stream for reading.
	  * @return the number of bytes that can be still read from the inner input stream */
	protected int innerAvailable() throws java.io.IOException {
		int amr_frame_size=(amr_format_frame_size>0)? amr_format_frame_size : MAX_AMR_FRAME_SIZE; 
		return (input_stream.available()/amr_frame_size)*320;
	}

}
