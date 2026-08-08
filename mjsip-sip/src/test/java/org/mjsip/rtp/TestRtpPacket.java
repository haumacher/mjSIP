/*
 * Copyright (c) 2026 Bernhard Haumacher et al. All Rights Reserved.
 */
package org.mjsip.rtp;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;

import org.junit.jupiter.api.Test;

/**
 * Test for deriving payload offset and length from a received {@link RtpPacket}, especially for
 * packets announcing a padding or CSRC count that does not match the data actually received.
 */
@SuppressWarnings("javadoc")
class TestRtpPacket {

	/** Size of the receive buffer of {@link org.mjsip.media.RtpStreamReceiver}. */
	private static final int BUFFER_SIZE=32768;

	/** Length of the RTP header without any CSRC identifier. */
	private static final int HDR_LEN=12;

	@Test
	void testPlainPacket() {
		byte[] payload={ 1, 2, 3, 4, 5 };
		RtpPacket packet=new RtpPacket(0,4711,1,0,payload,0,payload.length);

		assertEquals(HDR_LEN,packet.getHeaderLength());
		assertEquals(0,packet.getPaddingLength());
		assertEquals(payload.length,packet.getPayloadLength());
		assertArrayEquals(payload,packet.getPayload());
	}

	@Test
	void testPacketWithCsrc() {
		byte[] payload={ 1, 2, 3, 4, 5 };
		RtpPacket packet=new RtpPacket(0,4711,1,0,new long[] { 1, 2 },payload,0,payload.length);

		assertEquals(HDR_LEN+8,packet.getHeaderLength());
		assertEquals(payload.length,packet.getPayloadLength());
		assertArrayEquals(payload,packet.getPayload());
	}

	/** A received packet with four bytes of payload followed by four bytes of padding. */
	@Test
	void testPacketWithPadding() {
		RtpPacket packet=receivedPacket(HDR_LEN+8);
		setPadding(packet,4);

		assertEquals(4,packet.getPaddingLength());
		assertEquals(4,packet.getPayloadLength());
		assertPayloadWithinPacket(packet);
	}

	/**
	 * The padding length is an unsigned value: A padding byte with its highest bit set must not be
	 * read as a negative length, which would announce a payload longer than the received packet.
	 */
	@Test
	void testUnsignedPaddingLength() {
		for (int padding=0x80; padding<=0xFF; padding++) {
			RtpPacket packet=receivedPacket(BUFFER_SIZE);
			setPadding(packet,padding);

			assertTrue(packet.getPaddingLength()>=0,"Padding length must not be negative.");
			assertPayloadWithinPacket(packet);
		}
	}

	/** A padding length larger than the received packet leaves no payload at all. */
	@Test
	void testExcessivePaddingLength() {
		RtpPacket packet=receivedPacket(HDR_LEN+8);
		setPadding(packet,100);

		assertEquals(0,packet.getPayloadLength());
		assertPayloadWithinPacket(packet);
	}

	/**
	 * A CSRC count announcing more identifiers than the received packet can hold must not produce a
	 * negative payload length.
	 */
	@Test
	void testExcessiveCsrcCount() {
		RtpPacket packet=receivedPacket(HDR_LEN);
		packet.getPacketBuffer()[0]=(byte)0x8F; // version 2, CSRC count 15

		assertEquals(0,packet.getPayloadLength());
		assertEquals(0,packet.getPayload().length);
		assertPayloadWithinPacket(packet);
	}

	/** A packet shorter than the RTP header has no payload. */
	@Test
	void testTruncatedHeader() {
		RtpPacket packet=receivedPacket(HDR_LEN-1);

		assertEquals(0,packet.getPaddingLength());
		assertEquals(0,packet.getPayloadLength());
	}

	/** A packet filling the whole receive buffer must not announce a payload beyond that buffer. */
	@Test
	void testPacketFillingTheReceiveBuffer() {
		RtpPacket packet=receivedPacket(BUFFER_SIZE);
		setPadding(packet,0x80);

		assertPayloadWithinPacket(packet);
	}

	// **************************** Utilities ****************************

	/** A packet of the given length, as received into a buffer of the receiver's size. */
	private static RtpPacket receivedPacket(int length) {
		byte[] buffer=new byte[BUFFER_SIZE];
		RtpPacket packet=new RtpPacket(buffer,0,length);
		if (length>=HDR_LEN) packet.setVersion(2);
		return packet;
	}

	/** Announces the given padding length, without writing an actual padding. */
	private static void setPadding(RtpPacket packet, int padding_len) {
		byte[] buffer=packet.getPacketBuffer();
		buffer[packet.getPacketOffset()]|=0x20; // padding (P) bit
		buffer[packet.getPacketOffset()+packet.getPacketLength()-1]=(byte)padding_len;
	}

	/**
	 * Checks that the payload announced by the packet lies within the received data, the way
	 * {@link org.mjsip.media.RtpStreamReceiver} passes it on.
	 */
	private static void assertPayloadWithinPacket(RtpPacket packet) {
		int off=packet.getHeaderLength();
		int len=packet.getPayloadLength();

		assertTrue(len>=0,"Payload length must not be negative: "+len);
		assertTrue(off+len<=packet.getPacketLength(),
			"Payload ["+off+", "+off+"+"+len+") exceeds the received packet of "+packet.getPacketLength()+" bytes.");

		// Must not throw, whatever the packet announces.
		new ByteArrayOutputStream().write(packet.getPacketBuffer(),packet.getPacketOffset()+off,len);
	}

}
