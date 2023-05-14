/*
 * Copyright (C) 2006 Luca Veltri - University of Parma - Italy
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


import org.mjsip.sip.header.AcceptEncodingHeader;
import org.mjsip.sip.header.AcceptHeader;
import org.mjsip.sip.header.AcceptLanguageHeader;
import org.mjsip.sip.header.AllowHeader;
import org.mjsip.sip.header.SupportedHeader;
import org.mjsip.sip.message.SipMessage;
import org.mjsip.sip.message.SipMessageFactory;
import org.mjsip.sip.message.SipMethods;
import org.mjsip.sip.provider.MethodId;
import org.mjsip.sip.provider.SipProvider;
import org.mjsip.sip.provider.SipProviderListener;
import org.mjsip.sip.transaction.TransactionServer;
import org.zoolu.util.LogLevel;
import org.zoolu.util.Logger;


/** Simple UAS that responds to OPTIONS requests.
  */
public class OptionsServer implements SipProviderListener {
	
	/** Logger */
	Logger logger;
	
	/** SipProvider. */
	SipProvider sip_provider;
	
	/** Allow header vaule. */
	String allow; // e.g. "INVITE, ACK, CANCEL, OPTIONS, BYE"
	
	/** Accept header vaule. */
	String accept; // e.g.: application/sdp

	/** Accept-Encoding header vaule. */
	String accept_encoding; // e.g.: gzip

	/** Accept-Language header vaule. */
	String accept_language; // e.g.: en

	/** Supported option tags. */
	String[] supported_option_tags;



	// *************************** Public Methods **************************

	/** Costructs a new OptionsServer. */
	public OptionsServer(SipProvider sip_provider, String allow, String accept) {
		init(sip_provider,allow,accept,null,null,null);
	} 


	/** Costructs a new OptionsServer. */
	public OptionsServer(SipProvider sip_provider, String allow, String accept, String accept_encoding, String accept_language, String[] supported_option_tags) {
		init(sip_provider,allow,accept,accept_encoding,accept_language,supported_option_tags);
	} 


	/** Inits the OptionsServer. */
	private void init(SipProvider sip_provider, String allow, String accept, String accept_encoding, String accept_language, String[] supported_option_tags) {
		this.sip_provider=sip_provider;
		logger=sip_provider.getLogger();
		this.allow=allow;
		this.accept=accept;
		this.accept_encoding=accept_encoding;
		this.accept_language=accept_language;
		this.supported_option_tags=supported_option_tags;
		sip_provider.addSelectiveListener(new MethodId(SipMethods.OPTIONS),this);  
	} 


	/** Stops the OptionsServer */
	public void halt() {
		if (sip_provider!=null) sip_provider.removeSelectiveListener(new MethodId(SipMethods.OPTIONS));
		sip_provider=null;
		logger=null;
	}   


	// ************************* Callback functions ************************

	/** When a new Message is received by the SipProvider. */
	public void onReceivedMessage(SipProvider sip_provider, SipMessage msg) {
		// respond to OPTIONS request
		if (msg.isRequest() && msg.isOptions()) {
			log("responding to a new OPTIONS request");
			SipMessage resp=SipMessageFactory.createResponse(msg,200,null,null);
			if (allow!=null) resp.setAllowHeader(new AllowHeader(allow));
			if (accept!=null) resp.setAcceptHeader(new AcceptHeader(accept));
			if (accept_encoding!=null) resp.setAcceptEncodingHeader(new AcceptEncodingHeader(accept_encoding));
			if (accept_language!=null) resp.setAcceptLanguageHeader(new AcceptLanguageHeader(accept_language));
			if (supported_option_tags!=null && supported_option_tags.length>0) resp.setSupportedHeader(new SupportedHeader(supported_option_tags));
			TransactionServer ts=new TransactionServer(sip_provider,msg,null);
			ts.respondWith(resp);
		}
	}


	// ******************************** Logs *******************************

	/** Adds a new string to the default log. */
	void log(String str) {
		if (logger!=null) logger.log(LogLevel.INFO,"OptionsServer: "+str);  
	}

}
