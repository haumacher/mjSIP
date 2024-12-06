package org.mjsip.media;



import org.mjsip.net.UdpRelay;
import org.slf4j.LoggerFactory;



/** Media streamer based on a native command-line application.
  */
public class NativeMediaStreamer implements MediaStreamer {
	
	private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(NativeMediaStreamer.class);

	/** Runtime media process (native media application) */
	Process media_process=null;
	
	int local_port;
	int remote_port;
	
	/** Media application command */
	String command;

	/** Command-line arguments */
	String[] args;


	/** Creates a new media streamer.
	  * @param command the command-line program to be run
	  * @param args command-line arguments that have to be passed to the program
	  * @param logger the logger where running information are logged */
	public NativeMediaStreamer(String command, String[] args) {
		init(command, args, 0, 0);
	}

	/** Creates a new media streamer.
	  * @param command the command-line program to be run
	  * @param args command-line arguments that have to be passed to the program
	  * @param local_port local media port
	  * @param remote_port remote media port
	  * @param logger the logger where running information are logged */
	public NativeMediaStreamer(String command, String[] args, int local_port, int remote_port) {
		init(command, args, local_port, remote_port);
	}

	/** Inits the media streamer.
	  * @param command the command-line program to be run
	  * @param args command-line arguments that have to be passed to the program
	  * @param local_port local media port
	  * @param remote_port remote media port
	  * @param logger the logger where running information are logged */
	private void init(String command, String[] args, int local_port, int remote_port) {
		this.command=command;
		this.args=args;
		this.local_port=local_port;
		this.remote_port=remote_port;
	}

	/** Starts this media streams. */
	@Override
	public boolean start() {
		// udp flow adaptation for media streamer
		if (local_port!=remote_port)  {
			log("UDP local relay: src_port="+local_port+", dest_port="+remote_port);
			log("UDP local relay: src_port="+(local_port+1)+", dest_port="+(remote_port+1));
			new UdpRelay(local_port,"127.0.0.1",remote_port,null);
			new UdpRelay(local_port+1,"127.0.0.1",remote_port+1,null);  
		}
		else {
			log("local_port==remote_port --> no UDP relay is needed");
		}

		//debug...
		log("starting native media application ("+command+")");
	 
		String cmds[]=new String[((args!=null)?args.length:0)+1];
		cmds[0]=command;
        System.arraycopy(args, 0, cmds, 1, cmds.length - 1);

		// try to start the media application
		try {
			media_process=Runtime.getRuntime().exec(cmds);
			return true;
		}
		catch (Exception e) {
			e.printStackTrace();
			return false;
		}          
	}

	/** Stops this media streams. */
	@Override
	public boolean halt() {
		log("stopping native media application ("+command+")");
		if (media_process!=null) media_process.destroy();
		return true;
	}

	// ****************************** Logs *****************************

	/**
	 * Adds a new string to the default Log.
	 * 
	 * @param str
	 *        the string message to be logged.
	 */
	private void log(String str) {
		LOG.info("NativeMediaApp: {}", str);
	}

}
