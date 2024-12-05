/*
 * Copyright (c) 2023 Bernhard Haumacher et al. All Rights Reserved.
 */
package org.mjsip.sip.provider;

import org.mjsip.sip.address.SipURI;
import org.zoolu.net.AddressType;
import org.zoolu.net.IpAddress;

/**
 * Options for configuring a {@link SipProvider}.
 *
 * @author <a href="mailto:haui@haumacher.de">Bernhard Haumacher</a>
 */
public interface SipOptions {

	/**
	 * Network interface (IP address) used by SIP for selective binding. Use 'ALL-INTERFACES' or let
	 * it undefined for binding SIP to all interfaces.
	 */
	IpAddress getBindingIpAddr();

	/**
	 * Default SIP port. Note that this is not the port used by the running stack, but simply the
	 * standard default SIP port. <br>
	 * Normally it sould be set to 5060 as defined by RFC 3261. Using a different value may cause
	 * some problems when interacting with other unaware SIP UAs.
	 */
	int getDefaultPort();

	/** Default 'expires' value in seconds. RFC2361 suggests 3600s as default value. */
	int getDefaultExpires();

	/**
	 * For TLS. Absolute file name of the certificate (containing the public key) of the local node.
	 * The file name includes the full path starting from the current working folder.
	 */
	String getCertFile();

	/** Local SIP port */
	int getHostPort();

	/** Max number of (contemporary) open connections */
	int getMaxConnections();

	/**
	 * Outbound proxy URI ([sip:]host_addr[:host_port][;transport=proto]). Use 'NONE' for not using
	 * an outbound proxy (or let it undefined).
	 */
	SipURI getOutboundProxy();

	/**
	 * For TLS. Absolute file name of the private key of the local node. The file name includes the
	 * full path starting from the current working folder.
	 */
	String getKeyFile();

	/** List of supported methods. */
	String[] getAllowedMethods();

	/** Clearing timeout (milliseconds); T4 in RFC2361; they suggest T4=5sec */
	long getClearingTimeout();

	/**
	 * Default session interval (Session-Expires header field) for supporting "Session Timers" (RFC
	 * 4028).
	 */
	int getDefaultSessionInterval();

	/**
	 * Default SIP port for TLS transport (SIPS). Note that this is not the port used by the running
	 * stack, but simply the standard default SIPS port. <br>
	 * Normally it sould be set to 5061 as defined by RFC 3261. Using a different value may cause
	 * some problems when interacting with other unaware SIP UAs.
	 */
	int getDefaultTlsPort();

	/** Default max-forwards value (RFC3261 recommends value 70) */
	int getMaxForwards();

	/** Maximum retransmission timeout (milliseconds); called T2 in RFC2361; they suggest T2=4sec */
	long getMaxRetransmissionTimeout();

	/**
	 * Minimum session interval (Min-SE header field) for supporting "Session Timers" (RFC 4028).
	 */
	int getMinSessionInterval();

	/** Maximum number of consecutive registration authentication attempts before giving up */
	int getRegAuthAttempts();

	/** Maximum registration timeout (msecs) after a registration failure due to request timeout */
	long getRegMaxAttemptTimeout();

	/** Starting registration timeout (msecs) after a registration failure due to request timeout */
	long getRegMinAttemptTimeout();

	/** Required option-tags for corresponding required extensions. */
	String[] getRequiredOptionTags();

	/**
	 * Starting retransmission timeout (milliseconds); called T1 in RFC2361; they suggest T1=500ms
	 */
	long getRetransmissionTimeout();

	/**
	 * Server info included in response messages in the 'Server' header field Use "NONE" if the
	 * 'Server' header field is not added.
	 */
	String getServerInfo();

	/** Supported option-tags for corresponding supported extensions. */
	String[] getSupportedOptionTags();

	/**
	 * Tel Gatway URI ([sip:]host_addr[:host_port][;transport=proto]). URI of a default SIP
	 * proxy/gateway that is used for sending request messages with a "tel" URI as request-uri. Use
	 * 'NONE' for not using a tel gateway (or let it undefined).
	 */
	SipURI getTelGateway();

	/** Transaction timeout (milliseconds); RFC2361 suggests 64*T1=32000ms */
	long getTransactionTimeout();

	/** List of transport ports, ordered as the corresponding transport_protocols. */
	int[] getTransportPorts();

	/** List of enabled transport protocols (the first protocol is used as default). */
	String[] getTransportProtocols();

	/**
	 * For TLS. names of the files containing trusted certificates. The file names include the full
	 * path starting from the current working folder.
	 */
	String[] getTrustedCerts();

	/**
	 * For TLS. Path of the folder where trusted certificates are placed. All certificates (with
	 * file extension ".crt") found in this folder are considered trusted.
	 */
	String getTrustFolder();

	/**
	 * UA info included in request messages in the 'User-Agent' header field. Use "NONE" if the
	 * 'User-Agent' header filed must not be added.
	 */
	String getUaInfo();

	/**
	 * Whether to prefer an IPv4 address over an IPv6 address, if both are given but
	 * no special address type is requested.
	 * 
	 * @see AddressType#DEFAULT
	 */
	boolean getPreferIPv4();

	/**
	 * The default via IP address or fully-qualified domain name (FQDN).
	 * 
	 * <p>
	 * An address is selected with the following rules:
	 * </p>
	 * 
	 * <ul>
	 * <li>IP4 != null && preferIP4 => IP4</li>
	 * <li>IP4 != null && IP6 == null => IP4</li>
	 * <li>IP6 != null && !preferIP4 => IP6</li>
	 * <li>IP6 != null && IP4 == null => IP6</li>
	 * <li>else an error is thrown</li>
	 * </ul>
	 * 
	 * @return The default via address according to the rules described above
	 * 
	 * @see #getViaAddrIPv4()
	 * @see #getViaAddrIPv6()
	 * @see #getPreferIPv4()
	 */
	default String getViaAddr() {
		return getViaAddr(AddressType.DEFAULT);
	}

	/**
	 * Via IP address of the given address type.
	 */
	String getViaAddr(AddressType type);

	/**
	 * Via IPv4 address or fully-qualified domain name (FQDN).
	 * 
	 * <p>
	 * Use 'auto-configuration' for auto detection, or let it undefined.
	 * </p>
	 */
	String getViaAddrIPv4();

	/**
	 * Via IPv6 address.
	 * 
	 * <p>
	 * Use 'auto-configuration' for auto detection, or let it undefined.
	 * </p>
	 */
	String getViaAddrIPv6();

	/**
	 * Whether automatically sending PRACK messsages for incoming reliable 1xx
	 * responses in an INVITE dialog. <br>
	 * Note that if you set <i>true</i>, the PRACK messge are sent automatically
	 * without any message body. This may be in contrast with a possible
	 * offer/answer use of reliable 1xx response and PRACK.
	 */
	boolean isAutoPrack();

	/** Whether at UAS side automatically sending (by default) a 100 Trying on INVITE. */
	boolean isAutoTrying();

	/** Whether 1xx responses create an "early dialog" for methods that create dialog. */
	boolean isEarlyDialog();

	/** Whether logging all packets (including non-SIP keepalive tokens). */
	boolean isLogAllPackets();

	/**
	 * Whether forcing this node to stay within the dialog route as peer, by means of the insertion
	 * of a RecordRoute header. This is a non-standard behaviour and is normally not necessary.
	 */
	boolean isOnDialogRoute();

	/**
	 * For TLS. Whether all client and server certificates should be considered trusted.
	 */
	boolean isTrustAll();

	/** Whether adding 'rport' parameter on via header fields of outgoing requests. */
	boolean useRport();

	/** Whether adding (forcing) 'rport' parameter on via header fields of incoming requests. */
	boolean forceRport();

}
