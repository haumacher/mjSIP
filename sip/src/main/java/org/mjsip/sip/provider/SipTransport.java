/*
 * Copyright (C) 2005 Luca Veltri - University of Parma - Italy
 * 
 * This file is part of MjSip (http://www.mjsip.org)
 * 
 * MjSip is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * MjSip is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with MjSip; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 * 
 * Author(s):
 * Luca Veltri (luca.veltri@unipr.it)
 */

package org.mjsip.sip.provider;



import java.io.IOException;

import org.mjsip.sip.message.SipMessage;
import org.zoolu.net.IpAddress;



/** SipTransport is a generic transport service for SIP.
  */
public interface SipTransport {
	
	/** Gets protocol type */ 
	public String getProtocol();

	/** Gets local port */ 
	public int getLocalPort();

	/** Stops running */
	public void halt();

	/** Sets transport listener */
	public void setListener(SipTransportListener listener);

	/** From SipTransport. Sends a SipMessage to the given remote address and port, with a given TTL.
	  * <p>
	  * If the transport protocol is Connection Oriented (CO), this method first looks for a proper active
	  * connection; if no active connection is found, a new connection is opened.
	  * <p>
	  * If the transport protocol is Connection Less (CL) the message is simply sent to the remote point.
	  * @return Returns the id of the used connection for CO transport, or null for CL transport. */      
	public ConnectionId sendMessage(SipMessage msg, IpAddress dest_ipaddr, int dest_port, int ttl) throws IOException;

	/** Gets a String representation of the Object */
	public String toString();
}
