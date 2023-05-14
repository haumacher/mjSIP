/*
 * Copyright (C) 2006 Luca Veltri - University of Parma - Italy
 * 
 * This source code is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This source code is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this source code; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 * 
 * Author(s):
 * Luca Veltri (luca.veltri@unipr.it)
 */

package org.zoolu.sip.call;


import org.zoolu.sip.provider.*;
import org.zoolu.sip.header.AcceptHeader;
import org.zoolu.sip.header.AllowHeader;
import org.zoolu.sip.transaction.TransactionServer;
import org.zoolu.sip.message.*;
import org.zoolu.tools.Log;


/** Simple UAS that responds to OPTIONS requests.
  */
public class OptionsServer implements SipProviderListener
{           
   /** Log */
   Log log;
   
   /** SipProvider. */
   //SipProvider sip_provider;
   
   /** SipProvider. */
   SipProvider sip_provider;
   
   /** Allow header vaule. */
   String allow; // e.g. "INVITE, ACK, CANCEL, OPTIONS, BYE"
   
   /** Accept header vaule. */
   String accept; // e.g. "application/sdp"


   // *************************** Public Methods **************************

   /** Costructs a new OptionsServer. */
   public OptionsServer(SipProvider sip_provider, String allow, String accept)
   {  this.sip_provider=sip_provider;
      log=sip_provider.getLog();
      this.allow=allow;
      this.accept=accept;
      sip_provider.addSelectiveListener(new MethodId(SipMethods.OPTIONS),this);  
   } 


   /** Stops the OptionsServer */
   public void halt()
   {  if (sip_provider!=null) sip_provider.removeSelectiveListener(new MethodId(SipMethods.OPTIONS));
      sip_provider=null;
      log=null;
   }   


   // ************************* Callback functions ************************

   /** When a new Message is received by the SipProvider. */
   public void onReceivedMessage(SipProvider sip_provider, Message msg)
   {  // respond to OPTIONS request
      if (msg.isRequest() && msg.isOptions())
      {  printLog("responding to a new OPTIONS request");
         Message resp=MessageFactory.createResponse(msg,200,null,null);
         if (allow!=null) resp.setAllowHeader(new AllowHeader(allow));
         if (accept!=null) resp.setAcceptHeader(new AcceptHeader(accept));
         TransactionServer ts=new TransactionServer(sip_provider,msg,null);
         ts.respondWith(resp);
      }
   }


   // ******************************** Logs *******************************

   /** Adds a new string to the default Log */
   void printLog(String str)
   {  printLog(str,Log.LEVEL_HIGH);
   }

   /** Adds a new string to the default Log */
   void printLog(String str, int level)
   {  if (log!=null) log.println("OptionsServer: "+str,Call.LOG_OFFSET+level);  
   }

}
