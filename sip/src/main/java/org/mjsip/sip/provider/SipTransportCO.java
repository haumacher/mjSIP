/*
 * Copyright (C) 2009 Luca Veltri - University of Parma - Italy
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
import java.util.Enumeration;
import java.util.Hashtable;

import org.mjsip.sip.message.SipMessage;
import org.zoolu.net.IpAddress;
import org.zoolu.net.SocketAddress;
import org.zoolu.util.ExceptionPrinter;
import org.zoolu.util.LogLevel;
import org.zoolu.util.Logger;



/** SipTransportCO is a generic Connection Oriented (CO) transport service for SIP.
  */
public abstract class SipTransportCO implements SipTransport/*, SipTransportConnectionListener*/ {
	

	/** Event Logger */
	protected Logger logger;

	/** Table of active connections (Hashtable of <code>ConnectionId</code>,<code>SipTransportConnection</code>) */
	protected Hashtable connections;

	/** SipTransport listener */
	protected SipTransportListener listener=null;
	
	/** SipTransportConnection listener */
	protected SipTransportConnectionListener this_conn_listener;

	/** Max number of (contemporary) open connections */
	int nmax_connections=0;

	/** Whether connections can be established only manually */
	boolean manual=false;

	/** Whether changing the Via protocol, sent-by, and port values of sending messages according to the transport connection */
	boolean force_sent_by=false;   




	/** Creates a new SipTransportCO */ 
	public SipTransportCO(int local_port, int nmax_connections, Logger logger) throws IOException {
		this.nmax_connections=nmax_connections;
		this.logger=logger;
		connections=new Hashtable();
		this_conn_listener=new SipTransportConnectionListener() {
			public void onReceivedMessage(SipTransportConnection conn, SipMessage msg) {
				processReceivedMessage(conn,msg);
			}
			public void onConnectionTerminated(SipTransportConnection conn, Exception error) {
				processConnectionTerminated(conn,error);
			}
		};
	}


	/** Sets manual connection only mode.
	  * If true, outgoing connections can be only established manually through the {@link #addConnection(IpAddress,int)} or {@link #addConnection(SipTransportConnection)} methods. */ 
	public void setManualConnectionMode(boolean manual) {
		this.manual=manual;
	}


	/** Whether setting the Via protocol, sent-by, and port values according to the transport connection ("force-sent-by" mode).
	  * @param force_sent_by whether changing Via protocol, sent-by, and port values according to the transport connection */ 
	public void setForceSentBy(boolean force_sent_by) {
		this.force_sent_by=force_sent_by;
	}


	/** Whether the "force-sent-by" mode is set.
	  * See method {@link #setForceSentBy(boolean)} for more details.
	  * @return true if "force-sent-by" mode is set */ 
	public boolean isForceSentBySet() {
		return force_sent_by;
	}


	/** Gets protocol type */ 
	abstract public String getProtocol();


	/** Creates a proper transport connection to the remote end-point. */
	abstract protected SipTransportConnection createTransportConnection(SocketAddress remote_soaddr) throws IOException;


	/** Creates a proper transport connection to the remote end-point. */
	//abstract protected SipTransportConnection createTransportConnection(int local_port, SocketAddress remote_soaddr)  throws IOException;


	/** Creates a proper transport connection to the remote end-point. */
	//abstract protected SipTransportConnection createTransportConnection(SocketAddress local_soaddr, SocketAddress remote_soaddr)  throws IOException;


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
		ConnectionId connection_id=new ConnectionId(getProtocol(),dest_ipaddr,dest_port);
		// BEGIN SYNCHRONIZATION
		synchronized (connections) {
			
			if (connections.containsKey(connection_id))
			try {
				log(LogLevel.DEBUG,"already active connection found for connection-id "+connection_id);
				SipTransportConnection conn=(SipTransportConnection)connections.get(connection_id);
				log(LogLevel.DEBUG,"sending data through already active connection "+conn);
				//conn.sendMessage(msg);
				sendMessage(conn,msg);
				return connection_id;
			}
			catch (Exception e) {
				log(LogLevel.INFO,e);
				log(LogLevel.DEBUG,"error using previous connection with connection-id "+connection_id);
				removeConnection(connection_id);
			}
			// no active connection
			log(LogLevel.DEBUG,"no active connection for "+connection_id);
			if (!manual) {
				// AUTOMATIC CONN MODE
				log(LogLevel.DEBUG,"open "+getProtocol()+" connection to "+dest_ipaddr+":"+dest_port);
				try {
					SipTransportConnection conn=addConnection(dest_ipaddr,dest_port);
					if (conn!=null) {
						log(LogLevel.DEBUG,"sending data through connection "+conn);
						//conn.sendMessage(msg);
						sendMessage(conn,msg);
						return new ConnectionId(conn);
					}
					else {
						log(LogLevel.DEBUG,"null connection: message has not been sent");
						return null;
					}
				}
				catch (Exception e) {
					log(LogLevel.DEBUG,e);
					return null;
				}
			}
			else {
				// MANUAL CONN MODE
				log(LogLevel.DEBUG,"only manual connections: message has not been sent");
				return null;
			}
		}
		// END SYNCHRONIZATION      
	}


	/** Sends the message <i>msg</i> using active connection specified by the connection-id of the given message.
	  * <p>
	  * If connection-id is null, or if no active connection is found for such connection-id,
	  * an IOException is thrown. */
	public SipTransportConnection sendMessageCO(SipMessage msg) throws IOException  {
		ConnectionId connection_id=msg.getConnectionId();
		if (connection_id==null) {
			throw new IOException("null connection-id");
		}
		// else
		// BEGIN SYNCHRONIZATION
		synchronized (connections) {
			
			if (!connections.containsKey(connection_id)) {
				throw new IOException("no active connection found matching connection-id "+connection_id);
			}
			// else
			log(LogLevel.DEBUG,"active connection found matching "+connection_id);
			SipTransportConnection conn=(SipTransportConnection)connections.get(connection_id);
			//conn.sendMessage(msg);
			sendMessage(conn,msg);
			return conn;
		}
		// END SYNCHRONIZATION      
	}


	/** Sends a message thruogh a given connection.
	  * @param conn the connection.
	  * @param msg the message to be sent */
	private void sendMessage(SipTransportConnection conn, SipMessage msg) throws IOException  {
		if (force_sent_by) SipProvider.updateViaHeader(msg,conn.getProtocol(),conn.getLocalAddress().toString(),conn.getLocalPort());
		conn.sendMessage(msg);
	}


	/** Stops running */
	public void halt() {
		// close all connections
		if (connections!=null) {
			log(LogLevel.TRACE,"connections are going down");
			for (Enumeration e=connections.elements(); e.hasMoreElements(); ) {
				SipTransportConnection c=(SipTransportConnection)e.nextElement();
				c.halt();
			}
			//connections=null;
		}
		listener=null;
	}


	/** When a new SIP message is received. */
	protected void processReceivedMessage(SipTransportConnection conn, SipMessage msg) {
		if (listener!=null) listener.onReceivedMessage(this,msg);
	}
	

	/** When SipTransportConnection terminates. */
	protected void processConnectionTerminated(SipTransportConnection conn, Exception error) {
		//System.out.println("DEBUG: SipTransportCO: connection terminated");
		ConnectionId connection_id=new ConnectionId(conn);
		removeConnection(connection_id);
		log(LogLevel.INFO,"connection "+conn+" terminated");
		if (error!=null) log(LogLevel.INFO,error);
		if (listener!=null) listener.onTransportConnectionTerminated(this,new SocketAddress(conn.getRemoteAddress(),conn.getRemotePort()),error);
	}


	/** Adds a new transport connection. */ 
	public SipTransportConnection addConnection(IpAddress remote_ipaddr, int remote_port) throws IOException {
		SipTransportConnection conn=createTransportConnection(new SocketAddress(remote_ipaddr,remote_port));
		if (conn!=null)  {
			log(LogLevel.INFO,"connection "+conn+" opened");
			addConnection(conn);
		}
		else log(LogLevel.INFO,"no connection has been opened");
		return conn;
	}


	/** Adds a new transport connection. */ 
	public void addConnection(SipTransportConnection conn) {
		ConnectionId connection_id=new ConnectionId(conn);
		// BEGIN SYNCHRONIZATION
		synchronized (connections) {
			
			if (connections.containsKey(connection_id)) {
				// remove the previous connection
				log(LogLevel.INFO,"trying to add the already established connection "+connection_id);
				log(LogLevel.INFO,"connection "+connection_id+" will be replaced");
				SipTransportConnection old_conn=(SipTransportConnection)connections.get(connection_id);
				old_conn.halt();
				connections.remove(connection_id);
			}
			else
			if (connections.size()>=nmax_connections) {
				// remove the older unused connection
				log(LogLevel.INFO,"reached the maximum number of connection: removing the older unused connection");
				long older_time=System.currentTimeMillis();
				ConnectionId older_id=null;
				for (Enumeration e=connections.elements(); e.hasMoreElements(); ) {
					SipTransportConnection co=(SipTransportConnection)e.nextElement();
					if (co.getLastTimeMillis()<older_time) older_id=new ConnectionId(co);
				}
				if (older_id!=null) removeConnection(older_id);
			}
			connections.put(connection_id,conn);
			conn.setListener(this_conn_listener);
			//connection_id=new ConnectionId(conn);
			//conn=(SipTransportConnection)connections.get(connection_id);
			// DEBUG log:
			log(LogLevel.TRACE,"active connenctions:");
			for (Enumeration e=connections.keys(); e.hasMoreElements(); ) {
				ConnectionId id=(ConnectionId)e.nextElement();
				log(LogLevel.TRACE,"connection-id="+id+": "+((SipTransportConnection)connections.get(id)).toString());
			}
		}
		// END SYNCHRONIZATION      
	}

 
	/** Removes a transport connection */ 
	public void removeConnection(ConnectionId connection_id) {
		// BEGIN SYNCHRONIZATION
		synchronized (connections) {
			
			if (connections.containsKey(connection_id)) {
				SipTransportConnection conn=(SipTransportConnection)connections.get(connection_id);
				connections.remove(connection_id);
				conn.halt();
				// DEBUG log:
				log(LogLevel.TRACE,"active connenctions:");
				for (Enumeration e=connections.elements(); e.hasMoreElements(); ) {
					SipTransportConnection co=(SipTransportConnection)e.nextElement();
					log(LogLevel.TRACE,"conn "+co.toString());
				}
			}
		}
		// END SYNCHRONIZATION
	}


	// ****************************** Logs *****************************

	/** Adds a new string to the default log. */
	void log(LogLevel level, String str) {
		if (logger!=null) logger.log(level,getProtocol()+": "+str);  
	}

	/** Prints an exception to the event log. */
	void log(LogLevel level, Exception e) {
		log(level, "Exception: "+ExceptionPrinter.getStackTraceOf(e));
	}

}
