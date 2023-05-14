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

package org.zoolu.net;


import java.net.DatagramPacket;
import java.net.DatagramSocket;


/** UdpSocket provides a uniform interface to UDP transport protocol,
  * regardless J2SE or J2ME is used.
  */
public class UdpSocket {
	

	/** DatagramSocket */
	DatagramSocket socket;

	/** Sender packet counter */
	long sender_packet_count=0;   

	/** Sender octect counter */
	long sender_octect_count=0;   

	/** Receiver packet counter */
	long receiver_packet_count=0;   

	/** Receiver octect counter */
	long receiver_octect_count=0;   



	/** Creates a new void UdpSocket */ 
	protected UdpSocket() {
		socket=null;
	}

	/** Creates a new UdpSocket */ 
	protected UdpSocket(DatagramSocket sock) {
		socket=sock;
	}

	/** Creates a new UdpSocket */ 
	public UdpSocket(int port) throws java.net.SocketException {
		socket=new DatagramSocket(port);
	}

	/** Creates a new UdpSocket */ 
	public UdpSocket(int port, IpAddress ipaddr) throws java.net.SocketException {
		socket=new DatagramSocket(port,ipaddr.getInetAddress());
	}
	
	/** Closes this datagram socket. */
	public void close() {
		socket.close();
	}

	/** Gets the local address to which the socket is bound. */
	public IpAddress getLocalAddress() {
		return new IpAddress(socket.getInetAddress());
	}
	
	/** Gets the port number on the local host to which this socket is bound. */
	public int getLocalPort() {
		return socket.getLocalPort();
	}
	
	/** Gets the socket timeout. */
	public int getSoTimeout() throws java.net.SocketException {
		return socket.getSoTimeout();
	}
	
	/** Enables/disables socket timeout with the specified timeout, in milliseconds. */
	public void setSoTimeout(int timeout) throws java.net.SocketException {
		socket.setSoTimeout(timeout);
	}

	/** Receives a datagram packet from this socket. */
	public void receive(UdpPacket pkt) throws java.io.IOException {
		DatagramPacket dgram=pkt.getDatagramPacket();
		socket.receive(dgram);
		pkt.setDatagramPacket(dgram);
		receiver_packet_count++;
		receiver_octect_count+=pkt.getLength();
	}
	
	/** Sends an UDP packet from this socket. */ 
	public void send(UdpPacket pkt) throws java.io.IOException {
		socket.send(pkt.getDatagramPacket());
		sender_packet_count++;
		sender_octect_count+=pkt.getLength();
	}
	
	/** Converts this object to a String. */
	public String toString() {
		//return socket.toString();
		return "UDP:"+getLocalAddress()+":"+getLocalPort();
	}


	/** Gets the total number of sent packets. */
	public long getSenderPacketCounter() {
		return sender_packet_count;
	}

	/** Gets the total number of sent octects. */
	public long getSenderOctectCounter() {
		return sender_octect_count;
	}

	/** Gets the total number of received packets. */
	public long getReceiverPacketCounter() {
		return receiver_packet_count;
	}

	/** Gets the total number of received octects. */
	public long getReceiverOctectCounter() {
		return receiver_octect_count;
	}

	/** Gets the buffer size used by the platform for input on this UDP socket.
	 * @return the receiver buffer size */
	public long getReceiverBufferSize() throws java.net.SocketException {
		return socket.getReceiveBufferSize();
	}

	/** Sets the buffer size used by the platform for input on this UDP socket.
	 * @param size the receiver buffer size */
	public void setReceiverBufferSize(int size) throws java.net.SocketException {
		socket.setReceiveBufferSize(size);
	}

}
