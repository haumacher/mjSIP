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




/** CoreSipHeaders simply collects all standard SIP header field names, as defined by RFC 3261.
  */
public abstract class CoreSipHeaders {
	
	/** String "Accept" */
	public static final String Accept="Accept";
	/** String "Accept-Encoding" */
	public static final String Accept_Encoding="Accept-Encoding";
	/** String "Accept-Language" */
	public static final String Accept_Language="Accept-Language";
	/** String "Alert-Info" */
	public static final String Alert_Info="Alert-Info";
	/** String "Allow" */
	public static final String Allow="Allow";
	/** String "Authentication-Info" */
	public static final String Authentication_Info="Authentication-Info";
	/** String "Authorization" */
	public static final String Authorization="Authorization";
	/** String "Call-ID" */
	public static final String Call_ID="Call-ID";
	/** String "i" */
	public static final String Call_ID_short="i";
	/** String "Contact" */
	public static final String Contact="Contact";
	/** String "m" */
	public static final String Contact_short="m";
	/** String "Content-Disposition" */
	public static final String Content_Disposition="Content-Disposition";  
	/** String "Content-Length" */
	public static final String Content_Length="Content-Length";  
	/** String "l" */
	public static final String Content_Length_short="l";  
	/** String "Content-Type" */
	public static final String Content_Type="Content-Type"; 
	/** String "c" */
	public static final String Content_Type_short="c"; 
	/** String "CSeq" */
	public static final String CSeq="CSeq";
	/** String "Date" */
	public static final String Date="Date";
	/** String "Expires" */
	public static final String Expires="Expires";
	/** String "From" */
	public static final String From="From";
	/** String "f" */
	public static final String From_short="f";
	/** String "User-Agent" */
	public static final String User_Agent="User-Agent";  
	/** String "Max-Forwards" */
	public static final String Max_Forwards="Max-Forwards";  
	/** String "Proxy-Authenticate" */
	public static final String Proxy_Authenticate="Proxy-Authenticate";   
	/** String "Proxy-Authorization" */
	public static final String Proxy_Authorization="Proxy-Authorization";   
	/** String "Proxy-Require" */
	public static final String Proxy_Require="Proxy-Require";   
	/** String "Record-Route" */
	public static final String Record_Route="Record-Route"; 
	/** String "Require" */
	public static final String Require="Require";   
	/** String "Route" */
	public static final String Route="Route";   
	/** String "Server" */
	public static final String Server="Server";  
	/** String "Subject" */
	public static final String Subject="Subject";  
	/** String "s" */
	public static final String Subject_short="s";  
	/** String "Supported" */
	public static final String Supported="Supported";   
	/** String "k" */
	public static final String Supported_short="k";   
	/** String "To" */
	public static final String To="To";
	/** String "t" */
	public static final String To_short="t";
	/** String "Unsupported" */
	public static final String Unsupported="Unsupported";   
	/** String "Via" */
	public static final String Via="Via";
	/** String "v" */
	public static final String Via_short="v";
	/** String "WWW-Authenticate" */
	public static final String WWW_Authenticate="WWW-Authenticate";

	/** Whether <i>s1</i> and <i>s2</i> are case-unsensitive-equal. */
	/*protected static boolean same(String s1, String s2) {
		return s1.equalsIgnoreCase(s2);
	}*/
  
}
