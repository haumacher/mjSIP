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

package local.sbc;



import org.zoolu.net.*;
import org.zoolu.tools.Timer;
import org.zoolu.tools.TimerListener;
// logs
import org.zoolu.sip.provider.SipStack;
import org.zoolu.tools.Log;

//import java.util.Vector;



/**
  * SymmetricRegulatedUdpRelay implements a shaped symmetric bidirectional UDP relay system.
  */
public class SymmetricRegulatedUdpRelay extends SymmetricUdpRelay
{
   /** Minimum inter-packet departure time */
   long inter_time=0; 


   /** Costructs a new SymmetricRegulatedUdpRelay. */
   public SymmetricRegulatedUdpRelay(int left_port, SocketAddress left_soaddr, int right_port, SocketAddress right_soaddr, long relay_time, long inter_time, Log log, SymmetricUdpRelayListener listener)
   {  super();
      init(left_port,left_soaddr,right_port,right_soaddr,relay_time,inter_time,log,listener);
   }


   /** Initializes the SymmetricUdpRelay. */
   private void init(int left_port, SocketAddress left_soaddr, int right_port, SocketAddress right_soaddr, long relay_time, long inter_time, Log log, SymmetricUdpRelayListener listener)
   {  this.log=log;
      //this.left_port=left_port;
      this.left_soaddr=left_soaddr;
      //this.right_port=right_port;
      this.right_soaddr=right_soaddr;
      this.relay_time=relay_time;
      this.listener=listener;
      this.inter_time=inter_time;

      try
      {  //left_udp=new UdpProvider(new OutputRegulatedUdpSocket(left_port,inter_time),0,this);
         left_udp=new InputRegulatedUdpProvider(new UdpSocket(left_port),0,inter_time,this);
         printLog("udp interfce: "+left_udp.toString()+" started",Log.LEVEL_HIGH);    
         printLog("udp interfce regulated with "+inter_time+" millisecs of minimum inter-packet departure time",Log.LEVEL_HIGH);    
   
         //right_udp=new UdpProvider(new OutputRegulatedUdpSocket(right_port,inter_time),0,this);
         right_udp=new InputRegulatedUdpProvider(new UdpSocket(right_port),0,inter_time,this);
         printLog("udp interfce: "+right_udp.toString()+" started",Log.LEVEL_HIGH);
         printLog("udp interfce regulated with "+inter_time+" millisecs of minimum inter-packet departure time",Log.LEVEL_HIGH);    
      }   
      catch (Exception e)
      {  printException(e,Log.LEVEL_HIGH);
      }
   
      if (relay_time>0)
      {  long timer_time=relay_time/2;
         expire_time=System.currentTimeMillis()+relay_time;
         timer=new Timer(timer_time,null,this);
         timer.start();
      }
      last_left_change=last_right_change=System.currentTimeMillis();
   }


   // ****************************** Logs *****************************

   /** Adds a new string to the default Log */
   private void printLog(String str, int level)
   {  if (log!=null) log.println("SymmetricRegulatedUdpRelay: "+str,SessionBorderController.LOG_OFFSET+level);  
   }

}
