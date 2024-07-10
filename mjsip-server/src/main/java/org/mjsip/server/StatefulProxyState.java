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

package org.mjsip.server;


import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;

import org.mjsip.sip.message.SipMessage;
import org.mjsip.sip.message.SipResponses;
import org.mjsip.sip.provider.SipId;
import org.mjsip.sip.provider.SipProvider;
import org.mjsip.sip.transaction.Transaction;
import org.mjsip.sip.transaction.TransactionClient;
import org.mjsip.sip.transaction.TransactionServer;


/** Class StatefulProxyState allows the record and management
  * of all TransactionServer-to-TransactionClient mappings in a stateful proxy. */
public class StatefulProxyState {
	
	/** Mapping between t_clients and t_servers, as table of (TransactionId)t_client-->(TransactionServer)t_server */
	Hashtable<SipId, TransactionServer> c_server;
	/** Mapping between t_servers and their t_clients, as table of (TransactionId)t_server-->(HashSet)t_clients */
	Hashtable<SipId, HashSet<Transaction>> s_clients;
	/** Mapping between t_servers and their response messages, as table of (TransactionId)t_server-->(Message)resp */
	Hashtable<SipId, SipMessage> s_response;
	private SipProvider sip_provider;

	
	/** Creates the StatefulProxyState */
	public StatefulProxyState(SipProvider sip_provider) {
		this.sip_provider = sip_provider;
		if (c_server==null) c_server=new Hashtable<>();
		if (s_clients==null) s_clients=new Hashtable<>();
		if (s_response==null) s_response=new Hashtable<>();
	}

	/** Adds a new server <i>ts</i> */
	public synchronized void addServer(TransactionServer ts) {
		//printlog("addServer(ts)",LogWriter.LEVEL_LOW);
		if (hasServer(ts)) return;
		SipId sid=ts.getTransactionId();
		s_clients.put(sid,new HashSet<>());
		SipMessage request=new SipMessage(ts.getRequestMessage());
		//printlog("creating a possible server 408 final response",LogWriter.LEVEL_LOW);
		SipMessage resp=sip_provider.messageFactory().createResponse(request,SipResponses.REQUEST_TIMEOUT,null,null);
		//printlog("DEBUG: addServer()\r\n"+resp,LogWriter.LEVEL_LOW);
		s_response.put(sid,resp);
	}

	/** Appends a new client to server <i>ts</i>.
	  * If server <i>ts</i> is new, adds it. */
	public synchronized void addClient(TransactionServer ts, Transaction tc) {
		//printlog("addClient(ts,tc)",LogWriter.LEVEL_LOW);
		c_server.put(tc.getTransactionId(),ts);
		SipId sid=ts.getTransactionId();
		HashSet<Transaction> clients= s_clients.get(sid);
		if (clients==null) clients=new HashSet<>();
		clients.add(tc);
		s_clients.put(sid,clients);
		SipMessage request=new SipMessage(ts.getRequestMessage());
		//printlog("creating a possible server 408 final response",LogWriter.LEVEL_LOW);
		SipMessage resp=sip_provider.messageFactory().createResponse(request, SipResponses.REQUEST_TIMEOUT,null,null);
		//printlog("DEBUG addClient():\r\n"+resp,LogWriter.LEVEL_LOW);
		s_response.put(sid,resp);
	}
	
	/** Removes a client. */
	public synchronized void removeClient(TransactionClient tc) {
		SipId cid=tc.getTransactionId();
		TransactionServer ts= c_server.get(cid);
		if (ts==null) return;
		c_server.remove(cid);
		SipId sid=ts.getTransactionId();
		HashSet<Transaction> clients=s_clients.get(sid);
		if (clients==null) return;
		Transaction target=null;
		Transaction tc_i;
		for (Iterator<Transaction> i=clients.iterator(); i.hasNext(); )
			if ((tc_i= i.next()).getTransactionId().equals(cid)) target=tc_i;
		if (target!=null) clients.remove(target);
	}
	
	/** Removes all clients bound to server <i>ts</i>. */
	public synchronized void clearClients(TransactionServer ts) {
		SipId sid=ts.getTransactionId();
		s_clients.remove(sid);
		s_clients.put(sid,new HashSet<>());
	}

	/** Whether there is a server <i>ts</i>. */
	public boolean hasServer(TransactionServer ts) {
		SipId sid=ts.getTransactionId();
		return s_clients.containsKey(sid);
	}

	/** Removes server <i>ts</i>. */
	public synchronized void removeServer(TransactionServer ts) {
		SipId sid=ts.getTransactionId();
		s_clients.remove(sid);
		s_response.remove(sid);
	}

	/** Gets the server bound to client <i>tc</i> */
	public synchronized TransactionServer getServer(TransactionClient tc) {
		return c_server.get(tc.getTransactionId());
	}

	/** Gets all clients bound to server <i>ts</i>. */
	public synchronized HashSet<Transaction> getClients(TransactionServer ts) {
		return s_clients.get(ts.getTransactionId());
	}
		
	/** Sets the final response for server <i>ts</i>. */
	public synchronized void setFinalResponse(TransactionServer ts, SipMessage resp) {
		SipId sid=ts.getTransactionId();
		s_response.remove(sid);
		s_response.put(sid,resp);
	}
	 
	/** Gets the final response for server <i>ts</i>. */
	public synchronized SipMessage getFinalResponse(TransactionServer ts) {
		return s_response.get(ts.getTransactionId());
	}
	
	/** Gets the number of active servers. */
	public int numOfServers() {
		return s_clients.size();
	}
	
	/** Gets the number of active clients. */
	public int numOfClients() {
		return c_server.size();
	}
	
}  
