/*
 * Copyright (C) 2009 Luca Veltri - University of Parma - Italy
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

import org.zoolu.net.IpAddress;
import org.zoolu.net.SocketAddress;
import org.zoolu.net.TcpServer;
import org.zoolu.net.TcpServerListener;
import org.zoolu.net.TcpSocket;
import org.zoolu.util.LogLevel;
import org.zoolu.util.Logger;



/** TcpTransport provides a TCP-based transport service for SIP.
  */
public class TcpTransport extends SipTransportCO/* implements TcpServerListener*/ {
	
	/** TCP protocol type */
	public static final String PROTO_TCP="tcp";
	
	/** TCP server */
	TcpServer tcp_server=null;



	/** Creates a new TcpTransport */ 
	public TcpTransport(int local_port, int nmax_connections, Logger logger)   throws IOException {
		super(local_port,nmax_connections,logger);
		init(local_port,null);
	}


	/** Creates a new TcpTransport */ 
	public TcpTransport(int local_port, IpAddress host_ipaddr, int nmax_connections, Logger logger)   throws IOException {
		super(local_port,nmax_connections,logger);
		init(local_port,host_ipaddr);
	}


	/** Inits the TcpTransport */ 
	private void init(int local_port, IpAddress host_ipaddr) throws IOException {
		if (tcp_server!=null) tcp_server.halt();
		// start tcp
		TcpServerListener this_tcp_server_listener=new TcpServerListener() {
			public void onIncomingConnection(TcpServer tcp_server, TcpSocket socket) {
				processIncomingConnection(tcp_server,socket);
			}
			public void onServerTerminated(TcpServer tcp_server, Exception error) {
				processServerTerminated(tcp_server,error);
			}
		};
		if (host_ipaddr==null) tcp_server=new TcpServer(local_port,this_tcp_server_listener);
		else tcp_server=new TcpServer(local_port,host_ipaddr,this_tcp_server_listener);
	}


	/** Gets protocol type */ 
	public String getProtocol() {
		return PROTO_TCP;
	}


	/** Gets local port */ 
	public int getLocalPort() {
		if (tcp_server!=null) return tcp_server.getPort();
		else return 0;
	}


	/** Stops running */
	public void halt() {
		super.halt();
		if (tcp_server!=null) tcp_server.halt();
	}


	/** When a new incoming connection is established */ 
	private void processIncomingConnection(TcpServer tcp_server, TcpSocket socket) {
		log(LogLevel.DEBUG,"incoming connection from "+socket.getAddress()+":"+socket.getPort());
		if (tcp_server==this.tcp_server) {
			SipTransportConnection conn=new TcpTransportConnection(socket,this_conn_listener);
			log(LogLevel.DEBUG,"tcp connection "+conn+" opened");
			addConnection(conn);
			if (listener!=null) listener.onIncomingTransportConnection(this,new SocketAddress(socket.getAddress(),socket.getPort()));
		}
	}


	/** From TcpServerListener. When TcpServer terminates. */
	private void processServerTerminated(TcpServer tcp_server, Exception error)  {
		log(LogLevel.DEBUG,"tcp server "+tcp_server+" terminated");
	}


	/** Creates a transport connection to the remote end-point. */
	protected SipTransportConnection createTransportConnection(SocketAddress remote_soaddr) throws IOException {
		TcpSocket tcp_socket=new TcpSocket(remote_soaddr.getAddress(),remote_soaddr.getPort());
		return new TcpTransportConnection(tcp_socket,this_conn_listener);
	}


	/** Creates a transport connection to the remote end-point. */
	/*protected SipTransportConnection createTransportConnection(int local_port, SocketAddress remote_soaddr) throws IOException {
		TcpSocket tcp_socket=new TcpSocket(remote_soaddr.getAddress(),remote_soaddr.getPort(),null,local_port);
		return new TcpTransportConnection(tcp_socket,this);
	}*/


	/** Creates a transport connection to the remote end-point. */
	/*protected SipTransportConnection createTransportConnection(SocketAddress local_soaddr, SocketAddress remote_soaddr) throws IOException {
		TcpSocket tcp_socket=new TcpSocket(remote_soaddr.getAddress(),remote_soaddr.getPort(),local_soaddr.getAddress(),local_soaddr.getPort());
		return new TcpTransportConnection(tcp_socket,this);
	}*/


	/** Gets a String representation of the Object */
	public String toString() {
		if (tcp_server!=null) return tcp_server.toString();
		else return null;
	}

}
