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



import org.mjsip.media.MediaDesc;
import org.mjsip.sip.address.NameAddress;
import org.mjsip.sip.address.SipURI;
import org.mjsip.sip.header.RouteHeader;
import org.mjsip.sip.message.SipMessage;
import org.mjsip.sip.message.SipMethods;
import org.mjsip.sip.provider.MethodId;
import org.mjsip.sip.provider.SipProvider;
import org.mjsip.sip.provider.SipProviderListener;
import org.mjsip.sip.provider.SipStack;
import org.mjsip.sip.transaction.TransactionClient;
import org.mjsip.sip.transaction.TransactionServer;
import org.slf4j.LoggerFactory;



/** Echo is a simple UA that loops back any media streams.
  * It automatically responds to incoming calls and sends back
  * the received RTP streams.
  */
public class Echo extends MultipleUAS implements SipProviderListener {
	
	private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(Echo.class);

	/** Default number of available media ports */
	public static int MEDIA_PORTS=40;

	/** Maximum life time (call duration) in seconds */
	public static int MAX_LIFE_TIME=600;



	/** First media port */
	int first_media_port;

	/** Last media port */
	int last_media_port;

	/** Current media port */
	int media_port;

	
	/** Whether forcing reverse route */
	boolean force_reverse_route;



	/** Creates a new Echo. */
	public Echo(SipProvider sip_provider, UserAgentProfile ua_profile, int media_ports, boolean force_reverse_route) {
		// call UAS
		super(sip_provider,ua_profile);
		first_media_port=ua_profile.getMediaPort();
		last_media_port=first_media_port+media_ports-1;
		media_port=first_media_port;
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
			SipMessage reply=sip_provider.sipMessageFactory.createMessageRequest(sender,recipient,sip_provider.pickCallId(),null,content_type,content);
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
	

	/** From UserAgentListener. When a new call is incoming. */
	@Override
	public void onUaIncomingCall(UserAgent ua, NameAddress callee, NameAddress caller, MediaDesc[] media_descs) {
		if (media_descs!=null) {
			for (int i=0; i<media_descs.length; i++) {
				(media_descs[i]).setPort(media_port);
				if ((++media_port)>last_media_port) media_port=first_media_port;
			}
			ua.accept(media_descs);
		}
		else ua.accept();
		LOG.info("incoming call accepted");
	}
	
	/** The main method. */
	public static void main(String[] args) {
		
		UserAgentConfig.println("Echo "+SipStack.version);

		int media_ports=MEDIA_PORTS;
		boolean force_reverse_route=false;
		boolean prompt_exit=false;

		for (int i=0; i<args.length; i++) {
			if (args[i].equals("--mports")) {
				try {
					media_ports=Integer.parseInt(args[i+1]);
					args[i]="--skip";
					args[++i]="--skip";
				}
				catch (Exception e) {  e.printStackTrace();  }
			}
			else
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
		
		UserAgentConfig config = UserAgentConfig.init("Echo",args);
		config.ua_profile.audio=true;
		config.ua_profile.video=true;
		config.ua_profile.loopback=true;
		config.ua_profile.send_only=false;
		if (config.ua_profile.hangup_time<=0) config.ua_profile.hangup_time=MAX_LIFE_TIME;
		new Echo(config.sip_provider,config.ua_profile,media_ports,force_reverse_route);

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