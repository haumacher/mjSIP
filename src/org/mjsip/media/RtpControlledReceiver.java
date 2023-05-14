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

package org.mjsip.media;




/** RTP controlled receiver.
  */
public interface RtpControlledReceiver {
	
	/** Gets SSRC.
	  * @return he synchronization source (SSRC) identifier of the received RTP packets */
	public long getSSRC();

	/** Gets fraction lost.
	  * @return the fraction of RTP data packets lost since the previous SR or RR packet was sent; the fraction loss is defined as the number of packets lost divided by the number of packets expected; it is represented by the integer part after multiplying the loss fraction by 256 (8 bit) */
	public int getFractionLost();

	/** Gets cumulative number of packets lost.
	  * @return cumulative number of packets lost that is the total number of RTP data packets that have been lost since the beginning of reception; it is the number of packets expected less the number of packets actually received, where the number of packets received includes any which are late or duplicates */
	public long getCumulativePacketLost();

	/** Gets the extended highest sequence number received.
	  * @return the extended highest sequence number received (32bit); the low 16 bits contain the highest sequence number received in an RTP data packet, and the most significant 16 bits extend that sequence number with the corresponding count of sequence number cycles */
	public long getHighestSqnReceived();

	/** Gets the interarrival jitter.
	  * @return the interarrival jitter, that is an estimate of the statistical variance of the RTP data packet interarrival time, measured in timestamp units and expressed as an unsigned integer */
	public long getInterarrivalJitter();

	/** Gets last SR timestamp (LSR).
	  * @return last SR timestamp (LSR), that is the middle 32 bits out of 64 in the NTP timestamp received as part of the most recent RTCP SR packet */
	public long getLSR();

	/** Gets delay since last SR (DLSR).
	  * @return delay since last SR (DLSR), that is the delay, expressed in units of 1/65536 seconds, between receiving the last SR packet and sending this reception report block */ 
	public long getDLSR();
	
}
