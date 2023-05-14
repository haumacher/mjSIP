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
import org.zoolu.net.UdpPacket;
import org.zoolu.net.UdpProvider;
import org.zoolu.net.UdpProviderListener;
import org.zoolu.net.UdpSocket;
import org.zoolu.util.ByteUtils;



/** UdpTransport provides an UDP transport service for SIP.
  */
public class UdpTransport implements SipTransport/*, UdpProviderListener*/ {
	
	/** Ping data */
	static final byte[] PING=new byte[]{0x0d,0x0a,0x0d,0x0a}; // CRLF CRCF (RFC5626 PING)
	
	/** Pong data */
	static final byte[] PONG=new byte[]{0x0d,0x0a}; // CRCF (RFC5626 PONG)



	/** UDP protocol type */
	public static final String PROTO_UDP="udp";

	/** UDP provider */
	UdpProvider udp_provider;  

	/** SipTransport listener */
	SipTransportListener listener=null;   



	/** Creates a new UdpTransport */ 
	public UdpTransport(UdpSocket socket) {
		init(socket);
	}


	/** Creates a new UdpTransport */ 
	public UdpTransport(int local_port) throws IOException {
		init(local_port,null);
	}


	/** Creates a new UdpTransport */ 
	public UdpTransport(int local_port, IpAddress host_ipaddr) throws IOException {
		init(local_port,host_ipaddr);
	}


	/** Inits the UdpTransport */ 
	private void init(int local_port, IpAddress host_ipaddr) throws IOException {
		UdpSocket socket=(host_ipaddr==null)? new UdpSocket(local_port) : new UdpSocket(local_port,host_ipaddr);
		//UdpSocket socket=(host_ipaddr==null)? new org.zoolu.net.JumboUdpSocket(local_port,500) : new org.zoolu.net.JumboUdpSocket(local_port,host_ipaddr,500);
		init(socket);
	}


	/** Inits the UdpTransport */ 
	private void init(UdpSocket socket) {
		if (udp_provider!=null) udp_provider.halt();
		// start udp
		UdpProviderListener this_udp_provider_listener=new UdpProviderListener() {
			public void onReceivedPacket(UdpProvider udp, UdpPacket packet) {
				processReceivedPacket(udp,packet);
			}
			public void onServiceTerminated(UdpProvider udp, Exception error) {
				processServiceTerminated(udp,error);
			}
		};
		udp_provider=new UdpProvider(socket,this_udp_provider_listener);
	}


	/** Gets protocol type */ 
	public String getProtocol() {
		return PROTO_UDP;
	}


	/** Gets port */ 
	public int getLocalPort() {
		try {  return udp_provider.getUdpSocket().getLocalPort();  } catch (Exception e) {  return 0;  }
		
	}


	/** Sets transport listener */
	public void setListener(SipTransportListener listener) {
		this.listener=listener;
	}


	/** From SipTransport. Sends a SipMessage to the given remote address and port, with a given TTL.
	  * <p>
	  * If the transport protocol is Connection Oriented (CO), this method first looks for a proper active
	  * connection; if no active connection is found, a new connection is opened.
	  * <p>
	  * If the transport protocol is Connection Less (CL) the message is simply sent to the remote point.
	  * @return Returns the id of the used connection for CO transport, or null for CL transport. */      
	public ConnectionId sendMessage(SipMessage msg, IpAddress dest_ipaddr, int dest_port, int ttl) throws IOException {
		if (udp_provider!=null) {
			byte[] data=msg.getBytes();
			UdpPacket packet=new UdpPacket(data,data.length);
			// if (ttl>0 && multicast_address) do something?
			packet.setIpAddress(dest_ipaddr);
			packet.setPort(dest_port);
			udp_provider.send(packet);
		}
		return null;
	}


	/** Stops running */
	public void halt() {
		if (udp_provider!=null) udp_provider.halt();
	}


	/** Gets a String representation of the Object */
	public String toString() {
		if (udp_provider!=null) return udp_provider.toString();
		else return null;
	}


	//************************* Callback methods *************************
	
	/** When a new UDP datagram is received. */
	private void processReceivedPacket(UdpProvider udp, UdpPacket packet) {
		if (ByteUtils.match(packet.getData(),packet.getOffset(),packet.getLength(),PING,0,PING.length)) {
			if (udp_provider!=null)
			try {  udp_provider.send(new UdpPacket(PONG,packet.getIpAddress(),packet.getPort()));  } catch (Exception e) {};
		}
		else
		if (ByteUtils.match(packet.getData(),packet.getOffset(),packet.getLength(),PONG,0,PONG.length)) {
			// do something..
		}
		else {
			SipMessage msg=new SipMessage(packet.getData(),packet.getOffset(),packet.getLength());
			msg.setRemoteAddress(packet.getIpAddress().toString());
			msg.setRemotePort(packet.getPort());
			msg.setTransportProtocol(PROTO_UDP);
			if (listener!=null) listener.onReceivedMessage(this,msg);
		}
	}   


	/** When DatagramService stops receiving UDP datagrams. */
	private void processServiceTerminated(UdpProvider udp, Exception error) {
		if (listener!=null) listener.onTransportTerminated(this,error);
		UdpSocket socket=udp.getUdpSocket();
		if (socket!=null) try { socket.close(); } catch (Exception e) {}
		this.udp_provider=null;
		this.listener=null;
	}   

}
