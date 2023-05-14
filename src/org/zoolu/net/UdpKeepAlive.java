/*
 * Copyright (C) 2006 Luca Veltri - University of Parma - Italy
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

package org.zoolu.net;


import org.zoolu.tools.Timer;
import org.zoolu.tools.TimerListener;
import java.util.Date;


/** UdpKeepAlive keeps up the connection toward a target node
  * (e.g. toward the seriving proxy, gateway, or remote UAS).
  * It periodically sends keep-alive tokens in order to refresh NAT timeouts
  * (for the UDP session).
  * <p/>
  * It can be used for both signaling (SIP) or data plane (RTP/UDP). 
  */
public class UdpKeepAlive extends Thread
{
   /** Default udp keep-alive token */
   public static final byte[] DEFAULT_TOKEN={ (byte)'\r',(byte)'\n' };
   
   /** Destination socket address */
   protected SocketAddress target;

   /** Time between two keep-alive tokens [millisecs] */
   protected long delta_time;

   /** UdpSocket */
   protected UdpSocket udp_socket;

   /** Udp token packet */
   protected UdpPacket udp_token=null;

   /** Expiration date [millisecs] */
   protected long expire=0; 

   /** Whether it is running */
   protected boolean stop=false;


   /** Creates a new UdpKeepAlive daemon */
   protected UdpKeepAlive(SocketAddress target, long delta_time)
   {  this.target=target;
      this.delta_time=delta_time;
   }


   /** Creates a new UdpKeepAlive daemon */
   public UdpKeepAlive(UdpSocket udp_socket, SocketAddress target, long delta_time)
   {  this.target=target;
      this.delta_time=delta_time;
      init(udp_socket,null);
      start();
   }


   /** Creates a new UdpKeepAlive daemon */
   public UdpKeepAlive(UdpSocket udp_socket, SocketAddress target, UdpPacket udp_token, long delta_time)
   {  this.target=target;
      this.delta_time=delta_time;
      init(udp_socket,udp_token);
      start();
   }


   /** Inits the UdpKeepAlive */
   private void init(UdpSocket udp_socket, UdpPacket udp_packet)
   {  this.udp_socket=udp_socket;
      if (udp_token==null)
      {  byte[] buff=DEFAULT_TOKEN;
         udp_token=new UdpPacket(buff,0,buff.length);
      }
      if (target!=null)
      {  udp_token.setIpAddress(target.getAddress());
         udp_token.setPort(target.getPort());
      }
      this.udp_token=udp_token;
   }


   /** Whether the UDP relay is running */
   public boolean isRunning()
   {  return !stop;
   }

   /** Sets the time (in milliseconds) between two keep-alive tokens */
   public void setDeltaTime(long delta_time)
   {  this.delta_time=delta_time;
   }

   /** Gets the time (in milliseconds) between two keep-alive tokens */
   public long getDeltaTime()
   {  return delta_time;
   }


   /** Sets the destination SocketAddress */
   public void setDestSoAddress(SocketAddress soaddr)
   {  target=soaddr;
      if (udp_token!=null && target!=null)
      {  udp_token.setIpAddress(target.getAddress());
         udp_token.setPort(target.getPort());
      }
         
   }

   /** Gets the destination SocketAddress */
   public SocketAddress getDestSoAddress()
   {  return target;
   }


   /** Sets the expiration time (in milliseconds) */
   public void setExpirationTime(long time)
   {  if (time==0) expire=0;
      else expire=System.currentTimeMillis()+time;
   }


   /** Stops sending keep-alive tokens */
   public void halt()
   {  stop=true;
   }


   /** Sends the kepp-alive packet now. */
   public void sendToken() throws java.io.IOException
   {  // do send?
      if (!stop && target!=null && udp_socket!=null)
      {  udp_socket.send(udp_token);
      }
   }


   /** Main thread. */
   public void run()
   {  try   
      {  while(!stop)
         {  sendToken();
            Thread.sleep(delta_time);
            if (expire>0 && System.currentTimeMillis()>expire) halt(); 
         }
      }
      catch (Exception e) { e.printStackTrace(); }
      udp_socket=null;
   }
   
       
   /** Gets a String representation of the Object */
   public String toString()
   {  String str=null;
      if (udp_socket!=null)
      {  str="udp:"+udp_socket.getLocalAddress()+":"+udp_socket.getLocalPort()+"-->"+target.toString();
      }
      return str+" ("+delta_time+"ms)"; 
   }
    
}