/*
 * Copyright (C) 2005 Luca Veltri - University of Parma - Italy
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


/** RtpSocket implements a RTP socket for receiving and sending RTP packets. 
  * <p> RtpSocket is associated to a UdpSocket used to send and/or receive RtpPackets.
  */
public class RtpSocket {
	
	/** UDP socket */
	UdpSocket udp_socket;
		  
	/** Remote destination UDP socket address */
	SocketAddress remote_dest_soaddr;
	
	/** Remote source UDP socket address */
	SocketAddress remote_source_soaddr;

	/** Whether outgoing RTP packets have to be sent to the same address where incoming RTP packets come from (symmetric RTP mode) */
	boolean symmetric_rtp=true;



	/** Creates a new RTP socket (only receiver).
	  * @param udp_socket the local UDP socket used for receiving RTP packets */
	public RtpSocket(UdpSocket udp_socket) {
		this.udp_socket=udp_socket;
		this.remote_dest_soaddr=null;
	}

	/** Creates a new RTP socket (sender and receiver). 
	  * @param udp_socket the local UDP socket  used for sending and receiving RTP packets
	  * @param remote_dest_soaddr the remote destination UDP socket address where RTP packet are sent to */
	public RtpSocket(UdpSocket udp_socket, SocketAddress remote_dest_soaddr) {
		this.udp_socket=udp_socket;
		this.remote_dest_soaddr=remote_dest_soaddr;
	}


	/** Sets symmetric RTP mode.
	  * In symmetric RTP mode outgoing RTP packets are sent to the same address where incoming RTP packets come from (symmetric RTP mode). 
	  * @param symmetric_rtp whether working in symmetric RTP mode */
	public void setSymmetricRtpMode(boolean symmetric_rtp) {
		this.symmetric_rtp=symmetric_rtp;
	}

	/** Changes the remote destination socket address.   
	  * @param remote_dest_soaddr the remote UDP socket address where RTP packet are sent to */
	public void setRemoteDestSoAddress(SocketAddress remote_dest_soaddr) {
		this.remote_dest_soaddr=remote_dest_soaddr;
	}

	/** Gets the UDP socket.
	  * @return the local UDP socket */
	public UdpSocket getUdpSocket() {
		return udp_socket;
	}

	/** Gets the remote source socket address.
	  * @return the remote UDP socket address where RTP packets are sent from */
	public SocketAddress getRemoteSourceSoAddress() {
		return remote_source_soaddr;
	}

	/** Receives a RTP packet from this socket.
	  * @param rtp_packet RTP packet that will containing the received packet */
	public void receive(RtpPacket rtp_packet) throws IOException {
		UdpPacket udp_packet=new UdpPacket(rtp_packet.buffer,rtp_packet.buffer.length);
		udp_socket.receive(udp_packet);
		rtp_packet.length=udp_packet.getLength();
		IpAddress remote_ipaddr=udp_packet.getIpAddress();
		int remote_port=udp_packet.getPort();
		if (remote_source_soaddr==null || !remote_source_soaddr.getAddress().equals(remote_ipaddr) || remote_source_soaddr.getPort()!=remote_port) remote_source_soaddr=new SocketAddress(remote_ipaddr,remote_port);
		if (symmetric_rtp) remote_dest_soaddr=remote_source_soaddr;
	}
	
	/** Sends a RTP packet from this socket      
	  * @param rtp_packet RTP packet to be sent */
	public void send(RtpPacket rtp_packet) throws IOException {
		if (remote_dest_soaddr==null) {
			if (!symmetric_rtp) throw new IOException("Null destination address");
			return;
		}
		// else
		UdpPacket udp_packet=new UdpPacket(rtp_packet.buffer,rtp_packet.offset,rtp_packet.length);
		udp_packet.setIpAddress(remote_dest_soaddr.getAddress());
		udp_packet.setPort(remote_dest_soaddr.getPort());
		udp_socket.send(udp_packet);
	}

	/** Closes this socket. */      
	public void close() {
		//udp_socket.close();
	}

}
