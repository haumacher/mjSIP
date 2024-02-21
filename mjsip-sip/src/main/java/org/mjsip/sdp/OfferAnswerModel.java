/*
 * Copyright (C) 2006 Luca Veltri - University of Parma - Italy
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



import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.mjsip.sdp.field.MediaField;
import org.zoolu.util.VectorUtils;



/** Class OfferAnswerModel collects some static methods for managing SDP materials
  * in accord to RFC3264 ("An Offer/Answer Model with the Session Description Protocol (SDP)").
  */
public class OfferAnswerModel {
	
	/** Costructs a new SessionDescriptor from a given SessionDescriptor
	  * with olny media types and attribute values specified by a MediaDescriptor Vector.
	  * <p> If no attribute is specified for a particular media, all present attributes are kept.
	  * <br>If no attribute is present for a selected media, the media is kept (regardless any sepcified attributes).
	  * @param sdp the given SessionDescriptor
	  * @param m_descs Vector of MediaDescriptor with the selecting media types and attributes
	  * @return this SessionDescriptor */
	/*public static SessionDescriptor sdpMediaProduct(SessionDescriptor sdp, Vector m_descs) {
		Vector new_media=new Vector();
		if (m_descs!=null) {
			for (Enumeration e=m_descs.elements(); e.hasMoreElements(); ) {
				MediaDescriptor spec_md=(MediaDescriptor)e.nextElement();
				MediaDescriptor prev_md=sdp.getMediaDescriptor(spec_md.getMedia().getMedia());
				if (prev_md!=null) {
					Vector spec_attributes=spec_md.getAttributes();
					Vector prev_attributes=prev_md.getAttributes();
					if (spec_attributes.size()==0 || prev_attributes.size()==0) {
						new_media.addElement(prev_md);
					}
					else {
						Vector new_attributes=new Vector();
						for (Enumeration i=spec_attributes.elements(); i.hasMoreElements(); ) {
							AttributeField spec_attr=(AttributeField)i.nextElement();
							String spec_name=spec_attr.getAttributeName();
							String spec_value=spec_attr.getAttributeValue();
							for (Enumeration k=prev_attributes.elements(); k.hasMoreElements(); ) {
								AttributeField prev_attr=(AttributeField)k.nextElement();
								String prev_name=prev_attr.getAttributeName();
								String prev_value=prev_attr.getAttributeValue();
								if (prev_name.equals(spec_name) && prev_value.equalsIgnoreCase(spec_value)) {
									new_attributes.addElement(prev_attr);
									break;
								}
							}
						}
						if (new_attributes.size()>0) new_media.addElement(new MediaDescriptor(prev_md.getMedia(),prev_md.getConnection(),new_attributes));
					}
				}
			}
		}
		SessionDescriptor new_sdp=new SessionDescriptor(sdp);
		new_sdp.removeMediaDescriptors();
		new_sdp.addMediaDescriptors(new_media);
		return new_sdp;
	}*/
	
	/** Costructs a new SessionDescriptor from a given SessionDescriptor
	  * with olny the first specified media attribute.
	  * <p> If no attribute is present for a media, the media is dropped.
	  * @param sdp the given SessionDescriptor
	  * @param a_name the attribute name
	  * @return this SessionDescriptor */
	/*public static SessionDescriptor sdpAttributeSelection(SessionDescriptor sdp, String a_name) {
		Vector new_media=new Vector();
		for (Enumeration e=sdp.getMediaDescriptors().elements(); e.hasMoreElements(); ) {
			MediaDescriptor md=(MediaDescriptor)e.nextElement();
			AttributeField attr=md.getAttribute(a_name);
			if (attr!=null) {
				new_media.addElement(new MediaDescriptor(md.getMedia(),md.getConnection(),attr));
			}
		}
		SessionDescriptor new_sdp=new SessionDescriptor(sdp);
		new_sdp.removeMediaDescriptors();
		new_sdp.addMediaDescriptors(new_media);
		return new_sdp;
	}*/
	
	/**
	 * Calculates a SDP product of a starting SDP and an offered SDP.
	 * <p>
	 * The product is calculated as answer of a SDP offer, according to RFC3264.
	 * 
	 * @param localSdp
	 *        the starting SDP (SessionDescriptor)
	 * @param remoteSdp
	 *        the offered SDP (SessionDescriptor)
	 * @return the answered SDP (SessionDescriptor)
	 */
	public static SdpMessage matchSdp(SdpMessage localSdp, SdpMessage remoteSdp) {
		Vector<MediaDescriptor> matchingDescriptors = matchMedia(localSdp.getMediaDescriptors(), remoteSdp.getMediaDescriptors());
	
		return new SdpMessage(localSdp.getOrigin(), remoteSdp.getSessionName(), localSdp.getConnection(),
				localSdp.getTime(), matchingDescriptors);
	}

	/**
	 * Calculates a MediaDescriptor list product of a starting MediaDescriptor list and an offered
	 * MediaDescriptor list.
	 * <p>
	 * The product is calculated as answer of a media offer, according to RFC3264.
	 * 
	 * @param localDescriptors
	 *        the starting MediaDescriptor list (as Vector of MediaDescriptors)
	 * @param remoteDescriptors
	 *        the offered MediaDescriptor list (as Vector of MediaDescriptors)
	 * @return the answered MediaDescriptor list (as Vector of MediaDescriptors)
	 */
	public static Vector<MediaDescriptor> matchMedia(Vector<MediaDescriptor> localDescriptors,
			Vector<MediaDescriptor> remoteDescriptors) {
		Vector<MediaDescriptor> result = new Vector<>();

		Vector<MediaDescriptor> availableDescriptors = VectorUtils.copy(localDescriptors);
		for (MediaDescriptor remote : remoteDescriptors) {
			String mediaType = remote.getMediaField().getMediaType();
			for (int j = 0; j < availableDescriptors.size(); j++) {
				MediaDescriptor available = availableDescriptors.elementAt(j);
				if (available.getMediaField().getMediaType().equals(mediaType)) {
					MediaDescriptor match = matchDescriptor(available, remote);
					result.addElement(match);

					// remove this media from the base list (actually from the aux copy), and break
					availableDescriptors.removeElementAt(j);
					break;
				}
			}
		}
		return result;
	}   

	/** Calculates a MediaDescriptor product of a given MediaDescriptor and an offered
	  * MediaDescriptor.
	  * <p>
	  * The result is calculated as answer of a media offer, according to RFC3264. */
	public static MediaDescriptor matchDescriptor(MediaDescriptor local, MediaDescriptor remote) {
		// select the proper formats 
		MediaField localMedia = local.getMediaField();

		List<String> answerFormats = intersection(localMedia.getFormatList(), remote.getMediaField().getFormatList());

		// Only offer last common format to enforce using the format with the maximum sample rate.
		answerFormats = answerFormats.subList(answerFormats.size() - 1, answerFormats.size());

		// select the 'rtpmap' attributes
		AttributeField[] localAttributes = local.getAttributes("rtpmap");
		Vector<AttributeField> answerAttributes = new Vector<>();
		for (String answerFormat : answerFormats) {
			for (AttributeField localAttr : localAttributes) {
				if (localAttr.getAttributeValue().startsWith(answerFormat)) {
					answerAttributes.addElement(localAttr);
				}
			}
		}
		MediaField answerMedia = new MediaField(localMedia.getMediaType(), localMedia.getPort(), 0,
				localMedia.getTransport(), answerFormats);
		MediaDescriptor answer = new MediaDescriptor(answerMedia, null, answerAttributes);
		
		// select other attributes
		//answer_md.addAttributes(SdesOfferAnswerModel.makeCryptoAttributeProduct(start_md,offer_md));     
		
		return answer;
	}

	private static <T> List<T> intersection(List<T> l1, List<T> l2) {
		List<T> result = new ArrayList<>();
		for (T e2 : l2) {
			for (T e1 : l1) {
				if (e2.equals(e1)) {
					result.add(e2);
					break;
				}
			}
		}
		return result;
	}

}
