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
package org.mjsip.sip.header;

import org.mjsip.sip.address.SipURI;

/** SIP Header Via.
  * The Via header field indicates the transport used for the transaction
  * and identifies the location where the response is to be sent. 
  * <BR> When the UAC creates a request, it MUST insert a Via into that
  * request.  The protocol name and protocol version in the header field
  * is SIP and 2.0, respectively.
  * <BR> The Via header field value MUST
  * contain a branch parameter.  This parameter is used to identify the
  * transaction created by that request.  This parameter is used by both
  * the client and the server.
  * <BR> The branch parameter value MUST be unique across space and time for
  * all requests sent by the UA.  The exceptions to this rule are CANCEL
  * and ACK for non-2xx responses.  A CANCEL request
  * will have the same value of the branch parameter as the request it
  * cancels.  An ACK for a non-2xx
  * response will also have the same branch ID as the INVITE whose
  * response it acknowledges.
  */
public class ViaHeader extends HeaderWithParams {
	
	/** "z9hG4bK" magic cookie */
	public static final String MAGIC_COOKIE="z9hG4bK";


	/** "received" parameter */
	protected static final String received_param="received";

	/** "rport" parameter */
	protected static final String rport_param="rport";

	/** "branch" parameter */
	protected static final String branch_param="branch";

	/** "maddr" parameter */
	protected static final String maddr_param="maddr";

	/** "maddr" parameter */
	protected static final String ttl_param="ttl";

	private String _protocol;

	private String _version;

	private String _transport;

	private String _host;

	private boolean _ipv6;

	private int _port;

	/** Creates a new ViaHeader. */
	public ViaHeader(String proto, String host, int port) {
		this(proto.toUpperCase(), host, SipURI.isIPv6(host), port);
	}

	/** Creates a new ViaHeader. */
	public ViaHeader(String proto, String host, boolean ipv6, int port) {
		this("SIP", "2.0", proto, host, ipv6, port);
	}

	/**
	 * Creates a {@link ViaHeader}.
	 */
	public ViaHeader(String protocol, String version, String transport, String host, boolean ipv6, int port) {
		super(SipHeaders.Via);

		_protocol = protocol;
		_version = version;
		_transport = transport;
		_host = host;
		_ipv6 = ipv6;
		_port = port;
	}

	/** Gets the transport protocol. */
	public String getProtocol() {
		return _protocol;
	}

	/** Sets the transport protocol. */
	public void setProtocol(String proto) {
		_protocol = proto;
	}

	/**
	 * The SIP version.
	 */
	public String getVersion() {
		return _version;
	}

	/**
	 * The transport protocol.
	 */
	public String getTransport() {
		return _transport;
	}

	/**
	 * @see #getTransport()
	 */
	public void setTransport(String transport) {
		_transport = transport;
	}

	/** Gets "sent-by" parameter. */
	public String getSentBy() {
		return hasPort() ? getHostName() + ":" + getPort() : getHostName();
	}

	/** Gets host of ViaHeader. */
	public String getHost() {
		return _host;
	}

	/**
	 * Whether {@link #getHost()} is an IPv6 address.
	 */
	public boolean isIpv6() {
		return _ipv6;
	}

	/**
	 * The host name to to use as part of a SIP URI.
	 */
	public String getHostName() {
		if (_ipv6) {
			return '[' + _host + ']';
		} else {
			return _host;
		}
	}

	/** Returns boolean value indicating if ViaHeader has port. */
	public boolean hasPort() {
		return getPort() > 0;
	}
	
	/** Gets port of ViaHeader. */
	public int getPort() {
		return _port;
	}
	
	/** Makes a SipURI from ViaHeader. */
	public SipURI getSipURI() {
		return new SipURI(getHost(), isIpv6(), getPort());
	}   
	
	/** Checks if "branch" parameter is present. */
	public boolean hasBranch() {
		return hasParameter(branch_param);
	}
	/** Gets "branch" parameter. */
	public String getBranch() {
		return getParameter(branch_param);
	}   
	/** Sets "branch" parameter. */
	public void setBranch(String value) {
		setParameter(branch_param,value);
	}   
			 
	/** Checks if "received" parameter is present. */
	public boolean hasReceived() {
		return hasParameter(received_param);
	}
	/** Gets "received" parameter. */
	public String getReceived() {
		return getParameter(received_param);
	}     
	/** Sets "received" parameter. */
	public void setReceived(String value) {
		setParameter(received_param,value);
	}   

	/** Checks if "rport" parameter is present. */
	public boolean hasRport() {
		return hasParameter(rport_param);
	}

	/** Gets "rport" parameter. */
	public int getRport() {
		String value=getParameter(rport_param);
		if (value!=null) return Integer.parseInt(value);
		else return -1;
	}     

	/** Sets "rport" parameter. */
	public void setRport() {
		setParameter(rport_param,null);
	}   

	/** Sets "rport" parameter. */
	public void setRport(int port) {
		if (port<0) setParameter(rport_param,null);
		else setParameter(rport_param,Integer.toString(port));
	}

	/** Checks if "maddr" parameter is present. */
	public boolean hasMaddr() {
		return hasParameter(maddr_param);
	}

	/** Gets "maddr" parameter. */
	public String getMaddr() {
		return getParameter(maddr_param);
	}     

	/** Sets "maddr" parameter */
	public void setMaddr(String value) {
		setParameter(maddr_param,value);
	}   

	/** Checks if "ttl" parameter is present */
	public boolean hasTtl() {
		return hasParameter(ttl_param);
	}

	/** Gets "ttl" parameter. */
	public int getTtl() {
		String value=getParameter(ttl_param);
		if (value!=null) return Integer.parseInt(value);
		else return -1;
	}     

	/** Sets "ttl" parameter. */
	public void setTtl(int ttl) {
		setParameter(ttl_param,Integer.toString(ttl));
	}
	
	// Via               =  ( "Via" / "v" ) HCOLON via-parm *(COMMA via-parm)
	// via-parm          =  sent-protocol LWS sent-by *( SEMI via-params )   
	// via-params        =  via-ttl / via-maddr / via-received / via-branch / via-extension                                  
	// via-ttl           =  "ttl" EQUAL ttl                                  
	// via-maddr         =  "maddr" EQUAL host                               
	// via-received      =  "received" EQUAL (IPv4address / IPv6address)     
	// via-branch        =  "branch" EQUAL token                             
	// via-extension     =  generic-param       

	// generic-param     =  token [ EQUAL gen-value ]
	// gen-value         =  token / host / quoted-string
    // token             =  1*(alphanum / "-" / "." / "!" / "%" / "*" / "_" / "+" / "`" / "'" / "~" )
    // quoted-string     =  SWS DQUOTE *(qdtext / quoted-pair ) DQUOTE
    // qdtext            =  LWS / %x21 / %x23-5B / %x5D-7E / UTF8-NONASCII
	// quoted-pair       =  "\" (%x00-09 / %x0B-0C / %x0E-7F)

	// sent-protocol     =  protocol-name SLASH protocol-version SLASH transport                                  
	// protocol-name     =  "SIP" / token                                    
	// protocol-version  =  token                                            
	// transport         =  "UDP" / "TCP" / "TLS" / "SCTP" / other-transport

	// sent-by           =  host [ COLON port ]
	// hostport          =  host [ ":" port ]
	// host              =  hostname / IPv4address / IPv6reference
	// hostname          =  *( domainlabel "." ) toplabel [ "." ]
	// domainlabel       =  alphanum / alphanum *( alphanum / "-" ) alphanum
	// toplabel          =  ALPHA / ALPHA *( alphanum / "-" ) alphanum
	// IPv4address       =  1*3DIGIT "." 1*3DIGIT "." 1*3DIGIT "." 1*3DIGIT
	// IPv6reference     =  "[" IPv6address "]"
	// IPv6address       =  hexpart [ ":" IPv4address ]
	// hexpart           =  hexseq / hexseq "::" [ hexseq ] / "::" [ hexseq ]
	// hexseq            =  hex4 *( ":" hex4)
	// hex4              =  1*4HEXDIG
	// port              =  1*DIGIT
	// ttl               =  1*3DIGIT ; 0 to 255
    // HCOLON            =  *( SP / HTAB ) ":" SWS
    // COLON             =  SWS ":" SWS ; colon
    // SWS               =  [LWS] ; sep whitespace
    // LWS               =  [*WSP CRLF] 1*WSP ; linear whitespace
    // LWS               =  [CRLF] 1*( SP | HT )
    // CRLF              =  CR LF
    // CR                =  <US-ASCII CR, carriage return (13)>
    // LF                =  <US-ASCII LF, linefeed (10)>
    // SP                =  <US-ASCII SP, space (32)>
    // HT                =  <US-ASCII HT, horizontal-tab (9)>    		
	public static ViaHeader parse(String src) {
		Analyzer parser = new Analyzer(src);
		
		// sent-protocol
		do {
			// via-parm
			parser.skipWSPCRLF();
			String protocolName = parser.findChar('/').stringBefore();
			String protocolVersion = parser.skip().findChar('/').stringBefore();
			String transport = parser.skip().findWS().stringBefore();
			parser.skipWSPCRLF();
			
			// sent-by
			String host;
			boolean ipv6;
			int port = -1;
			switch (parser.currentChar()) {
			case '[': {
				// IPv6
				host = parser.skip().findChar(']').stringBefore();
				ipv6 = true;
				parser.skip();
				break;
			}
			default: {
				host = parser.findChars(" \t\r\n:;,").stringBefore();
				ipv6 = false;
			}
			}
			parser.skipWSPCRLF();

			if (parser.currentChar() == ':') {
				parser.skip();
				parser.skipWSPCRLF();
				port = parser.getInt();
				parser.skipWSPCRLF();
			}

			ViaHeader header = new ViaHeader(protocolName, protocolVersion, transport, host, ipv6, port);
			
			header.parseParams(parser);

			return header;
		} while (parser.getChar() == ',');
	}

	@Override
	public String getValue() {
		StringBuilder buffer = new StringBuilder();
		buffer.append(_protocol);
		buffer.append('/');
		buffer.append(_version);
		buffer.append('/');
		buffer.append(_transport);
		buffer.append(' ');
		if (_ipv6) {
			buffer.append('[');
			buffer.append(_host);
			buffer.append(']');
		} else {
			buffer.append(_host);
		}
		if (_port > 0) {
			buffer.append(':');
			buffer.append(_port);
		}
		appendParams(buffer);
		return buffer.toString();
	}

}
