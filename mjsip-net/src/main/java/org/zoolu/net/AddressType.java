package org.zoolu.net;

/**
 * Choice of addresses.
 */
public enum AddressType {
	/**
	 * Select either an IPv4 or IPv6 address depending on the context.
	 */
	DEFAULT, 
	
	/**
	 * Use an IPv4 address.
	 */
	IP4, 
	
	/**
	 * Use an IPv6 address.
	 */
	IP6;

	public static AddressType fromString(String type) {
		switch (type.toUpperCase()) {
		case "IP4": return IP4;
		case "IP6": return IP6;
		}
		return DEFAULT;
	}
}
