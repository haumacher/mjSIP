/*
 * Copyright (C) 2010 Luca Veltri - University of Parma - Italy
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



import org.zoolu.net.*;



/** UdpConnectedSocket. .
  */
public class UdpConnectedSocket extends UdpSocket
{
   /** Remote socket address */
   SocketAddress remote_soaddr;



   /** Creates a new UdpConnectedSocket. */
   public UdpConnectedSocket(SocketAddress remote_soaddr) throws java.net.SocketException
   {  super(0);
      this.remote_soaddr=remote_soaddr;
   }

   /** Creates a new UdpConnectedSocket. */
   public UdpConnectedSocket(int port, SocketAddress remote_soaddr) throws java.net.SocketException
   {  super(port);
      this.remote_soaddr=remote_soaddr;
   }

   /** Creates a new UdpConnectedSocket. */
   public UdpConnectedSocket(int port, IpAddress ipaddr, SocketAddress remote_soaddr) throws java.net.SocketException
   {  super(port,ipaddr);
      this.remote_soaddr=remote_soaddr;
   }



   /** Gets the remote socket address. */
   public SocketAddress getRemoteAddress()
   {  return remote_soaddr;
   }   

   /** Sets a new remote socket address. */
   public void setRemoteAddress(SocketAddress remote_soaddr)
   {  this.remote_soaddr=remote_soaddr;
   }   



   /** Sends a packet to the remote address (regardless of the address already set within the packet). */
   public void send(UdpPacket packet) throws java.io.IOException
   {  if (remote_soaddr!=null)
      {  packet.setIpAddress(remote_soaddr.getAddress());
         packet.setPort(remote_soaddr.getPort());
         super.send(packet);
      }
   }   

   /** Sends a packet to a given address (regardless of the address already set within the packet). */
   public void sendTo(UdpPacket packet, SocketAddress soaddr) throws java.io.IOException
   {  if (remote_soaddr!=null)
      {  packet.setIpAddress(soaddr.getAddress());
         packet.setPort(soaddr.getPort());
         super.send(packet);
      }
   }   
}
