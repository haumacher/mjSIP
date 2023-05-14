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
import org.zoolu.tools.ExceptionPrinter;

import java.util.Vector;



/**
 * SymmetricUdpRelay implements a symmetric bidirectional UDP relay system.
 */
public class SymmetricUdpRelay implements UdpProviderListener, TimerListener
{
   /** Log. */
   protected Log log=null;

   /** SymmetricUdpRelay listener */
   protected SymmetricUdpRelayListener listener;
   
   /** Left side udp interface. */
   protected UdpProvider left_udp=null;  
   /** Right side udp interface. */
   protected UdpProvider right_udp=null;
   
   /** Left port. */
   //protected int left_port;  
   /** Right port. */
   //protected int right_port;

   /** Left peer address. */
   protected SocketAddress left_soaddr;
   /** Right peer address. */
   protected SocketAddress right_soaddr;

   /** Maximum time that the SymmetricUdpRelay remains active without receiving UDP datagrams (in milliseconds) */
   protected long relay_time=0;

   /** Absolute time when the SymmetricUdpRelay should expire (if no packet is received meanwhile). */
   protected long expire_time=0;

   /** Timer that fires whether the SymmetricUdpRelay must expire. */
   protected Timer timer=null;

   /** Whether the SymmetricUdpRelay is running. */
   //protected boolean is_running=true;

   /** Last change time of left soaddr (in milliseconds) */
   protected long last_left_change;

   /** Last change time of right soaddr (in milliseconds) */
   protected long last_right_change;



   /** Costructs a new SymmetricUdpRelay. */
   protected SymmetricUdpRelay() {}


   /** Costructs a new SymmetricUdpRelay. */
   /*public SymmetricUdpRelay(UdpSocket left_socket, SocketAddress left_soaddr, UdpSocket right_socket, SocketAddress right_soaddr, long relay_time, Log log, SymmetricUdpRelayListener listener)
   {  init(left_socket,left_soaddr,right_socket,right_soaddr,relay_time,log,listener);
   }*/
   
   
   /** Costructs a new SymmetricUdpRelay. */
   public SymmetricUdpRelay(int left_port, SocketAddress left_soaddr, int right_port, SocketAddress right_soaddr, long relay_time, Log log, SymmetricUdpRelayListener listener)
   {  init(left_port,left_soaddr,right_port,right_soaddr,relay_time,log,listener);
   }


   /** Initializes the SymmetricUdpRelay. */
   private void init(int left_port, SocketAddress left_soaddr, int right_port, SocketAddress right_soaddr, long relay_time, Log log, SymmetricUdpRelayListener listener)
   {  this.log=log;
      //this.left_port=left_port;
      this.left_soaddr=left_soaddr;
      //this.right_port=right_port;
      this.right_soaddr=right_soaddr;
      this.relay_time=relay_time;
      this.listener=listener;

      try
      {  left_udp=new UdpProvider(new UdpSocket(left_port),0,this);
         printLog("udp interfce: "+left_udp.toString()+" started",Log.LEVEL_HIGH);    
   
         right_udp=new UdpProvider(new UdpSocket(right_port),0,this);
         printLog("udp interfce: "+right_udp.toString()+" started",Log.LEVEL_HIGH);
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

   
   /** Returns the left UdpSocket */ 
   /*public UdpSocket getLeftSocket()
   {  return left_socket;
   }*/


   /** Returns the right UdpSocket */ 
   /*public UdpSocket getRightSocket()
   {  return right_socket;
   }*/


   /** Whether the UDP receivers are running */
   public boolean isRunning()
   {  //return is_running;
      if (left_udp!=null && left_udp.isRunning()) return true;
      if (right_udp!=null && right_udp.isRunning()) return true;
      return false;
   }


   /** Stops the SymmetricUdpRelay */
   public void halt()
   {  if (left_udp!=null) left_udp.halt();
      if (right_udp!=null) right_udp.halt();
   }


   /** Gets the left peer SocketAddress. */
   public SocketAddress getLeftSoAddress()
   {  return left_soaddr;
   }
   
   /** Sets a new left peer SocketAddress. */
   public void setLeftSoAddress(SocketAddress left_soaddr)
   {  printLog("left soaddr "+this.left_soaddr+" becomes "+left_soaddr,Log.LEVEL_HIGH);
      this.left_soaddr=left_soaddr;
      last_left_change=System.currentTimeMillis();
   }
   
   /** Gets the right peer SocketAddress. */
   public SocketAddress getRightSoAddress()
   {  return right_soaddr;
   }

   /** Sets a new right peer SocketAddress. */
   public void setRightSoAddress(SocketAddress right_soaddr)
   {  printLog("right soaddr "+this.right_soaddr+" becomes "+right_soaddr,Log.LEVEL_HIGH);
      this.right_soaddr=right_soaddr;
      last_right_change=System.currentTimeMillis();
   }


   /** Gets the time of the last change of left soaddr. */
   public long getLastLeftChangeTime()
   {  return last_left_change;
   }

   /** Gets the time of the last change of left soaddr. */
   public long getLastRightChangeTime()
   {  return last_right_change;
   }



   /** When receiving a new packet. */
   public void onReceivedPacket(UdpProvider udp_service, UdpPacket packet)
   {  
      //if (packet.getLength()<=2) return; // discard the packet
      
      // postpone the expire time 
      if (relay_time>0) expire_time=System.currentTimeMillis()+relay_time;
         
      // set addresses for outgoing packet, and check whether remote addresses are changed for incoming packet
      SocketAddress src_soaddr=new SocketAddress(packet.getIpAddress(),packet.getPort());
      SocketAddress dest_soaddr=null;
      UdpProvider udp=null;
      if (udp_service==left_udp)
      {  // set the actual dest address and src socket for outgoing packet
         dest_soaddr=right_soaddr;
         udp=right_udp;    
         // check whether the source address and port are changed for incoming packet
         if (!left_soaddr.equals(src_soaddr))
         {  //printLog("left peer addr "+left_soaddr+" changed to "+src_soaddr,Log.LEVEL_HIGH);
            if (listener!=null) listener.onSymmetricUdpRelayLeftPeerChanged(this,src_soaddr);
         }
      }
      else
      if (udp_service==right_udp)
      {  // set the actual dest address and src socket for outgoing packet
         dest_soaddr=left_soaddr;
         udp=left_udp;
         // check whether the source address and port are changed for incoming packet
         if (!right_soaddr.equals(src_soaddr))
         {  //printLog("right peer addr "+right_soaddr+" changed to "+src_soaddr,Log.LEVEL_HIGH);
            if (listener!=null) listener.onSymmetricUdpRelayRightPeerChanged(this,src_soaddr);
         }
      }
      // relay
      if (udp!=null && dest_soaddr!=null)
      {  packet.setIpAddress(dest_soaddr.getAddress());
         packet.setPort(dest_soaddr.getPort());
         try
         {  udp.send(packet);
         }
         catch (java.io.IOException e) { }
      }
   }


   /** When UdpProvider stops receiving UDP datagrams. */
   public void onServiceTerminated(UdpProvider udp_service, Exception error)
   {  printLog("udp "+udp_service.toString()+" terminated",Log.LEVEL_HIGH);
      if (error!=null) printLog("DEBUG: udp "+udp_service.toString()+" exception:\n"+error.toString(),Log.LEVEL_HIGH);
      udp_service.getUdpSocket().close();
      if (!isRunning() && listener!=null) listener.onSymmetricUdpRelayTerminated(this);
   }


   /** When the Timer exceeds */
   public void onTimeout(Timer t)
   {  long now=System.currentTimeMillis();
      if (now<expire_time)
      {  long timer_time=relay_time/2;
         timer=new Timer(timer_time,null,this);
         timer.start();
      }
      else
      {  timer=null;
         printLog("relay inactive for more than "+relay_time+"ms",Log.LEVEL_HIGH);
         halt();
      }
   }


   /** Gets a String representation of the Object */
   public String toString()
   {  return left_soaddr+"<-->"+left_udp.getUdpSocket().getLocalPort()+"[--]"+right_udp.getUdpSocket().getLocalPort()+"<-->"+right_soaddr;
   }


   // ****************************** Logs *****************************

   /** Adds a new string to the default Log */
   private void printLog(String str, int level)
   {  if (log!=null) log.println("SymmetricUdpRelay: "+str,SessionBorderController.LOG_OFFSET+level);  
   }

   /** Adds the Exception message to the default Log */
   protected void printException(Exception e,int level)
   {  printLog("Exception: "+ExceptionPrinter.getStackTraceOf(e),level);
   }

}
