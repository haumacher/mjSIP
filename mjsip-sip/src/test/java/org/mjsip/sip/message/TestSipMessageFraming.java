/*
 * Copyright (c) 2026 Bernhard Haumacher et al. All Rights Reserved.
 */
package org.mjsip.sip.message;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

/**
 * Test for framing SIP messages, especially for messages announcing a body length that does not
 * match the number of bytes actually received.
 *
 * @see BasicSipMessage#setMessage(byte[], int, int, boolean)
 * @see SipMessageBuffer
 */
@SuppressWarnings("javadoc")
class TestSipMessageFraming {

	/** Message header without a Content-Length header field, terminated by an empty line. */
	private static final String HEADER=
		"INVITE sip:bob@example.com SIP/2.0\r\n"+
		"Via: SIP/2.0/TCP client.example.com:5060;branch=z9hG4bK74bf9\r\n"+
		"From: <sip:alice@example.com>;tag=9fxced76sl\r\n"+
		"To: <sip:bob@example.com>\r\n"+
		"Call-ID: 3848276298220188511@client.example.com\r\n"+
		"CSeq: 1 INVITE\r\n"+
		"Content-Type: application/sdp\r\n";

	// **************************** Datagram transport ****************************

	@Test
	void testCompleteMessage() throws MalformedSipMessageException {
		byte[] packet=bytes(message("v=0\r\n"));

		SipMessage msg=new SipMessage();
		assertEquals(packet.length,msg.setMessage(packet,0,packet.length));
		assertEquals("v=0\r\n",msg.getStringBody());
	}

	/**
	 * A Content-Length header field announcing a huge body must be rejected instead of trying to
	 * allocate a buffer of the announced size, which formerly produced an {@link OutOfMemoryError}
	 * that could not be caught by the callers expecting a {@link MalformedSipMessageException}.
	 */
	@Test
	void testHugeContentLength() {
		assertMalformed("Content-Length: 2000000000\r\n","v=0\r\n",false);
		assertMalformed("Content-Length: "+Integer.MAX_VALUE+"\r\n","v=0\r\n",false);
	}

	/** A Content-Length header field that does not even fit into an <code>int</code>. */
	@Test
	void testContentLengthOverflow() {
		assertMalformed("Content-Length: 99999999999999999999\r\n","v=0\r\n",false);
	}

	@Test
	void testNegativeContentLength() {
		assertMalformed("Content-Length: -1\r\n","v=0\r\n",false);
		assertMalformed("Content-Length: "+Integer.MIN_VALUE+"\r\n","v=0\r\n",false);
	}

	/** A body that is announced longer than the received one is an error in a datagram transport. */
	@Test
	void testTruncatedBody() {
		assertMalformed("Content-Length: 100\r\n","v=0\r\n",false);
	}

	@Test
	void testMissingHeaderDelimiter() {
		byte[] packet=bytes(HEADER);

		assertMalformed(packet,false);
	}

	/** Bytes following the announced body within the same datagram must be discarded. */
	@Test
	void testExtraBytesInPacketDiscarded() throws MalformedSipMessageException {
		String msg_str=message("v=0\r\n");
		byte[] packet=bytes(msg_str+"INVITE sip:eve@example.com SIP/2.0\r\n\r\n");

		SipMessage msg=new SipMessage();
		assertEquals(msg_str.length(),msg.setMessage(packet,0,packet.length));
		assertEquals("v=0\r\n",msg.getStringBody());
	}

	/** Without a Content-Length header field, the body of a datagram ends at the end of the packet. */
	@Test
	void testMissingContentLength() throws MalformedSipMessageException {
		byte[] packet=bytes(HEADER+"\r\n"+"v=0\r\n");

		SipMessage msg=new SipMessage();
		assertEquals(packet.length,msg.setMessage(packet,0,packet.length));
		assertEquals("v=0\r\n",msg.getStringBody());
	}

	/** A message body is not limited to the size of the receive buffer of a datagram transport. */
	@Test
	void testLargeBody() throws MalformedSipMessageException {
		String body=body(64*1024);
		byte[] packet=bytes(message(body));

		SipMessage msg=new SipMessage();
		assertEquals(packet.length,msg.setMessage(packet,0,packet.length));
		assertEquals(body,msg.getStringBody());
	}

	@Test
	void testEmptyBody() throws MalformedSipMessageException {
		byte[] packet=bytes(message(""));

		SipMessage msg=new SipMessage();
		assertEquals(packet.length,msg.setMessage(packet,0,packet.length));
		assertNull(msg.getBody());
	}

	/** A message may start at an offset within the given buffer. */
	@Test
	void testMessageAtOffset() throws MalformedSipMessageException {
		String msg_str=message("v=0\r\n");
		byte[] packet=bytes("junk"+msg_str);

		SipMessage msg=new SipMessage();
		assertEquals(msg_str.length(),msg.setMessage(packet,4,packet.length-4));
		assertEquals("v=0\r\n",msg.getStringBody());
	}

	/** The message body must be taken from the first empty line on, also for a LF-only sender. */
	@Test
	void testLfOnlyHeaderDelimiter() throws MalformedSipMessageException {
		String msg_str=HEADER.replace("\r\n","\n")+"Content-Length: 5\n"+"\n"+"v=0\r\n";
		byte[] packet=bytes(msg_str);

		SipMessage msg=new SipMessage();
		assertEquals(msg_str.length(),msg.setMessage(packet,0,packet.length));
		assertEquals("v=0\r\n",msg.getStringBody());
	}

	/**
	 * A message received from the network must be rejected instead of being delivered as a partially
	 * parsed message that may lack even the request line.
	 */
	@Test
	void testParse() throws MalformedSipMessageException {
		byte[] valid=bytes(message("v=0\r\n"));
		assertEquals("v=0\r\n",SipMessage.parse(valid,0,valid.length).getStringBody());

		byte[] invalid=bytes(message("Content-Length: 100\r\n","v=0\r\n"));
		assertMalformed(() -> SipMessage.parse(invalid,0,invalid.length));
	}

	// **************************** Stream transport ****************************

	@Test
	void testCompleteMessageInStream() throws MalformedSipMessageException {
		SipMessageBuffer buffer=buffer(message("v=0\r\n"));

		assertEquals("v=0\r\n",buffer.parseSipMessage().getStringBody());
		assertEquals(0,buffer.getLength());
	}

	/**
	 * A message announcing a body shorter than the buffered data must not consume the bytes of the
	 * message that follows it in the stream.
	 */
	@Test
	void testPipelinedMessages() throws MalformedSipMessageException {
		SipMessageBuffer buffer=buffer(message("v=0\r\nfirst\r\n")+message("v=0\r\nsecond\r\n"));

		assertEquals("v=0\r\nfirst\r\n",buffer.parseSipMessage().getStringBody());
		assertEquals("v=0\r\nsecond\r\n",buffer.parseSipMessage().getStringBody());
		assertEquals(0,buffer.getLength());
	}

	/** Messages of a stream may be separated by additional CRLF, as sent by keep-alive mechanisms. */
	@Test
	void testPipelinedMessagesWithKeepAlive() throws MalformedSipMessageException {
		SipMessageBuffer buffer=buffer(message("v=0\r\nfirst\r\n")+"\r\n\r\n"+message("v=0\r\nsecond\r\n"));

		assertEquals("v=0\r\nfirst\r\n",buffer.parseSipMessage().getStringBody());
		skipCrLf(buffer);
		assertEquals("v=0\r\nsecond\r\n",buffer.parseSipMessage().getStringBody());
		assertEquals(0,buffer.getLength());
	}

	/**
	 * A message announcing a huge body must not lead to allocating a buffer of the announced size,
	 * which formerly produced an {@link OutOfMemoryError}.
	 */
	@Test
	void testHugeContentLengthInStream() {
		SipMessageBuffer buffer=buffer("Content-Length: 2000000000\r\n","v=0\r\n");

		// The rest of the announced body may still arrive, but no buffer of the announced size must be
		// allocated before the data has actually been received.
		assertIncomplete(buffer);
	}

	@Test
	void testNegativeContentLengthInStream() {
		assertMalformed("Content-Length: -1\r\n","v=0\r\n",true);
	}

	@Test
	void testContentLengthOverflowInStream() {
		assertMalformed("Content-Length: 99999999999999999999\r\n","v=0\r\n",true);
	}

	/**
	 * Without a Content-Length header field, the end of a message within a stream cannot be
	 * determined, so the message must be rejected instead of consuming the bytes of a potentially
	 * following message.
	 */
	@Test
	void testMissingContentLengthInStream() {
		SipMessageBuffer buffer=buffer(HEADER+"\r\n"+message("v=0\r\n"));

		assertMalformed(buffer);
	}

	/** A message header split over multiple packets must be parsed when it is complete. */
	@Test
	void testIncompleteHeaderInStream() throws MalformedSipMessageException {
		String msg_str=message("v=0\r\n");
		int split=HEADER.length()/2;

		SipMessageBuffer buffer=buffer(msg_str.substring(0,split));
		assertIncomplete(buffer);

		buffer.append(bytes(msg_str.substring(split)));
		assertEquals("v=0\r\n",buffer.parseSipMessage().getStringBody());
		assertEquals(0,buffer.getLength());
	}

	/** A message body split over multiple packets must be parsed when it is complete. */
	@Test
	void testIncompleteBodyInStream() throws MalformedSipMessageException {
		String body=body(64*1024);
		String msg_str=message(body);
		int split=msg_str.length()-body.length()/2;

		SipMessageBuffer buffer=buffer(msg_str.substring(0,split));
		assertIncomplete(buffer);

		buffer.append(bytes(msg_str.substring(split)+message("v=0\r\n")));
		assertEquals(body,buffer.parseSipMessage().getStringBody());
		assertEquals("v=0\r\n",buffer.parseSipMessage().getStringBody());
		assertEquals(0,buffer.getLength());
	}

	/**
	 * Data that does not form a complete message must not be buffered indefinitely, since a peer could
	 * otherwise exhaust the memory by announcing a huge body, or by never terminating the message
	 * header.
	 */
	@Test
	void testMessageSizeLimitInStream() throws MalformedSipMessageException {
		String announced=message("Content-Length: 2000000000\r\n","v=0\r\n");
		SipMessageBuffer buffer=new SipMessageBuffer(announced.length()+16);

		buffer.append(bytes(announced));
		assertIncomplete(buffer);

		// The announced body never arrives, but the peer keeps sending.
		buffer.append(bytes(body(32)));
		assertMalformed(buffer);
	}

	/** A message not exceeding the limit must be parsed, whatever the limit is. */
	@Test
	void testMessageAtSizeLimitInStream() throws MalformedSipMessageException {
		String msg_str=message("v=0\r\n");
		SipMessageBuffer buffer=new SipMessageBuffer(msg_str.length());

		buffer.append(bytes(msg_str));
		assertEquals("v=0\r\n",buffer.parseSipMessage().getStringBody());
		assertEquals(0,buffer.getLength());
	}

	/** A message header that has not been received completely must not be reported as an error. */
	@Test
	void testEmptyStream() {
		assertIncomplete(new SipMessageBuffer());
		assertIncomplete(buffer(""));
		assertIncomplete(buffer("INV"));
	}

	// **************************** Utilities ****************************

	/** Creates a message with a matching Content-Length header field. */
	private static String message(String body) {
		return message("Content-Length: "+bytes(body).length+"\r\n",body);
	}

	/** Creates a message with the given Content-Length header field, which may be wrong. */
	private static String message(String content_length, String body) {
		return HEADER+content_length+"\r\n"+body;
	}

	/** A body of the given length. */
	private static String body(int len) {
		StringBuilder result=new StringBuilder("v=0\r\n");
		while (result.length()<len) result.append("a=filler:").append(result.length()).append("\r\n");
		return result.substring(0,len);
	}

	private static byte[] bytes(String str) {
		return str.getBytes(StandardCharsets.UTF_8);
	}

	private static SipMessageBuffer buffer(String data) {
		return new SipMessageBuffer().append(bytes(data));
	}

	private static SipMessageBuffer buffer(String content_length, String body) {
		return buffer(message(content_length,body));
	}

	/** Skips CRLF sent between messages of a stream, as TcpTransportConnection does. */
	private static void skipCrLf(SipMessageBuffer buffer) {
		byte b;
		while (buffer.getLength()>0 && ((b=buffer.byteAt(0))=='\r' || b=='\n')) buffer.skip(1);
	}

	/** Checks that a message with the given (invalid) Content-Length header field is rejected. */
	private static void assertMalformed(String content_length, String body, boolean stream) {
		if (stream) assertMalformed(buffer(content_length,body));
		else assertMalformed(bytes(message(content_length,body)),false);
	}

	private static void assertMalformed(byte[] packet, boolean stream) {
		SipMessage msg=new SipMessage();
		assertMalformed(() -> msg.setMessage(packet,0,packet.length,stream));
	}

	private static void assertMalformed(SipMessageBuffer buffer) {
		int length=buffer.getLength();
		assertMalformed(() -> buffer.parseSipMessage());
		assertEquals(length,buffer.getLength(),"Buffer must not be consumed by a failed parse.");
	}

	/** Checks that parsing fails in a way that must not be retried. */
	private static void assertMalformed(Parse parse) {
		MalformedSipMessageException problem=assertThrows(MalformedSipMessageException.class,() -> parse.run());
		assertFalse(problem instanceof IncompleteSipMessageException,
			"Message must be rejected, not be waited for: "+problem.getMessage());
	}

	/** Checks that parsing fails in a way that may be retried when more data has been received. */
	private static void assertIncomplete(SipMessageBuffer buffer) {
		int length=buffer.getLength();
		assertThrows(IncompleteSipMessageException.class,() -> buffer.parseSipMessage());
		assertEquals(length,buffer.getLength(),"Buffer must not be consumed by an incomplete message.");
	}

	/** A parse operation that may fail. */
	private interface Parse {
		void run() throws MalformedSipMessageException;
	}

}
