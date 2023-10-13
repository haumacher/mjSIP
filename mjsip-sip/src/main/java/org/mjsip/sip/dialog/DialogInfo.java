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

package org.mjsip.sip.dialog;



import java.util.Vector;

import org.mjsip.sip.address.GenericURI;
import org.mjsip.sip.address.NameAddress;
import org.mjsip.sip.address.SipURI;
import org.mjsip.sip.header.FromHeader;
import org.mjsip.sip.header.Header;
import org.mjsip.sip.header.RecordRouteHeader;
import org.mjsip.sip.header.RouteHeader;
import org.mjsip.sip.header.ToHeader;
import org.mjsip.sip.message.SipMessage;
import org.mjsip.sip.provider.SipProvider;
import org.mjsip.sip.provider.SipStack;



/** Class DialogInfo maintains a complete state information of a SIP dialog.
  * Such dialog state includes:
  * <ul>
  * <li>local and remote URIs</li>
  * <li>local and remote contact URIs</li>
  * <li>call-id</li>
  * <li>local and remote tags</li> 
  * <li>local and remote cseqs</li>
  * <li>route set</li>
  * <li>secure flag (i.e. whether dialog should be secured)</li>
  * </ul>
  */
public class DialogInfo/* extends org.zoolu.util.MonitoredObject*/ {
	
	
	// ************************ Private attributes ************************

	/** Local name */
	NameAddress local_name;

	/** Remote name */
	NameAddress remote_name;

	/** Local contact url */
	NameAddress local_contact;

	/** Remote contact url */
	NameAddress remote_contact;

	/** Call-id */
	String call_id;

	/** Local tag */
	String local_tag;

	/** Remote tag */
	String remote_tag;
	/** Sets the remote tag */

	/** Local CSeq number */
	long local_cseq;

	/** Remote CSeq number */
	long remote_cseq;

	/** RSeq of the most recently received in-order reliable provisional response */
	long last_rseq;

	/** Route set (Vector<NameAddress>) */
	Vector route; 

	/** Secure flag indicating whether the current dialog should be secured */
	boolean secure; 


	/** Session interval.
	  * The maximum amount of time that can occur between session refresh requests
	  * in a dialog before the session will be considered timed out. */
	//int session_interval=0;
	int session_interval=SipStack.default_session_interval;

	/** Session expiration.
	  * The time at which an element will consider the session timed out,
	  * if no successful session refresh transaction occurs beforehand. */
	long session_expiration=0; 

	/** Refresher.
	  * Who is doing the refreshing -- UAC or UAS. */
	String refresher=null; 



	// **************************** Costructors *************************** 

	/** Creates a new DialogInfo.
	  * @param di dialog info to be cloned */
	/*public DialogInfo(DialogInfo di) {
		this.local_name=di.local_name;
		this.remote_name=di.remote_name;
		this.local_contact=di.local_contact;
		this.remote_contact=di.remote_contact;
		this.call_id=di.call_id;
		this.local_tag=di.local_tag;
		this.remote_tag=di.remote_tag;
		this.local_cseq=di.local_cseq;
		this.remote_cseq=di.remote_cseq;
		this.route=di.route; 
		this.secure=di.secure;
	}*/


	/** Creates a new empty DialogInfo. */
	public DialogInfo() {
		this.local_name=null;
		this.remote_name=null;
		this.local_contact=null;
		this.remote_contact=null;
		this.call_id=null;
		this.local_tag=null;
		this.remote_tag=null;
		this.local_cseq=-1;
		this.remote_cseq=-1;
		this.last_rseq=-1;
		this.route=null;
		secure=false;
	}
 

	// ************************** Public methods **************************

	/** Sets the local name. */
	public void setLocalName(NameAddress url) { local_name=url; }
	/** Gets the local name. */
	public NameAddress getLocalName() { return local_name; }


	/** Sets the remote name. */
	public void setRemoteName(NameAddress url) { remote_name=url; }
	/** Gets the remote name. */
	public NameAddress getRemoteName() { return remote_name; }


	/** Sets the local contact url. */
	public void setLocalContact(NameAddress name_address) { local_contact=name_address; }
	/** Gets the local contact url */
	public NameAddress getLocalContact() { return local_contact; }


	/** Sets the remote contact url. */
	public void setRemoteContact(NameAddress name_address) { remote_contact=name_address; }
	/** Gets the remote contact url. */
	public NameAddress getRemoteContact() { return remote_contact; }

  
	/** Sets the call-id. */
	public void setCallID(String id) { call_id=id; }
	/** Gets the call-id. */
	public String getCallID() { return call_id; }

	
	/** Sets the local tag. */
	public void setLocalTag(String tag) { local_tag=tag; }
	/** Gets the local tag. */
	public String getLocalTag() { return local_tag; }


	public void setRemoteTag(String tag) { remote_tag=tag; }
	/** Gets the remote tag. */
	public String getRemoteTag() { return remote_tag; }

	
	/** Sets the local CSeq number. */
	public void setLocalCSeq(long cseq) { local_cseq=cseq; }
	/** Increments the local CSeq number. */
	public void incLocalCSeq() { local_cseq++; }
	/** Gets the local CSeq number. */
	public long getLocalCSeq() { return local_cseq; }


	/** Sets the remote CSeq number. */
	public void setRemoteCSeq(long cseq) { remote_cseq=cseq; }
	/** Increments the remote CSeq number. */
	public void incRemoteCSeq() { remote_cseq++; }
	/** Gets the remote CSeq number. */
	public long getRemoteCSeq() { return remote_cseq; }


	/** Sets the last RSeq number. */
	public void setLastRSeq(long rseq) { last_rseq=rseq; }
	/** Increments the last RSeq number. */
	//public void incLastRSeq() { last_rseq++; }
	/** Gets the last RSeq number. */
	public long getLastRSeq() { return last_rseq; }


	/** Sets the route set.
	  * @param r the route set (Vector of <code>NameAddress</code>) */
	public void setRoute(Vector r) { route=r; }
	/** Gets the route set.
	  * @return the route set (Vector of <code>NameAddress</code>) */
	public Vector getRoute() { return route; }

	/** Sets the secure flag.
	  * It indicates whether the current dialog should be secured. */
	public void setSecure(boolean sec) { secure=sec; }
	/** Whether secure flag is set.
	  * That is whether the current dialog should be secured. */
	public boolean isSecure() { return secure; }


	/** Sets the session interval.
	  * That is the maximum amount of time that can occur between session refresh requests
	  * in a dialog before the session will be considered timed out. */
	public void setSessionInterval(int session_interval) { this.session_interval=session_interval; }
	/** Gets the session interval.
	  * That is the maximum amount of time that can occur between session refresh requests
	  * in a dialog before the session will be considered timed out. */
	public int getSessionInterval() { return session_interval; }

	/** Sets the session expiration.
	  * That is the time at which an element will consider the session timed out,
	  * if no successful session refresh transaction occurs beforehand. */
	public void setSessionExpiration(long session_expiration) { this.session_expiration=session_expiration; }
	/** Gets the session expiration.
	  * That is the time at which an element will consider the session timed out,
	  * if no successful session refresh transaction occurs beforehand. */
	public long getSessionExpiration() { return session_expiration; }

	/** Sets the refresher.
	  * That is who is doing the refreshing -- UAC or UAS. */
	public void setRefresher(String refresher) { this.refresher=refresher; }
	/** Sets the refresher.
	  * That is who is doing the refreshing -- UAC or UAS. */
	public String getRefresher() { return refresher; }


	/** Updates empty attributes (tags, route set) and mutable attributes (cseqs, contacts), based on a new message.
	  * @param is_client indicates whether the Dialog is acting as transaction client for the current message.
	  * @param sip_provider the SIP provider used to get node address for updating the route set
	  * @param msg the message that is used to update the Dialog state */
	public void update(boolean is_client, SipProvider sip_provider, SipMessage msg) {
		update(is_client,sip_provider.getViaAddress(),sip_provider.getPort(),msg);
	}


	/** Updates empty attributes (tags, route set) and mutable attributes (cseqs, contacts), based on a new message.
	  * @param is_client indicates whether the Dialog is acting as transaction client for the current message.
	  * @param via_addr the via address of this node, used to update the route set
	  * @param host_port the via address of this node, used to update the route set
	  * @param msg the message that is used to update the Dialog state */
	public void update(boolean is_client, String via_addr, int host_port, SipMessage msg) {
		
		// update call_id
		if (call_id==null) call_id=msg.getCallIdHeader().getCallId();

		// update names and tags
		if (is_client) {
			/*if (remote_name==null || remote_tag==null)*/ {
				ToHeader to=msg.getToHeader();
				if (remote_name==null) remote_name=to.getNameAddress();
				/*if (remote_tag==null) */remote_tag=to.getTag();
			}
			if (local_name==null || local_tag==null) {
				FromHeader from=msg.getFromHeader();
				if (local_name==null) local_name=from.getNameAddress();
				if (local_tag==null) local_tag=from.getTag();
			}
			long cseq=msg.getCSeqHeader().getSequenceNumber();
			if (cseq>local_cseq) local_cseq=cseq;
			//if (remote_cseq==-1) remote_cseq=SipProvider.pickInitialCSeq()-1;
		}
		else {
			/*if (local_name==null || local_tag==null)*/ {
				ToHeader to=msg.getToHeader();
				if (local_name==null) local_name=to.getNameAddress();
				/*if (local_tag==null) */local_tag=to.getTag();
			}
			if (remote_name==null || remote_tag==null) {
				FromHeader from=msg.getFromHeader();
				if (remote_name==null) remote_name=from.getNameAddress();
				if (remote_tag==null) remote_tag=from.getTag();
			}
			long cseq=msg.getCSeqHeader().getSequenceNumber();
			if (cseq>remote_cseq) remote_cseq=cseq;
			if (local_cseq==-1) local_cseq=SipProvider.pickInitialCSeq()-1;
		}
		// update contact
		if (msg.hasContactHeader()) {
			if ((is_client && msg.isRequest()) || (!is_client && msg.isResponse()))
				local_contact=msg.getContactHeader().getNameAddress();
			else
				remote_contact=msg.getContactHeader().getNameAddress();
		}
		// update route
		if (is_client) {
			if (msg.isRequest() && msg.hasRouteHeader() && route==null) {
				Vector rh=msg.getRoutes().getHeaders();
				int size=rh.size();
				route=new Vector(size);
				for (int i=0; i<size; i++)
					route.addElement((new RouteHeader((Header)rh.elementAt(i))).getNameAddress());
			}
			if (msg.isResponse() && msg.hasRecordRouteHeader()) {
				Vector rrh=msg.getRecordRoutes().getHeaders();
				int size=rrh.size();
				route=new Vector(size);
				for (int i=0; i<size; i++)
					route.insertElementAt(new RecordRouteHeader((Header)rrh.elementAt(size-1-i)).getNameAddress(),i);
			}
		}
		else {
			if (msg.isRequest() && msg.hasRouteHeader() && route==null) {
				Vector reverse_rh=msg.getRoutes().getHeaders();
				int size=reverse_rh.size();
				route=new Vector(size);
				for (int i=0; i<size; i++)
					route.insertElementAt(new RouteHeader((Header)reverse_rh.elementAt(size-1-i)).getNameAddress(),i); 
			}
			if (msg.isRequest() && msg.hasRecordRouteHeader()) {
				Vector rrh=msg.getRecordRoutes().getHeaders();
				int size=rrh.size();
				route=new Vector(size);
				for (int i=0; i<size; i++)
					route.insertElementAt(new RecordRouteHeader((Header)rrh.elementAt(i)).getNameAddress(),i);
			}
		}
		// REMOVE THE LOCAL NODE FROM THE ROUTE SET (ELIMINATE FIRST-HOP LOOP)
		if (SipStack.on_dialog_route) {
			if (route!=null && route.size()>0) {
				GenericURI uri=((NameAddress)route.elementAt(0)).getAddress();
				SipURI sip_uri=(uri.isSipURI())? new SipURI(uri) : null; 
				if (sip_uri!=null && sip_uri.getHost().equals(via_addr) && sip_uri.getPort()==host_port) {
					route.removeElementAt(0);
				}
			}
			if (route!=null && route.size()>0) {
				GenericURI uri=((NameAddress)route.elementAt(route.size()-1)).getAddress();
				SipURI sip_uri=(uri.isSipURI())? new SipURI(uri) : null; 
				if (sip_uri!=null && sip_uri.getHost().equals(via_addr) && sip_uri.getPort()==host_port) {
					route.removeElementAt(route.size()-1);
				}
			}
		}
		// update secure
		if (!secure && msg.isRequest()) {
			GenericURI request_uri=msg.getRequestLine().getAddress();
			if (request_uri.isSipURI() && new SipURI(request_uri).isSecure() && msg.getViaHeader().getProtocol().equalsIgnoreCase("tls")) {
				secure=true;
			}
		}
	}

}
