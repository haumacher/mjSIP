package org.mjsip.media;



import org.mjsip.net.UdpRelay;
import org.zoolu.util.ExceptionPrinter;
import org.zoolu.util.LogLevel;
import org.zoolu.util.Logger;



/** System that sends back the incoming stream.
  */
public class LoopbackMediaStreamer implements MediaStreamer {
	
	/** Logger */
	Logger logger=null;

	/** UdpRelay */
	UdpRelay udp_relay=null;
	

	/** Creates a new media streamer. */
	public LoopbackMediaStreamer(FlowSpec flow_spec, Logger logger) {
		this.logger=logger;  
		try {
			udp_relay=new UdpRelay(flow_spec.getLocalPort(),flow_spec.getRemoteAddress(),flow_spec.getRemotePort(),null);
		log(LogLevel.TRACE,"relay "+udp_relay.toString()+" started");
		}
		catch (Exception e) {  log(LogLevel.INFO,e);  }
	}


	/** Starts media streams. */
	public boolean start() {
		// do nothing, already started..  
		return true;      
	}


	/** Stops media streams. */
	public boolean halt() {
		if (udp_relay!=null) {
			udp_relay.halt();
			udp_relay=null;
			log(LogLevel.TRACE,"relay halted");
		}      
		return true;
	}


	// *************************** Callbacks ***************************

	/** From UdpRelayListener. When the remote source address changes. */
	public void onUdpRelaySourceChanged(UdpRelay udp_relay, String remote_src_addr, int remote_src_port) {
		log(LogLevel.INFO,"UDP relay: remote address changed: "+remote_src_addr+":"+remote_src_port);
	}

	/** From UdpRelayListener. When UdpRelay stops relaying UDP datagrams. */
	public void onUdpRelayTerminated(UdpRelay udp_relay) {
		log(LogLevel.INFO,"UDP relay: terminated.");
	} 


	// ****************************** Logs *****************************

	/** Default log level offset */
	static final int LOG_OFFSET=0;
	

	/** Adds a new string to the default Log */
	private void log(String str) {
		log(LogLevel.INFO,str);
	}


	/** Adds a new string to the default Log */
	private void log(LogLevel level, String str) {
		if (logger!=null) logger.log(level,"LoopbackMediaApp: "+str);  
		if (level.getValue()>=LogLevel.INFO.getValue()) System.out.println("LoopbackMediaApp: "+str);
	}


	/** Adds the Exception message to the default Log */
	private void log(LogLevel level, Exception e) {
		log(level,"Exception: "+ExceptionPrinter.getStackTraceOf(e));
	if (level.getValue()>=LogLevel.INFO.getValue()) e.printStackTrace();
	}

}
