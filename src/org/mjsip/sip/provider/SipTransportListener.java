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



import org.mjsip.sip.message.SipMessage;
import org.zoolu.net.SocketAddress;



/** Listener for SipTransport events.
  */
public interface SipTransportListener {
	
	/** When a new SIP message is received. */
	public void onReceivedMessage(SipTransport transport, SipMessage msg);

	/** When a new incoming transport connection is established. It is called only for CO transport portocols. */ 
	public void onIncomingTransportConnection(SipTransport transport, SocketAddress remote_soaddr);

	/** When a transport connection terminates. It is called only for CO transport portocols. */
	public void onTransportConnectionTerminated(SipTransport transport, SocketAddress remote_soaddr, Exception error);

	/** When SipTransport terminates. */
	public void onTransportTerminated(SipTransport transport, Exception error);
}
