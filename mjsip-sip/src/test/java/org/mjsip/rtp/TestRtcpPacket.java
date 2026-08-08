/*
 * Copyright (c) 2026 Bernhard Haumacher et al. All Rights Reserved.
 */
package org.mjsip.rtp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Test for framing received RTCP packets, whose length is announced by the packets themselves.
 */
@SuppressWarnings("javadoc")
class TestRtcpPacket {

	/** Size of the buffer a RTCP packet is received into. */
	private static final int BUFFER_SIZE=1024;

	@Test
	void testPacketLength() {
		byte[] buffer=new byte[BUFFER_SIZE];
		RtcpPacket packet=new RtcpPacket(buffer);
		packet.setVersion(2);
		packet.setPacketLength(28);

		assertEquals(28,packet.getPacketLength());
		assertEquals(0,packet.getPaddingLength());
	}

	/** The length field of a packet at an offset must be read at that offset. */
	@Test
	void testPacketLengthAtOffset() {
		byte[] buffer=new byte[BUFFER_SIZE];
		RtcpPacket first=new RtcpPacket(buffer,0);
		first.setPacketLength(8);
		RtcpPacket second=new RtcpPacket(buffer,8);
		second.setPacketLength(28);

		assertEquals(8,first.getPacketLength());
		assertEquals(28,second.getPacketLength());
	}

	/**
	 * A packet announcing a length beyond the received data must not lead to reading beyond the
	 * buffer.
	 */
	@Test
	void testExcessivePacketLength() {
		byte[] buffer=new byte[64];
		buffer[0]=(byte)0xA0; // version 2, padding bit set
		buffer[2]=(byte)0xFF; // length: (0xFFFF + 1) * 4 = 262144 bytes
		buffer[3]=(byte)0xFF;
		RtcpPacket packet=new RtcpPacket(buffer);

		assertTrue(packet.getPacketLength()<=buffer.length,
			"Announced length "+packet.getPacketLength()+" exceeds the received "+buffer.length+" bytes.");
		// Must not throw, whatever the packet announces.
		assertTrue(packet.getPaddingLength()<=packet.getPacketLength());
	}

	/** A packet whose header is not completely received must not be read beyond the buffer. */
	@Test
	void testTruncatedHeader() {
		byte[] buffer=new byte[6];
		RtcpPacket packet=new RtcpPacket(buffer,4);

		assertTrue(packet.getPacketLength()<=2);
		assertEquals(0,packet.getPaddingLength());
	}

	/** The padding length is an unsigned value. */
	@Test
	void testUnsignedPaddingLength() {
		for (int padding=0x80; padding<=0xFF; padding++) {
			byte[] buffer=new byte[BUFFER_SIZE];
			RtcpPacket packet=new RtcpPacket(buffer);
			packet.setVersion(2);
			packet.setPacketLength(28);
			buffer[0]|=0x20; // padding (P) bit
			buffer[27]=(byte)padding;

			assertTrue(packet.getPaddingLength()>=0,"Padding length must not be negative.");
			assertTrue(packet.getPaddingLength()<=28-RtcpPacket.HDR_LEN,
				"Padding of "+packet.getPaddingLength()+" exceeds the packet.");
		}
	}

	/** All packets of a compound packet must be found. */
	@Test
	void testCompoundPacket() {
		byte[] buffer=new byte[BUFFER_SIZE];
		RtcpPacket first=new RtcpPacket(buffer,0);
		first.setVersion(2);
		first.setPacketLength(8);
		RtcpPacket second=new RtcpPacket(buffer,8);
		second.setVersion(2);
		second.setPacketLength(28);

		RtcpPacket[] packets=new RtcpCompoundPacket(buffer,0,36).getRtcpPackets();

		assertEquals(2,packets.length);
		assertEquals(0,packets[0].getPacketOffset());
		assertEquals(8,packets[1].getPacketOffset());
		assertEquals(28,packets[1].getPacketLength());
	}

	/** A packet that is not completely contained in the received data must be dropped. */
	@Test
	void testCompoundPacketWithTruncatedPacket() {
		byte[] buffer=new byte[BUFFER_SIZE];
		RtcpPacket first=new RtcpPacket(buffer,0);
		first.setVersion(2);
		first.setPacketLength(8);
		RtcpPacket second=new RtcpPacket(buffer,8);
		second.setVersion(2);
		second.setPacketLength(28);

		// Only the first packet and a part of the second one have been received.
		RtcpPacket[] packets=new RtcpCompoundPacket(buffer,0,20).getRtcpPackets();

		assertEquals(1,packets.length);
		assertEquals(0,packets[0].getPacketOffset());
	}

	/** A compound packet created for a buffer covers that whole buffer. */
	@Test
	void testCompoundPacketForBuffer() {
		byte[] buffer=new byte[36];
		RtcpPacket first=new RtcpPacket(buffer,0);
		first.setVersion(2);
		first.setPacketLength(8);
		RtcpPacket second=new RtcpPacket(buffer,8);
		second.setVersion(2);
		second.setPacketLength(28);

		RtcpCompoundPacket compound=new RtcpCompoundPacket(buffer);

		assertEquals(buffer.length,compound.getPacketLength());
		assertEquals(2,compound.getRtcpPackets().length);
	}

	/** A trailing fragment shorter than a RTCP header must be dropped. */
	@Test
	void testCompoundPacketWithTrailingFragment() {
		byte[] buffer=new byte[BUFFER_SIZE];
		RtcpPacket first=new RtcpPacket(buffer,0);
		first.setVersion(2);
		first.setPacketLength(8);

		RtcpPacket[] packets=new RtcpCompoundPacket(buffer,0,10).getRtcpPackets();

		assertEquals(1,packets.length);
	}

}
