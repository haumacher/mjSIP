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

package org.zoolu.util;




/** Generic state.
  */
abstract public class State {
	
	/** The state value */
	protected int state=0;


	/** Creates a State. */
	public State() {
		
	}

	/** Creates a State.
	  * @param state the initial state value */
	public State(int state) {
		this.state=state;
	}

	/** Creates a State.
	  * @param s the initial state */
	public State(State s) {
		setState(s.state);
	}

	/** Changes the state value.
	  * @param state the new state value */
	public void setState(int state) {
		this.state=state;
	}

	/** Gets the state value.
	  * @return the state value */
	public int getState() {
		return state;
	}

	/** Whether the state is equal to the given object.
	  * @param obj the compared object */
	public boolean equals(Object obj) {
		if (obj instanceof State) return equals(((State)obj).state);
		else return false;
	}

	/** Whether the state has a given value.
	  * @param state the compared state */
	public boolean equals(int state) {
		return this.state==state;
	}

	/** Gets a string representation of this object. */
	public String toString() {
		return "S_"+state;
	}

}
