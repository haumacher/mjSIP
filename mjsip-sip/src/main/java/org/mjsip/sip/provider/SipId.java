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
package org.mjsip.sip.provider;

import org.mjsip.sip.header.CSeqHeader;
import org.mjsip.sip.header.ViaHeader;
import org.mjsip.sip.message.SipMessage;
import org.mjsip.sip.message.SipMethods;
import org.zoolu.util.Identifier;

/** SipId is the abstract identifier for addressing a dialog, a transaction, or a given method.
  */
public final class SipId extends Identifier {
	
	/** Identifier for ANY messages (regardless their method). */
	public static final SipId ANY_METHOD = new SipId("ANY");

	public static SipId createDialogId(String call_id, String local_tag, String remote_tag) {
		return new SipId(dialodId(call_id, local_tag, remote_tag));
	}

	public static SipId createDialogId(SipMessage msg) {
		String call_id = msg.getCallIdHeader().getCallId();

		String local_tag, remote_tag;
		if (msg.isRequest()) {
			local_tag = msg.getToHeader().getTag();
			remote_tag = msg.getFromHeader().getTag();
		} else {
			local_tag = msg.getFromHeader().getTag();
			remote_tag = msg.getToHeader().getTag();
		}

		return new SipId(dialodId(call_id, local_tag, remote_tag));
	}

	private static String dialodId(String call_id, String local_tag, String remote_tag) {
		return call_id + "-" + local_tag + "-" + remote_tag;
	}

	public static SipId createTransactionServerId(String method) {
		return new SipId(method);
	}

	public static SipId createTransactionServerId(String call_id, long seqn, String method,
			String sent_by, String branch) {
		return createTransactionId(false, call_id, seqn, method, sent_by, branch);
	}

	public static SipId createTransactionServerId(SipMessage msg) {
		return createTransactionId(false, msg);
	}

	public static SipId createTransactionId(boolean uac, SipMessage msg) {
		String call_id = msg.getCallIdHeader().getCallId();
		ViaHeader top_via = msg.getViaHeader();
		String branch = null;
		String sent_by = null;
		if (top_via != null) {
			if (top_via.hasBranch())
				branch = top_via.getBranch();
			sent_by = top_via.getSentBy();
		}
		CSeqHeader cseqh = msg.getCSeqHeader();
		long seqn = cseqh.getSequenceNumber();
		String method = cseqh.getMethod();
		return new SipId(transactionId(uac, call_id, seqn, method, sent_by, branch));
	}

	public static SipId createTransactionId(boolean uac, String call_id, long seqn, String method,
			String sent_by, String branch) {
		return new SipId(transactionId(uac, call_id, seqn, method, sent_by, branch));
	}

	/**
	 * Gets the string value of the transaction identifier.
	 * 
	 * @param uac
	 *        whether it is a UAC side (true=UAC, false=UAS)
	 * @param call_id
	 *        the call-id
	 * @param seqn
	 *        the CSeq sequence number
	 * @param sent_by
	 *        the Via sent-by address
	 * @param branch
	 *        the Via branch
	 * @return the string value for a transaction identifier
	 */
	private static String transactionId(boolean uac, String call_id, long seqn, String method, String sent_by,
			String branch) {
		if (method.equals(SipMethods.ACK))
			method = SipMethods.INVITE;
		String type = (uac) ? "client" : "server";
		if (branch == null)
			branch = sent_by;
		return call_id + "-" + seqn + "-" + method + "-" + type + "-" + branch;
	}

	public static SipId createTransactionClientId(SipMessage msg) {
		return createTransactionId(true, msg);
	}

	public static SipId createMethodId(String method) {
		return new SipId(method);
	}

	public static SipId createMethodId(SipMessage msg) {
		return new SipId(msg.getCSeqHeader().getMethod());
	}

	/** Creates a new SipId.
	  * @param str_id the string value of the identifier */
	private SipId(String str_id) {
		super(str_id);
	}

	/** Creates a new SipId
	  * @param id a SIP identifier */
	private SipId(SipId id) {
		super(id);
	}
}
