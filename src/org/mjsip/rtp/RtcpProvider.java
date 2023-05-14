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
import org.zoolu.net.UdpProvider;
import org.zoolu.net.UdpProviderListener;
import org.zoolu.net.UdpSocket;


/** RtcpProvider implements the RTCP service for receiving and sending RTP control (RTCP) packets. 
  */
public class RtcpProvider implements UdpProviderListener {
	
	/** UDP */
	UdpProvider udp;
	
	/** Remote destination socket address */
	SocketAddress remote_dest_soaddr=null;

	/** Remote source socket address */
	SocketAddress remote_source_soaddr=null;

	/** RtcpProvider listener */
	RtcpProviderListener listener;

	/** Whether outgoing RTCP packets have to be sent to the same address where incoming RTCP packets come from (symmetric RTCP mode) */
	boolean symmetric_rtcp=false;



	/** Creates a new RTCP (receiver only, or symmetric RTCP). 
	  * @param udp_socket the local UDP socket used for receiving RTCP packets
	  * @param listener the RTCP provider listener */
	public RtcpProvider(UdpSocket udp_socket, RtcpProviderListener listener) {
		this.udp=new UdpProvider(udp_socket,this);
		this.listener=listener;
	}

	/** Creates a new RTCP (sender and receiver). 
	  * @param udp_socket the local UDP socket used for sending and receiving RTCP packets
	  * @param remote_dest_soaddr the remote UDP socket address where RTCP packet are sent to
	  * @param listener the RTCP provider listener */
	public RtcpProvider(UdpSocket udp_socket, SocketAddress remote_dest_soaddr, RtcpProviderListener listener) {
		this.udp=new UdpProvider(udp_socket,this);
		this.remote_dest_soaddr=remote_dest_soaddr;
		this.listener=listener;
	}


	/** Sets symmetric RTCP mode.
	  * In symmetric RTCP mode outgoing RTCP packets are sent to the source address of incoming RTCP packets. 
	  * @param symmetric_rtcp whether working in symmetric RTCP mode */
	public void setSymmetricRtcpMode(boolean symmetric_rtcp) {
		this.symmetric_rtcp=symmetric_rtcp;
	}

	/** Whether outgoing RTCP packets are sent to the same address where the incoming RTCP packets come from (symmetric RTCP mode). 
	  * @return true if symmetric RTCP mode is on */
	public boolean getSymmetricRtcpMode() {
		return symmetric_rtcp;
	}

	/** Changes the remote destination socket address.   
	  * @param remote_dest_soaddr the remote UDP socket address where RTCP packet are sent to */
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

	/** Sends a RTCP compound packet from this socket.   
	  * @param rcomp_packet RTCP compound packet to be sent */
	public void send(RtcpCompoundPacket rcomp_packet) throws IOException {
		send(rcomp_packet.buffer,rcomp_packet.offset,rcomp_packet.length);
	}

	/** Sends a RTCP packet from this socket.   
	  * @param rtcp_packet RTCP packet to be sent */
	public void send(RtcpPacket rtcp_packet) throws IOException {
		send(rtcp_packet.buffer,rtcp_packet.offset,rtcp_packet.getPacketLength());
	}

	/** Sends a RTCP packet from this socket.   
	  * @param buf buffer containing the packet to be sent
	  * @param off the offset of the packet within the buffer
	  * @param len the length of the packet */
	private void send(byte[] buf, int off, int len) throws IOException {
		if (remote_dest_soaddr==null) {
			if (!symmetric_rtcp) throw new IOException("Null destination address");
			return;
		}
		// else
		UdpPacket udp_packet=new UdpPacket(buf,off,len);
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
		RtcpCompoundPacket rcomp_packet=new RtcpCompoundPacket(udp_packet.getData(),udp_packet.getOffset(),udp_packet.getLength());
		IpAddress remote_ipaddr=udp_packet.getIpAddress();
		int remote_port=udp_packet.getPort();
		if (remote_source_soaddr==null || !remote_source_soaddr.getAddress().equals(remote_ipaddr) || remote_source_soaddr.getPort()!=remote_port) remote_source_soaddr=new SocketAddress(remote_ipaddr,remote_port);
		if (symmetric_rtcp) remote_dest_soaddr=remote_source_soaddr;
		RtcpPacket[] rtcp_packets=rcomp_packet.getRtcpPackets();
		if (listener!=null) for (int i=0; i<rtcp_packets.length; i++) listener.onReceivedPacket(this,rtcp_packets[i]);
	}


	/** From UdpProviderListener. When UdpProvider terminates. */
	public void onServiceTerminated(UdpProvider udp, Exception error) {
		if (listener!=null) listener.onServiceTerminated(this,error);
	}   

}
