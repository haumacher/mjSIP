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



import org.zoolu.net.IpAddress;
import org.zoolu.net.SocketAddress;
import org.zoolu.util.Identifier;



/** ConnectionId is the reference identifier for a transport connection.
  */
public class ConnectionId extends Identifier {
	

	/** Gets a connection identifier.
	  * @param protocol the transport protocol
	  * @param remote_ipaddr the remote IP address
	  * @param remote_port the remote port */
	private static String getConnectionId(String protocol, IpAddress remote_ipaddr, int remote_port) {
		return protocol+":"+remote_ipaddr+":"+remote_port;
	}
	


	/** Creates a new ConnectionId.
	  * @param conn_id a connection identifier */
	public ConnectionId(ConnectionId conn_id) {
		super(conn_id);
	}

	/** Creates a new ConnectionId.
	  * @param id the actual identifier */
	/*public ConnectionId(String id) {
		super(id);
	}*/

	/** Creates a new ConnectionId.
	  * @param protocol the transport protocol
	  * @param remote_ipaddr the remote IP address
	  * @param remote_port the remote port */
  public ConnectionId(String protocol, IpAddress remote_ipaddr, int remote_port) {
		super(getConnectionId(protocol,remote_ipaddr,remote_port));
	}

	/** Creates a new ConnectionId.
	  * @param protocol the transport protocol
	  * @param remote_soaddr the remote socket address */
	public ConnectionId(String protocol, SocketAddress remote_soaddr) {
		super(getConnectionId(protocol,remote_soaddr.getAddress(),remote_soaddr.getPort()));
	}

	/** Creates a new ConnectionId.
	  * @param conn a connection */
	public ConnectionId(SipTransportConnection conn) {
		super(getConnectionId(conn.getProtocol(),conn.getRemoteAddress(),conn.getRemotePort()));
	}

	/** Gets the transport protocol.
	  * @return the transport protocol */
	public String getProtocol() {
		return id.substring(0,id.indexOf(':'));
	}

}
