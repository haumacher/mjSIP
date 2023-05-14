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

package org.zoolu.sip.transaction;


import org.zoolu.sip.provider.*;
import org.zoolu.sip.message.*;
//import org.zoolu.tools.LogLevel;


/** InviteTransactionServerWatcher listens for new incoming invite transaction requests.
  */
public class InviteTransactionServerWatcher extends TransactionServerWatcher
{  
   /** InviteTransactionServerWatcherListener that captures new invite transactions. */
   InviteTransactionServerWatcherListener listener;


   /** Creates a new InviteTransactionServerWatcher of type <i>method</i>,
     * and starts listening for incoming trasaction requests. */
   public InviteTransactionServerWatcher(SipProvider sip_provider, InviteTransactionServerWatcherListener listener)
   {  super(sip_provider,SipMethods.INVITE,null);
      this.listener=listener;
   }


   /** Method derived from interface SipListener. */
   public void onReceivedMessage(SipProvider provider, Message msg)
   {  if (msg.isRequest() && msg.isInvite())
      {  if (listener!=null) listener.onNewInviteTransactionServer(this,new InviteTransactionServer(sip_provider,msg,listener));
      }
   }

}

