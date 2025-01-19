/*
 * Copyright (C) 2007 Luca Veltri - University of Parma - Italy
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

package org.mjsip.sip.address;



import org.mjsip.sip.provider.SipParser;



/** Class <i>NameAddress</i> is used to rapresent any valid SIP Name Address.
  * It contains a SIP or non-SIP URI and optionally a display name.
  * <BR> A  Name Address is a string of the form of:
  * <BR><BLOCKQUOTE><PRE>&nbsp;&nbsp; [ display-name ] address
  * <BR>&nbsp;&nbsp; where address can be a valid URI</PRE></BLOCKQUOTE>
*/
public class NameAddress {
	
	/**
	 * Parses a {@link NameAddress} from a string.
	 */
	public static NameAddress parse(String str) {
		SipParser par = new SipParser(str);
		NameAddress naddr = par.getNameAddress();
		return naddr;
	}

	/** Display name. */
	private final String name;

	/** URI. */
	private final GenericURI uri;


	/** Creates a new NameAddress. */
	public NameAddress(String display_name, GenericURI uri) {
		this.name=display_name;
		this.uri=uri;
	}

	/** Creates a new NameAddress. */
	public NameAddress(GenericURI uri) {
		this(null, uri);
	}

	/** Creates a new NameAddress. */
	public NameAddress(NameAddress naddr) {
		name=naddr.getDisplayName();
		uri=naddr.getAddress();
	}

	/** Creates a copy of this object. */
	@Override
	public Object clone() {
		return new NameAddress(this);
	}

	/** Whether object <i>obj</i> is "equal to" this. */
	@Override
	public boolean equals(Object obj) {
		try {
			NameAddress naddr=(NameAddress)obj;
			return ((name==naddr.name) || name.equals(naddr.name)) && uri.equals(naddr.getAddress());
		}
		catch (Exception e) {
			return false;
		}
	}

	/** Gets address of NameAddress */
	public GenericURI getAddress() {
		return uri;
	}

	/** Gets display name (returns null id display name does not exist). */
	public String getDisplayName() {
		return name;
	}

	/** Whether there is a display name. */
	public boolean hasDisplayName() {
		return name!=null;
	}

	/** Gets string representation of this object. */
	@Override
	public String toString() {
		StringBuilder sb=new StringBuilder();
		if (hasDisplayName()) sb.append('\"').append(name).append("\" <").append(uri).append('>');
		else sb.append('<').append(uri).append('>');
		return sb.toString();
	}

}
