/*
 * Copyright (c) 2023 Bernhard Haumacher et al. All Rights Reserved.
 */
package org.mjsip.sip.address;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Test for {@link SipURI} parsing.
 */
@SuppressWarnings("javadoc")
public class TestSipURI {

	@Test
	public void testParse() {
		SipURI uri = SipURI.parseSipURI("host.only");
		Assertions.assertEquals("sip", uri.getScheme());
		Assertions.assertFalse(uri.isSecure());
		Assertions.assertEquals("host.only", uri.getHost());
	}

	@Test
	public void testSip() {
		SipURI uri = SipURI.parseSipURI("sip:host.only");
		Assertions.assertEquals("sip", uri.getScheme());
		Assertions.assertFalse(uri.isSecure());
		Assertions.assertEquals("host.only", uri.getHost());
	}

	@Test
	public void testParseSips() {
		SipURI uri = SipURI.parseSipURI("sips:host.only");
		Assertions.assertEquals("sips", uri.getScheme());
		Assertions.assertTrue(uri.isSecure());
		Assertions.assertEquals("host.only", uri.getHost());
	}

	@Test
	public void testParsePort() {
		SipURI uri = SipURI.parseSipURI("host.only:5060");
		Assertions.assertEquals("host.only", uri.getHost());
		Assertions.assertEquals(5060, uri.getPort());
	}

	@Test
	public void testParseUser() {
		SipURI uri = SipURI.parseSipURI("foo@host.only:5060");
		Assertions.assertEquals("foo", uri.getUserName());
		Assertions.assertEquals("host.only", uri.getHost());
		Assertions.assertEquals(5060, uri.getPort());
	}

	@Test
	public void testParsePassword() {
		SipURI uri = SipURI.parseSipURI("foo:secure@host.only:5060");
		Assertions.assertEquals("foo", uri.getUserName());
		Assertions.assertEquals("secure", uri.getPassword());
		Assertions.assertEquals("host.only", uri.getHost());
		Assertions.assertEquals(5060, uri.getPort());
	}

	@Test
	public void testParsePasswordWithoutPort() {
		SipURI uri = SipURI.parseSipURI("foo:secure@host.only");
		Assertions.assertEquals("foo", uri.getUserName());
		Assertions.assertEquals("secure", uri.getPassword());
		Assertions.assertEquals("host.only", uri.getHost());
		Assertions.assertEquals(-1, uri.getPort());
	}

	@Test
	public void testParseParamWithoutValue() {
		SipURI uri = SipURI.parseSipURI("host.only;param");
		Assertions.assertEquals("host.only", uri.getHost());
		Assertions.assertTrue(uri.hasParameter("param"));
		Assertions.assertEquals(null, uri.getParameter("param"));
	}

	@Test
	public void testParseParam() {
		SipURI uri = SipURI.parseSipURI("host.only;key=value");
		Assertions.assertEquals("host.only", uri.getHost());
		Assertions.assertTrue(uri.hasParameter("key"));
		Assertions.assertEquals("value", uri.getParameter("key"));
	}

	@Test
	public void testIPv6() {
		SipURI uri = SipURI.parseSipURI("[fe80::43c6:1e57:8a59:ce55]");
		Assertions.assertEquals("[fe80::43c6:1e57:8a59:ce55]", uri.getHost());
	}

	@Test
	public void testIPv6WithPort() {
		SipURI uri = SipURI.parseSipURI("[fe80::43c6:1e57:8a59:ce55]:5060");
		Assertions.assertEquals("[fe80::43c6:1e57:8a59:ce55]", uri.getHost());
		Assertions.assertEquals(5060, uri.getPort());
	}

	@Test
	public void testIPv6WithUser() {
		SipURI uri = SipURI.parseSipURI("foo@[fe80::43c6:1e57:8a59:ce55]");
		Assertions.assertEquals("foo", uri.getUserName());
		Assertions.assertEquals("[fe80::43c6:1e57:8a59:ce55]", uri.getHost());
	}

	@Test
	public void testIPv6WithPortAndUser() {
		SipURI uri = SipURI.parseSipURI("foo:secure@[fe80::43c6:1e57:8a59:ce55]:5060");
		Assertions.assertEquals("foo", uri.getUserName());
		Assertions.assertEquals("secure", uri.getPassword());
		Assertions.assertEquals("[fe80::43c6:1e57:8a59:ce55]", uri.getHost());
		Assertions.assertEquals(5060, uri.getPort());
	}

	@Test
	public void testIPv6WithUserAndParam() {
		SipURI uri = SipURI.parseSipURI("foo@[fe80::43c6:1e57:8a59:ce55]:5060;key0=value0;key1");
		Assertions.assertEquals("foo", uri.getUserName());
		Assertions.assertEquals("[fe80::43c6:1e57:8a59:ce55]", uri.getHost());
		Assertions.assertEquals(5060, uri.getPort());
		Assertions.assertTrue(uri.hasParameter("key0"));
		Assertions.assertTrue(uri.hasParameter("key1"));
	}

	@Test
	public void testRandom() {
		Random rnd = new Random(42);
		for (int n = 0; n < 1024; n++) {
			StringBuilder buffer = new StringBuilder();
			for (TestPart part : PARTS) {
				part.build(buffer, rnd);
			}
			String source = buffer.toString();
			SipURI uri = SipURI.parseSipURI(source);
			for (TestPart part : PARTS) {
				part.check(source, uri);
			}
		}
	}

	public static TestPart[] PARTS = { new TestUser(), new TestHost(), new TestPort(), new TestParam(),
			new TestHeader() };

	static abstract class TestPart {
		abstract void build(StringBuilder buffer, Random rnd);

		abstract void check(String source, SipURI uri);
	}

	static class TestUser extends TestPart {
		private boolean _user;
		private boolean _password;

		@Override
		void build(StringBuilder buffer, Random rnd) {
			_user = rnd.nextBoolean();
			if (_user) {
				buffer.append("foo");

				_password = rnd.nextBoolean();
				if (_password) {
					buffer.append(":secure");
				}
				buffer.append("@");
			} else {
				_password = false;
			}
		}

		@Override
		void check(String source, SipURI uri) {
			Assertions.assertEquals(_user ? "foo" : null, uri.getUserName(), source);
			Assertions.assertEquals(_password ? "secure" : null, uri.getPassword(), source);
		}
	}

	static class TestHost extends TestPart {
		private boolean _ipv6;

		@Override
		void build(StringBuilder buffer, Random rnd) {
			_ipv6 = rnd.nextBoolean();
			buffer.append(host());
		}

		private String host() {
			return _ipv6 ? "[fe80::43c6:1e57:8a59:ce55]" : "myhost.org";
		}

		@Override
		void check(String source, SipURI uri) {
			Assertions.assertEquals(host(), uri.getHost(), source);
		}
	}

	static class TestPort extends TestPart {
		private int _port;

		@Override
		void build(StringBuilder buffer, Random rnd) {
			_port = rnd.nextBoolean() ? 5060 : -1;
			if (_port > 0) {
				buffer.append(':');
				buffer.append(_port);
			}
		}

		@Override
		void check(String source, SipURI uri) {
			Assertions.assertEquals(_port, uri.getPort(), source);
		}
	}

	static class TestParam extends TestPart {
		private Map<String, String> _param = new LinkedHashMap<>();

		@Override
		void build(StringBuilder buffer, Random rnd) {
			_param.clear();

			int cnt = rnd.nextInt(3);
			for (int n = 0; n < cnt; n++) {
				String key = "key" + n;

				buffer.append(';');
				buffer.append(key);

				String value;
				if (rnd.nextBoolean()) {
					value = "value" + n;

					buffer.append('=');
					buffer.append(value);
				} else {
					value = null;
				}
				_param.put(key, value);
			}
		}

		@Override
		void check(String source, SipURI uri) {
			for (Entry<String, String> param : _param.entrySet()) {
				Assertions.assertTrue(uri.hasParameter(param.getKey()), source);
				Assertions.assertEquals(param.getValue(), uri.getParameter(param.getKey()), source);
			}
		}
	}

	static class TestHeader extends TestPart {
		private Map<String, String> _headers = new LinkedHashMap<>();

		@Override
		void build(StringBuilder buffer, Random rnd) {
			_headers.clear();

			int cnt = rnd.nextInt(3);
			for (int n = 0; n < cnt; n++) {
				String key = "key" + n;

				buffer.append(n == 0 ? '?' : '&');
				buffer.append(key);

				String value;
				if (rnd.nextBoolean()) {
					value = "value" + n;

					buffer.append('=');
					buffer.append(value);
				} else {
					value = null;
				}
				_headers.put(key, value);
			}
		}

		@Override
		void check(String source, SipURI uri) {
			for (Entry<String, String> param : _headers.entrySet()) {
				Assertions.assertTrue(uri.hasHeader(param.getKey()), source);
				Assertions.assertEquals(param.getValue(), uri.getHeader(param.getKey()), source);
			}
		}
	}

}
