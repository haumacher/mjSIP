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


import org.mjsip.sip.authentication.DigestAuthentication;
import org.mjsip.sip.header.AuthenticationInfoHeader;
import org.mjsip.sip.header.AuthorizationHeader;
import org.mjsip.sip.header.ProxyAuthenticateHeader;
import org.mjsip.sip.header.WwwAuthenticateHeader;
import org.mjsip.sip.message.SipMessage;
import org.mjsip.sip.message.SipMessageFactory;
import org.zoolu.util.ByteUtils;
import org.zoolu.util.LogLevel;
import org.zoolu.util.Logger;
import org.zoolu.util.MD5;


/** Class AuthenticationServerImpl implements an AuthenticationServer
  * for HTTP Digest authentication.
  */
public class AuthenticationServerImpl implements AuthenticationServer {
	

	/** Server authentication. */
	protected static final int SERVER_AUTHENTICATION=0;

	/** Proxy authentication. */
	protected static final int PROXY_AUTHENTICATION=1;


	/** Logger */
	protected Logger logger=null;

	/** The repository of users's authentication data. */
	protected AuthenticationService authentication_service;
	
	/** The authentication realm. */
	protected String realm;
	
	/** The authentication scheme. */
	protected String authentication_scheme="Digest";

	/** The authentication qop-options. */
	//protected String qop_options="auth,auth-int";
	protected String qop_options="auth";

	/** The current random value. */
	protected byte[] rand;

	/** DIGEST */
	//public static final String DIGEST="Digest";
	/** AKA */
	//public static final String AKA="AKA";
	/** CHAP */
	//public static final String CHAP="CHAP";


	/** Costructs a new AuthenticationServerImpl. */
	public AuthenticationServerImpl(String realm, AuthenticationService authentication_service, Logger logger) {
		init(realm,authentication_service,logger);
	}
 
	
	/** Inits the AuthenticationServerImpl. */
	private void init(String realm, AuthenticationService authentication_service, Logger logger) {
		this.logger=logger;
		this.realm=realm;
		this.authentication_service=authentication_service;
		this.rand=pickRandBytes();
	}

	/** Gets the realm. */
	/*public String getRealm() {
		return realm;
	}*/


	/** Gets the qop-options. */
	/*public String getQopOptions() {
		return qop_options;
	}*/


	/** Gets the current rand value. */
	/*public String getRand() {
		return HEX(rand);
	}*/


	/** Authenticates a SIP request.
	  * @param msg is the SIP request to be authenticated
	  * @return it returns the error SipMessage in case of authentication failure,
	  * or null in case of authentication success. */
	public SipMessage authenticateRequest(SipMessage msg) {
		return authenticateRequest(msg,SERVER_AUTHENTICATION);
	}


	/** Authenticates a proxying SIP request.
	  * @param msg is the SIP request to be authenticated
	  * @return it returns the error SipMessage in case of authentication failure,
	  * or null in case of authentication success. */
	public SipMessage authenticateProxyRequest(SipMessage msg) {
		return authenticateRequest(msg,PROXY_AUTHENTICATION);
	}


	/** Authenticates a SIP request.
	  * @param msg the SIP request to be authenticated
	  * @param type the type of authentication ({@link AuthenticationServerImpl#SERVER_AUTHENTICATION} server authentication, for ({@link AuthenticationServerImpl#PROXY_AUTHENTICATION} for proxy authentication)
	  * @return the error SipMessage in case of authentication failure, or null in case of authentication success. */
	protected SipMessage authenticateRequest(SipMessage msg, int type) {
		SipMessage err_resp=null;

		//String username=msg.getFromHeader().getNameAddress().getAddress().getUserName();
		//String user=username+"@"+realm;

		AuthorizationHeader ah;
		if (type==SERVER_AUTHENTICATION) ah=msg.getAuthorizationHeader();
		else ah=msg.getProxyAuthorizationHeader();
			
		if (ah!=null && ah.getNonceParam().equals(HEX(rand))) {
			
			//String username=ah.getUsernameParam();
			String realm=ah.getRealmParam();
			String nonce=ah.getNonceParam();
			String username=ah.getUsernameParam();
			String scheme=ah.getAuthScheme();
			
			String user=username+"@"+realm;
			
			if (authentication_service.hasUser(user)) {
				
				if (authentication_scheme.equalsIgnoreCase(scheme)) {
					
					DigestAuthentication auth=new DigestAuthentication(msg.getRequestLine().getMethod(),ah,msg.getBody(),keyToPasswd(authentication_service.getUserKey(user)));

					// check user's authentication response
					boolean is_authorized=auth.checkResponse();

					rand=pickRandBytes();        
						
					if (!is_authorized) {
						// authentication/authorization failed
						int result=403; // response code 403 ("Forbidden")
						err_resp=SipMessageFactory.createResponse(msg,result,null,null);
						log(LogLevel.INFO,"LOGIN ERROR: Authentication of '"+user+"' failed");
					}
					else {
						// authentication/authorization successed
						log(LogLevel.INFO,"Authentication of '"+user+"' successed");
					}
				}
				else {
					// authentication/authorization failed
					int result=400; // response code 400 ("Bad request")
					err_resp=SipMessageFactory.createResponse(msg,result,null,null);
					log(LogLevel.INFO,"Authentication method '"+scheme+"' not supported.");
				}
			}
			else {
				// no authentication credential found for this user
				int result=404; // response code 404 ("Not Found")
				err_resp=SipMessageFactory.createResponse(msg,result,null,null);  
			}
		}
		else {
			// no Authorization header found
			log(LogLevel.INFO,"No Authorization header found or nonce mismatching");
			int result;
			if (type==SERVER_AUTHENTICATION) result=401; // response code 401 ("Unauthorized")
			else result=407; // response code 407 ("Proxy Authentication Required")
			err_resp=SipMessageFactory.createResponse(msg,result,null,null);
			WwwAuthenticateHeader wah;
			if (type==SERVER_AUTHENTICATION) wah=new WwwAuthenticateHeader("Digest");
			else wah=new ProxyAuthenticateHeader("Digest");
			wah.addRealmParam(realm);
			wah.addQopOptionsParam(qop_options);
			wah.addNonceParam(HEX(rand));
			err_resp.setWwwAuthenticateHeader(wah); 
		}
		return err_resp;
	}


	/** Gets AuthenticationInfoHeader. */
	public AuthenticationInfoHeader getAuthenticationInfoHeader() {
		AuthenticationInfoHeader aih=new AuthenticationInfoHeader();
		aih.addRealmParam(realm);
		aih.addQopOptionsParam(qop_options);
		aih.addNextnonceParam(HEX(rand));
		return aih;
	}


	/** Picks a random array of 16 bytes. */
	private static byte[] pickRandBytes() {
		return MD5(Long.toHexString(org.zoolu.util.Random.nextLong()));
	}

	/** Converts the byte[] key in a String passwd. */
	private static String keyToPasswd(byte[] key) {
		return new String(key);
	}

	/** Calculates the MD5 of a String. */
	private static byte[] MD5(String str) {
		return MD5.digest(str);
	}

	/** Calculates the MD5 of an array of bytes. */
	private static byte[] MD5(byte[] bb) {
		return MD5.digest(bb);
	}

	/** Calculates the HEX of an array of bytes. */
	private static String HEX(byte[] bb) {
		return ByteUtils.asHex(bb);
	}

	// ****************************** Logs *****************************

	/** Adds a new string to the default Log. */
	protected void log(LogLevel level, String str) {
		if (logger!=null) logger.log(level,"AuthenticationServer: "+str);  
	}

}
