/*
 * Copyright (C) 2013 Luca Veltri - University of Parma - Italy
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
import org.zoolu.net.UdpProvider;
import org.zoolu.net.UdpProviderListener;
import org.zoolu.net.UdpSocket;


/** RtpProvider implements the RTP service for receiving and sending RTP packets. 
  */
public class RtpProvider implements UdpProviderListener {
	
	/** UDP */
	UdpProvider udp;
	
	/** Remote destination UDP socket address */
	SocketAddress remote_dest_soaddr;
	
	/** Remote source UDP socket address */
	SocketAddress remote_source_soaddr;

	/** Whether outgoing RTP packets have to be sent to the same address where incoming RTP packets come from (symmetric RTP mode) */
	boolean symmetric_rtp=true;
	
	/** RtpProvider listener */
	RtpProviderListener listener;



	/** Creates a new RTP (receiver only, or symmetric RTP).
	  * @param udp_socket the local UDP socket used for receiving RTP packets
	  * @param listener the RTP provider listener */
	public RtpProvider(UdpSocket udp_socket, RtpProviderListener listener) {
		this.udp=new UdpProvider(udp_socket,this);
		this.remote_dest_soaddr=null;
		this.listener=listener;
	}

	/** Creates a new RTP (sender and receiver).
	  * @param udp_socket the local UDP socket  used for sending and receiving RTP packets
	  * @param remote_dest_soaddr the remote destination UDP socket address where RTP packet are sent to
	  * @param listener the RTP provider listener */
	public RtpProvider(UdpSocket udp_socket, SocketAddress remote_dest_soaddr, RtpProviderListener listener) {
		this.udp=new UdpProvider(udp_socket,this);
		this.remote_dest_soaddr=remote_dest_soaddr;
		this.listener=listener;
	}


	/** Sets symmetric RTP mode.
	  * In symmetric RTP mode outgoing RTP packets are sent to the same address where incoming RTP packets come from (symmetric RTP mode). 
	  * @param symmetric_rtp whether working in symmetric RTCP mode */
	public void setSymmetricRtpMode(boolean symmetric_rtp) {
		this.symmetric_rtp=symmetric_rtp;
	}

	/** Changes the remote destination socket address.   
	  * @param remote_dest_soaddr the remote UDP socket address where RTP packet are sent to */
	public void setRemoteDestSoAddress(SocketAddress remote_dest_soaddr) {
		this.remote_dest_soaddr=remote_dest_soaddr;
	}

	/** Gets the UDP provider.
	  * @return the local UDP provider */
	public UdpProvider getUdpProvider() {
		return udp;
	}

	/** Gets the remote source socket address.
	  * @return the remote UDP socket address where RTCP packet are sent from */
	public SocketAddress getRemoteSourceSoAddress() {
		return remote_source_soaddr;
	}

	/** Sends a RTP packet. 
	  * @param rtp_packet RTP packet to be sent */
	public void send(RtpPacket rtp_packet) throws IOException {
		if (remote_dest_soaddr==null) {
			if (!symmetric_rtp) throw new IOException("Null destination address");
			return;
		}
		// else
		UdpPacket udp_packet=new UdpPacket(rtp_packet.buffer,rtp_packet.length);
		udp_packet.setIpAddress(remote_dest_soaddr.getAddress());
		udp_packet.setPort(remote_dest_soaddr.getPort());
		udp.send(udp_packet);
	}

	/** Stops running. */
	public void halt() {
		udp.halt();
	}
 
	/** From UdpProviderListener. When a new UDP datagram is received. */
	public void onReceivedPacket(UdpProvider udp, UdpPacket udp_packet) {
		RtpPacket rtp_packet=new RtpPacket(udp_packet.getData(),udp_packet.getOffset(),udp_packet.getLength());
		IpAddress remote_ipaddr=udp_packet.getIpAddress();
		int remote_port=udp_packet.getPort();
		if (remote_source_soaddr==null || !remote_source_soaddr.getAddress().equals(remote_ipaddr) || remote_source_soaddr.getPort()!=remote_port) remote_source_soaddr=new SocketAddress(remote_ipaddr,remote_port);
		if (symmetric_rtp) remote_dest_soaddr=remote_source_soaddr;
		if (listener!=null) listener.onReceivedPacket(this,rtp_packet);
	}

	/** From UdpProviderListener. When UdpProvider terminates. */
	public void onServiceTerminated(UdpProvider udp, Exception error) {
		if (listener!=null) listener.onServiceTerminated(this,error);
	}   

}
