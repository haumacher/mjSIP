package org.mjsip.sip.authentication;


import org.mjsip.sip.header.AuthenticationHeader;
import org.mjsip.sip.header.AuthorizationHeader;
import org.mjsip.sip.header.ProxyAuthorizationHeader;
import org.zoolu.util.ByteUtils;
import org.zoolu.util.MD5;
import org.zoolu.util.Random;


/**
 * The HTTP Digest Authentication as defined in RFC2617. It can be used to i) calculate an
 * authentication response from an authentication request, or ii) validate an authentication
 * response.
 * 
 * <p>
 * in the former case the DigestAuthentication is created based on a WwwAuthenticationHeader (or
 * ProxyAuthenticationHeader), while in the latter case it is created based on an
 * AuthorizationHeader (or ProxyAuthorizationHeader).
 * </p>
 */
public class DigestAuthentication {
	
	protected String method;
	protected String username;
	protected String passwd;

	protected String realm;   
	protected String nonce; // e.g. base 64 encoding of time-stamp H(time-stamp ":" ETag ":" private-key)
	//protected String[] domain;
	protected String opaque;
	//protected boolean stale; // "true" | "false"
	protected String algorithm; // "MD5" | "MD5-sess" | token

	protected String qop; // "auth" | "auth-int" | token
	
	protected String uri;  
	protected String cnonce;
	protected String nc;
	protected String response;

	protected byte[] body;

	/** Constructs a new {@link DigestAuthentication}. */
	public DigestAuthentication(String method, AuthenticationHeader ah, byte[] body, String passwd) {
		this.method = method;
		this.username = ah.getUsernameParam();
		this.passwd = passwd;
		this.realm = ah.getRealmParam();
		this.opaque = ah.getOpaqueParam();
		this.nonce = ah.getNonceParam();
		this.algorithm = ah.getAlgorithParam();
		this.qop = ah.getQopParam();
		this.uri = ah.getUriParam();
		this.cnonce = ah.getCnonceParam();
		this.nc = ah.getNcParam();
		this.response = ah.getResponseParam();
		this.body = body;
	}

	/** Constructs a new {@link DigestAuthentication}. */
	public DigestAuthentication(String method, String uri, AuthenticationHeader ah, String qop, String cnonce, int nc,
			byte[] body, String username, String passwd) {
		this(method, ah, body, passwd);
		this.uri=uri;
		this.qop=qop;
		this.username=username;
		if (this.qop != null) {
			if (this.cnonce != null) {
				this.cnonce = cnonce;
			} else {
				this.cnonce = ByteUtils.asHex(Random.nextBytes(4));
			}
			if (nc>0) {
				this.nc=ByteUtils.asHex(ByteUtils.intToFourBytes(nc));
			} else {
				this.nc = "00000001";
			}
		}
	}

	/** Gets a String representation of the object. */
	@Override
	public String toString() {
		StringBuilder sb=new StringBuilder();
		sb.append("method=").append(method).append("\n");
		sb.append("username=").append(username).append("\n");
		sb.append("passwd=").append(passwd).append("\n");
		sb.append("realm=").append(realm).append("\n");
		sb.append("nonce=").append(nonce).append("\n");
		sb.append("opaque=").append(opaque).append("\n");
		sb.append("algorithm=").append(algorithm).append("\n");
		sb.append("qop=").append(qop).append("\n");
		sb.append("uri=").append(uri).append("\n");
		sb.append("cnonce=").append(cnonce).append("\n");
		sb.append("nc=").append(nc).append("\n");
		sb.append("response=").append(response).append("\n");
		sb.append("body=").append(new String(body)).append("\n");
		return sb.toString();
	}


	/** Whether the digest-response in the 'response' parameter in correct. */
	public boolean checkResponse() {
		if (response==null) return false;
		else return response.equals(getResponse());
	}


	/** Gets a new AuthorizationHeader based on current authentication attributes. */
	public AuthorizationHeader getAuthorizationHeader() {
		AuthorizationHeader ah=new AuthorizationHeader("Digest");
		ah.addUsernameParam(username);
		ah.addRealmParam(realm);
		ah.addNonceParam(nonce);
		ah.addUriParam(uri);
		if (algorithm!=null) ah.addAlgorithParam(algorithm);
		if (opaque!=null) ah.addOpaqueParam(opaque);
		if (qop!=null) ah.addQopParam(qop);
		if (cnonce!=null) ah.addCnonceParam(cnonce);
		if (nc!=null) ah.addNcParam(nc);
		String response=getResponse();
		ah.addResponseParam(response);
		return ah;
	}


	/** Gets a new ProxyAuthorizationHeader based on current authentication attributes. */
	public ProxyAuthorizationHeader getProxyAuthorizationHeader() {
		return new ProxyAuthorizationHeader(getAuthorizationHeader().getValue());
	}


	/** Calculates the digest-response.
	  * <p> If the "qop" value is "auth" or "auth-int":
	  * <br>   KD ( H(A1), unq(nonce) ":" nc ":" unq(cnonce) ":" unq(qop) ":" H(A2) )
	  *
	  * <p> If the "qop" directive is not present:
	  * <br>   KD ( H(A1), unq(nonce) ":" H(A2) )
	  */
	public String getResponse() {
		String secret=HEX(MD5(A1()));
		StringBuilder sb=new StringBuilder();
		if (nonce!=null) sb.append(nonce);
		sb.append(":");
		if (qop!=null) {
			if (nc!=null) sb.append(nc);
			sb.append(":");
			if (cnonce!=null) sb.append(cnonce);
			sb.append(":");
			sb.append(qop);
			sb.append(":");
		}
		sb.append(HEX(MD5(A2())));  
		String data=sb.toString();    
		return HEX(KD(secret,data));
	}

	
	/** Calculates KD() value.
	  * <p> KD(secret, data) = H(concat(secret, ":", data))
	  */
	private byte[] KD(String secret, String data) {
		StringBuilder sb=new StringBuilder();
		sb.append(secret).append(":").append(data);
		return MD5(sb.toString());
	}
		
	
	/** Calculates A1 value.
	  * <p> If the "algorithm" directive's value is "MD5" or is unspecified:
	  * <br>   A1 = unq(username) ":" unq(realm) ":" passwd
	  *
	  * <p> If the "algorithm" directive's value is "MD5-sess":
	  * <br>   A1 = H( unq(username) ":" unq(realm) ":" passwd ) ":" unq(nonce) ":" unq(cnonce)
	  */
	private byte[] A1() {
		StringBuilder sb=new StringBuilder();
		if (username!=null) sb.append(username);
		sb.append(":");
		if (realm!=null) sb.append(realm);
		sb.append(":");
		if (passwd!=null) sb.append(passwd); 
		
		if (algorithm==null || !algorithm.equalsIgnoreCase("MD5-sess")) {
			return sb.toString().getBytes();
		}
		else {
			StringBuilder sb2=new StringBuilder();
			sb2.append(":");
			if (nonce!=null) sb2.append(nonce); 
			sb2.append(":");
			if (cnonce!=null) sb2.append(cnonce); 
			return cat(MD5(sb.toString()),sb2.toString().getBytes()); 
		}
	}

  
	/** Calculates A2 value.
	  * <p> If the "qop" directive's value is "auth" or is unspecified:
	  * <br>   A2 = Method ":" digest-uri
	  *
	  * <p> If the "qop" value is "auth-int":
	  * <br>   A2 = Method ":" digest-uri ":" H(entity-body)
	  */
	private String A2() {
		StringBuilder sb=new StringBuilder();
		sb.append(method);
		sb.append(":");
		if (uri!=null) sb.append(uri);
		
		if (qop!=null && qop.equalsIgnoreCase("auth-int")) {
			sb.append(":");
			if (body==null) sb.append(HEX(MD5("")));
			else sb.append(HEX(MD5(body)));
		}
		return sb.toString();
	}


	/** Concatenates two arrays of bytes. */
	private static byte[] cat(byte[] a, byte[] b) {
		int len=a.length+b.length;
		byte[ ] c=new byte[len];
        System.arraycopy(a, 0, c, 0, a.length);
        System.arraycopy(b, 0, c, a.length, b.length);
		return c;
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

}
 
