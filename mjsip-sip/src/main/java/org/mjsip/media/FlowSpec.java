package org.mjsip.media;

import java.util.NoSuchElementException;

/** Flow(s) specification.
  */
public class FlowSpec {
	
	/** Interface for characterizing media direction */
	public enum Direction {

		/** Send only mode */
		SEND_ONLY,

		/** Receive only mode */
		RECV_ONLY,

		/** Full duplex mode */
		FULL_DUPLEX;

		/**
		 * An ASCII art arrow symbolizing the data flow direction.
		 */
		public String arrow() {
			switch (this) {
			case SEND_ONLY:
				return "-->";
			case RECV_ONLY:
				return "<--";
			case FULL_DUPLEX:
				return "<-->";
			}
			throw new NoSuchElementException(name());
		}
	}

	/** None mode */
	//public static final Direction NONE=new Direction();

	/** Loopback mode */
	//public static final Direction LOOPBACK=new Direction();

	/** Media spec */
	MediaSpec media_spec;

	/** Local port */
	int local_port;
	
	/** Remote address */
	String remote_addr;
	
	/** Remote port */
	int remote_port;
	
	/** Flow direction */
	Direction direction;
	

	
	/** Creates a new FlowSpec.
	  * @param media_spec media specification
	  * @param local_port local port
	  * @param remote_addr remote address
	  * @param remote_port remote port
	  * @param direction flow direction (Direction.SEND_ONLY, Direction.RECV_ONLY, or Direction.FULL_DUPLEX) */
	public FlowSpec(MediaSpec media_spec, int local_port, String remote_addr, int remote_port, Direction direction) {
		init(media_spec,local_port,remote_addr,remote_port,direction);
	}


	/** Inits the MediaSpec. */
	private void init(MediaSpec media_spec, int local_port, String remote_addr, int remote_port, Direction direction) {
		this.media_spec=media_spec;
		this.local_port=local_port;
		this.remote_addr=remote_addr;
		this.remote_port=remote_port;
		this.direction=direction;
	}


	/** Gets media specification. */
	public MediaSpec getMediaSpec() {
		return media_spec;
	}


	/** Gets local port. */
	public int getLocalPort() {
		return local_port;
	}


	/** Gets remote address. */
	public String getRemoteAddress() {
		return remote_addr;
	}


	/** Gets remote port. */
	public int getRemotePort() {
		return remote_port;
	}


	/** Gets direction. */
	public Direction getDirection() {
		return direction;
	}

}
