/*
 * Copyright (C) 2013 Luca Veltri - University of Parma - Italy
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

package org.mjsip.sip.call;



import org.mjsip.sip.address.NameAddress;



/** SIP user.
  */
public class SipUser {
	
	/** User's address (AOR) */
	protected NameAddress user_naddr;

	/** User's contact address (contact URI) */
	protected NameAddress contact_naddr;
	
	/** User's name for authentication */
	protected String username;

	/** User's realm for authentication */
	protected String realm;

	/** User's passwd for authentication */
	protected String passwd;
	


	/** Creates a new SipUser.
	  * @param user_naddr user's address (AOR) */
	public SipUser(NameAddress user_naddr) {
		init(user_naddr,null,null,null,null);
	}


	/** Creates a new SipUser.
	  * @param user_naddr user's address (AOR)
	  * @param contact_naddr user's contact address (contact URI) */
	public SipUser(NameAddress user_naddr, NameAddress contact_naddr) {
		init(user_naddr,contact_naddr,null,null,null);
	}


	/** Creates a new SipUser.
	  * @param user_naddr user's address (AOR)
	  * @param username authentication user's name
	  * @param realm authentication realm
	  * @param passwd authentication passwd */
	public SipUser(NameAddress user_naddr, String username, String realm, String passwd) {
		init(user_naddr,null,username,realm,passwd);
	}


	/** Creates a new SipUser.
	  * @param user_naddr user's address (AOR)
	  * @param contact_naddr user's contact address (contact URI)
	  * @param username authentication user's name
	  * @param realm authentication realm
	  * @param passwd authentication passwd */
	public SipUser(NameAddress user_naddr, NameAddress contact_naddr, String username, String realm, String passwd) {
		init(user_naddr,contact_naddr,username,realm,passwd);
	}


	/** Inits the SipUser.
	  * @param user_naddr user's address (AOR)
	  * @param contact_naddr user's contact address (contact URI)
	  * @param username authentication user's name
	  * @param realm authentication realm
	  * @param passwd authentication passwd */
	private void init(NameAddress user_naddr, NameAddress contact_naddr, String username, String realm, String passwd) {
		this.user_naddr=user_naddr;
		this.contact_naddr=contact_naddr;
		this.username=username;
		this.realm=realm;
		this.passwd=passwd;
	}


	/** Gets the user's address (AOR).
	  * @return the address */
	public NameAddress getAddress() {
		return user_naddr;
	}


	/** Gets the user's contact address (contact URI).
	  * @return the contact address */
	public NameAddress getContactAddress() {
		return contact_naddr;
	}

  
	/** Gets the authentication user's name.
	  * @return the user's name */
	public String getAuhUserName() {
		return username;
	}


	/** Gets the authentication realm.
	  * @return the realm */
	public String getAuhRealm() {
		return realm;
	}


	/** Gets the authentication passwd.
	  * @return the passwd */
	public String getAuhPasswd() {
		return passwd;
	}
	
}
