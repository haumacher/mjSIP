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



import org.zoolu.net.SocketAddress;
import org.zoolu.net.UdpPacket;
import org.zoolu.net.UdpProvider;
import org.zoolu.net.UdpProviderListener;
import org.zoolu.net.UdpSocket;
import org.zoolu.util.ExceptionPrinter;
import org.zoolu.util.LogLevel;
// logs
import org.zoolu.util.Logger;
import org.zoolu.util.Timer;
import org.zoolu.util.TimerListener;



/**
 * SymmetricUdpRelay implements a symmetric bidirectional UDP relay system.
 */
public class SymmetricUdpRelay implements UdpProviderListener, TimerListener {
	
	/** Logger */
	protected Logger logger=null;

	/** SymmetricUdpRelay listener */
	protected SymmetricUdpRelayListener listener;
	
	/** Left side udp interface. */
	protected UdpProvider left_udp=null;  
	/** Right side udp interface. */
	protected UdpProvider right_udp=null;
	
	/** Left port. */
	//protected int left_port;  
	/** Right port. */
	//protected int right_port;

	/** Left peer address. */
	protected SocketAddress left_soaddr;
	/** Right peer address. */
	protected SocketAddress right_soaddr;

	/** Maximum time that the SymmetricUdpRelay remains active without receiving UDP datagrams (in milliseconds) */
	protected long relay_time=0;

	/** Absolute time when the SymmetricUdpRelay should expire (if no packet is received meanwhile). */
	protected long expire_time=0;

	/** Timer that fires whether the SymmetricUdpRelay must expire. */
	protected Timer timer=null;

	/** Whether the SymmetricUdpRelay is running. */
	//protected boolean is_running=true;

	/** Last change time of left soaddr (in milliseconds) */
	protected long last_left_change;

	/** Last change time of right soaddr (in milliseconds) */
	protected long last_right_change;



	/** Costructs a new SymmetricUdpRelay. */
	protected SymmetricUdpRelay() {}


	/** Costructs a new SymmetricUdpRelay. */
	/*public SymmetricUdpRelay(UdpSocket left_socket, SocketAddress left_soaddr, UdpSocket right_socket, SocketAddress right_soaddr, long relay_time, Logger logger, SymmetricUdpRelayListener listener) {
		init(left_socket,left_soaddr,right_socket,right_soaddr,relay_time,log,listener);
	}*/
	
	
	/** Costructs a new SymmetricUdpRelay. */
	public SymmetricUdpRelay(int left_port, SocketAddress left_soaddr, int right_port, SocketAddress right_soaddr, long relay_time, Logger logger, SymmetricUdpRelayListener listener) {
		init(left_port,left_soaddr,right_port,right_soaddr,relay_time,logger,listener);
	}


	/** Initializes the SymmetricUdpRelay. */
	private void init(int left_port, SocketAddress left_soaddr, int right_port, SocketAddress right_soaddr, long relay_time, Logger logger, SymmetricUdpRelayListener listener) {
		this.logger=logger;
		//this.left_port=left_port;
		this.left_soaddr=left_soaddr;
		//this.right_port=right_port;
		this.right_soaddr=right_soaddr;
		this.relay_time=relay_time;
		this.listener=listener;

		try {
			left_udp=new UdpProvider(new UdpSocket(left_port),0,this);
			log(LogLevel.INFO,"udp interfce: "+left_udp.toString()+" started");    
	
			right_udp=new UdpProvider(new UdpSocket(right_port),0,this);
			log(LogLevel.INFO,"udp interfce: "+right_udp.toString()+" started");
		}   
		catch (Exception e) {
			log(LogLevel.INFO,e);
		}
	
		if (relay_time>0) {
			long timer_time=relay_time/2;
			expire_time=System.currentTimeMillis()+relay_time;
			timer=new Timer(timer_time,this);
			timer.start();
		}
		last_left_change=last_right_change=System.currentTimeMillis();
	}

	
	/** Returns the left UdpSocket */ 
	/*public UdpSocket getLeftSocket() {
		return left_socket;
	}*/


	/** Returns the right UdpSocket */ 
	/*public UdpSocket getRightSocket() {
		return right_socket;
	}*/


	/** Whether the UDP receivers are running */
	public boolean isRunning() {
		//return is_running;
		if (left_udp!=null && left_udp.isRunning()) return true;
		if (right_udp!=null && right_udp.isRunning()) return true;
		return false;
	}


	/** Stops the SymmetricUdpRelay */
	public void halt() {
		if (left_udp!=null) left_udp.halt();
		if (right_udp!=null) right_udp.halt();
	}


	/** Gets the left peer SocketAddress. */
	public SocketAddress getLeftSoAddress() {
		return left_soaddr;
	}
	
	/** Sets a new left peer SocketAddress. */
	public void setLeftSoAddress(SocketAddress left_soaddr) {
		log(LogLevel.INFO,"left soaddr "+this.left_soaddr+" becomes "+left_soaddr);
		this.left_soaddr=left_soaddr;
		last_left_change=System.currentTimeMillis();
	}
	
	/** Gets the right peer SocketAddress. */
	public SocketAddress getRightSoAddress() {
		return right_soaddr;
	}

	/** Sets a new right peer SocketAddress. */
	public void setRightSoAddress(SocketAddress right_soaddr) {
		log(LogLevel.INFO,"right soaddr "+this.right_soaddr+" becomes "+right_soaddr);
		this.right_soaddr=right_soaddr;
		last_right_change=System.currentTimeMillis();
	}


	/** Gets the time of the last change of left soaddr. */
	public long getLastLeftChangeTime() {
		return last_left_change;
	}

	/** Gets the time of the last change of left soaddr. */
	public long getLastRightChangeTime() {
		return last_right_change;
	}



	/** When receiving a new packet. */
	public void onReceivedPacket(UdpProvider udp_service, UdpPacket packet) {
		
		//if (packet.getLength()<=2) return; // discard the packet
		
		// postpone the expire time 
		if (relay_time>0) expire_time=System.currentTimeMillis()+relay_time;
			
		// set addresses for outgoing packet, and check whether remote addresses are changed for incoming packet
		SocketAddress src_soaddr=new SocketAddress(packet.getIpAddress(),packet.getPort());
		SocketAddress dest_soaddr=null;
		UdpProvider udp=null;
		if (udp_service==left_udp) {
			// set the actual dest address and src socket for outgoing packet
			dest_soaddr=right_soaddr;
			udp=right_udp;    
			// check whether the source address and port are changed for incoming packet
			if (!left_soaddr.equals(src_soaddr)) {
				//printLog("left peer addr "+left_soaddr+" changed to "+src_soaddr,LogWriter.LEVEL_HIGH);
				if (listener!=null) listener.onSymmetricUdpRelayLeftPeerChanged(this,src_soaddr);
			}
		}
		else
		if (udp_service==right_udp) {
			// set the actual dest address and src socket for outgoing packet
			dest_soaddr=left_soaddr;
			udp=left_udp;
			// check whether the source address and port are changed for incoming packet
			if (!right_soaddr.equals(src_soaddr)) {
				//log(LogLevel.INFO,"right peer addr "+right_soaddr+" changed to "+src_soaddr);
				if (listener!=null) listener.onSymmetricUdpRelayRightPeerChanged(this,src_soaddr);
			}
		}
		// relay
		if (udp!=null && dest_soaddr!=null) {
			packet.setIpAddress(dest_soaddr.getAddress());
			packet.setPort(dest_soaddr.getPort());
			try {
				udp.send(packet);
			}
			catch (java.io.IOException e) { }
		}
	}


	/** When UdpProvider stops receiving UDP datagrams. */
	public void onServiceTerminated(UdpProvider udp_service, Exception error) {
		log(LogLevel.INFO,"udp "+udp_service.toString()+" terminated");
		if (error!=null) log(LogLevel.DEBUG,"udp "+udp_service.toString()+" exception:\n"+error.toString());
		udp_service.getUdpSocket().close();
		if (!isRunning() && listener!=null) listener.onSymmetricUdpRelayTerminated(this);
	}


	/** When the Timer exceeds */
	public void onTimeout(Timer t) {
		long now=System.currentTimeMillis();
		if (now<expire_time) {
			long timer_time=relay_time/2;
			timer=new Timer(timer_time,this);
			timer.start();
		}
		else {
			timer=null;
			log(LogLevel.INFO,"relay inactive for more than "+relay_time+"ms");
			halt();
		}
	}


	/** Gets a String representation of the Object */
	public String toString() {
		return left_soaddr+"<-->"+left_udp.getUdpSocket().getLocalPort()+"[--]"+right_udp.getUdpSocket().getLocalPort()+"<-->"+right_soaddr;
	}


	// ****************************** Logs *****************************

	/** Adds a new string to the default Log */
	private void log(LogLevel level, String str) {
		if (logger!=null) logger.log(level,"SymmetricUdpRelay: "+str);  
	}

	/** Adds the Exception message to the default Log */
	protected void log(LogLevel level, Exception e) {
		log(level,"Exception: "+ExceptionPrinter.getStackTraceOf(e));
	}

}
