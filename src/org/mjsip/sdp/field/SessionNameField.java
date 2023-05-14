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



/** SDP session name field.
  * <p>
  * <BLOCKQUOTE><PRE>
  *    session-name-field = "s=" text CRLF
  * </PRE></BLOCKQUOTE>
  */
public class SessionNameField extends SdpField {
	
	/** Creates a new SessionNameField.
	 * @param session_name session name */
	public SessionNameField(String session_name) {
		super('s',session_name);
	}

	/** Creates a new SessionNameField with value '-'. */
	public SessionNameField() {
		super('s',"-");
	}

	/** Creates a new SessionNameField.
	 * @param sf the session name filed */
	public SessionNameField(SdpField sf) {
		super(sf);
	}
		
	/** Gets the session name.
	 * @return the session name */
	public String getSession() {
		return value;
	}

}
