package local.server;


import org.zoolu.sip.address.SipURL;


/** ProxyingRule allows the matching of a SipURL with a proper next-hop SipURL. 
  */
public interface ProxyingRule
{
   /** Gets the proper next-hop SipURL for the selected URL.
     * It returns the SipURL used to reach the selected URL.
     * @param sip_url the selected destination URL
     * @return returns the proper next-hop SipURL for the selected URL
     * if the proxying rule matches the URL, otherwise it returns null. */
   public SipURL getNexthop(SipURL sip_url);  

   /** Gets the String value. */
   public String toString();  
}  