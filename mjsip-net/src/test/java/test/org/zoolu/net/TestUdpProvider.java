/*
 * Copyright (c) 2026 Bernhard Haumacher et al. All Rights Reserved.
 */
package test.org.zoolu.net;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.zoolu.net.IpAddress;
import org.zoolu.net.UdpPacket;
import org.zoolu.net.UdpProvider;
import org.zoolu.net.UdpProviderListener;
import org.zoolu.net.UdpSocket;

/**
 * Test for the robustness of the {@link UdpProvider} receiver thread.
 */
class TestUdpProvider {

	/** Maximum time to wait for the packets to be delivered. */
	private static final int TIMEOUT_MS=5000;

	/**
	 * A packet that cannot be processed must not terminate the receiver thread, since that would stop
	 * the service for all peers. This especially holds for an {@link Error} such as the
	 * {@link OutOfMemoryError} that a packet announcing a huge content length used to provoke.
	 */
	@Test
	void testReceiverSurvivesFailureWhileProcessingPacket() throws IOException, InterruptedException {
		List<String> packets=Arrays.asList("error: huge content length","exception: some other problem","regular packet");

		CountDownLatch received=new CountDownLatch(packets.size());
		List<String> processed=new ArrayList<>();

		UdpProviderListener listener=new UdpProviderListener() {
			@Override
			public void onReceivedPacket(UdpProvider udp, UdpPacket packet) {
				String data=new String(packet.getData(),packet.getOffset(),packet.getLength(),StandardCharsets.UTF_8);
				try {
					if (data.startsWith("error")) throw new OutOfMemoryError("Simulated failure processing '"+data+"'.");
					if (data.startsWith("exception")) throw new IllegalStateException("Simulated failure processing '"+data+"'.");
					synchronized (processed) {
						processed.add(data);
					}
				}
				finally {
					received.countDown();
				}
			}

			@Override
			public void onServiceTerminated(UdpProvider udp, Exception error) {
				// Ignore.
			}
		};

		IpAddress localhost=new IpAddress(InetAddress.getLoopbackAddress());
		try (UdpSocket receiver=new UdpSocket(0,localhost); UdpSocket sender=new UdpSocket(0,localhost)) {
			UdpProvider provider=new UdpProvider(receiver,listener);
			try {
				for (String data : packets) {
					byte[] buf=data.getBytes(StandardCharsets.UTF_8);
					sender.send(new UdpPacket(buf,buf.length,localhost,receiver.getLocalPort()));
				}

				assertTrue(received.await(TIMEOUT_MS,TimeUnit.MILLISECONDS),"Not all packets have been received.");
				assertTrue(provider.isRunning(),"The receiver thread has been terminated by a failing packet.");
				synchronized (processed) {
					assertEquals(Collections.singletonList("regular packet"),processed);
				}
			}
			finally {
				provider.halt();
			}
		}
	}

}
