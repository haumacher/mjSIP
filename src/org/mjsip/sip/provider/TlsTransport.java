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
import org.zoolu.net.TlsContext;
import org.zoolu.net.TlsServerFactory;
import org.zoolu.net.TlsSocketFactory;
import org.zoolu.util.LogLevel;
import org.zoolu.util.Logger;



/** TlsTransport provides a TLS-based transport service for SIP.
  */
public class TlsTransport extends SipTransportCO/* implements TcpServerListener*/ {
	
	/** TLS protocol type */
	public static final String PROTO_TLS="tls";
	
	/** TLS server */
	TcpServer tls_server=null;

	/** TLS socket factory */
	TlsSocketFactory tls_socket_factory=null;



	/** Creates a new TLS transport. 
	  * @param local_port local TLS port
	  * @param host_ipaddr local ip address/interface the TLS has to be bound to (null for binding to all interfaces)
	  * @param nmax_connections maximum number of active connections
	  * @param key_file file containing the node's private key
	  * @param cert_file file containing the node's certificate
	  * @param trusted_certs files containing trusted certificates (for verifying server-side certificates)
	  * @param logger the logger used for event logging */ 
	public TlsTransport(int local_port, IpAddress host_ipaddr, int nmax_connections, String key_file, String cert_file, String[] trusted_certs, Logger logger)   throws IOException {
		super(local_port,nmax_connections,logger);
		init(local_port,host_ipaddr,key_file,cert_file,trusted_certs,null,false);
	}


	/** Creates a new TLS transport. 
	  * @param local_port local TLS port
	  * @param host_ipaddr local ip address/interface the TLS has to be bound to (null for binding to all interfaces)
	  * @param nmax_connections maximum number of active connections
	  * @param key_file file containing the node's private key
	  * @param cert_file file containing the node's certificate
	  * @param trust_folder folder containing all trusted certificates (for verifying server-side certificates)
	  * @param logger the logger used for event logging */ 
	public TlsTransport(int local_port, IpAddress host_ipaddr, int nmax_connections, String key_file, String cert_file, String trust_folder, Logger logger)   throws IOException {
		super(local_port,nmax_connections,logger);
		init(local_port,host_ipaddr,key_file,cert_file,null,trust_folder,false);
	}


	/** Creates a new TLS transport.
	  * Server-side certificates are automatically trusted (no verification of server-side certificates is perfomed).
	  * @param local_port local TLS port
	  * @param host_ipaddr local ip address/interface the TLS has to be bound to (null for binding to all interfaces)
	  * @param nmax_connections maximum number of active connections
	  * @param key_file file containing the node's private key
	  * @param cert_file file containing the node's certificate
	  * @param logger the logger used for event logging */ 
	public TlsTransport(int local_port, IpAddress host_ipaddr, int nmax_connections, String key_file, String cert_file, Logger logger)   throws IOException {
		super(local_port,nmax_connections,logger);
		init(local_port,host_ipaddr,key_file,cert_file,null,null,true);
	}


	/** Initializes the TLS transport.
	  * @param local_port local TLS port
	  * @param host_ipaddr local ip address/interface the TLS has to be bound to (null for binding to all interfaces)
	  * @param key_file file containing the node's private key
	  * @param cert_file file containing the node's certificate
	  * @param trusted_certs files containing trusted certificates (for verifying server-side certificates)
	  * @param trust_folder folder containing all trusted certificates (for verifying server-side certificates)
	  * @param trust_all if <i>true</i>, all certificates are considered trusted */ 
	private void init(int local_port, IpAddress host_ipaddr, String key_file, String cert_file, String[] trusted_certs, String trust_folder, boolean trust_all) throws IOException {
		if (tls_server!=null) tls_server.halt();
		// start tls
		try {
			TlsContext tls_context=new TlsContext();
			tls_context.setKeyCert(key_file,cert_file);
			if (trust_all) tls_context.setTrustAll(true);
			else {
				// load specific trusted certificates
				if (trusted_certs!=null) {
					for (int i=0; i<trusted_certs.length; i++) tls_context.addTrustCert(trusted_certs[i]);
				}
				// load all trusted certificates from folder
				if (trust_folder!=null) tls_context.addTrustFolder(trust_folder);
			}
			// tls server
			TlsServerFactory tls_server_factory=new TlsServerFactory(tls_context);
			TcpServerListener this_tls_server_listener=new TcpServerListener() {
				public void onIncomingConnection(TcpServer tcp_server, TcpSocket socket) {
					processIncomingConnection(tcp_server,socket);
				}
				public void onServerTerminated(TcpServer tcp_server, Exception error) {
					processServerTerminated(tcp_server,error);
				}
			};
			if (host_ipaddr==null) tls_server=tls_server_factory.createTlsServer(local_port,this_tls_server_listener);
			else tls_server=tls_server_factory.createTlsServer(local_port,host_ipaddr,this_tls_server_listener);
			// tls client
			tls_socket_factory=new TlsSocketFactory(tls_context);
			// force the newest TLS version
			//String[] ep=tls_socket_factory.getEnabledProtocols();
			//String[] sp={ ep[ep.length-1] };
			//tls_socket_factory.setEnabledProtocols(sp);
			//System.err.println("DEBUG: TlsTransport: enabled protocols: "+sp[0]);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new IOException(e.getMessage());
		}
	}


	/** Gets protocol type */ 
	public String getProtocol() {
		return PROTO_TLS;
	}


	/** Gets local port */ 
	public int getLocalPort() {
		if (tls_server!=null) return tls_server.getPort();
		else return 0;
	}


	/** Stops running */
	public void halt() {
		super.halt();
		if (tls_server!=null) tls_server.halt();
	}


	/** From TcpServerListener. When a new incoming connection is established */ 
	private void processIncomingConnection(TcpServer tcp_server, TcpSocket socket) {
		log(LogLevel.DEBUG,"incoming connection from "+socket.getAddress()+":"+socket.getPort());
		if (tcp_server==this.tls_server) {
			SipTransportConnection conn=new TlsTransportConnection(socket,this_conn_listener);
			log(LogLevel.DEBUG,"tls connection "+conn+" opened");
			addConnection(conn);
			if (listener!=null) listener.onIncomingTransportConnection(this,new SocketAddress(socket.getAddress(),socket.getPort()));
		}
	}


	/** From TcpServerListener. When TcpServer terminates. */
	private void processServerTerminated(TcpServer tcp_server, Exception error)  {
		log(LogLevel.DEBUG,"tls server "+tcp_server+" terminated");
	}


	/** Creates a transport connection to the remote end-point. */
	protected SipTransportConnection createTransportConnection(SocketAddress remote_soaddr) throws IOException {
		TcpSocket tls_socket=tls_socket_factory.createTlsSocket(remote_soaddr.getAddress(),remote_soaddr.getPort());
		return new TlsTransportConnection(tls_socket,this_conn_listener);
	}


	/** Creates a transport connection to the remote end-point. */
	/*protected SipTransportConnection createTransportConnection(int local_port, SocketAddress remote_soaddr) throws IOException {
		TcpSocket tls_socket=tls_socket_factory.createTlsSocket(remote_soaddr.getAddress(),remote_soaddr.getPort(),null,local_port);
		return new TlsTransportConnection(tls_socket,this);
	}*/


	/** Creates a transport connection to the remote end-point. */
	/*protected SipTransportConnection createTransportConnection(SocketAddress local_soaddr, SocketAddress remote_soaddr) throws IOException {
		TcpSocket tls_socket=tls_socket_factory.createTlsSocket(remote_soaddr.getAddress(),remote_soaddr.getPort(),local_soaddr.getAddress(),local_soaddr.getPort());
		return new TlsTransportConnection(tls_socket,this);
	}*/


	/** Gets a String representation of the Object */
	public String toString() {
		if (tls_server!=null) return tls_server.toString();
		else return null;
	}
}
