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



import org.kohsuke.args4j.Option;
import org.mjsip.config.YesNoHandler;
import org.mjsip.sip.address.SipURI;
import org.mjsip.sip.config.IpAddressHandler;
import org.mjsip.sip.config.SipURIHandler;
import org.mjsip.sip.message.SipMethods;
import org.slf4j.LoggerFactory;
import org.zoolu.net.IpAddress;
import org.zoolu.util.Configure;



/**
 * SIP configuration options.
 * <p>
 * Options are: the default SIP port, deafult supported transport protocols, timeouts, log
 * configuration, etc.
 * </p>
 */
public class SipConfig implements SipOptions {
	
	private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(SipConfig.class);

	/** String value "auto-configuration" used for auto configuration of the host address */
	public static final String ALL_INTERFACES="ALL-INTERFACES";

	/** String value "auto-configuration" used for auto configuration of the host address */
	public static final String AUTO_CONFIGURATION="AUTO-CONFIGURATION";

	/** @see #getDefaultPort() */
	@Option(name = "--default-port")
	private int _defaultPort=5060; 

	/** @see #getDefaultTlsPort() */
	@Option(name = "--default-tls-port")
	private int _defaultTlsPort = 5061;

	/** @see #getDefaultMaxConnections() */
	@Option(name = "--default-max-connections")
	private int _defaultMaxConnections=32;

	/** @see #useRport() */
	@Option(name = "--use-rport", handler = YesNoHandler.class)
	private boolean _useRport = true;

	/** @see #forceRport() */
	@Option(name = "--force-rport", handler = YesNoHandler.class)
	private boolean _forceRport = false;

	// ********************* transaction timeouts *********************

	/** @see #getRetransmissionTimeout() */
	@Option(name = "--retransmission-timeout")
	private long _retransmissionTimeout = 500;

	/** @see #getMaxRetransmissionTimeout() */
	@Option(name = "--max-retransmission-timeout")
	private long _maxRetransmissionTimeout = 4000;

	/** @see #getTransactionTimeout() */
	@Option(name = "--transaction-timeout")
	private long _transactionTimeout = 32000;

	/** @see #getClearingTimeout() */
	@Option(name = "--clearing-timeout")
	private long _clearingTimeout = 5000;

	// ******************** general configurations ********************

	@Option(name = "--max-forwards")
	private int _maxForwards = 70;

	@Option(name = "--auto-trying", handler = YesNoHandler.class)
	private boolean _autoTrying=true;

	@Option(name = "--early-dialog", handler = YesNoHandler.class)
	private boolean _earlyDialog=true;

	@Option(name = "--auto-prack", handler = YesNoHandler.class)
	private boolean _autoPrack=false;

	@Option(name = "--default-expires")
	private int _defaultExpires=3600;

	private String _uaInfo=SipStack.release;

	private String _serverInfo = SipStack.release;

	private String[] _supportedOptionTags={ SipStack.OTAG_100rel,SipStack.OTAG_timer }; //{ OTAG_100rel,OTAG_timer,OTAG_precondition };
	private String[] _requiredOptionTags=null; //{ OTAG_100rel,OTAG_timer };
	private String[] _allowedMethods={ SipMethods.INVITE,SipMethods.ACK,SipMethods.OPTIONS,SipMethods.BYE,SipMethods.CANCEL,SipMethods.INFO,SipMethods.PRACK,SipMethods.NOTIFY,SipMethods.MESSAGE,SipMethods.UPDATE };

	@Option(name = "--min-session-interval")
	private int _minSessionInterval=90;

	@Option(name = "--default-session-interval")
	private int _defaultSessionInterval=0;


	// ************** registration client configurations **************

	private long _regMinAttemptTimeout=60*1000; // 1min
	private long _regMaxAttemptTimeout=900*1000; // 15min  
	private int _regAuthAttempts=3;


	// ************************** extensions **************************

	@Option(name = "--on-dialog-route", handler = YesNoHandler.class)
	private boolean _onDialogRoute=false;

	/** Whether using an alternative transaction id that does not include the 'sent-by' value. */
	//public boolean alternative_transaction_id=false;

	@Option(name = "--via-addr", usage = "Host via address.")
	private String _viaAddr = null;

	@Option(name = "--host-port", usage = "Local SIP port.")
	private int _hostPort=0;

	@Option(name = "--binding-addr", handler = IpAddressHandler.class)
	private IpAddress _bindingIpAddr = null;

	@Option(name = "--transport-protocols", usage = "Use the given transport protocol for SIP.")
	private String[] _transportProtocols = { SipProvider.PROTO_UDP, SipProvider.PROTO_TCP };
	
	private int[] _transportPorts = {};

	@Option(name = "--max-connections")
	private int _maxConnections = 0;

	@Option(name = "--outbound-proxy", handler = SipURIHandler.class, usage = "Use the given outbound proxy.")
	private SipURI _outboundProxy = null;

	private SipURI _telGateway = null;

	private boolean _logAllPackets = false;

	@Option(name = "--trust-all", handler = YesNoHandler.class)
	private boolean _trustAll;

	private String[] _trustedCerts;

	@Option(name = "--trust-folder")
	private String _trustFolder;

	@Option(name = "--cert-file")
	private String _certFile;

	@Option(name = "--key-file")
	private String _keyFile;

	// for backward compatibility:

	private String _outboundAddr = null;

	private int _outboundPort = -1;

	public SipConfig() {
		super();
	}

	public void normalize() {
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
