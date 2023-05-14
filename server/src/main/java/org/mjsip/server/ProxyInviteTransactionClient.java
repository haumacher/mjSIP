/*
 * Copyright (C) 2006 Luca Veltri - University of Parma - Italy
 * 
 * This file is part of MjSip (http://www.mjsip.org)
 * 
 * MjSip is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * MjSip is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with MjSip; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 * 
 * Author(s):
 * Luca Veltri (luca.veltri@unipr.it)
 */

package org.mjsip.server;


import org.mjsip.sip.message.SipMessage;
import org.mjsip.sip.provider.SipProvider;
import org.mjsip.sip.transaction.InviteTransactionClient;
import org.mjsip.sip.transaction.TransactionClientListener;
import org.zoolu.util.LogLevel;
import org.zoolu.util.Timer;


/** ProxyInviteTransactionClient extends InviteTransactionClient adding "Timer C"
  * as defined in RFC 3261.
  */ 
public class ProxyInviteTransactionClient extends InviteTransactionClient {
	
	/** Proxy-transaction timeout for client transactions ("Timer C" in RFC 3261) */
	Timer proxy_transaction_to;

	/** TransactionClientListener */
	TransactionClientListener transaction_listener;

 
	/** Creates a new ProxyInviteTransactionClient */
	public ProxyInviteTransactionClient(SipProvider sip_provider, SipMessage req, TransactionClientListener listener) {
		super(sip_provider,req,listener);
		transaction_listener=listener;
		proxy_transaction_to=new Timer(ServerProfile.proxy_transaction_timeout,this);
		proxy_transaction_to.start();
	}
	

	/** From interface TimerListener. When the Timer expires. */
	public void onTimeout(Timer to) {
		try {
			if (to.equals(proxy_transaction_to)) {
				log(LogLevel.INFO,"Proxy-transaction timeout expired");
				doTerminate();
				if (transaction_listener!=null) transaction_listener.onTransTimeout(this);
				transaction_listener=null;
			}
			else super.onTimeout(to);
		}
		catch (Exception e) {
			log(LogLevel.INFO,e);
		}
	}


	/** Moves to terminate state. */
	protected void doTerminate() {
		if (!statusIs(STATE_TERMINATED)) {
			proxy_transaction_to.halt();
		}
		super.doTerminate();
	}
 
}
