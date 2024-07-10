/*
 * Copyright (C) 2005 Luca Veltri - University of Parma - Italy
 * 
 * This source code is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This source code is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this source code; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 * 
 * Author(s):
 * Luca Veltri (luca.veltri@unipr.it)
 */

package org.mjsip.server;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;

import org.mjsip.sip.address.NameAddress;
import org.mjsip.sip.header.ContactHeader;
import org.mjsip.sip.header.SipHeaders;
import org.mjsip.sip.provider.SipParser;
import org.slf4j.LoggerFactory;
import org.zoolu.util.Parser;



/** LocationServiceImpl is a simple implementation of a LocationService.
  * LocationServiceImpl allows creation and maintainance of a
  * location service for registered users.
  */
public class LocationServiceImpl implements LocationService {
	
	private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(LocationServiceImpl.class);

	/** Maximum expiration date (equivalent to NEVER).
	  * <p>
	  * Note: time 3116354400000 is 2/10/2068, that is when I will be 100 years old.. good luck! ;) */
	static final long NEVER=(long)31163544*100000;


	/** LocationService name. */
	String file_name=null;
	
	/** Whether the Location DB has been changed without saving. */
	boolean changed=false;
	
	/** Users bindings. Set of pairs of { (String)user , (UserBindingInfo)binding }. */
	Hashtable<String, UserBindingInfo> users;
	

	
	/** Creates a new LocationServiceImpl */
	public LocationServiceImpl(String file_name) {
		this.file_name=file_name;
		if (file_name == null)
			LOG.warn("no file has been provided for location DB: only temporary memory (RAM) will be used.");
		users=new Hashtable<>();
		load();
	}


	// **************** Methods of interface Registry ****************

	/** Syncronizes the database.
	  * <p> Can be used, for example, to save the current memory image of the DB. */
	@Override
	public void sync() {
		if (changed) save();
	}

	/** Returns the numbers of users in the database.
	  * @return the numbers of user entries */
	@Override
	public int size() {
		return users.size();
	}
	
	/** Returns an enumeration of the users in this database.
	  * @return the list of user names as an Enumeration of String */
	@Override
	public Enumeration<String> getUsers() {
		return users.keys();
	}
		
	/** Whether a user is present in the database and can be used as key.
	  * @param user the user name
	  * @return true if the user name is present as key */
	@Override
	public boolean hasUser(String user) {
		return (users.containsKey(user));
	}
	
	/** Adds a new user at the database.
	  * @param user the user name
	  * @return this object */
	@Override
	public Repository addUser(String user) {
		if (hasUser(user)) return this;
		UserBindingInfo ur=new UserBindingInfo(user);
		users.put(user,ur);
		changed=true;
		return this;
	}
		
	/** Removes the user from the database.
	  * @param user the user name
	  * @return this object */
	@Override
	public Repository removeUser(String user) {
		if (!hasUser(user)) return this;
		//else
		users.remove(user);
		changed=true;
		return this;
	}
  
	/** Removes all users from the database.
	  * @return this object */
	@Override
	public Repository removeAllUsers() {
		users.clear();
		changed=true;
		return this;
	}

	/** Gets the String value of this Object.
	  * @return the String value */
	@Override
	public String toString() {
		StringBuilder str= new StringBuilder();
		for (Enumeration<UserBindingInfo> i=getUserBindings(); i.hasMoreElements(); ) {
			UserBindingInfo u= i.nextElement();
			str.append(u.toString());
		}
		return str.toString();
	}


	// **************** Methods of interface LocationService ****************

	/** Whether the user has contact <i>uri</i>.
	  * @param user the user name
	  * @param uri the contact URI
	  * @return true if is the contact present */
	@Override
	public boolean hasUserContact(String user, String uri) {
		if (!hasUser(user)) return false;
		//else
		return getUserBindingInfo(user).hasContact(uri);
	}

	/** Adds a contact.
	  * @param user the user name
	  * @param name_addresss the contact NameAddress
	  * @param expire the contact expire Date
	  * @return this object */
	@Override
	public LocationService addUserContact(String user, NameAddress name_addresss, Date expire) {
		if (!hasUser(user)) addUser(user);
		UserBindingInfo ur=getUserBindingInfo(user);
		ur.addContact(name_addresss,expire);
		changed=true;
		return this;
	}

	/** Removes a contact.
	  * @param user the user name
	  * @param uri the contact URI
	  * @return this object */
	@Override
	public LocationService removeUserContact(String user, String uri) {
		if (!hasUser(user)) return this;
		//else
		UserBindingInfo ur=getUserBindingInfo(user);
		ur.removeContact(uri);
		changed=true;
		return this;
	}   
	
	/** Gets the user contacts that are not expired.
	  * @param user the user name
	  * @return the list of contact URIs as Enumeration of String */
	@Override
	public Enumeration<String> getUserContactURIs(String user) {
		if (!hasUser(user)) return null;
		//else
		changed=true;
		return getUserBindingInfo(user).getContacts();
	}

	/** Gets NameAddress value of the user contact.
	  * @param user the user name
	  * @param uri the contact URI
	  * @return the contact NameAddress */
	@Override
	public NameAddress getUserContactNameAddress(String user, String uri) {
		if (!hasUser(user)) return null;
		//else
		return getUserBindingInfo(user).getNameAddress(uri);
	}

	/** Gets expiration date of the user contact.
	  * @param user the user name
	  * @param uri the contact URI
	  * @return the contact expire Date */
	@Override
	public Date getUserContactExpirationDate(String user, String uri) {
		if (!hasUser(user)) return null;
		//else
		return getUserBindingInfo(user).getExpirationDate(uri);
	}
	
	/** Whether the contact is expired.
	  * @param user the user name
	  * @param uri the contact URI
	  * @return true if it has expired */
	@Override
	public boolean isUserContactExpired(String user, String uri) {
		if (!hasUser(user)) return true;
		//else
		return getUserBindingInfo(user).isExpired(uri);
	}
	
	/** Removes all contacts from the database.
	  * @return this object */
	/*public LocationService removeAllContacts() {
		for (Enumeration i=getUserBindings(); i.hasMoreElements(); ) {
			((UserBindingInfo)i.nextElement()).removeContacts();
		}
		changed=true;
		return this;
	}*/

	/** Adds a 'static' contact that never expires.
	  * A static contact is a sort of 'alias' for the user's AOR.
	  * @param user the user name
	  * @param name_addresss the contact NameAddress
	  * @return this object */
	@Override
	public LocationService addUserStaticContact(String user, NameAddress name_addresss) {
		return addUserContact(user,name_addresss,new Date(NEVER));
	}

	/** Whether the contact is 'static', that is it never expires.
	  * A static contact is a sort of 'alias' for the user's AOR.
	  * @param user the user name
	  * @param uri the contact URI
	  * @return true if it static */
	@Override
	public boolean isUserContactStatic(String user, String uri) {
		return getUserContactExpirationDate(user,uri).getTime()>=NEVER;
	}


	// ***************************** Private methods *****************************

	/** Returns the name of the database. */
	private String getName() { return file_name; }

	/** Whether the database is changed. */
	private boolean isChanged() { return changed; }


	/** Adds a user record in the database */
	private void addUserBindingInfo(UserBindingInfo ur) {
		if (hasUser(ur.getName())) removeUser(ur.getName());
		users.put(ur.getName(),ur);
	}
	
	/** Adds a user record in the database */
	private UserBindingInfo getUserBindingInfo(String user) {
		return users.get(user);
	}

	/** Returns an enumeration of the values in this database */
	private Enumeration<UserBindingInfo> getUserBindings() {
		return users.elements();
	}
	
	/** Loads the database */
	private void load() {
		if (file_name == null) return;
		// else
		changed = false;
		try (BufferedReader in = new BufferedReader(new FileReader(file_name))) {
			String user = null;
			while (true) {
				String line = null;
				try {
					line = in.readLine();
				} catch (Exception e) {
					e.printStackTrace();
					System.exit(0);
				}
				if (line == null)
					break;
				if (line.startsWith("#"))
					continue;
				if (line.startsWith("To")) {
					Parser par = new Parser(line);
					user = par.skipString().getString();
					addUser(user);
					continue;
				}
				if (line.startsWith(SipHeaders.Contact)) {
					SipParser par = new SipParser(line);
					NameAddress name_address = ((SipParser) par.skipString()).getNameAddress();
					String expire_value = par.goTo("expires=").skipN(8).getStringUnquoted();
					if (expire_value.equalsIgnoreCase("NEVER")) addUserStaticContact(user, name_address);
					else {
						Date expire_time = (new SipParser(expire_value)).getDate();
						addUserContact(user, name_address, expire_time);
					}
					Date date = getUserContactExpirationDate(user, name_address.getAddress().toString());
					continue;
				}
			}
		} catch (FileNotFoundException e) {
			LOG.warn("file \"" + file_name + "\" not found: created new empty DB");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
 
 
	/** Saves the database */
	private synchronized void save() {
		if (file_name==null) return;
		// else
		changed=false;
		try (BufferedWriter out = new BufferedWriter(new FileWriter(file_name))){
			out.write(this.toString());
        }
		catch (IOException e) {
			LOG.warn("error trying to write on file \""+file_name+"\"", e);
        }
	}
	
}


/** User's binding info.
  * This class represents a user record of the location DB.
  * <p> A UserBindingInfo contains the user name, and a set of
  * contact information (i.e. contact and expire-time).
  * <p> Method getContacts() returns an Enumeration of String values
  * rapresenting the various contact SipURIs.
  * Such values can be used as keys for getting for each contact
  * both the contact NameAddress and the expire Date. 
  */
class UserBindingInfo {
	
	/** User name */
	String name;
	
	/** Hashtable of ContactHeader with String as key. */
	Hashtable<String, ContactHeader> contact_list;


	/** Costructs a new UserBindingInfo for user <i>name</i>.
	  * @param name the user name */
	public UserBindingInfo(String name) {
		this.name=name;
		contact_list=new Hashtable<>();
	}
	
	/** Gets the user name.
	  * @return the user name */
	public String getName() {
		return name;
	}
  
	/** Gets the user contacts.
	  * @return the user contacts as an Enumeration of String */
	public Enumeration<String> getContacts() {
		return contact_list.keys();
	}

	/** Whether the user has any registered contact.
	  * @param uri the contact URI (String) 
	  * @return true if one or more contacts are present */
	public boolean hasContact(String uri) {
		return contact_list.containsKey(uri);
	}
	
	/** Adds a new contact.
	  * @param contact the contact address (NameAddress) 
	  * @param expire the expire value (Date) 
	  * @return this object */
	public UserBindingInfo addContact(NameAddress contact, Date expire) {
		String key=contact.getAddress().toString();
		if (!contact_list.containsKey(key)) contact_list.put(key,(new ContactHeader(contact)).setExpires(expire));
		return this;
	}
 
	/** Removes a contact.
	  * @param uri the contact URI (String) 
	  * @return this object */
	public UserBindingInfo removeContact(String uri) {
		if (contact_list.containsKey(uri)) contact_list.remove(uri);
		return this;
	}  
	
	/** Gets NameAddress of a contact.
	  * @param uri the contact URI (String) 
	  * @return the contact NameAddress, or null if the contact is not present */
	public NameAddress getNameAddress(String uri) {
		if (contact_list.containsKey(uri)) return (contact_list.get(uri)).getNameAddress();
		else return null;
	}

	/** Whether the contact is expired.
	  * @param uri the contact URI (String) 
	  * @return true if the contact is expired or contact does not exist */
	public boolean isExpired(String uri) {
		if (contact_list.containsKey(uri)) return (contact_list.get(uri)).isExpired();
		else return true;
	}
	
	/** Gets expiration date.
	  * @param uri the contact URI (String) 
	  * @return the expire Date */
	public Date getExpirationDate(String uri) {
		if (contact_list.containsKey(uri)) return (contact_list.get(uri)).getExpiresDate();
		else return null;
	}

	/** Removes all contacts.
	  * @return this object */
	/*public UserBindingInfo removeContacts() {
		contact_list.clear();
		return this;
	}*/

	/** Gets the String value of this Object.
	  * @return the String value */
	@Override
	public String toString() {
		StringBuilder str= new StringBuilder("To: "+name+"\r\n");
		for (Enumeration<String> i=getContacts(); i.hasMoreElements(); ) {
			ContactHeader ch= contact_list.get(i.nextElement());
			if (ch.getExpiresDate().getTime()>=LocationServiceImpl.NEVER) (ch=new ContactHeader(ch)).removeExpires().setParameter("expires","\"NEVER\"");
			str.append(ch);
		}
		return str.toString();
	}
}

