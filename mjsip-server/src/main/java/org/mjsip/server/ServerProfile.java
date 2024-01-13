package org.mjsip.server;


import java.net.InetAddress;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.OptionDef;
import org.kohsuke.args4j.spi.OptionHandler;
import org.kohsuke.args4j.spi.Parameters;
import org.kohsuke.args4j.spi.Setter;
import org.mjsip.config.YesNoHandler;
import org.mjsip.sip.provider.SipConfig;
import org.zoolu.net.IpAddress;
import org.zoolu.net.SocketAddress;
import org.zoolu.util.Configure;
import org.zoolu.util.Parser;


/** 
 * Server configuration options.
 */
public class ServerProfile {
	
	@Option(name = "--proxy-transaction-timeout", usage = "Proxy transaction timeout (in milliseconds), that corresponds to Timer 'C' of RFC2361; RFC2361 suggests C > 3min = 180000ms.")
	public int proxyTransactionTimeout=180000;

	@Option(name = "--domain-names", usage = "The domain names that the server manages. "
			+ "Specify the domain names for which the location service maintains user bindings. "
			+ "Use 'auto-configuration' for automatic configuration of the domain name.", handler = DomainNamesHandler.class)
	public String[] domainNames= {SipConfig.AUTO_CONFIGURATION};
	
	@Option(name = "--domain-port-any", usage = "Whether all ports are considered valid local domain ports (regardless which SIP port is used).", handler = YesNoHandler.class)
	public boolean domainPortAny=false;

	@Option(name = "--is-registrar", usage = "Whether the server should act as registrar (i.e. respond to REGISTER requests).", handler = YesNoHandler.class)
	public boolean isRegistrar=true;
	
	@Option(name = "--expires", usage = "Maximum expires time (in seconds).")
	public int expires=3600;
	
	@Option(name = "--register-new-users", usage = "Whether the registrar can register new users (i.e. REGISTER requests from unregistered users).", handler = YesNoHandler.class)
	public boolean registerNewUsers=true;
	
	@Option(name = "--is-open-proxy", usage = "Whether the server relays requests for (or to) non-local users.", handler = YesNoHandler.class)
	public boolean isOpenProxy=false;
	
	@Option(name = "--location-service", usage = "The type of location service. "
			+ "Valid location service types are (local, ldap, radius, mysql) or a class name (e.g. local.server.LocationServiceImpl).")
	public String locationService="local";
	
	@Option(name = "--location-db", usage = "The file name of the location DB.")
	public String locationDb="users.db";
	
	@Option(name = "--clean-location-db", usage = "Whether the location DB is cleaned during startup.", handler = YesNoHandler.class)
	public boolean cleanLocationDb=false;

	@Option(name = "--do-authentication", usage = "Whether the server requires authentication from local users.", handler = YesNoHandler.class)
	public boolean doAuthentication=false;
	
	@Option(name = "--do-proxy-authentication", usage = "Whether the proxy requires authentication from users.", handler = YesNoHandler.class)
	public boolean doProxyAuthentication=false;
	
	@Option(name = "--authentication-scheme", usage = "The authentication scheme. "
			+ "Valid authentication scheme names are Digest, AKA, or a class name (e.g. local.server.AuthenticationServerImpl).")
	public String authenticationScheme="Digest";
	
	@Option(name = "--authentication-realm", usage = "The authentication realm. "
			+ "If not defined or equal to 'NONE' (default), the used via address is used instead.")
	public String authenticationRealm=null;
	
	@Option(name = "--authentication-service", usage = "The type of authentication service. "
			+ "Valid authentication service types are 'local', 'ldap', 'radius', 'mysql', or a class name (e.g. local.server.AuthenticationServiceImpl).")
	public String authenticationService="local";
	
	@Option(name = "--authentication-db", usage = "The fila name of the authentication DB.")
	public String authenticationDb="aaa.db";

	@Option(name = "--on-route", usage = "Whether the server should stay in the signaling path (uses Record-Route/Route).", handler = YesNoHandler.class)
	public boolean onRoute=false;
	
	@Option(name = "--loose-route", usage = "Whether implementing the RFC3261 Loose Route (or RFC2543 Strict Route) rule.", handler = YesNoHandler.class)
	public boolean looseRoute=true;
	
	@Option(name = "--loop-detection", usage = "Whether checking for loops before forwarding a request (Loop Detection). In RFC3261 it is optional.", handler = YesNoHandler.class)
	public boolean loopDetection=true;

	/** Array of ProxyingRules based on pairs of username or phone prefix and corresponding nexthop address.
	  * It provides static rules for proxying number-based SIP-URI the server is responsible for.
	  * Use "default" (or "*") as default prefix.
	  * Example: <br>
	  * server is responsible for the domain 'example.com' <br>
	  * phone_proxying_rules={prefix=0123,nexthop=127.0.0.2:7002} {prefix=*,nexthop=127.0.0.3:7003} <br>
	  * a message with recipient 'sip:01234567@example.com' is forwarded to 'sip:01234567@127.0.0.2:7002'
	  */
	@Option(name = "--authenticated-phone-proxying-rules", handler = ProxyRuleHandler.class)
	public ProxyingRule[] authenticatedPhoneProxyingRules=null;

	@Option(name = "--phone-proxying-rules", handler = ProxyRuleHandler.class)
	public ProxyingRule[] phoneProxyingRules=null;

	/** Array of ProxyingRules based on pairs of destination domain and corresponding nexthop address.
	  * It provides static rules for proxying domain-based SIP-URI the server is NOT responsible for.
	  * It make the server acting (also) as 'Interrogating' Proxy, i.e. I-CSCF in the 3G networks.
	  * Example: <br>
	  * server is responsible for the domain 'example.com' <br>
	  * domain_proxying_rules={domain=domain1.foo,nexthop=proxy.example.net:5060} <br>
	  * a message with recipient 'sip:01234567@domain1.foo' is forwarded to 'sip:01234567@proxy.example.net:5060'
	  */
	@Option(name = "--authenticated-doman-proxying-rules", handler = DomainRuleHandler.class)
	public ProxyingRule[] authenticatedDomainProxyingRules=null;
	
	@Option(name = "--doman-proxying-rules", handler = DomainRuleHandler.class)
	public ProxyingRule[] domainProxyingRules=null;

	@Option(name = "--memory-log", usage = "Whether maintaining a memory log.")
	public boolean memoryLog=false;

	public void normalize() {
		if (authenticationRealm!=null && authenticationRealm.equals(Configure.NONE)) authenticationRealm=null;
		if (domainNames==null) domainNames=new String[0];
		
		for (int n = 0, cnt = domainNames.length; n < cnt; n++) {
			if (domainNames[n].equalsIgnoreCase(SipConfig.AUTO_CONFIGURATION)) {
				InetAddress host_addr=IpAddress.getLocalHostAddress();
				domainNames[n] = host_addr.getHostAddress();
			}
		}
		
		if (authenticatedPhoneProxyingRules==null) authenticatedPhoneProxyingRules=new ProxyingRule[0];
		if (phoneProxyingRules==null) phoneProxyingRules=new ProxyingRule[0];
		if (authenticatedDomainProxyingRules==null) authenticatedDomainProxyingRules=new ProxyingRule[0];
		if (domainProxyingRules==null) domainProxyingRules=new ProxyingRule[0];
	}
	
	public static class DomainNamesHandler extends OptionHandler<String> {
		public DomainNamesHandler(CmdLineParser parser, OptionDef option, Setter<? super String> setter) {
			super(parser, option, setter);
		}

		@Override
		public int parseArguments(Parameters params) throws CmdLineException {
			String value = params.getParameter(0);
			Parser par = new Parser(value);
			char[] delim={' ',','};
			
			do {
				String domain=par.getWord(delim);
				setter.addValue(domain);
			} while (par.hasMore());
			
			return 1;
		}

		@Override
		public String getDefaultMetaVariable() {
			return "<domain1>,<domain2>,...";
		}
	}
	
	public static class ProxyRuleHandler extends OptionHandler<ProxyingRule> {
		public ProxyRuleHandler(CmdLineParser parser, OptionDef option, Setter<? super ProxyingRule> setter) {
			super(parser, option, setter);
		}
		
		@Override
		public int parseArguments(Parameters params) throws CmdLineException {
			Parser par = new Parser(params.getParameter(0));
			char[] delim={' ',',',';','}'};
			par.goTo('{');
			while (par.hasMore()) {
				par.goTo("prefix").skipN(6).goTo('=').skipChar();
				String prefix=par.getWord(delim);
				if (prefix.equals("*")) prefix=PrefixProxyingRule.DEFAULT_PREFIX;
				par.goTo("nexthop").skipN(7).goTo('=').skipChar();
				String nexthop=par.getWord(delim);
				setter.addValue(createRule(prefix, nexthop));
				par.goTo('{');
			}
			return 1;
		}

		protected ProxyingRule createRule(String prefix, String nexthop) {
			return new PrefixProxyingRule(prefix,new SocketAddress(nexthop));
		}
		
		@Override
		public String getDefaultMetaVariable() {
			return "<rule>";
		}
	}

	public static class DomainRuleHandler extends ProxyRuleHandler {
		public DomainRuleHandler(CmdLineParser parser, OptionDef option, Setter<? super ProxyingRule> setter) {
			super(parser, option, setter);
		}
		
		@Override
		protected ProxyingRule createRule(String prefix, String nexthop) {
			return new DomainProxyingRule(prefix,new SocketAddress(nexthop));
		}
	}
	
}
