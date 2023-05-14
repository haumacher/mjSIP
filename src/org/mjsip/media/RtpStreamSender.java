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


import java.io.InputStream;

import org.mjsip.rtp.RtpControl;
import org.mjsip.rtp.RtpPacket;
import org.mjsip.rtp.RtpPayloadFormat;
import org.mjsip.rtp.RtpSocket;
import org.zoolu.net.IpAddress;
import org.zoolu.net.SocketAddress;
import org.zoolu.net.UdpSocket;
import org.zoolu.util.Encoder;
import org.zoolu.util.Random;


/** RtpStreamSender is a generic RTP sender.
  * It takes media from a given InputStream and sends it through RTP packets to a remote destination.
  */
public class RtpStreamSender extends Thread implements RtpControlledSender {
	
	/** Inter-time of RTCP Sending Report (SR) packets [millisecs]. */
	public static long RTCP_SR_TIME=5000;

	/** Size of the input buffer (that must fit the RTP header plus the input data) */
	public static final int BUFFER_SIZE=1472; // 1500 (Etherent MTU) - 20 (IPH) - 8 (UDPH)

	/** Minimum inter-packet time as packet time fraction (min_time=pcket_time/x); default value is 2. */
	public static int MIN_INTER_PACKET_TIME_FRACTION=2;

	/** Force this SSRC if greater than 0 */
	public static long STATIC_SSRC=-1;

	/** Force this initial sequence number if greater than 0 */
	public static int STATIC_SQN=-1;

	/** Force this initial timestamp if greater than 0 */
	public static long STATIC_TIMESTAMP=-1;

	/** Whether working in debug mode. */
	public static boolean DEBUG=false;

	// DEBUG DROP RATE
	/** Mean interval between two dropping periods (in mean number of packets) */
	//public static int DEBUG_DROP_RATE=500; // drop interval every 10s 
	public static int DEBUG_DROP_RATE=0;
	/** Duration of a packet dropping period (in number of dropped packets) */
	public static int DEBUG_DROP_TIME=100; // drop interval duration = 2s 

	/** RTP header length. */
	private static final int RTPH_LEN=12;

	
	/** Listener */
	RtpStreamSenderListener listener=null;

	/** The InputStream */
	InputStream input_stream=null;

	/** The RtpSocket */
	RtpSocket rtp_socket=null;
	
	/** Whether the socket has been created here */
	boolean socket_is_local_attribute=false;   

	/** Remote destination UDP socket address */
	SocketAddress remote_soaddr;
	
	/** Payload type */
	int p_type;
	
	/** Number of samples per second */
	long sample_rate;
	
	/** Number of packets per second */
	//long packet_rate;

	/** Number of audio channels */
	int channels;

	/** Inter-packet time (in milliseconds) */
	long packet_time;

	/** Number of payload bytes per packet */
	int payload_size;

	/** Whether it works synchronously with a local clock, or it it acts as slave of the InputStream  */
	boolean do_sync=true;

	/** Synchronization correction value, in milliseconds.
	  * It accellarates (sync_adj<0) or reduces (sync_adj>0) the sending rate respect to the nominal value. */
	long sync_adj=0;

	/** Whether it is running */
	boolean running=false;   

	/** Synchronization source (SSRC) identifier. */
	//long ssrc=0;
	long ssrc=Random.nextLong()&0xffffffff;

	/** RTP sequence number (SQN) */
	//int sqn=0;
	int sqn=Random.nextInt()&0xffff;

	/** RTP timestamp */
	//long timestamp=0;
	long timestamp=Random.nextLong()&0xffffffff;

	/** Packet counter */
	long packet_count=0;   

	/** Octect counter */
	long octect_count=0;   

	/** RTCP */
	 RtpControl rtp_control=null;

	/** RTP payload format */
	RtpPayloadFormat rtp_payload_format=null;
	
	/** Additional RTP payload encoder */
	Encoder additional_encoder;

	

	/** Constructs a RtpStreamSender.
	  * @param input_stream the stream source
	  * @param do_sync whether time synchronization must be performed by the RtpStreamSender,
	  *        or it is performed by the InputStream (e.g. by the system audio input)
	  * @param payload_type the payload type
	  * @param sample_rate audio sample rate
	  * @param channels number of audio channels (1 for mono, 2 for stereo)
	  * @param packet_time the inter-packet time (in milliseconds); it is used in the calculation of the the next departure time, in case of do_sync==true,
	  * @param payload_size the size of the payload
	  * @param additional_encoder additional RTP payload encoder (optional)
	  * @param dest_addr the destination address
	  * @param dest_port the destination port */
	public RtpStreamSender(InputStream input_stream, boolean do_sync, int payload_type, long sample_rate, int channels, long packet_time, int payload_size, Encoder additional_encoder, String dest_addr, int dest_port, RtpStreamSenderListener listener) throws java.net.SocketException, java.net.UnknownHostException {
		//if (src_port>0) src_socket=new UdpSocket(src_port); else
		UdpSocket src_socket=new UdpSocket(0);
		socket_is_local_attribute=true;
		init(input_stream,do_sync,payload_type,sample_rate,channels,packet_time,payload_size,additional_encoder,src_socket,dest_addr,dest_port,listener);
	}                


	/** Constructs a RtpStreamSender.
	  * @param input_stream the stream to be sent
	  * @param do_sync whether time synchronization must be performed by the RtpStreamSender,
	  *        or it is performed by the InputStream (e.g. by the system audio input)
	  * @param payload_type the payload type
	  * @param sample_rate audio sample rate
	  * @param channels number of audio channels (1 for mono, 2 for stereo)
	  * @param packet_time the inter-packet time (in milliseconds); it is used in the calculation of the next departure time, in case of do_sync==true,
	  * @param payload_size the size of the payload
	  * @param additional_encoder additional RTP payload encoder (optional)
	  * @param src_socket the socket used to send the RTP packet
	  * @param dest_addr the destination address
	  * @param dest_port the destination port */
	public RtpStreamSender(InputStream input_stream, boolean do_sync, int payload_type, long sample_rate, int channels, long packet_time, int payload_size, Encoder additional_encoder, UdpSocket src_socket, String dest_addr, int dest_port, RtpStreamSenderListener listener) throws java.net.UnknownHostException {
		init(input_stream,do_sync,payload_type,sample_rate,channels,packet_time,payload_size,additional_encoder,src_socket,dest_addr,dest_port,listener);
	}                


	/** Inits the RtpStreamSender */
	private void init(InputStream input_stream, boolean do_sync, int payload_type, long sample_rate, int channels, long packet_time, int payload_size, Encoder additional_encoder, UdpSocket src_socket, String dest_addr, int dest_port, RtpStreamSenderListener listener) throws java.net.UnknownHostException {
		this.listener=listener;
		this.input_stream=input_stream;
		this.p_type=payload_type;
		this.sample_rate=sample_rate;
		this.channels=channels;
		this.packet_time=packet_time;
		this.payload_size=payload_size;
		this.additional_encoder=additional_encoder;
		this.do_sync=do_sync;
		this.remote_soaddr=new SocketAddress(IpAddress.getByName(dest_addr),dest_port);
		rtp_socket=new RtpSocket(src_socket,remote_soaddr);
	}          


	/** Sets RTCP. */
	public void setControl(RtpControl rtp_control) {
		this.rtp_control=rtp_control;
		if (rtp_control!=null) rtp_control.setRtpSender(this);
	}


	/** Gets the local port. */
	public int getLocalPort() {
		if (rtp_socket!=null) return rtp_socket.getUdpSocket().getLocalPort();
		else return 0;
	}


	/** Changes the remote destination socket address. */
	public void setRemoteSoAddress(SocketAddress remote_soaddr) throws java.net.UnknownHostException {
		if (rtp_socket!=null) rtp_socket.setRemoteDestSoAddress(remote_soaddr);
	}


	/** Gets the remote destination socket address. */
	public SocketAddress getRemoteSoAddress() {
		return remote_soaddr;
	}


	/** Sets the synchronization adjustment time (in milliseconds). 
	  * It accellarates (sync_adj &lt; 0) or reduces (sync_adj &gt; 0) the sending rate respect to the nominal value.
	  * @param sync_adj the difference between the actual inter-packet sending time respect to the nominal value (in milliseconds). */
	public void setSyncAdj(long sync_adj) {
		this.sync_adj=sync_adj;
	}


	/** Gets the synchronization source (SSRC) identifier. */
	public long getSSRC() {
		return ssrc;
	}


	/** Gets the current RTP timestamp value. */
	public long getRtpTimestamp() {
		return timestamp;
	}


	/** Gets the total number of sent packets. */
	public long getPacketCounter() {
		return packet_count;
	}


	/** Gets the total number of sent octects. */
	public long getOctectCounter() {
		return octect_count;
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
	public void run() {
		
		if (rtp_socket==null || input_stream==null) return;
		//else
		
		// number of payload bytes after RTP formatting
		int formatted_len=(rtp_payload_format!=null)? rtp_payload_format.getRtpPayloadFormatLength(payload_size) : payload_size;		
		byte[] packet_buffer=new byte[BUFFER_SIZE];
		RtpPacket rtp_packet=new RtpPacket(packet_buffer,RTPH_LEN); // empty RTP packet
		if (STATIC_SSRC>=0) ssrc=STATIC_SSRC;
		if (STATIC_SQN>=0) sqn=STATIC_SQN;
		if (STATIC_TIMESTAMP>=0) timestamp=STATIC_TIMESTAMP;
		rtp_packet.setHeader(p_type,ssrc,sqn,timestamp);

		long time=0;
		long time_sync=0;
		//long timestamp=0;
		long start_time=System.currentTimeMillis();
		//long byte_rate=payload_size*1000/packet_time;
		
		// sending report counters
		//long packet_count=0;
		//long octect_count=0;
		long next_report_time=0;

		running=true;

		if (DEBUG) println("RTP: localhost:"+rtp_socket.getUdpSocket().getLocalPort()+" --> "+remote_soaddr);
		if (DEBUG) println("RTP: sending pkts of "+(formatted_len)+" bytes of RTP payload");

		// DEBUG DROP RATE
		int debug_drop_count=0;

		Exception error=null;
		try {
			while (running) {
				
				if (time>=next_report_time) {
					//if (rtp_control!=null) rtp_control.send(new local.net.SrRtcpPacket(rtp_packet.getSsrc(),System.currentTimeMillis(),timestamp,packet_count,octect_count));
					if (rtp_control!=null) rtp_control.sendReport();
					next_report_time+=RTCP_SR_TIME;
				}
				//java.util.Arrays.fill(packet_buffer,RTPH_LEN,formatted_len,(byte)0);
				int len=input_stream.read(packet_buffer,RTPH_LEN,payload_size);
				// check running state, since the read() method may be blocking..
				if (!running) break;
				// else
				if (len>0) {					
					// apply possible RTP payload format (if required, e.g. in case of AMR)
					formatted_len=(rtp_payload_format!=null)? rtp_payload_format.setRtpPayloadFormat(packet_buffer,RTPH_LEN,len) : len;

					// do additional encoding (if defined)
					formatted_len=(additional_encoder!=null)? additional_encoder.encode(packet_buffer,RTPH_LEN,formatted_len,packet_buffer,RTPH_LEN): formatted_len;

					rtp_packet.setSequenceNumber(sqn++);
					rtp_packet.setTimestamp(timestamp);
					rtp_packet.setPayloadLength(formatted_len);
					
					// DEBUG DROP RATE BEGIN
					//rtp_socket.send(rtp_packet);
					if (debug_drop_count==0) {
						rtp_socket.send(rtp_packet);
						if (DEBUG_DROP_RATE>0 && Random.nextInt(DEBUG_DROP_RATE)==0) debug_drop_count=DEBUG_DROP_TIME;
					}
					else debug_drop_count--;
					// DEBUG DROP RATE END
					
					// update rtp timestamp (in milliseconds)
					//long this_packet_time=(num*1000)/byte_rate;
					long this_packet_time=packet_time*len/payload_size/channels;
					time+=this_packet_time;
					timestamp+=(this_packet_time*sample_rate)/1000;
					// update sending report counters
					packet_count++;
					octect_count+=formatted_len;
					// wait for next departure
					if (do_sync) {
						time_sync+=this_packet_time+sync_adj;
						// wait before next departure..
						long sleep_time=start_time+time_sync-System.currentTimeMillis();
						// compensate possible inter-time reduction due to the approximated time obtained by System.currentTimeMillis()
						if (MIN_INTER_PACKET_TIME_FRACTION>1) {
							long min_time=this_packet_time/MIN_INTER_PACKET_TIME_FRACTION;
							if (sleep_time<min_time) sleep_time=min_time;
						}
						// sleep
						if (sleep_time>0) try {  Thread.sleep(sleep_time);  } catch (Exception e) {}
					}
				}
				else
				if (len<0) {
					if (DEBUG) println("Error reading from InputStream");
					running=false;
				}
			}
		}
		catch (Exception e) {
			running=false;
			error=e;
			if (DEBUG) e.printStackTrace();
		}     

		//if (DEBUG) println("rtp time:  "+time);
		//if (DEBUG) println("real time: "+(System.currentTimeMillis()-start_time));

		// close RtpSocket and local UdpSocket
		UdpSocket udp_socket=rtp_socket.getUdpSocket();
		rtp_socket.close();
		if (socket_is_local_attribute && udp_socket!=null) udp_socket.close();
		
		// free all references
		input_stream=null;
		rtp_socket=null;

		if (DEBUG) println("rtp sender terminated");
		if (listener!=null) listener.onRtpStreamSenderTerminated(this,error);
	}
	
	
	/** Sets RTP payload format. */
	public void setRtpPayloadFormat(RtpPayloadFormat rtp_payload_format) {
		this.rtp_payload_format=rtp_payload_format;
	}


	/** Gets the total number of UDP sent packets. */
	public long getUdpPacketCounter() {
		if (rtp_socket!=null) return rtp_socket.getUdpSocket().getSenderPacketCounter();
		else return 0;
	}


	/** Gets the total number of UDP sent octects. */
	public long getUdpOctectCounter() {
		if (rtp_socket!=null) return rtp_socket.getUdpSocket().getSenderOctectCounter();
		else return 0;
	}


	/** Debug output */
	private static void println(String str) {
		System.err.println("RtpStreamSender: "+str);
	}

}
