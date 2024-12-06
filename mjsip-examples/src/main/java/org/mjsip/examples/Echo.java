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

package org.mjsip.examples;



import org.kohsuke.args4j.Option;
import org.mjsip.config.OptionParser;
import org.mjsip.media.MediaDesc;
import org.mjsip.pool.PortConfig;
import org.mjsip.pool.PortPool;
import org.mjsip.sip.address.NameAddress;
import org.mjsip.sip.address.SipURI;
import org.mjsip.sip.header.RouteHeader;
import org.mjsip.sip.message.SipMessage;
import org.mjsip.sip.message.SipMethods;
import org.mjsip.sip.message.SipResponses;
import org.mjsip.sip.provider.SipConfig;
import org.mjsip.sip.provider.SipId;
import org.mjsip.sip.provider.SipProvider;
import org.mjsip.sip.provider.SipStack;
import org.mjsip.sip.transaction.TransactionClient;
import org.mjsip.sip.transaction.TransactionServer;
import org.mjsip.time.ConfiguredScheduler;
import org.mjsip.time.SchedulerConfig;
import org.mjsip.ua.MediaAgent;
import org.mjsip.ua.RegisteringMultipleUAS;
import org.mjsip.ua.ServiceConfig;
import org.mjsip.ua.ServiceOptions;
import org.mjsip.ua.UAConfig;
import org.mjsip.ua.UserAgent;
import org.mjsip.ua.UserAgentListener;
import org.mjsip.ua.UserAgentListenerAdapter;
import org.mjsip.ua.streamer.LoopbackStreamerFactory;
import org.mjsip.ua.streamer.StreamerFactory;
import org.slf4j.LoggerFactory;



/** Echo is a simple UA that loops back any media streams.
  * It automatically responds to incoming calls and sends back
  * the received RTP streams.
  */
public class Echo extends RegisteringMultipleUAS {
	
	private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(Echo.class);

	/** Maximum life time (call duration) in seconds */
	public static int MAX_LIFE_TIME=600;

	/** Current media port */
	int media_port;

	/** Whether forcing reverse route */
	boolean force_reverse_route;

	private final StreamerFactory _streamerFactory;

	/** 
	 * Creates a {@link Echo} service. 
	 */
	public Echo(SipProvider sip_provider, StreamerFactory streamerFactory, UAConfig uaConfig, PortPool portPool,
			boolean force_reverse_route, ServiceOptions serviceConfig) {
		super(sip_provider,portPool, uaConfig, serviceConfig);
		_streamerFactory = streamerFactory;
		this.force_reverse_route=force_reverse_route;
		// message UAS
		sip_provider.addSelectiveListener(SipId.createMethodId(SipMethods.MESSAGE),this); 
	} 

	/** From SipProviderListener. When a new Message is received by the SipProvider. */
	@Override
	public void onReceivedMessage(SipProvider sip_provider, SipMessage msg) {
		if (msg.isRequest() && msg.isMessage()) {
			// get caller, callee, sdp
			NameAddress recipient=msg.getToHeader().getNameAddress();
			NameAddress sender=msg.getFromHeader().getNameAddress();
			String content_type=msg.getContentTypeHeader().getContentType();
			byte[] content=msg.getBody();
			LOG.info("message received: {}", new String(content)); //not so nice, but if you want the bytes as characters ...
			// respond
			TransactionServer ts=new TransactionServer(sip_provider,msg,null);
			ts.respondWith(SipResponses.OK);
			// reply
			SipMessage reply=sip_provider.messageFactory().createMessageRequest(sender,recipient,sip_provider.pickCallId(),null,content_type,content);
			if (force_reverse_route) {
				SipURI previous_hop=new SipURI(msg.getRemoteAddress(),msg.getRemotePort());
				previous_hop.addLr();
				reply.addRouteHeader(new RouteHeader(new NameAddress(previous_hop)));
			}
			TransactionClient tc=new TransactionClient(sip_provider,reply,null);
			tc.request();
			LOG.info("echo reply sent");
		}
		else super.onReceivedMessage(sip_provider,msg);
	}
	
	@Override
	protected UserAgentListener createCallHandler(SipMessage msg) {
		return new UserAgentListenerAdapter() {
			@Override
			public void onUaIncomingCall(UserAgent ua, NameAddress callee, NameAddress caller, MediaDesc[] media_descs) {
				ua.accept(new MediaAgent(media_descs, _streamerFactory));
				LOG.info("incoming call accepted");
			}
		};
	}
	
	public static class Config {
		@Option(name = "--rroute", usage = "Force reverse route.")
		boolean forceReverseRoute;
		
		@Option(name = "--prompt", usage = "Prompt before exit.")
		boolean prompt;
	}

	/** The main method. */
	public static void main(String[] args) {
		System.out.println("Echo "+SipStack.version);

		SipConfig sipConfig = new SipConfig();
		UAConfig uaConfig = new UAConfig();
		SchedulerConfig schedulerConfig = new SchedulerConfig();
		PortConfig portConfig = new PortConfig();
		ServiceConfig serviceConfig = new ServiceConfig();
		
		Config config = new Config();

		OptionParser.parseOptions(args, ".mjsip-ua", sipConfig, uaConfig, schedulerConfig, portConfig, serviceConfig, config);
		
		sipConfig.normalize();
		uaConfig.normalize(sipConfig);
		
		new Echo(new SipProvider(sipConfig, new ConfiguredScheduler(schedulerConfig)),new LoopbackStreamerFactory(),uaConfig,portConfig.createPool(), config.forceReverseRoute, serviceConfig);

		// Prompt before exit
		if (config.prompt) {
			try {
				System.out.println("press 'enter' to exit");
				(new java.io.BufferedReader(new java.io.InputStreamReader(System.in))).readLine();
				System.exit(0);
			}
			catch (Exception e) {}
		}
	}    

}
