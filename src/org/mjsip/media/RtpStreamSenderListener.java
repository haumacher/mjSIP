package org.mjsip.media;




/** Listens for RtpStreamSender events. 
 */
public interface RtpStreamSenderListener {
	
	/** When the stream sender terminated. */
	public void onRtpStreamSenderTerminated(RtpStreamSender rs, Exception error);

}
