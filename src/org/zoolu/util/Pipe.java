/*
 * Copyright (C) 2006 Luca Veltri - University of Parma - Italy
 * 
 * This source code is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This source code is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this source code; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 * 
 * Author(s):
 * Luca Veltri (luca.veltri@unipr.it)
 */

package org.zoolu.util;



//import java.io.InputStream;
//import java.io.OutputStream;



/** Pipe.
  * <p>
  * A Pipe is a FIFO line, where bytes can be written in and read out.
  * The size of the Pipe is the maximum number of bytes that can be written without read,
  * that is the maximum number of bytes that can be starved within the Pipe.
  * <p>
  * If a byte is written into a full Pipe, an ArrayIndexOutOfBoundsException is thrown.
  * <br>
  * If a byte is read from an empty Pipe, an ArrayIndexOutOfBoundsException is thrown.
  */
public class Pipe {
	
	/** Buffer */
	protected byte[] pipe_buffer;

	/** Index of the first byte */
	protected int pipe_top;

	/** Number of bytes */
	protected int pipe_size;

	/** Pipe's InputStream */
	//protected InputStream is=null;

	/** Pipe's InputStream */
	//protected OutputStream os=null;



	/** Creates a new Pipe. */
	public Pipe(int size) {
		pipe_buffer=new byte[size];
		pipe_top=0;
		pipe_size=0;
	}


	/** Creates a new Pipe. */
	/*public Pipe(byte[] pipe_buffer) {
		this.pipe_buffer=pipe_buffer;
		pipe_top=0;
		pipe_size=0;
	}*/


	/** Gets the Pipe's InputStream. */
	/*public InputStream getInputStream() {
		if (is==null) is=new PipeInputStream(this);
		return is;
	}*/


	/** Gets the Pipe's OutputStream. */
	/*public OutputStream getOutputStream() {
		if (os==null) os=new PipeOutputStream(this);
		return os;
	}*/


	/** Returns the size of this pipe. */
	public int size() {
		return pipe_buffer.length;
	}


	/** Returns the number of bytes that can be read from this pipe. */
	public int available() {
		return pipe_size;
	}


	/** Returns the number of bytes that can be write to this pipe. */
	public synchronized int freespace() {
		return pipe_buffer.length-pipe_size;
	}


	/** Writes the specified byte to this pipe. */
	public synchronized void write(byte b) {
		if (pipe_size==pipe_buffer.length) {
			throw new ArrayIndexOutOfBoundsException("Pipe full ("+available()+")");
		}
		// else
		pipe_buffer[(pipe_top+(pipe_size++))%pipe_buffer.length]=b;
	}


	/** Writes the specified array of bytes to this pipe. */
	public void write(byte[] data) {
		write(data,0,data.length);
	}


	/** Writes <i>len</i> bytes into this pipe
	  * from the <i>buf</i> array starting from position <i>off</i>. */
	public synchronized void write(byte[] buf, int off, int len) {
		if (len<0) {
			throw new ArrayIndexOutOfBoundsException("Pipe: trying to write "+len+" byte");
		}
		// else
		if (len>(pipe_buffer.length-pipe_size)) {
			throw new ArrayIndexOutOfBoundsException("Pipe full: "+len+">"+(pipe_buffer.length-pipe_size));
		}
		// else
		int pos=(pipe_top+pipe_size)%pipe_buffer.length;
		for (int i=0; i<len; i++) {
			pipe_buffer[pos++]=buf[off++];
			if (pos==pipe_buffer.length) pos=0;
		}
		pipe_size+=len;
	}


	/** Reads the next byte of data from the pipe. */
	public synchronized byte read() {
		if (pipe_size==0) {
			throw new ArrayIndexOutOfBoundsException("Pipe empty ("+available()+")");
		}
		// else
		byte b=pipe_buffer[pipe_top];
		if ((++pipe_top)==pipe_buffer.length) pipe_top=0;
		pipe_size--;
		return b;
	}


	/** Reads an array of bytes from the pipe. */
	public int read(byte[] data) {
		return read(data,0,data.length);
	}


	/** Reads <i>len</i> bytes from the pipe and puts them
	  * into the <i>buf</i> array starting from position <i>off</i>. */
	public synchronized int read(byte[] buf, int off, int len) {
		if (len<0) {
			throw new ArrayIndexOutOfBoundsException("Pipe: trying to read "+len+" byte");
		}
		// else
		if (len>pipe_size) {
			throw new ArrayIndexOutOfBoundsException("Pipe empty: "+pipe_size+"<"+len);
		}
		// else
		for (int i=0; i<len; i++) {
			buf[off++]=pipe_buffer[pipe_top++];
			if (pipe_top==pipe_buffer.length) pipe_top=0;
		}
		pipe_size-=len;
		return len;
	}


	/** Skips over and discards n bytes of data from this input stream. */
	public synchronized long skip(int len) {
		if (len<0) {
			throw new ArrayIndexOutOfBoundsException("Pipe: trying to skip "+len+" byte");
		}
		// else
		if (len>pipe_size) {
			throw new ArrayIndexOutOfBoundsException("Pipe empty: "+pipe_size+"<"+len);
		}
		// else
		pipe_size-=len;
		pipe_top=(pipe_top+len)%pipe_buffer.length;
		return len;
	}


	/** Closes the Pipe.
	  * It actually closes the Pipe's input and output streams. */
	/*public void close() {
		try {  if (is!=null) is.close(); } catch (Exception e) {}
		try {  if (os!=null) os.close(); } catch (Exception e) {}
	}*/

}
