/*
 * Copyright (C) 2013 Luca Veltri - University of Parma - Italy
 * 
 * THE PUBLICATION, REDISTRIBUTION OR MODIFY, COMPLETE OR PARTIAL OF CONTENTS, 
 * CAN BE MADE ONLY AFTER AUTHORIZATION BY THE AFOREMENTIONED COPYRIGHT HOLDER.
 *
 * Author(s):
 * Luca Veltri (luca.veltri@unipr.it)
 */

package org.zoolu.sound;



import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.spi.FormatConversionProvider;

import org.zoolu.util.Pipe;
import org.zoolu.util.PipeInputStream;
import org.zoolu.util.PipeOutputStream;



/** ConvertedAudioOutputStream extends class AudioOutputStream in order to obtaing
  * a transcoded (converted) AudioOutputStream from a base AudioOutputStream by using
  * a specific FormatConversionProvider.
  */
class ConvertedAudioOutputStream extends AudioOutputStream {
	
	/** Whether printing debugging information on standard error output. */
	public static boolean DEBUG=false;
	
	/** Internal buffer size. */
	static final int INTERNAL_BUFFER_SIZE=40960;

	/** Internal buffer. */
	byte[] buff=new byte[INTERNAL_BUFFER_SIZE];

	/** Converted InputStream read and written to the final OutputStream. */
	InputStream converted_input_stream;

	/** Ingress OutputStream piped within a intermediate InputStream passed to the FormatConversionProvider. */
	OutputStream ingress_output_stream;

	/** Final AudioOutputStream. */
	AudioOutputStream final_output_stream;


	/** Creates a new ConvertedAudioOutputStream of a specified AudioFormat <i>format</i>
	  * from a specified AudioOutputStream <i>os</i> using a specified FormatConversionProvider <i>converter</i>. */
	public ConvertedAudioOutputStream(AudioFormat format, AudioOutputStream os, FormatConversionProvider converter) throws IOException {
		super(format); 
		final_output_stream=os;
		// External ConvertedAudioOutputStream behaviour:
		//    write()-->[ConvertedAudioOutputStream-->input_stream-->converter-->converted_input_stream-->final_output_stream]

		// Internal ConvertedAudioOutputStream scheme:
		//    first_output_stream-->pipe-->piped_input_stream-->converter-->converted_input_stream-->final_output_stream
		
		// create an internal pipe for converting an OutputStream into an InputStream
		// note: my implementation of PipeInputStream (and PipeOutputStream) seems to be quite faster than the equivalent PipedInputStream (and PipedOutputStream)
		//PipedInputStream piped_input_stream=new PipedInputStream();
		//ingress_output_stream=new PipedOutputStream(piped_input_stream);
		Pipe pipe=new Pipe(INTERNAL_BUFFER_SIZE);
		InputStream piped_input_stream=new PipeInputStream(pipe);
		ingress_output_stream=new PipeOutputStream(pipe);
		
		AudioInputStream audio_input_stream=new AudioInputStream(piped_input_stream,format,-1);
		if (audio_input_stream==null) {
			throw new IOException("Failed while creating a new AudioInputStream.");
		}
		converted_input_stream=converter.getAudioInputStream(final_output_stream.getFormat(),audio_input_stream);

		printOut("input codec: "+format);
		printOut("output codec: "+final_output_stream.getFormat());
		if (converted_input_stream==null) {
			throw new IOException("Failed while getting a transcoded AudioOuputStream of format:"+format+", for an AudioOuputStream with format: "+final_output_stream.getFormat()+".");
		}
	}


	/** Closes this output stream and releases any system resources associated with this stream. */
	public void close() {
		try  {
			final_output_stream.close();
			converted_input_stream.close();
			ingress_output_stream.close();
		}
		catch (IOException e) {}
	}

  
	/** Flushes this output stream and forces any buffered output bytes to be written out. */
	public void flush() {
		try  {
			final_output_stream.flush();
		}
		catch (IOException e) {}
	}

	
	/** Writes b.length bytes from the specified byte array to this output stream. */
	public void write(byte[] b) throws IOException {
		write(b,0,b.length);
	}

	
	/** Writes len bytes from the specified byte array starting at offset off to this output stream. */
	public void write(byte[] b, int off, int len) throws IOException {
		if (ingress_output_stream!=null) {
			ingress_output_stream.write(b,off,len);
			int available=converted_input_stream.available();
			// @@@@@@@@@@@ PATCH for codec implementations (like the tritonus's GSM) that do not correctly support method available()
			if (available==0) available=buff.length; // otherwise you could try to put =320; that is 20ms
			// @@@@@@@@@@@ end of PATCH
			if (available>buff.length) available=buff.length;
			len=converted_input_stream.read(buff,0,available);
			final_output_stream.write(buff,0,len);
		}
	}

  
	/** Writes the specified byte to this output stream. */
	public void write(int b) throws IOException {
		if (ingress_output_stream!=null) {
			ingress_output_stream.write(b);
			int len=converted_input_stream.read(buff,0,buff.length);
			final_output_stream.write(buff,0,len);
		}
	}


	/** Prints debugging information. */
	private static void printOut(String str) {
		if (DEBUG) System.err.println("DEBUG: ConvertedAudioOutputStream: "+str);
	}

}
