/*
 * Copyright (C) 2012 Luca Veltri - University of Parma - Italy
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

import org.mjsip.sip.address.SipURI;
import org.mjsip.sip.message.SipMethods;
import org.slf4j.LoggerFactory;
import org.zoolu.net.IpAddress;
import org.zoolu.util.Configure;
import org.zoolu.util.Parser;
import org.zoolu.util.Timer;



/** SIP configuration options. 
  * <p>
  * Options are: the default SIP port, deafult supported transport protocols,
  * timeouts, log configuration, etc.
  * </p>
  */
public class SipConfig extends Configure {
	
	private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(SipConfig.class);

	/** String value "auto-configuration" used for auto configuration of the host address */
	public static final String ALL_INTERFACES="ALL-INTERFACES";

	/** String value "auto-configuration" used for auto configuration of the host address */
	public static final String AUTO_CONFIGURATION="AUTO-CONFIGURATION";

	/** Default SIP port.
	 * Note that this is not the port used by the running stack, but simply the standard default SIP port.
	 * <br> Normally it sould be set to 5060 as defined by RFC 3261. Using a different value may cause
	 * some problems when interacting with other unaware SIP UAs. */
	public int defaultPort=5060; 
	/** Default SIP port for TLS transport (SIPS).
	 * Note that this is not the port used by the running stack, but simply the standard default SIPS port.
	 * <br> Normally it sould be set to 5061 as defined by RFC 3261. Using a different value may cause
	 * some problems when interacting with other unaware SIP UAs. */
	public int defaultTlsPort=5061; 
	/** Default supported transport protocols. */
	public String[] default_transport_protocols={ SipProvider.PROTO_UDP, SipProvider.PROTO_TCP };
	/** Default max number of contemporary open transport connections. */
	public int defaultMaxConnections=32;
	/** Whether adding 'rport' parameter on via header fields of outgoing requests. */
	public boolean useRport=true;
	/** Whether adding (forcing) 'rport' parameter on via header fields of incoming requests. */
	public boolean forceRport=false;

	// ********************* transaction timeouts *********************

	/** starting retransmission timeout (milliseconds); called T1 in RFC2361; they suggest T1=500ms */
	public long retransmissionTimeout=500;  
	/** maximum retransmission timeout (milliseconds); called T2 in RFC2361; they suggest T2=4sec */
	public long maxRetransmissionTimeout=4000;   
	/** transaction timeout (milliseconds); RFC2361 suggests 64*T1=32000ms */
	public long transaction_timeout=32000;    
	/** clearing timeout (milliseconds); T4 in RFC2361; they suggest T4=5sec */
	public long clearingTimeout=5000;

	// ******************** general configurations ********************

	/** default max-forwards value (RFC3261 recommends value 70) */
	public int maxForwards=70;
	/** Whether the default timer mode is 'daemon', or not.
	 * In 'daemon' mode, when all other threads terminate, the program also ends
	 * regardless the timer was still running, and no timeout callback is fired.
	 * In 'non-daemon' mode, the program ends only when all active timers have expired
	 * or explicitly halted. */
	public boolean timerDaemonMode=true;
	/** Whether at UAS side automatically sending (by default) a 100 Trying on INVITE. */
	public boolean autoTrying=true;
	/** Whether 1xx responses create an "early dialog" for methods that create dialog. */
	public boolean earlyDialog=true;
	/** Whether automatically sending PRACK messsages for incoming reliable 1xx responses in an INVITE dialog.
	  * <br> Note that if you set <i>true</i>, the PRACK messge are sent automatically without any message body.
	  * This may be in contrast with a possible offer/answer use of reliable 1xx response and PRACK. */
	public boolean autoPrack=false;
	/** Default 'expires' value in seconds. RFC2361 suggests 3600s as default value. */
	public int defaultExpires=3600;
	/** UA info included in request messages in the 'User-Agent' header field.
	  * Use "NONE" if the 'User-Agent' header filed must not be added. */
	public String uaInfo=SipStack.release;
	/** Server info included in response messages in the 'Server' header field
	  * Use "NONE" if the 'Server' header filed must not be added. */
	public String serverInfo=SipStack.release; 
	/** Supported option-tags for corresponding supported extensions. */
	public String[] supportedOptionTags={ SipStack.OTAG_100rel,SipStack.OTAG_timer }; //{ OTAG_100rel,OTAG_timer,OTAG_precondition };
	/** Required option-tags for corresponding required extensions. */
	public String[] requiredOptionTags=null; //{ OTAG_100rel,OTAG_timer };
	/** List of supported methods. */
	public String[] allowedMethods={ SipMethods.INVITE,SipMethods.ACK,SipMethods.OPTIONS,SipMethods.BYE,SipMethods.CANCEL,SipMethods.INFO,SipMethods.PRACK,SipMethods.NOTIFY,SipMethods.MESSAGE,SipMethods.UPDATE };
	/** Minimum session interval (Min-SE header field) for supporting "Session Timers" (RFC 4028). */
	public int minSessionInterval=90;
	/** Default session interval (Session-Expires header field) for supporting "Session Timers" (RFC 4028). */
	public int defaultSessionInterval=0;


	// ************** registration client configurations **************

	/** starting registration timeout (msecs) after a registration failure due to request timeout */
	public long regMinAttemptTimeout=60*1000; // 1min
	/** maximum registration timeout (msecs) after a registration failure due to request timeout */
	public long regMaxAttemptTimeout=900*1000; // 15min  
	/** maximum number of consecutive registration authentication attempts before giving up */
	public int regAuthAttempts=3;


	// ************************** extensions **************************

	/** Whether forcing this node to stay within the dialog route as peer,
	  * by means of the insertion of a RecordRoute header.
	  * This is a non-standard behaviour and is normally not necessary. */
	public boolean onDialogRoute=false;

	/** Whether using an alternative transaction id that does not include the 'sent-by' value. */
	//public boolean alternative_transaction_id=false;

	/** Via IP address or fully-qualified domanin name (FQDN).
	  * Use 'auto-configuration' for auto detection, or let it undefined. */
	String via_addr=null;

	/** Local SIP port */
	int host_port=0;

	/** Network interface (IP address) used by SIP for selective binding.
	  * Use 'ALL-INTERFACES' or let it undefined for binding SIP to all interfaces. */
	IpAddress bindingIpAddr=null;

	/** List of enabled transport protocols (the first protocol is used as default). */
	String[] transportProtocols=null;
	
	/** List of transport ports, ordered as the corresponding transport_protocols. */
	int[] transportPorts=null;

	/** Max number of (contemporary) open connections */
	int maxConnections=0;

	/** Outbound proxy URI ([sip:]host_addr[:host_port][;transport=proto]).
	  * Use 'NONE' for not using an outbound proxy (or let it undefined). */
	SipURI outboundProxy=null;

	/** Tel Gatway URI ([sip:]host_addr[:host_port][;transport=proto]).
	  * URI of a default SIP proxy/gateway that is used for sending request messages with a "tel" URI as request-uri.
	  * Use 'NONE' for not using a tel gateway (or let it undefined). */
	SipURI telGateway=null;

	/** Whether logging all packets (including non-SIP keepalive tokens). */
	boolean logAllPackets=false;


	/** For TLS. Whether all client and server certificates should be considered trusted.
	  * By default, trust_all={@link #default_tls_trust_all}. */
	boolean trustAll;

	/** For TLS. names of the files containing trusted certificates.
	  * The file names include the full path starting from the current working folder.
	  * By default, trust_all={@link SipConfig#default_tls_trusted_certs}. */
	String[] trustedCerts;

	/** For TLS. Path of the folder where trusted certificates are placed.
	  * All certificates (with file extension ".crt") found in this folder are considered trusted.
	  * By default, trust_folder={@link SipConfig#default_tls_trust_folder}. */
	String trustFolder;

	/** For TLS. Absolute file name of the certificate (containing the public key) of the local node.
	  * The file name includes the full path starting from the current working folder.
	  * By default, trust_folder={@link SipConfig#default_tls_cert_file}. */
	String certFile;

	/** For TLS. Absolute file name of the private key of the local node.
	  * The file name includes the full path starting from the current working folder.
	  * By default, trust_folder={@link SipConfig#default_tls_key_file}. */
	String keyFile;

	// for backward compatibility:

	/** Outbound proxy addr (for backward compatibility). */
	private String outboundAddr=null;

	/** Outbound proxy port (for backward compatibility). */
	private int outboundPort=-1;

	// ************************** constructor **************************

	/** Parses a single text line (read from the config file) */
	@Override
	public void setOption(String attribute, Parser par) {
		char[] delim={' ',','};

		// default sip provider configurations
		if (attribute.equals("default_port")) { defaultPort=par.getInt(); return; }
		if (attribute.equals("default_tls_port")) { defaultTlsPort=par.getInt(); return; }
		if (attribute.equals("default_transport_protocols")) { default_transport_protocols=par.getWordArray(delim); return; }
		if (attribute.equals("default_nmax_connections")) { defaultMaxConnections=par.getInt(); return; }
		if (attribute.equals("use_rport")) { useRport=(par.getString().toLowerCase().startsWith("y")); return; }
		if (attribute.equals("force_rport")) { forceRport=(par.getString().toLowerCase().startsWith("y")); return; }

		// transaction timeouts
		if (attribute.equals("retransmission_timeout")) { retransmissionTimeout=par.getInt(); return; }
		if (attribute.equals("max_retransmission_timeout")) { maxRetransmissionTimeout=par.getInt(); return; }
		if (attribute.equals("transaction_timeout")) { transaction_timeout=par.getInt(); return; }
		if (attribute.equals("clearing_timeout")) { clearingTimeout=par.getInt(); return; }

		// general configurations
		if (attribute.equals("max_forwards"))   { maxForwards=par.getInt(); return; }
		if (attribute.equals("timer_daemon_mode"))   { timerDaemonMode=(par.getString().toLowerCase().startsWith("y")); return; }
		if (attribute.equals("auto_trying"))    { autoTrying=(par.getString().toLowerCase().startsWith("y")); return; }
		if (attribute.equals("early_dialog"))   { earlyDialog=(par.getString().toLowerCase().startsWith("y")); return; }
		if (attribute.equals("default_expires")){ defaultExpires=par.getInt(); return; }
		if (attribute.equals("ua_info"))        { uaInfo=par.getRemainingString().trim(); return; }
		if (attribute.equals("server_info"))    { serverInfo=par.getRemainingString().trim(); return; }
		if (attribute.equals("supported_option_tags")) { supportedOptionTags=par.getWordArray(delim); return; }
		if (attribute.equals("required_option_tags"))  { requiredOptionTags=par.getWordArray(delim); return; }
		if (attribute.equals("allowed_methods"))       { allowedMethods=par.getWordArray(delim); return; }
		if (attribute.equals("min_session_interval"))  { minSessionInterval=par.getInt(); return; }
		if (attribute.equals("default_session_interval"))  { defaultSessionInterval=par.getInt(); return; }

		// registration client configurations
		if (attribute.equals("regc_min_attempt_timeout")) { regMinAttemptTimeout=par.getInt(); return; }
		if (attribute.equals("regc_max_attempt_timeout")) { regMaxAttemptTimeout=par.getInt(); return; }
		if (attribute.equals("regc_auth_attempts")) { regAuthAttempts=par.getInt(); return; }

		// extensions
		if (attribute.equals("on_dialog_route")){ onDialogRoute=(par.getString().toLowerCase().startsWith("y")); return; }
		//if (attribute.equals("alternative_transaction_id")){ alternative_transaction_id=(par.getString().toLowerCase().startsWith("y")); return; }

		// old parameters
		if (attribute.equals("host_addr")) LOG.warn("parameter 'host_addr' is no more supported; use 'via_addr' instead.");
		if (attribute.equals("all_interfaces")) LOG.warn("parameter 'all_interfaces' is no more supported; use 'host_iaddr' for setting a specific interface or let it undefined.");
		if (attribute.equals("use_outbound")) LOG.warn("parameter 'use_outbound' is no more supported; use 'outbound_addr' for setting an outbound proxy or let it undefined.");
		if (attribute.equals("log_file")) LOG.warn("parameter 'log_file' is no more supported.");

		if (attribute.equals("via_addr")) {  via_addr=par.getString(); return;  }
		if (attribute.equals("host_port")) {  host_port=par.getInt(); return;  }
		if (attribute.equals("binding_ipaddr")) {  setBindingIpAddress(par.getString()); return;  }
		if (attribute.equals("transport_protocols")) {  transportProtocols=par.getWordArray(delim); return;  }
		if (attribute.equals("transport_ports")) {  transportPorts=par.getIntArray(); return;  }
		if (attribute.equals("nmax_connections")) {  maxConnections=par.getInt(); return;  }
		if (attribute.equals("outbound_proxy")) {
			String str_uri=par.getString();
			if (str_uri==null || str_uri.length()==0 || str_uri.equalsIgnoreCase(Configure.NONE) || str_uri.equalsIgnoreCase("NO-OUTBOUND")) outboundProxy=null;
			else outboundProxy=new SipURI(str_uri);
			return;
		}
		if (attribute.equals("tel_gateway")) {
			String str_uri=par.getString();
			if (str_uri==null || str_uri.length()==0 || str_uri.equalsIgnoreCase(Configure.NONE)) telGateway=null;
			else telGateway=new SipURI(str_uri);
			return;
		}
		if (attribute.equals("log_all_packets")) { logAllPackets=(par.getString().toLowerCase().startsWith("y")); return; }

		// certificates
		if (attribute.equals("trust_all")){ trustAll=(par.getString().toLowerCase().startsWith("y")); return; }
		if (attribute.equals("trusted_certs")){ trustedCerts=par.getStringArray(); return; }
		if (attribute.equals("trust_folder")){ trustFolder=par.getRemainingString().trim(); return; }
		if (attribute.equals("cert_file")){ certFile=par.getRemainingString().trim(); return; }
		if (attribute.equals("key_file")){ keyFile=par.getRemainingString().trim(); return; }

		// old parameters
		if (attribute.equals("host_addr"))
			LOG.warn("parameter 'host_addr' is no more supported; use 'via_addr' instead.");
		if (attribute.equals("tls_port"))
			LOG.warn("parameter 'tls_port' is no more supported; use 'transport_ports' instead.");
		if (attribute.equals("all_interfaces"))
			LOG.warn(
					"parameter 'all_interfaces' is no more supported; use 'host_iaddr' for setting a specific interface or let it undefined.");
		if (attribute.equals("use_outbound"))
			LOG.warn(
					"parameter 'use_outbound' is no more supported; use 'outbound_proxy' for setting an outbound proxy or let it undefined.");
		if (attribute.equals("outbound_addr")) {
			LOG.warn(
					"parameter 'outbound_addr' has been deprecated; use 'outbound_proxy=[sip:]<host_addr>[:<host_port>][;transport=proto]' instead.");
			outboundAddr=par.getString();
			return;
		}
		if (attribute.equals("outbound_port")) {
			LOG.warn(
					"parameter 'outbound_port' has been deprecated; use 'outbound_proxy=<host_addr>[:<host_port>]' instead.");
			outboundPort=par.getInt();
			return;
		}
		if (attribute.equals("host_ifaddr")) {
			LOG.warn("parameter 'host_ifaddr' has been deprecated; use 'binding_ipaddr' instead.");
			setBindingIpAddress(par.getString());
			return;
		}
	}  

	/** Sets the binding IP address . */
	private void setBindingIpAddress(String str) {
		if (str==null || str.equalsIgnoreCase(ALL_INTERFACES)) bindingIpAddr=null;
		else {
			try {
				bindingIpAddr=IpAddress.getByName(str);
			}
			catch (IOException e) {
				LOG.warn("Unable to set the following binding address: " + str, e);
			}
		}
	}

	private SipConfig() {
		super();
	}

	/** Creates {@link SipConfig} from the specified <i>file</i> */
	public static SipConfig init(String file) {
		LOG.info("Loading SIP configuration from: " + file);
		
		SipConfig result = new SipConfig();
		result.loadFile(file);
		result.normalize();
		
		// timers
		Timer.DEFAULT_DAEMON_MODE=result.timerDaemonMode;
		
		return result;
	}

	/** Inits the SipProvider, initializing the SipProviderListeners, the transport protocols, the outbound proxy, and other attributes. */ 
	public void update(String via_addr, int host_port) {
		this.via_addr=via_addr;
		this.host_port=host_port;
	}

	private void normalize() {
		// user-agent info
		if (uaInfo!=null && (uaInfo.length()==0 || uaInfo.equalsIgnoreCase(Configure.NONE) || uaInfo.equalsIgnoreCase("NO-UA-INFO"))) uaInfo=null;      

		// server info
		if (serverInfo!=null && (serverInfo.length()==0 || serverInfo.equalsIgnoreCase(Configure.NONE) || serverInfo.equalsIgnoreCase("NO-SERVER-INFO"))) serverInfo=null;      

		// just for backward compatibility..
		if (outboundPort<0) outboundPort=defaultPort;
		
		if (outboundAddr!=null) {
			if (outboundAddr.equalsIgnoreCase(Configure.NONE) || outboundAddr.equalsIgnoreCase("NO-OUTBOUND")) outboundProxy=null;
			else outboundProxy=new SipURI(outboundAddr,outboundPort);
		}
		
		if (via_addr==null || via_addr.equalsIgnoreCase(AUTO_CONFIGURATION)) via_addr=IpAddress.getLocalHostAddress().toString();
		if (host_port<=0) host_port=defaultPort;
	}
}
