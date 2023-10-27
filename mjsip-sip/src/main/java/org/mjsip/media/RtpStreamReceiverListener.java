package org.mjsip.media;



import org.zoolu.net.SocketAddress;



/** Listens for RtpStreamReceiver events. 
 */
public interface RtpStreamReceiverListener {
	
	/** When the remote socket address (source) is changed. */
	public void onRemoteSoAddressChanged(RtpStreamReceiver rr, SocketAddress remote_soaddr);

	/** When the stream receiver terminated. */
	public void onRtpStreamReceiverTerminated(RtpStreamReceiver rr, Exception error);

	/**
	 * Creates a listener concatenation that first calls this listener and then the given other
	 * listener.
	 */
	default RtpStreamReceiverListener andThen(RtpStreamReceiverListener other) {
		if (other == null) {
			return this;
		}

		RtpStreamReceiverListener self = this;
		return new RtpStreamReceiverListener() {
			@Override
			public void onRemoteSoAddressChanged(RtpStreamReceiver rr, SocketAddress remote_soaddr) {
				try {
					self.onRemoteSoAddressChanged(rr, remote_soaddr);
				} finally {
					other.onRemoteSoAddressChanged(rr, remote_soaddr);
				}
			}

			@Override
			public void onRtpStreamReceiverTerminated(RtpStreamReceiver rr, Exception error) {
				try {
					self.onRtpStreamReceiverTerminated(rr, error);
				} finally {
					other.onRtpStreamReceiverTerminated(rr, error);
				}
			}
		};
	}

}
