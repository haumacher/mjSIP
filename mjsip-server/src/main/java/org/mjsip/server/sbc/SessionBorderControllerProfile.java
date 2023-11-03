package org.mjsip.server.sbc;

import org.kohsuke.args4j.Option;
import org.mjsip.config.YesNoHandler;
import org.mjsip.sip.config.SocketAddressHandler;
import org.zoolu.net.SocketAddress;

/**
 * SessionBorderControllerProfile maintains the SessionBorderController configuration.
 */
public class SessionBorderControllerProfile {
	
	@Option(name = "--relay-timeout", usage = "Maximum time that the UDP relay remains active without receiving UDP datagrams (in milliseconds).")
	public long relayTimeout=60000; // 1min

	@Option(name = "--binding_timeout", usage = "Refresh time of address-binding cache (in milliseconds).")
	public long bindingTimeout=3600000;

	@Option(name = "--handover_time", usage = "Minimum time between two changes of peer address (in milliseconds).")
	public int handoverTime=0; //5000;

	@Option(name = "--keepalive-time", usage = "Rate of keep-alive datagrams sent toward all registered UAs (in milliseconds). Set keepalive_time=0 to disable the sending of keep-alive datagrams.")
	public long keepaliveTime=0;

	// /** Whether sending keepalive datagram only to UAs that explicitely request it through 'keep-alive' parameter. */
	// public boolean keepalive_selective=false;

	@Option(name = "--keepalive-aggressive", usage = "Whether sending keep-alive datagrams to all user agents (also to non-registered ones).", handler = YesNoHandler.class)
	public boolean keepaliveAggressive=false;

	// /** Whether implementing symmetric RTP for NAT traversal. */
	// boolean symmetric_rtp=false;
	
	@Option(name = "--interpacket-time", usage = "Minimum inter-packet departure time.")
	public long interpacketTime=0; 

	@Option(name = "--do-interception", usage = "Whether to intercept media traffic.", handler = YesNoHandler.class)
	public boolean doInterception=false;

	@Option(name = "--do-activeInterception", usage = "Whether to inject new media flows.", handler = YesNoHandler.class)
	public boolean doActiveInterception=false;

	@Option(name = "--sink-addr", usage = "Sink address for media traffic interception.")
	public String sinkAddr="127.0.0.1";

	@Option(name = "--sink-port", usage = "Sink port for media traffic interception.")
	public int sinkPort=0;

	@Option(name = "--media-addr", usage = "Media address.")
	public String mediaAddr="0.0.0.0";

	@Option(name = "--backend-proxy", usage = "Backend proxy where all requests not coming from it are passed to. "
			+ "It can be specified as FQDN or host_addr[:host_port]. "
			+ "Use 'NONE' for not using a backend proxy (or let it undefined).", handler = SocketAddressHandler.class)
	public SocketAddress backendProxy=null;

}
