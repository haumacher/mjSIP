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




/** SIP Header fieald Reason.
  */
public class ReasonHeader extends TokenParametricHeader {
	
	/** Reason protocol "SIP" */
	protected static final String REASON_SIP="SIP";

	/** Reason protocol "Q.850" */
	protected static final String REASON_Q850="Q.850";

	/** Protocol "cause" parameter */
	protected static final String PARAM_CAUSE="cause";

	/** Reason "text" parameter */
	protected static final String PARAM_TEXT="text";



	/** Creates a ReasonHeader with value <i>hvalue</i>. */
	public ReasonHeader(String hvalue) {
		super(SipHeaders.Reason,hvalue);
	}

	/** Creates a new ReasonHeader equal to ReasonHeader <i>hd</i>. */
	public ReasonHeader(Header hd) {
		super(hd);
	}

	/** Gets the reason. */
	public String getReason() {
		return getToken();
	}

	/** Whether reason protocol is "SIP". */
	public boolean isReasonProtocolSIP() {
		return tokenEqualsTo(REASON_SIP);
	}

	/** Whether reason protocol is "Q.850". */
	public boolean isReasonProtocolQ850() {
		return tokenEqualsTo(REASON_Q850);
	}


	/** Whether has "cause" parameter. */
	public boolean hasParameterCause() {
		return hasParameter(PARAM_CAUSE);
	}
	
	/** Gets the "cause" parameter. */
	public String getParameterCause() {
		return getParameter(PARAM_CAUSE);
	}

	/** Whether has "text" parameter. */
	public boolean hasParameterText() {
		return hasParameter(PARAM_TEXT);
	}
	
	/** Gets the "text" parameter. */
	public String getParameterText() {
		return getParameter(PARAM_TEXT);
	}

}
