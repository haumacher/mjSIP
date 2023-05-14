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



import org.mjsip.sip.provider.SipParser;



/** SIP RAck header field.
  * It contains two numbers and a method tag.
  * <p>
  * The first number is the value from the RSeq header in the provisional
  * response that is being acknowledged.  The next number, and the
  * method, are copied from the CSeq in the response that is being
  * acknowledged.
  */
public class RAckHeader extends Header {
	

	/** Creates a new RAckHeader. */
	public RAckHeader(Header hd) {
		super(hd);
	}

	/** Creates a new RAckHeader. */
	public RAckHeader(String hvalue) {
		super(SipHeaders.RAck,hvalue);
	}

	/** Creates a new RAckHeader. */
	public RAckHeader(long rseq, long cseq, String method) {
		super(SipHeaders.RAck,String.valueOf(rseq)+" "+String.valueOf(cseq)+" "+method);
	}

	/** Gets RAck sequence number. */
	public long getRAckSequenceNumber() {
		return (new SipParser(value)).getLong();
	}

	/** Gets CSeq sequence number. */
	public long getCSeqSequenceNumber() {
		return (new SipParser(value)).skipString().getLong();
	}

	/** Gets CSeq method. */
	public String getCSeqMethod() {
		return (new SipParser(value)).skipString().skipString().getString();
	}

}

