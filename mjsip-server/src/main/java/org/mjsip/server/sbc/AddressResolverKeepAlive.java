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

import org.mjsip.sip.provider.SipKeepAlive;
import org.mjsip.sip.provider.SipProvider;
import org.slf4j.LoggerFactory;
import org.zoolu.net.SocketAddress;



/** Class AddressResolverKeepAlive maintains an address binding.
  * It maps socket addresses into other socket addresses and try to keep NAT connection
  * open sending keepalive datagrams toward the remote UAs.
  * <p> Class AddressResolverKeepAlive can be used to maintain a reference to the actual address
  * to be used to reach a remote node, against to the address optained in other ways.
  * For example, it can be used to maintain correct remote address mapping
  * for symmetric NAT traversal.
  */
public class AddressResolverKeepAlive extends AddressResolver {
	
	private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(AddressResolverKeepAlive.class);

	/** Keep-alive time [millisecs] */
	long keepalive_time;

	/** Sip keep-alive daemons */
	Hashtable<String, SipKeepAlive> keepalive_daemons=null;
	
	/** Costructs an empty AddressResolverKeepAlive */
	public AddressResolverKeepAlive(SipProvider sip_provider, long refresh_time, long keepalive_time) {
		super(sip_provider, refresh_time);
		this.keepalive_time=keepalive_time;
		keepalive_daemons=new Hashtable<>();
	}

	
	/** Adds or updates a new SocketAddress mapping */
	@Override
	public void updateBinding(SocketAddress refer_soaddr, SocketAddress actual_soaddr) {
		if (refer_soaddr!=null) {
			String key=refer_soaddr.toString();
			if (keepalive_daemons.containsKey(key)) {
				if (!binding_table.get(key).equals(actual_soaddr)) {
					SipKeepAlive keepalive=keepalive_daemons.get(key);
					keepalive.setDestSoAddress(actual_soaddr);
					LOG.debug("KeepAlive: change dest: {}", actual_soaddr);
				}
			}
			else {
				SipKeepAlive keepalive=new SipKeepAlive(sip_provider,actual_soaddr,keepalive_time);
				keepalive_daemons.put(key,keepalive);
				LOG.debug("KeepAlive: start: {}", actual_soaddr);
			}
		}
		super.updateBinding(refer_soaddr,actual_soaddr);
	}


	/** Removes a SocketAddress mapping */
	@Override
	public void removeBinding(SocketAddress refer_soaddr) {
		if (refer_soaddr!=null) {
			String key=refer_soaddr.toString();
			if (keepalive_daemons.containsKey(key)) {
				SipKeepAlive keepalive=keepalive_daemons.get(key);
				keepalive_daemons.remove(key);
				keepalive.halt();
				LOG.debug("KeepAlive: halt: {}", keepalive.getDestSoAddress());
			}
		}
		super.removeBinding(refer_soaddr);
	}


	/** When the refresh timeout fires */
	@Override
	protected void onTimeout() {
		// enumerate expired binding
		long now=(new Date()).getTime();
		Vector<String> aux=new Vector<>();
		for (Enumeration<String> e=time_table.keys(); e.hasMoreElements(); ) {
			String key= e.nextElement();
			long expire= time_table.get(key).longValue();
			if (expire<now) aux.addElement(key);
		}
		// remove expired binding
		for (int i=0; i<aux.size(); i++) {
			String key= aux.elementAt(i);
			SipKeepAlive keepalive= keepalive_daemons.get(key);
			keepalive_daemons.remove(key);
			keepalive.halt();
			LOG.debug("KeepAlive: halt: {}", keepalive.getDestSoAddress());
		}
		super.onTimeout();
	}

}
