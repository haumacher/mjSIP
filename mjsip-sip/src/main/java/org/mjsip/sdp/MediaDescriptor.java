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
import org.mjsip.sdp.field.MediaField;
import org.zoolu.util.VectorUtils;


/**
 * A raw SDP media description record.
 * 
 * <p>
 * A {@link MediaDescriptor} can be part of a SessionDescriptor, and contains details that apply to
 * a single media stream.
 * </p>
 * <p>
 * A single SessionDescriptor may contain zero or more {@link MediaDescriptor}s.
 * </p>
 * <p>
 * In the current implementation, the {@link MediaDescriptor} consists of the <code>m</code> (media)
 * and <code>c</code> (connection information) fields, followed by zero or more <code>a</code>
 * (attribute) fields. The <code>m</code> field is mandatory.
 * </p>
 */
public class MediaDescriptor {
	
	/** Media field ('m'). */
	private final MediaField m;

	/** Connection field ('c') */
	private final ConnectionField c;

	/** Vector of attribute fileds ('a') */
	private final Vector<AttributeField> av;

	/** Creates a new MediaDescriptor with m=<i>media</i> and c=<i>connection</i>,
	  * with attributes 'a' equals to <i>attributes</i> (Vector of AttributeField).
	  * @param media the MediaField
	  * @param connection the ConnectionField, or null if no ConnectionField
	  * is present in the MediaDescriptor
	  * @param attributes array of attributes */
	public MediaDescriptor(MediaField media, ConnectionField connection, AttributeField[] attributes) {
		m=media;
		c=connection;
		av=VectorUtils.arrayToVector(attributes);
	}

	/** Gets media.
	  * @return the MediaField */
	public MediaField getMedia() {
		return m;
	} 

	/** Gets connection information.
	  * @return the ConnectionField */
	public ConnectionField getConnection() {
		return c;
	} 

	/** Gets attributes.
	  * @return an array of attributes */
	public AttributeField[] getAttributes() {
		return av.toArray(new AttributeField[] {});
	} 

	/** Adds a new attribute.
	  * @param attribute the new AttributeField
	  * @return this MediaDescriptor */
	public MediaDescriptor addAttribute(AttributeField attribute) {
		av.addElement(new AttributeField(attribute));
		return this;
	} 

	/** Adds a new attributes.
	  * @param attributes a vector o new attribute fields (Vector of <code>AttributeField</code>)
	  * @return this MediaDescriptor */
	public MediaDescriptor addAttributes(Vector<AttributeField> attributes) {
		for (int i = 0; i < attributes.size(); i++)
			addAttribute(attributes.elementAt(i));
		return this;
	} 

	/** Whether it has a particular attribute.
	  * @param a_name the attribute name
	  * @return true if found, otherwise returns null */
	public boolean hasAttribute(String a_name) {
		for (int i=0; i<av.size(); i++) {
			if (av.elementAt(i).getAttributeName().equals(a_name))
				return true;
		}
		return false;
	} 
	
	/** Gets a particular attribute.
	  * @param a_name the attribute name
	  * @return the AttributeField, or null if not found */
	public AttributeField getAttribute(String a_name) {
		for (int i=0; i<av.size(); i++) {
			AttributeField a = av.elementAt(i);
			if (a.getAttributeName().equals(a_name)) return a;
		}
		return null;
	} 

	/** Gets all attributes of a particular attribute name.
	  * @param a_name the attribute name
	  * @return an array of attributes */
	public AttributeField[] getAttributes(String a_name) {
		Vector<AttributeField> v = new Vector<>(av.size());
		for (int i=0; i<av.size(); i++) {
			AttributeField a = av.elementAt(i);
			if (a.getAttributeName().equals(a_name)) v.addElement(a);
		}
		return v.toArray(new AttributeField[] {});
	} 
	
	/** Gets a String rapresentation of the MediaDescriptor.
	  * @return the string representation */
	@Override
	public String toString() {
		String str=""; str+=m; if (c!=null) str+=c;
		for (int i = 0; i < av.size(); i++)
			str += av.elementAt(i);
		return str;
	}

	/**
	 * Creates a deep copy of the given {@link MediaDescriptor}.
	 */
	public static MediaDescriptor copy(MediaDescriptor other) {
		MediaField m = new MediaField(other.m);
		ConnectionField c = (other.c != null) ? c = new ConnectionField(other.c) : null;
		Vector<AttributeField> av = new Vector<>();
		for (int i = 0; i < other.av.size(); i++) {
			av.addElement(new AttributeField(other.av.elementAt(i)));
		}
		return new MediaDescriptor(m, c, av.toArray(new AttributeField[] {}));
	}
	
}

