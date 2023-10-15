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

package org.mjsip.server.sbc;



import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import java.util.concurrent.ScheduledFuture;

import org.mjsip.time.Scheduler;
import org.slf4j.LoggerFactory;
import org.zoolu.net.SocketAddress;



/** Class AddressResolver maintains address binding.
  * It maps socket addresses into other socket addresses.
  * <p>
  * Class AddressResolver can be used to maintain a reference to the actual address
  * to be used to reach a remote node, against to the address optained in other ways.
  * For example, it can be used to maintain correct remote address mapping
  * for symmetric NAT traversal.
  */
public class AddressResolver {
	
	private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(AddressResolver.class);

	/** Refresh time [millisecs].
	  * When expired bindings are removed. */
	long refresh_time;

	/** Expire time [millisecs].
	  * The maximum time that a binding is considered active. */
	long expire_time;

	/** Binding table */
	Hashtable binding_table;

	/** Time table */
	Hashtable time_table;

	/** Refresh timer */
	ScheduledFuture<?> timer;
	
	/** Costructs an empty AddressResolve.r */
	public AddressResolver(long refresh_time) {
		this.refresh_time=refresh_time;
		expire_time=refresh_time/2;
		binding_table=new Hashtable();
		time_table=new Hashtable();
		timer=Scheduler.scheduleWithFixedDelay(refresh_time, this::onTimeout);
	}
	
	/** Gets the size of the resolver's db */
	public int size() {
		return binding_table.size();
	}


	/** Gets list of all reference SocketAddresses */
	public Enumeration getAllSocketAddresses() {
		return binding_table.keys();
	}


	/** Whether there is a mapping for the selected SocketAddress */
	public boolean contains(SocketAddress refer_soaddr) {
		if (refer_soaddr!=null) {
			return binding_table.containsKey(refer_soaddr.toString());
		}
		return false;
	}
	
	
	/** Adds or updates a new SocketAddress mapping */
	public void updateBinding(SocketAddress refer_soaddr, SocketAddress actual_soaddr) {
		if (refer_soaddr!=null) {
			String key=refer_soaddr.toString();
			Long expire=new Long((new Date()).getTime()+expire_time);
			if (binding_table.containsKey(key)) {
				if (!((SocketAddress)binding_table.get(key)).equals(actual_soaddr)) {
					LOG.info("change BINDING "+refer_soaddr+" >> "+actual_soaddr);
					binding_table.remove(key);
					binding_table.put(key,actual_soaddr);
				}
				else {
					LOG.debug("update BINDING "+refer_soaddr+" >> "+actual_soaddr);
					// do not change binding_table
				}
				time_table.remove(key);
				time_table.put(key,expire);
			}
			else {
				LOG.info("add BINDING "+refer_soaddr+" >> "+actual_soaddr);
				binding_table.put(key,actual_soaddr);
				time_table.put(key,expire);
			}
		}
	}


	/** Removes a SocketAddress mapping */
	public void removeBinding(SocketAddress refer_soaddr) {
		if (refer_soaddr!=null) {
			String key=refer_soaddr.toString();
			if (binding_table.containsKey(key)) {
				LOG.info("remove BINDING for "+refer_soaddr);
				binding_table.remove(key);
				time_table.remove(key);
			}
		}
	}


	/** Gets the actual SocketAddress for the selected SocketAddress */
	public SocketAddress getSocketAddress(SocketAddress refer_soaddr) {
		if (refer_soaddr!=null) {
			String key=refer_soaddr.toString();
			if (binding_table.containsKey(key)) return (SocketAddress)binding_table.get(key);
		}
		return null;
	}


	/** When the refresh timeout fires */
	protected void onTimeout() {
		// enumerate expired binding
		LOG.debug("refresh all address bindings:");         
		long now=(new Date()).getTime();
		Vector aux=new Vector();
		for (Enumeration e=time_table.keys(); e.hasMoreElements(); ) {
			String key=(String)e.nextElement();
			long expire=((Long)time_table.get(key)).longValue();
			if (expire<now) aux.addElement(key);
		}
		// remove expired binding
		for (int i=0; i<aux.size(); i++) {
			String key=(String)aux.elementAt(i);
			LOG.info("remove BINDING for "+key);         
			binding_table.remove(key);
			time_table.remove(key);
		}
		LOG.debug("done.");         
	}

}
