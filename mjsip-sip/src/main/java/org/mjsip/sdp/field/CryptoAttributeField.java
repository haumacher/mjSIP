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



import java.util.Vector;

import org.mjsip.sdp.AttributeField;
import org.mjsip.sdp.SdpField;
import org.zoolu.util.Parser;



/** SDP crypto attribute field.
  * <p>
  * <BLOCKQUOTE><PRE>
  *    a=crypto:&lt;tag&gt; &lt;crypto-suite&gt; &lt;key-params&gt; [&lt;session-params&gt;]
  * </PRE></BLOCKQUOTE>
  * <p>
  * where:
  * <ul>
  *   <li> tag is a decimal number used as an identifier for a particular crypto attribute;</li>
  *   <li> crypto-suite is an identifier that describes the encryption and authentication algorithms (e.g., AES_CM_128_HMAC_SHA1_80);</li>
  *   <li> key-params provides one or more sets of keying material for the crypto-suite;</li>
  *   <li> session-params are OPTIONAL parameters, specific to the given secure transport.</li>
  * </ul>
  * <p>
  * Example:
  * <BLOCKQUOTE><PRE>
  *    a=crypto:1 AES_CM_128_HMAC_SHA1_80 inline:PS1uQCVeeCFCanVmcjkpPywjNWhcYD0mXXtxaVBR|2^20|1:32
  * </PRE></BLOCKQUOTE>
  */
public class CryptoAttributeField extends AttributeField {
	
	/** Crypto suite AES_CM_128_HMAC_SHA1_80 */
	public static final String CRYPTO_AES_CM_128_HMAC_SHA1_80="AES_CM_128_HMAC_SHA1_80";

	/** Crypto suite AES_CM_128_HMAC_SHA1_32 */
	public static final String CRYPTO_AES_CM_128_HMAC_SHA1_32="AES_CM_128_HMAC_SHA1_32";

	/** Crypto suite F8_HMAC_SHA1_80 */
	public static final String CRYPTO_F8_HMAC_SHA1_80="F8_HMAC_SHA1_80";


	/** Attribute name "crypto" */
	//static final String A_NAME="crypto";



	/** Creates a new CryptoAttributeField without session parameters.
	  * @param tag the tag value
	  * @param crypto_suite the crypto-suite value
	  * @param key_params the key-params value
	  */
	public CryptoAttributeField(int tag, String crypto_suite, String key_params) {
		super("crypto",String.valueOf(tag)+" "+crypto_suite+" "+key_params);
	}


	/** Creates a new CryptoAttributeField without session parameters.
	  * @param tag the tag value
	  * @param crypto_suite the crypto-suite value
	  * @param key_params the key-params as Vector of KeyParams
	  */
	public CryptoAttributeField(int tag, String crypto_suite, Vector key_params) {
		super("crypto",String.valueOf(tag)+" "+crypto_suite);
		if (key_params==null) return;
		// else
		for (int i=0; i<key_params.size(); i++) value+=((i==0)?' ':';')+((KeyParam)key_params.elementAt(i)).toString();
	}


	/** Creates a new CryptoAttributeField without session parameters.
	  * @param tag the tag value
	  * @param crypto_suite the crypto-suite value
	  * @param key_method the key-method value
	  * @param key_info the key-info value
	  */
	//public CryptoAttributeField(int tag, String crypto_suite, String key_method, String key_info)
	//{  super("crypto",String.valueOf(tag)+" "+crypto_suite+" "+key_method+":"+key_info);
	//}


	/** Creates a new CryptoAttributeField/
	  * @param a_value the entire crypto attribute value
	  */
	public CryptoAttributeField(String a_value) {
		super("crypto",a_value);
	}


	/** Creates a new CryptoAttributeField.
	  * @param sf a crypto attribute SdpField
	  */
	public CryptoAttributeField(SdpField sf) {
		super(sf);
	}



	/** Adds a session-param. */
	public void addSessionParam(String session_param) {
		value+=" "+session_param;
	}


	/** Adds a session-params. */
	public void addSessionParams(Vector session_params) {
		for (int i=0; i<session_params.size(); i++) addSessionParam((String)session_params.elementAt(i));
	}


	/** Gets the tag value. */
	public int getTag() {
		Parser par=new Parser(getAttributeValue());
		return par.getInt();
	}


	/** Gets the crypto-suite. */
	public String getCryptoSuite() {
		Parser par=new Parser(getAttributeValue());
		return par.skipString().getString();
	}


	/** Gets the key-params as a single String. */
	String getKeyParamsAsString() {
		Parser par=new Parser(getAttributeValue());
		return par.skipString().skipString().getString();
	}


	/** Gets the key-params values (as a Vector of KeyParam). */
	public Vector getKeyParams() {
		Parser par=new Parser(getKeyParamsAsString());
		char[] delim={';'};   
		Vector aux=par.getWordVector(delim);
		if (aux==null) return null;
		// else
		Vector res=new Vector();
		for (int i=0; i<aux.size(); i++) res.addElement(new KeyParam((String)aux.elementAt(i)));
		return res;
	}


	/** Gets session-params values (as a Vector of Strings). */
	public Vector getSessionParams() {
		Parser par=new Parser(getAttributeValue());
		return par.skipString().skipString().skipString().getStringVector();
	}

}
