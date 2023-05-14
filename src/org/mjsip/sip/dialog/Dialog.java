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
import org.mjsip.sip.provider.DialogId;
import org.mjsip.sip.provider.SipProvider;
import org.mjsip.sip.provider.SipProviderListener;
import org.zoolu.util.ExceptionPrinter;
import org.zoolu.util.LogLevel;
import org.zoolu.util.Logger;



/** Class Dialog extends DialogInfo maintaining current dialog status ("early", "confirmed", or "terminated").
  */
public abstract class Dialog extends DialogInfo implements SipProviderListener {
	
	
	// ********************* Static private attributes ********************

	/** Dialogs counter */
	private static int dialog_counter=0;


	// *********************** Protected attributes ***********************

	/** Logger */
	protected Logger logger;
 
	/** SipProvider */
	protected SipProvider sip_provider;

	/** Internal dialog status. */
	protected int status;
	
	/** Dialog number */
	protected int dialog_num;

	/** Dialog identifier */
	protected DialogId dialog_id;


	// ************************* Abstract methods *************************

	/** Gets the dialog state */
	abstract protected String getStatus();

	/** Whether the dialog is in "early" state. */
	abstract public boolean isEarly();

	/** Whether the dialog is in "confirmed" state. */
	abstract public boolean isConfirmed();

	/** Whether the dialog is in "terminated" state. */
	abstract public boolean isTerminated();

	/** When a new Message is received by the SipProvider. */
	abstract public void onReceivedMessage(SipProvider provider, SipMessage message);


	// **************************** Costructors *************************** 

	/** Creates a new empty Dialog */
	protected Dialog(SipProvider provider) {
		super(); 
		this.sip_provider=provider;
		this.logger=sip_provider.getLogger();
		this.dialog_num=dialog_counter++;  
		this.status=0;
		this.dialog_id=null;
	}
 

	// ************************* Protected methods ************************

	/** Changes the internal dialog state */
	protected void changeStatus(int newstatus) {
		status=newstatus;
		log(LogLevel.DEBUG,"changed dialog state: "+getStatus());
		
		// remove the sip_provider listener when going to "terminated" state
		if (isTerminated()) {
			if (dialog_id!=null && sip_provider.getListeners().containsKey(dialog_id)) sip_provider.removeSelectiveListener(dialog_id);
		}
		else
		// add sip_provider listener when going to "early" or "confirmed" state
		if (isEarly() || isConfirmed()) {
			if (dialog_id!=null && !sip_provider.getListeners().containsKey(dialog_id)) sip_provider.addSelectiveListener(dialog_id,this);
		}
	}


	/** Whether the dialog state is equal to <i>st</i> */
	protected boolean statusIs(int st) {
		return status==st;
	}


	// ************************** Public methods **************************

	/** Gets the SipProvider of this Dialog. */
	public SipProvider getSipProvider() {
		return sip_provider;
	}


	/** Gets an unique dialog ideintifier */
	public DialogId getDialogID() {
		return dialog_id;
	} 


	/** Updates empty attributes (tags, route set) and mutable attributes (cseqs, contacts), based on a new message.
	  * @param is_client indicates whether the Dialog is acting as transaction client for the current message.
	  * @param msg the message that is used to update the Dialog state */
	public void updateDialogInfo(boolean is_client, SipMessage msg) {
		
		if (isTerminated()) {
			log(LogLevel.WARNING,"trying to update a terminated dialog: do nothing.");
			return;
		}
		// else
		
		boolean secure_old=secure;
		
		update(is_client,sip_provider,msg);
			
		if (secure_old!=secure) log(LogLevel.INFO,"secure dialog: on");

		// update dialog_id and sip_provider listener
		DialogId new_dialog_id=new DialogId(call_id,local_tag,remote_tag);
		if (dialog_id==null || !dialog_id.equals(new_dialog_id)) {
			log(LogLevel.INFO,"new dialog-id: "+new_dialog_id);
			if (sip_provider!=null) sip_provider.addSelectiveListener(new_dialog_id,this);
			if (dialog_id!=null && sip_provider!=null) sip_provider.removeSelectiveListener(dialog_id);
			dialog_id=new_dialog_id;
		}

	}

 
	//**************************** Logs ****************************/

	/** Adds a new string to the default log. */
	protected void log(LogLevel level, String str) {
		if (logger!=null) logger.log(level,"Dialog#"+dialog_num+": "+str);  
	}

	/** Adds the Exception message to the default log. */
	protected final void log(LogLevel level, Exception e) {
		log(level,"Exception: "+ExceptionPrinter.getStackTraceOf(e));
	}

	/** Verifies the correct status; if not logs the event. */
	protected final boolean verifyStatus(boolean expression) {
		return verifyThat(expression,"dialog state mismatching");
	}

	/** Verifies an event; if not logs it. */
	protected final boolean verifyThat(boolean expression, String str) {
		if (!expression) {
			if (str==null || str.length()==0) log(LogLevel.WARNING,"expression check failed. ");
			else log(LogLevel.WARNING,str);
		}
		return expression;
	}

}
