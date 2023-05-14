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

package org.zoolu.sip.provider;



import org.zoolu.net.*;
import org.zoolu.sip.header.*;
import org.zoolu.sip.message.Message;
import org.zoolu.sip.address.*;
import org.zoolu.sip.transaction.Transaction;
import org.zoolu.tools.*;

import java.util.Hashtable;
import java.util.Vector;
import java.util.Enumeration;
import java.util.Date;
import java.io.IOException;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;



/** SipProvider implements the SIP transport layer, that is the layer responsable for
  * sending and receiving SIP messages.
  * <p/>
  * Any SipProvider user can send SIP messages through method <i>sendMessage()</i>
  * and receive incoming SIP messages by setting itself as message listener through the method 
  * <i>addSelectiveListener()</i> (specifing also the type of messages it wants to listen to)
  * and by implementing the callback method <i>onReceivedMessage()</i> defined by the interface
  * SipProviderListener.
  * <p/>
  * if a SipProvider user wants to capture ALL received message regardless of any other
  * concurrent listening users, it may use the <i>addPromiscuousListener()<i/> method.
  * <p/>
  * SipProvider implements also multiplexing/demultiplexing service allowing a listener
  * to be bound to a specific type of SIP messages through the
  * addSelectiveListener(<i>id<i/>,<i>listener<i/>) method, where:
  * <b> - <i>id<i/> is the SIP identifier the listener has to be bound to,
  * <b> - <i>listener<i/> is the SipProviderListener that received messages are passed to.
  * <p/>
  * The SIP identifier specifies the type of messages that the listener is going to
  * receive for. Together with the SipProvider transport protocols, port numbers and
  * IP addresses, it represents the complete SIP Service Access Point (SAP) address/identifier
  * at receiving side. 
  * <p/>
  * The identifier can be of one of the three following types: transaction_id, dialog_id,
  * or method_id. These types of identifiers characterize respectively:
  * <br/> - messages within a specific transaction,
  * <br/> - messages within a specific dialog,
  * <br/> - messages related to a specific SIP method.
  * It is also possible to use the the identifier ANY to specify 
  * <br/> - all messages that are out of any transactions, dialogs, or method types
  *         already specified.
  * <p/>
  * When receiving a message, the SipProvider passes the message to any active listeners
  * added in promiscuous mode thorugh method <i>addPromiscuousListener()</i>.
  * Then the message is passed to the eventual selective listeners added
  * thorugh method <i>addSelectiveListener()</i> and matching the given message.
  * For this purpose the SipProvider first tries to look for a matching  
  * transaction id, then looks for a matching dialog id, then for a matching method id,
  * and finally for a default listener (i.e. that with id "ANY").
  * For the matched SipProviderListener, the method <i>onReceivedMessage()</i> is fired.
  * <p/>
  * Note: no 482 (Loop Detected) nor 501 (Not Implemented) responses are generated for requests
  * that does not properly match any active transactions, dialogs, nor method types.
  */
public class SipProvider implements Configurable, TransportListener
{

   // **************************** Constants ****************************

   /** UDP protocol type */
   public static final String PROTO_UDP="udp";
   /** TCP protocol type */
   public static final String PROTO_TCP="tcp";
   /** TLS protocol type */
   public static final String PROTO_TLS="tls";
   /** SCTP protocol type */
   public static final String PROTO_SCTP="sctp";
  
   /** String value "auto-configuration" used for auto configuration of the host address. */
   public static final String AUTO_CONFIGURATION="AUTO-CONFIGURATION";

   /** String value "auto-configuration" used for auto configuration of the host address. */
   public static final String ALL_INTERFACES="ALL-INTERFACES";

   /** String value "NO-OUTBOUND" used for setting no outbound proxy. */
   //public static final String NO_OUTBOUND="NO-OUTBOUND";

   /** Identifier used as listener id for capturing ANY incoming messages
     * that does not match any active method_id, transaction_id, nor dialog_id.
     * <br/> In this context, "active" means that there is a active listener
     * for that specific method, transaction, or dialog. */
   public static final SipId ANY=new SipId("ANY"); 

   /** Minimum length for a valid SIP message.  */
   private static final int MIN_MESSAGE_LENGTH=12;


   // ********************* Configurable attributes *********************

   /** Via IP address or fully-qualified domanin name (FQDN).
     * Use 'auto-configuration' for auto detection, or let it undefined. */
   String via_addr=null;

   /** Local SIP port */
   int host_port=0;

   /** Network interface (IP address) used by SIP.
     * Use 'ALL-INTERFACES' for binding SIP to all interfaces (or let it undefined). */
   String host_ifaddr=null;

   /** List of enabled transport protocols (the first protocol is used as default). */
   String[] transport_protocols=null;
   
   /** List of transport ports, ordered as the corresponding transport_protocols. */
   int[] transport_ports=null;

   /** Max number of (contemporary) open connections */
   int nmax_connections=0;

   /** Outbound proxy URL ([sip:]host_addr[:host_port][;transport=proto]).
     * Use 'NONE' for not using an outbound proxy (or let it undefined). */
   SipURL outbound_proxy=null;

   /** Whether logging all packets (including non-SIP keepalive tokens). */
   boolean log_all_packets=false;


   /** For TLS. Whether all client and server certificates should be considered trusted.
     * By default, trust_all=false. */
   public boolean trust_all=false;

   /** For TLS. Path of the folder where trusted certificates are placed.
     * All certificates (with file extension ".crt") found in this folder are considered trusted.
     * By default, the folder "./cert" is used. */
   public String trust_folder="cert";

   /** For TLS. Absolute file name of the certificate (containing the public key) of the local node.
     * The file name includes the full path starting from the current working folder.
     * By default, the file "./cert/ssl.crt" is used. */
   public String cert_file="cert/ssl.crt";

   /** For TLS. Absolute file name of the private key of the local node.
     * The file name includes the full path starting from the current working folder.
     * By default, the file "./cert/ssl.key" is used. */
   public String key_file="cert/ssl.key";


   // for backward compatibility:

   /** Outbound proxy addr (for backward compatibility). */
   private String outbound_addr=null;
   /** Outbound proxy port (for backward compatibility). */
   private int outbound_port=-1;


   // ************************ Other attributes *************************

   /** Event Log */
   protected Log log=null;

   /** Message Log */
   protected Log message_log=null;

   /** Network interface (IP address) used by SIP. */
   IpAddress host_ipaddr=null;

   /** Table of supported transport layers for SIP, as table:(String)protocol-->(Transport)transport. */
   Hashtable sip_transports=null;

   /** Default transport */
   String default_transport=null;

   /** Whether adding 'rport' parameter on outgoing requests. */
   boolean rport=true;
   
   /** Whether forcing 'rport' parameter on incoming requests ('force-rport' mode). */
   boolean force_rport=false;

   /** Table of sip listeners as Hashtable:(SipId)id-->(SipProviderListener)listener */
   Hashtable sip_listeners=new Hashtable();
   
   /** Vector of promiscuous listeners */
   Vector promiscuous_listeners=new Vector();

   /** Vector of exception listeners */
   Vector exception_listeners=new Vector();



   // *************************** Costructors ***************************

   /** Creates a void SipProvider. */ 
   /*protected SipProvider()
   {  
   }*/

   /** Creates a new SipProvider. */ 
   public SipProvider(String via_addr, int host_port)
   {  init(via_addr,host_port);
      initLog();
      initSipTrasport(transport_protocols,null,null);
   }


   /** Creates a new SipProvider. 
     * Costructs the SipProvider, initializing the SipProviderListeners, the transport protocols, and other attributes. */ 
   public SipProvider(String via_addr, int host_port, String[] transport_protocols, String ifaddr)
   {  init(via_addr,host_port);
      initLog();
      initSipTrasport(transport_protocols,null,host_ifaddr);
   }


   /** Creates a new SipProvider. 
     * Costructs the SipProvider, initializing the SipProviderListeners, the transport protocols, and other attributes. */ 
   public SipProvider(String via_addr, int host_port, String[] transport_protocols, int[] transport_ports, String ifaddr)
   {  init(via_addr,host_port);
      initLog();
      initSipTrasport(transport_protocols,transport_ports,host_ifaddr);
   }


   /** Creates a new SipProvider. 
     * Costructs the SipProvider, initializing the SipProviderListeners, the transport protocols, and other attributes. */ 
   public SipProvider(String via_addr, int host_port, Transport[] sip_transports)
   {  init(via_addr,host_port);
      initLog();
      // init transport
      this.sip_transports=new Hashtable();
      if (sip_transports!=null)
      {  for (int i=0; i<sip_transports.length; i++) setTransport(sip_transports[i]);
         if (sip_transports.length>0) default_transport=sip_transports[0].getProtocol();
      }
   }


   /** Creates a new SipProvider. 
     * The SipProvider attributres are read from file. */ 
   public SipProvider(String file)
   {  if (!SipStack.isInit()) SipStack.init(file);
      new Configure(this,file);
      init(via_addr,host_port);
      initLog();
      initSipTrasport(transport_protocols,transport_ports,host_ifaddr);
   }


   /** Inits the SipProvider, initializing the SipProviderListeners, the transport protocols, the outbound proxy, and other attributes. */ 
   private void init(String via_addr, int host_port)
   {  if (!SipStack.isInit()) SipStack.init();
      if (via_addr==null || via_addr.equalsIgnoreCase(AUTO_CONFIGURATION)) via_addr=IpAddress.getLocalHostAddress().toString();
      this.via_addr=via_addr;
      if (host_port<=0) host_port=SipStack.default_port;
      this.host_port=host_port;
      rport=SipStack.use_rport; 
      force_rport=SipStack.force_rport; 
      
      // just for backward compatibility..
      if (outbound_port<0) outbound_port=SipStack.default_port;
      if (outbound_addr!=null)
      {  if (outbound_addr.equalsIgnoreCase(Configure.NONE) || outbound_addr.equalsIgnoreCase("NO-OUTBOUND")) outbound_proxy=null;
         else outbound_proxy=new SipURL(outbound_addr,outbound_port);
      }
   }

   
   /** Inits logs. */ 
   private void initLog()
   {  if (SipStack.debug_level>0)
      {  String filename=SipStack.log_path+"//"+via_addr+"."+host_port;
         log=new RotatingLog(filename+"_events.log",SipStack.debug_level,SipStack.max_logsize*1024,SipStack.log_rotations,SipStack.rotation_scale,SipStack.rotation_time);
         message_log=new RotatingLog(filename+"_messages.log",SipStack.debug_level,SipStack.max_logsize*1024,SipStack.log_rotations,SipStack.rotation_scale,SipStack.rotation_time);
      }
      printLog("Date: "+DateFormat.formatHHMMSS(new Date()),Log.LEVEL_HIGH);
      printLog("SipStack: "+SipStack.release,Log.LEVEL_HIGH);
      printLog("new SipProvider(): "+toString(),Log.LEVEL_HIGH);
   }
 
  
   /** Inits and starts the transport services. */ 
   private void initSipTrasport(String[] transport_protocols, int[] transport_ports, String ifaddr)
   {
      host_ipaddr=null;
      if (ifaddr!=null && !ifaddr.equalsIgnoreCase(ALL_INTERFACES))
      {  try {  host_ipaddr=IpAddress.getByName(ifaddr);  } catch (IOException e) {  e.printStackTrace(); host_ipaddr=null;  }
      }
      if (transport_protocols==null) transport_protocols=SipStack.default_transport_protocols;
      this.transport_protocols=transport_protocols;
      if (transport_protocols.length>0) default_transport=transport_protocols[0];
      if (nmax_connections<=0) nmax_connections=SipStack.default_nmax_connections;

      sip_transports=new Hashtable();
      for (int i=0; i<transport_protocols.length; i++)
      {  try
         {  String proto=transport_protocols[i].toLowerCase();
            int port=(transport_ports!=null && transport_ports.length>i)? port=transport_ports[i] : 0;
            Transport transp=null;
            if (proto.equals(PROTO_UDP))
            {  if (port==0) port=host_port;
               transp=new UdpTransport(port,host_ipaddr);
            }
            else
            if (proto.equals(PROTO_TCP))
            {  if (port==0) port=host_port;
               transp=new TcpTransport(port,host_ipaddr,nmax_connections,log);
            }
            else
            if (proto.equals(PROTO_TLS))
            {  if (port==0) port=(host_port==SipStack.default_port)? SipStack.default_tls_port : host_port;
               transp=new TlsTransport(port,host_ipaddr,nmax_connections,key_file,cert_file,trust_folder,trust_all,log);
            }
            if (transp!=null) 
            {  setTransport(transp);
            }
         }
         catch (Exception e)
         {  printException(e,Log.LEVEL_HIGH);
         }
      }
      //printLog("transport is up",Log.LEVEL_MEDIUM);
   }


   /** Stops the transport services. */ 
   private void stopSipTrasport()
   {  if (sip_transports!=null) 
      {  for(Enumeration e=sip_transports.keys(); e.hasMoreElements(); )
         {  String proto=(String)e.nextElement();
            Transport transp=(Transport)sip_transports.get(proto);
            printLog(proto+" is going down",Log.LEVEL_LOWER);
            transp.halt();
         }
         sip_transports.clear();
         sip_transports=null;
      }
   }


   /** Sets a specific transport protocol. */ 
   public void setTransport(Transport transport)
   {  String proto=transport.getProtocol();
      removeTransport(proto);
      sip_transports.put(proto,transport);
      transport.setListener(this);
      if (default_transport==null) default_transport=proto;
      printLog(proto+" is up at port "+transport.getLocalPort(),Log.LEVEL_HIGH);
   }


   /** Removes a specific transport protocol. */ 
   public void removeTransport(String proto)
   {  if (sip_transports.containsKey(proto)) 
      {  Transport t=(Transport)sip_transports.get(proto);
         sip_transports.remove(proto);
         t.halt();
         if (proto.equals(default_transport)) default_transport=null;
         printLog(proto+" is down",Log.LEVEL_HIGH);
      }
   }


   /** Stops the SipProviders. */ 
   public synchronized void halt()
   {  printLog("halt: SipProvider is going down",Log.LEVEL_MEDIUM);
      stopSipTrasport();
      sip_listeners=new Hashtable();
      promiscuous_listeners=new Vector();
      exception_listeners=new Vector();
   }


   /** From Configurable. Parses a single line (loaded from the config file). */
   public void parseLine(String line)
   {  String attribute;
      Parser par;
      int index=line.indexOf("=");
      if (index>0) {  attribute=line.substring(0,index).trim(); par=new Parser(line,index+1);  }
      else {  attribute=line; par=new Parser("");  }
      char[] delim={' ',','};
      
      if (attribute.equals("via_addr")) {  via_addr=par.getString(); return;  }
      if (attribute.equals("host_port")) {  host_port=par.getInt(); return;  }
      if (attribute.equals("host_ifaddr")) {  host_ifaddr=par.getString(); return;  }
      if (attribute.equals("transport_protocols")) {  transport_protocols=par.getWordArray(delim); return;  }
      if (attribute.equals("transport_ports")) {  transport_ports=par.getIntArray(); return;  }
      if (attribute.equals("nmax_connections")) {  nmax_connections=par.getInt(); return;  }
      if (attribute.equals("outbound_proxy"))
      {  String url=par.getString();
         if (url==null || url.length()==0 || url.equalsIgnoreCase(Configure.NONE) || url.equalsIgnoreCase("NO-OUTBOUND")) outbound_proxy=null;
         else outbound_proxy=new SipURL(url);
         return;
      }
      if (attribute.equals("log_all_packets")) { log_all_packets=(par.getString().toLowerCase().startsWith("y")); return; }

      // certificates
      if (attribute.equals("trust_all")){ trust_all=(par.getString().toLowerCase().startsWith("y")); return; }
      if (attribute.equals("trust_folder")){ trust_folder=par.getRemainingString().trim(); return; }
      if (attribute.equals("cert_file")){ cert_file=par.getRemainingString().trim(); return; }
      if (attribute.equals("key_file")){ key_file=par.getRemainingString().trim(); return; }

      // old parameters
      if (attribute.equals("host_addr")) System.err.println("WARNING: parameter 'host_addr' is no more supported; use 'via_addr' instead.");
      if (attribute.equals("tls_port")) System.err.println("WARNING: parameter 'tls_port' is no more supported; use 'transport_ports' instead.");
      if (attribute.equals("all_interfaces")) System.err.println("WARNING: parameter 'all_interfaces' is no more supported; use 'host_iaddr' for setting a specific interface or let it undefined.");
      if (attribute.equals("use_outbound")) System.err.println("WARNING: parameter 'use_outbound' is no more supported; use 'outbound_proxy' for setting an outbound proxy or let it undefined.");
      if (attribute.equals("outbound_addr"))
      {  System.err.println("WARNING: parameter 'outbound_addr' has been deprecated; use 'outbound_proxy=[sip:]<host_addr>[:<host_port>][;transport=proto]' instead.");
         outbound_addr=par.getString();
         return;
      }
      if (attribute.equals("outbound_port"))
      {  System.err.println("WARNING: parameter 'outbound_port' has been deprecated; use 'outbound_proxy=<host_addr>[:<host_port>]' instead.");
         outbound_port=par.getInt();
         return;
      }
   }  


   /** Converts the entire object into lines (to be saved into the config file) */
   protected String toLines()
   {  // currently not implemented..
      return toString();
   }


   /** Gets a String with the list of transport protocols. */ 
   private String transportProtocolsToString()
   {  if (sip_transports==null) return ""; 
      // else
      StringBuffer sb=new StringBuffer();
      for (Enumeration e=sip_transports.keys(); e.hasMoreElements(); )
      {  sb.append("/").append((String)e.nextElement());
      }
      return sb.toString();
   }


   // ************************** Public methods *************************

   /** Whether it supports <i>proto</i> as transport protocol. */
   public boolean hasTransport(String proto)
   {  if (sip_transports!=null) return sip_transports.containsKey(proto.toLowerCase());
      else return false; 
   }    

   /** Gets via address. */ 
   public String getViaAddress()
   {  return via_addr;
   }    
   
   /** Sets via address. */ 
   /*public void setViaAddress(String addr)
   {  via_addr=addr;
   }*/   

   /** Gets host port. */ 
   public int getPort()
   {  return host_port;
   }       

   /** Gets tls port. */ 
   public int getTlsPort()
   {  return (sip_transports.containsKey(PROTO_TLS))? ((Transport)sip_transports.get(PROTO_TLS)).getLocalPort() : 0;
   }       

   /** Gets a valid contact address with user name and transport information. */
   public SipURL getContactAddress(String user)
   {  SipURL url=(getPort()!=SipStack.default_port)? new SipURL(user,getViaAddress(),getPort()) : new SipURL(user,getViaAddress());
      if (!hasTransport(PROTO_UDP)) url.addTransport(getDefaultTransport());
      return url;
   }

   /** Gets a valid secure contact address with user name and transport information. */
   public SipURL getSecureContactAddress(String user)
   {  if (hasTransport(SipProvider.PROTO_TLS))
      {  SipURL url=(getTlsPort()!=SipStack.default_tls_port)? new SipURL(user,getViaAddress(),getTlsPort()) : new SipURL(user,getViaAddress()); 
         url.setSecure(true);
         return url;
      }
      else return null;
   }

   /** Whether binding the sip provider to all interfaces or only on the specified host address. */
   public boolean isAllInterfaces()
   {  return host_ipaddr==null;
   }       

   /** Gets host interface IpAddress. */ 
   public IpAddress getInterfaceAddress()
   {  return host_ipaddr;
   }    
   
   /** Gets array of transport protocols. */ 
   public String[] getTransportProtocols()
   {  String[] protocols=new String[sip_transports.size()];
      Enumeration e=sip_transports.keys();
      for (int i=0; i<protocols.length; i++) protocols[i]=(String)e.nextElement(); 
      return protocols;
   }    
   
   /** Whether the given transport protocol is supported. */ 
   public boolean isSupportedTransport(String proto)
   {  return sip_transports.containsKey(proto.toLowerCase());
   }    
   
   /** Whether the given transport protocol is supported and reliable. */ 
   public boolean isReliableTransport(String proto)
   {  return isReliableTransport((Transport)sip_transports.get(proto.toLowerCase()));
   }    
   
   /** Whether the given transport is reliable. */ 
   boolean isReliableTransport(Transport transp)
   {  if (transp!=null) try {  return Class.forName("org.zoolu.sip.provider.ConnectedTransport").isInstance(transp);  } catch (ClassNotFoundException e) {}
      // else
      return false;
   }    
   
   /** Gets the default transport protocol. */ 
   public String getDefaultTransport()
   {  return default_transport;
   } 
   
   /** Gets the default transport protocol. */ 
   public synchronized void setDefaultTransport(String proto)
   {  default_transport=proto;
   }    

   /** Sets rport support. */ 
   public synchronized void setRport(boolean flag)
   {  rport=flag;
   }   

   /** Whether using rport. */ 
   public boolean isRportSet()
   {  return rport;
   }   

   /** Sets 'force-rport' mode. */ 
   public synchronized void setForceRport(boolean flag)
   {  force_rport=flag;
   }   

   /** Whether using 'force-rport' mode. */ 
   public boolean isForceRportSet()
   {  return force_rport;
   }   

   /** Whether has outbound proxy. */ 
   public boolean hasOutboundProxy()
   {  return outbound_proxy!=null;
   }    

   /** Gets the outbound proxy. */ 
   public SipURL getOutboundProxy()
   {  return outbound_proxy;
   }    

   /** Sets the outbound proxy. Use 'null' for not using any outbound proxy. */ 
   public synchronized void setOutboundProxy(SipURL url)
   {  outbound_proxy=url;
   }

   /** Removes the outbound proxy. */ 
   /*public void removeOutboundProxy()
   {  setOutboundProxy(null);
   }*/

   /** Gets the max number of (contemporary) open connections. */ 
   public int getNMaxConnections()
   {  return nmax_connections;
   }    

   /** Sets the max number of (contemporary) open connections. */ 
   public synchronized void setNMaxConnections(int n)
   {  nmax_connections=n;
   }    
      
            
   /** Gets event log. */ 
   public Log getLog()
   {  return log;
   }    
   

   /** Returns the table of active listeners as Hastable:(SipId)IDs-->(SipListener)listener. */ 
   public Hashtable getListeners()
   {  return sip_listeners;
   }   


   /** Sets a SipProvider listener for a target type of method, transaction, or dialog messages.
     * @param id is the identifier that specifies the messages that the listener
     * as to be associated to. It may identify a method, a transaction, or a dialog, or all messages.
     * Use SipProvider.ANY to capture all messages.
     * @param listener is the SipProviderListener that the specified type of messages has to be passed to. */
   public synchronized void addSelectiveListener(SipId id, SipProviderListener listener)
   {  printLog("setting SipProviderListener: "+id,Log.LEVEL_MEDIUM);
      SipId key=id;
      if (sip_listeners.containsKey(key))
      {  printWarning("setting a SipProvider listener with an identifier already selected: the previous listener is removed.",Log.LEVEL_HIGH);
         sip_listeners.remove(key);
      }
      sip_listeners.put(key,listener);   
      printLog("active sip listeners: "+sip_listeners.size(),Log.LEVEL_LOW);
   }


   /** Removes a SipProviderListener.
     * @param id is the identifier that specifies the messages that the listener was associated to. */
   public synchronized void removeSelectiveListener(SipId id)
   {  printLog("removing SipProviderListener: "+id,Log.LEVEL_MEDIUM);
      SipId key=id;
      if (!sip_listeners.containsKey(key))
      {  printWarning("removeListener("+id+"): no such listener found.",Log.LEVEL_HIGH);
      }
      else
      {  sip_listeners.remove(key);
      }
      printLog("active sip listeners: "+sip_listeners.size(),Log.LEVEL_LOW);
   }

  
   /** Adds a SipProvider listener for caputering any message in promiscuous mode.
     * <p/>
     * When a SipProviderListener captures messages in promiscuous mode
     * messages are passed to this listener before passing them to other specific listener.
     * <br/> More that one SipProviderListener can be active in promiscuous mode at the same time;
     * in that case the same message is passed to all promiscuous SipProviderListeners.
     * @param listener is the SipProviderListener. */
   public synchronized void addPromiscuousListener(SipProviderListener listener)
   {  printLog("adding SipProviderListener in promiscuous mode",Log.LEVEL_MEDIUM);
      if (promiscuous_listeners.contains(listener))
      {  printWarning("trying to add an already present SipProviderListener in promiscuous mode.",Log.LEVEL_HIGH);
      }
      else
      {  promiscuous_listeners.addElement(listener);
      }
   }


   /** Removes a SipProviderListener in promiscuous mode. 
     * @param listener is the SipProviderListener to be removed. */
   public synchronized void removePromiscuousListener(SipProviderListener listener)
   {  printLog("removing SipProviderListener in promiscuous mode",Log.LEVEL_MEDIUM);
      if (!promiscuous_listeners.contains(listener))
      {  printWarning("trying to remove a missed SipProviderListener in promiscuous mode.",Log.LEVEL_HIGH);
      }
      else
      {  promiscuous_listeners.removeElement(listener);
      }
   }


   /** Adds a SipProviderExceptionListener.
     * The SipProviderExceptionListener is a listener for all exceptions thrown by the SipProviders.
     * @param listener is the SipProviderExceptionListener. */
   public synchronized void addExceptionListener(SipProviderExceptionListener listener)
   {  printLog("adding a SipProviderExceptionListener",Log.LEVEL_MEDIUM);
      if (exception_listeners.contains(listener))
      {  printWarning("trying to add an already present SipProviderExceptionListener.",Log.LEVEL_HIGH);
      }
      else
      {  exception_listeners.addElement(listener);
      }
   }


   /** Removes a SipProviderExceptionListener. 
     * @param listener is the SipProviderExceptionListener to be removed. */
   public synchronized void removeExceptionListener(SipProviderExceptionListener listener)
   {  printLog("removing a SipProviderExceptionListener",Log.LEVEL_MEDIUM);
      if (!exception_listeners.contains(listener))
      {  printWarning("trying to remove a missed SipProviderExceptionListener.",Log.LEVEL_HIGH);
      }
      else
      {  exception_listeners.removeElement(listener);
      }
   }


   /** Sends the message <i>msg</i>.
     * <p/>
     * The destination for the request is computed as follows:
     * <br/> - if <i>outbound_addr</i> is set, <i>outbound_addr</i> and 
     *        <i>outbound_port</i> are used, otherwise
     * <br/> - if message has Route header with lr option parameter (i.e. RFC3261 compliant),
     *        the first Route address is used, otherwise
     * <br/> - the request's Request-URI is considered.
     * <p/>
     * The destination for the response is computed based on the sent-by parameter in
     *     the Via header field (RFC3261 compliant)
     * <p/>
     * As transport it is used the protocol specified in the 'via' header field 
     * <p/>
     * In case of connection-oriented transport:
     * <br/> - if an already established connection is found matching the destination
     *        end point (socket), such connection is used, otherwise
     * <br/> - a new connection is established
     *
     * @return Returns a TransportConnId in case of connection-oriented delivery
     * (e.g. TCP) or null in case of connection-less delivery (e.g. UDP)
     */
   public TransportConnId sendMessage(Message msg)
   {  printLog("Sending message:\r\n"+msg.toString(),Log.LEVEL_LOWER);

      // select the transport protocol
      ViaHeader via=msg.getViaHeader();
      String via_proto=via.getProtocol().toLowerCase();
      String proto=via_proto;
      
      // select the destination address and port
      String dest_addr=null;
      int dest_port=0;
      int ttl=0;
      
      if (msg.isRequest())
      {  // REQUESTS
         SipURL url=outbound_proxy;
         // else
         if (url==null)
         {  if (msg.hasRouteHeader())
            {  SipURL route_url=msg.getRouteHeader().getNameAddress().getAddress();
               if (route_url.hasLr()) url=msg.getRouteHeader().getNameAddress().getAddress();
            }
         }
         // else
         if (url==null)
         {  url=msg.getRequestLine().getAddress();
         }
         dest_addr=url.getHost();
         dest_port=url.getPort();
         if (url.isSecure()) proto="tls";
         else if (url.hasTransport()) proto=url.getTransport();
         // if maddr is set, update the via header by adding maddr and ttl params 
         if (url.hasMaddr())
         {  dest_addr=url.getMaddr();
            if (url.hasTtl()) ttl=url.getTtl();
            via.setMaddr(dest_addr);
            if (ttl>0) via.setTtl(ttl);
            msg.removeViaHeader();
            msg.addViaHeader(via);
         }
         // if the resulting proto differs from via proto, update via 
         if (!via_proto.equalsIgnoreCase(proto))
         {  via.setProtocol(proto);
            msg.removeViaHeader();
            msg.addViaHeader(via);
         }
         printLog("using transport "+proto,Log.LEVEL_MEDIUM);
      }
      else
      {  // RESPONSES
         SipURL url=via.getSipURL();
         if (via.hasReceived()) dest_addr=via.getReceived(); else dest_addr=url.getHost();
         if (via.hasRport()) dest_port=via.getRport();
         if (dest_port<=0) dest_port=url.getPort();
      }

      if (dest_port<=0)
      {  if (proto.equalsIgnoreCase("tls")) dest_port=SipStack.default_tls_port;
         else dest_port=SipStack.default_port;
      }
      
      return sendMessage(msg,proto,dest_addr,dest_port,ttl); 
   }


   /** Sends a Message, specifing the transport portocol, nexthop address and port.
     * <p/>
     * This is a low level method and forces the message to be routed to
     * a specific nexthop address, port and transport,
     * regardless whatever the Via, Route, or request-uri, address to. 
     * <p/>
     * In case of connection-oriented transport, the connection is selected as follows:
     * <br/> - if an existing connection is found matching the destination
     *        end point (destination socket), such connection is used, otherwise
     * <br/> - a new connection is established.
     *
     * @return It returns a Connection in case of connection-oriented delivery
     * (e.g. TCP) or null in case of connection-less delivery (e.g. UDP)
     */
   public TransportConnId sendMessage(Message msg, String proto, String dest_addr, int dest_port, int ttl)
   {  if (log_all_packets || msg.getLength()>MIN_MESSAGE_LENGTH) printLog("Resolving host address '"+dest_addr+"'",Log.LEVEL_MEDIUM);
      try
      {  IpAddress dest_ipaddr=IpAddress.getByName(dest_addr);  
         return sendMessage(msg,proto,dest_ipaddr,dest_port,ttl); 
      }
      catch (Exception e)
      {  printException(e,Log.LEVEL_HIGH);
         return null;
      }     
   }

   /** Sends a Message, specifing the transport portocol, nexthop address and port. */
   private TransportConnId sendMessage(Message msg, String proto, IpAddress dest_ipaddr, int dest_port, int ttl)
   {  if (log_all_packets || msg.getLength()>MIN_MESSAGE_LENGTH) printLog("Sending message to "+(new TransportConnId(proto,dest_ipaddr,dest_port)).toString(),Log.LEVEL_MEDIUM);

      TransportConn conn=null;
      try
      {  Transport transp=(Transport)sip_transports.get(proto.toLowerCase());
         if (transp!=null)
         {  conn=transp.sendMessage(msg,dest_ipaddr,dest_port,ttl);
         }
         else
         {  printWarning("Unsupported protocol ("+proto+"): Message discarded",Log.LEVEL_HIGH);
            return null;
         }
      }
      catch (IOException e)
      {  printException(e,Log.LEVEL_HIGH);
         return null;
      }
      // logs
      String dest_addr=dest_ipaddr.toString();
      printMessageLog(proto,dest_addr,dest_port,msg.getLength(),msg,"sent");
      
      if (conn!=null) return new TransportConnId(conn);
      else return null;
   }


   /** Sends the message <i>msg</i> using the specified transport connection. */
   public TransportConnId sendMessage(Message msg, TransportConnId conn_id)
   {  if (log_all_packets || msg.getLength()>MIN_MESSAGE_LENGTH) printLog("Sending message through conn "+conn_id,Log.LEVEL_HIGH);
      printLog("message:\r\n"+msg.toString(),Log.LEVEL_LOWER);
      TransportConn conn=null;
      for (Enumeration e=sip_transports.elements(); e.hasMoreElements() && conn==null; )
      {  Transport transp=(Transport)e.nextElement();
         if (isReliableTransport(transp)) conn=((ConnectedTransport)transp).sendMessage(msg,conn_id);
      }
      if (conn!=null)
      {  // logs
         String proto=conn.getProtocol();
         String dest_addr=conn.getRemoteAddress().toString();
         int dest_port=conn.getRemotePort();
         printMessageLog(proto,dest_addr,dest_port,msg.getLength(),msg,"sent");
               
         return new TransportConnId(conn);
      }
      else
      {  return sendMessage(msg);
      }
   }


   //************************* Callback methods *************************
   
   /** From TransportListener. When a new SIP message is received. */
   public synchronized void onReceivedMessage(Transport transport, Message msg)
   {  try
      {  // logs
         printMessageLog(msg.getTransportProtocol(),msg.getRemoteAddress(),msg.getRemotePort(),msg.getLength(),msg,"received");

         // discard too short messages
         if (msg.getLength()<=2)
         {  if (log_all_packets) printLog("message too short: discarded\r\n",Log.LEVEL_LOW);
            return;
         }
         // discard non-SIP messages
         String first_line=msg.getFirstLine();
         if (first_line==null || first_line.toUpperCase().indexOf("SIP/2.0")<0)
         {  if (log_all_packets) printLog("NOT a SIP message: discarded\r\n",Log.LEVEL_LOW);
            return;
         }
         printLog("received new SIP message",Log.LEVEL_HIGH);
         printLog("message:\r\n"+msg.toString(),Log.LEVEL_LOWER);
         
         // if a request, handle "received" and "rport" parameters
         if (msg.isRequest())
         {  ViaHeader vh=msg.getViaHeader();
            boolean via_changed=false;
            String src_addr=msg.getRemoteAddress();
            int src_port=msg.getRemotePort();
            String via_addr=vh.getHost();
            int via_port=vh.getPort();
            if (via_port<=0) via_port=SipStack.default_port;
             
            if (!via_addr.equals(src_addr))
            {  vh.setReceived(src_addr);
               via_changed=true;
            }
            
            if (vh.hasRport())
            {  vh.setRport(src_port);
               via_changed=true;
            }
            else
            {  if (force_rport && via_port!=src_port)
               {  vh.setRport(src_port);
                  via_changed=true;
               }
            }
            
            if (via_changed)
            {  msg.removeViaHeader();
               msg.addViaHeader(vh);
            }
         }
         
         // is there any listeners?
         if (sip_listeners.size()==0)
         {  printLog("no listener found: meesage discarded.",Log.LEVEL_HIGH);
            return;
         }

         // try to look for listeners in promiscuous mode
         for (int i=0; i<promiscuous_listeners.size(); i++)
         {  SipProviderListener listener=(SipProviderListener)promiscuous_listeners.elementAt(i);
            printLog("message passed to promiscuous listener",Log.LEVEL_MEDIUM);
            listener.onReceivedMessage(this,msg);
         }
         
         // check if the message is still valid
         if (!msg.isRequest() && !msg.isResponse())
         {  printLog("no valid SIP message: message discarded.",Log.LEVEL_HIGH);
            printLog("message:\r\n"+msg.toString(),Log.LEVEL_LOWER);
            return;
         }

         // look for a specifid listener:

         // try to look for a transaction
         SipId key;
         // pass requests to transaction servers and response to transaction clients
         if (msg.isRequest()) key=msg.getTransactionServerId(); else key=msg.getTransactionClientId();
         printLog("transaction-id: "+key,Log.LEVEL_MEDIUM);
         if (sip_listeners.containsKey(key))
         {  printLog("message passed to transaction: "+key,Log.LEVEL_MEDIUM);
            ((SipProviderListener)sip_listeners.get(key)).onReceivedMessage(this,msg);
            return;
         }
         // try to look for a dialog
         key=msg.getDialogId();
         printLog("dialog-id: "+key,Log.LEVEL_MEDIUM);
         if (sip_listeners.containsKey(key))
         {  printLog("message passed to dialog: "+key,Log.LEVEL_MEDIUM);
            ((SipProviderListener)sip_listeners.get(key)).onReceivedMessage(this,msg);
            return;
         }
         // try to look for a UAS
         key=msg.getMethodId();
         if (sip_listeners.containsKey(key))
         {  printLog("message passed to uas: "+key,Log.LEVEL_MEDIUM);
            ((SipProviderListener)sip_listeners.get(key)).onReceivedMessage(this,msg);
            return;
         }        
         // try to look for a default UA
         if (sip_listeners.containsKey(ANY))
         {  printLog("message passed to uas: "+ANY,Log.LEVEL_MEDIUM);
            ((SipProviderListener)sip_listeners.get(ANY)).onReceivedMessage(this,msg);
            return;
         }
                   
         // if we are here, no listener_ID matched..
         printLog("no listener found matching that message: message discarded.",Log.LEVEL_HIGH);
         //printLog("Pending SipProviderListeners= "+getListeners().size(),3);
         printLog("active listeners: "+sip_listeners.size(),Log.LEVEL_MEDIUM);
      }
      catch (Exception exception)
      {  printWarning("Error handling a new incoming message",Log.LEVEL_HIGH);
         printException(exception,Log.LEVEL_MEDIUM);
         for (int i=0; i<exception_listeners.size(); i++)
         {  try
            {  ((SipProviderExceptionListener)exception_listeners.elementAt(i)).onMessageException(msg,exception);
            }
            catch (Exception e)
            {  printWarning("Error handling the Exception",Log.LEVEL_HIGH);
               printException(e,Log.LEVEL_MEDIUM);
            }
         }
      }
   }   


   /** From TransportListener. When Transport terminates. */
   public void onTransportTerminated(Transport transport, Exception error)
   {  printLog("transport "+transport+" terminated",Log.LEVEL_MEDIUM);
      // TRY TO RESTART UDP WHEN ERRORS OCCUR
      if (error!=null && transport.getProtocol().equals(PROTO_UDP))
      {  printLog("transport UDP terminated with error: trying to restart it (after 1000ms)..",Log.LEVEL_HIGH);
         try {  Thread.sleep(1000);  } catch (Exception e) {}
         try
         {  Transport udp=new UdpTransport(host_port,host_ipaddr);
            setTransport(udp);
         }
         catch (Exception e)
         {  printException(e,Log.LEVEL_HIGH);
         }
      }
   }   


   //************************** Other methods ***************************
   
   /** Picks a fresh branch value.
     * The branch ID MUST be unique across space and time for
     * all requests sent by the UA.
     * The branch ID always begin with the characters "z9hG4bK". These
     * 7 characters are used by RFC 3261 as a magic cookie. */
   public static String pickBranch()
   {  //String str=Long.toString(Math.abs(Random.nextLong()),16);
      //if (str.length()<5) str+="00000";
      //return "z9hG4bK"+str.substring(0,5);
      return "z9hG4bK"+Random.nextHexString(8);
   }  

   /** Picks an unique branch value based on a SIP message.
     * This value could also be used as transaction ID */
   public String pickBranch(Message msg)
   {  StringBuffer sb=new StringBuffer();
      sb.append(msg.getRequestLine().getAddress().toString());
      sb.append(getViaAddress()+getPort());
      ViaHeader top_via=msg.getViaHeader();
      if (top_via.hasBranch())
         sb.append(top_via.getBranch());
      else
      {  sb.append(top_via.getHost()+top_via.getPort());
         sb.append(msg.getCSeqHeader().getSequenceNumber());
         sb.append(msg.getCallIdHeader().getCallId());
         sb.append(msg.getFromHeader().getTag());
         sb.append(msg.getToHeader().getTag());
      }
      //return "z9hG4bK"+(new MD5(unique_str)).asHex().substring(0,9);
      return "z9hG4bK"+(new SimpleDigest(5,sb.toString())).asHex();
   }  


   /** Picks a new tag.
     * A tag  MUST be globally unique and cryptographically random
     * with at least 32 bits of randomness.  A property of this selection
     * requirement is that a UA will place a different tag into the From
     * header of an INVITE than it would place into the To header of the
     * response to the same INVITE.  This is needed in order for a UA to
     * invite itself to a session. */
   public static String pickTag()
   {  //String str=Long.toString(Math.abs(Random.nextLong()),16);
      //if (str.length()<8) str+="00000000";
      //return str.substring(0,8);
      return Random.nextNumString(12);
   }   

   /** Picks a new tag. The tag is generated uniquely based on message <i>req</i>.
     * This tag can be generated for responses in a stateless
     * manner - in a manner that will generate the same tag for the
     * same request consistently.
     */
   public static String pickTag(Message req)
   {  //return String.valueOf(tag_generator++);
      //return (new MD5(request.toString())).asHex().substring(0,8);
      return (new SimpleDigest(8,req.toString())).asHex();
   }


   /** Picks a new call-id.
     * The call-id is a globally unique identifier over space and time.
     * It is implemented in the form "localid@host".
     * Call-id must be considered case-sensitive and is compared byte-by-byte. */
   public String pickCallId()
   {  return pickCallId(getViaAddress());
   }   


   /** Picks a new call-id.
     * The call-id is a globally unique
     * identifier over space and time. It is implemented in the
     * form "localid@host". Call-id must be considered case-sensitive and is
     * compared byte-by-byte. */
   public static String pickCallId(String hostaddr)
   {  //String str=Long.toString(Math.abs(Random.nextLong()),16);
      //if (str.length()<12) str+="000000000000";
      //return str.substring(0,12)+"@"+hostaddr();
      return Random.nextNumString(12)+"@"+hostaddr;
   }   


   /** picks an initial CSeq */
   public static int pickInitialCSeq()
   {  return 1;
   }   


   /** (<b>Deprecated</b>) Constructs a NameAddress based on an input string.
     * The input string can be a:
     * <br/> - <i>user</i> name,
     * <br/> - <i>user@address</i> url,
     * <br/> - <i>"Name" &lt;sip:user@address&gt;</i> address,
     * <p/>
     * In the former case,
     * a SIP URL is costructed using the outbound proxy as host address if present,
     * otherwise the local via address is used. */
   /*public NameAddress completeNameAddress(String str)
   {  if (str.indexOf("<sip:")>=0) return new NameAddress(str);
      else
      {  SipURL url=completeSipURL(str);
         return new NameAddress(url);
      }
   }*/
   /** Constructs a SipURL based on an input string. */
   /*private SipURL completeSipURL(String str)
   {  // in case it is passed only the 'user' field, add '@'<outbound_proxy>[':'<outbound_port>]
      if (!str.startsWith("sip:") && !str.startsWith("sips:") && str.indexOf("@")<0 && str.indexOf(".")<0 && str.indexOf(":")<0)
      {  // probably it is just the user name..
         if (outbound_proxy!=null)
         {  String host=outbound_proxy.getHost();
            int port=outbound_proxy.getPort();
            SipURL url=(port>0 && port!=SipStack.default_port)? new SipURL(str,host,port) : new SipURL(str,host);
            if (outbound_proxy.isSecure()) url.setSecure(true);
            return url;
         }
         else
         {  SipURL url=(host_port>0 && host_port!=SipStack.default_port)? new SipURL(str,via_addr,host_port) : new SipURL(str,via_addr);
            if (transport_protocols[0].equals(PROTO_TLS)) url.setSecure(true);
            return url;
         }
      }
      else return new SipURL(str);
   }*/


   /** Gets a String value for this object. */ 
   public String toString()
   {  if (host_ipaddr==null) return host_port+"/"+transportProtocolsToString();
      else return host_ipaddr.toString()+":"+host_port+"/"+transportProtocolsToString();
   }   


   //******************************* Logs *******************************

   /** Default log level offset */
   static final int LOG_OFFSET=1;
 
   
   /** Prints a message to the event log. */
   private final void printLog(String message, int level)
   {  String id=(host_ipaddr==null)? Integer.toString(host_port) : host_ipaddr.toString()+":"+host_port;
      String tag="SipProvider-"+id+": ";
      if (log!=null) log.println(tag+message,LOG_OFFSET+level);
   }


   /** Prints an exception to the event log. */
   private final void printException(Exception e, int level)
   {  printLog("Exception: "+ExceptionPrinter.getStackTraceOf(e),level);
   }


   /** Prints a warning to the event log. */
   private final void printWarning(String message, int level)
   {  printLog("WARNING: "+message,level);
   }

  
   /** Adds the SIP message to the message log. */
   private final void printMessageLog(String proto, String addr, int port, int len, Message msg, String str)
   {  if (log_all_packets || len>=MIN_MESSAGE_LENGTH)
      {  if (message_log!=null)
         {  message_log.println(getPacketTimestamp(proto,addr,port,len)+" "+str+"\r\n"+msg.toString()+"-----End-of-message-----\r\n");
         }
         if (log!=null)
         {  String first_line=msg.getFirstLine();
            if (first_line!=null) first_line=first_line.trim(); else first_line="NOT a SIP message";
            log.println("",LOG_OFFSET+Log.LEVEL_HIGH);
            log.println(getPacketTimestamp(proto,addr,port,len)+first_line+", "+str,LOG_OFFSET+Log.LEVEL_HIGH);
         }
      }
   }


   /** Gets a packet timestamp. */
   private static String getPacketTimestamp(String proto, String remote_addr, int remote_port, int len)
   {  String str=remote_addr+":"+remote_port+"/"+proto+" ("+len+" bytes)";
      return DateFormat.formatHHMMSS(new Date())+", "+str;
   }

}
