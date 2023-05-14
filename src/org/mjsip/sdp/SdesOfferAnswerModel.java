/*
 * Copyright (C) 2010 Luca Veltri - University of Parma - Italy
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

package org.mjsip.sdp;



import java.util.Vector;

import org.mjsip.sdp.field.CryptoAttributeField;



/** Class SdecsOfferAnswerModel collects some static methods for managing SDP materials
  * in accord to RFC3264 "An Offer/Answer Model with the Session Description Protocol (SDP)"
  * and RFC4568 "Session Description Protocol (SDP) Security Descriptions for Media Streams".
  * <p>
  * In particular RFC4568 defines how a master key and a cryptographic context can be negotiated
  * between two parties for using for secure the transport of unicast media streams,
  * for example through the use of the Secure Real-time Transport Protocol (SRTP).
  */
public class SdesOfferAnswerModel /*extends OfferAnswerModel*/ {
	
	/** Whether working in loopback mode (useful for debugging and testing).
	  * In this case the answered key parameters are the same as in (copied by) the offer. */
	public static boolean DEBUG_LOOPBACK_MODE=false;
	
	
	/** Calculates a SDP product of a starting SDP and an offered SDP.
	  * <p>
	  * The product is calculated as answer of a SDP offer, according to RFC3264 and RFC4568.
	  * @param start_sdp the starting SDP (SessionDescriptor)
	  * @param offer_sdp the offered SDP (SessionDescriptor)
	  * @return the answered SDP (SessionDescriptor) */
	/*public static SessionDescriptor makeSessionDescriptorProduct(SessionDescriptor start_sdp, SessionDescriptor offer_sdp) {
		return OfferAnswerModel.makeSessionDescriptorProduct(start_sdp,offer_sdp);
	}*/


	/** Calculates a MediaDescriptor list product of a starting MediaDescriptor list
	  * and an offered MediaDescriptor list.
	  * <p>
	  * The product is calculated as answer of a media offer, according to RFC3264 and RFC4568.
	  * @param start_md_list the starting MediaDescriptor list (as Vector of MediaDescriptors)
	  * @param offer_md_list the offered MediaDescriptor list (as Vector of MediaDescriptors)
	  * @return the answered MediaDescriptor list (as Vector of MediaDescriptors) */
	/*public static Vector makeMediaDescriptorProduct(Vector start_md_list, Vector offer_md_list) {
		return OfferAnswerModel.makeMediaDescriptorProduct(start_md_list,offer_md_list);
	}*/


	/** Calculates a MediaDescriptor product of a given MediaDescriptor and an offered
	  * MediaDescriptor.
	  * <p>
	  * The result is calculated as answer of a media offer, according to RFC4568. */
	/*public static MediaDescriptor makeMediaDescriptorProduct(MediaDescriptor start_md, MediaDescriptor offer_md) {
		MediaDescriptor answer_md=OfferAnswerModel.makeMediaDescriptorProduct(start_md,offer_md);
		// select the 'crypto' attributes
		answer_md.addAttributes(makeCryptoAttributeProduct(start_md,offer_md));
		return answer_md;
	}*/


	/** Calculates a 'crypto' attributes product of given media attributes
	  * and offered media attributes (as part of the corresponding MediaDescriptors).
	  * <p>
	  * The result is calculated as answer of a media offer, according to RFC4568.
	  * According to RFC4568 only one 'crypto' is generated in case of success.
	  * @return Returns a Vector of AttributeFields. The size of such Vector is 1 if succeeded,
	  * or 0 if falied (no matching found between SDPs according to RFC4568). */
	public static Vector makeCryptoAttributeProduct(MediaDescriptor start_md, MediaDescriptor offer_md) {
		// select the 'crypto' attributes
		AttributeField[] start_attrs=start_md.getAttributes("crypto");
		AttributeField[] offer_attrs=offer_md.getAttributes("crypto");
		CryptoAttributeField answer_cf=null;
		for (int i=0; i<start_attrs.length && answer_cf==null; i++) {
			CryptoAttributeField start_cf=new CryptoAttributeField(start_attrs[i]);
			for (int j=0; j<offer_attrs.length && answer_cf==null; j++) {
				CryptoAttributeField offer_cf=new CryptoAttributeField(offer_attrs[j]);
				if (offer_cf.getCryptoSuite().equals(start_cf.getCryptoSuite())) {
					answer_cf=new CryptoAttributeField(offer_cf.getTag(),offer_cf.getCryptoSuite(),start_cf.getKeyParams());
					// testing in loopback mode
					if (DEBUG_LOOPBACK_MODE) {
						System.out.println("DEBUG: CRYPTO: LOOPBACK");
						answer_cf=offer_cf;
					}
				}
			}
		}
		Vector answer_attr_list=new Vector();
		if (answer_cf!=null) answer_attr_list.addElement(answer_cf);
		if (DEBUG_LOOPBACK_MODE) System.out.println("DEBUG: CRYPTO: "+answer_cf);
		return answer_attr_list;
	}

}
