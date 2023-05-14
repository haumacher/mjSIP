/*
 * Copyright (C) 2005 Luca Veltri - University of Parma - Italy
 * 
 * This file is part of MjSip (http://www.mjsip.org)
 * 
 * MjSip is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * MjSip is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with MjSip; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 * 
 * Author(s):
 * Luca Veltri (luca.veltri@unipr.it)
 */

package org.zoolu.sip.message;


import org.zoolu.sip.provider.*;
import org.zoolu.sip.header.*;
import org.zoolu.sip.address.*;
import org.zoolu.sip.message.SipMethods;
import org.zoolu.net.UdpPacket;
import java.util.*;


/** Class BaseMessageOtp implements a generic SIP Message.
  * It extends class BaseMessage adding one-time-parsing functionality
  * (it parses the entire Message just when it is costructed).
  * <p/> At the contrary, class BaseMessage works in a just-in-time manner
  * (it parses the message each time a particular header field is requested).
  */
public abstract class BaseMessageOtp extends BaseMessage
{

   /** Whether printing debugging information on standard error output. */
   public static boolean DEBUG=false;

   /** Request-line */
   protected RequestLine request_line=null;
   /** Status-line */
   protected StatusLine status_line=null;

   /** Vector of all header fields */
   protected Vector headers=null;
   /** Message body */
   protected String body=null;


   /** Costructs a new empty Message. */
   public BaseMessageOtp()
   {  headers=new Vector();
   }
      
   /** Costructs a new Message. */
   public BaseMessageOtp(byte[] data, int offset, int len)
   {  parseIt(new String(data,offset,len));
   }

   /** Costructs a new Message. */
   public BaseMessageOtp(UdpPacket packet)
   {  parseIt(new String(packet.getData(),packet.getOffset(),packet.getLength()));
   }

   /** Costructs a new Message. */
   public BaseMessageOtp(String str)
   {  parseIt(str);
   }

   /** Costructs a new Message. */
   public BaseMessageOtp(BaseMessageOtp msg)
   {  remote_addr=msg.remote_addr;
      remote_port=msg.remote_port;
      transport_proto=msg.transport_proto;
      conn_id=msg.conn_id;
      //packet_length=msg.packet_length;
      request_line=msg.request_line;
      status_line=msg.status_line;
      headers=new Vector();
      for (int i=0; i<msg.headers.size(); i++) headers.addElement(msg.headers.elementAt(i));
      body=msg.body;
   }
   
   /** Sets the entire message. */
   public void setMessage(String str)
   {  parseIt(str);
   }   

   /** Parses the Message from a String. */
   protected void parseIt(String str)
   {  try
      {  SipParser par=new SipParser(str);
         String proto_version=str.substring(0,SIP_VERSION.length());     
         if (proto_version.equalsIgnoreCase(SIP_VERSION)) status_line=par.getStatusLine();
         else request_line=par.getRequestLine();
         
         headers=new Vector();
         Header h=par.getHeader();
         while (h!=null)
         {  headers.addElement(h);
            h=par.getHeader();
         }
         ContentLengthHeader clh=getContentLengthHeader();
         if (clh!=null)
         {  int len=clh.getContentLength();
            body=par.getString(len);
         }
         else
         if (getContentTypeHeader()!=null)
         {  body=par.getRemainingString();
            if (body.length()==0) body=null;
         }
      }
      catch (Exception e)
      {  if (DEBUG) System.err.println("BaseMessageOTP: parser error");
      }
   }

   /** Gets string representation of Message. */
   public String toString()
   {  StringBuffer str=new StringBuffer();
      if (request_line!=null) str.append(request_line.toString());
      else if (status_line!=null) str.append(status_line.toString());
      for (int i=0; i<headers.size(); i++) str.append(((Header)headers.elementAt(i)).toString());
      str.append("\r\n");
      if (body!=null) str.append(body);
      return str.toString();
   }
   
   /** Gets message length. */
   public int getLength()
   {  return toString().length();
   }   


   //**************************** Requests ****************************/

   /** Whether Message is a Request. */
   public boolean isRequest()
   {  if (request_line!=null) return true;
      else return false;
   }
   
   /** Whether Message is a <i>method</i> request. */
   public boolean isRequest(String method)
   {  if (request_line!=null && request_line.getMethod().equalsIgnoreCase(method)) return true;
      else return false;
   }


   /** Whether Message has the request-line. */
   protected boolean hasRequestLine()
   {  return request_line!=null;
   }

   /** Gets the RequestLine of the Message (Returns null if called for no request message). */
   public RequestLine getRequestLine()
   {  return request_line;
   }

   /** Sets the RequestLine of the Message. */
   public void setRequestLine(RequestLine rl)
   {  request_line=rl;
   }   
   
   /** Removes the RequestLine of the Message. */
   public void removeRequestLine()
   {  request_line=null;
   } 


   //**************************** Responses ****************************/

   /** Whether Message is a Response. */
   public boolean isResponse() throws NullPointerException
   {  if (status_line!=null) return true;
      else return false;
   }
   
   /** Whether Message has status-line. */
   protected boolean hasStatusLine()
   {  return status_line!=null;
   }

   /** Gets the StautsLine of the Message (Returns null if called for no response message). */
   public StatusLine getStatusLine()
   {  return status_line;
   }

   /** Sets the StatusLine of the Message. */
   public void setStatusLine(StatusLine sl)
   {  status_line=sl;
   }      
   
   /** Removes the StatusLine of the Message. */
   public void removeStatusLine()
   {  status_line=null;
   } 


   //**************************** Generic Headers ****************************/

   /** Removes Request\Status Line of the Message. */
   protected void removeFirstLine()
   {  removeRequestLine();
      removeStatusLine();
   }
     
   /** Gets the position of header <i>hname</i>.. */
   protected int indexOfHeader(String hname) 
   {  for (int i=0; i<headers.size(); i++)
      {  Header hi=(Header)headers.elementAt(i);
         if (hname.equalsIgnoreCase(hi.getName())) return i;
      }
      return -1;
   }

   /** Gets the first Header of specified name (Returns null if no Header is found). */
   public Header getHeader(String hname)
   {  int i=indexOfHeader(hname);
      if (i<0) return null;
      else return (Header)headers.elementAt(i);
   }

   /** Gets a Vector of all Headers of specified name (Returns empty Vector if no Header is found). */
   public Vector getHeaders(String hname)
   {  Vector v=new Vector();
      for (int i=0; i<headers.size(); i++)
      {  Header hi=(Header)headers.elementAt(i);
         if (hname.equalsIgnoreCase(hi.getName())) v.addElement(hi);
      }
      return v; 
   }

   /** Adds Header at the top/bottom.
     * The bottom is considered before the Content-Length and Content-Type headers. */
   public void addHeader(Header header, boolean top) 
   {  int pos=0;
      if (!top)
      {  pos=headers.size();
         // if Content_Length is present, jump before
         int cl=indexOfHeader(SipHeaders.Content_Length);
         if (cl>=0 && cl<pos) pos=cl;
         // if Content_Type is present, jump before
         int ct=indexOfHeader(SipHeaders.Content_Type);
         if (ct>=0 && ct<pos) pos=ct;
      }
      headers.insertElementAt(header,pos);
   }
   
   /** Adds a Vector of Headers at the top/bottom. */
   public void addHeaders(Vector headers, boolean top) 
   {  int pos=0;
      if (!top)
      {  pos=headers.size();
         // if Content_Length is present, jump before
         int cl=indexOfHeader(SipHeaders.Content_Length);
         if (cl>=0 && cl<pos) pos=cl;
         // if Content_Type is present, jump before
         int ct=indexOfHeader(SipHeaders.Content_Type);
         if (ct>=0 && ct<pos) pos=ct;
      }
      for (int i=0; i<headers.size(); i++) this.headers.insertElementAt(headers.elementAt(i),pos+i);
   }

   /** Adds MultipleHeader(s) <i>mheader</i> at the top/bottom. */
   public void addHeaders(MultipleHeader mheader, boolean top) 
   {  if (mheader.isCommaSeparated()) addHeader(mheader.toHeader(),top); 
      else addHeaders(mheader.getHeaders(),top);
   }

   /** Adds Header before the first header <i>refer_hname</i>
     * . <p>If there is no header of such type, it is added at top. */
   public void addHeaderBefore(Header new_header, String refer_hname) 
   {  int i=indexOfHeader(refer_hname);
      if (i<0) i=0;
      headers.insertElementAt(new_header,i);
   }

   /** Adds MultipleHeader(s) before the first header <i>refer_hname</i>
     * . <p>If there is no header of such type, they are added at top. */
   public void addHeadersBefore(MultipleHeader mheader, String refer_hname) 
   {  if (mheader.isCommaSeparated()) addHeaderBefore(mheader.toHeader(),refer_hname); 
      else
      {  int index=indexOfHeader(refer_hname);
         if (index<0) index=0;
         Vector hs=mheader.getHeaders();
         for (int k=0; k<hs.size(); k++) headers.insertElementAt(hs.elementAt(k),index+k);
      }
   }

   /** Adds Header after the first header <i>refer_hname</i>
     * . <p>If there is no header of such type, it is added at bottom. */
   public void addHeaderAfter(Header new_header, String refer_hname) 
   {  int i=indexOfHeader(refer_hname);
      if (i>=0) i++; else i=headers.size();
      headers.insertElementAt(new_header,i);
   }

   /** Adds MultipleHeader(s) after the first header <i>refer_hname</i>
     * . <p>If there is no header of such type, they are added at bottom. */
   public void addHeadersAfter(MultipleHeader mheader, String refer_hname) 
   {  if (mheader.isCommaSeparated()) addHeaderAfter(mheader.toHeader(),refer_hname); 
      else
      {  int index=indexOfHeader(refer_hname);
         if (index>=0) index++; else index=headers.size();
         Vector hs=mheader.getHeaders();
         for (int k=0; k<hs.size(); k++) headers.insertElementAt(hs.elementAt(k),index+k);
      }
   }

   /** Removes first Header of specified name. */
   public void removeHeader(String hname)
   {  removeHeader(hname,true);
   }

   /** Removes first (or last) Header of specified name.. */
   public void removeHeader(String hname, boolean first)
   {  int index=-1;
      for (int i=0 ; i<headers.size(); i++)
      {  Header hi=(Header)headers.elementAt(i);
         if (hname.equalsIgnoreCase(hi.getName()))
         {  index=i;
            if (first) i=headers.size();
         }
      }
      if (index>=0) headers.removeElementAt(index);
   }
   
   /** Removes all Headers of specified name. */
   public void removeAllHeaders(String hname) 
   {  for (int i=0 ; i<headers.size(); i++)
      {  Header hi=(Header)headers.elementAt(i);
         if (hname.equalsIgnoreCase(hi.getName()))
         {  headers.removeElementAt(i);
            i--;
         }
      }
   }
   
   /** Sets the Header <i>hd</i> removing any previous headers of the same type.. */
   public void setHeader(Header hd) 
   {  boolean not_found=true;
      String hname=hd.getName();
      for (int i=0 ; i<headers.size(); i++)
      {  Header hi=(Header)headers.elementAt(i);
         if (hname.equalsIgnoreCase(hi.getName()))
         {  if (not_found)
            {  // replace it
               headers.setElementAt(hd,i);
               not_found=false;
            }
            else 
            {  // remove it
               headers.removeElementAt(i);
               i--;
            }
         }
      }
      if (not_found) addHeader(hd,false);
   }          

   /** Sets MultipleHeader <i>mheader</i>. */
   public void setHeaders(MultipleHeader mheader) 
   {  if (mheader.isCommaSeparated()) setHeader(mheader.toHeader()); 
      else
      {  boolean not_found=true;
         String hname=mheader.getName();
         for (int i=0 ; i<headers.size(); i++)
         {  Header hi=(Header)headers.elementAt(i);
            if (hname.equalsIgnoreCase(hi.getName()))
            {  if (not_found)
               {  // replace it
                  Vector hs=mheader.getHeaders();
                  for (int k=0; k<hs.size(); k++) headers.insertElementAt(hs.elementAt(k),i+k);
                  not_found=false;
                  i+=hs.size()-1;
               }
               else 
               {  // remove it
                  headers.removeElementAt(i);
                  i--;
               }
            }
         }
         if (not_found) addHeaders(mheader,false);
      }
   }


   //**************************** Specific Headers ****************************/
  
   /** Whether Message has Body. */   
   public boolean hasBody()
   {  return this.body!=null;
   }
   /** Gets body(content) type. */
   public String getBodyType()
   {  return getContentTypeHeader().getContentType();
   } 
   /** Sets the message body. */
   public void setBody(String content_type, String body) 
   {  removeBody();
      if (body!=null && body.length()>0)
      {  setContentTypeHeader(new ContentTypeHeader(content_type));
         setContentLengthHeader(new ContentLengthHeader(body.length()));
         this.body=body;
      }
      else
      {  setContentLengthHeader(new ContentLengthHeader(0));
         this.body=null;
      }
   }          
   /** Gets message body. The end of body is evaluated
     * from the Content-Length header if present (SIP-RFC compliant),
     * or from the end of message if no Content-Length header is present (non-SIP-RFC compliant). */
   public String getBody()
   {  return this.body;
   }  
   /** Removes the message body (if it exists) and the final empty line. */
   public void removeBody() 
   {  removeContentLengthHeader();
      removeContentTypeHeader();
      this.body=null;
   }

}
