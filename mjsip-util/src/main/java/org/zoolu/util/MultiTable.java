/*
 * Copyright (C) 2008 Luca Veltri - University of Parma - Italy
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

package org.zoolu.util;



import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;



/** MultiTable (multiple-values table) is a one-to-many table.
  * A MultiTable maps a key to a set (actually Vector) of objects.
  */
public class MultiTable<K, V> {
	
	/** Hashtable that maps (String)key to (Vector)set. */
	Hashtable<K, Vector<V>> table;



	/** Creates a new MultiTable. */
	public MultiTable() {
		table = new Hashtable<>();
	}  

	/** Clears this hashtable so that it contains no keys. */
	public void clear() {
		table.clear();
	}
	
	/** Tests if some key maps into the specified value in this table. */
	public boolean contains(V value) {
		for (var keys = keys(); keys.hasMoreElements();) {
			if (get(keys.nextElement()).contains(value))
				return true;
		}
		return false;
	}
	
	/** Tests if the specified object is a key in this table. */
	public boolean containsKey(Object key) {
		return table.containsKey(key);
	}
	
	/** Returns an enumeration of the values in this table. */
	public Enumeration<V> elements() {
		Vector<V> elements = new Vector<>();
		for (Enumeration<K> keys = keys(); keys.hasMoreElements();) {
			//elements.addAll(((Vector)keys.nextElement()));
			for (Enumeration<V> e = get(keys.nextElement()).elements(); e.hasMoreElements();)
				elements.addElement(e.nextElement());
		}
		return elements.elements();
	}
	
	/** Returns the set of values to which the specified key is mapped in this table. */
	public Vector<V> get(Object key) {
		return table.get(key);
	}
	
	/** Tests if this hashtable maps no keys to values. */
	public boolean isEmpty() {
		return table.isEmpty();
	}
	
	/** Returns an enumeration of the keys in this table. */
	public Enumeration<K> keys() {
		return table.keys();
	}
	
	/** Maps the specified key to the specified value in this table. */
	public void put(K key, V value) {
		if (!table.containsKey(key))
			table.put(key, new Vector<>());
		Vector<V> set = table.get(key);
		set.addElement(value);
	}
	
	/** Removes the specified key (and their corresponding values) from this table. */
	public void remove(Object key) {
		table.remove(key);
	}
	
	/** Removes a given mapping <i>key--&gt;value</i> from this table. */
	public void remove(K key, V value) {
		Vector<V> set = table.get(key);
		if (set!=null) {
			set.removeElement(value);
			if (set.isEmpty()) table.remove(key);
		}
	}
	
	/** Returns the number of keys in this table. */
	public int size() {
		int size=0;
		for (Enumeration<K> keys = keys(); keys.hasMoreElements();) {
			size += get(keys.nextElement()).size();
		}
		return size;
	}
	
	/** Returns a rather long string representation of this table. */
	@Override
	public String toString() {
		return table.toString();
	}

}

