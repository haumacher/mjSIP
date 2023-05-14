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

package org.mjsip.sip.call;



import java.util.Vector;

import org.mjsip.sip.address.GenericURI;
import org.mjsip.sip.address.NameAddress;
import org.mjsip.sip.address.SipNameAddress;
import org.mjsip.sip.address.SipURI;
import org.mjsip.sip.authentication.DigestAuthentication;
import org.mjsip.sip.header.AuthorizationHeader;
import org.mjsip.sip.header.CSeqHeader;
import org.mjsip.sip.header.ContactHeader;
import org.mjsip.sip.header.ExpiresHeader;
import org.mjsip.sip.header.Header;
import org.mjsip.sip.header.ProxyAuthenticateHeader;
import org.mjsip.sip.header.ProxyAuthorizationHeader;
import org.mjsip.sip.header.StatusLine;
import org.mjsip.sip.header.ViaHeader;
import org.mjsip.sip.header.WwwAuthenticateHeader;
import org.mjsip.sip.message.SipMessage;
import org.mjsip.sip.message.SipMessageFactory;
import org.mjsip.sip.message.SipMethods;
import org.mjsip.sip.provider.SipProvider;
import org.mjsip.sip.provider.SipStack;
import org.mjsip.sip.transaction.TransactionClient;
import org.mjsip.sip.transaction.TransactionClientListener;
import org.zoolu.util.ExceptionPrinter;
//import org.mjsip.sip.provider.SipKeepAlive;
import org.zoolu.util.LogLevel;
import org.zoolu.util.Logger;
import org.zoolu.util.Timer;
import org.zoolu.util.TimerListener;



/** RegistrationClient does register (one time or periodically)
  * a contact address with a registrar server.
  */
public class RegistrationClient implements TransactionClientListener, TimerListener {
	
	/** Logger */
	protected Logger logger;

	/** RegistrationClient listener */
	protected RegistrationClientListener listener;
		
	/** SipProvider */
	protected SipProvider sip_provider;

	/** URI of the registrar server */
	protected SipURI registrar_uri;

	/** Target AOR to be registered with the registrar server (the URI in the To header field) */
	protected NameAddress to_naddr;

	/** URI of the user that is actually performing the registration (the URI in the From header field) */
	protected NameAddress from_naddr;

	/** User name. */
	protected String username=null;

	/** User name. */
	protected String realm=null;

	/** User's passwd. */
	protected String passwd=null;

	/** Nonce for the next authentication. */
	protected String next_nonce=null;

	/** Qop for the next authentication. */
	protected String qop=null;

	/** User's contact name address. */
	protected NameAddress contact_naddr; 

	/** Expiration time. */
	protected int expire_time;

	/** Renew time. */
	protected int renew_time;

	/** Attempt timeout */
	Timer attempt_to;

	/** Registration timeout */
	Timer registration_to;

	/** Whether keep on registering. */
	boolean loop=false;

	/** Whether the thread is running. */
	boolean is_running=false;

	/** Number of registration attempts. */
	int attempts=0;
	
	/** SipKeepAlive daemon. */
	//SipKeepAlive keep_alive=null;

		
	/** Creates a new RegistrationClient.
	  * <p>
	  * The From URI is equal to the To URI. The Contact URI is automatically formed with the user name from the AOR and the address:port from the SIP provider.
	  * If a secure transport is preset (e.g. TLS), a SIPS URI is registered as contact URI.
	  * @param sip_provider the SIP provider
	  * @param registrar the registrar server
	  * @param to_naddr the AOR of the resource that has to be registered (the URI in the To header field)
	  * @param listener the RegistrationClient listener */
	public RegistrationClient(SipProvider sip_provider, SipURI registrar, NameAddress to_naddr, RegistrationClientListener listener) {
		init(sip_provider,registrar,to_naddr,to_naddr,null,listener);
	}
	
	
	/** Creates a new RegistrationClient.
	  * <p>
	  * The Contact URI is automatically formed with the user name from the AOR and the address:port from the SIP provider.
	  * If a secure transport is preset (e.g. TLS), a SIPS URI is registered as contact URI.
	  * @param sip_provider the SIP provider
	  * @param registrar the registrar server
	  * @param to_naddr the AOR of the resource that has to be registered (the URI in the To header field)
	  * @param from_naddr the URI of the registering UA (the URI in the From header field)
	  * @param listener the RegistrationClient listener */
	/*public RegistrationClient(SipProvider sip_provider, SipURI registrar, NameAddress to_naddr, NameAddress from_naddr, RegistrationClientListener listener) {
		init(sip_provider,registrar,to_naddr,from_naddr,null,listener);
	}*/
	
	
	/** Creates a new RegistrationClient.
	  * @param sip_provider the SIP provider
	  * @param registrar the registrar server
	  * @param to_naddr the AOR of the resource that has to be registered (the URI in the To header field)
	  * @param from_naddr the URI of the registering UA (the URI in the From header field)
	  * @param contact_naddr the registered contact URI
	  * @param listener the RegistrationClient listener */
	public RegistrationClient(SipProvider sip_provider, SipURI registrar, NameAddress to_naddr, NameAddress from_naddr, NameAddress contact_naddr, RegistrationClientListener listener) {
		init(sip_provider,registrar,to_naddr,from_naddr,contact_naddr,listener);
	}
	
	
	/** Creates a new RegistrationClient with authentication credentials (i.e. username, realm, and passwd).
	  * <p>
	  * The From URI is equal to the To URI. The Contact URI is automatically formed with the user name from the AOR and the address:port from the SIP provider.
	  * If a secure transport is preset (e.g. TLS), a SIPS URI is registered as contact URI.
	  * @param sip_provider the SIP provider
	  * @param registrar the registrar server
	  * @param to_naddr the AOR of the resource that has to be registered (the URI in the To header field)
	  * @param username the username for authentication
	  * @param realm the realm for authentication
	  * @param passwd the password for authentication
	  * @param listener the RegistrationClient listener */
	public RegistrationClient(SipProvider sip_provider, SipURI registrar, NameAddress to_naddr, String username, String realm, String passwd, RegistrationClientListener listener) {
		init(sip_provider,registrar,to_naddr,to_naddr,null,username,realm,passwd,listener);
	}


	/** Creates a new RegistrationClient with authentication credentials (i.e. username, realm, and passwd).
	  * <p>
	  * The Contact URI is automatically formed with the user name from the AOR and the address:port from the SIP provider.
	  * If a secure transport is preset (e.g. TLS), a SIPS URI is registered as contact URI.
	  * @param sip_provider the SIP provider
	  * @param registrar the registrar server
	  * @param to_naddr the AOR of the resource that has to be registered (the URI in the To header field)
	  * @param from_naddr the URI of the registering UA (the URI in the From header field)
	  * @param username the username for authentication
	  * @param realm the realm for authentication
	  * @param passwd the password for authentication
	  * @param listener the RegistrationClient listener */
	/*public RegistrationClient(SipProvider sip_provider, SipURI registrar, NameAddress to_naddr, NameAddress from_naddr, String username, String realm, String passwd, RegistrationClientListener listener) {
		init(sip_provider,registrar,to_naddr,from_naddr,null,username,realm,passwd,listener);
	}*/


	/** Creates a new RegistrationClient with authentication credentials (i.e. username, realm, and passwd).
	  * @param sip_provider the SIP provider
	  * @param registrar the registrar server
	  * @param to_naddr the AOR of the resource that has to be registered (the URI in the To header field)
	  * @param from_naddr the URI of the registering UA (the URI in the From header field)
	  * @param contact_naddr the registered contact URI
	  * @param username the username for authentication
	  * @param realm the realm for authentication
	  * @param passwd the password for authentication
	  * @param listener the RegistrationClient listener */
	public RegistrationClient(SipProvider sip_provider, SipURI registrar, NameAddress to_naddr, NameAddress from_naddr, NameAddress contact_naddr, String username, String realm, String passwd, RegistrationClientListener listener) {
		init(sip_provider,registrar,to_naddr,from_naddr,contact_naddr,username,realm,passwd,listener);
	}


	/** Inits the RegistrationClient.
	  * @param sip_provider the SIP provider
	  * @param registrar the registrar server
	  * @param to_naddr the AOR of the resource that has to be registered (the URI in the To header field)
	  * @param from_naddr the URI of the registering UA (the URI in the From header field)
	  * @param contact_naddr the registered contact URI
	  * @param username the username for authentication
	  * @param realm the realm for authentication
	  * @param passwd the password for authentication
	  * @param listener the RegistrationClient listener */
	private void init(SipProvider sip_provider, SipURI registrar, NameAddress to_naddr, NameAddress from_naddr, NameAddress contact_naddr, String username, String realm, String passwd, RegistrationClientListener listener) {
		init(sip_provider,registrar,to_naddr,from_naddr,contact_naddr,listener);
		// authentication
		this.username=username;
		this.realm=realm;
		this.passwd=passwd;
	}


	/** Inits the RegistrationClient.
	  * @param sip_provider the SIP provider
	  * @param registrar_uri the registrar server
	  * @param to_naddr the AOR of the resource that has to be registered (the URI in the To header field)
	  * @param from_naddr the URI of the registering UA (the URI in the From header field)
	  * @param contact_naddr the registered contact URI
	  * @param listener the RegistrationClient listener */
	private void init(SipProvider sip_provider, SipURI registrar_uri, NameAddress to_naddr, NameAddress from_naddr, NameAddress contact_naddr, RegistrationClientListener listener) {
		this.listener=listener;
		this.sip_provider=sip_provider;
		this.logger=sip_provider.getLogger();
		this.registrar_uri=registrar_uri;
		if (contact_naddr==null) {
			GenericURI to_uri=to_naddr.getAddress();
			String user=(to_uri.isSipURI())? new SipURI(to_uri).getUserName() : null;
			contact_naddr=new NameAddress(sip_provider.getContactAddress(user));
		}
		if (SipNameAddress.isSIPS(contact_naddr)) {
			// change scheme of to-uri, from-uri, and request-uri to SIPS
			to_naddr=SipNameAddress.toSIPS(to_naddr);
			from_naddr=SipNameAddress.toSIPS(from_naddr);
			registrar_uri.setSecure(true);
		}
		this.to_naddr=to_naddr;
		this.from_naddr=from_naddr;
		this.contact_naddr=contact_naddr;

		this.expire_time=SipStack.default_expires;
		this.renew_time=SipStack.default_expires;
	}


	/** Gets the target AOR registered with the registrar server.
	  * @return the AOR */
	public NameAddress getTargetAOR() {
		return to_naddr;
	}


	/** Whether it is periodically registering. */
	public boolean isRegistering() {
		return is_running;
	}


	/** Registers with the registrar server.
	  * It does register with the previously set expire time.  */
	public void register() {
		register(expire_time);
	}


	/** Unregister with the registrar server.
	  * It does register with expire time = 0. */
	public void unregister() {
		register(0);
	} 


	/** Registers with the registrar server for <i>expire_time</i> seconds. */
	public void register(int expire_time) {
		register(expire_time,null,null);
	}


	/** Registers with the registrar server for <i>expire_time</i> seconds, with a given message body. */
	protected void register(int expire_time, String content_type, byte[] body) {
		log(LogLevel.INFO,"register with "+registrar_uri+" for "+expire_time+" secs");
		attempts=0;
		if (expire_time>0) this.expire_time=expire_time;
		String call_id=sip_provider.pickCallId();
		SipMessage req=SipMessageFactory.createRegisterRequest(registrar_uri,to_naddr,from_naddr,contact_naddr,call_id);
		req.setExpiresHeader(new ExpiresHeader(String.valueOf(expire_time)));
		if (next_nonce!=null) {
			AuthorizationHeader ah=new AuthorizationHeader("Digest");
			//GenericURI to_uri=to_naddr.getAddress();
			ah.addUsernameParam(username);
			ah.addRealmParam(realm);
			ah.addNonceParam(next_nonce);
			ah.addUriParam(req.getRequestLine().getAddress().toString());
			ah.addQopParam(qop);
			String response=(new DigestAuthentication(SipMethods.REGISTER,ah,null,passwd)).getResponse();
			ah.addResponseParam(response);
			req.setAuthorizationHeader(ah);
		}
		if (body!=null) {
			log(LogLevel.INFO,"register body type: "+content_type+"; length: "+body.length+" bytes");
			req.setBody(content_type,body);
		}
		if (expire_time>0) log(LogLevel.INFO,"registering contact "+contact_naddr+" (it expires in "+expire_time+" secs)");
		else log(LogLevel.INFO,"unregistering contact "+contact_naddr);
		TransactionClient t=new TransactionClient(sip_provider,req,this);
		t.request(); 
	}


	/** Unregisters all contacts with the registrar server.
	  * It performs an unregistration (registration with 0 secs as expiration time) using '*' as contact address. */
	public void unregisterall() {
		attempts=0;
		NameAddress user=new NameAddress(to_naddr);
		String call_id=sip_provider.pickCallId();
		SipMessage req=SipMessageFactory.createRegisterRequest(registrar_uri,to_naddr,from_naddr,(NameAddress)null,call_id);
		//ContactHeader contact_star=new ContactHeader(); // contact is *
		//req.setContactHeader(contact_star);
		req.setExpiresHeader(new ExpiresHeader(String.valueOf(0)));
		log(LogLevel.INFO,"unregistering all contacts");
		TransactionClient t=new TransactionClient(sip_provider,req,this); 
		t.request(); 
	}


	/** Periodically registers with the registrar server.
	  * @param expire_time expiration time in seconds
	  * @param renew_time renew time in seconds */
	public void loopRegister(int expire_time, int renew_time) {
		this.expire_time=expire_time;
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
	/*public void loopRegister(int expire_time, int renew_time, long keepalive_time) {
		loopRegister(expire_time,renew_time);
		// keep-alive
		if (keepalive_time>0) {
			SipURI to_uri=to_naddr.getAddress();
			String host=to_uri.getHost();
			int port=to_uri.getPort();
			if (port<0) port=SipStack.default_port;
			new SipKeepAlive(sip_provider,new SocketAddress(host,port),null,keepalive_time);
		}
	}*/


	/** Halts the periodic registration. */
	public void halt() {
		if (is_running) loop=false;
		//if (keep_alive!=null) keep_alive.halt();
	}

	
	// **************** Transaction callback functions *****************

	/** Callback function called when client sends back a provisional response. */
	public void onTransProvisionalResponse(TransactionClient transaction, SipMessage resp) {
		// do nothing..
	}

	/** Callback function called when client sends back a success response. */
	public void onTransSuccessResponse(TransactionClient transaction, SipMessage resp) {
		if (transaction.getTransactionMethod().equals(SipMethods.REGISTER)) {
			if (resp.hasAuthenticationInfoHeader()) {
				next_nonce=resp.getAuthenticationInfoHeader().getNextnonceParam();
			}
			StatusLine status=resp.getStatusLine();
			String result=status.getCode()+" "+status.getReason();
			
			// update the renew_time
			int expires=0;
			if (resp.hasExpiresHeader()) {
				expires=resp.getExpiresHeader().getDeltaSeconds();
			}
			else
			if (resp.hasContactHeader()) {
				Vector contacts=resp.getContacts().getHeaders();
				for (int i=0; i<contacts.size(); i++) {
					int exp_i=(new ContactHeader((Header)contacts.elementAt(i))).getExpires();
					if (exp_i>0 && (expires==0 || exp_i<expires)) expires=exp_i;
				}    
			}
			if (expires>0 && expires<renew_time) renew_time=expires;
			
			log(LogLevel.INFO,"Registration success: expires: "+expires+"s: "+result);
			if (loop) {
				attempt_to=null;
				(registration_to=new Timer((long)renew_time*1000,this)).start();
				log(LogLevel.TRACE,"next registration after "+renew_time+" secs");
			}
			if (listener!=null) listener.onRegistrationSuccess(this,to_naddr,contact_naddr,expires,result);
		}
	}

	/** Callback function called when client sends back a failure response. */
	public void onTransFailureResponse(TransactionClient transaction, SipMessage resp) {
		if (transaction.getTransactionMethod().equals(SipMethods.REGISTER)) {
			StatusLine status=resp.getStatusLine();
			int code=status.getCode();
			if (code==401 && attempts<SipStack.regc_auth_attempts && resp.hasWwwAuthenticateHeader() && resp.getWwwAuthenticateHeader().getRealmParam().equalsIgnoreCase(realm)) {
				// UAS authentication
				attempts++;
				SipMessage req=transaction.getRequestMessage();
				CSeqHeader csh=req.getCSeqHeader().incSequenceNumber();
				req.setCSeqHeader(csh);
				ViaHeader vh=req.getViaHeader();
				req.removeViaHeader();
				vh.setBranch(SipProvider.pickBranch());
				req.addViaHeader(vh);
				WwwAuthenticateHeader wah=resp.getWwwAuthenticateHeader();
				String qop_options=wah.getQopOptionsParam();
				//log(LogLevel.DEBUG,"qop-options: "+qop_options);
				qop=(qop_options!=null)? "auth" : null;
				AuthorizationHeader ah=(new DigestAuthentication(SipMethods.REGISTER,req.getRequestLine().getAddress().toString(),wah,qop,null,0,null,username,passwd)).getAuthorizationHeader();
				req.setAuthorizationHeader(ah);
				TransactionClient t=new TransactionClient(sip_provider,req,this);
				t.request();
			}
			else
			if (code==407 && attempts<SipStack.regc_auth_attempts && resp.hasProxyAuthenticateHeader() && resp.getProxyAuthenticateHeader().getRealmParam().equalsIgnoreCase(realm)) {
				// Proxy authentication
				attempts++;
				SipMessage req=transaction.getRequestMessage();
				req.setCSeqHeader(req.getCSeqHeader().incSequenceNumber());
				ProxyAuthenticateHeader pah=resp.getProxyAuthenticateHeader();
				String qop_options=pah.getQopOptionsParam();
				//log(LogLevel.DEBUG,"qop-options: "+qop_options);
				qop=(qop_options!=null)? "auth" : null;
				ProxyAuthorizationHeader ah=(new DigestAuthentication(SipMethods.REGISTER,req.getRequestLine().getAddress().toString(),pah,qop,null,0,null,username,passwd)).getProxyAuthorizationHeader();
				req.setProxyAuthorizationHeader(ah);
				TransactionClient t=new TransactionClient(sip_provider,req,this);
				t.request();
			}
			else {
				// Registration failure
				String result=code+" "+status.getReason();
				log(LogLevel.INFO,"Registration failure: "+result);
				if (loop) {
					registration_to=null;
					(attempt_to=new Timer(SipStack.regc_max_attempt_timeout,this)).start();
					log(LogLevel.TRACE,"next attempt after "+(SipStack.regc_max_attempt_timeout/1000)+" secs");
				}
				if (listener!=null) listener.onRegistrationFailure(this,to_naddr,contact_naddr,result);
			}
		}
	}

	/** Callback function called when client expires timeout. */
	public void onTransTimeout(TransactionClient transaction) {
		if (transaction.getTransactionMethod().equals(SipMethods.REGISTER)) {
			log(LogLevel.INFO,"Registration failure: No response from server");
			if (loop) {
				registration_to=null;
				long inter_time_msecs=(attempt_to==null)? SipStack.regc_min_attempt_timeout : attempt_to.getTime()*2;
				if (inter_time_msecs>SipStack.regc_max_attempt_timeout) inter_time_msecs=SipStack.regc_max_attempt_timeout;
				(attempt_to=new Timer(inter_time_msecs,this)).start();
				log(LogLevel.TRACE,"next attempt after "+(inter_time_msecs/1000)+" secs");
			}
			if (listener!=null) listener.onRegistrationFailure(this,to_naddr,contact_naddr,"Timeout");
		}
	}


	// ******************* Timer callback functions ********************

	/** When the Timer exceeds. */
	public void onTimeout(Timer t) {
		if ((t==attempt_to || t==registration_to) && loop) {
			register();
		}
	}


	// ***************************** run() *****************************

	/** Run method */
	public void run() {
		
		is_running=true;
		try {
			while (loop) {
				register();
				Thread.sleep(renew_time*1000);
			}
		}
		catch (Exception e) {  log(LogLevel.INFO,e);  }
		is_running=false;
	}

	
	// ****************************** Logs *****************************

	/** Adds a new string to the default log. */
	void log(LogLevel level, String str) {
		if (logger!=null) logger.log("RegistrationClient: "+str);  
	}

	/** Adds the Exception message to the default log. */
	void log(LogLevel level, Exception e) {
		log(level,"Exception: "+ExceptionPrinter.getStackTraceOf(e));
	}

}
