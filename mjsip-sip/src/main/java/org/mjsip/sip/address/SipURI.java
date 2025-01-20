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
package org.mjsip.sip.address;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Vector;

import org.mjsip.sip.provider.SipParser;
import org.zoolu.net.AddressType;

/**
 * SIP and SIPS URIs.
 * 
 * <p>
 * A SIP URI is a string of the form of:
 * </p>
 * 
 * <pre>
 * sip:[user@]hostname[:port][;parameters]
 * </pre>
 * 
 * <p>
 * If <i>port</i> number is ommitted, -1 is returned
 * </p>
 */
public class SipURI extends GenericURI {

	/** Transport param name */
	public static final String PARAM_TRANSPORT="transport"; 
	
	/** Maddr param name */
	public static final String PARAM_MADDR="maddr"; 
	
	/** TTL param name */
	public static final String PARAM_TTL="ttl"; 

	/** Scheme of SIP URI */
	protected static final String SIP_COLON="sip:";

	/** Scheme of SIPS URI */
	protected static final String SIPS_COLON="sips:";

	/** Whether has SIPS scheme */
	private boolean _secure = false;

	private String _user;

	private String _host;

	private int _port;

	private Map<String, String> _params;

	private String _password;

	private Map<String, String> _headers;

	private boolean _ipv6;

	private void checkScheme() {
		if (!getScheme().equals(SCHEME_SIP) && !getScheme().equals(SCHEME_SIPS)) {
			throw new UnexpectedUriSchemeException(getScheme());
		}
	}

	/**
	 * Creates a SipURI.
	 *
	 * @param host
	 *        The host address (IP address or fully qualified domain name) of the SIP device.
	 */
	public SipURI(String host) {
		this(null, host);
	}

	/**
	 * Creates a SipURI.
	 *
	 * @param host
	 *        The host address (IP address or fully qualified domain name) of the SIP device.
	 */
	public SipURI(String user, String host) {
		this(user, host, -1);
	}

	/**
	 * Creates a SipURI.
	 *
	 * @param host
	 *        The host address (IP address or fully qualified domain name) of the SIP device.
	 */
	public SipURI(String host, int portnumber) {
		this(null, host, portnumber);
	}

	/**
	 * Creates a SipURI.
	 *
	 * @param host
	 *        The host address (IP address or fully qualified domain name) of the SIP device.
	 * @param ipv6
	 *        Whether the given host address is an IPv6 address.
	 */
	public SipURI(String host, boolean ipv6, int portnumber) {
		this(null, host, ipv6, portnumber);
	}

	/**
	 * Creates a SipURI.
	 *
	 * @param host
	 *        The host address (IP address or fully qualified domain name) of the SIP device.
	 */
	public SipURI(String user, String host, int portnumber) {
		this(user, null, host, isIPv6(host), portnumber, false, new HashMap<>(), Collections.emptyMap());
	}

	/**
	 * Creates a SipURI.
	 *
	 * @param host
	 *        The host address (IP address or fully qualified domain name) of the SIP device.
	 * @param ipv6
	 *        Whether the given host address is an IPv6 address.
	 */
	public SipURI(String user, String host, boolean ipv6, int portnumber) {
		this(user, null, host, portnumber, false, new HashMap<>(), Collections.emptyMap());
	}

	/**
	 * Creates a SipURI.
	 *
	 * @param host
	 *        The host address (IP address or fully qualified domain name) of the SIP device.
	 */
	public SipURI(String user, String password, String host, int port, boolean secure, Map<String, String> params,
			Map<String, String> headers) {
		this(user, password, host, isIPv6(host), port, secure, params, headers);
	}

	/**
	 * Creates a SipURI.
	 *
	 * @param host
	 *        The host address (IP address or fully qualified domain name) of the SIP device.
	 * @param ipv6
	 *        Whether the given host address is an IPv6 address.
	 */
	public SipURI(String user, String password, String host, boolean ipv6, int port, boolean secure,
			Map<String, String> params, Map<String, String> headers) {
		_user = user;
		_password = password;
		_secure = secure;
		_host = host;
		_ipv6 = ipv6;
		_port = port;
		_params = params;
		_headers = headers;
	}

	/**
	 * Whether the given host name or address is an IPv6 address.
	 */
	public static boolean isIPv6(String host) {
		return host.indexOf(':') >= 0;
	}

	/**
	 * Parses a {@link SipURI} in the format
	 * <code>sip[s]:user:password@host:port;uri-parameters?headers</code>.
	 */
	public static SipURI parseSipURI(String uri) {
		if (uri == null || uri.isBlank()) {
			return null;
		}
		return new SipURIParser(uri).parse();
	}

	/**
	 * Parses an address header value such as
	 * <code><sip:ab-87042252@[2a01:0:0:0:0:0:0:1]:50060</code>
	 */
	public static SipURI parseAddress(String sipAddress) {
		if (sipAddress == null || sipAddress.isBlank()) {
			return null;
		}
		return parseSipURI(new SipParser(sipAddress).getURISource());
	}

	@Override
	public String getScheme() {
		return _secure ? SCHEME_SIPS : SCHEME_SIP;
	}

	/** Gets user name of SipURI (Returns null if user name does not exist). */
	public String getUserName() {
		return _user;
	}

	/**
	 * The optional {@link #getUserName() user's} password.
	 */
	public String getPassword() {
		return _password;
	}

	/** Gets host of SipURI. */
	public String getHost() {
		return _host;
	}

	/**
	 * Whether the {@link #getHost()} is an IPv6 address.
	 */
	public boolean isIpv6() {
		return _ipv6;
	}

	/**
	 * The {@link AddressType} of this address.
	 */
	public AddressType getAddressType() {
		return isIpv6() ? AddressType.IP6 : AddressType.IP4;
	}

	/** Gets port of SipURI; returns -1 if port is not specidfied. */
	public int getPort() {
		return _port;
	}

	/** Gets boolean value to indicate if SipURI has user name. */
	public boolean hasUserName() {
		return getUserName()!=null;
	}

	/** Gets boolean value to indicate if SipURI has port &gt;= 0. */
	public boolean hasPort() {
		return getPort()>=0;
	}

	/** Whether is SIPS. */
	public boolean isSecure() {
		return _secure;
	}

	/** Sets scheme to SIPS. */
	public void setSecure(boolean secure)  {
		this._secure = secure;
	}

	/** Gets the value of transport parameter.
	  * @return null if no transport parameter is present. */
	public String getTransport()  {
		return getParameter(PARAM_TRANSPORT);
	}  

	/** Whether transport parameter is present. */
	public boolean hasTransport() {
		return hasParameter(PARAM_TRANSPORT);
	}

	/**
	 * Adds transport parameter.
	 */
	public SipURI addTransport(String proto) {
		addParameter(PARAM_TRANSPORT,proto.toLowerCase());
		return this;
	}

	/** Gets the value of maddr parameter.
	  * @return null if no maddr parameter is present. */
	public String getMaddr()  {
		return getParameter(PARAM_MADDR);
	}  

	/** Whether maddr parameter is present. */
	public boolean hasMaddr() {
		return hasParameter(PARAM_MADDR);
	}

	/**
	 * Adds maddr parameter.
	 */
	public SipURI addMaddr(String maddr) {
		addParameter(PARAM_MADDR,maddr);
		return this;
	}

	@Override
	public boolean hasParameters() {
		return !_params.isEmpty();
	}

	@Override
	public Vector<String> getParameterNames() {
		return new Vector<>(_params.keySet());
	}

	@Override
	public String getParameter(String name) {
		return _params.get(name);
	}

	@Override
	public boolean hasParameter(String name) {
		return _params.keySet().contains(name);
	}

	@Override
	public void addParameter(String name) {
		_params.put(name, null);
	}

	@Override
	public void addParameter(String name, String value) {
		_params.put(name, value);
	}

	@Override
	public void removeParameter(String name) {
		_params.remove(name);
	}

	@Override
	public void removeParameters() {
		_params.clear();
	}

	public String getHeader(String name) {
		return _headers.get(name);
	}

	public boolean hasHeader(String name) {
		return _headers.keySet().contains(name);
	}

	/** Gets the value of ttl parameter.
	  * @return 1 if no ttl parameter is present. */
	public int getTtl()  {
		try {  return Integer.parseInt(getParameter(PARAM_TTL));  } catch (Exception e) {  return 1;  }
	}  

	/** Whether ttl parameter is present. */
	public boolean hasTtl() {
		return hasParameter(PARAM_TTL);
	}

	/** Adds ttl parameter. */
	public void addTtl(int ttl)  {
		addParameter(PARAM_TTL,Integer.toString(ttl));
	}

	@Override
	public SipURI addLr() {
		super.addLr();
		return this;
	}

	@Override
	public Object clone() {
		return copy();
	}

	/**
	 * Creates a copy of this URI.
	 */
	public SipURI copy() {
		return new SipURI(_user, _password, _host, _ipv6, _port, _secure, new LinkedHashMap<>(_params),
				new LinkedHashMap<>(_headers));
	}

	@Override
	public boolean isSipURI() {
		return true;
	}

	@Override
	public SipURI toSipURI() {
		return this;
	}

	@Override
	public boolean isTelURI() {
		return false;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		sb.append((_secure) ? SIPS_COLON : SIP_COLON);

		appendSpecific(sb);

		return sb.toString();
	}

	@Override
	public String getSpecificPart() {
		StringBuilder sb = new StringBuilder();

		appendSpecific(sb);

		return sb.toString();
	}

	private void appendSpecific(StringBuilder sb) {
		if (_user != null) {
			sb.append(_user);

			if (_password != null) {
				sb.append(':');
				sb.append(_password);
			}
			sb.append('@');
		}

		if (_ipv6) {
			sb.append('[');
			sb.append(_host);
			sb.append(']');
		} else {
			sb.append(_host);
		}

		if (_port > 0) {
			sb.append(":");
			sb.append(_port);
		}

		for (Entry<String, String> param : _params.entrySet()) {
			sb.append(';');
			sb.append(param.getKey());
			String value = param.getValue();
			if (value != null) {
				sb.append('=');
				sb.append(value);
			}
		}
	}

}


