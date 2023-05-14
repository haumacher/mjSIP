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
import org.zoolu.sip.header.*;
import org.zoolu.sip.message.*;
import org.zoolu.tools.Timer;
import org.zoolu.tools.TimerListener;
import org.zoolu.tools.Log;
import org.zoolu.tools.ExceptionPrinter;


/** A generic SIP transaction (Invite, Non-Invite, Ack).
  * It is hinerited by the following classes:
  * ClientTransaction, ServerTransaction,
  * InviteClientTransaction, InviteServerTransaction,
  * AckTransactionClient, and AckTransactionServer.
  * <p/>
  * The changes of the internal status and the received messages are fired to the
  * corrsponding transaction listener.
  */
public abstract class Transaction extends org.zoolu.tools.MonitoredObject implements SipProviderListener, TimerListener
{ 
   /** Transactions counter */
   protected static int transaction_counter=0;

   // all transaction states:
   
   /** State Waiting (used only by server transactions) - When transaction is just created. */
   protected static final int STATE_IDLE=0; 
   /** State Waiting (used only by server transactions) - When the server transaction starts listening. */
   protected static final int STATE_WAITING=1; 
   /** State Trying - When sent/received the request. */
   protected static final int STATE_TRYING=2; 
   /** State Proceeding - When sent/received a provisional response. */
   protected static final int STATE_PROCEEDING=3;
   /** State Completed - When sent/received a final response. */
   protected static final int STATE_COMPLETED=4;
   /** State Confirmed (used only by invite server transactions) - When received an ACK request.  */
   protected static final int STATE_CONFIRMED=5;
   /** State Terminated - When the transaction is terminated. */
   protected static final int STATE_TERMINATED=7; 

  
   /** Gets the transaction state. */
   protected static String getStatus(int st)
   {  switch (st)
      {  case STATE_IDLE       : return "T_Idle";
         case STATE_WAITING    : return "T_Waiting"; 
         case STATE_TRYING     : return "T_Trying"; 
         case STATE_PROCEEDING : return "T_Proceeding";
         case STATE_COMPLETED  : return "T_Completed";
         case STATE_CONFIRMED  : return "T_Confirmed";
         case STATE_TERMINATED : return "T_Terminated";
         default : return null;
      }
   }


   /** Transaction sequence number */
   int transaction_sqn;

   /** Log */
   Log log;
 
   /** Lower layer dispatcher that sends and receive messages.
     * The messages received by the SipProvider are fired to the Transaction
     * by means of the onReceivedMessage() method. */
   SipProvider sip_provider;
 
   /** Internal state-machine status */
   int status;

   /** transaction request message/method */
   Message request;
   
   /** the Transaction ID */
   TransactionId transaction_id;

   /** Transaction connection id */
   TransportConnId connection_id;


   /** Costructs a new Transaction */
   protected Transaction(SipProvider sip_provider)
   {  this.sip_provider=sip_provider;
      log=sip_provider.getLog();
      this.transaction_id=null;
      this.request=null;
      this.connection_id=null;
      this.transaction_sqn=transaction_counter++;
      this.status=STATE_IDLE;
   }

   /** Changes the internal status */
   protected void changeStatus(int newstatus)
   {  status=newstatus;
      //transaction_listener.onChangedTransactionStatus(status);
      printLog("changed transaction state: "+getStatus(),Log.LEVEL_MEDIUM);
   }
   
   /** Whether the internal status is equal to <i>st</i> */
   protected boolean statusIs(int st)
   {  return status==st;
   }      

   /** Gets the current transaction state. */
   protected String getStatus()
   {  return getStatus(status);
   }


   /** Whether the transaction is in TRYING state, i.e. it has sent/received the request. */
   public boolean isTrying()
   {  return status==STATE_TRYING;
   }

   /** Whether the transaction is in PROCEEDING state, i.e. it has sent/received a provisional response. */
   public boolean isProceeding()
   {  return status==STATE_PROCEEDING;
   }

   /** Whether the transaction is in COMPLETED state, i.e. it has sent/received a final response. */
   public boolean isCompleted()
   {  return status==STATE_COMPLETED;
   }

   /** Whether the transaction is in TERMINATED state, i.e. it is terminated. */
   public boolean isTerminated()
   {  return status==STATE_TERMINATED;
   }


   /** Gets the SipProvider of this Transaction. */
   public SipProvider getSipProvider()
   {  return sip_provider;
   }

   /** Gets the Transaction request message */
   public Message getRequestMessage()
   {  return request;
   }

   /** Gets the Transaction method */
   public String getTransactionMethod()
   {  return request.getTransactionMethod();
   }

   /** Gets the transaction-ID */
   public TransactionId getTransactionId()
   {  return transaction_id;
   }

   /** Gets the transaction connection id */
   public TransportConnId getTransportConnId()
   {  return connection_id;
   }

   /** Method derived from interface SipListener.
     * It's fired from the SipProvider when a new message is catch for to the present ServerTransaction.
     */
   public void onReceivedMessage(SipProvider provider, Message msg)
   {  //do nothing
   }
   
   /** Method derived from interface TimerListener.
     * It's fired from an active Timer.
     */
   public void onTimeout(Timer to)
   {  //do nothing
   }

   /** Terminates the transaction. */
   public abstract void terminate();

   
   //**************************** Logs ****************************/

   /** Default log level offset */
   static final int LOG_OFFSET=2;

   /** Adds a new string to the default Log. */
   protected void printLog(String str, int level)
   {  if (log!=null) log.println("Transaction#"+transaction_sqn+": "+str,Transaction.LOG_OFFSET+level);  
   }

   /** Adds a WARNING to the default Log. */
   protected void printWarning(String str, int level)
   {  printLog("WARNING: "+str,level); 
   }

   /** Adds the Exception to the log file. */
   protected void printException(Exception e, int level)
   {  printLog("Exception: "+ExceptionPrinter.getStackTraceOf(e),level);
   }

}
