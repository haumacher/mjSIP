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

package org.mjsip.server.sbc;



import java.util.HashSet;
import java.util.Hashtable;
import java.util.Vector;

import org.mjsip.pool.PortPool;
import org.mjsip.sdp.MediaDescriptor;
import org.mjsip.sdp.SdpMessage;
import org.mjsip.sdp.field.MediaField;
import org.mjsip.sip.message.SipMessage;
import org.mjsip.time.Scheduler;
import org.slf4j.LoggerFactory;
import org.zoolu.net.SocketAddress;



/**
 * MediaGw is the actual SBC's MGW.
 * A MediaGw can be used to process and mangle SDP bodies
 * (through method <i>processSessionDescriptor(Message msg, String masq_addr)</i>),
 * and to automatically create ad-hoc SymmetricUdpRelays.
 * <p> A MediaGw is constructed based on
 * <br> - a Vector of available media ports,
 * <br> - a MGW timeout value,
 * <br> - a SymmetricUdpRelayListener that captures SymmetricUdpRelay events.
 */
public class MediaGw implements SymmetricUdpRelayListener {
	
	private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(MediaGw.class);

	/** The SipGw configuration */
	SessionBorderControllerProfile sbc_profile;

	/** SymmetricUdpRelay listener. */
	//SymmetricUdpRelayListener mgw_listener;

	PortPool _portPool;
	
	/** Media address */
	//String media_addr=null;

	/** Hashtable of pending Masquerades referred by call_id|leg|media (i.e., call_id|leg|media --> masq) */
	Hashtable<String, Masquerade> masq_table;

	/** Hashtable of established call_id. */
	HashSet<String> call_set;

	private Scheduler _scheduler;

	/**
	 * Constructs a new MediaGw.
	 */
	public MediaGw(Scheduler scheduler, PortPool portPool, SessionBorderControllerProfile sbc_profile) {
		_scheduler = scheduler;
		this.sbc_profile=sbc_profile;
		_portPool=portPool;
		masq_table=new Hashtable<>();
		call_set=new HashSet<>();
	}
	
	/**
	 * The task scheduler.
	 */
	public Scheduler scheduler() {
		return _scheduler;
	}

	/** Processes the sdp data */
	public SipMessage processSessionDescriptor(SipMessage msg) {
		LOG.debug("inside processSessionDescriptor()");
		
		SdpMessage sdp=new SdpMessage(msg.getStringBody());
		String dest_addr=sdp.getConnection().getAddress();
		// substitute 0.0.0.0 with 127.0.0.1
		if (dest_addr.equals("0.0.0.0")) dest_addr="127.0.0.1";
		
		String masq_addr=sbc_profile.mediaAddr;

		//String[] media={ "audio" };
		//int[] masq_port=new int[media.length];                   
		Vector<MediaDescriptor> mediaDescriptors=sdp.getMediaDescriptors();
		String[] mediaTypes=new String[mediaDescriptors.size()];                   
		int[] masq_port=new int[mediaDescriptors.size()];                   
			
		String call_id=msg.getCallIdHeader().getCallId();
		String leg=(msg.isRequest())? "caller" : "callee";

		//for (int i=0; i<media.length; i++)
		for (int i=0; i<mediaDescriptors.size(); i++) {
			//int dest_port=sdp.getMediaDescriptor(media[i]).getMedia().getPort();
			MediaDescriptor media_descriptor=mediaDescriptors.elementAt(i);
			MediaField mediaField=media_descriptor.getMediaField();
			mediaTypes[i]=mediaField.getMediaType();
			int dest_port=mediaField.getPort();
						
			String key=call_id+"-"+leg+"-"+mediaTypes[i];
			LOG.info("media-id: {}", key);
			if (masq_table.containsKey(key)) {
				// get masq
				Masquerade masq=masq_table.get(key);
				masq_addr=masq.getMasqSoaddr().getAddress().toString();
				masq_port[i]=masq.getMasqSoaddr().getPort();
			}
			else {
				// set masq
				masq_port[i]=_portPool.allocate();
				Masquerade masq=new Masquerade(new SocketAddress(dest_addr,dest_port),new SocketAddress(masq_addr,masq_port[i]));
				masq_table.put(key,masq);
			}
		}
		// mangle sdp
		for (int i=0; i<mediaTypes.length; i++) {
			LOG.info("mangle body: media={} masq_port={}", mediaTypes[i], masq_port[i]);
		}
		msg=SipMangler.mangleBody(msg,masq_addr,mediaTypes,masq_port);

		// creates the actual media relay (SymmetricUdpRelay) when both media legs are available
		if (mediaTypes.length>0) {
			if(masq_table.containsKey(call_id+"-caller"+"-"+mediaTypes[0]) && masq_table.containsKey(call_id+"-callee"+"-"+mediaTypes[0])) {
				LOG.info("complete call");
				if (!call_set.contains(call_id)) {
					LOG.info("creating new MediaGW");
					for (int i=0; i<mediaTypes.length; i++) {
						Masquerade masq_left=masq_table.get(call_id+"-caller"+"-"+mediaTypes[i]);
						Masquerade masq_right=masq_table.get(call_id+"-callee"+"-"+mediaTypes[i]);
						createSymmetricUdpRelay(masq_left,masq_right);
					}
					call_set.add(call_id);
				}
				else {
					LOG.info("MediaGW exists");
				}
			}
			else {
				LOG.info("half call");
			}
		}

		return msg;
	}


	/** Creates a new SymmetricUdpRelay */
	protected SymmetricUdpRelay createSymmetricUdpRelay(Masquerade masq_left, Masquerade masq_right) {
		try {
			int left_port=masq_right.getMasqSoaddr().getPort();
			int right_port=masq_left.getMasqSoaddr().getPort();
			
			SymmetricUdpRelay symm_relay;
			if (sbc_profile.doInterception) {
				// intercepting symmetric UDP relay
				int left_intercept_port=_portPool.allocate();
				int right_intercept_port=_portPool.allocate();
				SocketAddress sink_soaddr=null;
				if (sbc_profile.sinkAddr!=null && sbc_profile.sinkPort>0) sink_soaddr=new SocketAddress(sbc_profile.sinkAddr,sbc_profile.sinkPort);
				symm_relay = new InterceptingUdpRelay(scheduler(), left_port, masq_left.getPeerSoaddr(), right_port,
						masq_right.getPeerSoaddr(), left_intercept_port, sink_soaddr, right_intercept_port, sink_soaddr,
						sbc_profile.doActiveInterception, sbc_profile.relayTimeout, this);
				LOG.debug("IMGW started: {}", symm_relay);
			}
			else
			if (sbc_profile.interpacketTime>0) {
				// symmetric regulated UDP relay
				symm_relay = new SymmetricRegulatedUdpRelay(scheduler(), left_port, masq_left.getPeerSoaddr(), right_port,
						masq_right.getPeerSoaddr(), sbc_profile.relayTimeout, sbc_profile.interpacketTime, this);
				LOG.debug("MGW started: {}", symm_relay);
			}
			else {
				// simple symmetric UDP relay
				symm_relay = new SymmetricUdpRelay(scheduler(), left_port, masq_left.getPeerSoaddr(), right_port,
						masq_right.getPeerSoaddr(), sbc_profile.relayTimeout, this);
				LOG.debug("MGW started: {}", symm_relay);
			}

			return symm_relay;
		}
		catch (Exception e) {
			LOG.info("Exception.", e);
			return null;
		}
	}


	// ********************** SymmetricUdpRelay callbacks *********************

	/** When left peer address changes. */
	@Override
	public void onSymmetricUdpRelayLeftPeerChanged(SymmetricUdpRelay symm_relay, SocketAddress soaddr) {
		LOG.info("change left peer soaddr {}", soaddr);
		// handover?
		long htime=sbc_profile.handoverTime;
		if (htime>0 && (System.currentTimeMillis()+htime)<symm_relay.getLastLeftChangeTime()) return;
		// else
		symm_relay.setLeftSoAddress(soaddr);
		LOG.info("MGW change L: {}", symm_relay);
	}


	/** When right peer address changes. */
	@Override
	public void onSymmetricUdpRelayRightPeerChanged(SymmetricUdpRelay symm_relay, SocketAddress soaddr) {
		LOG.debug("change right peer soaddr {}", soaddr);
		// handover?
		long htime=sbc_profile.handoverTime;
		if (htime>0 && (System.currentTimeMillis()+htime)<symm_relay.getLastRightChangeTime()) return;
		// else
		symm_relay.setRightSoAddress(soaddr);
		LOG.info("MGW change R: {}", symm_relay);
	}


	/** When it stops relaying UDP datagrams (both directions). */
	@Override
	public void onSymmetricUdpRelayTerminated(SymmetricUdpRelay symm_relay) {
		LOG.debug("MGW terminated: {}", symm_relay);
	}
	
}
