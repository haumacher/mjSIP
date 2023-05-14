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
import org.mjsip.sip.message.SipMessageBuffer;
import org.zoolu.net.IpAddress;
import org.zoolu.net.TcpConnection;
import org.zoolu.net.TcpConnectionListener;
import org.zoolu.net.TcpSocket;



/** TcpTransportConnection provides a TCP trasport service for SIP.
  */
public class TcpTransportConnection implements SipTransportConnection/*, TcpConnectionListener*/ {
	
	/** TCP protocol type */
	static final String PROTO_TCP="tcp";

	/** TCP connection */
	TcpConnection tcp_conn;  

	/** TCP connection */
	ConnectionId connection_id;  

	/** The last time that has been used (in milliseconds) */
	long last_time;
	
	/** Receiver buffer. */
	SipMessageBuffer buffer=new SipMessageBuffer();
	  
	/** SipTransportConnection listener */
	SipTransportConnectionListener listener;   



	/** Creates a new TcpTransportConnection. */ 
	public TcpTransportConnection(IpAddress remote_ipaddr, int remote_port, SipTransportConnectionListener listener) throws IOException {
		init(new TcpSocket(remote_ipaddr,remote_port),listener);
	}


	/** Creates a new TcpTransportConnection.
	  * @param socket the TCP socket
	  * @param listener the TcpTransportConnection listener */
	public TcpTransportConnection(TcpSocket socket, SipTransportConnectionListener listener) {
		init(socket,listener);
	}


	/** Inits the TcpTransportConnection.
	  * @param socket the TCP socket
	  * @param listener the TcpTransportConnection listener */
	private void init(TcpSocket socket, SipTransportConnectionListener listener) {
		this.listener=listener;
		TcpConnectionListener this_tcp_conn_listener=new TcpConnectionListener() {
			public void onReceivedData(TcpConnection tcp_conn, byte[] data, int len) {
				processReceivedData(tcp_conn,data,len);
			}   
			public void onConnectionTerminated(TcpConnection tcp_conn, Exception error)   {
				processConnectionTerminated(tcp_conn,error);
			}
		};
		tcp_conn=new TcpConnection(socket,this_tcp_conn_listener);
		connection_id=new ConnectionId(this);
		last_time=System.currentTimeMillis();
	}


	/** Sets the SipTransportConnection listener. */      
	public void setListener(SipTransportConnectionListener listener) {
		this.listener=listener;
	}


	/** Gets protocol type. */ 
	public String getProtocol() {
		return PROTO_TCP;
	}


	/** Gets the remote IpAddress. */
	public IpAddress getRemoteAddress() {
		if (tcp_conn!=null) return tcp_conn.getRemoteAddress();
		else return null;
	}
	
	
	/** Gets the remote port. */
	public int getRemotePort() {
		if (tcp_conn!=null) return tcp_conn.getRemotePort();
		else return 0;
	}


	/** Gets the local IpAddress. */
	public IpAddress getLocalAddress() {
		if (tcp_conn!=null) return tcp_conn.getLocalAddress();
		else return null;
	}

	/** Gets the local port. */
	public int getLocalPort() {
		if (tcp_conn!=null) return tcp_conn.getLocalPort();
		else return 0;
	}


	/** Gets the last time the Connection has been used (in millisconds). */
	public long getLastTimeMillis() {
		return last_time;
	}


	/** Sends a SipMessage. */      
	public void sendMessage(SipMessage msg) throws IOException {
		if (tcp_conn!=null) {
			last_time=System.currentTimeMillis();
			byte[] data=msg.getBytes();
			tcp_conn.send(data);
			// DEBUG:
			//int offset=data.length/2;
			//tcp_conn.send(data,0,offset);
			//try { Thread.sleep(20); } catch (Exception e) {}
			//tcp_conn.send(data,offset,data.length-offset);
		}
	}


	/** Stops running. */
	public void halt() {
		if (tcp_conn!=null) tcp_conn.halt();
	}


	/** Gets a String representation of the Object. */
	public String toString() {
		if (tcp_conn!=null) return tcp_conn.toString();
		else return null;
	}


	//************************* Callback methods *************************

	/** When new data is received through the TcpConnection. */
	private void processReceivedData(TcpConnection tcp_conn, byte[] data, int len) {
		//System.out.println("DEBUG: TcpTransportConnection: onReceivedData(): len: "+len);
		last_time=System.currentTimeMillis();

		buffer.append(data,0,len);

		// try to get one or more SIP messages from the buffer    
		SipMessage msg;
		while ((msg=getSipMessage())!=null) {
			//System.out.println("DEBUG: TcpTransportConnection: onReceivedData(): msg len: "+msg.getLength());
			msg.setRemoteAddress(tcp_conn.getRemoteAddress().toString());
			msg.setRemotePort(tcp_conn.getRemotePort());
			msg.setTransportProtocol(getProtocol());
			msg.setConnectionId(connection_id);
			if (listener!=null) listener.onReceivedMessage(this,msg);
		}     
	}


	/** When TcpConnection terminates. */
	private void processConnectionTerminated(TcpConnection tcp_conn, Exception error)   {
		if (listener!=null) listener.onConnectionTerminated(this,error);
		TcpSocket socket=tcp_conn.getSocket();
		if (socket!=null) try { socket.close(); } catch (Exception e) {}
		this.tcp_conn=null;
		this.listener=null;
	}


	/** Tries to get a SIP message from the receiver buffer. */
	private SipMessage getSipMessage()   {
		SipMessage msg=null;
		// skip possible leading CRLF
		byte b;
		while (buffer.getLength()>0 && ((b=buffer.byteAt(0))=='\r' || b=='\n')) buffer.skip(1);
		// try to get a SIP message
		try {  msg=buffer.parseSipMessage();  } catch (Exception e) {}
		return msg;
	}

}
