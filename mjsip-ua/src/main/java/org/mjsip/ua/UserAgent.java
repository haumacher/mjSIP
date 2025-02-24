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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Vector;
import java.util.concurrent.ScheduledFuture;

import org.mjsip.media.FlowSpec;
import org.mjsip.media.MediaDesc;
import org.mjsip.media.MediaSpec;
import org.mjsip.media.MediaStreamer;
import org.mjsip.pool.PortPool;
import org.mjsip.sdp.MediaDescriptor;
import org.mjsip.sdp.OfferAnswerModel;
import org.mjsip.sdp.SdpMessage;
import org.mjsip.sdp.field.ConnectionField;
import org.mjsip.sdp.field.MediaField;
import org.mjsip.sip.address.NameAddress;
import org.mjsip.sip.address.SipURI;
import org.mjsip.sip.call.Call;
import org.mjsip.sip.call.CallListenerAdapter;
import org.mjsip.sip.call.DTMFInfo;
import org.mjsip.sip.call.ExtendedCall;
import org.mjsip.sip.call.SipUser;
import org.mjsip.sip.message.SipMessage;
import org.mjsip.sip.message.SipResponses;
import org.mjsip.sip.provider.SipParser;
import org.mjsip.sip.provider.SipProvider;
import org.mjsip.sip.provider.SipProviderListener;
import org.mjsip.ua.registration.RegistrationClient;
import org.mjsip.ua.registration.RegistrationClientListener;
import org.slf4j.LoggerFactory;
import org.zoolu.net.AddressType;
import org.zoolu.util.VectorUtils;

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

	// ***************************** attributes ****************************
	
	/** UserAgentProfile */
	protected final ClientOptions _config;

	/** SipProvider */
	protected final SipProvider sip_provider;

	private final PortPool _portPool;

	/** Call */
	private ExtendedCall call;
	
	/** Call transfer */
	private ExtendedCall call_transfer;

	/** UAS */
	//protected CallWatcher ua_server;

	/** {@link MediaAgent} managing media of the current call. */
	private MediaAgent _mediaAgent;
	
	/** Active media streamers, as table of: (String)media-->(MediaStreamer)media_streamer */
	private Map<String, MediaStreamer> _mediaSessions = new HashMap<>();
	
	/** UserAgent listener */
	private final UserAgentListener listener;

	/** Response timeout */
	private ScheduledFuture<?> response_to=null;

	/** Whether the outgoing call is already in progress */
	private boolean progress;   
	/** Whether the outgoing call is already ringing */
	private boolean ringing;

	/** Creates a {@link UserAgent}.*/
	public UserAgent(SipProvider sip_provider, PortPool portPool, ClientOptions uaConfig, UserAgentListener listener) {
		this.sip_provider=sip_provider;
		_portPool = portPool;
		this.listener=listener;
		this._config=uaConfig;
	}

	/** Gets SessionDescriptor from Vector of MediaSpec. */
	private SdpMessage getSessionDescriptor(AddressType addressType) {
		String owner=_config.getSipUser();
		String mediaAddr = _config.getMediaAddr();
		String media_addr=(mediaAddr!=null)? mediaAddr : sip_provider.getViaAddress(addressType);
		SdpMessage sdp=SdpMessage.createSdpMessage(owner, media_addr);
		for (MediaDesc md : _mediaAgent.getCallMedia()) {
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
		if (_config.getProxy()!=null && !str.startsWith("sip:") && !str.startsWith("sips:") && str.indexOf("@")<0 && str.indexOf(".")<0 && str.indexOf(":")<0) {
			// may be it is just the user name..
			return new SipURI(str,_config.getProxy());
		}
		else return SipURI.parseSipURI(str);
	}


	/** Makes a new call (acting as UAC) with specific media description (Vector of MediaDesc). */
	public void call(String callee, MediaAgent mediaAgent) {
		// in case of incomplete URI (e.g. only 'user' is present), try to complete it
		call(completeNameAddress(callee),mediaAgent);
	}

	/**
	 * Makes a new call (acting as UAC) with specific media descriptions.
	 * 
	 * @param mediaAgent
	 *        The {@link MediaAgent} start the call with
	 */
	public void call(NameAddress callee, MediaAgent mediaAgent) {
		setupMedia(mediaAgent);
		
		AddressType addressType = callee.getAddress().toSipURI().getAddressType();
		
		// new call
		SdpMessage sdp=_config.getNoOffer()? null : getSessionDescriptor(addressType);

		call = new ExtendedCall(sip_provider, new SipUser(_config.getUserURI(), _config.getAuthUser(),
				_config.getAuthRealm(), _config.getAuthPasswd()),this);      
		if (_config.getNoOffer()) {
			call.call(callee);
		} else {
			call.call(callee,sdp);
		}
		progress=false;
		ringing=false;
	}

	/** Closes an ongoing, incoming, or pending call. */
	public void hangup() {
		// response timeout
		cancelResponseTimeout();

		closeMediaSessions();
		if (call!=null) call.hangup();
		call=null;
		
		if (listener!=null) listener.onUaCallClosed(this);
	}

	private void cancelResponseTimeout() {
		if (response_to!=null) response_to.cancel(false);
	} 

	/**
	 * Accepts an incoming call with specific media description (Vector of MediaDesc).
	 * 
	 * @param mediaAgent
	 *        The {@link MediaAgent} to accept the call with.
	 */
	public void accept(MediaAgent mediaAgent) {
		setupMedia(mediaAgent);
		
		if (listener!=null) listener.onUaCallIncomingAccepted(this);

		// response timeout
		cancelResponseTimeout();
		// return if no active call
		if (call==null) return;
		
		// new sdp
		SdpMessage remoteDescriptor = call.getRemoteSessionDescriptor();
		AddressType addressType = remoteDescriptor.getConnection().getAddressType();
		SdpMessage newSdp = OfferAnswerModel.matchSdp(getSessionDescriptor(addressType), remoteDescriptor);
		// accept
		call.accept(newSdp);
	}

	private void setupMedia(MediaAgent mediaAgent) {
		mediaAgent.allocateMediaPorts(_portPool);
		_mediaAgent = mediaAgent;
	}

	/** Redirects an incoming call. */
	public void redirect(String redirect_to) {
		// in case of incomplete URI (e.g. only 'user' is present), try to complete it
		redirect(completeNameAddress(redirect_to));
	}

	/** Redirects an incoming call. */
	public void redirect(NameAddress redirect_to) {
		// response timeout
		cancelResponseTimeout();
		
		if (call!=null) call.redirect(redirect_to);
		
		if (listener!=null) listener.onUaCallRedirected(this, redirect_to);
	}   

	/** Modifies the current session. It re-invites the remote party changing the contact URI and SDP. */
	public void modify(SdpMessage sdp) {
		if (call!=null && call.getState().isActive()) {
			LOG.debug("RE-INVITING/MODIFING");
			call.modify(sdp);
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
			LOG.debug("REFER/TRANSFER");
			call.transfer(transfer_to);
		}
	}

	// ********************** protected methods **********************

	/** Starts media sessions (audio and/or video). */
	protected void startMediaSessions() {
		// exit if the media application is already running  
		if (_mediaSessions.size()>0) {
			LOG.debug("media sessions already active");
			return;
		}

		// get local and remote rtp addresses and ports
		SdpMessage localSdp=call.getLocalSessionDescriptor();
		SdpMessage remoteSdp=call.getRemoteSessionDescriptor();
		
		// calculate media descriptor product
		Vector<MediaDescriptor> localMedia = localSdp.getMediaDescriptors();
		Vector<MediaDescriptor> remoteMedia = remoteSdp.getMediaDescriptors();
		Vector<MediaDescriptor> matchingMedia = OfferAnswerModel.matchMedia(localMedia,remoteMedia);
		
		if (LOG.isDebugEnabled()) {
			for (MediaDescriptor desc : localMedia) {
				LOG.debug("Local media: {}", desc);
			}
			for (MediaDescriptor desc : remoteMedia) {
				LOG.debug("Remote media: {}", desc);
			}
			for (MediaDescriptor desc : matchingMedia) {
				LOG.debug("Matching media: {}", desc);
			}
		}
		
		// select the media direction (send_only, recv_ony, fullduplex)
		FlowSpec.Direction dir = _config.getDirection();
		String remote_address=remoteSdp.getConnection().getAddress();
		// for each media
		
		Vector<MediaDescriptor> remoteCopy = VectorUtils.copy(remoteMedia);
		for (MediaDescriptor matchingDescriptor : matchingMedia) {
			String mediaType=matchingDescriptor.getMediaField().getMediaType();
			
			MediaDescriptor remoteDescriptor = MediaDescriptor.withType(remoteCopy, mediaType);
			remoteCopy.remove(remoteDescriptor);
			
			FlowSpec flow_spec = buildFlowSpec(mediaType, remoteDescriptor, matchingDescriptor, dir, remote_address);
			if (flow_spec == null) {
				continue;
			}

			// stop previous media streamer (just in case something was wrong..)
			MediaStreamer existing = _mediaSessions.remove(mediaType);
			if (existing != null) {
				existing.halt();
			}
			
			LOG.debug("Starting media session: {}", flow_spec);
			MediaStreamer streamer = _mediaAgent.startMediaSession(sip_provider.scheduler(), flow_spec);
			
			if (streamer == null) {
				LOG.warn("No media streamer found for type: {}", mediaType);
				continue;
			}
			 
			// Start the new stream.
			if (streamer.start()) {
				_mediaSessions.put(mediaType, streamer);
				
				String codec = flow_spec.getMediaSpec().getCodec();
				if (listener!=null) listener.onUaMediaSessionStarted(this,mediaType,codec);
			}
		}
	}

	private FlowSpec buildFlowSpec(String mediaType, MediaDescriptor remoteDescriptor, MediaDescriptor matchingDescriptor, FlowSpec.Direction dir, String remote_address) {
		MediaField mediaField=matchingDescriptor.getMediaField();
		String transport=mediaField.getTransport();
		List<String> formatList = mediaField.getFormatList();
		if (formatList.isEmpty()) {
			LOG.warn("No matching formats found to establish flow: {}", remoteDescriptor);
			return null;
		}
		
		int avp=Integer.parseInt(formatList.get(0));

		int local_port=mediaField.getPort();
		int remote_port=remoteDescriptor.getMediaField().getPort();
		
		MediaSpec media_spec = findMatchingMediaSpec(mediaType, avp);
		
		if (local_port!=0 && remote_port!=0 && media_spec!=null) {
			return new FlowSpec(mediaType,media_spec,local_port,remote_address,remote_port, dir);
		} else {
			LOG.warn("No matching media found (local_port={}, remote_port={}, remoteDescriptor={}).", local_port, remote_port, remoteDescriptor);
			return null;
		}
	}

	private MediaSpec findMatchingMediaSpec(String mediaType, int avp) {
		for (MediaDesc descriptors : _mediaAgent.getCallMedia()) {
			if (descriptors.getMediaType().equalsIgnoreCase(mediaType)) {
				MediaSpec[] specs=descriptors.getMediaSpecs();
				for (MediaSpec spec : specs) {
					if (spec.getAVP() == avp) {
						return spec;
					}
				}
			}
		}
		return null;
	}
	
	/** Closes media sessions.  */
	protected void closeMediaSessions() {
		for (Entry<String, MediaStreamer> entry : _mediaSessions.entrySet()) {
			entry.getValue().halt();
			
			if (listener!=null) listener.onUaMediaSessionStopped(this,entry.getKey());
		}
		_mediaSessions.clear();
		if (_mediaAgent != null) {
			_mediaAgent.releaseMediaPorts(_portPool);
			_mediaAgent = null;
		}
	}

	// ************************* RA callbacks ************************

	/** From RegistrationClientListener. When it has been successfully (un)registered. */
	@Override
	public void onRegistrationSuccess(RegistrationClient rc, NameAddress target, NameAddress contact, int expires, int renewTime, String result) {
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
	public void onCallInvite(Call call, NameAddress callee, NameAddress caller, SdpMessage remoteSdp, SipMessage invite) {
		LOG.debug("onCallInvite()");
		if (this.call!=null && !this.call.getState().isClosed()) {
			LOG.debug("Busy, refusing incoming call from: {}", invite.getFromUser());
			call.refuse();
			return;
		}
   
		LOG.debug("Incoming call from: {}", invite.getFromUser());
		this.call=(ExtendedCall)call;
		call.ring();

		if (_config.getRefuseTime()>=0) {
			response_to=sip_provider.scheduler().schedule(_config.getRefuseTime()*1000, this::onResponseTimeout);
		}
		
		if (listener!=null) {
			listener.onUaIncomingCall(this,callee,caller,MediaDesc.parseDescriptors(remoteSdp.getMediaDescriptors()));
		}
	}

	/** From CallListener. Callback function called when arriving a new Re-INVITE method (re-inviting/call modify) */
	@Override
	public void onCallModify(Call call, SdpMessage remoteSdp, SipMessage invite) {
		LOG.debug("onCallModify()");
		if (call!=this.call) {
			LOG.warn("Modify of unknown call received: {}", call.getCallId());  
			return;  
		}
		LOG.debug("Received RE-INVITE/MODIFY.");
		// to be implemented.
		// currently it simply accepts the session changes (see method onCallModify() in CallListenerAdapter)
		super.onCallModify(call,remoteSdp,invite);
	}

	/** From CallListener. Callback function called when arriving a 183 Session Progress */
	@Override
	public void onCallProgress(Call call, SipMessage resp) {
		LOG.debug("onCallProgress()");
		if (call!=this.call && call!=call_transfer) {  LOG.debug("NOT the current call");  return;  }
		if (!progress) {
			LOG.debug("PROGRESS");
			progress=true;
			
			if (listener!=null) listener.onUaCallProgress(this);
		}
	}

	/** From CallListener. Callback function that may be overloaded (extended). Called when arriving a 180 Ringing */
	@Override
	public void onCallRinging(Call call, SipMessage resp) {
		LOG.debug("onCallRinging()");
		if (call!=this.call && call!=call_transfer) {  LOG.debug("NOT the current call");  return;  }
		if (!ringing) {
			LOG.debug("RINGING");
			ringing=true;
			
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
	public void onCallAccepted(Call call, SdpMessage remoteSdp, SipMessage resp) {
		if (call != this.call && call != call_transfer) {
			LOG.debug("Ignoring accept for unknown call: {}", call.getRemoteSessionDescriptor().getOrigin().getValue());  
			return;  
		}
		LOG.debug("Call accepted: {}", call.getRemoteSessionDescriptor().getOrigin().getValue());
		
		if (_config.getNoOffer()) {
			AddressType addressType = remoteSdp.getAddressType();
			SdpMessage answerSdp = OfferAnswerModel.matchSdp(getSessionDescriptor(addressType), remoteSdp);         
			call.confirm2xxWithAnswer(answerSdp);
		}
		
		if (listener!=null) listener.onUaCallAccepted(this);

		startMediaSessions();
		
		if (call==call_transfer) {
			this.call.notify(resp);
		}
	}

	/** From CallListener. Callback function called when arriving an ACK method (call confirmed) */
	@Override
	public void onCallConfirmed(Call call, SdpMessage sdp, SipMessage ack) {
		if (call != this.call) {
			LOG.debug("Ignoring conform of unknown call: {}", call.getRemoteSessionDescriptor().getOrigin().getValue());  
			return;
		}
		LOG.debug("Call confirmed: {}", call.getRemoteSessionDescriptor().getOrigin().getValue());

		if (listener!=null) listener.onUaCallConfirmed(this);
		
		startMediaSessions();
	}

	/** From CallListener. Callback function called when arriving a 2xx (re-invite/modify accepted) */
	@Override
	public void onCallModifyAccepted(Call call, SdpMessage sdp, SipMessage resp) {
		LOG.debug("onCallModifyAccepted()");
		if (call!=this.call) {  LOG.debug("NOT the current call");  return;  }
		LOG.debug("RE-INVITE-ACCEPTED/CALL");
	}

	/** From CallListener. Callback function called when arriving a 4xx (re-invite/modify failure) */
	@Override
	public void onCallModifyRefused(Call call, String reason, SipMessage resp) {
		LOG.debug("onCallReInviteRefused()");
		if (call!=this.call) {  LOG.debug("NOT the current call");  return;  }
		LOG.debug("RE-INVITE-REFUSED ({})/CALL", reason);
		if (listener!=null) listener.onUaCallFailed(this,reason);
	}

	/** From CallListener. Callback function called when arriving a 4xx (call failure) */
	@Override
	public void onCallRefused(Call call, String reason, SipMessage resp) {
		LOG.debug("onCallRefused()");
		if (call!=this.call && call!=call_transfer) {  LOG.debug("NOT the current call");  return;  }
		LOG.debug("REFUSED ({})", reason);
		if (call==call_transfer) {
			this.call.notify(resp);
			call_transfer=null;
		}
		else this.call=null;
		
		if (listener!=null) listener.onUaCallFailed(this,reason);
	}

	/** From CallListener. Callback function called when arriving a 3xx (call redirection) */
	@Override
	public void onCallRedirected(Call call, String reason, Vector contact_list, SipMessage resp) {
		LOG.debug("onCallRedirected()");
		if (call!=this.call) {  LOG.debug("NOT the current call");  return;  }
		LOG.debug("REDIRECTION ({})", reason);
		NameAddress first_contact=NameAddress.parse((String)contact_list.elementAt(0));
		call.call(first_contact); 
	}

	/** From CallListener. Callback function called when arriving a CANCEL request */
	@Override
	public void onCallCancel(Call call, SipMessage cancel) {
		LOG.debug("onCallCancel()");
		if (call!=this.call) {  LOG.debug("NOT the current call");  return;  }
		LOG.debug("CANCEL");
		this.call=null;
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
			LOG.debug("CLOSE PREVIOUS CALL");
			this.call=call_transfer;
			call_transfer=null;
			return;
		}
		// else
		LOG.debug("CLOSE");
		this.call=null;
		closeMediaSessions();
		
		if (listener!=null) listener.onUaCallClosed(this);
	}


	/** From CallListener. Callback function called when arriving a response after a BYE request (call closed) */
	@Override
	public void onCallClosed(Call call, SipMessage resp) {
		LOG.debug("LogLevel.DEBUG,onCallClosed()");
		if (call!=this.call) {  LOG.debug("NOT the current call");  return;  }
		LOG.debug("CLOSE/OK");
		if (listener!=null) listener.onUaCallClosed(this);
	}

	/** Callback function called when the invite expires */
	@Override
	public void onCallTimeout(Call call) {
		LOG.debug("onCallTimeout()");
		if (call!=this.call) {  LOG.debug("NOT the current call");  return;  }
		LOG.debug("NOT FOUND/TIMEOUT");
		String reason="Request Timeout";
		if (call==call_transfer) {
			this.call.notify(SipResponses.REQUEST_TIMEOUT,reason);
			call_transfer=null;
		}
		
		if (listener!=null) listener.onUaCallFailed(this,reason);
	}

	@Override
	protected void onDtmfInfo(Call call, SipMessage msg, DTMFInfo dtmf) {
		super.onDtmfInfo(call, msg, dtmf);

		if (LOG.isDebugEnabled()) {
			LOG.debug("Received DTMF info: {}", dtmf);
		}
		
		if (listener!=null) listener.onDtmfInfo(this, dtmf);
	}

	// ******************* ExtendedCall callbacks ********************


	/** From ExtendedCallListener. Callback function called when arriving a new UPDATE method (update request). */
	public void onCallUpdate(ExtendedCall call, SdpMessage sdp, SipMessage update) {
		LOG.debug("onCallUpdate()");
		if (call!=this.call) {  LOG.debug("NOT the current call");  return;  }
		LOG.debug("UPDATE");
		// to be implemented.
		// currently it simply accepts the session changes (see method onCallModify() in CallListenerAdapter)
		super.onCallUpdate(call,sdp,update);
	}


	/** From ExtendedCallListener. Callback function called when arriving a new REFER method (transfer request) */
	@Override
	public void onCallTransfer(ExtendedCall call, NameAddress refer_to, NameAddress refered_by, SipMessage refer) {
		LOG.debug("onCallTransfer()");
		if (call!=this.call) {  LOG.debug("NOT the current call");  return;  }
		LOG.debug("transfer to {}", refer_to);
		call.acceptTransfer();
		call_transfer=new ExtendedCall(sip_provider,new SipUser(_config.getUserURI()),this);
		AddressType addressType = ConnectionField.addressType(refer_to.getAddress().getSpecificPart());
		call_transfer.call(refer_to,getSessionDescriptor(addressType));
	}

	/** From ExtendedCallListener. Callback function called when a call transfer is accepted. */
	@Override
	public void onCallTransferAccepted(ExtendedCall call, SipMessage resp) {
		LOG.debug("onCallTransferAccepted()");
		if (call!=this.call) {  LOG.debug("NOT the current call");  return;  }
		LOG.debug("transfer accepted");
	}

	/** From ExtendedCallListener. Callback function called when a call transfer is refused. */
	@Override
	public void onCallTransferRefused(ExtendedCall call, String reason, SipMessage resp) {
		LOG.debug("onCallTransferRefused()");
		if (call!=this.call) {  LOG.debug("NOT the current call");  return;  }
		LOG.debug("transfer refused");
	}

	/** From ExtendedCallListener. Callback function called when a call transfer is successfully completed */
	@Override
	public void onCallTransferSuccess(ExtendedCall call, SipMessage notify) {
		LOG.debug("onCallTransferSuccess()");
		if (call!=this.call) {  LOG.trace("NOT the current call");  return;  }
		LOG.debug("transfer successed");
		call.hangup();
		if (listener!=null) listener.onUaCallTransferred(this);
	}

	/** From ExtendedCallListener. Callback function called when a call transfer is NOT sucessfully completed */
	@Override
	public void onCallTransferFailure(ExtendedCall call, String reason, SipMessage notify) {
		LOG.debug("onCallTransferFailure()");
		if (call!=this.call) {  LOG.debug("NOT the current call");  return;  }
		LOG.debug("transfer failed");
	}

	// *********************** Timer callbacks ***********************

	private void onResponseTimeout() {
		LOG.debug("response time expired: incoming call declined");
		if (call!=null) call.refuse();
		
		if (listener!=null) listener.onUaIncomingCallTimeout(this);
	}

}
