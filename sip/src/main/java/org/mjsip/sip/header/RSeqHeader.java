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




/** SIP RSeq header field.
  * The RSeq header is used in provisional responses in order to transmit
  * them reliably.
  * <br>
  * It contains a single numeric value from 1 to 2^32 - 1.
  */
public class RSeqHeader extends Header {
	

	/** Creates a new RSeqHeader. */
	public RSeqHeader(Header hd) {
		super(hd);
	}
	
	/** Creates a new RSeqHeader. */
	public RSeqHeader(String hvalue) {
		super(SipHeaders.RSeq,hvalue);
	}

	/** Creates a new RSeqHeader. */
	public RSeqHeader(long rseq) {
		super(SipHeaders.RSeq,String.valueOf(rseq));
	}

	/** Gets sequence number. */
	public long getSequenceNumber() {
		return Long.parseLong(value);
	}

	/** Sets sequence number. */
	public void setSequenceNumber(long rseq) {
		value=String.valueOf(rseq);
	}

	/** Increments sequence number. */
	public RSeqHeader incSequenceNumber() {
		value=String.valueOf(getSequenceNumber()+1);
		return this;
	}
}

