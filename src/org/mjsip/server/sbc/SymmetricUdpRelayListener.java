package org.mjsip.server.sbc;


import org.zoolu.net.SocketAddress;


/** Listener for SymmetricUdpRelay events.
  */
public interface SymmetricUdpRelayListener {
	
	/** When left peer address changes. */
	public void onSymmetricUdpRelayLeftPeerChanged(SymmetricUdpRelay symm_relay, SocketAddress soaddr);

	/** When right peer address changes. */
	public void onSymmetricUdpRelayRightPeerChanged(SymmetricUdpRelay symm_relay,SocketAddress soaddr);

	/** When it stops relaying UDP datagrams (both directions). */
	public void onSymmetricUdpRelayTerminated(SymmetricUdpRelay symm_relay);   
}
