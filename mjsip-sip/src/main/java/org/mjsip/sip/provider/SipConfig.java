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
	public int default_port=5060; 
	/** Default SIP port for TLS transport (SIPS).
	 * Note that this is not the port used by the running stack, but simply the standard default SIPS port.
	 * <br> Normally it sould be set to 5061 as defined by RFC 3261. Using a different value may cause
	 * some problems when interacting with other unaware SIP UAs. */
	public int default_tls_port=5061; 
	/** Default supported transport protocols. */
	public String[] default_transport_protocols={ SipProvider.PROTO_UDP, SipProvider.PROTO_TCP };
	/** Default max number of contemporary open transport connections. */
	public int default_nmax_connections=32;
	/** Whether adding 'rport' parameter on via header fields of outgoing requests. */
	public boolean use_rport=true;
	/** Whether adding (forcing) 'rport' parameter on via header fields of incoming requests. */
	public boolean force_rport=false;

	// ********************* transaction timeouts *********************

	/** starting retransmission timeout (milliseconds); called T1 in RFC2361; they suggest T1=500ms */
	public long retransmission_timeout=500;  
	/** maximum retransmission timeout (milliseconds); called T2 in RFC2361; they suggest T2=4sec */
	public long max_retransmission_timeout=4000;   
	/** transaction timeout (milliseconds); RFC2361 suggests 64*T1=32000ms */
	public long transaction_timeout=32000;    
	/** clearing timeout (milliseconds); T4 in RFC2361; they suggest T4=5sec */
	public long clearing_timeout=5000;

	// ******************** general configurations ********************

	/** default max-forwards value (RFC3261 recommends value 70) */
	public int max_forwards=70;
	/** Whether the default timer mode is 'daemon', or not.
	 * In 'daemon' mode, when all other threads terminate, the program also ends
	 * regardless the timer was still running, and no timeout callback is fired.
	 * In 'non-daemon' mode, the program ends only when all active timers have expired
	 * or explicitly halted. */
	public boolean timer_daemon_mode=true;
	/** Whether at UAS side automatically sending (by default) a 100 Trying on INVITE. */
	public boolean auto_trying=true;
	/** Whether 1xx responses create an "early dialog" for methods that create dialog. */
	public boolean early_dialog=true;
	/** Whether automatically sending PRACK messsages for incoming reliable 1xx responses in an INVITE dialog.
	  * <br> Note that if you set <i>true</i>, the PRACK messge are sent automatically without any message body.
	  * This may be in contrast with a possible offer/answer use of reliable 1xx response and PRACK. */
	public boolean auto_prack=false;
	/** Default 'expires' value in seconds. RFC2361 suggests 3600s as default value. */
	public int default_expires=3600;
	/** UA info included in request messages in the 'User-Agent' header field.
	  * Use "NONE" if the 'User-Agent' header filed must not be added. */
	public String ua_info=SipStack.release;
	/** Server info included in response messages in the 'Server' header field
	  * Use "NONE" if the 'Server' header filed must not be added. */
	public String server_info=SipStack.release; 
	/** Supported option-tags for corresponding supported extensions. */
	public String[] supported_option_tags={ SipStack.OTAG_100rel,SipStack.OTAG_timer }; //{ OTAG_100rel,OTAG_timer,OTAG_precondition };
	/** Required option-tags for corresponding required extensions. */
	public String[] required_option_tags=null; //{ OTAG_100rel,OTAG_timer };
	/** List of supported methods. */
	public String[] allowed_methods={ SipMethods.INVITE,SipMethods.ACK,SipMethods.OPTIONS,SipMethods.BYE,SipMethods.CANCEL,SipMethods.INFO,SipMethods.PRACK,SipMethods.NOTIFY,SipMethods.MESSAGE,SipMethods.UPDATE };
	/** Minimum session interval (Min-SE header field) for supporting "Session Timers" (RFC 4028). */
	public int min_session_interval=90;
	/** Default session interval (Session-Expires header field) for supporting "Session Timers" (RFC 4028). */
	public int default_session_interval=0;


	// ************** registration client configurations **************

	/** starting registration timeout (msecs) after a registration failure due to request timeout */
	public long regc_min_attempt_timeout=60*1000; // 1min
	/** maximum registration timeout (msecs) after a registration failure due to request timeout */
	public long regc_max_attempt_timeout=900*1000; // 15min  
	/** maximum number of consecutive registration authentication attempts before giving up */
	public int regc_auth_attempts=3;


	// ************************** extensions **************************

	/** Whether forcing this node to stay within the dialog route as peer,
	  * by means of the insertion of a RecordRoute header.
	  * This is a non-standard behaviour and is normally not necessary. */
	public boolean on_dialog_route=false;

	/** Whether using an alternative transaction id that does not include the 'sent-by' value. */
	//public boolean alternative_transaction_id=false;

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
	boolean trust_all;

	/** For TLS. names of the files containing trusted certificates.
	  * The file names include the full path starting from the current working folder.
	  * By default, trust_all={@link SipConfig#default_tls_trusted_certs}. */
	String[] trusted_certs;

	/** For TLS. Path of the folder where trusted certificates are placed.
	  * All certificates (with file extension ".crt") found in this folder are considered trusted.
	  * By default, trust_folder={@link SipConfig#default_tls_trust_folder}. */
	String trust_folder;

	/** For TLS. Absolute file name of the certificate (containing the public key) of the local node.
	  * The file name includes the full path starting from the current working folder.
	  * By default, trust_folder={@link SipConfig#default_tls_cert_file}. */
	String cert_file;

	/** For TLS. Absolute file name of the private key of the local node.
	  * The file name includes the full path starting from the current working folder.
	  * By default, trust_folder={@link SipConfig#default_tls_key_file}. */
	String key_file;

	// for backward compatibility:

	/** Outbound proxy addr (for backward compatibility). */
	private String outbound_addr=null;

	/** Outbound proxy port (for backward compatibility). */
	private int outbound_port=-1;

	// ************************** constructor **************************

	/** Parses a single text line (read from the config file) */
	@Override
	protected void parseLine(String line) {
		String attribute;
		Parser par;
		int index=line.indexOf("=");
		if (index>0) {  attribute=line.substring(0,index).trim(); par=new Parser(line,index+1);  }
		else {  attribute=line; par=new Parser("");  }
		char[] delim={' ',','};

		// default sip provider configurations
		if (attribute.equals("default_port")) { default_port=par.getInt(); return; }
		if (attribute.equals("default_tls_port")) { default_tls_port=par.getInt(); return; }
		if (attribute.equals("default_transport_protocols")) { default_transport_protocols=par.getWordArray(delim); return; }
		if (attribute.equals("default_nmax_connections")) { default_nmax_connections=par.getInt(); return; }
		if (attribute.equals("use_rport")) { use_rport=(par.getString().toLowerCase().startsWith("y")); return; }
		if (attribute.equals("force_rport")) { force_rport=(par.getString().toLowerCase().startsWith("y")); return; }

		// transaction timeouts
		if (attribute.equals("retransmission_timeout")) { retransmission_timeout=par.getInt(); return; }
		if (attribute.equals("max_retransmission_timeout")) { max_retransmission_timeout=par.getInt(); return; }
		if (attribute.equals("transaction_timeout")) { transaction_timeout=par.getInt(); return; }
		if (attribute.equals("clearing_timeout")) { clearing_timeout=par.getInt(); return; }

		// general configurations
		if (attribute.equals("max_forwards"))   { max_forwards=par.getInt(); return; }
		if (attribute.equals("timer_daemon_mode"))   { timer_daemon_mode=(par.getString().toLowerCase().startsWith("y")); return; }
		if (attribute.equals("auto_trying"))    { auto_trying=(par.getString().toLowerCase().startsWith("y")); return; }
		if (attribute.equals("early_dialog"))   { early_dialog=(par.getString().toLowerCase().startsWith("y")); return; }
		if (attribute.equals("default_expires")){ default_expires=par.getInt(); return; }
		if (attribute.equals("ua_info"))        { ua_info=par.getRemainingString().trim(); return; }
		if (attribute.equals("server_info"))    { server_info=par.getRemainingString().trim(); return; }
		if (attribute.equals("supported_option_tags")) { supported_option_tags=par.getWordArray(delim); return; }
		if (attribute.equals("required_option_tags"))  { required_option_tags=par.getWordArray(delim); return; }
		if (attribute.equals("allowed_methods"))       { allowed_methods=par.getWordArray(delim); return; }
		if (attribute.equals("min_session_interval"))  { min_session_interval=par.getInt(); return; }
		if (attribute.equals("default_session_interval"))  { default_session_interval=par.getInt(); return; }

		// registration client configurations
		if (attribute.equals("regc_min_attempt_timeout")) { regc_min_attempt_timeout=par.getInt(); return; }
		if (attribute.equals("regc_max_attempt_timeout")) { regc_max_attempt_timeout=par.getInt(); return; }
		if (attribute.equals("regc_auth_attempts")) { regc_auth_attempts=par.getInt(); return; }

		// extensions
		if (attribute.equals("on_dialog_route")){ on_dialog_route=(par.getString().toLowerCase().startsWith("y")); return; }
		//if (attribute.equals("alternative_transaction_id")){ alternative_transaction_id=(par.getString().toLowerCase().startsWith("y")); return; }

		// old parameters
		if (attribute.equals("host_addr")) LOG.warn("parameter 'host_addr' is no more supported; use 'via_addr' instead.");
		if (attribute.equals("all_interfaces")) LOG.warn("parameter 'all_interfaces' is no more supported; use 'host_iaddr' for setting a specific interface or let it undefined.");
		if (attribute.equals("use_outbound")) LOG.warn("parameter 'use_outbound' is no more supported; use 'outbound_addr' for setting an outbound proxy or let it undefined.");
		if (attribute.equals("log_file")) LOG.warn("parameter 'log_file' is no more supported.");

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
			outbound_addr=par.getString();
			return;
		}
		if (attribute.equals("outbound_port")) {
			LOG.warn(
					"parameter 'outbound_port' has been deprecated; use 'outbound_proxy=<host_addr>[:<host_port>]' instead.");
			outbound_port=par.getInt();
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
		if (str==null || str.equalsIgnoreCase(ALL_INTERFACES)) binding_ipaddr=null;
		else {
			try {
				binding_ipaddr=IpAddress.getByName(str);
			}
			catch (IOException e) {
				LOG.warn("Unable to set the following binding address: " + str, e);
			}
		}
	}

	/** Converts the entire object into lines (to be saved into the config file) */
	@Override
	protected String toLines() {
		// currently not implemented..
		return "SipStack/"+SipStack.release;
	}
 
	private SipConfig() {
		super();
	}

	/** Creates a {@link SipConfig}. */
	public static SipConfig init() {
		return init(null);
	}

	/** Inits SipStack from the specified <i>file</i> */
	public static SipConfig init(String file) {
		SipConfig result = new SipConfig();
		result.loadFile(file);
		result.normalize();
		
		// timers
		Timer.DEFAULT_DAEMON_MODE=result.timer_daemon_mode;
		
		return result;
	}

	/** Inits the SipProvider, initializing the SipProviderListeners, the transport protocols, the outbound proxy, and other attributes. */ 
	public void update(String via_addr, int host_port) {
		if (via_addr==null || via_addr.equalsIgnoreCase(AUTO_CONFIGURATION)) via_addr=IpAddress.getLocalHostAddress().toString();
		this.via_addr=via_addr;

		if (host_port<=0) host_port=default_port;
		this.host_port=host_port;
	}

	private void normalize() {
		// user-agent info
		if (ua_info!=null && (ua_info.length()==0 || ua_info.equalsIgnoreCase(Configure.NONE) || ua_info.equalsIgnoreCase("NO-UA-INFO"))) ua_info=null;      

		// server info
		if (server_info!=null && (server_info.length()==0 || server_info.equalsIgnoreCase(Configure.NONE) || server_info.equalsIgnoreCase("NO-SERVER-INFO"))) server_info=null;      

		// just for backward compatibility..
		if (outbound_port<0) outbound_port=default_port;
		
		if (outbound_addr!=null) {
			if (outbound_addr.equalsIgnoreCase(Configure.NONE) || outbound_addr.equalsIgnoreCase("NO-OUTBOUND")) outbound_proxy=null;
			else outbound_proxy=new SipURI(outbound_addr,outbound_port);
		}
	}
}
