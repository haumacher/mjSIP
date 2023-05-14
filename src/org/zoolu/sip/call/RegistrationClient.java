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

package org.zoolu.sip.call;


import org.zoolu.net.SocketAddress;
import org.zoolu.sip.address.*;
import org.zoolu.sip.provider.SipStack;
import org.zoolu.sip.provider.SipProvider;
//import org.zoolu.sip.provider.SipKeepAlive;
import org.zoolu.sip.header.*;
import org.zoolu.sip.message.*;
import org.zoolu.sip.transaction.TransactionClient;
import org.zoolu.sip.transaction.TransactionClientListener;
import org.zoolu.sip.authentication.DigestAuthentication;
import org.zoolu.tools.Timer;
import org.zoolu.tools.TimerListener;
import org.zoolu.tools.Log;
import org.zoolu.tools.ExceptionPrinter;

import java.util.Vector;


/** RegistrationClient does register (one time or periodically)
  * a contact address with a registrar server.
  */
public class RegistrationClient implements TransactionClientListener, TimerListener
{
   /** Log */
   protected Log log;

   /** RegistrationClient listener */
   protected RegistrationClientListener listener;
      
   /** SipProvider */
   protected SipProvider sip_provider;

   /** Fully qualified domain name of the registrar server. */
   protected SipURL registrar;

   /** URI registered with registrar server. */
   protected NameAddress target;

   /** URI of the user that is actually performing the registration. */
   protected NameAddress from;

   /** User name. */
   protected String username;

   /** User name. */
   protected String realm;

   /** User's passwd. */
   protected String passwd;

   /** Nonce for the next authentication. */
   protected String next_nonce;

   /** Qop for the next authentication. */
   protected String qop;

   /** User's contact address. */
   protected NameAddress contact; 

   /** Expiration time. */
   protected int expire_time;

   /** Renew time. */
   protected int renew_time;

   /** Attempt timeout */
   Timer attempt_to;

   /** Registration timeout */
   Timer registration_to;

   /** Whether keep on registering. */
   boolean loop;

   /** Whether the thread is running. */
   boolean is_running;

   /** Number of registration attempts. */
   int attempts;
   
   /** SipKeepAlive daemon. */
   //SipKeepAlive keep_alive;

      
   /** Creates a new RegistrationClient. */
   public RegistrationClient(SipProvider sip_provider, SipURL registrar, NameAddress target_url, NameAddress from_url, RegistrationClientListener listener)
   {  init(sip_provider,registrar,target_url,from_url,listener);
   }
   
   
   /** Creates a new RegistrationClient with authentication credentials (i.e. username, realm, and passwd). */
   public RegistrationClient(SipProvider sip_provider, SipURL registrar, NameAddress target_url, NameAddress from_url, String username, String realm, String passwd, RegistrationClientListener listener)
   {  init(sip_provider,registrar,target_url,from_url,listener);
      // authentication
      this.username=username;
      this.realm=realm;
      this.passwd=passwd;
   }

   /** Inits the RegistrationClient. */
   private void init(SipProvider sip_provider, SipURL registrar, NameAddress target_url, NameAddress from_url, RegistrationClientListener listener)
   {  this.listener=listener;
      this.sip_provider=sip_provider;
      this.log=sip_provider.getLog();
      this.registrar=registrar;
      this.target=target_url;
      this.from=from_url;
      this.contact=new NameAddress(sip_provider.getContactAddress(from.getAddress().getUserName()));
      this.expire_time=SipStack.default_expires;
      this.renew_time=SipStack.default_expires;
      this.is_running=false;
      //this.keep_alive=null;
      // authentication
      this.username=null;
      this.realm=null;
      this.passwd=null;
      this.next_nonce=null;
      this.qop=null;
      this.attempts=0;
   }


   /** Gets the target NameAddress. */
   public NameAddress getTarget()
   {  return target;
   }


   /** Whether it is periodically registering. */
   public boolean isRegistering()
   {  return is_running;
   }


   /** Registers with the registrar server.
     * It does register with the previously set expire time.  */
   public void register()
   {  register(expire_time);
   }


   /** Unregister with the registrar server.
     * It does register with expire time = 0. */
   public void unregister()
   {  register(0);
   } 


   /** Registers with the registrar server for <i>expire_time</i> seconds. */
   public void register(int expire_time)
   {  register(expire_time,null);
   }


   /** Registers with the registrar server for <i>expire_time</i> seconds, with a given message body. */
   protected void register(int expire_time, String body)
   {  printLog("register with "+registrar+" for "+expire_time+" secs",Log.LEVEL_HIGH);
      attempts=0;
      if (expire_time>0) this.expire_time=expire_time;
      Message req=MessageFactory.createRegisterRequest(sip_provider,registrar,target,from,contact);
      req.setExpiresHeader(new ExpiresHeader(String.valueOf(expire_time)));
      if (next_nonce!=null)
      {  AuthorizationHeader ah=new AuthorizationHeader("Digest");
         SipURL target_url=target.getAddress();
         ah.addUsernameParam(username);
         ah.addRealmParam(realm);
         ah.addNonceParam(next_nonce);
         ah.addUriParam(req.getRequestLine().getAddress().toString());
         ah.addQopParam(qop);
         String response=(new DigestAuthentication(SipMethods.REGISTER,ah,null,passwd)).getResponse();
         ah.addResponseParam(response);
         req.setAuthorizationHeader(ah);
      }
      if (body!=null)
      {  printLog("register body: "+body.length()+" bytes",Log.LEVEL_HIGH);
         req.setBody(body);
      }
      if (expire_time>0) printLog("registering contact "+contact+" (it expires in "+expire_time+" secs)",Log.LEVEL_HIGH);
      else printLog("unregistering contact "+contact,Log.LEVEL_HIGH);
      TransactionClient t=new TransactionClient(sip_provider,req,this);
      t.request(); 
   }


   /** Unregisters all contacts with the registrar server.
     * It performs an unregistration (registration with 0 secs as expiration time) using '*' as contact address. */
   public void unregisterall()
   {  attempts=0;
      NameAddress user=new NameAddress(target);
      Message req=MessageFactory.createRegisterRequest(sip_provider,registrar,target,from,null);
      //ContactHeader contact_star=new ContactHeader(); // contact is *
      //req.setContactHeader(contact_star);
      req.setExpiresHeader(new ExpiresHeader(String.valueOf(0)));
      printLog("unregistering all contacts",Log.LEVEL_HIGH);
      TransactionClient t=new TransactionClient(sip_provider,req,this); 
      t.request(); 
   }


   /** Periodically registers with the registrar server.
     * @param expire_time expiration time in seconds
     * @param renew_time renew time in seconds */
   public void loopRegister(int expire_time, int renew_time)
   {  this.expire_time=expire_time;
      this.renew_time=renew_time;
      attempt_to=null;
      registration_to=null;
      loop=true;
      register(expire_time);
   }


   /** Periodically registers with the registrar server.
     * @param expire_time expiration time in seconds
     * @param renew_time renew time in seconds
     * @param keepalive_time keep-alive packet rate (inter-arrival time) in milliseconds */
   /*public void loopRegister(int expire_time, int renew_time, long keepalive_time)
   {  loopRegister(expire_time,renew_time);
      // keep-alive
      if (keepalive_time>0)
      {  SipURL target_url=target.getAddress();
         String target_host=target_url.getHost();
         int targe_port=target_url.getPort();
         if (targe_port<0) targe_port=SipStack.default_port;
         new SipKeepAlive(sip_provider,new SocketAddress(target_host,targe_port),null,keepalive_time);
      }
   }*/


   /** Halts the periodic registration. */
   public void halt()
   {  if (is_running) loop=false;
      //if (keep_alive!=null) keep_alive.halt();
   }

   
   // **************** Transaction callback functions *****************

   /** Callback function called when client sends back a provisional response. */
   public void onTransProvisionalResponse(TransactionClient transaction, Message resp)
   {  // do nothing..
   }

   /** Callback function called when client sends back a success response. */
   public void onTransSuccessResponse(TransactionClient transaction, Message resp)
   {  if (transaction.getTransactionMethod().equals(SipMethods.REGISTER))
      {  if (resp.hasAuthenticationInfoHeader())
         {  next_nonce=resp.getAuthenticationInfoHeader().getNextnonceParam();
         }
         StatusLine status=resp.getStatusLine();
         String result=status.getCode()+" "+status.getReason();
         
         // update the renew_time
         int expires=0;
         if (resp.hasExpiresHeader())
         {  expires=resp.getExpiresHeader().getDeltaSeconds();
         }
         else
         if (resp.hasContactHeader())
         {  Vector contacts=resp.getContacts().getHeaders();
            for (int i=0; i<contacts.size(); i++)
            {  int exp_i=(new ContactHeader((Header)contacts.elementAt(i))).getExpires();
               if (exp_i>0 && (expires==0 || exp_i<expires)) expires=exp_i;
            }    
         }
         if (expires>0 && expires<renew_time) renew_time=expires;
         
         printLog("Registration success: "+result,Log.LEVEL_HIGH);
         if (loop)
         {  attempt_to=null;
            (registration_to=new Timer((long)renew_time*1000,this)).start();
            printLog("next registration after "+renew_time+" secs",Log.LEVEL_LOW);
         }
         if (listener!=null) listener.onRegistrationSuccess(this,target,contact,result);
      }
   }

   /** Callback function called when client sends back a failure response. */
   public void onTransFailureResponse(TransactionClient transaction, Message resp)
   {  if (transaction.getTransactionMethod().equals(SipMethods.REGISTER))
      {  StatusLine status=resp.getStatusLine();
         int code=status.getCode();
         if (code==401 && attempts<SipStack.regc_auth_attempts && resp.hasWwwAuthenticateHeader() && resp.getWwwAuthenticateHeader().getRealmParam().equalsIgnoreCase(realm))
         {  // UAS authentication
            attempts++;
            Message req=transaction.getRequestMessage();
            CSeqHeader csh=req.getCSeqHeader().incSequenceNumber();
            req.setCSeqHeader(csh);
            ViaHeader vh=req.getViaHeader();
            req.removeViaHeader();
            vh.setBranch(SipProvider.pickBranch());
            req.addViaHeader(vh);
            WwwAuthenticateHeader wah=resp.getWwwAuthenticateHeader();
            String qop_options=wah.getQopOptionsParam();
            //printLog("DEBUG: qop-options: "+qop_options,Log.LEVEL_MEDIUM);
            qop=(qop_options!=null)? "auth" : null;
            AuthorizationHeader ah=(new DigestAuthentication(SipMethods.REGISTER,req.getRequestLine().getAddress().toString(),wah,qop,null,0,null,username,passwd)).getAuthorizationHeader();
            req.setAuthorizationHeader(ah);
            TransactionClient t=new TransactionClient(sip_provider,req,this);
            t.request();
         }
         else
         if (code==407 && attempts<SipStack.regc_auth_attempts && resp.hasProxyAuthenticateHeader() && resp.getProxyAuthenticateHeader().getRealmParam().equalsIgnoreCase(realm))
         {  // Proxy authentication
            attempts++;
            Message req=transaction.getRequestMessage();
            req.setCSeqHeader(req.getCSeqHeader().incSequenceNumber());
            ProxyAuthenticateHeader pah=resp.getProxyAuthenticateHeader();
            String qop_options=pah.getQopOptionsParam();
            //printLog("DEBUG: qop-options: "+qop_options,Log.LEVEL_MEDIUM);
            qop=(qop_options!=null)? "auth" : null;
            ProxyAuthorizationHeader ah=(new DigestAuthentication(SipMethods.REGISTER,req.getRequestLine().getAddress().toString(),pah,qop,null,0,null,username,passwd)).getProxyAuthorizationHeader();
            req.setProxyAuthorizationHeader(ah);
            TransactionClient t=new TransactionClient(sip_provider,req,this);
            t.request();
         }
         else
         {  // Registration failure
            String result=code+" "+status.getReason();
            printLog("Registration failure: "+result,Log.LEVEL_HIGH);
            if (loop)
            {  registration_to=null;
               (attempt_to=new Timer(SipStack.regc_max_attempt_timeout,this)).start();
               printLog("next attempt after "+(SipStack.regc_max_attempt_timeout/1000)+" secs",Log.LEVEL_LOW);
            }
            if (listener!=null) listener.onRegistrationFailure(this,target,contact,result);
         }
      }
   }

   /** Callback function called when client expires timeout. */
   public void onTransTimeout(TransactionClient transaction)
   {  if (transaction.getTransactionMethod().equals(SipMethods.REGISTER))
      {  printLog("Registration failure: No response from server",Log.LEVEL_HIGH);
         if (loop)
         {  registration_to=null;
            long inter_time_msecs=(attempt_to==null)? SipStack.regc_min_attempt_timeout : attempt_to.getTime()*2;
            if (inter_time_msecs>SipStack.regc_max_attempt_timeout) inter_time_msecs=SipStack.regc_max_attempt_timeout;
            (attempt_to=new Timer(inter_time_msecs,this)).start();
            printLog("next attempt after "+(inter_time_msecs/1000)+" secs",Log.LEVEL_LOW);
         }
         if (listener!=null) listener.onRegistrationFailure(this,target,contact,"Timeout");
      }
   }


   // ******************* Timer callback functions ********************

   /** When the Timer exceeds. */
   public void onTimeout(Timer t)
   {  if ((t==attempt_to || t==registration_to) && loop)
      {  register();
      }
   }


   // ***************************** run() *****************************

   /** Run method */
   public void run()
   {  
      is_running=true;
      try
      {  while (loop)
         {  register();
            Thread.sleep(renew_time*1000);
         }
      }
      catch (Exception e) {  printException(e,Log.LEVEL_HIGH);  }
      is_running=false;
   }

   
   // ****************************** Logs *****************************

   /** Adds a new string to the default Log */
   void printLog(String str, int level)
   {  if (log!=null) log.println("RegistrationClient: "+str,Call.LOG_OFFSET+level);  
   }

   /** Adds the Exception message to the default Log */
   void printException(Exception e,int level)
   {  printLog("Exception: "+ExceptionPrinter.getStackTraceOf(e),level);
   }

}
