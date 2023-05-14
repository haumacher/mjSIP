/*
 * Copyright (C) 2005 Luca Veltri - University of Parma - Italy
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

package org.mjsip.sdp;


import org.mjsip.sdp.field.TimeField;


/** Time description.
 * It is formed by a time field ('t') and zero or more repeat times fields ('r'). 
 */
public class TimeDescription {
	
	/** Time field */
	TimeField t;
	/** Zero or more repeat fields */
	SdpField[] rv;

	
	/** Creates a new time description.
	  * @param t time field
	  * @param rv repeat fields */
	public TimeDescription(TimeField t, SdpField[] rv) {
		this.t=t;
		this.rv=rv;
	}

	/** Creates a new time description.
	  * @param t time field */
	public TimeDescription(TimeField t) {
		this.t=t;
	}
			  
	/** Gets time field.
	  * @return the time field */
	public TimeField getTimeField() {
		return t;
	} 

	/** Gets repeat fields.
	  * @return array of repeat fields */
	public SdpField[] getRepeatFields() {
		return rv;
	} 
	
	/** Gets a string representation of the time description.
	  * @return the string representation */
	public String toString() {
		StringBuffer sb=new StringBuffer();
		sb.append(t.toString());
		if (rv!=null) for (int i=0; i<rv.length; i++) sb.append(rv[i].toString());
		return sb.toString();
	}
	
}

