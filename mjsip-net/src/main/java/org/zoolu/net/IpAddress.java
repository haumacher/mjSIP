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



import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.Vector;



/** IpAddress is an IP address.
  */
public class IpAddress {
	

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
	public static IpAddress getLocalHostAddress() {
		IpAddress ip_address=null;
		try {
			// try to get the address used for going to a remote public node
			DatagramSocket socket=new DatagramSocket();
			socket.connect(InetAddress.getByName("8.8.8.8"), 9999);
			ip_address=new IpAddress(socket.getLocalAddress());
			socket.close();
		}
		catch (Exception e) {}
		
		if (ip_address==null) {
			try {
				Vector<IpAddress> all_ip4_addrs=new Vector<>();
				Enumeration<NetworkInterface> networks = NetworkInterface.getNetworkInterfaces();
				while (networks.hasMoreElements()) {
					NetworkInterface intf = networks.nextElement();
					Enumeration<InetAddress> iaddrs = intf.getInetAddresses();
					while (iaddrs.hasMoreElements()) {
						InetAddress iaddr=iaddrs.nextElement();
						if (iaddr.getClass().getName().equals("java.net.Inet4Address")) {
							if (iaddr.isLoopbackAddress()) continue;
							if (iaddr.isLinkLocalAddress()) continue;
							if (iaddr.isMulticastAddress()) continue;
							if (iaddr.getHostAddress().equals("255.255.255.255")) continue;

							all_ip4_addrs.add(new IpAddress(iaddr));
						}
					}
				}
				for (int i=0; i<all_ip4_addrs.size(); i++) {
					IpAddress addr=(IpAddress)all_ip4_addrs.get(i);
					if (!addr.isPrivateAddress()) {
						ip_address=addr;
						break;
					}
				}
				if (ip_address==null) {
					for (int i=0; i<all_ip4_addrs.size(); i++) {
						IpAddress addr=(IpAddress)all_ip4_addrs.get(i);
						if (addr.toString().startsWith("172.")) {
							ip_address=addr;
							break;
						}
					}
				}
				if (ip_address==null) {
					for (int i=0; i<all_ip4_addrs.size(); i++) {
						IpAddress addr=(IpAddress)all_ip4_addrs.get(i);
						if (addr.toString().startsWith("192.")) {
							ip_address=addr;
							break;
						}
					}
				}
				if (ip_address==null) {
					ip_address=(IpAddress)all_ip4_addrs.get(0);
				}
			}
			catch (SocketException e) {}			
		}
		if (ip_address==null) {
			try {
				ip_address=new IpAddress(InetAddress.getLocalHost());
			}
			catch (java.net.UnknownHostException e) {}
		}
		if (ip_address==null) ip_address=new IpAddress("127.0.0.1");
		
		return ip_address;
	}

}
