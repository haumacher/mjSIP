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



import org.mjsip.sip.message.SipMessage;
import org.mjsip.sip.message.SipMessageFactory;
import org.mjsip.sip.provider.MethodId;
import org.mjsip.sip.provider.SipProvider;
import org.mjsip.sip.provider.SipProviderListener;
import org.mjsip.sip.transaction.TransactionServer;
import org.zoolu.util.LogLevel;
import org.zoolu.util.Logger;



/** Simple UAS that responds to any requests (that are not captured by other active servers)
  * with 501 "NOT IMPLEMENTED" responses.
  */
public class NotImplementedServer implements SipProviderListener {
	
	/** Logger */
	Logger logger;
	
	/** SipProvider. */
	SipProvider sip_provider;
	
	/** Array of implemented methods. */
	String[] implemented_methods;



	// *************************** Public Methods **************************

	/** Costructs a new NotImplementedServer. */
	public NotImplementedServer(SipProvider sip_provider) {
		this.sip_provider=sip_provider;
		logger=sip_provider.getLogger();
		implemented_methods=null;
		sip_provider.addSelectiveListener(MethodId.ANY,this);  
	} 


	/** Costructs a new NotImplementedServer. */
	public NotImplementedServer(String[] implemented_methods, SipProvider sip_provider) {
		this.sip_provider=sip_provider;
		logger=sip_provider.getLogger();
		this.implemented_methods=implemented_methods;
		sip_provider.addSelectiveListener(MethodId.ANY,this);  
	} 


	/** Stops the NotImplementedServer */
	public void halt() {
		if (sip_provider!=null) sip_provider.removeSelectiveListener(MethodId.ANY);
		sip_provider=null;
		logger=null;
	}   


	// ************************* Callback functions ************************

	/** When a new Message is received by the SipProvider. */
	public void onReceivedMessage(SipProvider sip_provider, SipMessage msg) {
		// respond
		if (msg.isRequest() && !msg.isAck() && !msg.isCancel()) {
			String method=msg.getRequestLine().getMethod();
			boolean is_implemented=false;
			if (implemented_methods!=null) {
				for (int i=0; i<implemented_methods.length; i++) if (method.equalsIgnoreCase(implemented_methods[i])) is_implemented=true;
			}
			if (!is_implemented)      {
				log("responding to a new "+method+" request");
				SipMessage resp=SipMessageFactory.createResponse(msg,501,null,null);
				TransactionServer ts=new TransactionServer(sip_provider,msg,null);
				ts.respondWith(resp);
			}
		}
	}


	// ******************************** Logs *******************************

	/** Adds a new string to the default log. */
	void log(String str) {
		if (logger!=null) logger.log(LogLevel.INFO,"NotImplementedServer: "+str);  
	}

}
