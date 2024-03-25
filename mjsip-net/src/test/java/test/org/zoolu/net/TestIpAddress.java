package test.org.zoolu.net;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.junit.jupiter.api.Test;
import org.zoolu.net.AddressType;
import org.zoolu.net.IpAddress;

/**
 * Test case for {@link IpAddress}.
 */
class TestIpAddress {

	@Test
	void testAutoConfig() throws UnknownHostException {
		InetAddress ipv4 = IpAddress.getLocalHostAddress(AddressType.IP4);
		InetAddress ipv6 = IpAddress.getLocalHostAddress(AddressType.IP6);
		
		assertNotNull(ipv4);
		assertNotNull(ipv6);
		
		System.out.println("Found IPv4 address: " + ipv4.getHostAddress());
		System.out.println("Found IPv6 address: " + ipv6.getHostAddress());
	}
}
