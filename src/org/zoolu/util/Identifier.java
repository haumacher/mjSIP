/*
 * Copyright (C) 2014 Luca Veltri - University of Parma - Italy
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




/** A generic identifier that has an unique string representation.
  */
public class Identifier {
	
	/** String value of the identifier */   
	protected String id;



	/** Creates a void Identifier. */
	protected Identifier() {
		
	}

	/** Creates a new Identifier.
	  * @param id string value of the identifier */
	public Identifier(String id) {
		this.id=id;
	}

	/** Creates a new Identifier.
	  * @param i an identifier */
	public Identifier(Identifier i) {
		this.id=i.id;
	}



	/** Whether this object equals to an other object.
	  * @param obj the other object that is compared to
	  * @return true if the two objects are equal */
	public boolean equals(Object obj) {
		try {
			Identifier i=(Identifier)obj;
			return id.equals(i.id);
		}
		catch (Exception e) {  return false;  }
	}

	/** Gets an int hash-code for this object.
	  * @return the hash-code */
	public int hashCode() {
		return id.hashCode();
	}

	/** Gets a string value for this object.
	  * @return the string */
	public String toString() {
		return id;
	}
}
