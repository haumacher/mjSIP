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



import java.io.InputStream;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;



/** Abstract buffered AudioInputStream.
  * Allows the standard reading from an AudioInputStream that actually requires
  * (or prefers) buffered reading access.
  * <p>
  * A class that extends BufferedAudioInputStream must implements the abstract methods
  * {@link #innerRead(byte[])} and {@link #innerAvailable()}.
  */
abstract public class BufferedAudioInputStream extends AudioInputStream {
	
	/** Debug mode */
	public static boolean DEBUG=false;


	/** Input stream */
	protected InputStream input_stream;
	
	/** Source audio format */
	AudioFormat source_format;

	/** Buffer of unread bytes */
	private byte[] buffer;
	
	/** Index of the first unread byte within buffer */
	//protected int index;
	private int index;

	/** Number of bytes within the buffer */
	private int length;

	/** Whether this input stream is closed */
	private boolean is_closed=false;



	/** Creates a new BufferedAudioInputStream. */
	public BufferedAudioInputStream(AudioInputStream input_stream, AudioFormat source_format, int buffer_size) {
		super(input_stream,input_stream.getFormat(),0);
		this.input_stream=input_stream;
		this.source_format=source_format;
		buffer=new byte[buffer_size];
		index=length=0;
	}



	/** Reads a block of bytes from the inner input stream.
	  * @param buffer the buffer where the the bytes are read to
	  * @return the number of bytes that have been read */
	protected abstract int innerRead(byte[] buffer);


	/** Gets the number of bytes that are currently available in the inner input stream for reading.
	  * @return the number of bytes that can be still read from the inner input stream */
	protected abstract int innerAvailable() throws java.io.IOException;



	/** Gets the number of bytes that are currently available in the input stream for reading. */
	/** @return the number of bytes that can be read (or skipped over) from this input stream without blocking by the next invocation of a method for this input stream. */
	public synchronized int available() throws java.io.IOException {
		if (is_closed) return 0;
		// else
		return (length-index)+innerAvailable();
	}


	/** Reads the next byte of data from the input stream.
	  * @return the next byte, or -1 if no byte is read */
	public synchronized int read()  {
		if (is_closed) return -1;
		// else
		if (index<length) {
			return buffer[index++]&0xFF;
		}
		else {
			int chunk_len=innerRead(buffer);
			while (chunk_len<=0) {
				try {  Thread.sleep(100);  } catch (Exception e) {}
				chunk_len=innerRead(buffer);
			}
			length=chunk_len;
			index=0;
			return buffer[index++]&0xFF;
		}
	}


	/** Reads some number of bytes from the input stream and stores them into the buffer array b.
	  * @param buf the buffer where the the bytes are read to
	  * @return the number of bytes that have been read */
	public int read(byte[] buf) throws java.io.IOException {
		return read(buf,0,buf.length);
	}


	/** Reads up to len bytes of data from the input stream into an array of bytes.
	  * @param buf the buffer where the the bytes are read to
	  * @param off the offset within the buffer
	  * @param len the availble size within the buffer (from the offset)
	  * @return the number of bytes that have been read */
	public synchronized int read(byte[] buf, int off, int len) throws java.io.IOException  {
		if (is_closed) return 0;
		// else
		/*int i=0;
		int val=0;
		while (i<len && (val=read())>=0) {
			buf[off+(i++)]=(byte)val;
		}
		return i;*/
		int i=0;
		while (i<len) {
			if (index>=length) {
				int chunk_len=innerRead(buffer);
				while (chunk_len<=0) {
					try {  Thread.sleep(100);  } catch (Exception e) {}
					chunk_len=innerRead(buffer);
				}
				length=chunk_len;
				index=0;            
			}
			buf[off+(i++)]=buffer[index++];
		}
		return i;
	}


	/** Skips over and discards <i>n</i> bytes of data from this input stream.
	  * @param n the number of bytes to be skipped */
	public long skip(long n)  {
		// TODO
		return 0;
	}


	/** Closes this input stream and releases any system resources associated with the stream. */
	public void close() throws java.io.IOException {
		is_closed=true;
		if (input_stream!=null) input_stream.close();
	}


	/** Tests if this input stream supports the mark and reset methods.
	  * @return true if this input stream supports the mark and reset methods (false if not) */
	public boolean markSupported()  {
		return false;
	}


	/** Marks the current position in this input stream. */
	public void mark(int readlimit)  {
		// TODO
	}


	/** Repositions this stream to the position at the time the mark method was last called on this input stream. */
	public void reset()  {
		// TODO
	}


	/** Prints debugging information. */
	protected void printOut(String str) {
		if (DEBUG) {
			//System.err.println("DEBUG: "+getClass().getName()+": "+str);
			String[] class_name=getClass().getName().split("[.]");
			System.err.println("DEBUG: "+class_name[class_name.length-1]+": "+str);
		}
	}

}
