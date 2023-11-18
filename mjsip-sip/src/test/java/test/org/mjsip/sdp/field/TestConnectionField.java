/*
 * Copyright (c) 2023 Bernhard Haumacher et al. All Rights Reserved.
 */
package test.org.mjsip.sdp.field;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mjsip.sdp.field.ConnectionField;

/**
 * Test for {@link ConnectionField}
 */
@SuppressWarnings("javadoc")
public class TestConnectionField {

	@Test
	public void testParse() {
		ConnectionField field = new ConnectionField("IN IP6 2001:9e8:2050:becc:7eff:4dff:fe57:1a5a");

		Assertions.assertEquals("IP6", field.getAddressType());
		Assertions.assertEquals("2001:9e8:2050:becc:7eff:4dff:fe57:1a5a", field.getAddress());
		Assertions.assertEquals(0, field.getNum());
		Assertions.assertEquals(0, field.getTTL());
	}

}
