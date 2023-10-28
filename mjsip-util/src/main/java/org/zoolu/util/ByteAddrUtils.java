/*
 * Copyright (C) 2012 Luca Veltri - University of Parma - Italy
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

package org.zoolu.util;



/** Class that collects static methods for dealing with binary address (e.g. IP, MAC addresses).
  */
public class ByteAddrUtils {
	

	/** Transforms a four-bytes array into a dotted four-decimals string. */
	public static String bytesToIpv4addr(byte[] b) {
		return bytesToIpv4addr(b,0);
	}
	
	/** Transforms a four-bytes array into a dotted four-decimals string. */
	public static String bytesToIpv4addr(byte[] b, int off) {
		return Integer.toString(ByteUtils.uByte(b[off++]))+"."+Integer.toString(ByteUtils.uByte(b[off++]))+"."+Integer.toString(ByteUtils.uByte(b[off++]))+"."+Integer.toString(ByteUtils.uByte(b[off]));
	}

	/** Transforms a dotted four-decimals string (ipv4 address) into a four-bytes array. */
	public static byte[] ipv4addrToBytes(String addr) {
		byte[] b=new byte[4];
		/*int begin=0;
		int end;
		for (int i=0; i<3; i++) {
			end=addr.indexOf('.',begin);
			b[i]=(byte)Integer.parseInt(addr.substring(begin,end));
			begin=end+1;
		}
		b[3]=(byte)Integer.parseInt(addr.substring(begin));*/
		ipv4addrToBytes(addr,b,0);
		return b;
	} 
	
	/** Transforms a dotted four-decimals string (ipv4 address) into a four-bytes array. */
	public static void ipv4addrToBytes(String addr, byte[] buf, int off) {
		int begin=0;
		int end;
		for (int i=0; i<3; i++) {
			end=addr.indexOf('.',begin);
			buf[off+i]=(byte)Integer.parseInt(addr.substring(begin,end));
			begin=end+1;
		}
		buf[off+3]=(byte)Integer.parseInt(addr.substring(begin));
	} 



	/** Converts an array of bytes into a string of hexadecimal 4-byte words. */
	public static String asHex4Bytes(byte[] buf) {
		return asHex4Bytes(buf,0,buf.length);
	}

	/** Converts a portion of an array of bytes into a string of hexadecimal 4-byte words. */
	public static String asHex4Bytes(byte[] buf, int off, int len) {
		StringBuffer sb=new StringBuffer();
		for (int i=0; i<len; i+=4) {
			int res=len-i;
			sb.append(ByteUtils.asHex(buf,off+i,((res<4)?res:4)));
			sb.append(' ');
			//sb.append('\n');
		}
		return sb.toString();
	}  



	/** Converts an array of bytes representing a data packet into a hex string. */
	public static String asHexEthernetPacket(byte[] buf) {
		return asHexEthernetPacket(buf,0,buf.length);
	}

	/** Converts a portion of an array of bytes  representing a data packet into a hex string. */
	public static String asHexEthernetPacket(byte[] buf, int off, int len) {
		StringBuffer sb=new StringBuffer();
		sb.append(ByteUtils.asHex(buf,off,6));
		sb.append(' ').append(ByteUtils.asHex(buf,off+6,6));
		sb.append(' ').append(ByteUtils.asHex(buf,off+12,2));
		off+=14;
		for (; off<len; off+=4) sb.append(' ').append(ByteUtils.asHex(buf,off,((len-off)>4)? 4: len-off));
		return sb.toString();
	}


	
	/** Checksum calculation. */
	public static int checksum(byte[] buf, int off, int len) {
		int sum=0;
		for (int i=0; i<len; i+=2) sum+=((((int)buf[off+i])<<8)&0xFF00)+(((int)buf[off+i+1])&0xFF);  
		while ((sum>>16)!=0) sum=(sum&0xFFFF)+(sum>>16);
		return ~sum;
	}

	/** Updates IPv4 header chacksum within the given IPv4 datagram. */
	public static void updateIPv4HeaderChecksum(byte[] buf, int off) {
		int hlen=4*(buf[off]&0x0F);
		ByteUtils.intToTwoBytes(0,buf,off+10);
		int checksum=checksum(buf,off,hlen);
		ByteUtils.intToTwoBytes(checksum,buf,off+10);
	}



	/** Eliminates all non-hexadecimal characters. */
	public static String trimHexString(String str) {
		if (str.startsWith("0x")) str=str.substring(2);
		for (int i=0; i<str.length(); i++) {
			int c=str.charAt(i);
			if ((c<'0' || c>'9') && (c<'a' || c>'f')  && (c<'A' || c>'F')) str=str.substring(0,i)+str.substring(i+1);
		}
		return str;
	}



	/** Changes MAC and IP destination addresses of the given Ethernet frame. */
	public static void changeFrameMacIpDestinationAddresses(byte[] data, String mac_addr, String ip_addr) {
		changeFrameMacIpDestinationAddresses(data,0,mac_addr,ip_addr);
	}

	/** Changes MAC and IP destination addresses of the given Ethernet frame. */
	public static void changeFrameMacIpDestinationAddresses(byte[] buf, int off, String mac_addr, String ip_addr) {
		ByteUtils.copy(ByteUtils.hexToBytes(mac_addr),0,buf,off,6);
		ipv4addrToBytes(ip_addr,buf,off+14+16);
		updateIPv4HeaderChecksum(buf,off+14);
	}

	/** Changes MAC and IP destination addresses of a VLAN-tagged Ethernet frame. */
	public static void changeVlanFrameMacIpDestinationAddresses(byte[] data, String mac_addr, String ip_addr) {
		changeVlanFrameMacIpDestinationAddresses(data,0,mac_addr,ip_addr);
	}

	/** Changes MAC and IP destination addresses of a VLAN-tagged Ethernet frame. */
	public static void changeVlanFrameMacIpDestinationAddresses(byte[] buf, int off, String mac_addr, String ip_addr) {
		ByteUtils.copy(ByteUtils.hexToBytes(mac_addr),0,buf,off,6);
		ipv4addrToBytes(ip_addr,buf,off+14+4+16);
		updateIPv4HeaderChecksum(buf,off+14+4);
	}

	/** Changes MAC destination addresses of a VLAN-tagged Ethernet frame. */
	public static void changeVlanFrameMacDestinationAddresses(byte[] data, String mac_addr) {
		changeVlanFrameMacDestinationAddresses(data,0,mac_addr);
	}

	/** Changes MAC destination addresses of a VLAN-tagged Ethernet frame. */
	public static void changeVlanFrameMacDestinationAddresses(byte[] buf, int off, String mac_addr) {
		ByteUtils.copy(ByteUtils.hexToBytes(mac_addr),0,buf,off,6);
	}

}
