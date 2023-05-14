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

package local.conference;



import local.net.*;
import org.zoolu.net.SocketAddress;
import org.zoolu.sip.provider.SipStack;
import org.zoolu.tools.Log;
//import java.io.*;
import java.util.Hashtable;
import java.util.Vector;



/** MultimediaResourceProcessor acts as SIP MP (Multipoint Processor).
  * It is the same as the 3GPP-MRFP (Multimedia Resource Function Processor),
  * that is the central unit that mixes and/or transcodes and/or relays
  * users' media streams.
  * <p> Currently, it simply relays the udp media streams in a
  * point-multipoint configuration without performing audio or video mixing.
  */
public class MultimediaResourceProcessor implements UdpMultiRelayListener
{     
   /** Whether working in debug mode. */
   public static boolean DEBUG=true;

   /** Log */
   Log log=null;

   /** Whether using symmetric RTP */
   boolean symmetric_rtp;
   
   /** List of active UDP multipliers */
   Hashtable multipliers;

   /** Vector of RTP/UDP connected-sockets (media endpoints) */
   Vector rtp_cosocks;

   /** Vector of RTCP/UDP connected-sockets */
   Vector rtcp_cosocks;



   /** Costructs a new MultimediaResourceProcessor. */
   public MultimediaResourceProcessor(boolean symmetric_rtp, Log log)
   {  this.log=log;
      this.symmetric_rtp=symmetric_rtp;
      multipliers=new Hashtable(); 
      rtp_cosocks=new Vector();
      rtcp_cosocks=new Vector();
      printLog("symmetric rtp: "+symmetric_rtp,Log.LEVEL_HIGH);
   }   

   /** Adds a pair of new RTP/RTCP relays. */
   public void addRtpRtcp(int local_port, String dest_addr, int dest_port)
   {  addRtp(local_port,dest_addr,dest_port);
      addRtcp(local_port+1,dest_addr,dest_port+1);
   } 

   /** Adds a new RTP relay. */
   public void addRtp(int local_port, String dest_addr, int dest_port)
   {  addToEndpoints(rtp_cosocks,local_port,new SocketAddress(dest_addr,dest_port));
   } 

   /** Adds a new RTCP relay. */
   public void addRtcp(int local_port, String dest_addr, int dest_port)
   {  addToEndpoints(rtcp_cosocks,local_port,new SocketAddress(dest_addr,dest_port));
   } 

   /** Removes a pair of RTP/RTCP relays. */
   public void removeRtpRtcp(int local_port)
   {  removeRtp(local_port);
      removeRtcp(local_port+1);
   } 
   
   /** Removes a RTP relay. */
   public void removeRtp(int local_port)
   {  removeFromEndpoints(rtp_cosocks,local_port);
   } 

   /** Removes a RTCP relay. */
   public void removeRtcp(int local_port)
   {  removeFromEndpoints(rtcp_cosocks,local_port);
   } 

   /** Adds a new relay. */
   private void addToEndpoints(Vector socket_list, int local_port, SocketAddress remote_soaddr)
   {  printLog("adding "+remote_soaddr.toString()+" to relay list",Log.LEVEL_MEDIUM);
      try
      {  UdpConnectedSocket socket=new UdpConnectedSocket(local_port,remote_soaddr);
         socket_list.addElement(socket);
         printLog("adding relay: localhost:"+local_port+" --> relay list except for "+remote_soaddr.toString(),Log.LEVEL_HIGH);
         multipliers.put(Integer.toString(local_port),new UdpMultiRelay(socket,socket_list,symmetric_rtp,true,this));
      }
      catch (java.net.SocketException e)
      {  printLog(e.getMessage(),Log.LEVEL_HIGH);
      }
   } 

   /** Removes a relay. */
   private void removeFromEndpoints(Vector socket_list, int local_port)
   {  printLog("removing relay: localhost:"+local_port,Log.LEVEL_HIGH);
      String key=Integer.toString(local_port);
      UdpMultiRelay relay=(UdpMultiRelay)multipliers.get(key);
      multipliers.remove(key);
      UdpConnectedSocket socket=relay.getSocket();
      socket_list.remove(socket);
      relay.halt();
      //socket.close();
   }


   //************************* Callblacks *************************/

   /** From UdpMultiRelayListener. When the remote source address changes. */
   public void onUdpMultiRelaySourceAddressChanged(UdpMultiRelay mrelay, SocketAddress src_soaddr)
   {  printLog("src address "+mrelay.getSocket().getRemoteAddress()+" changed to "+src_soaddr,Log.LEVEL_HIGH);
      if (symmetric_rtp) mrelay.getSocket().setRemoteAddress(src_soaddr);
   }

   /** From UdpMultiRelayListener. When UdpRelay stops relaying UDP datagrams. */
   public void onUdpMultiRelayTerminated(UdpMultiRelay mrelay)
   {  UdpConnectedSocket socket=mrelay.getSocket();
      if (socket!=null) socket.close();
   }


   //**************************** Logs ****************************/

   /** Adds a new string to the default Log. */
   private void printLog(String str,int level)
   {  if (log!=null) log.println("MMRelay: "+str,ConferenceServer.LOG_OFFSET+level);  
      if (DEBUG) System.err.println("MMRelay: "+str);
   }

}