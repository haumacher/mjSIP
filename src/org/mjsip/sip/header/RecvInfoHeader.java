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

package org.mjsip.sip.header;



import java.util.Vector;



/** SIP header field Recv-Info, defined by RFC 6086.
  */
public class RecvInfoHeader extends ListHeader {
	

	/** Creates a new RecvInfoHeader. */
	public RecvInfoHeader(Header hd) {
		super(hd);
	}

	/** Creates a new RecvInfoHeader. */
	public RecvInfoHeader(String hvalue) {
		super(SipHeaders.Recv_Info,hvalue);
	}

	/** Creates a new RecvInfoHeader. */
	public RecvInfoHeader(Vector packages) {
		super(SipHeaders.Recv_Info,packages);
	}

	/** Creates a new RecvInfoHeader. */
	public RecvInfoHeader(String[] packages) {
		super(SipHeaders.Recv_Info,packages);
	}


	/** Gets list of packages (as Vector of Strings). */
	public Vector getPackages() {
		return super.getElements();
	}

	/** Sets the list of methods. */
	public void setPackages(Vector packages) {
		super.setElements(packages);
	}

	/** Adds a new method to the methods list. */
	public void addPackages(String packages) {
		super.addElement(packages);
	}
}
