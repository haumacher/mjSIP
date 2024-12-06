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

package org.mjsip.server;


import java.util.Date;
import java.util.Enumeration;
import java.util.Vector;

import org.mjsip.config.MetaConfig;
import org.mjsip.config.OptionParser;
import org.mjsip.sip.address.GenericURI;
import org.mjsip.sip.address.NameAddress;
import org.mjsip.sip.address.SipURI;
import org.mjsip.sip.header.ContactHeader;
import org.mjsip.sip.header.ExpiresHeader;
import org.mjsip.sip.header.Header;
import org.mjsip.sip.header.MultipleHeader;
import org.mjsip.sip.header.ToHeader;
import org.mjsip.sip.message.SipMessage;
import org.mjsip.sip.message.SipResponses;
import org.mjsip.sip.provider.SipConfig;
import org.mjsip.sip.provider.SipProvider;
import org.mjsip.sip.transaction.TransactionServer;
import org.mjsip.time.ConfiguredScheduler;
import org.mjsip.time.SchedulerConfig;
import org.slf4j.LoggerFactory;
import org.zoolu.util.DateFormat;


/** Class Registrar implements a Registrar SIP Server.
  * It extends class ServerEngine.
  */
public class Registrar extends ServerEngine {
	
	private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(Registrar.class);
	
	/** Costructs a void Registrar. */
	protected Registrar() {}
	
	
	/** Costructs a new Registrar. The Location Service is stored within the file <i>db_name</i> */
	//public Registrar(SipProvider provider, String db_class, String db_name)
	public Registrar(SipProvider provider, ServerProfile profile) {
		super(provider,profile);
	}


	/** When a new request is received for the local server. */
	@Override
	public void processRequestToLocalServer(SipMessage msg) {
		LOG.debug("inside processRequestToLocalServer(msg)");
		if (server_profile.isRegistrar && msg.isRegister()) {
			TransactionServer t=new TransactionServer(sip_provider,msg,null);
	
			/*if (server_profile.do_authentication) {
				// check message authentication
				SipMessage err_resp=as.authenticateRequest(msg);  
				if (err_resp!=null) {
					t.respondWith(err_resp);
					return;
				}
			}*/
			
			SipMessage resp=updateRegistration(msg);
			if (resp==null) return;
			
			if (server_profile.doAuthentication) {
				// add Authentication-Info header field
				resp.setAuthenticationInfoHeader(as.getAuthenticationInfoHeader());
			}
			
			t.respondWith(resp);
		} else if (!msg.isAck()) {
			// send a stateless error response
			SipMessage resp=sip_provider.messageFactory().createResponse(msg,SipResponses.NOT_IMPLEMENTED,null,null);
			sip_provider.sendMessage(resp);
		}     
	}

	/** When a new request message is received for a local user. */
	@Override
	public void processRequestToLocalUser(SipMessage msg) {
		LOG.debug("inside processRequestToLocalUser(msg)");
		// stateless-response (in order to avoid DoS attacks)
		if (!msg.isAck()) sip_provider.sendMessage(sip_provider.messageFactory().createResponse(msg,SipResponses.NOT_FOUND,null,null));
		else LOG.info("message discarded");
	}
 
	
	/** When a new request message is received for a remote UA. */
	@Override
	public void processRequestToRemoteUA(SipMessage msg) {
		// stateless-response (in order to avoid DoS attacks)
		if (msg.isAck()) {
			// Ignore.
		} else {
			LOG.info("Ignoring proxy request to: {}", msg.getToHeader().getValue());
			sip_provider.sendMessage(sip_provider.messageFactory().createResponse(msg,SipResponses.NOT_FOUND,null,null));
		}
	}


	/** When a new response message is received. */
	@Override
	public void processResponse(SipMessage resp) {
		LOG.debug("inside processResponse(msg)");
		// no actions..
		LOG.info("message discarded");
	}
	
	
	// *********************** protected methods ***********************

	/** Gets the request's targets.
	  * @return a vector of target URIs (Vector of <code>String</code>). */
	protected Vector<String>  getTargets(SipMessage msg) {
		LOG.trace("inside getTargets(msg)");

		Vector<String> targets=new Vector<>();
		
		if (location_service==null) {
			LOG.info("Location service is not active");
			return targets;
		}           

		GenericURI request_uri=msg.getRequestLine().getAddress();
		if (!request_uri.isSipURI()) {
			LOG.info("request-URI is not a SIP URI");
			return targets;
		}
		SipURI sip_uri=request_uri.toSipURI();
		String username=sip_uri.getUserName();
		if (username==null) {
			LOG.info("no username found");
			return targets;
		}
		String user=username+"@"+sip_uri.getHost();
		LOG.debug("user: {}", user); 
			  
		if (!location_service.hasUser(user)) {
			LOG.info("user {} not found", user);
			return targets;
		}

		GenericURI to_uri=msg.getToHeader().getNameAddress().getAddress();
		
		Enumeration<String> e=location_service.getUserContactURIs(user);
		LOG.trace("message targets: ");  
		for (int i=0; e.hasMoreElements(); i++) {
			// if exipred, remove the contact URI
			String contact= e.nextElement();
			if (location_service.isUserContactExpired(user,contact)) {
				location_service.removeUserContact(user,contact);
			LOG.trace("target {} expired: contact URI removed", i);
			}
			// otherwise add the URI to the target list
			else {
				targets.addElement(contact);
				LOG.trace("target {}={}", i, targets.elementAt(i));
			}
		}
		// for SIPS request-uri remove non-SIPS targets
		if (request_uri.equals(GenericURI.SCHEME_SIPS)) {
			for (int i=0; i<targets.size(); i++) {
				SipURI uri=SipURI.parseSipURI(targets.elementAt(i));
				if (!uri.isSecure()) {
					LOG.info("{} has not SIPS scheme: skipped", uri.toString());
					targets.removeElementAt(i--);
				}
			}
		}
		return targets;
	}


	/** Updates the registration of a local user.
	  * @return it returns the response message for the registration. */
	protected SipMessage updateRegistration(SipMessage msg) {
		ToHeader th=msg.getToHeader();
		if (th==null)   {
			LOG.info("ToHeader missed: message discarded");
			return sip_provider.messageFactory().createResponse(msg,SipResponses.BAD_REQUEST,null,null);  
		}         
		SipURI dest_sip_uri=th.getNameAddress().getAddress().toSipURI();
		String user=dest_sip_uri.getUserName()+"@"+dest_sip_uri.getHost();

		int exp_secs=server_profile.expires;
		// set the expire value
		ExpiresHeader eh=msg.getExpiresHeader();
		if (eh!=null) {
			exp_secs=eh.getDeltaSeconds();
		}
		// limit the expire value
		if (exp_secs<0) exp_secs=0;
		else
		if (exp_secs>server_profile.expires) exp_secs=server_profile.expires;

		// known user?
		if (!location_service.hasUser(user)) {
			if (server_profile.registerNewUsers) {
				location_service.addUser(user);
				LOG.info("new user '{}' added", user);
			} 
			else {
				LOG.info("user '{}' unknown: message discarded.", user);
				return sip_provider.messageFactory().createResponse(msg,SipResponses.NOT_FOUND,null,null);  
			}
		}

		// Get the "device" parameter. Set device=null if not present or not supported
		//String device=null;
		//if (msg.hasApplicationHeader()) app=msg.getApplicationHeader().getApplication();
		//SipURI to_uri=msg.getToHeader().getNameAddress().getAddress();
		//if (to_uri.hasParameter("device")) device=to_uri.getParameter("device");

		if (!msg.hasContactHeader())   {
			//LOG.info("ContactHeader missed: message discarded");
			//int result=484;
			//return SipMessageFactory.createResponse(msg,result,null,null,null);  
			LOG.debug("no contact found: fetching bindings..");
			SipMessage resp=sip_provider.messageFactory().createResponse(msg,SipResponses.OK,null,null);  
			// add current contacts
			Vector<Header> v=new Vector<>();
			for (Enumeration<String> e=location_service.getUserContactURIs(user); e.hasMoreElements(); ) {
				String contact= e.nextElement();
				int expires=(int)(location_service.getUserContactExpirationDate(user,contact).getTime()-System.currentTimeMillis())/1000;
				if (expires>0) {
					// not expired
					ContactHeader ch=new ContactHeader(location_service.getUserContactNameAddress(user,contact));
					ch.setExpires(expires);
					v.addElement(ch);
				}
			}
			if (!v.isEmpty()) resp.setContacts(new MultipleHeader(v));
			return resp;
		}
		// else     

		Vector<Header> contacts=msg.getContacts().getHeaders();
		SipMessage resp=sip_provider.messageFactory().createResponse(msg,SipResponses.OK,null,null);  

		ContactHeader ch_0=new ContactHeader(contacts.elementAt(0));
		if (ch_0.isStar()) {
			LOG.trace("ContactHeader is star");
			Vector<Header> resp_contacts=new Vector<>();
			for (Enumeration<String> e=location_service.getUserContactURIs(user); e.hasMoreElements();)  {
				String contact= e.nextElement();
				if (!location_service.isUserContactStatic(user,contact))  {
					NameAddress name_address=location_service.getUserContactNameAddress(user,contact);
					// update db
					location_service.removeUserContact(user,contact);
					LOG.trace("contact removed: {}", contact);
					if (exp_secs>0) {
						Date exp_date=new Date(System.currentTimeMillis()+((long)exp_secs)*1000);
						location_service.addUserContact(user,name_address,exp_date);
						//DateFormat df=new SimpleDateFormat("EEE, dd MMM yyyy hh:mm:ss 'GMT'",Locale.ITALIAN);
						//printLog("contact added: "+uri+"; expire: "+df.format(location_service.getUserContactExpire(user,url)),LogWriter.LEVEL_LOW);
						LOG.trace("contact added: {}; expire: {}", contact, DateFormat.formatEEEddMMMyyyyhhmmss(location_service.getUserContactExpirationDate(user,contact)));
					}
					ContactHeader ch_i=new ContactHeader(name_address.getAddress());
					ch_i.setExpires(exp_secs);
					resp_contacts.addElement(ch_i);
				}
			}
			if (!resp_contacts.isEmpty()) resp.setContacts(new MultipleHeader(resp_contacts));
		}
		else {
			Vector<Header> resp_contacts=new Vector<>();
			for (int i=0; i<contacts.size(); i++)      {
				ContactHeader ch_i=new ContactHeader(contacts.elementAt(i));
				NameAddress name_address=ch_i.getNameAddress();     
				String contact=name_address.getAddress().toString();     
				int exp_secs_i=exp_secs;
				if (ch_i.hasExpires())  {
					exp_secs_i=ch_i.getExpires();
				}
				// limit the expire value
				if (exp_secs_i<0) exp_secs_i=0;
				else
				if (exp_secs_i>server_profile.expires) exp_secs_i=server_profile.expires;
								
				// update db
				location_service.removeUserContact(user,contact);
				if (exp_secs_i>0) {
					Date exp_date=new Date(System.currentTimeMillis()+((long)exp_secs)*1000);
					location_service.addUserContact(user,name_address,exp_date);
					LOG.info("registration of user '{}' updated", user);
				}           
				ch_i.setExpires(exp_secs_i);
				resp_contacts.addElement(ch_i);
			}
			if (!resp_contacts.isEmpty()) resp.setContacts(new MultipleHeader(resp_contacts));
		}

		location_service.sync();  
		return resp;
	}


	// ****************************** MAIN *****************************

	/** The main method. */
	public static void main(String[] args) {
		SipConfig sipConfig = new SipConfig();
		SchedulerConfig schedulerConfig = new SchedulerConfig();
		ServerProfile server_profile=new ServerProfile();

		MetaConfig metaConfig = OptionParser.parseOptions(args, ".mjsip-registrar", sipConfig, schedulerConfig, server_profile);
		
		sipConfig.normalize();
		server_profile.normalize();
			
		SipProvider sip_provider=new SipProvider(sipConfig, new ConfiguredScheduler(schedulerConfig));
		
		new Registrar(sip_provider,server_profile);
	}
}
