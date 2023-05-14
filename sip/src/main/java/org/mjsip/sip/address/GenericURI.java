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

import org.mjsip.sip.provider.SipParser;
import org.zoolu.util.Parser;



/** Generic URI, according to RFC 2369.
  */
public class GenericURI {
	
	/** Scheme of SIP URI */
	public static final String SCHEME_SIP="sip";

	/** Scheme of SIPS URI */
	public static final String SCHEME_SIPS="sips";

	/** Scheme of TEL URI */
	public static final String SCHEME_TEL="tel";

	/** Lr param name */
	public static final String PARAM_LR="lr"; 



	/** Complete URI */
	protected String uri=null;



	/** Creates a new GenericURI. */
	public GenericURI(GenericURI u) {
		uri=u.uri;
	}

	/** Creates a new GenericURI. */
	public GenericURI(String uri) {
		this.uri=uri;
	}

	/** Creates and returns a copy of this object. */
	public Object clone() {
		return new GenericURI(this);
	}

	/** Whether object <i>obj</i> is "equal to" this. */
	public boolean equals(Object obj) {
		return uri.equals(obj.toString());
	}

	/** Gets URI scheme. */
	public String getScheme() {
		return uri.substring(0,uri.indexOf(':'));
	}

	/** Gets scheme specific part of the URI, i.e. the part after the colon. */
	public String getSpecificPart() {
		return uri.substring(uri.indexOf(':')+1);
	}

	/** Whether it is a SIP or SIPS URI. */
	public boolean isSipURI() {
		String scheme=getScheme();
		return scheme.equals(SCHEME_SIP) || scheme.equals(SCHEME_SIPS);
	}

	/** Whether it is a TEL URI. */
	public boolean isTelURI() {
		String scheme=getScheme();
		return scheme.equals(SCHEME_TEL);
	}

	/** Gets string representation of this object. */
	public String toString() {
		return uri;
	}



	/** Whether lr (loose-route) parameter is present. */
	public boolean hasLr() {
		return hasParameter(PARAM_LR);
	}
	/** Adds lr parameter. */
	public void addLr()  {
		addParameter(PARAM_LR);
	}



	/** Gets the value of specified parameter.
	  * @return Returns the value of the specified parameter or null if not present. */
	public String getParameter(String name)  {
		SipParser par=new SipParser(uri);
		return ((SipParser)par.goTo(';').skipChar()).getParameter(name);
	}
	
	/** Gets a String Vector of parameter names.
	  * @return Returns a String Vector of all parameter names or null if no parameter is present. */
	public Vector getParameterNames()  {
		SipParser par=new SipParser(uri);
		return ((SipParser)par.goTo(';').skipChar()).getParameterNames();
	}
	
	/** Whether there is the specified parameter. */
	public boolean hasParameter(String name) {
		SipParser par=new SipParser(uri);
		return ((SipParser)par.goTo(';').skipChar()).hasParameter(name);
	}
	
	/** Whether there are any parameters. */
	public boolean hasParameters() {
		if (uri!=null && uri.indexOf(';')>=0) return true;
		else return false;
	}
	
	/** Adds a new parameter without a value. */
	public void addParameter(String name)  {
		uri=uri+";"+name;       
	}
	
	/** Adds a new parameter with value. */
	public void addParameter(String name, String value)  {
		if (value!=null) uri=uri+";"+name+"="+value;
		else uri=uri+";"+name;       
	}

	/** Removes all parameters (if any). */
	public void removeParameters()  {
		int index=uri.indexOf(';');
		if (index>=0) uri=uri.substring(0,index);      
	}

	/** Removes specified parameter (if present). */
	public void removeParameter(String name)  {
		int index=uri.indexOf(';');
		if (index<0) return;
		Parser par=new Parser(uri,index);
		while (par.hasMore()) {
			int begin_param=par.getPos();
			par.skipChar();
			if (par.getWord(SipParser.param_separators).equals(name)) {
				String top=uri.substring(0,begin_param); 
				par.goToSkippingQuoted(';');
				String bottom="";
				if (par.hasMore()) bottom=uri.substring(par.getPos()); 
				uri=top.concat(bottom);
				return;
			}
			par.goTo(';');
		}
	}

}


