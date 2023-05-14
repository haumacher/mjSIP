/*
 * Copyright (C) 2006 Luca Veltri - University of Parma - Italy
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

package org.zoolu.net;


import org.zoolu.tools.Timer;
import org.zoolu.tools.TimerListener;

import java.net.DatagramSocket;
import java.util.Vector;


/** OutputRegulatedUdpSocket provides a shaped UDP transport protocol.
  * A minimum inter-packets time is guaranteed on departures.
  */
public class OutputRegulatedUdpSocket extends UdpSocket implements TimerListener
{
   /** Minimum inter-packet departure time (in milliseconds) */
   long inter_time=0; 

   /** Last departure time */
   long last_departure=0; 

   /** Packet buffer */
   Vector buffer=new Vector(); 


   /** Creates a new OutputRegulatedUdpSocket */ 
   public OutputRegulatedUdpSocket(int port, long inter_time) throws java.net.SocketException
   {  super(port);
      this.inter_time=inter_time;
   }


   /** Creates a new OutputRegulatedUdpSocket */ 
   public OutputRegulatedUdpSocket(int port, IpAddress ipaddr, long inter_time) throws java.net.SocketException
   {  super(port,ipaddr);
      this.inter_time=inter_time;
   }
 

   /** Creates a new OutputRegulatedUdpSocket */ 
   OutputRegulatedUdpSocket(DatagramSocket sock, long inter_time)
   {  super(sock);
      this.inter_time=inter_time;
   }


   /** Sets the minimum inter-packet departure time (in milliseconds) */
   synchronized public void setMinimumInterPacketTime(long time)
   {  inter_time=time;
   }


   /** Gets the minimum inter-packet departure time (in milliseconds) */
   synchronized public long getMinimumInterPacketTime()
   {  return inter_time;
   }


   /** Sends an UDP packet from this socket. */ 
   public void send(UdpPacket pkt) throws java.io.IOException
   {  sendRegulated(pkt);
   }


   /** When the Timer exceeds */
   public void onTimeout(Timer t)
   {  try { sendTop(); } catch (Exception e) {  e.printStackTrace();  }
   }


   /** Sends an UDP packet from this socket. */ 
   synchronized private void sendRegulated(UdpPacket pkt) throws java.io.IOException
   {  long now=System.currentTimeMillis();
      if (buffer.size()==0 && now>=(last_departure+inter_time)) 
      {  super.send(pkt);
         last_departure=now;
      }
      else
      {  buffer.addElement(pkt);
         if (buffer.size()==1)
         {  if (inter_time<=0) sendTop();
            else (new Timer(last_departure+inter_time-now,this)).start();
         }
      }
   }


   /** Sends the first UdpPacket in queue. */ 
   synchronized private void sendTop() throws java.io.IOException
   {  UdpPacket pkt=(UdpPacket)buffer.elementAt(0);
      buffer.removeElementAt(0);
      super.send(pkt);
      last_departure=System.currentTimeMillis();
      if (buffer.size()>0)
      {  if (inter_time<=0) sendTop();
         else (new Timer(inter_time,this)).start();
      }
   }
   
}
