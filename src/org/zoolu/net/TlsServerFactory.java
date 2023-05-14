/*
 * Copyright (C) 2007 Luca Veltri - University of Parma - Italy
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



import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.KeyStore;
import javax.net.ssl.*;



/** TLS server factory.
  */
public class TlsServerFactory
{  
   /** SSLServerSocketFactory */
   SSLServerSocketFactory ssl_factory;

   /** Whether using client mode in first TLS handshake */
   boolean client_mode=false;
   
   /** Whether requiring client authentication */
   boolean client_auth=false;

   /* Supported protocol versions */
   String[] supported_protocols=null;

   /* Enabled protocol versions */
   String[] enabled_protocols=null;



   /** Creates a new TlsServerFactory */
   public TlsServerFactory(TlsContext tls_context) throws java.security.KeyStoreException, java.security.KeyManagementException, java.security.UnrecoverableKeyException, java.security.NoSuchAlgorithmException
   {  KeyStore ks=tls_context.getKeyStore();
      // get key managers
      KeyManagerFactory key_manager_factory=KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
      key_manager_factory.init(ks,TlsContext.DEFAULT_PASSWORD);            
      KeyManager[] key_managers=key_manager_factory.getKeyManagers();
      // get trust managers
      TrustManagerFactory trust_manager_factory=TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
      trust_manager_factory.init(ks);            
      TrustManager[] trust_managers=trust_manager_factory.getTrustManagers();      
      // install only the key managers
      SSLContext sc=SSLContext.getInstance("SSL");
      sc.init(key_managers,trust_managers,null/*new java.security.SecureRandom()*/);
      // get the socket factory
      ssl_factory=sc.getServerSocketFactory();
   }


   /** Sets whether using client (or server) mode in its first handshake.
     * Servers normally authenticate themselves, and clients are not required to do so. */
   public void setUseClientMode(boolean flag)
   {  client_mode=flag;
   }


   /** Whether using client (or server) mode in its first handshake.
     * Servers normally authenticate themselves, and clients are not required to do so. */
   public boolean getUseClientMode()
   {  return client_mode;
   }


   /** Sets whether requiring client authentication. */
   public void setNeedClientAuth(boolean flag)
   {  client_auth=flag;
   }


   /** Whether requiring client authentication. */
   public boolean getNeedClientAuth()
   {  return client_auth;
   }


   /** Gets the list of enabled protocol versions. */
   public String[] getEnabledProtocols()
   {  if (enabled_protocols==null) initSupportedProtocols();
      return enabled_protocols;
   }


   /** Sets the list of enabled protocol versions. */
   public void setEnabledProtocols(String[] enabled_protocols)
   {  this.enabled_protocols=enabled_protocols;
   }


   /** Inits supported and enabled protocol versions. */
   private void initSupportedProtocols()
   {  try
      {  SSLServerSocket ssl_server=(SSLServerSocket)ssl_factory.createServerSocket();
         if (supported_protocols==null) supported_protocols=ssl_server.getSupportedProtocols();
         if (enabled_protocols==null) enabled_protocols=ssl_server.getEnabledProtocols();
         ssl_server.close();
      }
      catch (Exception e) {  e.printStackTrace();  }
   }


   /** Creates a new TlsServer */
   public TlsServer createTlsServer(int port, TcpServerListener listener) throws java.io.IOException
   {  SSLServerSocket ssl_server=(SSLServerSocket)ssl_factory.createServerSocket(port);
      if (client_mode) ssl_server.setUseClientMode(true);
      if (client_auth) ssl_server.setNeedClientAuth(true);
      if (enabled_protocols!=null) ssl_server.setEnabledProtocols(enabled_protocols);
      return new TlsServer(ssl_server,listener);
   }


   /** Creates a new TlsServer */
   public TlsServer createTlsServer(int port, IpAddress bind_ipaddr, TcpServerListener listener) throws java.io.IOException
   {  SSLServerSocket ssl_server=(SSLServerSocket)ssl_factory.createServerSocket(port,TlsServer.DEFAULT_SOCKET_BACKLOG,bind_ipaddr.getInetAddress());
      if (client_mode) ssl_server.setUseClientMode(true);
      if (client_auth) ssl_server.setNeedClientAuth(true);
      if (enabled_protocols!=null) ssl_server.setEnabledProtocols(enabled_protocols);
      return new TlsServer(ssl_server,listener);
   }

}
