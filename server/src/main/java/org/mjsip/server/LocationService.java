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


import java.util.Date;
import java.util.Enumeration;

import org.mjsip.sip.address.NameAddress;


/** LocationService is the interface used by a SIP registrar to access to a
  * location repository.
  * <p> A LocationService allows the maintinance of bindings between users and contacts.
  * <br> For each user the LocationService should maintain information regarding:
  * <br> - username, that is a fully qualified name for this service (e.g. alice@example.net)
  * <br> - data, that is an opaque block of data (a string),
  *        that can be set and fetched for any service-depending use,
  * <br> - contacts/expires, that is the list of user contacts with the time when it expires,
  * <p> LocationService has a set of methods for query and modifing such data.
  * <p> Some of these methods include an optional parameter <i>app</i> that could be used 
  * to implement application-dependent mobility, i.e. lists of contacts that are specific
  * for particular applications. This feature might be used by guessing the application
  * by the SIP body (e.g. SDP) or by a new non-standard Application header (ref. [draft-XX.txt])  
  */
public interface LocationService extends Repository {
	
	/** Whether the user has contact <i>uri</i>.
	  * @param user the user name
	  * @param uri the contact URI
	  * @return true if is the contact present */
	public boolean hasUserContact(String user, String uri);

	/** Adds a contact.
	  * @param user the user name
	  * @param contact the contact NameAddress
	  * @param expire the contact expire Date
	  * @return this object */
	public LocationService addUserContact(String user, NameAddress contact, Date expire);

	/** Gets the user contacts that are not expired.
	  * @param user the user name
	  * @return the list of contact URIs as Enumeration of String */
	public Enumeration getUserContactURIs(String user);

	/** Removes a contact.
	  * @param user the user name
	  * @param uri the contact URI
	  * @return this object */
	public LocationService removeUserContact(String user, String uri);
	
	/** Gets NameAddress value of the user contact.
	  * @param user the user name
	  * @param uri the contact URI
	  * @return the contact NameAddress */
	public NameAddress getUserContactNameAddress(String user, String uri);

	/** Gets expiration date of the user contact.
	  * @param user the user name
	  * @param uri the contact URI
	  * @return the contact expire Date */
	public Date getUserContactExpirationDate(String user, String uri);
	
	/** Whether the contact is expired.
	  * @param user the user name
	  * @param uri the contact URI
	  * @return true if it has expired */
	public boolean isUserContactExpired(String user, String uri);
	
	/** Removes all contacts from the database.
	  * @return this object */
	//public LocationService removeAllContacts();

	/** Adds a 'static' contact that never expires.
	  * A static contact is a sort of 'alias' for the user's AOR.
	  * @param user the user name
	  * @param name_addresss the contact NameAddress
	  * @return this object */
	public LocationService addUserStaticContact(String user, NameAddress name_addresss);

	/** Whether the contact is 'static', that is it never expires.
	  * A static contact is a sort of 'alias' for the user's AOR.
	  * @param user the user name
	  * @param uri the contact URI
	  * @return true if it static */
	public boolean isUserContactStatic(String user, String uri);
	
}
