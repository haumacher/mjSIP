/*
 * Copyright (C) 2010 Luca Veltri - University of Parma - Italy
 * 
 * This file is part of MjSip (http://www.mjsip.org)
 * 
 * MjSip is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * MjSip is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with MjSip; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 * 
 * Author(s):
 * Luca Veltri (luca.veltri@unipr.it)
 */

package local.net;


import org.zoolu.net.*;
import java.io.*;


/** UdpConnection provides a connection-like transport service with UDP.
  */
public class UdpConnection implements UdpProviderListener
{
   /** The UDP provider */ 
   UdpProvider udp_provider;  

   /** Remote socket address */
   SocketAddress remote_soaddr;

   /** UdpConnection listener */
   UdpConnectionListener listener;




   /** Costructs a new UdpConnection */
   public UdpConnection(UdpSocket socket, SocketAddress remote_soaddr, UdpConnectionListener listener)
   {  udp_provider=new UdpProvider(socket,this);
      this.remote_soaddr=remote_soaddr;
      this.listener=listener;
   }


   /** Gets the UdpSocket */ 
   public UdpSocket getUdpSocket()
   {  return udp_provider.getUdpSocket();
   }


   /** Gets the remote socket address. */
   public SocketAddress getRemoteAddress()
   {  return remote_soaddr;
   }   


   /** Sets a new remote socket address. */
   public void setRemoteAddress(SocketAddress remote_soaddr)
   {  this.remote_soaddr=remote_soaddr;
   }   


   /** Sends a packet. */
   public void send(UdpPacket packet) throws java.io.IOException
   {  if (remote_soaddr!=null)
      {  packet.setIpAddress(remote_soaddr.getAddress());
         packet.setPort(remote_soaddr.getPort());
         udp_provider.send(packet);
      }
   }   

 
   /** Gets a String representation of this object. */
   public String toString()
   {  return udp_provider.toString()+"<->"+remote_soaddr.toString();
   }



   // *************************** callbacks ***************************
   
   /** From UdpProviderListener. When a new UDP datagram is received. */
   public void onReceivedPacket(UdpProvider udp, UdpPacket packet)
   {  if (listener!=null) listener.onReceivedPacket(this,packet);
   }


   /** From UdpProviderListener. When UdpProvider terminates. */
   public void onServiceTerminated(UdpProvider udp, Exception error)
   {  if (listener!=null) listener.onConnectionTerminated(this,error);
   } 

}
