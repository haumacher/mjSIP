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

package org.zoolu.sip.call;


import org.zoolu.sip.dialog.*;
import org.zoolu.sip.provider.*;
import org.zoolu.sip.message.*;
import org.zoolu.sip.address.NameAddress;
import org.zoolu.sip.header.MultipleHeader;
import org.zoolu.tools.Log;
import org.zoolu.sdp.*;
import java.util.Vector;


/** Class Call implements SIP calls.
  * <p/>
  * The Call layer (class) provides a simplified interface
  * to the functionalities implemented by the InviteDialog layer (class).
  * <p/>
  * It handles both outgoing or incoming calls.
  * <p/>
  * Both offer/answer models are supported, that is: <br/>
  * i) offer/answer in invite/2xx, or <br/>
  * ii) offer/answer in 2xx/ack
  */
public class Call extends org.zoolu.tools.MonitoredObject implements InviteDialogListener
{  
   /** Log */
   Log log;

   /** The SipProvider used for the call */
   protected SipProvider sip_provider;
   
   /** The invite dialog (sip.dialog.InviteDialog) */
   protected InviteDialog dialog;
   
   /** The user url (AOR) */
   protected NameAddress from_naddr;

   /** The user contact url */
   protected NameAddress contact_naddr;

   /** The user secure contact url */
   protected NameAddress secure_contact_naddr;

   /** The local sdp */
   protected String local_sdp;

   /** The remote sdp */
   protected String remote_sdp;
   
   /** The call listener */
   CallListener listener;

   
   /** Internal call state. */
   protected int status;
   
   protected static final int C_IDLE=0;   
   protected static final int C_INCOMING=1;
   protected static final int C_OUTGOING=2;
   protected static final int C_ACTIVE=6;
   protected static final int C_CLOSED=9;
      
   /** Gets the dialog state */
   protected String getStatus()
   {  switch (status)
      {  case C_IDLE       : return "C_IDLE";
         case C_INCOMING   : return "C_INCOMING";   
         case C_OUTGOING   : return "C_OUTGOING";
         case C_ACTIVE     : return "C_ACTIVE";
         case C_CLOSED     : return "C_CLOSED";   
         default : return null;
      }
   }

   /** Changes the internal dialog state */
   protected void changeStatus(int newstatus)
   {  status=newstatus;
      printLog("changed call state: "+getStatus(),Log.LEVEL_MEDIUM);
   }

   /** Whether the call state is equal to <i>st</i> */
   protected boolean statusIs(int st)
   {  return status==st;
   }


   /** Whether the call is in "idle" state. */
   public boolean isIdle()
   {  return statusIs(C_IDLE);
   }

   /** Whether the call is in "incoming" (called) state. */
   public boolean isIncoming()
   {  return statusIs(C_INCOMING);
   }

   /** Whether the call is in "outgoing" (calling) state. */
   public boolean isOutgoing()
   {  return statusIs(C_OUTGOING);
   }

   /** Whether the call is in "active" (call) state. */
   public boolean isActive()
   {  return statusIs(C_ACTIVE);
   }

   /** Whether the call is in "closed" state. */
   public boolean isClosed()
   {  return statusIs(C_CLOSED);
   }


   /** Creates a new Call. */
   public Call(SipProvider sip_provider, NameAddress from_naddr, CallListener call_listener)
   {  initCall(sip_provider,from_naddr,call_listener);
   }

   /** Creates a new Call for the already received INVITE request <i>invite</i>. */
   public Call(SipProvider sip_provider, Message invite, CallListener call_listener)
   {  initCall(sip_provider,invite.getToHeader().getNameAddress(),call_listener);
      this.dialog=new InviteDialog(sip_provider,invite,this);
      this.remote_sdp=invite.getBody();
      changeStatus(C_INCOMING);
   }

   /** Inits the Call. */
   private void initCall(SipProvider sip_provider, NameAddress from_naddr, CallListener call_listener)
   {  this.sip_provider=sip_provider;
      this.log=sip_provider.getLog();
      this.listener=call_listener;
      this.from_naddr=from_naddr;
      String user=(from_naddr!=null)? from_naddr.getAddress().getUserName() : null;
      this.contact_naddr=new NameAddress(sip_provider.getContactAddress(user));
      this.secure_contact_naddr=new NameAddress(sip_provider.getSecureContactAddress(user));
      this.dialog=null;
      this.local_sdp=null;
      this.remote_sdp=null;
      changeStatus(C_IDLE);
   }

   /** Waits for an incoming call */
   public void listen()
   {  dialog=new InviteDialog(sip_provider,this);
      dialog.listen();
      changeStatus(C_IDLE);
   }

   /** Gets the current invite dialog */
   /*public InviteDialog getInviteDialog()
   {  return dialog;
   }*/

   /** Gets the current call-id */
   public String getCallId()
   {  if (dialog==null) return null;
      else return dialog.getCallID();
   }

   /** Gets the current local session descriptor */
   public String getLocalSessionDescriptor()
   {  return local_sdp;
   }
   
   /** Sets a new local session descriptor */
   public void setLocalSessionDescriptor(String sdp)
   {  local_sdp=sdp;
   }

   /** Gets the current remote session descriptor */
   public String getRemoteSessionDescriptor()
   {  return remote_sdp;
   }
   
   /** Whether the call is on (active). */
   /*public boolean isOnCall()
   {  return dialog.isSessionActive();
   }*/
         
   /** Starts a new call, inviting a remote user (<i>callee</i>) */
   public void call(NameAddress callee)
   {  call(callee,null,null);
   }

   /** Starts a new call, inviting a remote user (<i>callee</i>) */
   public void call(NameAddress callee, String sdp)
   {  call(callee,null,sdp);
   }

   /** Starts a new call, inviting a remote user (<i>callee</i>) */
   public void call(NameAddress callee, NameAddress from, String sdp)
   {  printLog("calling "+callee,Log.LEVEL_MEDIUM);
      dialog=new InviteDialog(sip_provider,this);
      if (from==null) from=from_naddr;
      if (sdp!=null) local_sdp=sdp;
      NameAddress contact=(callee.getAddress().isSecure())? secure_contact_naddr : contact_naddr;
      if (local_sdp!=null) dialog.invite(callee,from,contact,local_sdp);
      else dialog.inviteWithoutOffer(callee,from,contact);
      changeStatus(C_OUTGOING);
   }

   /** Starts a new call with the <i>invite</i> message request */
   public void call(Message invite)
   {  printLog("calling "+invite.getRequestLine().getAddress(),Log.LEVEL_MEDIUM);
      dialog=new InviteDialog(sip_provider,this);
      local_sdp=invite.getBody();
      if (local_sdp!=null) dialog.invite(invite);
      else dialog.inviteWithoutOffer(invite);
      changeStatus(C_OUTGOING);
   }

   /** Answers at the 2xx/offer (in the ack message) */
   public void ackWithAnswer(String sdp)
   {  local_sdp=sdp;
      dialog.ackWithAnswer(contact_naddr,sdp);
   }

   /** Rings back for the incoming call */
   public void ring()
   {  if (dialog!=null) dialog.ring();
   }

   /** Respond to a incoming call (invite) with <i>resp</i> */
   /*public void respond(Message resp)
   {  if (dialog!=null) dialog.respond(resp);
   }*/

   /** Accepts the incoming call */
   /*public void accept()
   {  accept(local_sdp);
   }*/    

   /** Accepts the incoming call */
   public void accept(String sdp)
   {  local_sdp=sdp;
      if (dialog!=null)
      {  if (dialog.isSecure()) dialog.accept(secure_contact_naddr,local_sdp);
         else dialog.accept(contact_naddr,local_sdp);
      }
      changeStatus(C_ACTIVE);
   }

   /** Redirects the incoming call */
   public void redirect(NameAddress redirect_url)
   {  if (dialog!=null) dialog.redirect(302,"Moved Temporarily",redirect_url);
      changeStatus(C_CLOSED);
   }

   /** Refuses the incoming call */
   public void refuse()
   {  if (dialog!=null) dialog.refuse();
      changeStatus(C_CLOSED);
   }

   /** Cancels the outgoing call */
   /*public void cancel()
   {  if (dialog!=null) dialog.cancel();
   }*/

   /** Close the ongoing call */
   /*public void bye()
   {  if (dialog!=null) dialog.bye();
   }*/

   /** Modifies the current call */
   public void modify(String sdp)
   {  local_sdp=sdp;
      if (dialog!=null) dialog.reInvite(dialog.getLocalContact(),local_sdp);
   }

   /** Closes an ongoing or incoming/outgoing call.
     * It sends a 403 "Forbidden" response, or a CANCEL or BYE request, depending on the call state. */
   public void hangup()
   {  if (dialog!=null)
      {  // try dialog.refuse(), cancel(), and bye() methods..
         //dialog.refuse();
         //dialog.cancel();
         //dialog.bye();
         if (isIdle()) dialog.cancel();
         else
         if (isIncoming()) dialog.refuse();
         else
         if (isOutgoing()) dialog.cancel();
         else
         if (isActive()) dialog.bye();
         else
         if (isClosed()) dialog.bye();
         
         changeStatus(C_CLOSED);
      }
   }


   // ************** Inherited from InviteDialogListener **************

   /** Inherited from class InviteDialogListener and called by an InviteDialag. Normally you should not use it. Use specific callback methods instead (e.g. onCallInvite()). */ 
   public void onDlgInvite(InviteDialog d, NameAddress callee, NameAddress caller, String sdp, Message msg)
   {  if (d!=dialog) {  printLog("NOT the current dialog",Log.LEVEL_HIGH);  return;  }
      changeStatus(C_INCOMING);
      if (sdp!=null && sdp.length()!=0) remote_sdp=sdp;
      if (listener!=null) listener.onCallInvite(this,callee,caller,sdp,msg);
   }

   /** Inherited from class InviteDialogListener and called by an InviteDialag. Normally you should not use it. Use specific callback methods instead (e.g. onCallModify()). */ 
   public void onDlgReInvite(InviteDialog d, String sdp, Message msg)
   {  if (d!=dialog) {  printLog("NOT the current dialog",Log.LEVEL_HIGH);  return;  }
      if (sdp!=null && sdp.length()!=0) remote_sdp=sdp;
      if (listener!=null) listener.onCallModify(this,sdp,msg);
   }

   /** Inherited from class InviteDialogListener and called by an InviteDialag. Normally you should not use it. Use specific callback methods instead (e.g. onCallRinging()). */ 
   public void onDlgInviteProvisionalResponse(InviteDialog d, int code, String reason, String sdp, Message msg)
   {  if (d!=dialog) {  printLog("NOT the current dialog",Log.LEVEL_HIGH);  return;  }
      if (sdp!=null && sdp.length()!=0) remote_sdp=sdp;
      if (code==183) if (listener!=null) listener.onCallProgress(this,msg);
      if (code==180) if (listener!=null) listener.onCallRinging(this,msg);
   }

   /** Inherited from class InviteDialogListener and called by an InviteDialag. Normally you should not use it. Use specific callback methods instead (e.g. onCallAccepted()). */ 
   public void onDlgInviteSuccessResponse(InviteDialog d, int code, String reason, String sdp, Message msg)
   {  if (d!=dialog) {  printLog("NOT the current dialog",Log.LEVEL_HIGH);  return;  }
      // check if the call has been already cancelled or closed
      if (isClosed())
      {  printLog("call already closed",Log.LEVEL_HIGH);
         dialog.bye();
         return;
      } 
      // else
      changeStatus(C_ACTIVE);
      if (sdp!=null && sdp.length()!=0) remote_sdp=sdp;
      if (listener!=null) listener.onCallAccepted(this,sdp,msg);
   }
   
   /** Inherited from class InviteDialogListener and called by an InviteDialag. Normally you should not use it. Use specific callback methods instead (e.g. onCallRedirected()). */ 
   public void onDlgInviteRedirectResponse(InviteDialog d, int code, String reason, MultipleHeader contacts, Message msg)
   {  if (d!=dialog) {  printLog("NOT the current dialog",Log.LEVEL_HIGH);  return;  }
      changeStatus(C_CLOSED);
      if (listener!=null) listener.onCallRedirected(this,reason,contacts.getValues(),msg);
   }
   
   /** Inherited from class InviteDialogListener and called by an InviteDialag. Normally you should not use it. Use specific callback methods instead (e.g. onCallRefused()). */ 
   public void onDlgInviteFailureResponse(InviteDialog d, int code, String reason, Message msg)
   {  if (d!=dialog) {  printLog("NOT the current dialog",Log.LEVEL_HIGH);  return;  }
      changeStatus(C_CLOSED);
      if (listener!=null) listener.onCallRefused(this,reason,msg);
   }
   
   /** Inherited from class InviteDialogListener and called by an InviteDialag. Normally you should not use it. Use specific callback methods instead (e.g. onCallTimeout()). */ 
   public void onDlgTimeout(InviteDialog d)
   {  if (d!=dialog) {  printLog("NOT the current dialog",Log.LEVEL_HIGH);  return;  }
      changeStatus(C_CLOSED);
      if (listener!=null) listener.onCallTimeout(this);
   }

   /** Inherited from class InviteDialogListener and called by an InviteDialag. Normally you should not use it. */ 
   public void onDlgReInviteProvisionalResponse(InviteDialog d, int code, String reason, String sdp, Message msg)
   {  if (d!=dialog) {  printLog("NOT the current dialog",Log.LEVEL_HIGH);  return;  }
      if (sdp!=null && sdp.length()!=0) remote_sdp=sdp;
   }

   /** Inherited from class InviteDialogListener and called by an InviteDialag. Normally you should not use it. Use specific callback methods instead (e.g. onCallReInviteAccepted()). */ 
   public void onDlgReInviteSuccessResponse(InviteDialog d, int code, String reason, String sdp, Message msg)
   {  if (d!=dialog) {  printLog("NOT the current dialog",Log.LEVEL_HIGH);  return;  }
      if (sdp!=null && sdp.length()!=0) remote_sdp=sdp;
      if (listener!=null) listener.onCallReInviteAccepted(this,sdp,msg);
   }

   /** Inherited from class InviteDialogListener and called by an InviteDialag. Normally you should not use it. Use specific callback methods instead (e.g. onCallReInviteRedirection()). */ 
   //public void onDlgReInviteRedirectResponse(InviteDialog d, int code, String reason, MultipleHeader contacts, Message msg)
   //{  if (d!=dialog) {  printLog("NOT the current dialog",Log.LEVEL_HIGH);  return;  }
   //   if (listener!=null) listener.onCallReInviteRedirection(this,reason,contacts.getValues(),msg);
   //}

   /** Inherited from class InviteDialogListener and called by an InviteDialag. Normally you should not use it. Use specific callback methods instead (e.g. onCallReInviteRefused()). */ 
   public void onDlgReInviteFailureResponse(InviteDialog d, int code, String reason, Message msg)
   {  if (d!=dialog) {  printLog("NOT the current dialog",Log.LEVEL_HIGH);  return;  }
      changeStatus(C_CLOSED);
      if (listener!=null) listener.onCallReInviteRefused(this,reason,msg);
   }

   /** Inherited from class InviteDialogListener and called by an InviteDialag. Normally you should not use it. Use specific callback methods instead (e.g. onCallReInviteTimeout()). */ 
   public void onDlgReInviteTimeout(InviteDialog d)
   {  if (d!=dialog) {  printLog("NOT the current dialog",Log.LEVEL_HIGH);  return;  }
      if (listener!=null) listener.onCallReInviteTimeout(this);
   }

   /** Inherited from class InviteDialogListener and called by an InviteDialag. Normally you should not use it. Use specific callback methods instead (e.g. onCallConfirmed()). */ 
   public void onDlgAck(InviteDialog d, String sdp, Message msg)
   {  if (d!=dialog) {  printLog("NOT the current dialog",Log.LEVEL_HIGH);  return;  }
      if (sdp!=null && sdp.length()!=0) remote_sdp=sdp;
      if (listener!=null) listener.onCallConfirmed(this,sdp,msg);
   }
   
   /** Inherited from class InviteDialogListener and called by an InviteDialag. Normally you should not use it. Use specific callback methods instead (e.g. onCallBye()). */ 
   public void onDlgCancel(InviteDialog d, Message msg)
   {  if (d!=dialog) {  printLog("NOT the current dialog",Log.LEVEL_HIGH);  return;  }
      changeStatus(C_CLOSED);
      if (listener!=null) listener.onCallCancel(this,msg);
   }

   /** Inherited from class InviteDialogListener and called by an InviteDialag. Normally you should not use it. Use specific callback methods instead (e.g. onClosing()). */ 
   public void onDlgBye(InviteDialog d, Message msg)      
   {  if (d!=dialog) {  printLog("NOT the current dialog",Log.LEVEL_HIGH);  return;  }
      changeStatus(C_CLOSED);
      if (listener!=null) listener.onCallBye(this,msg);
   }
   
   /** Inherited from class InviteDialogListener and called by an InviteDialag. Normally you should not use it. Use specific callback methods instead (e.g. onClosed()). */ 
   public void onDlgByeFailureResponse(InviteDialog d, int code, String reason, Message msg)
   {  if (d!=dialog) {  printLog("NOT the current dialog",Log.LEVEL_HIGH);  return;  }
      if (listener!=null) listener.onCallClosed(this,msg);
   }

   /** Inherited from class InviteDialogListener and called by an InviteDialag. Normally you should not use it. Use specific callback methods instead (e.g. onClosed()). */ 
   public void onDlgByeSuccessResponse(InviteDialog d, int code, String reason, Message msg)
   {  if (d!=dialog) {  printLog("NOT the current dialog",Log.LEVEL_HIGH);  return;  }
      if (listener!=null) listener.onCallClosed(this,msg);
   }
 
   /** When an incoming INVITE is accepted. */ 
   //public void onDlgAccepted(InviteDialog dialog) {}

   /** When an incoming INVITE is refused. */ 
   //public void onDlgRefused(InviteDialog dialog) {}

   /** When the INVITE handshake is successful terminated  and the call is active. */ 
   public void onDlgCall(InviteDialog dialog) {}

   /** When an incoming Re-INVITE is accepted. */ 
   //public void onDlgReInviteAccepted(InviteDialog dialog) {}

   /** When an incoming Re-INVITE is refused. */ 
   //public void onDlgReInviteRefused(InviteDialog dialog) {}

   /** When a BYE request traqnsaction has been started. */ 
   //public void onDlgByeing(InviteDialog dialog) {}

   /** When the dialog is finally closed (after receiving a BYE request, a BYE response, or after BYE timeout). */ 
   public void onDlgClosed(InviteDialog dialog) {}


   //**************************** Logs ****************************/

   /** Default log level offset */
   static final int LOG_OFFSET=1;
   
   /** Adds a new string to the default Log. */
   protected void printLog(String str, int level)
   {  if (log!=null) log.println("Call: "+str,Call.LOG_OFFSET+level);  
   }
}

