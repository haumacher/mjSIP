/*
 * Copyright (C) 2007 Luca Veltri - University of Parma - Italy
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



/** Class <i>SipURI</i> implements SIP and SIPS URIs.
  * <P> A SIP URI is a string of the form of:
  * <BR><BLOCKQUOTE><PRE>&nbsp;&nbsp; sip:[user@]hostname[:port][;parameters] </PRE></BLOCKQUOTE>
  * <P> If <i>port</i> number is ommitted, -1 is returned
  */
public class SipURI extends GenericURI {
	

	/** Transport param name */
	public static final String PARAM_TRANSPORT="transport"; 
	
	/** Maddr param name */
	public static final String PARAM_MADDR="maddr"; 
	
	/** TTL param name */
	public static final String PARAM_TTL="ttl"; 



	/** Scheme of SIP URI */
	protected static final String SIP_COLON="sip:";

	/** Scheme of SIPS URI */
	protected static final String SIPS_COLON="sips:";



	/** Whether has SIPS scheme */
	protected boolean secure=false;



	/** Creates a new SipURI. */
	public SipURI(GenericURI uri) {
		super(uri);
		String scheme=getScheme();
		if (scheme.equals(SCHEME_SIPS)) secure=true;
		else
		if (!scheme.equals(SCHEME_SIP)) throw new UnexpectedUriSchemeException(scheme);
	}


	/** Creates a new SipURI.
	  * @param uri the [user@]hostname[:port][;parameters] or the complete SIP or SIPS URI including SIP or SIPS scheme. */
	public SipURI(String uri) {
		super(uri);
		if (uri.startsWith(SIPS_COLON)) secure=true;
		else
		if(!uri.startsWith(SIP_COLON)) this.uri=SIP_COLON+uri;
	}

	/** Creates a new SipURI. */
	public SipURI(String username, String hostname) {
		super((String)null);
		init(username,hostname,-1,false);
	}

	/** Creates a new SipURI. */
	public SipURI(String hostname, int portnumber) {
		super((String)null);
		init(null,hostname,portnumber,false);
	}

	/** Creates a new SipURI. */
	public SipURI(String username, String hostname, int portnumber) {
		super((String)null);
		init(username,hostname,portnumber,false);
	}


	/** Creates a new SipURI. */
	public SipURI(String username, String hostname, int portnumber, boolean secure) {
		super((String)null);
		init(username,hostname,portnumber,secure);
	}


	/** Inits the SipURI. */
	private void init(String username, String hostname, int portnumber, boolean secure) {
		StringBuffer sb=new StringBuffer((secure)? SIPS_COLON : SIP_COLON);
		if (username!=null) sb.append(username).append('@');
		sb.append(hostname);
		if (portnumber>0) sb.append(":"+portnumber);
		uri=sb.toString();
	}


	/** Gets user name of SipURI (Returns null if user name does not exist). */
	public String getUserName() {
		int begin=getScheme().length()+1; // skip "sip:"
		int end=uri.indexOf('@',begin);
		if (end<0) return null;
		else return uri.substring(begin,end);
	}

	/** Gets host of SipURI. */
	public String getHost() {
		char[] host_terminators={':',';','?'};
		Parser par=new Parser(uri);
		int begin=par.indexOf('@'); // skip "sip:user@"
		if (begin<0) begin=getScheme().length()+1; // skip "sip:"
			else begin++; // skip "@"
		par.setPos(begin);
		int end=par.indexOf(host_terminators);
		if (end<0) return uri.substring(begin);
			else return uri.substring(begin,end);
	}

	/** Gets port of SipURI; returns -1 if port is not specidfied. */
	public int getPort() {
		char[] port_terminators={';','?'};
		Parser par=new Parser(uri,getScheme().length()+1); // skip "sip:"
		int begin=par.indexOf(':');
		if (begin<0) return -1;
		else {
			begin++;
			par.setPos(begin);
			int end=par.indexOf(port_terminators);
			if (end<0) return Integer.parseInt(uri.substring(begin));
			else return Integer.parseInt(uri.substring(begin,end));
		}
	}

	/** Gets boolean value to indicate if SipURI has user name. */
	public boolean hasUserName() {
		return getUserName()!=null;
	}

	/** Gets boolean value to indicate if SipURI has port &gt;= 0. */
	public boolean hasPort() {
		return getPort()>=0;
	}

	/** Whether is SIPS. */
	public boolean isSecure() {
		return secure;
	}

	/** Sets scheme to SIPS. */
	public void setSecure(boolean secure)  {
		if (this.secure!=secure) {
			this.secure=secure;
			uri=SCHEME_SIPS+uri.substring(uri.indexOf(':'));
		}
	}


	/** Gets the value of transport parameter.
	  * @return null if no transport parameter is present. */
	public String getTransport()  {
		return getParameter(PARAM_TRANSPORT);
	}  

	/** Whether transport parameter is present. */
	public boolean hasTransport() {
		return hasParameter(PARAM_TRANSPORT);
	}

	/** Adds transport parameter. */
	public void addTransport(String proto)  {
		addParameter(PARAM_TRANSPORT,proto.toLowerCase());
	}

	/** Gets the value of maddr parameter.
	  * @return null if no maddr parameter is present. */
	public String getMaddr()  {
		return getParameter(PARAM_MADDR);
	}  

	/** Whether maddr parameter is present. */
	public boolean hasMaddr() {
		return hasParameter(PARAM_MADDR);
	}

	/** Adds maddr parameter. */
	public void addMaddr(String maddr)  {
		addParameter(PARAM_MADDR,maddr);
	}

	/** Gets the value of ttl parameter.
	  * @return 1 if no ttl parameter is present. */
	public int getTtl()  {
		try {  return Integer.parseInt(getParameter(PARAM_TTL));  } catch (Exception e) {  return 1;  }
	}  

	/** Whether ttl parameter is present. */
	public boolean hasTtl() {
		return hasParameter(PARAM_TTL);
	}

	/** Adds ttl parameter. */
	public void addTtl(int ttl)  {
		addParameter(PARAM_TTL,Integer.toString(ttl));
	}

}


