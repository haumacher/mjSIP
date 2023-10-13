/*
 * Copyright (C) 2012 Luca Veltri - University of Parma - Italy
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



import org.mjsip.sip.provider.SipParser;



/** Abstract TokenParametricHeader is the base class for all SIP Header fields that contains a token,
  * optionally followed by parameters */
public abstract class TokenParametricHeader extends ParametricHeader {
	

	/** Creates a new TokenParametricHeader. */
	protected TokenParametricHeader(String hname, String hvalue) {
		super(hname,hvalue);
	}

	/** Creates a new TokenParametricHeader. */
	protected TokenParametricHeader(Header hd) {
		super(hd);
	}

	/** Gets token value. */
	protected String getToken() {
		int index=indexOfFirstSemi();
		String str=(index<0)? value : value.substring(0,index);
		return (new SipParser(str)).getString();
	}

	/** Whether token equals the given value. */
	protected boolean tokenEqualsTo(String token_value) {
		return getToken().equals(token_value);
	}


}
