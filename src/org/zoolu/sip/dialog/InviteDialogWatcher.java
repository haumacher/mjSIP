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

package org.zoolu.sip.dialog;


import org.zoolu.sip.provider.*;
import org.zoolu.sip.message.*;


/** InviteDialogWatcher listens for new incoming invite requests that create an InviteDialog.
  */
public class InviteDialogWatcher implements SipProviderListener
{  
   /** InviteDialogWatcherListener that captures new invite dialogs. */
   InviteDialogWatcherListener listener;

   /** SipProvider that receives incoming invite requests. */
   SipProvider sip_provider;


   /** Creates a new InviteDialogWatcher of type <i>method</i>,
     * and starts listening for incoming invite requests. */
   public InviteDialogWatcher(SipProvider sip_provider, InviteDialogWatcherListener listener)
   {  this.listener=listener;
      sip_provider.addSelectiveListener(new MethodId(SipMethods.INVITE),this); 
   }


   /** Method derived from interface SipListener. */
   public void onReceivedMessage(SipProvider provider, Message msg)
   {  if (msg.isRequest() && msg.isInvite())
      {  if (listener!=null) listener.onNewInviteDialog(this,new InviteDialog(sip_provider,msg,listener),msg.getToHeader().getNameAddress(),msg.getFromHeader().getNameAddress(),msg.getBody(),msg);
      }
   }


   /** Stops listening for incoming invite requests. */
   public void halt()
   {  sip_provider.removeSelectiveListener(new MethodId(SipMethods.INVITE));
   }

}
