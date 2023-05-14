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
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import org.mjsip.sip.address.GenericURI;
import org.mjsip.sip.address.NameAddress;
import org.mjsip.sip.address.SipURI;
import org.mjsip.sip.address.UnexpectedUriSchemeException;
import org.mjsip.sip.header.ViaHeader;
import org.mjsip.sip.message.SipMessage;
import org.zoolu.net.IpAddress;
import org.zoolu.net.SocketAddress;
import org.zoolu.util.Configurable;
import org.zoolu.util.Configure;
import org.zoolu.util.DateFormat;
import org.zoolu.util.ExceptionPrinter;
import org.zoolu.util.LogLevel;
import org.zoolu.util.LogRotationWriter;
import org.zoolu.util.Logger;
import org.zoolu.util.Parser;
import org.zoolu.util.Random;
import org.zoolu.util.SimpleDigest;
import org.zoolu.util.VectorUtils;



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
public class SipProvider implements Configurable, SipTransportListener {
	

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
	
	/** String value "auto-configuration" used for auto configuration of the host address */
	public static final String AUTO_CONFIGURATION="AUTO-CONFIGURATION";

	/** String value "auto-configuration" used for auto configuration of the host address */
	public static final String ALL_INTERFACES="ALL-INTERFACES";

	/** String value "NO-OUTBOUND" used for setting no outbound proxy */
	//public static final String NO_OUTBOUND="NO-OUTBOUND";

	/** Minimum length for a valid SIP message */
	private static final int MIN_MESSAGE_LENGTH=12;
	
	/** Message begin delimiter */
	private static final String MESSAGE_BEGIN_DELIMITER="-----Begin-of-message-----\r\n";

	/** Message end delimiter */
	private static final String MESSAGE_END_DELIMITER="-----End-of-message-----\r\n";


	// ********************* Configurable attributes *********************

	/** Via IP address or fully-qualified domanin name (FQDN).
	  * Use 'auto-configuration' for auto detection, or let it undefined. */
	String via_addr=null;

	/** Local SIP port */
	int host_port=0;

	/** Network interface (IP address) used by SIP for selective binding.
	  * Use 'ALL-INTERFACES' or let it undefined for binding SIP to all interfaces. */
	IpAddress binding_ipaddr=null;

	/** List of enabled transport protocols (the first protocol is used as default). */
	String[] transport_protocols=null;
	
	/** List of transport ports, ordered as the corresponding transport_protocols. */
	int[] transport_ports=null;

	/** Max number of (contemporary) open connections */
	int nmax_connections=0;

	/** Outbound proxy URI ([sip:]host_addr[:host_port][;transport=proto]).
	  * Use 'NONE' for not using an outbound proxy (or let it undefined). */
	SipURI outbound_proxy=null;

	/** Tel Gatway URI ([sip:]host_addr[:host_port][;transport=proto]).
	  * URI of a default SIP proxy/gateway that is used for sending request messages with a "tel" URI as request-uri.
	  * Use 'NONE' for not using a tel gateway (or let it undefined). */
	SipURI tel_gateway=null;

	/** Whether logging all packets (including non-SIP keepalive tokens). */
	boolean log_all_packets=false;


	/** For TLS. Whether all client and server certificates should be considered trusted.
	  * By default, trust_all={@link #default_tls_trust_all}. */
	boolean trust_all=SipStack.default_tls_trust_all;

	/** For TLS. names of the files containing trusted certificates.
	  * The file names include the full path starting from the current working folder.
	  * By default, trust_all={@link SipStack#default_tls_trusted_certs}. */
	String[] trusted_certs=SipStack.default_tls_trusted_certs;

	/** For TLS. Path of the folder where trusted certificates are placed.
	  * All certificates (with file extension ".crt") found in this folder are considered trusted.
	  * By default, trust_folder={@link SipStack#default_tls_trust_folder}. */
	String trust_folder=SipStack.default_tls_trust_folder;

	/** For TLS. Absolute file name of the certificate (containing the public key) of the local node.
	  * The file name includes the full path starting from the current working folder.
	  * By default, trust_folder={@link SipStack#default_tls_cert_file}. */
	String cert_file=SipStack.default_tls_cert_file;

	/** For TLS. Absolute file name of the private key of the local node.
	  * The file name includes the full path starting from the current working folder.
	  * By default, trust_folder={@link SipStack#default_tls_key_file}. */
	String key_file=SipStack.default_tls_key_file;


	// for backward compatibility:

	/** Outbound proxy addr (for backward compatibility). */
	private String outbound_addr=null;
	/** Outbound proxy port (for backward compatibility). */
	private int outbound_port=-1;


	// ************************ Other attributes *************************

	/** Event logger */
	protected Logger event_logger=null;

	/** Message logger */
	protected Logger message_logger=null;

	/** Table of supported transport layers for SIP (Hashtable of protocol (<code>String</code>), transport (<code>SipTransport</code>)) */
	protected Hashtable sip_transports=null;

	/** Default transport */
	String default_transport=null;

	/** Whether adding 'rport' parameter on outgoing requests. */
	boolean rport=true;
	
	/** Whether forcing 'rport' parameter on incoming requests ('force-rport' mode). */
	boolean force_rport=false;

	/** Table of sip listeners (Hashtable<SipId id, SipProviderListener listener>) */
	Hashtable sip_listeners=new Hashtable();
	
	/** Vector of promiscuous listeners (Vector<SipProviderListener>) */
	Vector promiscuous_listeners=new Vector();

	/** Vector of exception listeners (Vector<SipProviderListener>) */
	Vector exception_listeners=new Vector();



	// *************************** Costructors ***************************

	/** Creates a void SipProvider. */ 
	/*protected SipProvider() {
		
	}*/

	/** Creates a new SipProvider.
	  * @param via_addr SIP local via address
	  * @param host_port SIP local port */ 
	public SipProvider(String via_addr, int host_port) {
		init(via_addr,host_port);
		initLog();
		initSipTrasport(transport_protocols,null);
	}


	/** Creates a new SipProvider. 
	  * @param via_addr SIP local via address
	  * @param host_port SIP local port
	  * @param transport_protocols array of active transport protocols */
	public SipProvider(String via_addr, int host_port, String[] transport_protocols) {
		init(via_addr,host_port);
		initLog();
		initSipTrasport(transport_protocols,null);
	}


	/** Creates a new SipProvider. 
	  * @param via_addr SIP local via address
	  * @param host_port SIP local port
	  * @param transport_protocols array of active transport protocols
	  * @param binding_addr local IP address to which the SIP provider has to be bound to */ 
	public SipProvider(String via_addr, int host_port, String[] transport_protocols, String binding_addr) {
		init(via_addr,host_port);
		initLog();
		setBindingIpAddress(binding_addr);
		initSipTrasport(transport_protocols,null);
	}


	/** Creates a new SipProvider. 
	  * @param via_addr SIP local via address
	  * @param transport_protocols array of active transport protocols 
	  * @param transport_ports array of transport port used for the corresponding transport protocols */ 
	public SipProvider(String via_addr, int host_port, String[] transport_protocols, int[] transport_ports) {
		init(via_addr,host_port);
		initLog();
		initSipTrasport(transport_protocols,transport_ports);
	}


	/** Creates a new SipProvider. 
	  * @param via_addr SIP local via address
	  * @param transport_protocols array of active transport protocols 
	  * @param transport_ports array of transport port used for the corresponding transport protocols 
	  * @param binding_addr local IP address to which the SIP provider has to be bound to */ 
	public SipProvider(String via_addr, int host_port, String[] transport_protocols, int[] transport_ports, String binding_addr) {
		init(via_addr,host_port);
		initLog();
		setBindingIpAddress(binding_addr);
		initSipTrasport(transport_protocols,transport_ports);
	}


	/** Creates a new SipProvider. 
	  * @param via_addr SIP local via address
	  * @param transport_protocols array of active transport protocols 
	  * @param transport_ports array of transport port used for the corresponding transport protocols 
	  * @param binding_ipaddr local IP address to which the SIP provider has to be bound to */ 
	/*public SipProvider(String via_addr, int host_port, String[] transport_protocols, int[] transport_ports, IpAddress binding_ipaddr) {
		init(via_addr,host_port);
		initLog();
		this.binding_ipaddr=binding_ipaddr;
		initSipTrasport(transport_protocols,transport_ports);
	}*/


	/** Creates a new SipProvider. 
	  * @param via_addr SIP local via address
	  * @param host_port SIP local port 
	  * @param sip_transports array of active transport services (SipTransport) */ 
	public SipProvider(String via_addr, int host_port, SipTransport[] sip_transports) {
		init(via_addr,host_port);
		initLog();
		// init transport
		this.sip_transports=new Hashtable();
		if (sip_transports!=null) {
			for (int i=0; i<sip_transports.length; i++) setTransport(sip_transports[i]);
			if (sip_transports.length>0) default_transport=sip_transports[0].getProtocol();
		}
	}


	/** Creates a new SipProvider. 
	  * @param file file where all configuration parameters are read from. */ 
	public SipProvider(String file) {
		if (!SipStack.isInit()) SipStack.init(file);
		new Configure(this,file);
		init(via_addr,host_port);
		initLog();
		initSipTrasport(transport_protocols,transport_ports);
	}


	/** Inits the SipProvider, initializing the SipProviderListeners, the transport protocols, the outbound proxy, and other attributes. */ 
	private void init(String via_addr, int host_port) {
		if (!SipStack.isInit()) SipStack.init();
		if (via_addr==null || via_addr.equalsIgnoreCase(AUTO_CONFIGURATION)) via_addr=IpAddress.getLocalHostAddress().toString();
		this.via_addr=via_addr;
		if (host_port<=0) host_port=SipStack.default_port;
		this.host_port=host_port;
		rport=SipStack.use_rport; 
		force_rport=SipStack.force_rport; 
		
		// just for backward compatibility..
		if (outbound_port<0) outbound_port=SipStack.default_port;
		if (outbound_addr!=null) {
			if (outbound_addr.equalsIgnoreCase(Configure.NONE) || outbound_addr.equalsIgnoreCase("NO-OUTBOUND")) outbound_proxy=null;
			else outbound_proxy=new SipURI(outbound_addr,outbound_port);
		}
	}

	
	/** Inits logs. */ 
	private void initLog() {
		event_logger=(SipStack.event_logger!=null)? SipStack.event_logger : (SipStack.debug_level>0)? newLogger("_events.log") : null;
		message_logger=(SipStack.message_logger!=null)? SipStack.message_logger : (SipStack.debug_level>0)? newLogger("_messages.log") : null;

		log(LogLevel.INFO,"Date: "+DateFormat.formatHHmmssSSSEEEddMMMyyyy(new Date()));
		log(LogLevel.INFO,"SipStack: "+SipStack.release);
		log(LogLevel.INFO,"new SipProvider(): "+toString());
	}

  
	/** Gets a new logger for this SIP provider.
	  * The name of the log file is formed by the SIP provider Via address, port, and the given suffix
	  * @param file_suffix the log file suffix
	  * @return a new logger for this SIP provider that writes log messages to a file with the given suffix */ 
	private Logger newLogger(String file_suffix) {
		String filename=SipStack.log_path+"//"+via_addr+"."+host_port+file_suffix;
		int debug_level=SipStack.debug_level;
		LogLevel logging_level=debug_level>=6? LogLevel.ALL : debug_level==5? LogLevel.TRACE : debug_level==4? LogLevel.DEBUG : debug_level==3? LogLevel.INFO : debug_level==2? LogLevel.WARNING : debug_level==1? LogLevel.SEVERE : LogLevel.OFF;
		return new LogRotationWriter(filename,logging_level,SipStack.max_logsize*1024,SipStack.log_rotations,SipStack.rotation_scale,SipStack.rotation_time);
	}
 
  
	/** Whether the given transport protocol is secure (TLS or DTLS) or not (UDP, TCP, SCTP, etc.).
	  * @return true for TLS or DTLS, false otherwise */
	public static boolean isSecureTransport(String proto) {
		return proto.equalsIgnoreCase(PROTO_TLS) || proto.equalsIgnoreCase(PROTO_DTLS);
	}


	/** Inits and starts the transport services. */ 
	private void initSipTrasport(String[] transport_protocols, int[] transport_ports) {
		
		if (transport_protocols==null) transport_protocols=SipStack.default_transport_protocols;
		this.transport_protocols=transport_protocols;
		if (transport_protocols.length>0) default_transport=transport_protocols[0];
		if (nmax_connections<=0) nmax_connections=SipStack.default_nmax_connections;

		sip_transports=new Hashtable();
		for (int i=0; i<transport_protocols.length; i++) {
			try {
				String proto=transport_protocols[i].toLowerCase();
				int port=(transport_ports!=null && transport_ports.length>i)? port=transport_ports[i] : 0;
				SipTransport transp=null;
				if (proto.equals(PROTO_UDP)) {
					if (port==0) port=host_port;
					transp=new UdpTransport(port,binding_ipaddr);
				}
				else
				if (proto.equals(PROTO_TCP)) {
					if (port==0) port=host_port;
					transp=new TcpTransport(port,binding_ipaddr,nmax_connections,event_logger);
				}
				else
				if (proto.equals(PROTO_TLS)) {
					if (port==0) port=(host_port==SipStack.default_port)? SipStack.default_tls_port : host_port+1;
					if (trust_all) transp=new TlsTransport(port,binding_ipaddr,nmax_connections,key_file,cert_file,event_logger);
					else {
						if (trusted_certs!=null) transp=new TlsTransport(port,binding_ipaddr,nmax_connections,key_file,cert_file,trusted_certs,event_logger);
						else transp=new TlsTransport(port,binding_ipaddr,nmax_connections,key_file,cert_file,trust_folder,event_logger);
					}
				}
				else
				if (proto.equals(PROTO_DTLS)) {
					// TODO
					//..
				}
				else
				if (proto.equals(PROTO_SCTP)) {
					if (port==0) port=host_port;
					//transp=new SctpTransport(new SocketAddress(binding_ipaddr,port),nmax_connections,log_writer);
					Class sctp_transport=Class.forName("org.zoolu.ext.sip.provider.SctpTransport");
					Class[] param_classes={ Class.forName("org.zoolu.net.SocketAddress"), int.class, Class.forName("org.zoolu.util.LogWriter") };
					Object[] param_objects={ new SocketAddress(binding_ipaddr,port), new Integer(nmax_connections), event_logger };
					try  {
						java.lang.reflect.Constructor constructor=sctp_transport.getConstructor(param_classes);
						transp=(SipTransport)constructor.newInstance(param_objects);
					}
					catch (NoSuchMethodException e) {
						log(LogLevel.DEBUG,e);
					}
				}
				
				if (transp!=null)  {
					setTransport(transp);
				}
			}
			catch (Exception e) {
				log(LogLevel.INFO,e);
			}
		}
		//log(LogLevel.DEBUG,"transport is up");
	}


	/** Stops the transport services. */ 
	private void stopSipTrasport() {
		if (sip_transports!=null)  {
			for(Enumeration e=sip_transports.keys(); e.hasMoreElements(); ) {
				String proto=(String)e.nextElement();
				SipTransport transp=(SipTransport)sip_transports.get(proto);
				log(LogLevel.TRACE,proto+" is going down");
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
		log(LogLevel.INFO,proto+" is up at port "+transport.getLocalPort());
	}


	/** Removes a specific transport protocol. */ 
	public void removeSipTransport(String proto) {
		if (sip_transports.containsKey(proto))  {
			SipTransport t=(SipTransport)sip_transports.get(proto);
			sip_transports.remove(proto);
			t.halt();
			if (proto.equals(default_transport)) default_transport=null;
			log(LogLevel.INFO,proto+" is down");
		}
	}


	/** Stops the SipProviders. */ 
	public synchronized void halt() {
		log(LogLevel.DEBUG,"halt: SipProvider is going down");
		stopSipTrasport();
		sip_listeners=new Hashtable();
		promiscuous_listeners=new Vector();
		exception_listeners=new Vector();
	}


	/** From Configurable. Parses a single line (loaded from the config file). */
	public void parseLine(String line) {
		String attribute;
		Parser par;
		int index=line.indexOf("=");
		if (index>0) {  attribute=line.substring(0,index).trim(); par=new Parser(line,index+1);  }
		else {  attribute=line; par=new Parser("");  }
		char[] delim={' ',','};
		
		if (attribute.equals("via_addr")) {  via_addr=par.getString(); return;  }
		if (attribute.equals("host_port")) {  host_port=par.getInt(); return;  }
		if (attribute.equals("binding_ipaddr")) {  setBindingIpAddress(par.getString()); return;  }
		if (attribute.equals("transport_protocols")) {  transport_protocols=par.getWordArray(delim); return;  }
		if (attribute.equals("transport_ports")) {  transport_ports=par.getIntArray(); return;  }
		if (attribute.equals("nmax_connections")) {  nmax_connections=par.getInt(); return;  }
		if (attribute.equals("outbound_proxy")) {
			String str_uri=par.getString();
			if (str_uri==null || str_uri.length()==0 || str_uri.equalsIgnoreCase(Configure.NONE) || str_uri.equalsIgnoreCase("NO-OUTBOUND")) outbound_proxy=null;
			else outbound_proxy=new SipURI(str_uri);
			return;
		}
		if (attribute.equals("tel_gateway")) {
			String str_uri=par.getString();
			if (str_uri==null || str_uri.length()==0 || str_uri.equalsIgnoreCase(Configure.NONE)) tel_gateway=null;
			else tel_gateway=new SipURI(str_uri);
			return;
		}
		if (attribute.equals("log_all_packets")) { log_all_packets=(par.getString().toLowerCase().startsWith("y")); return; }

		// certificates
		if (attribute.equals("trust_all")){ trust_all=(par.getString().toLowerCase().startsWith("y")); return; }
		if (attribute.equals("trusted_certs")){ trusted_certs=par.getStringArray(); return; }
		if (attribute.equals("trust_folder")){ trust_folder=par.getRemainingString().trim(); return; }
		if (attribute.equals("cert_file")){ cert_file=par.getRemainingString().trim(); return; }
		if (attribute.equals("key_file")){ key_file=par.getRemainingString().trim(); return; }

		// old parameters
		if (attribute.equals("host_addr")) System.err.println("WARNING: parameter 'host_addr' is no more supported; use 'via_addr' instead.");
		if (attribute.equals("tls_port")) System.err.println("WARNING: parameter 'tls_port' is no more supported; use 'transport_ports' instead.");
		if (attribute.equals("all_interfaces")) System.err.println("WARNING: parameter 'all_interfaces' is no more supported; use 'host_iaddr' for setting a specific interface or let it undefined.");
		if (attribute.equals("use_outbound")) System.err.println("WARNING: parameter 'use_outbound' is no more supported; use 'outbound_proxy' for setting an outbound proxy or let it undefined.");
		if (attribute.equals("outbound_addr")) {
			System.err.println("WARNING: parameter 'outbound_addr' has been deprecated; use 'outbound_proxy=[sip:]<host_addr>[:<host_port>][;transport=proto]' instead.");
			outbound_addr=par.getString();
			return;
		}
		if (attribute.equals("outbound_port")) {
			System.err.println("WARNING: parameter 'outbound_port' has been deprecated; use 'outbound_proxy=<host_addr>[:<host_port>]' instead.");
			outbound_port=par.getInt();
			return;
		}
		if (attribute.equals("host_ifaddr")) {
			System.err.println("WARNING: parameter 'host_ifaddr' has been deprecated; use 'binding_ipaddr' instead.");
			setBindingIpAddress(par.getString());
			return;
		}
	}  


	/** Sets the binding IP address . */
	private void setBindingIpAddress(String str) {
		if (str==null || str.equalsIgnoreCase(ALL_INTERFACES)) binding_ipaddr=null;
		else {
			try {
				binding_ipaddr=IpAddress.getByName(str);
			}
			catch (IOException e) {
				log(LogLevel.INFO,"Unable to set the following binding address: "+str);
			log(LogLevel.DEBUG,e);
			}
		}
	}


	/** Converts the entire object into lines (to be saved into the config file) */
	protected String toLines() {
		// currently not implemented..
		return toString();
	}


	/** Gets a String with the list of transport protocols. */ 
	private String transportProtocolsToString() {
		if (sip_transports==null) return ""; 
		// else
		StringBuffer sb=new StringBuffer();
		for (Enumeration e=sip_transports.keys(); e.hasMoreElements(); ) {
			sb.append("/").append((String)e.nextElement());
		}
		return sb.toString();
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
	public String getViaAddress() {
		return via_addr;
	}    
	
	/** Sets via address. */ 
	/*public void setViaAddress(String addr) {
		via_addr=addr;
	}*/   

	/** Gets host port. */ 
	public int getPort() {
		return host_port;
	}       

	/** Gets tls port. */ 
	public int getTlsPort() {
		return (sip_transports.containsKey(PROTO_TLS))? ((SipTransport)sip_transports.get(PROTO_TLS)).getLocalPort() : 0;
	}       

	/** Gets a valid SIP or SIPS contact address.
	  * @return a SIP or SIPS contact URI for this SIP provider. A SIPS URI is returned if TLS (or DTLS) transport is supported */
	public NameAddress getContactAddress() {
		return getContactAddress(null,hasSecureTransport());
	}

	/** Gets a valid SIP or SIPS contact address.
	  * @return a SIP or SIPS contact URI for this SIP provider. A SIPS URI is returned if TLS (or DTLS) transport is supported */
	public NameAddress getContactAddress(String user) {
		return getContactAddress(user,hasSecureTransport());
	}

	/** Gets a valid SIP or SIPS contact address.
	  * @param secure whether returning a SIPS or SIP URI (true=SIPS, false=SIP)
	  * @return a SIP or SIPS contact URI for this SIP provider */
	public NameAddress getContactAddress(boolean secure) {
		return getContactAddress(null,secure);
	}

	/** Gets a valid SIP or SIPS contact address.
	  * @param user local user's name
	  * @param secure whether returning a SIPS or SIP URI (true=SIPS, false=SIP)
	  * @return a SIP or SIPS contact URI for this SIP provider */
	public NameAddress getContactAddress(String user, boolean secure) {
		SipURI uri=(getPort()!=SipStack.default_port)? new SipURI(user,getViaAddress(),getPort()) : new SipURI(user,getViaAddress());
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
		return binding_ipaddr==null;
	}       

	/** Gets the binding IP address. */ 
	public IpAddress getBindingIpAddress() {
		return binding_ipaddr;
	}    
	
	/** Gets array of transport protocols. */ 
	public String[] getTransportProtocols() {
		String[] protocols=new String[sip_transports.size()];
		Enumeration e=sip_transports.keys();
		for (int i=0; i<protocols.length; i++) protocols[i]=(String)e.nextElement(); 
		return protocols;
	}    
	
	/** Whether the given transport protocol is supported. */ 
	public boolean isSupportedTransport(String proto) {
		return sip_transports.containsKey(proto.toLowerCase());
	}    
	
	/** Whether the given transport protocol is supported and reliable. */ 
	public boolean isReliableTransport(String proto) {
		return isReliableTransport((SipTransport)sip_transports.get(proto.toLowerCase()));
	}    
	
	/** Whether the given transport is reliable. */ 
	boolean isReliableTransport(SipTransport transp) {
		if (transp!=null) try {  return Class.forName("org.mjsip.sip.provider.SipTransportCO").isInstance(transp);  } catch (ClassNotFoundException e) {}
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

	/** Sets rport support. */ 
	public synchronized void setRport(boolean flag) {
		rport=flag;
	}   

	/** Whether using rport. */ 
	public boolean isRportSet() {
		return rport;
	}   

	/** Sets 'force-rport' mode. */ 
	public synchronized void setForceRport(boolean flag) {
		force_rport=flag;
	}   

	/** Whether using 'force-rport' mode. */ 
	public boolean isForceRportSet() {
		return force_rport;
	}

	/** Whether setting the Via protocol, sent-by, and port values according to the transport connection.
	  * @param force_sent_by whether setting Via protocol, sent-by, and port values according to the transport connection */ 
	public synchronized void setForceSentBy(boolean force_sent_by) {
		for (Enumeration i=sip_transports.elements(); i.hasMoreElements(); ) {
			try {
				SipTransportCO transp=(SipTransportCO)i.nextElement();
				transp.setForceSentBy(force_sent_by);
			}
			catch (Exception e) {}
		}
	}

	/** Whether "force-sent-by" mode is set. */ 
	public boolean isForceSentBySet() {
		for (Enumeration i=sip_transports.elements(); i.hasMoreElements(); ) {
			try {
				SipTransportCO transp=(SipTransportCO)i.nextElement();
				return transp.isForceSentBySet();
			}
			catch (Exception e) {}
		}
		return false;
	}

	/** Whether has outbound proxy. */ 
	public boolean hasOutboundProxy() {
		return outbound_proxy!=null;
	}    

	/** Gets the outbound proxy. */ 
	public SipURI getOutboundProxy() {
		return outbound_proxy;
	}    

	/** Sets the outbound proxy. Use 'null' for not using any outbound proxy. */ 
	public synchronized void setOutboundProxy(SipURI uri) {
		outbound_proxy=uri;
	}

	/** Removes the outbound proxy. */ 
	/*public void removeOutboundProxy() {
		setOutboundProxy(null);
	}*/

	/** Whether has tel gateway. */ 
	public boolean hasTelGateway() {
		return tel_gateway!=null;
	}    

	/** Gets the tel gateway. */ 
	public SipURI getTelGateway() {
		return tel_gateway;
	}    

	/** Sets the tel gateway. Use 'null' for not using any tel gateway. */ 
	public synchronized void setTelGateway(SipURI uri) {
		tel_gateway=uri;
	}

	/** Gets the max number of (contemporary) open connections. */ 
	public int getNMaxConnections() {
		return nmax_connections;
	}    

	/** Sets the max number of (contemporary) open connections. */ 
	public synchronized void setNMaxConnections(int n) {
		nmax_connections=n;
	}    
		
				
	/** Gets event logger. */ 
	public Logger getLogger() {
		return event_logger;
	}    
	

	/** Returns the table of active listeners as Hastable:(SipId)IDs--&gt;(SipListener)listener. */ 
	public Hashtable getListeners() {
		return sip_listeners;
	}   


	/** Sets a SipProvider listener for a target type of method, transaction, or dialog messages.
	  * @param id specifies the kind of messages that the listener
	  * as to be associated to. It may identify a method, a transaction, or a dialog, or all messages.
	  * Use MethodId.ANY to capture all messages.
	  * @param listener is the SipProviderListener that the specified type of messages has to be passed to. */
	public synchronized void addSelectiveListener(SipId id, SipProviderListener listener) {
		log(LogLevel.DEBUG,"adding SipProviderListener: "+id);
		if (sip_listeners.containsKey(id)) {
			log(LogLevel.WARNING,"adding a SipProvider listener with an identifier already present: the previous listener is removed.");
			sip_listeners.remove(id);
		}
		sip_listeners.put(id,listener);   
		log(LogLevel.TRACE,"active sip listeners: "+sip_listeners.size());
	}


	/** Removes a SipProviderListener.
	  * @param id specifies the messages that the listener was associated to. */
	public synchronized void removeSelectiveListener(SipId id) {
		log(LogLevel.DEBUG,"removing SipProviderListener: "+id);
		if (!sip_listeners.containsKey(id)) {
			log(LogLevel.WARNING,"removeListener("+id+"): no such listener found.");
		}
		else {
			sip_listeners.remove(id);
		}
		log(LogLevel.TRACE,"active sip listeners: "+sip_listeners.size());
	}

  
	/** Adds a SipProvider listener for caputering any message in promiscuous mode.
	  * <p>
	  * When a SipProviderListener captures messages in promiscuous mode
	  * messages are passed to this listener before passing them to other specific listener.
	  * <br> More that one SipProviderListener can be active in promiscuous mode at the same time;
	  * in that case the same message is passed to all promiscuous SipProviderListeners.
	  * @param listener is the SipProviderListener. */
	public synchronized void addPromiscuousListener(SipProviderListener listener) {
		log(LogLevel.DEBUG,"adding SipProviderListener in promiscuous mode");
		if (promiscuous_listeners.contains(listener)) {
			log(LogLevel.WARNING,"trying to add an already present SipProviderListener in promiscuous mode.");
		}
		else {
			promiscuous_listeners.addElement(listener);
		}
	}


	/** Removes a SipProviderListener in promiscuous mode. 
	  * @param listener is the SipProviderListener to be removed. */
	public synchronized void removePromiscuousListener(SipProviderListener listener) {
		log(LogLevel.DEBUG,"removing SipProviderListener in promiscuous mode");
		if (!promiscuous_listeners.contains(listener)) {
			log(LogLevel.WARNING,"trying to remove a missed SipProviderListener in promiscuous mode.");
		}
		else {
			promiscuous_listeners.removeElement(listener);
		}
	}


	/** Adds a SipProviderExceptionListener.
	  * The SipProviderExceptionListener is a listener for all exceptions thrown by the SipProviders.
	  * @param listener is the SipProviderExceptionListener. */
	public synchronized void addExceptionListener(SipProviderExceptionListener listener) {
		log(LogLevel.DEBUG,"adding a SipProviderExceptionListener");
		if (exception_listeners.contains(listener)) {
			log(LogLevel.WARNING,"trying to add an already present SipProviderExceptionListener.");
		}
		else {
			exception_listeners.addElement(listener);
		}
	}


	/** Removes a SipProviderExceptionListener. 
	  * @param listener is the SipProviderExceptionListener to be removed. */
	public synchronized void removeExceptionListener(SipProviderExceptionListener listener) {
		log(LogLevel.DEBUG,"removing a SipProviderExceptionListener");
		if (!exception_listeners.contains(listener)) {
			log(LogLevel.WARNING,"trying to remove a missed SipProviderExceptionListener.");
		}
		else {
			exception_listeners.removeElement(listener);
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
		if (log_all_packets || msg.getLength()>MIN_MESSAGE_LENGTH) log(LogLevel.INFO,"sendMessage()");
		log(LogLevel.TRACE,"message to send:\r\n"+MESSAGE_BEGIN_DELIMITER+msg.toString()+MESSAGE_END_DELIMITER);

		ConnectionId conn_id=msg.getConnectionId();
		if (conn_id!=null) {
			log(LogLevel.INFO,"trying to send message through connection "+conn_id);
		
			SipTransport sip_transport=(SipTransport)sip_transports.get(conn_id.getProtocol());
			if (sip_transport!=null)
			try {
				SipTransportConnection conn=((SipTransportCO)sip_transport).sendMessageCO(msg);
				// logs
				logMessage(conn.getProtocol(),conn.getRemoteAddress().toString(),conn.getRemotePort(),msg.getLength(),msg,"sent");
				return conn_id;
			}
			catch (IOException e) {
				log(LogLevel.INFO,e);
				log(LogLevel.WARNING,"failed when tried to send message through connection "+conn_id);
			}
			else {
				log(LogLevel.WARNING,"no support for protocol "+conn_id.getProtocol());
			}         
		}
		// else
		
		// destination address and port, proto, and ttl have to be set
		String dest_addr=null;
		int dest_port=0;
		String proto=null;
		String maddr=null;
		int ttl=0;
		
		if (msg.isRequest()) {
			// REQUESTS
			GenericURI nexthop_uri=outbound_proxy;
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
				nexthop_sip_uri=new SipURI(nexthop_uri);
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
			
			if (nexthop_sip_uri.isSecure()) proto=PROTO_TLS;
			else
			if (nexthop_sip_uri.hasTransport()) proto=nexthop_sip_uri.getTransport();
			else proto=getDefaultTransport();

			// for TLS and DTLS port=port+1
			if (dest_port>0 && isSecureTransport(proto)) dest_port++;

			// if not present, add via
			if (!msg.hasViaHeader()) {
				ViaHeader via=new ViaHeader(proto,via_addr,host_port);
				if (rport) via.setRport();
				via.setBranch(pickBranch());
				msg.addViaHeader(via);
			}
			// update the via according to transport information
			updateViaHeader(msg,proto,via_addr,host_port,maddr,ttl);
			
			log(LogLevel.DEBUG,"using transport "+proto);
		}
		else {
			// RESPONSES
			ViaHeader via=msg.getViaHeader();
			proto=via.getProtocol();
			SipURI uri=via.getSipURI();
			if (via.hasReceived()) dest_addr=via.getReceived(); else dest_addr=uri.getHost();
			//if (!isReliableTransport(via.getProtocol()) && via.hasRport()) dest_port=via.getRport();
			if (via.hasRport()) dest_port=via.getRport();
			if (dest_port<=0) dest_port=uri.getPort();
		}

		// if port <= use default port
		if (dest_port<=0) dest_port=(isSecureTransport(proto))? SipStack.default_port+1 : SipStack.default_port;

		return sendMessage(msg,proto,dest_addr,dest_port,ttl); 
	}


	/** Updates the top Via header field of a SIP message, according to the given transport information.
	  * @param msg the message to be updated
	  * @param proto the transport protocol
	  * @param via_addr the sent-by address of the via
	  * @param host_port the host port */
	public static void updateViaHeader(SipMessage msg, String proto, String via_addr, int host_port) {
		updateViaHeader(msg,proto,via_addr,host_port,null,-1);
	}


	/** Updates the top Via header field of a SIP message, according to the given transport information.
	  * @param msg the message to be updated
	  * @param proto the transport protocol
	  * @param via_addr the sent-by address of the via
	  * @param host_port the host port
	  * @param maddr the IP multicast address (if applicable) or null
	  * @param ttl the TTL for multicast (used only when parameter <i>maddr</i> is set) */
	public static void updateViaHeader(SipMessage msg, String proto, String via_addr, int host_port, String maddr, int ttl) {
		ViaHeader via=msg.getViaHeader();
		boolean via_changed=false;        
		// if sent-by differs, update the via header
		if (!via.getHost().equalsIgnoreCase(via_addr) || via.getPort()!=host_port) {
			boolean rport=via.hasRport();
			String branch=via.getBranch();
			via=new ViaHeader(proto,via_addr,host_port);
			if (rport) via.setRport();
			via.setBranch(branch);
			via_changed=true;
		}
		// if proto differs, update the proto of via header
		if (!via.getProtocol().equalsIgnoreCase(proto)) {
			via.setProtocol(proto);
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
				ViaHeader via=new ViaHeader(proto,via_addr,host_port);
				if (rport) via.setRport();
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
			if (log_all_packets || msg.getLength()>MIN_MESSAGE_LENGTH) log(LogLevel.DEBUG,"Resolving host address '"+dest_addr+"'");
			IpAddress dest_ipaddr=IpAddress.getByName(dest_addr);  
			return sendRawMessage(msg,proto,dest_ipaddr,dest_port,ttl); 
		}
		catch (Exception e) {
			log(LogLevel.INFO,e);
			return null;
		}     
	}


	/** Sends the <i>msg</i> message, specifing the transport protocol, nexthop address and port.
	  * For request messages, no via address is added. */
	private ConnectionId sendRawMessage(SipMessage msg, String proto, IpAddress dest_ipaddr, int dest_port, int ttl) {
		if (log_all_packets || msg.getLength()>MIN_MESSAGE_LENGTH) log(LogLevel.DEBUG,"Sending message to "+(new ConnectionId(proto,dest_ipaddr,dest_port)).toString());

		if (proto==null) {
			log(LogLevel.WARNING,"null protocol; message discarded");
			return null;
		}
		// else
		SipTransport sip_transport=(SipTransport)sip_transports.get(proto.toLowerCase());
		if (sip_transport==null) {
			log(LogLevel.WARNING,"unsupported protocol "+proto+"; message discarded");
			return null;
		}
		// else
		try {
			ConnectionId connection_id=sip_transport.sendMessage(msg,dest_ipaddr,dest_port,ttl);
			// logs
			logMessage(proto,dest_ipaddr.toString(),dest_port,msg.getLength(),msg,"sent");

			return connection_id;
		}
		catch (IOException e) {
			log(LogLevel.INFO,e);
			return null;
		}
	}


	/** Sends the <i>msg</i> message using the specified transport connection. */
	/*public ConnectionId sendMessage(SipMessage msg, ConnectionId conn_id) {
		if (log_all_packets || msg.getLength()>MIN_MESSAGE_LENGTH) log(LogLevel.INFO,"Sending message through conn "+conn_id);
		log(LogLevel.TRACE,"message to send:\r\n"+MESSAGE_BEGIN_DELIMITER+msg.toString()+MESSAGE_END_DELIMITER);
		SipTransportConnection conn=null;
		for (Enumeration e=sip_transports.elements(); e.hasMoreElements() && conn==null; ) {
			SipTransport transp=(SipTransport)e.nextElement();
			if (isReliableTransport(transp)) conn=((SipTransportCO)transp).sendMessage(msg,conn_id);
		}
		if (conn!=null) {
			// logs
			String proto=conn.getProtocol();
			log(LogLevel.DEBUG,"SipProvider: sendMessage(msg,conn): conn: "+conn);
			log(LogLevel.DEBUG,"SipProvider: sendMessage(msg,conn): remote_addr: "+conn.getRemoteAddress());
			String dest_addr=conn.getRemoteAddress().toString();
			int dest_port=conn.getRemotePort();
			logMessage(proto,dest_addr,dest_port,msg.getLength(),msg,"sent");
					
			return new ConnectionId(conn);
		}
		else {
			return sendMessage(msg);
		}
	}*/


	//************************* Callback methods *************************
	
	/** From SipTransportListener. When a new SIP message is received. */
	public void onReceivedMessage(SipTransport transport, SipMessage msg) {
		try {
			// logs
			logMessage(msg.getTransportProtocol(),msg.getRemoteAddress(),msg.getRemotePort(),msg.getLength(),msg,"received");
			
			// discard too short messages (e.g. CRLFCRLF "PING", or CRLF "PONG")
			if (msg.getLength()<=4) {
				if (log_all_packets) log(LogLevel.TRACE,"message too short: discarded\r\n");
				return;
			}
			// discard non-SIP messages
			String first_line=msg.getFirstLine();
			if (first_line==null || first_line.toUpperCase().indexOf("SIP/2.0")<0) {
				if (log_all_packets) log(LogLevel.TRACE,"NOT a SIP message: discarded\r\n");
				return;
			}
			log(LogLevel.INFO,"received new SIP message");
			log(LogLevel.TRACE,"received message:\r\n"+MESSAGE_BEGIN_DELIMITER+msg.toString()+MESSAGE_END_DELIMITER);
			
			// if a request, handle "received" and "rport" parameters
			if (msg.isRequest()) {
				ViaHeader vh=msg.getViaHeader();
				boolean via_changed=false;
				String src_addr=msg.getRemoteAddress();
				int src_port=msg.getRemotePort();
				String via_addr=vh.getHost();
				int via_port=vh.getPort();
				if (via_port<=0) via_port=SipStack.default_port;
				 
				if (!via_addr.equals(src_addr)) {
					vh.setReceived(src_addr);
					via_changed=true;
				}
				
				if (vh.hasRport()) {
					vh.setRport(src_port);
					via_changed=true;
				}
				else {
					if (force_rport && via_port!=src_port) {
						vh.setRport(src_port);
						via_changed=true;
					}
				}
				
				if (via_changed) {
					msg.removeViaHeader();
					msg.addViaHeader(vh);
				}
			}
			
			// is there any listeners?
			if (sip_listeners.size()==0 && promiscuous_listeners.size()==0) {
				log(LogLevel.INFO,"no listener found: meesage discarded.");
				return;
			}

			// try to look for listeners in promiscuous mode
			Vector promiscuous_listeners=getPromisquousListeners();
			for (int i=0; promiscuous_listeners!=null && i<promiscuous_listeners.size(); i++) {
				SipProviderListener listener=(SipProviderListener)promiscuous_listeners.elementAt(i);
				log(LogLevel.DEBUG,"message passed to promiscuous listener");
				listener.onReceivedMessage(this,msg);
			}
			
			// check if the message is still valid
			if (!msg.isRequest() && !msg.isResponse()) {
				log(LogLevel.INFO,"no valid SIP message: message discarded.");
				return;
			}

			// look for a specific listener
			SipProviderListener listener=getListener(msg);
			if (listener!=null) listener.onReceivedMessage(this,msg);
			else {
				// no listener_ID matched..
				log(LogLevel.INFO,"no listener found matching that message: message discarded.");
				log(LogLevel.DEBUG,"active listeners: "+sip_listeners.size());
			}
		}
		catch (Exception exception) {
			log(LogLevel.WARNING,"Error handling a new incoming message");
			log(LogLevel.DEBUG,exception);
			Vector exception_listeners=getExceptionListeners();
			for (int i=0; exception_listeners!=null && i<exception_listeners.size(); i++) {
				try {
					((SipProviderExceptionListener)exception_listeners.elementAt(i)).onMessageException(msg,exception);
				}
				catch (Exception e) {
					log(LogLevel.WARNING,"Error handling the Exception");
					log(LogLevel.DEBUG,e);
				}
			}
		}
	}


	/** Gets all promisquous listeners. */
	private synchronized Vector getPromisquousListeners() {
		if (promiscuous_listeners!=null && promiscuous_listeners.size()>0) {
			//return new Vector(promiscuous_listeners);
			return VectorUtils.copy(promiscuous_listeners);
		}
		// else
		return null;
	}


	/** Gets all exception listeners. */
	private synchronized Vector getExceptionListeners() {
		if (exception_listeners!=null && exception_listeners.size()>0) {
			//return new Vector(exception_listeners);
			return VectorUtils.copy(exception_listeners);
		}
		// else
		return null;
	}


	/** Gets a listener for a given message.
	 * @param msg the SIP message */
	private synchronized SipProviderListener getListener(SipMessage msg) {
		SipId key;
		// try to look for a transaction (requests go to transaction servers and response go to transaction clients)
		key=(msg.isRequest())? (SipId)new TransactionServerId(msg) : (SipId)new TransactionClientId(msg);
		log(LogLevel.DEBUG,"transaction-id: "+key);
		if (sip_listeners.containsKey(key)) {
			log(LogLevel.DEBUG,"message passed to transaction: "+key);
			return (SipProviderListener)sip_listeners.get(key);
		}
		// try to look for a dialog
		key=new DialogId(msg);
		log(LogLevel.DEBUG,"dialog-id: "+key);
		if (sip_listeners.containsKey(key)) {
			log(LogLevel.DEBUG,"message passed to dialog: "+key);
			return (SipProviderListener)sip_listeners.get(key);
		}
		// try to look for a UAS
		key=new MethodId(msg);
		if (sip_listeners.containsKey(key)) {
			log(LogLevel.DEBUG,"message passed to uas: "+key);
			return (SipProviderListener)sip_listeners.get(key);
		}        
		// try to look for a default UA
		if (sip_listeners.containsKey(MethodId.ANY)) {
			log(LogLevel.DEBUG,"message passed to uas: "+MethodId.ANY);
			return (SipProviderListener)sip_listeners.get(MethodId.ANY);
		}
		// else
		return null;
	}

	

	/** From SipTransportListener. When SipTransport terminates. */
	public void onTransportTerminated(SipTransport transport, Exception error) {
		log(LogLevel.DEBUG,"transport "+transport+" terminated");
		// TRY TO RESTART UDP WHEN ERRORS OCCUR
		if (error!=null && transport.getProtocol().equals(PROTO_UDP)) {
			log(LogLevel.INFO,"transport UDP terminated with error: trying to restart it (after 1000ms)..");
			try {  Thread.sleep(1000);  } catch (Exception e) {}
			try {
				SipTransport udp=new UdpTransport(host_port,binding_ipaddr);
				setTransport(udp);
			}
			catch (Exception e) {
				log(LogLevel.INFO,e);
			}
		}
	}   


	/** When a new incoming transport connection is established. It is called only for CO transport portocols. */ 
	public void onIncomingTransportConnection(SipTransport transport, SocketAddress remote_soaddr) {
		log(LogLevel.DEBUG,"incoming connection established with "+transport+":"+remote_soaddr);
	}


	/** When a transport connection terminates. */
	public void onTransportConnectionTerminated(SipTransport transport, SocketAddress remote_soaddr, Exception error) {
		log(LogLevel.DEBUG,"connection to "+transport+":"+remote_soaddr+" terminated");
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
		StringBuffer sb=new StringBuffer();
		sb.append(msg.getRequestLine().getAddress().toString());
		sb.append(getViaAddress()+getPort());
		ViaHeader top_via=msg.getViaHeader();
		if (top_via.hasBranch())
			sb.append(top_via.getBranch());
		else {
			sb.append(top_via.getHost()+top_via.getPort());
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
		return pickCallId(getViaAddress());
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
	public String toString() {
		if (binding_ipaddr==null) return host_port+"/"+transportProtocolsToString();
		else return binding_ipaddr.toString()+":"+host_port+"/"+transportProtocolsToString();
	}   


	//******************************* Logs *******************************

	/** Prints a message to the event log. */
	private final void log(LogLevel level, String message) {
		String id=(binding_ipaddr==null)? Integer.toString(host_port) : binding_ipaddr.toString()+":"+host_port;
		String tag="SipProvider-"+id+": ";
		if (event_logger!=null) event_logger.log(level,tag+message);
	}


	/** Prints an exception to the event log. */
	private final void log(LogLevel level, Exception e) {
		log(level,"Exception: "+ExceptionPrinter.getStackTraceOf(e));
	}


	/** Adds the SIP message to the message log. */
	private final void logMessage(String proto, String addr, int port, int len, SipMessage msg, String str) {
		if (log_all_packets || len>=MIN_MESSAGE_LENGTH) {
			if (message_logger!=null) {
				message_logger.log(getPacketTimestamp(proto,addr,port,len)+" "+str+"\r\n"+msg.toString()+MESSAGE_END_DELIMITER);
			}
			if (event_logger!=null) {
				String first_line=msg.getFirstLine();
				if (first_line!=null) first_line=first_line.trim(); else first_line="NOT a SIP message";
				event_logger.log(LogLevel.INFO,"");
				event_logger.log(LogLevel.INFO,getPacketTimestamp(proto,addr,port,len)+first_line+", "+str);
			}
		}
	}


	/** Gets a packet timestamp. */
	private static String getPacketTimestamp(String proto, String remote_addr, int remote_port, int len) {
		String str=remote_addr+":"+remote_port+"/"+proto+" ("+len+" bytes)";
		return DateFormat.formatHHmmssSSSEEEddMMMyyyy(new Date())+", "+str;
	}

}
