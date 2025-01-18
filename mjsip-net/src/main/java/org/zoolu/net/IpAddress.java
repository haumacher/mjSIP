/*
 * Copyright (C) 2006 Luca Veltri - University of Parma - Italy
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

package org.zoolu.net;



import java.io.IOException;
import java.net.DatagramSocket;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



/** IpAddress is an IP address.
  */
public class IpAddress {
	
	private static final Logger LOG = LoggerFactory.getLogger(IpAddress.class);

	/** The host address/name */
	String address;

	/** The InetAddress */
	InetAddress inet_address;

 

	/** Creates an IpAddress */
	public IpAddress(String address) {
		init(address,null);
	}
 
	/** Creates an IpAddress */
	public IpAddress(IpAddress ipaddr) {
		init(ipaddr.address,ipaddr.inet_address);
	}

	/** Creates an IpAddress */
	public IpAddress(InetAddress iaddress) {
		init(null,iaddress);
	}

	/** Inits the IpAddress */
	private void init(String address, InetAddress iaddress) {
		this.address=address;
		this.inet_address=iaddress;
	}


	/** Gets the host address */
	/*public String getAddress() {
		if (address==null) address=inet_address.getHostAddress();
		return address;
	}*/

	/** Gets the InetAddress */
	public InetAddress getInetAddress() {
		if (inet_address==null) try { inet_address=InetAddress.getByName(address); } catch (java.net.UnknownHostException e) {}
		return inet_address;
	}

	/** Makes a copy */
	@Override
	public Object clone() {
		return new IpAddress(this);
	}

	/** Wthether it is equal to Object <i>obj</i> */
	@Override
	public boolean equals(Object obj) {
		try {
			IpAddress ipaddr=(IpAddress)obj;
			if (!toString().equals(ipaddr.toString())) return false;
			return true;
		}
		catch (Exception e) {  return false;  }
	}

	/** Returns a hash code value for the object. */
	@Override
	public int hashCode() {
		return toString().hashCode();
	}

	/** Gets a String representation of the Object */
	@Override
	public String toString() {
		if (address==null && inet_address!=null) address=inet_address.getHostAddress();
		return address;
	}
	

	// *********************** Static ***********************

	/** Gets the IpAddress for a given fully-qualified host name. */
	public static IpAddress getByName(String host_addr) throws java.net.UnknownHostException {
		InetAddress iaddr=InetAddress.getByName(host_addr);
		return new IpAddress(iaddr);
	}


	/** Checks if it is a private address.
	 * @return <i>true</i> in case of private address */
	public boolean isPrivateAddress() {
		String addr=toString();
		if (addr.startsWith("10.")) return true;
		if (addr.startsWith("192.168.")) return true;
		for (int i=16; i<32; i++) if (addr.startsWith("172."+i+".")) return true;
		// else
		return false;
	}
	
	
	/** Detects the default IP address of this host. */
	public static InetAddress getLocalHostAddress() {
		return getLocalHostAddress(null);
	}
	
	public static InetAddress getLocalHostAddress(AddressType type) {
		boolean ipv6 = type == AddressType.IP6;
		
		String remote = ipv6 ? "2001:4860:4860::8888" : "8.8.8.8";
		try {
			// Try to get an address with internet connection.
			try (DatagramSocket socket=new DatagramSocket()) {
				// Use Google DNS server
				socket.connect(InetAddress.getByName(remote), 9999);
				return socket.getLocalAddress();
			}
		} catch (Exception ex) {
			// Ignore. Note: Here, not only a IOException can be thrown, but also a
			// UncheckedIOException that wraps a SocketException, if executed on a host that
			// has no IPv6 available.
			LOG.debug("Cannot access {} for local socket address testing.", remote, ex);
		}
		
		try {
			int prio = 3;
			
			final int linkPrio = 2;
			final int sitePrio = 1;
			
			InetAddress result = null;
			
			Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
			while (interfaces.hasMoreElements()) {
				NetworkInterface network = interfaces.nextElement();
				if (network.isLoopback()) {
					continue;
				}
				if (network.isVirtual()) {
					continue;
				}
				if (!network.isUp()) {
					continue;
				}
				Enumeration<InetAddress> addresses = network.getInetAddresses();
				while (addresses.hasMoreElements()) {
					InetAddress address = addresses.nextElement();
					if (address.isAnyLocalAddress() || address.isLoopbackAddress()  || address.isMulticastAddress()) {
						continue;
					}
					
					if (ipv6 != (address instanceof Inet6Address)) {
						continue;
					}

					try {
						if (address.isLinkLocalAddress()) {
							if (prio > linkPrio) {
								result = normalize(address);
								prio = linkPrio;
							}
							continue;
						}
						
						if (address.isSiteLocalAddress()) {
							if (prio > sitePrio) {
								result = normalize(address);
								prio = sitePrio;
							}
							continue;
						}

						result = normalize(address);
					} catch (UnknownHostException e) {
						continue;
					}
					
					return result;
				}
			}
			
			if (result != null) {
				return result;
			}
		} catch (SocketException e1) {
			// Ignore.
		}
		
		try {
			return InetAddress.getLocalHost();
		} catch (UnknownHostException e) {
			// Ignore.
		}

		return InetAddress.getLoopbackAddress();
	}

	private static InetAddress normalize(InetAddress address) throws UnknownHostException {
		return InetAddress.getByAddress(address.getAddress());
	}

}
