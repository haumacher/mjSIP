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

package local.ua;


import org.zoolu.sip.message.*;
import org.zoolu.sip.provider.*;


/** DummyUAS is a very trivial UAS that replies to all incoming SIP requests
  * with a default response code. 
  */
public class DummyUAS implements SipProviderListener
{           
   /** Response code */
   int code;

   /** Response reason */
   String reason;


   /** Costructs a new DummyUAS. */
   public DummyUAS(int port, int code, String reason)
   {  this.code=code;
      this.reason=reason;
      SipProvider sip_provider=new SipProvider(null,port);
      sip_provider.addSelectiveListener(SipProvider.ANY,this);
   }


   // *********************** SipProvider callback ***********************

   /** When a new Message is received by the SipProvider. */
   public void onReceivedMessage(SipProvider sip_provider, Message msg)
   {  if (msg.isRequest() && !msg.isAck())
      {  Message resp=MessageFactory.createResponse(msg,code,reason,null);
      sip_provider.sendMessage(resp);
      }
   }


   // ******************************* MAIN *******************************

   /** The main method. */
   public static void main(String[] args)
   {  int port=5060;
      int code=403;
      String reason=null;
      
      try
      {  
         for (int i=0; i<args.length; i++)
         {
            if (args[i].equals("-p") && args.length>(i+1))
            {  port=Integer.parseInt(args[++i]);
               continue;
            }
            if (args[i].equals("-c") && args.length>(i+1))
            {  code=Integer.parseInt(args[++i]);
               continue;
            }
            if (args[i].equals("-r") && args.length>(i+1))
            {  reason=args[++i];
               continue;
            }
            
            // else, do:
            if (!args[i].equals("-h"))
               System.out.println("unrecognized param '"+args[i]+"'\n");
            
            System.out.println("usage:\n   java DummyUAS [options]");
            System.out.println("   options:");
            System.out.println("   -h           this help");
            System.out.println("   -p <port>    sip port");
            System.out.println("   -c <code>    response code");
            System.out.println("   -r <reason>  response reason");
            System.exit(0);
         }
                     
         new DummyUAS(port,code,reason);
      }
      catch (Exception e)  {  e.printStackTrace(); System.exit(0);  }
   }    
   
}
