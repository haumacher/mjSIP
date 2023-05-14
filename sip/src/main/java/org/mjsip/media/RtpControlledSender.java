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




/** RTP controlled sender.
  */
public interface RtpControlledSender {
	
	/** Gets SSRC. 
	  * @return the synchronization source (SSRC) identifier */
	public long getSSRC();
	
	/** Gets current RTP timestamp.
	  * @return the same time as the NTP timestamp, but in the same units and with the same random offset as the RTP timestamps in data packets */
	public long getRtpTimestamp();

	/** Gets packet count.
	  * @return the total number of RTP data packets transmitted by the sender since starting transmission up until now */
	public long getPacketCounter();

	/** Gets octect count.
	  * @return the total number of payload octets (i.e., not including header or padding) transmitted in RTP data packets by the sender since starting transmission up until now */ 
	public long getOctectCounter();

	
}
