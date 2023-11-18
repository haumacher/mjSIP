/*
 * Copyright (c) 2023 Bernhard Haumacher et al. All Rights Reserved.
 */
package org.mjsip.sip.address;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Parser for {@link SipURI}s.
 */
public class SipURIParser {

	int pos;

	private String uri;

	boolean secure;

	String user = null;

	String password = null;

	String host = null;

	boolean ipv6 = false;

	int port = -1;

	Map<String, String> params = new LinkedHashMap<>();

	Map<String, String> headers = new LinkedHashMap<>();

	/**
	 * Creates a {@link SipURIParser}.
	 */
	public SipURIParser(String uri) {
		this.uri = uri;
	}

	/**
	 * Parses and return the {@link SipURI}.
	 * 
	 * <p>
	 * <code>sip[s]:user:password@host:port;uri-parameters?headers</code>
	 * </p>
	 */
	public SipURI parse() {
		if (uri.startsWith(SipURI.SIP_COLON)) {
			secure = false;
			pos = SipURI.SIP_COLON.length();
		} else if (uri.startsWith(SipURI.SIPS_COLON)) {
			secure = true;
			pos = SipURI.SIPS_COLON.length();
		} else {
			secure = false;
			pos = 0;
		}

		int index = next(new char[] { ':', '@', '[', ';', '?' });
		if (index < 0) {
			host = remaining();
			return uri();
		}

		switch (current(index)) {
		case ':': {
			return parseUserOrPort(index);
		}

		case '@': {
			user = consume(index);
			return parseHost();
		}

		case '[': {
			skip(index);
			return parseIPv6();
		}

		case ';': {
			host = consume(index);
			return parseParams();
		}
		case '?': {
			host = consume(index);
			return parseHeaders();
		}
		}

		throw new AssertionError("Unreachable");
	}

	private SipURI parseUserOrPort(int index) {
		String token = consume(index);

		int next = next('@', ';', '?');
		if (next < 0) {
			// This was the port.
			host = token;
			port = port(remaining());
			return uri();
		}
		
		switch (current(next)) {
		case '@': {
			// Was user and password.
			user = token;
			password = consume(next);
			return parseHost();
		}

		case ';': {
			host = token;
			port = port(consume(next));
			return parseParams();
		}

		case '?': {
			host = token;
			port = port(consume(next));
			return parseHeaders();
		}
		}

		throw new AssertionError("Unreachable");
	}

	/**
	 * <code>host:port;uri-parameters?headers</code>
	 */
	private SipURI parseHost() {
		int index = next(new char[] { ':', '[', ';', '?' });
		if (index < 0) {
			if (done()) {
				throw new IllegalArgumentException("Missing host at position " + pos + ".");
			}
			host = remaining();
			return uri();
		}

		switch (current(index)) {
		case '[': {
			skip(index);
			return parseIPv6();
		}

		case ':': {
			host = consume(index);
			return parsePort();
		}

		case ';': {
			host = consume(index);
			return parseParams();
		}

		case '?': {
			host = consume(index);
			return parseHeaders();
		}
		}

		throw new AssertionError("Unreachable");
	}

	/**
	 * <code>[fe80::43c6:1e57:8a59:ce55]:port;uri-parameters?headers</code>
	 */
	private SipURI parseIPv6() {
		int index = next(']');
		if (index < 0) {
			throw parseError(pos);
		}

		host = consume(index);
		ipv6 = true;
		
		index = next(':', ';', '?');

		if (index < 0) {
			if (done()) {
				return uri();
			} else {
				parseError(pos);
			}
		}

		skip(index);

		switch (current(index)) {
		case ':': {
			return parsePort();
		}

		case ';': {
			return parseParams();
		}

		case '?': {
			return parseHeaders();
		}
		}
		
		throw new AssertionError("Unreachable");
	}

	private void skip(int index) {
		pos = index + 1;
	}

	/**
	 * <code>port;uri-parameters?headers</code>
	 */
	private SipURI parsePort() {
		int index = next(';', '?');

		if (index < 0) {
			if (done()) {
				throw new IllegalArgumentException("Missing port at position " + pos + ".");
			} else {
				port = port(remaining());
				return uri();
			}
		}

		switch (current(index)) {
		case ';': {
			port = port(consume(index));
			return parseParams();
		}

		case '?': {
			port = port(consume(index));
			return parseHeaders();
		}
		}

		throw new AssertionError("Unreachable");
	}

	private int port(String token) {
		return Integer.parseInt(token);
	}

	private SipURI parseParams() {
		while (true) {
			int index = next(';', '?');

			if (index < 0) {
				if (done()) {
					throw new IllegalArgumentException("Missing param at position " + pos + ".");
				} else {
					parseKeyValue(params, uri.length());
					return uri();
				}
			}

			switch (current(index)) {
			case ';': {
				parseKeyValue(params, index);
				continue;
			}

			case '?': {
				parseKeyValue(params, index);
				return parseHeaders();
			}
			}

			throw new AssertionError("Unreachable");
		}
	}

	private void parseKeyValue(Map<String, String> map, int limit) {
		int index = nextLimit(limit, '=');
		if (index < 0) {
			String key = consume(limit);
			map.put(key, null);
		} else {
			String key = consume(index);
			String value = consume(limit);
			map.put(key, value);
		}
	}

	private SipURI parseHeaders() {
		while (true) {
			int index = next('&');

			if (index < 0) {
				if (done()) {
					throw new IllegalArgumentException("Missing header at position " + pos + ".");
				} else {
					parseKeyValue(headers, uri.length());
					return uri();
				}
			}

			parseKeyValue(headers, index);
		}
	}

	private boolean done() {
		return pos == uri.length();
	}

	private String consume(int index) {
		String token = uri.substring(pos, index);
		pos = index + 1;
		return token;
	}

	private String consumeInclusive(int index) {
		int next = index + 1;
		String token = uri.substring(pos, next);
		pos = next;
		return token;
	}

	private int current(int index) {
		return uri.charAt(index);
	}

	private SipURI uri() {
		return new SipURI(user, password, host, ipv6, port, secure, params, headers);
	}

	private String remaining() {
		return uri.substring(pos);
	}

	/**
	 * Finds the position of the next separator character given in the character array.
	 */
	private int next(char... separators) {
		return nextLimit(uri.length(), separators);
	}

	private int nextLimit(int limit, char... separators) {
		for (int n = pos; n < limit; n++) {
			char ch = uri.charAt(n);
			for (char sep : separators) {
				if (ch == sep) {
					return n;
				}
			}
		}
		return -1;
	}

	private IllegalArgumentException parseError(int pos) {
		if (pos >= uri.length()) {
			return new IllegalArgumentException("Unexpected end of URI: " + uri);
		} else {
			return new IllegalArgumentException(
					"Unexpected character '" + uri.charAt(pos) + "' at position " + pos + ": " + uri);
		}
	}

}
