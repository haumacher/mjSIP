/*
 * Copyright (C) 2013 Luca Veltri - University of Parma - Italy
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

package org.mjsip.sip.address;




/** Name address with SIP (or SIPS) URI.
  */
public class SipNameAddress {
	

	/** Whether the given NameAddress contains SIPS URI.
	  * @param naddr the NameAddress
	  * @return true if the NameAddress contains a SIPS URI, false otherwise */
	public static boolean isSIPS(NameAddress naddr) {
		GenericURI uri=naddr.getAddress();
		return (uri.isSipURI() && new SipURI(uri).isSecure());
	}


	/** Changes an AOR's URI from SIP to SIPS URI.
	  * @param naddr the AOR
	  * @return new AOR with SIPS URI if it was SIP, otherwise the unchanged original AOR */
	public static NameAddress toSIPS(NameAddress naddr) {
		GenericURI uri=naddr.getAddress();
		if (!uri.isSipURI()) return naddr;
		// else
		SipURI sip_uri=new SipURI(uri);
		if (sip_uri.isSecure()) return naddr;
		// else
		sip_uri.setSecure(true);
		return new NameAddress(naddr.getDisplayName(),sip_uri);
	}


	/** Changes an AOR's URI from SIPS to SIP URI.
	  * @param naddr the AOR
	  * @return new AOR with SIP URI if it was SIPS, otherwise the unchanged original AOR */
	public static NameAddress toSIP(NameAddress naddr) {
		GenericURI uri=naddr.getAddress();
		if (!uri.isSipURI()) return naddr;
		// else
		SipURI sip_uri=new SipURI(uri);
		if (!sip_uri.isSecure()) return naddr;
		// else
		sip_uri.setSecure(false);
		return new NameAddress(naddr.getDisplayName(),sip_uri);
	}

} 
