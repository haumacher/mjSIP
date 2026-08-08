/*
 * Copyright (c) 2026 Bernhard Haumacher et al. All Rights Reserved.
 */
package org.mjsip.sip.provider;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.mjsip.sip.message.SipMessage;
import org.zoolu.net.IpAddress;
import org.zoolu.net.SocketAddress;
import org.zoolu.net.UdpPacket;
import org.zoolu.net.UdpSocket;

/**
 * Test for receiving SIP messages over {@link UdpTransport}.
 */
class TestUdpTransport {

	/** Maximum time to wait for the messages to be delivered. */
	private static final int TIMEOUT_MS=5000;

	/** Message header of a request with a body of five bytes. */
	private static final String HEADER=
		"INVITE sip:bob@example.com SIP/2.0\r\n"+
		"Via: SIP/2.0/UDP client.example.com:5060;branch=z9hG4bK74bf9\r\n"+
		"From: <sip:alice@example.com>;tag=9fxced76sl\r\n"+
		"To: <sip:bob@example.com>\r\n"+
		"Call-ID: 3848276298220188511@client.example.com\r\n"+
		"CSeq: 1 INVITE\r\n"+
		"Content-Type: application/sdp\r\n";

	/**
	 * A datagram that cannot be parsed must be dropped instead of being delivered as a partially
	 * parsed message, and it must not stop the transport from receiving the messages that follow.
	 */
	@Test
	void testMalformedDatagramDropped() throws IOException, InterruptedException {
		// Note: The last message is valid, so that receiving it proves that the preceding invalid ones
		// have been processed (datagrams are delivered in order over the loopback interface).
		List<String> datagrams=List.of(
			HEADER+"Content-Length: 2000000000\r\n\r\nv=0\r\n",
			HEADER+"Content-Length: -1\r\n\r\nv=0\r\n",
			HEADER+"Content-Length: 100\r\n\r\nv=0\r\n",
			"not a SIP message at all",
			HEADER+"Content-Length: 5\r\n\r\nv=0\r\n");

		CountDownLatch received=new CountDownLatch(1);
		List<SipMessage> messages=new ArrayList<>();

		SipTransportListener listener=new SipTransportListener() {
			@Override
			public void onReceivedMessage(SipTransport transport, SipMessage msg) {
				synchronized (messages) {
					messages.add(msg);
				}
				received.countDown();
			}

			@Override
			public void onIncomingTransportConnection(SipTransport transport, SocketAddress remote_soaddr) {
				// Ignore.
			}

			@Override
			public void onTransportConnectionTerminated(SipTransport transport, SocketAddress remote_soaddr, Exception error) {
				// Ignore.
			}

			@Override
			public void onTransportTerminated(SipTransport transport, Exception error) {
				// Ignore.
			}
		};

		IpAddress localhost=new IpAddress(InetAddress.getLoopbackAddress());
		try (UdpSocket sender=new UdpSocket(0,localhost)) {
			UdpTransport transport=new UdpTransport(0,localhost);
			transport.setListener(listener);
			try {
				for (String datagram : datagrams) {
					byte[] buf=datagram.getBytes(StandardCharsets.UTF_8);
					sender.send(new UdpPacket(buf,buf.length,localhost,transport.getLocalPort()));
				}

				assertTrue(received.await(TIMEOUT_MS,TimeUnit.MILLISECONDS),"The valid message has not been received.");
				synchronized (messages) {
					assertEquals(1,messages.size(),"Only the valid message must be delivered.");
					SipMessage msg=messages.get(0);
					assertEquals("v=0\r\n",msg.getStringBody());
					assertEquals("INVITE",msg.getRequestLine().getMethod());
				}
			}
			finally {
				transport.halt();
			}
		}
	}

}
