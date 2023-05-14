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


import java.io.IOException;
import java.io.InterruptedIOException;


/** InputRegulatedUdpProvider extends UdpProvider providing traffic shaping
  * to incoming packets.
  * <p/>
  * Incoming packets are read and passed to the upper layers (the provider listener)
  * with a minimum pre-defined inter-packet delay.
  */
public class InputRegulatedUdpProvider extends UdpProvider
{
   /** Minimum inter-packet arrival time (in milliseconds) */
   long inter_time=0; 


   /** Creates a new InputRegulatedUdpProvider */ 
   public InputRegulatedUdpProvider(UdpSocket socket, long inter_time, UdpProviderListener listener)
   {  super(socket,listener);
      this.inter_time=inter_time;
   }


   /** Creates a new InputRegulatedUdpProvider */ 
   public InputRegulatedUdpProvider(UdpSocket socket, long alive_time, long inter_time, UdpProviderListener listener)
   {  super(socket,alive_time,listener);
      this.inter_time=inter_time;
   }


   /** Sets the minimum inter-packet departure time (in milliseconds) */
   public void setMinimumInterPacketTime(long time)
   {  inter_time=time;
   }


   /** Gets the minimum inter-packet departure time (in milliseconds) */
   public long getMinimumInterPacketTime()
   {  return inter_time;
   }


   /** The main thread */
   public void run()
   {  
      byte[] buf=new byte[BUFFER_SIZE];
      UdpPacket packet=new UdpPacket(buf, buf.length);
               
      Exception error=null;
      long expire=0;
      if (alive_time>0) expire=System.currentTimeMillis()+alive_time;
      try   
      {  socket.setSoTimeout(socket_timeout);
         // loop
         while(!stop)
         {  try
            {  socket.receive(packet);           
            }
            catch (InterruptedIOException ie)
            {  if (alive_time>0 && System.currentTimeMillis()>expire) halt();
               continue;
            }
            if (packet.getLength()>=minimum_length)
            {  if (listener!=null) listener.onReceivedPacket(this,packet);
               if (alive_time>0) expire=System.currentTimeMillis()+alive_time;
            }
            packet=new UdpPacket(buf, buf.length);
            // starve in order to guarantee a minimum inter-packet arrival time
            if (inter_time>0) try {  Thread.sleep(inter_time);  } catch (Exception e) {}
         }
      }
      catch (Exception e)
      {  error=e;
         stop=true;
      } 
      is_running=false;
      if (listener!=null) listener.onServiceTerminated(this,error);
      listener=null;
   }

}
