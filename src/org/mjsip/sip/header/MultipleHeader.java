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

import org.mjsip.sip.provider.SipParser;



/** MultipleHeader can be used to handle SIP headers that support comma-separated (multiple-header) rapresentation,
 *  as explaned in section 7.3.1 of RFC 3261.
 */
public class MultipleHeader {
	
	/** The header type */
	protected String name;
	/** Vector of header values (Vector of <code>String</code>) */
	protected Vector values;
	/** whether to be rapresented with a comma-separated(compact) header line or multiple header lines */
	protected boolean compact;



	/** Creates a void new MultipleHeader. */
	protected MultipleHeader() {
		name=null;
		values=new Vector();
		compact=true;
	}

	/** Creates a new MultipleHeader.
	  * @param hname the header name */
	public MultipleHeader(String hname) {
		name=hname;
		values=new Vector();
		compact=true;
	}

	/** Creates a new MultipleHeader.
	  * @param hname the header name
	  * @param hvalues vector of header values (Vector of <code>String</code>) */
	public MultipleHeader(String hname, Vector hvalues) {
		name=hname;
		values=hvalues;
		compact=true;
	}
	
	/** Creates a new MultipleHeader.
	  * @param headers vector of headers (Vector of <code>Header</code>). Each header can be a single header or a multiple-comma-separated header */
	public MultipleHeader(Vector headers) {
		name=((Header)headers.elementAt(0)).getName();
		values=new Vector(headers.size());
		for (int i=0; i<headers.size(); i++) {
			addBottom((Header)headers.elementAt(i));
		}
		compact=false;
	}   

	/** Creates a new MultipleHeader.
	  * @param hd comma-separated header */
	public MultipleHeader(Header hd) {
		name=hd.getName();
		values=new Vector();
		SipParser par=new SipParser(hd.getValue());
		int comma=par.indexOfCommaHeaderSeparator();
		while (comma>=0) {
			values.addElement(par.getString(comma-par.getPos()).trim());
			par.skipChar(); //skip comma
			comma=par.indexOfCommaHeaderSeparator();
		}
		values.addElement(par.getRemainingString().trim());
		compact=true;      
	}

	/** Creates a new MultipleHeader.
	  * @param mhd multiple headers */
	public MultipleHeader(MultipleHeader mhd) {
		name=mhd.getName();
		values=mhd.getValues();
		compact=mhd.isCommaSeparated();      
	}

	/** Checks if Header <i>hd</i> contains comma-separated multi-header.
	  * @param hd the header
	  * return whether the header contains comma-separated multi-header */
	public static boolean isCommaSeparated(Header hd) {
		SipParser par=new SipParser(hd.getValue());
		return par.indexOfCommaHeaderSeparator()>=0;
	}

	/** Sets the MultipleHeader rappresentation as comma-separated or multiple headers.
	  * @param comma_separated whether comma-separated or multiple headers. */
	public void setCommaSeparated(boolean comma_separated) {
		compact=comma_separated;
	}

	/** Whether the MultipleHeader rappresentation is comma-separated or multiple headers.
	  * @return whether comma-separated or multiple headers. */
	public boolean isCommaSeparated() {
		return compact;
	}

	/** Gets the size of the MultipleHeader.
	  * @return the size of the MultipleHeader. */
	public int size() {
		return values.size();
	}

	/** Whether it is empty.
	  * @return true if empty, false otherwise */
	public boolean isEmpty() {
		return values.isEmpty();
	}

	/** Creates and returns a copy of Header.
	  * @return the new Header. */
	public Object clone() {
		return new MultipleHeader(getName(),getValues());
	}

	/** Indicates whether an other Object is "equal to" this Header.
	  * @return true if it is "equal to", false otherwise */
	public boolean equals(Object obj) {
		MultipleHeader hd=(MultipleHeader)obj;
		if (hd.getName().equals(this.getName()) && hd.getValues().equals(this.getValues())) return true;
			else return false;
	}

	/** Gets name of Header.
	  * @return the header name */
	public String getName() {
		return name; 
	}

	/** Gets a vector of header values.
	  * @return the header values (Vector of <code>String</code>) */
	public Vector getValues() {
		return values;
	}

	/** Sets header values.
	  * @param v the header values (Vector of <code>String</code>) */
	public void setValues(Vector v) {
		values=v; 
	}
	
	/** Gets a vector of headers.
	  * @return vector of headers (Vector of <code>Header</code>) */
	public Vector getHeaders() {
		Vector v=new Vector(values.size());
		for (int i=0; i<values.size(); i++) {
			Header h=new Header(name,(String)values.elementAt(i));
			v.addElement(h);
		}
		return v; 
	}   

	/** Sets headers.
	  * @param hdv the headers (Vector of <code>Header</code>) */
	public void setHeaders(Vector hdv) {
		values=new Vector(hdv.size());
		for (int i=0; i<hdv.size(); i++) {
			values.addElement(((Header)hdv.elementAt(i)).getValue());
		}
	}
	
	/** Gets the i-value.
	  * @return i-header value */
	public String getValue(int i) {
		return (String)values.elementAt(i);
	}

	/** Adds top.
	  * @param value the new top header value */
	//public void addTop(String value)
	//{  values.insertElementAt(value,0);
	//}
	
	/** Adds top.
	  * @param hd the new top header */
	public void addTop(Header hd) {
		values.insertElementAt(hd.getValue(),0);
	}

	/** Gets top Header
	  * @return the top header */
	public Header getTop() {
		return new Header(name,(String)values.firstElement());
	}
	
	/** Removes the top Header. */
	public void removeTop() {
		values.removeElementAt(0);
	}   

	/** Adds bottom.
	  * @param value the new bottom header value */
	//public void addBottom(String value)
	//{  values.addElement(value);
	//}
	
	/** Adds bottom.
	  * @param hd the new bottom header */
	public void addBottom(Header hd) {
		if (!MultipleHeader.isCommaSeparated(hd)) values.addElement(hd.getValue());
		else addBottom(new MultipleHeader(hd));
	}    

	/** Adds other MultipleHeader at bottom.
	  * @param mhd the new bottom headers */
	public void addBottom(MultipleHeader mhd) {
		for (int i=0; i<mhd.size(); i++)
			values.addElement(mhd.getValue(i));   
	}

	/** Gets bottom Header.
	  * @return the bottom header */
	public Header getBottom() {
		return new Header(name,(String)values.lastElement());
	}
	
	/** Removes bottom Header. */
	public void removeBottom() {
		values.removeElementAt(values.size()-1);
	}   

	/** Gets an Header containing the comma-separated (compact) representation.
	  * @return the new comma-separated (compact) header */
	public Header toHeader() {
		String str="";
		for (int i=0; i<values.size()-1; i++) str+=values.elementAt(i)+", ";
		if (values.size()>0) str+=values.elementAt(values.size()-1);
		return new Header(name,str);
	}

	/** Gets comma-separated (compact) or multi-headers (extended) representation of this multi-header.<BR>
	  *  Note that an empty header is rapresentated as:<BR>
	  *  - empty String (i.e. ""), for multi-headers(extended) rapresentation,
	  *  - empty-value Header (i.e. "HeaderName: \r\n"), for comma-separated(compact) rapresentation.
	  * @return the string representation of this multi-header */
	public String toString() {
		if (compact) {
			String str=name+": ";
			for (int i=0; i<values.size()-1; i++) str+=values.elementAt(i)+", ";
			if (values.size()>0) str+=values.elementAt(values.size()-1);
			return str+"\r\n";
		}
		else  {
			String str="";
			for (int i=0; i<values.size(); i++) str+=name+": "+values.elementAt(i)+"\r\n";
			return str;
		}
	}

		
}

