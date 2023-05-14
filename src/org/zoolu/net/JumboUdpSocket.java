/*
 * Copyright (C) 2008 Luca Veltri - University of Parma - Italy
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



import org.zoolu.tools.BinTools;
import org.zoolu.tools.Timer;
import org.zoolu.tools.TimerListener;

import java.util.Hashtable;
import java.util.Vector;
import java.util.Date;
import java.util.Calendar;



/** JumboUdpSocket extends UdpSocket providing fragmentation and
  * reassembly functions.
  * <p/>
  * It can be used to deal with non-standard UDP/IP implementations that
  * force an upper bound for datagram length
  * (e.g. in Symbian 7.0 or 8.0 it is limited up to 512 bytes).
  */
public class JumboUdpSocket extends UdpSocket
{
   /** Whether printing debugging information on standard error output. */
   public static boolean DEBUG=false;
   
   /** Reassembly timeout [millisecs]. */
   public static long REASSEMBLY_TIMEOUT=4000;

   /** Inter-packet departure time [millisecs], used to prevent that some packets are lost. */
   public static long DEPARTURE_TIME=5;

   /** Maximum datagram length */
   int max_len;

   /** Packet cache */
   Cache cache=new Cache();



   /** Creates a new JumboUdpSocket. */ 
   public JumboUdpSocket(int port, int max_len) throws java.net.SocketException
   {  super(port);
      init(max_len);
   }

   /** Creates a new JumboUdpSocket. */ 
   public JumboUdpSocket(int port, IpAddress ipaddr, int max_len) throws java.net.SocketException
   {  super(port,ipaddr);
      init(max_len);
   }
  
   /** Inits the JumboUdpSocket. */ 
   private void init(int max_len)
   {  this.max_len=max_len;
   }


   /** Gets the maximum datagram length (MTU). */
   public int getMaximumLength()
   {  return max_len;
   }

   /** Sets the maximum datagram length (MTU). */
   public void setMaximumLength(int max_len)
   {  this.max_len=max_len;
   }
   
   /** Receives a datagram packet from this socket. */
   public void receive(UdpPacket pkt) throws java.io.IOException
   {  int pkt_length=pkt.getLength();
      super.receive(pkt);
      if(pkt.getLength() < JumboPacket.HLEN) return;
      // else 
      JumboPacket jumbo_packet=JumboPacket.parsePacket(BinTools.getBytes(pkt.getData(),pkt.getOffset(),pkt.getLength()));   
      if (!jumbo_packet.isFragment())
      {  pkt.setData(jumbo_packet.getData(),jumbo_packet.getDataOffset(),jumbo_packet.getDataLength());
         pkt.setLength(jumbo_packet.getDataLength());
         if (DEBUG) println("Received: "+jumbo_packet.toString()+" (NO FRAGS)");
         return;
      }
      else
      {  if (DEBUG) println("Received: "+jumbo_packet.toString()+" (FRAGMENT)");
         cache.putFragment(jumbo_packet);
         Vector fragments=cache.getFragments(jumbo_packet.getPacketId());
         if (JumboPacket.isComplete(fragments))
         {  // last fragment
            try
            {  JumboPacket jp=JumboPacket.doReassemble(fragments);
               pkt.setData(jp.getData(),jp.getDataOffset(),jp.getDataLength());
               cache.removeFragments(jumbo_packet.getPacketId());
               if (DEBUG) println("Received: "+jp.toString()+" ("+fragments.size()+" FRAGS)");
            }
            catch (Exception e) {  e.printStackTrace();  System.exit(0);  }
            return;
         }
         else
         {  // more fragments
            pkt.setLength(pkt_length);
            receive(pkt);
         }
      }
   }

   /** Sends an UDP packet from this socket. */ 
   public void send(UdpPacket pkt) throws java.io.IOException
   {  JumboPacket jumbo_packet=new JumboPacket(pkt.getData(),pkt.getOffset(),pkt.getLength());
      JumboPacket[] fragments=jumbo_packet.doFragment(max_len);   
      if (DEBUG) println("Send: "+jumbo_packet.toString()+" ("+fragments.length+" FRAGS)");
      for (int i=0; i<fragments.length; i++)
      {  byte[] raw_packet=fragments[i].getBytes();
         super.send(new UdpPacket(raw_packet,0,raw_packet.length,pkt.getIpAddress(),pkt.getPort()));
         // Wait a while before sennding next packet (otherwise some packets are lost)..
         sleep(DEPARTURE_TIME);
      }
   }

   /** Converts this object to a String. */
   public String toString()
   {  return "limited-"+super.toString();
   }

   /** Waits for <i>time</i> millisecs. */
   static void sleep(long time)
   {  try {  if (time>0) Thread.sleep(time);  } catch (Exception e) {}
   }

   /** Prints a message on standard error output. */
   static void println(String message)
   {  System.err.println("JumboUDP: "+message);
   }

}



/** Cache for temporarily storing packet fragments.
  */
class Cache implements TimerListener
{
   /** Table:(Integer)pkt_id-->(Vector<JumboPAcket>)packet_fragments */
   Hashtable table_fragments=new Hashtable();

   /** Table:(Integer)pkt_id-->Timers */
   Hashtable table_timers=new Hashtable();

   /** Table:Timer-->(Integer)pkt_id */
   Hashtable table_ids=new Hashtable();
   
   
   /** Creates a new Cache. */
   public Cache()
   {  // do nothing
   }

   /** Puts a packet fragment. */
   public synchronized void putFragment(JumboPacket jumbo_packet)
   {  Integer packet_id=new Integer(jumbo_packet.getPacketId());
      if (!table_fragments.containsKey(packet_id))
      {  table_fragments.put(packet_id,new Vector());
         Timer timer=new Timer(JumboUdpSocket.REASSEMBLY_TIMEOUT,this);
         timer.start();
         table_timers.put(packet_id,timer);
         table_ids.put(timer,packet_id);
      }
      Vector fragments=(Vector)table_fragments.get(packet_id);
      fragments.addElement(jumbo_packet);
   }

   /** Gets packet fragments. */
   public synchronized Vector getFragments(int id)
   {  Integer packet_id=new Integer(id);
      return (Vector)table_fragments.get(packet_id);
   }
   
   /** Removes packet fragments. */
   public synchronized void removeFragments(int id)
   {  Integer packet_id=new Integer(id);
      Timer timer=(Timer)table_timers.get(packet_id);
      timer.halt();
      table_ids.remove(timer);
      table_timers.remove(packet_id);
      table_fragments.remove(packet_id);
   }

   /** Removes packet fragments. */
   public synchronized void removeFragment(Timer t)
   {  if (table_ids.containsKey(t))
      {  Integer packet_id=(Integer)table_ids.get(t);
         if (JumboUdpSocket.DEBUG)
         {  Vector frags=(Vector)table_fragments.get(packet_id);
            int total_len=0;
            for (int i=0; i<frags.size(); i++) total_len+=((JumboPacket)frags.elementAt(i)).getDataLength();
            JumboUdpSocket.println("PACKET LOST ("+frags.size()+"): "+total_len+"/"+((JumboPacket)frags.elementAt(0)).getOriginalPacketLength());
         }
         table_ids.remove(t);
         table_timers.remove(packet_id);
         table_fragments.remove(packet_id);
      }
   }

   /** From TimerListener. When the Timer exceeds. */
   public void onTimeout(Timer t)
   {  removeFragment(t);
   }

}
