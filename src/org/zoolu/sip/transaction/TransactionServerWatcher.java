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


/** TransactionServerWatcher listens for new incoming transaction requests.
  */
public class TransactionServerWatcher implements SipProviderListener
{  
   /** TransactionServerWatcherListener that captures new transactions. */
   TransactionServerWatcherListener listener;

   /** SipProvider that receives incoming transaction requests..*/
   SipProvider sip_provider;
 
   /** Method name. */
   String method;


   /** Creates a new TransactionServerWatcher of type <i>method</i>,
     * and starts listening for incoming trasaction requests. */
   public TransactionServerWatcher(SipProvider sip_provider, String method, TransactionServerWatcherListener listener)
   {  this.listener=listener;
      this.method=method;
      sip_provider.addSelectiveListener(new MethodId(method),this); 
      //printLog("method: "+method,Log.LEVEL_HIGH);
      //printLog("created",Log.LEVEL_HIGH);
   }


   /** Method derived from interface SipListener. */
   public void onReceivedMessage(SipProvider provider, Message msg)
   {  if (msg.isRequest())
      {  if (listener!=null) listener.onNewTransactionServer(this,new TransactionServer(sip_provider,msg,null/*listener*/));
      }
   }


   /** Stops listening for incoming trasaction requests. */
   public void halt()
   {  sip_provider.removeSelectiveListener(new MethodId(method));
   }


   //**************************** Logs ****************************/

   /** Adds a new string to the default Log */
   //protected void printLog(String str, int level)
   //{  if (log!=null) log.println("TransactionServerWatcher("+method+"): "+str,level+SipStack.LOG_LEVEL_TRANSACTION);  
   //}

}

