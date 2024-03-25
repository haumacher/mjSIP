/*
 * Copyright (c) 2023 Bernhard Haumacher et al. All Rights Reserved.
 */
package org.mjsip.sip.authentication;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mjsip.sip.header.AuthorizationHeader;

/**
 * Test for {@link DigestAuthentication}.
 */
@SuppressWarnings("javadoc")
class TestDigestAuthentication {

	@Test
	void testResponse() {
		AuthorizationHeader ah = new AuthorizationHeader(
				"Digest username=\"Mufasa\", "
				+ "realm=\"testrealm@host.com\", " 
				+ "nonce=\"dcd98b7102dd2f0e8b11d0f600bfb0c093\", "
				+ "uri=\"/dir/index.html\", " 
				+ "qop=auth, " 
				+ "nc=00000001, " 
				+ "cnonce=\"0a4f113b\", "
				+ "response=\"6629fae49393a05397450978507c4ef1\", " 
				+ "opaque=\"5ccc069c403ebaf9f0171e9517f40e41\"\n");
		DigestAuthentication a = new DigestAuthentication("GET", ah, null, "Circle Of Life");

		Assertions.assertEquals("GET", a.method);
		Assertions.assertEquals("Circle Of Life", a.passwd);
		Assertions.assertEquals("testrealm@host.com", a.realm);
		Assertions.assertEquals("dcd98b7102dd2f0e8b11d0f600bfb0c093", a.nonce);
		Assertions.assertEquals("/dir/index.html", a.uri);
		Assertions.assertEquals("auth", a.qop);
		Assertions.assertEquals("00000001", a.nc);
		Assertions.assertEquals("0a4f113b", a.cnonce);
		Assertions.assertEquals("Mufasa", a.username);

		Assertions.assertEquals("6629fae49393a05397450978507c4ef1", a.getResponse());
		Assertions.assertTrue(a.checkResponse());
	}
}
