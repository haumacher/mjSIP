/*
 * Copyright (C) 2010 Luca Veltri - University of Parma - Italy
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

package org.mjsip.sdp.field;



import org.zoolu.util.Parser;



/** Key-param of a crypto attribute field.
  */
public class KeyParam {
	
	/** Key-param value */
	String value;

	
	/** Creates a new KeyParam. */ 
	public KeyParam(String key_method, String key_info) {
		value=key_method+":"+key_info;
	}

	/** Creates a new KeyParam. */ 
	public KeyParam(String key_param) {
		value=key_param;
	}

	/** Creates a new KeyParam. */ 
	public KeyParam(KeyParam kp) {
		value=kp.value;
	}


	/** Gets the key-method. */
	public String getKeyMethod() {
		Parser par=new Parser(value);
		char[] delim={':'};
		return par.getWord(delim);
	}
	
	/** Gets the key-info. */
	public String getKeyInfo() {
		Parser par=new Parser(value);
		return par.goTo(':').skipChar().getString();
	}

	/** Converts this object to String. */ 
	public String toString() {
		return value;
	}
}
