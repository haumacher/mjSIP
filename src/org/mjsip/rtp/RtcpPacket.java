/*
 * Copyright (C) 2012 Luca Veltri - University of Parma - Italy
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

package org.mjsip.rtp;




/** RTCP packet, as defined by RFC 3550. 
 */
public class RtcpPacket {
	
	/** SR (Sendr Report) RTCP packet type */
	public static final int PT_SR=200;

	/** RR (Receiver Report) RTCP packet type */
	public static final int PT_RR=201;

	/** SDES (Source Description) RTCP packet type */
	public static final int PT_SDES=202;

	/** BYE (Goodbye) RTCP packet type */
	public static final int PT_BYE=203;

	/** APP (Application-Defined) RTCP packet type */
	public static final int PT_APP=204;



	/* RTCP packet buffer containing the RTCP packet */   
	byte[] buffer;

	/* RTCP packet offset within the packet buffer */   
	int offset;



	/** Creates a new RTCP packet. 
	  * @param buffer buffer containing the RTCP packet */ 
	public RtcpPacket(byte[] buffer) {
		this.buffer=buffer;
		this.offset=0;
	}


	/** Creates a new RTCP packet.
	  * @param buffer buffer containing the RTCP packet
	  * @param offset packet offset within the buffer */ 
	public RtcpPacket(byte[] buffer, int offset) {
		this.buffer=buffer;
		this.offset=offset;
	}



	/** Gets the RTCP packet buffer.
	  * @return the buffer containing the RTCP packet */   
	public byte[] getPacketBuffer() {
		return buffer;
	}

	/** Gets the RTCP packet offset within the buffer.
	  * @return the RTP packet offset within the buffer */   
	public int getPacketOffset() {
		return offset;
	}



	// version (V): 2 bits
	// padding (P): 1 bit
	// packet type (PT): 8 bits; 200=SR, 201=RR, 202=SDES.
	// length: 16 bits; The length of this RTCP packet in 32-bit words minus one, including the header and any padding.  (The offset of one makes zero a valid length and avoids a possible infinite loop in scanning a compound RTCP packet, while counting 32-bit words avoids a validity check for a multiple of 4.)

	/** Gets the version (V).
	  * @return the RTP version */
	public int getVersion() {
		return (buffer[offset]>>6 & 0x03);
	}

	/** Sets the version (V).
	  * @param v the RTP version */
	public void setVersion(int v) {
		buffer[offset]=(byte)((buffer[offset] & 0x3F) | ((v & 0x03)<<6));
	}

	/** Gets padding length
	  * @return the number of padding bytes */
	public int getPaddingLength() {
		boolean p=RtpPacket.getBit(buffer[offset],5);
		if (p) return buffer[offset+getPacketLength()-1];
		else return 0;
	}

	/** Sets padding length.
	  * @param padding_len the number of padding bytes */
	public void setPaddingLength(int padding_len) {
		if (padding_len>0)  {
			for (int i=1; i<padding_len; i++) buffer[offset+getPacketLength()-1-i]=0;
			buffer[offset+getPacketLength()-1]=(byte)padding_len;
			RtpPacket.setBit(true,buffer[offset],5);
		}
		else RtpPacket.setBit(false,buffer[offset],5);
	}



	/** Gets the payload type (PT).
	  * @return the payload type value */
	public int getPayloadType() {
		return (buffer[offset+1] & 0xFF);
	}

	/** Sets the payload type (PT).
	  * @param pt the payload type */
	public void setPayloadType(int pt) {
		buffer[offset+1]=(byte)((buffer[offset+1] & 0x80) | (pt & 0xFF));
	}

	/** Gets the RTCP packet length.
	  * @return the RTCP packet length including the header and any padding */   
	public int getPacketLength() {
		return (RtpPacket.getInt(buffer,2,4)+1)*4;
	}

	/** Sets the RTCP packet length.
	  * @param len the RTCP packet length including the header and any padding */   
	public void setPacketLength(int len) {
		RtpPacket.setInt(len/4-1,buffer,2,4);
	}

}
