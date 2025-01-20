/*
 * Copyright (C) 2007 Luca Veltri - University of Parma - Italy
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
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import org.mjsip.sip.address.GenericURI;
import org.mjsip.sip.address.NameAddress;
import org.mjsip.sip.address.SipURI;
import org.mjsip.sip.address.UnexpectedUriSchemeException;
import org.mjsip.sip.header.ViaHeader;
import org.mjsip.sip.message.SipMessage;
import org.mjsip.sip.message.SipMessageFactory;
import org.mjsip.time.Scheduler;
import org.slf4j.LoggerFactory;
import org.zoolu.net.AddressType;
import org.zoolu.net.IpAddress;
import org.zoolu.net.SocketAddress;
import org.zoolu.util.Random;
import org.zoolu.util.SimpleDigest;



/** SipProvider implements the SIP transport layer, that is the layer responsable for
  * sending and receiving SIP messages.
  * <p>
  * Any SipProvider user can send SIP messages through method <i>sendMessage()</i>
  * and receive incoming SIP messages by setting itself as message listener through the method 
  * <i>addSelectiveListener()</i> (specifing also the type of messages it wants to listen to)
  * and by implementing the callback method {@link SipProviderListener#onReceivedMessage(SipProvider,SipMessage) onReceivedMessage()} defined by the interface
  * SipProviderListener.
  * <p>
  * If a SipProvider user wants to capture ALL incoming messages regardless of any other
  * concurrent listening users, it may use the {@link SipProvider#addPromiscuousListener(SipProviderListener) addPromiscuousListener()} method.
  * <p>
  * SipProvider implements also multiplexing/demultiplexing service allowing a listener
  * to be bound to a specific type of SIP messages through the
  * {@link SipProvider#addSelectiveListener(SipId,SipProviderListener) addSelectiveListener(identifier,listener)} method, where:
  * <br> - <i>identifier</i> is a message identifier specifying the kind of messages the listener has to be associated to,
  * <br> - <i>listener</i> is the listener that received messages will be passed to.
  * <p>
  * The identifier, together with the SIP provider transport protocols, port numbers and
  * IP addresses, may be viewed as SIP Service Access Point (SAP) identifier
  * at receiving side. 
  * <p>
  * The message identifier can be of one of the three following types: transaction_id, dialog_id,
  * or method_id. These types of identifiers characterize respectively:
  * <br> - messages within a specific transaction,
  * <br> - messages within a specific dialog,
  * <br> - messages related to a specific SIP method.
  * It is also possible to use the message identifier {@link MethodId#ANY} to select 
  * <br> - all messages that are out of any transactions, dialogs, or method types
  *         already specified.
  * <p>
  * When receiving a message, the SipProvider passes the message to any active listeners
  * added in promiscuous mode thorugh method {@link SipProvider#addPromiscuousListener(SipProviderListener) addPromiscuousListener()}.
  * Then the message is passed to the eventual selective listeners added
  * thorugh method <i>addSelectiveListener()</i> and matching the given message.
  * For this purpose the SipProvider first tries to look for a matching  
  * transaction-id, then looks for a matching dialog-id, then for a matching method-id,
  * and finally for a default listener (i.e. that with id "ANY").
  * For the matched SipProviderListener, the method {@link SipProviderListener#onReceivedMessage(SipProvider,SipMessage) onReceivedMessage()} is fired.
  * <p>
  * Note: no 482 (Loop Detected) nor 501 (Not Implemented) responses are generated for requests
  * that does not properly match any active transactions, dialogs, nor method types.
  */
public class SipProvider implements SipTransportListener {
	
	private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(SipProvider.class);

	// **************************** Constants ****************************

	/** UDP protocol type */
	public static final String PROTO_UDP="udp";
	/** TCP protocol type */
	public static final String PROTO_TCP="tcp";
	/** TLS protocol type */
	public static final String PROTO_TLS="tls";
	/** DTLS protocol type */
	public static final String PROTO_DTLS="dtls";
	/** SCTP protocol type */
	public static final String PROTO_SCTP="sctp";
	

	// ************************ Other attributes *************************

	/** Table of supported transport layers for SIP (Hashtable of protocol (<code>String</code>), transport (<code>SipTransport</code>)) */
	protected Hashtable<String, SipTransport> sip_transports = null;

	/** Default transport */
	String default_transport=null;
	
	/** Table of sip listeners (Hashtable<SipId id, SipProviderListener listener>) */
	private final Map<SipId, SipProviderListener> sip_listeners = new HashMap<>();
	
	/** Vector of promiscuous listeners (Vector<SipProviderListener>) */
	private CopyOnWriteListeners<SipProviderListener, SipMessage> promisquousListeners = new CopyOnWriteListeners<>() {
		@Override
		protected void handle(SipProviderListener listener, SipMessage msg) {
			listener.onReceivedMessage(SipProvider.this, msg);
		}
	};

	/** Vector of exception listeners (Vector<SipProviderListener>) */
	private CopyOnWriteListeners<SipProviderExceptionListener, MessageProblem> exceptionListeners = new CopyOnWriteListeners<>() {
		@Override
		protected void handle(SipProviderExceptionListener listener, MessageProblem problem) {
			listener.onMessageException(problem.getMsg(), problem.getException());
		}
	};

	private final SipOptions _sipConfig;
	
	private final Scheduler _scheduler;

	private final SipMessageFactory _sipMessageFactory;

	/**
	 * Creates a new {@link SipProvider}.
	 */
	public SipProvider(SipOptions sipConfig, Scheduler scheduler) {
		this._sipConfig = sipConfig;
		_scheduler = scheduler;
		_sipMessageFactory = new SipMessageFactory(sipConfig);
		initLog();
		initSipTrasport(sipConfig.getTransportProtocols(),sipConfig.getTransportPorts());
	}

	/** Inits logs. */ 
	private void initLog() {
		LOG.info("SipStack: {}", SipStack.release);
		LOG.info("SipProvider: {}", this);
	}

	/**
	 * The {@link SipOptions} used.
	 */
	public SipOptions sipConfig() {
		return _sipConfig;
	}

	/**
	 * The scheduler for tasks.
	 */
	public Scheduler scheduler() {
		return _scheduler;
	}

	/**
	 * The {@link SipMessageFactory} in use.
	 */
	public SipMessageFactory messageFactory() {
		return _sipMessageFactory;
	}

	/**
	 * Whether the given transport protocol is secure (TLS or DTLS) or not (UDP,
	 * TCP, SCTP, etc.).
	 * 
	 * @return true for TLS or DTLS, false otherwise
	 */
	public static boolean isSecureTransport(String proto) {
		return proto.equalsIgnoreCase(PROTO_TLS) || proto.equalsIgnoreCase(PROTO_DTLS);
	}

	/**
	 * Inits and starts the transport services.
	 */
	private void initSipTrasport(String[] transport_protocols, int[] transport_ports) {
		if (transport_protocols.length > 0) {
			default_transport = transport_protocols[0];
		}

		sip_transports = new Hashtable<>();
		for (int i=0; i<transport_protocols.length; i++) {
			try {
				String proto=transport_protocols[i].toLowerCase();
				int port=(transport_ports!=null && transport_ports.length>i)? port=transport_ports[i] : 0;
				SipTransport transp=null;
				if (proto.equals(PROTO_UDP)) {
					if (port == 0)
						port = _sipConfig.getHostPort();
					transp = new UdpTransport(port, _sipConfig.getBindingIpAddr());
				}
				else
				if (proto.equals(PROTO_TCP)) {
					if (port == 0)
						port = _sipConfig.getHostPort();
					transp = new TcpTransport(port, _sipConfig.getBindingIpAddr(), _sipConfig.getMaxConnections());
				}
				else
				if (proto.equals(PROTO_TLS)) {
					if (port == 0)
						port = (_sipConfig.getHostPort() == _sipConfig.getDefaultPort())
								? _sipConfig.getDefaultTlsPort()
								: _sipConfig.getHostPort() + 1;
					if (_sipConfig.isTrustAll())
						transp = new TlsTransport(port, _sipConfig.getBindingIpAddr(), _sipConfig.getMaxConnections(),
								_sipConfig.getKeyFile(), _sipConfig.getCertFile());
					else {
						if (_sipConfig.getTrustedCerts() != null)
							transp = new TlsTransport(port, _sipConfig.getBindingIpAddr(),
									_sipConfig.getMaxConnections(), _sipConfig.getKeyFile(), _sipConfig.getCertFile(),
									_sipConfig.getTrustedCerts());
						else
							transp = new TlsTransport(port, _sipConfig.getBindingIpAddr(),
									_sipConfig.getMaxConnections(), _sipConfig.getKeyFile(), _sipConfig.getCertFile(),
									_sipConfig.getTrustFolder());
					}
				}
				else
				if (proto.equals(PROTO_DTLS)) {
					// TODO
					//..
				}
				else
				if (proto.equals(PROTO_SCTP)) {
					if (port == 0)
						port = _sipConfig.getHostPort();
					//transp=new SctpTransport(new SocketAddress(binding_ipaddr,port),nmax_connections,log_writer);
					Class<?> sctp_transport = Class.forName("org.zoolu.ext.sip.provider.SctpTransport");
					Class<?>[] param_classes = { Class.forName("org.zoolu.net.SocketAddress"), int.class,
							Class.forName("org.zoolu.util.LogWriter") };
					Object[] param_objects = { new SocketAddress(_sipConfig.getBindingIpAddr(), port),
							Integer.valueOf(_sipConfig.getMaxConnections()) };
					try  {
						java.lang.reflect.Constructor<?> constructor = sctp_transport.getConstructor(param_classes);
						transp=(SipTransport)constructor.newInstance(param_objects);
					}
					catch (NoSuchMethodException e) {
						LOG.debug("Exception.", e);
					}
				}
				
				if (transp!=null)  {
					setTransport(transp);
				}
			}
			catch (Exception e) {
				LOG.warn("Exception while initialiing sip tranport.", e);
			}
		}
		// LOG.debug("transport is up");
	}


	/** Stops the transport services. */ 
	private void stopSipTrasport() {
		if (sip_transports!=null)  {
			for (Enumeration<String> e = sip_transports.keys(); e.hasMoreElements();) {
				String proto = e.nextElement();
				SipTransport transp = sip_transports.get(proto);
				LOG.trace("{} is going down", proto);
				transp.halt();
			}
			sip_transports.clear();
			sip_transports=null;
		}
	}


	/** Sets a specific transport protocol. */ 
	public void setTransport(SipTransport transport) {
		String proto=transport.getProtocol();
		removeSipTransport(proto);
		sip_transports.put(proto,transport);
		transport.setListener(this);
		if (default_transport==null) default_transport=proto;
		LOG.info("{} is up at port {}", proto, transport.getLocalPort());
	}


	/** Removes a specific transport protocol. */ 
	public void removeSipTransport(String proto) {
		if (sip_transports.containsKey(proto))  {
			SipTransport t = sip_transports.get(proto);
			sip_transports.remove(proto);
			t.halt();
			if (proto.equals(default_transport)) default_transport=null;
			LOG.info("{} is down", proto);
		}
	}


	/** Stops the SipProviders. */ 
	public synchronized void halt() {
		LOG.debug("halt: SipProvider is going down");
		stopSipTrasport();
		sip_listeners.clear();
		promisquousListeners.clear();
		exceptionListeners.clear();
	}

	/** Converts the entire object into lines (to be saved into the config file) */
	protected String toLines() {
		// currently not implemented..
		return toString();
	}

	// ************************** Public methods *************************

	/** Whether it has the given transport protocol. */
	public boolean hasTransport(String proto) {
		if (sip_transports!=null) return sip_transports.containsKey(proto.toLowerCase());
		else return false; 
	}

	/** Whether it has TLS or DTLS transport protocol. */
	public boolean hasSecureTransport() {
		return hasTransport(PROTO_TLS) || hasTransport(PROTO_DTLS); 
	}

	/** Gets via address. */
	public String getViaAddress(AddressType type) {
		return _sipConfig.getViaAddr(type);
	}

	/** Gets via address. */
	public String getViaAddress(boolean ipV6) {
		return _sipConfig.getViaAddr(ipV6 ? AddressType.IP6 : AddressType.IP4);
	}

	/** Gets via address. */
	public String getViaAddress() {
		return getViaAddress(AddressType.DEFAULT);
	}

	/** Gets host port. */
	public int getPort() {
		return _sipConfig.getHostPort();
	}

	/** Gets tls port. */ 
	public int getTlsPort() {
		return (sip_transports.containsKey(PROTO_TLS)) ? sip_transports.get(PROTO_TLS).getLocalPort() : 0;
	}       

	/**
	 * Gets a valid SIP or SIPS contact address.
	 * 
	 * @param addressType The preferred type of address to use.
	 * @return a SIP or SIPS contact URI for this SIP provider. A SIPS URI is
	 *         returned if TLS (or DTLS) transport is supported
	 */
	public NameAddress getContactAddress(String user, AddressType addressType) {
		return getContactAddress(user, hasSecureTransport(), addressType);
	}

	/**
	 * Gets a valid SIP or SIPS contact address.
	 * 
	 * @param user        local user's name
	 * @param secure      whether returning a SIPS or SIP URI (true=SIPS, false=SIP)
	 * @param addressType The preferred type of address to use.
	 * @return a SIP or SIPS contact URI for this SIP provider
	 */
	public NameAddress getContactAddress(String user, boolean secure, AddressType addressType) {
		SipURI uri = (getPort() != _sipConfig.getDefaultPort()) 
				? new SipURI(user, getViaAddress(addressType), getPort())
				: new SipURI(user, getViaAddress(addressType));
		if (secure) {
			if (!hasSecureTransport()) return null;
			// else
			uri.setSecure(true);
		}
		else if (!hasTransport(PROTO_UDP)) uri.addTransport(getDefaultTransport());
		
		return new NameAddress(uri);
	}

	/** Whether binding the sip provider to all interfaces or only on the specified host address. */
	public boolean isAllInterfaces() {
		return _sipConfig.getBindingIpAddr() == null;
	}       

	/** Gets the binding IP address. */ 
	public IpAddress getBindingIpAddress() {
		return _sipConfig.getBindingIpAddr();
	}    
	
	/** Gets array of transport protocols. */ 
	public String[] getTransportProtocols() {
		String[] protocols=new String[sip_transports.size()];
		Enumeration<String> e = sip_transports.keys();
		for (int i = 0; i < protocols.length; i++) {
			protocols[i] = e.nextElement();
		}
		return protocols;
	}    
	
	/** Whether the given transport protocol is supported. */ 
	public boolean isSupportedTransport(String proto) {
		return sip_transports.containsKey(proto.toLowerCase());
	}    
	
	/** Whether the given transport protocol is supported and reliable. */ 
	public boolean isReliableTransport(String proto) {
		return isReliableTransport(sip_transports.get(proto.toLowerCase()));
	}    
	
	/** Whether the given transport is reliable. */ 
	boolean isReliableTransport(SipTransport transp) {
		if (transp != null) {
			try {
				return Class.forName("org.mjsip.sip.provider.SipTransportCO").isInstance(transp);
			} catch (ClassNotFoundException e) {
				// Ignore.
			}
		}
		// else
		return false;
	}    
	
	/** Gets the default transport protocol. */ 
	public String getDefaultTransport() {
		return default_transport;
	} 
	
	/** Gets the default transport protocol. */ 
	public synchronized void setDefaultTransport(String proto) {
		default_transport=proto;
	}    

	/** Whether using rport. */ 
	public boolean isRportSet() {
		return _sipConfig.useRport();
	}   

	/** Whether using 'force-rport' mode. */ 
	public boolean forceRport() {
		return _sipConfig.forceRport();
	}

	/** Whether setting the Via protocol, sent-by, and port values according to the transport connection.
	  * @param force_sent_by whether setting Via protocol, sent-by, and port values according to the transport connection */ 
	public synchronized void setForceSentBy(boolean force_sent_by) {
		for (Enumeration<SipTransport> i = sip_transports.elements(); i.hasMoreElements();) {
			try {
				SipTransportCO transp=(SipTransportCO)i.nextElement();
				transp.setForceSentBy(force_sent_by);
			}
			catch (Exception e) {
				// Ignore.
			}
		}
	}

	/** Whether "force-sent-by" mode is set. */ 
	public boolean isForceSentBySet() {
		for (Enumeration<SipTransport> i = sip_transports.elements(); i.hasMoreElements();) {
			try {
				SipTransportCO transp=(SipTransportCO)i.nextElement();
				return transp.isForceSentBySet();
			}
			catch (Exception e) {
				// Ignore.
			}
		}
		return false;
	}

	/** Whether has outbound proxy. */ 
	public boolean hasOutboundProxy() {
		return _sipConfig.getOutboundProxy() != null;
	}    

	/** Gets the outbound proxy. */ 
	public SipURI getOutboundProxy() {
		return _sipConfig.getOutboundProxy();
	}    

	/** Whether has tel gateway. */ 
	public boolean hasTelGateway() {
		return _sipConfig.getTelGateway() != null;
	}    

	/** Gets the tel gateway. */ 
	public SipURI getTelGateway() {
		return _sipConfig.getTelGateway();
	}    

	/** Gets the max number of (contemporary) open connections. */ 
	public int getNMaxConnections() {
		return _sipConfig.getMaxConnections();
	}    

	/** Sets a SipProvider listener for a target type of method, transaction, or dialog messages.
	  * @param id specifies the kind of messages that the listener
	  * as to be associated to. It may identify a method, a transaction, or a dialog, or all messages.
	  * Use MethodId.ANY to capture all messages.
	  * @param listener is the SipProviderListener that the specified type of messages has to be passed to. */
	public synchronized void addSelectiveListener(SipId id, SipProviderListener listener) {
		LOG.debug("Adding SipProviderListener: {}", id);
		sip_listeners.put(id,listener);   
	}


	/** Removes a SipProviderListener.
	  * @param id specifies the messages that the listener was associated to. */
	public synchronized void removeSelectiveListener(SipId id) {
		LOG.debug("Removing SipProviderListener: {}", id);
		sip_listeners.remove(id);
	}
  
	/** Adds a SipProvider listener for caputering any message in promiscuous mode.
	  * <p>
	  * When a SipProviderListener captures messages in promiscuous mode
	  * messages are passed to this listener before passing them to other specific listener.
	  * <br> More that one SipProviderListener can be active in promiscuous mode at the same time;
	  * in that case the same message is passed to all promiscuous SipProviderListeners.
	  * @param listener is the SipProviderListener. */
	public synchronized void addPromiscuousListener(SipProviderListener listener) {
		LOG.debug("Adding SipProviderListener in promiscuous mode.");
		if (!promisquousListeners.add(listener)) {
			LOG.warn("trying to add an already present SipProviderListener in promiscuous mode.");
		}
	}


	/** Removes a SipProviderListener in promiscuous mode. 
	  * @param listener is the SipProviderListener to be removed. */
	public synchronized void removePromiscuousListener(SipProviderListener listener) {
		LOG.debug("Removing SipProviderListener in promiscuous mode.");
		if (!promisquousListeners.remove(listener)) {
			LOG.warn("trying to remove a missed SipProviderListener in promiscuous mode.");
		}
	}

	/** Adds a SipProviderExceptionListener.
	  * The SipProviderExceptionListener is a listener for all exceptions thrown by the SipProviders.
	  * @param listener is the SipProviderExceptionListener. */
	public synchronized void addExceptionListener(SipProviderExceptionListener listener) {
		if (!exceptionListeners.add(listener)) {
			LOG.warn("trying to add an already present SipProviderExceptionListener.");
		}
	}


	/** Removes a SipProviderExceptionListener. 
	  * @param listener is the SipProviderExceptionListener to be removed. */
	public synchronized void removeExceptionListener(SipProviderExceptionListener listener) {
		if (!exceptionListeners.remove(listener)) {
			LOG.warn("trying to remove a missed SipProviderExceptionListener.");
		}
	}

	/** Sends the <i>msg</i> message.
	  * For request messages, if no Via header field is preset, a new Via is added;
	  * if protocol or sent-by fields of the Via header field do not match the current
	  * SipProvider configuration (e.g. via address or port) or the request-uri
	  * (required transport protocol), they are properly updated. 
	  * <p>
	  * The destination for the request is computed as follows:
	  * <br> - if <i>outbound_addr</i> is set, <i>outbound_addr</i> and 
	  *        <i>outbound_port</i> are used, otherwise
	  * <br> - if message has Route header with lr option parameter (i.e. RFC3261 compliant),
	  *        the first Route address is used, otherwise
	  * <br> - the request's Request-URI is considered.
	  * <p>
	  * The destination for the response is computed based on the sent-by parameter in
	  * the Via header field (RFC3261 compliant).
	  * <p>
	  * If a Via header field is already present, the specified transport protocol is used.
	  * Otherwise the default transport protocol is used. 
	  * <p>
	  * In case of connection-oriented transport:
	  * <br> - if an already established connection is found matching the destination
	  *        end point (socket), such connection is used, otherwise;
	  * <br> - a new connection is established.
	  *
	  * @return Returns a ConnectionId in case of connection-oriented delivery
	  * (e.g. TCP) or null in case of connection-less delivery (e.g. UDP) */
	public ConnectionId sendMessage(SipMessage msg) {
		ConnectionId conn_id=msg.getConnectionId();
		if (conn_id!=null) {
			LOG.debug("trying to send message through connection {}", conn_id);
		
			SipTransport sip_transport = sip_transports.get(conn_id.getProtocol());
			if (sip_transport!=null)
			try {
				SipTransportConnection conn=((SipTransportCO)sip_transport).sendMessageCO(msg);

				logMessage("Sent message to: ", conn.getProtocol(), conn.getRemoteAddress().toString(),
						conn.getRemotePort(), msg);
				return conn_id;
			}
			catch (IOException e) {
				LOG.warn("failed when tried to send message through connection {}", conn_id, e);
			}
			else {
				LOG.warn("no support for protocol {}", conn_id.getProtocol());
			}         
		}
		// else
		
		// destination address and port, proto, and ttl have to be set
		String dest_addr=null;
		int dest_port=0;
		String transport = null;
		String maddr=null;
		int ttl=0;
		
		if (msg.isRequest()) {
			// REQUESTS
			GenericURI nexthop_uri = _sipConfig.getOutboundProxy();
			// else
			if (nexthop_uri==null) {
				if (msg.hasRouteHeader()) {
					GenericURI route_uri=msg.getRouteHeader().getNameAddress().getAddress();
					if (route_uri.hasLr()) nexthop_uri=route_uri;
				}
			}
			// else
			if (nexthop_uri==null) {
				nexthop_uri=msg.getRequestLine().getAddress();
			}
			
			SipURI nexthop_sip_uri=null;
			if (nexthop_uri.isSipURI()) {
				nexthop_sip_uri = nexthop_uri.toSipURI();
			}
			else
			if (nexthop_uri.isTelURI()) {
				if (hasTelGateway()) nexthop_sip_uri=getTelGateway();
				else throw new RuntimeException("Trying to send a message to tel URI "+nexthop_uri+", but no Tel-Gateway has been set");
			}
			else throw new UnexpectedUriSchemeException(nexthop_uri.getScheme());
			
			if (nexthop_sip_uri.hasMaddr()) {
				maddr=nexthop_sip_uri.getMaddr();
				if (nexthop_sip_uri.hasTtl()) ttl=nexthop_sip_uri.getTtl();
			}
			dest_addr=(maddr==null)? nexthop_sip_uri.getHost() : maddr;
			dest_port=nexthop_sip_uri.getPort();
			
			if (nexthop_sip_uri.isSecure())
				transport = PROTO_TLS;
			else
			if (nexthop_sip_uri.hasTransport())
				transport = nexthop_sip_uri.getTransport();
			else
				transport = getDefaultTransport();

			// for TLS and DTLS port=port+1
			if (dest_port > 0 && isSecureTransport(transport))
				dest_port++;

			// if not present, add via
			if (!msg.hasViaHeader()) {
				ViaHeader via = new ViaHeader(transport, getViaAddress(nexthop_sip_uri.isIpv6()), getPort());
				if (_sipConfig.useRport()) {
					via.setRport();
				}
				via.setBranch(pickBranch());
				msg.addViaHeader(via);
			}
			// update the via according to transport information
			updateViaHeader(msg, transport, getViaAddress(SipURI.isIPv6(dest_addr)), getPort(), maddr, ttl);
			
			LOG.debug("using transport {}", transport);
		}
		else {
			// RESPONSES
			ViaHeader via=msg.getViaHeader();
			transport = via.getTransport();
			SipURI uri=via.getSipURI();
			if (via.hasReceived()) dest_addr=via.getReceived(); else dest_addr=uri.getHost();
			//if (!isReliableTransport(via.getProtocol()) && via.hasRport()) dest_port=via.getRport();
			if (via.hasRport()) dest_port=via.getRport();
			if (dest_port<=0) dest_port=uri.getPort();
		}

		// if port <= use default port
		if (dest_port <= 0)
			dest_port = (isSecureTransport(transport)) ? _sipConfig.getDefaultPort() + 1 : _sipConfig.getDefaultPort();

		return sendMessage(msg, transport, dest_addr, dest_port, ttl);
	}


	/** Updates the top Via header field of a SIP message, according to the given transport information.
	  * @param msg the message to be updated
	  * @param proto the transport protocol
	  * @param via_addr the sent-by address of the via
	  * @param host_port the host port */
	public static void updateViaHeader(SipMessage msg, String proto, String via_addr, int host_port) {
		updateViaHeader(msg,proto,via_addr,host_port,null,-1);
	}


	/**
	 * Updates the top Via header field of a SIP message, according to the given transport
	 * information.
	 * 
	 * @param msg
	 *        the message to be updated
	 * @param transport
	 *        the transport protocol
	 * @param via_addr
	 *        the sent-by address of the via
	 * @param host_port
	 *        the host port
	 * @param maddr
	 *        the IP multicast address (if applicable) or null
	 * @param ttl
	 *        the TTL for multicast (used only when parameter <i>maddr</i> is set)
	 */
	public static void updateViaHeader(SipMessage msg, String transport, String via_addr, int host_port, String maddr,
			int ttl) {
		ViaHeader via=msg.getViaHeader();
		boolean via_changed=false;        
		// if sent-by differs, update the via header
		if (!via.getHost().equalsIgnoreCase(via_addr) || via.getPort()!=host_port) {
			boolean rport=via.hasRport();
			String branch=via.getBranch();
			via = new ViaHeader(transport, via_addr, host_port);
			if (rport) via.setRport();
			via.setBranch(branch);
			via_changed=true;
		}
		// if proto differs, update the proto of via header
		if (!via.getTransport().equalsIgnoreCase(transport)) {
			via.setTransport(transport);
			via_changed=true;
		}
		// if maddr is set, update the via header by adding maddr and ttl params 
		if (maddr!=null) {
			via.setMaddr(maddr);
			if (ttl>0) via.setTtl(ttl);
			via_changed=true;
		}
		if (via_changed) {
			msg.removeViaHeader();
			msg.addViaHeader(via);
		}
	}


	/** Sends the <i>msg</i> message, specifing the transport portocol, nexthop address and port.
	  * For request messages, if no Via header field is preset, a new Via is added.
	  * <p>
	  * This is a low level method and forces the message to be routed to
	  * a specific nexthop address, port and transport,
	  * regardless whatever the Via, Route, or request-uri, address to. 
	  * <p>
	  * In case of connection-oriented transport, the connection is selected as follows:
	  * <br> - if an existing connection is found matching the destination
	  *        end point (destination socket), such connection is used, otherwise
	  * <br> - a new connection is established.
	  *
	  * @return It returns a ConnectionId in case of connection-oriented tranport protocol
	  * (e.g. TCP) or null in case of connection-less tranport protocol (e.g. UDP) */
	public ConnectionId sendMessage(SipMessage msg, String proto, String dest_addr, int dest_port, int ttl) {
		if (msg.isRequest()) {
			// if not present, add via
			if (!msg.hasViaHeader()) {
				ViaHeader via = new ViaHeader(proto, getViaAddress(SipURI.isIPv6(dest_addr)), getPort());
				if (_sipConfig.useRport()) {
					via.setRport();
				}
				via.setBranch(pickBranch());
				msg.addViaHeader(via);
			}
		}
		return sendRawMessage(msg,proto,dest_addr,dest_port,ttl); 
	}


	// /** @deprecated  The same as, and replaced by {@link #sendRawMessageTo(SipMessage,String,String,int,int)}. */
	/*public ConnectionId sendRawMessage(SipMessage msg, String proto, String dest_addr, int dest_port, int ttl) {
		return sendRawMessageTo(msg,proto,dest_addr,dest_port,ttl); --
	}*/


	/** Sends the <i>msg</i> message, specifing the transport portocol, nexthop address and port.
	  * It does the same as method {@link #sendMessage(SipMessage,String,String,int,int)}, but no via address is added (if not already present) in request messages.  */
	public ConnectionId sendRawMessage(SipMessage msg, String proto, String dest_addr, int dest_port, int ttl) {
		try {
			IpAddress dest_ipaddr = IpAddress.getByName(dest_addr);
			return sendRawMessage(msg,proto,dest_ipaddr,dest_port,ttl); 
		}
		catch (Exception e) {
			LOG.warn("Exception.", e);
			return null;
		}     
	}


	/** Sends the <i>msg</i> message, specifing the transport protocol, nexthop address and port.
	  * For request messages, no via address is added. */
	private ConnectionId sendRawMessage(SipMessage msg, String proto, IpAddress dest_ipaddr, int dest_port, int ttl) {
		if (proto==null) {
			LOG.warn("No protocol, message discarded.");
			return null;
		}
		// else
		SipTransport sip_transport = sip_transports.get(proto.toLowerCase());
		if (sip_transport==null) {
			LOG.warn("Unsupported protocol {}, message discarded.", proto);
			return null;
		}
		// else
		try {
			ConnectionId connection_id=sip_transport.sendMessage(msg,dest_ipaddr,dest_port,ttl);

			logMessage("Sent message to: ", proto, dest_ipaddr.toString(), dest_port, msg);

			return connection_id;
		}
		catch (IOException e) {
			LOG.warn("Exception", e);
			return null;
		}
	}


	/** Sends the <i>msg</i> message using the specified transport connection. */
	/*
	 * public ConnectionId sendMessage(SipMessage msg, ConnectionId conn_id) { if
	 * (log_all_packets || msg.getLength()>MIN_MESSAGE_LENGTH)
	 * LOG.info("Sending message through conn "+conn_id);
	 * LOG.trace("message to send:."+MESSAGE_BEGIN_DELIMITER+msg.toString()+
	 * MESSAGE_END_DELIMITER); SipTransportConnection conn=null; for (Enumeration
	 * e=sip_transports.elements(); e.hasMoreElements() && conn==null; ) {
	 * SipTransport transp=(SipTransport)e.nextElement(); if
	 * (isReliableTransport(transp))
	 * conn=((SipTransportCO)transp).sendMessage(msg,conn_id); } if (conn!=null) {
	 * // logs String proto=conn.getProtocol();
	 * LOG.debug("SipProvider: sendMessage(msg,conn): conn: {}", conn);
	 * LOG.debug("SipProvider: sendMessage(msg,conn): remote_addr: {}",
	 * conn.getRemoteAddress()); String
	 * dest_addr=conn.getRemoteAddress().toString(); int
	 * dest_port=conn.getRemotePort();
	 * logMessage(proto,dest_addr,dest_port,msg.getLength(),msg,"sent");
	 * 
	 * return new ConnectionId(conn); } else { return sendMessage(msg); } }
	 */


	//************************* Callback methods *************************
	
	/** From SipTransportListener. When a new SIP message is received. */
	@Override
	public void onReceivedMessage(SipTransport transport, SipMessage msg) {
		try {
			// logs
			logMessage("Received message from: ", msg.getTransportProtocol(), msg.getRemoteAddress(),
					msg.getRemotePort(), msg);
			
			// discard too short messages (e.g. CRLFCRLF "PING", or CRLF "PONG")
			if (msg.getLength()<=4) {
				LOG.warn("message too short: discarded.");
				return;
			}
			// discard non-SIP messages
			String first_line=msg.getFirstLine();
			if (first_line==null || first_line.toUpperCase().indexOf("SIP/2.0")<0) {
				LOG.warn("NOT a SIP message: discarded.");
				return;
			}
			
			// if a request, handle "received" and "rport" parameters
			if (msg.isRequest()) {
				ViaHeader vh=msg.getViaHeader();
				if (vh == null) {
					LOG.info("Message without via header discarded.");
					return;
				}
				
				boolean via_changed=false;

				String viaAddr = vh.getHost();
				int viaPort = vh.getPort();
				if (viaPort <= 0) {
					viaPort = _sipConfig.getDefaultPort();
				}
				 
				String srcAddr = msg.getRemoteAddress();
				if (vh.hasReceived() || (forceRport() && !viaAddr.equals(srcAddr))) {
					vh.setReceived(srcAddr);
					via_changed=true;
				}
				
				int srcPort = msg.getRemotePort();
				if (vh.hasRport() || (forceRport() && viaPort != srcPort)) {
					vh.setRport(srcPort);
					via_changed=true;
				}
				
				if (via_changed) {
					msg.removeViaHeader();
					msg.addViaHeader(vh);
				}
			}
			
			promisquousListeners.notify(msg);
			
			// check if the message is still valid
			if (!msg.isRequest() && !msg.isResponse()) {
				LOG.info("No valid SIP message, discarded.");
				return;
			}

			// look for a specific listener
			SipProviderListener listener=getListener(msg);
			if (listener != null) {
				listener.onReceivedMessage(this, msg);
			} else {
				LOG.info("No listener found for message, discarded.");
			}
		}
		catch (Exception exception) {
			LOG.warn("Error handling a new incoming message", exception);
			exceptionListeners.notify(new MessageProblem(msg, exception));
		}
	}

	/** Gets a listener for a given message.
	 * @param msg the SIP message */
	private synchronized SipProviderListener getListener(SipMessage msg) {
		// try to look for a transaction (requests go to transaction servers and response go to transaction clients)
		SipId transactionKey = SipId.createTransactionId(!msg.isRequest(), msg);
		SipProviderListener transactionListener = sip_listeners.get(transactionKey);
		if (transactionListener != null) {
			LOG.debug("Message passed to transaction: {}", transactionKey);
			return transactionListener;
		}

		// try to look for a dialog
		SipId dialogKey = SipId.createDialogId(msg);
		SipProviderListener dialogListener = sip_listeners.get(dialogKey);
		if (dialogListener != null) {
			LOG.debug("Message passed to dialog: {}", dialogKey);
			return dialogListener;
		}

		// try to look for a UAS
		SipId methodKey = SipId.createMethodId(msg);
		SipProviderListener methodListener = sip_listeners.get(methodKey);
		if (methodListener != null) {
			LOG.debug("Message passed to method: {}", methodKey);
			return methodListener;
		}        

		// try to look for a default UA
		SipProviderListener anyListener = sip_listeners.get(SipId.ANY_METHOD);
		if (anyListener != null) {
			LOG.debug("Message passed to ua: {}", SipId.ANY_METHOD);
			return anyListener;
		}

		LOG.warn("No listener found for message: {}", msg.getFirstLine());
		return null;
	}

	/** From SipTransportListener. When SipTransport terminates. */
	@Override
	public void onTransportTerminated(SipTransport transport, Exception error) {
		LOG.debug("transport {} terminated", transport);
		// TRY TO RESTART UDP WHEN ERRORS OCCUR
		if (error!=null && transport.getProtocol().equals(PROTO_UDP)) {
			LOG.info("transport UDP terminated with error: trying to restart it (after 1000ms)..");
			try {
				Thread.sleep(1000);
			} catch (Exception e) {
				// Ignore.
			}
			try {
				SipTransport udp = new UdpTransport(_sipConfig.getHostPort(), _sipConfig.getBindingIpAddr());
				setTransport(udp);
			}
			catch (Exception e) {
				LOG.info("Transport problem.", e);
			}
		}
	}   


	/** When a new incoming transport connection is established. It is called only for CO transport portocols. */ 
	@Override
	public void onIncomingTransportConnection(SipTransport transport, SocketAddress remote_soaddr) {
		LOG.debug("incoming connection established with {}:{}", transport, remote_soaddr);
	}


	/** When a transport connection terminates. */
	@Override
	public void onTransportConnectionTerminated(SipTransport transport, SocketAddress remote_soaddr, Exception error) {
		LOG.debug("connection to {}:{} terminated", transport, remote_soaddr);
	}


	//************************** Other methods ***************************
	
	/** Picks a fresh branch value.
	  * The branch ID MUST be unique across space and time for
	  * all requests sent by the UA.
	  * The branch ID always begin with the characters "z9hG4bK". These
	  * 7 characters are used by RFC 3261 as a magic cookie. */
	public static String pickBranch() {
		//String str=Long.toString(Math.abs(Random.nextLong()),16);
		//if (str.length()<5) str+="00000";
		//return "z9hG4bK"+str.substring(0,5);
		//return "z9hG4bK"+Random.nextHexString(8);
		return ViaHeader.MAGIC_COOKIE+Random.nextHexString(8);
	}  

	/** Picks an unique branch value based on a SIP message.
	  * This value could also be used as transaction ID */
	public String pickBranch(SipMessage msg) {
		StringBuilder sb=new StringBuilder();
		sb.append(msg.getRequestLine().getAddress().toString());
		sb.append(getViaAddress()+getPort());
		ViaHeader top_via=msg.getViaHeader();
		if (top_via.hasBranch())
			sb.append(top_via.getBranch());
		else {
			sb.append(top_via.getHost());
			sb.append(top_via.getPort());
			//sb.append(msg.getToHeader().getTag());
			sb.append(msg.getFromHeader().getTag());
			sb.append(msg.getCallIdHeader().getCallId());
			sb.append(msg.getCSeqHeader().getSequenceNumber());
		}
		//return "z9hG4bK"+(new MD5(unique_str)).asHex().substring(0,9);
		//return "z9hG4bK"+(new SimpleDigest(5,sb.toString())).asHex();
		return ViaHeader.MAGIC_COOKIE+(new SimpleDigest(5,sb.toString())).asHex();
	}  


	/** Picks a new tag.
	  * A tag  MUST be globally unique and cryptographically random
	  * with at least 32 bits of randomness.  A property of this selection
	  * requirement is that a UA will place a different tag into the From
	  * header of an INVITE than it would place into the To header of the
	  * response to the same INVITE.  This is needed in order for a UA to
	  * invite itself to a session. */
	public static String pickTag() {
		//String str=Long.toString(Math.abs(Random.nextLong()),16);
		//if (str.length()<8) str+="00000000";
		//return str.substring(0,8);
		return Random.nextNumString(12);
	}   

	/** Picks a new tag. The tag is generated uniquely based on message <i>req</i>.
	  * This tag can be generated for responses in a stateless
	  * manner - in a manner that will generate the same tag for the
	  * same request consistently.
	  */
	public static String pickTag(SipMessage req) {
		//return String.valueOf(tag_generator++);
		//return (new MD5(request.toString())).asHex().substring(0,8);
		return (new SimpleDigest(8,req.toString())).asHex();
	}


	/** Picks a new call-id.
	  * The call-id is a globally unique identifier over space and time.
	  * It is implemented in the form "localid@host".
	  * Call-id must be considered case-sensitive and is compared byte-by-byte. */
	public String pickCallId() {
		return pickCallId(getViaAddress(false));
	}   


	/** Picks a new call-id.
	  * The call-id is a globally unique
	  * identifier over space and time. It is implemented in the
	  * form "localid@host". Call-id must be considered case-sensitive and is
	  * compared byte-by-byte. */
	public static String pickCallId(String hostaddr) {
		//String str=Long.toString(Math.abs(Random.nextLong()),16);
		//if (str.length()<12) str+="000000000000";
		//return str.substring(0,12)+"@"+hostaddr();
		return Random.nextNumString(12)+"@"+hostaddr;
	}   


	/** picks an initial CSeq */
	public static int pickInitialCSeq() {
		return 1;
	}   


	/** (<b>Deprecated</b>) Constructs a NameAddress based on an input string.
	  * The input string can be a:
	  * <br> - <i>user</i> name,
	  * <br> - <i>user@address</i> url,
	  * <br> - <i>"Name" &lt;sip:user@address&gt;</i> address,
	  * <p>
	  * In the former case,
	  * a SIP URI is costructed using the outbound proxy as host address if present,
	  * otherwise the local via address is used. */
	/*public NameAddress completeNameAddress(String str) {
		if (str.indexOf("<sip:")>=0) return new NameAddress(str);
		else {
			SipURI uri=completeSipURI(str);
			return new NameAddress(uri);
		}
	}*/
	/** Constructs a SipURI based on an input string. */
	/*private SipURI completeSipURI(String str) {
		// in case it is passed only the 'user' field, add '@'<outbound_proxy>[':'<outbound_port>]
		if (!str.startsWith("sip:") && !str.startsWith("sips:") && str.indexOf("@")<0 && str.indexOf(".")<0 && str.indexOf(":")<0) {
			// probably it is just the user name..
			if (outbound_proxy!=null) {
				String host=outbound_proxy.getHost();
				int port=outbound_proxy.getPort();
				SipURI url=(port>0 && port!=SipStack.default_port)? new SipURI(str,host,port) : new SipURI(str,host);
				if (outbound_proxy.isSecure()) url.setSecure(true);
				return url;
			}
			else {
				SipURI url=(host_port>0 && host_port!=SipStack.default_port)? new SipURI(str,via_addr,host_port) : new SipURI(str,via_addr);
				if (transport_protocols[0].equals(PROTO_TLS)) url.setSecure(true);
				return url;
			}
		}
		else return new SipURI(str);
	}*/


	/** Gets a String value for this object. */ 
	@Override
	public String toString() {
		String portAndProtocols = _sipConfig.getHostPort() + toStringTransportProtocols();

		if (_sipConfig.getBindingIpAddr() == null) {
			return portAndProtocols;
		} else {
			return _sipConfig.getBindingIpAddr().toString() + ":" + portAndProtocols;
		}
	}   

	/** Gets a String with the list of transport protocols. */
	private String toStringTransportProtocols() {
		if (sip_transports == null) {
			return "";
		}

		StringBuilder sb = new StringBuilder();
		for (Enumeration<String> e = sip_transports.keys(); e.hasMoreElements();) {
			sb.append("/").append(e.nextElement());
		}
		return sb.toString();
	}

	//******************************* Logs *******************************

	/** Adds the SIP message to the message log. */
	private final void logMessage(String message, String proto, String addr, int port, SipMessage msg) {
		if (_sipConfig.isLogAllPackets()) {
			LOG.info("{} {}:{}/{} ({} bytes)\n-----Begin-of-message-----\n{}\n-----End-of-message-----", message, addr,
					port, proto, msg.getLength(), msg);
		}
	}

	/**
	 * Computes a new timeout based on the last retransmission timeout.
	 */
	public long retransmissionSlowdown(long retransmissionTimeout) {
		return Math.min(2 * retransmissionTimeout, sipConfig().getMaxRetransmissionTimeout());
	}

}
