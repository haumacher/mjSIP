package org.mjsip.examples;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.mjsip.sip.provider.SipConfig;
import org.mjsip.sip.provider.SipProvider;
import org.mjsip.time.Scheduler;
import org.mjsip.time.SchedulerConfig;
import org.mjsip.ua.UAConfig;
import org.mjsip.ua.UIConfig;
import org.mjsip.ua.cli.MessageAgentCli;
import org.zoolu.util.Flags;

public class MA {

	/** The main method. */
	public static void main(String[] args) {
		Flags flags=new Flags(MA.class.getName(), args);
		String config_file=flags.getString("-f","<config_file>",null,"specifies a configuration file");
		String remote_user=flags.getString("-c","<call_to>",null,"calls a remote user");      
		boolean unregist=flags.getBoolean("-u","unregisters the contact address with the registrar server (the same as -g 0)");
		boolean unregist_all=flags.getBoolean("-z","unregisters ALL contact addresses");
		int regist_time=flags.getInteger("-g","<time>",-1,"registers the contact address with the registrar server for a gven duration, in seconds");
		SipConfig sipConfig = SipConfig.init(config_file, flags);
		UAConfig uaConfig=UAConfig.init(config_file, flags, sipConfig);         
		UIConfig uiConfig=UIConfig.init(config_file, flags);         
		SchedulerConfig schedulerConfig = SchedulerConfig.init(config_file);
		flags.close();
				
		
		if (regist_time>0) {
			uaConfig.doRegister=true;
			uaConfig.expires=regist_time;
		}
		if (unregist) uiConfig.doUnregister=true;
		if (unregist_all) uiConfig.doUnregisterAll=true;

		MessageAgentCli cli=new MessageAgentCli(new SipProvider(sipConfig, new Scheduler(schedulerConfig)),uaConfig);
		if (uiConfig.doUnregisterAll) {
			cli.unregisterall();
		} 
		if (uiConfig.doUnregister) {
			cli.unregister();
		} 
		if (uaConfig.doRegister) {
			cli.register(uaConfig.expires);
		} 
		
		// start sending messages
		System.out.println("type the messages to send or 'exit' to quit:");
		while (true) {
			try {
				BufferedReader in=new BufferedReader(new InputStreamReader(System.in));
				String subject=null;
				String message=in.readLine();
				if (message.equals("exit")) System.exit(0);
				// else
				if (remote_user==null) remote_user=cli.getRemoteUser();
				cli.send(remote_user,subject,message);
			}
			catch (Exception e) {  e.printStackTrace();  }
		} 
	} 

}
