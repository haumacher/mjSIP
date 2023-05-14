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

/** SIP Header fieald Content-Disposition.
  */
public class ContentDispositionHeader extends TokenParametricHeader {
	
	/** Disposition "alert"; the body is a custom ring tone to alert the user */
	protected static final String DISPOSITION_ALERT="alert";

	/** Disposition "icon"; the body is displayed as an icon to the user */
	protected static final String DISPOSITION_ICON="icon";

	/** Disposition "render"; the body should be displayed to the user */
	protected static final String DISPOSITION_RENDER="render";

	/** Disposition "session"; the body describes a communications session, for example, as SDP body */
	protected static final String DISPOSITION_SESSION="session";



	/** Creates a ContentDispositionHeader with value <i>hvalue</i>. */
	public ContentDispositionHeader(String hvalue) {
		super(SipHeaders.Content_Disposition,hvalue);
	}

	/** Creates a new ContentDispositionHeader equal to ContentDispositionHeader <i>hd</i>. */
	public ContentDispositionHeader(Header hd) {
		super(hd);
	}

	/** Gets the disposition. */
	public String getDisposition() {
		return getToken();
	}

	/** Whether disposition equals to the given disposition value. */
	public boolean dispositionEqualsTo(String disposition_value) {
		return tokenEqualsTo(disposition_value);
	}

	/** Whether disposition is "session". */
	public boolean isSession() {
		return dispositionEqualsTo(DISPOSITION_SESSION);
	}

	/** Whether disposition is "rener". */
	public boolean isRender() {
		return dispositionEqualsTo(DISPOSITION_RENDER);
	}

	/** Whether disposition is "icon". */
	public boolean isIcon() {
		return dispositionEqualsTo(DISPOSITION_ICON);
	}

	/** Whether disposition is "alert". */
	public boolean isAlert() {
		return dispositionEqualsTo(DISPOSITION_ALERT);
	}

}
