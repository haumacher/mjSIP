/*
 * Copyright (c) 2026 Bernhard Haumacher et al. All Rights Reserved.
 */
package test.org.zoolu.net;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.Test;
import org.zoolu.net.IpAddress;
import org.zoolu.net.TcpConnection;
import org.zoolu.net.TcpConnectionListener;
import org.zoolu.net.TcpServer;
import org.zoolu.net.TcpServerListener;
import org.zoolu.net.TcpSocket;

/**
 * Test for the robustness of the {@link TcpServer} and {@link TcpConnection} receiver threads.
 */
class TestTcpReceiveLoops {

	/** Maximum time to wait for a connection or for data to be delivered. */
	private static final int TIMEOUT_MS=5000;

	private static final IpAddress LOCALHOST=new IpAddress(InetAddress.getLoopbackAddress());

	/**
	 * Data that cannot be processed must not terminate the connection handler, since that would leave
	 * the connection running without ever notifying its listener. This especially holds for an
	 * {@link Error} such as the {@link OutOfMemoryError} that malicious data may provoke.
	 */
	@Test
	void testConnectionSurvivesFailureWhileProcessingData() throws IOException, InterruptedException {
		CountDownLatch failed=new CountDownLatch(1);
		List<String> processed=new ArrayList<>();

		TcpConnectionListener listener=new TcpConnectionListener() {
			@Override
			public void onReceivedData(TcpConnection connection, byte[] data, int len) {
				String received=new String(data,0,len,StandardCharsets.UTF_8);
				if (received.startsWith("error")) {
					failed.countDown();
					throw new OutOfMemoryError("Simulated failure while processing '"+received+"'.");
				}
				synchronized (processed) {
					processed.add(received);
				}
			}

			@Override
			public void onConnectionTerminated(TcpConnection connection, Exception error) {
				// Ignore.
			}
		};

		try (Acceptor acceptor=new Acceptor(); TcpSocket client=new TcpSocket(LOCALHOST,acceptor.getPort())) {
			TcpSocket accepted=acceptor.accepted();
			TcpConnection connection=new TcpConnection(accepted,listener);
			try {
				send(client,"error: some broken data");
				// Note: Wait for the data to be processed, so that it is not delivered together with the
				// data sent next, which the connection has to survive to.
				assertTrue(failed.await(TIMEOUT_MS,TimeUnit.MILLISECONDS),"The broken data has not been received.");

				send(client,"regular data");
				assertEquals("regular data",awaitFirst(processed),
					"The connection handler has been terminated by data that could not be processed.");
				assertTrue(connection.isRunning(),"The connection handler has been terminated.");
			}
			finally {
				connection.halt();
			}
		}
	}

	/**
	 * A connection that cannot be handled must neither terminate the server, since that would stop
	 * accepting connections at all, nor leak the accepted socket.
	 */
	@Test
	void testServerSurvivesFailureWhileHandlingConnection() throws IOException, InterruptedException {
		AtomicBoolean first_connection=new AtomicBoolean(true);
		BlockingQueue<TcpSocket> refused=new ArrayBlockingQueue<>(1);
		BlockingQueue<TcpSocket> accepted=new ArrayBlockingQueue<>(1);

		TcpServerListener listener=new TcpServerListener() {
			@Override
			public void onIncomingConnection(TcpServer server, TcpSocket socket) {
				if (first_connection.compareAndSet(true,false)) {
					refused.add(socket);
					throw new OutOfMemoryError("Simulated failure while handling a connection.");
				}
				accepted.add(socket);
			}

			@Override
			public void onServerTerminated(TcpServer server, Exception error) {
				// Ignore.
			}
		};

		TcpServer server=new TcpServer(0,LOCALHOST,listener);
		try (TcpSocket first=new TcpSocket(LOCALHOST,server.getPort())) {
			TcpSocket refused_socket=poll(refused);
			assertNotNull(refused_socket,"The first connection has not been handled.");
			assertEquals(-1,readByte(first),"The refused connection must be closed by the server.");

			try (TcpSocket second=new TcpSocket(LOCALHOST,server.getPort())) {
				assertNotNull(poll(accepted),"The server stopped accepting connections.");
				assertTrue(server.isRunning(),"The server has been terminated.");
			}
		}
		finally {
			server.halt();
		}
	}

	/** A server listening to a port assigned by the operating system must report that port. */
	@Test
	void testEphemeralPort() throws IOException, InterruptedException {
		try (Acceptor acceptor=new Acceptor()) {
			assertTrue(acceptor.getPort()>0,"No port assigned: "+acceptor.getPort());

			try (TcpSocket client=new TcpSocket(LOCALHOST,acceptor.getPort())) {
				assertEquals(acceptor.getPort(),acceptor.accepted().getLocalPort());
			}
		}
	}

	// **************************** Utilities ****************************

	private static void send(TcpSocket socket, String data) throws IOException {
		socket.getOutputStream().write(data.getBytes(StandardCharsets.UTF_8));
		socket.getOutputStream().flush();
	}

	/** Reads a single byte, or -1 if the remote end has closed the connection. */
	private static int readByte(TcpSocket socket) throws IOException {
		socket.setSoTimeout(TIMEOUT_MS);
		InputStream in=socket.getInputStream();
		return in.read();
	}

	private static TcpSocket poll(BlockingQueue<TcpSocket> queue) throws InterruptedException {
		return queue.poll(TIMEOUT_MS,TimeUnit.MILLISECONDS);
	}

	/** Waits for the first element of the given list. */
	private static String awaitFirst(List<String> values) throws InterruptedException {
		for (long end=System.currentTimeMillis()+TIMEOUT_MS; System.currentTimeMillis()<end;) {
			synchronized (values) {
				if (!values.isEmpty()) return values.get(0);
			}
			Thread.sleep(10);
		}
		return null;
	}

	/** A TCP server accepting a single connection. */
	private static class Acceptor implements AutoCloseable {

		private final BlockingQueue<TcpSocket> _accepted=new ArrayBlockingQueue<>(1);

		private final TcpServer _server;

		Acceptor() throws IOException {
			_server=new TcpServer(0,LOCALHOST,new TcpServerListener() {
				@Override
				public void onIncomingConnection(TcpServer server, TcpSocket socket) {
					_accepted.add(socket);
				}

				@Override
				public void onServerTerminated(TcpServer server, Exception error) {
					// Ignore.
				}
			});
		}

		int getPort() {
			return _server.getPort();
		}

		TcpSocket accepted() throws InterruptedException {
			TcpSocket socket=poll(_accepted);
			assertNotNull(socket,"No connection has been accepted.");
			return socket;
		}

		@Override
		public void close() {
			_server.halt();
		}

	}

}
