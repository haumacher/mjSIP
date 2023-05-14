package org.mjsip.ua;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.mjsip.sip.provider.SipProvider;
import org.mjsip.sip.provider.SipStack;
import org.mjsip.ua.cli.MessageAgentCli;
import org.zoolu.util.Flags;

public class MA {

	/** The main method. */
	public static void main(String[] args) {
		
		Flags flags=new Flags(args);
		boolean help=flags.getBoolean("-h","prints this message");
		String file=flags.getString("-f","<config_file>",null,"specifies a configuration file");
		String remote_user=flags.getString("-c","<call_to>",null,"calls a remote user");      
		boolean unregist=flags.getBoolean("-u","unregisters the contact address with the registrar server (the same as -g 0)");
		boolean unregist_all=flags.getBoolean("-z","unregisters ALL contact addresses");
		int regist_time=flags.getInteger("-g","<time>",-1,"registers the contact address with the registrar server for a gven duration, in seconds");
		String[] remaining_params=flags.getRemainingStrings(true,null,null);
		
		if (remaining_params.length>0) {
			System.out.println("unrecognized param '"+remaining_params[0]+"'\n");
		}
		if (remaining_params.length>0 || help) {
			System.out.println(flags.toUsageString(MA.class.getName()));
			return;
		}
				
		SipStack.init(file);
		UserAgentProfile user_profile=new UserAgentProfile(file);         
		
		if (regist_time>0) {
			user_profile.do_register=true;
			user_profile.expires=regist_time;
		}
		if (unregist) user_profile.do_unregister=true;
		if (unregist_all) user_profile.do_unregister_all=true;

		MessageAgentCli cli=new MessageAgentCli(new SipProvider(file),user_profile);
		if (user_profile.do_unregister_all) {
			cli.unregisterall();
		} 
		if (user_profile.do_unregister) {
			cli.unregister();
		} 
		if (user_profile.do_register) {
			cli.register(user_profile.expires);
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
