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




/** Listener for RtcpProvider events.
  */
public interface RtcpProviderListener {
	
	/** When a new RTCP packet is received. */
	public void onReceivedPacket(RtcpProvider rtcp, RtcpPacket rtcp_packet);

	/** When RtcpProvider terminates. */
	public void onServiceTerminated(RtcpProvider rtcp, Exception error);

}
