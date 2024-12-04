/*
 * Copyright (C) 2012 Luca Veltri - University of Parma - Italy
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

package org.mjsip.sip.dialog;



import java.util.Vector;

import org.mjsip.sdp.SdpMessage;
import org.mjsip.sip.address.GenericURI;
import org.mjsip.sip.address.NameAddress;
import org.mjsip.sip.address.SipURI;
import org.mjsip.sip.header.AllowHeader;
import org.mjsip.sip.header.ContactHeader;
import org.mjsip.sip.header.MinSEHeader;
import org.mjsip.sip.header.RecordRouteHeader;
import org.mjsip.sip.header.RecvInfoHeader;
import org.mjsip.sip.header.RequireHeader;
import org.mjsip.sip.header.SessionExpiresHeader;
import org.mjsip.sip.header.StatusLine;
import org.mjsip.sip.header.SupportedHeader;
import org.mjsip.sip.header.UnsupportedHeader;
import org.mjsip.sip.message.SipMessage;
import org.mjsip.sip.message.SipMessageFactory;
import org.mjsip.sip.message.SipMethods;
import org.mjsip.sip.message.SipResponses;
import org.mjsip.sip.provider.ConnectionId;
import org.mjsip.sip.provider.SipOptions;
import org.mjsip.sip.provider.SipProvider;
import org.mjsip.sip.provider.SipProviderListener;
import org.mjsip.sip.provider.SipStack;
import org.mjsip.sip.transaction.AckTransactionClient;
import org.mjsip.sip.transaction.AckTransactionServer;
import org.mjsip.sip.transaction.AckTransactionServerListener;
import org.mjsip.sip.transaction.CancelTransactionServer;
import org.mjsip.sip.transaction.InviteTransactionClient;
import org.mjsip.sip.transaction.InviteTransactionServer;
import org.mjsip.sip.transaction.InviteTransactionServerListener;
import org.mjsip.sip.transaction.ReliableProvisionalResponder;
import org.mjsip.sip.transaction.ReliableProvisionalResponderListener;
import org.mjsip.sip.transaction.TransactionClient;
import org.mjsip.sip.transaction.TransactionClientListener;
import org.mjsip.sip.transaction.TransactionServer;
import org.mjsip.sip.transaction.TransactionServerListener;
import org.slf4j.LoggerFactory;



/** Class InviteDialog can be used to manage invite dialogs.
  * An InviteDialog can be both client or server.
  * (i.e. generating an INVITE request or responding to an incoming INVITE request).
  * <p>
  * An InviteDialog can be in state inviting/waiting/invited, accepted/refused, call,
  * byed/byeing, and close.
  * <p>
  * InviteDialog supports the offer/answer model for the sip body, with the following rules:
  * <br> - both INVITE-offer/2xx-answer and 2xx-offer/ACK-answer modes for incoming calls
  * <br> - INVITE-offer/2xx-answer mode for outgoing calls.
  */
public class InviteDialog extends Dialog implements TransactionClientListener, InviteTransactionServerListener,
		AckTransactionServerListener, ReliableProvisionalResponderListener {
	
	private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(InviteDialog.class);

	// **************************** Attributes ****************************

	/** The last INVITE request */
	SipMessage invite_req=null;
	/** The last (invite) 2xx response */
	SipMessage inv2xx_resp=null;
	/** The last ACK request */
	SipMessage ack_req=null;

	/** The InviteTransactionClient. */
	InviteTransactionClient invite_tc;
	/** The InviteTransactionServer. */
	InviteTransactionServer invite_ts;
	/** The CANCEL TransactionServer. */
	TransactionServer cancel_ts;
	/** The AckTransactionServer. */
	AckTransactionServer ack_ts;
	/** The BYE TransactionServer. */
	TransactionServer bye_ts;

	/** The UPDATE TransactionServer. */
	TransactionServer update_ts;

	/** The InviteDialog listener */
	private final InviteDialogListener listener;
	

	/** Whether offer/answer are in INVITE/200_OK */
	boolean invite_offer;

	/** Supported extensions, as String[] of option-tags */
	String[] supported_option_tags=null;
	/** Required extensions, as String[] of option-tags */
	String[] required_option_tags=null;
	/** Proxy required extensions, as String[] of option-tags */
	String[] proxy_required_option_tags=null;

	/** Supported methods */
	String[] allowed_methods=null;

	/** Reliable provisional responder */
	ReliableProvisionalResponder reliable_responder=null;

	/** Supported info packages */
	String[] info_packages=null;

	private final SipMessageFactory sipMessageFactory;

	/**
	 * The maximum amount of time that can occur between session refresh requests in a dialog before
	 * the session will be considered timed out.
	 */
	private int session_interval;

	/**
	 * The time at which an element will consider the session timed out, if no successful session
	 * refresh transaction occurs beforehand.
	 */
	private long session_expiration = 0;

	/**
	 * Who is doing the refreshing -- UAC or UAS.
	 * 
	 * @see SessionExpiresHeader#PARAM_REFRESHER_UAC
	 * @see SessionExpiresHeader#PARAM_REFRESHER_UAS
	 */
	private String refresher = null;

	// ************************** Public methods **************************

	/** Creates a new InviteDialog.
	  * @param sip_provider the SIP provider
	  * @param listener invite dialog listener */
	public InviteDialog(SipProvider sip_provider, InviteDialogListener listener) {
		super(sip_provider);
		this.listener = listener;
		this.sipMessageFactory = sip_provider.messageFactory();

		SipOptions config = sip_provider.sipConfig();
		session_interval = config.getDefaultSessionInterval();
		supported_option_tags = config.getSupportedOptionTags();
		required_option_tags = config.getRequiredOptionTags();
		allowed_methods = config.getAllowedMethods();

		this.invite_offer = true;

		changeStatus(DialogStatus.D_INIT);
	}


	/** Creates a new InviteDialog for an already received INVITE request.
	  * @param sip_provider the SIP provider
	  * @param invite the already received INVITE message that creates this dialog
	  * @param listener invite dialog listener */
	public InviteDialog(SipProvider sip_provider, SipMessage invite, InviteDialogListener listener) {
		this(sip_provider, listener);

		// changeStatus(DialogStatus.D_INVITED);
		//this.invite_req=invite;
		//updateDialogInfo(false,invite);
		//invite_ts=new InviteTransactionServer(sip_provider,invite,this);
		onReceivedMessage(sip_provider,invite);
	}

	/** Gets the invite message */   
	public SipMessage getInviteMessage() {
		return invite_req;
	}

	/** Sets the supported extensions (using the corresponding option-tags). */
	public void setSupportedExtensions(String[] option_tags) {
		supported_option_tags=option_tags;
	}

	/** Whether the given optional extension is supported.
	  * @param option_tag the option tag of the given extension
	  * @return true if the given extension is supported */
	public boolean isExtensionSupported(String option_tag) {
		if (supported_option_tags!=null) {
			for (int i=0; i<supported_option_tags.length; i++) if (supported_option_tags[i].equals(option_tag)) return true;
		}
		// else
		return false;
	}

	/** Sets the required extensions (using the corresponding option-tags). */
	public void setRequiredExtensions(String[] option_tags) {
		required_option_tags=option_tags;
	}

	/** Whether the given optional extension is required.
	  * @param option_tag the option tag of the given extension
	  * @return true if the given extension is required */
	public boolean isExtensionRequired(String option_tag) {
		if (required_option_tags!=null) {
			for (int i=0; i<required_option_tags.length; i++) if (required_option_tags[i].equals(option_tag)) return true;
		}
		// else
		return false;
	}

	/** Sets the proxy required extensions (using the corresponding option-tags). */
	public void setProxyRequiredExtensions(String[] option_tags) {
		proxy_required_option_tags=option_tags;
	}


	/** Sets the supported methods. */
	public void setAllowedMethods(String[] allowed_methods) {
		this.allowed_methods=allowed_methods;
	}


	/** Sets the info packages. */
	public void setInfoPackages(String[] info_packages) {
		this.info_packages=info_packages;
	}

	/**
	 * Sets the session interval. That is the maximum amount of time that can occur between session
	 * refresh requests in a dialog before the session will be considered timed out.
	 */
	public void setSessionInterval(int session_interval) {
		this.session_interval = session_interval;
	}

	/**
	 * Gets the session interval. That is the maximum amount of time that can occur between session
	 * refresh requests in a dialog before the session will be considered timed out.
	 */
	public int getSessionInterval() {
		return session_interval;
	}

	/**
	 * Sets the session expiration. That is the time at which an element will consider the session
	 * timed out, if no successful session refresh transaction occurs beforehand.
	 */
	public void setSessionExpiration(long session_expiration) {
		this.session_expiration = session_expiration;
	}

	/**
	 * Gets the session expiration. That is the time at which an element will consider the session
	 * timed out, if no successful session refresh transaction occurs beforehand.
	 */
	public long getSessionExpiration() {
		return session_expiration;
	}

	/**
	 * Sets the refresher. That is who is doing the refreshing -- UAC or UAS.
	 */
	public void setRefresher(String refresher) {
		this.refresher = refresher;
	}

	/**
	 * Sets the refresher. That is who is doing the refreshing -- UAC or UAS.
	 */
	public String getRefresher() {
		return refresher;
	}

	/** Starts a new InviteTransactionServer. */
	public void listen() {
		if (!statusIs(DialogStatus.D_INIT))
			return;
		//else
		changeStatus(DialogStatus.D_WAITING);
		invite_ts=new InviteTransactionServer(sip_provider,this);
		invite_ts.listen();
	}  


	/** Starts a new InviteTransactionClient
	  * and initializes the dialog state information.
	  * @param target the callee url (optionally with the display name)
	  * @param from the caller url (optionally with the display name)
	  * @param contact the contact url (null for default contact)
	  * @param session_descriptor SDP message body */
	public void invite(NameAddress target, NameAddress from, NameAddress contact, SdpMessage session_descriptor) {
		LOG.debug("inside invite(callee,caller,contact,sdp)");
		if (!statusIs(DialogStatus.D_INIT))
			return;
		// else
		GenericURI request_uri=target.getAddress();
		String call_id=sip_provider.pickCallId();
		SipMessage invite = sipMessageFactory.createInviteRequest(request_uri, target, from, contact, call_id,
				"application/sdp", session_descriptor.toString().getBytes());
		// do invite
		invite(invite);
	}


	/** Starts a new InviteTransactionClient
	  * and initializes the dialog state information.
	  * @param invite the INVITE message */
	public void invite(SipMessage invite) {
		LOG.debug("inside invite(invite)");
		if (!statusIs(DialogStatus.D_INIT))
			return;
		// else
		changeStatus(DialogStatus.D_INVITING);
		// FORCE THIS NODE IN THE DIALOG ROUTE
		if (sip_provider.sipConfig().isOnDialogRoute()) {
			SipURI uri=new SipURI(sip_provider.getViaAddress(),sip_provider.getPort());
			uri.addLr();
			invite.addRecordRouteHeader(new RecordRouteHeader(new NameAddress(uri)));
		}
		// OPTIONAL EXTENSIONS
		if (required_option_tags!=null && required_option_tags.length>0) {
			invite.setRequireHeader(new RequireHeader(required_option_tags));
		}
		if (supported_option_tags!=null && supported_option_tags.length>0) {
			invite.setSupportedHeader(new SupportedHeader(supported_option_tags));
		}
		// ALLOWED METHODS
		if (allowed_methods!=null && allowed_methods.length>0) {
			invite.setAllowHeader(new AllowHeader(allowed_methods));
		}
		// SESSION TIMERS
		if (invite.hasSupportedHeader() && invite.getSupportedHeader().hasOptionTag(SipStack.OTAG_timer) && session_interval>0) {
			invite.setMinSEHeader(new MinSEHeader(sip_provider.sipConfig().getMinSessionInterval()));
			invite.setSessionExpiresHeader(new SessionExpiresHeader(session_interval));
		}
		// INFO PACKAGES
		if (info_packages!=null && info_packages.length>0) {
			invite.setRecvInfoHeader(new RecvInfoHeader(info_packages));
		}
		this.invite_req=invite;
		updateDialogInfo(true,invite);
		invite_tc=new InviteTransactionClient(sip_provider,invite,this);      
		invite_tc.request();
	}
	

	/** Starts a new InviteTransactionClient with offer/answer in 2xx/ack
	  * and initializes the dialog state information. */
	public void inviteWithoutOffer(NameAddress target, NameAddress from, NameAddress contact) {
		invite_offer=false;
		invite(target,from,contact,null);
	}


	/** Starts a new InviteTransactionClient with offer/answer in 2xx/ack
	  * and initializes the dialog state information. */
	public void inviteWithoutOffer(SipMessage invite) {
		invite_offer=false;
		invite(invite);
	}


	/** Re-invites the remote user.
	  * It starts a new InviteTransactionClient and changes the dialog state information.
	  * @param contact the contact uri (null for default contact)
	  * @param session_descriptor SDP body
	  */
	public void reInvite(NameAddress contact, SdpMessage session_descriptor) {
		LOG.debug("inside reInvite(contact,sdp)");
		if (!statusIs(DialogStatus.D_CALL))
			return;
		// else
		SipMessage invite = sipMessageFactory.createInviteRequest(this, "application/sdp",
				session_descriptor.toString().getBytes());
		if (contact!=null) invite.setContactHeader(new ContactHeader(contact));
		reInvite(invite);
	}


	/** Re-invites the remote user.
	  * It starts a new InviteTransactionClient and changes the dialog state information. */
	public void reInvite(SipMessage invite) {
		LOG.debug("inside reInvite(invite)");
		if (!statusIs(DialogStatus.D_CALL))
			return;
		// else
		changeStatus(DialogStatus.D_ReINVITING);
		this.invite_req=invite;
		updateDialogInfo(true,invite);
		invite_tc=new InviteTransactionClient(sip_provider,invite,this);                
		invite_tc.request();
	}


	/** Re-invites the remote user with offer/answer in 2xx/ack.
	  * It starts a new InviteTransactionClient and changes the dialog state information. */
	public void reInviteWithoutOffer(NameAddress contact, SdpMessage session_descriptor) {
		invite_offer=false;
		reInvite(contact,session_descriptor);
	}


	/** Re-invites the remote user with offer/answer in 2xx/ack.
	  * It starts a new InviteTransactionClient and changes the dialog state information. */
	public void reInviteWithoutOffer(SipMessage invite) {
		invite_offer=false;
		reInvite(invite);
	}


	/** Signals that the phone is ringing.
	  * This method should be called when the InviteDialog is in D_INVITED or D_ReINVITED state. */
	public void ring() {
		LOG.debug("inside ring()");
		respond(SipResponses.RINGING, null, null, null, null);
	}


	/** Accepts the incoming call.
	  * This method should be called when the InviteDialog is in D_INVITED or D_ReINVITED state. */
	public void accept(NameAddress contact, SdpMessage sdp) {
		LOG.debug("inside accept(contact,sdp)");
		respond(SipResponses.OK, null, contact, "application/sdp", sdp.toString().getBytes());
	}

	
	/** Refuses the incoming call.
	  * This method should be called when the InviteDialog is in D_INVITED or D_ReINVITED state. */
	public void refuse() {
		LOG.debug("inside refuse()");
		//refuse(480,null);
		//refuse(603,null);
		//refuse(403,null);
		refuse(SipResponses.BUSY_HERE, null);
	}


	/**
	 * Refuses the incoming call. This method should be called when the InviteDialog is in D_INVITED
	 * or D_ReINVITED state.
	 * 
	 * @param code
	 *        See {@link SipResponses}.
	 */
	public void refuse(int code, String reason) {
		LOG.debug("inside refuse({},{})", code, (reason != null) ? reason : SipResponses.reasonOf(code));
		respond(code,reason,null,null,null);
	}


	/**
	 * Redirects the incoming call , specifing the <i>code</i> and <i>reason</i>. This method can be
	 * called when the InviteDialog is in D_INVITED or D_ReINVITED state.
	 * 
	 * @param code
	 *        See {@link SipResponses}.
	 */
	public void redirect(int code, String reason, NameAddress contact) {
		LOG.debug("inside redirect({},{},{})", code, reason, contact);
		respond(code,reason,contact,null,null);
	}


	/**
	 * Responds with <i>code</i> and <i>reason</i>. This method can be called when the InviteDialog
	 * is in D_INVITED, D_ReINVITED states.
	 * 
	 * @param code
	 *        See {@link SipResponses}.
	 * @param content_type
	 *        the type of the content to be included within the response (or <i>null</i> in case of
	 *        no message body)
	 * @param body
	 *        the message body to be included within the response message, or <i>null</i>
	 */
	public void respond(int code, String reason, NameAddress contact, String content_type, byte[] body) {
		LOG.debug("inside respond({},{})", code, reason);
		if (statusIs(DialogStatus.D_INVITED) || statusIs(DialogStatus.D_ReINVITED)) {
			SipMessage resp=sipMessageFactory.createResponse(invite_req,code,reason,contact);
			resp.setBody(content_type,body);
			// ALLOWED METHODS
			if (allowed_methods!=null && allowed_methods.length>0) {
				resp.setAllowHeader(new AllowHeader(allowed_methods));
			}
			respond(resp);
		}
		else {
			LOG.warn("Dialog isn't in \"invited\" state: cannot respond ({}/{}/{})", code, getStatus(), getDialogID());
		}
	}


	/** Responds with <i>resp</i>.
	  * This method can be called when the InviteDialog is in D_INVITED or D_BYED states.
	  * <p>
	  * If the CSeq method is INVITE and the response is 2xx,
	  * it moves to state D_ACCEPTED, adds a new listener to the SipProviderListener,
	  * and creates new AckTransactionServer
	  * <p>
	  * If the CSeq method is INVITE and the response is not 2xx,
	  * it moves to state D_REFUSED, and sends the response. */
	//private void respond(SipMessage resp) {
	public void respond(SipMessage resp) {
		LOG.debug("inside respond(resp)");
		String method=resp.getCSeqHeader().getMethod();
		if (method.equals(SipMethods.INVITE)) {
			if (!verifyStatus("Invite dialog not in INVITED state, no response yet.",
					statusIs(DialogStatus.D_INVITED) || statusIs(DialogStatus.D_ReINVITED))) {
				return;
			}     
			int code = resp.getStatusLine().getCode();
			// OPTIONAL EXTENSIONS
			if (code>=200 && code<300) {
				if (supported_option_tags != null && supported_option_tags.length > 0) {
					resp.setSupportedHeader(new SupportedHeader(supported_option_tags));
				}
			}
			// 1xx provisional responses
			if (code>=100 && code<200) {
				if (sip_provider.sipConfig().isEarlyDialog()) {
					updateDialogInfo(false, resp);
				}

				// RELIABILITY OF PROVISIONAL RESPONSES
				if (code!=100 && ((invite_req.hasRequireHeader() && invite_req.getRequireHeader().hasOptionTag(SipStack.OTAG_100rel)) || (isExtensionRequired(SipStack.OTAG_100rel) && invite_req.hasSupportedHeader() && invite_req.getSupportedHeader().hasOptionTag(SipStack.OTAG_100rel)))) {
					if (reliable_responder == null) {
						reliable_responder = new ReliableProvisionalResponder(sip_provider, invite_ts, this);
					}
					LOG.debug("respond(): reliable provisional response");
					reliable_responder.respond(resp);
				} else {
					invite_ts.respondWith(resp);
				}
				return;
			}
			// For all final responses establish the dialog
			if (code>=200) {
				// changeStatus(DialogStatus.D_ACCEPTED);
				updateDialogInfo(false,resp);
			}
			// 2xx success responses         
			if (code>=200 && code<300) {
				if (statusIs(DialogStatus.D_INVITED)) {
					changeStatus(DialogStatus.D_ACCEPTED);
				} else {
					changeStatus(DialogStatus.D_ReACCEPTED);
				}
				// terminates the INVITE Transaction server and activates an ACK Transaction server
				invite_ts.terminate();

				// RELIABILITY OF PROVISIONAL RESPONSES
				if (reliable_responder != null) {
					reliable_responder.terminate();
				}

				// SESSION TIMERS
				updateTimers(invite_req, resp);
				ConnectionId conn_id=invite_ts.getTransportConnId();
				ack_ts = new AckTransactionServer(sip_provider, conn_id, invite_req, resp, this);
				ack_ts.respond();
				//if (listener!=null)
				// { if (statusIs(DialogStatus.D_ReACCEPTED)) listener.onDlgReInviteAccepted(this);
				//   else listener.onDlgAccepted(this);
				//}         
				return;
			} else {
				// 300-699 failure responses
				/* if (code>=300) */

				if (statusIs(DialogStatus.D_INVITED)) {
					changeStatus(DialogStatus.D_REFUSED);
				} else {
					changeStatus(DialogStatus.D_ReREFUSED);
				}
				invite_ts.respondWith(resp);
				//if (listener!=null)
				// { if (statusIs(DialogStatus.D_ReREFUSED)) listener.onDlgReInviteRefused(this);
				//   else listener.onDlgRefused(this);         
				//}         
				return;
			}
		} else if (method.equals(SipMethods.UPDATE)) {
			if (!verifyStatus("Update method expects early or confirmed state.", isEarly() || isConfirmed())) {
				return;
			}
			updateDialogInfo(false,resp);
			update_ts.respondWith(resp);
		} else if (method.equals(SipMethods.BYE)) {
			if (!verifyStatus("BYE method expects that bye was sent.", statusIs(DialogStatus.D_BYED))) {
				return;
			}
			bye_ts.respondWith(resp);
		}
	}

	private void updateTimers(SipMessage req, SipMessage resp) {
		if (req.hasSupportedHeader() && req.getSupportedHeader().hasOptionTag(SipStack.OTAG_timer)
				&& req.hasSessionExpiresHeader()) {
			SessionExpiresHeader requestExpiresHeader = req.getSessionExpiresHeader();
			int sessionTimeout = requestExpiresHeader.getDeltaSeconds();
			if (session_interval > 0 && sessionTimeout > session_interval) {
				sessionTimeout = session_interval;
			} else {
				session_interval = sessionTimeout;
			}

			refresher = requestExpiresHeader.getRefresher();
			if (refresher == null) {
				refresher = SessionExpiresHeader.PARAM_REFRESHER_UAS;
			}

			addRequireTimer(resp);
			resp.setSessionExpiresHeader(new SessionExpiresHeader(sessionTimeout, refresher));
		} else if (session_interval > 0) {
			MinSEHeader minHeader = req.getMinSEHeader();
			int min_seconds = (minHeader != null) ? minHeader.getDeltaSeconds()
					: sip_provider.sipConfig().getMinSessionInterval();
			if (min_seconds > session_interval) {
				session_interval = min_seconds;
			}
			if (req.hasSupportedHeader() && req.getSupportedHeader().hasOptionTag(SipStack.OTAG_timer)) {
				addRequireTimer(resp);
			}
			refresher = SessionExpiresHeader.PARAM_REFRESHER_UAS;
			resp.setSessionExpiresHeader(new SessionExpiresHeader(session_interval, refresher));
		}
	}

	private void addRequireTimer(SipMessage resp) {
		RequireHeader requireHeader = resp.getRequireHeader();
		if (requireHeader == null) {
			requireHeader = new RequireHeader(SipStack.OTAG_timer);
		} else if (!requireHeader.hasOptionTag(SipStack.OTAG_timer)) {
			requireHeader.addOptionTag(SipStack.OTAG_timer);
		}
		resp.setRequireHeader(requireHeader);
	} 
  

	/** Sends the invite confirmation (ACK) with an "answer", when the "offer" were within the 2xx response instead of being in the request (INVITE).
	  * @param contact the local contact address
	  * @param session_descriptor the SDP answer */
	public void confirm2xxWithAnswer(NameAddress contact, SdpMessage session_descriptor) {
		if (contact!=null) setLocalContact(contact);
		SipMessage ack = sipMessageFactory.create2xxAckRequest(this, inv2xx_resp, "application/sdp",
				session_descriptor.toString().getBytes());
		confirm2xxWithAnswer(ack);
	}


	/** Sends the invite confirmation (ACK) with an "answer", when the "offer" were within the 2xx response instead of being in the request (INVITE).
	  * @param ack the ACK message */
	public void confirm2xxWithAnswer(SipMessage ack) {
		this.ack_req=ack;
		// reset the offer/answer flag to the default value
		invite_offer=true;
		if (ack.hasContactHeader()) setLocalContact(ack.getContactHeader().getNameAddress());
		AckTransactionClient ack_tc=new AckTransactionClient(sip_provider,ack,null);
		ack_tc.request();
	}


	/** Sends the 1xx confirmation (PRACK) for a reliable provisional (1xx) response.
	  * @param resp_1xx the reliable 1xx response the has to be confirmed by the PRACK
	  * @param content_type the type of the content to be included within the PRACK (or <i>null</i> in case of no message body)
	  * @param body the message body to be included within the PRACK, or <i>null</i>. In an offer/answer model, this body represents the answer to the offer contained in the 1xx response message */
	public void confirm1xx(SipMessage resp_1xx, String content_type, byte[] body) {
		SipMessage prack=sipMessageFactory.createPrackRequest(this,resp_1xx,content_type,body);
		confirm1xx(prack);
	}


	/** Sends the 1xx confirmation (PRACK) for a reliable provisional (1xx) response.
	  * @param prack the PRACK confirmation message */
	public void confirm1xx(SipMessage prack) {
		if (prack.isPrack()) {
			LOG.info("confirm1xx(): sending PRACK");
			(new TransactionClient(sip_provider,prack,null)).request();
		}
	}


	/** Whether there are some provisional (1xx) responses that have not been confirmed yet.
	  * @return true if one or more provisional responses are still waiting for a confirmation (PRACK) */
	public boolean hasPendingReliableProvisionalResponses() {
		return reliable_responder!=null && reliable_responder.hasPendingResponses();
	}


	/** Termiante the call.
	  * This method should be called when the InviteDialog is in D_CALL state.
	  * <p>
	  * Increments the Cseq, moves to state D_BYEING, and creates new BYE TransactionClient. */
	public void bye() {
		LOG.debug("inside bye()");
		if (statusIs(DialogStatus.D_CALL)) {
			SipMessage bye=sipMessageFactory.createByeRequest(this);
			bye(bye);        
		}
	}


	/** Termiante the call.
	  * This method should be called when the InviteDialog is in D_CALL state.
	  * <p>
	  * Increments the Cseq, moves to state D_BYEING, and creates new BYE TransactionClient.
	  * @param bye the BYE request message */
	public void bye(SipMessage bye) {
		LOG.debug("inside bye(bye)");
		if (statusIs(DialogStatus.D_CALL)) {
			changeStatus(DialogStatus.D_BYEING);
			//dialog_state.incLocalCSeq(); // done by SipMessageFactory.createRequest()
			TransactionClient tc=new TransactionClient(sip_provider,bye,this);
			tc.request();
			//if (listener!=null) listener.onDlgByeing(this);         
		}
	}


	/** Cancel the ongoing call request or a call listening.
	  * This method should be called when the InviteDialog is in D_INVITING (or D_ReINVITING) state
	  * or in the D_WAITING (or D_ReWAITING) state */
	public void cancel() {
		LOG.debug("inside cancel()");
		if (statusIs(DialogStatus.D_INVITING) || statusIs(DialogStatus.D_ReINVITING)) {
			if (invite_tc.isProceeding())  {
				SipMessage cancel=sipMessageFactory.createCancelRequest(invite_req);
				cancel(cancel);
			}
			else {
				invite_tc.terminate();
			}
		}
		else
		if (statusIs(DialogStatus.D_WAITING) || statusIs(DialogStatus.D_ReWAITING)) {
			invite_ts.terminate();
		}      
	}


	/** Cancel the ongoing call request or a call listening.
	  * This method should be called when the InviteDialog is in D_INVITING or D_ReINVITING state
	  * or in the D_WAITING state */
	public void cancel(SipMessage cancel) {
		LOG.debug("inside cancel(cancel)");
		if (statusIs(DialogStatus.D_INVITING) || statusIs(DialogStatus.D_ReINVITING)) {
			if (invite_tc.isProceeding())  {
				// changeStatus(DialogStatus.D_CANCELING);
				TransactionClient tc=new TransactionClient(sip_provider,cancel,null);
				tc.request();
			}
			else {
				invite_tc.terminate();
			}
		}
		else
		if (statusIs(DialogStatus.D_WAITING) || statusIs(DialogStatus.D_ReWAITING)) {
			invite_ts.terminate();
		}      
	}


	/** Sends info. */
	public void info(String content_type, byte[] body) {
		LOG.debug("inside info(content_type,body)");
		if (statusIs(DialogStatus.D_CALL)) {
			SipMessage req=sipMessageFactory.createRequest(this,SipMethods.INFO,content_type,body);
			// INFO PACKAGES
			if (info_packages!=null && info_packages.length>0) {
				req.setRecvInfoHeader(new RecvInfoHeader(info_packages));
			}
			info(req);        
		}
	}


	/** Sends info. */
	public void info(SipMessage req) {
		LOG.debug("inside info(req)");
		if (statusIs(DialogStatus.D_CALL)) {
			(new TransactionClient(sip_provider,req,this)).request();
		}
	}


	/** Sends a new UPDATE request message within the dialog.
	  * It updates parameter of a session (either in early or confirmed state).
	  * @param contact the contact url (null for default contact)
	  * @param session_descriptor SDP body */
	public void update(NameAddress contact, String session_descriptor) {
		LOG.debug("inside update(contact,sdp)");
		if (!(isEarly() || isConfirmed())) return;
		// else
		SipMessage req=sipMessageFactory.createRequest(this,SipMethods.UPDATE,"application/sdp",session_descriptor.getBytes());
		if (contact!=null) req.setContactHeader(new ContactHeader(contact));
		update(req);
	}


	/** Sends a new UPDATE request message within the dialog.
	  * It updates parameter of a session (either in early or confirmed state).
	  * @param req the UPDATE request message. */
	public void update(SipMessage req) {
		LOG.debug("inside update(update)");
		if (!(isEarly() || isConfirmed())) return;
		// else
		//updateDialogInfo(true,req);
		(new TransactionClient(sip_provider,req,this)).request();
	}

	/** Accepts an UPDATE request.
	  * @param sdp the answered sdp (if any). */
	public void acceptUpdate(SipMessage update, SdpMessage sdp) {
		if (update_ts == null) {
			LOG.warn("Cannot accept update outside transaction.");
			return;
		}

		LOG.info("Accepting update.");
		SipMessage resp = sipMessageFactory.createResponse(update_ts.getRequestMessage(), SipResponses.OK, null, null);
		updateTimers(update, resp);
		if (sdp != null) {
			resp.setSdpBody(sdp);
		}
		respond(resp);
	} 

	/** Refuses an UPDATE request. */
	public void refuseUpdate() {
		if (update_ts == null) {
			LOG.warn("Cannot refuse update outside transation.");
			return;
		}

		LOG.info("Refusing update.");
		SipMessage resp = sipMessageFactory.createResponse(update_ts.getRequestMessage(), SipResponses.SERVER_TIME_OUT,
				null, null);
		respond(resp);
	} 

	/** Termiantes the dialog without sending a CANCEL request for an ongoing INVITE, or a 4xx response to an incoming INVITE, or a BYE request for a confirmed session. */
	public void terminate() {
		LOG.info("terminate()");
		if (!statusIs(DialogStatus.D_CLOSE)) {
			changeStatus(DialogStatus.D_CLOSE);
			if (invite_tc!=null) invite_tc.terminate();
			if (invite_ts!=null) invite_ts.terminate();
			if (cancel_ts!=null) cancel_ts.terminate();
			if (update_ts!=null) update_ts.terminate();
			if (ack_ts!=null) ack_ts.terminate();         
			if (bye_ts!=null) bye_ts.terminate();
			invite_tc=null;
			invite_ts=null;
			cancel_ts=null;
			update_ts=null;
			ack_ts=null;
			bye_ts=null;
			if (listener!=null) listener.onDlgClosed(this);         
		}
		else LOG.debug("dialog already closed: nothing to do.");
	}


	// ************************* Callback methods *************************

	/**
	 * From {@link SipProviderListener}. Called when a new message is received (out of any ongoing
	 * transaction) for the current {@link InviteDialog}. Always checks for out-of-date methods
	 * (CSeq header sequence number).
	 * <p>
	 * If the message is ACK(2xx/INVITE) request, it moves to D_CALL state, and fires
	 * {@link InviteDialogListener#onDlgAck(InviteDialog, SdpMessage, SipMessage)}.
	 * </p>
	 * <p>
	 * If the message is 2xx(INVITE) response, it create a new {@link AckTransactionClient}.
	 * </p>
	 * <p>
	 * If the message is BYE, it moves to D_BYED state, removes the listener from
	 * {@link SipProvider}, fires {@link InviteDialogListener#onDlgBye(InviteDialog, SipMessage)}
	 * and then it responds with 200 OK, moves to D_CLOSE state and fires
	 * {@link InviteDialogListener#onDlgClosed(InviteDialog)}.
	 * </p>
	 */
	@Override
	public void onReceivedMessage(SipProvider sip_provider, SipMessage msg) {
		LOG.debug("inside onReceivedMessage(sip_provider,message)");
		// if request
		if (msg.isRequest()) {
			// check CSeq
			if (!(msg.isAck() || msg.isCancel()) && msg.getCSeqHeader().getSequenceNumber()<=getRemoteCSeq()) {
				LOG.info("Request message is too late (CSeq too small): Message discarded");
				return;
			}

			// SESSION TIMERS
			if (msg.hasSupportedHeader() && msg.getSupportedHeader().hasOptionTag(SipStack.OTAG_timer) && msg.hasSessionExpiresHeader() && msg.getSessionExpiresHeader().getDeltaSeconds()<sip_provider.sipConfig().getMinSessionInterval()) {
				SipMessage resp = sipMessageFactory.createResponse(msg, SipResponses.SESSION_INTERVAL_TOO_SMALL, null,
						null);
				resp.setMinSEHeader(new MinSEHeader(sip_provider.sipConfig().getMinSessionInterval()));
				(new TransactionServer(sip_provider,msg,null)).respondWith(resp);
				return;
			}

			if (msg.isInvite())       {
				verifyStatus("INVITE method requires INIT or CALL state.",
						statusIs(DialogStatus.D_INIT) || statusIs(DialogStatus.D_CALL));
				// NOTE: you arrive here in D_INIT if this method is called by the constructor
				// InviteDialog(SipProvider,SipMessage,InviteDialogListener);
				// otherwise, INVITE message in D_INIT are captured by the callback method onTransRequest().
				if (statusIs(DialogStatus.D_INIT)) {
					changeStatus(DialogStatus.D_INVITED);
				} else {
					changeStatus(DialogStatus.D_ReINVITED);
				}
				this.invite_req=msg;
				updateDialogInfo(false,msg);
				invite_ts=new InviteTransactionServer(sip_provider,msg,this);
				SipMessage resp=processInviteMessage(msg);
				if (resp != null) {
					respond(resp);
				} else {
					cancel_ts=new CancelTransactionServer(sip_provider,invite_req,this);
					cancel_ts.listen();
					if (listener!=null) {
						if (statusIs(DialogStatus.D_INVITED)) {
							listener.onDlgInvite(this, invite_req.getToHeader().getNameAddress(),
									invite_req.getFromHeader().getNameAddress(), invite_req.getSdpBody(), invite_req);
						} else {
							listener.onDlgReInvite(this, invite_req.getSdpBody(), invite_req);
						}
					}
				}
			} else if (msg.isAck()) {
				// if ack (for 2xx)
				if (!verifyStatus("ACK method requires ACCEPTED state.",
						statusIs(DialogStatus.D_ACCEPTED) || statusIs(DialogStatus.D_ReACCEPTED))) {
					return;
				}
				changeStatus(DialogStatus.D_CALL);
				// terminate the ack transaction server
				ack_ts.terminate();
				// terminate the cancel transaction server
				if (cancel_ts!=null) cancel_ts.terminate();
				if (listener != null)
					listener.onDlgAck(this, msg.getSdpBody(), msg);
				if (listener!=null) listener.onDlgCall(this);
			} else if (msg.isBye()) {
				if (!verifyStatus("BYE method requires CALL or BYEING state.",
						statusIs(DialogStatus.D_CALL) || statusIs(DialogStatus.D_BYEING))) {
					return;
				}
				changeStatus(DialogStatus.D_BYED);
				bye_ts=new TransactionServer(sip_provider,msg,this);
				// automatically send a 200 OK
				SipMessage resp = sipMessageFactory.createResponse(msg, SipResponses.OK, null, null);
				respond(resp);
				if (listener!=null) listener.onDlgBye(this,msg);
				changeStatus(DialogStatus.D_CLOSE);
				if (listener!=null) listener.onDlgClosed(this);         
//			} else if (msg.isCancel()) {
//				if (!verifyStatus("CANCEL method expects INVITED state.",
//						statusIs(DialogStatus.D_INVITED) || statusIs(DialogStatus.D_ReINVITED))) {
//					return;
//				}
//
//				// create a CANCEL TransactionServer and send a 200 OK (CANCEL)
//				TransactionServer ts = new TransactionServer(sip_provider, msg, null);
//				ts.respondWith(sip_provider.messageFactory().createResponse(msg, 200, null, null));
//
//				// automatically sends a 487 Cancelled SipMessage
//				respond(sip_provider.messageFactory().createResponse(invite_req, 487, null, null));
//				if (listener != null) {
//					listener.onDlgCancel(this, msg);
//				}
			} else if (msg.isInfo()) {
				TransactionServer ts=new TransactionServer(sip_provider,msg,null);
				ts.respondWith(sipMessageFactory.createResponse(msg, SipResponses.OK, null, null));
				String info_package=(msg.hasInfoPackageHeader())? info_package=msg.getInfoPackageHeader().getPackage() : null;
				if (listener!=null) listener.onDlgInfo(this,info_package,msg.getContentTypeHeader().getContentType(),msg.getBody(),msg);
			} else if (msg.isPrack()) {
				// RELIABILITY OF PROVISIONAL RESPONSES
				if (reliable_responder!=null) reliable_responder.processPrack(sip_provider, msg);
			} else if (msg.isUpdate()) {
				verifyStatus("UPDATE requires early or confirmed state.", isEarly() || isConfirmed());
				LOG.debug("Received update request.");
				updateDialogInfo(false,msg);
				update_ts=new TransactionServer(sip_provider,msg,null);
				if (listener != null) {
					listener.onDlgUpdate(this, msg.getSdpBody(), msg);
				}
			} else {
				// if any other request
				TransactionServer ts=new TransactionServer(sip_provider,msg,null);
				ts.respondWith(sipMessageFactory.createResponse(msg, SipResponses.METHOD_NOT_ALLOWED, null, null));
			}
		} else if (msg.isResponse()) {
			if (!verifyStatus("Response method requires CALL state.", statusIs(DialogStatus.D_CALL))) {
				return;
			}
			int code=msg.getStatusLine().getCode();
			verifyCode(code,"code 2xx was expected", code>=200 && code<300);
			// keep sending ACK (if already sent) for any "200 OK" received
			if (ack_req!=null) {
				AckTransactionClient ack_tc=new AckTransactionClient(sip_provider,ack_req,null);
				ack_tc.request();
			}
		}
	}

	/**
	 * From {@link TransactionClientListener}. When the TransactionClientListener is in "Proceeding"
	 * state and receives a new 1xx response.
	 * <p>
	 * For INVITE transaction it fires
	 * {@link InviteDialogListener#onDlgInviteFailureResponse(InviteDialog, int, String, SipMessage)}.
	 * </p>
	 */
	@Override
	public void onTransProvisionalResponse(TransactionClient tc, SipMessage msg) {
		LOG.debug("inside onTransProvisionalResponse(tc,mdg)");
		if (tc.getTransactionMethod().equals(SipMethods.INVITE)) {
			if (sip_provider.sipConfig().isEarlyDialog()) {
				updateDialogInfo(true, msg);
			}

			StatusLine statusline=msg.getStatusLine();
			// RELIABILITY OF PROVISIONAL RESPONSES
			if (msg.hasRequireHeader() && msg.getRequireHeader().hasOptionTag(SipStack.OTAG_100rel) && msg.hasRSeqHeader()) {
				long rseq=msg.getRSeqHeader().getSequenceNumber();
				// transmit the PRACK request only in case it is the first time that such provisional response is receive (do not retrasmit the PRACK when receive a retransmission of the provisional response)
				final long last_rseq=getLastRSeq();
				if (last_rseq<0 || last_rseq<rseq) {
					setLastRSeq(rseq);
					if (sip_provider.sipConfig().isAutoPrack()) confirm1xx(msg,null,null);
					if (listener != null) {
						listener.onDlgInviteReliableProvisionalResponse(this, statusline.getCode(),
								statusline.getReason(),
								(msg.hasContentTypeHeader()) ? msg.getContentTypeHeader().getContentType() : null,
								msg.getBody(), msg);
					}
				}
			} else {
				if (listener != null) {
					listener.onDlgInviteProvisionalResponse(this, statusline.getCode(), statusline.getReason(),
							msg.getStringBody(), msg);
				}
			}
		}
	}


	/** From TransactionClientListener. When the TransactionClientListener goes into the "Completed" state, receiving a failure response.
	  * <p>
	  * If called for a INVITE transaction, it moves to D_CLOSE state, removes the listener from SipProvider.
	  * <p>
	  * If called for a BYE transaction, it moves to D_CLOSE state,
	  * removes the listener from SipProvider, and fires <i>onClose(this,msg)</i>. */
	@Override
	public void onTransFailureResponse(TransactionClient tc, SipMessage msg) {
		LOG.debug("inside onTransFailureResponse({},{})", tc.getTransactionId(), msg);
		if (tc.getTransactionMethod().equals(SipMethods.INVITE)) {
			if (!verifyStatus("INVITE failure response requires INVITING state.",
					statusIs(DialogStatus.D_INVITING) || statusIs(DialogStatus.D_ReINVITING))) {
				return;
			}
			StatusLine statusline=msg.getStatusLine();
			int code=statusline.getCode();
			verifyCode(code,"error code was expected", code>=300 && code <700);
			if (statusIs(DialogStatus.D_ReINVITING)) {
				changeStatus(DialogStatus.D_CALL);
				if (listener!=null) listener.onDlgReInviteFailureResponse(this,code,statusline.getReason(),msg);
			} else {
				changeStatus(DialogStatus.D_CLOSE);
				if (listener!=null)  {
					if (code>=300 && code<400) listener.onDlgInviteRedirectResponse(this,code,statusline.getReason(),msg.getContacts(),msg);
					else listener.onDlgInviteFailureResponse(this,code,statusline.getReason(),msg);
				}
				if (listener!=null) listener.onDlgClosed(this);
			}
		} else if (tc.getTransactionMethod().equals(SipMethods.UPDATE)) {
			if (!(isEarly() || isConfirmed())) {
				return;
			}

			//updateDialogInfo(true,msg);
			StatusLine statusline=msg.getStatusLine();
			int code=statusline.getCode();
			if (listener!=null) listener.onDlgUpdateResponse(this,code,statusline.getReason(),msg.getStringBody(),msg);
		} else if (tc.getTransactionMethod().equals(SipMethods.BYE)) {
			if (!verifyStatus("BYE transaction failure requires BYEING state", statusIs(DialogStatus.D_BYEING))) {
				return;
			}

			StatusLine statusline=msg.getStatusLine();
			int code=statusline.getCode();
			verifyCode(code,"error code was expected", code>=300 && code <700);
			changeStatus(DialogStatus.D_CALL);
			if (listener!=null) listener.onDlgByeFailureResponse(this,code,statusline.getReason(),msg);
		}
	}


	/** From TransactionClientListener. When an TransactionClientListener goes into the "Terminated" state, receiving a 2xx response.
	  * <p>
	  * If called for a INVITE transaction, it updates the dialog information, moves to D_CALL state,
	  * add a listener to the SipProvider, creates a new AckTransactionClient(ack,this),
	  * and fires <i>onSuccessResponse(this,code,body,msg)</i>. 
	  * <p>
	  * If called for a BYE transaction, it moves to D_CLOSE state,
	  * removes the listener from SipProvider, and fires <i>onClose(this,msg)</i>. */
	@Override
	public void onTransSuccessResponse(TransactionClient tc, SipMessage msg) {
		LOG.debug("inside onTransSuccessResponse(tc,msg)");
		if (tc.getTransactionMethod().equals(SipMethods.INVITE)) {
			if (!verifyStatus("INVITE transaction success requires INVITING state.",
					statusIs(DialogStatus.D_INVITING) || statusIs(DialogStatus.D_ReINVITING))) {
				return;
			}

			StatusLine statusline=msg.getStatusLine();
			int code=statusline.getCode();
			if (!verifyCode(code,"2xx for invite was expected", code>=200 && code <300 && msg.getTransactionMethod().equals(SipMethods.INVITE))) return;
			boolean re_inviting = statusIs(DialogStatus.D_ReINVITING);
			changeStatus(DialogStatus.D_CALL);
			updateDialogInfo(true,msg);
			if (invite_offer) {
				//invite_req=SipMessageFactory.createRequest(SipMethods.ACK,dialog_state,sdp.toString());
				//ack=SipMessageFactory.createRequest(this,SipMethods.ACK,null);
				ack_req=sipMessageFactory.create2xxAckRequest(this,msg,null,null);
				AckTransactionClient ack_tc=new AckTransactionClient(sip_provider,ack_req,null);
				ack_tc.request();
			} else {
				inv2xx_resp = msg;
			}
			
			if (!re_inviting) {
				if (listener != null) {
					listener.onDlgInviteSuccessResponse(this, code, statusline.getReason(), msg.getSdpBody(), msg);
					listener.onDlgCall(this);
				}
			} else {
				if (listener != null) {
					listener.onDlgReInviteSuccessResponse(this, code, statusline.getReason(), msg.getSdpBody(), msg);
				}
			}
			// SESSION TIMERS
			if (msg.hasSessionExpiresHeader()) {
				SessionExpiresHeader sh=msg.getSessionExpiresHeader();
				session_interval=sh.getDeltaSeconds();
				refresher=sh.getRefresher();
			}
		} else if (tc.getTransactionMethod().equals(SipMethods.UPDATE)) {
			if (!(isEarly() || isConfirmed())) {
				return;
			}

			updateDialogInfo(true,msg);
			StatusLine statusline=msg.getStatusLine();
			int code=statusline.getCode();
			if (listener!=null) listener.onDlgUpdateResponse(this,code,statusline.getReason(),msg.getStringBody(),msg);
		} else if (tc.getTransactionMethod().equals(SipMethods.BYE)) {
			if (!verifyStatus("BYE transaction success requires BYEING state.", statusIs(DialogStatus.D_BYEING))) {
				return;
			}

			StatusLine statusline=msg.getStatusLine();
			int code=statusline.getCode();
			verifyCode(code,"2xx for bye was expected", code>=200 && code <300);
			changeStatus(DialogStatus.D_CLOSE);
			if (listener!=null) listener.onDlgByeSuccessResponse(this,code,statusline.getReason(),msg);
			if (listener!=null) listener.onDlgClosed(this);         
		}
	}   


	/** From TransactionClientListener. When the TransactionClient goes into the "Terminated" state, caused by transaction timeout. */
	@Override
	public void onTransTimeout(TransactionClient tc) {
		LOG.debug("inside onTransTimeout(tc,msg)");
		if (tc.getTransactionMethod().equals(SipMethods.INVITE)) {
			if (!verifyStatus("INVITE timeout requires INVITING state.",
					statusIs(DialogStatus.D_INVITING) || statusIs(DialogStatus.D_ReINVITING))) {
				return;
			}

			changeStatus(DialogStatus.D_CLOSE);
			if (listener!=null) listener.onDlgInviteTimeout(this);
			if (listener!=null) listener.onDlgClosed(this);
		} else if (tc.getTransactionMethod().equals(SipMethods.BYE)) {
			if (!verifyStatus("BYE timeout requires BYEING state.", statusIs(DialogStatus.D_BYEING))) {
				return;
			}

			changeStatus(DialogStatus.D_CLOSE);
			if (listener!=null) listener.onDlgClosed(this);         
		}
	} 


	/**
	 * From {@link TransactionServerListener}. When the {@link TransactionServer} goes into the
	 * "Trying" state receiving a request.
	 * <p>
	 * If called for a INVITE transaction, it initializes the dialog information, moves to D_INVITED
	 * state, and adds a listener to the {@link SipProvider}, and fires
	 * {@link InviteDialogListener#onDlgInvite(InviteDialog, NameAddress, NameAddress, SdpMessage, SipMessage)}.
	 * </p>
	 */
	@Override
	public void onTransRequest(TransactionServer ts, SipMessage req) {
		LOG.debug("inside onTransRequest(ts,msg)");
		// INVITE
		if (ts.getTransactionMethod().equals(SipMethods.INVITE)) {
			if (!verifyStatus("INVITE transaction requires WAITING state.", statusIs(DialogStatus.D_WAITING))) {
				return;
			}

			changeStatus(DialogStatus.D_INVITED);
			invite_req=req;
			updateDialogInfo(false,req);
			SipMessage resp=processInviteMessage(req);
			if (resp!=null) respond(resp);
			else
			if (listener != null)
				listener.onDlgInvite(this, invite_req.getToHeader().getNameAddress(),
						invite_req.getFromHeader().getNameAddress(), invite_req.getSdpBody(), invite_req);
		} else if (ts.getTransactionMethod().equals(SipMethods.CANCEL)) {
			// always responds with 200 OK to the CANCEL 
			ts.respondWith(sipMessageFactory.createResponse(req, SipResponses.OK, null, null));

			// if in "invited" state, respond with 487 Request Terminated
			if (statusIs(DialogStatus.D_INVITED) || statusIs(DialogStatus.D_ReINVITED)) {
				// automatically sends a 487 Cancelled
				SipMessage resp = sipMessageFactory.createResponse(invite_req, SipResponses.REQUEST_TERMINATED, null,
						null);
				respond(resp);
				if (listener!=null) listener.onDlgCancel(this,req);
			}
		}
	}


	/** Processes INVITE message. */
	private SipMessage processInviteMessage(SipMessage invite) {
		SipMessage refuse_resp=null;
		// FORCE THIS NODE IN THE DIALOG ROUTE
		if (sip_provider.sipConfig().isOnDialogRoute()) {
			SipURI uri=new SipURI(sip_provider.getViaAddress(),sip_provider.getPort());
			uri.addLr();
			invite.addRecordRouteHeader(new RecordRouteHeader(new NameAddress(uri)));
		}
		// OPTIONAL EXTENSIONS
		if (invite.hasRequireHeader()) {
			Vector<String> remote_required_option_tags=invite.getRequireHeader().getAllOptionTags();
			Vector<String> unsupported_option_tags=new Vector<>();
			for (int i=0; i<remote_required_option_tags.size(); i++) {
				String option_tag= remote_required_option_tags.elementAt(i);
				if (!isExtensionSupported(option_tag)) unsupported_option_tags.addElement(option_tag);
			}
			if (unsupported_option_tags.size()>0) {
				refuse_resp = sipMessageFactory.createResponse(invite, SipResponses.BAD_EXTENSION, null, null);
				refuse_resp.setUnsupportedHeader(new UnsupportedHeader(unsupported_option_tags));
			}
		}
		return refuse_resp;
	}


	/**
	 * From {@link TransactionServerListener}. When an {@link InviteTransactionServer} goes into the
	 * "Confirmed" state receiving an ACK for NON-2xx response.
	 * <p>
	 * It moves to D_CLOSE state and removes the listener from {@link SipProvider}.
	 * </p>
	 */
	@Override
	public void onTransFailureAck(InviteTransactionServer ts, SipMessage msg) {
		LOG.debug("inside onTransFailureAck(ts,msg)");
		if (!verifyStatus("ACK failure requires REFUSED state.",
				statusIs(DialogStatus.D_REFUSED) || statusIs(DialogStatus.D_ReREFUSED))) {
			return;
		}

		if (cancel_ts != null) {
			cancel_ts.terminate();
		}
		if (statusIs(DialogStatus.D_ReREFUSED)) {
			changeStatus(DialogStatus.D_CALL);
		} else {
			changeStatus(DialogStatus.D_CLOSE);
			if (listener!=null) listener.onDlgClosed(this);
		}
	}


	/** From AckTransactionServerListener. When the AckTransactionServer goes into the "Terminated" state, caused by transaction timeout. */
	@Override
	public void onTransAckTimeout(AckTransactionServer ts) {
		LOG.debug("inside onAckSrvTimeout(ts)");
		if (!verifyStatus("ACK timeout requires ACCEPTED or REFUSED state.",
				statusIs(DialogStatus.D_ACCEPTED) || statusIs(DialogStatus.D_ReACCEPTED)
						|| statusIs(DialogStatus.D_REFUSED) || statusIs(DialogStatus.D_ReREFUSED))) {
			return;
		}

		LOG.info("No ACK received.");
		changeStatus(DialogStatus.D_CLOSE);
		if (listener!=null) listener.onDlgClosed(this);
	}


	/** From ReliableProvisionalResponderListener. When a provisional response has been confirmed (PRACK). */
	@Override
	public void onReliableProvisionalResponseConfirmation(ReliableProvisionalResponder rr, SipMessage resp, SipMessage prack) {
		if (listener!=null) listener.onDlgInviteReliableProvisionalResponseConfirmed(this,resp.getStatusLine().getCode(),resp,(prack.hasContentTypeHeader())? prack.getContentTypeHeader().getContentType() : null,prack.getBody(),prack);
	}

	
	/** From ReliableProvisionalResponderListener. When the retransmission timeout expired without receiving coinfirmation. */
	@Override
	public void onReliableProvisionalResponseTimeout(ReliableProvisionalResponder rr, SipMessage resp) {
		if (listener!=null) listener.onDlgInviteReliableProvisionalResponseTimeout(this,resp.getStatusLine().getCode(),resp);
	}

}
