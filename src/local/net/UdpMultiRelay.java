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
import java.io.InterruptedIOException;
import java.util.Vector;


/** UdpMultiRelay implements an UDP multiple relay agent. 
  * It receives UDP packets at a local socket and relays them
  * toward a list of remote destination.
  * <p/>
  * It can operate according to two relay rules:
  * <ul>
  *   <li/> one-to-one: packets directed to the same destination are sent using the same source socket, that is the socket where the remote node sends its packet to (per-destinaiotn outgoing socket)
  *   <li/> many-to-one: packets directed to the same destination are sent using different source sockets; that coincide with the sockets where they have been received (per-source outgoing socket)
  * </ul>
  * <p/>
  * Moreover, it can operate according to two filtering rules:
  * <ul>
  *   <li/> unfiltered: all packets are relayed to all remote nodes regardless the origin
  *   <li/> filtered: packets are not sent to the same node they come from
  * </ul>
  */
public class UdpMultiRelay extends Thread
{
   /** Local socket */
   UdpConnectedSocket socket;  

   /** Remote source addresses (where packets come from) */
   SocketAddress src_soaddr=null;  

   /** Destination sockets (where packets have to be sent to) */
   Vector dest_sockets;  

  /** Whether sending all packets to a remote node using the same socket where the remote node sends its packet to (per-destinaiotn outgoing socket). */
   boolean one2one;

   /** Whether filtering packet directed to local socket's remote address */
   boolean filtered;

   /** UdpMultiRelay listener */
   UdpMultiRelayListener listener;   

   /** Whether it is running */
   boolean stop;
   /** Maximum time that the UDP relay can remain active after been halted */
   int socket_to=3000; // 3sec 

  

   /** Creates a new UDP relay and starts it.
     * @socket local socket where packet arrives (and where are sent from, in case of per-destination outgoing socket)
     * @dest_soaddrs destination addresses (where packets have to be sent to)
     * @filter whether filtering packet directed to local socket's remote address
     * @listener listener of UdpMultiRelay events */
   public UdpMultiRelay(UdpConnectedSocket socket, Vector dest_sockets, boolean one2one, boolean filtered, UdpMultiRelayListener listener)
   {  init(socket,dest_sockets,one2one,filtered,listener);
      start();
   }
    
   /** Inits a new UDP relay and starts it.
     * @local_port local port used for both incoming and outgoing packet fows
     * @dest_soaddrs destination addresses (where packets have to be sent to)
     * @filtered_soaddr filtered destination address, where packet does not have to be sent to
     * @listener listener of UdpMultiRelay events */
   private void init(UdpConnectedSocket socket, Vector dest_sockets, boolean one2one, boolean filtered, UdpMultiRelayListener listener)
   {  this.listener=listener;
      this.socket=socket;     
      this.dest_sockets=dest_sockets;
      this.one2one=one2one;
      this.filtered=filtered;
      stop=false;
   }

   /** Gets the recv socket */
   public UdpConnectedSocket getSocket()
   {  return socket;
   }

   /** Gets the destination sockets */
   public Vector getDestSockets()
   {  return dest_sockets;
   }

   /** Whether one-to-one rule is used */
   public boolean isOneToOne()
   {  return one2one;
   }

   /** Sets one-to-one rule */
   public void setOneToOne(boolean one2one)
   {  this.one2one=one2one;
   }

   /** Whether source filtering is used */
   public boolean isFiltered()
   {  return filtered;
   }

   /** Sets source filtering */
   public void setFiltered(boolean filtered)
   {  this.filtered=filtered;
   }

   /** Stops the UDP relay */
   public void halt()
   {  stop=true;
   }

   /** Sets the maximum time that the UDP relay can remain active after been halted */
   public void setSoTimeout(int so_to)
   {  socket_to=so_to;
   }

   /** Gets the maximum time that the UDP relay can remain active after been halted */
   public int getSoTimeout()
   {  return socket_to;
   }
       
   /** Redirect packets from source addr/port to destination addr/port  */
   public void run()
   {  try   
      {  byte []buf = new byte[2000];
         
         socket.setSoTimeout(socket_to);
         while(!stop)
         {  UdpPacket packet = new UdpPacket(buf, buf.length);          
            
            // non-blocking receiver
            try
            {  socket.receive(packet);           
            }
            catch (InterruptedIOException ie) { continue; }

            SocketAddress pkt_soaddr=new SocketAddress(packet.getIpAddress(),packet.getPort());
            if (src_soaddr==null || !src_soaddr.equals(pkt_soaddr))
            {  //System.out.println("DEBUG: src address "+src_soaddr+" changed to "+pkt_soaddr);
               src_soaddr=pkt_soaddr;
               if (listener!=null) listener.onUdpMultiRelaySourceAddressChanged(this,src_soaddr);
            }

            for (int i=0; i<dest_sockets.size(); i++)
            {  try
               {  UdpConnectedSocket dest_socket=(UdpConnectedSocket)dest_sockets.elementAt(i);         
                  if (!filtered || !dest_socket.equals(socket))
                  {  if (one2one) dest_socket.send(packet);
                     else socket.sendTo(packet,dest_socket.getRemoteAddress());
                     //System.out.print("*");
                  }
                  //else System.out.print(".");
               }
               catch (ArrayIndexOutOfBoundsException e) { }
            }
         }
         //socket.close();
         if (listener!=null) listener.onUdpMultiRelayTerminated(this);
      }
      catch (Exception e) { e.printStackTrace(); } 
   }  
   
}
 