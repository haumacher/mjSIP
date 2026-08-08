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

	/** Default value for the maximum size of a single SIP message (in bytes). */
	public static final int DEFAULT_MAX_MESSAGE_SIZE=1024*1024;

	/** Buffer */
	byte[] buffer=null;

	/** Current data offset within the buffer */
	int offset=0;

	/** Current data length */
	//int length=0;

	/** Maximum size of a single SIP message (in bytes) */
	private final int max_message_size;



	/** Creates a new SipMessageBuffer accepting messages of {@link #DEFAULT_MAX_MESSAGE_SIZE} bytes. */
	public SipMessageBuffer() {
		this(DEFAULT_MAX_MESSAGE_SIZE);
	}

	/** Creates a new SipMessageBuffer.
	  * @param max_message_size the maximum size of a single SIP message (in bytes); data exceeding
	  *        that size without forming a complete message is rejected, since it can neither be
	  *        parsed nor be buffered indefinitely */
	public SipMessageBuffer(int max_message_size) {
		this.max_message_size=max_message_size;
	}

	/** Gets the maximum size of a single SIP message (in bytes).
	  * @return the maximum message size */
	public int getMaxMessageSize() {
		return max_message_size;
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
	  * <p>
	  * The buffer is a stream of messages, therefore only a message with a Content-Length header field
	  * can be framed, see {@link BasicSipMessage#setMessage(byte[],int,int,boolean)}. As long as the buffer
	  * does not contain a complete message, an {@link IncompleteSipMessageException} is thrown and the
	  * buffer contents are kept for a later retry. Any other {@link MalformedSipMessageException} means
	  * that the start of the next message within the stream cannot be determined any more.
	  * </p>
	  * @return a new SIP message or null
	  * @exception IncompleteSipMessageException if the buffer does not (yet) contain a complete message
	  * @exception MalformedSipMessageException if the buffer does not start with a valid SIP message, or
	  *            if the buffered data exceeds {@link #getMaxMessageSize()} without forming a message */
	public synchronized SipMessage parseSipMessage() throws MalformedSipMessageException {
		int length=getLength();
		if (length<=0) throw new IncompleteSipMessageException("No data buffered.");
		SipMessage msg=new SipMessage();
		try {
			offset+=msg.setMessage(buffer,offset,length,true);
		}
		catch (IncompleteSipMessageException ex) {
			// Note: An incomplete message must be kept until the rest of it arrives. Without a limit,
			// a peer could exhaust the memory by announcing a huge Content-Length or by never
			// terminating the message header.
			if (length>max_message_size)
				throw new MalformedSipMessageException("Message too large: More than "+max_message_size+" bytes received without a complete message.");
			throw ex;
		}
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
