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




/** RTP payload format.
  */
public interface RtpPayloadFormat {
	

	/** Gets padding data for a given silence interval between two RTP packat.
	  * @param sqn_interval the RTP sqn interval; it is the different between the sequence numbers
				  of the two received RTP packets. Usually, silince is present if <i>sqn_interval</i> is greater than 1
	  * @param timestamp_interval the RTP timestamp interval (in samples); it is the different between
				  the timestampss of the two received RTP packets
	  * @param buf buffer that will be filled with the padding data for the given interval
	  * @param off offset within the buffer
	  * @return the length of the padding data */
	public int getSilencePad(int sqn_interval, long timestamp_interval, byte[] buf, int off);


	/** Gets the actual payload length after an additional format is applied (if required by the specific payload type). 
	  * @param len the number of payload bytes before applying the additional RTP payload format
	  * @return the number of paylaod bytes after applying the additional RTP payload format. */
	public int getRtpPayloadFormatLength(int len);


	/** Applies an additional format to the RTP payload, depending to the specific payload type.
	  * <p> 
	  * By re-defined this method (by a class that extends RtpStreamSender) it is possible to
	  * implement additional RTP encapsulation format or encoding.
	  * @param buf the RTP payload buffer
	  * @param off the offset within the RTP payload buffer
	  * @param len the number of bytes of the original (unformatted) RTP payload.
	  * @return the number of bytes after additional RTP payload format has been applied */
	public int setRtpPayloadFormat(byte[] buf, int off, int len);


	/** Removes possible RTP payload format, if present.
	  * The presence of an additonal RTP payload format depends to the specific payload type.
	  * <p> 
	  * By re-defined this method (by a class that extends RtpStreamReceiver) it is possible to
	  * implement additional RTP format decapsulation or decoding.
	  * @param buf the RTP payload buffer
	  * @param off the offset within the RTP payload buffer
	  * @param len the number of bytes of the received (formatted) RTP payload.
	  * @return the number of bytes after removing the RTP payload format */
	public int removeRtpPayloadFormat(byte[] buf, int off, int len) throws Exception;
  
}
