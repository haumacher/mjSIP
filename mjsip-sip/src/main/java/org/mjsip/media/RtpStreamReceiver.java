/*
 * Copyright (C) 2013 Luca Veltri - University of Parma - Italy
 * 
 * This source code is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This source code is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this source code; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 * 
 * Author(s):
 * Luca Veltri (luca.veltri@unipr.it)
 */

package org.mjsip.media;


import java.io.IOException;
import java.io.OutputStream;

import org.mjsip.media.rx.RtpReceiverOptions;
import org.mjsip.rtp.RtpPacket;
import org.mjsip.rtp.RtpPayloadFormat;
import org.mjsip.rtp.RtpSocket;
import org.slf4j.LoggerFactory;
import org.zoolu.net.SocketAddress;
import org.zoolu.net.UdpSocket;
import org.zoolu.util.Encoder;


/** RtpStreamReceiver is a generic RTP receiver.
  * It receives packets from RTP and writes media into a given OutputStream.
  */
public class RtpStreamReceiver implements Runnable, RtpControlledReceiver {
	
	private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(RtpStreamReceiver.class);
	
	/** Whether working in debug mode. */
	public static final boolean DEBUG = LOG.isDebugEnabled();

	/**
	 * Time waited before start playing out packets (in milliseconds). All packet received in the
	 * meantime are dropped in order to reduce the effect of an eventual initial packet burst.
	 */
	public static final int EARLY_DROP_TIME=200;

	/** Size of the receiver buffer (including the RTP header) */
	public static final int BUFFER_SIZE=32768;

	/** Maximum blocking time, spent waiting for reading new bytes [milliseconds] */
	public static final int SO_TIMEOUT=200;

	/** Integer value 2^16 */
	private static final int TWO_16=(1<<16);

	/** Long value 2^32 */
	private static final long TWO_32=(1<<32);

	/** Listener */
	private RtpStreamReceiverListener listener = null;

	/** The OutputStream */
	private OutputStream output_stream = null;

	/** The RtpSocket */
	private RtpSocket rtp_socket = null;

	/** Remote socket address */
	private SocketAddress remote_soaddr = null;

	/** Packet random early drop (RED) value; if greater than 0, it is the inverse of the packet drop rate */
	private int random_early_drop = 0;

	/** Whether it is running */
	private volatile boolean running = false;

	/** Packet counter (incremented only if packet_drop_rate>0) */
	private long packet_counter = 0;

	/** RTP payload format */
	private RtpPayloadFormat rtp_payload_format = null;
	
	/** Whether discarding packets with wrong SSRC (i.e. with a different SSRC compared to the first packet) */
	private boolean ssrc_check = false;

	/** Whether discarding out-of-sequence and duplicated packets */
	private boolean sequence_check = false;

	/** Whether filling silence intervals with (silence-equivalent) void data */
	private boolean silence_padding = false;
	
	/** Additional RTP payload decoder */
	private Encoder additional_decoder;

	/**
	 * Constructs a RtpStreamReceiver.
	 * 
	 * @param options
	 *        Additional options.
	 * @param output_stream
	 *        the stream sink
	 * @param additional_decoder
	 *        additional RTP payload decoder (optional)
	 * @param payloadFormat
	 *        The RtpPayloadFormat to use.
	 * @param socket
	 *        the local receiver UdpSocket
	 * @param listener
	 *        the RtpStreamReceiver listener
	 */
	public RtpStreamReceiver(RtpReceiverOptions options, OutputStream output_stream, Encoder additional_decoder, RtpPayloadFormat payloadFormat, UdpSocket socket, RtpStreamReceiverListener listener) {
		this.output_stream = output_stream;
		this.listener = listener;
		this.additional_decoder = additional_decoder;
		if (socket != null) {
			rtp_socket = new RtpSocket(socket);
		}
		this.rtp_payload_format=payloadFormat;
		this.random_early_drop=options.randomEarlyDrop();
		this.silence_padding = options.silencePadding();
		this.sequence_check = silence_padding || options.sequenceCheck();
		this.ssrc_check = options.ssrcCheck();

		LOG.info("Created RTP stream receiver: {} <-- {}", socket, remote_soaddr);
	}

	/** Gets the local port. */
	public int getLocalPort() {
		if (rtp_socket!=null) return rtp_socket.getUdpSocket().getLocalPort();
		else return 0;
	}

	/** Gets SSRC.
	  * @return he synchronization source (SSRC) identifier of the received RTP packets */
	@Override
	public long getSSRC() {
		// to be implemented..
		return 0;
	}

	/** Gets fraction lost.
	  * @return the fraction of RTP data packets lost since the previous SR or RR packet was sent; the fraction loss is defined as the number of packets lost divided by the number of packets expected; it is represented by the integer part after multiplying the loss fraction by 256 (8 bit) */
	@Override
	public int getFractionLost() {
		// to be implemented..
		return 0;
	}

	/** Gets cumulative number of packets lost.
	  * @return cumulative number of packets lost that is the total number of RTP data packets that have been lost since the beginning of reception; it is the number of packets expected less the number of packets actually received, where the number of packets received includes any which are late or duplicates */
	@Override
	public long getCumulativePacketLost() {
		// to be implemented..
		return 0;
	}

	/** Gets the extended highest sequence number received.
	  * @return the extended highest sequence number received (32bit); the low 16 bits contain the highest sequence number received in an RTP data packet, and the most significant 16 bits extend that sequence number with the corresponding count of sequence number cycles */
	@Override
	public long getHighestSqnReceived() {
		// to be implemented..
		return 0;
	}

	/** Gets the interarrival jitter.
	  * @return the interarrival jitter, that is an estimate of the statistical variance of the RTP data packet interarrival time, measured in timestamp units and expressed as an unsigned integer */
	@Override
	public long getInterarrivalJitter() {
		// to be implemented..
		return 0;
	}

	/** Gets last SR timestamp (LSR).
	  * @return last SR timestamp (LSR), that is the middle 32 bits out of 64 in the NTP timestamp received as part of the most recent RTCP SR packet */
	@Override
	public long getLSR() {
		// to be implemented..
		return 0;
	}

	/** Gets delay since last SR (DLSR).
	  * @return delay since last SR (DLSR), that is the delay, expressed in units of 1/65536 seconds, between receiving the last SR packet and sending this reception report block */ 
	@Override
	public long getDLSR() {
		// to be implemented..
		return 0;
	}



	/** Whether is running */
	public boolean isRunning() {
		return running;
	}


	/** Stops running */
	public void halt() {
		running=false;
	}


	/** Runs it in a new Thread. */
	@Override
	public void run() {
		
		if (rtp_socket==null) {
			LOG.error("RTP socket is null");
			return;
		}
		//else

		byte[] buffer=new byte[BUFFER_SIZE];
		RtpPacket rtp_packet=new RtpPacket(buffer,0);

		running=true;    

		if (DEBUG)
			LOG.debug("RTP: localhost:{} <-- remotesocket", rtp_socket.getUdpSocket().getLocalPort());
		if (DEBUG)
			LOG.debug("RTP: receiving pkts of MAXIMUM {} bytes", buffer.length);

		Exception error=null;
		try {
			rtp_socket.getUdpSocket().setSoTimeout(SO_TIMEOUT);
			long early_drop_to=(EARLY_DROP_TIME>0)? System.currentTimeMillis()+EARLY_DROP_TIME : -1;

			long ssrc=-1;
			int last_sqn=-1;
			long last_timestamp=-1;
			byte[] silence_buffer=new byte[4000];

			while (running) {
				
				try {
					// read a block of data from the rtp socket
					rtp_socket.receive(rtp_packet);
					// drop the first packets in order to reduce the effect of an eventual initial packet burst
					if (early_drop_to>0 && System.currentTimeMillis()<early_drop_to) continue;
					else early_drop_to=-1;
					
					// only if still running..
					if (running) {
						
						if (ssrc_check) {
							// discard packets with wrong SSRC (i.e. when the SSRC differs from the one in the first received packet)
							long pkt_ssrc=rtp_packet.getSsrc();
							if (ssrc==-1) ssrc=pkt_ssrc;
							else if (pkt_ssrc!=ssrc) continue; // discarded packet with wrong SSRC
						}
						if (sequence_check) {
							// discard out of sequence and duplicated packets
							int sqn=rtp_packet.getSequenceNumber();
							if (sqn==last_sqn) continue; // discarded duplicated packet
							// else
							if (last_sqn<0) last_sqn=sqn;
							if (sqn<last_sqn) sqn+=TWO_16;
							int sqn_diff=sqn-last_sqn;
							if (sqn_diff>TWO_16/2) continue; // discarded out of sequence packet
							// else
							last_sqn=sqn&0xffff;

							if (silence_padding) {
								// silence padding
								long timestamp=rtp_packet.getTimestamp();
								if (last_timestamp<0) last_timestamp=timestamp;
								if (timestamp<last_timestamp) timestamp+=(TWO_32);
								long timestamp_diff=timestamp-last_timestamp;
								last_timestamp=timestamp&0xffffffff;
								if (rtp_payload_format!=null) {
									int silence_len=rtp_payload_format.getSilencePad(sqn_diff,timestamp_diff,silence_buffer,0);
									if (silence_len>0) {
										output_stream.write(silence_buffer,0,silence_len);
									}
								}
							}
						}
						// get payload
						byte[] payload_buf=rtp_packet.getPacketBuffer();
						int payload_off=rtp_packet.getHeaderLength();
						int payload_len=rtp_packet.getPayloadLength();
						
						// remove possible RTP payload format
						int unformatted_len=(rtp_payload_format!=null)? rtp_payload_format.removeRtpPayloadFormat(payload_buf,payload_off,payload_len) : payload_len;
					
						// drop a small percentage of packets
						if (random_early_drop>0 && (++packet_counter)%random_early_drop==0) continue;
						// else 

						if (additional_decoder!=null) unformatted_len=additional_decoder.encode(payload_buf,payload_off,unformatted_len,payload_buf,payload_off);

						// write the payload data to the output_stream
						try {
							output_stream.write(payload_buf,payload_off,unformatted_len);
						}
						catch (IOException e) {
							System.out.println("DEBUG: RtpStreamReceiver: write(buf,off="+payload_off+", len="+unformatted_len+"): error: "+e);
							throw e;
						}
					}
					// check whether remote socket address is changed
					SocketAddress source_soaddr=rtp_socket.getRemoteSourceSoAddress();
					if (remote_soaddr==null || !remote_soaddr.equals(source_soaddr)) {
						remote_soaddr=source_soaddr;
						if (listener!=null) listener.onRemoteSoAddressChanged(this,remote_soaddr);
					}
				}
				catch (java.io.InterruptedIOException e) {
					// Ignore.
				}
			}
		}
		catch (Exception e) {
			running=false;
			error=e;
			if (DEBUG)
				LOG.debug("Exception.", e);
		}
		
		// close RtpSocket
		rtp_socket.close();
		
		// free all
		output_stream=null;
		rtp_socket=null;
		
		onRtpStreamReceiverTerminated(error);
	}

	/**
	 * Callback invoked when stream terminates.
	 *
	 * @param error
	 *        The error, if termination was caused by an exception.
	 */
	protected void onRtpStreamReceiverTerminated(Exception error) {
		if (DEBUG)
			LOG.debug("rtp receiver terminated");
		if (listener!=null) listener.onRtpStreamReceiverTerminated(this,error);
	}


	/** Sets the packet random early drop (RED) value; if greater than 0, it is the inverse of the packet drop rate.
	  * @param random_early_drop the number of packets that separates two drops; a value of 0 means no drop. */
	public void setRED(int random_early_drop) {
		this.random_early_drop=random_early_drop;
	}


	/** Gets the packet random early drop (RED) value; if greater than 0, it is the inverse of the packet drop rate. */
	public int getRED() {
		return random_early_drop;
	}

}


