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

package org.mjsip.sip.header;



import java.util.Vector;



/** SIP Header Supported.
  */
public class SupportedHeader extends OptionTagsHeader {
	

	/** Creates a new SupportedHeader.
	  * @param option_tags Vector (of <code>String</code>) of option-tags. */
	public SupportedHeader(Vector option_tags) {
		super(SipHeaders.Supported,option_tags);
	}

	/** Creates a new SupportedHeader. 
	  * @param option_tags array of option-tags. */
	public SupportedHeader(String[] option_tags) {
		super(SipHeaders.Supported,option_tags);
	}

	/** Creates a new SupportedHeader.
	  * @param option_tag a single option-tag or a comma-separated list of option-tags. */
	public SupportedHeader(String option_tag) {
		super(SipHeaders.Supported,option_tag);
	}


	/** Creates a new SupportedHeader. */
	public SupportedHeader(Header hd) {
		super(hd);
	}
}
