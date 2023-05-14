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




/** Receiver Report (RR) RTCP packet, for reception statistics from participants
  * that are not active senders and in combination with SR for active senders
  * reporting on more than 31 sources (RFC 3550). 
  */
public class RrRtcpPacket extends RtcpPacket {
	

	/** Header length */
	static final int HDR_LEN=8;

	/** Report block length */
	static final int REPORT_BLOCK_LEN=24;

	/** Gets the relative reception report offset. */
	protected static int receptionReportOffset() {
		return HDR_LEN;
	}



	/** Creates a new RR RTCP packet. */ 
	public RrRtcpPacket(RtcpPacket rp) {
		super(rp.buffer,rp.offset);
	}

	/** Creates a new RR RTCP packet. */ 
	public RrRtcpPacket(byte[] buffer) {
		super(buffer);
	}

	/** Creates a new RR RTCP packet. */ 
	public RrRtcpPacket(byte[] buffer, int offset) {
		super(buffer,offset);
	}


	/** Creates a new RR RTCP packet.
	  * @param ssrc the synchronization source identifier (SSRC) for the originator of this SR packet
	  * @param report_blocks array of report blocks for this SR packet */
	public RrRtcpPacket(long ssrc, ReportBlock[] report_blocks) {
		super(new byte[HDR_LEN+REPORT_BLOCK_LEN*report_blocks.length]);
		setVersion(2);
		setPayloadType(PT_RR);
		setPacketLength(HDR_LEN+REPORT_BLOCK_LEN*report_blocks.length);
		setSsrc(ssrc);
		setReportBlocks(report_blocks);
	}



	// reception report count (RC): 5 bits; The number of reception report blocks contained in this packet.  A value of zero is valid.
	// SSRC: 32 bits; The synchronization source identifier for the originator of this SR packet.
	// NTP timestamp: 64 bits; Indicates the wallclock time (see Section 4) when this report was sent so that it may be used in combination with timestamps returned in reception reports from other receivers to measure round-trip propagation to those receivers.

	/** Gets the reception report count (RC). */
	protected int getReceptionReportCount() {
		return (buffer[offset] & 0x1F);
	}

	/** Sets the reception report count (RC). */
	protected void setReceptionReportCount(int rc) {
		buffer[offset]=(byte)(((buffer[offset]>>5)<<5)+rc);
	}

	/** Gets the SSRC.
	  * @return the synchronization source identifier (SSRC) for the originator of this SR packet. */
	public long getSsrc() {
		return RtpPacket.getLong(buffer,offset+4,offset+8);
	}

	/** Sets the SSRC.
	  * @param ssrc the synchronization source identifier (SSRC) for the originator of this SR packet. */
	public void setSsrc(long ssrc) {
		RtpPacket.setLong(ssrc,buffer,offset+4,offset+8);
	}

	/** Gets the report blocks.
	  * return array of report blocks of this SR packet. */
	public ReportBlock[] getReportBlocks() {
		int rc=getReceptionReportCount();
		ReportBlock[] report_blocks=new ReportBlock[rc];
		for (int i=0; i<rc; i++) report_blocks[i]=new ReportBlock(buffer,offset+receptionReportOffset()+REPORT_BLOCK_LEN*i); 
		return report_blocks;
	}

	/** Sets the report blocks.
	  * @param report_blocks array of report blocks for this SR packet. */
	public void setReportBlocks(ReportBlock[] report_blocks) {
		int rc=(report_blocks!=null)? report_blocks.length : 0;
		if (rc>31) rc=31;
		setReceptionReportCount(rc);
		for (int i=0; i<rc; i++) {
			byte[] rb_buf=report_blocks[i].getBuffer();
			int rb_off=report_blocks[i].getBufferOffset();
			int begin=offset+receptionReportOffset()+REPORT_BLOCK_LEN*i;
			for (int j=0; j<REPORT_BLOCK_LEN; j++) buffer[begin+j]=rb_buf[rb_off+j];
		}
	}



	/** Report block.
	  */
	public static class ReportBlock {
		
		/** Buffer */
		byte[] buf;
		
		/** Buffer offset */
		int off;
		
		/** Creates a new ReportBlock. */
		public ReportBlock(byte[] buf, int off) {
			this.buf=buf;
			this.off=off;
		}


		/** Creates a new ReportBlock.
		  * @param ssrc the SSRC identifier of the source to which the information in this reception report block pertains
		  * @param fraction_lost the fraction of RTP data packets lost since the previous SR or RR packet was sent; the fraction loss is defined as the number of packets lost divided by the number of packets expected; it is represented by the integer part after multiplying the loss fraction by 256 (8 bit)
		  * @param packet_lost cumulative number of packets lost that is the total number of RTP data packets that have been lost since the beginning of reception; it is the number of packets expected less the number of packets actually received, where the number of packets received includes any which are late or duplicates
		  * @param highest_sqn the extended highest sequence number received (32bit); the low 16 bits contain the highest sequence number received in an RTP data packet, and the most significant 16 bits extend that sequence number with the corresponding count of sequence number cycles
		  * @param jitter the interarrival jitter, that is an estimate of the statistical variance of the RTP data packet interarrival time, measured in timestamp units and expressed as an unsigned integer
		  * @param lsr last SR timestamp (LSR), that is the middle 32 bits out of 64 in the NTP timestamp received as part of the most recent RTCP SR packet 
		  * @param dlsr delay since last SR (DLSR), that is the delay, expressed in units of 1/65536 seconds, between receiving the last SR packet and sending this reception report block */ 
		public ReportBlock(long ssrc, int fraction_lost, long packet_lost, long highest_sqn, long jitter, long lsr, long dlsr) {
			buf=new byte[REPORT_BLOCK_LEN];
			off=0;
			setSSRC(ssrc);
			setFractionLost(fraction_lost);
			setCumulativePacketLost(packet_lost);
			setHighestSqnReceived(highest_sqn);
			setInterarrivalJitter(jitter);
			setLSR(lsr);
			setDLSR(dlsr);
		}


		/** Gets buffer. */
		public byte[] getBuffer() {
			return buf;
		}

		/** Gets buffer offset. */
		public int getBufferOffset() {
			return off;
		}


		/** Sets the synchronization source (SSRC) identifier.
		  * @param ssrc the SSRC identifier of the source to which the information in this reception report block pertains */
		public void setSSRC(long ssrc) {
			RtpPacket.setLong(ssrc,buf,off,off+4);
		}

		/** Gets the synchronization source (SSRC) identifier.
		  * @return the SSRC identifier of the source to which the information in this reception report block pertains */
		public long getSSRC() {
			return RtpPacket.getLong(buf,off,off+4);
		}

		/** Sets fraction lost.
		  * @param fraction_lost the fraction of RTP data packets lost since the previous SR or RR packet was sent; the fraction loss is defined as the number of packets lost divided by the number of packets expected; it is represented by the integer part after multiplying the loss fraction by 256 (8 bit) */
		public void setFractionLost(int fraction_lost) {
			RtpPacket.setInt(fraction_lost,buf,off+4,off+5);
		}

		/** Gets fraction lost.
		  * @return the fraction of RTP data packets lost since the previous SR or RR packet was sent; the fraction loss is defined as the number of packets lost divided by the number of packets expected; it is represented by the integer part after multiplying the loss fraction by 256 (8 bit) */
		public int getFractionLost() {
			return RtpPacket.getInt(buf,off+4,off+5);
		}

		/** Sets cumulative number of packets lost.
		  * @param packet_lost cumulative number of packets lost that is the total number of RTP data packets that have been lost since the beginning of reception; it is the number of packets expected less the number of packets actually received, where the number of packets received includes any which are late or duplicates */
		public void setCumulativePacketLost(long packet_lost) {
			RtpPacket.setLong(packet_lost,buf,off+5,off+8);
		}

		/** Gets cumulative number of packets lost.
		  * @return cumulative number of packets lost that is the total number of RTP data packets that have been lost since the beginning of reception; it is the number of packets expected less the number of packets actually received, where the number of packets received includes any which are late or duplicates */
		public long getCumulativePacketLost() {
			return RtpPacket.getLong(buf,off+5,off+8);
		}

		/** Sets the extended highest sequence number received.
		  * @param highest_sqn the extended highest sequence number received (32bit); the low 16 bits contain the highest sequence number received in an RTP data packet, and the most significant 16 bits extend that sequence number with the corresponding count of sequence number cycles */
		public void setHighestSqnReceived(long highest_sqn) {
			RtpPacket.setLong(highest_sqn,buf,off+8,off+12);
		}

		/** Gets the extended highest sequence number received.
		  * @return the extended highest sequence number received (32bit); the low 16 bits contain the highest sequence number received in an RTP data packet, and the most significant 16 bits extend that sequence number with the corresponding count of sequence number cycles */
		public long getHighestSqnReceived() {
			return RtpPacket.getLong(buf,off+8,off+12);
		}

		/** Sets the interarrival jitter.
		  * @param jitter the interarrival jitter, that is an estimate of the statistical variance of the RTP data packet interarrival time, measured in timestamp units and expressed as an unsigned integer */
		public void setInterarrivalJitter(long jitter) {
			RtpPacket.setLong(jitter,buf,off+12,off+16);
		}

		/** Gets the interarrival jitter.
		  * @return the interarrival jitter, that is an estimate of the statistical variance of the RTP data packet interarrival time, measured in timestamp units and expressed as an unsigned integer */
		public long getInterarrivalJitter() {
			return RtpPacket.getLong(buf,off+12,off+16);
		}

		/** Sets last SR timestamp (LSR).
		  * @param lsr last SR timestamp (LSR), that is the middle 32 bits out of 64 in the NTP timestamp received as part of the most recent RTCP SR packet */
		public void setLSR(long lsr) {
			RtpPacket.setLong(lsr,buf,off+16,off+20);
		}

		/** Gets last SR timestamp (LSR).
		  * @return last SR timestamp (LSR), that is the middle 32 bits out of 64 in the NTP timestamp received as part of the most recent RTCP SR packet */
		public long getLSR() {
			return RtpPacket.getLong(buf,off+16,off+20);
		}

		/** Sets delay since last SR (DLSR).
		  * @param dlsr delay since last SR (DLSR), that is the delay, expressed in units of 1/65536 seconds, between receiving the last SR packet and sending this reception report block */ 
		public void setDLSR(long dlsr) {
			RtpPacket.setLong(dlsr,buf,off+20,off+24);
		}

		/** Gets delay since last SR (DLSR).
		  * @return delay since last SR (DLSR), that is the delay, expressed in units of 1/65536 seconds, between receiving the last SR packet and sending this reception report block */ 
		public long getDLSR() {
			return RtpPacket.getLong(buf,off+20,off+24);
		}
		
	}

}
