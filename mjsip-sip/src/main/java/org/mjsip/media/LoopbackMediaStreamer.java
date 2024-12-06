package org.mjsip.media;



import org.mjsip.net.UdpRelay;
import org.slf4j.LoggerFactory;



/** System that sends back the incoming stream.
  */
public class LoopbackMediaStreamer implements MediaStreamer {

	private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(LoopbackMediaStreamer.class);
	
	/** UdpRelay */
	UdpRelay udp_relay=null;
	

	/** Creates a new media streamer. */
	public LoopbackMediaStreamer(FlowSpec flow_spec) {
		try {
			udp_relay=new UdpRelay(flow_spec.getLocalPort(),flow_spec.getRemoteAddress(),flow_spec.getRemotePort(),null);
			LOG.trace("relay {} started", udp_relay.toString());
		}
		catch (Exception e) {
			LOG.info("Exception.", e);
		}
	}


	/** Starts media streams. */
	@Override
	public boolean start() {
		// do nothing, already started..  
		return true;      
	}


	/** Stops media streams. */
	@Override
	public boolean halt() {
		if (udp_relay!=null) {
			udp_relay.halt();
			udp_relay=null;
			LOG.trace("relay halted");
		}      
		return true;
	}


	// *************************** Callbacks ***************************

	/** From UdpRelayListener. When the remote source address changes. */
	public void onUdpRelaySourceChanged(UdpRelay udp_relay, String remote_src_addr, int remote_src_port) {
		LOG.info("UDP relay: remote address changed: {}:{}", remote_src_addr, remote_src_port);
	}

	/** From UdpRelayListener. When UdpRelay stops relaying UDP datagrams. */
	public void onUdpRelayTerminated(UdpRelay udp_relay) {
		LOG.info("UDP relay: terminated.");
	} 

}
