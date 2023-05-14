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


import java.io.IOException;

import org.zoolu.net.IpAddress;
import org.zoolu.net.SocketAddress;
import org.zoolu.net.UdpPacket;
import org.zoolu.net.UdpSocket;


/** RtcpSocket implements a RTCP socket for receiving and sending RTCP packets. 
  * <p> RtcpSocket is associated to a UdpSocket used to send and/or receive RtcpPackets.
  */
public class RtcpSocket {
	
	/** UDP socket */
	UdpSocket udp_socket;
		  
	/** Remote destination UDP socket address */
	SocketAddress remote_dest_soaddr;
	
	/** Remote source UDP socket address */
	SocketAddress remote_source_soaddr;

	/** Whether outgoing RTCP packets have to be sent to the same address where incoming RTCP packets come from (symmetric RTCP mode) */
	boolean symmetric_rtcp=false;



	/** Creates a new RTCP socket (only receiver).
	  * @param udp_socket the local UDP socket used for receiving RTCP packets */
	public RtcpSocket(UdpSocket udp_socket) {
		this.udp_socket=udp_socket;
		this.remote_dest_soaddr=null;
	}

	/** Creates a new RTCP socket (sender and receiver).
	  * @param udp_socket the local UDP socket  used for sending and receiving RTCP packets
	  * @param remote_dest_soaddr the remote destination UDP socket address where RTCP packet are sent to */
	public RtcpSocket(UdpSocket udp_socket, SocketAddress remote_dest_soaddr) {
		this.udp_socket=udp_socket;
		this.remote_dest_soaddr=remote_dest_soaddr;
	}


	/** Sets symmetric RTCP mode.
	  * In symmetric RTCP mode outgoing RTCP packets are sent to the same address where incoming RTCP packets come from (symmetric RTCP mode). 
	  * @param symmetric_rtcp whether working in symmetric RTCP mode */
	public void setSymmetricRtpMode(boolean symmetric_rtcp) {
		this.symmetric_rtcp=symmetric_rtcp;
	}

	/** Changes the remote destination socket address.   
	  * @param remote_dest_soaddr the remote UDP socket address where RTCP packet are sent to */
	public void setRemoteDestSoAddress(SocketAddress remote_dest_soaddr) {
		this.remote_dest_soaddr=remote_dest_soaddr;
	}

	/** Gets the UDP socket.
	  * @return the local UDP socket */
	public UdpSocket getUdpSocket() {
		return udp_socket;
	}

	/** Gets the remote source socket address.
	  * @return the remote UDP socket address where RTCP packets are sent from */
	public SocketAddress getRemoteSourceSoAddress() {
		return remote_source_soaddr;
	}


	/** Receives a RTCP compound packet from this socket.
	  * @param rcomp_packet RTCP compound packet that will containing the received packet */
	public void receive(RtcpCompoundPacket rcomp_packet) throws IOException {
		UdpPacket udp_packet=new UdpPacket(rcomp_packet.buffer,rcomp_packet.offset,rcomp_packet.buffer.length-rcomp_packet.offset);
		udp_socket.receive(udp_packet);
		rcomp_packet.length=udp_packet.getLength();
		IpAddress remote_ipaddr=udp_packet.getIpAddress();
		int remote_port=udp_packet.getPort();
		if (remote_source_soaddr==null || !remote_source_soaddr.getAddress().equals(remote_ipaddr) || remote_source_soaddr.getPort()!=remote_port) remote_source_soaddr=new SocketAddress(remote_ipaddr,remote_port);
		if (symmetric_rtcp) remote_dest_soaddr=remote_source_soaddr;
	}

	/** Sends a RTCP compound packet from this socket.     
	  * @param rcomp_packet RTCP compound packet to be sent */
	public void send(RtcpCompoundPacket rcomp_packet) throws IOException {
		if (remote_dest_soaddr==null) {
			if (!symmetric_rtcp) throw new IOException("Null destination address");
			return;
		}
		// else
		UdpPacket udp_packet=new UdpPacket(rcomp_packet.getPacketBuffer(),rcomp_packet.getPacketOffset(),rcomp_packet.getPacketLength());
		udp_packet.setIpAddress(remote_dest_soaddr.getAddress());
		udp_packet.setPort(remote_dest_soaddr.getPort());
		udp_socket.send(udp_packet);
	}

	/** Sends a RTCP packet from this socket.      
	  * @param rtcp_packet RTCP packet to be sent */
	public void send(RtcpPacket rtcp_packet) throws IOException {
		if (remote_dest_soaddr==null) {
			if (!symmetric_rtcp) throw new IOException("Null destination address");
			return;
		}
		// else
		UdpPacket udp_packet=new UdpPacket(rtcp_packet.getPacketBuffer(),rtcp_packet.getPacketOffset(),rtcp_packet.getPacketLength());
		udp_packet.setIpAddress(remote_dest_soaddr.getAddress());
		udp_packet.setPort(remote_dest_soaddr.getPort());
		udp_socket.send(udp_packet);
	}

	/** Closes this socket. */      
	public void close() {
		//udp_socket.close();
	}

}
