/*
 * Copyright (C) 2005 Luca Veltri - University of Parma - Italy
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

package org.mjsip.sip.provider;



import org.mjsip.sip.message.SipMessage;



/** MethodId is used to identify messages that refer to given method.
  */
public class MethodId extends SipId {
	
	/** Identifier for ANY messages (regardless their method). */
	public static final MethodId ANY=new MethodId("ANY"); 


	/** Creates a new MethodId.
	  * @param method the method name */
	public MethodId(String method) {
		super(getMethodId(method));
	}

	/** Creates a new MethodId.
	  * @param msg a SIP massage */
	public MethodId(SipMessage msg) {
		super(getMethodId(msg.getCSeqHeader().getMethod()));
	}

	/** Creates a new MethodId
	  * @param id a method identifier */
	public MethodId(MethodId id) {
		super(id);
	}

	/** Gets the string value of the method identifier.
	  * @param msg a SIP massage
	  * @return the method identifier */
	private static String getMethodId(String method) {
		return method;
	}
}
