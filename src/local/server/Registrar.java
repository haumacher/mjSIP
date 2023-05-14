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


import org.zoolu.net.SocketAddress;
import org.zoolu.sip.address.*;
import org.zoolu.sip.provider.*;
import org.zoolu.sip.header.SipHeaders;
import org.zoolu.sip.header.Header;
import org.zoolu.sip.header.ToHeader;
import org.zoolu.sip.header.ViaHeader;
import org.zoolu.sip.header.ExpiresHeader;
import org.zoolu.sip.header.StatusLine;
import org.zoolu.sip.header.ContactHeader;
import org.zoolu.sip.header.MultipleHeader;
import org.zoolu.sip.header.WwwAuthenticateHeader;
import org.zoolu.sip.header.AuthorizationHeader;
import org.zoolu.sip.header.AuthenticationInfoHeader;
import org.zoolu.sip.transaction.TransactionServer;
import org.zoolu.sip.message.Message;
import org.zoolu.sip.message.MessageFactory;
import org.zoolu.sip.message.SipResponses;
import org.zoolu.tools.Parser;
import org.zoolu.tools.Log;
import org.zoolu.tools.DateFormat;

import java.util.Date;
//import java.util.Locale;
//import java.text.DateFormat;
//import java.text.SimpleDateFormat;
import java.util.Vector;
import java.util.Enumeration;


/** Class Registrar implements a Registrar SIP Server.
  * It extends class ServerEngine.
  */
public class Registrar extends ServerEngine
{   
   
   /** Costructs a void Registrar. */
   protected Registrar() {}
   
   
   /** Costructs a new Registrar. The Location Service is stored within the file <i>db_name</i> */
   //public Registrar(SipProvider provider, String db_class, String db_name)
   public Registrar(SipProvider provider, ServerProfile profile)
   {  super(provider,profile);
   }


   /** When a new request is received for the local server. */
   public void processRequestToLocalServer(Message msg)
   {  
      printLog("inside processRequestToLocalServer(msg)",Log.LEVEL_MEDIUM);
      if (server_profile.is_registrar && msg.isRegister())
      {  TransactionServer t=new TransactionServer(sip_provider,msg,null);
   
         /*if (server_profile.do_authentication)
         {  // check message authentication
            Message err_resp=as.authenticateRequest(msg);  
            if (err_resp!=null)
            {  t.respondWith(err_resp);
               return;
            }
         }*/
         
         Message resp=updateRegistration(msg);
         if (resp==null) return;
         
         if (server_profile.do_authentication)
         {  // add Authentication-Info header field
            resp.setAuthenticationInfoHeader(as.getAuthenticationInfoHeader());
         }
         
         t.respondWith(resp);
      }
      else
      if (!msg.isAck())
      {  // send a stateless error response
         int result=501; // response code 501 ("Not Implemented")
         Message resp=MessageFactory.createResponse(msg,result,null,null);
         sip_provider.sendMessage(resp);
      }     
   }


   /** When a new request message is received for a local user. */
   public void processRequestToLocalUser(Message msg)
   {  printLog("inside processRequestToLocalUser(msg)",Log.LEVEL_MEDIUM);
      // stateless-response (in order to avoid DoS attacks)
      if (!msg.isAck()) sip_provider.sendMessage(MessageFactory.createResponse(msg,404,null,null));
      else printLog("message discarded",Log.LEVEL_HIGH);
   }
 
   
   /** When a new request message is received for a remote UA. */
   public void processRequestToRemoteUA(Message msg)
   {  printLog("inside processRequestToRemoteUA(msg)",Log.LEVEL_MEDIUM);
      // stateless-response (in order to avoid DoS attacks)
      if (!msg.isAck()) sip_provider.sendMessage(MessageFactory.createResponse(msg,404,null,null));
      else printLog("message discarded",Log.LEVEL_HIGH);
   }


   /** When a new response message is received. */
   public void processResponse(Message resp)
   {  printLog("inside processResponse(msg)",Log.LEVEL_MEDIUM);
      // no actions..
      printLog("message discarded",Log.LEVEL_HIGH);
   }
   
   
   // *********************** protected methods ***********************

   /** Gets the request's targets as Vector of String.
     * @return It returns a Vector of String representing the target URLs. */
   protected Vector getTargets(Message msg)
   {  printLog("inside getTargets(msg)",Log.LEVEL_LOW);

      Vector targets=new Vector();
      
      if (location_service==null)
      {  printLog("Location service is not active",Log.LEVEL_HIGH);
         return targets;
      }           

      SipURL request_uri=msg.getRequestLine().getAddress();
      String username=request_uri.getUserName();
      if (username==null)
      {  printLog("no username found",Log.LEVEL_HIGH);
         return targets;
      }
      String user=username+"@"+request_uri.getHost();
      printLog("user: "+user,Log.LEVEL_MEDIUM); 
           
      if (!location_service.hasUser(user))
      {  printLog("user "+user+" not found",Log.LEVEL_HIGH);
         return targets;
      }

      SipURL to_url=msg.getToHeader().getNameAddress().getAddress();
      
      Enumeration e=location_service.getUserContactURLs(user);
      printLog("message targets: ",Log.LEVEL_LOW);  
      for (int i=0; e.hasMoreElements(); i++)
      {  // if exipred, remove the contact url
         String contact=(String)e.nextElement();
         if (location_service.isUserContactExpired(user,contact))
         {  location_service.removeUserContact(user,contact);
            printLog("target"+i+" expired: contact url removed",Log.LEVEL_LOW);
         }
         // otherwise add the url to the target list
         else
         {  targets.addElement(contact);
            printLog("target"+i+"="+targets.elementAt(i),Log.LEVEL_LOW);
         }
      }
      // for sips request-uri remove non-sips targets; for sip request-uri remove sips targets
      boolean is_sips=request_uri.isSecure();
      for (int i=0; i<targets.size(); i++)
      {  SipURL uri=new SipURL((String)targets.elementAt(i));
         if (uri.isSecure()!=is_sips)
         {  printLog(uri.toString()+" has not a coherent sip/sips scheme: skipped",Log.LEVEL_HIGH);
            targets.removeElementAt(i--);
         }
      }
      return targets;
   }


   /** Updates the registration of a local user.
     * @return it returns the response message for the registration. */
   protected Message updateRegistration(Message msg)
   {  ToHeader th=msg.getToHeader();
      if (th==null)  
      {  printLog("ToHeader missed: message discarded",Log.LEVEL_HIGH);
         int result=400;
         return MessageFactory.createResponse(msg,result,null,null);  
      }         
      SipURL dest_uri=th.getNameAddress().getAddress();
      String user=dest_uri.getUserName()+"@"+dest_uri.getHost();

      int exp_secs=server_profile.expires;
      // set the expire value
      ExpiresHeader eh=msg.getExpiresHeader();
      if (eh!=null)
      {  exp_secs=eh.getDeltaSeconds();
      }
      // limit the expire value
      if (exp_secs<0) exp_secs=0;
      else
      if (exp_secs>server_profile.expires) exp_secs=server_profile.expires;

      // known user?
      if (!location_service.hasUser(user))
      {  if (server_profile.register_new_users)
         {  location_service.addUser(user);
            printLog("new user '"+user+"' added",Log.LEVEL_HIGH);
         } 
         else
         {  printLog("user '"+user+"' unknown: message discarded.",Log.LEVEL_HIGH);
            int result=404;
            return MessageFactory.createResponse(msg,result,null,null);  
         }
      }

      // Get the "device" parameter. Set device=null if not present or not supported
      //String device=null;
      // if (msg.hasApplicationHeader()) app=msg.getApplicationHeader().getApplication();
      SipURL to_url=msg.getToHeader().getNameAddress().getAddress();
      //if (to_url.hasParameter("device")) device=to_url.getParameter("device");

      if (!msg.hasContactHeader())  
      {  //printLog("ContactHeader missed: message discarded",Log.LEVEL_HIGH);
         //int result=484;
         //return MessageFactory.createResponse(msg,result,null,null,null);  
         printLog("no contact found: fetching bindings..",Log.LEVEL_MEDIUM);
         int result=200;
         Message resp=MessageFactory.createResponse(msg,result,null,null);  
         // add current contacts
         Vector v=new Vector();
         for (Enumeration e=location_service.getUserContactURLs(user); e.hasMoreElements(); )
         {  String contact=(String)e.nextElement();
            int expires=(int)(location_service.getUserContactExpirationDate(user,contact).getTime()-System.currentTimeMillis())/1000;
            if (expires>0)
            {  // not expired
               ContactHeader ch=new ContactHeader(location_service.getUserContactNameAddress(user,contact));
               ch.setExpires(expires);
               v.addElement(ch);
            }
         }
         if (v.size()>0) resp.setContacts(new MultipleHeader(v));
         return resp;
      }
      // else     

      Vector contacts=msg.getContacts().getHeaders();
      int result=200;
      Message resp=MessageFactory.createResponse(msg,result,null,null);  

      ContactHeader ch_0=new ContactHeader((Header)contacts.elementAt(0));
      if (ch_0.isStar())
      {  printLog("DEBUG: ContactHeader is star",Log.LEVEL_LOW);
         Vector resp_contacts=new Vector();
         for (Enumeration e=location_service.getUserContactURLs(user); e.hasMoreElements();) 
         {  String contact=(String)(e.nextElement());
            if (!location_service.isUserContactStatic(user,contact)) 
            {  NameAddress name_address=location_service.getUserContactNameAddress(user,contact);
               // update db
               location_service.removeUserContact(user,contact);
               printLog("contact removed: "+contact,Log.LEVEL_LOW);
               if (exp_secs>0)
               {  Date exp_date=new Date(System.currentTimeMillis()+((long)exp_secs)*1000);
                  location_service.addUserContact(user,name_address,exp_date);
                  //DateFormat df=new SimpleDateFormat("EEE, dd MMM yyyy hh:mm:ss 'GMT'",Locale.ITALIAN);
                  //printLog("contact added: "+url+"; expire: "+df.format(location_service.getUserContactExpire(user,url)),Log.LEVEL_LOW);
                  printLog("contact added: "+contact+"; expire: "+DateFormat.formatEEEddMMM(location_service.getUserContactExpirationDate(user,contact)),Log.LEVEL_LOW);
               }
               ContactHeader ch_i=new ContactHeader(name_address.getAddress());
               ch_i.setExpires(exp_secs);
               resp_contacts.addElement(ch_i);
            }
         }
         if (resp_contacts.size()>0) resp.setContacts(new MultipleHeader(resp_contacts));
      }
      else
      {  Vector resp_contacts=new Vector();
         for (int i=0; i<contacts.size(); i++)     
         {  ContactHeader ch_i=new ContactHeader((Header)contacts.elementAt(i));
            NameAddress name_address=ch_i.getNameAddress();     
            String contact=name_address.getAddress().toString();     
            int exp_secs_i=exp_secs;
            if (ch_i.hasExpires()) 
            {  exp_secs_i=ch_i.getExpires();
            }
            // limit the expire value
            if (exp_secs_i<0) exp_secs_i=0;
            else
            if (exp_secs_i>server_profile.expires) exp_secs_i=server_profile.expires;
                        
            // update db
            location_service.removeUserContact(user,contact);
            if (exp_secs_i>0)
            {  Date exp_date=new Date(System.currentTimeMillis()+((long)exp_secs)*1000);
               location_service.addUserContact(user,name_address,exp_date);
               printLog("registration of user "+user+" updated",Log.LEVEL_HIGH);
            }           
            ch_i.setExpires(exp_secs_i);
            resp_contacts.addElement(ch_i);
         }
         if (resp_contacts.size()>0) resp.setContacts(new MultipleHeader(resp_contacts));
      }

      location_service.sync();  
      return resp;
   }


   // ****************************** Logs *****************************

   /** Adds a new string to the default Log. */
   private void printLog(String str, int level)
   {  if (log!=null) log.println("Registrar: "+str,ServerEngine.LOG_OFFSET+level);  
   }
  

   // ****************************** MAIN *****************************

   /** The main method. */
   public static void main(String[] args)
   {  
         
      String file=null;
      
      for (int i=0; i<args.length; i++)
      {  if (args[i].equals("-f") && args.length>(i+1))
         {  file=args[++i];
            continue;
         }
         if (args[i].equals("-h"))
         {  System.out.println("usage:\n   java Registrar [-f <config_file>] \n");
            System.exit(0);
         }
      }
                  
      SipStack.init(file);
      SipProvider sip_provider=new SipProvider(file);
      ServerProfile server_profile=new ServerProfile(file);
      
      new Registrar(sip_provider,server_profile);
   }
}