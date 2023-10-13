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




/** SIP header field Info-Package, defined by RFC 6086.
  */
public class InfoPackageHeader extends TokenParametricHeader {
	

	/** Creates a InfoPackageHeader with value <i>hvalue</i>. */
	public InfoPackageHeader(String hvalue) {
		super(SipHeaders.Info_Package,hvalue);
	}

	/** Creates a new InfoPackageHeader equal to InfoPackageHeader <i>hd</i>. */
	public InfoPackageHeader(Header hd) {
		super(hd);
	}

	/** Gets the package. */
	public String getPackage() {
		return getToken();
	}

	/** Whether package equals to the given package value. */
	public boolean packageEqualsTo(String package_value) {
		return tokenEqualsTo(package_value);
	}

}
