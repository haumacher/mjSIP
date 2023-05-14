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

package org.mjsip.rtp;


import java.io.IOException;

import org.mjsip.media.RtpControlledReceiver;
import org.mjsip.media.RtpControlledSender;
import org.zoolu.net.IpAddress;
import org.zoolu.net.SocketAddress;
import org.zoolu.net.UdpSocket;
import org.zoolu.util.ByteUtils;


/** RtpControl sends and receives RTP control (RTCP) packets.
  */
public class RtpControl implements RtcpProviderListener {
	
	/** Debug mode */
	public static boolean DEBUG=true;

	/** In case of no sender RTP stream, whether the SSRC of RR packets is set equal to the SSRC of the received RTP stream */
	public static boolean DEBUG_RR_SSRC_SYMMETRIC=true;

	/** RTCP */
	RtcpProvider rtcp;

	/** Whether the UDP socket has been created here */
	boolean udp_socket_is_local;   

	/** Start timestamp */
	long start_timestamp=-1;

	/** RTP sender */
	RtpControlledSender rtp_sender=null;

	/** RTP receiver */
	RtpControlledReceiver rtp_receiver=null;

	/** Canonical end-point identifier (CNAME) */
	String cname;
	

	/** Creates a new RtpControl.
	  * @param cname canonical end-point identifier (CNAME)
	  * @param local_port local RTCP port */
	public RtpControl(String cname, int local_port) throws IOException {
		if (DEBUG) System.out.println("DEBUG: RtpControl: RtpControl("+local_port+")");
		this.cname=cname;
		rtcp=new RtcpProvider(new UdpSocket(local_port),this);
		udp_socket_is_local=true;
	}


	/** Creates a new RtpControl.
	  * @param cname canonical end-point identifier (CNAME)
	  * @param local_port local RTCP port
	  * @param remote_addr the remote RTCP address
	  * @param remote_port the remote RTCP port */
	public RtpControl(String cname, int local_port, String remote_addr, int remote_port) throws IOException {
		if (DEBUG) System.out.println("DEBUG: RtpControl: RtpControl("+local_port+","+remote_addr+":"+remote_port+")");
		this.cname=cname;
		rtcp=new RtcpProvider(new UdpSocket(local_port),new SocketAddress(IpAddress.getByName(remote_addr),remote_port),this);
		udp_socket_is_local=true;
	}


	/** Creates a new RtpControl.
	  * @param cname canonical end-point identifier (CNAME)
	  * @param local_socket local UDP socket for RTCP */
	public RtpControl(String cname, UdpSocket local_socket) {
		if (DEBUG) System.out.println("DEBUG: RtpControl: RtpControl("+local_socket+")");
		this.cname=cname;
		rtcp=new RtcpProvider(local_socket,this);
		udp_socket_is_local=false;
	}


	/** Creates a new RtpControl.
	  * @param cname canonical end-point identifier (CNAME)
	  * @param local_socket local UDP socket for RTCP
	  * @param remote_addr the remote RTCP address
	  * @param remote_port the remote RTCP port */
	public RtpControl(String cname, UdpSocket local_socket, String remote_addr, int remote_port) throws java.net.UnknownHostException {
		if (DEBUG) System.out.println("DEBUG: RtpControl: RtpControl("+local_socket+","+remote_addr+":"+remote_port+")");
		this.cname=cname;
		rtcp=new RtcpProvider(local_socket,new SocketAddress(IpAddress.getByName(remote_addr),remote_port),this);
		udp_socket_is_local=false;
	}


	/** Sets the destination remote socket address.   
	  * @param cname canonical end-point identifier (CNAME) */
	/*public void setCName(String cname) {
		this.cname=cname;
	}*/

	/** Sets the destination remote socket address.   
	  * @param remote_dest_soaddr the remote destination UDP socket address where RTCP packet are sent to */
	/*public void setRemoteSocketAddress(SocketAddress remote_dest_soaddr) {
		rtcp.setRemoteDestSoAddress(remote_dest_soaddr);
	}*/

	/** Sets symmetric RTCP mode.
	  * In symmetric RTCP mode outgoing RTCP packets are sent to the source address of the incoming RTCP packets. 
	  * @param symmetric_rtcp whether working in symmetric RTCP mode */
	public void setSymmetricRtcpMode(boolean symmetric_rtcp) {
		rtcp.setSymmetricRtcpMode(symmetric_rtcp);
	}

	/** Sets the RTP sender. */
	public void setRtpSender(RtpControlledSender rtp_sender) {
		this.rtp_sender=rtp_sender;
	}


	/** Sets the RTP receiver. */
	public void setRtpReceiver(RtpControlledReceiver rtp_receiver) {
		this.rtp_receiver=rtp_receiver;
	}


	/** From RtcpProviderListener. When a new RTCP packet is received. */
	public void onReceivedPacket(RtcpProvider rtcp, RtcpPacket rtcp_packet) {
		if (DEBUG) System.out.println("\nDEBUG: RtpControl: new RTCP packet received: "+rtcp_packet.getPacketLength()+"B");
		if (DEBUG) System.out.println("DEBUG: RtpControl: bytes: "+ByteUtils.asHex(rtcp_packet.getPacketBuffer(),rtcp_packet.getPacketOffset(),rtcp_packet.getPacketLength()));
		if (DEBUG) System.out.println("DEBUG: RtpControl: type: "+rtcp_packet.getPayloadType());
		if (rtcp_packet.getPayloadType()==RtcpPacket.PT_SR) {
			SrRtcpPacket sr_packet=new SrRtcpPacket(rtcp_packet);
			SrRtcpPacket.SenderInfo si=sr_packet.getSenderInfo();
			if (DEBUG) System.out.println("DEBUG: RtpControl: SR: packet count: "+si.getPacketCount());
			if (DEBUG) System.out.println("DEBUG: RtpControl: SR: octect count: "+si.getOctectCount());
			long timestamp=si.getRtpTimestamp();
			if (start_timestamp<0) start_timestamp=timestamp;
			if (DEBUG) System.out.println("DEBUG: RtpControl: SR: timestamp: "+timestamp+" ("+(timestamp-start_timestamp)+")");
		}
	}


	/** From RtcpProviderListener. When RtcpProvider terminates. */
	public void onServiceTerminated(RtcpProvider rtcp, Exception error) {
		if (udp_socket_is_local) rtcp.getUdpProvider().getUdpSocket().close();
	}


	/** Sends a RTCP packet. */
	public void send(RtcpPacket rtcp_packet) {
		try {
			rtcp.send(rtcp_packet);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}


	/** Sends a RTCP report (SR or RR). */
	public void sendReport() {
		if (DEBUG) System.out.println("DEBUG: sendReport()");
		try {
			long ssrc=(rtp_sender!=null)? rtp_sender.getSSRC() : 0;
			SrRtcpPacket.SenderInfo si=(rtp_sender!=null)? new SrRtcpPacket.SenderInfo(System.currentTimeMillis(),rtp_sender.getRtpTimestamp(),rtp_sender.getPacketCounter(),rtp_sender.getOctectCounter()) : null;
			if (DEBUG) System.out.println("DEBUG: sendReport(): sender info: "+si);
			RrRtcpPacket.ReportBlock rb=(rtp_receiver!=null)? new RrRtcpPacket.ReportBlock(rtp_receiver.getSSRC(),rtp_receiver.getFractionLost(),rtp_receiver.getCumulativePacketLost(),rtp_receiver.getHighestSqnReceived(),rtp_receiver.getInterarrivalJitter(),rtp_receiver.getLSR(),rtp_receiver.getDLSR()) : null;
			
			if (si!=null) {
				SrRtcpPacket sr=(rb!=null)? new SrRtcpPacket(ssrc,si,new RrRtcpPacket.ReportBlock[]{rb}) : new SrRtcpPacket(ssrc,si);
				if (cname==null) rtcp.send(sr);
				else {
					SdesRtcpPacket sdes=new SdesRtcpPacket(ssrc,cname);
					RtcpCompoundPacket cp=new RtcpCompoundPacket(new RtcpPacket[]{ sr, sdes });
					rtcp.send(cp);
				}
			}
			else
			if (rb!=null) {
				long rr_ssrc=(DEBUG_RR_SSRC_SYMMETRIC)? rb.getSSRC(): 0;
				RrRtcpPacket rr=new RrRtcpPacket(rr_ssrc,new RrRtcpPacket.ReportBlock[]{rb});
				if (cname==null) rtcp.send(rr);
				else {
					SdesRtcpPacket sdes=new SdesRtcpPacket(rr_ssrc,cname);
					RtcpCompoundPacket cp=new RtcpCompoundPacket(new RtcpPacket[]{ rr, sdes });
					rtcp.send(cp);
				}
			}
			
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}


	/** Closes RTCP. */
	public void halt() {
		rtcp.halt();
		rtp_sender=null;
		rtp_receiver=null;
	}

}


