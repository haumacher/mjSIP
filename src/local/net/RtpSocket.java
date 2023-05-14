/*
 * Copyright (C) 2005 Luca Veltri - University of Parma - Italy
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

package local.net;



//import java.net.*;
import org.zoolu.net.*;
import org.zoolu.tools.Random;
import java.io.IOException;



/** RtpSocket implements a RTP socket for receiving and sending RTP packets. 
  * <p> RtpSocket is associated to a DatagramSocket that is used
  * to send and/or receive RtpPackets.
  */
public class RtpSocket
{
   /** UDP socket */
   //DatagramSocket udp_socket;
   UdpSocket udp_socket;
        
   /** Remote address */
   //InetAddress remote_addr;
   IpAddress remote_addr;

   /** Remote port */
   int remote_port;

   /** Creates a new RTP socket (only receiver) */ 
   //public RtpSocket(DatagramSocket udp_socket)
   public RtpSocket(UdpSocket udp_socket)
   {  this.udp_socket=udp_socket;
      this.remote_addr=null;
      this.remote_port=0;
   }

   /** Creates a new RTP socket (sender and receiver) */ 
   //public RtpSocket(DatagramSocket udp_socket, InetAddress remote_address, int remote_port)
   public RtpSocket(UdpSocket udp_socket, IpAddress remote_address, int remote_port)
   {  this.udp_socket=udp_socket;
      this.remote_addr=remote_address;
      this.remote_port=remote_port;
   }

   /** Gets the UDP socket */ 
   //public DatagramSocket getDatagramSocket()
   public UdpSocket getUdpSocket()
   {  return udp_socket;
   }

   /** Gets the remote IP address */ 
   //public InetAddress getRemoteAddress()
   public IpAddress getRemoteAddress()
   {  return remote_addr;
   }

   /** Gets the remote port */ 
   public int getRemotePort()
   {  return remote_port;
   }

   /** Receives a RTP packet from this socket */
   public void receive(RtpPacket rtpp) throws IOException
   {  //DatagramPacket udp_packet=new DatagramPacket(rtpp.packet,rtpp.packet.length);
      UdpPacket udp_packet=new UdpPacket(rtpp.packet,rtpp.packet.length);
      udp_socket.receive(udp_packet);
      rtpp.packet_len=udp_packet.getLength();
      //remote_addr=udp_packet.getAddress();
      remote_addr=udp_packet.getIpAddress();
      remote_port=udp_packet.getPort();
   }
   
   /** Sends a RTP packet from this socket */      
   public void send(RtpPacket rtpp) throws IOException
   {  //DatagramPacket udp_packet=new DatagramPacket(rtpp.packet,rtpp.packet_len);
      UdpPacket udp_packet=new UdpPacket(rtpp.packet,rtpp.packet_len);
      //udp_packet.setAddress(remote_addr);
      udp_packet.setIpAddress(remote_addr);
      udp_packet.setPort(remote_port);
      udp_socket.send(udp_packet);
   }

   /** Closes this socket */      
   public void close()
   {  //udp_socket.close();
   }

}
