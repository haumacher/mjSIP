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
import org.mjsip.sip.provider.SipConfig;
import org.mjsip.sip.provider.SipProvider;
import org.mjsip.sip.transaction.TransactionServer;
import org.mjsip.time.Scheduler;
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
		if (server_profile.is_registrar && msg.isRegister()) {
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
			
			if (server_profile.do_authentication) {
				// add Authentication-Info header field
				resp.setAuthenticationInfoHeader(as.getAuthenticationInfoHeader());
			}
			
			t.respondWith(resp);
		}
		else
		if (!msg.isAck()) {
			// send a stateless error response
			int result=501; // response code 501 ("Not Implemented")
			SipMessage resp=sip_provider.messageFactory().createResponse(msg,result,null,null);
			sip_provider.sendMessage(resp);
		}     
	}


	/** When a new request message is received for a local user. */
	@Override
	public void processRequestToLocalUser(SipMessage msg) {
		LOG.debug("inside processRequestToLocalUser(msg)");
		// stateless-response (in order to avoid DoS attacks)
		if (!msg.isAck()) sip_provider.sendMessage(sip_provider.messageFactory().createResponse(msg,404,null,null));
		else LOG.info("message discarded");
	}
 
	
	/** When a new request message is received for a remote UA. */
	@Override
	public void processRequestToRemoteUA(SipMessage msg) {
		LOG.debug("inside processRequestToRemoteUA(msg)");
		// stateless-response (in order to avoid DoS attacks)
		if (!msg.isAck()) sip_provider.sendMessage(sip_provider.messageFactory().createResponse(msg,404,null,null));
		else LOG.info("message discarded");
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
	protected Vector getTargets(SipMessage msg) {
		LOG.trace("inside getTargets(msg)");

		Vector targets=new Vector();
		
		if (location_service==null) {
			LOG.info("Location service is not active");
			return targets;
		}           

		GenericURI request_uri=msg.getRequestLine().getAddress();
		if (!request_uri.isSipURI()) {
			LOG.info("request-URI is not a SIP URI");
			return targets;
		}
		SipURI sip_uri=new SipURI(request_uri);
		String username=sip_uri.getUserName();
		if (username==null) {
			LOG.info("no username found");
			return targets;
		}
		String user=username+"@"+sip_uri.getHost();
		LOG.debug("user: "+user); 
			  
		if (!location_service.hasUser(user)) {
			LOG.info("user "+user+" not found");
			return targets;
		}

		GenericURI to_uri=msg.getToHeader().getNameAddress().getAddress();
		
		Enumeration e=location_service.getUserContactURIs(user);
		LOG.trace("message targets: ");  
		for (int i=0; e.hasMoreElements(); i++) {
			// if exipred, remove the contact URI
			String contact=(String)e.nextElement();
			if (location_service.isUserContactExpired(user,contact)) {
				location_service.removeUserContact(user,contact);
			LOG.trace("target"+i+" expired: contact URI removed");
			}
			// otherwise add the URI to the target list
			else {
				targets.addElement(contact);
				LOG.trace("target"+i+"="+targets.elementAt(i));
			}
		}
		// for SIPS request-uri remove non-SIPS targets
		if (request_uri.equals(GenericURI.SCHEME_SIPS)) {
			for (int i=0; i<targets.size(); i++) {
				SipURI uri=new SipURI((String)targets.elementAt(i));
				if (!uri.isSecure()) {
					LOG.info(uri.toString()+" has not SIPS scheme: skipped");
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
			int result=400;
			return sip_provider.messageFactory().createResponse(msg,result,null,null);  
		}         
		SipURI dest_sip_uri=new SipURI(th.getNameAddress().getAddress());
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
			if (server_profile.register_new_users) {
				location_service.addUser(user);
				LOG.info("new user '"+user+"' added");
			} 
			else {
				LOG.info("user '"+user+"' unknown: message discarded.");
				int result=404;
				return sip_provider.messageFactory().createResponse(msg,result,null,null);  
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
			int result=200;
			SipMessage resp=sip_provider.messageFactory().createResponse(msg,result,null,null);  
			// add current contacts
			Vector v=new Vector();
			for (Enumeration e=location_service.getUserContactURIs(user); e.hasMoreElements(); ) {
				String contact=(String)e.nextElement();
				int expires=(int)(location_service.getUserContactExpirationDate(user,contact).getTime()-System.currentTimeMillis())/1000;
				if (expires>0) {
					// not expired
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
		SipMessage resp=sip_provider.messageFactory().createResponse(msg,result,null,null);  

		ContactHeader ch_0=new ContactHeader((Header)contacts.elementAt(0));
		if (ch_0.isStar()) {
			LOG.trace("ContactHeader is star");
			Vector resp_contacts=new Vector();
			for (Enumeration e=location_service.getUserContactURIs(user); e.hasMoreElements();)  {
				String contact=(String)(e.nextElement());
				if (!location_service.isUserContactStatic(user,contact))  {
					NameAddress name_address=location_service.getUserContactNameAddress(user,contact);
					// update db
					location_service.removeUserContact(user,contact);
					LOG.trace("contact removed: "+contact);
					if (exp_secs>0) {
						Date exp_date=new Date(System.currentTimeMillis()+((long)exp_secs)*1000);
						location_service.addUserContact(user,name_address,exp_date);
						//DateFormat df=new SimpleDateFormat("EEE, dd MMM yyyy hh:mm:ss 'GMT'",Locale.ITALIAN);
						//printLog("contact added: "+uri+"; expire: "+df.format(location_service.getUserContactExpire(user,url)),LogWriter.LEVEL_LOW);
						LOG.trace("contact added: "+contact+"; expire: "+DateFormat.formatEEEddMMMyyyyhhmmss(location_service.getUserContactExpirationDate(user,contact)));
					}
					ContactHeader ch_i=new ContactHeader(name_address.getAddress());
					ch_i.setExpires(exp_secs);
					resp_contacts.addElement(ch_i);
				}
			}
			if (resp_contacts.size()>0) resp.setContacts(new MultipleHeader(resp_contacts));
		}
		else {
			Vector resp_contacts=new Vector();
			for (int i=0; i<contacts.size(); i++)      {
				ContactHeader ch_i=new ContactHeader((Header)contacts.elementAt(i));
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
					LOG.info("registration of user "+user+" updated");
				}           
				ch_i.setExpires(exp_secs_i);
				resp_contacts.addElement(ch_i);
			}
			if (resp_contacts.size()>0) resp.setContacts(new MultipleHeader(resp_contacts));
		}

		location_service.sync();  
		return resp;
	}


	// ****************************** MAIN *****************************

	/** The main method. */
	public static void main(String[] args) {
		SipConfig sipConfig = new SipConfig();
		SchedulerConfig schedulerConfig = new SchedulerConfig();

		MetaConfig metaConfig = OptionParser.parseOptions(args, ".mjsip-ua", sipConfig, schedulerConfig);
		
		sipConfig.normalize();
			
		SipProvider sip_provider=new SipProvider(sipConfig, new Scheduler(schedulerConfig));
		ServerProfile server_profile=new ServerProfile(metaConfig.getConfigFile());
		
		new Registrar(sip_provider,server_profile);
	}
}
