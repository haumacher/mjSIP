package org.mjsip.media;

/** Listens for RtpStreamSender events. 
 */
public interface RtpStreamSenderListener {
	
	/** When the stream sender terminated. */
	public void onRtpStreamSenderTerminated(RtpStreamSender rs, Exception error);

	/**
	 * Creates a listener concatenation that first calls this listener and then calls the given
	 * other listener.
	 */
	default RtpStreamSenderListener andThen(RtpStreamSenderListener other) {
		if (other == null) {
			return this;
		}
		RtpStreamSenderListener self = this;
		return new RtpStreamSenderListener() {
			@Override
			public void onRtpStreamSenderTerminated(RtpStreamSender rs, Exception error) {
				try {
					self.onRtpStreamSenderTerminated(rs, error);
				} finally {
					other.onRtpStreamSenderTerminated(rs, error);
				}
			}
		};
	}

}
