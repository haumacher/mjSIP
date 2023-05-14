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



import org.zoolu.net.IpAddress;
import org.zoolu.net.SocketAddress;
import org.zoolu.sip.address.SipURL;
import org.zoolu.sip.header.ViaHeader;
import org.zoolu.sip.provider.*;
import org.zoolu.sip.message.Message;
import org.zoolu.tools.Log;
import org.zoolu.tools.ExceptionPrinter;

import java.util.Enumeration;



/** ExtendedSipProvider extends SipProvider implementing
  * symmetric SIP (over symmetric UDP), allowing symmetric NAT traversal.
  * <p/>
  * When sending a SIP message, as destination address it is taken
  * the source address used in the opposite direction,
  * that is the address where the messages in the opposite direction came from
  * (reverse forwarding).
  */
public class ExtendedSipProvider extends org.zoolu.sip.provider.SipProvider
{

   /** Binder of pairs of SocketAddresses */
   AddressResolver address_resolver;


   /** Inits private attribute. */ 
   private void init(long refresh_time, long keepalive_time)
   {  if (keepalive_time>0) address_resolver=new AddressResolverKeepAlive(refresh_time,log,this,keepalive_time);
      else address_resolver=new AddressResolver(refresh_time,log);
   }


   /** Costructs a new ExtendedSipProvider. 
     * Creates the ExtendedSipProvider, initializing the SipProviderListeners, the transport protocols, and other attributes. */ 
   public ExtendedSipProvider(String via_addr, int port, String[] protocols, long refresh_time, long keepalive_time)
   {  super(via_addr,port,protocols,null);
      init(refresh_time,keepalive_time);
   }    


   /** Costructs a new ExtendedSipProvider. 
     * Creates the SipProvider, initializing the SipProviderListeners, the transport protocols, the outbound proxy, and other attributes. */ 
   public ExtendedSipProvider(String via_addr, int port, String[] protocols, String host_ifaddr, long refresh_time, long keepalive_time)
   {  super(via_addr,port,protocols,host_ifaddr);
      init(refresh_time,keepalive_time);
   }


   /** Costructs a new ExtendedSipProvider. 
     * The SipProvider attributres are read from file. */ 
   public ExtendedSipProvider(String file, long refresh_time, long keepalive_time)
   {  super(file);
      init(refresh_time,keepalive_time);
   }


   //********************** extended methods **********************

   /** From TransportListener. When a new SIP message is received. */
   public synchronized void onReceivedMessage(Transport transport, Message msg)
   {
      SocketAddress src_soaddr=new SocketAddress(msg.getRemoteAddress(),msg.getRemotePort());
      
      // update src address binding for received message
      if (msg.isRequest())
      try
      {  // maintain socket address bindiding for symmetring NAT
         ViaHeader via=msg.getViaHeader();
         SocketAddress via_soaddr=new SocketAddress(via.getHost(),(via.hasPort())?via.getPort():SipStack.default_port);
         if (via_soaddr.equals(src_soaddr))
         {  if (address_resolver.contains(via_soaddr))
            {  // remove binding
               address_resolver.removeBinding(via_soaddr);        
            }
         }
         else
         {  // update binding
            address_resolver.updateBinding(via_soaddr,src_soaddr);
         }
      }
      catch (Exception e)
      {  printWarning("Error managing addressing material",Log.LEVEL_HIGH);
         printException(e,Log.LEVEL_MEDIUM);
      }

      super.onReceivedMessage(transport,msg);
   }
   

   /** Sends a Message, specifing the transport portocol, nexthop address and port. */
   public TransportConnId sendMessage(Message msg, String proto, String dest_addr, int dest_port, int ttl)
   {  // logs
      String foot_print=msg.getFirstLine();
      if (foot_print==null) foot_print="NOT a SIP message\r\n";
      printContLog("Sending message ("+msg.getLength()+" bytes): "+foot_print,Log.LEVEL_HIGH);
      String refer_addr=dest_addr;
      int refer_port=dest_port;
      if (msg.isResponse())
      {  // try to look if the via parameter "received" has changed the destination address
         ViaHeader via=msg.getViaHeader();
         if (via.hasReceived())
         {  refer_addr=via.getHost();
         }
         // try to look if the via parameter "rport" has changed the destination port
         if (via.hasRport())
         {  refer_port=via.getPort();
         }
      }
      SocketAddress refer_soaddr=new SocketAddress(refer_addr,refer_port);
      SocketAddress dest_soaddr=refer_soaddr;
      //printLog("DEBUG: refer_soaddr="+refer_soaddr.toString(),Log.LEVEL_HIGH);
      //for (Enumeration e=address_resolver.getAllSocketAddresses(); e.hasMoreElements(); )
      //{  printLog("DEBUG: resolv_soaddr="+((String)e.nextElement()),Log.LEVEL_HIGH);
      //}
      if (address_resolver.contains(refer_soaddr))
      {  dest_soaddr=address_resolver.getSocketAddress(refer_soaddr);
         printLog("CHANGING DESTINATION "+refer_soaddr+" >> "+dest_soaddr,Log.LEVEL_HIGH);
         //System.out.println("DEBUG: SPX: CHANGING DESTINATION "+refer_soaddr+" >> "+dest_soaddr);
         dest_addr=dest_soaddr.getAddress().toString();
         dest_port=dest_soaddr.getPort();
      }
      else
      {  printLog("destination unchanged: "+dest_soaddr,Log.LEVEL_HIGH);
      }
      
      return super.sendMessage(msg,proto,dest_addr,dest_port,ttl);
   }


   //**************************** logs ****************************

   /** Default log level offset */
   static final int LOG_OFFSET=1;
   
   /** Adds a new log line to the default log output */ 
   private void printContLog(String str, int level)
   {  if (log!=null)
      {  String provider_id=(isAllInterfaces())? Integer.toString(getPort()) : getInterfaceAddress().toString()+":"+getPort();
         log.print("SipProviderX-"+provider_id+": "+str,LOG_OFFSET+level);  
      }    
   }

   /** Adds a new log line to the default log output */ 
   private final void printLog(String str, int level)
   {  printContLog(str+"\r\n",level);
   }

   /** Adds a WARNING to the default Log */
   private final void printWarning(String str, int level)
   {  printLog("WARNING: "+str,level);
   }

   /** Adds the Exception message to the default Log */
   private final void printException(Exception e,int level)
   {  printLog("Exception: "+ExceptionPrinter.getStackTraceOf(e),level);
   }
}