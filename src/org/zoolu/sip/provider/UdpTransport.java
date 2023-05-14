/*
 * Copyright (C) 2005 Luca Veltri - University of Parma - Italy
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

package org.zoolu.sip.provider;


import org.zoolu.net.*;
import org.zoolu.sip.message.Message;
import java.io.IOException;


/** UdpTransport provides an UDP transport service for SIP.
  */
public class UdpTransport implements Transport, UdpProviderListener
{
   /** UDP protocol type */
   public static final String PROTO_UDP="udp";

   /** UDP provider */
   UdpProvider udp_provider;  

   /** Transport listener */
   TransportListener listener=null;   




   /** Creates a new UdpTransport */ 
   public UdpTransport(UdpSocket socket)
   {  udp_provider=new UdpProvider(socket,this);
   }


   /** Creates a new UdpTransport */ 
   public UdpTransport(int local_port) throws IOException
   {  initUdp(local_port,null);
   }


   /** Creates a new UdpTransport */ 
   public UdpTransport(int local_port, IpAddress host_ipaddr) throws IOException
   {  initUdp(local_port,host_ipaddr);
   }


   /** Inits the UdpTransport */ 
   protected void initUdp(int local_port, IpAddress host_ipaddr) throws IOException
   {  if (udp_provider!=null) udp_provider.halt();
      // start udp
      UdpSocket socket=(host_ipaddr==null)? new UdpSocket(local_port) : new UdpSocket(local_port,host_ipaddr);
      //UdpSocket socket=(host_ipaddr==null)? new org.zoolu.net.JumboUdpSocket(local_port,500) : new org.zoolu.net.JumboUdpSocket(local_port,host_ipaddr,500);
      udp_provider=new UdpProvider(socket,this);
      
   }


   /** Gets protocol type */ 
   public String getProtocol()
   {  return PROTO_UDP;
   }


   /** Gets port */ 
   public int getLocalPort()
   {  try {  return udp_provider.getUdpSocket().getLocalPort();  } catch (Exception e) {  return 0;  }
      
   }


   /** Sets transport listener */
   public void setListener(TransportListener listener)
   {  this.listener=listener;
   }


   /** Sends a Message to a destination address and port */
   public TransportConn sendMessage(Message msg, IpAddress dest_ipaddr, int dest_port, int ttl) throws IOException
   {  if (udp_provider!=null)
      {  byte[] data=msg.toString().getBytes();
         UdpPacket packet=new UdpPacket(data,data.length);
         // if (ttl>0 && multicast_address) do something?
         packet.setIpAddress(dest_ipaddr);
         packet.setPort(dest_port);
         udp_provider.send(packet);
      }
      return null;
   }


   /** Stops running */
   public void halt()
   {  if (udp_provider!=null) udp_provider.halt();
   }


   /** Gets a String representation of the Object */
   public String toString()
   {  if (udp_provider!=null) return udp_provider.toString();
      else return null;
   }


   //************************* Callback methods *************************
   
   /** When a new UDP datagram is received. */
   public void onReceivedPacket(UdpProvider udp, UdpPacket packet)
   {  Message msg=new Message(packet);
      msg.setRemoteAddress(packet.getIpAddress().toString());
      msg.setRemotePort(packet.getPort());
      msg.setTransport(PROTO_UDP);
      if (listener!=null) listener.onReceivedMessage(this,msg);
   }   


   /** When DatagramService stops receiving UDP datagrams. */
   public void onServiceTerminated(UdpProvider udp, Exception error)
   {  if (listener!=null) listener.onTransportTerminated(this,error);
      UdpSocket socket=udp.getUdpSocket();
      if (socket!=null) try { socket.close(); } catch (Exception e) {}
      this.udp_provider=null;
      this.listener=null;
   }   

}
