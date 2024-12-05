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
import org.zoolu.net.UdpSocket;



/**
  * SymmetricRegulatedUdpRelay implements a shaped symmetric bidirectional UDP relay system.
  */
public class SymmetricRegulatedUdpRelay extends SymmetricUdpRelay {
	
	private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(SymmetricRegulatedUdpRelay.class);

	/** Minimum inter-packet departure time */
	long inter_time=0;

	/** Costructs a new SymmetricRegulatedUdpRelay. 
	 * @param scheduler */
	public SymmetricRegulatedUdpRelay(Scheduler scheduler, int left_port, SocketAddress left_soaddr, int right_port,
			SocketAddress right_soaddr, long relay_time, long inter_time, SymmetricUdpRelayListener listener) {
		super(scheduler);
		init(left_port, left_soaddr, right_port, right_soaddr, relay_time, inter_time, listener);
	}


	/** Initializes the SymmetricUdpRelay. */
	private void init(int left_port, SocketAddress left_soaddr, int right_port, SocketAddress right_soaddr,
			long relay_time, long inter_time, SymmetricUdpRelayListener listener) {
		//this.left_port=left_port;
		this.left_soaddr=left_soaddr;
		//this.right_port=right_port;
		this.right_soaddr=right_soaddr;
		this.relay_time=relay_time;
		this.listener=listener;
		this.inter_time=inter_time;

		try {
			//left_udp=new UdpProvider(new OutputRegulatedUdpSocket(left_port,inter_time),0,this);
			left_udp=new InputRegulatedUdpProvider(new UdpSocket(left_port),0,inter_time,this);
			LOG.info("udp interfce: {} started", left_udp);    
			LOG.info("udp interfce regulated with {} millisecs of minimum inter-packet departure time", inter_time);    
	
			//right_udp=new UdpProvider(new OutputRegulatedUdpSocket(right_port,inter_time),0,this);
			right_udp=new InputRegulatedUdpProvider(new UdpSocket(right_port),0,inter_time,this);
			LOG.info("udp interfce: {} started", right_udp);
			LOG.info("udp interfce regulated with {} millisecs of minimum inter-packet departure time", inter_time);    
		}   
		catch (Exception e) {
			LOG.info("Exception.", e);
		}
	
		if (relay_time>0) {
			long timer_time=relay_time/2;
			expire_time=System.currentTimeMillis()+relay_time;
			timer=scheduler().schedule(timer_time, this::onTimeout);
		}
		last_left_change=last_right_change=System.currentTimeMillis();
	}

}
