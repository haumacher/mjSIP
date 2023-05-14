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

package org.mjsip.sdp.field;



import org.mjsip.sdp.SdpField;
import org.zoolu.util.Parser;



/** SDP origin field.
  * <p>
  * <BLOCKQUOTE><PRE>
  *    origin-field = "o=" username SP sess-id SP sess-version SP
  *                        nettype SP addrtype SP unicast-address CRLF
  * </PRE></BLOCKQUOTE>
  */
public class OriginField extends SdpField {
	
	/** Creates a new OriginField.
	 * @param origin the origin field */
	public OriginField(String origin) {
		super('o',origin);
	}

	/** Creates a new OriginField.
	 * @param username the name of the owner of the session
	 * @param sess_id session identifier
	 * @param sess_version session version
	 * @param addrtype address type, e.g. IP4, IP6 (default is IP4)
	 * @param address the address */
	public OriginField(String username, String sess_id, String sess_version, String addrtype, String address) {
		//super('o',username+" "+sess_id+" "+sess_version+" IN "+addrtype+" "+address);
		super('o',getValue(username,sess_id,sess_version,addrtype,address));
	}

	/** Creates a new OriginField.
	 * @param username the name of the owner of the session
	 * @param sess_id session identifier
	 * @param sess_version session version
	 * @param address the IPv4 address */
	/*public OriginField(String username, String sess_id, String sess_version, String address) {
		//super('o',username+" "+sess_id+" "+sess_version+" IN IP4 "+address);
		super('o',getValue(username,sess_id,sess_version,null,address));
	}*/

	/** Creates a new OriginField.
	 * @param username the name of the owner of the session
	 * @param addrtype address type, e.g. IP4, IP6 (default is IP4)
	 * @param address the IPv4 address */
	public OriginField(String username, String addrtype, String address) {
		super('o',getValue(username,null,null,addrtype,address));
	}

	/** Gets the value of the origin field.
	 * @return a string with the value of the origin field */
	private static String getValue(String username, String sess_id, String sess_version, String addrtype, String address) {
		StringBuffer sb=new StringBuffer();
		if (username==null || username.length()==0) username="-";
		if (sess_id==null || sess_id.length()==0) sess_id="0";
		if (sess_version==null || sess_version.length()==0) sess_version="0";
		if (addrtype==null || addrtype.length()==0) addrtype="IP4";
		sb.append(username);
		sb.append(' ').append(sess_id);
		sb.append(' ').append(sess_version);
		sb.append(' ').append("IN").append(' ').append(addrtype);
		sb.append(' ').append(address);
		return sb.toString();
	}

	/** Creates a new OriginField.
	 * @param sf origin field */
	public OriginField(SdpField sf) {
		super(sf);
	}
		
	/** Gets the user name.
	 * @return the name of the owner of the session */
	public String getUserName() {
		return (new Parser(value)).getString();
	}

	/** Gets the session id.
	 * @return the session identifier */
	public String getSessionId() {
		return (new Parser(value)).skipString().getString();
	}

	/** Gets the session version.
	 * @return the session version */
	public String getSessionVersion() {
		return (new Parser(value)).skipString().skipString().getString();
	}

	/** Gets the address type.
	 * @return the address type */
	public String getAddressType() {
		return (new Parser(value)).skipString().skipString().skipString().skipString().getString();
	}

  /** Gets the address.
	 * @return the address */
	public String getAddress() {
		return (new Parser(value)).skipString().skipString().skipString().skipString().skipString().getString();
	}

}
