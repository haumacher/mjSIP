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



import java.util.Hashtable;

import org.mjsip.sip.address.NameAddress;
import org.mjsip.sip.authentication.DigestAuthentication;
import org.mjsip.sip.header.AuthenticationHeader;
import org.mjsip.sip.header.AuthorizationHeader;
import org.mjsip.sip.header.CSeqHeader;
import org.mjsip.sip.header.ReplacesHeader;
import org.mjsip.sip.header.RequestLine;
import org.mjsip.sip.header.StatusLine;
import org.mjsip.sip.header.ViaHeader;
import org.mjsip.sip.message.SipMessage;
import org.mjsip.sip.message.SipMethods;
import org.mjsip.sip.message.SipResponses;
import org.mjsip.sip.provider.SipId;
import org.mjsip.sip.provider.SipProvider;
import org.mjsip.sip.transaction.InviteTransactionClient;
import org.mjsip.sip.transaction.TransactionClient;
import org.mjsip.sip.transaction.TransactionServer;
import org.slf4j.LoggerFactory;



/** Class ExtendedInviteDialog can be used to manage extended invite dialogs.
  * <p>
  * ExtendedInviteDialog extends the basic InviteDialog in order to:
  * <br>- support UAS and proxy authentication,
  * <br>- handle REFER/NOTIFY methods,
  * <br>- capture all methods within the dialog.
  */
public class ExtendedInviteDialog extends org.mjsip.sip.dialog.InviteDialog {
	
	private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(ExtendedInviteDialog.class);

	/** Max number of registration attempts. */
	static final int MAX_ATTEMPTS=3;

	/** ExtendedInviteDialog listener. */
	ExtendedInviteDialogListener ext_listener;
	
	/** Acive transactions. */
	Hashtable transactions;
 
	
	/** User name. */
	String username;

	/** User name. */
	String realm;

	/** User's passwd. */
	String passwd;

	/** Nonce for the next authentication. */
	String next_nonce;

	/** Qop for the next authentication. */
	String qop;

	/** Number of authentication attempts. */
	int attempts;




	/** Creates a new ReliableInviteDialog.
	  * @param sip_provider the SIP provider
	  * @param listener invite dialog listener */
	public ExtendedInviteDialog(SipProvider sip_provider, ExtendedInviteDialogListener listener) {
		super(sip_provider,listener);
		init(listener);
	}

		
	/** Creates a new ReliableInviteDialog for an already received INVITE request.
	  * @param sip_provider the SIP provider
	  * @param invite the already received INVITE message that creates this dialog
	  * @param listener invite dialog listener */
	public ExtendedInviteDialog(SipProvider sip_provider, SipMessage invite, ExtendedInviteDialogListener listener) {
		super(sip_provider,listener);
		init(listener);    
		//changeStatus(D_INVITED);
		//invite_req=invite;
		//update(false,invite_req);
		//invite_ts=new InviteTransactionServer(sip_provider,invite_req,this);
		onReceivedMessage(sip_provider,invite);
	}

	
	/** Creates a new ReliableInviteDialog.
	  * @param sip_provider the SIP provider
	  * @param username the user name for server or proxy authentication
	  * @param realm the realm for server or proxy authentication
	  * @param passwd for server or proxy authentication
	  * @param listener invite dialog listener */
	public ExtendedInviteDialog(SipProvider sip_provider, String username, String realm, String passwd, ExtendedInviteDialogListener listener) {
		super(sip_provider,listener);
		init(listener);
		this.username=username;
		this.realm=realm;
		this.passwd=passwd;
	}


	/** Creates a new ReliableInviteDialog for an already received INVITE request.
	  * @param sip_provider the SIP provider
	  * @param invite the already received INVITE message that creates this dialog
	  * @param username the user name for server or proxy authentication
	  * @param realm the realm for server or proxy authentication
	  * @param passwd for server or proxy authentication
	  * @param listener invite dialog listener */
	public ExtendedInviteDialog(SipProvider sip_provider, SipMessage invite, String username, String realm, String passwd, ExtendedInviteDialogListener listener) {
		super(sip_provider,listener);
		init(listener);    
		this.username=username;
		this.realm=realm;
		this.passwd=passwd;
		//changeStatus(D_INVITED);
		//invite_req=invite;
		//update(false,invite_req);
		//invite_ts=new InviteTransactionServer(sip_provider,invite_req,this);
		onReceivedMessage(sip_provider,invite);
	}

	
	/** Inits the ExtendedInviteDialog. */
	private void init(ExtendedInviteDialogListener listener) {
		this.ext_listener=listener;
		this.transactions=new Hashtable();
		this.username=null;
		this.realm=null;
		this.passwd=null;
		this.next_nonce=null;
		this.qop=null;
		this.attempts=0;
	}


	/** Sends a new request within the dialog */
	public void request(SipMessage req) {
		TransactionClient t=new TransactionClient(sip_provider,req,this);
		transactions.put(t.getTransactionId(),t);
		t.request();
	}


	/** Sends a new REFER within the dialog */
	public void refer(NameAddress refer_to) {
		refer(refer_to,null);
	}


	/** Sends a new REFER within the dialog */
	public void refer(NameAddress refer_to, NameAddress referred_by) {
		SipMessage req=sip_provider.messageFactory().createReferRequest(this,refer_to,referred_by);
		request(req);
	}


	/** Sends a new REFER within the dialog */
	public void refer(NameAddress refer_to, NameAddress referred_by, Dialog replaced_dialog) {
		SipMessage req=sip_provider.messageFactory().createReferRequest(this,refer_to,referred_by);
		req.setReplacesHeader(new ReplacesHeader(replaced_dialog.getCallID(),replaced_dialog.getRemoteTag(),replaced_dialog.getLocalTag()));
		request(req);
	}


	/**
	 * Sends a new NOTIFY within the dialog
	 * 
	 * @param code
	 *        See {@link SipResponses}.
	 */
	public void notify(int code, String reason) {
		notify((new StatusLine(code,reason)).toString());
	}


	/** Sends a new NOTIFY within the dialog */
	public void notify(String sipfragment) {
		SipMessage req=sip_provider.messageFactory().createNotifyRequest(this,"refer",null,sipfragment);
		request(req);
	}


	/** Responds with <i>resp</i> */
	@Override
	public void respond(SipMessage resp) {
		LOG.debug("inside x-respond(resp)");
		String method=resp.getCSeqHeader().getMethod();
		if (method.equals(SipMethods.INVITE) || method.equals(SipMethods.CANCEL) || method.equals(SipMethods.UPDATE)
				|| method.equals(SipMethods.BYE)) {
			super.respond(resp);
		}
		else {
			SipId transaction_id = SipId.createTransactionServerId(resp);
			LOG.debug("transaction-id={}", transaction_id);
			if (transactions.containsKey(transaction_id)) {
				LOG.trace("responding");
				TransactionServer t=(TransactionServer)transactions.get(transaction_id);
				t.respondWith(resp);
			}
			else {
				LOG.debug("transaction server not found; message discarded");
			}
		}
	} 


	/** Accepts a REFER request. */
	public void acceptRefer(SipMessage req) {
		LOG.debug("inside acceptRefer(refer)");
		SipMessage resp = sip_provider.messageFactory().createResponse(req, SipResponses.ACCEPTED, null, null);
		respond(resp);
	} 


	/** Refuses a REFER request. */
	public void refuseRefer(SipMessage req) {
		LOG.debug("inside refuseRefer(refer)");
		SipMessage resp = sip_provider.messageFactory().createResponse(req, SipResponses.DECLINE, null, null);
		respond(resp);
	} 


	/** Inherited from class SipProviderListener. */
	@Override
	public void onReceivedMessage(SipProvider provider, SipMessage msg) {
		LOG.trace("onReceivedMessage(): {}", msg.getFirstLine().substring(0, msg.toString().indexOf('\r')));
		if (msg.isResponse() || msg.isInvite() || msg.isAck() || msg.isCancel() || msg.isBye() || msg.isInfo()
				|| msg.isPrack() || msg.isUpdate()) {
			super.onReceivedMessage(provider,msg);
		} else {
			TransactionServer t=new TransactionServer(sip_provider,msg,this);
			transactions.put(t.getTransactionId(),t);
		 
			if (msg.isRefer()) {
				//SipMessage resp=SipMessageFactory.createResponse(msg,202,null,null,null);
				//respond(resp);
				NameAddress refer_to=msg.getReferToHeader().getNameAddress();
				NameAddress referred_by=null;
				if (msg.hasReferredByHeader()) referred_by=msg.getReferredByHeader().getNameAddress();
				if (ext_listener!=null) ext_listener.onDlgRefer(this,refer_to,referred_by,msg);
			} 
			else
			if (msg.isNotify()) {
				SipMessage resp = sip_provider.messageFactory().createResponse(msg, SipResponses.OK, null, null);
				respond(resp);
				String event=msg.getEventHeader().getValue();
				String sipfragment=msg.getStringBody();
				if (ext_listener!=null) ext_listener.onDlgNotify(this,event,sipfragment,msg);
			} 
			else {
				LOG.debug("Received alternative request {}", msg.getRequestLine().getMethod());
				if (ext_listener!=null) ext_listener.onDlgAltRequest(this,msg.getRequestLine().getMethod(),msg.getStringBody(),msg);
			}
		}
	}
	  

	/** Inherited from TransactionClientListener.
	  * When the TransactionClientListener goes into the "Completed" state, receiving a failure response */
	@Override
	public void onTransFailureResponse(TransactionClient tc, SipMessage msg) {
		LOG.trace("inside onTransFailureResponse({}, {})", tc.getTransactionId(), msg);
		String method=tc.getTransactionMethod();
		StatusLine status_line=msg.getStatusLine();
		int code=status_line.getCode();
		String reason=status_line.getReason();
		
		// AUTHENTICATION-BEGIN
		if ((code==401 && attempts<MAX_ATTEMPTS && msg.hasWwwAuthenticateHeader() && msg.getWwwAuthenticateHeader().getRealmParam().equalsIgnoreCase(realm))
		 || (code==407 && attempts<MAX_ATTEMPTS && msg.hasProxyAuthenticateHeader() && msg.getProxyAuthenticateHeader().getRealmParam().equalsIgnoreCase(realm)))   {
			attempts++;
			SipMessage req=tc.getRequestMessage();
			CSeqHeader csh=req.getCSeqHeader().incSequenceNumber();
			req.setCSeqHeader(csh);
			ViaHeader vh=req.getViaHeader();
			req.removeViaHeader();
			vh.setBranch(SipProvider.pickBranch());
			req.addViaHeader(vh);
			AuthenticationHeader wah;
			if (code==401) wah=msg.getWwwAuthenticateHeader();
			else wah=msg.getProxyAuthenticateHeader();
			String qop_options=wah.getQopOptionsParam();
			qop=(qop_options!=null)? "auth" : null;
			RequestLine rl=req.getRequestLine();
			DigestAuthentication digest=new DigestAuthentication(rl.getMethod(),rl.getAddress().toString(),wah,qop,null,0,null,username,passwd);
			AuthorizationHeader ah;
			if (code==401) ah=digest.getAuthorizationHeader();
			else ah=digest.getProxyAuthorizationHeader();
			req.setAuthorizationHeader(ah);
			transactions.remove(tc.getTransactionId());
			if (req.isInvite()) tc=new InviteTransactionClient(sip_provider,req,this);
			else tc=new TransactionClient(sip_provider,req,this);
			transactions.put(tc.getTransactionId(),tc);
			tc.request();
		}
		else
		// AUTHENTICATION-END
		if (method.equals(SipMethods.INVITE) || method.equals(SipMethods.CANCEL) || method.equals(SipMethods.BYE)) {
			super.onTransFailureResponse(tc,msg);
		}
		else
		if (tc.getTransactionMethod().equals(SipMethods.REFER)) {
			transactions.remove(tc.getTransactionId());
			if (ext_listener!=null) ext_listener.onDlgReferResponse(this,code,reason,msg);       
		}
		else {
			String body=msg.getStringBody();
			transactions.remove(tc.getTransactionId());
			if (ext_listener!=null) ext_listener.onDlgAltResponse(this,method,code,reason,body,msg);       
		}
	}


	/** Inherited from TransactionClientListener.
	  * When an TransactionClientListener goes into the "Terminated" state, receiving a 2xx response  */
	@Override
	public void onTransSuccessResponse(TransactionClient t, SipMessage msg) {
		LOG.trace("inside onTransSuccessResponse({}, {})", t.getTransactionId(), msg);
		attempts=0;
		String method=t.getTransactionMethod();
		StatusLine status_line=msg.getStatusLine();
		int code=status_line.getCode();
		String reason=status_line.getReason();
		
		if (method.equals(SipMethods.INVITE) || method.equals(SipMethods.CANCEL) || method.equals(SipMethods.BYE)) {
			super.onTransSuccessResponse(t,msg);
		}
		else
		if (t.getTransactionMethod().equals(SipMethods.REFER)) {
			transactions.remove(t.getTransactionId());
			if (ext_listener!=null) ext_listener.onDlgReferResponse(this,code,reason,msg);       
		}
		else {
			String body=msg.getStringBody();
			transactions.remove(t.getTransactionId());
			if (ext_listener!=null) ext_listener.onDlgAltResponse(this,method,code,reason,body,msg);       
		}
	}


	/** Inherited from TransactionClientListener.
	  * When the TransactionClient goes into the "Terminated" state, caused by transaction timeout */
	@Override
	public void onTransTimeout(TransactionClient t) {
		LOG.trace("inside onTransTimeout({})", t.getTransactionId());
		String method=t.getTransactionMethod();
		if (method.equals(SipMethods.INVITE) || method.equals(SipMethods.BYE)) {
			super.onTransTimeout(t);
		}
		else {
			// do something..        
			transactions.remove(t.getTransactionId());
		}
	} 

}
