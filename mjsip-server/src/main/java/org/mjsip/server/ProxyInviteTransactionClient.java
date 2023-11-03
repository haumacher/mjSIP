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


import java.util.concurrent.ScheduledFuture;

import org.mjsip.sip.message.SipMessage;
import org.mjsip.sip.provider.SipProvider;
import org.mjsip.sip.transaction.InviteTransactionClient;
import org.mjsip.sip.transaction.TransactionClientListener;
import org.slf4j.LoggerFactory;


/** ProxyInviteTransactionClient extends InviteTransactionClient adding "Timer C"
  * as defined in RFC 3261.
  */ 
public class ProxyInviteTransactionClient extends InviteTransactionClient {
	
	private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(ProxyInviteTransactionClient.class);

	/** Proxy-transaction timeout for client transactions ("Timer C" in RFC 3261) */
	ScheduledFuture<?> proxy_transaction_to;

	/** TransactionClientListener */
	TransactionClientListener transaction_listener;

 
	/** Creates a new ProxyInviteTransactionClient */
	public ProxyInviteTransactionClient(SipProvider sip_provider, SipMessage req, long proxyTransactionTimeout, TransactionClientListener listener) {
		super(sip_provider,req,listener);
		transaction_listener=listener;
		proxy_transaction_to=sip_provider.scheduler().schedule(proxyTransactionTimeout, this::onProxyTransactionTimeout);
	}
	
	private void onProxyTransactionTimeout() {
		LOG.info("Proxy-transaction timeout expired");
		doTerminate();
		if (transaction_listener!=null) transaction_listener.onTransTimeout(this);
		transaction_listener=null;
	}


	/** Moves to terminate state. */
	@Override
	protected void doTerminate() {
		if (!statusIs(STATE_TERMINATED)) {
			proxy_transaction_to.cancel(false);
		}
		super.doTerminate();
	}
 
}
