/*
 * Copyright (C) 2010 Luca Veltri - University of Parma - Italy
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

package org.mjsip.sdp.field;



import org.zoolu.util.Base64;
import org.zoolu.util.Parser;



/** SRTP-key-param of a SRTP crypto attribute field.
  */
public class SrtpKeyParam extends KeyParam {
	
	/** Creates a new SrtpKeyParam. */ 
	public SrtpKeyParam(byte[] key_salt, String lifetime, String mki_len) {
		super("inline",Base64.encode(key_salt)+"|"+lifetime+"|"+mki_len);
	}

	/** Creates a new SrtpKeyParam. */ 
	/*public SrtpKeyParam(byte[] key_salt, String lifetime, int mki, int mki_len) {
		super("inline",Base64.encode(key_salt)+"|"+lifetime+"|"+mki+":"+mki_len);
	}*/

	/** Creates a new SrtpKeyParam. */ 
	public SrtpKeyParam(String key_info) {
		super("inline",key_info);
	}

	/** Creates a new SrtpKeyParam. */ 
	public SrtpKeyParam(KeyParam kp) {
		super(kp);
	}

	/** Gets the key and salt. */
	public byte[] getKeySalt() {
		Parser par=new Parser(value);
		char[] delim={'|'};
		return Base64.decode(par.goTo(':').skipChar().getWord(delim));
	}

	/** Gets the lifetime as String. */
	public String getLifetimeString() {
		Parser par=new Parser(value);
		char[] delim={'|'};
		String lifetime=par.goTo(':').goTo('|').skipChar().getWord(delim);
		if (lifetime!=null && lifetime.indexOf(':')<0) return lifetime;
		// else
		return null;
	}

	/** Gets colon-separated MKI and the MKI field length. */
	public String getMkiLen() {
		Parser par=new Parser(value);
		par.goTo(':').goTo('|').skipChar();
		if (par.indexOf('|')>0) par.goTo('|').skipChar();
		if (par.indexOf(':')>0) return par.getString();
		// else
		return null;
	}

	/** Gets the MKI. */
	/*public int getMki() {
		Parser par=new Parser(value);
		par.goTo(':').goTo('|').skipChar();
		if (par.indexOf('|')>0) par.goTo('|').skipChar();
		char[] delim={':'};
		if (par.indexOf(':')>0) return Integer.parseInt(par.getWord(delim));
		// else
		else return -1;
	}*/

	/** Gets the MKI length. */
	/*public int getMkiLen() {
		Parser par=new Parser(value);
		par.goTo(':').goTo('|').skipChar();
		if (par.indexOf('|')>0) par.goTo('|').skipChar();
		if (par.indexOf(':')>0) return Integer.parseInt(par.goTo(':').skipChar().getString());
		// else
		else return -1;
	}*/



	/** Test main method. */
	/*public static void main(String[] args) {
		
		java.io.BufferedReader in=new java.io.BufferedReader(new java.io.InputStreamReader(System.in));
		try {
			String line=in.readLine();
			double n=Double.parseDouble(line);
			System.out.println("value= "+Double.toString(n));
		}
		catch (Exception e) {  e.printStackTrace();  }
		
		//System.out.println("value= "+Math.pow(2,40));
	}*/

}
