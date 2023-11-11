package org.mjsip.examples;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.kohsuke.args4j.Option;
import org.mjsip.config.OptionParser;
import org.mjsip.sip.provider.SipConfig;
import org.mjsip.sip.provider.SipProvider;
import org.mjsip.time.ConfiguredScheduler;
import org.mjsip.time.SchedulerConfig;
import org.mjsip.ua.UAConfig;
import org.mjsip.ua.UIConfig;
import org.mjsip.ua.registration.RegistrationClient;
import org.mjsip.ua.registration.RegistrationLogger;

public class MA {
	
	public static class Config {
		@Option(name = "-c", aliases = "--call", usage = "URI of the remote user to call.")
		String remoteUser;
	}

	/** The main method. */
	public static void main(String[] args) {
		SipConfig sipConfig = new SipConfig();
		UAConfig uaConfig = new UAConfig();
		SchedulerConfig schedulerConfig = new SchedulerConfig();
		UIConfig uiConfig = new UIConfig();
		
		Config config = new Config();

		OptionParser.parseOptions(args, ".mjsip-ua", sipConfig, uaConfig, schedulerConfig, uiConfig, config);
		
		sipConfig.normalize();
		uaConfig.normalize(sipConfig);
		
		SipProvider sip_provider = new SipProvider(sipConfig, new ConfiguredScheduler(schedulerConfig));
		MessageAgentCli cli=new MessageAgentCli(sip_provider,uaConfig);
		
		if (uaConfig.isRegister()) {
			RegistrationClient rc = new RegistrationClient(sip_provider, uaConfig, new RegistrationLogger());
			
			if (uiConfig.doUnregisterAll) {
				rc.unregisterall();
			}
			
			if (uiConfig.doUnregister) {
				rc.unregister();
			}
			
			rc.register(uaConfig.getExpires());
		}
		
		String remoteUser = config.remoteUser;
		
		// start sending messages
		System.out.println("type the messages to send or 'exit' to quit:");
		while (true) {
			try {
				BufferedReader in=new BufferedReader(new InputStreamReader(System.in));
				String subject=null;
				String message=in.readLine();
				if (message.equals("exit")) System.exit(0);
				// else
				if (remoteUser==null) remoteUser=cli.getRemoteUser();
				cli.send(remoteUser,subject,message);
			}
			catch (Exception e) {  e.printStackTrace();  }
		} 
	} 

}
