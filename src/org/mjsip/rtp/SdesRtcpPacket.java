/*
 * Copyright (C) 2014 Luca Veltri - University of Parma - Italy
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



import java.util.ArrayList;



/** Source Description (SDES) RTCP packet, as defined in RFC 3550. 
  */
public class SdesRtcpPacket extends RtcpPacket {
	
	/** CNAME: Canonical End-Point Identifier SDES Item */
	public static final int ITEM_CNAME=1;
			
	/** NAME: User Name SDES Item */
	public static final int ITEM_NAME=2;

	/** EMAIL: Electronic Mail Address SDES Item */
	public static final int ITEM_EMAIL=3;

	/** PHONE: Phone Number SDES Item */
	public static final int ITEM_PHONE=4;

	/** LOC: Geographic User Location SDES Item */
	public static final int ITEM_LOC=5;

	/** TOOL: Application or Tool Name SDES Item */
	public static final int ITEM_TOOL=6;

	/** NOTE: Notice/Status SDES Item */
	public static final int ITEM_NOTE=7;

	/** PRIV: Private Extensions SDES Item */
	public static final int ITEM_PRIV=8;


	/** Header length */
	private static final int SDES_HDR_LEN=4;



	/** Creates a new SDES RTCP packet.
	  * @param rp the RTCP SDES packet */
	public SdesRtcpPacket(RtcpPacket rp) {
		super(rp.buffer,rp.offset);
	}

	/** Creates a new SDES RTCP packet.
	  * @param buffer buffer containing the RTCP SDES packet */
	public SdesRtcpPacket(byte[] buffer) {
		super(buffer);
	}

	/** Creates a new SDES RTCP packet. 
	  * @param buffer buffer containing the RTCP SDES packet
	  * @param offset packet offset within the buffer */ 
	public SdesRtcpPacket(byte[] buffer, int offset) {
		super(buffer,offset);
	}

	/** Creates a new SDES RTCP packet. 
	  * @param chunks array of SDES chunks */
	public SdesRtcpPacket(Chunk[] chunks) {
		super((byte[])null);
		init(chunks);
	}

	/** Creates a new SDES RTCP packet.
	  * @param ssrc the SSRC identifier
	  * @param cname canonical end-point identifier (CNAME) */
	public SdesRtcpPacket(long ssrc, String cname) {
		super((byte[])null);
		init(new Chunk[]{ new Chunk(ssrc,new SdesItem[]{ new SdesItem(ITEM_CNAME,cname) }) });
	}

	/** Inits the SDES RTCP packet. 
	  * @param chunks array of SDES chunks */
	private void init(Chunk[] chunks) {
		int len=SDES_HDR_LEN;
		for (int i=0; i<chunks.length; i++) len+=chunks[i].getLength();
		buffer=new byte[len];
		offset=0;
		setVersion(2);
		setSourceCount(chunks.length);
		setPayloadType(PT_SDES);
		setPacketLength(len);
		int index=SDES_HDR_LEN;
		for (int i=0; i<chunks.length; i++) {
			index+=chunks[i].getBytes(buffer,index);
		}
	}


	/** Gets the source count (SC). */
	protected int getSourceCount() {
		return (buffer[offset] & 0x1F);
	}


	/** Sets the source count (SC).
	  * SC is the number of of SSRC/CSRC chunks contained in this SDES packet. A value of zero is valid but useless.
	  * @param rc the source count (SC) */
	protected void setSourceCount(int rc) {
		buffer[offset]=(byte)(((buffer[offset]>>5)<<5)+rc);
	}


	/** Gets chunks.
	  * @return an array of zero or more SDES chunks */
	public Chunk[] getChunks() {
		Chunk[] chunks=new Chunk[getSourceCount()];
		int begin=offset+SDES_HDR_LEN;
		for (int i=0; i<chunks.length; i++) {
			chunks[i]=new Chunk(buffer,begin);
		}
		return chunks;
	}


	/** Gets a string representation of this object.
	  * @return a string representing this object. */
	public String toString() {
		StringBuffer sb=new StringBuffer();
		Chunk[] chunks=getChunks();
		for (int i=0; i<chunks.length; i++) sb.append(chunks[i].toString());
		return sb.toString();
	}




	/** SDES item.
	  */
	public static class SdesItem {
		
		/** Item type */
		int type;
				 
		/** Buffer containing the item value */
		byte[] buf;
		
		/** Offset within the buffer */
		int off;
		
		/** Length of the item value */
		int len;


		/** Creates a new SdesItem.
		 * @param buf buffer containing the SDES item
		 * @param off offset within the buffer */
		public SdesItem(byte[] buf, int off) {
			this.type=buf[off]&0xFF;
			this.buf=buf;
			this.len=buf[off+1]&0xFF;
			this.off=off+2;
		}


		/** Creates a new SdesItem.
		 * @param type item type
		 * @param buf buffer containing the item value
		 * @param off offset within the buffer
		 * @param len length of the item value */
		public SdesItem(int type, byte[] buf, int off, int len) {
			this.type=type;
			this.buf=buf;
			this.off=off;
			this.len=len;
		}


		/** Creates a new SdesItem.
		 * @param type item type
		 * @param value the item value */
		public SdesItem(int type, byte[] value) {
			this.type=type;
			this.buf=value;
			this.off=0;
			this.len=value.length;
		}

		/** Creates a new SdesItem.
		 * @param type item type
		 * @param value item value */
		public SdesItem(int type, String value) {
			this.type=type;
			this.buf=value.getBytes();
			this.off=0;
			this.len=buf.length;
		}


		/** Gets the item type.
		  * @return the item type */
		public int getType() {
			return type;
		}


		/** Gets the item value.
		  * @return the item value */
		public byte[] getValue() {
			if (off==0 && len==buf.length) return buf;
			else {
				byte[] value=new byte[len];
				System.arraycopy(buf,off,value,0,len);
				return value;
			}
		}


		/** Gets the the total length of the item, including the item type and length fields.
		  * @return the item length */
		public int getLength() {
			return len+2;
		}

	  
		/** Gets the item bytes.
		  * @return the item bytes. */
		public byte[] getBytes() {
			byte[] data=new byte[len+2];
			getBytes(data,0);
			return data;
		}


		/** Gets the item bytes.
		  * @return the total length of the item, including the item type and length fields. */
		public int getBytes(byte[] buf, int off) {
			buf[off]=(byte)type;
			buf[off+1]=(byte)len;
			System.arraycopy(this.buf,this.off,buf,off+2,len);
			return len+2;
		}


		/** Gets a string representation of this object.
		  * @return a string representing this object. */
		public String toString() {
			StringBuffer sb=new StringBuffer();
			sb.append("type=");
			sb.append(type);
			sb.append(",value=");
			sb.append(new String(buf,off,len));
			return sb.toString();
		}

	} 




	/** SSRC or CSRC chunk.
	  */
	public static class Chunk {
		
		/** SSRC/CSRC */
		long ssrc;
		
		/** List of zero or more items, which carry information about the SSRC/CSRC */
		SdesItem[] items;


		/** Creates a new Chunk. */
		public Chunk(byte[] buf, int off) {
			ssrc=RtpPacket.getLong(buf,off,off+4);
			int begin=off+4;
			ArrayList items_list=new ArrayList();
			while (buf[begin]!=0) {
				SdesItem item=new SdesItem(buf,begin);
				items_list.add(item);
				begin+=item.getLength();
			}
			items=new SdesItem[items_list.size()];
			for (int i=0; i<items.length; i++) items[i]=(SdesItem)items_list.get(i);
		}


		/** Creates a new Chunk.
		  * @param ssrc the SSRC/CSRC identifier
		  * @param items array of zero or more items, which carry information about the SSRC/CSRC */ 
		public Chunk(long ssrc, SdesItem[] items) {
			this.ssrc=ssrc;
			this.items=items;
		}


		/** Gets the SSRC/CSRC identifier.
		  * @return the SSRC/CSRC identifier */
		public long getSSRC() {
			return ssrc;
		}


		/** Gets the list of items, which carry information about the SSRC/CSRC.
		  * @return an array of zero or more items */
		public SdesItem[] getSdesItems() {
			return items;
		}


		/** Gets the chunk length.
		  * @return the total length of the chunk. */
		public int getLength() {
			int len=4;
			for (int i=0; i<items.length; i++) len+=items[i].getLength();
			len++;
			return ((len+3)/4)*4;
		}


		/** Gets the chunk bytes.
		  * @return an array of bytes containing this chunk. */
		public byte[] getBytes() {
			byte[] data=new byte[getLength()];
			getBytes(data,0);
			return data;
		}


		/** Gets the chunk bytes.
		  * @return the chunk length. */
		public int getBytes(byte[] buf, int off) {
			RtpPacket.setLong(ssrc,buf,off,off+4);
			int len=4;
			for (int i=0; i<items.length; i++) {
				len+=items[i].getBytes(buf,off+len);
			}
			// the list of items in each chunk MUST be terminated by one or more null octets
			buf[off+(len++)]=(byte)0x00;
			// additional null octets MUST be included if needed to pad until the next 32-bit boundary
			int padded_len=((len+3)/4)*4;
			while (len<padded_len) buf[off+(len++)]=(byte)0x00;
			return len;
		}


		/** Gets a string representation of this object.
		  * @return a string representing this object. */
		public String toString() {
			StringBuffer sb=new StringBuffer();
			sb.append("ssrc=");
			sb.append(ssrc);
			sb.append(",items=[");
			for (int i=0; i<items.length; i++) {
				sb.append("{");
				sb.append(items[i].toString());
				sb.append("}");
			}
			sb.append("]");
			return sb.toString();
		}
		
	}

}
