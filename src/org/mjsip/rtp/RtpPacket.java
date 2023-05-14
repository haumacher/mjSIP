/*
 * Copyright (C) 2013 Luca Veltri - University of Parma - Italy
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




/** RTP packet, as defined by RFC 3550. 
 */
public class RtpPacket {
	
	/** Minimum RTP header length */
	static final int HDR_LEN=12;


	/** RTP packet buffer containing the RTP packet (including RTP header and payload) */   
	byte[] buffer;

	/** RTP packet offset within the buffer */   
	int offset;

	/** RTP packet length */   
	int length;

	/** RTP header length */   
	//int hdr_len;



	/** Creates a new RTP packet.
	  * @param buffer buffer containing the RTP packet 
	  * @param length packet length */ 
	public RtpPacket(byte[] buffer, int length) {
		init(buffer,0,length);
	}

	/** Creates a new RTP packet.
	  * @param buffer buffer containing the RTP packet 
	  * @param offset packet offset within the buffer 
	  * @param length packet length */ 
	public RtpPacket(byte[] buffer, int offset, int length) {
		init(buffer,offset,length);
	}

	/** Inits the RTP packet.
	  * @param buffer buffer containing the RTP packet 
	  * @param offset RTP packet offset within the buffer 
	  * @param length RTP packet length */ 
	private void init(byte[] buffer, int offset, int length) {
		this.buffer=buffer;
		this.offset=offset;
		//this.length=(length<HDR_LEN)? HDR_LEN : length;
		this.length=length;
	}

	/** Creates a new RTP packet. 
	  * @param pl_type payload type
	  * @param pl_buf payload buffer
	  * @param pl_len payload length */ 
	/*public RtpPacket(int pl_type, byte[] pl_data) {
		initBuffer(0,pl_data.length);
		setHeader(pl_type);
		setPayload(pl_data,0,pl_data.length);
	}*/

	/** Creates a new RTP packet. 
	  * @param pl_type payload type
	  * @param pl_buf payload buffer
	  * @param pl_off payload offset
	  * @param pl_len payload length */ 
	/*public RtpPacket(int pl_type, byte[] pl_buf, int pl_off, int pl_len) {
		initBuffer(0,pl_len);
		setHeader(pl_type);
		setPayload(pl_buf,pl_off,pl_len);
	}*/

	/** Creates a new RTP packet.
	  * @param pl_type payload type
	  * @param ssrc synchronization source (SSRC) identifier
	  * @param seq_num sequence number
	  * @param timestamp timestamp
	  * @param pl_buf payload buffer
	  * @param pl_off payload offset
	  * @param pl_len payload length */ 
	public RtpPacket(int pl_type, long ssrc, int seq_num, long timestamp, byte[] pl_buf, int pl_off, int pl_len) {
		initBuffer(0,pl_len);
		setHeader(pl_type,ssrc,seq_num,timestamp);
		setPayload(pl_buf,pl_off,pl_len);
	}

	/** Creates a new RTP packet. 
	  * @param pl_type payload type
	  * @param ssrc synchronization source (SSRC) identifier
	  * @param seq_num sequence number
	  * @param timestamp timestamp
	  * @param csrc array of contributing source (CSRC) identifiers
	  * @param pl_buf payload buffer
	  * @param pl_off payload offset
	  * @param pl_len payload length */ 
	public RtpPacket(int pl_type, long ssrc, int seq_num, long timestamp, long[] csrc, byte[] pl_buf, int pl_off, int pl_len) {
		int cc=(csrc==null)? 0 : csrc.length;
		initBuffer(cc,pl_len);
		setHeader(pl_type,ssrc,seq_num,timestamp,csrc);
		setPayload(pl_buf,pl_off,pl_len);
	}

	/** Creates a new RTP packet.
	  * @param rtp_ctx the current RTP context
	  * @param pl_buf payload buffer
	  * @param pl_off payload offset
	  * @param pl_len payload length */ 
	public RtpPacket(RtpContext rtp_ctx, byte[] pl_buf, int pl_off, int pl_len) {
		initBuffer(rtp_ctx.getCC(),pl_len);
		setHeader(rtp_ctx.getPayloadType(),rtp_ctx.getSsrc(),rtp_ctx.getSequenceNumber(),rtp_ctx.getTimestamp(),rtp_ctx.getCsrc());
		setPayload(pl_buf,pl_off,pl_len);
	}

	/** Inits the RTP packet buffer. 
	  * @param cc number of contributing source (CSRC) identifiers
	  * @param pl_len payload length */ 
	private void initBuffer(int cc, int pl_len) {
		length=HDR_LEN+cc*4+pl_len;
		offset=0;
		buffer=new byte[length];
	}


	/** Gets the RTP packet buffer.
	  * @return the buffer containing the RTP packet (including RTP header and payload) */   
	public byte[] getPacketBuffer() {
		return buffer;
	}

	/** Gets the RTP packet offset within the buffer.
	  * @return the RTP packet offset within the buffer */   
	public int getPacketOffset() {
		return offset;
	}

	/** Gets the RTP packet length.
	  * @return the total packet length (including RTP header and payload) */   
	public int getPacketLength() {
		return length;
	}


	/** Sets the RTP packet length.
	  * @param length the new total packet length (including RTP header and payload) */   
	public void setPacketLength(int length) {
		this.length=length;
		if (length>(buffer.length-offset)) {
			byte[] aux=new byte[length];
			System.arraycopy(buffer,offset,aux,0,buffer.length-offset);
			buffer=aux;
			offset=0;
		}
	}



	// version (V): 2 bits
	// padding (P): 1 bit
	// extension (X): 1 bit
	// CSRC count (CC): 4 bits
	// marker (M): 1 bit
	// payload type (PT): 7 bits
	// sequence number: 16 bits
	// timestamp: 32 bits
	// SSRC: 32 bits
	// CSRC list: 0 to 15 items, 32 bits each


	/** Gets the version (V).
	  * @return the RTP version */
	public int getVersion() {
		if (length<HDR_LEN) return 0; // broken packet
		// else
		return (buffer[offset]>>6 & 0x03);
	}

	/** Sets the version (V).
	  * @param v the RTP version */
	public void setVersion(int v) {
		if (length<HDR_LEN) return; // broken packet
		// else
		buffer[offset]=(byte)((buffer[offset] & 0x3f) | ((v & 0x03)<<6));
	}

	/** Whether has padding (P). */
	/*public boolean hasPadding() {
		if (length<HDR_LEN) return false; // broken packet
		// else
		return getBit(buffer[offset],5);
	}*/

	/** Sets padding (P). */
	/*public void setPadding(boolean p) {
		if (length<HDR_LEN) return 0; // broken packet
		// else
		setBit(p,buffer[offset],5);
	}*/

	/** Gets padding length
	  * @return the number of padding bytes */
	public int getPaddingLength() {
		if (length<HDR_LEN) return 0; // broken packet
		// else
		boolean p=getBit(buffer[offset],5);
		if (p) return buffer[offset+length-1];
		else return 0;
	}

	/** Sets padding length.
	  * @param padding_len the number of padding bytes */
	public void setPaddingLength(int padding_len) {
		if (length<HDR_LEN) return; // broken packet
		// else
		if (padding_len>0)  {
			int pl_len=getPayloadLength();
			int hdr_len=getHeaderLength();
			for (int i=0; i<padding_len-1; i++) buffer[offset+hdr_len+pl_len+i]=0;
			buffer[offset+hdr_len+pl_len+padding_len-1]=(byte)padding_len;
			setBit(true,buffer[offset],5);
		}
		else setBit(false,buffer[offset],5);
	}

	/** Whether it has extension (X).
	  * @return true if it has extension, false otherwise */
	public boolean hasExtension() {
		if (length<HDR_LEN) return false; // broken packet
		// else
		return getBit(buffer[offset],4);
	}

	/** Sets extension (X).
	  * @param x whether it has extension (true if it has extension, false otherwise) */
	public void setExtension(boolean x) {
		if (length<HDR_LEN) return; // broken packet
		// else
		setBit(x,buffer[offset],4);
	}

	/** Gets the CSRC count (CC).
	  * @return the CSRC count */
	protected int getCsrcCount() {
		if (length<HDR_LEN) return 0; // broken packet
		// else
		return (buffer[offset] & 0x0F);
	}

	/** Sets the CSRC count (CC).
	  * @param cc the CSRC count */
	protected void setCsrcCount(int cc) {
		if (length<HDR_LEN) return; // broken packet
		// else
		buffer[offset]=(byte)(((buffer[offset]>>4)<<4)+cc);
	}

	/** Whether has marker (M)
	  * @return marker field (true=1, false=0) */
	public boolean hasMarker() {
		if (length<HDR_LEN) return false; // broken packet
		// else
		return getBit(buffer[offset+1],7);
	}

	/** Sets marker (M).
	  * @param m the marker field (true=1, false=0) */
	public void setMarker(boolean m) {
		if (length<HDR_LEN) return; // broken packet
		// else
		setBit(m,buffer[offset+1],7);
	}

	/** Gets the payload type (PT).
	  * @return the 7-bit payload type value */
	public int getPayloadType() {
		if (length<HDR_LEN) return -1; // broken packet
		// else
		return (buffer[offset+1] & 0x7F);
	}

	/** Sets the payload type (PT).
	  * @param pt the 7-bit payload type value */
	public void setPayloadType(int pt) {
		if (length<HDR_LEN) return; // broken packet
		// else
		buffer[offset+1]=(byte)((buffer[offset+1] & 0x80) | (pt & 0x7F));
	}

	/** Gets the sequence number.
	  * @return the 16-bit sequence number */
	public int getSequenceNumber() {
		if (length<HDR_LEN) return 0; // broken packet
		// else
		return getInt(buffer,offset+2,offset+4);
	}

	/** Sets the sequence number.
	  * @param sn the 16-bit sequence number */
	public void setSequenceNumber(int sn) {
		if (length<HDR_LEN) return; // broken packet
		// else
		setInt(sn,buffer,offset+2,offset+4);
	}

	/** Gets the timestamp.
	  * @return the 32-bit timestamp value */
	public long getTimestamp() {
		if (length<HDR_LEN) return 0; // broken packet
		// else
		return getLong(buffer,offset+4,offset+8);
	}

	/** Sets the timestamp.
	  * @param timestamp the 32-bit timestamp value */
	public void setTimestamp(long timestamp) {
		if (length<HDR_LEN) return; // broken packet
		// else
		setLong(timestamp,buffer,offset+4,offset+8);
	}
	
	/** Gets the SSRC.
	  * @return the 32-bit SSRC value */
	public long getSsrc() {
		if (length<HDR_LEN) return 0; // broken packet
		// else
		return getLong(buffer,offset+8,offset+12);
	}

	/** Sets the SSRC.
	  * @param ssrc the 32-bit SSRC value */
	public void setSsrc(long ssrc) {
		if (length<HDR_LEN) return; // broken packet
		// else
		setLong(ssrc,buffer,offset+8,offset+12);
	}

	/** Gets the CSRC list.
	  * @return an array of CSRCs */
	public long[] getCsrcList() {
		if (length<HDR_LEN) return null; // broken packet
		// else
		int cc=getCsrcCount();
		long[] csrc=new long[cc];
		for (int i=0; i<cc; i++) csrc[i]=getLong(buffer,offset+HDR_LEN+4*i,offset+HDR_LEN+4*i+2); 
		return csrc;
	}

	/** Sets the CSRC list.
	  * @param csrc array of CSRCs */
	public void setCsrcList(long[] csrc) {
		if (length<HDR_LEN) return; // broken packet
		// else
		int cc=(csrc!=null)? csrc.length : 0;
		if (cc>15) cc=15;
		if (length>=(HDR_LEN+4*cc)) {
			setCsrcCount(cc);
			for (int i=0; i<cc; i++) setLong(csrc[i],buffer,offset+HDR_LEN+4*i,offset+HDR_LEN+4*i+2);   
			//hdr_len=HDR_LEN+4*cc;
		}
	}

	/** Sets the RTP packet header (only PT).
	  * @param pl_type payload type */ 
	/*public void setHeader(int pl_type) {
		setHeader(pl_type,Random.nextLong(),Random.nextInt(),Random.nextLong(),(long[])null);
	}*/

	/** Sets the RTP packet header (PT, SSRC, SQN, TimeStamp). 
	  * @param pl_type payload type
	  * @param ssrc synchronization source (SSRC) identifier 
	  * @param seq_num sequence number
	  * @param timestamp timestamp */
	public void setHeader(int pl_type, long ssrc, int seq_num, long timestamp) {
		setHeader(pl_type,ssrc,seq_num,timestamp,(long[])null);
	}

	/** Sets the RTP packet header (PT, SSRC, SQN, TimeStamp, CSRC).
	  * @param pl_type payload type
	  * @param ssrc synchronization source (SSRC) identifier
	  * @param seq_num sequence number
	  * @param timestamp timestamp
	  * @param csrc array of contributing source (CSRC) identifiers */
	public void setHeader(int pl_type, long ssrc, int seq_num, long timestamp, long[] csrc) {
		setVersion(2);
		setPaddingLength(0);
		setPayloadType(pl_type);
		setSsrc(ssrc);
		setSequenceNumber(seq_num);
		setTimestamp(timestamp);
		if (csrc!=null && csrc.length>0) setCsrcList(csrc);
	}

	/** Sets the RTP packet header (PT, SSRC, SQN, TimeStamp).
	  * @param rtp_ctx the current RTP context */ 
	public void setHeader(RtpContext rtp_ctx) {
		setHeader(rtp_ctx.getPayloadType(),rtp_ctx.getSsrc(),rtp_ctx.getSequenceNumber(),rtp_ctx.getTimestamp(),rtp_ctx.getCsrc());
	}

	/** Gets the RTP header length.
	  * @return the RTP header length */
	public int getHeaderLength() {
		if (length>=HDR_LEN) return HDR_LEN+4*getCsrcCount();
		else return length; // broken packet
	}

	/** Sets the RTP payload.
	  * @param data payload data */ 
	public void setPayload(byte[] data) {
		setPayload(data,0,data.length);
	}

	/** Sets the RTP payload.
	  * @param pl_buf payload buffer
	  * @param pl_off payload offset
	  * @param pl_len payload length */ 
	public void setPayload(byte[] pl_buf, int pl_off, int pl_len) {
		int hdr_len=getHeaderLength();
		length=hdr_len+pl_len;
		if (buffer.length<(offset+length)) {
			offset=0;
			buffer=new byte[length];
		}
		for (int i=0; i<pl_len; i++) buffer[offset+hdr_len+i]=pl_buf[pl_off+i];
	}

	/** Gets the payload.
	  * @return a new byte array containing the the payload data */ 
	public byte[] getPayload() {
		int hdr_len=getHeaderLength();
		int pl_len=length-hdr_len;
		byte[] pl_buf=new byte[pl_len];
		for (int i=0; i<pl_len; i++) pl_buf[i]=buffer[offset+hdr_len+i]; 
		return pl_buf;
	}

	/** Gets the RTP payload length.
	  * @return the RTP payload length */
	public int getPayloadLength() {
		int pl_len=length-getHeaderLength()-getPaddingLength();
		return (pl_len>0)? pl_len : 0; // returns 0 in case of broken packet
	}

	/** Sets the RTP payload length.
	  * @param pl_len the RTP payload length */
	public void setPayloadLength(int pl_len) {
		setPacketLength(getHeaderLength()+pl_len);
		setPaddingLength(0);
	}



	// *********************** Private and Static ***********************

	/** Gets int value. */
	static int getInt(byte b) {
		return ((int)b+256)%256;
	}

	/** Gets long value. */
	static long getLong(byte[] data, int begin, int end) {
		long n=0;
		for (; begin<end; begin++) {
			n<<=8;
			n+=getInt(data[begin]);
		}
		return n;
	}

	/** Sets long value. */
	static void setLong(long n, byte[] data, int begin, int end) {
		for (end-- ; end>=begin; end--) {
			data[end]=(byte)(n%256);
			n>>=8;
		}
	}

	/** Gets Int value. */
	static int getInt(byte[] data, int begin, int end) {
		return (int)getLong(data,begin,end);
	}

	/** Sets Int value. */
	static void setInt(int n, byte[] data, int begin, int end) {
		setLong(n,data,begin,end);
	}

	/** Gets bit value. */
	static boolean getBit(byte b, int bit) {
		return (b>>bit)==1;
	}

	/** Sets bit value. */
	static void setBit(boolean value, byte b, int bit) {
		if (value) b=(byte)(b|(1<<bit));
		else b=(byte)((b|(1<<bit))^(1<<bit));
	}
}
