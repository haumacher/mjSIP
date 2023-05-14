/*
 * Copyright (C) 2014 Luca Veltri - University of Parma - Italy
 * 
 * This file is part of MjSip (http://www.mjsip.org)
 * 
 * MjSip is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * MjSip is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with MjSip; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 * 
 * Author(s):
 * Luca Veltri (luca.veltri@unipr.it)
 */

package org.mjsip.sip.message;

/** Class SipMessageBuffer provides methods for extracting SIP messages from a byte buffer.
  */
public class SipMessageBuffer {
	

	/** Buffer */
	byte[] buffer=null;
	
	/** Current data offset within the buffer */
	int offset=0;
	
	/** Current data length */
	//int length=0;

	
	
	/** Creates a new SipMessageBuffer. */
	public SipMessageBuffer() {
		
	}

	/** Gets the current buffer.
	  * @return the offset */
	public synchronized byte[] getBuffer() {
		return buffer;
	}

	/** Gets the current offset of the data within the buffer.
	  * @return the length */
	public synchronized int getLength() {
		//return length;
		return buffer==null? 0 : buffer.length-offset;
	}

	/** Gets the current length of the data within the buffer.
	  * @return the offset */
	public synchronized int getOffset() {
		return offset;
	}

	/** Gets the value of a byte at a given relative position.
	  * @param i the position of the byte, starting from the current offset
	  * @return the value of the byte */
	public synchronized byte byteAt(int i) {
		return buffer[offset+i];
	}

	/** Skips the first <i>n</i> bytes.
	  * @param n the number of bytes to be skipped
	  * @return this SipMessageBuffer */
	public synchronized SipMessageBuffer skip(int n) {
		offset+=n;
		if (buffer==null || offset>buffer.length) throw new RuntimeException("Exceeded the buffer length: "+offset+">"+(buffer==null? 0 : buffer.length));
		return this;
	}

	/** Appends new bytes to the buffer.
	  * @param data a byte array containing bytes to be added
	  * @return this object */
	public synchronized SipMessageBuffer append(byte[] data) {
		return append(data,0,data.length);
	}

	/** Appends new bytes to the buffer.
	  * @param buf a byte array containing bytes to be added
	  * @param off the offset within the array
	  * @param len number of bytes
	  * @return this object */
	public synchronized SipMessageBuffer append(byte[] buf, int off, int len) {
		byte[] new_buffer=new byte[buffer==null? len : buffer.length-offset+len];
		int index=0;
		// copy old bytes from the previous buffer
		if (buffer!=null) while (offset<buffer.length) new_buffer[index++]=buffer[offset++];
		// copy new bytes
		for (int i=0; i<len; i++) new_buffer[index++]=buf[off+i];
		buffer=new_buffer;
		offset=0;
		return this;
	}

	/** Tries to get a new SIP message from the buffer.
	  * @return a new SIP message or null */
	public synchronized SipMessage parseSipMessage() throws MalformedSipMessageException {
		SipMessage msg=new SipMessage();
		offset+=msg.setMessage(buffer,offset,buffer.length-offset);
		// DEBUG:
		/*try {
			offset+=msg.setMessage(buffer,offset,buffer.length-offset);
			System.out.println("DEBUG: SipMessageBuffer: parseSipMessage(): "+offset+"/"+buffer.length);
		}
		catch (MalformedSipMessageException e) {
			System.out.println("DEBUG: SipMessageBuffer: parseSipMessage(): "+offset+"/"+buffer.length);
			throw e;
		}*/
		return msg;
	}
	
}
