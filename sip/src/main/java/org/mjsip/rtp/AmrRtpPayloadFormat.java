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



import org.zoolu.sound.codec.AMR;
import org.zoolu.util.BitString;
import org.zoolu.util.BitStringBuffer;



/** It provides methods for adding and removing AMR RTP payload format (according to RFC 4867).
  * Both Bandwidth-Efficient and Octet-Aligned format modes are supported.
  */
public class AmrRtpPayloadFormat implements RtpPayloadFormat {
	
	/** Default codec mode request (CMR) field (15=no specific mode) */
	public static short DEFAULT_CMR=15;
	//public static short DEFAULT_CMR=7;


	/** Whether using the AMR RTP payload format in Bandwidth-Efficient Mode (true=Bandwidth-Efficient false=Octet-Aligned) */
	boolean bandwidth_efficient_mode;

	/** Codec mode request (CMR) field */
	//short cmr;



	/** Creates a AmrRtpPayloadFormat.
	  * @param bandwidth_efficient_mode whether using the AMR RTP payload format in Bandwidth-Efficient Mode or Octet-Aligned Mode (true=Bandwidth-Efficient false=Octet-Aligned) */
	public AmrRtpPayloadFormat(boolean bandwidth_efficient_mode) {
		this.bandwidth_efficient_mode=bandwidth_efficient_mode;
		//this.cmr=DEFAULT_CMR;
	}


	/** Gets padding data for a given silence interval between two RTP packat.
	  * @param sqn_interval the RTP sqn interval; it is the different between the sequence numbers
				  of the two received RTP packets. Usually, silince is present if <i>sqn_interval</i> is greater than 1
	  * @param timestamp_interval the RTP timestamp interval (in samples); it is the different between
				  the timestampss of the two received RTP packets
	  * @param buf buffer that will be filled with the padding data for the given interval
	  * @param off offset within the buffer
	  * @return the length of the padding data */
	public int getSilencePad(int sqn_interval, long timestamp_interval, byte[] buf, int off) {
		//int len=sqn_interval-1;
		int len=(int)(timestamp_interval/160)-1;
		if (len<0) return 0;
		// else
		java.util.Arrays.fill(buf,off,off+len,(byte)0x7c);
		return len;
	}


	/** Removes RTP payload format.
	  * @param buf the RTP payload buffer
	  * @param off the offset within the RTP payload buffer
	  * @param len the number of bytes of the received (formatted) RTP payload.
	  * @return the number of bytes after removing the RTP payload format */
	public int removeRtpPayloadFormat(byte[] buf, int off, int len) throws Exception {
		if (bandwidth_efficient_mode) return removeRtpPayloadFormatBandwidthEfficient(buf,off,len);
		else return removeRtpPayloadFormatOctetAligned(buf,off,len);
	}


	/** Applies additional format to the RTP payload.
	  * @param buf the RTP payload buffer
	  * @param off the offset within the RTP payload buffer
	  * @param len the number of bytes of the original (unformatted) RTP payload.
	  * @return the number of bytes after additional RTP payload format has been applied */
	public int setRtpPayloadFormat(byte[] buf, int off, int len) {
		if (bandwidth_efficient_mode) return setRtpPayloadFormatBandwidthEfficient(buf,off,len);
		else return setRtpPayloadFormatOctetAligned(buf,off,len);
	}


	/** Gets the actual payload length after the additional format is applied. 
	  * @param len the number of payload bytes before applying the additional RTP payload format
	  * @return the number of paylaod bytes after applying the additional RTP payload format. */
	public int getRtpPayloadFormatLength(int len) {
		if (bandwidth_efficient_mode) return getRtpPayloadFormatLengthBandwidthEfficient(len);
		else return getRtpPayloadFormatLengthOctetAligned(len);
	}




	/** Removes RTP payload format in Bandwidth-Efficient Mode.
	  * @param buf the RTP payload buffer
	  * @param off the offset within the RTP payload buffer
	  * @param len the number of bytes of the received (formatted) RTP payload.
	  * @return the number of bytes after removing the RTP payload format */
	private static int removeRtpPayloadFormatBandwidthEfficient(byte[] buf, int off, int len) throws Exception {
		BitStringBuffer bb=new BitStringBuffer().append(new BitString(buf,off,len,false));
		int cmr=(bb.poll(4).toBitString().getBytes(false)[0]&0xf0)>>4;
		int hdr=bb.poll(6).toBitString().getBytes(false)[0];
		int type=(hdr>>3)&0xf;
		int frame_bit_len=AMR.framePayloadBitSize(type);
		if (frame_bit_len==0) return 0;
		// else
		byte[] data=bb.poll(frame_bit_len).toBitString().getBytes(false);
		//int pad_len=(8-(frame_bit_len+10)%8)%8;
		//bb.poll(pad_len);
		//System.out.println("CMR="+cmr+", TF="+type+", pad="+pad_len+", data="+org.zoolu.util.ByteUtils.asHex(data));

		//if (type>=8 || data.length==0) return 0; // AMR SID (comfort noise)
		// else
		buf[off]=(byte)hdr;
		for (int i=0; i<data.length; i++) buf[off+1+i]=data[i];
		return data.length+1;
	}


	/** Applies additional format to the RTP payload in Bandwidth-Efficient Mode.
	  * @param buf the RTP payload buffer
	  * @param off the offset within the RTP payload buffer
	  * @param len the number of bytes of the original (unformatted) RTP payload.
	  * @return the number of bytes after additional RTP payload format has been applied */
	private static int setRtpPayloadFormatBandwidthEfficient(byte[] buf, int off, int len) {
		BitStringBuffer bb=new BitStringBuffer();
		//BitString cmr=new BitString("1111"); // no specific mode
		BitString cmr=new BitString(new byte[]{(byte)DEFAULT_CMR},false).substring(4,8);
		bb.append(cmr);
		int hdr=buf[off];
		int type=(hdr>>3)&0xf;
		BitString toc=new BitString(new byte[]{(byte)((type<<1)|0x1)},false).substring(2,8);
		bb.append(toc);
		bb.append(new BitString(buf,off+1,len-1,false));
		int frame_bit_len=AMR.framePayloadBitSize(type);
		bb=bb.substring(0,10+frame_bit_len);
		return bb.toBitString().getBytes(buf,off,false);
	}


	/** Gets the actual payload length after the additional format is applied in Bandwidth-Efficient Mode. 
	  * @param len the number of payload bytes before applying the additional RTP payload format
	  * @return the number of paylaod bytes after applying the additional RTP payload format. */
	private static int getRtpPayloadFormatLengthBandwidthEfficient(int len) {
		//int frame_len=AMR.framePayloadBitLength(type);
		//return ((4+frame_len)+7)/8;
		switch (len) {
			case 13 : return 14; // AMR_0475: 95+10 bits / 8
			case 14 : return 15; // AMR_0515: 103+10 bits / 8
			case 16 : return 16; // AMR_0590: 118+10 bits / 8
			case 18 : return 18; // AMR_0670: 134+10 bits / 8
			case 20 : return 20; // AMR_0740: 148+10 bits / 8
			case 21 : return 22; // AMR_0795: 159+10 bits / 8
			case 27 : return 27; // AMR_1020: 204+10 bits / 8
			case 32 : return 32; // AMR_1220: 244+10 bits / 8
			case 6 : return 7;   // AMR_SID (comfort noise): 39+10 bits / 8
			case 1 : return 2;   // AMR_NO_DATA: 0+10 bits / 8
			default : return len+1;
		}
	}


	/** Removes RTP payload format in Octet-Aligned Mode.
	  * @param buf the RTP payload buffer
	  * @param off the offset within the RTP payload buffer
	  * @param len the number of bytes of the received (formatted) RTP payload.
	  * @return the number of bytes after removing the RTP payload format */
	private static int removeRtpPayloadFormatOctetAligned(byte[] buf, int off, int len) throws Exception {
		//for (int i=off; i<off+len; i++) buf[i]=buf[i+1];
		//return len-1;
		int frame_count=1;
		for (int i=off+1; (buf[i]&0x80)==0x80; i++) frame_count++;
		byte amr_hdr=buf[off+frame_count];
		int amr_mode=(amr_hdr>>3)&0xf;
		int frame_size=AMR.frameSize(amr_mode);
		int src_index=off+frame_count+1;
		int dst_index=off;
		for (int i=0; i<frame_count; i++) {
			buf[dst_index++]=amr_hdr;
			for (int j=1; j<frame_size; j++) buf[dst_index++]=buf[src_index++];
		}
		return frame_count*frame_size;
	}


	/** Applies additional format to the RTP payload in Octet-Aligned Mode.
	  * @param buf the RTP payload buffer
	  * @param off the offset within the RTP payload buffer
	  * @param len the number of bytes of the original (unformatted) RTP payload.
	  * @return the number of bytes after additional RTP payload format has been applied */
	private static int setRtpPayloadFormatOctetAligned(byte[] buf, int off, int len) {
		for (int i=off+len; i>off; i--) buf[i]=buf[i-1];
		buf[off]=(byte)(DEFAULT_CMR<<4);
		return len+1;
	}


	/** Gets the actual payload length after the additional format is applied in Octet-Aligned Mode. 
	  * @param len the number of payload bytes before applying the additional RTP payload format
	  * @return the number of paylaod bytes after applying the additional RTP payload format. */
	private static int getRtpPayloadFormatLengthOctetAligned(int len) {
		return len+1;
	}

}
