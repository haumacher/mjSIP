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

package org.mjsip.sip.message;



import java.util.Vector;

import org.mjsip.sip.header.ContentDispositionHeader;
import org.mjsip.sip.header.ContentLengthHeader;
import org.mjsip.sip.header.ContentTypeHeader;
import org.mjsip.sip.header.Header;
import org.mjsip.sip.header.MultipleHeader;
import org.mjsip.sip.header.RequestLine;
import org.mjsip.sip.header.SipHeaders;
import org.mjsip.sip.header.StatusLine;
import org.mjsip.sip.provider.ConnectionId;
import org.mjsip.sip.provider.SipParser;
import org.zoolu.util.ByteUtils;



/** BasicSipMessage is a standard SIP Message as defined by RFC 3261.
  * <p>
  * BasicSipMessage provides all basic methods for parsing any message header fields,
  * message body, message type, and message transport address. It does not provides
  * header-specific methods.
  * <br>
  * It implements one-time-parsing, that is it parses the entire message just one time
  * when it is created by a text String or byte array.
  */
public abstract class BasicSipMessage {
	

	/** Whether printing debugging information on standard error output. */
	public static boolean DEBUG=false;


	/** UDP */
	public static final String PROTO_UDP="udp"; 
	/** TCP */
	public static final String PROTO_TCP="tcp"; 
	/** TLS */
	public static final String PROTO_TLS="tls"; 
	/** SCTP */
	public static final String PROTO_SCTP="sctp"; 


	/** SIP version String (actually just "SIP/") */
	protected static final String SIP_VERSION="SIP/"; 

	/** Maximum receiving packet size */
	protected static int MAX_PKT_SIZE=8000; 
	
	/** The remote ip address */
	protected String remote_addr=null;

	/** The remote port */
	protected int remote_port=0;
	
	/** Transport protocol */
	protected String transport_proto=null;

	/** Connection identifier */
	protected ConnectionId connection_id=null;



	/** Request-line */
	protected RequestLine request_line=null;

	/** Status-line */
	protected StatusLine status_line=null;

	/** Vector of all header fields */
	protected Vector headers=new Vector();

	/** Message body */
	protected byte[] body=null;




	/** Costructs a new empty Message. */
	public BasicSipMessage() {
		//headers=new Vector();
	}

	/** Creates a new SIP request message.
	  * @param request_line the request-line
	  * @param headers vector of SIP header fields
	  * @param body the message body */
	public BasicSipMessage(RequestLine request_line, Vector headers, byte[] body) {
		this.request_line=request_line;
		this.headers=headers;
		this.body=body;
	}

	/** Creates a new SIP response message.
	  * @param status_line the response status-line
	  * @param headers vector of SIP header fields
	  * @param body the message body */
	public BasicSipMessage(StatusLine status_line, Vector headers, byte[] body) {
		this.status_line=status_line;
		this.headers=headers;
		this.body=body;
	}

	/** Costructs a new Message.
	  * @param buf the byte array containing the message 
	  * @param off the message offset within the byte array
	  * @param len the message len */
	public BasicSipMessage(byte[] buf, int off, int len) {
		try {
			setMessage(buf,off,len);
		}
		catch (Exception e) {
			if (DEBUG) {
				System.err.println("DEBUG: BasicSipMessage: BasicSipMessage(byte[],int,int): parser error");
				e.printStackTrace();
			}
		}
	}

	/** Costructs a new Message.
	  * @param str the string containing the message */ 
	public BasicSipMessage(String str) {
		try {
			setMessage(str);
		}
		catch (Exception e) {
			if (DEBUG) {
				System.err.println("DEBUG: BasicSipMessage: BasicSipMessage(String): parser error");
				e.printStackTrace();
			}
		}
	}

	/** Costructs a new Message.
	  * @param msg the message to be copied */ 
	public BasicSipMessage(BasicSipMessage msg) {
		setMessage(msg);
	}
	

	/** Sets the message as a copy of an other message.
	  * @param msg the message to be copied */ 
	protected void setMessage(BasicSipMessage msg) {
		remote_addr=msg.remote_addr;
		remote_port=msg.remote_port;
		transport_proto=msg.transport_proto;
		connection_id=msg.connection_id;
		request_line=msg.request_line;
		status_line=msg.status_line;
		//headers=new Vector();
		for (int i=0; i<msg.headers.size(); i++) headers.addElement(msg.headers.elementAt(i));
		body=msg.body;
	}
	
	/** Sets the message from a string representing the SIP message.
	  * The string must contain (and start with) a valid SIP message, otherwise a MalformedSipMessageException is thrown.
	  * Possible additional characters after the end of the SIP message are simply ignored.
	  * @param str the string containing the SIP message
	  * @return the number of used chars
	  * @exception MalformedSipMessageException in case the string does not contain (not start with) a valid SIP message */
	protected int setMessage(String str) throws MalformedSipMessageException {
		try {
			SipParser par=new SipParser(str);

			// skip any leading CRLF
			//par.skipCRLF();

			// parse first line
			String proto_version=str.substring(par.getPos(),SIP_VERSION.length());     
			if (proto_version.equalsIgnoreCase(SIP_VERSION)) status_line=par.getStatusLine();
			else request_line=par.getRequestLine();
			
			// parse all header fields
			//headers=new Vector();
			if (headers.size()>0) headers.removeAllElements();
			Header h=par.getHeader();
			while (h!=null) {
				headers.addElement(h);
				h=par.getHeader();
			}

			// get body
			ContentLengthHeader clh=getContentLengthHeader();
			if (clh!=null) {
				int body_len=clh.getContentLength();
				body=par.getString(body_len).getBytes();
			}
			else
			if (getContentTypeHeader()!=null) {
				String body_str=par.getRemainingString();
				body=(body_str.length()>0)? body_str.getBytes() : null;
			}
			
			return par.getPos();
		}
		catch (Exception e) {
			throw new MalformedSipMessageException(e.getMessage()); 
		}
	}


	/** Sets the message from an array of bytes containing the SIP message.
	  * The array of bytes must contain a valid SIP message, otherwise a MalformedSipMessageException is thrown.
	  * Possible additional bytes after the end of the SIP message are simply ignored.
	  * @param buf the byte array containing the SIP message 
	  * @param off the offset within the byte array
	  * @param len the number of available bytes
	  * @return the number of used bytes
	  * @exception MalformedSipMessageException in case the array of bytes does not contain (starting at the given offset with) a valid SIP message */
	protected int setMessage(byte[] buf, int off, int len) throws MalformedSipMessageException {
		try {
			// skip any leading CRLF
			/*int skip_len=0;
			while (len>0 && (buf[off]=='\r' || buf[off]=='\n')) {
				off++;
				len--;
				skip_len++;
			}*/
		
			// find total header length
			byte[] delim={(byte)'\r',(byte)'\n',(byte)'\r',(byte)'\n'};
			int siph_len=ByteUtils.indexOf(delim,buf,off,len);
			if (siph_len<0) {
				delim=new byte[]{(byte)'\n',(byte)'\n'};
				siph_len=ByteUtils.indexOf(delim,buf,off,len);
			}
			if (siph_len<0) throw new MalformedSipMessageException("No SIP header delimiter found.");
			// else
			siph_len+=delim.length;
			String siph_str=new String(buf,off,siph_len);
	
			// parse first line
			SipParser par=new SipParser(siph_str);
			String proto_version=siph_str.substring(0,SIP_VERSION.length());     
			if (proto_version.equalsIgnoreCase(SIP_VERSION)) status_line=par.getStatusLine();
			else request_line=par.getRequestLine();
	
			// parse all header fields
			//headers=new Vector();
			if (headers.size()>0) headers.removeAllElements();
			Header h=par.getHeader();
			while (h!=null) {
				headers.addElement(h);
				h=par.getHeader();
			}
	
			// get body
			int body_len=0;
			ContentLengthHeader clh=getContentLengthHeader();
			if (clh!=null) body_len=clh.getContentLength();
			else if (getContentTypeHeader()!=null) body_len=len-siph_len;
			body=(body_len>0)? ByteUtils.copy(buf,off+siph_len,body_len) : null;
			
			return /*skip_len+*/siph_len+body_len;
		}
		catch (Exception e) {
			throw new MalformedSipMessageException(e.getMessage()); 
		}
	}


	/** Creates and returns a clone of this object. */
	abstract public Object clone();
	//{  return new Message(message);
	//}


	/** Gets string representation of Message. */
	public String toString() {
		StringBuffer str=getMessageHeader();
		if (body!=null) str.append(new String(body));
		return str.toString();
	}

  
	/** Gets the array of bytes of this message.
	  * @return an array of bytes containing this message */
	public byte[] getBytes() {
		byte[] data=getMessageHeader().toString().getBytes();
		if (body!=null) {
			byte[] siph=data;
			data=new byte[siph.length+body.length];
			ByteUtils.copy(siph,data,0);
			ByteUtils.copy(body,data,siph.length);
		}
		return data;
	}


	/** Gets the message header. */
	private StringBuffer getMessageHeader() {
		StringBuffer sb=new StringBuffer();
		if (request_line!=null) sb.append(request_line.toString());
		else if (status_line!=null) sb.append(status_line.toString());
		for (int i=0; i<headers.size(); i++) sb.append(((Header)headers.elementAt(i)).toString());
		sb.append("\r\n");
		return sb;
	}


	/** Gets remote ip address. */
	public String getRemoteAddress() {
		return remote_addr;
	}

	/** Sets remote ip address. */
	public void setRemoteAddress(String addr) {
		remote_addr=addr;
	}

	/** Gets remote port. */
	public int getRemotePort() {
		return remote_port;
	}   

	/** Sets remote port. */
	public void setRemotePort(int port) {
		remote_port=port;
	}   

	/** Gets transport protocol. */
	public String getTransportProtocol() {
		return transport_proto;
	}

	/** Sets transport protocol. */
	public void setTransportProtocol(String proto) {
		transport_proto=proto;
	}

	/** Gets connection identifier. */
	public ConnectionId getConnectionId() {
		return connection_id;
	}

	/** Sets connection identifier. */
	public void setConnectionId(ConnectionId connection_id) {
		this.connection_id=connection_id;
	}

	/** Clears transport information (transport protocol, address, port, and connection). */
	public void clearTransport() {
		setTransportProtocol(null);
		setRemoteAddress(null);
		setRemotePort(-1);
		setConnectionId(null);
	}

	/** Gets message length. */
	public int getLength() {
		int len=getMessageHeader().length();
		if (body!=null) len+=body.length;
		return len;
	}


	//**************************** Requests ****************************/

	/** Whether Message is a Request. */
	public boolean isRequest() {
		if (request_line!=null) return true;
		else return false;
	}
	
	/** Whether Message is a <i>method</i> request. */
	public boolean isRequest(String method) {
		if (request_line!=null && request_line.getMethod().equalsIgnoreCase(method)) return true;
		else return false;
	}


	/** Whether Message has the request-line. */
	protected boolean hasRequestLine() {
		return request_line!=null;
	}

	/** Gets the RequestLine of the Message (Returns null if called for no request message). */
	public RequestLine getRequestLine() {
		return request_line;
	}

	/** Sets the RequestLine of the Message. */
	public void setRequestLine(RequestLine rl) {
		request_line=rl;
	}   
	
	/** Removes the RequestLine of the Message. */
	public void removeRequestLine() {
		request_line=null;
	} 


	/** Whether Message is a Method that creates a dialog. */
	public boolean createsDialog() {
		if (!isRequest()) return false;
		//else
		return SipMethods.isCreateDialogMethod(getRequestLine().getMethod());
	}


	//**************************** Responses ****************************/

	/** Whether Message is a Response. */
	public boolean isResponse() throws NullPointerException {
		if (status_line!=null) return true;
		else return false;
	}
	
	/** Whether Message has status-line. */
	protected boolean hasStatusLine() {
		return status_line!=null;
	}

	/** Gets the StautsLine of the Message (Returns null if called for no response message). */
	public StatusLine getStatusLine() {
		return status_line;
	}

	/** Sets the StatusLine of the Message. */
	public void setStatusLine(StatusLine sl) {
		status_line=sl;
	}      
	
	/** Removes the StatusLine of the Message. */
	public void removeStatusLine() {
		status_line=null;
	} 


	//**************************** Generic Headers ****************************/

	/** Gets the first line of the Message. */
	public String getFirstLine() {
		if (isRequest()) return getRequestLine().toString();
		if (isResponse()) return getStatusLine().toString();
		return null;
	}  


	/** Removes Request\Status Line of the Message. */
	protected void removeFirstLine() {
		removeRequestLine();
		removeStatusLine();
	}
	  
	/** Gets the position of header <i>hname</i>.. */
	protected int indexOfHeader(String hname)  {
		for (int i=0; i<headers.size(); i++) {
			Header hi=(Header)headers.elementAt(i);
			if (hname.equalsIgnoreCase(hi.getName())) return i;
		}
		return -1;
	}

	/** Whether Message has any headers of specified name. */   
	public boolean hasHeader(String name) {
		Header hd=getHeader(name);
		if (hd==null) return false;
		else return true;
	}
	
	/** Gets the first Header of specified name (Returns null if no Header is found). */
	public Header getHeader(String hname) {
		int i=indexOfHeader(hname);
		if (i<0) return null;
		else return (Header)headers.elementAt(i);
	}

	/** Gets a Vector of all Headers of specified name (Returns empty Vector if no Header is found). */
	public Vector getHeaders(String hname) {
		Vector v=new Vector();
		for (int i=0; i<headers.size(); i++) {
			Header hi=(Header)headers.elementAt(i);
			if (hname.equalsIgnoreCase(hi.getName())) v.addElement(hi);
		}
		return v; 
	}

	/** Gets a Vector with all Headers. */
	public Vector getHeaders() {
		Vector v=new Vector();
		for (int i=0; i<headers.size(); i++) v.addElement(headers.elementAt(i));
		return v;
	}

	/** Adds Header at the top/bottom.
	  * The bottom is considered before the Content-Length and Content-Type headers. */
	public void addHeader(Header header, boolean top)  {
		int pos=0;
		if (!top) {
			pos=headers.size();
			// if Content_Length is present, jump before
			int cl=indexOfHeader(SipHeaders.Content_Length);
			if (cl>=0 && cl<pos) pos=cl;
			// if Content_Type is present, jump before
			int ct=indexOfHeader(SipHeaders.Content_Type);
			if (ct>=0 && ct<pos) pos=ct;
		}
		headers.insertElementAt(header,pos);
	}
	
	/** Adds a Vector of Headers at the top/bottom. */
	public void addHeaders(Vector headers, boolean top)  {
		int pos=0;
		if (!top) {
			pos=headers.size();
			// if Content_Length is present, jump before
			int cl=indexOfHeader(SipHeaders.Content_Length);
			if (cl>=0 && cl<pos) pos=cl;
			// if Content_Type is present, jump before
			int ct=indexOfHeader(SipHeaders.Content_Type);
			if (ct>=0 && ct<pos) pos=ct;
		}
		for (int i=0; i<headers.size(); i++) this.headers.insertElementAt(headers.elementAt(i),pos+i);
	}

	/** Adds MultipleHeader(s) <i>mheader</i> at the top/bottom. */
	public void addHeaders(MultipleHeader mheader, boolean top)  {
		if (mheader.isCommaSeparated()) addHeader(mheader.toHeader(),top); 
		else addHeaders(mheader.getHeaders(),top);
	}

	/** Adds Header before the first header <i>refer_hname</i>
	  * . <p>If there is no header of such type, it is added at top. */
	public void addHeaderBefore(Header new_header, String refer_hname)  {
		int i=indexOfHeader(refer_hname);
		if (i<0) i=0;
		headers.insertElementAt(new_header,i);
	}

	/** Adds MultipleHeader(s) before the first header <i>refer_hname</i>
	  * . <p>If there is no header of such type, they are added at top. */
	public void addHeadersBefore(MultipleHeader mheader, String refer_hname)  {
		if (mheader.isCommaSeparated()) addHeaderBefore(mheader.toHeader(),refer_hname); 
		else {
			int index=indexOfHeader(refer_hname);
			if (index<0) index=0;
			Vector hs=mheader.getHeaders();
			for (int k=0; k<hs.size(); k++) headers.insertElementAt(hs.elementAt(k),index+k);
		}
	}

	/** Adds Header after the first header <i>refer_hname</i>
	  * . <p>If there is no header of such type, it is added at bottom. */
	public void addHeaderAfter(Header new_header, String refer_hname)  {
		int i=indexOfHeader(refer_hname);
		if (i>=0) i++; else i=headers.size();
		headers.insertElementAt(new_header,i);
	}

	/** Adds MultipleHeader(s) after the first header <i>refer_hname</i>
	  * . <p>If there is no header of such type, they are added at bottom. */
	public void addHeadersAfter(MultipleHeader mheader, String refer_hname)  {
		if (mheader.isCommaSeparated()) addHeaderAfter(mheader.toHeader(),refer_hname); 
		else {
			int index=indexOfHeader(refer_hname);
			if (index>=0) index++; else index=headers.size();
			Vector hs=mheader.getHeaders();
			for (int k=0; k<hs.size(); k++) headers.insertElementAt(hs.elementAt(k),index+k);
		}
	}

	/** Removes first Header of specified name. */
	public void removeHeader(String hname) {
		removeHeader(hname,true);
	}

	/** Removes first (or last) Header of specified name.. */
	public void removeHeader(String hname, boolean first) {
		int index=-1;
		for (int i=0 ; i<headers.size(); i++) {
			Header hi=(Header)headers.elementAt(i);
			if (hname.equalsIgnoreCase(hi.getName())) {
				index=i;
				if (first) i=headers.size();
			}
		}
		if (index>=0) headers.removeElementAt(index);
	}
	
	/** Removes all Headers of specified name. */
	public void removeAllHeaders(String hname)  {
		for (int i=0 ; i<headers.size(); i++) {
			Header hi=(Header)headers.elementAt(i);
			if (hname.equalsIgnoreCase(hi.getName())) {
				headers.removeElementAt(i);
				i--;
			}
		}
	}
	
	/** Sets the Header <i>hd</i> removing any previous headers of the same type.. */
	public void setHeader(Header hd)  {
		boolean not_found=true;
		String hname=hd.getName();
		for (int i=0 ; i<headers.size(); i++) {
			Header hi=(Header)headers.elementAt(i);
			if (hname.equalsIgnoreCase(hi.getName())) {
				if (not_found) {
					// replace it
					headers.setElementAt(hd,i);
					not_found=false;
				}
				else  {
					// remove it
					headers.removeElementAt(i);
					i--;
				}
			}
		}
		if (not_found) addHeader(hd,false);
	}          

	/** Sets MultipleHeader <i>mheader</i>. */
	public void setHeaders(MultipleHeader mheader)  {
		if (mheader.isCommaSeparated()) setHeader(mheader.toHeader()); 
		else {
			boolean not_found=true;
			String hname=mheader.getName();
			for (int i=0 ; i<headers.size(); i++) {
				Header hi=(Header)headers.elementAt(i);
				if (hname.equalsIgnoreCase(hi.getName())) {
					if (not_found) {
						// replace it
						Vector hs=mheader.getHeaders();
						for (int k=0; k<hs.size(); k++) headers.insertElementAt(hs.elementAt(k),i+k);
						not_found=false;
						i+=hs.size()-1;
					}
					else  {
						// remove it
						headers.removeElementAt(i);
						i--;
					}
				}
			}
			if (not_found) addHeaders(mheader,false);
		}
	}


	//********************************** Body **********************************/
  
	/** Whether Message has Body. */   
	public boolean hasBody() {
		return body!=null;
	}
	/** Gets body(content) type. */
	public String getBodyType() {
		return getContentTypeHeader().getContentType();
	}
	/** Gets body(content) disposition. */
	public String getBodyDisposition() {
		return getContentDispositionHeader().getDisposition();
	} 
	/** Sets the message body.
	  * @param body the message body */
	public void setBody(byte[] body)  {
		setBody(null,null,body);
	}
	/** Sets the message body, specifing also the content-type.
	  * @param content_type the content-type
	  * @param body the message body */
	public void setBody(String content_type, byte[] body)  {
		setBody(content_type,null,body);
	}
	/** Sets the message body, specifing also the content-type and content-disposition.
	  * @param content_type the content-type
	  * @param content_disposition the content-disposition
	  * @param body the message body */
	public void setBody(String content_type, String content_disposition, byte[] body)  {
		removeBody();
		if (body!=null && body.length>0) {
			if (content_type!=null) setContentTypeHeader(new ContentTypeHeader(content_type));
			if (content_disposition!=null) setContentDispositionHeader(new ContentDispositionHeader(content_disposition));
			setContentLengthHeader(new ContentLengthHeader(body.length));
			this.body=body;
		}
		else {
			setContentLengthHeader(new ContentLengthHeader(0));
			this.body=null;
		}
	}
	/** Gets message body. The end of body is evaluated
	  * from the Content-Length header if present (RFC3261 compliant),
	  * or from the end of message if no Content-Length header is present (non-RFC3261 compliant). */
	public byte[] getBody() {
		return body;
	}  
	/** Removes the message body (if it exists), the body related methods, and the final empty line. */
	public void removeBody()  {
		removeContentTypeHeader();
		removeContentDispositionHeader();
		removeContentLengthHeader();
		this.body=null;
	}
	/** Sets sdp body. */
	public void setSdpBody(String body)  {
		if (body!=null) setBody("application/sdp",body.getBytes());
		else setBody(null,null);
	}            
	/** Gets text body. The end of body is evaluated
	  * from the Content-Length header if present (RFC3261 compliant),
	  * or from the end of message if no Content-Length header is present (non-RFC3261 compliant). */
	public String getStringBody() {
		if (body!=null) return new String(body);
		else return null;
	}  


	//**************************** Specific Headers ****************************/
  
	/** Whether Message has ContentDispositionHeader. */
	public boolean hasContentDispositionHeader() {
		return hasHeader(SipHeaders.Content_Disposition);
	}
	/** Sets ContentDispositionHeader of Message. */
	public void setContentDispositionHeader(ContentDispositionHeader sh)  {
		setHeader(sh);
	} 
	/** Gets ContentDispositionHeader of Message. */
	public ContentDispositionHeader getContentDispositionHeader() {
		Header h=getHeader(SipHeaders.Content_Disposition);
		if (h==null) return null;
		else return new ContentDispositionHeader(h);
	} 
	/** Removes ContentDispositionHeader from Message. */
	public void removeContentDispositionHeader()  {
		removeHeader(SipHeaders.Content_Disposition);
	}


	/** Whether Message has ContentTypeHeader. */   
	public boolean hasContentTypeHeader() {
		return hasHeader(SipHeaders.Content_Type);
	}   
	/** Gets ContentTypeHeader of Message. */
	public ContentTypeHeader getContentTypeHeader() {
		Header h=getHeader(SipHeaders.Content_Type);
		if (h==null) return null;
		else return new ContentTypeHeader(h);
	} 
	/** Sets ContentTypeHeader of Message. */
	protected void setContentTypeHeader(ContentTypeHeader cth)  {
		setHeader(cth);
	}    
	/** Removes ContentTypeHeader from Message (if it exists). */
	protected void removeContentTypeHeader()  {
		removeHeader(SipHeaders.Content_Type);
	}
 
	
	/** Whether Message has ContentLengthHeader. */   
	public boolean hasContentLengthHeader() {
		return hasHeader(SipHeaders.Content_Length);
	}  
	/** Gets ContentLengthHeader of Message. */
	public ContentLengthHeader getContentLengthHeader() {
		Header h=getHeader(SipHeaders.Content_Length);
		if (h==null) return null;
		else return new ContentLengthHeader(h);
	} 
	/** Sets ContentLengthHeader of Message. */
	protected void setContentLengthHeader(ContentLengthHeader clh)  {
		setHeader(clh);
	}    
	/** Removes ContentLengthHeader from Message (if it exists). */
	protected void removeContentLengthHeader()  {
		removeHeader(SipHeaders.Content_Length);
	}   

}
