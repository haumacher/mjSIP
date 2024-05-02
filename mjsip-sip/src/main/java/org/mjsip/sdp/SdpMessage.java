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

package org.mjsip.sdp;


import java.util.Vector;

import org.mjsip.sdp.field.ConnectionField;
import org.mjsip.sdp.field.KeyField;
import org.mjsip.sdp.field.OriginField;
import org.mjsip.sdp.field.SessionNameField;
import org.mjsip.sdp.field.TimeField;
import org.zoolu.net.AddressType;


/**
 * SDP (Session Description Protocol) message as defined in RFC 4566.
 * <p>
 * A SDP message consists of a session-level description (information that apply to the whole
 * session and all media streams) and zero or more media-level descriptions (details that apply to a
 * single media stream).
 * </p>
 * <p>
 * The session-level part starts with a `v=' line and continues to the first media-level section.
 * The media description starts with an `m=' line and continues to the next media description or to
 * the end of the whole session description. In general, session-level values are the default for
 * all media unless overridden by an equivalent media-level value.
 * </p>
 */
public class SdpMessage {
	
	/** Protocol version */
	private final SdpField _version;

	/** Originator and session identifier */
	private final OriginField _origin;

	/** Session name */
	private final SessionNameField _sessionName;

	/** Session information (optional) */
	private final SdpField _info;

	/** URI of description (optional) */
	private final SdpField _uri;

	/** Email address (optional) */
	private final SdpField _email;

	/** Phone number (optional) */
	private final SdpField _phone;

	/** Connection information; not required if included in all media */
	private ConnectionField _connection;

	/** Zero or more bandwidth information fields */
	private final SdpField[] _bandwidthInfos;

	/** Time field */
	//TimeField t;
	/** One or more time descriptions */
	private final TimeDescription[] _timeDescriptions;

	/** Time zone adjustments (optional) */
	private final SdpField _zone;

	/** Encryption key (optional) */
	private final KeyField _key;

	/** Zero or more session attributes */
	private final Vector<AttributeField> _attributeFields = new Vector<AttributeField>();

	/** Zero or more media descriptions. */
	private final Vector<MediaDescriptor> _mediaDescriptors = new Vector<>();
		
	/**
	 * Creates a new SDP message specifying o, s, c, and t fields.
	 * 
	 * @param origin
	 *        the origin field
	 * @param session
	 *        the session name field
	 * @param connection
	 *        the connection field
	 * @param time
	 *        the time field
	 * @param media_descs
	 *        {@link MediaDescriptor}s to offer.
	 */
	public SdpMessage(OriginField origin, SessionNameField session, ConnectionField connection, TimeField time,
			Vector<MediaDescriptor> media_descs) {
		this(origin, session, connection, new TimeDescription[] { new TimeDescription(time) });
		addMediaDescriptors(media_descs);
	}

	private static String nonNull(String address) {
		if (address==null) address="127.0.0.1";
		return address;
	}
	
	/**
	 * Creates {@link SdpMessage} for the given owner without any media.
	 */
	public static SdpMessage createSdpMessage(String owner, String address) {
		String nonNullAddress = nonNull(address);
		AddressType addrtype = ConnectionField.addressType(nonNullAddress);
		return new SdpMessage(
				new OriginField(owner, addrtype, nonNullAddress),
				new SessionNameField(),
				new ConnectionField(addrtype, nonNullAddress),
				new TimeDescription[] { new TimeDescription(new TimeField()) });
	}

	/** Inits the mandatory fields of the SDP message. */
	private SdpMessage(OriginField o, SessionNameField s, ConnectionField c, TimeDescription[] tv) {
		_version = new SdpField('v', "0");
		_origin = o;
		_sessionName = s;
		_connection = c;
		_timeDescriptions = tv;

		_info = null;
		_uri = null;
		_email = null;
		_phone = null;
		_bandwidthInfos = null;
		_zone = null;
		_key = null;
	}

	/** Creates a new SDP message.
	  * @param sdp the SDP message */
	public SdpMessage(String sdp) {
		SdpParser par=new SdpParser(sdp);
		// parse mandatory fields
		SdpField version = par.parseSdpField('v');
		if (version == null)
			version = new SdpField('v', "0");
		_version = version;

		OriginField origin = par.parseOriginField();
		if (origin == null)
			origin = new OriginField("unknown");
		_origin = origin;

		SessionNameField sessionName = par.parseSessionNameField();
		if (sessionName == null)
			sessionName = new SessionNameField("-");
		_sessionName = sessionName;

		_info = par.parseSdpField('i');
		_uri = par.parseSdpField('u');
		_email = par.parseSdpField('e');
		_phone = par.parseSdpField('p');
		_connection = par.parseConnectionField();
		if (_connection == null)
			_connection = new ConnectionField(AddressType.IP4, "0.0.0.0");
		Vector<SdpField> bb = new Vector<>();
		SdpField b=par.parseSdpField('b');
		while (b!=null) {
			bb.add(b);
			b=par.parseSdpField('b');
		}
		if (bb.size() > 0)
			_bandwidthInfos = bb.toArray(new SdpField[0]);
		else {
			_bandwidthInfos = null;
		}

		Vector<TimeDescription> tt = new Vector<>();
		TimeField t=par.parseTimeField();
		while (t!=null) {
			tt.add(new TimeDescription(t));
			t=par.parseTimeField();
		}
		if (tt.size() > 0)
			_timeDescriptions = tt.toArray(new TimeDescription[0]);
		else {
			_timeDescriptions = null;
		}
		_zone = par.parseSdpField('z');
		_key = par.parseKeyField();
		// parse session attributes
		//av=new Vector();   
		while (par.hasMore() && par.startsWith("a=")) {
			AttributeField attribute=par.parseAttributeField();
			_attributeFields.addElement(new AttributeField(attribute));
		}
		// parse media descriptors
		//media=new Vector();
		MediaDescriptor md;
		while ((md=par.parseMediaDescriptor())!=null) {
			addMediaDescriptor(md);
		}
	} 
	
	/**
	 * A copy of this {@link SdpMessage} with a replaced connection field.
	 */
	public SdpMessage withConnection(ConnectionField connection, Vector<MediaDescriptor> descriptors) {
		SdpMessage result = new SdpMessage(this);
		result._connection = connection;
		result._mediaDescriptors.setSize(0);
		result.addMediaDescriptors(descriptors);
		return result;
	}

	/**
	 * A copy of this {@link SdpMessage} with replaced {@link MediaDescriptor}s.
	 */
	public SdpMessage withMediaDescriptors(Vector<MediaDescriptor> descriptors) {
		SdpMessage result = new SdpMessage(this);
		result._mediaDescriptors.setSize(0);
		result.addMediaDescriptors(descriptors);
		return result;
	}

	/** Creates copy of the given {@link SdpMessage}. */
	private SdpMessage(SdpMessage sd) {
		_version = new SdpField('v', "0");
		_origin = sd._origin;
		_sessionName = sd._sessionName;
		_connection = sd._connection;
		_timeDescriptions = sd._timeDescriptions;
	
		_info = sd._info;
		_uri = sd._uri;
		_email = sd._email;
		_phone = sd._phone;
		_bandwidthInfos = sd._bandwidthInfos;
		_zone = sd._zone;
		_key = sd._key;
	
		for (int i = 0; i < sd._attributeFields.size(); i++)
			_attributeFields.addElement(new AttributeField(sd._attributeFields.elementAt(i)));
		for (int i = 0; i < sd._mediaDescriptors.size(); i++)
			addMediaDescriptor(sd._mediaDescriptors.elementAt(i));
	}

	/** Gets the origin 'o' field */
	public OriginField getOrigin() {
		//System.out.println("DEBUG: inside SessionDescriptor.getOwner(): sdp=\n"+toString());
		return _origin;
	}
 
	/** Gets the session-name 's' field */
	public SessionNameField getSessionName() {
		return _sessionName;
	}

	/** Gets the connection-information 'c' field */
	public ConnectionField getConnection() {
		return _connection;
	}

	/** Gets the time 't' field */
	public TimeField getTime() {
		return _timeDescriptions[0].getTimeField();
	}
  
	/** Gets the key 'k' field */
	public KeyField getKey() {
		return _key;
	}
  
	/** Adds a new MediaDescriptor
	  * @param media_desc a MediaDescriptor
	  * @return this SDP message */
	public SdpMessage addMediaDescriptor(MediaDescriptor media_desc) {
		//printlog("DEBUG: media desc: "+media_desc,5);
		_mediaDescriptors.addElement(MediaDescriptor.copy(media_desc));
		return this;
	}

	/** Adds a Vector of MediaDescriptors
	  * @param media_descs Vector if MediaDescriptor 
	  * @return this SDP message */
	private SdpMessage addMediaDescriptors(Vector<MediaDescriptor> media_descs) {
		//media.addAll(media_descs); // not supported by J2ME..
		for (MediaDescriptor descriptor : media_descs) {
			addMediaDescriptor(descriptor);
		}
		return this;
	}

	/** Gets all MediaDescriptors */
	public Vector<MediaDescriptor> getMediaDescriptors() {
		return _mediaDescriptors;
	}

	/** Gets the first media descriptor of a particular media.
	  * @param media_type the media type
	  * @return the media descriptor */
	public MediaDescriptor getMediaDescriptor(String media_type) {
		return MediaDescriptor.withType(_mediaDescriptors, media_type);
	}

	/** Gets a Vector of session attribute values.
	  * @return a Vector of attribute field */
	public Vector<AttributeField> getAttributes() {
		return _attributeFields;
	} 

	/** Whether it has a particular session attribute
	  * @param attribute_name the attribute name
	  * @return true if found, otherwise returns 'null' */
	public boolean hasAttribute(String attribute_name) {
		for (int i = 0; i < _attributeFields.size(); i++) {
			if (_attributeFields.get(i).getAttributeName().equals(attribute_name))
				return true;
		}
		return false;
	} 
	
	/** Gets the first attribute field of a particular session attribute name.
	  * @param attribute_name the attribute name
	  * @return the attribute field, or 'null' if not found */
	public AttributeField getAttribute(String attribute_name) {
		for (int i = 0; i < _attributeFields.size(); i++) {
			AttributeField af = _attributeFields.get(i);
			if (af.getAttributeName().equals(attribute_name)) return af; 
		}
		return null;
	}

	/** Gets a vector of attribute values of a particular session attribute name.
	  * @param attribute_name the attribute name
	  * @return a vector of attributes (Vector of <code>AttributeField</code>) */
	public Vector<AttributeField> getAttributes(String attribute_name) {
		Vector<AttributeField> v = new Vector<>(_attributeFields.size());
		for (int i = 0; i < _attributeFields.size(); i++) {
			AttributeField af = _attributeFields.get(i);
			if (af.getAttributeName().equals(attribute_name)) v.addElement(af);
		}
		return v;
	} 

	/** Gets a String representation of this object.
	 * @return the string */
	@Override
	public String toString() {
		//String str=v.toString()+o.toString()+s.toString();
		StringBuilder sb=new StringBuilder();
		if (_version!=null) sb.append(_version.toString());
		if (_origin != null)
			sb.append(_origin.toString());
		if (_sessionName != null)
			sb.append(_sessionName.toString());
		if (_info != null)
			sb.append(_info.toString());
		if (_uri != null)
			sb.append(_uri.toString());
		if (_email != null)
			sb.append(_email.toString());
		if (_phone != null)
			sb.append(_phone.toString());
		if (_connection != null)
			sb.append(_connection.toString());
		if (_bandwidthInfos != null)
			for (int i = 0; i < _bandwidthInfos.length; i++)
				sb.append(_bandwidthInfos[i].toString());
		if (_timeDescriptions != null)
			for (int i = 0; i < _timeDescriptions.length; i++)
				sb.append(_timeDescriptions[i].toString());
		if (_zone != null)
			sb.append(_zone.toString());
		if (_key != null)
			sb.append(_key.toString());
		for (int i = 0; i < _attributeFields.size(); i++)
			sb.append(_attributeFields.get(i).toString());
		for (int i=0; i<_mediaDescriptors.size(); i++) sb.append(_mediaDescriptors.elementAt(i).toString());
		return sb.toString();
	}

	public AddressType getAddressType() {
		AddressType addressType = getConnection().getAddressType();
		return addressType;
	}
	
}
