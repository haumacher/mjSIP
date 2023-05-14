/*
 * Copyright (C) 2008 Luca Veltri - University of Parma - Italy
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



import java.io.File;
import java.net.URL;
import java.util.Enumeration;
import java.util.Vector;

import org.mjsip.media.AudioClipPlayer;
import org.mjsip.media.FlowSpec;
import org.mjsip.media.MediaDesc;
import org.mjsip.media.MediaSpec;
import org.mjsip.sdp.MediaDescriptor;
import org.mjsip.sdp.OfferAnswerModel;
import org.mjsip.sdp.SdpMessage;
import org.mjsip.sdp.field.MediaField;
import org.mjsip.sip.address.NameAddress;
import org.mjsip.sip.address.SipURI;
import org.mjsip.sip.call.Call;
import org.mjsip.sip.call.CallListenerAdapter;
import org.mjsip.sip.call.ExtendedCall;
import org.mjsip.sip.call.ExtendedCallListener;
import org.mjsip.sip.call.NotImplementedServer;
import org.mjsip.sip.call.OptionsServer;
import org.mjsip.sip.call.RegistrationClient;
import org.mjsip.sip.call.RegistrationClientListener;
import org.mjsip.sip.call.SipUser;
import org.mjsip.sip.message.SipMessage;
import org.mjsip.sip.message.SipMethods;
import org.mjsip.sip.provider.MethodId;
import org.mjsip.sip.provider.SipKeepAlive;
import org.mjsip.sip.provider.SipParser;
import org.mjsip.sip.provider.SipProvider;
import org.mjsip.sip.provider.SipProviderListener;
import org.mjsip.sip.provider.SipStack;
import org.zoolu.net.SocketAddress;
import org.zoolu.util.Archive;
import org.zoolu.util.ExceptionPrinter;
import org.zoolu.util.LogLevel;
import org.zoolu.util.Logger;
import org.zoolu.util.Timer;
import org.zoolu.util.TimerListener;



/** Simple SIP call agent (signaling and media).
  * It supports both audio and video sessions, by means of embedded media applications
  * that can use the default Java sound support (javax.sound.sampled.AudioSystem)
  * and/or the Java Multimedia Framework (JMF).
  * <p>
  * As media applications it can also use external audio/video tools.
  * Currently only support for RAT (Robust Audio Tool) and VIC has been implemented.
  */
public class UserAgent extends CallListenerAdapter implements SipProviderListener, ExtendedCallListener, RegistrationClientListener, TimerListener {
	

	/** On wav file */
	static final String CLIP_ON="on.wav";
	/** Off wav file */
	static final String CLIP_OFF="off.wav";
	/** Ring wav file */
	static final String CLIP_RING="ring.wav";
	/** Progress wav file */
	static final String CLIP_PROGRESS="progress.wav";


	// ***************************** attributes ****************************

	/** Logger */
	Logger logger;
	
	/** UserAgentProfile */
	protected UserAgentProfile ua_profile;

	/** SipProvider */
	protected SipProvider sip_provider;

	/** RegistrationClient */
	protected RegistrationClient rc=null;

	/** SipKeepAlive daemon */
	protected SipKeepAlive keep_alive;

	/** Call */
	protected ExtendedCall call;
	/** Call transfer */
	protected ExtendedCall call_transfer;

	/** UAS */
	//protected CallWatcher ua_server;

	/** OptionsServer */
	protected OptionsServer options_server;

	/** NotImplementedServer */
	protected NotImplementedServer null_server;

	/** MediaAgent */
	MediaAgent media_agent;
	
	/** List of active media sessions */
	protected Vector media_sessions=new Vector();

	/** Current local media descriptions */
	protected MediaDesc[] media_descs=null;

	/** UserAgent listener */
	protected UserAgentListener listener=null;

	/** Response timeout */
	Timer response_to=null;

	/** Whether the outgoing call is already in progress */
	boolean progress;   
	/** Whether the outgoing call is already ringing */
	boolean ringing;

	/** On sound */
	AudioClipPlayer clip_on;
	/** Off sound */
	AudioClipPlayer clip_off;
	/** Ring sound */
	AudioClipPlayer clip_ring;
	/** Progress sound */
	AudioClipPlayer clip_progress;

	/** On volume gain */
	float clip_on_volume_gain=(float)0.0; // not changed
	/** Off volume gain */
	float clip_off_volume_gain=(float)0.0; // not changed
	/** Ring volume gain */
	float clip_ring_volume_gain=(float)0.0; // not changed
	/** Progress volume gain */
	float clip_progress_volume_gain=(float)0.0; // not changed


	// **************************** constructors ***************************

	/** Creates a new UserAgent. */
	public UserAgent(SipProvider sip_provider, UserAgentProfile ua_profile, UserAgentListener listener) {
		init(sip_provider,ua_profile,listener);
	} 


	// ************************** private methods **************************

	/** Inits the UserAgent */
	private void init(SipProvider sip_provider, UserAgentProfile ua_profile, UserAgentListener listener) {
		this.sip_provider=sip_provider;
		logger=sip_provider.getLogger();
		this.listener=listener;
		this.ua_profile=ua_profile;
		// update user profile information
		ua_profile.setUnconfiguredAttributes(sip_provider);

		// log main config parameters
		log(LogLevel.DEBUG,"ua_address: "+ua_profile.ua_address);
		log(LogLevel.DEBUG,"user's uri: "+ua_profile.getUserURI());
		log(LogLevel.DEBUG,"proxy: "+ua_profile.proxy);
		log(LogLevel.DEBUG,"registrar: "+ua_profile.registrar);
		log(LogLevel.DEBUG,"auth_realm: "+ua_profile.auth_realm);
		log(LogLevel.DEBUG,"auth_user: "+ua_profile.auth_user);
		log(LogLevel.DEBUG,"auth_passwd: ******");
		log(LogLevel.DEBUG,"audio: "+ua_profile.audio);
		log(LogLevel.DEBUG,"video: "+ua_profile.video);
		for (int i=0; i<ua_profile.media_descs.length; i++) {
			log(LogLevel.DEBUG,"media: "+(ua_profile.media_descs[i]).toString());
		}      
		// log other config parameters
		log(LogLevel.TRACE,"loopback: "+ua_profile.loopback);
		log(LogLevel.TRACE,"send_only: "+ua_profile.send_only);
		log(LogLevel.TRACE,"recv_only: "+ua_profile.recv_only);
		log(LogLevel.TRACE,"send_file: "+ua_profile.send_file);
		log(LogLevel.TRACE,"recv_file: "+ua_profile.recv_file);
		log(LogLevel.TRACE,"send_tone: "+ua_profile.send_tone);

		// start listening for INVITE requests (UAS)
		if (ua_profile.ua_server) sip_provider.addSelectiveListener(new MethodId(SipMethods.INVITE),this);
		
		// start OPTIONS server
		if (ua_profile.options_server) options_server=new OptionsServer(sip_provider,"INVITE, ACK, CANCEL, OPTIONS, BYE","application/sdp");

		// start "Not Implemented" server
		if (ua_profile.null_server) null_server=new NotImplementedServer(sip_provider);

		// init media agent
		media_agent=new MediaAgent(ua_profile,logger);

		// load sounds
		// ################# patch to make rat working.. #################
		// in case of rat, do not load and play audio clips
		if (!ua_profile.use_rat && !ua_profile.no_system_audio) {
			try {
				clip_on=getAudioClip(ua_profile.ua_jar,ua_profile.res_path,ua_profile.media_path+"/"+CLIP_ON);
				clip_off=getAudioClip(ua_profile.ua_jar,ua_profile.res_path,ua_profile.media_path+"/"+CLIP_OFF);
				clip_ring=getAudioClip(ua_profile.ua_jar,ua_profile.res_path,ua_profile.media_path+"/"+CLIP_RING);
				clip_progress=getAudioClip(ua_profile.ua_jar,ua_profile.res_path,ua_profile.media_path+"/"+CLIP_PROGRESS);
				
				clip_ring.setLoop();
				clip_progress.setLoop();
				clip_on.setVolumeGain(clip_on_volume_gain);
				clip_off.setVolumeGain(clip_off_volume_gain);
				clip_ring.setVolumeGain(clip_ring_volume_gain);
				clip_progress.setVolumeGain(clip_progress_volume_gain);
			}
			catch (Exception e) {
				log(LogLevel.INFO,e);
			}
		}
	}


	/** Inits the RegistrationClient */
	private void initRegistrationClient() {
		rc=new RegistrationClient(sip_provider,new SipURI(ua_profile.registrar),ua_profile.getUserURI(),ua_profile.auth_user,ua_profile.auth_realm,ua_profile.auth_passwd,this);
	}


	/** Gets SessionDescriptor from Vector of MediaSpec. */
	private SdpMessage getSessionDescriptor(MediaDesc[] media_descs) {
		String owner=ua_profile.user;
		String media_addr=(ua_profile.media_addr!=null)? ua_profile.media_addr : sip_provider.getViaAddress();
		SdpMessage sd=new SdpMessage(owner,media_addr);
		for (int i=0; i<media_descs.length; i++) {
			MediaDesc md=media_descs[i];
			// check if audio or video have been disabled
			if (md.getMedia().equalsIgnoreCase("audio") && !ua_profile.audio) continue;
			if (md.getMedia().equalsIgnoreCase("video") && !ua_profile.video) continue;
			// else
			sd.addMediaDescriptor(md.toMediaDescriptor());
		}
		return sd;
	}


	/** Creates a new SessionDescriptor from owner, address, and Vector of MediaDesc. */
	/*private static SessionDescriptor newSessionDescriptor(String owner, String media_addr, Vector media_descs) {
		SessionDescriptor sd=new SessionDescriptor(owner,media_addr);
		for (int i=0; i<media_descs.size(); i++) sd.addMediaDescriptor((media_descs[i]).toMediaDescriptor());
		return sd;
	}*/


	/** Sets new media descriptions. */
	public void setMediaDescription(MediaDesc[] media_descs) {
		this.media_descs=media_descs;
	}


	/** Gets a NameAddress based on an input string.
	  * The input string can be a:
	  * <br> - user name,
	  * <br> - an address of type <i>user@address</i>,
	  * <br> - a complete address in the form of <i>"Name" &lt;sip:user@address&gt;</i>,
	  * <p>
	  * In the former case, a SIP URI is costructed using the proxy address
	  * if available. */
	private NameAddress completeNameAddress(String str) {
		if (new SipParser(str).indexOf(SipParser.naddr_uri_schemes)>=0) return new NameAddress(str);
		else {
			SipURI uri=completeSipURI(str);
			return new NameAddress(uri);
		}
	}


	/** Gets a SipURI based on an input string. */
	private SipURI completeSipURI(String str) {
		// in case it is passed only the user field, add "@" + proxy address
		if (ua_profile.proxy!=null && !str.startsWith("sip:") && !str.startsWith("sips:") && str.indexOf("@")<0 && str.indexOf(".")<0 && str.indexOf(":")<0) {
			// may be it is just the user name..
			return new SipURI(str,ua_profile.proxy);
		}
		else return new SipURI(str);
	}


	// *************************** public methods **************************

	/** Sets the automatic answer time (default is -1 that means no auto accept mode) */
	/*public void setAcceptTime(int accept_time) {
		ua_profile.accept_time=accept_time; 
	}*/

	/** Sets the automatic hangup time (default is 0, that corresponds to manual hangup mode) */
	/*public void setHangupTime(int time) {
		ua_profile.hangup_time=time; 
	}*/

	/** Sets the redirection URI (default is null, that is no redircetion) */
	/*public void setRedirection(NameAddress uri) {
		ua_profile.redirect_to=uri; 
	}*/

	/** Sets the no offer mode for the invite (default is false) */
	/*public void setNoOfferMode(boolean nooffer) {
		ua_profile.no_offer=nooffer;
	}*/

	/** Enables audio */
	/*public void setAudio(boolean enable) {
		ua_profile.audio=enable;
	}*/

	/** Enables video */
	/*public void setVideo(boolean enable) {
		ua_profile.video=enable;
	}*/

	/** Sets the receive only mode */
	/*public void setReceiveOnlyMode(boolean r_only) {
		ua_profile.recv_only=r_only;
	}*/

	/** Sets the send only mode */
	/*public void setSendOnlyMode(boolean s_only) {
		ua_profile.send_only=s_only;
	}*/

	/** Sets the send tone mode */
	/*public void setSendToneMode(boolean s_tone) {
		ua_profile.send_tone=s_tone;
	}*/

	/** Sets the send file */
	/*public void setSendFile(String file_name) {
		ua_profile.send_file=file_name;
	}*/

	/** Sets the recv file */
	/*public void setRecvFile(String file_name) {
		ua_profile.recv_file=file_name;
	}*/

	/** Gets the local SDP */
	/*public String getLocalSDP() {
		return session_descriptor.toString();
	}*/  

	/** Sets the local SDP */
	/*public void setLocalSDP(String sdp) {
		session_descriptor=new SessionDescriptor(sdp);
	}*/


	/** Register with the registrar server
	  * @param expire_time expiration time in seconds */
	public void register(int expire_time) {
		if (rc.isRegistering()) rc.halt();
		rc.register(expire_time);
	}


	/** Periodically registers the contact address with the registrar server.
	  * @param expire_time expiration time in seconds
	  * @param renew_time renew time in seconds
	  * @param keepalive_time keep-alive packet rate (inter-arrival time) in milliseconds */
	public void loopRegister(int expire_time, int renew_time, long keepalive_time) {
		// create registration client
		if (rc==null) initRegistrationClient();
		// stop previous operation
		if (rc.isRegistering()) rc.halt();
		// start registering
		rc.loopRegister(expire_time,renew_time);
		// keep-alive
		if (keepalive_time>0) {
			SipURI target_uri=(sip_provider.hasOutboundProxy())? sip_provider.getOutboundProxy() : new SipURI(rc.getTargetAOR().getAddress());
			String target_host=target_uri.getHost();
			int target_port=target_uri.getPort();
			if (target_port<0) target_port=SipStack.default_port;
			SocketAddress target_soaddr=new SocketAddress(target_host,target_port);
			if (keep_alive!=null && keep_alive.isRunning()) keep_alive.halt();
			keep_alive=new SipKeepAlive(sip_provider,target_soaddr,null,keepalive_time);
		}
	}


	/** Unregisters with the registrar server */
	public void unregister() {
		// create registration client
		if (rc==null) initRegistrationClient();
		// stop registering
		if (keep_alive!=null && keep_alive.isRunning()) keep_alive.halt();
		if (rc.isRegistering()) rc.halt();
		// unregister
		rc.unregister();
	}


	/** Unregister all contacts with the registrar server */
	public void unregisterall() {
		// create registration client
		if (rc==null) initRegistrationClient();
		// stop registering
		if (keep_alive!=null && keep_alive.isRunning()) keep_alive.halt();
		if (rc.isRegistering()) rc.halt();
		// unregister
		rc.unregisterall();
	}


	/** Makes a new call (acting as UAC). */
	public void call(String callee) {
		call(callee,null);
	}


	/** Makes a new call (acting as UAC) with specific media description (Vector of MediaDesc). */
	public void call(String callee, MediaDesc[] media_descs) {
		// in case of incomplete URI (e.g. only 'user' is present), try to complete it
		call(completeNameAddress(callee),media_descs);
	}


	/** Makes a new call (acting as UAC). */
	public void call(NameAddress callee) {
		call(callee,(MediaDesc[])null);
	}


	/** Makes a new call (acting as UAC) with specific media descriptions. */
	public void call(NameAddress callee, MediaDesc[] media_descs) {
		// new media description
		if (media_descs==null) media_descs=ua_profile.media_descs;
		this.media_descs=media_descs;
		// new call
		SdpMessage sdp=ua_profile.no_offer? null : getSessionDescriptor(media_descs);
		call(callee,sdp);
	}


	/** Makes a new call (acting as UAC) with specific SDP. */
	public void call(NameAddress callee, SdpMessage sdp) {
		call=new ExtendedCall(sip_provider,new SipUser(ua_profile.getUserURI(),ua_profile.auth_user,ua_profile.auth_realm,ua_profile.auth_passwd),this);      
		if (ua_profile.no_offer) call.call(callee);
		else {
			call.call(callee,sdp.toString());
		}
		progress=false;
		ringing=false;
	}


	/** Waits for an incoming call (acting as UAS). */
	/*public void listen() {
		new CallWatcher(sip_provider,ua_profile.contact_uri,this);
	}*/


	/** Closes an ongoing, incoming, or pending call. */
	public void hangup() {
		// sound
		if (clip_progress!=null) clip_progress.stop();
		if (clip_ring!=null) clip_ring.stop();
		// response timeout
		if (response_to!=null) response_to.halt();

		closeMediaSessions();
		if (call!=null) call.hangup();
		call=null;
	} 


	/** Accepts an incoming call. */
	public void accept() {
		accept(null);
	}


	/** Accepts an incoming call with specific media description (Vector of MediaDesc). */
	public void accept(MediaDesc[] media_descs) {
		// sound
		if (clip_ring!=null) clip_ring.stop();
		// response timeout
		if (response_to!=null) response_to.halt();
		// return if no active call
		if (call==null) return;
		// else
		// new media description
		if (media_descs==null) media_descs=ua_profile.media_descs;
		this.media_descs=media_descs;
		// new sdp
		SdpMessage local_sdp=getSessionDescriptor(media_descs);
		SdpMessage remote_sdp=new SdpMessage(call.getRemoteSessionDescriptor());
		SdpMessage new_sdp=new SdpMessage(local_sdp.getOrigin(),remote_sdp.getSessionName(),local_sdp.getConnection(),remote_sdp.getTime());
		new_sdp.addMediaDescriptors(local_sdp.getMediaDescriptors());
		new_sdp=OfferAnswerModel.makeSessionDescriptorProduct(new_sdp,remote_sdp);
		// accept
		call.accept(new_sdp.toString());
	}


	/** Redirects an incoming call. */
	public void redirect(String redirect_to) {
		// in case of incomplete URI (e.g. only 'user' is present), try to complete it
		redirect(completeNameAddress(redirect_to));
	}


	/** Redirects an incoming call. */
	public void redirect(NameAddress redirect_to) {
		// sound
		if (clip_ring!=null) clip_ring.stop();
		// response timeout
		if (response_to!=null) response_to.halt();
		
		if (call!=null) call.redirect(redirect_to);
	}   


	/** Modifies the current session. It re-invites the remote party changing the contact URI and SDP. */
	public void modify(String body) {
		if (call!=null && call.getState().isActive()) {
			log("RE-INVITING/MODIFING");
			call.modify(body);
		}
	}


	/** Transfers the current call to a remote UA. */
	public void transfer(String transfer_to) {
		// in case of incomplete URI (e.g. only 'user' is present), try to complete it
		transfer(completeNameAddress(transfer_to));
	}


	/** Transfers the current call to a remote UA. */
	public void transfer(NameAddress transfer_to) {
		if (call!=null && call.getState().isActive()) {
			log("REFER/TRANSFER");
			call.transfer(transfer_to);
		}
	}


	// ********************** protected methods **********************

	/** Starts media sessions (audio and/or video). */
	protected void startMediaSessions() {
		
		// exit if the media application is already running  
		if (media_sessions.size()>0) {
			log(LogLevel.DEBUG,"media sessions already active");
			return;
		}
		// get local and remote rtp addresses and ports
		SdpMessage local_sdp=new SdpMessage(call.getLocalSessionDescriptor());
		SdpMessage remote_sdp=new SdpMessage(call.getRemoteSessionDescriptor());
		String local_address=local_sdp.getConnection().getAddress();
		String remote_address=remote_sdp.getConnection().getAddress();
		// calculate media descriptor product
		Vector md_list=OfferAnswerModel.makeMediaDescriptorProduct(local_sdp.getMediaDescriptors(),remote_sdp.getMediaDescriptors());
		// select the media direction (send_only, recv_ony, fullduplex)
		FlowSpec.Direction dir=FlowSpec.FULL_DUPLEX;
		if (ua_profile.recv_only) dir=FlowSpec.RECV_ONLY;
		else
		if (ua_profile.send_only) dir=FlowSpec.SEND_ONLY;
		// for each media
		for (Enumeration ei=md_list.elements(); ei.hasMoreElements(); ) {
			MediaField md=((MediaDescriptor)ei.nextElement()).getMedia();
			String media=md.getMedia();
			// local and remote ports
			int local_port=md.getPort();
			int remote_port=remote_sdp.getMediaDescriptor(media).getMedia().getPort();
			remote_sdp.removeMediaDescriptor(media);
			// media and flow specifications
			String transport=md.getTransport();
			String format=(String)md.getFormatList().elementAt(0);
			int avp=Integer.parseInt(format);
			MediaSpec media_spec=null;
			for (int i=0; i<media_descs.length && media_spec==null; i++) {
				MediaDesc media_desc=media_descs[i];
				if (media_desc.getMedia().equalsIgnoreCase(media)) {
					MediaSpec[] media_specs=media_desc.getMediaSpecs();
					for (int j=0; j<media_specs.length && media_spec==null; j++) {
						MediaSpec ms=(MediaSpec)media_specs[j];
						if (ms.getAVP()==avp) media_spec=ms;
					}
				}
			}
			if (local_port!=0 && remote_port!=0 && media_spec!=null) {
				FlowSpec flow_spec=new FlowSpec(media_spec,local_port,remote_address,remote_port,dir);
				log(media+" format: "+flow_spec.getMediaSpec().getCodec());
				boolean success=media_agent.startMediaSession(flow_spec);           
				if (success) {
					media_sessions.addElement(media);
					if (listener!=null) listener.onUaMediaSessionStarted(this,media,format);
				}
			}
			else {
				log("DEBUG: media session cannot be started (local_port="+local_port+", remote_port="+remote_port+", media_spec="+media_spec+").");
			}
		}
	}
 
	
	/** Closes media sessions.  */
	protected void closeMediaSessions() {
		for (int i=0; i<media_sessions.size(); i++) {
			String media=(String)media_sessions.elementAt(i);
			media_agent.stopMediaSession(media);
			if (listener!=null) listener.onUaMediaSessionStopped(this,media);
		}
		media_sessions.removeAllElements();
	}


	// ************************* RA callbacks ************************

	/** From RegistrationClientListener. When it has been successfully (un)registered. */
	public void onRegistrationSuccess(RegistrationClient rc, NameAddress target, NameAddress contact, int expires, String result) {
		log(LogLevel.INFO,"Registration success: expires="+expires+": "+result);
		if (listener!=null) listener.onUaRegistrationSucceeded(this,result);   
	}

	/** From RegistrationClientListener. When it failed on (un)registering. */
	public void onRegistrationFailure(RegistrationClient rc, NameAddress target, NameAddress contact, String result) {
		log(LogLevel.INFO,"Registration failure: "+result);
		if (listener!=null) listener.onUaRegistrationFailed(this,result);
	}


	// ************************ Call callbacks ***********************
	
	/** From SipProviderListener. When a new SipMessage is received by the SipProvider. */
	public void onReceivedMessage(SipProvider sip_provider, SipMessage message) {
		new ExtendedCall(sip_provider,message,this);
	}


	/** From CallListener. Callback function called when arriving a new INVITE method (incoming call) */
	public void onCallInvite(Call call, NameAddress callee, NameAddress caller, String sdp, SipMessage invite) {
		log(LogLevel.DEBUG,"onCallInvite()");
		if (this.call!=null && !this.call.getState().isClosed()) {
			log(LogLevel.INFO,"LOCALLY BUSY: INCOMING CALL REFUSED");
			call.refuse();
			return;
		}
		// else   
		log(LogLevel.INFO,"INCOMING");
		this.call=(ExtendedCall)call;
		call.ring();
		// sound
		if (clip_ring!=null) clip_ring.play();
		// response timeout
		if (ua_profile.refuse_time>=0) response_to=new Timer(ua_profile.refuse_time*1000,this);
		response_to.start();
		
		MediaDesc[] media_descs=new MediaDesc[]{};
		if (sdp!=null) {
			Vector md_list=(new SdpMessage(sdp)).getMediaDescriptors();
			media_descs=new MediaDesc[md_list.size()];
			for (int i=0; i<md_list.size(); i++) media_descs[i]=new MediaDesc((MediaDescriptor)md_list.elementAt(i));
		}
		if (listener!=null) listener.onUaIncomingCall(this,callee,caller,media_descs);
	}  


	/** From CallListener. Callback function called when arriving a new Re-INVITE method (re-inviting/call modify) */
	public void onCallModify(Call call, String sdp, SipMessage invite) {
		log(LogLevel.DEBUG,"onCallModify()");
		if (call!=this.call) {  log(LogLevel.DEBUG,"NOT the current call");  return;  }
		log(LogLevel.INFO,"RE-INVITE/MODIFY");
		// to be implemented.
		// currently it simply accepts the session changes (see method onCallModify() in CallListenerAdapter)
		super.onCallModify(call,sdp,invite);
	}


	/** From CallListener. Callback function called when arriving a 183 Session Progress */
	public void onCallProgress(Call call, SipMessage resp) {
		log(LogLevel.DEBUG,"onCallProgress()");
		if (call!=this.call && call!=call_transfer) {  log(LogLevel.DEBUG,"NOT the current call");  return;  }
		if (!progress) {
			log(LogLevel.INFO,"PROGRESS");
			progress=true;
			// sound
			if (clip_progress!=null) clip_progress.play();
			
			if (listener!=null) listener.onUaCallProgress(this);
		}
	}


	/** From CallListener. Callback function that may be overloaded (extended). Called when arriving a 180 Ringing */
	public void onCallRinging(Call call, SipMessage resp) {
		log(LogLevel.DEBUG,"onCallRinging()");
		if (call!=this.call && call!=call_transfer) {  log(LogLevel.DEBUG,"NOT the current call");  return;  }
		if (!ringing) {
			log(LogLevel.INFO,"RINGING");
			ringing=true;
			// sound
			if (clip_progress!=null) clip_progress.play();
			
			if (listener!=null) listener.onUaCallRinging(this);
		}
	}


	/** Callback function called when arriving a 1xx response (e.g. 183 Session Progress) that has to be confirmed */
	public void onCallConfirmableProgress(Call call, SipMessage resp) {
		// TODO
	}

	/** Callback function called when arriving a PRACK for a reliable 1xx response, that had to be confirmed */
	public void onCallProgressConfirmed(Call call, SipMessage resp, SipMessage prack) {
		// TODO
	}

	/** From CallListener. Callback function called when arriving a 2xx (call accepted) */
	public void onCallAccepted(Call call, String sdp, SipMessage resp) {
		log(LogLevel.DEBUG,"onCallAccepted()");
		if (call!=this.call && call!=call_transfer) {  log(LogLevel.DEBUG,"NOT the current call");  return;  }
		log(LogLevel.INFO,"ACCEPTED/CALL");
		if (ua_profile.no_offer) {
			// new sdp
			SdpMessage local_sdp=getSessionDescriptor(media_descs);
			SdpMessage remote_sdp=new SdpMessage(sdp);
			SdpMessage new_sdp=new SdpMessage(local_sdp.getOrigin(),remote_sdp.getSessionName(),local_sdp.getConnection(),remote_sdp.getTime());
			new_sdp.addMediaDescriptors(local_sdp.getMediaDescriptors());
			new_sdp=OfferAnswerModel.makeSessionDescriptorProduct(new_sdp,remote_sdp);         
			// answer with the local sdp
			call.confirm2xxWithAnswer(new_sdp.toString());
		}
		// sound
		if (clip_progress!=null) clip_progress.stop();
		if (clip_on!=null) clip_on.play();
		
		if (listener!=null) listener.onUaCallAccepted(this);

		startMediaSessions();
		
		if (call==call_transfer) {
			this.call.notify(resp);
		}
	}


	/** From CallListener. Callback function called when arriving an ACK method (call confirmed) */
	public void onCallConfirmed(Call call, String sdp, SipMessage ack) {
		log(LogLevel.DEBUG,"onCallConfirmed()");
		if (call!=this.call) {  log(LogLevel.DEBUG,"NOT the current call");  return;  }
		log(LogLevel.INFO,"CONFIRMED/CALL");
		// sound
		if (clip_on!=null) clip_on.play();
		
		startMediaSessions();
	}


	/** From CallListener. Callback function called when arriving a 2xx (re-invite/modify accepted) */
	public void onCallModifyAccepted(Call call, String sdp, SipMessage resp) {
		log(LogLevel.DEBUG,"onCallModifyAccepted()");
		if (call!=this.call) {  log(LogLevel.DEBUG,"NOT the current call");  return;  }
		log(LogLevel.INFO,"RE-INVITE-ACCEPTED/CALL");
	}


	/** From CallListener. Callback function called when arriving a 4xx (re-invite/modify failure) */
	public void onCallModifyRefused(Call call, String reason, SipMessage resp) {
		log(LogLevel.DEBUG,"onCallReInviteRefused()");
		if (call!=this.call) {  log(LogLevel.DEBUG,"NOT the current call");  return;  }
		log(LogLevel.INFO,"RE-INVITE-REFUSED ("+reason+")/CALL");
		if (listener!=null) listener.onUaCallFailed(this,reason);
	}


	/** From CallListener. Callback function called when arriving a 4xx (call failure) */
	public void onCallRefused(Call call, String reason, SipMessage resp) {
		log(LogLevel.DEBUG,"onCallRefused()");
		if (call!=this.call && call!=call_transfer) {  log(LogLevel.DEBUG,"NOT the current call");  return;  }
		log(LogLevel.INFO,"REFUSED ("+reason+")");
		if (call==call_transfer) {
			this.call.notify(resp);
			call_transfer=null;
		}
		else this.call=null;
		// sound
		if (clip_progress!=null) clip_progress.stop();
		if (clip_off!=null) clip_off.play();
		
		if (listener!=null) listener.onUaCallFailed(this,reason);
	}


	/** From CallListener. Callback function called when arriving a 3xx (call redirection) */
	public void onCallRedirected(Call call, String reason, Vector contact_list, SipMessage resp) {
		log(LogLevel.DEBUG,"onCallRedirected()");
		if (call!=this.call) {  log(LogLevel.DEBUG,"NOT the current call");  return;  }
		log(LogLevel.INFO,"REDIRECTION ("+reason+")");
		NameAddress first_contact=new NameAddress((String)contact_list.elementAt(0));
		call.call(first_contact); 
	}


	/** From CallListener. Callback function called when arriving a CANCEL request */
	public void onCallCancel(Call call, SipMessage cancel) {
		log(LogLevel.DEBUG,"onCallCancel()");
		if (call!=this.call) {  log(LogLevel.DEBUG,"NOT the current call");  return;  }
		log(LogLevel.INFO,"CANCEL");
		this.call=null;
		// sound
		if (clip_ring!=null) clip_ring.stop();
		if (clip_off!=null) clip_off.play();
		// response timeout
		if (response_to!=null) response_to.halt();
		
		if (listener!=null) listener.onUaCallCancelled(this);
	}


	/** From CallListener. Callback function called when arriving a BYE request */
	public void onCallBye(Call call, SipMessage bye) {
		log(LogLevel.DEBUG,"onCallBye()");
		if (call!=this.call && call!=call_transfer) {  log(LogLevel.DEBUG,"NOT the current call");  return;  }
		if (call!=call_transfer && call_transfer!=null) {
			log(LogLevel.INFO,"CLOSE PREVIOUS CALL");
			this.call=call_transfer;
			call_transfer=null;
			return;
		}
		// else
		log(LogLevel.INFO,"CLOSE");
		this.call=null;
		closeMediaSessions();
		// sound
		if (clip_off!=null) clip_off.play();
		
		if (listener!=null) listener.onUaCallClosed(this);
	}


	/** From CallListener. Callback function called when arriving a response after a BYE request (call closed) */
	public void onCallClosed(Call call, SipMessage resp) {
		log("LogLevel.DEBUG,onCallClosed()");
		if (call!=this.call) {  log(LogLevel.DEBUG,"NOT the current call");  return;  }
		log(LogLevel.INFO,"CLOSE/OK");
		if (listener!=null) listener.onUaCallClosed(this);
	}

	/** Callback function called when the invite expires */
	public void onCallTimeout(Call call) {
		log(LogLevel.DEBUG,"onCallTimeout()");
		if (call!=this.call) {  log(LogLevel.DEBUG,"NOT the current call");  return;  }
		log(LogLevel.INFO,"NOT FOUND/TIMEOUT");
		int code=408;
		String reason="Request Timeout";
		if (call==call_transfer) {
			this.call.notify(code,reason);
			call_transfer=null;
		}
		// sound
		if (clip_off!=null) clip_off.play();
		
		if (listener!=null) listener.onUaCallFailed(this,reason);
	}


	// ******************* ExtendedCall callbacks ********************


	/** From ExtendedCallListener. Callback function called when arriving a new UPDATE method (update request). */
	public void onCallUpdate(ExtendedCall call, String sdp, SipMessage update) {
		log(LogLevel.DEBUG,"onCallUpdate()");
		if (call!=this.call) {  log(LogLevel.DEBUG,"NOT the current call");  return;  }
		log(LogLevel.INFO,"UPDATE");
		// to be implemented.
		// currently it simply accepts the session changes (see method onCallModify() in CallListenerAdapter)
		super.onCallUpdate(call,sdp,update);
	}


	/** From ExtendedCallListener. Callback function called when arriving a new REFER method (transfer request) */
	public void onCallTransfer(ExtendedCall call, NameAddress refer_to, NameAddress refered_by, SipMessage refer) {
		log(LogLevel.DEBUG,"onCallTransfer()");
		if (call!=this.call) {  log(LogLevel.DEBUG,"NOT the current call");  return;  }
		log(LogLevel.INFO,"transfer to "+refer_to.toString());
		call.acceptTransfer();
		call_transfer=new ExtendedCall(sip_provider,new SipUser(ua_profile.getUserURI()),this);
		call_transfer.call(refer_to,getSessionDescriptor(media_descs).toString());
	}

	/** From ExtendedCallListener. Callback function called when a call transfer is accepted. */
	public void onCallTransferAccepted(ExtendedCall call, SipMessage resp) {
		log(LogLevel.DEBUG,"onCallTransferAccepted()");
		if (call!=this.call) {  log(LogLevel.DEBUG,"NOT the current call");  return;  }
		log(LogLevel.INFO,"transfer accepted");
	}

	/** From ExtendedCallListener. Callback function called when a call transfer is refused. */
	public void onCallTransferRefused(ExtendedCall call, String reason, SipMessage resp) {
		log(LogLevel.DEBUG,"onCallTransferRefused()");
		if (call!=this.call) {  log(LogLevel.DEBUG,"NOT the current call");  return;  }
		log(LogLevel.INFO,"transfer refused");
	}

	/** From ExtendedCallListener. Callback function called when a call transfer is successfully completed */
	public void onCallTransferSuccess(ExtendedCall call, SipMessage notify) {
		log(LogLevel.DEBUG,"onCallTransferSuccess()");
		if (call!=this.call) {  log(LogLevel.TRACE,"NOT the current call");  return;  }
		log(LogLevel.INFO,"transfer successed");
		call.hangup();
		if (listener!=null) listener.onUaCallTransferred(this);
	}

	/** From ExtendedCallListener. Callback function called when a call transfer is NOT sucessfully completed */
	public void onCallTransferFailure(ExtendedCall call, String reason, SipMessage notify) {
		log(LogLevel.DEBUG,"onCallTransferFailure()");
		if (call!=this.call) {  log(LogLevel.DEBUG,"NOT the current call");  return;  }
		log(LogLevel.INFO,"transfer failed");
	}


	// *********************** Timer callbacks ***********************

	/** When the Timer exceeds. */
	public void onTimeout(Timer t) {
		if (response_to==t) {
			log(LogLevel.INFO,"response time expired: incoming call declined");
			if (call!=null) call.refuse();
			// sound
			if (clip_ring!=null) clip_ring.stop();
		}
	}


	// **************************** Static ****************************

	private static AudioClipPlayer getAudioClip(String jar_file, String res_path, String image_file) throws java.io.IOException {
		if (jar_file!=null && new File(jar_file).canRead()) {
			return new AudioClipPlayer(Archive.getJarURL(jar_file,image_file),null);
		}
		else
		if (new File(res_path+"/"+image_file).canRead()) {
			return new AudioClipPlayer(res_path+"/"+image_file,null);
		}
		else {
			return new AudioClipPlayer(new URL(res_path+"/"+image_file),null);
		}   
	}


	// ***************************** logs ****************************

	/** Adds a new string to the default Log. */
	public void log(String str) {
		log(LogLevel.INFO,str);
	}

	/** Adds a new string to the default Log. */
	public void log(LogLevel level, String str) {
		if (logger!=null) logger.log(level,this.getClass(),str);  
		else if ((ua_profile==null || !ua_profile.no_prompt) && level.getValue()>=LogLevel.INFO.getValue()) System.out.println("UA: "+str);
	}

	/** Adds the Exception message to the default Log. */
	public void log(LogLevel level, Exception e) {
		log(level,"Exception: "+ExceptionPrinter.getStackTraceOf(e));
	}

}
