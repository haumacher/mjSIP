/*
 * Copyright (C) 2007 Luca Veltri - University of Parma - Italy
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

package org.mjsip.ua;


import org.mjsip.pool.PortPool;
import org.mjsip.sip.message.SipMessage;
import org.mjsip.sip.message.SipMethods;
import org.mjsip.sip.provider.SipId;
import org.mjsip.sip.provider.SipProvider;
import org.mjsip.sip.provider.SipProviderListener;
import org.slf4j.LoggerFactory;


/**
 * UA that automatically responds to incoming calls.
 * 
 * @see RegisteringMultipleUAS
 */
public abstract class MultipleUAS implements SipProviderListener {
	
	private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(MultipleUAS.class);

	/** UserAgentProfile */
	protected final StaticOptions _config;
			
	/** SipProvider */
	protected final SipProvider sip_provider;

	protected final PortPool _portPool;
	

	/**
	 * Creates a {@link MultipleUAS}.
	 */
	public MultipleUAS(SipProvider sip_provider, PortPool portPool, StaticOptions uaConfig) {
		this.sip_provider=sip_provider;
		_portPool = portPool;
		_config=uaConfig;

		// start UAS     
		sip_provider.addSelectiveListener(SipId.createMethodId(SipMethods.INVITE),this); 
	}

	/**
	 * Unregisters from the SIP provider.
	 */
	public void halt() {
		sip_provider.removeSelectiveListener(SipId.createMethodId(SipMethods.INVITE));
	}

	// ******************* SipProviderListener methods  ******************

	/** From SipProviderListener. When a new Message is received by the SipProvider. */
	@Override
	public void onReceivedMessage(SipProvider sip_provider, SipMessage msg) {
		LOG.debug("onReceivedMessage()");
		if (msg.isRequest() && msg.isInvite()) {
			onInviteReceived(msg);
		}
	}

	/**
	 * Handles an SIP invite.
	 * 
	 * <p>
	 * By default the call is accepted and a {@link UserAgent} created for handling the new call.
	 * </p>
	 * @param msg
	 *        The invite message.
	 */
	protected abstract void onInviteReceived(SipMessage msg);


}
