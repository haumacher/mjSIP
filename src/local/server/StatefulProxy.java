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

package local.server;


import org.zoolu.sip.address.*;
import org.zoolu.sip.provider.*;
import org.zoolu.sip.header.RequestLine;
import org.zoolu.sip.header.Header;
import org.zoolu.sip.header.ViaHeader;
import org.zoolu.sip.header.MultipleHeader;
import org.zoolu.sip.header.RouteHeader;
import org.zoolu.sip.header.RecordRouteHeader;
import org.zoolu.sip.transaction.*;
import org.zoolu.sip.message.*;
import org.zoolu.tools.Log;

import java.util.Vector;
import java.util.Iterator;
import java.util.HashSet;
import java.io.BufferedReader;
import java.io.InputStreamReader;


/** StatefulProxy server. 
  * Class StatefulProxy implement a stateful SIP proxy server.
  * It extends class Registrar. A StatefulProxy can work as simply SIP proxy,
  * or it can handle calls for registered users. 
  */
public class StatefulProxy extends Proxy implements TransactionClientListener
{
   /** Transactions state */
   protected StatefulProxyState state=null;
   
   /** end timeout for client transactions ("Timer C" in RFC 3261) */
   //Timer proxy_transaction_to;
   
   /** SipProvider for client transactions */
   protected SipProvider sip_provider_client;   

   /** SipProvider for server transactions */
   protected SipProvider sip_provider_server;   

      
   /** Costructs a void StatefulProxy */
   protected StatefulProxy() {}


   /** Inits the stateful server */
   private void init()
   {  sip_provider_client=sip_provider;
      sip_provider_server=sip_provider;
      state=new StatefulProxyState();
   }   

      
   /** Costructs a new StatefulProxy that acts also as location server for registered users. */
   /*public StatefulProxy(SipProvider provider_server, SipProvider provider_client, ServerProfile server_profile)
   {  super(provider_server,server_profile);
      sip_provider_client=provider_client;
      sip_provider_server=provider_server;
      init();
   }*/


   /** Costructs a new StatefulProxy that acts also as location server for registered users. */
   public StatefulProxy(SipProvider provider, ServerProfile server_profile)
   {  super(provider,server_profile);
      init();
   }


   /** When a new request is received for the local server */
   public void processRequestToLocalServer(Message req)
   {  printLog("inside processRequestToLocalServer(msg)",Log.LEVEL_MEDIUM);
      super.processRequestToLocalServer(req);
   }


   /** When a new request message is received for a local user */
   public void processRequestToLocalUser(Message msg)
   {  printLog("inside processRequestToLocalUser(msg)",Log.LEVEL_MEDIUM);

      if (msg.isAck())
      {  printLog("ACK received out of an active InviteServerTransaction, message forwarded",Log.LEVEL_MEDIUM);
         // ACK out of an active transaction is treated in statelss manner
         super.processRequestToLocalUser(msg);
         return; 
      }
      
      TransactionServer ts;
      if (msg.isInvite()) ts=new InviteTransactionServer(sip_provider_server,msg,null);
      else ts=new TransactionServer(sip_provider_server,msg,null);
   
      // proxy authentication
      /*if (server_profile.do_proxy_authentication && !msg.isAck() && !msg.isCancel())
      {  Message err_resp=as.authenticateProxyRequest(msg);  
         if (err_resp!=null)
         {  ts.respondWith(err_resp);
            return;
         }
      }*/

      // message targets
      Vector targets=getTargets(msg);

      if (targets.isEmpty())
      {  // prefix-based forwarding
         SipURL request_uri=msg.getRequestLine().getAddress();
         SipURL new_target=null;
         if (isResponsibleFor(msg.getFromHeader().getNameAddress().getAddress())) new_target=getAuthPrefixBasedProxyingTarget(request_uri);
         if (new_target==null) new_target=getPrefixBasedProxyingTarget(request_uri);
         if (new_target!=null) targets.addElement(new_target.toString());
      }
      if (targets.isEmpty())
      {  printLog("No target found, message discarded",Log.LEVEL_HIGH);
         // the msg is not an ACK (already checked)
         sendStatefulServerResponse(ts,MessageFactory.createResponse(msg,404,null,null));
         return;
      }

      printLog("message will be forwarded to "+targets.size()+" user's contact(s)",Log.LEVEL_MEDIUM); 
      for (int i=0; i<targets.size(); i++) 
      {  SipURL target_url=new SipURL((String)(targets.elementAt(i)));
         Message request=new Message(msg);
         request.removeRequestLine();
         request.setRequestLine(new RequestLine(msg.getRequestLine().getMethod(),target_url));

         updateProxyingRequest(request);         

         TransactionClient tc;
         if (msg.isInvite()) tc=new ProxyInviteTransactionClient(sip_provider_client,request,this);
         else tc=new TransactionClient(sip_provider_client,request,this);
         //printLog("DEBUG: processLocalRequest()\r\n"+tc.getRequestMessage().toString(),Log.LEVEL_LOWER);
         state.addClient(ts,tc);
      }
      HashSet clients=state.getClients(ts);
      for (Iterator i=clients.iterator(); i.hasNext(); ) ((TransactionClient)i.next()).request();
   }

   
   /** When a new request message is received for a remote UA */
   public void processRequestToRemoteUA(Message msg)
   {  printLog("inside processRequestToRemoteUA(msg)",Log.LEVEL_MEDIUM);
      if (msg.isAck())
      {  printLog("ACK received out of an active InviteServerTransaction, message forwarded",Log.LEVEL_MEDIUM);
         // just send the ack..
         super.processRequestToRemoteUA(msg);
         return; 
      }
      TransactionServer ts;
      if (msg.isInvite()) ts=new InviteTransactionServer(sip_provider_server,msg,null);
      else ts=new TransactionServer(sip_provider_server,msg,null);

      if (!server_profile.is_open_proxy)
      {  // check whether the caller or callee is a local user 
         if (!isResponsibleFor(msg.getFromHeader().getNameAddress().getAddress()) && !isResponsibleFor(msg.getToHeader().getNameAddress().getAddress()))
         {  printLog("both caller and callee are not registered with the local server: proxy denied.",Log.LEVEL_HIGH);
            ts.respondWith(MessageFactory.createResponse(msg,503,null,null));
            return;
         }
      }

      // proxy authentication
      /*if (server_profile.do_proxy_authentication && !msg.isAck() && !msg.isCancel())
      {  Message err_resp=as.authenticateProxyRequest(msg);  
         if (err_resp!=null)
         {  ts.respondWith(err_resp);
            return;
         }
      }*/

      // domain-based forwarding
      RequestLine rl=msg.getRequestLine();
      SipURL request_uri=rl.getAddress();
      SipURL nexthop=null;
      if (isResponsibleFor(msg.getFromHeader().getNameAddress().getAddress())) nexthop=getAuthDomainBasedProxyingTarget(request_uri);
      if (nexthop==null) nexthop=getDomainBasedProxyingTarget(request_uri);
      if (nexthop!=null) msg.setRequestLine(new RequestLine(rl.getMethod(),nexthop));
      
      updateProxyingRequest(msg);         

      TransactionClient tc;
      if (msg.isInvite()) tc=new ProxyInviteTransactionClient(sip_provider_client,msg,this);
      else tc=new TransactionClient(sip_provider_client,msg,this);
      state.addClient(ts,tc);
      tc.request(); 
   }   


   /** When a new response message is received */
   public void processResponse(Message resp)
   {  printLog("inside processResponse(msg)",Log.LEVEL_MEDIUM);
      //printLog("Response received out of an active ClientTransaction, message discarded",Log.LEVEL_HIGH);
      super.processResponse(resp);   
   }


   /** Sends a server final response */
   protected void sendStatefulServerResponse(TransactionServer ts, Message resp)
   {  printLog("inside sendStatefulServerResponse(msg)",Log.LEVEL_MEDIUM);
      printLog("Server response: "+resp.getStatusLine().toString(),Log.LEVEL_MEDIUM);
      ts.respondWith(resp);
   }   


   /** Process provisional response */
   protected void processProvisionalResponse(TransactionClient transaction, Message resp)
   {  printLog("inside processProvisionalResponse(t,resp)",Log.LEVEL_MEDIUM);
      int code=resp.getStatusLine().getCode();
      TransactionServer ts=state.getServer(transaction);
      if (ts!=null && code!=100)
      {  updateProxyingResponse(resp);
         if (resp.hasViaHeader()) ts.respondWith(resp);
      }
   }
   
   /** Process failure response */
   protected void processFailureResponse(TransactionClient transaction, Message resp)
   {  printLog("inside processFailureResponse(t,resp)",Log.LEVEL_MEDIUM);
      TransactionServer ts=state.getServer(transaction);
      state.removeClient(transaction);
      if (ts==null) return;
      if (!state.hasServer(ts)) return;
      // updates the non-2xx final response
      state.setFinalResponse(ts,resp);
      // if there are no more pending clients, sends the final response
      HashSet clients=state.getClients(ts);
      if (clients.isEmpty())
      {  printLog("only this t_client remained: send the response",Log.LEVEL_LOW);
         resp=state.getFinalResponse(ts);
         updateProxyingResponse(resp);
         if (resp.hasViaHeader()) ts.respondWith(resp); else ts.terminate();
         state.removeServer(ts);
      }
      printLog("t_clients still active: "+state.numOfClients(),Log.LEVEL_LOW);
      printLog("t_servers still active: "+state.numOfClients(),Log.LEVEL_LOW);
   }

   /** Process success response */
   protected void processSuccessResponse(TransactionClient transaction, Message resp)
   {  printLog("inside processSuccessResponse(t,resp)",Log.LEVEL_MEDIUM);
      TransactionServer ts=state.getServer(transaction);
      state.removeClient(transaction);
      if (ts==null) return;
      updateProxyingResponse(resp);
      if (resp.hasViaHeader())
      {  ts.respondWith(resp);
         if (!state.hasServer(ts)) return;
         //else
         // cancel all other pending transaction clients
         HashSet clients=state.getClients(ts);
         //printLog("Cancel pending clients..",Log.LEVEL_LOW);
         // cancel ONLY INVITE transaction clients
         if (transaction.getTransactionMethod().equals(SipMethods.INVITE))
         {  //printLog("Cancelling "+clients.size()+" pending clients",Log.LEVEL_LOW);
            printLog(clients.size()+" pending clients",Log.LEVEL_LOW);
            int canc_counter=0;
            for (Iterator i=clients.iterator(); i.hasNext(); )
            {  Transaction tc=(Transaction)i.next();
               // cancel ONLY transaction clients that has (only) received a provisional response
               if (tc.isProceeding())
               {  Message cancel=MessageFactory.createCancelRequest(tc.getRequestMessage());
                  TransactionClient tc_cancel=new TransactionClient(sip_provider_server,cancel,null);
                  tc_cancel.request();
                  canc_counter++;
               }
            }
            printLog("Cancelled "+canc_counter+" clients in \"proceeding\" state",Log.LEVEL_LOW);
         }
         state.removeServer(ts);
      }
      printLog("t_clients still active: "+state.numOfClients(),Log.LEVEL_LOW);
      printLog("t_servers still active: "+state.numOfServers(),Log.LEVEL_LOW);
   }


   /** Process tmeout */
   protected void processTimeout(TransactionClient transaction)
   {  printLog("inside processTimeout(t)",Log.LEVEL_MEDIUM);
      TransactionServer ts=state.getServer(transaction);
      state.removeClient(transaction);
      if (ts==null) return;
      HashSet clients=state.getClients(ts);
      if (clients==null) return;
      if (clients.isEmpty())
      {  printLog("DEBUG: responding..",Log.LEVEL_LOW);
         //printLog("DEBUG:\r\n"+state.getFinalResponse(ts),Log.LEVEL_LOW);
         Message resp=state.getFinalResponse(ts);
         updateProxyingResponse(resp);
         if (resp.hasViaHeader()) sendStatefulServerResponse(ts,resp);
         else ts.terminate();
         state.removeServer(ts);
      }      
      printLog("t_clients still active: "+state.numOfClients(),Log.LEVEL_LOW);
      printLog("t_servers still active: "+state.numOfClients(),Log.LEVEL_LOW);
   }

   // ******************* TransactionClient callback methods *******************

   /** When the TransactionClient is in "Proceeding" state and receives a new 1xx response */
   public void onTransProvisionalResponse(TransactionClient transaction, Message resp)
   {  processProvisionalResponse(transaction,resp);
   }
   
   /** When the TransactionClient goes into the "Completed" state, receiving a failure response */
   public void onTransFailureResponse(TransactionClient transaction, Message resp)
   {  processFailureResponse(transaction,resp);
   }
           
   /** When an TransactionClient goes into the "Terminated" state, receiving a 2xx response */
   public void onTransSuccessResponse(TransactionClient transaction, Message resp) 
   {  processSuccessResponse(transaction,resp);
   }

   /** When the TransactionClient goes into the "Terminated" state, caused by transaction timeout */
   public void onTransTimeout(TransactionClient transaction)
   {  processTimeout(transaction);
   }



   // ****************************** Logs *****************************

   /** Adds a new string to the default Log */
   private void printLog(String str, int level)
   {  if (log!=null) log.println("StatefulProxy: "+str,ServerEngine.LOG_OFFSET+level);  
   }


   // ****************************** MAIN *****************************

   /** The main method. */
   public static void main(String[] args)
   {  
         
      String file=null;
      boolean prompt_exit=false;
      
      for (int i=0; i<args.length; i++)
      {  if (args[i].equals("-f") && args.length>(i+1))
         {  file=args[++i];
            continue;
         }
         if (args[i].equals("--prompt"))
         {  prompt_exit=true;
            continue;
         }
         if (args[i].equals("-h"))
         {  System.out.println("usage:\n   java StatefulProxy [options] \n");
            System.out.println("   options:");
            System.out.println("   -h               this help");
            System.out.println("   -f <config_file> specifies a configuration file");
            System.out.println("   --prompt         prompt for exit");
            System.exit(0);
         }
      }
                  
      SipStack.init(file);
      SipProvider sip_provider=new SipProvider(file);
      ServerProfile server_profile=new ServerProfile(file);
      
      StatefulProxy sproxy=new StatefulProxy(sip_provider,server_profile);   
      
      // promt before exit
      if (prompt_exit) 
      try
      {  System.out.println("press 'enter' to exit");
         BufferedReader in=new BufferedReader(new InputStreamReader(System.in)); 
         in.readLine();
         System.exit(0);
      }
      catch (Exception e) {}
   }
   
}