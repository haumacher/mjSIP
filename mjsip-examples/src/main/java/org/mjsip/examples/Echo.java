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



import org.mjsip.media.MediaDesc;
import org.mjsip.sip.address.NameAddress;
import org.mjsip.sip.address.SipURI;
import org.mjsip.sip.header.RouteHeader;
import org.mjsip.sip.message.SipMessage;
import org.mjsip.sip.message.SipMethods;
import org.mjsip.sip.provider.MethodId;
import org.mjsip.sip.provider.SipConfig;
import org.mjsip.sip.provider.SipProvider;
import org.mjsip.sip.provider.SipProviderListener;
import org.mjsip.sip.provider.SipStack;
import org.mjsip.sip.transaction.TransactionClient;
import org.mjsip.sip.transaction.TransactionServer;
import org.mjsip.time.Scheduler;
import org.mjsip.time.SchedulerConfig;
import org.mjsip.ua.MediaConfig;
import org.mjsip.ua.MultipleUAS;
import org.mjsip.ua.PortConfig;
import org.mjsip.ua.PortPool;
import org.mjsip.ua.ServiceConfig;
import org.mjsip.ua.UAConfig;
import org.mjsip.ua.UserAgent;
import org.mjsip.ua.UserAgentListener;
import org.mjsip.ua.UserAgentListenerAdapter;
import org.mjsip.ua.streamer.LoopbackStreamerFactory;
import org.mjsip.ua.streamer.StreamerFactory;
import org.slf4j.LoggerFactory;
import org.zoolu.util.Flags;



/** Echo is a simple UA that loops back any media streams.
  * It automatically responds to incoming calls and sends back
  * the received RTP streams.
  */
public class Echo extends MultipleUAS implements SipProviderListener {
	
	private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(Echo.class);

	/** Maximum life time (call duration) in seconds */
	public static int MAX_LIFE_TIME=600;

	/** Current media port */
	int media_port;

	/** Whether forcing reverse route */
	boolean force_reverse_route;

	private PortPool _portPool;

	/** 
	 * Creates a {@link Echo} service. 
	 */
	public Echo(SipProvider sip_provider, StreamerFactory streamerFactory, UAConfig uaConfig, PortPool portPool, boolean force_reverse_route, ServiceConfig serviceConfig) {
		super(sip_provider,streamerFactory, uaConfig, serviceConfig);
		_portPool = portPool;
		this.force_reverse_route=force_reverse_route;
		// message UAS
		sip_provider.addSelectiveListener(new MethodId(SipMethods.MESSAGE),this); 
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
			LOG.info("message received: "+new String(content));
			// respond
			TransactionServer ts=new TransactionServer(sip_provider,msg,null);
			ts.respondWith(200);
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
			MediaConfig _callMedia;

			@Override
			public void onUaIncomingCall(UserAgent ua, NameAddress callee, NameAddress caller, MediaDesc[] media_descs) {
				_callMedia = MediaConfig.from(media_descs);
				_callMedia.allocateMediaPorts(_portPool);
				
				ua.accept(_callMedia.mediaDescs);
				LOG.info("incoming call accepted");
			}
			
			@Override
			public void onUaCallClosed(UserAgent ua) {
				_callMedia.releaseMediaPorts(_portPool);
			}
		};
	}

	/** The main method. */
	public static void main(String[] args) {
		System.out.println("Echo "+SipStack.version);

		boolean force_reverse_route=false;
		boolean prompt_exit=false;

		for (int i=0; i<args.length; i++) {
			if (args[i].equals("--rroute")) {
				force_reverse_route=true;
				args[i]="--skip";
			}
			else
			if (args[i].equals("--prompt")) {
				prompt_exit=true;
				args[i]="--skip";
			}
		}
		Flags flags=new Flags("Echo", args);
		String config_file=flags.getString("-f","<file>", System.getProperty("user.home") + "/.mjsip-ua" ,"loads configuration from the given file");
		SipConfig sipConfig = SipConfig.init(config_file, flags);
		UAConfig uaConfig = UAConfig.init(config_file, flags, sipConfig);
		SchedulerConfig schedulerConfig = SchedulerConfig.init(config_file);
		PortConfig portConfig = PortConfig.init(config_file, flags);
		ServiceConfig serviceConfig=ServiceConfig.init(config_file, flags);         
		flags.close();
		
		PortPool portPool = new PortPool(portConfig.mediaPort, portConfig.portCount);
		
		new Echo(new SipProvider(sipConfig, new Scheduler(schedulerConfig)),new LoopbackStreamerFactory(),uaConfig,portPool, force_reverse_route, serviceConfig);

		// promt before exit
		if (prompt_exit) 
		try {
			System.out.println("press 'enter' to exit");
			(new java.io.BufferedReader(new java.io.InputStreamReader(System.in))).readLine();
			System.exit(0);
		}
		catch (Exception e) {}
	}    

}
