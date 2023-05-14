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
import org.mjsip.sdp.field.MediaField;
import org.mjsip.sdp.field.OriginField;
import org.mjsip.sdp.field.SessionNameField;
import org.mjsip.sdp.field.TimeField;


/** SDP message as defined in RFC 4566.
  * <p>
  * A SDP message consists of a session-level description
  * (information that apply to the whole session and all media streams) and
  * zero or more media-level descriptions (details that apply onto
  * to a single media stream).
  * <p>
  * The session-level part starts with a
  * `v=' line and continues to the first media-level section.  The media
  * description starts with an `m=' line and continues to the next media
  * description or end of the whole session description.  In general,
  * session-level values are the default for all media unless overridden
  * by an equivalent media-level value.
  */
public class SdpMessage {
	
	/** Protocol version */
	SdpField v;
	/** Originator and session identifier */
	OriginField o;
	/** Session name */
	SessionNameField s;
	/** Session information (optional) */
	SdpField i;
	/** URI of description (optional) */
	SdpField u;
	/** Email address (optional) */
	SdpField e;
	/** Phone number (optional) */
	SdpField p;
	/** Connection information; not required if included in all media */
	ConnectionField c;
	/** Zero or more bandwidth information fields */
	SdpField[] bv=null;
	/** Time field */
	//TimeField t;
	/** One or more time descriptions */
	TimeDescription[] tv;
	/** Time zone adjustments (optional) */
	SdpField z;
	/** Encryption key (optional) */
	KeyField k;
	/** Zero or more session attribute (Vector<AttributeField>) */
    //AttributeField[] av=null;
    Vector av=new Vector();
	/** Zero or more media descriptions (Vector<MediaDescriptor>) */
	Vector media=new Vector();
		

	/** Inits the mandatory fields of the SDP message. */
	private void init(OriginField o, SessionNameField s, ConnectionField c, TimeDescription[] tv) {
		this.v=new SdpField('v',"0");
		this.o=o;
		this.s=s;
		this.c=c;
		this.tv=tv;
	}

	/** Creates a new SDP message.
	  * @param sd SDP message to be cloned */
	public SdpMessage(SdpMessage sd) {
		init(sd.o,sd.s,sd.c,sd.tv);
		i=sd.i;
		u=sd.u;
		e=sd.e;
		p=sd.p;
		bv=sd.bv;
		z=sd.z;
		k=sd.k;
		for (int i=0; i<sd.av.size(); i++) addAttribute((AttributeField)sd.av.elementAt(i));
		for (int i=0; i<sd.media.size(); i++) addMediaDescriptor((MediaDescriptor)sd.media.elementAt(i));
	}

	/** Creates a new SDP message specifying o, s, c, and t fields.
	  * @param origin the origin field
	  * @param session the  session name field
	  * @param connection the connection field
	  * @param time the time field */
	public SdpMessage(OriginField origin, SessionNameField session, ConnectionField connection, TimeField time) {
		init(origin,session,connection,new TimeDescription[]{new TimeDescription(time)});
	}

	/** Creates a new SDP message specifying o, s, c, and t fields.
	  * @param origin the origin field value
	  * @param session the session name field value
	  * @param connection the connection field value
	  * @param time the time field value */
	public SdpMessage(String origin, String session, String connection, String time) {
		init(new OriginField(origin),new SessionNameField(session),new ConnectionField(connection),new TimeDescription[]{new TimeDescription(new TimeField(time))});
	}

	/** Creates a new SDP message.
	  * <p> with:
	  * <br> o=<i>owner</i> 0 0 IN IP4 <i>address</i>
	  * <br> s=-
	  * <br> c=IN IP4 <i>address</i>
	  * <br> t=0 0
	  * <p>if <i>address</i>==null, '127.0.0.1' is used
	  * @param owner the owner of the session
	  * @param address the IPv4 address */
	public SdpMessage(String owner, String address) {
		if (address==null) address="127.0.0.1";
		init(new OriginField(owner,null,address),new SessionNameField(),new ConnectionField("IP4",address),new TimeDescription[]{new TimeDescription(new TimeField())});
	}
	
	/** Creates a default SDP message.
	  * <p> with:
	  * <br> o=- 0 0 IN IP4 127.0.0.1
	  * <br> s=-
	  * <br> c=IN IP4 127.0.0.1
	  * <br> t=0 0 */
	public SdpMessage() {
		String address="127.0.0.1";
		init(new OriginField(null,null,address),new SessionNameField(),new ConnectionField("IP4",address),new TimeDescription[]{new TimeDescription(new TimeField())});
	}

	/** Creates a new SDP message.
	  * @param sdp the SDP message */
	public SdpMessage(String sdp) {
		SdpParser par=new SdpParser(sdp);
		// parse mandatory fields
		v=par.parseSdpField('v');
		if (v==null) v=new SdpField('v',"0");
		o=par.parseOriginField();
		if (o==null) o=new OriginField("unknown");
		s=par.parseSessionNameField();
		if (s==null) s=new SessionNameField("-");
		i=par.parseSdpField('i');
		u=par.parseSdpField('u');
		e=par.parseSdpField('e');
		p=par.parseSdpField('p');
		c=par.parseConnectionField();
		if (c==null) c=new ConnectionField("IP4","0.0.0.0");
		Vector bb=new Vector();
		SdpField b=par.parseSdpField('b');
		while (b!=null) {
			bb.add(b);
			b=par.parseSdpField('b');
		}
		if (bb.size()>0) bv=(SdpField[])bb.toArray(new SdpField[0]);
		Vector tt=new Vector();
		TimeField t=par.parseTimeField();
		while (t!=null) {
			tt.add(new TimeDescription(t));
			t=par.parseTimeField();
		}
		if (tt.size()>0) tv=(TimeDescription[])tt.toArray(new TimeDescription[0]);
		z=par.parseSdpField('z');
		k=par.parseKeyField();
		// parse session attributes
		//av=new Vector();   
		while (par.hasMore() && par.startsWith("a=")) {
			AttributeField attribute=par.parseAttributeField();
			addAttribute(attribute);
		}
		// parse media descriptors
		//media=new Vector();
		MediaDescriptor md;
		while ((md=par.parseMediaDescriptor())!=null) {
			addMediaDescriptor(md);
		}
	} 
	
	/** Sets the origin 'o' field.
	  * @param origin the origin field
	  * @return this SDP message */
	public SdpMessage setOrigin(OriginField origin) {
		o=origin;
		return this;
	}

	/** Gets the origin 'o' field */
	public OriginField getOrigin() {
		//System.out.println("DEBUG: inside SessionDescriptor.getOwner(): sdp=\n"+toString());
		return o;
	}
 
	/** Sets the session-name 's' field. 
	  * @param session the SessionNameField
	  * @return this SDP message */
	public SdpMessage setSessionName(SessionNameField session) {
		s=session;
		return this;
	}

	/** Gets the session-name 's' field */
	public SessionNameField getSessionName() {
		return s;
	}

	/** Sets the connection-information 'c' field.
	  * @param connection the ConnectionField
	  * @return this SDP message */
	public SdpMessage setConnection(ConnectionField connection) {
		c=connection;
		return this;
	}

	/** Gets the connection-information 'c' field */
	public ConnectionField getConnection() {
		return c;
	}

	/** Sets the time 't' field.
	  * @param time the TimeField
	  * @return this SDP message */
	public SdpMessage setTime(TimeField time) {
		tv=new TimeDescription[]{new TimeDescription(time)};
		return this;
	}

	/** Gets the time 't' field */
	public TimeField getTime() {
		return tv[0].getTimeField();
	}
  
	/** Sets the key 'k' field.
	  * @param key the KeyField
	  * @return this SDP message */
	public SdpMessage setKey(KeyField key) {
		k=key;
		return this;
	}

	/** Gets the key 'k' field */
	public KeyField getKey() {
		return k;
	}
  
	/** Adds a new attribute for a particular media
	  * @param media the MediaField
	  * @param attribute an AttributeField 
	  * @return this SDP message */
	public SdpMessage addMedia(MediaField media, AttributeField attribute) {
		//printlog("DEBUG: media: "+media,5);
		//printlog("DEBUG: attribute: "+attribute,5);
		addMediaDescriptor(new MediaDescriptor(media,null,attribute));
		return this;
	}
	
	/** Adds a new media.
	  * @param mf the MediaField
	  * @param attributes array of attributes
	  * @return this SDP message */
	public SdpMessage addMedia(MediaField mf, AttributeField[] attributes) {
		//printlog("DEBUG: mf: "+media,5);
		//printlog("DEBUG: attribute: "+attributes,5);
		media.addElement(new MediaDescriptor(mf,null,attributes));
		return this;
	}

	/** Adds a new MediaDescriptor
	  * @param media_desc a MediaDescriptor
	  * @return this SDP message */
	public SdpMessage addMediaDescriptor(MediaDescriptor media_desc) {
		//printlog("DEBUG: media desc: "+media_desc,5);
		media.addElement(new MediaDescriptor(media_desc));
		return this;
	}

	/** Adds a Vector of MediaDescriptors
	  * @param media_descs Vector if MediaDescriptor 
	  * @return this SDP message */
	public SdpMessage addMediaDescriptors(Vector media_descs) {
		//media.addAll(media_descs); // not supported by J2ME..
		for (int i=0; i<media_descs.size(); i++) addMediaDescriptor((MediaDescriptor)media_descs.elementAt(i));
		return this;
	}

	/** Gets all MediaDescriptors */
	public Vector getMediaDescriptors() {
		return media;
	}

	/** Removes all MediaDescriptors
	 * @return this SDP message */
	public SdpMessage removeMediaDescriptor(String media_type) {
		for (int i=media.size()-1; i>=0; i--)
			if (((MediaDescriptor)media.elementAt(i)).getMedia().getMedia().equals(media_type)) media.removeElementAt(i);
		return this;
	}

	/** Removes all MediaDescriptors
	 * @return this SDP message */
	public SdpMessage removeMediaDescriptors() {
		//media.clear(); // not supported by J2ME..
		media.setSize(0);
		return this;
	}
	
	/** Gets the first media descriptor of a particular media.
	  * @param media_type the media type
	  * @return the media descriptor */
	public MediaDescriptor getMediaDescriptor(String media_type) {
		for (int i=0; i<media.size(); i++) {
			MediaDescriptor md=(MediaDescriptor)media.elementAt(i);
			if (md.getMedia().getMedia().equals(media_type)) return md; 
		}
		return null;
	}


	/** Keeps only selected media types; other media are removed.
	  * @param media_types the media types to be kept
	  * @return this SDP message */
	public SdpMessage selectMedia(String[] media_types) {
		if (media_types!=null) {
			Vector md_list=new Vector();
			for (int i=0; i<media_types.length; i++) {
				MediaDescriptor md=getMediaDescriptor(media_types[i]);
				if (md!=null) {
					removeMediaDescriptor(media_types[i]);
					md_list.addElement(md);
				}
			}
			removeMediaDescriptors();
			addMediaDescriptors(md_list);
		}
		return this;
	}


	/** Adds a Vector of session attributes.
	  * @param attributes Vector of AttributeFields
	  * @return this SDP message */
	public SdpMessage addAttributes(Vector attributes) {
		for (int i=0; i<attributes.size(); i++) addAttribute((AttributeField)attributes.elementAt(i));
		return this;
	}

	/** Adds a new session attribute
	  * @param attribute the new AttributeField
	  * @return this SDP message */
	public SdpMessage addAttribute(AttributeField attribute) {
		av.addElement(new AttributeField(attribute));
		return this;
	}

	/** Removes all session attributes.
	* @return this SDP message */
	public SdpMessage removeAttributes() {
		//av.clear(); // not supported by J2ME..
		av.setSize(0);
		return this;
	}

	/** Gets a Vector of session attribute values.
	  * @return a Vector of attribute field */
	public Vector getAttributes() {
		return av;
	} 

	/** Whether it has a particular session attribute
	  * @param attribute_name the attribute name
	  * @return true if found, otherwise returns 'null' */
	public boolean hasAttribute(String attribute_name) {
		for (int i=0; i<av.size(); i++) {
			if (((AttributeField)av.get(i)).getAttributeName().equals(attribute_name)) return true;
		}
		return false;
	} 
	
	/** Gets the first attribute field of a particular session attribute name.
	  * @param attribute_name the attribute name
	  * @return the attribute field, or 'null' if not found */
	public AttributeField getAttribute(String attribute_name) {
		for (int i=0; i<av.size(); i++) {
			AttributeField af=(AttributeField)av.get(i);
			if (af.getAttributeName().equals(attribute_name)) return af; 
		}
		return null;
	}

	/** Gets a vector of attribute values of a particular session attribute name.
	  * @param attribute_name the attribute name
	  * @return a vector of attributes (Vector of <code>AttributeField</code>) */
	public Vector getAttributes(String attribute_name) {
		Vector v=new Vector(av.size());
		for (int i=0; i<av.size(); i++) {
			AttributeField af=(AttributeField)av.get(i);
			if (af.getAttributeName().equals(attribute_name)) v.addElement(af);
		}
		return v;
	} 


	/** Gets a String representation of this object.
	 * @return the string */
	public String toString() {
		//String str=v.toString()+o.toString()+s.toString();
		StringBuffer sb=new StringBuffer();
		if (v!=null) sb.append(v.toString());
		if (o!=null) sb.append(o.toString());
		if (s!=null) sb.append(s.toString());
		if (i!=null) sb.append(i.toString());
		if (u!=null) sb.append(u.toString());
		if (e!=null) sb.append(e.toString());
		if (p!=null) sb.append(p.toString());
		if (c!=null) sb.append(c.toString());
		if (bv!=null) for (int i=0; i<bv.length; i++) sb.append(bv[i].toString());
		if (tv!=null) for (int i=0; i<tv.length; i++) sb.append(tv[i].toString());
		if (z!=null) sb.append(z.toString());
		if (k!=null) sb.append(k.toString());
		for (int i=0; i<av.size(); i++) sb.append(av.get(i).toString());
		for (int i=0; i<media.size(); i++) sb.append(media.elementAt(i).toString());
		return sb.toString();
	}
	
}
