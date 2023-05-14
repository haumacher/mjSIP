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



import org.zoolu.util.Parser;



/** Class <i>TelURI</i> implements "tel" URIs, as defined by RFC 3966.
  * <p>
  * The "tel" URI is a globally unique identifier ("name") only; it does
  * not describe the steps necessary to reach a particular number and
  * does not imply dialling semantics.
  */
public class TelURI extends GenericURI {
	

	/** ISDN subaddress parameter */
	public static final String PARAM_ISUB="isub"; 
	
	/** Extension parameter */
	public static final String PARAM_EXT="extension"; 

	/** Context parameter */
	public static final String PARAM_CONTEXT="phone-context"; 



	/** Scheme of TEL URI */
	protected static final String TEL_COLON="tel:";



	/** Creates a new TelURI. */
	public TelURI(GenericURI u) {
		super(u);
		String scheme=getScheme();
		if (!scheme.equals(SCHEME_TEL)) throw new UnexpectedUriSchemeException(scheme);
	}


	/** Creates a new TelURI.
	  * @param uri the URI wth or without the "tel:" scheme prefix. */
	public TelURI(String uri) {
		super(uri);
		if(!uri.startsWith(TEL_COLON)) this.uri=TEL_COLON+uri;
	}


	/** Gets digits of the global or local number. */
	public String getNumber() {
		char[] host_terminators={':',';','?'};
		Parser par=new Parser(uri);
		int begin=getScheme().length()+1; // skip "sip:"
		par.setPos(begin);
		int end=par.indexOf(host_terminators);
		if (end<0) return uri.substring(begin);
		else return uri.substring(begin,end);
	}



	/** Gets ISDN subaddress parameter. */
	public String getIsdnSubaddress()  {
		return getParameter(PARAM_ISUB);
	}  

	/** Whether ISDN subaddress parameter is present. */
	public boolean hasIsdnSubaddress() {
		return hasParameter(PARAM_ISUB);
	}

	/** Adds ISDN subaddress parameter. */
	public void addIsdnSubaddress(String subaddress)  {
		addParameter(PARAM_ISUB,subaddress);
	}



	/** Gets "extension" parameter. */
	public String getExtension()  {
		return getParameter(PARAM_EXT);
	}  

	/** Whether "extension" parameter is present. */
	public boolean hasExtension() {
		return hasParameter(PARAM_EXT);
	}

	/** Adds "extension" parameter. */
	public void addExtension(String extension)  {
		addParameter(PARAM_EXT,extension);
	}



	/** Gets phone-context parameter. */
	public String getPhoneContext()  {
		return getParameter(PARAM_CONTEXT);
	}  

	/** Whether phone-context parameter is present. */
	public boolean hasPhoneContext() {
		return hasParameter(PARAM_CONTEXT);
	}

	/** Adds  parameter. */
	public void addPhoneContext(String context)  {
		addParameter(PARAM_CONTEXT,context);
	}

}


