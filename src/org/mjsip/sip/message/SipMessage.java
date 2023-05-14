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

import org.mjsip.sip.address.GenericURI;
import org.mjsip.sip.address.NameAddress;
import org.mjsip.sip.header.AcceptEncodingHeader;
import org.mjsip.sip.header.AcceptHeader;
import org.mjsip.sip.header.AcceptLanguageHeader;
import org.mjsip.sip.header.AlertInfoHeader;
import org.mjsip.sip.header.AllowEventsHeader;
import org.mjsip.sip.header.AllowHeader;
import org.mjsip.sip.header.AuthenticationInfoHeader;
import org.mjsip.sip.header.AuthorizationHeader;
import org.mjsip.sip.header.CSeqHeader;
import org.mjsip.sip.header.CallIdHeader;
import org.mjsip.sip.header.ContactHeader;
import org.mjsip.sip.header.DateHeader;
import org.mjsip.sip.header.EventHeader;
import org.mjsip.sip.header.ExpiresHeader;
import org.mjsip.sip.header.FromHeader;
import org.mjsip.sip.header.Header;
import org.mjsip.sip.header.InfoPackageHeader;
import org.mjsip.sip.header.MaxForwardsHeader;
import org.mjsip.sip.header.MinSEHeader;
import org.mjsip.sip.header.MultipleHeader;
import org.mjsip.sip.header.ProxyAuthenticateHeader;
import org.mjsip.sip.header.ProxyAuthorizationHeader;
import org.mjsip.sip.header.ProxyRequireHeader;
import org.mjsip.sip.header.RAckHeader;
import org.mjsip.sip.header.RSeqHeader;
import org.mjsip.sip.header.RecordRouteHeader;
import org.mjsip.sip.header.RecvInfoHeader;
import org.mjsip.sip.header.ReferToHeader;
import org.mjsip.sip.header.ReferredByHeader;
import org.mjsip.sip.header.ReplacesHeader;
import org.mjsip.sip.header.RequestLine;
import org.mjsip.sip.header.RequireHeader;
import org.mjsip.sip.header.RouteHeader;
import org.mjsip.sip.header.ServerHeader;
import org.mjsip.sip.header.SessionExpiresHeader;
import org.mjsip.sip.header.SipHeaders;
import org.mjsip.sip.header.StatusLine;
import org.mjsip.sip.header.SubjectHeader;
import org.mjsip.sip.header.SubscriptionStateHeader;
import org.mjsip.sip.header.SupportedHeader;
import org.mjsip.sip.header.ToHeader;
import org.mjsip.sip.header.UnsupportedHeader;
import org.mjsip.sip.header.UserAgentHeader;
import org.mjsip.sip.header.ViaHeader;
import org.mjsip.sip.header.WwwAuthenticateHeader;



/** Class SipMessage extends class BasicSipMessage providing
  * methods for handling and parsing specific methods and header fields.
  * <p>
  * It supports all methods and header fields definened in RFC 3261, plus:
  * <ul>
  * <li> method MESSAGE (RFC3428) </li>
  * <li> method REFER (RFC3515) </li>
  * <li> header Refer-To </li>
  * <li> header Referred-By </li>
  * <li> header Event </li>
  * </ul>
  */
public class SipMessage extends BasicSipMessage {
	
	/** Creates a new empty Message */
	public SipMessage() { super(); }

	/** Creates a new SIP request message.
	  * @param request_line the request-line
	  * @param headers vector of SIP header fields
	  * @param body the message body */
	public SipMessage(RequestLine request_line, Vector headers, byte[] body) {
		super(request_line,headers,body);
	}

	/** Creates a new SIP response message.
	  * @param status_line the response status-line
	  * @param headers vector of SIP header fields
	  * @param body the message body */
	public SipMessage(StatusLine status_line, Vector headers, byte[] body) {
		super(status_line,headers,body);
	}

	/** Creates a new Message */
	public SipMessage(String str) {
		super(str);
	}

	/** Creates a new Message */
	public SipMessage(byte[] buff, int offset, int len) {
		super(buff,offset,len);
	}

	/** Creates a new Message */
	public SipMessage(SipMessage msg) {
		super(msg);
	}
	
	/** Creates and returns a clone of the Message */
	public Object clone() {
		return new SipMessage(this);
	}


	//*************************** Basic (RFC 3261) ****************************

	/** Whether Message is an Invite. */
	public boolean isInvite() {
		return isRequest(SipMethods.INVITE);
	}

	/** Whether Message is an Ack. */
	public boolean isAck() {
		return isRequest(SipMethods.ACK);
	}

	/** Whether Message is an Options. */
	public boolean isOptions() {
		return isRequest(SipMethods.OPTIONS);
	}

	/** Whether Message is a Bye. */
	public boolean isBye() {
		return isRequest(SipMethods.BYE);
	}
	
	/** Whether Message is a Cancel. */
	public boolean isCancel() {
		return isRequest(SipMethods.CANCEL);
	}

	/** Whether Message is a Register. */
	public boolean isRegister() {
		return isRequest(SipMethods.REGISTER);
	}


	/** Returns the transaction method. */
	public String getTransactionMethod() {
		return getCSeqHeader().getMethod();
	} 
 

	/** Whether Message has MaxForwardsHeader. */
	public boolean hasMaxForwardsHeader() {
		return hasHeader(SipHeaders.Max_Forwards);
	}  
	/** Gets MaxForwardsHeader of Message. */
	public MaxForwardsHeader getMaxForwardsHeader() {
		Header h=getHeader(SipHeaders.Max_Forwards);
		if (h==null) return null;
		else return new MaxForwardsHeader(h);
	} 
	/** Sets MaxForwardsHeader of Message. */
	public void setMaxForwardsHeader(MaxForwardsHeader mfh) {
		setHeader(mfh);
	}  
	/** Removes MaxForwardsHeader from Message. */
	public void removeMaxForwardsHeader()  {
		removeHeader(SipHeaders.Max_Forwards);
	}

	  
	/** Whether Message has FromHeader. */
	public boolean hasFromHeader() {
		return hasHeader(SipHeaders.From);
	}  
	/** Gets FromHeader of Message. */
	public FromHeader getFromHeader() {
		Header h=getHeader(SipHeaders.From);
		if (h==null) return null;
		else return new FromHeader(h);
	} 
	/** Sets FromHeader of Message. */
	public void setFromHeader(FromHeader fh)  {
		setHeader(fh);
	}  
	/** Removes FromHeader from Message. */
	public void removeFromHeader()  {
		removeHeader(SipHeaders.From);
	}

	/** Whether Message has ToHeader. */
	public boolean hasToHeader() {
		return hasHeader(SipHeaders.To);
	} 
	/** Gets ToHeader of Message. */
	public ToHeader getToHeader() {
		Header h=getHeader(SipHeaders.To);
		if (h==null) return null;
		else return new ToHeader(h);
	} 
	/** Sets ToHeader of Message. */
	public void setToHeader(ToHeader th)  {
		setHeader(th);
	} 
	/** Removes ToHeader from Message. */
	public void removeToHeader()  {
		removeHeader(SipHeaders.To);
	}

	
	/** Whether Message has Contact header field.
	  * @return true if it has Contact header field (false otherwise) */
	public boolean hasContactHeader() {
		return hasHeader(SipHeaders.Contact);
	}
	/** <b>Deprecated</b>. Gets ContactHeader of Message. Use getContacts instead.
	  * @return the top Contact header field */  
	public ContactHeader getContactHeader() {
		//Header h=getHeader(SipHeaders.Contact);
		//if (h==null) return null; else return new ContactHeader(h);
		MultipleHeader mh=getContacts();
		if (mh==null) return null; return new ContactHeader(mh.getTop());
	} 
	/** Gets a MultipleHeader of Contacts.
	  * @return all contacts (MultipleHeader of <code>ContactHeader</code>) */
	public MultipleHeader getContacts() {
		Vector v=getHeaders(SipHeaders.Contact);
		if (v.size()>0) return new MultipleHeader(v);
		else return null;
	}   
	/** Adds ContactHeader.
	  * @param ch the Contact header field */
	public void addContactHeader(ContactHeader ch)  {
		addHeader(ch,false);
	}   
	/** Adds Contacts.
	  * @param contacts the Contact header fields to be added
	  * @param top whether they will be added at top (or bottom) */
	public void addContacts(MultipleHeader contacts, boolean top)  {
		addHeaders(contacts,top);
	}   
	/** Sets ContactHeader. */
	public void setContactHeader(ContactHeader ch)  {
		if (hasContactHeader()) removeContacts();
		addHeader(ch,false);
	}
	/** Sets Contacts.
	  * @param contacts contacts (MultipleHeader of <code>Contact</code>) */
	public void setContacts(MultipleHeader contacts)  {
		if (hasContactHeader()) removeContacts();
		addContacts(contacts,false);
	}    
	/** Removes ContactHeaders from Message. */
	public void removeContacts()  {
		removeAllHeaders(SipHeaders.Contact);
	}
 
	
	/** Whether Message has Via header field.
	  * @return true if it has Via header field (false otherwise) */
	public boolean hasViaHeader() {
		return hasHeader(SipHeaders.Via);
	}      
	/** Gets the top ViaHeader.
	  * @return the top Via header field */  
	public ViaHeader getViaHeader() {
		//Header h=getHeader(SipHeaders.Via);
		//if (h==null) return null; else return new ViaHeader(h);
		MultipleHeader mh=getVias();
		if (mh==null) return null; return new ViaHeader(mh.getTop());
	} 
	/** Gets all Via header fields.
	  * @return all Via header fields (MultipleHeader of <code>ViaHeader</code>) */
	public MultipleHeader getVias() {
		Vector v=getHeaders(SipHeaders.Via);
		if (v.size()>0) return new MultipleHeader(v);
		else return null;
	}
	/** Adds a ViaHeader at the top.
	  * @param vh the Via header field */
	public void addViaHeader(ViaHeader vh)  {
		addHeader(vh,true);
	} 
	/** Adds some Via header fields.
	  * @param vias the Via header fields to be added
	  * @param top whether they will be added at top (or bottom) */
	public void addVias(MultipleHeader vias, boolean top)  {
		addHeaders(vias,top);
	}   
	/** Sets Via header fields.
	  * @param vias Via header fields (MultipleHeader of <code>Via</code>) */
	public void setVias(MultipleHeader vias)  {
		if (hasViaHeader()) removeVias();
		addContacts(vias,true);
	}    
	/** Removes the top ViaHeader. */
	public void removeViaHeader()  {
		//removeHeader(SipHeaders.Via);
		MultipleHeader mh=getVias();
		mh.removeTop();
		setVias(mh);
	}  
	/** Removes all Via header fields from Message (if any). */
	public void removeVias()  {
		removeAllHeaders(SipHeaders.Via);
	}
	
		
	/** Whether Message has Route header field.
	  * @return true if it has Route header field (false otherwise) */
	public boolean hasRouteHeader() {
		return hasHeader(SipHeaders.Route);
	}      
	/** Gets the top RouteHeader.
	  * @return the top Route header field */  
	public RouteHeader getRouteHeader() {
		//Header h=getHeader(SipHeaders.Route);
		//if (h==null) return null; else return new RouteHeader(h);
		MultipleHeader mh=getRoutes();
		if (mh==null) return null; return new RouteHeader(mh.getTop());
	} 
	/** Gets all Route header fields.
	  * @return all routes (MultipleHeader of <code>RouteHeader</code>) */
	public MultipleHeader getRoutes() {
		Vector v=getHeaders(SipHeaders.Route);
		if (v.size()>0) return new MultipleHeader(v);
		else return null;
	}
	/** Adds a RouteHeader at the top.
	  * @param rh the Route header field */
	public void addRouteHeader(RouteHeader rh)  {
		addHeaderAfter(rh,SipHeaders.Via);
	} 
	/** Adds some Route header fields at the top.
	  * @param routes the Route header fields to be added */
	public void addRoutes(MultipleHeader routes)  {
		addHeadersAfter(routes,SipHeaders.Via);
	} 
	/** Sets all Route header fields.
	  * @param routes routes (MultipleHeader of <code>RouteHeader</code>) */
	public void setRoutes(MultipleHeader routes)  {
		if (hasRouteHeader()) removeRoutes();
		addRoutes(routes);
	}    
	/** Removes the top RouteHeader. */
	public void removeRouteHeader()  {
		//removeHeader(SipHeaders.Route);
		MultipleHeader mh=getRoutes();
		mh.removeTop();
		setRoutes(mh);
	}  
	/** Removes all Route header fields (if any). */
	public void removeRoutes()  {
		removeAllHeaders(SipHeaders.Route);
	}
	

	/** Whether Message has Record-Route header field.
	  * @return true if it has Record-Route header field (false otherwise) */
	public boolean hasRecordRouteHeader() {
		return hasHeader(SipHeaders.Record_Route);
	}      
	/** Gets the top RecordRouteHeader.
	  * @return the top Record-Route header field */  
	public RecordRouteHeader getRecordRouteHeader() {
		//Header h=getHeader(SipHeaders.Record_Route);
		//if (h==null) return null; else return new RecordRouteHeader(h);
		MultipleHeader mh=getRecordRoutes();
		if (mh==null) return null; return new RecordRouteHeader(mh.getTop());
	} 
	/** Gets all Record-Route header fields.
	  * @return all routes (MultipleHeader of <code>RecordRouteHeader</code>) */
	public MultipleHeader getRecordRoutes() {
		Vector v=getHeaders(SipHeaders.Record_Route);
		if (v.size()>0) return new MultipleHeader(v);
		else return null;
	}
	/** Adds a RecordRouteHeader at the top.
	  * @param rrh the Record-Route header field */
	public void addRecordRouteHeader(RecordRouteHeader rrh)  {
		//addHeaderAfter(rrh,SipHeaders.Via);
		addHeaderAfter(rrh,SipHeaders.CSeq);
	} 
	/** Adds some Record-Route header fields at the top.
	  * @param routes the Record-Route header fields to be added */
	public void addRecordRoutes(MultipleHeader routes)  {
		//addHeadersAfter(routes,SipHeaders.Via);
		addHeadersAfter(routes,SipHeaders.CSeq);
	} 
	/** Sets Record-Route header fields.
	  * @param routes routes (MultipleHeader of <code>RecordRouteHeader</code>) */
	public void setRecordRoutes(MultipleHeader routes)  {
		if (hasRecordRouteHeader()) removeRecordRoutes();
		addRecordRoutes(routes);
	}
	/** Removes the top RecordRouteHeader. */
	public void removeRecordRouteHeader()  {
		//removeHeader(SipHeaders.Record_Route);
		MultipleHeader mh=getRecordRoutes();
		mh.removeTop();
		setRecordRoutes(mh);
	}  
	/** Removes all Record-Route header fields (if any). */
	public void removeRecordRoutes()  {
		removeAllHeaders(SipHeaders.Record_Route);
	}


	/** Whether Message has Service-Route header field.
	  * @return true if it has Service-Route header field (false otherwise) */
	public boolean hasServiceRouteHeader() {
		return hasHeader(SipHeaders.ServiceRoute);
	}      
	/** Gets all Service-Route header fields.
	  * @return all service routes (MultipleHeader of <code>ServiceRouteHeader</code>) */
	public MultipleHeader getServiceRoutes() {
		Vector v=getHeaders(SipHeaders.ServiceRoute);
		if (v.size()>0) return new MultipleHeader(v);
		else return null;
	}
	/** Sets Service-Route header fields.
	  * @param service_routes service routes (MultipleHeader of <code>ServiceRouteHeader</code>) */
	public void setServiceRoutes(MultipleHeader service_routes)  {
		if (hasServiceRouteHeader()) removeServiceRoutes();
		addHeadersAfter(service_routes,SipHeaders.Via);
	} 
	/** Removes all Service-Route header fields (if any). */
	public void removeServiceRoutes()  {
		removeAllHeaders(SipHeaders.ServiceRoute);
	}


	/** Whether Message has CSeqHeader. */
	public boolean hasCSeqHeader() {
		return hasHeader(SipHeaders.CSeq);
	}  
	/** Gets CSeqHeader of Message. */
	public CSeqHeader getCSeqHeader() {
		Header h=getHeader(SipHeaders.CSeq);
		if (h==null) return null;
		else return new CSeqHeader(h);
	} 
	/** Sets CSeqHeader of Message. */
	public void setCSeqHeader(CSeqHeader csh)  {
		setHeader(csh);
	} 
	/** Removes CSeqHeader from Message. */
	public void removeCSeqHeader()  {
		removeHeader(SipHeaders.CSeq);
	}
	
		
	/** Whether has CallIdHeader. */
	public boolean hasCallIdHeader() {
		return hasHeader(SipHeaders.Call_ID);
	}
	/** Sets CallIdHeader of Message. */
	public void setCallIdHeader(CallIdHeader cih)  {
		setHeader(cih);
	} 
	/** Gets CallIdHeader of Message. */
	public CallIdHeader getCallIdHeader() {
		Header h=getHeader(SipHeaders.Call_ID);
		if (h==null) return null;
		else return new CallIdHeader(h);
	} 
	/** Removes CallIdHeader from Message. */
	public void removeCallIdHeader()  {
		removeHeader(SipHeaders.Call_ID);
	}


	/** Whether Message has SubjectHeader. */
	public boolean hasSubjectHeader() {
		return hasHeader(SipHeaders.Subject);
	}
	/** Sets SubjectHeader of Message. */
	public void setSubjectHeader(SubjectHeader sh)  {
		setHeader(sh);
	} 
	/** Gets SubjectHeader of Message. */
	public SubjectHeader getSubjectHeader() {
		Header h=getHeader(SipHeaders.Subject);
		if (h==null) return null;
		else return new SubjectHeader(h);
	} 
	/** Removes SubjectHeader from Message. */
	public void removeSubjectHeader()  {
		removeHeader(SipHeaders.Subject);
	}

	
	/** Whether Message has DateHeader. */   
	public boolean hasDateHeader() {
		return hasHeader(SipHeaders.Date);
	}  
	/** Gets DateHeader of Message. */
	public DateHeader getDateHeader() {
		Header h=getHeader(SipHeaders.Date);
		if (h==null) return null;
		else return new DateHeader(h);
	} 
	/** Sets DateHeader of Message. */
	public void setDateHeader(DateHeader dh)  {
		setHeader(dh);
	}  
	/** Removes DateHeader from Message (if it exists). */
	public void removeDateHeader()  {
		removeHeader(SipHeaders.Date);
	}

	
	/** Whether has UserAgentHeader. */
	public boolean hasUserAgentHeader() {
		return hasHeader(SipHeaders.User_Agent);
	}
	/** Sets UserAgentHeader. */
	public void setUserAgentHeader(UserAgentHeader h)  {
		setHeader(h);
	} 
	/** Gets UserAgentHeader. */
	public UserAgentHeader getUserAgentHeader() {
		Header h=getHeader(SipHeaders.User_Agent);
		if (h==null) return null;
		else return new UserAgentHeader(h);
	} 
	/** Removes UserAgentHeader. */
	public void removeUserAgentHeader()  {
		removeHeader(SipHeaders.User_Agent);
	}


	/** Whether has ServerHeader. */
	public boolean hasServerHeader() {
		return hasHeader(SipHeaders.Server);
	}
	/** Sets ServerHeader. */
	public void setServerHeader(ServerHeader h)  {
		setHeader(h);
	} 
	/** Gets ServerHeader. */
	public ServerHeader getServerHeader() {
		Header h=getHeader(SipHeaders.Server);
		if (h==null) return null;
		else return new ServerHeader(h);
	} 
	/** Removes ServerHeader. */
	public void removeServerHeader()  {
		removeHeader(SipHeaders.Server);
	}


	/** Whether has AcceptHeader. */
	public boolean hasAcceptHeader() {
		return hasHeader(SipHeaders.Accept);
	}
	/** Sets AcceptHeader. */
	public void setAcceptHeader(AcceptHeader h)  {
		setHeader(h);
	} 
	/** Gets AcceptHeader. */
	public AcceptHeader getAcceptHeader() {
		Header h=getHeader(SipHeaders.Accept);
		if (h==null) return null;
		else return new AcceptHeader(h);
	} 
	/** Removes AcceptHeader. */
	public void removeAcceptHeader()  {
		removeHeader(SipHeaders.Accept);
	}


	/** Whether has AcceptEncodingHeader. */
	public boolean hasAcceptEncodingHeader() {
		return hasHeader(SipHeaders.Accept_Encoding);
	}
	/** Sets AcceptEncodingHeader. */
	public void setAcceptEncodingHeader(AcceptEncodingHeader h)  {
		setHeader(h);
	} 
	/** Gets AcceptEncodingHeader. */
	public AcceptEncodingHeader getAcceptEncodingHeader() {
		Header h=getHeader(SipHeaders.Accept_Encoding);
		if (h==null) return null;
		else return new AcceptEncodingHeader(h);
	} 
	/** Removes AcceptEncodingHeader. */
	public void removeAcceptEncodingHeader()  {
		removeHeader(SipHeaders.Accept_Encoding);
	}


	/** Whether has AcceptLanguageHeader. */
	public boolean hasAcceptLanguageHeader() {
		return hasHeader(SipHeaders.Accept_Language);
	}
	/** Sets AcceptLanguageHeader. */
	public void setAcceptLanguageHeader(AcceptLanguageHeader h)  {
		setHeader(h);
	} 
	/** Gets AcceptLanguageHeader. */
	public AcceptLanguageHeader getAcceptLanguageHeader() {
		Header h=getHeader(SipHeaders.Accept_Language);
		if (h==null) return null;
		else return new AcceptLanguageHeader(h);
	} 
	/** Removes AcceptLanguageHeader. */
	public void removeAcceptLanguageHeader()  {
		removeHeader(SipHeaders.Accept_Language);
	}


	/** Whether has AlertInfoHeader. */
	public boolean hasAlertInfoHeader() {
		return hasHeader(SipHeaders.Alert_Info);
	}
	/** Sets AlertInfoHeader. */
	public void setAlertInfoHeader(AlertInfoHeader h)  {
		setHeader(h);
	} 
	/** Gets AlertInfoHeader. */
	public AlertInfoHeader getAlertInfoHeader() {
		Header h=getHeader(SipHeaders.Alert_Info);
		if (h==null) return null;
		else return new AlertInfoHeader(h);
	} 
	/** Removes AlertInfoHeader. */
	public void removeAlertInfoHeader()  {
		removeHeader(SipHeaders.Alert_Info);
	}


	/** Whether has AllowHeader. */
	public boolean hasAllowHeader() {
		return hasHeader(SipHeaders.Allow);
	}
	/** Sets AllowHeader. */
	public void setAllowHeader(AllowHeader h)  {
		setHeader(h);
	} 
	/** Gets AllowHeader. */
	public AllowHeader getAllowHeader() {
		Header h=getHeader(SipHeaders.Allow);
		if (h==null) return null;
		else return new AllowHeader(h);
	} 
	/** Removes AllowHeader. */
	public void removeAllowHeader()  {
		removeHeader(SipHeaders.Allow);
	}


	/** Whether Message has ExpiresHeader. */   
	public boolean hasExpiresHeader() {
		return hasHeader(SipHeaders.Expires);
	}   
	/** Gets ExpiresHeader of Message. */
	public ExpiresHeader getExpiresHeader() {
		Header h=getHeader(SipHeaders.Expires);
		if (h==null) return null;
		else return new ExpiresHeader(h);
	} 
	/** Sets ExpiresHeader of Message. */
	public void setExpiresHeader(ExpiresHeader eh)  {
		setHeader(eh);
	}    
	/** Removes ExpiresHeader from Message (if it exists). */
	public void removeExpiresHeader()  {
		removeHeader(SipHeaders.Expires);
	}   


	/** Whether has AuthenticationInfoHeader. */
	public boolean hasAuthenticationInfoHeader() {
		return hasHeader(SipHeaders.Authentication_Info);
	}
	/** Sets AuthenticationInfoHeader. */
	public void setAuthenticationInfoHeader(AuthenticationInfoHeader h)  {
		setHeader(h);
	} 
	/** Gets AuthenticationInfoHeader. */
	public AuthenticationInfoHeader getAuthenticationInfoHeader() {
		Header h=getHeader(SipHeaders.Authentication_Info);
		if (h==null) return null;
		else return new AuthenticationInfoHeader(h);
	} 
	/** Removes AuthenticationInfoHeader. */
	public void removeAuthenticationInfoHeader()  {
		removeHeader(SipHeaders.Authentication_Info);
	}


	/** Whether has AuthorizationHeader. */
	public boolean hasAuthorizationHeader() {
		return hasHeader(SipHeaders.Authorization);
	}
	/** Sets AuthorizationHeader. */
	public void setAuthorizationHeader(AuthorizationHeader h)  {
		setHeader(h);
	} 
	/** Gets AuthorizationHeader. */
	public AuthorizationHeader getAuthorizationHeader() {
		Header h=getHeader(SipHeaders.Authorization);
		if (h==null) return null;
		else return new AuthorizationHeader(h);
	} 
	/** Removes AuthorizationHeader. */
	public void removeAuthorizationHeader()  {
		removeHeader(SipHeaders.Authorization);
	}


	/** Whether has WwwAuthenticateHeader. */
	public boolean hasWwwAuthenticateHeader() {
		return hasHeader(SipHeaders.WWW_Authenticate);
	}
	/** Sets WwwAuthenticateHeader. */
	public void setWwwAuthenticateHeader(WwwAuthenticateHeader h)  {
		setHeader(h);
	} 
	/** Gets WwwAuthenticateHeader. */
	public WwwAuthenticateHeader getWwwAuthenticateHeader() {
		Header h=getHeader(SipHeaders.WWW_Authenticate);
		if (h==null) return null;
		else return new WwwAuthenticateHeader(h);
	} 
	/** Removes WwwAuthenticateHeader. */
	public void removeWwwAuthenticateHeader()  {
		removeHeader(SipHeaders.WWW_Authenticate);
	}


	/** Whether has ProxyAuthenticateHeader. */
	public boolean hasProxyAuthenticateHeader() {
		return hasHeader(SipHeaders.Proxy_Authenticate);
	}
	/** Sets ProxyAuthenticateHeader. */
	public void setProxyAuthenticateHeader(ProxyAuthenticateHeader h)  {
		setHeader(h);
	} 
	/** Gets ProxyAuthenticateHeader. */
	public ProxyAuthenticateHeader getProxyAuthenticateHeader() {
		Header h=getHeader(SipHeaders.Proxy_Authenticate);
		if (h==null) return null;
		else return new ProxyAuthenticateHeader(h);
	} 
	/** Removes ProxyAuthenticateHeader. */
	public void removeProxyAuthenticateHeader()  {
		removeHeader(SipHeaders.Proxy_Authenticate);
	}


	/** Whether has ProxyAuthorizationHeader. */
	public boolean hasProxyAuthorizationHeader() {
		return hasHeader(SipHeaders.Proxy_Authorization);
	}
	/** Sets ProxyAuthorizationHeader. */
	public void setProxyAuthorizationHeader(ProxyAuthorizationHeader h)  {
		setHeader(h);
	} 
	/** Gets ProxyAuthorizationHeader. */
	public ProxyAuthorizationHeader getProxyAuthorizationHeader() {
		Header h=getHeader(SipHeaders.Proxy_Authorization);
		if (h==null) return null;
		else return new ProxyAuthorizationHeader(h);
	} 
	/** Removes ProxyAuthorizationHeader. */
	public void removeProxyAuthorizationHeader()  {
		removeHeader(SipHeaders.Proxy_Authorization);
	}


	/** Whether has SupportedHeader. */
	public boolean hasSupportedHeader() {
		return hasHeader(SipHeaders.Supported);
	}
	/** Sets SupportedHeader. */
	public void setSupportedHeader(SupportedHeader h)  {
		setHeader(h);
	} 
	/** Gets SupportedHeader. */
	public SupportedHeader getSupportedHeader() {
		Header h=getHeader(SipHeaders.Supported);
		if (h==null) return null;
		else return new SupportedHeader(h);
	} 
	/** Removes SupportedHeader. */
	public void removeSupportedHeader()  {
		removeHeader(SipHeaders.Supported);
	}


	/** Whether has RequireHeader. */
	public boolean hasRequireHeader() {
		return hasHeader(SipHeaders.Require);
	}
	/** Sets RequireHeader. */
	public void setRequireHeader(RequireHeader h)  {
		setHeader(h);
	} 
	/** Gets RequireHeader. */
	public RequireHeader getRequireHeader() {
		Header h=getHeader(SipHeaders.Require);
		if (h==null) return null;
		else return new RequireHeader(h);
	} 
	/** Removes RequireHeader. */
	public void removeRequireHeader()  {
		removeHeader(SipHeaders.Require);
	}


	/** Whether has UnsupportedHeader. */
	public boolean hasUnsupportedHeader() {
		return hasHeader(SipHeaders.Unsupported);
	}
	/** Sets UnsupportedHeader. */
	public void setUnsupportedHeader(UnsupportedHeader h)  {
		setHeader(h);
	} 
	/** Gets UnsupportedHeader. */
	public UnsupportedHeader getUnsupportedHeader() {
		Header h=getHeader(SipHeaders.Unsupported);
		if (h==null) return null;
		else return new UnsupportedHeader(h);
	} 
	/** Removes UnsupportedHeader. */
	public void removeUnsupportedHeader()  {
		removeHeader(SipHeaders.Unsupported);
	}


	/** Whether has ProxyRequireHeader. */
	public boolean hasProxyRequireHeader() {
		return hasHeader(SipHeaders.Proxy_Require);
	}
	/** Sets ProxyRequireHeader. */
	public void setProxyRequireHeader(ProxyRequireHeader h)  {
		setHeader(h);
	} 
	/** Gets ProxyRequireHeader. */
	public ProxyRequireHeader getProxyRequireHeader() {
		Header h=getHeader(SipHeaders.Proxy_Require);
		if (h==null) return null;
		else return new ProxyRequireHeader(h);
	} 
	/** Removes ProxyRequireHeader. */
	public void removeProxyRequireHeader()  {
		removeHeader(SipHeaders.Proxy_Require);
	}


	
	//**************************** RFC 2543 Legacy ****************************


	/** Checks whether the next Route is formed according to RFC2543 Strict Route
	  * and adapts the message. */
	public void rfc2543RouteAdapt()  {
		if (hasRouteHeader()) {
			MultipleHeader mrh=getRoutes();
			RouteHeader rh=new RouteHeader(mrh.getTop());
			if (!(new RouteHeader(mrh.getTop())).getNameAddress().getAddress().hasLr()) {
				// re-format the message according to the RFC2543 Strict Route rule
				GenericURI next_hop=(new RouteHeader(mrh.getTop())).getNameAddress().getAddress();
				GenericURI recipient=getRequestLine().getAddress();
				mrh.removeTop();
				mrh.addBottom(new RouteHeader(new NameAddress(recipient)));
				setRoutes(mrh);
				setRequestLine(new RequestLine(getRequestLine().getMethod(),next_hop));
			}   
		}
	}
  

	/** Changes form RFC2543 Strict Route to RFC3261 Lose Route.
	  * <p> The Request-URI is replaced with the last
	  * value from the Route header, and that value is removed from the
	  * Route header. */
	public void rfc2543toRfc3261RouteUpdate()  {
		// the message is formed according with RFC2543 strict route
		// the next hop is the request-uri
		// the recipient of the message is the last Route value
		RequestLine request_line=getRequestLine();
		GenericURI next_hop=request_line.getAddress();
		MultipleHeader mrh=getRoutes();
		GenericURI target=(new RouteHeader(mrh.getBottom())).getNameAddress().getAddress();
		mrh.removeBottom();
		next_hop.addLr();
		mrh.addTop(new RouteHeader(new NameAddress(next_hop)));
		removeRoutes();
		addRoutes(mrh);
		setRequestLine(new RequestLine(request_line.getMethod(),target));
	}



	//****************************** Extensions *******************************

	/** Returns boolean value to indicate if Message is a INFO request (RFC2976) */
	public boolean isInfo() throws NullPointerException {
		return isRequest(SipMethods.INFO);
	}

	/** Returns boolean value to indicate if Message is a PRACK request (RFC3262) */
	public boolean isPrack() throws NullPointerException {
		return isRequest(SipMethods.PRACK);
	}

	/** Returns boolean value to indicate if Message is a UPDATE request (RFC3311) */
	public boolean isUpdate() throws NullPointerException {
		return isRequest(SipMethods.UPDATE);
	}

	/** Returns boolean value to indicate if Message is a MESSAGE request (RFC3428) */
	public boolean isMessage() throws NullPointerException {
		return isRequest(SipMethods.MESSAGE);
	}

	/** Returns boolean value to indicate if Message is a REFER request (RFC3515) */
	public boolean isRefer() throws NullPointerException {
		return isRequest(SipMethods.REFER);
	}

	/** Returns boolean value to indicate if Message is a NOTIFY request (RFC3265) */
	public boolean isNotify() throws NullPointerException {
		return isRequest(SipMethods.NOTIFY);
	}

	/** Returns boolean value to indicate if Message is a SUBSCRIBE request (RFC3265) */
	public boolean isSubscribe() throws NullPointerException {
		return isRequest(SipMethods.SUBSCRIBE);
	}

	/** Returns boolean value to indicate if Message is a PUBLISH request (RFC3903) */
	public boolean isPublish() throws NullPointerException {
		return isRequest(SipMethods.PUBLISH);
	}



	/** Whether the message has the RSeqHeader */   
	public boolean hasRSeqHeader() {
		return hasHeader(SipHeaders.RSeq);
	}
	/** Gets RSeqHeader */
	public RSeqHeader getRSeqHeader() {
		Header h=getHeader(SipHeaders.RSeq);
		if (h==null) return null;
		return new RSeqHeader(h);
	}  
	/** Sets RSeqHeader */
	public void setRSeqHeader(RSeqHeader h)  {
		setHeader(h);
	} 
	/** Removes RSeqHeader from Message (if it exists) */
	public void removeRSeqHeader()  {
		removeHeader(SipHeaders.RSeq);
	}


	/** Whether the message has the RAckHeader */   
	public boolean hasRAckHeader() {
		return hasHeader(SipHeaders.RAck);
	}
	/** Gets RAckHeader */
	public RAckHeader getRAckHeader() {
		Header h=getHeader(SipHeaders.RAck);
		if (h==null) return null;
		return new RAckHeader(h);
	}  
	/** Sets RAckHeader */
	public void setRAckHeader(RAckHeader h)  {
		setHeader(h);
	} 
	/** Removes RAckHeader from Message (if it exists) */
	public void removeRAckHeader()  {
		removeHeader(SipHeaders.RAck);
	}


	/** Whether the message has the SessionExpiresHeader */   
	public boolean hasSessionExpiresHeader() {
		return hasHeader(SipHeaders.Session_Expires);
	}
	/** Gets SessionExpiresHeader */
	public SessionExpiresHeader getSessionExpiresHeader() {
		Header h=getHeader(SipHeaders.Session_Expires);
		if (h==null) return null;
		return new SessionExpiresHeader(h);
	}  
	/** Sets SessionExpiresHeader */
	public void setSessionExpiresHeader(SessionExpiresHeader h)  {
		setHeader(h);
	} 
	/** Removes SessionExpiresHeader from Message (if it exists) */
	public void removeSessionExpiresHeader()  {
		removeHeader(SipHeaders.Session_Expires);
	}


	/** Whether the message has the MinSEHeader */   
	public boolean hasMinSEHeader() {
		return hasHeader(SipHeaders.Min_SE);
	}
	/** Gets MinSEHeader */
	public MinSEHeader getMinSEHeader() {
		Header h=getHeader(SipHeaders.Min_SE);
		if (h==null) return null;
		return new MinSEHeader(h);
	}  
	/** Sets MinSEHeader */
	public void setMinSEHeader(MinSEHeader h)  {
		setHeader(h);
	} 
	/** Removes MinSEHeader from Message (if it exists) */
	public void removeMinSEHeader()  {
		removeHeader(SipHeaders.Min_SE);
	}


	/** Whether the message has the Refer-To header */   
	public boolean hasReferToHeader() {
		return hasHeader(SipHeaders.Refer_To);
	}
	/** Gets ReferToHeader */
	public ReferToHeader getReferToHeader() {
		Header h=getHeader(SipHeaders.Refer_To);
		if (h==null) return null;
		return new ReferToHeader(h);
	}  
	/** Sets ReferToHeader */
	public void setReferToHeader(ReferToHeader h)  {
		setHeader(h);
	} 
	/** Removes ReferToHeader from Message (if it exists) */
	public void removeReferToHeader()  {
		removeHeader(SipHeaders.Refer_To);
	}


	/** Whether the message has the Replaces header */   
	public boolean hasReplacesHeader() {
		return hasHeader(SipHeaders.Replaces);
	}
	/** Gets ReplacesHeader */
	public ReplacesHeader getReplacesHeader() {
		Header h=getHeader(SipHeaders.Replaces);
		if (h==null) return null;
		return new ReplacesHeader(h);
	}  
	/** Sets ReplacesHeader */
	public void setReplacesHeader(ReplacesHeader h)  {
		setHeader(h);
	} 
	/** Removes ReplacesHeader from Message (if it exists) */
	public void removeReplacesHeader()  {
		removeHeader(SipHeaders.Replaces);
	}


	/** Whether the message has the Referred-By header */   
	public boolean hasReferredByHeader() {
		return hasHeader(SipHeaders.Refer_To);
	}
	/** Gets ReferredByHeader */
	public ReferredByHeader getReferredByHeader() {
		Header h=getHeader(SipHeaders.Referred_By);
		if (h==null) return null;
		return new ReferredByHeader(h);
	}  
	/** Sets ReferredByHeader */
	public void setReferredByHeader(ReferredByHeader h)  {
		setHeader(h);
	} 
	/** Removes ReferredByHeader from Message (if it exists) */
	public void removeReferredByHeader()  {
		removeHeader(SipHeaders.Referred_By);
	}


	/** Whether the message has the EventHeader */   
	public boolean hasEventHeader() {
		return hasHeader(SipHeaders.Event);
	}
	/** Gets EventHeader */
	public EventHeader getEventHeader() {
		Header h=getHeader(SipHeaders.Event);
		if (h==null) return null;
		return new EventHeader(h);
	}  
	/** Sets EventHeader */
	public void setEventHeader(EventHeader h)  {
		setHeader(h);
	} 
	/** Removes EventHeader from Message (if it exists) */
	public void removeEventHeader()  {
		removeHeader(SipHeaders.Event);
	}


	/** Whether the message has the AllowEventsHeader */   
	public boolean hasAllowEventsHeader() {
		return hasHeader(SipHeaders.Allow_Events);
	}
	/** Gets AllowEventsHeader */
	public AllowEventsHeader getAllowEventsHeader() {
		Header h=getHeader(SipHeaders.Allow_Events);
		if (h==null) return null;
		return new AllowEventsHeader(h);
	}  
	/** Sets AllowEventsHeader */
	public void setAllowEventsHeader(AllowEventsHeader h)  {
		setHeader(h);
	} 
	/** Removes AllowEventsHeader from Message (if it exists) */
	public void removeAllowEventsHeader()  {
		removeHeader(SipHeaders.Allow_Events);
	}


	/** Whether the message has the Subscription-State header */   
	public boolean hasSubscriptionStateHeader() {
		return hasHeader(SipHeaders.Subscription_State);
	}
	/** Gets SubscriptionStateHeader */
	public SubscriptionStateHeader getSubscriptionStateHeader() {
		Header h=getHeader(SipHeaders.Subscription_State);
		if (h==null) return null;
		return new SubscriptionStateHeader(h);
	}  
	/** Sets SubscriptionStateHeader */
	public void setSubscriptionStateHeader(SubscriptionStateHeader h)  {
		setHeader(h);
	} 
	/** Removes SubscriptionStateHeader from Message (if it exists) */
	public void removeSubscriptionStateHeader()  {
		removeHeader(SipHeaders.Subscription_State);
	}


	/** Whether has InfoPackageHeader. */
	public boolean hasInfoPackageHeader() {
		return hasHeader(SipHeaders.Info_Package);
	}
	/** Sets InfoPackageHeader. */
	public void setInfoPackageHeader(InfoPackageHeader h)  {
		setHeader(h);
	} 
	/** Gets InfoPackageHeader. */
	public InfoPackageHeader getInfoPackageHeader() {
		Header h=getHeader(SipHeaders.Info_Package);
		if (h==null) return null;
		else return new InfoPackageHeader(h);
	} 
	/** Removes InfoPackageHeader. */
	public void removeInfoPackageHeader()  {
		removeHeader(SipHeaders.Info_Package);
	}



	/** Whether has RecvInfoHeader. */
	public boolean hasRecvInfoHeader() {
		return hasHeader(SipHeaders.Recv_Info);
	}
	/** Sets RecvInfoHeader. */
	public void setRecvInfoHeader(RecvInfoHeader h)  {
		setHeader(h);
	} 
	/** Gets RecvInfoHeader. */
	public RecvInfoHeader getRecvInfoHeader() {
		Header h=getHeader(SipHeaders.Recv_Info);
		if (h==null) return null;
		else return new RecvInfoHeader(h);
	} 
	/** Removes RecvInfoHeader. */
	public void removeRecvInfoHeader()  {
		removeHeader(SipHeaders.Recv_Info);
	}

}
