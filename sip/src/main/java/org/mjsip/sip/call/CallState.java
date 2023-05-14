/*
 * Copyright (C) 2013 Luca Veltri - University of Parma - Italy
 * 
 * This file is part of MjSip (http://www.mjsip.org)
 * 
 * MjSip is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * MjSip is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with MjSip; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 * 
 * Author(s):
 * Luca Veltri (luca.veltri@unipr.it)
 */

package org.mjsip.sip.call;



import org.zoolu.util.State;



/** Call state.
  */
public class CallState extends State {
	
	/** Call state IDLE */
	public static final int C_IDLE=0;   

	/** Call state INCOMING */
	public static final int C_INCOMING=1;

	/** Call state OUTGOING */
	public static final int C_OUTGOING=2;

	/** Call state ACTIVE */
	public static final int C_ACTIVE=6;

	/** Call state CLOSED */
	public static final int C_CLOSED=9;



	/** Creates a CallState. */
	public CallState() {
		super(C_IDLE);
	}

	/** Creates a CallState. */
	public CallState(int state) {
		super(state);
	}

	/** Creates a CallState. */
	public CallState(CallState s) {
		super(s);
	}

	/** Whether the call is in "idle" state. */
	public boolean isIdle() {
		return equals(C_IDLE);
	}

	/** Whether the call is in "incoming" (called) state. */
	public boolean isIncoming() {
		return equals(C_INCOMING);
	}

	/** Whether the call is in "outgoing" (calling) state. */
	public boolean isOutgoing() {
		return equals(C_OUTGOING);
	}

	/** Whether the call is in "active" (call) state. */
	public boolean isActive() {
		return equals(C_ACTIVE);
	}

	/** Whether the call is in "closed" state. */
	public boolean isClosed() {
		return equals(C_CLOSED);
	}

	/** Gets a string representation of the state. */
	public String toString() {
		switch (state) {
			case C_IDLE       : return "C_IDLE";
			case C_INCOMING   : return "C_INCOMING";   
			case C_OUTGOING   : return "C_OUTGOING";
			case C_ACTIVE     : return "C_ACTIVE";
			case C_CLOSED     : return "C_CLOSED";   
			default : return null;
		}
	}

}
