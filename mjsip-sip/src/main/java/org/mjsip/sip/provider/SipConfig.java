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
import org.zoolu.net.AddressType;
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
	public static final String AUTO_CONFIGURATION = "auto-configuration";

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

	@Option(name = "--min-session-interval", usage = "Minimum session interval (Min-SE header field) for supporting \"Session Timers\" (RFC 4028).")
	private int _minSessionInterval=90;

	@Option(name = "--default-session-interval", usage = "Default session interval (Session-Expires header field) for supporting \"Session Timers\" (RFC  4028).")
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

	@Option(name = "--via-addr", usage = "Host IPv4 address added to the via header.")
	private String _viaAddr4 = null;

	@Option(name = "--via-addr-v6", usage = "Host IPv6 address used in communication with an IPv6 counterpart in the via header.")
	private String _viaAddr6 = null;

	@Option(name = "--prefer-ipv4", handler = YesNoHandler.class, usage = "Whether to use IPv4 addresses by default.")
	private boolean _preferIPv4 = false;

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

	@Option(name = "--log-all-packets", handler = YesNoHandler.class, usage = "Whether to log all SIP messages.")
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
		
		String via4config = getViaAddrIPv4();
		String via6config = getViaAddrIPv6();
		boolean hasVia4 = via4config != null && !via4config.equalsIgnoreCase(AUTO_CONFIGURATION);
		boolean hasVia6 = via6config != null && !via6config.equalsIgnoreCase(AUTO_CONFIGURATION);

		if (!hasVia4) {
			setViaAddrIPv4(hasVia6 ? via6config : IpAddress.getLocalHostAddress(AddressType.IP4).getHostAddress());
		}
		if (!hasVia6) {
			setViaAddrIPv6(hasVia4 ? via4config : IpAddress.getLocalHostAddress(AddressType.IP6).getHostAddress());
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

	public void setDefaultPort(int defaultPort) {
		this._defaultPort = defaultPort;
	}

	@Override
	public int getDefaultTlsPort() {
		return _defaultTlsPort;
	}

	public void setDefaultTlsPort(int defaultTlsPort) {
		this._defaultTlsPort = defaultTlsPort;
	}

	/** Default max number of contemporary open transport connections. */
	public int getDefaultMaxConnections() {
		return _defaultMaxConnections;
	}

	public void setDefaultMaxConnections(int defaultMaxConnections) {
		this._defaultMaxConnections = defaultMaxConnections;
	}

	@Override
	public boolean useRport() {
		return _useRport;
	}

	public void setUseRport(boolean useRport) {
		this._useRport = useRport;
	}

	@Override
	public boolean forceRport() {
		return _forceRport;
	}

	public void setForceRport(boolean forceRport) {
		this._forceRport = forceRport;
	}

	@Override
	public long getRetransmissionTimeout() {
		return _retransmissionTimeout;
	}

	public void setRetransmissionTimeout(long retransmissionTimeout) {
		this._retransmissionTimeout = retransmissionTimeout;
	}

	@Override
	public long getMaxRetransmissionTimeout() {
		return _maxRetransmissionTimeout;
	}

	public void setMaxRetransmissionTimeout(long maxRetransmissionTimeout) {
		this._maxRetransmissionTimeout = maxRetransmissionTimeout;
	}

	@Override
	public long getTransactionTimeout() {
		return _transactionTimeout;
	}

	public void setTransactionTimeout(long transaction_timeout) {
		this._transactionTimeout = transaction_timeout;
	}

	@Override
	public long getClearingTimeout() {
		return _clearingTimeout;
	}

	public void setClearingTimeout(long clearingTimeout) {
		this._clearingTimeout = clearingTimeout;
	}

	@Override
	public int getMaxForwards() {
		return _maxForwards;
	}

	public void setMaxForwards(int maxForwards) {
		this._maxForwards = maxForwards;
	}

	@Override
	public boolean isAutoTrying() {
		return _autoTrying;
	}

	public void setAutoTrying(boolean autoTrying) {
		_autoTrying = autoTrying;
	}

	@Override
	public boolean isEarlyDialog() {
		return _earlyDialog;
	}

	public void setEarlyDialog(boolean earlyDialog) {
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

	public void setDefaultExpires(int defaultExpires) {
		this._defaultExpires = defaultExpires;
	}

	@Override
	public String getUaInfo() {
		return _uaInfo;
	}

	public void setUaInfo(String uaInfo) {
		this._uaInfo = uaInfo;
	}

	@Override
	public String getServerInfo() {
		return _serverInfo;
	}

	public void setServerInfo(String serverInfo) {
		this._serverInfo = serverInfo;
	}

	@Override
	public String[] getSupportedOptionTags() {
		return _supportedOptionTags;
	}

	public void setSupportedOptionTags(String[] supportedOptionTags) {
		this._supportedOptionTags = supportedOptionTags;
	}

	@Override
	public String[] getRequiredOptionTags() {
		return _requiredOptionTags;
	}

	public void setRequiredOptionTags(String[] requiredOptionTags) {
		_requiredOptionTags = requiredOptionTags;
	}

	@Override
	public String[] getAllowedMethods() {
		return _allowedMethods;
	}

	public void setAllowedMethods(String[] allowedMethods) {
		this._allowedMethods = allowedMethods;
	}

	@Override
	public int getMinSessionInterval() {
		return _minSessionInterval;
	}

	public void setMinSessionInterval(int minSessionInterval) {
		this._minSessionInterval = minSessionInterval;
	}

	@Override
	public int getDefaultSessionInterval() {
		return _defaultSessionInterval;
	}

	public void setDefaultSessionInterval(int defaultSessionInterval) {
		this._defaultSessionInterval = defaultSessionInterval;
	}

	@Override
	public long getRegMinAttemptTimeout() {
		return _regMinAttemptTimeout;
	}

	public void setRegMinAttemptTimeout(long regMinAttemptTimeout) {
		this._regMinAttemptTimeout = regMinAttemptTimeout;
	}

	@Override
	public long getRegMaxAttemptTimeout() {
		return _regMaxAttemptTimeout;
	}

	public void setRegMaxAttemptTimeout(long regMaxAttemptTimeout) {
		this._regMaxAttemptTimeout = regMaxAttemptTimeout;
	}

	@Override
	public int getRegAuthAttempts() {
		return _regAuthAttempts;
	}

	public void setRegAuthAttempts(int regAuthAttempts) {
		this._regAuthAttempts = regAuthAttempts;
	}

	@Override
	public boolean isOnDialogRoute() {
		return _onDialogRoute;
	}

	public void setOnDialogRoute(boolean onDialogRoute) {
		this._onDialogRoute = onDialogRoute;
	}

	@Override
	public boolean getPreferIPv4() {
		return _preferIPv4;
	}

	@Override
	public String getViaAddr(AddressType type) {
		switch (type) {
		case IP4:
			return getViaAddrIPv4();
		case IP6:
			return getViaAddrIPv6();
		case DEFAULT:
			if (_viaAddr4 != null && (_preferIPv4 || _viaAddr6 == null)) {
				return _viaAddr4;
			} else if (_viaAddr6 != null) {
				return _viaAddr6;
			} else {
				// Consistent with the other via address getters.
				return null;
			}
		}
		throw new IllegalStateException("No such address type: " + type);
	}

	@Override
	public String getViaAddrIPv4() {
		return _viaAddr4;
	}

	public void setViaAddrIPv4(String via_addr) {
		this._viaAddr4 = via_addr;
	}

	@Override
	public String getViaAddrIPv6() {
		return _viaAddr6;
	}

	public void setViaAddrIPv6(String via_addr) {
		this._viaAddr6 = via_addr;
	}

	@Override
	public int getHostPort() {
		return _hostPort;
	}

	public void setHostPort(int host_port) {
		this._hostPort = host_port;
	}

	@Override
	public IpAddress getBindingIpAddr() {
		return _bindingIpAddr;
	}

	public void setBindingIpAddr(IpAddress bindingIpAddr) {
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

	public void setTransportPorts(int[] transportPorts) {
		this._transportPorts = transportPorts;
	}

	@Override
	public int getMaxConnections() {
		return _maxConnections;
	}

	public void setMaxConnections(int maxConnections) {
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

	public void setTelGateway(SipURI telGateway) {
		this._telGateway = telGateway;
	}

	@Override
	public boolean isLogAllPackets() {
		return _logAllPackets;
	}

	public void setLogAllPackets(boolean logAllPackets) {
		this._logAllPackets = logAllPackets;
	}

	@Override
	public boolean isTrustAll() {
		return _trustAll;
	}

	public void setTrustAll(boolean trustAll) {
		this._trustAll = trustAll;
	}

	@Override
	public String[] getTrustedCerts() {
		return _trustedCerts;
	}

	public void setTrustedCerts(String[] trustedCerts) {
		this._trustedCerts = trustedCerts;
	}

	@Override
	public String getTrustFolder() {
		return _trustFolder;
	}

	public void setTrustFolder(String trustFolder) {
		this._trustFolder = trustFolder;
	}

	@Override
	public String getCertFile() {
		return _certFile;
	}

	public void setCertFile(String certFile) {
		this._certFile = certFile;
	}

	@Override
	public String getKeyFile() {
		return _keyFile;
	}

	public void setKeyFile(String keyFile) {
		this._keyFile = keyFile;
	}

	/** Outbound proxy addr (for backward compatibility). */
	private String getOutboundAddr() {
		return _outboundAddr;
	}

	public void setOutboundAddr(String outboundAddr) {
		this._outboundAddr = outboundAddr;
	}

	/** Outbound proxy port (for backward compatibility). */
	private int getOutboundPort() {
		return _outboundPort;
	}

	public void setOutboundPort(int outboundPort) {
		this._outboundPort = outboundPort;
	}
}
