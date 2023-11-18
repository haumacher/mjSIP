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

		/**
		 * Whether to send data.
		 */
		public boolean doSend() {
			switch (this) {
			case FULL_DUPLEX:
			case SEND_ONLY:
				return true;
			case RECV_ONLY:
				return false;
			}
			throw new AssertionError("No such direction: " + this);
		}

		/**
		 * Whether to receive data.
		 */
		public boolean doReceive() {
			switch (this) {
			case FULL_DUPLEX:
			case RECV_ONLY:
				return true;
			case SEND_ONLY:
				return false;
			}
			throw new AssertionError("No such direction: " + this);
		}
	}

	/** Media spec */
	private final MediaSpec media_spec;

	/** Local port */
	private final int local_port;
	
	/** Remote address */
	private final String remote_addr;
	
	/** Remote port */
	private final int remote_port;
	
	/** Flow direction */
	private final Direction direction;

	private String _mediaType;
	
	/**
	 * Creates a new {@link FlowSpec}.
	 * 
	 * @param mediaType
	 *        The type of media transported.
	 * @param media_spec
	 *        media specification
	 * @param local_port
	 *        local port
	 * @param remote_addr
	 *        remote address
	 * @param remote_port
	 *        remote port
	 * @param direction
	 *        flow direction (Direction.SEND_ONLY, Direction.RECV_ONLY, or Direction.FULL_DUPLEX)
	 */
	public FlowSpec(String mediaType, MediaSpec media_spec, int local_port, String remote_addr, int remote_port, Direction direction) {
		_mediaType = mediaType;
		this.media_spec=media_spec;
		this.local_port=local_port;
		this.remote_addr=remote_addr;
		this.remote_port=remote_port;
		this.direction=direction;
	}

	/**
	 * The type of media being transported.
	 * 
	 * @see MediaDesc#getMediaType()
	 */
	public String getMediaType() {
		return _mediaType;
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

	@Override
	public String toString() {
		return getMediaType() + " " + getMediaSpec() + ": " + getLocalPort() + " " + getDirection().arrow() + " "
				+ getRemoteAddress() + ":" + getRemotePort();
	}
}
