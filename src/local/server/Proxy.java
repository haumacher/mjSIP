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


import org.zoolu.sip.address.*;
import org.zoolu.sip.provider.*;
import org.zoolu.sip.header.RequestLine;
import org.zoolu.sip.header.Header;
import org.zoolu.sip.header.ViaHeader;
import org.zoolu.sip.header.MaxForwardsHeader;
import org.zoolu.sip.header.MultipleHeader;
import org.zoolu.sip.header.RouteHeader;
import org.zoolu.sip.header.RecordRouteHeader;
import org.zoolu.sip.message.Message;
import org.zoolu.sip.message.MessageFactory;
import org.zoolu.sip.message.SipResponses;
import org.zoolu.tools.Log;


//import java.util.Enumeration;
import java.util.Vector;
import java.io.BufferedReader;
import java.io.InputStreamReader;


/** Class Proxy implement a Proxy SIP Server.
  * It extends class Registrar. A Proxy can work as simply SIP Proxy,
  * or it can handle calls for registered users. 
  */
public class Proxy extends Registrar
{   
   /** Log of processed calls */
   CallLogger call_logger;


   /** Costructs a void Proxy */
   protected Proxy() {}


   /** Costructs a new Proxy that acts also as location server for registered users. */
   public Proxy(SipProvider provider, ServerProfile server_profile)
   {  super(provider,server_profile);
      if (server_profile.call_log) call_logger=new CallLoggerImpl(SipStack.log_path+"//"+provider.getViaAddress()+"."+provider.getPort()+"_calls.log");
   }


   /** When a new request is received for the local server. */
   public void processRequestToLocalServer(Message msg)
   {  printLog("inside processRequestToLocalServer(msg)",Log.LEVEL_MEDIUM);
      if (msg.isRegister())
      {  super.processRequestToLocalServer(msg);
      }
      else
      if (!msg.isAck())
      {  // send a stateless error response
         //int result=501; // response code 501 ("Not Implemented")
         //int result=485; // response code 485 ("Ambiguous");
         int result=484; // response code 484 ("Address Incomplete");
         Message resp=MessageFactory.createResponse(msg,result,null,null);
         sip_provider.sendMessage(resp);
      }
   }


   /** When a new request message is received for a local user */
   public void processRequestToLocalUser(Message msg)
   {  printLog("inside processRequestToLocalUser(msg)",Log.LEVEL_MEDIUM);

      if (server_profile.call_log) call_logger.update(msg);

      // proxy authentication
      /*if (server_profile.do_proxy_authentication && !msg.isAck() && !msg.isCancel())
      {  Message err_resp=as.authenticateProxyRequest(msg);  
         if (err_resp!=null)
         {  sip_provider.sendMessage(err_resp);
            return;
         }
      }*/

      // message targets
      Vector targets=getTargets(msg);
      
      if (targets.isEmpty())
      {  // prefix-based forwarding
         SipURL request_uri=msg.getRequestLine().getAddress();
         SipURL new_target=null;
         if (isResponsibleFor(msg.getFromHeader().getNameAddress().getAddress())) new_target=getAuthPrefixBasedProxyingTarget(request_uri);
         if (new_target==null) new_target=getPrefixBasedProxyingTarget(request_uri);
         if (new_target!=null) targets.addElement(new_target.toString());
      }
      if (targets.isEmpty())
      {  printLog("No target found, message discarded",Log.LEVEL_HIGH);
         if (!msg.isAck()) sip_provider.sendMessage(MessageFactory.createResponse(msg,404,null,null));
         return;
      }           
      
      printLog("message will be forwarded to "+targets.size()+" user's contact(s)",Log.LEVEL_MEDIUM); 
      for (int i=0; i<targets.size(); i++) 
      {  SipURL target_url=new SipURL((String)(targets.elementAt(i)));
         Message request=new Message(msg);
         request.removeRequestLine();
         request.setRequestLine(new RequestLine(msg.getRequestLine().getMethod(),target_url));
         
         updateProxyingRequest(request);
         sip_provider.sendMessage(request);
      }
   }

   
   /** When a new request message is received for a remote UA */
   public void processRequestToRemoteUA(Message msg)
   {  printLog("inside processRequestToRemoteUA(msg)",Log.LEVEL_MEDIUM);
   
      if (call_logger!=null) call_logger.update(msg);

      if (!server_profile.is_open_proxy)
      {  // check whether the caller or callee is a local user 
         if (!isResponsibleFor(msg.getFromHeader().getNameAddress().getAddress()) && !isResponsibleFor(msg.getToHeader().getNameAddress().getAddress()))
         {  printLog("both caller and callee are not registered with the local server: proxy denied.",Log.LEVEL_HIGH);
            sip_provider.sendMessage(MessageFactory.createResponse(msg,503,null,null));
            return;
         }
      }
      
      // proxy authentication
      /*if (server_profile.do_proxy_authentication && !msg.isAck() && !msg.isCancel())
      {  Message err_resp=as.authenticateProxyRequest(msg);  
         if (err_resp!=null)
         {  sip_provider.sendMessage(err_resp);
            return;
         }
      }*/
      
      // domain-based forwarding
      RequestLine rl=msg.getRequestLine();
      SipURL request_uri=rl.getAddress();
      SipURL nexthop=null;
      if (isResponsibleFor(msg.getFromHeader().getNameAddress().getAddress())) nexthop=getAuthDomainBasedProxyingTarget(request_uri);
      if (nexthop==null) nexthop=getDomainBasedProxyingTarget(request_uri);
      if (nexthop!=null) msg.setRequestLine(new RequestLine(rl.getMethod(),nexthop));
      
      updateProxyingRequest(msg); 
     
      sip_provider.sendMessage(msg);
   }

   
   /** Processes the Proxy headers of the request.
     * Such headers are: Via, Record-Route, Route, Max-Forwards, etc. */
   protected Message updateProxyingRequest(Message msg)
   {  printLog("inside updateProxyingRequest(msg)",Log.LEVEL_LOW);

      // remove Route if present
      //boolean is_on_route=false;  
      if (msg.hasRouteHeader())
      {  MultipleHeader mr=msg.getRoutes();
         SipURL route=(new RouteHeader(mr.getTop())).getNameAddress().getAddress();
         if (isResponsibleFor(route.getHost(),route.getPort()))
         {  mr.removeTop();
            if (mr.size()>0) msg.setRoutes(mr);
            else msg.removeRoutes();
            //is_on_route=true;
         }
      }
      // add Record-Route?
      if (server_profile.on_route && msg.isInvite()/* && !is_on_route*/)
      {  SipURL rr_url;
         if (sip_provider.getPort()==SipStack.default_port) rr_url=new SipURL(sip_provider.getViaAddress());
         else rr_url=new SipURL(sip_provider.getViaAddress(),sip_provider.getPort());
         if (server_profile.loose_route) rr_url.addLr();
         RecordRouteHeader rrh=new RecordRouteHeader(new NameAddress(rr_url));
         msg.addRecordRouteHeader(rrh);
      }
      // which protocol?
      String proto=null;
      if (msg.hasRouteHeader())
      {  SipURL route=msg.getRouteHeader().getNameAddress().getAddress();
         if (route.hasTransport()) proto=route.getTransport();
      }
      else proto=msg.getRequestLine().getAddress().getTransport();
      if (proto==null) proto=sip_provider.getDefaultTransport();
      
      // add Via
      ViaHeader via=new ViaHeader(proto,sip_provider.getViaAddress(),sip_provider.getPort());
      if (sip_provider.isRportSet()) via.setRport();
      String branch=sip_provider.pickBranch(msg);
      if (server_profile.loop_detection)
      {  String loop_tag=msg.getHeader(Loop_Tag).getValue();
         if (loop_tag!=null)
         {  msg.removeHeader(Loop_Tag);
            branch+=loop_tag;
         }
      }
      via.setBranch(branch);
      msg.addViaHeader(via);

      // decrement Max-Forwards
      MaxForwardsHeader maxfwd=msg.getMaxForwardsHeader();
      if (maxfwd!=null) maxfwd.decrement();
      else maxfwd=new MaxForwardsHeader(SipStack.max_forwards);
      msg.setMaxForwardsHeader(maxfwd);      

      // check whether the next Route is formed according to RFC2543
      msg.rfc2543RouteAdapt();
              
      return msg;                             
   }
   

   /** When a new response message is received */
   public void processResponse(Message resp)
   {  printLog("inside processResponse(msg)",Log.LEVEL_MEDIUM);
   
      if(call_logger!=null) call_logger.update(resp);

      updateProxyingResponse(resp);
      
      if (resp.hasViaHeader()) sip_provider.sendMessage(resp);
      else
         printLog("no VIA header found: message discarded",Log.LEVEL_HIGH);            
   }
   
   
   /** Processes the Proxy headers of the response.
     * Such headers are: Via, .. */
   protected Message updateProxyingResponse(Message resp)
   {  printLog("inside updateProxyingResponse(resp)",Log.LEVEL_MEDIUM);
      // remove the top most via
      //ViaHeader vh=new ViaHeader((Header)resp.getVias().getHeaders().elementAt(0));
      //if (vh.getHost().equals(sip_provider.getViaAddress())) resp.removeViaHeader();
      // remove the top most via regardless the via has been insterted by this node or not (this prevents loops)
      resp.removeViaHeader();
      return resp;
   }
   

   /** Gets a new target according to the domain-based forwarding rules. */
   protected SipURL getAuthDomainBasedProxyingTarget(SipURL request_uri)
   {  printLog("inside getAuthDomainBasedProxyingTarget(uri)",Log.LEVEL_LOW);
      // authenticated rules
      for (int i=0; i<server_profile.authenticated_domain_proxying_rules.length; i++)
      {  ProxyingRule rule=(ProxyingRule)server_profile.authenticated_domain_proxying_rules[i];
         SipURL nexthop=rule.getNexthop(request_uri);
         if (nexthop!=null)
         {  printLog("domain-based authenticated forwarding: "+rule.toString()+": YES",Log.LEVEL_MEDIUM);
            printLog("target="+nexthop.toString(),Log.LEVEL_MEDIUM);
            return nexthop;
         }
         else printLog("domain-based authenticated forwarding: "+rule.toString()+": NO",Log.LEVEL_MEDIUM);
      }
      return null;
   }


   /** Gets a new target according to the domain-based forwarding rules. */
   protected SipURL getDomainBasedProxyingTarget(SipURL request_uri)
   {  printLog("inside getDomainBasedForwardingTarget(uri)",Log.LEVEL_LOW);
      // non-authenticated rules
      for (int i=0; i<server_profile.domain_proxying_rules.length; i++)
      {  ProxyingRule rule=(ProxyingRule)server_profile.domain_proxying_rules[i];
         SipURL nexthop=rule.getNexthop(request_uri);
         if (nexthop!=null)
         {  printLog("domain-based forwarding: "+rule.toString()+": YES",Log.LEVEL_MEDIUM);
            printLog("target="+nexthop.toString(),Log.LEVEL_MEDIUM);
            return nexthop;
         }
         else printLog("domain-based forwarding: "+rule.toString()+": NO",Log.LEVEL_MEDIUM);
      }
      return null;
   }


   /** Gets a new target according to the authenticated prefix-based forwarding rules. */
   protected SipURL getAuthPrefixBasedProxyingTarget(SipURL request_uri)
   {  printLog("inside getAuthPrefixBasedProxyingTarget(uri)",Log.LEVEL_LOW);
      String username=request_uri.getUserName();
      if (username==null || !isPhoneNumber(username))  return null;
      // authenticated rules
      printLog("authenticated prefix-based rules: "+server_profile.authenticated_phone_proxying_rules.length,Log.LEVEL_LOW);
      for (int i=0; i<server_profile.authenticated_phone_proxying_rules.length; i++)
      {  ProxyingRule rule=(ProxyingRule)server_profile.authenticated_phone_proxying_rules[i];
         SipURL nexthop=rule.getNexthop(request_uri);
         if (nexthop!=null)
         {  printLog("prefix-based authenticated forwarding: "+rule.toString()+": YES",Log.LEVEL_MEDIUM);
            printLog("target="+nexthop.toString(),Log.LEVEL_MEDIUM);
            return nexthop;
         }
         else printLog("prefix-based authenticated forwarding: "+rule.toString()+": NO",Log.LEVEL_MEDIUM);
      }
      return null;
   }


   /** Gets a new target according to the prefix-based forwarding rules. */
   protected SipURL getPrefixBasedProxyingTarget(SipURL request_uri)
   {  printLog("inside getPrefixBasedProxyingTarget(uri)",Log.LEVEL_LOW);
      String username=request_uri.getUserName();
      if (username==null || !isPhoneNumber(username))  return null;
      // non-authenticated rules
      printLog("prefix-based rules: "+server_profile.phone_proxying_rules.length,Log.LEVEL_LOW);
      for (int i=0; i<server_profile.phone_proxying_rules.length; i++)
      {  ProxyingRule rule=(ProxyingRule)server_profile.phone_proxying_rules[i];
         SipURL nexthop=rule.getNexthop(request_uri);
         if (nexthop!=null)
         {  printLog("prefix-based forwarding: "+rule.toString()+": YES",Log.LEVEL_MEDIUM);
            printLog("target="+nexthop.toString(),Log.LEVEL_MEDIUM);
            return nexthop;
         }
         else printLog("prefix-based forwarding: "+rule.toString()+": NO",Log.LEVEL_MEDIUM);
      }
      return null;
   }


   /** Whether the String is a phone number. */
   protected boolean isPhoneNumber(String str)
   {  if (str==null || str.length()==0) return false;
      for (int i=0; i<str.length(); i++)
      {  char c=str.charAt(i);
         if (c!='+' && c!='-' && c!='*' && c!='#' && (c<'0' || c>'9')) return false;
      }
      return true;
   }   


   // ****************************** Logs *****************************

   /** Adds a new string to the default Log */
   private void printLog(String str, int level)
   {  if (log!=null) log.println("Proxy: "+str,ServerEngine.LOG_OFFSET+level);  
   }


   // ****************************** MAIN *****************************

   /** The main method. */
   public static void main(String[] args)
   {  
         
      String file=null;
      boolean prompt_exit=false;
      
      for (int i=0; i<args.length; i++)
      {  if (args[i].equals("-f") && args.length>(i+1))
         {  file=args[++i];
            continue;
         }
         if (args[i].equals("--prompt"))
         {  prompt_exit=true;
            continue;
         }
         if (args[i].equals("-h"))
         {  System.out.println("usage:\n   java Proxy [options] \n");
            System.out.println("   options:");
            System.out.println("   -h               this help");
            System.out.println("   -f <config_file> specifies a configuration file");
            System.out.println("   --prompt         prompt for exit");
            System.exit(0);
         }
      }
                  
      SipStack.init(file);
      SipProvider sip_provider=new SipProvider(file);
      ServerProfile server_profile=new ServerProfile(file);

      new Proxy(sip_provider,server_profile);
      
      // promt before exit
      if (prompt_exit) 
      try
      {  System.out.println("press 'enter' to exit");
         BufferedReader in=new BufferedReader(new InputStreamReader(System.in)); 
         in.readLine();
         System.exit(0);
      }
      catch (Exception e) {}
   }
  
}