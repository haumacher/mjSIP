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


import org.zoolu.sip.provider.*;
import org.zoolu.sip.message.*;
import org.zoolu.sip.dialog.*;
import org.zoolu.sip.header.StatusLine;
import org.zoolu.sip.address.*;
import org.zoolu.tools.Log;
import org.zoolu.sdp.*;
import java.util.Vector;


/** Class ExtendedCall implements a SIP call.
  * <p/>
  * ExtendedCall extends basic Call in order to:
  * <br>- support call transfer (REFER/NOTIFY methods),
  * <br>- support UAS and proxy authentication.
  */
public class ExtendedCall extends Call implements ExtendedInviteDialogListener
{  

   ExtendedCallListener xcall_listener;

   Message refer;
   
   
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


   /** Creates a new ExtendedCall. */
   public ExtendedCall(SipProvider sip_provider, NameAddress from_naddr, ExtendedCallListener call_listener)
   {  super(sip_provider,from_naddr,call_listener);
      initExtendedCall(null,null,null,call_listener);
   }


   /** Creates a new ExtendedCall for the already received INVITE request <i>invite</i>. */
   public ExtendedCall(SipProvider sip_provider, Message invite, ExtendedCallListener call_listener)
   {  super(sip_provider,invite.getToHeader().getNameAddress(),call_listener);
      initExtendedCall(null,null,null,call_listener);
      this.dialog=new ExtendedInviteDialog(sip_provider,invite,this);
      this.remote_sdp=invite.getBody();
      changeStatus(C_INCOMING);
   }


   /** Creates a new ExtendedCall. */
   public ExtendedCall(SipProvider sip_provider, NameAddress from_naddr, String username, String realm, String passwd, ExtendedCallListener call_listener)
   {  super(sip_provider,from_naddr,call_listener);
      initExtendedCall(username,realm,passwd,call_listener);
   }


   /** Creates a new ExtendedCall for the already received INVITE request <i>invite</i>. */
   public ExtendedCall(SipProvider sip_provider, Message invite, String username, String realm, String passwd, ExtendedCallListener call_listener)
   {  super(sip_provider,invite.getToHeader().getNameAddress(),call_listener);
      initExtendedCall(username,realm,passwd,call_listener);
      this.from_naddr=invite.getToHeader().getNameAddress();
      this.dialog=new ExtendedInviteDialog(sip_provider,invite,username,realm,passwd,this);
      this.remote_sdp=invite.getBody();
      changeStatus(C_INCOMING);
   }


   /** Inits the ExtendedCall. */
   private void initExtendedCall(String username, String realm, String passwd, ExtendedCallListener call_listener)
   {  this.xcall_listener=call_listener;
      this.refer=null;
      this.username=username;
      this.realm=realm;
      this.passwd=passwd;
      this.next_nonce=null;
      this.qop=null;
      changeStatus(C_IDLE);
   }


   /** Waits for an incoming call */
   public void listen()
   {  if (username!=null) dialog=new ExtendedInviteDialog(sip_provider,username,realm,passwd,this);
      else dialog=new ExtendedInviteDialog(sip_provider,this);
      dialog.listen();
      changeStatus(C_IDLE);
   }


   /** Starts a new call, inviting a remote user (<i>r_user</i>) */
   public void call(NameAddress callee, NameAddress from, String sdp)
   {  printLog(" calling "+callee,Log.LEVEL_MEDIUM);
      if (username!=null) dialog=new ExtendedInviteDialog(sip_provider,username,realm,passwd,this);
      else dialog=new ExtendedInviteDialog(sip_provider,this);
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
      if (username!=null) dialog=new ExtendedInviteDialog(sip_provider,username,realm,passwd,this);
      else dialog=new ExtendedInviteDialog(sip_provider,this);
      local_sdp=invite.getBody();
      if (local_sdp!=null)
         dialog.invite(invite);
      else dialog.inviteWithoutOffer(invite);
      changeStatus(C_OUTGOING);
   } 
   
   
   /** Requests a call transfer */
   public void transfer(NameAddress transfer_to)
   {  ((ExtendedInviteDialog)dialog).refer(transfer_to);
   }

   /** Requests an attended call transfer, replacing an existing call */
   public void attendedTransfer(NameAddress transfer_to, Call replaced_call)
   {  ((ExtendedInviteDialog)dialog).refer(transfer_to,from_naddr,replaced_call.dialog);
   }

   /** Accepts a call transfer request */
   public void acceptTransfer()
   {  ((ExtendedInviteDialog)dialog).acceptRefer(refer);
   }

   
   /** Refuses a call transfer request */
   public void refuseTransfer()
   {  ((ExtendedInviteDialog)dialog).refuseRefer(refer);
   }


   /** Notifies about the satus of an other call (the given response belongs to). */
   public void notify(Message resp)
   {  if (resp.isResponse())
      {  StatusLine status_line=resp.getStatusLine();
         int code=status_line.getCode();
         String reason=status_line.getReason();
         ((ExtendedInviteDialog)dialog).notify(code,reason);
      }
   }


   /** Notifies about the satus of an other call */
   public void notify(int code, String reason)
   {  ((ExtendedInviteDialog)dialog).notify(code,reason);
   }


   // ************** Inherited from InviteDialogListener **************


   /** When an incoming REFER request is received within the dialog */ 
   public void onDlgRefer(org.zoolu.sip.dialog.InviteDialog d, NameAddress refer_to, NameAddress referred_by, Message msg)
   {  if (d!=dialog) {  printLog("NOT the current dialog",Log.LEVEL_HIGH);  return;  }
      printLog("onDlgRefer("+refer_to.toString()+")",Log.LEVEL_LOW);       
      refer=msg;
      if (xcall_listener!=null)
      {  String replcall_id=null;
         if (msg.hasReplacesHeader()) replcall_id=msg.getReplacesHeader().getCallId();
         if (replcall_id==null) xcall_listener.onCallTransfer(this,refer_to,referred_by,msg);
         else xcall_listener.onCallAttendedTransfer(this,refer_to,referred_by,replcall_id,msg);
      }
   }

   /** When a response is received for a REFER request within the dialog */ 
   public void onDlgReferResponse(org.zoolu.sip.dialog.InviteDialog d, int code, String reason, Message msg)
   {  if (d!=dialog) {  printLog("NOT the current dialog",Log.LEVEL_HIGH);  return;  }
      printLog("onDlgReferResponse("+code+" "+reason+")",Log.LEVEL_LOW);       
      if (code>=200 && code <300)
      {  if(xcall_listener!=null) xcall_listener.onCallTransferAccepted(this,msg);
      }
      else
      if (code>=300)
      {  if(xcall_listener!=null) xcall_listener.onCallTransferRefused(this,reason,msg);
      }
   }

   /** When an incoming NOTIFY request is received within the dialog */ 
   public void onDlgNotify(org.zoolu.sip.dialog.InviteDialog d, String event, String sipfragment, Message msg)
   {  if (d!=dialog) {  printLog("NOT the current dialog",Log.LEVEL_HIGH);  return;  }
      printLog("onDlgNotify()",Log.LEVEL_LOW);
      if (event.equals("refer"))
      {  Message fragment=new Message(sipfragment);
         printLog("Notify: "+sipfragment,Log.LEVEL_HIGH);
         if (fragment.isResponse())
         {  StatusLine status_line=fragment.getStatusLine();
            int code=status_line.getCode();
            String reason=status_line.getReason();
            if (code>=200 && code<300)
            {  printLog("Call successfully transferred",Log.LEVEL_MEDIUM);
               if(xcall_listener!=null) xcall_listener.onCallTransferSuccess(this,msg);
            }
            else
            if (code>=300)
            {  printLog("Call NOT transferred",Log.LEVEL_MEDIUM);
               if(xcall_listener!=null) xcall_listener.onCallTransferFailure(this,reason,msg);
            }            
         }
      }
   }

   /** When an incoming request is received within the dialog
     * different from INVITE, CANCEL, ACK, BYE */ 
   public void onDlgAltRequest(org.zoolu.sip.dialog.InviteDialog d, String method, String body, Message msg)
   {
   }

   /** When a response is received for a request within the dialog 
     * different from INVITE, CANCEL, ACK, BYE */ 
   public void onDlgAltResponse(org.zoolu.sip.dialog.InviteDialog d, String method, int code, String reason, String body, Message msg)
   {
   }


   //**************************** Logs ****************************/

   /** Adds a new string to the default Log */
   protected void printLog(String str, int level)
   {  if (log!=null) log.println("ExtendedCall: "+str,Call.LOG_OFFSET+level);  
   }
}

