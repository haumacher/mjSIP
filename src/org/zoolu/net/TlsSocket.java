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

package org.zoolu.net;



import javax.net.ssl.SSLSocket;



/** TlsSocket provides a uniform interface to TLS transport protocol.
  */
public class TlsSocket extends TcpSocket
{

   /** Creates a new TlsSocket */ 
   TlsSocket()
   {  super();
   }

   /** Creates a new TlsSocket */ 
   TlsSocket(SSLSocket sock)
   {  super(sock);
   }

   /** Creates a new TlsSocket */ 
   /*public TlsSocket(String host, int port) throws java.io.IOException
   {  super(host,port);
   }*/

   /** Creates a new TlsSocket */ 
   /*public TlsSocket(String host, int port, IpAddress local_ipaddr, int local_port) throws java.io.IOException
   {  super(host,port,local_ipaddr,local_port);
   }*/

   /** Creates a new TlsSocket */ 
   /*public TlsSocket(IpAddress ipaddr, int port) throws java.io.IOException
   {  super(ipaddr,port);
   }*/

   /** Creates a new TlsSocket */ 
   /*public TlsSocket(IpAddress ipaddr, int port, IpAddress local_ipaddr, int local_port) throws java.io.IOException
   {  super(ipaddr,port,local_ipaddr,local_port);
   }*/

}
