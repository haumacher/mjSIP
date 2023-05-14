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


import org.zoolu.sip.address.SipURL;
import org.zoolu.sip.provider.*;
import org.zoolu.sip.message.*;
import org.zoolu.tools.Timer;
import org.zoolu.tools.TimerListener;
import org.zoolu.tools.Log;


/** INVITE server transaction as defined in RFC 3261 (Section 17.2.1).
  * <BR> An InviteTransactionServer is responsable to create a new SIP invite
  * transaction that starts with a INVITE message received by the SipProvider
  * and ends sending a final response.
  * <BR> The changes of the internal status and the received messages are fired
  * to the TransactionListener passed to the InviteTransactionServer object.
  * <BR> This implementation of InviteTransactionServer automatically generates
  * a "100 Trying" response when the INVITE message is received
  * (as suggested by RFC3261) 
  */
public class InviteTransactionServer extends TransactionServer
{     
   /** Default behavior for automatically sending 100 Trying on INVITE. */
   //public static boolean AUTO_TRYING=true;


   /** the TransactionServerListener that captures the events fired by the InviteTransactionServer */
   InviteTransactionServerListener transaction_listener;

   /** last response message */
   //Message response=null;
   
   /** retransmission timeout ("Timer G" in RFC 3261) */
   Timer retransmission_to;
   /** end timeout ("Timer H" in RFC 3261) */
   Timer end_to;
   /** clearing timeout ("Timer I" in RFC 3261) */
   //Timer clearing_to; 

   /** Whether automatically sending 100 Trying on INVITE. */
   boolean auto_trying;


   // ************************** Costructors **************************

   /** Creates a new InviteTransactionServer. */
   public InviteTransactionServer(SipProvider sip_provider, InviteTransactionServerListener listener)
   {  super(sip_provider);
      init(listener,new TransactionId(SipMethods.INVITE),null);
   }  
      
   /** Creates a new InviteTransactionServer for the already received INVITE request <i>invite</i>. */
   public InviteTransactionServer(SipProvider sip_provider, Message invite, InviteTransactionServerListener listener)
   {  super(sip_provider);
      request=new Message(invite);
      init(listener,request.getTransactionServerId(),request.getTransportConnId());

      changeStatus(STATE_TRYING);
      sip_provider.addSelectiveListener(transaction_id,this);
      // automatically send "100 Tryng" response and go to STATE_PROCEEDING
      if (auto_trying)
      {  Message trying100=MessageFactory.createResponse(request,100,null,null);
         respondWith(trying100); // this method makes it going automatically to STATE_PROCEEDING
      }
   }  

   /** Creates a new InviteTransactionServer for the already received INVITE request <i>invite</i>. */
   public InviteTransactionServer(SipProvider sip_provider, Message invite, boolean auto_trying, InviteTransactionServerListener listener)
   {  super(sip_provider);
      request=new Message(invite);
      init(listener,request.getTransactionServerId(),request.getTransportConnId());      
      this.auto_trying=auto_trying;

      changeStatus(STATE_TRYING);
      sip_provider.addSelectiveListener(transaction_id,this);
      // automatically send "100 Tryng" response and go to STATE_PROCEEDING
      if (auto_trying)
      {  Message trying100=MessageFactory.createResponse(request,100,null,null);
         respondWith(trying100); // this method makes it going automatically to STATE_PROCEEDING
      }
   }  

   /** Initializes it. */
   void init(InviteTransactionServerListener listener, TransactionId transaction_id, TransportConnId connection_id)
   {  this.transaction_listener=listener;
      this.transaction_id=transaction_id;
      this.connection_id=connection_id;
      auto_trying=SipStack.auto_trying;
      // init the timer just to set the timeout value and label, without listener (never started)
      retransmission_to=new Timer(SipStack.retransmission_timeout,"Retransmission",null);
      end_to=new Timer(SipStack.transaction_timeout,"End",null);
      clearing_to=new Timer(SipStack.clearing_timeout,"Clearing",null);
      printLog("new transaction-id: "+transaction_id.toString(),Log.LEVEL_HIGH);
   }   


   // ************************ Public methods *************************

   /** Whether automatically sending 100 Trying on INVITE. */
   public void setAutoTrying(boolean auto_trying)
   {  this.auto_trying=auto_trying;
   }

   /** Starts the InviteTransactionServer. */
   public void listen()
   {  printLog("start",Log.LEVEL_LOW);
      if (statusIs(STATE_IDLE))
      {  changeStatus(STATE_WAITING);  
         //sip_provider.addSelectiveListener(new TransactionId(SipMethods.INVITE),this); 
         sip_provider.addSelectiveListener(transaction_id,this); 
      }
   }  

   /** Sends a response message */
   public void respondWith(Message resp)
   {  response=resp;
      int code=response.getStatusLine().getCode();
      if (statusIs(STATE_TRYING) || statusIs(STATE_PROCEEDING)) sip_provider.sendMessage(response,connection_id);         
      if (code>=100 && code<200 && statusIs(STATE_TRYING))
      {  changeStatus(STATE_PROCEEDING);
         return;
      }
      if (code>=200 && code<300 && (statusIs(STATE_TRYING) || statusIs(STATE_PROCEEDING)))
      {  doTerminate();
         transaction_listener=null;
         return;
      }
      if (code>=300 && code<700 && (statusIs(STATE_TRYING) || statusIs(STATE_PROCEEDING)))
      {  changeStatus(STATE_COMPLETED);
         // retransmission only in case of unreliable transport 
         if (connection_id==null)
         {  retransmission_to=new Timer(retransmission_to.getTime(),retransmission_to.getLabel(),this);
            retransmission_to.start();
            end_to=new Timer(end_to.getTime(),end_to.getLabel(),this);
            end_to.start();
         }
         else
         {  printLog("No retransmissions for reliable transport ("+connection_id+")",Log.LEVEL_LOW);
            onTimeout(end_to);
         }
      }
   }

   /** Method derived from interface SipListener.
     * It's fired from the SipProvider when a new message is catch for to the present ServerTransaction. */
   public void onReceivedMessage(SipProvider provider, Message msg)
   {  if (msg.isRequest())
      {  String req_method=msg.getRequestLine().getMethod();
         
         // invite received
         if (req_method.equals(SipMethods.INVITE))
         {
            if (statusIs(STATE_WAITING))
            {  request=new Message(msg);
               connection_id=request.getTransportConnId();
               sip_provider.removeSelectiveListener(transaction_id);
               transaction_id=request.getTransactionServerId();
               sip_provider.addSelectiveListener(transaction_id,this); 
               changeStatus(STATE_TRYING);
               // automatically send "100 Tryng" response and go to STATE_PROCEEDING
               if (auto_trying)
               {  Message trying100=MessageFactory.createResponse(request,100,null,null);
                  respondWith(trying100); // this method makes it going automatically to STATE_PROCEEDING
               }
               if (transaction_listener!=null) transaction_listener.onTransRequest(this,msg);
               return;            
            }
            if (statusIs(STATE_PROCEEDING) || statusIs(STATE_COMPLETED))
            {  // retransmission of the last response
               sip_provider.sendMessage(response,connection_id);
               return;
            }
         }
         // ack received
         if (req_method.equals(SipMethods.ACK) && statusIs(STATE_COMPLETED))
         {  retransmission_to.halt();
            end_to.halt();
            changeStatus(STATE_CONFIRMED);
            if (transaction_listener!=null) transaction_listener.onTransFailureAck(this,msg);
            clearing_to=new Timer(clearing_to.getTime(),clearing_to.getLabel(),this);
            clearing_to.start();
            return;
         }
      }    
   }

   /** Method derived from interface TimerListener.
     * It's fired from an active Timer. */
   public void onTimeout(Timer to)
   {  try
      {  if (to.equals(retransmission_to) && statusIs(STATE_COMPLETED))
         {  printLog("Retransmission timeout expired",Log.LEVEL_HIGH);
            long timeout=2*retransmission_to.getTime();
            if (timeout>SipStack.max_retransmission_timeout) timeout=SipStack.max_retransmission_timeout;
            retransmission_to=new Timer(timeout,retransmission_to.getLabel(),this);
            retransmission_to.start();
            sip_provider.sendMessage(response,connection_id);
         }
         if (to.equals(end_to) && statusIs(STATE_COMPLETED))
         {  printLog("End timeout expired",Log.LEVEL_HIGH);
            doTerminate();
            transaction_listener=null;
         }  
         if (to.equals(clearing_to) && statusIs(STATE_CONFIRMED))
         {  printLog("Clearing timeout expired",Log.LEVEL_HIGH);
            doTerminate();
            transaction_listener=null;
         }  
      }
      catch (Exception e)
      {  printException(e,Log.LEVEL_HIGH);
      }
   }   

   /** Method used to drop an active transaction */
   public void terminate()
   {  doTerminate();
      transaction_listener=null;
   }


   // *********************** Protected methods ***********************

   /** Moves to terminate state. */
   protected void doTerminate()
   {  if (!statusIs(STATE_TERMINATED))
      {  retransmission_to.halt();
         clearing_to.halt();     
         end_to.halt();
         //if (statusIs(STATE_WAITING)) sip_provider.removeSelectiveListener(new TransactionId(SipMethods.INVITE));
         //else sip_provider.removeSelectiveListener(transaction_id);
         sip_provider.removeSelectiveListener(transaction_id);
         changeStatus(STATE_TERMINATED);
      }
   }

}
