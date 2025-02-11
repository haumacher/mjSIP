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

package org.mjsip.server.sbc;



import org.mjsip.time.Scheduler;
import org.slf4j.LoggerFactory;
import org.zoolu.net.SocketAddress;
import org.zoolu.net.UdpPacket;
import org.zoolu.net.UdpProvider;
import org.zoolu.net.UdpSocket;



/**
 * InterceptingUdpRelay extends SymmetricUdpRelay by intercepting the relayed traffics and/or injecting new flows.
 */
public class InterceptingUdpRelay extends SymmetricUdpRelay {
	
	private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(InterceptingUdpRelay.class);

	/** Left side intercept udp interface. */
	UdpProvider left_intercept_udp=null;  
	/** Right side intercept udp interface. */
	UdpProvider right_intercept_udp=null;
	
	/** Left peer address. */
	SocketAddress left_intercept_soaddr;
	/** Right peer address. */
	SocketAddress right_intercept_soaddr;

	/** Whether injecting new UDP flows. */
	public boolean active_interception;

	/** Constructs a new InterceptingUdpRelay. */
	public InterceptingUdpRelay(Scheduler scheduler, int left_port, SocketAddress left_soaddr,
											int right_port, SocketAddress right_soaddr,
											int left_intercept_port, SocketAddress left_intercept_soaddr,
											int right_intercept_port, SocketAddress right_intercept_soaddr,
			boolean active_interception, long relay_time, SymmetricUdpRelayListener listener) {
		
		super(scheduler, left_port, left_soaddr, right_port, right_soaddr, relay_time, listener);
		this.left_intercept_soaddr=left_intercept_soaddr;
		this.right_intercept_soaddr=right_intercept_soaddr;
		this.active_interception=active_interception;

		try {
			left_intercept_udp=new UdpProvider(new UdpSocket(left_intercept_port),0,this);
			LOG.info("intercept udp interface: {} started", left_intercept_udp);    
	
			right_intercept_udp=new UdpProvider(new UdpSocket(right_intercept_port),0,this);
			LOG.info("intercept udp interface: {} started", right_intercept_udp);
		}   
		catch (Exception e) {
			LOG.info("Exception.", e);
		}
	}


	/** Stops the SymmetricUdpRelay */
	@Override
	public void halt() {
		super.halt();
		if (left_intercept_udp!=null) left_intercept_udp.halt();
		if (right_intercept_udp!=null) right_intercept_udp.halt();
	}


	/** When receiving a new packet. */
	@Override
	public void onReceivedPacket(UdpProvider udp_service, UdpPacket packet) {
		
		if (udp_service==left_udp) {
			// relay to the remote peer only in case of passive interception 
			if (!active_interception) {
				super.onReceivedPacket(udp_service,packet);
			}
			// relay to the intercepting node
			if (left_intercept_soaddr!=null) {
				packet.setIpAddress(left_intercept_soaddr.getAddress());
				packet.setPort(left_intercept_soaddr.getPort());
				try { left_intercept_udp.send(packet); } catch (java.io.IOException e) {}
			}
		}
		else
		if (udp_service==right_udp) {
			// relay to the remote peer only in case of passive interception 
			if (!active_interception) {
				super.onReceivedPacket(udp_service,packet);
			}
			// relay to the intercepting node
			if (right_intercept_soaddr!=null) {
				packet.setIpAddress(right_intercept_soaddr.getAddress());
				packet.setPort(right_intercept_soaddr.getPort());
				try { right_intercept_udp.send(packet); } catch (java.io.IOException e) {}
			}
		}
		else
		if (udp_service==left_intercept_udp) {
			// relay to the remote peer
			packet.setIpAddress(left_soaddr.getAddress());
			packet.setPort(left_soaddr.getPort());
			try { left_intercept_udp.send(packet); } catch (java.io.IOException e) { }
		}
		else
		if (udp_service==right_intercept_udp) {
			// relay to the remote peer
			packet.setIpAddress(right_soaddr.getAddress());
			packet.setPort(right_soaddr.getPort());
			try { left_intercept_udp.send(packet); } catch (java.io.IOException e) { }
		}
	}


	/** When UdpProvider stops receiving UDP datagrams. */
	@Override
	public void onServiceTerminated(UdpProvider udp_service, Exception error) {
		if (udp_service==left_intercept_udp || udp_service==right_intercept_udp) {
			LOG.info("udp interface: {} terminated", udp_service);
			udp_service.getUdpSocket().close();
		}
		else {
			super.onServiceTerminated(udp_service,error);
		}
	}


	/** Gets a String representation of the Object */
	@Override
	public String toString() {
		return left_soaddr+"<-->"+left_udp.getUdpSocket().getLocalPort()+"<-->"+left_intercept_soaddr+"] ["+right_intercept_soaddr+"<-->"+right_udp.getUdpSocket().getLocalPort()+"<-->"+right_soaddr;
	}

}