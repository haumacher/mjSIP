/*
 * Copyright (c) 2023 Bernhard Haumacher et al. All Rights Reserved.
 */
package test.org.mjsip.sip.header;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mjsip.sip.header.ViaHeader;

/**
 * Test for {@link ViaHeader}.
 */
class TestViaHeader {

	@Test
	void testParse() {
		ViaHeader header = ViaHeader
				.parse("SIP/2.0/UDP [2001:9e8:2050:becc:7eff:4dff:fe57:1a5a]:5060;branch=z9hG4bK0D731C24CCBB2565");

		Assertions.assertEquals("SIP", header.getProtocol());
		Assertions.assertEquals("2.0", header.getVersion());
		Assertions.assertEquals("UDP", header.getTransport());
		Assertions.assertEquals("2001:9e8:2050:becc:7eff:4dff:fe57:1a5a", header.getHost());
		Assertions.assertEquals("[2001:9e8:2050:becc:7eff:4dff:fe57:1a5a]", header.getHostName());
		Assertions.assertEquals(5060, header.getPort());
		Assertions.assertEquals("z9hG4bK0D731C24CCBB2565", header.getParameter("branch"));
	}

	@Test
	void testWhiteSpace() {
		ViaHeader header = ViaHeader
				.parse("SIP/2.0/UDP   phoneblock.net	: 	5060   ;  branch =    z9hG4bK0D731C24CCBB2565    ");

		Assertions.assertEquals("SIP/2.0/UDP phoneblock.net:5060;branch=z9hG4bK0D731C24CCBB2565", header.getValue());
	}

	@Test
	void testNoPort() {
		doTestValue("SIP/2.0/UDP phoneblock.net;branch=z9hG4bK0D731C24CCBB2565");
	}

	@ParameterizedTest
	@ValueSource(strings = {"SIP/2.0/UDP phoneblock.net;branch=z9hG4bK0D731C24CCBB2565;rport",
			"SIP/2.0/UDP phoneblock.net;rport;branch=z9hG4bK0D731C24CCBB2565"})
	void testRPort(String value) {
		doTestValue(value);
	}

	private static void doTestValue(String value) {
		ViaHeader header = ViaHeader.parse(value);
		Assertions.assertEquals(value, header.getValue());
	}
}
