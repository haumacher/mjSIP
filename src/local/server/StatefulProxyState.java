/*
 * Copyright (C) 2005 Luca Veltri - University of Parma - Italy
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


import org.zoolu.sip.provider.SipStack;
import org.zoolu.sip.provider.TransactionId;
import org.zoolu.sip.transaction.*;
import org.zoolu.sip.message.*;
import org.zoolu.tools.Log;

import java.util.Iterator;
import java.util.HashSet;
import java.util.Hashtable;


/** Class StatefulProxyState allows the record and management
  * of all TransactionServer-to-TransactionClient mappings in a stateful proxy. */
public class StatefulProxyState
{
   /** Mapping between t_clients and t_servers, as table of (TransactionIdentidier)t_client-->(TransactionServer)t_server */
   Hashtable c_server;
   /** Mapping between t_servers and their t_clients, as table of (TransactionIdentidier)t_server-->(HashSet)t_clients */
   Hashtable s_clients;  
   /** Mapping between t_servers and their response messages, as table of (TransactionIdentidier)t_server-->(Message)resp */
   Hashtable s_response;

   
   /** Creates the StatefulProxyState */
   public StatefulProxyState()
   {  if (c_server==null) c_server=new Hashtable();
      if (s_clients==null) s_clients=new Hashtable();
      if (s_response==null) s_response=new Hashtable();
   }

   /** Adds a new server <i>ts</i> */
   public synchronized void addServer(TransactionServer ts)
   {  //printlog("addServer(ts)",Log.LEVEL_LOW);
      if (hasServer(ts)) return;
      TransactionId sid=ts.getTransactionId();
      s_clients.put(sid,new HashSet());
      Message request=new Message(ts.getRequestMessage());
      //printlog("creating a possible server 408 final response",Log.LEVEL_LOW);
      Message resp=MessageFactory.createResponse(request,408,null,null);
      //printlog("DEBUG: addServer()\r\n"+resp,Log.LEVEL_LOW);
      s_response.put(sid,resp);
   }

   /** Appends a new client to server <i>ts</i>.
     * If server <i>ts</i> is new, adds it. */
   public synchronized void addClient(TransactionServer ts, Transaction tc)
   {  //printlog("addClient(ts,tc)",Log.LEVEL_LOW);
      c_server.put(tc.getTransactionId(),ts);
      TransactionId sid=ts.getTransactionId();
      HashSet clients=(HashSet)s_clients.get(sid);
      if (clients==null) clients=new HashSet();
      clients.add(tc);
      s_clients.put(sid,clients);
      Message request=new Message(ts.getRequestMessage());
      //printlog("creating a possible server 408 final response",Log.LEVEL_LOW);
      Message resp=MessageFactory.createResponse(request,408,null,null);
      //printlog("DEBUG addClient():\r\n"+resp,Log.LEVEL_LOW);
      s_response.put(sid,resp);
   }
   
   /** Removes a client. */
   public synchronized void removeClient(TransactionClient tc)
   {  TransactionId cid=tc.getTransactionId();
      TransactionServer ts=(TransactionServer)c_server.get(cid);
      if (ts==null) return;
      c_server.remove(cid);
      TransactionId sid=ts.getTransactionId();
      HashSet clients=(HashSet)s_clients.get(sid);
      if (clients==null) return;
      Transaction target=null;
      Transaction tc_i;
      for (Iterator i=clients.iterator(); i.hasNext(); )
         if ((tc_i=(Transaction)i.next()).getTransactionId().equals(cid)) target=tc_i;
      if (target!=null) clients.remove(target);
   }
   
   /** Removes all clients bound to server <i>ts</i>. */
   public synchronized void clearClients(TransactionServer ts)
   {  TransactionId sid=ts.getTransactionId();
      s_clients.remove(sid);
      s_clients.put(sid,new HashSet());
   }

   /** Whether there is a server <i>ts</i>. */
   public boolean hasServer(TransactionServer ts)
   {  TransactionId sid=ts.getTransactionId();
      return s_clients.containsKey(sid);
   }

   /** Removes server <i>ts</i>. */
   public synchronized void removeServer(TransactionServer ts)
   {  TransactionId sid=ts.getTransactionId();
      s_clients.remove(sid);
      s_response.remove(sid);
   }

   /** Gets the server bound to client <i>tc</i> */
   public synchronized TransactionServer getServer(TransactionClient tc)
   {  return (TransactionServer)c_server.get(tc.getTransactionId());
   }

   /** Gets all clients bound to server <i>ts</i>. */
   public synchronized HashSet getClients(TransactionServer ts)
   {  return (HashSet)s_clients.get(ts.getTransactionId());
   }
      
   /** Sets the final response for server <i>ts</i>. */
   public synchronized void setFinalResponse(TransactionServer ts, Message resp)
   {  TransactionId sid=ts.getTransactionId();
      s_response.remove(sid);
      s_response.put(sid,resp);
   }
    
   /** Gets the final response for server <i>ts</i>. */
   public synchronized Message getFinalResponse(TransactionServer ts)
   {  return (Message)s_response.get(ts.getTransactionId());
   }
   
   /** Gets the number of active servers. */
   public int numOfServers()
   {  return s_clients.size();
   }
   
   /** Gets the number of active clients. */
   public int numOfClients()
   {  return c_server.size();
   }
   
}  