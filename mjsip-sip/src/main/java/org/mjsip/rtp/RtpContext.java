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



import org.zoolu.util.Random;



/** RtpContext mantains the state information of an RTP stream. 
  */
public class RtpContext {
	
	/** Payload type (PT) */
	int pt;

	/** 16-bit sequence number (module 2^16) */
	int sqn;

	/** 32-bit RTP timestamp */
	long timestamp;

	/** 32-bit Synchronization source (SSRC) identifier */
	long ssrc;

	/** Array of contributing source (CSRC) identifiers */
	long[] csrc;

	/** Packet counter */
	//long packet_count=0;   

	/** Octect counter */
	//long octect_count=0;   



	/** Creates a new RtpContext.
	  * @param pt payload type */
	public RtpContext(int pt) {
		init(pt,-1L,-1,-1L,null);
	}


	/** Creates a new RtpContext.
	  * @param pt payload type
	  * @param ssrc 32-bit synchronization source (SSRC) identifier */
	public RtpContext(int pt, long ssrc) {
		init(pt,ssrc,-1,-1L,null);
	}


	/** Creates a new RtpContext.
	  * @param pt payload type
	  * @param ssrc 32-bit synchronization source (SSRC) identifier
	  * @param sqn 16-bit sequence number
	  * @param timestamp 32-bit RTP timestamp */
	public RtpContext(int pt, long ssrc, int sqn, long timestamp) {
		init(pt,ssrc,sqn,timestamp,null);
	}


	/** Creates a new RtpContext.
	  * @param pt payload type
	  * @param ssrc 32-bit synchronization source (SSRC) identifier
	  * @param sqn 16-bit sequence number
	  * @param timestamp 32-bit RTP timestamp
	  * @param csrc array of contributing source (CSRC) identifiers */
	public RtpContext(int pt, long ssrc, int sqn, long timestamp, long[] csrc) {
		init(pt,ssrc,sqn,timestamp,csrc);
	}


	/** Inits the RtpContext.
	  * @param pt payload type
	  * @param ssrc 32-bit synchronization source (SSRC) identifier
	  * @param sqn 16-bit sequence number
	  * @param timestamp 32-bit RTP timestamp
	  * @param csrc array of contributing source (CSRC) identifiers */
	private void init(int pt, long ssrc, int sqn, long timestamp, long[] csrc) {
		this.pt=pt;
		this.ssrc=(ssrc<0)? (Random.nextLong()&0xffffffff) : (ssrc&0xffffffff);
		this.sqn=(sqn<0)? (Random.nextInt()&0xffff) : (sqn&0xffff);
		this.timestamp=(timestamp<0)? (Random.nextLong()&0xffffffff) : (timestamp&0xffffffff);
		this.csrc=csrc;
	}


	/** Gets the payload type (PT). */
	public int getPayloadType() {
		return pt;
	}


	/** Gets the synchronization source (SSRC) identifier.
	  * @return the SSRC */
	public long getSsrc() {
		return ssrc;
	}


	/** Gets the number contributing source (CSRC) identifiers.
	  * @return the number of CSRCs */
	public int getCC() {
		return (csrc==null)? 0 : csrc.length;
	}


	/** Gets the contributing source (CSRC) identifiers.
	  * @return the array of CSRCs */
	public long[] getCsrc() {
		return csrc;
	}


	/** Gets the sequence number. */
	public int getSequenceNumber() {
		return sqn;
	}


	/** Sets the sequence number.
	  * @param sqn sequence number */
	public void setSequenceNumber(int sqn) {
		this.sqn=sqn&0xffff;
	}


	/** Increments the sequence number (module 2^16).
	  * @return the new value of the sequence number */
	public int incSequenceNumber() {
		//return (sqn==65535)? sqn=0 : ++sqn;
		return sqn=(++sqn)&0xffff;
	}


	/** Gets the timestamp.
	  * @return the timestamp value */
	public long getTimestamp() {
		return timestamp;
	}


	/** Sets the timestamp.
	  * @param timestamp RTP timestamp */
	public void setTimestamp(long timestamp) {
		this.timestamp=timestamp&0xffffffff;
	}


	/** Increments the timestamp.
	  * @param delta_timestamp the time lapse from the previous timestamp (in sampling periods)
	  * @return the new value of the timestamp */
	public long incTimestamp(long delta_timestamp) {
		return timestamp=(timestamp+delta_timestamp)&0xffffffff;
	}


	/** Updates the RTP context based on the current RTP packet.
	  * @param rtp_packet the RTP packet
	  * @param delta_timestamp the normalized packet time (in sampling periods), used for calculating the next timestamp */
	/*public void update(RtpPacket rtp_packet, long delta_timestamp) {
		if (rtp_packet.getSsrc()==ssrc); {
			update(rtp_packet.getPayloadType(),rtp_packet.getSequenceNumber(),rtp_packet.getTimestamp(),delta_timestamp);
		}  
	}*/


	/** Updates the RTP context based on a RTP packet.
	  * @param delta_timestamp the normalized packet time (in sampling periods), used for calculating the next timestamp */
	/*public void update(long delta_timestamp) {
		update(pt,sqn,timestamp,delta_timestamp);
	}*/


	/** Updates the RTP context based on a RTP packet.
	  * @param pt payload type
	  * @param sqn the sequence number of the RTP packet
	  * @param timestamp the timestamp of the RTP packet
	  * @param delta_timestamp the normalized packet time (in sampling periods), used for calculating the next timestamp */
	/*public void update(int pt, int sqn, long timestamp, long delta_timestamp) {
		this.pt=pt;
		this.sqn=sqn+1;
		this.timestamp=timestamp+delta_timestamp;
	}*/


	/** Returns next RTP packet.
	  * @param pl_buf payload buffer
	  * @param pl_off payload offset
	  * @param pl_len payload length
	  * @param packet_time the normalized packet time (in sampling periods), used for calculating the next timestamp */
	/*public RtpPacket nextPacket(byte[] pl_buf, int pl_off, int pl_len, long packet_time) {
		RtpPacket rtp_packet=new RtpPacket(this,pl_buf,pl_off,pl_len);
		sqn++;
		timestamp+=packet_time;
		return rtp_packet;
	}*/

}
