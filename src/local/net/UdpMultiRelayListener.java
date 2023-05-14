package local.net;



import org.zoolu.net.SocketAddress;



/** Listener for MultiUdpRelay events.
  */
public interface UdpMultiRelayListener
{
   /** When the remote source address changes. */
   public void onUdpMultiRelaySourceAddressChanged(UdpMultiRelay mrelay, SocketAddress src_soaddr);

   /** When UdpRelay stops relaying UDP datagrams. */
   public void onUdpMultiRelayTerminated(UdpMultiRelay mrelay); 
}
