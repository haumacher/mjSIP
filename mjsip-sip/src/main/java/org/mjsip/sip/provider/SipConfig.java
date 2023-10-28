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
import org.zoolu.util.Flags;
import org.zoolu.util.Parser;



/**
 * SIP configuration options.
 * <p>
 * Options are: the default SIP port, deafult supported transport protocols, timeouts, log
 * configuration, etc.
 * </p>
 */
public class SipConfig extends Configure implements SipOptions {
	
	private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(SipConfig.class);

	/** String value "auto-configuration" used for auto configuration of the host address */
	public static final String ALL_INTERFACES="ALL-INTERFACES";

	/** String value "auto-configuration" used for auto configuration of the host address */
	public static final String AUTO_CONFIGURATION="AUTO-CONFIGURATION";

	/** @see #getDefaultPort() */
	private int _defaultPort=5060; 

	/** @see #getDefaultTlsPort() */
	private int _defaultTlsPort = 5061;

	/** @see #getDefaultTransportProtocols() */
	private String[] _defaultTransportProtocols={ SipProvider.PROTO_UDP, SipProvider.PROTO_TCP };

	/** @see #getDefaultMaxConnections() */
	private int _defaultMaxConnections=32;

	/** @see #useRport() */
	private boolean _useRport = true;

	/** @see #forceRport() */
	private boolean _forceRport = false;

	// ********************* transaction timeouts *********************

	/** @see #getRetransmissionTimeout() */
	private long _retransmissionTimeout = 500;

	/** @see #getMaxRetransmissionTimeout() */
	private long _maxRetransmissionTimeout = 4000;

	/** @see #getTransactionTimeout() */
	private long _transactionTimeout = 32000;

	/** @see #getClearingTimeout() */
	private long _clearingTimeout = 5000;

	// ******************** general configurations ********************

	private int _maxForwards = 70;

	private boolean _autoTrying=true;
	private boolean _earlyDialog=true;
	private boolean _autoPrack=false;
	private int _defaultExpires=3600;
	private String _uaInfo=SipStack.release;
	private String _serverInfo=SipStack.release; 
	private String[] _supportedOptionTags={ SipStack.OTAG_100rel,SipStack.OTAG_timer }; //{ OTAG_100rel,OTAG_timer,OTAG_precondition };
	private String[] _requiredOptionTags=null; //{ OTAG_100rel,OTAG_timer };
	private String[] _allowedMethods={ SipMethods.INVITE,SipMethods.ACK,SipMethods.OPTIONS,SipMethods.BYE,SipMethods.CANCEL,SipMethods.INFO,SipMethods.PRACK,SipMethods.NOTIFY,SipMethods.MESSAGE,SipMethods.UPDATE };
	private int _minSessionInterval=90;
	private int _defaultSessionInterval=0;


	// ************** registration client configurations **************

	private long _regMinAttemptTimeout=60*1000; // 1min
	private long _regMaxAttemptTimeout=900*1000; // 15min  
	private int _regAuthAttempts=3;


	// ************************** extensions **************************

	private boolean _onDialogRoute=false;

	/** Whether using an alternative transaction id that does not include the 'sent-by' value. */
	//public boolean alternative_transaction_id=false;

	private String _viaAddr = null;

	private int _hostPort=0;

	private IpAddress _bindingIpAddr = null;

	private String[] _transportProtocols = null;
	
	private int[] _transportPorts = null;

	private int _maxConnections = 0;

	private SipURI _outboundProxy = null;

	private SipURI _telGateway = null;

	private boolean _logAllPackets = false;


	private boolean _trustAll;

	private String[] _trustedCerts;

	private String _trustFolder;

	private String _certFile;

	private String _keyFile;

	// for backward compatibility:

	private String _outboundAddr = null;

	private int _outboundPort = -1;

	// ************************** constructor **************************

	/** Parses a single text line (read from the config file) */
	@Override
	public void setOption(String attribute, Parser par) {
		char[] delim={' ',','};

		// default sip provider configurations
		if (attribute.equals("default_port")) { setDefaultPort(par.getInt()); return; }
		if (attribute.equals("default_tls_port")) { setDefaultTlsPort(par.getInt()); return; }
		if (attribute.equals("default_transport_protocols")) { setDefaultTransportProtocols(par.getWordArray(delim)); return; }
		if (attribute.equals("default_nmax_connections")) { setDefaultMaxConnections(par.getInt()); return; }
		if (attribute.equals("use_rport")) { setUseRport((par.getString().toLowerCase().startsWith("y"))); return; }
		if (attribute.equals("force_rport")) {
			setForceRport((par.getString().toLowerCase().startsWith("y")));
			return;
		}

		// transaction timeouts
		if (attribute.equals("retransmission_timeout")) {
			setRetransmissionTimeout(par.getInt());
			return;
		}
		if (attribute.equals("max_retransmission_timeout")) {
			setMaxRetransmissionTimeout(par.getInt());
			return;
		}
		if (attribute.equals("transaction_timeout")) {
			setTransactionTimeout(par.getInt());
			return;
		}
		if (attribute.equals("clearing_timeout")) {
			setClearingTimeout(par.getInt());
			return;
		}

		// general configurations
		if (attribute.equals("max_forwards"))   { setMaxForwards(par.getInt()); return; }
		if (attribute.equals("auto_trying"))    { setAutoTrying((par.getString().toLowerCase().startsWith("y"))); return; }
		if (attribute.equals("early_dialog"))   { setEarlyDialog((par.getString().toLowerCase().startsWith("y"))); return; }
		if (attribute.equals("default_expires")){ setDefaultExpires(par.getInt()); return; }
		if (attribute.equals("ua_info"))        { setUaInfo(par.getRemainingString().trim()); return; }
		if (attribute.equals("server_info"))    { setServerInfo(par.getRemainingString().trim()); return; }
		if (attribute.equals("supported_option_tags")) { setSupportedOptionTags(par.getWordArray(delim)); return; }
		if (attribute.equals("required_option_tags"))  { setRequiredOptionTags(par.getWordArray(delim)); return; }
		if (attribute.equals("allowed_methods"))       { setAllowedMethods(par.getWordArray(delim)); return; }
		if (attribute.equals("min_session_interval"))  { setMinSessionInterval(par.getInt()); return; }
		if (attribute.equals("default_session_interval"))  { setDefaultSessionInterval(par.getInt()); return; }

		// registration client configurations
		if (attribute.equals("regc_min_attempt_timeout")) { setRegMinAttemptTimeout(par.getInt()); return; }
		if (attribute.equals("regc_max_attempt_timeout")) { setRegMaxAttemptTimeout(par.getInt()); return; }
		if (attribute.equals("regc_auth_attempts")) { setRegAuthAttempts(par.getInt()); return; }

		// extensions
		if (attribute.equals("on_dialog_route")){ setOnDialogRoute((par.getString().toLowerCase().startsWith("y"))); return; }
		//if (attribute.equals("alternative_transaction_id")){ alternative_transaction_id=(par.getString().toLowerCase().startsWith("y")); return; }

		// old parameters
		if (attribute.equals("host_addr")) LOG.warn("parameter 'host_addr' is no more supported; use 'via_addr' instead.");
		if (attribute.equals("all_interfaces")) LOG.warn("parameter 'all_interfaces' is no more supported; use 'host_iaddr' for setting a specific interface or let it undefined.");
		if (attribute.equals("use_outbound")) LOG.warn("parameter 'use_outbound' is no more supported; use 'outbound_addr' for setting an outbound proxy or let it undefined.");
		if (attribute.equals("log_file")) LOG.warn("parameter 'log_file' is no more supported.");

		if (attribute.equals("via_addr")) {
			setViaAddr(par.getString());
			return;
		}
		if (attribute.equals("host_port")) {  setHostPort(par.getInt()); return;  }
		if (attribute.equals("binding_ipaddr")) {  setBindingIpAddress(par.getString()); return;  }
		if (attribute.equals("transport_protocols")) {  setTransportProtocols(par.getWordArray(delim)); return;  }
		if (attribute.equals("transport_ports")) {  setTransportPorts(par.getIntArray()); return;  }
		if (attribute.equals("nmax_connections")) {  setMaxConnections(par.getInt()); return;  }
		if (attribute.equals("outbound_proxy")) {
			String str_uri=par.getString();
			if (str_uri==null || str_uri.length()==0 || str_uri.equalsIgnoreCase(Configure.NONE) || str_uri.equalsIgnoreCase("NO-OUTBOUND")) setOutboundProxy(null);
			else setOutboundProxy(new SipURI(str_uri));
			return;
		}
		if (attribute.equals("tel_gateway")) {
			String str_uri=par.getString();
			if (str_uri==null || str_uri.length()==0 || str_uri.equalsIgnoreCase(Configure.NONE)) setTelGateway(null);
			else setTelGateway(new SipURI(str_uri));
			return;
		}
		if (attribute.equals("log_all_packets")) { setLogAllPackets((par.getString().toLowerCase().startsWith("y"))); return; }

		// certificates
		if (attribute.equals("trust_all")){ setTrustAll((par.getString().toLowerCase().startsWith("y"))); return; }
		if (attribute.equals("trusted_certs")){ setTrustedCerts(par.getStringArray()); return; }
		if (attribute.equals("trust_folder")){ setTrustFolder(par.getRemainingString().trim()); return; }
		if (attribute.equals("cert_file")){ setCertFile(par.getRemainingString().trim()); return; }
		if (attribute.equals("key_file")){ setKeyFile(par.getRemainingString().trim()); return; }

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
			setOutboundAddr(par.getString());
			return;
		}
		if (attribute.equals("outbound_port")) {
			LOG.warn(
					"parameter 'outbound_port' has been deprecated; use 'outbound_proxy=<host_addr>[:<host_port>]' instead.");
			setOutboundPort(par.getInt());
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
		if (str==null || str.equalsIgnoreCase(ALL_INTERFACES)) setBindingIpAddr(null);
		else {
			try {
				setBindingIpAddr(IpAddress.getByName(str));
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
		
		return result;
	}

	/** Creates {@link SipConfig} from the specified file and program arguments */
	public static SipConfig init(String config_file, Flags flags) {
		SipConfig sipConfig = init(config_file);
		
		int host_port=flags.getInteger("-p","<port>",sipConfig.getDefaultPort(),"local SIP port, used ONLY without -f option");
		String via_addr=flags.getString("--via-addr","<addr>",AUTO_CONFIGURATION,"host via address, used ONLY without -f option");
	
		String outbound_proxy=flags.getString("-o","<addr>[:<port>]",null,"uses the given outbound proxy");
		if (outbound_proxy!=null) {
			sipConfig.setOutboundProxy(new SipURI(outbound_proxy));
		}
	
		String contact_uri=flags.getString("--contact-uri","<uri>",null,"user's contact URI");
		
		String transport=flags.getString("--transport","<proto>",null,"use the given transport protocol for SIP");
		if (transport!=null) {
			sipConfig.setTransportProtocols(new String[]{transport});
		}
		
		// init sip_provider
		if (config_file==null) {
			sipConfig.update(via_addr, host_port);
		}
		return sipConfig;
	}

	/** Inits the SipProvider, initializing the SipProviderListeners, the transport protocols, the outbound proxy, and other attributes. */ 
	public void update(String via_addr, int host_port) {
		this.setViaAddr(via_addr);
		this.setHostPort(host_port);
	}

	private void normalize() {
		// user-agent info
		if (getUaInfo()!=null && (getUaInfo().length()==0 || getUaInfo().equalsIgnoreCase(Configure.NONE) || getUaInfo().equalsIgnoreCase("NO-UA-INFO"))) setUaInfo(null);      

		// server info
		if (getServerInfo()!=null && (getServerInfo().length()==0 || getServerInfo().equalsIgnoreCase(Configure.NONE) || getServerInfo().equalsIgnoreCase("NO-SERVER-INFO"))) setServerInfo(null);      

		// just for backward compatibility..
		if (getOutboundPort()<0) setOutboundPort(getDefaultPort());
		
		if (getOutboundAddr()!=null) {
			if (getOutboundAddr().equalsIgnoreCase(Configure.NONE) || getOutboundAddr().equalsIgnoreCase("NO-OUTBOUND")) setOutboundProxy(null);
			else setOutboundProxy(new SipURI(getOutboundAddr(),getOutboundPort()));
		}
		
		if (getViaAddr() == null || getViaAddr().equalsIgnoreCase(AUTO_CONFIGURATION)) {
			setViaAddr(IpAddress.getLocalHostAddress().toString());
		}
		if (getHostPort() <= 0) {
			setHostPort(getDefaultPort());
		}

		if (getTransportProtocols() == null) {
			setTransportProtocols(getDefaultTransportProtocols());
		}
		if (getMaxConnections() <= 0) {
			setMaxConnections(getDefaultMaxConnections());
		}
	}

	@Override
	public int getDefaultPort() {
		return _defaultPort;
	}

	private void setDefaultPort(int defaultPort) {
		this._defaultPort = defaultPort;
	}

	@Override
	public int getDefaultTlsPort() {
		return _defaultTlsPort;
	}

	private void setDefaultTlsPort(int defaultTlsPort) {
		this._defaultTlsPort = defaultTlsPort;
	}

	@Override
	public String[] getDefaultTransportProtocols() {
		return _defaultTransportProtocols;
	}

	private void setDefaultTransportProtocols(String[] defaultTransportProtocols) {
		_defaultTransportProtocols = defaultTransportProtocols;
	}

	/** Default max number of contemporary open transport connections. */
	public int getDefaultMaxConnections() {
		return _defaultMaxConnections;
	}

	private void setDefaultMaxConnections(int defaultMaxConnections) {
		this._defaultMaxConnections = defaultMaxConnections;
	}

	@Override
	public boolean useRport() {
		return _useRport;
	}

	private void setUseRport(boolean useRport) {
		this._useRport = useRport;
	}

	/** Whether adding (forcing) 'rport' parameter on via header fields of incoming requests. */
	public boolean forceRport() {
		return _forceRport;
	}

	private void setForceRport(boolean forceRport) {
		this._forceRport = forceRport;
	}

	@Override
	public long getRetransmissionTimeout() {
		return _retransmissionTimeout;
	}

	private void setRetransmissionTimeout(long retransmissionTimeout) {
		this._retransmissionTimeout = retransmissionTimeout;
	}

	@Override
	public long getMaxRetransmissionTimeout() {
		return _maxRetransmissionTimeout;
	}

	private void setMaxRetransmissionTimeout(long maxRetransmissionTimeout) {
		this._maxRetransmissionTimeout = maxRetransmissionTimeout;
	}

	@Override
	public long getTransactionTimeout() {
		return _transactionTimeout;
	}

	private void setTransactionTimeout(long transaction_timeout) {
		this._transactionTimeout = transaction_timeout;
	}

	@Override
	public long getClearingTimeout() {
		return _clearingTimeout;
	}

	private void setClearingTimeout(long clearingTimeout) {
		this._clearingTimeout = clearingTimeout;
	}

	@Override
	public int getMaxForwards() {
		return _maxForwards;
	}

	private void setMaxForwards(int maxForwards) {
		this._maxForwards = maxForwards;
	}

	@Override
	public boolean isAutoTrying() {
		return _autoTrying;
	}

	private void setAutoTrying(boolean autoTrying) {
		_autoTrying = autoTrying;
	}

	@Override
	public boolean isEarlyDialog() {
		return _earlyDialog;
	}

	private void setEarlyDialog(boolean earlyDialog) {
		this._earlyDialog = earlyDialog;
	}

	@Override
	public boolean isAutoPrack() {
		return _autoPrack;
	}

	/** @see #isAutoPrack() */
	public void setAutoPrack(boolean autoPrack) {
		this._autoPrack = autoPrack;
	}

	@Override
	public int getDefaultExpires() {
		return _defaultExpires;
	}

	private void setDefaultExpires(int defaultExpires) {
		this._defaultExpires = defaultExpires;
	}

	@Override
	public String getUaInfo() {
		return _uaInfo;
	}

	private void setUaInfo(String uaInfo) {
		this._uaInfo = uaInfo;
	}

	@Override
	public String getServerInfo() {
		return _serverInfo;
	}

	private void setServerInfo(String serverInfo) {
		this._serverInfo = serverInfo;
	}

	@Override
	public String[] getSupportedOptionTags() {
		return _supportedOptionTags;
	}

	private void setSupportedOptionTags(String[] supportedOptionTags) {
		this._supportedOptionTags = supportedOptionTags;
	}

	@Override
	public String[] getRequiredOptionTags() {
		return _requiredOptionTags;
	}

	private void setRequiredOptionTags(String[] requiredOptionTags) {
		_requiredOptionTags = requiredOptionTags;
	}

	@Override
	public String[] getAllowedMethods() {
		return _allowedMethods;
	}

	private void setAllowedMethods(String[] allowedMethods) {
		this._allowedMethods = allowedMethods;
	}

	@Override
	public int getMinSessionInterval() {
		return _minSessionInterval;
	}

	private void setMinSessionInterval(int minSessionInterval) {
		this._minSessionInterval = minSessionInterval;
	}

	@Override
	public int getDefaultSessionInterval() {
		return _defaultSessionInterval;
	}

	private void setDefaultSessionInterval(int defaultSessionInterval) {
		this._defaultSessionInterval = defaultSessionInterval;
	}

	@Override
	public long getRegMinAttemptTimeout() {
		return _regMinAttemptTimeout;
	}

	private void setRegMinAttemptTimeout(long regMinAttemptTimeout) {
		this._regMinAttemptTimeout = regMinAttemptTimeout;
	}

	@Override
	public long getRegMaxAttemptTimeout() {
		return _regMaxAttemptTimeout;
	}

	private void setRegMaxAttemptTimeout(long regMaxAttemptTimeout) {
		this._regMaxAttemptTimeout = regMaxAttemptTimeout;
	}

	@Override
	public int getRegAuthAttempts() {
		return _regAuthAttempts;
	}

	private void setRegAuthAttempts(int regAuthAttempts) {
		this._regAuthAttempts = regAuthAttempts;
	}

	@Override
	public boolean isOnDialogRoute() {
		return _onDialogRoute;
	}

	private void setOnDialogRoute(boolean onDialogRoute) {
		this._onDialogRoute = onDialogRoute;
	}

	@Override
	public String getViaAddr() {
		return _viaAddr;
	}

	private void setViaAddr(String via_addr) {
		this._viaAddr = via_addr;
	}

	@Override
	public int getHostPort() {
		return _hostPort;
	}

	private void setHostPort(int host_port) {
		this._hostPort = host_port;
	}

	@Override
	public IpAddress getBindingIpAddr() {
		return _bindingIpAddr;
	}

	private void setBindingIpAddr(IpAddress bindingIpAddr) {
		this._bindingIpAddr = bindingIpAddr;
	}

	@Override
	public String[] getTransportProtocols() {
		return _transportProtocols;
	}

	/**
	 * @see #getTransportProtocols()
	 */
	public void setTransportProtocols(String[] transportProtocols) {
		this._transportProtocols = transportProtocols;
	}

	@Override
	public int[] getTransportPorts() {
		return _transportPorts;
	}

	private void setTransportPorts(int[] transportPorts) {
		this._transportPorts = transportPorts;
	}

	@Override
	public int getMaxConnections() {
		return _maxConnections;
	}

	private void setMaxConnections(int maxConnections) {
		this._maxConnections = maxConnections;
	}

	@Override
	public SipURI getOutboundProxy() {
		return _outboundProxy;
	}

	/** @see #getOutboundProxy() */
	public void setOutboundProxy(SipURI outboundProxy) {
		this._outboundProxy = outboundProxy;
	}

	@Override
	public SipURI getTelGateway() {
		return _telGateway;
	}

	private void setTelGateway(SipURI telGateway) {
		this._telGateway = telGateway;
	}

	@Override
	public boolean isLogAllPackets() {
		return _logAllPackets;
	}

	private void setLogAllPackets(boolean logAllPackets) {
		this._logAllPackets = logAllPackets;
	}

	@Override
	public boolean isTrustAll() {
		return _trustAll;
	}

	private void setTrustAll(boolean trustAll) {
		this._trustAll = trustAll;
	}

	@Override
	public String[] getTrustedCerts() {
		return _trustedCerts;
	}

	private void setTrustedCerts(String[] trustedCerts) {
		this._trustedCerts = trustedCerts;
	}

	@Override
	public String getTrustFolder() {
		return _trustFolder;
	}

	private void setTrustFolder(String trustFolder) {
		this._trustFolder = trustFolder;
	}

	@Override
	public String getCertFile() {
		return _certFile;
	}

	private void setCertFile(String certFile) {
		this._certFile = certFile;
	}

	@Override
	public String getKeyFile() {
		return _keyFile;
	}

	private void setKeyFile(String keyFile) {
		this._keyFile = keyFile;
	}

	/** Outbound proxy addr (for backward compatibility). */
	private String getOutboundAddr() {
		return _outboundAddr;
	}

	private void setOutboundAddr(String outboundAddr) {
		this._outboundAddr = outboundAddr;
	}

	/** Outbound proxy port (for backward compatibility). */
	private int getOutboundPort() {
		return _outboundPort;
	}

	private void setOutboundPort(int outboundPort) {
		this._outboundPort = outboundPort;
	}
}
