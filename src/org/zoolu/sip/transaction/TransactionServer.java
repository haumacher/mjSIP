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


import org.zoolu.tools.Timer;
import org.zoolu.tools.TimerListener;
import org.zoolu.sip.address.SipURL;
import org.zoolu.sip.provider.*;
import org.zoolu.sip.message.*;
import org.zoolu.tools.Log;


/** Generic server transaction as defined in RFC 3261 (Section 17.2.2).
  * A TransactionServer is responsable to create a new SIP transaction that starts with a request message received by the SipProvider and ends sending a final response.<BR>
  * The changes of the internal status and the received messages are fired to the TransactionListener passed to the TransactionServer object.<BR>
  * When costructing a new TransactionServer, the transaction type is passed as String parameter to the costructor (e.g. "CANCEL", "BYE", etc..)
  */
 
public class TransactionServer extends Transaction
{  
   /** the TransactionServerListener that captures the events fired by the TransactionServer */
   TransactionServerListener transaction_listener;

   /** last response message */
   Message response;
   
   /** clearing timeout ("Timer J" in RFC 3261) */
   Timer clearing_to;


   // ************************** Costructors **************************

   /** Creates a new TransactionServer. */
   protected TransactionServer(SipProvider sip_provider)
   {  super(sip_provider);
      transaction_listener=null;
      response=null;
   } 

   /** Creates a new TransactionServer of type <i>method</i>. */
   public TransactionServer(SipProvider sip_provider, String method, TransactionServerListener listener)
   {  super(sip_provider);
      init(listener,new TransactionId(method),null);
   }  

   /** Creates a new TransactionServer for the already received request <i>req</i>. */
   public TransactionServer(SipProvider provider, Message req, TransactionServerListener listener)
   {  super(provider);
      request=new Message(req);
      init(listener,request.getTransactionServerId(),request.getTransportConnId());
      
      printLog("start",Log.LEVEL_LOW);
      changeStatus(STATE_TRYING);
      sip_provider.addSelectiveListener(transaction_id,this); 
   }  

   /** Initializes it. */
   protected void init(TransactionServerListener listener, TransactionId transaction_id, TransportConnId connection_id)
   {  this.transaction_listener=listener;
      this.transaction_id=transaction_id;
      this.connection_id=connection_id;
      this.response=null;
      // init the timer just to set the timeout value and label, without listener (never started)
      clearing_to=new Timer(SipStack.transaction_timeout,"Clearing",null);
      printLog("new transaction-id: "+transaction_id.toString(),Log.LEVEL_HIGH);
   }  


   // ************************ Public methods *************************

   /** Starts the TransactionServer. */
   public void listen()
   {  if (statusIs(STATE_IDLE))
      {  printLog("start",Log.LEVEL_LOW);
         changeStatus(STATE_WAITING);  
         sip_provider.addSelectiveListener(transaction_id,this); 
      }
   }  

   /** Sends a response message */
   public void respondWith(int code)
   {  Message resp=MessageFactory.createResponse(request,code,null,null);
      respondWith(resp);
   }  

   /** Sends a response message */
   public void respondWith(Message resp)
   {  response=resp;
      if (statusIs(STATE_TRYING) || statusIs(STATE_PROCEEDING))
      {  sip_provider.sendMessage(response,connection_id);
         int code=response.getStatusLine().getCode();
         if (code>=100 && code<200 && statusIs(STATE_TRYING))
         {  changeStatus(STATE_PROCEEDING);
         }
         if (code>=200 && code<700)
         {  changeStatus(STATE_COMPLETED);
            if (connection_id==null)
            {  clearing_to=new Timer(clearing_to.getTime(),clearing_to.getLabel(),this);
               clearing_to.start();
            }
            else
            {  printLog("clearing_to=0 for reliable transport",Log.LEVEL_LOW);
               onTimeout(clearing_to);
            }
         }
      }
   }  

   /** Method derived from interface SipListener.
     * It's fired from the SipProvider when a new message is received for to the present TransactionServer. */
   public void onReceivedMessage(SipProvider provider, Message msg)
   {  if (msg.isRequest())
      {  if (statusIs(STATE_WAITING))
         {  request=new Message(msg);
            connection_id=msg.getTransportConnId();
            sip_provider.removeSelectiveListener(transaction_id);
            transaction_id=request.getTransactionServerId();
            sip_provider.addSelectiveListener(transaction_id,this); 
            changeStatus(STATE_TRYING);
            if (transaction_listener!=null) transaction_listener.onTransRequest(this,msg);
            return;
         }
         if (statusIs(STATE_PROCEEDING) || statusIs(STATE_COMPLETED))
         {  // retransmission of the last response
            printLog("response retransmission",Log.LEVEL_LOW);
            sip_provider.sendMessage(response,connection_id);
            return;
         }
      }
   }

   /** Method derived from interface TimerListener.
     * It's fired from an active Timer. */
   public void onTimeout(Timer to)
   {  try
      {  if (to.equals(clearing_to))
         {  printLog("Clearing timeout expired",Log.LEVEL_HIGH);
            doTerminate();
         }
      }
      catch (Exception e)
      {  printException(e,Log.LEVEL_HIGH);
      }
   }   

   /** Terminates the transaction. */
   public void terminate()
   {  doTerminate();
      transaction_listener=null;
   }


   // *********************** Protected methods ***********************

   /** Moves to terminate state. */
   protected void doTerminate()
   {  if (!statusIs(STATE_TERMINATED))
      {  clearing_to.halt();
         //clearing_to=null;
         sip_provider.removeSelectiveListener(transaction_id);
         changeStatus(STATE_TERMINATED);
      }
   }


   // ****************************** Logs *****************************

   /** Adds a new string to the default Log */
   protected void printLog(String str, int level)
   {  if (log!=null) log.println("TransactionServer#"+transaction_sqn+": "+str,Transaction.LOG_OFFSET+level);  
   }

}

