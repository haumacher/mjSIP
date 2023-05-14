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

import org.zoolu.util.Parser;



/** SIP Header that carries a list of option-tags.
  */
public abstract class OptionTagsHeader extends Header {
	
	/** Creates a new OptionTagsHeader.
	  * @param option_tags Vector (of <code>String</code>) of option-tags. */
	protected OptionTagsHeader(String header, Vector option_tags) {
		super(header,null);
		if (option_tags!=null && option_tags.size()>0) {
			StringBuffer sb=new StringBuffer((String)option_tags.elementAt(0));
			for (int i=1; i<option_tags.size(); i++) sb.append(',').append((String)option_tags.elementAt(i));
			value=sb.toString();
		}
	}

	/** Creates a new OptionTagsHeader. 
	  * @param option_tags array of option-tags. */
	protected OptionTagsHeader(String header, String[] option_tags) {
		super(header,null);
		if (option_tags!=null && option_tags.length>0) {
			StringBuffer sb=new StringBuffer(option_tags[0]);
			for (int i=1; i<option_tags.length; i++) sb.append(',').append(option_tags[i]);
			value=sb.toString();
		}
	}

	/** Creates a new OptionTagsHeader.
	  * @param option_tag a single option-tag or a comma-separated list of option-tags. */
	protected OptionTagsHeader(String header, String option_tag) {
		super(header,option_tag);
	}

	/** Creates a new OptionTagsHeader. */
	protected OptionTagsHeader(Header hd) {
		super(hd);
	}

	/** Gets all option-tags. */
	public Vector getAllOptionTags() {
		if (value==null) return null;
		// else
		final char[] COMMA={','};
		Vector option_tags=new Vector();
		for (Parser par=new Parser(value); par.hasMore(); ) {
			String option_tag=par.getWord(COMMA).trim();
			if (option_tag.length()>0) option_tags.addElement(option_tag);
		}
		return option_tags;
	}

	/** Whether a given option-tag is present. */
	public boolean hasOptionTag(String option_tag) {
		Vector option_tags=getAllOptionTags();
		for (int i=0; option_tags!=null && i<option_tags.size(); i++) if (((String)option_tags.elementAt(i)).equals(option_tag)) return true;
		// else
		return false;
	}

	/** Adds an option-tag. */
	public void addOptionTag(String option_tag) {
		if (value!=null && value.length()>0) value+=","+option_tag;
		else value=option_tag;
	}
}
