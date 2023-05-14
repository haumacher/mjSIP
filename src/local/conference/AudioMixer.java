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


import local.media.RtpStreamSender;
import local.media.RtpStreamReceiver;
import local.media.RtpStreamReceiverListener;
import org.zoolu.net.SocketAddress;
import org.zoolu.sip.provider.SipStack;
import org.zoolu.tools.Log;
import org.zoolu.tools.ExceptionPrinter;
import java.io.*;
import java.net.DatagramSocket;
import java.util.Hashtable;
import java.util.Enumeration;


/** AudioMixer acts as audio mixers.
  * It receives a number of RTP input streams and produces the same number of
  * RTP output streams. Each output stream is the mix of all other input streams.
  * The number of inputs/outputs can dynamically vary, by adding  or removing participants.  
  * <p/>
  * AudioMixer may act as central conference unit.
  * In H.323 architecture, AudioMixer could correspond to the MP (Multipoint Processor).
  * In 3GPP, AudioMixer could correspond to the MRFP (Multimedia Resource Function Processor).
  */
public class AudioMixer implements RtpStreamReceiverListener
{     
   /** Whether working in debug mode. */
   public static boolean DEBUG=true;

   /** Default mixer codec */
   static final String CODEC="PCMU";
   /** Default mixer payload type */
   static final int PAYLOAD_TYPE=0;
   /** Default mixer sample rate */
   static final float SAMPLE_RATE=8000;
   /** Default mixer frame size */
   static final int FRAME_SIZE=160;

   /** Local sockets, as table of (Integer)local_port-->(Socket)socket. */
   Hashtable sockets;
   /** Rtp senders, as table of (Integer)local_port-->(RtpStreamSender)rtp_sender. */
   Hashtable rtp_senders;
   /** Rtp receivers, as table of (Integer)local_port-->(RtpStreamReceiver)rtp_receiver. */
   Hashtable rtp_receivers;
   
   /** The central audio mixer */
   protected Mixer mixer;

   /** Log */
   Log log=null;


   /** Creates a new AudioMixer. */
   public AudioMixer(Log log)
   {  init(log);
   }   


   /** Inits the AudioMixer. */
   private void init(Log log)
   {  this.log=log;
      mixer=new Mixer(); 
      sockets=new Hashtable();
      rtp_senders=new Hashtable();
      rtp_receivers=new Hashtable();
   }   


   /** Adds a new end point. */
   public void addEndpoint(int local_port, String dest_addr, int dest_port)
   {  addEndpoint(local_port,dest_addr,dest_port,CODEC,PAYLOAD_TYPE,SAMPLE_RATE,FRAME_SIZE);
   }   


   /** Adds a new end point. */
   public void addEndpoint(int local_port, String dest_addr, int dest_port, String codec, int payload_type, float sample_rate, int frame_size)
   {  printLog("add fullduplex channel: localhost:"+local_port+" <--> "+dest_addr+":"+dest_port+" ("+codec+")",Log.LEVEL_HIGH);    
      try
      {  Integer id=new Integer(local_port);
         DatagramSocket socket=new DatagramSocket(local_port);

         InputStream is=mixer.newOutputLine(id);
         RtpStreamSender rtp_sender=newRtpStreamSender(is,codec,payload_type,sample_rate,frame_size,socket,dest_addr,dest_port);
         rtp_sender.start();

         OutputStream os=mixer.newInputLine(id);
         RtpStreamReceiver rtp_receiver=newRtpStreamReceiver(os,codec,sample_rate,socket);
         rtp_receiver.start();

         sockets.put(id,socket);
         rtp_senders.put(id,rtp_sender);
         rtp_receivers.put(id,rtp_receiver);
      }
      catch (Exception e)
      {  printException(e,Log.LEVEL_HIGH);
      }
   } 


   /** Gets a new RtpStreamSender.
     * This method is used by method addEndpoint() and can be re-defined 
     * by a class that extends AudioMixer in order to implement new output encoding mechanisms.
     * @return It returns a new RtpStreamSender. */
   protected RtpStreamSender newRtpStreamSender(InputStream is, String codec, int payload_type, float sample_rate, int frame_size, DatagramSocket socket, String dest_addr, int dest_port) throws Exception
   {  return new RtpStreamSender(is,true,payload_type,(int)(sample_rate/frame_size),frame_size,socket,dest_addr,dest_port);
   }


   /** Gets a new RtpStreamReceiver.
     * This method is used by method addEndpoint() and can be re-defined 
     * by a class that extends AudioMixer in order to implement new input decoding mechanisms.
     * @return It returns a new RtpStreamReceiver. */
   protected RtpStreamReceiver newRtpStreamReceiver(OutputStream os, String codec, float sample_rate, DatagramSocket socket)  throws Exception
   {  return new RtpStreamReceiver(os,socket,this);
   }


   /** Removes an end point. */
   public void removeEndpoint(int local_port)
   {  printLog("remove fullduplex channel: localhost:"+local_port,Log.LEVEL_HIGH);
      Integer id=new Integer(local_port);
      
      int pause=2*RtpStreamReceiver.SO_TIMEOUT;

      ((RtpStreamSender)rtp_senders.get(id)).halt();
      rtp_senders.remove(id);

      ((RtpStreamReceiver)rtp_receivers.get(id)).halt();
      rtp_receivers.remove(id);

      try { Thread.sleep(pause); } catch (Exception e) {}      

      mixer.removeOutputLine(id);
      mixer.removeInputLine(id);
      
      ((DatagramSocket)sockets.get(id)).close();
      sockets.remove(id);
   } 
 

   /** Stops it. */
   public void halt()
   {  int pause=2*RtpStreamReceiver.SO_TIMEOUT;
      // close all RTP senders and receivers
      for (Enumeration e=rtp_senders.elements(); e.hasMoreElements(); )
      {  ((RtpStreamSender)e.nextElement()).halt();
      }
      for (Enumeration e=rtp_receivers.elements(); e.hasMoreElements(); )
      {  ((RtpStreamReceiver)e.nextElement()).halt();
      }
      try { Thread.sleep(pause); } catch (Exception e) {}      
      // close all mixer lines and local sockets
      close();
   }


   /** Closes all mixer lines and local sockets. */
   protected void close()
   {  // close all mixer lines 
      try { mixer.close(); } catch (IOException e) {}
      // close all local sockets   
      for (Enumeration e=sockets.elements(); e.hasMoreElements(); )
      {  ((DatagramSocket)e.nextElement()).close();
      }
      // free data references
      mixer=null;
      sockets=null;
   }


  //************************** callbacks *************************/


   /** From RtpStreamReceiverListener. When the remote socket address (source) is changed. */
   public void onRemoteSoAddressChanged(RtpStreamReceiver rr, SocketAddress remote_soaddr)
   {  //System.out.println("DEBUG: changed RTP remote address: "+remote_soaddr);
      Integer id=new Integer(rr.getLocalPort());
      RtpStreamSender rs=(RtpStreamSender)rtp_senders.get(id);
      if (rs!=null) rs.setRemoteSoAddress(remote_soaddr);
   }



  //**************************** Logs ****************************/

   /** Adds a new string to the default Log. */
   private void printLog(String str,int level)
   {  if (log!=null) log.println("AudioMixer: "+str,ConferenceServer.LOG_OFFSET+level); 
      if (DEBUG) System.err.println("AudioMixer: "+str);
   }


   /** Adds the Exception message to the default Log */
   private final void printException(Exception e, int level)
   {  printLog("Exception: "+ExceptionPrinter.getStackTraceOf(e),level);
   }
  
}