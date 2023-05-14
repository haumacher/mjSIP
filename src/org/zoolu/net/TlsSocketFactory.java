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
import java.net.Socket;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import javax.net.ssl.*;



/** TLS socket factory.
  */
public class TlsSocketFactory
{

   /** SSLSocketFactory */
   SSLSocketFactory ssl_factory;

   /** Whether using client mode in first TLS handshake */
   boolean client_mode=true;

   /* Supported protocol versions */
   String[] supported_protocols=null;

   /* Enabled protocol versions */
   String[] enabled_protocols=null;



   /** Creates a new TlsSocketFactory */
   public TlsSocketFactory(TlsContext tls_context) throws java.security.KeyStoreException, java.security.KeyManagementException, java.security.UnrecoverableKeyException, java.security.NoSuchAlgorithmException
   {  KeyStore ks=tls_context.getKeyStore();
      // get key managers
      KeyManagerFactory key_manager_factory=KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
      key_manager_factory.init(ks,TlsContext.DEFAULT_PASSWORD);            
      KeyManager[] key_managers=key_manager_factory.getKeyManagers();
      TrustManager[] trust_managers;
      // get trust managers
      if (tls_context.isTrustAll())
      {  X509TrustManager trust_all=new X509TrustManager()
         {  public X509Certificate[] getAcceptedIssuers() {  return new X509Certificate[0];  }
            public void checkClientTrusted(X509Certificate[] certs, String auth_type) {}
            public void checkServerTrusted(X509Certificate[] certs, String auth_type) {}
         };
         trust_managers=new TrustManager[] { trust_all };  
      }
      else
      {  TrustManagerFactory trust_manager_factory=TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
         trust_manager_factory.init(ks);            
         trust_managers=trust_manager_factory.getTrustManagers();      
      }
      // install only the trust managers
      SSLContext sc=SSLContext.getInstance("SSL");
      sc.init(key_managers,trust_managers,null/*new java.security.SecureRandom()*/);
      // get the socket factory
      ssl_factory=sc.getSocketFactory();
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


   /** Gets the list of supported protocol versions. */
   public String[] getSupportedProtocols()
   {  if (supported_protocols==null) initSupportedProtocols();
      return supported_protocols;
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
      {  SSLSocket ssl_socket=(SSLSocket)ssl_factory.createSocket();
         if (supported_protocols==null) supported_protocols=ssl_socket.getSupportedProtocols();
         if (enabled_protocols==null) enabled_protocols=ssl_socket.getEnabledProtocols();
         ssl_socket.close();
      }
      catch (Exception e) {  e.printStackTrace();  }
   }


   /** Creates a new TlsSocket */
   public TlsSocket createTlsSocket(String host, int port) throws java.io.IOException
   {  //SSLSocket ssl_socket=(SSLSocket)ssl_factory.createSocket(host,port);
      //return new TlsSocket(ssl_socket);
      SSLSocket ssl_socket=(SSLSocket)ssl_factory.createSocket();
      if (!client_mode) ssl_socket.setUseClientMode(false);
      if (enabled_protocols!=null) ssl_socket.setEnabledProtocols(enabled_protocols);
      ssl_socket.connect(new java.net.InetSocketAddress(host,port));
      return new TlsSocket(ssl_socket);
   }


   /** Creates a new TlsSocket */
   public TlsSocket createTlsSocket(IpAddress ipaddr, int port) throws java.io.IOException
   {  //SSLSocket ssl_socket=(SSLSocket)ssl_factory.createSocket(ipaddr.getInetAddress(),port);
      //return new TlsSocket(ssl_socket);
      SSLSocket ssl_socket=(SSLSocket)ssl_factory.createSocket();
      if (!client_mode) ssl_socket.setUseClientMode(false);
      if (enabled_protocols!=null) ssl_socket.setEnabledProtocols(enabled_protocols);
      ssl_socket.connect(new java.net.InetSocketAddress(ipaddr.getInetAddress(),port));
      return new TlsSocket(ssl_socket);
   }
   
}
