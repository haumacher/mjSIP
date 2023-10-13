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



import org.zoolu.util.Parser;



/** SIP Min-SE header field, defined in RFC 4028.
  * The Min-SE header field indicates the minimum value for the session
  * interval, in units of delta-seconds.  When used in an INVITE or
  * UPDATE request, it indicates the smallest value of the session
  * interval that can be used for that session.
  * <p>
  * When present in a request or response, its value MUST NOT be less
  * than 90 seconds.
  */
public class MinSEHeader extends ParametricHeader {
	

	/** State delimiters. */
	private static final char [] delim={',', ';', ' ', '\t', '\n', '\r'};



	/** Creates a new MinSEHeader. */
	public MinSEHeader(Header hd) {
		super(hd);
	}

	/** Creates a new MinSEHeader.
	  * @param delta_seconds minimum value (in seconds) for the session interval. */
	public MinSEHeader(int delta_seconds) {
		super(SipHeaders.Min_SE,String.valueOf(delta_seconds));
	}


	/** Gets delta-time. */
	public int getDeltaSeconds() {
		return Integer.parseInt((new Parser(value)).getWord(delim));
	}


}
