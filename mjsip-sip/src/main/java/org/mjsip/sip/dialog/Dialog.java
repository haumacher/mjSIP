/*
 * Copyright (C) 2005 Luca Veltri - University of Parma - Italy
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

import org.mjsip.sip.message.SipMessage;
import org.mjsip.sip.provider.SipId;
import org.mjsip.sip.provider.SipProvider;
import org.mjsip.sip.provider.SipProviderListener;
import org.slf4j.LoggerFactory;

/** Class Dialog extends DialogInfo maintaining current dialog status ("early", "confirmed", or "terminated").
  */
public abstract class Dialog extends DialogInfo implements SipProviderListener {
	
	private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(Dialog.class);
	
	// ********************* Static private attributes ********************

	/** Dialogs counter */
	private static int dialog_counter=0;

	// *********************** Protected attributes ***********************

	/** Internal dialog status. */
	private DialogStatus status;
	
	/** Dialog number */
	protected int dialog_num;

	/** Dialog identifier */
	protected SipId dialog_id;

	// ************************* Abstract methods *************************

	/** Gets the dialog state */
	protected DialogStatus getStatus() {
		return status;
	}

	/** Whether the call is not yet accepted. */
	public boolean isEarly() {
		return status.isEarly();
	}

	/** Whether the call is alive (accepted and not yet closed). */
	public boolean isConfirmed() {
		return status.isConfirmed();
	}

	/** Whether the dialog is in "terminated" state. */
	public boolean isTerminated() {
		return status == DialogStatus.D_CLOSE;
	}

	/** Whether the session is "active". */
	public boolean isSessionActive() {
		return status == DialogStatus.D_CALL;
	}

	/** When a new Message is received by the SipProvider. */
	@Override
	abstract public void onReceivedMessage(SipProvider provider, SipMessage message);

	// **************************** Costructors *************************** 

	/** Creates a new empty Dialog */
	protected Dialog(SipProvider provider) {
		super(provider); 
		this.dialog_num=dialog_counter++;  
		this.status = DialogStatus.D_INIT;
		this.dialog_id=null;
	}
 
	// ************************* Protected methods ************************

	/** Changes the internal dialog state */
	protected void changeStatus(DialogStatus newStatus) {
		status = newStatus;
		LOG.debug("Set state of dialog {} to: {}", (dialog_id != null ? dialog_id : ""), getStatus());
		
		// remove the sip_provider listener when going to "terminated" state
		if (dialog_id != null) {
			if (isTerminated()) {
				sip_provider.removeSelectiveListener(dialog_id);
			} else if (isEarly() || isConfirmed()) {
				sip_provider.addSelectiveListener(dialog_id, this);
			}
		}
	}

	/** Whether the dialog state is equal to <i>st</i> */
	protected boolean statusIs(DialogStatus st) {
		return status==st;
	}

	// ************************** Public methods **************************

	/** Gets the SipProvider of this Dialog. */
	public SipProvider getSipProvider() {
		return sip_provider;
	}

	/** Gets an unique dialog ideintifier */
	public SipId getDialogID() {
		return dialog_id;
	} 

	/** Updates empty attributes (tags, route set) and mutable attributes (cseqs, contacts), based on a new message.
	  * @param is_client indicates whether the Dialog is acting as transaction client for the current message.
	  * @param msg the message that is used to update the Dialog state */
	public void updateDialogInfo(boolean is_client, SipMessage msg) {
		if (isTerminated()) {
			LOG.warn("trying to update a terminated dialog: do nothing.");
			return;
		}
		
		boolean secure_old=secure;
		
		update(is_client, msg);
			
		if (LOG.isDebugEnabled()) {
			if (secure_old != secure) {
				LOG.debug("Switing secure dialog to: {}", secure);
			}
		}

		// Update dialog_id and sip_provider listener
		SipId oldDialogId = dialog_id;
		SipId newDialogId = SipId.createDialogId(call_id, local_tag, remote_tag);
		if (oldDialogId == null || !oldDialogId.equals(newDialogId)) {
			if (LOG.isDebugEnabled()) {
				LOG.debug("Updated dialog ID to: {}", newDialogId);
			}

			if (sip_provider != null) {
				if (oldDialogId != null) {
					sip_provider.removeSelectiveListener(oldDialogId);
				}

				sip_provider.addSelectiveListener(newDialogId, this);
			}
			dialog_id = newDialogId;
		}
	}

	/**
	 * Verifies the correct status; if not logs the event.
	 * 
	 * @param message
	 *        The message to log, if condition is false.
	 */
	protected final boolean verifyStatus(String message, boolean expression) {
		if (!expression) {
			LOG.warn("Status {}: {}", getStatus(), message);
		}
		return expression;
	}

	/** Verifies a message code, logs failures. */
	protected final boolean verifyCode(int code, String message, boolean expression) {
		if (!expression) {
			LOG.warn("Code {}: {}", code, message);
		}
		return expression;
	}
}
