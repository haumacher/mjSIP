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

package org.mjsip.sip.header;



import java.util.Vector;

import org.zoolu.util.Parser;



/** Generic SIP Header containing a list of tokens (Strings). */
public abstract class ListHeader extends LegacyHeader {
	
	/** Creates a new ListHeader. */
	public ListHeader(Header hd) {
		super(hd);
	}

	/** Creates a new ListHeader. */
	public ListHeader(String hname, String hvalue) {
		super(hname,hvalue);
	}

	/** Creates a new ListHeader. */
	public ListHeader(String hname, Vector<String> elements) {
		super(hname,null);
		setElements(elements);
	}

	/** Creates a new ListHeader. */
	public ListHeader(String hname, String[] elements) {
		super(hname,null);
		setElements(elements);
	}


	/** Gets list of tokens (as Vector of Strings). */
	public Vector<String> getElements() {
		Vector<String> elements=new Vector<>();
		Parser par=new Parser(value);
		char[] delim={ ',' };
		while (par.hasMore()) {
			String elem=par.getWord(delim).trim();
			if (elem!=null && elem.length()>0) elements.addElement(elem);
			par.skipChar();
		} 
		return elements;
	}

	/** Sets the list of tokens. */
	public void setElements(Vector<String> elements) {
		StringBuilder sb=new StringBuilder();
		for (int i=0; i<elements.size(); i++) {
			if (i>0) sb.append(","); 
			sb.append(elements.elementAt(i));
		}
		value=sb.toString();
	}

	/** Sets the list of tokens. */
	public void setElements(String[] elements) {
		StringBuilder sb=new StringBuilder();
		for (int i=0; i<elements.length; i++) {
			if (i>0) sb.append(","); 
			sb.append(elements[i]);
		}
		value=sb.toString();
	}

	/** Adds a new token to the elements list. */
	public void addElement(String elem) {
		if (value==null || value.isEmpty()) value=elem;
		else value+=", "+elem;
	}
}
