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

package local.server;


import org.zoolu.sip.address.SipURL;
import org.zoolu.sip.provider.*;
import org.zoolu.sip.transaction.*;
import org.zoolu.sip.header.MultipleHeader;
import org.zoolu.sip.header.ViaHeader;
import org.zoolu.sip.header.Header;
import org.zoolu.sip.header.RouteHeader;
import org.zoolu.sip.header.RequestLine;
import org.zoolu.sip.header.MaxForwardsHeader;
import org.zoolu.sip.header.MultipleHeader;
import org.zoolu.sip.message.Message;
import org.zoolu.sip.message.SipResponses;
import org.zoolu.sip.message.MessageFactory;
import org.zoolu.tools.Log;
import org.zoolu.tools.ExceptionPrinter;
import org.zoolu.tools.SimpleDigest;

import java.util.Vector;
import java.util.Enumeration;


/** Class ServerEngine implement a stateless abstract SIP Server.
  * The ServerEngine can act as SIP Proxy Server, SIP Registrar Server or both.
  * <p> For each incoming message, the ServerEngine fires one of the following
  * abstract methods:
  * <ul>
  * <li>public abstract processRequestToRemoteUA(Message),</li>
  * <li>public abstract processRequestToLocalServer(Message),</li>
  * <li>public abstract processRequestToLocalServer(Message),</li>
  * <li>public abstract processResponse(Message).</li>
  * </ul>
  * depending of the type of received message.
  */
public abstract class ServerEngine implements SipProviderListener
{
   /** Name of the Loop-Tag header field.
     * It is used as temporary field for carry loop detection information
     * added to the via branch parameter of the forwarded requests. */
   protected static final String Loop_Tag="Loop-Tag";

   /** Log */
   protected Log log=null;

   /** ServerProfile of the server. */
   protected ServerProfile server_profile=null;

   /** SipProvider used by the server. */
   protected SipProvider sip_provider=null;
   
   /** LocationService. */
   protected LocationService location_service;

   /** AuthenticationService (i.e. the repository with authentication credentials). */
   protected AuthenticationService authentication_service;

   /** AuthenticationServer. */
   protected AuthenticationServer as;
   
   /** List of already supported location services */
   protected static final String[] LOCATION_SERVICES={ "local", "ldap" };
   /** List of location service Classes (ordered as in <i>LOCATION_SERVICES</i>) */
   protected static final String[] LOCATION_SERVICE_CLASSES={ "local.server.LocationServiceImpl", "local.ldap.LdapLocationServiceImpl" };

   /** List of already supported authentication services */
   protected static final String[] AUTHENTICATION_SERVICES={ "local", "ldap" };
   /** List of authentication service Classes (ordered as in <i>AUTHENTICATION_SERVICES</i>) */
   protected static final String[] AUTHENTICATION_SERVICE_CLASSES={ "local.server.AuthenticationServiceImpl", "local.ldap.LdapAuthenticationServiceImpl" };

   /** List of already supported authentication schemes */
   protected static final String[] AUTHENTICATION_SCHEMES={ "Digest" };
   /** List of authentication server Classes (ordered as in <i>AUTHENTICATION_SCHEMES</i>) */
   protected static final String[] AUTHENTICATION_SERVER_CLASSES={ "local.server.AuthenticationServerImpl" };


   // *************************** abstract methods ***************************

   /** When a new request message is received for a remote UA */
   public abstract void processRequestToRemoteUA(Message req);

   /** When a new request message is received for a locally registered user */
   public abstract void processRequestToLocalUser(Message req);

   /** When a new request request is received for the local server */
   public abstract void processRequestToLocalServer(Message req);
   
   /** When a new response message is received */
   public abstract void processResponse(Message resp);
   

   // ****************************** costructors *****************************

   /** Costructs a void ServerEngine */
   protected ServerEngine() {}


   /** Costructs a new ServerEngine on SipProvider <i>provider</i>,
     * and adds it as SipProviderListener. */
   public ServerEngine(SipProvider provider, ServerProfile profile)
   {  server_profile=profile;
      sip_provider=provider;
      log=sip_provider.getLog();
      sip_provider.addSelectiveListener(SipProvider.ANY,this);
          
      // LOCAL DOMAINS
      printLog("Domains="+getLocalDomains(),Log.LEVEL_HIGH);

      // LOCATION SERVICE
      String location_service_class=profile.location_service;
      for (int i=0; i<LOCATION_SERVICES.length; i++)
         if (LOCATION_SERVICES[i].equalsIgnoreCase(profile.location_service)) {  location_service_class=LOCATION_SERVICE_CLASSES[i];  break;  }
      try
      {  Class myclass=Class.forName(location_service_class);
         Class[] parameter_types={ Class.forName("java.lang.String") };
         Object[] parameters={ profile.location_db };
         try 
         {  java.lang.reflect.Constructor constructor=myclass.getConstructor(parameter_types);
            location_service=(LocationService)constructor.newInstance(parameters);
         }
         catch (NoSuchMethodException e)
         {  printException(e,Log.LEVEL_MEDIUM);
            location_service=(LocationService)myclass.newInstance();
         }
      }
      catch (Exception e)
      {  printException(e,Log.LEVEL_HIGH);
         printLog("Error trying to use location service '"+location_service_class+"': use default class.",Log.LEVEL_HIGH);
      }
      // use default location service
      if (location_service==null) location_service=new LocationServiceImpl(profile.location_db);   
      // do clean all?
      if (profile.clean_location_db) 
      {  for (Enumeration u=location_service.getUsers(); u.hasMoreElements(); )
         {  String user=(String)u.nextElement();
            for (Enumeration c=location_service.getUserContactURLs(user); c.hasMoreElements(); )
            {  String contact=(String)c.nextElement();
               if (!location_service.isUserContactStatic(user,contact)) location_service.removeUserContact(user,contact);
            }
         }
         location_service.sync();
         printLog("LocationService \""+profile.location_db+"\": cleaned\r\n",Log.LEVEL_MEDIUM);
      }
      else
      { // remove all expired contacts
         boolean changed=false;    
         for (Enumeration u=location_service.getUsers(); u.hasMoreElements(); )
         {  String user=(String)u.nextElement();
            for (Enumeration c=location_service.getUserContactURLs(user); c.hasMoreElements(); )
            {  String contact=(String)c.nextElement();
               if ((changed=location_service.isUserContactExpired(user,contact))==true) location_service.removeUserContact(user,contact);
            }
            // Note: uncomment the next line, if you want that 'unbound' users (i.e. without registered contacts) are automatically removed
            //if (!location_service.getUserContacts(user).hasMoreElements()) location_service.removeUser(user);   
         }
         if (changed) location_service.sync();
      }  
      printLog("LocationService ("+profile.authentication_service+"): size="+location_service.size()+"\r\n"+location_service.toString(),Log.LEVEL_MEDIUM);
      printLog("LocationService ("+profile.authentication_service+"): size="+location_service.size()+"\r\n"+location_service.toString(),Log.LEVEL_MEDIUM);

      // AUTHENTICATION SERVICE
      if (server_profile.do_authentication || server_profile.do_proxy_authentication)
      {  // first, init the proper authentication service
         String realm=(server_profile.authentication_realm!=null)? server_profile.authentication_realm : sip_provider.getViaAddress();
         String authentication_service_class=profile.authentication_service;
         for (int i=0; i<AUTHENTICATION_SERVICES.length; i++)
            if (AUTHENTICATION_SERVICES[i].equalsIgnoreCase(profile.authentication_service)) {  authentication_service_class=AUTHENTICATION_SERVICE_CLASSES[i];  break;  }
         try
         {  Class myclass=Class.forName(authentication_service_class);
            Class[] parameter_types={ Class.forName("java.lang.String") };
            Object[] parameters={ profile.authentication_db };
            try 
            {  java.lang.reflect.Constructor constructor=myclass.getConstructor(parameter_types);
               authentication_service=(AuthenticationService)constructor.newInstance(parameters);
            }
            catch (NoSuchMethodException e)
            {  printException(e,Log.LEVEL_MEDIUM);
               authentication_service=(AuthenticationService)myclass.newInstance();
            }
         }
         catch (Exception e)
         {  printException(e,Log.LEVEL_HIGH);
            printLog("Error trying to use authentication service '"+authentication_service_class+"': use default class.",Log.LEVEL_HIGH);
         }
         // use default authentication service
         if (authentication_service==null) authentication_service=new AuthenticationServiceImpl(server_profile.authentication_db);
         printLog("AuthenticationService ("+profile.authentication_service+"): size="+authentication_service.size()+"\r\n"+authentication_service.toString(),Log.LEVEL_MEDIUM);
         
         // now, init the proper authentication server
         String authentication_server_class=profile.authentication_scheme;
         for (int i=0; i<AUTHENTICATION_SCHEMES.length; i++)
            if (AUTHENTICATION_SCHEMES[i].equalsIgnoreCase(profile.authentication_scheme)) {  authentication_server_class=AUTHENTICATION_SERVER_CLASSES[i];  break;  }
         try
         {  Class myclass=Class.forName(authentication_server_class);
            Class[] parameter_types={ Class.forName("java.lang.String"), Class.forName("local.server.AuthenticationService"), Class.forName("org.zoolu.tools.Log") };
            Object[] parameters={ realm, authentication_service, sip_provider.getLog() };
            try
            {  java.lang.reflect.Constructor constructor=myclass.getConstructor(parameter_types);
               as=(AuthenticationServer)constructor.newInstance(parameters);
            }
            catch (NoSuchMethodException e)
            {  printException(e,Log.LEVEL_MEDIUM);
               as=(AuthenticationServer)myclass.newInstance();
            }
         }
         catch (Exception e)
         {  printException(e,Log.LEVEL_HIGH);
            printLog("Error trying to use authentication server '"+authentication_server_class+"': use default class.",Log.LEVEL_HIGH);
         }
         // use default authentication service
         if (as==null) as=new AuthenticationServerImpl(realm,authentication_service,sip_provider.getLog());
         printLog("AuthenticationServer: scheme: "+profile.authentication_scheme,Log.LEVEL_MEDIUM);
         printLog("AuthenticationServer: realm: "+profile.authentication_realm,Log.LEVEL_MEDIUM);
      }
      else as=null;

      // MEMORY MONITOR
      if (server_profile.memory_log)
      {  String filename=SipStack.log_path+"//"+sip_provider.getViaAddress()+"."+sip_provider.getPort()+"_memory.log";
         Log memory_log=new Log(filename,1);
         final org.zoolu.tools.MonitoredObjectWatcher obj_watcher=new org.zoolu.tools.MonitoredObjectWatcher(20000,memory_log);
         // decouple the two events
         try {  Thread.sleep(2000);  } catch (Exception e) {}
         new org.zoolu.tools.GarbageCollectorWatcher(60000,memory_log)
         {  public void doSomething() {  obj_watcher.dump();  }
         };
      }
   }

   
   // **************************** public methods ****************************

   /** When a new message is received by the SipProvider.
     * If the received message is a request, it cheks for loops, */
   public void onReceivedMessage(SipProvider provider, Message msg)
   {  printLog("message received",Log.LEVEL_MEDIUM);
      if (msg.isRequest()) // it is an INVITE or ACK or BYE or OPTIONS or REGISTER or CANCEL
      {  printLog("message is a request",Log.LEVEL_MEDIUM);

         // validate the message
         Message err_resp=validateRequest(msg);
         if (err_resp!=null)
         {  // for non-ACK requests respond with an error message
            if (!msg.isAck()) sip_provider.sendMessage(err_resp);
            return;
         }

         // target
         SipURL target=msg.getRequestLine().getAddress();  
         
         // look if the msg sent by the previous UA is compliant with the RFC2543 Strict Route rule..
         if (isResponsibleFor(target) && msg.hasRouteHeader())
         {  
            //SipURL route_url=msg.getRouteHeader().getNameAddress().getAddress();
            SipURL route_url=(new RouteHeader(msg.getRoutes().getBottom())).getNameAddress().getAddress();
            if (!route_url.hasLr())
            {  printLog("probably the message was compliant to RFC2543 Strict Route rule: message is updated to RFC3261",Log.LEVEL_MEDIUM);

               // the message has been sent to this server according with RFC2543 Strict Route
               // the proxy MUST replace the Request-URI in the request with the last
               // value from the Route header field, and remove that value from the
               // Route header field. The proxy MUST then proceed as if it received
               // this modified request.
               msg.rfc2543toRfc3261RouteUpdate();
               
               // update the target
               target=msg.getRequestLine().getAddress();
               printLog("new recipient: "+target.toString(),Log.LEVEL_LOW);
               
               // check again if this server is the target
               //this_is_target=matchesDomainName(target.getHost(),target.getPort());
            }
         }
         
         // removes the local Route value, if present
         /*if (msg.hasRouteHeader())
         {  MultipleHeader mr=msg.getRoutes();
            SipURL top_route=(new RouteHeader(mr.getTop())).getNameAddress().getAddress();
            if (matchesDomainName(top_route.getHost(),top_route.getPort()))
            {  mr.removeTop();
               if (mr.size()>0) msg.setRoutes(mr);
               else msg.removeRoutes();
            }
         }*/

         // check whether the request is for a domain the server is responsible for
         boolean is_for_this_domain=isResponsibleFor(msg);
         printLog("is for local doamin? "+((is_for_this_domain)?"yes":"no"),Log.LEVEL_LOW);
         
         // check whether the request is coming from a user belonging to a domain the server is responsible for
         boolean is_from_this_domain=isResponsibleFor(msg.getFromHeader().getNameAddress().getAddress());
         printLog("is from local doamin? "+((is_from_this_domain)?"yes":"no"),Log.LEVEL_LOW);

         if (is_for_this_domain && !target.hasUserName())
         {  printLog("the recipient is this server",Log.LEVEL_LOW);
            // check message authentication (server authentication)
            if (server_profile.do_authentication && !msg.isAck() && !msg.isCancel())
            {  err_resp=as.authenticateRequest(msg);  
               if (err_resp!=null)
               {  //sip_provider.sendMessage(err_resp);
                  TransactionServer ts=new TransactionServer(sip_provider,msg,null);
                  ts.respondWith(err_resp);
                  return;
               }
            }
            // process the message
            processRequestToLocalServer(msg);
         }
         else
         {  printLog("the recipient is NOT this server",Log.LEVEL_LOW);
            // check message authentication (proxy authentication)
            boolean is_spiral=(msg.getRemotePort()==sip_provider.getPort() && (msg.getRemoteAddress().startsWith("127.") || msg.getRemoteAddress().equals(sip_provider.getViaAddress())));
            if (server_profile.do_proxy_authentication && is_from_this_domain && !is_spiral && !msg.isAck() && !msg.isCancel())
            {  err_resp=as.authenticateProxyRequest(msg);
               if (err_resp!=null)
               {  //sip_provider.sendMessage(err_resp);
                  TransactionServer ts;
                  if (msg.isInvite()) ts=new InviteTransactionServer(sip_provider,msg,null);
                  else ts=new TransactionServer(sip_provider,msg,null);
                  ts.respondWith(err_resp);
                  return;
               }
            }
            if (is_for_this_domain)
            {  printLog("the request is for a local user",Log.LEVEL_LOW);
               // process the message
               processRequestToLocalUser(msg);
            }
            else
            {  printLog("the request is for a remote UA",Log.LEVEL_LOW);
               // process the message
               processRequestToRemoteUA(msg);
            }
         }
      }
      else // the message may be a response
      {  
         if (msg.isResponse())
         {  printLog("message is a response",Log.LEVEL_LOW);
            processResponse(msg);
         }
         else printWarning("received message is not recognized as a request nor a response: discarded",Log.LEVEL_HIGH);
      }
   }


   /** Relays the massage.
     * Called after a received message has been successful processed for being relayed */
   //protected void sendMessage(Message msg)
   //{  printLog("sending the successfully processed message",Log.LEVEL_MEDIUM);
   //   sip_provider.sendMessage(msg);
   //}
   

   /** Whether the server is responsible for the given <i>domain</i>
     * (i.e. the <i>domain</i> is included in the local domain names list)
     * and <i>port</i> (if >0) matches the local server port. */
   protected boolean isResponsibleFor(String domain, int port)
   {  // check port
      if (!server_profile.domain_port_any && port>0 && port!=sip_provider.getPort()) return false;
      // check host address
      if (domain.equals(sip_provider.getViaAddress())) return true;
      // check domain name
      boolean result=false;
      for (int i=0; i<server_profile.domain_names.length; i++)
      {  if (server_profile.domain_names[i].equals(domain)) { result=true; break; }
      }
      return result;
   }
   
   /** Whether the server is responsible for the request-uri of the request <i>req</i>. */
   protected boolean isResponsibleFor(Message req)
   {  SipURL target=req.getRequestLine().getAddress();
      return isResponsibleFor(target.getHost(),target.getPort());
   }

   /** Whether the server is responsible for the specified url. */
   protected boolean isResponsibleFor(SipURL url)
   {  //return isResponsibleFor(url.getHost(),url.getPort());
      String hostaddr=url.getHost();
      int hostport=url.getPort();
      if (!isResponsibleFor(hostaddr,hostport)) return false;
      // check whether he/she is a local user 
      String username=url.getUserName();
      if (username!=null) return location_service.hasUser(username+"@"+hostaddr);
      else return true;
   }

   /** Whether the request is for the local server */
   /*protected boolean isTargetOf(Message req)
   {  SipURL target=req.getRequestLine().getAddress();
      if (!isResponsibleFor(target.getHost(),target.getPort())) return false;
      // else, request-uri matches a domain the server is responsible for
      if (!req.hasRouteHeader()) return true; 
      // else, has route..
      MultipleHeader route=req.getRoutes();
      if (route.size()>1) return false;
      // else, only 1 route, check it
      target=(new RouteHeader(route.getTop())).getNameAddress().getAddress();
      if (!isResponsibleFor(target.getHost(),target.getPort()))  return false;
      // else
      return true;
   }*/

   /** Gets a String of the list of local domain names. */
   protected String getLocalDomains()
   {  if (server_profile.domain_names.length>0)
      {  String str="";
         for (int i=0; i<server_profile.domain_names.length-1; i++)
         {  str+=server_profile.domain_names[i]+", ";
         }
         return str+server_profile.domain_names[server_profile.domain_names.length-1];
      }
      else return "";
   }
   
   /** Validates the message.
     * @return It returns 0 if the message validation successes, otherwise return the error code. */
   protected Message validateRequest(Message msg)
   {  printLog("inside validateRequest(msg)",Log.LEVEL_LOW);
   
      int err_code=0;
      
      // Max-Forwads
      if (err_code==0) 
      {  MaxForwardsHeader mfh=msg.getMaxForwardsHeader();
         if (mfh!=null && mfh.getNumber()==0) err_code=483;
      }
      // Loops
      // Insert also a temporary Loop-Tag header field in order to correctly compose
      // the branch field when forwarding the message.
      // This behaviour has been choosen since the message validation is done
      // when receiving the message while the information used for loop detection
      // (the branch parameter) is calculated and added when sending the message.
      // Note that RFC 2631 suggests to calculate the branch parameter based on
      // the original request-uri, but the request-uri has been already replaced
      // and forgotten when processing the message for calculating the branch..
      // telepathy? ;)
      if (err_code==0 && server_profile.loop_detection)
      {  String loop_tag=pickLoopTag(msg);
         // add temporary Loop-Tag header field
         msg.setHeader(new Header(Loop_Tag,loop_tag));
         // check for loop
         if (!msg.hasRouteHeader()) 
         {  Vector v=msg.getVias().getHeaders();
            for (int i=0; i<v.size(); i++)
            {  ViaHeader vh=new ViaHeader((Header)v.elementAt(i));
               if (sip_provider.getViaAddress().equals(vh.getHost()) && sip_provider.getPort()==vh.getPort())
               {  // possible loop
                  if (!vh.hasBranch()) err_code=482;
                  else
                  {  // check branch
                     String branch=vh.getBranch();
                     if (branch.indexOf(loop_tag,branch.length()-loop_tag.length())>=0) err_code=482;
                  }
               }
            }
         }
      } 
            
      // Proxy-Require

      // Proxy-Authorization

      if (err_code>0)
      {  printLog("Message validation failed ("+err_code+" "+SipResponses.reasonOf(err_code)+"), message discarded",Log.LEVEL_HIGH);
         return MessageFactory.createResponse(msg,err_code,null,null);
      }
      else return null;
   }

   /** Picks an unique branch value based on a SIP message.
     * This value could also be used for loop detection. */
   /*public String pickBranch(Message msg)
   {  String branch=sip_provider.pickBranch(msg);
      if (server_profile.loop_detection) branch+=pickLoopTag(msg);
      return branch;
   }*/

   /** Picks the token used for loop detection. */
   private String pickLoopTag(Message msg)
   {  StringBuffer sb=new StringBuffer();
      sb.append(msg.getToHeader().getTag());
      sb.append(msg.getFromHeader().getTag());
      sb.append(msg.getCallIdHeader().getCallId());
      sb.append(msg.getRequestLine().getAddress().toString());
      sb.append(msg.getCSeqHeader().getSequenceNumber());
      MultipleHeader rr=msg.getRoutes();
      if (rr!=null) sb.append(rr.size());
      return (new SimpleDigest(7,sb.toString())).asHex();
   }


   // ********************************* logs *********************************

   /** Default log level offset */
   static final int LOG_OFFSET=0;
   
   /** Adds a new string to the default Log */
   private void printLog(String str, int level)
   {  if (log!=null) log.println("ServerEngine: "+str,ServerEngine.LOG_OFFSET+level);  
   }

   /** Adds a Warning message to the default Log */
   private final void printWarning(String str, int level)
   {  printLog("WARNING: "+str,level); 
   }

   /** Adds the Exception message to the default Log */
   private final void printException(Exception e, int level)
   {  printLog("Exception: "+ExceptionPrinter.getStackTraceOf(e),level);
   }
}
