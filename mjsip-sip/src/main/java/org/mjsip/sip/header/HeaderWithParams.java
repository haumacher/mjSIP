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


import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;


/** Abstract ParametricHeader is the base class for all SIP Headers that include parameters */
public abstract class HeaderWithParams extends Header {

	private final Map<String, String> _parameters = new LinkedHashMap<>();

	/** Creates a new ParametricHeader. */
	protected HeaderWithParams(String hname) {
		super(hname);
	}

	/** Creates a new ParametricHeader. */
	protected HeaderWithParams(Header hd) {
		super(hd);
		if (hd instanceof HeaderWithParams) {
			_parameters.putAll(((HeaderWithParams) hd)._parameters);
		}
	}

	/** Gets the value of specified parameter.
	  * @return Returns the value of the specified parameter or null if not present. */
	public String getParameter(String pname)  {
		return _parameters.get(pname);
	}
	 
	/** Gets a String Vector of parameter names.
	  * @return Returns a String Vector of all parameter names or null if no parameter is present. */
	public Set<String> getParameterNames() {
		return _parameters.keySet();
	}

	/** Whether there is the specified parameter */
	public boolean hasParameter(String pname) {
		return _parameters.containsKey(pname);
	}

	/** Whether there are any parameters */
	public boolean hasParameters() {
		return !_parameters.isEmpty();
	}

	/** Removes all parameters (if any) */
	public void removeParameters()  {
		_parameters.clear();
	}

	/** Removes specified parameter (if present) */
	public void removeParameter(String pname)  {
		_parameters.remove(pname);
	}

	/** Sets the value of a specified parameter.
	  * Zero-length String is returned in case of flag parameter (without value). */
	public void setParameter(String pname, String pvalue)  {
		_parameters.put(pname, pvalue);
	}

	/**
	 * Fills the parameters of the given header.
	 */
	protected void parseParams(Analyzer in) {
		while (in.currentChar() == ';') {
			// generic-param
			String pName = in.skip().skipWSPCRLF().findChars(" \t\r\n=;").stringBefore();
			in.skipWSPCRLF();

			String pValue;
			if (in.currentChar() == ';' || in.eof()) {
				pValue = null;
			} else {
				in.skip().skipWSPCRLF();

				if (in.currentChar() == '"') {
					in.skip();

					StringBuilder contents = new StringBuilder();
					while (true) {
						in.findChars("\\\"");
						if (in.currentChar() == '"' || in.eof()) {
							contents.append(in.stringBefore());
							in.skipWSPCRLF();
							break;
						} else {
							in.skip();
							if (in.eof()) {
								break;
							}
							contents.append(in.getChar());
						}
					}
					pValue = contents.toString();
				} else {
					pValue = in.findChars(" \t\r\n;,").stringBefore();
					in.skipWSPCRLF();
				}
			}

			setParameter(pName, pValue);
		}
	}

	protected final void appendParams(StringBuilder buffer) {
		for (Entry<String, String> entry : _parameters.entrySet()) {
			buffer.append(';');
			buffer.append(entry.getKey());

			String value = entry.getValue();
			if (value != null) {
				buffer.append('=');
				if (mustQuote(value)) {
					appendQuoted(buffer, value);
				} else {
					buffer.append(value);
				}
			}
		}
	}

	private void appendQuoted(StringBuilder buffer, String value) {
		buffer.append('"');
		for (int n = 0, cnt = value.length(); n < cnt; n++) {
			char ch = value.charAt(n);
			switch (ch) {
			case '"':
			case '\\':
				buffer.append('\\');
				buffer.append(ch);
				break;
			default:
				buffer.append(ch);
				break;
			}
		}
		buffer.append('"');
	}

	private boolean mustQuote(String value) {
		return containsAny(value, " \t\r\n\"");
	}

	private boolean containsAny(String value, String chars) {
		for (int n = 0, cnt = value.length(); n < cnt; n++) {
			if (chars.indexOf(value.charAt(n)) >= 0) {
				return true;
			}
		}
		return false;
	}

	/**
	 * TODO
	 *
	 * @param value
	 * @param c
	 * @return
	 */
	private boolean containe(String value, char c) {
		// TODO: Automatically created
		return false;
	}

}
