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

package org.zoolu.sip.provider;


import org.zoolu.net.*;
import org.zoolu.sip.message.Message;
import java.io.IOException;


/** TcpTransportConn provides a TCP trasport service for SIP.
  */
class TcpTransportConn implements TransportConn, TcpConnectionListener
{
   /** TCP protocol type */
   static final String PROTO_TCP="tcp";

   /** TCP connection */
   TcpConnection tcp_conn;  

   /** TCP connection */
   TransportConnId connection_id;  

   /** The last time that has been used (in milliseconds) */
   long last_time;
   
   /** the current received text. */
   String text;
     
   /** TransportConn listener */
   TransportConnListener listener;   



   /** Creates a new TcpTransportConn */ 
   public TcpTransportConn(IpAddress remote_ipaddr, int remote_port, TransportConnListener listener) throws IOException
   {  this.listener=listener;
      TcpSocket socket=new TcpSocket(remote_ipaddr,remote_port);
      tcp_conn=new TcpConnection(socket,this);
      connection_id=new TransportConnId(this);
      last_time=System.currentTimeMillis();
      text="";
   }


   /** Costructs a new TcpTransportConn */
   public TcpTransportConn(TcpSocket socket, TransportConnListener listener)
   {  this.listener=listener;
      tcp_conn=new TcpConnection(socket,this);
      connection_id=new TransportConnId(this);
      last_time=System.currentTimeMillis();
      text="";
   }


   /** Gets protocol type */ 
   public String getProtocol()
   {  return PROTO_TCP;
   }


   /** Gets the remote IpAddress */
   public IpAddress getRemoteAddress()
   {  if (tcp_conn!=null) return tcp_conn.getRemoteAddress();
      else return null;
   }
   
   
   /** Gets the remote port */
   public int getRemotePort()
   {  if (tcp_conn!=null) return tcp_conn.getRemotePort();
      else return 0;
   }


   /** Gets the last time the Connection has been used (in millisconds) */
   public long getLastTimeMillis()
   {  return last_time;
   }


   /** Sends a Message */      
   public void sendMessage(Message msg) throws IOException
   {  if (tcp_conn!=null)
      {  last_time=System.currentTimeMillis();
         byte[] data=msg.toString().getBytes();
         tcp_conn.send(data);
      }
   }


   /** Stops running */
   public void halt()
   {  if (tcp_conn!=null) tcp_conn.halt();
   }


   /** Gets a String representation of the Object */
   public String toString()
   {  if (tcp_conn!=null) return tcp_conn.toString();
      else return null;
   }


   //************************* Callback methods *************************
   
   /** When new data is received through the TcpConnection. */
   public void onReceivedData(TcpConnection tcp_conn, byte[] data, int len)
   {  last_time=System.currentTimeMillis();
      
      text+=new String(data,0,len);
      SipParser par=new SipParser(text);
      Message msg=par.getSipMessage();
      while (msg!=null)
      {  //System.out.println("DEBUG: message len: "+msg.getLength());
         msg.setRemoteAddress(tcp_conn.getRemoteAddress().toString());
         msg.setRemotePort(tcp_conn.getRemotePort());
         msg.setTransport(getProtocol());
         msg.setTransportConnId(connection_id);
         if (listener!=null) listener.onReceivedMessage(this,msg);

         text=par.getRemainingString();
         //System.out.println("DEBUG: text left: "+text.length());
         par=new SipParser(text);
         msg=par.getSipMessage();
      }     
   }   


   /** When TcpConnection terminates. */
   public void onConnectionTerminated(TcpConnection tcp_conn, Exception error)  
   {  if (listener!=null) listener.onConnectionTerminated(this,error);
      TcpSocket socket=tcp_conn.getSocket();
      if (socket!=null) try { socket.close(); } catch (Exception e) {}
      this.tcp_conn=null;
      this.listener=null;
   }

}
