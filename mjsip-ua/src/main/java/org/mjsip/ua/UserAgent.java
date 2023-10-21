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



import java.util.Vector;
import java.util.concurrent.ScheduledFuture;

import org.mjsip.media.AudioClipPlayer;
import org.mjsip.media.FlowSpec;
import org.mjsip.media.FlowSpec.Direction;
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
import org.mjsip.sip.call.DTMFInfo;
import org.mjsip.sip.call.ExtendedCall;
import org.mjsip.sip.call.NotImplementedServer;
import org.mjsip.sip.call.OptionsServer;
import org.mjsip.sip.call.RegistrationClient;
import org.mjsip.sip.call.RegistrationClientListener;
import org.mjsip.sip.call.SipUser;
import org.mjsip.sip.header.FromHeader;
import org.mjsip.sip.message.SipMessage;
import org.mjsip.sip.message.SipMethods;
import org.mjsip.sip.provider.MethodId;
import org.mjsip.sip.provider.SipKeepAlive;
import org.mjsip.sip.provider.SipParser;
import org.mjsip.sip.provider.SipProvider;
import org.mjsip.sip.provider.SipProviderListener;
import org.slf4j.LoggerFactory;
import org.zoolu.net.SocketAddress;



/** Simple SIP call agent (signaling and media).
  * It supports both audio and video sessions, by means of embedded media applications
  * that can use the default Java sound support (javax.sound.sampled.AudioSystem)
  * and/or the Java Multimedia Framework (JMF).
  * <p>
  * As media applications it can also use external audio/video tools.
  * Currently only support for RAT (Robust Audio Tool) and VIC has been implemented.
  */
public class UserAgent extends CallListenerAdapter implements SipProviderListener, RegistrationClientListener {
	
	private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(UserAgent.class);

	/** On wav file */
	static final String CLIP_ON="on.wav";
	/** Off wav file */
	static final String CLIP_OFF="off.wav";
	/** Ring wav file */
	static final String CLIP_RING="ring.wav";
	/** Progress wav file */
	static final String CLIP_PROGRESS="progress.wav";


	// ***************************** attributes ****************************
	
	/** UserAgentProfile */
	private final UAConfig uaConfig;

	/** SipProvider */
	private final SipProvider sip_provider;

	/** RegistrationClient */
	private RegistrationClient rc=null;

	/** SipKeepAlive daemon */
	private SipKeepAlive keep_alive;

	/** Call */
	private ExtendedCall call;
	
	/** Call transfer */
	private ExtendedCall call_transfer;

	/** UAS */
	//protected CallWatcher ua_server;

	/** OptionsServer */
	private OptionsServer options_server;

	/** NotImplementedServer */
	private NotImplementedServer null_server;

	/** MediaAgent */
	private final MediaAgent media_agent;
	
	/** List of active media sessions */
	private final Vector<String> media_sessions=new Vector<>();

	/** UserAgent listener */
	private final UserAgentListener listener;

	/** Response timeout */
	private ScheduledFuture<?> response_to=null;

	/** Whether the outgoing call is already in progress */
	private boolean progress;   
	/** Whether the outgoing call is already ringing */
	private boolean ringing;

	/** On sound */
	private AudioClipPlayer clip_on;
	/** Off sound */
	private AudioClipPlayer clip_off;
	/** Ring sound */
	private AudioClipPlayer clip_ring;
	/** Progress sound */
	private AudioClipPlayer clip_progress;

	/** On volume gain */
	private float clip_on_volume_gain=(float)0.0; // not changed
	/** Off volume gain */
	private float clip_off_volume_gain=(float)0.0; // not changed
	/** Ring volume gain */
	private float clip_ring_volume_gain=(float)0.0; // not changed
	/** Progress volume gain */
	private float clip_progress_volume_gain=(float)0.0; // not changed

	private MediaConfig _mediaConfig;

	/** Creates a {@link UserAgent}. */
	public UserAgent(SipProvider sip_provider, UAConfig uaConfig, UserAgentListener listener) {
		this.sip_provider=sip_provider;
		this.listener=listener;
		this.uaConfig=uaConfig;
		// update user profile information
		uaConfig.setUnconfiguredAttributes(sip_provider);

		// log main config parameters
		if (LOG.isDebugEnabled()) {
			LOG.debug("ua_address: "+uaConfig.ua_address);
			LOG.debug("user's uri: "+uaConfig.getUserURI());
			LOG.debug("proxy: "+uaConfig.proxy);
			LOG.debug("registrar: "+uaConfig.registrar);
			LOG.debug("auth_realm: "+uaConfig.authRealm);
			LOG.debug("auth_user: "+uaConfig.authUser);
			LOG.debug("auth_passwd: " + (uaConfig.authPasswd != null && !uaConfig.authPasswd.isEmpty() ? "***" : "-"));
			LOG.debug("audio: "+uaConfig.audio);
			LOG.debug("video: "+uaConfig.video);
			
			// log other config parameters
			if (LOG.isTraceEnabled()) {
				LOG.trace("loopback: "+uaConfig.loopback);
				LOG.trace("send_only: "+uaConfig.sendOnly);
				LOG.trace("recv_only: "+uaConfig.recvOnly);
				LOG.trace("send_file: "+uaConfig.sendFile);
				LOG.trace("recv_file: "+uaConfig.recvFile);
				LOG.trace("send_tone: "+uaConfig.sendTone);
			}
		}

		// start listening for INVITE requests (UAS)
		if (uaConfig.uaServer) sip_provider.addSelectiveListener(new MethodId(SipMethods.INVITE),this);
		
		// start OPTIONS server
		if (uaConfig.optionsServer) options_server=new OptionsServer(sip_provider,"INVITE, ACK, CANCEL, OPTIONS, BYE","application/sdp");

		// start "Not Implemented" server
		if (uaConfig.nullServer) null_server=new NotImplementedServer(sip_provider);

		// init media agent
		media_agent = new MediaAgent(uaConfig);

		// load sounds
		// ################# patch to make rat working.. #################
		// in case of rat, do not load and play audio clips
		if (!uaConfig.useRat && !uaConfig.noSystemAudio) {
			try {
				clip_on=getAudioClip(uaConfig.mediaPath+"/"+CLIP_ON);
				clip_off=getAudioClip(uaConfig.mediaPath+"/"+CLIP_OFF);
				clip_ring=getAudioClip(uaConfig.mediaPath+"/"+CLIP_RING);
				clip_progress=getAudioClip(uaConfig.mediaPath+"/"+CLIP_PROGRESS);
				
				clip_ring.setLoop();
				clip_progress.setLoop();
				clip_on.setVolumeGain(clip_on_volume_gain);
				clip_off.setVolumeGain(clip_off_volume_gain);
				clip_ring.setVolumeGain(clip_ring_volume_gain);
				clip_progress.setVolumeGain(clip_progress_volume_gain);
			}
			catch (Exception e) {
				LOG.info("Exception.", e);
			}
		}
	}


	/** Inits the RegistrationClient */
	private void initRegistrationClient() {
		rc=new RegistrationClient(sip_provider,new SipURI(uaConfig.registrar),uaConfig.getUserURI(),uaConfig.authUser,uaConfig.authRealm,uaConfig.authPasswd,this);
	}


	/** Gets SessionDescriptor from Vector of MediaSpec. */
	private SdpMessage getSessionDescriptor(MediaDesc[] media_descs) {
		String owner=uaConfig.user;
		String media_addr=(uaConfig.mediaAddr!=null)? uaConfig.mediaAddr : sip_provider.getViaAddress();
		SdpMessage sdp=new SdpMessage(owner,media_addr);
		for (int i=0; i<media_descs.length; i++) {
			MediaDesc md=media_descs[i];

			// check if audio or video have been disabled
			if (md.getMedia().equalsIgnoreCase("audio") && !uaConfig.audio) continue;
			if (md.getMedia().equalsIgnoreCase("video") && !uaConfig.video) continue;

			sdp.addMediaDescriptor(md.toMediaDescriptor());
		}
		return sdp;
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
		if (new SipParser(str).indexOf(SipParser.naddr_uri_schemes)>=0) return NameAddress.parse(str);
		else {
			SipURI uri=completeSipURI(str);
			return new NameAddress(uri);
		}
	}


	/** Gets a SipURI based on an input string. */
	private SipURI completeSipURI(String str) {
		// in case it is passed only the user field, add "@" + proxy address
		if (uaConfig.proxy!=null && !str.startsWith("sip:") && !str.startsWith("sips:") && str.indexOf("@")<0 && str.indexOf(".")<0 && str.indexOf(":")<0) {
			// may be it is just the user name..
			return new SipURI(str,uaConfig.proxy);
		}
		else return new SipURI(str);
	}

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
			if (target_port<0) target_port=sip_provider.sipConfig().getDefaultPort();
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
	public void call(String callee, MediaConfig mediaConfig) {
		// in case of incomplete URI (e.g. only 'user' is present), try to complete it
		call(completeNameAddress(callee),mediaConfig);
	}

	/** Makes a new call (acting as UAC) with specific media descriptions. */
	public void call(NameAddress callee, MediaConfig mediaConfig) {
		// new media description
		_mediaConfig=mediaConfig;
		
		// new call
		SdpMessage sdp=uaConfig.noOffer? null : getSessionDescriptor(_mediaConfig.mediaDescs);
		call(callee,sdp);
	}


	/** Makes a new call (acting as UAC) with specific SDP. */
	public void call(NameAddress callee, SdpMessage sdp) {
		call=new ExtendedCall(sip_provider,new SipUser(uaConfig.getUserURI(),uaConfig.authUser,uaConfig.authRealm,uaConfig.authPasswd),this);      
		if (uaConfig.noOffer) call.call(callee);
		else {
			call.call(callee,sdp.toString());
		}
		progress=false;
		ringing=false;
	}

	/** Closes an ongoing, incoming, or pending call. */
	public void hangup() {
		// sound
		if (clip_progress!=null) clip_progress.stop();
		if (clip_ring!=null) clip_ring.stop();
		// response timeout
		cancelResponseTimeout();

		closeMediaSessions();
		if (call!=null) call.hangup();
		call=null;
	}

	private void cancelResponseTimeout() {
		if (response_to!=null) response_to.cancel(false);
	} 

	/** Accepts an incoming call with specific media description (Vector of MediaDesc). */
	public void accept(MediaConfig mediaConfig) {
		_mediaConfig = mediaConfig;
		
		// sound
		if (clip_ring!=null) clip_ring.stop();
		// response timeout
		cancelResponseTimeout();
		// return if no active call
		if (call==null) return;
		// new sdp
		SdpMessage local_sdp=getSessionDescriptor(_mediaConfig.mediaDescs);
		SdpMessage remote_sdp=new SdpMessage(call.getRemoteSessionDescriptor());
		SdpMessage new_sdp=new SdpMessage(local_sdp.getOrigin(),remote_sdp.getSessionName(),local_sdp.getConnection(),remote_sdp.getTime());
		new_sdp.addMediaDescriptors(local_sdp.getMediaDescriptors());
		new_sdp=OfferAnswerModel.makeSessionDescriptorMatch(new_sdp,remote_sdp);
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
		cancelResponseTimeout();
		
		if (call!=null) call.redirect(redirect_to);
	}   


	/** Modifies the current session. It re-invites the remote party changing the contact URI and SDP. */
	public void modify(String body) {
		if (call!=null && call.getState().isActive()) {
			LOG.info("RE-INVITING/MODIFING");
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
			LOG.info("REFER/TRANSFER");
			call.transfer(transfer_to);
		}
	}


	// ********************** protected methods **********************

	/** Starts media sessions (audio and/or video). */
	protected void startMediaSessions() {
		// exit if the media application is already running  
		if (media_sessions.size()>0) {
			LOG.debug("media sessions already active");
			return;
		}

		// get local and remote rtp addresses and ports
		SdpMessage local_sdp=new SdpMessage(call.getLocalSessionDescriptor());
		SdpMessage remote_sdp=new SdpMessage(call.getRemoteSessionDescriptor());
		
		// calculate media descriptor product
		Vector<MediaDescriptor> localMedia = local_sdp.getMediaDescriptors();
		Vector<MediaDescriptor> remoteMedia = remote_sdp.getMediaDescriptors();
		Vector<MediaDescriptor> matchingMedia = OfferAnswerModel.makeMediaDescriptorMatch(localMedia,remoteMedia);
		
		if (LOG.isDebugEnabled()) {
			for (MediaDescriptor desc : localMedia) {
				LOG.debug("Local media: " + desc);
			}
			for (MediaDescriptor desc : remoteMedia) {
				LOG.debug("Remote media: " + desc);
			}
			for (MediaDescriptor desc : matchingMedia) {
				LOG.debug("Matching media: " + desc);
			}
		}
		
		// select the media direction (send_only, recv_ony, fullduplex)
		FlowSpec.Direction dir=Direction.FULL_DUPLEX;
		if (uaConfig.recvOnly) dir=Direction.RECV_ONLY;
		else
		if (uaConfig.sendOnly) dir=Direction.SEND_ONLY;
		// for each media
		for (MediaDescriptor matchingDescriptor : matchingMedia) {
			MediaField mediaField=matchingDescriptor.getMedia();
			String mediaType=mediaField.getMedia();
			
			MediaDescriptor remoteDescriptor = remote_sdp.getMediaDescriptor(mediaType);
			remote_sdp.removeMediaDescriptor(mediaType);

			// media and flow specifications
			String transport=mediaField.getTransport();

			String format=mediaField.getFormatList().elementAt(0);
			int avp=Integer.parseInt(format);

			int local_port=mediaField.getPort();
			int remote_port=remoteDescriptor.getMedia().getPort();
			MediaSpec media_spec = findMatchingMediaSpec(mediaType, avp);
			
			if (local_port!=0 && remote_port!=0 && media_spec!=null) {
				String remote_address=remote_sdp.getConnection().getAddress();
				FlowSpec flow_spec=new FlowSpec(mediaType,media_spec,local_port,remote_address,remote_port, dir);
				LOG.info("Starting media session: " + mediaType + " format: " + flow_spec.getMediaSpec().getCodec());
				boolean success=media_agent.startMediaSession(flow_spec, _mediaConfig);           
				if (success) {
					media_sessions.addElement(mediaType);
					if (listener!=null) listener.onUaMediaSessionStarted(this,mediaType,format);
				}
			} else {
				LOG.info("No matching media found (local_port="+local_port+", remote_port="+remote_port+", media_spec="+media_spec+").");
			}
		}
	}

	private MediaSpec findMatchingMediaSpec(String mediaType, int avp) {
		MediaSpec media_spec=null;
		
		findMediaSpec:
		for (MediaDesc descriptors : _mediaConfig.mediaDescs) {
			if (descriptors.getMedia().equalsIgnoreCase(mediaType)) {
				MediaSpec[] specs=descriptors.getMediaSpecs();
				for (MediaSpec spec : specs) {
					if (spec.getAVP() == avp) {
						media_spec=spec;
						break findMediaSpec;
					}
				}
			}
		}
		return media_spec;
	}
 
	
	/** Closes media sessions.  */
	protected void closeMediaSessions() {
		for (int i=0; i<media_sessions.size(); i++) {
			String media=media_sessions.elementAt(i);
			media_agent.stopMediaSession(media);
			if (listener!=null) listener.onUaMediaSessionStopped(this,media);
		}
		media_sessions.removeAllElements();
	}


	// ************************* RA callbacks ************************

	/** From RegistrationClientListener. When it has been successfully (un)registered. */
	@Override
	public void onRegistrationSuccess(RegistrationClient rc, NameAddress target, NameAddress contact, int expires, String result) {
		if (listener!=null) listener.onUaRegistrationSucceeded(this,result);   
	}

	/** From RegistrationClientListener. When it failed on (un)registering. */
	@Override
	public void onRegistrationFailure(RegistrationClient rc, NameAddress target, NameAddress contact, String result) {
		if (listener!=null) listener.onUaRegistrationFailed(this,result);
	}


	// ************************ Call callbacks ***********************
	
	/** From SipProviderListener. When a new SipMessage is received by the SipProvider. */
	@Override
	public void onReceivedMessage(SipProvider sip_provider, SipMessage message) {
		new ExtendedCall(sip_provider,message,this);
	}


	/** From CallListener. Callback function called when arriving a new INVITE method (incoming call) */
	@Override
	public void onCallInvite(Call call, NameAddress callee, NameAddress caller, String sdp, SipMessage invite) {
		LOG.debug("onCallInvite()");
		if (this.call!=null && !this.call.getState().isClosed()) {
			LOG.info("LOCALLY BUSY: INCOMING CALL REFUSED");
			call.refuse();
			return;
		}
   
		LOG.info("INCOMING: " + extractFrom(invite));
		this.call=(ExtendedCall)call;
		call.ring();
		// sound
		if (clip_ring!=null) clip_ring.play();
		// response timeout
		if (uaConfig.refuseTime>=0) response_to=(ScheduledFuture<?>) sip_provider.scheduler().schedule((long) (uaConfig.refuseTime*1000), this::onResponseTimeout);
		
		if (listener!=null) listener.onUaIncomingCall(this,callee,caller,MediaDesc.parseSdpDescriptors(sdp));
	}

	private String extractFrom(SipMessage invite) {
		FromHeader fromHeader = invite.getFromHeader();
		String from;
		if (fromHeader == null) {
			from = "ANONYMOUS";
		} else {
			String value = fromHeader.getValue();
			
			int start = value.indexOf(':');
			if (start < 0) {
				start = 0;
			}
			int stop = value.indexOf('@');
			if (stop < 0) {
				stop = value.length();
			}
			from = value.substring(start + 1, stop);
		}
		return from;
	}  


	/** From CallListener. Callback function called when arriving a new Re-INVITE method (re-inviting/call modify) */
	@Override
	public void onCallModify(Call call, String sdp, SipMessage invite) {
		LOG.debug("onCallModify()");
		if (call!=this.call) {  LOG.debug("NOT the current call");  return;  }
		LOG.info("RE-INVITE/MODIFY");
		// to be implemented.
		// currently it simply accepts the session changes (see method onCallModify() in CallListenerAdapter)
		super.onCallModify(call,sdp,invite);
	}


	/** From CallListener. Callback function called when arriving a 183 Session Progress */
	@Override
	public void onCallProgress(Call call, SipMessage resp) {
		LOG.debug("onCallProgress()");
		if (call!=this.call && call!=call_transfer) {  LOG.debug("NOT the current call");  return;  }
		if (!progress) {
			LOG.info("PROGRESS");
			progress=true;
			// sound
			if (clip_progress!=null) clip_progress.play();
			
			if (listener!=null) listener.onUaCallProgress(this);
		}
	}


	/** From CallListener. Callback function that may be overloaded (extended). Called when arriving a 180 Ringing */
	@Override
	public void onCallRinging(Call call, SipMessage resp) {
		LOG.debug("onCallRinging()");
		if (call!=this.call && call!=call_transfer) {  LOG.debug("NOT the current call");  return;  }
		if (!ringing) {
			LOG.info("RINGING");
			ringing=true;
			// sound
			if (clip_progress!=null) clip_progress.play();
			
			if (listener!=null) listener.onUaCallRinging(this);
		}
	}


	/** Callback function called when arriving a 1xx response (e.g. 183 Session Progress) that has to be confirmed */
	@Override
	public void onCallConfirmableProgress(Call call, SipMessage resp) {
		// TODO
	}

	/** Callback function called when arriving a PRACK for a reliable 1xx response, that had to be confirmed */
	@Override
	public void onCallProgressConfirmed(Call call, SipMessage resp, SipMessage prack) {
		// TODO
	}

	/** From CallListener. Callback function called when arriving a 2xx (call accepted) */
	@Override
	public void onCallAccepted(Call call, String sdp, SipMessage resp) {
		LOG.debug("onCallAccepted()");
		if (call!=this.call && call!=call_transfer) {  LOG.debug("NOT the current call");  return;  }
		LOG.info("ACCEPTED/CALL");
		if (uaConfig.noOffer) {
			// new sdp
			SdpMessage local_sdp=getSessionDescriptor(_mediaConfig.mediaDescs);
			SdpMessage remote_sdp=new SdpMessage(sdp);
			SdpMessage new_sdp=new SdpMessage(local_sdp.getOrigin(),remote_sdp.getSessionName(),local_sdp.getConnection(),remote_sdp.getTime());
			new_sdp.addMediaDescriptors(local_sdp.getMediaDescriptors());
			new_sdp=OfferAnswerModel.makeSessionDescriptorMatch(new_sdp,remote_sdp);         
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
	@Override
	public void onCallConfirmed(Call call, String sdp, SipMessage ack) {
		LOG.debug("onCallConfirmed()");
		if (call!=this.call) {  LOG.debug("NOT the current call");  return;  }
		LOG.info("CONFIRMED/CALL");
		// sound
		if (clip_on!=null) clip_on.play();
		
		startMediaSessions();
	}


	/** From CallListener. Callback function called when arriving a 2xx (re-invite/modify accepted) */
	@Override
	public void onCallModifyAccepted(Call call, String sdp, SipMessage resp) {
		LOG.debug("onCallModifyAccepted()");
		if (call!=this.call) {  LOG.debug("NOT the current call");  return;  }
		LOG.info("RE-INVITE-ACCEPTED/CALL");
	}


	/** From CallListener. Callback function called when arriving a 4xx (re-invite/modify failure) */
	@Override
	public void onCallModifyRefused(Call call, String reason, SipMessage resp) {
		LOG.debug("onCallReInviteRefused()");
		if (call!=this.call) {  LOG.debug("NOT the current call");  return;  }
		LOG.info("RE-INVITE-REFUSED ("+reason+")/CALL");
		if (listener!=null) listener.onUaCallFailed(this,reason);
	}


	/** From CallListener. Callback function called when arriving a 4xx (call failure) */
	@Override
	public void onCallRefused(Call call, String reason, SipMessage resp) {
		LOG.debug("onCallRefused()");
		if (call!=this.call && call!=call_transfer) {  LOG.debug("NOT the current call");  return;  }
		LOG.info("REFUSED ("+reason+")");
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
	@Override
	public void onCallRedirected(Call call, String reason, Vector contact_list, SipMessage resp) {
		LOG.debug("onCallRedirected()");
		if (call!=this.call) {  LOG.debug("NOT the current call");  return;  }
		LOG.info("REDIRECTION ("+reason+")");
		NameAddress first_contact=NameAddress.parse((String)contact_list.elementAt(0));
		call.call(first_contact); 
	}


	/** From CallListener. Callback function called when arriving a CANCEL request */
	@Override
	public void onCallCancel(Call call, SipMessage cancel) {
		LOG.debug("onCallCancel()");
		if (call!=this.call) {  LOG.debug("NOT the current call");  return;  }
		LOG.info("CANCEL");
		this.call=null;
		// sound
		if (clip_ring!=null) clip_ring.stop();
		if (clip_off!=null) clip_off.play();
		// response timeout
		cancelResponseTimeout();
		
		if (listener!=null) listener.onUaCallCancelled(this);
	}


	/** From CallListener. Callback function called when arriving a BYE request */
	@Override
	public void onCallBye(Call call, SipMessage bye) {
		LOG.debug("onCallBye()");
		if (call!=this.call && call!=call_transfer) {  LOG.debug("NOT the current call");  return;  }
		if (call!=call_transfer && call_transfer!=null) {
			LOG.info("CLOSE PREVIOUS CALL");
			this.call=call_transfer;
			call_transfer=null;
			return;
		}
		// else
		LOG.info("CLOSE");
		this.call=null;
		closeMediaSessions();
		// sound
		if (clip_off!=null) clip_off.play();
		
		if (listener!=null) listener.onUaCallClosed(this);
	}


	/** From CallListener. Callback function called when arriving a response after a BYE request (call closed) */
	@Override
	public void onCallClosed(Call call, SipMessage resp) {
		LOG.info("LogLevel.DEBUG,onCallClosed()");
		if (call!=this.call) {  LOG.debug("NOT the current call");  return;  }
		LOG.info("CLOSE/OK");
		if (listener!=null) listener.onUaCallClosed(this);
	}

	/** Callback function called when the invite expires */
	@Override
	public void onCallTimeout(Call call) {
		LOG.debug("onCallTimeout()");
		if (call!=this.call) {  LOG.debug("NOT the current call");  return;  }
		LOG.info("NOT FOUND/TIMEOUT");
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

	@Override
	protected void onDtmfInfo(Call call, SipMessage msg, DTMFInfo dtmf) {
		super.onDtmfInfo(call, msg, dtmf);

		if (LOG.isDebugEnabled()) {
			LOG.debug("Received DTMF info: " + dtmf);
		}
		
		if (listener!=null) listener.onDtmfInfo(this, dtmf);
	}

	// ******************* ExtendedCall callbacks ********************


	/** From ExtendedCallListener. Callback function called when arriving a new UPDATE method (update request). */
	public void onCallUpdate(ExtendedCall call, String sdp, SipMessage update) {
		LOG.debug("onCallUpdate()");
		if (call!=this.call) {  LOG.debug("NOT the current call");  return;  }
		LOG.info("UPDATE");
		// to be implemented.
		// currently it simply accepts the session changes (see method onCallModify() in CallListenerAdapter)
		super.onCallUpdate(call,sdp,update);
	}


	/** From ExtendedCallListener. Callback function called when arriving a new REFER method (transfer request) */
	@Override
	public void onCallTransfer(ExtendedCall call, NameAddress refer_to, NameAddress refered_by, SipMessage refer) {
		LOG.debug("onCallTransfer()");
		if (call!=this.call) {  LOG.debug("NOT the current call");  return;  }
		LOG.info("transfer to "+refer_to.toString());
		call.acceptTransfer();
		call_transfer=new ExtendedCall(sip_provider,new SipUser(uaConfig.getUserURI()),this);
		call_transfer.call(refer_to,getSessionDescriptor(_mediaConfig.mediaDescs).toString());
	}

	/** From ExtendedCallListener. Callback function called when a call transfer is accepted. */
	@Override
	public void onCallTransferAccepted(ExtendedCall call, SipMessage resp) {
		LOG.debug("onCallTransferAccepted()");
		if (call!=this.call) {  LOG.debug("NOT the current call");  return;  }
		LOG.info("transfer accepted");
	}

	/** From ExtendedCallListener. Callback function called when a call transfer is refused. */
	@Override
	public void onCallTransferRefused(ExtendedCall call, String reason, SipMessage resp) {
		LOG.debug("onCallTransferRefused()");
		if (call!=this.call) {  LOG.debug("NOT the current call");  return;  }
		LOG.info("transfer refused");
	}

	/** From ExtendedCallListener. Callback function called when a call transfer is successfully completed */
	@Override
	public void onCallTransferSuccess(ExtendedCall call, SipMessage notify) {
		LOG.debug("onCallTransferSuccess()");
		if (call!=this.call) {  LOG.trace("NOT the current call");  return;  }
		LOG.info("transfer successed");
		call.hangup();
		if (listener!=null) listener.onUaCallTransferred(this);
	}

	/** From ExtendedCallListener. Callback function called when a call transfer is NOT sucessfully completed */
	@Override
	public void onCallTransferFailure(ExtendedCall call, String reason, SipMessage notify) {
		LOG.debug("onCallTransferFailure()");
		if (call!=this.call) {  LOG.debug("NOT the current call");  return;  }
		LOG.info("transfer failed");
	}


	// *********************** Timer callbacks ***********************

	private void onResponseTimeout() {
		LOG.info("response time expired: incoming call declined");
		if (call!=null) call.refuse();
		// sound
		if (clip_ring!=null) clip_ring.stop();
	}


	// **************************** Static ****************************

	private static AudioClipPlayer getAudioClip(String image_file) throws java.io.IOException {
		return new AudioClipPlayer(UserAgent.class.getResource("/" + image_file), null);
	}

}
