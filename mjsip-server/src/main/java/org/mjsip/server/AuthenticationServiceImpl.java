package org.mjsip.server;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;

import org.slf4j.LoggerFactory;
import org.zoolu.util.Base64;
import org.zoolu.util.Parser;


/** AuthenticationServiceImpl is a simple implementation of a AuthenticationService.
  * AuthenticationServiceImpl allows creation and maintainance of a
  * AAA service for registered users.
  */
public class AuthenticationServiceImpl implements AuthenticationService {
	
	private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(AuthenticationServiceImpl.class);

	/** AuthenticationService name */
	String filename=null;
	
	/** Whether the AuthenticationService DB has been changed without saving */
	boolean changed=false;
	
	/** Users AAA DB. Set of pairs of { (String)user , (UserAuthInfo)binding }. */
	Hashtable<String, UserAuthInfo> users;

	/** Void byte array. */
	private static final byte[] NULL_ARRAY=new byte[0];


	/** Creates a new AuthenticationService. */
	public AuthenticationServiceImpl(String file_name) {
		filename=file_name;
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
		addUser(user,NULL_ARRAY);
		return this;
	}

	/** Removes the user from the database.
	  * @param user the user name
	  * @return this object */
	@Override
	public Repository removeUser(String user) {
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


	// **************** Methods of interface AuthenticationService ****************

	/** Adds a new user at the database.
	  * @param user the user name
	  * @param key the user key
	  * @return this object */
	@Override
	public AuthenticationService addUser(String user, byte[] key) {
		if (hasUser(user)) return this;
		UserAuthInfo ur=new UserAuthInfo(user,key);
		users.put(user,ur);
		changed=true;
		return this;
	}

	/** Sets the user key */
	@Override
	public AuthenticationService setUserKey(String user, byte[] key) {
		UserAuthInfo ur=getUserAuthInfo(user);
		if (ur!=null) {
			ur.setKey(key);
			changed=true;
		}
		return this;
	}   
	/** Gets the user key */
	@Override
	public byte[] getUserKey(String user) {
		if (hasUser(user)) return getUserAuthInfo(user).getKey();
		else return null;
	}


	// ******************************* New methods *******************************

	/** Returns the name of the database. */
	public String getName() { return filename; }

	/** Whether the database is changed. */
	public boolean isChanged() { return changed; }

	/** Adds a user record in the database */
	private void addUserAuthInfo(UserAuthInfo ur) {
		if (hasUser(ur.getName())) removeUser(ur.getName());
		users.put(ur.getName(),ur);
	}
	
	/** Gets the record of the user */
	private UserAuthInfo getUserAuthInfo(String user) {
		return users.get(user);
	}
	
	/** Returns an enumeration of the values in this database */
	private Enumeration<UserAuthInfo> getUserAuthInfos() {
		return users.elements();
	}
	
	/** Loads the database */
	public void load() {
		changed = false;
		try (BufferedReader in = new BufferedReader(new FileReader(filename))){
			String user = null;
			byte[] key = NULL_ARRAY;
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

				Parser par = new Parser(line);

				if (line.startsWith("#"))
					continue;
				if (line.startsWith("user")) {
					if (user != null) addUser(user, key);
					user = par.goTo('=').skipChar().getString();
					key = NULL_ARRAY;
					continue;
				}
				if (line.startsWith("key")) {
					key = Base64.decode(par.goTo('=').skipChar().getString());
					continue;
				}
				if (line.startsWith("passwd")) {
					key = par.goTo('=').skipChar().getString().getBytes();
					continue;
				}
			}
			if (user != null) addUser(user, key);
		} catch (FileNotFoundException e) {
			LOG.warn("file \"" + filename + "\" not found: created new empty DB");
        } catch (IOException e) {
			e.printStackTrace();
		}
	}

	/** Saves the database */
	public synchronized void save() {
		changed=false;
		try (BufferedWriter out= new BufferedWriter(new FileWriter(filename))){
			out.write(this.toString());
        }
		catch (IOException e) {
			LOG.warn("error trying to write on file \""+filename+"\"", e);
        }
	}

	/** Gets the String value of this Object.
	  * @return the String value */
	@Override
	public String toString() {
		String str="";
		for (Enumeration<UserAuthInfo> e=getUserAuthInfos(); e.hasMoreElements(); ) {
			UserAuthInfo ur= e.nextElement();
			str+=ur.toString();
			//str+="\r\n";
		}
		return str;
	}

}


/** User's authentication info.
  * This class represents a user record of the AAA DB.
  */
class UserAuthInfo {
	
	/** User name */
	String name;
	String getName() {  return name;  }   
	void setName(String name) {  this.name=name;  }

	/** User key */
	byte[] key;
	byte[] getKey() {  return key;  }  
	void setKey(byte[] key) {  this.key=key;  }


	/** Gets the String value of this Object.
	  * @return the String value */
	@Override
	public String toString() {
		String str="";
		str+="user= "+name+"\r\n";
		str+="key= "+Base64.encode(key)+"\r\n";
		return str;
	}

	/** Costructs a new UserAuthInfo for user <i>name</i>
	  * @param name the user name */
	UserAuthInfo(String name, byte[] key) {
		this.name=name;
		this.key=key;
	} 
}

