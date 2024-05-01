package test.org.mjsip.sdp.field;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mjsip.sdp.field.OriginField;
import org.zoolu.net.AddressType;

/**
 * Test case for {@link OriginField}.
 */
public class TestOriginField {

	@Test
	void testParse() {
		OriginField field = new OriginField("user 6597542 6597543 IN IP6 fd00::9a9b:cbff:fe34:c1e9");
		Assertions.assertEquals("fd00::9a9b:cbff:fe34:c1e9", field.getAddress());
		Assertions.assertEquals(AddressType.IP6, field.getAddressType());
		Assertions.assertEquals("6597542", field.getSessionId());
		Assertions.assertEquals("6597543", field.getSessionVersion());
		Assertions.assertEquals("user", field.getUserName());
		Assertions.assertEquals('o', field.getType());
	}

}
