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



import org.zoolu.net.SocketAddress;
import org.zoolu.sip.provider.SipStack;
import org.zoolu.sip.provider.SipProvider;
import org.zoolu.sip.message.Message;
import org.zoolu.sip.address.*;
import org.zoolu.sip.header.*;
import org.zoolu.sdp.*;
import org.zoolu.tools.Parser;

import java.util.Hashtable;
import java.util.Vector;



/** Class SipMangler collects static methods for mangling SIP messages.
  */
public class SipMangler
{   
   /** Escape char. */
   //protected static final char ESC='/'; 
   protected static final char ESC='Z'; 

   /** Escaped sequence for ESC. */
   protected static final String escaped_ESC="~"; 

   /** Start sequence for mangled URL (without the leading ESC). */
   protected static final String startof_URL="MjSBC2U-"; 

   /** Escaped sequence for '@' char (without the leading ESC). */
   protected static final String escaped_AT="AT-"; 

   /** Escaped sequence for ':' char (without the leading ESC). */
   protected static final String escaped_PORT="PORT-"; 

   /** Magic cookie that distinguishes mangled SIP URIs.
     * <p/>
     * It is equal to the start sequence, formed as <i>ESC</i>+<i>startof_URL</i>. */
   public static final String magic_cookie=ESC+startof_URL; 


   /** Mangles request-uri */
   /*public static Message mangleRequestLine(Message msg, SipURL uri)
   {  RequestLine rl=msg.getRequestLine();
      RequestLine new_rl=new RequestLine(rl.getMethod(),uri);
      msg.setRequestLine(new_rl);
      return msg;
   }*/


   /** Whether request-uri has been mangled. */
   public static boolean isRequestLineMangled(Message msg)
   {  SipURL request_uri=msg.getRequestLine().getAddress();
      String username=request_uri.getUserName();
      return (username!=null && username.startsWith(magic_cookie));
   }


   /** Unmangles request-uri */
   public static Message unmangleRequestLine(Message msg)
   {  RequestLine rl=msg.getRequestLine();
      SipURL request_uri=rl.getAddress();
      String username=request_uri.getUserName();
      if (username!=null && username.startsWith(magic_cookie))
      {  request_uri=unstuffUrl(request_uri);
         RequestLine new_rl=new RequestLine(rl.getMethod(),request_uri);
         msg.setRequestLine(new_rl);
      }
      return msg;
   }


   /** Mangles Contact address with the new NameAddress. */
   /*public static Message mangleContact(Message msg, NameAddress naddress)
   {  if (!msg.hasContactHeader()) return msg;
      //else
      ContactHeader ch=msg.getContactHeader();
      if (!ch.isStar())
      {  ContactHeader new_ch=new ContactHeader(naddress);
         if (ch.hasExpires()) new_ch.setExpires(ch.getExpires());
         msg.setContactHeader(new_ch);
      }
      return msg;
   }*/


   /** Mangles/unmangles Contact address in automatic and reversible manner. */
   /*public static Message mangleContact(Message msg, String host, int port)
   {  if (!msg.hasContactHeader()) return msg;
      //else
      ContactHeader ch=msg.getContactHeader();
      if (!ch.isStar())
      {  NameAddress name_address=ch.getNameAddress();
         SipURL contact_url=name_address.getAddress();
         if (contact_url.getUserName().startsWith(magic_cookie)) contact_url=unstuffedUrl(contact_url);
         else contact_url=stuffedUrl(contact_url,host,port);
         ContactHeader new_ch=new ContactHeader(new NameAddress(name_address.getDisplayName(),contact_url));
         if (ch.hasExpires()) new_ch.setExpires(ch.getExpires());
         msg.setContactHeader(new_ch);
      }
      return msg;
   }*/


   /** Mangles Contact address in automatic and reversible manner. */
   public static Message mangleContact(Message msg, String host, int port)
   {  if (!msg.hasContactHeader()) return msg;
      //else
      ContactHeader ch=msg.getContactHeader();
      if (!ch.isStar())
      {  NameAddress name_address=ch.getNameAddress();
         SipURL contact_url=name_address.getAddress();
         // do not mangle already mangled URLs
         //String username=contact_url.getUserName();
         //if (username==null || !username.startsWith(magic_cookie))
         {  contact_url=stuffUrl(contact_url,host,port);
            ContactHeader new_ch=new ContactHeader(new NameAddress(name_address.getDisplayName(),contact_url));
            if (ch.hasExpires()) new_ch.setExpires(ch.getExpires());
            msg.setContactHeader(new_ch);
         }
      }
      return msg;
   }


   /** Unmangles Contact address. */
   public static Message unmangleContact(Message msg)
   {  if (!msg.hasContactHeader()) return msg;
      //else
      ContactHeader ch=msg.getContactHeader();
      if (!ch.isStar())
      {  NameAddress name_address=ch.getNameAddress();
         SipURL contact_url=name_address.getAddress();
         String username=contact_url.getUserName();
         if (username!=null && username.startsWith(magic_cookie))
         {  contact_url=unstuffUrl(contact_url);
            ContactHeader new_ch=new ContactHeader(new NameAddress(name_address.getDisplayName(),contact_url));
            if (ch.hasExpires()) new_ch.setExpires(ch.getExpires());
            msg.setContactHeader(new_ch);
         }
      }
      return msg;
   }


   /** Stuffes a String. */
   private static String stuffString(String str)
   {  StringBuffer stuffed=new StringBuffer();
      for (int i=0; i<str.length(); )
      {  char c=str.charAt(i++);
         switch (c)
         {  case ESC : stuffed.append(ESC).append(escaped_ESC); break;
            case '@' : stuffed.append(ESC).append(escaped_AT); break;
            case ':' : stuffed.append(ESC).append(escaped_PORT); break;
            default  : stuffed.append(c);
         }
      }
      return stuffed.toString();
   }


   /** Untuffes a String. */
   private static String unstuffString(String str)
   {  StringBuffer unstuffed=new StringBuffer();
      for (int i=0; i<str.length(); )
      {  char c=str.charAt(i++);
         if (c==ESC)
         {  if (str.startsWith(escaped_ESC,i))
            {  unstuffed.append(ESC);
               i+=escaped_ESC.length();
            }
            else
            if (str.startsWith(escaped_AT,i))
            {  unstuffed.append('@');
               i+=escaped_AT.length();
            }
            else
            if (str.startsWith(escaped_PORT,i))
            {  unstuffed.append(':');
               i+=escaped_PORT.length();
            }
         }
         else unstuffed.append(c);
      }
      return unstuffed.toString();
   }


   /** Stuffes a SipURL in automatic and reversible manner. */
   private static SipURL stuffUrl(SipURL url, String host, int port)
   {  //String str=url.toString().substring(4); // skip "sip:"
      String str=url.getHost();
      if (url.hasUserName()) str=url.getUserName()+"@"+str;
      if (url.hasPort()) str=str+":"+url.getPort();
      String stuffed_str=stuffString(str);
      String username=magic_cookie+stuffed_str;
      return new SipURL(username,host,port);
   }


   /** Unstuffes a stuffed SipURL. */
   private static SipURL unstuffUrl(SipURL url)
   {  String str=url.getUserName();
      if (str!=null && str.startsWith(magic_cookie))
      {  String unstuffed_str=unstuffString(str.substring(magic_cookie.length()));
         url=new SipURL(unstuffed_str);
      }
      return url;
   }


   /** Mangles the Record-Route url */
   /*public static Message mangleRecordRoute(Message msg, SipURL url)
   {  if (!msg.hasRecordRouteHeader()) return msg;
      //else
      MultipleHeader routes=msg.getRecordRoutes();
      routes.removeTop();
      url.addLr();
      routes.addTop(new RecordRouteHeader(new NameAddress(url)));
      msg.removeRecordRoutes();
      msg.addRecordRoutes(routes);
      return msg;
   }*/


   /** Mangles last Record-Route url */
   /*public static Message mangleLastRecordRoute(Message msg, SipURL url)
   {  if (!msg.hasRecordRouteHeader()) return msg;
      //else
      MultipleHeader routes=msg.getRecordRoutes();
      routes.removeBottom();
      url.addLr();
      routes.addBottom(new RecordRouteHeader(new NameAddress(url)));
      msg.removeRecordRoutes();
      msg.addRecordRoutes(routes);
      return msg;
   }*/

   /** Mangles the Route url */
   /*public static Message mangleRoute(Message msg, SipURL url)
   {  if (!msg.hasRouteHeader()) return msg;
      //else
      msg.removeRouteHeader();
      msg.addRouteHeader(new RouteHeader(new NameAddress(url)));
      return msg;
   }*/


   /** Mangles the Via url */
   /*public static Message mangleVia(Message msg, String addr, int port)
   {  if (!msg.hasViaHeader()) return msg;
      //else
      ViaHeader via=msg.getViaHeader();
      int rport=via.getRport();
      String branch=via.getBranch();
      via=new ViaHeader(addr,port);
      via.setRport(rport);
      via.setBranch(branch);
      msg.removeViaHeader();
      msg.addViaHeader(via);
      return msg;
   }/*


   /** Mangles the sdp 'connection' field */
   protected static SessionDescriptor mangleSdpConnection(SessionDescriptor sdp, String masq_addr)
   {  //printLog("inside mangleSdpConnection()",Log.LEVEL_MEDIUM);       
      // masquerade the address
      String dest_addr=sdp.getConnection().getAddress();              
      //printLog("address "+dest_addr+" becomes "+masq_addr,Log.LEVEL_HIGH);        
      ConnectionField conn=sdp.getConnection();
      conn=new ConnectionField(conn.getAddressType(),masq_addr,conn.getTTL(),conn.getNum());
      sdp.setConnection(conn);
      return sdp;
   }
      

   /** Mangles the sdp media port */
   protected static SessionDescriptor mangleSdpMediaPort(SessionDescriptor sdp, String media, int masq_port)
   {  //printLog("inside mangleSdpConnection()",Log.LEVEL_MEDIUM); 
      Vector old_media_descriptors=sdp.getMediaDescriptors();
      Vector new_media_descriptors=new Vector(); 
           
      for (int i=0; i<old_media_descriptors.size(); i++)
      {  MediaDescriptor md=(MediaDescriptor)old_media_descriptors.elementAt(i);
         MediaField mf=md.getMedia();
         if (mf.getMedia().equals(media))
         {  // masquerade the port
            //printLog(media+" port "+mf.getPort()+" becomes "+masq_port,Log.LEVEL_HIGH);        
            mf=new MediaField(mf.getMedia(),masq_port,0,mf.getTransport(),mf.getFormats());
            md=new MediaDescriptor(mf,md.getConnection(),md.getAttributes());
         }
         new_media_descriptors.addElement(md);
      }
      // update the sdp with the new media descriptors
      sdp.removeMediaDescriptors();
      sdp.addMediaDescriptors(new_media_descriptors);
      return sdp;
   }


   /** Mangles the body */
   public static Message mangleBody(Message msg, String masq_addr, String[] media, int[] masq_port)
   {  //printLog("inside mangleBody()",Log.LEVEL_MEDIUM);
      if (!msg.hasBody()) return msg;
      //else
      SessionDescriptor sdp=new SessionDescriptor(msg.getBody());
      sdp=mangleSdpConnection(sdp,masq_addr);       
      for (int i=0; i<media.length; i++) sdp=mangleSdpMediaPort(sdp,media[i],masq_port[i]);
      msg.setBody(sdp.toString());
      return msg;
   }

}