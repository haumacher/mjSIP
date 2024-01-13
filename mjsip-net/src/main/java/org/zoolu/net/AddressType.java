package org.zoolu.net;

/**
 * Choice of addresses.
 */
public enum AddressType {
	DEFAULT, IP4, IP6;

	public static AddressType fromString(String type) {
		switch (type.toUpperCase()) {
		case "IP4": return IP4;
		case "IP6": return IP6;
		}
		return DEFAULT;
	}
}
