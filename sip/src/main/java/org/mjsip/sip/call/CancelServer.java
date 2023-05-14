/*
 * Copyright (C) 2014 Luca Veltri - University of Parma - Italy
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
import org.mjsip.sip.message.SipMethods;
import org.mjsip.sip.provider.MethodId;
import org.mjsip.sip.provider.SipProvider;
import org.mjsip.sip.provider.SipProviderListener;
import org.mjsip.sip.transaction.TransactionServer;
import org.zoolu.util.LogLevel;
import org.zoolu.util.Logger;



/** Simple UAS that responds to CANCEL requests that are outside of any already established dialog or CANCEL transaction.
  */
public class CancelServer implements SipProviderListener {
	
	/** SipProvider. */
	SipProvider sip_provider;
	


	/** Costructs a new CancelServer. */
	public CancelServer(SipProvider sip_provider) {
		this.sip_provider=sip_provider;
		sip_provider.addSelectiveListener(new MethodId(SipMethods.CANCEL),this);  
	} 


	/** Stops the CancelServer */
	public void halt() {
		if (sip_provider!=null) {
			sip_provider.removeSelectiveListener(new MethodId(SipMethods.CANCEL));
			sip_provider=null;
		}
	}   


	/** When a new Message is received by the SipProvider. */
	public void onReceivedMessage(SipProvider sip_provider, SipMessage msg) {
		// respond to CANCEL request
		if (msg.isRequest() && msg.isCancel()) {
			log("responding to CANCEL request with 481 \"Call Leg/Transaction Does Not Exist\"");
			SipMessage resp=SipMessageFactory.createResponse(msg,481,null,null);
			TransactionServer ts=new TransactionServer(sip_provider,msg,null);
			ts.respondWith(resp);
		}
	}


	/** Adds a new string to the default log. */
	void log(String str) {
		Logger logger=sip_provider.getLogger();
		if (logger!=null) logger.log(LogLevel.INFO,"CancelServer: "+str);  
	}

}
