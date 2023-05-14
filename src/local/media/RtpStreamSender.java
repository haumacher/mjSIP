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

import java.io.InputStream;



/** RtpStreamSender is a generic RTP sender.
  * It takes media from a given InputStream and sends it through RTP packets to a remote destination.
  */
public class RtpStreamSender extends Thread
{
   /** Whether working in debug mode. */
   public static boolean DEBUG=false;

   /** The InputStream */
   InputStream input_stream=null;

   /** The RtpSocket */
   RtpSocket rtp_socket=null;
   
   /** Whether the socket has been created here */
   boolean socket_is_local_attribute=false;   

   /** Payload type */
   int p_type;
   
   /** Number of frame per second */
   long frame_rate;  

   /** Number of bytes per frame */
   int frame_size;

   /** Whether it works synchronously with a local clock, or it it acts as slave of the InputStream  */
   boolean do_sync=true;

   /** Synchronization correction value, in milliseconds.
     * It accellarates the sending rate respect to the nominal value,
     * in order to compensate program latencies. */
   int sync_adj=0;

   /** Whether it is running */
   boolean running=false;   



   /** Constructs a RtpStreamSender.
     * @param input_stream the stream source
     * @param do_sync whether time synchronization must be performed by the RtpStreamSender,
     *        or it is performed by the InputStream (e.g. by the system audio input)
     * @param payload_type the payload type
     * @param frame_rate the frame rate, i.e. the number of frames that should be sent per second;
     *        it is used to calculate the nominal packet time and, in case of do_sync==true,
              the next departure time
     * @param frame_size the size of the payload
     * @param dest_addr the destination address
     * @param dest_port the destination port */
   public RtpStreamSender(InputStream input_stream, boolean do_sync, int payload_type, long frame_rate, int frame_size, String dest_addr, int dest_port)
   {  init(input_stream,do_sync,payload_type,frame_rate,frame_size,null,dest_addr,dest_port);
   }                


   /** Constructs a RtpStreamSender.
     * @param input_stream the stream source
     * @param do_sync whether time synchronization must be performed by the RtpStreamSender,
     *        or it is performed by the InputStream (e.g. by the system audio input)
     * @param payload_type the payload type
     * @param frame_rate the frame rate, i.e. the number of frames that should be sent per second;
     *        it is used to calculate the nominal packet time and, in case of do_sync==true,
              the next departure time
     * @param frame_size the size of the payload
     * @param src_port the source port
     * @param dest_addr the destination address
     * @param dest_port the destination port */
   //public RtpStreamSender(InputStream input_stream, boolean do_sync, int payload_type, long frame_rate, int frame_size, int src_port, String dest_addr, int dest_port)
   //{  init(input_stream,do_sync,payload_type,frame_rate,frame_size,null,src_port,dest_addr,dest_port);
   //}                


   /** Constructs a RtpStreamSender.
     * @param input_stream the stream to be sent
     * @param do_sync whether time synchronization must be performed by the RtpStreamSender,
     *        or it is performed by the InputStream (e.g. by the system audio input)
     * @param payload_type the payload type
     * @param frame_rate the frame rate, i.e. the number of frames that should be sent per second;
     *        it is used to calculate the nominal packet time and, in case of do_sync==true,
              the next departure time
     * @param frame_size the size of the payload
     * @param src_socket the socket used to send the RTP packet
     * @param dest_addr the destination address
     * @param dest_port the destination port */
   public RtpStreamSender(InputStream input_stream, boolean do_sync, int payload_type, long frame_rate, int frame_size, UdpSocket src_socket, String dest_addr, int dest_port)
   {  init(input_stream,do_sync,payload_type,frame_rate,frame_size,src_socket,dest_addr,dest_port);
   }                


   /** Inits the RtpStreamSender */
   private void init(InputStream input_stream, boolean do_sync, int payload_type, long frame_rate, int frame_size, UdpSocket src_socket, /*int src_port,*/ String dest_addr, int dest_port)
   {
      this.input_stream=input_stream;
      this.p_type=payload_type;
      this.frame_rate=frame_rate;
      this.frame_size=frame_size;
      this.do_sync=do_sync;
      try
      {  if (src_socket==null)
         {  //if (src_port>0) src_socket=new UdpSocket(src_port); else
            src_socket=new UdpSocket(0);
            socket_is_local_attribute=true;
         }
         rtp_socket=new RtpSocket(src_socket,IpAddress.getByName(dest_addr),dest_port);
      }
      catch (Exception e) {  e.printStackTrace();  }    
   }          


   /** Gets the local port. */
   public int getLocalPort()
   {  if (rtp_socket!=null) return rtp_socket.getUdpSocket().getLocalPort();
      else return 0;
   }


   /** Changes the remote socket address. */
   public void setRemoteSoAddress(SocketAddress remote_soaddr)
   {  if (remote_soaddr!=null && rtp_socket!=null)
      try
      {  rtp_socket=new RtpSocket(rtp_socket.getUdpSocket(),IpAddress.getByName(remote_soaddr.getAddress().toString()),remote_soaddr.getPort());
      }
      catch (Exception e) {  e.printStackTrace();  }
   }


   /** Gets the remote socket address. */
   public SocketAddress getRemoteSoAddress()
   {  if (rtp_socket!=null) return new SocketAddress(rtp_socket.getRemoteAddress().toString(),rtp_socket.getRemotePort());
      else return null;
   }


   /** Sets the synchronization adjustment time (in milliseconds). */
   public void setSyncAdj(int millisecs)
   {  sync_adj=millisecs;
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
      if (rtp_socket==null || input_stream==null) return;
      //else
      
      byte[] packet_buffer=new byte[12+frame_size];
      RtpPacket rtp_packet=new RtpPacket(packet_buffer,0);
      rtp_packet.setHeader(p_type);
      int seqn=0;
      long time=0;
      long start_time=System.currentTimeMillis();
      long byte_rate=frame_rate*frame_size;
      
      running=true;
            
      if (DEBUG) println("RTP: localhost:"+rtp_socket.getUdpSocket().getLocalPort()+" --> "+rtp_socket.getRemoteAddress().toString()+":"+rtp_socket.getRemotePort());
      if (DEBUG) println("RTP: sending pkts of "+(packet_buffer.length-12)+" bytes of RTP payload");

      try
      {  while (running)
         {
            //int num=input_stream.read(packet_buffer,12,packet_buffer.length-12);
            int num=read(input_stream,packet_buffer,12,packet_buffer.length-12);
            if (num>0)
            {  rtp_packet.setSequenceNumber(seqn++);
               rtp_packet.setTimestamp(time);
               rtp_packet.setPayloadLength(num);
               rtp_socket.send(rtp_packet);
               // update rtp timestamp (in milliseconds)
               long frame_time=(num*1000)/byte_rate;
               time+=frame_time;
               // wait for next departure
               if (do_sync || sync_adj>0)
               {  // wait before next departure..
                  //long sleep_time=frame_time;
                  long sleep_time=start_time+time-System.currentTimeMillis();
                  // compensate possible inter-time reduction due to the approximated time obtained by System.currentTimeMillis()
                  long min_time=frame_time/2;
                  // compensate possible program latency
                  sleep_time-=sync_adj;
                  if (sleep_time<min_time) sleep_time=min_time;
                  if (sleep_time>0) try {  Thread.sleep(sleep_time);  } catch (Exception e) {}
               }
            }
            else
            if (num<0)
            {  if (DEBUG) println("Error reading from InputStream");
               running=false;
            }
         }
      }
      catch (Exception e) {  running=false;  e.printStackTrace();  }     

      //if (DEBUG) println("rtp time:  "+time);
      //if (DEBUG) println("real time: "+(System.currentTimeMillis()-start_time));

      // close RtpSocket and local UdpSocket
      UdpSocket socket=rtp_socket.getUdpSocket();
      rtp_socket.close();
      if (socket_is_local_attribute && socket!=null) socket.close();

      // free all
      input_stream=null;
      rtp_socket=null;

      if (DEBUG) println("rtp sender terminated");
   }
   
   
   /** Reads a block of bytes from an InputStream and put it into a given buffer.
     * This method is used by the RtpStreamSender to compose RTP packets,
     * and can be re-defined by a class that extends RtpStreamSender in order to
     * implement new RTP encoding mechanisms.
     * @return It returns the number of bytes read. */
   protected int read(InputStream input_stream, byte[] buff, int off, int len) throws Exception
   {  return input_stream.read(buff,12,buff.length-12);
   }


   /** Debug output */
   private static void println(String str)
   {  System.err.println("RtpStreamSender: "+str);
   }

}