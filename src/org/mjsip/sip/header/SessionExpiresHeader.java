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



/** SIP Session-Expires header field, defined in RFC 4028.
  * The Session-Expires header field conveys the session interval for a
  * SIP session.  It is placed only in INVITE or UPDATE requests, as well
  * as in any 2xx response to an INVITE or UPDATE.  Like the SIP Expires
  * header field, it contains a delta-time.
  * <p>
  * The absolute minimum for the Session-Expires header field is 90
  * seconds.
  */
public class SessionExpiresHeader extends ParametricHeader {
	
	/** Refresher parameter. */
	public static final String PARAM_REFRESHER="refresher";

	/** Refresher parameter value "uac". */
	public static final String PARAM_REFRESHER_UAC="uac";

	/** Refresher parameter value "uas". */
	public static final String PARAM_REFRESHER_UAS="uas";


	/** State delimiters. */
	private static final char [] delim={',', ';', ' ', '\t', '\n', '\r'};



	/** Creates a new SessionExpiresHeader. */
	public SessionExpiresHeader(Header hd) {
		super(hd);
	}

	/** Creates a new SessionExpiresHeader.
	  * @param delta_seconds delta-time (in seconds) before the session expires. */
	public SessionExpiresHeader(int delta_seconds) {
		super(SipHeaders.Session_Expires,String.valueOf(delta_seconds));
	}

	/** Creates a new SessionExpiresHeader.
	  * @param delta_seconds delta-time (in seconds) before the session expires
	  * @param refresher indicates who is doing the refreshing ("uac" or "uas"). */
	public SessionExpiresHeader(int delta_seconds, String refresher) {
		super(SipHeaders.Session_Expires,String.valueOf(delta_seconds));
		if (refresher!=null) setParameter(PARAM_REFRESHER,refresher);
	}


	/** Gets delta-time. */
	public int getDeltaSeconds() {
		return Integer.parseInt((new Parser(value)).getWord(delim));
	}

	/** Whether it has 'refresher' parameter. */
	public boolean hasRefresher() {
		return hasParameter(PARAM_REFRESHER);
	}

	/** Gets 'refresher' parameter. */
	public String getRefresher() {
		return getParameter(PARAM_REFRESHER);
	}

	/** Sets 'refresher' parameter. */
	public void setRefresher(String refresher) {
		setParameter(PARAM_REFRESHER,refresher);
	}

}

