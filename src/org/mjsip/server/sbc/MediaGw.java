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

import org.mjsip.sdp.MediaDescriptor;
import org.mjsip.sdp.SdpMessage;
import org.mjsip.sdp.field.MediaField;
import org.mjsip.sip.message.SipMessage;
import org.zoolu.net.SocketAddress;
import org.zoolu.util.ExceptionPrinter;

// logs



import org.zoolu.util.LogLevel;
import org.zoolu.util.LogWriter;
import org.zoolu.util.Logger;



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
	
	/** Logger */
	Logger logger=null;

	/** Dumper */
	Logger dumper=null;

	/** The SipGw configuration */
	SessionBorderControllerProfile sbc_profile;

	/** SymmetricUdpRelay listener. */
	//SymmetricUdpRelayListener mgw_listener;

	/** Avaliable local media ports. */
	CircularEnumeration media_ports;
	
	/** Media address */
	//String media_addr=null;

	/** Hashtable of pending Masquerades referred by call_id|leg|media (i.e., call_id|leg|media --> masq) */
	Hashtable masq_table;

	/** Hashtable of established call_id. */
	HashSet call_set;



	/** Costructs a new MediaGw. */
	public MediaGw(SessionBorderControllerProfile sbc_profile, Logger logger) {
		this.sbc_profile=sbc_profile;
		media_ports=new CircularEnumeration(sbc_profile.media_ports);
		this.logger=logger;
		dumper=new LogWriter(System.out,LogLevel.INFO);
		masq_table=new Hashtable();
		call_set=new HashSet();
	}


	/** Processes the sdp data */
	public SipMessage processSessionDescriptor(SipMessage msg) {
		log(LogLevel.DEBUG,"inside processSessionDescriptor()");
		
		SdpMessage sdp=new SdpMessage(msg.getStringBody());
		String dest_addr=sdp.getConnection().getAddress();
		// substitute 0.0.0.0 with 127.0.0.1
		if (dest_addr.equals("0.0.0.0")) dest_addr="127.0.0.1";
		
		String masq_addr=sbc_profile.media_addr;

		//String[] media={ "audio" };
		//int[] masq_port=new int[media.length];                   
		Vector media_descriptors=sdp.getMediaDescriptors();
		String[] media=new String[media_descriptors.size()];                   
		int[] masq_port=new int[media_descriptors.size()];                   
			
		String call_id=msg.getCallIdHeader().getCallId();
		String leg=(msg.isRequest())? "caller" : "callee";

		//for (int i=0; i<media.length; i++)
		for (int i=0; i<media_descriptors.size(); i++) {
			//int dest_port=sdp.getMediaDescriptor(media[i]).getMedia().getPort();
			MediaDescriptor media_descriptor=(MediaDescriptor)media_descriptors.elementAt(i);
			MediaField media_filed=media_descriptor.getMedia();
			media[i]=media_filed.getMedia();
			int dest_port=media_filed.getPort();
						
			String key=call_id+"-"+leg+"-"+media[i];
			log(LogLevel.INFO,"media-id: "+key);
			if (masq_table.containsKey(key)) {
				// get masq
				Masquerade masq=(Masquerade)masq_table.get(key);
				masq_addr=masq.getMasqSoaddr().getAddress().toString();
				masq_port[i]=masq.getMasqSoaddr().getPort();
			}
			else {
				// set masq
				masq_port[i]=((Integer)media_ports.nextElement()).intValue();
				Masquerade masq=new Masquerade(new SocketAddress(dest_addr,dest_port),new SocketAddress(masq_addr,masq_port[i]));
				masq_table.put(key,masq);
			}
		}
		// mangle sdp
		for (int i=0; i<media.length; i++) log(LogLevel.INFO,"mangle body: media="+media[i]+" masq_port="+masq_port[i]);
		msg=SipMangler.mangleBody(msg,masq_addr,media,masq_port);

		// creates the actual media relay (SymmetricUdpRelay) when both media legs are available
		if (media.length>0) {
			if(masq_table.containsKey(call_id+"-caller"+"-"+media[0]) && masq_table.containsKey(call_id+"-callee"+"-"+media[0])) {
				log(LogLevel.INFO,"complete call");
				if (!call_set.contains(call_id)) {
					log(LogLevel.INFO,"creating new MediaGW");
					for (int i=0; i<media.length; i++) {
						Masquerade masq_left=(Masquerade)masq_table.get(call_id+"-caller"+"-"+media[i]);
						Masquerade masq_right=(Masquerade)masq_table.get(call_id+"-callee"+"-"+media[i]);
						createSymmetricUdpRelay(masq_left,masq_right);
					}
					call_set.add(call_id);
				}
				else {
					log(LogLevel.INFO,"MediaGW exists");
				}
			}
			else {
				log(LogLevel.INFO,"half call");
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
			if (sbc_profile.do_interception) {
				// intercepting symmetric UDP relay
				int left_intercept_port=((Integer)media_ports.nextElement()).intValue();
				int right_intercept_port=((Integer)media_ports.nextElement()).intValue();
				SocketAddress sink_soaddr=null;
				if (sbc_profile.sink_addr!=null && sbc_profile.sink_port>0) sink_soaddr=new SocketAddress(sbc_profile.sink_addr,sbc_profile.sink_port);
				symm_relay=new InterceptingUdpRelay(left_port,masq_left.getPeerSoaddr(),right_port,masq_right.getPeerSoaddr(),left_intercept_port,sink_soaddr,right_intercept_port,sink_soaddr,sbc_profile.do_active_interception,sbc_profile.relay_timeout,logger,this);
				log(LogLevel.DEBUG,"IMGW started: "+symm_relay);
				dump("IMGW started: "+symm_relay);
			}
			else
			if (sbc_profile.interpacket_time>0) {
				// symmetric regulated UDP relay
				symm_relay=new SymmetricRegulatedUdpRelay(left_port,masq_left.getPeerSoaddr(),right_port,masq_right.getPeerSoaddr(),sbc_profile.relay_timeout,sbc_profile.interpacket_time,logger,this);
				log(LogLevel.DEBUG,"MGW started: "+symm_relay);
				dump("MGW started: "+symm_relay);
			}
			else {
				// simple symmetric UDP relay
				symm_relay=new SymmetricUdpRelay(left_port,masq_left.getPeerSoaddr(),right_port,masq_right.getPeerSoaddr(),sbc_profile.relay_timeout,logger,this);
				log(LogLevel.DEBUG,"MGW started: "+symm_relay);
				dump("MGW started: "+symm_relay);
			}

			return symm_relay;
		}
		catch (Exception e) {
			log(LogLevel.INFO,e);
			return null;
		}
	}


	// ********************** SymmetricUdpRelay callbacks *********************

	/** When left peer address changes. */
	public void onSymmetricUdpRelayLeftPeerChanged(SymmetricUdpRelay symm_relay, SocketAddress soaddr) {
		log(LogLevel.INFO,"change left peer soaddr "+soaddr);
		// handover?
		long htime=sbc_profile.handover_time;
		if (htime>0 && (System.currentTimeMillis()+htime)<symm_relay.getLastLeftChangeTime()) return;
		// else
		symm_relay.setLeftSoAddress(soaddr);
		dump("MGW change L: "+symm_relay);
	}


	/** When right peer address changes. */
	public void onSymmetricUdpRelayRightPeerChanged(SymmetricUdpRelay symm_relay, SocketAddress soaddr) {
		log(LogLevel.DEBUG,"change right peer soaddr "+soaddr);
		// handover?
		long htime=sbc_profile.handover_time;
		if (htime>0 && (System.currentTimeMillis()+htime)<symm_relay.getLastRightChangeTime()) return;
		// else
		symm_relay.setRightSoAddress(soaddr);
		dump("MGW change R: "+symm_relay);
	}


	/** When it stops relaying UDP datagrams (both directions). */
	public void onSymmetricUdpRelayTerminated(SymmetricUdpRelay symm_relay) {
		log(LogLevel.DEBUG,"MGW terminated: "+symm_relay);
		dump("MGW terminated: "+symm_relay);
	}
	
	
	// ****************************** Logs *****************************

	/** Adds a new string to the Dumper log. */
	private void dump(String str) {
		if (dumper!=null) dumper.log("GW: "+str);
	}

	/** Adds a new string to the default Log. */
	private void log(LogLevel level, String str) {
		if (logger!=null) logger.log(level,"MediaGw: "+str);  
	}

	/** Adds the Exception message to the default Log. */
	private void log(LogLevel level, Exception e) {
		log(level,"Exception: "+ExceptionPrinter.getStackTraceOf(e));
	}

}
