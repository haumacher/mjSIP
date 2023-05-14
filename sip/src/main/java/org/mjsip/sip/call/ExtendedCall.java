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

package org.mjsip.sip.call;



import org.mjsip.sip.address.NameAddress;
import org.mjsip.sip.address.SipNameAddress;
import org.mjsip.sip.dialog.ExtendedInviteDialog;
import org.mjsip.sip.dialog.ExtendedInviteDialogListener;
import org.mjsip.sip.dialog.InviteDialog;
import org.mjsip.sip.header.StatusLine;
import org.mjsip.sip.message.SipMessage;
import org.mjsip.sip.provider.SipProvider;
import org.zoolu.util.LogLevel;



/** Class ExtendedCall implements a SIP call.
  * <p>
  * ExtendedCall extends basic Call in order to:
  * <br>- support call transfer (REFER/NOTIFY methods),
  * <br>- support UAS and proxy authentication.
  */
public class ExtendedCall extends Call {
	

	/** Extended-call listener. */
	ExtendedCallListener xcall_listener;

	/** Last received refer request. */
	SipMessage refer=null;

  
	/** User name. */
	String username=null;

	/** User name. */
	String realm=null;

	/** User's passwd. */
	String passwd=null;

	/** Nonce for the next authentication. */
	String next_nonce;

	/** Qop for the next authentication. */
	String qop;

	/** Extended invite dialog listener */
	private ExtendedInviteDialogListener this_extended_invite_dialog_listener;



	/** Creates a new ExtendedCall for a caller.
	  * @param sip_provider the SIP provider
	  * @param caller the local calling user
	  * @param call_listener the call listener */
	public ExtendedCall(SipProvider sip_provider, SipUser caller, ExtendedCallListener call_listener) {
		super(sip_provider,caller,call_listener);
		initExtendedCall(caller,call_listener);
	}


	/** Creates a new Call for a callee, based on an already received INVITE request.
	  * @param sip_provider the SIP provider
	  * @param invite the received INVITE message
	  * @param call_listener the call listener */
	public ExtendedCall(SipProvider sip_provider, SipMessage invite, ExtendedCallListener call_listener) {
		super(sip_provider,(SipUser)null,call_listener);
		initExtendedCall(null,call_listener);
		this.from_naddr=invite.getToHeader().getNameAddress();
		this.dialog=new ExtendedInviteDialog(sip_provider,invite,this_extended_invite_dialog_listener);
		//this.remote_sdp=invite.getStringBody();
		//changeState(C_INCOMING);
	}


	/** Creates a new Call for a callee, based on an already received INVITE request.
	  * @param sip_provider the SIP provider
	  * @param invite the received INVITE message
	  * @param callee the local called user
	  * @param call_listener the call listener */
	public ExtendedCall(SipProvider sip_provider, SipMessage invite, SipUser callee, ExtendedCallListener call_listener) {
		super(sip_provider,callee,call_listener);
		initExtendedCall(callee,call_listener);
		this.from_naddr=invite.getToHeader().getNameAddress();
		this.dialog=new ExtendedInviteDialog(sip_provider,invite,callee.getAuhUserName(),callee.getAuhRealm(),callee.getAuhPasswd(),this_extended_invite_dialog_listener);
		//this.remote_sdp=invite.getStringBody();
		//changeState(C_INCOMING);
	}


	/** Inits the ExtendedCall. */
	private void initExtendedCall(SipUser user, ExtendedCallListener call_listener) {
		this.xcall_listener=call_listener;
		if (user!=null) {
			this.username=user.getAuhUserName();
			this.realm=user.getAuhRealm();
			this.passwd=user.getAuhPasswd();
			this.next_nonce=null;
		}
		this.qop=null;
		changeState(CallState.C_IDLE);
		
		this_extended_invite_dialog_listener=new ThisExtendedInviteDialogListener(this);
	}


	/** Waits for an incoming call. */
	public void listen() {
		if (username!=null) dialog=new ExtendedInviteDialog(sip_provider,username,realm,passwd,this_extended_invite_dialog_listener);
		else dialog=new ExtendedInviteDialog(sip_provider,this_extended_invite_dialog_listener);
		dialog.listen();
		changeState(CallState.C_IDLE);
	}


	/** Starts a new call, inviting a remote user (<i>callee</i>).
	  * @param callee the callee address
	  * @param caller the caller address
	  * @param sdp the session descriptor */
	public void call(NameAddress callee, NameAddress caller, String sdp) {
		log(LogLevel.DEBUG,"calling "+callee);
		if (username!=null) dialog=new ExtendedInviteDialog(sip_provider,username,realm,passwd,this_extended_invite_dialog_listener);
		else dialog=new ExtendedInviteDialog(sip_provider,this_extended_invite_dialog_listener);
		if (caller==null) caller=from_naddr;
		if (sdp!=null) local_sdp=sdp;
		NameAddress caller_contact=getContactAddress(SipNameAddress.isSIPS(callee));
		if (local_sdp!=null) dialog.invite(callee,caller,caller_contact,local_sdp);
		else dialog.inviteWithoutOffer(callee,caller,caller_contact);
		changeState(CallState.C_OUTGOING);
	} 


	/** Starts a new call, with the given <i>INVITE</i> request.
	  * @param invite the INVITE request message */
	public void call(SipMessage invite) {
		log(LogLevel.DEBUG,"calling "+invite.getRequestLine().getAddress());
		if (username!=null) dialog=new ExtendedInviteDialog(sip_provider,username,realm,passwd,this_extended_invite_dialog_listener);
		else dialog=new ExtendedInviteDialog(sip_provider,this_extended_invite_dialog_listener);
		local_sdp=invite.getStringBody();
		if (local_sdp!=null)
			dialog.invite(invite);
		else dialog.inviteWithoutOffer(invite);
		changeState(CallState.C_OUTGOING);
	} 
	
	
	/** Requests a call transfer. */
	public void transfer(NameAddress transfer_to) {
		((ExtendedInviteDialog)dialog).refer(transfer_to);
	}


	/** Requests an attended call transfer, replacing an existing call */
	public void attendedTransfer(NameAddress transfer_to, Call replaced_call) {
		((ExtendedInviteDialog)dialog).refer(transfer_to,from_naddr,replaced_call.dialog);
	}


	/** Accepts a call transfer request. */
	public void acceptTransfer() {
		((ExtendedInviteDialog)dialog).acceptRefer(refer);
	}


	/** Refuses a call transfer request. */
	public void refuseTransfer() {
		((ExtendedInviteDialog)dialog).refuseRefer(refer);
	}


	/** Notifies about the satus of an other call (the given response belongs to). */
	public void notify(SipMessage resp) {
		if (resp.isResponse()) {
			StatusLine status_line=resp.getStatusLine();
			int code=status_line.getCode();
			String reason=status_line.getReason();
			((ExtendedInviteDialog)dialog).notify(code,reason);
		}
	}


	/** Notifies about the satus of an other call. */
	public void notify(int code, String reason) {
		((ExtendedInviteDialog)dialog).notify(code,reason);
	}


	// ************************ Callback methods ***********************

	/** From ExtendedInviteDialogListener. When an incoming REFER request is received within the dialog */ 
	private void processDlgRefer(org.mjsip.sip.dialog.InviteDialog d, NameAddress refer_to, NameAddress referred_by, SipMessage msg) {
		if (d!=dialog) {  log(LogLevel.INFO,"NOT the current dialog");  return;  }
		log(LogLevel.TRACE,"onDlgRefer("+refer_to.toString()+")");       
		refer=msg;
		if (xcall_listener!=null) {
			String replcall_id=null;
			if (msg.hasReplacesHeader()) replcall_id=msg.getReplacesHeader().getCallId();
			if (replcall_id==null) xcall_listener.onCallTransfer(this,refer_to,referred_by,msg);
			else xcall_listener.onCallAttendedTransfer(this,refer_to,referred_by,replcall_id,msg);
		}
	}

	/** From ExtendedInviteDialogListener. When a response is received for a REFER request within the dialog */ 
	private void processDlgReferResponse(org.mjsip.sip.dialog.InviteDialog d, int code, String reason, SipMessage msg) {
		if (d!=dialog) {  log(LogLevel.INFO,"NOT the current dialog");  return;  }
		log(LogLevel.TRACE,"onDlgReferResponse("+code+" "+reason+")");       
		if (code>=200 && code <300) {
			if(xcall_listener!=null) xcall_listener.onCallTransferAccepted(this,msg);
		}
		else
		if (code>=300) {
			if(xcall_listener!=null) xcall_listener.onCallTransferRefused(this,reason,msg);
		}
	}

	/** From ExtendedInviteDialogListener. When an incoming NOTIFY request is received within the dialog */ 
	private void processDlgNotify(org.mjsip.sip.dialog.InviteDialog d, String event, String sipfragment, SipMessage msg) {
		if (d!=dialog) {  log(LogLevel.INFO,"NOT the current dialog");  return;  }
		log(LogLevel.TRACE,"onDlgNotify()");
		if (event.equals("refer")) {
			SipMessage fragment=new SipMessage(sipfragment);
			log(LogLevel.INFO,"Notify: "+sipfragment);
			if (fragment.isResponse()) {
				StatusLine status_line=fragment.getStatusLine();
				int code=status_line.getCode();
				String reason=status_line.getReason();
				if (code>=200 && code<300) {
					log(LogLevel.DEBUG,"Call successfully transferred");
					if(xcall_listener!=null) xcall_listener.onCallTransferSuccess(this,msg);
				}
				else
				if (code>=300) {
					log(LogLevel.DEBUG,"Call NOT transferred");
					if(xcall_listener!=null) xcall_listener.onCallTransferFailure(this,reason,msg);
				}            
			}
		}
	}

	/** From ExtendedInviteDialogListener. When an incoming request is received within the dialog
	  * different from INVITE, CANCEL, ACK, BYE */ 
	private void processDlgAltRequest(org.mjsip.sip.dialog.InviteDialog d, String method, String body, SipMessage msg) {
		
	}

	/** From ExtendedInviteDialogListener. When a response is received for a request within the dialog 
	  * different from INVITE, CANCEL, ACK, BYE */ 
	private void processDlgAltResponse(org.mjsip.sip.dialog.InviteDialog d, String method, int code, String reason, String body, SipMessage msg) {
		
	}

	
	// ************************* Inner classes *************************

	/** This ExtendedInviteDialogListener.
	  */ 
	protected class ThisExtendedInviteDialogListener extends ThisInviteDialogListener implements ExtendedInviteDialogListener {
		
		ExtendedCall c;
		
		public ThisExtendedInviteDialogListener(ExtendedCall c) {
			super(c);
			this.c=c;
		}

		public void onDlgRefer(InviteDialog dialog, NameAddress refer_to, NameAddress referred_by, SipMessage msg) {
			c.processDlgRefer(dialog,refer_to,referred_by,msg);
		}
		public void onDlgReferResponse(InviteDialog dialog, int code, String reason, SipMessage msg) {
			c.processDlgReferResponse(dialog,code,reason,msg);
		}
		public void onDlgNotify(InviteDialog dialog, String event, String sipfragment, SipMessage msg) {
			c.processDlgNotify(dialog,event,sipfragment,msg);
		}
		public void onDlgAltRequest(InviteDialog dialog, String method, String body, SipMessage msg) {
			c.processDlgAltRequest(dialog,method,body,msg);
		}
		public void onDlgAltResponse(InviteDialog dialog, String method, int code, String reason, String body, SipMessage msg) {
			c.processDlgAltResponse(dialog,method,code,reason,body,msg);
		}
		
	}



	// ****************************** Logs *****************************

	/** Adds a new string to the default log. */
	protected void log(LogLevel level, String str) {
		if (logger!=null) logger.log(level,"ExtendedCall: "+str);  
	}
}

