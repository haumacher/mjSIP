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

package local.media;



import local.net.RtpPacket;
import local.net.RtpSocket;
//import java.net.*;
import org.zoolu.net.*;

import java.io.*;



/** RtpStreamReceiver is a generic RTP receiver.
  * It receives packets from RTP and writes media into a given OutputStream.
  */
public class RtpStreamReceiver extends Thread
{

   /** Whether working in debug mode. */
   public static boolean DEBUG=false;

   /** Time waited before starting playing out packets (in millisecs). All packet received in the meantime are dropped in order to reduce the effect of an eventual initial packet burst. */
   public static final int EARLY_DROP_TIME=200;

   /** Size of the read buffer */
   public static final int BUFFER_SIZE=32768;

   /** Maximum blocking time, spent waiting for reading new bytes [milliseconds] */
   public static final int SO_TIMEOUT=200;

   /** The OutputStream */
   OutputStream output_stream=null;

   /** The RtpSocket */
   RtpSocket rtp_socket=null;

   /** Whether the socket has been created here */
   boolean socket_is_local_attribute=false;

   /** Remote socket address */
   SocketAddress remote_soaddr=null;

   /** Whether it is running */
   boolean running=false;

   /** Packet drop rate (actually it is the inverse of the packet drop rate) */
   protected int packet_drop_rate=0;

   /** Packet counter (incremented only if packet_drop_rate>0) */
   protected int packet_counter=0;

   /** Listener */
   RtpStreamReceiverListener listener=null;



   /** Constructs a RtpStreamReceiver.
     * @param output_stream the stream sink
     * @param local_port the local receiver port */
   public RtpStreamReceiver(OutputStream output_stream, int local_port)
   {  init(output_stream,local_port,null);
   }

   /** Constructs a RtpStreamReceiver.
     * @param output_stream the stream sink
     * @param local_port the local receiver port
     * @listener the RtpStreamReceiver listener */
   //public RtpStreamReceiver(OutputStream output_stream, int local_port, RtpStreamReceiverListener listener)
   //{  init(output_stream,local_port,listener);
   //}

   /** Constructs a RtpStreamReceiver.
     * @param output_stream the stream sink
     * @param socket the local receiver UdpSocket
     * @listener the RtpStreamReceiver listener */
   public RtpStreamReceiver(OutputStream output_stream, UdpSocket socket)
   {  init(output_stream,socket,null);
   }

   /** Constructs a RtpStreamReceiver.
     * @param output_stream the stream sink
     * @param socket the local receiver UdpSocket
     * @listener the RtpStreamReceiver listener */
   public RtpStreamReceiver(OutputStream output_stream, UdpSocket socket, RtpStreamReceiverListener listener)
   {  init(output_stream,socket,listener);
   }

   /** Inits the RtpStreamReceiver.
     * @param output_stream the stream sink
     * @param local_port the local receiver port
     * @listener the RtpStreamReceiver listener */
   private void init(OutputStream output_stream, int local_port, RtpStreamReceiverListener listener)
   {  try
      {  UdpSocket socket=new UdpSocket(local_port);
         socket_is_local_attribute=true;
         init(output_stream,socket,listener);
      }
      catch (Exception e) {  e.printStackTrace();  }
   }

   /** Inits the RtpStreamReceiver.
     * @param output_stream the stream sink
     * @param socket the local receiver UdpSocket
     * @listener the RtpStreamReceiver listener */
   private void init(OutputStream output_stream, UdpSocket udp_socket, RtpStreamReceiverListener listener)
   {  this.output_stream=output_stream;
      this.listener=listener;
      if (udp_socket!=null) rtp_socket=new RtpSocket(udp_socket);
   }


   /** Gets the local port. */
   public int getLocalPort()
   {  if (rtp_socket!=null) return rtp_socket.getUdpSocket().getLocalPort();
      else return 0;
   }


   /** Whether is running */
   public boolean isRunning()
   {  return running;
   }


   /** Stops running */
   public void halt()
   {  running=false;
   }


   /** Runs it in a new Thread. */
   public void run()
   {
      if (rtp_socket==null)
      {  if (DEBUG) println("ERROR: RTP socket is null");
         return;
      }
      //else

      byte[] buffer=new byte[BUFFER_SIZE];
      RtpPacket rtp_packet=new RtpPacket(buffer,0);

      running=true;    

      if (DEBUG) println("RTP: localhost:"+rtp_socket.getUdpSocket().getLocalPort()+" <-- remotesocket");
      if (DEBUG) println("RTP: receiving pkts of MAXIMUM "+buffer.length+" bytes");

      try
      {  rtp_socket.getUdpSocket().setSoTimeout(SO_TIMEOUT);
         long early_drop_to=(EARLY_DROP_TIME>0)? System.currentTimeMillis()+EARLY_DROP_TIME : -1;
         while (running)
         {  try
            {  // read a block of data from the rtp socket
               rtp_socket.receive(rtp_packet);
               // drop the first packets in order to reduce the effect of an eventual initial packet burst
               if (early_drop_to>0 && System.currentTimeMillis()<early_drop_to) continue; else early_drop_to=-1;

               // write this block to the output_stream (only if still running..)
               if (running) write(output_stream,rtp_packet.getPacket(),rtp_packet.getHeaderLength(),rtp_packet.getPayloadLength());

               // check if remote socket address is changed
               String addr=rtp_socket.getRemoteAddress().toString();
               int port=rtp_socket.getRemotePort();
               if (remote_soaddr==null || !remote_soaddr.getAddress().toString().equals(addr) || remote_soaddr.getPort()!=port)
               {  remote_soaddr=new SocketAddress(addr,port);
                  if (listener!=null) listener.onRemoteSoAddressChanged(this,remote_soaddr);
               }
            }
            catch (java.io.InterruptedIOException e) { }
         }
      }
      catch (Exception e) {  running=false;  e.printStackTrace();  }

      // close RtpSocket and local UdpSocket
      UdpSocket udp_socket=rtp_socket.getUdpSocket();
      rtp_socket.close();
      if (socket_is_local_attribute && udp_socket!=null) udp_socket.close();
      
      // free all
      output_stream=null;
      rtp_socket=null;
      
      if (DEBUG) println("rtp receiver terminated");
   }


   /** Sets the random early drop (RED) rate. Actually it sets the inverse of the packet drop rate. */
   public void setRED(int rate)
   {  this.packet_drop_rate=rate;
   }


   /** Gets the random early drop (RED) rate. Actually it gets the inverse of the packet drop rate. */
   public int getRED()
   {  return packet_drop_rate;
   }


   /** Writes a block of bytes to an InputStream taken from a given buffer.
     * This method is used by the RtpStreamReceiver to process incoming RTP packets,
     * and can be re-defined by a class that extends RtpStreamReceiver in order to
     * implement new RTP decoding mechanisms. */
   protected void write(OutputStream output_stream, byte[] buff, int off, int len) throws Exception
   {  if (packet_drop_rate>0 && (++packet_counter)%packet_drop_rate==0) return;        
      // else
      output_stream.write(buff,off,len);
   }


   /** Debug output */
   private static void println(String str)
   {  System.err.println("RtpStreamReceiver: "+str);
   }
   

   public static int byte2int(byte b)
   {  //return (b>=0)? b : -((b^0xFF)+1);
      //return (b>=0)? b : b+0x100; 
      return (b+0x100)%0x100;
   }

   public static int byte2int(byte b1, byte b2)
   {  return (((b1+0x100)%0x100)<<8)+(b2+0x100)%0x100; 
   }
}


