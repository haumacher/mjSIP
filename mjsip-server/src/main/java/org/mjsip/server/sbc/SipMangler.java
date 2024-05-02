/*
 * Copyright (C) 2005 Luca Veltri - University of Parma - Italy
 * 
 * This source code is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This source code is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this source code; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 * 
 * Author(s):
 * Luca Veltri (luca.veltri@unipr.it)
 */

package org.mjsip.server.sbc;



import java.util.Vector;

import org.mjsip.sdp.MediaDescriptor;
import org.mjsip.sdp.SdpMessage;
import org.mjsip.sdp.field.ConnectionField;
import org.mjsip.sdp.field.MediaField;
import org.mjsip.sip.address.GenericURI;
import org.mjsip.sip.address.NameAddress;
import org.mjsip.sip.address.SipURI;
import org.mjsip.sip.header.ContactHeader;
import org.mjsip.sip.header.RequestLine;
import org.mjsip.sip.message.SipMessage;



/** Class SipMangler collects static methods for mangling SIP messages.
  */
public class SipMangler {
	
	/** Escape char. */
	//protected static final char ESC='/'; 
	protected static final char ESC='Z'; 

	/** Escaped sequence for ESC. */
	protected static final String escaped_ESC="~"; 

	/** Start sequence for mangled URI (without the leading ESC). */
	protected static final String startof_URI="MjSBC2U-"; 

	/** Escaped sequence for '@' char (without the leading ESC). */
	protected static final String escaped_AT="AT-"; 

	/** Escaped sequence for ':' char (without the leading ESC). */
	protected static final String escaped_PORT="PORT-"; 

	/** Magic cookie that distinguishes mangled SIP URIs.
	  * <p>
	  * It is equal to the start sequence, formed as <i>ESC</i>+<i>startof_URI</i>. */
	public static final String magic_cookie=ESC+startof_URI; 


	/** Mangles request-uri */
	/*public static SipMessage mangleRequestLine(SipMessage msg, SipURI uri) {
		RequestLine rl=msg.getRequestLine();
		RequestLine new_rl=new RequestLine(rl.getMethod(),uri);
		msg.setRequestLine(new_rl);
		return msg;
	}*/


	/** Whether request-uri has been mangled. */
	public static boolean isRequestLineMangled(SipMessage msg) {
		GenericURI request_uri=msg.getRequestLine().getAddress();
		if (!request_uri.isSipURI()) return false;
		// else
		SipURI sip_uri=request_uri.toSipURI();
		String username=sip_uri.getUserName();
		return (username!=null && username.startsWith(magic_cookie));
	}


	/** Unmangles request-uri */
	public static SipMessage unmangleRequestLine(SipMessage msg) {
		RequestLine rl=msg.getRequestLine();
		GenericURI request_uri=msg.getRequestLine().getAddress();
		if (!request_uri.isSipURI()) return msg;
		// else
		SipURI sip_uri=request_uri.toSipURI();
		String username=sip_uri.getUserName();
		if (username!=null && username.startsWith(magic_cookie)) {
			sip_uri=unstuffUri(sip_uri);
			RequestLine new_rl=new RequestLine(rl.getMethod(),sip_uri);
			msg.setRequestLine(new_rl);
		}
		return msg;
	}


	/** Mangles Contact address with the new NameAddress. */
	/*public static SipMessage mangleContact(SipMessage msg, NameAddress naddress) {
		if (!msg.hasContactHeader()) return msg;
		//else
		ContactHeader ch=msg.getContactHeader();
		if (!ch.isStar()) {
			ContactHeader new_ch=new ContactHeader(naddress);
			if (ch.hasExpires()) new_ch.setExpires(ch.getExpires());
			msg.setContactHeader(new_ch);
		}
		return msg;
	}*/


	/** Mangles/unmangles Contact address in automatic and reversible manner. */
	/*public static SipMessage mangleContact(SipMessage msg, String host, int port) {
		if (!msg.hasContactHeader()) return msg;
		//else
		ContactHeader ch=msg.getContactHeader();
		if (!ch.isStar()) {
			NameAddress name_address=ch.getNameAddress();
			SipURI contact_uri=name_address.getAddress();
			if (contact_uri.getUserName().startsWith(magic_cookie)) contact_uri=unstuffedUri(contact_uri);
			else contact_uri=stuffedUri(contact_uri,host,port);
			ContactHeader new_ch=new ContactHeader(new NameAddress(name_address.getDisplayName(),contact_uri));
			if (ch.hasExpires()) new_ch.setExpires(ch.getExpires());
			msg.setContactHeader(new_ch);
		}
		return msg;
	}*/


	/** Mangles Contact address in automatic and reversible manner. */
	public static SipMessage mangleContact(SipMessage msg, String host, int port) {
		if (!msg.hasContactHeader()) return msg;
		//else
		ContactHeader ch=msg.getContactHeader();
		if (!ch.isStar()) {
			NameAddress name_address=ch.getNameAddress();
			GenericURI contact_uri=name_address.getAddress();
			if (contact_uri.isSipURI()) {
				SipURI sip_uri=contact_uri.toSipURI();
				// do not mangle already mangled URIs
				//String username=contact_uri.getUserName();
				/*if (username==null || !username.startsWith(magic_cookie))*/ {
					sip_uri=stuffUri(sip_uri,host,port);
					ContactHeader new_ch=new ContactHeader(new NameAddress(name_address.getDisplayName(),sip_uri));
					if (ch.hasExpires()) new_ch.setExpires(ch.getExpires());
					msg.setContactHeader(new_ch);
				}
			}
		}
		return msg;
	}


	/** Unmangles Contact address. */
	public static SipMessage unmangleContact(SipMessage msg) {
		if (!msg.hasContactHeader()) return msg;
		//else
		ContactHeader ch=msg.getContactHeader();
		if (!ch.isStar()) {
			NameAddress name_address=ch.getNameAddress();
			GenericURI contact_uri=name_address.getAddress();
			if (contact_uri.isSipURI()) {
				SipURI sip_uri=contact_uri.toSipURI();
				String username=sip_uri.getUserName();
				if (username!=null && username.startsWith(magic_cookie)) {
					sip_uri=unstuffUri(sip_uri);
					ContactHeader new_ch=new ContactHeader(new NameAddress(name_address.getDisplayName(),sip_uri));
					if (ch.hasExpires()) new_ch.setExpires(ch.getExpires());
					msg.setContactHeader(new_ch);
				}
			}
		}
		return msg;
	}


	/** Stuffes a String. */
	private static String stuffString(String str) {
		StringBuilder stuffed=new StringBuilder();
		for (int i=0; i<str.length(); ) {
			char c=str.charAt(i++);
			switch (c) {
				case ESC : stuffed.append(ESC).append(escaped_ESC); break;
				case '@' : stuffed.append(ESC).append(escaped_AT); break;
				case ':' : stuffed.append(ESC).append(escaped_PORT); break;
				default  : stuffed.append(c);
			}
		}
		return stuffed.toString();
	}


	/** Untuffes a String. */
	private static String unstuffString(String str) {
		StringBuilder unstuffed=new StringBuilder();
		for (int i=0; i<str.length(); ) {
			char c=str.charAt(i++);
			if (c==ESC) {
				if (str.startsWith(escaped_ESC,i)) {
					unstuffed.append(ESC);
					i+=escaped_ESC.length();
				}
				else
				if (str.startsWith(escaped_AT,i)) {
					unstuffed.append('@');
					i+=escaped_AT.length();
				}
				else
				if (str.startsWith(escaped_PORT,i)) {
					unstuffed.append(':');
					i+=escaped_PORT.length();
				}
			}
			else unstuffed.append(c);
		}
		return unstuffed.toString();
	}


	/** Stuffes a SipURI in automatic and reversible manner. */
	private static SipURI stuffUri(SipURI uri, String host, int port) {
		//String str=uri.toString().substring(4); // skip "sip:"
		String str=uri.getHost();
		if (uri.hasUserName()) str=uri.getUserName()+"@"+str;
		if (uri.hasPort()) str=str+":"+uri.getPort();
		String stuffed_str=stuffString(str);
		String username=magic_cookie+stuffed_str;
		return new SipURI(username,host,port);
	}


	/** Unstuffes a stuffed SipURI. */
	private static SipURI unstuffUri(SipURI uri) {
		String str=uri.getUserName();
		if (str!=null && str.startsWith(magic_cookie)) {
			String unstuffed_str=unstuffString(str.substring(magic_cookie.length()));
			uri=SipURI.parseSipURI(unstuffed_str);
		}
		return uri;
	}


	/** Mangles the Record-Route URI */
	/*public static SipMessage mangleRecordRoute(SipMessage msg, SipURI uri) {
		if (!msg.hasRecordRouteHeader()) return msg;
		//else
		MultipleHeader routes=msg.getRecordRoutes();
		routes.removeTop();
		uri.addLr();
		routes.addTop(new RecordRouteHeader(new NameAddress(uri)));
		msg.removeRecordRoutes();
		msg.addRecordRoutes(routes);
		return msg;
	}*/


	/** Mangles last Record-Route URI */
	/*public static SipMessage mangleLastRecordRoute(SipMessage msg, SipURI uri) {
		if (!msg.hasRecordRouteHeader()) return msg;
		//else
		MultipleHeader routes=msg.getRecordRoutes();
		routes.removeBottom();
		uri.addLr();
		routes.addBottom(new RecordRouteHeader(new NameAddress(uri)));
		msg.removeRecordRoutes();
		msg.addRecordRoutes(routes);
		return msg;
	}*/

	/** Mangles the Route URI */
	/*public static SipMessage mangleRoute(SipMessage msg, SipURI uri) {
		if (!msg.hasRouteHeader()) return msg;
		//else
		msg.removeRouteHeader();
		msg.addRouteHeader(new RouteHeader(new NameAddress(uri)));
		return msg;
	}*/


	/** Mangles the body */
	public static SipMessage mangleBody(SipMessage msg, String masq_addr, String[] mediaTypes, int[] masq_port) {
		//printLog("inside mangleBody()",LogWriter.LEVEL_MEDIUM);
		if (!msg.hasBody()) {
			return msg;
		}
	
		SdpMessage sdp=msg.getSdpBody();
		ConnectionField newConn = mapConnection(sdp.getConnection(), masq_addr);
		Vector<MediaDescriptor> newDescriptors = mapMediaDescriptors(sdp.getMediaDescriptors(), mediaTypes, masq_port);
		
		sdp=sdp.withConnection(newConn, newDescriptors);
		
		msg.setSdpBody(sdp);
		return msg;
	}

	private static ConnectionField mapConnection(ConnectionField conn, String masq_addr) {
		ConnectionField newConn=new ConnectionField(conn.getAddressType(),masq_addr,conn.getTTL(),conn.getNum());
		return newConn;
	}

	private static Vector<MediaDescriptor> mapMediaDescriptors(Vector<MediaDescriptor> descriptors,
			String[] mediaTypes, int[] masq_port) {
		Vector<MediaDescriptor> result = new Vector<>(); 
			  
		for (MediaDescriptor descriptor : descriptors) {
			MediaField mf = descriptor.getMediaField();
			
			MediaDescriptor newDescriptor;

			createDescriptor:
			{
				for (int i=0; i < mediaTypes.length; i++) {
					if (mf.getMediaType().equals(mediaTypes[i])) {
						// Masquerade the port
						MediaField newMedia = 
							new MediaField(mf.getMediaType(), masq_port[i], 0, mf.getTransport(), mf.getFormats());
						newDescriptor = new MediaDescriptor(newMedia, descriptor.getConnection(), descriptor.getAttributes());
						break createDescriptor;
					}
				}
				
				newDescriptor = descriptor;
			}

			result.addElement(newDescriptor);
		}
		return result;
	}

}
