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



import java.util.Vector;



/** RTCP compound packet (see RFC 3550). 
 */
public class RtcpCompoundPacket {
	


	/* Buffer containing the RTCP compound packet */   
	byte[] buffer;

	/* Offset of the RTCP compound packet within the buffer */   
	int offset;

	/* RTCP compound packet length */   
	int length;



	/** Creates a new RtcpCompoundPacket. */ 
	public RtcpCompoundPacket(byte[] buffer) {
		this.buffer=buffer;
		this.offset=0;
	}

	/** Creates a new RtcpCompoundPacket.
	  * @param buffer packet buffer
	  * @param offset packet offset within the buffer 
	  * @param length packet length */ 
	public RtcpCompoundPacket(byte[] buffer, int offset, int length) {
		this.buffer=buffer;
		this.offset=offset;
		this.length=length;
	}

	/** Creates a new RtcpCompoundPacket. */ 
	public RtcpCompoundPacket(RtcpPacket[] rtcp_packets) {
		length=0;
		for (int i=0; i<rtcp_packets.length; i++) length+=rtcp_packets[i].getPacketLength();
		buffer=new byte[length];
		offset=0;
		int k=0;
		for (int i=0; i<rtcp_packets.length; i++) {
			RtcpPacket rp=rtcp_packets[i];
			byte[] rp_buf=rp.getPacketBuffer();
			int rp_off=rp.getPacketOffset();
			int rp_len=rp.getPacketLength();
			for (int j=0; j<rp_len; j++) buffer[k++]=rp_buf[rp_off+j];
		}
	}



	/** Gets the RTCP compound packet buffer. */
	public byte[] getPacketBuffer() {
		return buffer;
	}

	/** Gets the RTCP compound packet offset. */
	public int getPacketOffset() {
		return offset;
	}

	/** Gets the RTCP compound packet length. */
	public int getPacketLength() {
		return length;
	}

	/** Gets the RTCP packets componing the compound packet. */
	public RtcpPacket[] getRtcpPackets() {
		int index=0;
		Vector aux=new Vector();
		while (index<length) {
			RtcpPacket rp=new RtcpPacket(buffer,offset+index);
			index+=rp.getPacketLength();
			aux.addElement(rp);
		}
		RtcpPacket[] rtcp_packets=new RtcpPacket[aux.size()];
		for (int i=0; i<rtcp_packets.length; i++) rtcp_packets[i]=(RtcpPacket)aux.elementAt(i);
		return rtcp_packets;
	}

}
