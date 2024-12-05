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

package org.zoolu.util;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;

import org.slf4j.LoggerFactory;

/** Configure helps the loading and saving of configuration data.
  */
public abstract class Configure {
	
	private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(Configure.class);
	
	/** String 'NONE' used as undefined value (i.e. null). */
	public static String NONE="NONE";

	/** Constructs a Configure container */
	protected Configure() {
		super();
	}

	/** Loads Configure attributes from the specified <i>file</i> */
	public void loadFile(File file) {
		if (file==null) {
			return;
		}

		try {
			readFrom(new FileReader(file));
		}
		catch (Exception e) {
			LOG.warn("error reading file ({})", file, e);
			return;
		}
	}

	/** Loads Configure attributes from the specified URL <i>url</i> */
	public void loadFile(URL url) {
		if (url==null) {
			return;
		}
		//else
		try {
			readFrom(new InputStreamReader(url.openStream()));
		}
		catch (Exception e) {
			LOG.warn("error reading from ({})", url, e);
			return;
		}
	}

	/** Reads Configure attributes from the specified Reader <i>rd</i> */
	public void readFrom(Reader rd) throws java.io.IOException {
		BufferedReader in=new BufferedReader(rd);           
		while (true) {
			String line=null;
			try { line=in.readLine(); } catch (Exception e) { e.printStackTrace(); System.exit(0); }
			if (line==null) break;

			line = line.trim();
			if (line.isEmpty()) {
				continue;
			}
			if (line.startsWith("#")) {
				continue;
			}
			
			parseLine(line);
		} 
		in.close();
	}

	/** Parses a single text line (read from the config file) */
	protected void parseLine(String line) {
		String attribute;
		Parser par;
		int index=line.indexOf("=");
		if (index>0) {  attribute=line.substring(0,index).trim(); par=new Parser(line,index+1);  }
		else {  attribute=line; par=new Parser("");  }
	
		setOption(attribute, par);
	}

	/** Parses a single text line (read from the config file) */
	public abstract void setOption(String attribute, Parser par);

}
