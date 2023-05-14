package org.mjsip.server;



import org.mjsip.sip.message.SipMessage;



/** CallLogger keeps a complete trace of processed calls.
  */
public interface CallLogger {
	
	/** Updates log with the present message. */
	public void update(SipMessage msg);
}
