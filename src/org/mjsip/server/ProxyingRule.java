package org.mjsip.server;


import org.mjsip.sip.address.GenericURI;
import org.mjsip.sip.address.SipURI;


/** ProxyingRule allows the matching of a SipURI with a proper next-hop SipURI. 
  */
public interface ProxyingRule {
	
	/** Gets the proper next-hop SipURI for the selected URI.
	  * It returns the SipURI used to reach the selected URI.
	  * @param uri the selected destination URI
	  * @return the proper next-hop SipURI for the selected URI
	  * if the proxying rule matches the URI, otherwise it returns null. */
	public SipURI getNexthop(GenericURI uri);  

	/** Gets the String value. */
	public String toString();  
}  
