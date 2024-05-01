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

package org.mjsip.sip.address;



import java.util.Vector;



/** Generic URI, according to RFC 2369.
  */
public abstract class GenericURI {
	
	/** Scheme of SIP URI */
	public static final String SCHEME_SIP="sip";

	/** Scheme of SIPS URI */
	public static final String SCHEME_SIPS="sips";

	/** Scheme of TEL URI */
	public static final String SCHEME_TEL="tel";

	/** Lr param name */
	public static final String PARAM_LR="lr"; 

	@Override
	public abstract Object clone();

	/** Whether object <i>obj</i> is "equal to" this. */
	@Override
	public boolean equals(Object obj) {
		return toString().equals(obj.toString());
	}

	/** Gets URI scheme. */
	public abstract String getScheme();

	/** Gets scheme specific part of the URI, i.e. the part after the colon. */
	public abstract String getSpecificPart();

	/** Whether it is a SIP or SIPS URI. */
	public abstract boolean isSipURI();

	/**
	 * Converts this URI to a {@link SipURI}.
	 * 
	 * <p>
	 * Note: Must only be called if {@link #isSipURI()} is <code>true</code>.
	 * </p>
	 */
	public abstract SipURI toSipURI();

	/** Whether it is a TEL URI. */
	public abstract boolean isTelURI();

	/** Gets string representation of this object. */
	@Override
	public abstract String toString();

	/** Gets the value of specified parameter.
	  * @return Returns the value of the specified parameter or null if not present. */
	public abstract String getParameter(String name);
	
	/** Gets a String Vector of parameter names.
	  * @return Returns a String Vector of all parameter names or null if no parameter is present. */
	public abstract Vector<String> getParameterNames();
	
	/** Whether there is the specified parameter. */
	public abstract boolean hasParameter(String name);
	
	/** Whether there are any parameters. */
	public abstract boolean hasParameters();
	
	/** Adds a new parameter without a value. */
	public abstract void addParameter(String name);
	
	/** Adds a new parameter with value. */
	public abstract void addParameter(String name, String value);

	/** Removes all parameters (if any). */
	public abstract void removeParameters();

	/** Removes specified parameter (if present). */
	public abstract void removeParameter(String name);

	/** Whether lr (loose-route) parameter is present. */
	public boolean hasLr() {
		return hasParameter(PARAM_LR);
	}

	/**
	 * Adds the <code>lr</code> (loose-route) parameter.
	 */
	public GenericURI addLr() {
		addParameter(PARAM_LR);
		return this;
	}

}


