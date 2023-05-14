/*
 * Copyright (C) 2006 Luca Veltri - University of Parma - Italy
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

package org.zoolu.sip.provider;


import org.zoolu.net.*;
import org.zoolu.sip.message.Message;

import org.zoolu.tools.Timer;
import org.zoolu.tools.TimerListener;
import java.util.Date;


/** SipKeepAlive keeps up the connection up toward a target SIP node
  * (e.g. toward the seriving proxy/gw or a remote UAS).
  * <p/>
  * It periodically sends keep-alive tokens in order to refresh TCP connection timeouts
  * and/or NAT timeouts (for the TCP and/or UDP sessions).
  */
public class SipKeepAlive extends UdpKeepAlive
{ 
   /** Class SipToken extends class Message in order to support simple and eventually malformed SIP message tokens. */
   private static class SipToken extends Message
   {  /** The raw message */
      String token;   
      /** Creates a new SipToken. */
      public SipToken(String str) {  super();  token=str;  }
      /** Creates a new SipToken. */
      public SipToken(Message msg) {  super();  token=msg.toString();  }   
      /** Gets string value of Message. */
      public String toString() {  return token;  }     
      /** Gets message length */
      public int getLength() {  return token.length();  }   
   }

   /** SipProvider */
   SipProvider sip_provider;
   
   /** Sip token */
   Message sip_token=null;


   /** Creates a new SipKeepAlive. */
   public SipKeepAlive(SipProvider sip_provider, SocketAddress target, long delta_time)
   {  super(target,delta_time);
      init(sip_provider,null);
      start();
   }
   
   /** Creates a new SipKeepAlive. */
   public SipKeepAlive(SipProvider sip_provider, SocketAddress target, Message sip_token, long delta_time)
   {  super(target,delta_time);
      init(sip_provider,sip_token);
      start();
   }
   

   /** Inits the SipKeepAlive. */
   private void init(SipProvider sip_provider, Message sip_token)
   {  this.sip_provider=sip_provider;
      if (sip_token==null)
      {  sip_token=new SipToken(new String(DEFAULT_TOKEN));
      }
      //if (target!=null)
      //{  sip_token.setRemoteAddress(target.getAddress().toString());
      //   sip_token.setRemotePort(target.getPort());
      //}
      this.sip_token=sip_token;
   }


   /** Sends the kepp-alive packet now. */
   public void sendToken() throws java.io.IOException
   {  // do send?
      if (!stop && target!=null && sip_provider!=null)
      {  sip_provider.sendMessage(sip_token,sip_provider.getDefaultTransport(),target.getAddress().toString(),target.getPort(),127);
      }
   }


   /** Main thread. */
   public void run()
   {  super.run();
      sip_provider=null;
   }
   
       
   /** Gets a String representation of the Object */
   public String toString()
   {  String str=null;
      if (sip_provider!=null)
      {  str="sip:"+sip_provider.getViaAddress()+":"+sip_provider.getPort()+"-->"+target.toString();
      }
      return str+" ("+delta_time+"ms)"; 
   }
    
}