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


//import java.net.InetAddress;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;

import org.slf4j.LoggerFactory;


/** TcpConnection provides a TCP connection oriented transport service.
  */
public class TcpConnection extends Thread {
	
	private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(TcpConnection.class);

	/** The reading buffer size */
	static final int BUFFER_SIZE=65535;

	/** Default value for the maximum time that the tcp connection can remain active after been halted (in milliseconds) */
	public static final int DEFAULT_SOCKET_TIMEOUT=2000; // 2sec 

	/** The TCP socket */ 
	TcpSocket socket;  

	/** Maximum time that the connection can remain active after been halted (in milliseconds) */
	int socket_timeout;

	/** Maximum time that the connection remains active without receiving data (in milliseconds) */
	long alive_time; 

	/** The InputStream */
	InputStream istream;
	
	/** The OutputStream */
	OutputStream ostream;

	/** Whether it has been halted */
	boolean stop; 

	/** Whether it is running */
	boolean is_running; 

	/** TcpConnection listener */
	TcpConnectionListener listener;



	/** Constructs a new TcpConnection.*/
	public TcpConnection(TcpSocket socket, TcpConnectionListener listener) throws IOException {
		init(socket,0,listener);
		start();
	}


	/** Constructs a new TcpConnection.*/
	public TcpConnection(TcpSocket socket, long alive_time, TcpConnectionListener listener) throws IOException {
		init(socket,alive_time,listener);
		start();
	}


	/** Inits the TcpConnection. */
	private void init(TcpSocket socket, long alive_time, TcpConnectionListener listener) throws IOException {
		this.listener=listener;
		this.socket=socket;
		this.socket_timeout=DEFAULT_SOCKET_TIMEOUT;
		this.alive_time=alive_time;
		this.stop=false; 
		this.is_running=true; 

		istream=new BufferedInputStream(socket.getInputStream());
		ostream=new BufferedOutputStream(socket.getOutputStream());
	}


	/** Whether the service is running. */
	public boolean isRunning() {
		return is_running;
	}


	/** Gets the TcpSocket. */ 
	public TcpSocket getSocket() {
		return socket;
	}


	/** Gets the remote IP address. */
	public IpAddress getRemoteAddress() {
		return socket.getAddress();
	}

	
	/** Gets the remote port. */
	public int getRemotePort() {
		return socket.getPort();
	}


	/** Gets the local address. */
	public IpAddress getLocalAddress() {
		return socket.getLocalAddress();
	}


	/** Gets the local port. */
	public int getLocalPort() {
		return socket.getLocalPort();
	}


	/** Stops running. */
	public void halt() {
		if (!stop) {
			LOG.debug("Stopping TCP connection to: {}", socket);
			stop = true;
		}
	}


	/** Sends data. */
	public void send(byte[] buff, int offset, int len)  throws IOException {
		if (!stop && ostream!=null) {
			ostream.write(buff,offset,len);
			ostream.flush();

			LOG.debug("Sent {} bytes to: {}", len , socket);
		}
	}


	/** Sends data. */
	public void send(byte[] buff)  throws IOException {
		send(buff,0,buff.length);
	}


	/** Runs the tcp receiver. */
	@Override
	public void run() {
		LOG.debug("Starting connection handler for: {}", socket);
		
		byte[] buff=new byte[BUFFER_SIZE];
		long expire=0;
		if (alive_time>0) expire=System.currentTimeMillis()+alive_time;
		IOException error = null;
		try {
			socket.setSoTimeout(socket_timeout);         
			// loop
			while(!stop) {
				int len=0;
				if (istream!=null) {
					try {
						len=istream.read(buff);
					}
					catch (InterruptedIOException ie) {
						if (alive_time>0 && System.currentTimeMillis()>expire) halt();
						continue;
					}
				}
				if (len<0) {
					LOG.debug("Connection closed: {}", socket);
					stop=true;
				} else if (len > 0) {
					if (listener!=null) listener.onReceivedData(this,buff,len);
					if (alive_time>0) expire=System.currentTimeMillis()+alive_time;
				}
			}
		}
		catch (IOException e) {
			LOG.info("TCP connection terminated: {}", e.getMessage());
			error=e;
			stop=true;
		}
		is_running=false;
		if (istream!=null) try {  istream.close();  } catch (Exception e) {}
		if (ostream!=null) try {  ostream.close();  } catch (Exception e) {}
		if (listener!=null) listener.onConnectionTerminated(this,error);
		listener=null;

		LOG.debug("Connection handler terminated for: {}", socket);
	} 

 
	/** Gets a String representation of the Object. */
	@Override
	public String toString() {
		return "tcp:"+socket.getLocalAddress()+":"+socket.getLocalPort()+"<->"+socket.getAddress()+":"+socket.getPort();
	}

}
