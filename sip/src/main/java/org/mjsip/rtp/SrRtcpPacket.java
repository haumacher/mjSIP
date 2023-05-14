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




/** Sender Report (SR) RTCP packet, for transmission and reception
  * statistics from participants that are active senders (RFC 3550). 
  */
public class SrRtcpPacket extends RrRtcpPacket {
	

	/** SR header length */
	//static final int SR_HDR_LEN=8;

	/** Sender info length */
	static final int SENDER_INFO_LEN=20;

	/** Report block length */
	//static final int REPORT_BLOCK_LEN=24;

	/** Gets the relative reception report offset. */
	protected static int receptionReportOffset() {
		return HDR_LEN+SENDER_INFO_LEN;
	}



	/** Creates a new SR RTCP packet. */ 
	public SrRtcpPacket(RtcpPacket rp) {
		super(rp.buffer,rp.offset);
	}

	/** Creates a new SR RTCP packet. */ 
	public SrRtcpPacket(byte[] buffer) {
		super(buffer);
	}

	/** Creates a new SR RTCP packet. */ 
	public SrRtcpPacket(byte[] buffer, int offset) {
		super(buffer,offset);
	}

	/** Creates a new SR RTCP packet.
	  * @param ssrc the synchronization source identifier (SSRC) for the originator of this SR packet
	  * @param ntp_timestamp the wallclock time (NTP timestamp) when this report is sent, in Java format
	  * @param rtp_timestamp the same time as the NTP timestamp, but in the same units and with the same random offset as the RTP timestamps in data packets
	  * @param packet_count the total number of RTP data packets transmitted by the sender since starting transmission up until the time this SR packet was generated
	  * @param octect_count the total number of payload octets (i.e., not including header or padding) transmitted in RTP data packets by the sender since starting transmission up until the time this SR packet was generated */ 
	public SrRtcpPacket(long ssrc, long ntp_timestamp, long rtp_timestamp, long packet_count, long octect_count) {
		super(new byte[HDR_LEN+SENDER_INFO_LEN]);
		setVersion(2);
		setPayloadType(PT_SR);
		setPacketLength(HDR_LEN+SENDER_INFO_LEN);
		setSsrc(ssrc);
		setSenderInfo(new SenderInfo(ntp_timestamp,rtp_timestamp,packet_count,octect_count));
	}

	/** Creates a new SR RTCP packet. 
	  * @param ssrc the synchronization source identifier (SSRC) for the originator of this SR packet
	  * @param sender_info the sender info for this SR packet. */
	public SrRtcpPacket(long ssrc, SenderInfo sender_info) {
		super(new byte[HDR_LEN+SENDER_INFO_LEN]);
		setVersion(2);
		setPayloadType(PT_SR);
		setPacketLength(HDR_LEN+SENDER_INFO_LEN);
		setSsrc(ssrc);
		setSenderInfo(sender_info);
	}

	/** Creates a new SR RTCP packet. 
	  * @param ssrc the synchronization source identifier (SSRC) for the originator of this SR packet
	  * @param sender_info the sender info for this SR packet
	  * @param report_blocks array of report blocks for this SR packet */
	public SrRtcpPacket(long ssrc, SenderInfo sender_info, RrRtcpPacket.ReportBlock[] report_blocks) {
		super(new byte[HDR_LEN+SENDER_INFO_LEN+REPORT_BLOCK_LEN*report_blocks.length]);
		setVersion(2);
		setPayloadType(PT_SR);
		setPacketLength(HDR_LEN+SENDER_INFO_LEN+REPORT_BLOCK_LEN*report_blocks.length);
		setSsrc(ssrc);
		setSenderInfo(sender_info);
		setReportBlocks(report_blocks);
	}



	// reception report count (RC): 5 bits; The number of reception report blocks contained in this packet.  A value of zero is valid.
	// SSRC: 32 bits; The synchronization source identifier for the originator of this SR packet.
	// NTP timestamp: 64 bits; Indicates the wallclock time (see Section 4) when this report was sent so that it may be used in combination with timestamps returned in reception reports from other receivers to measure round-trip propagation to those receivers.

	/** Gets SenderInfo.
	  * @return the sender info of this SR packet */
	public SenderInfo getSenderInfo() {
		return new SenderInfo(buffer,offset+HDR_LEN);
	}

	/** Sets SenderInfo.
	  * @param sender_info the sender info for this SR packet */
	public void setSenderInfo(SenderInfo sender_info) {
		byte[] si_buf=sender_info.getBuffer();
		int si_off=sender_info.getBufferOffset();
		int begin=offset+HDR_LEN;
		for (int i=0; i<SENDER_INFO_LEN; i++) buffer[begin+i]=si_buf[si_off+i];
	}



	/** Sender info.
	  */
	public static class SenderInfo {
		
		/** Buffer */
		byte[] buf;
		
		/** Buffer offset */
		int off;
		
		/** Creates a new SenderInfo. */
		public SenderInfo(byte[] buf, int off) {
			this.buf=buf;
			this.off=off;
		}

		/** Creates a new SenderInfo.
		  * @param ntp_timestamp the wallclock time (NTP timestamp) when this report is sent, in Java format
		  * @param rtp_timestamp the same time as the NTP timestamp, but in the same units and with the same random offset as the RTP timestamps in data packets
		  * @param packet_count the total number of RTP data packets transmitted by the sender since starting transmission up until the time this SR packet was generated
		  * @param octect_count the total number of payload octets (i.e., not including header or padding) transmitted in RTP data packets by the sender since starting transmission up until the time this SR packet was generated */ 
		public SenderInfo(long ntp_timestamp, long rtp_timestamp, long packet_count, long octect_count) {
			buf=new byte[SENDER_INFO_LEN];
			off=0;
			setNtpTimestamp(ntp_timestamp);
			setRtpTimestamp(rtp_timestamp);
			setPacketCount(packet_count);
			setOctectCount(octect_count);
		}
		
		/** Gets buffer.
		  * @return the buffer containing this sender info */
		public byte[] getBuffer() {
			return buf;
		}

		/** Gets buffer offset.
		  * @return the offset of the sender info within the buffer */
		public int getBufferOffset() {
			return off;
		}

		/** Sets NTP timestamp value.
		  * @param time the wallclock time (in Java format) when this report is sent */
		public void setNtpTimestamp(long time) {
			NtpTimeStamp ts=new NtpTimeStamp(time);
			RtpPacket.setLong(ts.getNtpSeconds(),buf,off,off+4);
			RtpPacket.setLong(ts.getNtpFraction(),buf,off+4,off+8);
		}

		/** Gets NTP timestamp.
		  * @return the wallclock time (in Java format) when this report is sent */
		public long getNtpTimestamp() {
			long seconds=RtpPacket.getLong(buf,off,off+4);
			long fraction=RtpPacket.getLong(buf,off+4,off+8);
			NtpTimeStamp ts=new NtpTimeStamp(seconds,fraction);
			return ts.getTime();
		}

		/** Sets RTP timestamp.
		  * @param rtp_timestamp the same time as the NTP timestamp, but in the same units and with the same random offset as the RTP timestamps in data packets */
		public void setRtpTimestamp(long rtp_timestamp) {
			RtpPacket.setLong(rtp_timestamp,buf,off+8,off+12);
		}

		/** Gets RTP timestamp.
		  * @return the same time as the NTP timestamp, but in the same units and with the same random offset as the RTP timestamps in data packets */
		public long getRtpTimestamp() {
			return RtpPacket.getLong(buf,off+8,off+12);
		}

		/** Sets packet count.
		  * @param packet_count the total number of RTP data packets transmitted by the sender since starting transmission up until the time this SR packet was generated */
		public void setPacketCount(long packet_count) {
			RtpPacket.setLong(packet_count,buf,off+12,off+16);
		}

		/** Gets packet count.
		  * @return the total number of RTP data packets transmitted by the sender since starting transmission up until the time this SR packet was generated */
		public long getPacketCount() {
			return RtpPacket.getLong(buf,off+12,off+16);
		}

		/** Sets octect count.
		  * @param octect_count the total number of payload octets (i.e., not including header or padding) transmitted in RTP data packets by the sender since starting transmission up until the time this SR packet was generated */ 
		public void setOctectCount(long octect_count) {
			RtpPacket.setLong(octect_count,buf,off+16,off+20);
		}

		/** Gets octect count.
		  * @return the total number of payload octets (i.e., not including header or padding) transmitted in RTP data packets by the sender since starting transmission up until the time this SR packet was generated */ 
		public long getOctectCount() {
			return RtpPacket.getLong(buf,off+16,off+20);
		}
		
		/** Gets a string representation of this object.
		  * @return a string representing this object. */
		public String toString() {
			StringBuffer sb=new StringBuffer();
			sb.append("NTP time: ");
			sb.append(org.zoolu.util.DateFormat.formatYyyyMMddHHmmssSSS(new java.util.Date(getNtpTimestamp())));
			sb.append(", RTP time: ");
			sb.append(getRtpTimestamp());
			sb.append(", packets: ");
			sb.append(getPacketCount());
			sb.append(", octects: ");
			sb.append(getOctectCount());
			return sb.toString();
		}

	} 
	
}
