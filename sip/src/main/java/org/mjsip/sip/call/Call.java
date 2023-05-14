/*
 * Copyright (C) 2013 Luca Veltri - University of Parma - Italy
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

package org.mjsip.sip.call;


import org.mjsip.sip.address.GenericURI;
import org.mjsip.sip.address.NameAddress;
import org.mjsip.sip.address.SipNameAddress;
import org.mjsip.sip.address.SipURI;
import org.mjsip.sip.dialog.InviteDialog;
import org.mjsip.sip.dialog.InviteDialogListener;
import org.mjsip.sip.header.MultipleHeader;
import org.mjsip.sip.message.SipMessage;
import org.mjsip.sip.provider.SipProvider;
import org.zoolu.util.LogLevel;
import org.zoolu.util.Logger;


/** Class Call implements SIP calls.
  * <p>
  * The Call layer (class) provides a simplified interface
  * to the functionalities implemented by the InviteDialog layer (class).
  * <p>
  * It handles both outgoing or incoming calls.
  * <p>
  * Both offer/answer models are supported, that is: <br>
  * i) offer/answer in invite/2xx, or <br>
  * ii) offer/answer in 2xx/ack
  */
public class Call/* extends org.zoolu.util.MonitoredObject*/ {
	
	/** Logger */
	Logger logger;

	/** The SipProvider used for the call */
	protected SipProvider sip_provider;
	
	/** The invite dialog (sip.dialog.InviteDialog) */
	protected InviteDialog dialog;
	
	/** The user url (AOR) */
	protected NameAddress from_naddr=null;

	/** The user contact url */
	protected NameAddress contact_naddr=null;

	/** The user secure contact url */
	//protected NameAddress secure_contact_naddr;

	/** The local sdp */
	protected String local_sdp;

	/** The remote sdp */
	protected String remote_sdp;
	
	/** The call listener */
	CallListener listener;
 
	/** Call state */
	protected CallState call_state=new CallState();

	/** Invite dialog listener */
	private InviteDialogListener this_invite_dialog_listener;
	


	/** Creates a new Call for a caller.
	  * @param sip_provider the SIP provider
	  * @param caller the local calling user
	  * @param call_listener the call listener */
	public Call(SipProvider sip_provider, SipUser caller, CallListener call_listener) {
		initCall(sip_provider,caller,call_listener);
	}

	/** Creates a new Call for a callee, based on an already received INVITE request.
	  * @param sip_provider the SIP provider
	  * @param invite the received INVITE message
	  * @param call_listener the call listener */
	public Call(SipProvider sip_provider, SipMessage invite, CallListener call_listener) {
		initCall(sip_provider,null,call_listener);
		this.from_naddr=invite.getToHeader().getNameAddress();
		this.dialog=new InviteDialog(sip_provider,invite,this_invite_dialog_listener);
		//this.remote_sdp=invite.getStringBody();
		//changeState(CallState.C_INCOMING);
	}

	/** Creates a new Call for a callee, based on an already received INVITE request.
	  * @param sip_provider the SIP provider
	  * @param invite the received INVITE message
	  * @param callee the local called user
	  * @param call_listener the call listener */
	public Call(SipProvider sip_provider, SipMessage invite, SipUser callee, CallListener call_listener) {
		initCall(sip_provider,callee,call_listener);
		this.from_naddr=invite.getToHeader().getNameAddress();
		this.dialog=new InviteDialog(sip_provider,invite,this_invite_dialog_listener);
		//this.remote_sdp=invite.getStringBody();
		//changeState(CallState.C_INCOMING);
	}

	/** Inits the Call.
	  * @param sip_provider the SIP provider
	  * @param user the local user
	  * @param call_listener the call listener */
	private void initCall(SipProvider sip_provider, SipUser user, CallListener call_listener) {
		this.sip_provider=sip_provider;
		this.logger=sip_provider.getLogger();
		this.listener=call_listener;
		if (user!=null) {
			this.from_naddr=user.getAddress();
			this.contact_naddr=user.getContactAddress();
		}
		this.dialog=null;
		this.local_sdp=null;
		this.remote_sdp=null;
		changeState(CallState.C_IDLE);

		this_invite_dialog_listener=new ThisInviteDialogListener(this);
	}

	/** Waits for an incoming call. */
	public void listen() {
		dialog=new InviteDialog(sip_provider,this_invite_dialog_listener);
		dialog.listen();
		changeState(CallState.C_IDLE);
	}

	/** Gets the current call state.
	  * @return (copy of) the call state */
	public CallState getState() {
		return new CallState(call_state);
	}

	/** Changes the call state.
	  * @param state the new state value */
	protected void changeState(int state) {
		call_state.setState(state);
		log(LogLevel.DEBUG,"changed call state: "+call_state.toString());
	}

	/** Gets the current invite dialog. */
	/*public InviteDialog getInviteDialog() {
		return dialog;
	}*/

	/** Gets the current call-id.
	  * @return the call-id */
	public String getCallId() {
		if (dialog==null) return null;
		else return dialog.getCallID();
	}

	/** Gets the current local session descriptor.
	  * @return the session descriptor */
	public String getLocalSessionDescriptor() {
		return local_sdp;
	}
	
	/** Sets a new local session descriptor.
	  * @param sdp the session descriptor */
	public void setLocalSessionDescriptor(String sdp) {
		local_sdp=sdp;
	}

	/** Gets the current remote session descriptor.
	  * @return the session descriptor */
	public String getRemoteSessionDescriptor() {
		return remote_sdp;
	}
	
	/** Whether the call is on (active). */
	/*public boolean isOnCall() {
		return dialog.isSessionActive();
	}*/

	/** Starts a new call, inviting a remote user (<i>callee</i>).
	  * @param callee the callee */
	public void call(NameAddress callee) {
		call(callee,null,null);
	}

	/** Starts a new call, inviting a remote user (<i>callee</i>).
	  * @param callee the callee address
	  * @param sdp the session descriptor */
	public void call(NameAddress callee, String sdp) {
		call(callee,null,sdp);
	}

	/** Starts a new call, inviting a remote user (<i>callee</i>).
	  * @param callee the callee address
	  * @param caller the caller address
	  * @param sdp the session descriptor */
	public void call(NameAddress callee, NameAddress caller, String sdp) {
		log(LogLevel.DEBUG,"calling "+callee);
		dialog=new InviteDialog(sip_provider,this_invite_dialog_listener);
		if (caller==null) caller=from_naddr;
		NameAddress caller_contact=getContactAddress(SipNameAddress.isSIPS(callee));
		if (sdp!=null) local_sdp=sdp;
		if (local_sdp!=null) dialog.invite(callee,caller,caller_contact,local_sdp);
		else dialog.inviteWithoutOffer(callee,caller,caller_contact);
		changeState(CallState.C_OUTGOING);
	}

	/** Starts a new call, with the given <i>INVITE</i> request.
	  * @param invite the INVITE request message */
	public void call(SipMessage invite) {
		log(LogLevel.DEBUG,"calling "+invite.getRequestLine().getAddress());
		dialog=new InviteDialog(sip_provider,this_invite_dialog_listener);
		local_sdp=invite.getStringBody();
		if (local_sdp!=null) dialog.invite(invite);
		else dialog.inviteWithoutOffer(invite);
		changeState(CallState.C_OUTGOING);
	}

	/** Confirms the 2xx with an answer.
	  * The <i>offer</i> was in the 2xx response message, and the <i>answer</i> is sent in the ACK message.
	  * @param sdp the session descriptor answer */
	public void confirm2xxWithAnswer(String sdp) {
		local_sdp=sdp;
		dialog.confirm2xxWithAnswer(contact_naddr,sdp);
	}

	/** Confirms a 1xx reliable response.
	  * @param resp_1xx the reliable 1xx response the has to be confirmed with PRACK
	  * @param content_type the type of the content to be included within the PRACK (or <i>null</i> in case of no message body)
	  * @param content the message body to be included within the PRACK, or <i>null</i>. In an offer/answer model, this body represents the answer to the offer contained in the 1xx response message */
	public void confirm1xx(SipMessage resp_1xx, String content_type, byte[] content) {
		dialog.confirm1xx(resp_1xx,content_type,content);
	}

	/** Confirms a 1xx reliable response.
	 * @param prack the PRACK confirmation message */
	public void confirm1xx(SipMessage prack) {
		dialog.confirm1xx(prack);
	}

	/** Rings back for the incoming call. */
	public void ring() {
		if (dialog!=null) dialog.ring();
	}

	/** Respond to a incoming call (invite) with 183 progress.
	 * @param content_type the type of the content to be included within the response (or <i>null</i> in case of no message body)
	 * @param body the message body to be included within the response, or <i>null</i>. In an offer/answer model, this body represents the answer to the offer contained in the 1xx response message */
	public void progress(String content_type, byte[] body) {
		if (dialog!=null) dialog.respond(183,null,null,content_type,body);
	}

	/** Respond to a incoming call (invite) with 183 progress.
	* @param resp_183 the 183 response */
	public void progress(SipMessage resp_183) {
		if (dialog!=null) dialog.respond(resp_183);
	}

	/** Respond to a incoming call (invite) with <i>resp</i>. */
	/*public void respond(SipMessage resp) {
		if (dialog!=null) dialog.respond(resp);
	}*/

	/** Accepts the incoming call. */
	/*public void accept() {
		accept(local_sdp);
	}*/    

	/** Accepts the incoming call.
	  * @param sdp the session descriptor answer */
	public void accept(String sdp) {
		local_sdp=sdp;
		if (dialog!=null) {
			NameAddress callee_contact=getContactAddress(dialog.isSecure());
			dialog.accept(callee_contact,local_sdp);
		}
		changeState(CallState.C_ACTIVE);
	}

	/** Redirects the incoming call.
	  * @param redirect_url the new address where the call is redirected to */
	public void redirect(NameAddress redirect_url) {
		if (dialog!=null) dialog.redirect(302,"Moved Temporarily",redirect_url);
		changeState(CallState.C_CLOSED);
	}

	/** Refuses the incoming call. */
	public void refuse() {
		if (dialog!=null) dialog.refuse();
		changeState(CallState.C_CLOSED);
	}

	/** Cancels the outgoing call. */
	/*public void cancel() {
		if (dialog!=null) dialog.cancel();
	}*/

	/** Close the ongoing call. */
	/*public void bye() {
		if (dialog!=null) dialog.bye();
	}*/

	/** Modifies the current call.
	  * @param sdp the new session descriptor offer */
	public void modify(String sdp) {
		local_sdp=sdp;
		if (dialog!=null) dialog.reInvite(dialog.getLocalContact(),local_sdp);
	}

	/** Accepts an update request. */
	public void acceptUpdate(String sdp) {
		local_sdp=sdp;
		dialog.acceptUpdate(local_sdp);
	}


	/** Refuses an update request. */
	public void refuseUpdate() {
		dialog.refuseUpdate();
	}


	/** Closes an ongoing or incoming call.
	  * In case of incoming call it sends a 403 "Forbidden" response message,
	  * in case of outgoing call it sends a CANCEL request message,
	  * in case of an established call it sends a BYE request message. */
	public void hangup() {
		if (dialog!=null) {
			// try dialog.refuse(), cancel(), and bye() methods..
			//dialog.refuse();
			//dialog.cancel();
			//dialog.bye();
			if (call_state.isIdle()) dialog.cancel();
			else
			if (call_state.isIncoming()) dialog.refuse();
			else
			if (call_state.isOutgoing()) dialog.cancel();
			else
			if (call_state.isActive()) dialog.bye();
			else
			if (call_state.isClosed()) dialog.bye();
			
			changeState(CallState.C_CLOSED);
		}
	}
	
	/** Gets a local SIP or SIPS contact address.
	  * @param secure whether returning a SIPS or SIP URI (true=SIPS, false=SIP). */
	protected NameAddress getContactAddress(boolean secure) {
		if (contact_naddr!=null) return contact_naddr;
		// else
		GenericURI uri=from_naddr.getAddress();
		String user=(uri.isSipURI())? new SipURI(uri).getUserName() : null;
		return sip_provider.getContactAddress(user,secure);
	}


	// ************************ Callback methods ***********************

	/** When an incoming INVITE is received. */ 
	private void processDlgInvite(InviteDialog d, NameAddress callee, NameAddress caller, String sdp, SipMessage msg) {
		if (dialog!=null && d!=dialog) {  log(LogLevel.INFO,"NOT the current dialog");  return;  }
		// else
		this.dialog=d;
		changeState(CallState.C_INCOMING);
		if (sdp!=null && sdp.length()!=0) remote_sdp=sdp;
		if (listener!=null) listener.onCallInvite(this,callee,caller,sdp,msg);
	}

	/** When an incoming Re-INVITE is received. */ 
	private void processDlgReInvite(InviteDialog d, String sdp, SipMessage msg) {
		if (d!=dialog) {  log(LogLevel.INFO,"NOT the current dialog");  return;  }
		if (sdp!=null && sdp.length()!=0) remote_sdp=sdp;
		if (listener!=null) listener.onCallModify(this,sdp,msg);
	}

	/** When a 1xx response response is received for an INVITE transaction */ 
	private void processDlgInviteProvisionalResponse(InviteDialog d, int code, String reason, String sdp, SipMessage resp) {
		if (d!=dialog) {  log(LogLevel.INFO,"NOT the current dialog");  return;  }
		// else
		if (code==183) if (listener!=null) listener.onCallProgress(this,resp);
		if (code==180) if (listener!=null) listener.onCallRinging(this,resp);
	}

	/** When a reliable 1xx response is received for an INVITE request.
	  * If a body ("offer") has been included in the respose, method {@link InviteDialog#confirm1xxWithAnswer(SipMessage,String,byte[]) confirm1xxWithAnswer()} must be explicitely called. */ 
	private void processDlgInviteReliableProvisionalResponse(InviteDialog dialog, int code, String reason, String content_type, byte[] body, SipMessage resp) {
		if (dialog!=this.dialog) {  log(LogLevel.INFO,"NOT the current dialog");  return;  }
		// else
		if (listener!=null) listener.onCallConfirmableProgress(this,resp);
	}
	
	/** When a reliable 1xx response has been confirmed by the reception of a corresponding PRACK request. */
	private void processDlgInviteReliableProvisionalResponseConfirmed(InviteDialog dialog, int code, SipMessage resp, String content_type, byte[] body, SipMessage prack) {
		if (dialog!=this.dialog) {  log(LogLevel.INFO,"NOT the current dialog");  return;  }
		// else
		if (listener!=null) listener.onCallProgressConfirmed(this,resp,prack);
	}

	/** When a reliable 1xx response has NOT been confirmed (with a PRACK), and the retransmission timeout expired. */
	private void processDlgInviteReliableProvisionalResponseTimeout(InviteDialog dialog, int code, SipMessage resp) {
		// TODO
	}

	/** When a 2xx successful final response is received for an INVITE request.
	  * If a body ("offer") has been included in the respose, method {@link InviteDialog#confirm2xxWithAnswer(NameAddress,String) confirm2xxWithAnswer()} must be explicitely called. */ 
	private void processDlgInviteSuccessResponse(InviteDialog d, int code, String reason, String sdp, SipMessage msg) {
		if (d!=dialog) {  log(LogLevel.INFO,"NOT the current dialog");  return;  }
		// check if the call has been already cancelled or closed
		if (call_state.isClosed()) {
			log(LogLevel.INFO,"call already closed");
			dialog.bye();
			return;
		} 
		// else
		changeState(CallState.C_ACTIVE);
		if (sdp!=null && sdp.length()!=0) remote_sdp=sdp;
		if (listener!=null) listener.onCallAccepted(this,sdp,msg);
	}
	
	/** When a 3xx redirection response is received for an INVITE transaction. */ 
	private void processDlgInviteRedirectResponse(InviteDialog d, int code, String reason, MultipleHeader contacts, SipMessage msg) {
		if (d!=dialog) {  log(LogLevel.INFO,"NOT the current dialog");  return;  }
		changeState(CallState.C_CLOSED);
		if (listener!=null) listener.onCallRedirected(this,reason,contacts.getValues(),msg);
	}
	
	/** When a 400-699 failure response is received for an INVITE transaction. */ 
	private void processDlgInviteFailureResponse(InviteDialog d, int code, String reason, SipMessage msg) {
		if (d!=dialog) {  log(LogLevel.INFO,"NOT the current dialog");  return;  }
		changeState(CallState.C_CLOSED);
		if (listener!=null) listener.onCallRefused(this,reason,msg);
	}
	
	/** When INVITE transaction expires */ 
	private void processDlgInviteTimeout(InviteDialog d) {
		if (d!=dialog) {  log(LogLevel.INFO,"NOT the current dialog");  return;  }
		changeState(CallState.C_CLOSED);
		if (listener!=null) listener.onCallTimeout(this);
	}

	/** When a 1xx response response is received for a Re-INVITE transaction. */ 
	private void processDlgReInviteProvisionalResponse(InviteDialog d, int code, String reason, String sdp, SipMessage msg) {
		if (d!=dialog) {  log(LogLevel.INFO,"NOT the current dialog");  return;  }
		if (sdp!=null && sdp.length()!=0) remote_sdp=sdp;
	}

	/** When a 2xx successful final response is received for a Re-INVITE transaction */ 
	private void processDlgReInviteSuccessResponse(InviteDialog d, int code, String reason, String sdp, SipMessage msg) {
		if (d!=dialog) {  log(LogLevel.INFO,"NOT the current dialog");  return;  }
		if (sdp!=null && sdp.length()!=0) remote_sdp=sdp;
		if (listener!=null) listener.onCallModifyAccepted(this,sdp,msg);
	}

	/** When a 3xx redirection response is received for a Re-INVITE transaction */ 
	//private void processDlgReInviteRedirectResponse(InviteDialog d, int code, String reason, MultipleHeader contacts, SipMessage msg)
	//{  if (d!=dialog) {  log(LogLevel.INFO,"NOT the current dialog");  return;  }
	//   if (listener!=null) listener.onCallModifyRedirection(this,reason,contacts.getValues(),msg);
	//}

	/** When a 400-699 failure response is received for a Re-INVITE transaction. */ 
	private void processDlgReInviteFailureResponse(InviteDialog d, int code, String reason, SipMessage msg) {
		if (d!=dialog) {  log(LogLevel.INFO,"NOT the current dialog");  return;  }
		changeState(CallState.C_ACTIVE);
		if (listener!=null) listener.onCallModifyRefused(this,reason,msg);
	}

	/** When a Re-INVITE transaction expires */ 
	private void processDlgReInviteTimeout(InviteDialog d) {
		if (d!=dialog) {  log(LogLevel.INFO,"NOT the current dialog");  return;  }
		if (listener!=null) listener.onCallModifyTimeout(this);
	}

	/** When an incoming ACK is received for an INVITE transaction. */ 
	private void processDlgAck(InviteDialog d, String sdp, SipMessage msg) {
		if (d!=dialog) {  log(LogLevel.INFO,"NOT the current dialog");  return;  }
		if (sdp!=null && sdp.length()!=0) remote_sdp=sdp;
		if (listener!=null) listener.onCallConfirmed(this,sdp,msg);
	}
	
	/** When the INVITE handshake is successful terminated  and the call is active. */ 
	private void processDlgCall(InviteDialog dialog) {}

	/** When an incoming INFO is received. */ 
	private void processDlgInfo(InviteDialog dialog, String info_package, String content_type, byte[] body, SipMessage msg) {
		if (this.dialog!=dialog) {  log(LogLevel.INFO,"NOT the current dialog");  return;  }
		if (listener!=null) listener.onCallInfo(this,info_package,content_type,body,msg);
	}

	/** When an incoming CANCEL is received for an INVITE transaction. */ 
	private void processDlgCancel(InviteDialog d, SipMessage msg) {
		if (d!=dialog) {  log(LogLevel.INFO,"NOT the current dialog");  return;  }
		changeState(CallState.C_CLOSED);
		if (listener!=null) listener.onCallCancel(this,msg);
	}

	/** When an incoming UPDATE request is received within the dialog. */ 
	private void processDlgUpdate(InviteDialog dialog, String sdp, SipMessage msg) {
		if (this.dialog!=dialog) {  log(LogLevel.INFO,"NOT the current dialog");  return;  }
		if (sdp!=null && sdp.length()!=0) remote_sdp=sdp;
		if (listener!=null) listener.onCallUpdate(this,sdp,msg);
	}

	/** When a response is received for an UPDATE request. */ 
	private void processDlgUpdateResponse(InviteDialog dialog, int code, String reason, String body, SipMessage msg) {
		if (this.dialog!=dialog) {  log(LogLevel.INFO,"NOT the current dialog");  return;  }
		if (listener!=null) {
			if (code>=200 && code<300) listener.onCallUpdateAccepted(this,body,msg);
			else listener.onCallUpdateRefused(this,body,msg);
		}
	}

	/** When an incoming BYE is received */ 
	private void processDlgBye(InviteDialog d, SipMessage msg)       {
		if (d!=dialog) {  log(LogLevel.INFO,"NOT the current dialog");  return;  }
		changeState(CallState.C_CLOSED);
		if (listener!=null) listener.onCallBye(this,msg);
	}
	
	/** When a success response is received for a Bye request. */ 
	private void processDlgByeSuccessResponse(InviteDialog d, int code, String reason, SipMessage msg) {
		if (d!=dialog) {  log(LogLevel.INFO,"NOT the current dialog");  return;  }
		if (listener!=null) listener.onCallClosed(this,msg);
	}
 
	/** When a failure response is received for a Bye request. */ 
	private void processDlgByeFailureResponse(InviteDialog d, int code, String reason, SipMessage msg) {
		if (d!=dialog) {  log(LogLevel.INFO,"NOT the current dialog");  return;  }
		if (listener!=null) listener.onCallClosed(this,msg);
	}

	/** When the dialog is finally closed (after receiving a BYE request, a BYE response, or after BYE timeout). */ 
	private void processDlgClosed(InviteDialog dialog) {}


	// ************************* Inner classes *************************

	/** This InviteDialogListener.
	  */ 
	protected class ThisInviteDialogListener implements InviteDialogListener {
		
		Call c;
		
		public ThisInviteDialogListener(Call c) {
			this.c=c;
		}

		public void onDlgInvite(InviteDialog dialog, NameAddress callee, NameAddress caller, String body, SipMessage msg) {
			c.processDlgInvite(dialog,callee,caller,body,msg);
		}
		public void onDlgReInvite(InviteDialog dialog, String body, SipMessage msg) {
			c.processDlgReInvite(dialog,body,msg);
		}
		public void onDlgInviteProvisionalResponse(InviteDialog dialog, int code, String reason, String body, SipMessage msg) {
			c.processDlgInviteProvisionalResponse(dialog,code,reason,body,msg);
		}
		public void onDlgInviteSuccessResponse(InviteDialog dialog, int code, String reason, String body, SipMessage msg) {
			c.processDlgInviteSuccessResponse(dialog,code,reason,body,msg);
		}
		public void onDlgInviteRedirectResponse(InviteDialog dialog, int code, String reason, MultipleHeader contacts, SipMessage msg) {
			c.processDlgInviteRedirectResponse(dialog,code,reason,contacts,msg);
		}
		public void onDlgInviteFailureResponse(InviteDialog dialog, int code, String reason, SipMessage msg) {
			c.processDlgInviteFailureResponse(dialog,code,reason,msg);
		}
		public void onDlgInviteTimeout(InviteDialog dialog) {
			c.processDlgInviteTimeout(dialog);
		}
		public void onDlgReInviteProvisionalResponse(InviteDialog dialog, int code, String reason, String body, SipMessage msg) {
			c.processDlgReInviteProvisionalResponse(dialog,code,reason,body,msg);
		}
		public void onDlgInviteReliableProvisionalResponse(InviteDialog dialog, int code, String reason, String content_type, byte[] body, SipMessage resp) {
			c.processDlgInviteReliableProvisionalResponse(dialog,code,reason,content_type,body,resp);
		}
		public void onDlgInviteReliableProvisionalResponseConfirmed(InviteDialog dialog, int code, SipMessage resp, String content_type, byte[] body, SipMessage prack) {
			c.processDlgInviteReliableProvisionalResponseConfirmed(dialog,code,resp,content_type,body,prack);
		}
		public void onDlgInviteReliableProvisionalResponseTimeout(InviteDialog dialog, int code, SipMessage resp) {
			c.processDlgInviteReliableProvisionalResponseTimeout(dialog,code,resp);
		}
		public void onDlgReInviteSuccessResponse(InviteDialog dialog, int code, String reason, String body, SipMessage msg) {
			c.processDlgReInviteSuccessResponse(dialog,code,reason,body,msg);
		}
		public void onDlgReInviteFailureResponse(InviteDialog dialog, int code, String reason, SipMessage msg) {
			c.processDlgReInviteFailureResponse(dialog,code,reason,msg);
		}
		public void onDlgReInviteTimeout(InviteDialog dialog) {
			c.processDlgReInviteTimeout(dialog);
		}
		public void onDlgAck(InviteDialog dialog, String body, SipMessage msg) {
			c.processDlgAck(dialog,body,msg);
		}
		public void onDlgCall(InviteDialog dialog) {
			c.processDlgCall(dialog);
		}
		public void onDlgInfo(InviteDialog dialog, String info_package, String content_type, byte[] body, SipMessage msg) {
			c.processDlgInfo(dialog,info_package,content_type,body,msg);
		}
		public void onDlgCancel(InviteDialog dialog, SipMessage msg) {
			c.processDlgCancel(dialog,msg);
		}
		public void onDlgUpdate(InviteDialog dialog, String body, SipMessage msg) {
			c.processDlgUpdate(dialog,body,msg);
		}
		public void onDlgUpdateResponse(InviteDialog dialog, int code, String reason, String body, SipMessage msg) {
			c.processDlgUpdateResponse(dialog,code,reason,body,msg);
		}
		public void onDlgBye(InviteDialog dialog, SipMessage msg) {
			c.processDlgBye(dialog,msg);
		}
		public void onDlgByeSuccessResponse(InviteDialog dialog, int code, String reason, SipMessage msg) {
			c.processDlgByeSuccessResponse(dialog, code,reason,msg);
		}
		public void onDlgByeFailureResponse(InviteDialog dialog, int code, String reason, SipMessage msg) {
			c.processDlgByeFailureResponse(dialog,code,reason,msg);
		}
		public void onDlgClosed(InviteDialog dialog) {
			c.processDlgClosed(dialog);
		}

	}



	//**************************** Logs ****************************/

	/** Adds a new string to the default log. */
	protected void log(LogLevel level, String str) {
		if (logger!=null) logger.log(level,"Call: "+str);  
	}
}

