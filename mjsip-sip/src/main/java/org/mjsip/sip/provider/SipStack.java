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
package org.mjsip.sip.provider;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Global constants for SIP.
 */
public class SipStack {

	/** Version */
	public static final String version;

	/** Release */
	public static final String release;

	/** Extension option tag "100rel". */
	public static final String OTAG_100rel="100rel";

	/** Extension option tag "timer". */
	public static final String OTAG_timer="timer";

	/** Extension option tag "precondition". */
	public static final String OTAG_precondition="precondition";

	static {
		String v;
		try (InputStream in = SipStack.class
				.getResourceAsStream("/META-INF/maven/org.mjsip/mjsip-sip/pom.properties")) {
			if (in == null) {
				// This may happen, if no Maven builder is used in a development environment
				v = "development";
			} else {
				Properties properties = new Properties();
				properties.load(in);
				v = properties.getProperty("version");
			}
		} catch (IOException ex) {
			v = "unknown";
		}
		version = v;
		release = "mjsip " + v;
	}
}
