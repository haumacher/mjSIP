/*
 * Copyright (c) 2023 Bernhard Haumacher et al. All Rights Reserved.
 */
package org.mjsip.ua;

import org.kohsuke.args4j.Option;
import org.mjsip.config.YesNoHandler;
import org.mjsip.sip.address.NameAddress;
import org.mjsip.sip.config.NameAddressHandler;
import org.zoolu.util.Configure;
import org.zoolu.util.Flags;
import org.zoolu.util.Parser;

/**
 * Options for SIP a command-line tool or user interface.
 */
public class UIConfig extends Configure {

	/** Whether unregistering the contact address */
	@Option(name = "--do-unregister", handler = YesNoHandler.class)
	public boolean doUnregister=false;
	
	/** Whether unregistering all contacts beafore registering the contact address */
	@Option(name = "--do-unregister-all", handler = YesNoHandler.class)
	public boolean doUnregisterAll=false;

	/** Do not use system audio  */
	@Option(name = "--no-system-audio", handler = YesNoHandler.class)
	public boolean noSystemAudio=false;

	/** Absolute path (or complete URI) of the buddy list file where the buddy list is and loaded from (and saved to).
	  * By default, the file "buddy.lst" is used. */
	@Option(name = "--buddy-list-file")
	public String buddyListFile="buddy.lst";

	/** Relative path of UA media resources (gif, wav, etc.) within the UA jar file or within the resources folder. 
	  * By default, the folder "media/org/mjsip/ua" is used. */
	@Option(name = "--media-path")
	public String mediaPath="media/org/mjsip/ua";

	/** Automatic answer time in seconds; time&lt;0 corresponds to manual answer mode. */
	@Option(name = "--accept-time")
	public int acceptTime=-1;        

	/** Automatic call transfer time in seconds; time&lt;0 corresponds to no auto transfer mode. */
	@Option(name = "--transfer-time")
	public int transferTime=-1;
	
	/** Automatic re-inviting time in seconds; time&lt;0 corresponds to no auto re-invite mode.  */
	@Option(name = "--reinvite-time")
	public int reinviteTime=-1;
	
	/** Automatic re-call time in seconds; time&lt;0 corresponds to no auto re-call mode.  */
	@Option(name = "--recall-time")
	public int recallTime=-1;
	
	/** Number of successive automatic re-calls; it is used only if call_to!=null, re_call_time&gt;0, and re_call_count&gt;0.  */
	@Option(name = "--recall-count")
	public int recallCount=-1;
	
	/** Automatic call a remote user secified by the 'call_to' value.
	 * Use value 'NONE' for manual calls (or let it undefined).  */
	@Option(name = "--call-to", handler = NameAddressHandler.class)
	public NameAddress callTo=null;
	
	/** Redirect incoming call to the secified URI.
	  * Use value 'NONE' for not redirecting incoming calls (or let it undefined). */
	@Option(name = "--redirect-to", handler = NameAddressHandler.class)
	public NameAddress redirectTo=null;

	/** Transfer calls to the secified URI.
	  * Use value 'NONE' for not transferring calls (or let it undefined). */
	@Option(name = "--transfer-to", handler = NameAddressHandler.class)
	public NameAddress transferTo=null;

	/** 
	 * Constructs a {@link UAConfig} from the given configuration file and program arguments.
	 */
	public static UIConfig init(String file, Flags flags) {
		UIConfig result=new UIConfig();
		result.loadFile(file);
		result.updateWith(flags);
		return result;
	}

	@Override
	public void setOption(String attribute, Parser par) {
		if (attribute.equals("do_unregister"))  {  doUnregister=(par.getString().toLowerCase().startsWith("y"));  return;  }
		if (attribute.equals("do_unregister_all")) {  doUnregisterAll=(par.getString().toLowerCase().startsWith("y"));  return;  }
		if (attribute.equals("no_system_audio")){  noSystemAudio=(par.getString().toLowerCase().startsWith("y"));  return;  }
		
		if (attribute.equals("media_path"))     {  mediaPath=par.getStringUnquoted();  return;  }      
		if (attribute.equals("buddy_list_file")){  buddyListFile=par.getStringUnquoted();  return;  }
		
		if (attribute.equals("accept_time"))    {  acceptTime=par.getInt();  return;  }
		if (attribute.equals("transfer_time"))  {  transferTime=par.getInt();  return;  } 
		if (attribute.equals("re_invite_time")) {  reinviteTime=par.getInt();  return;  } 
		if (attribute.equals("re_call_time"))   {  recallTime=par.getInt();  return;  } 
		if (attribute.equals("re_call_count"))  {  recallCount=par.getInt();  return;  } 

		if (attribute.equals("call_to")) {
			String naddr=par.getRemainingString().trim();
			if (naddr==null || naddr.length()==0 || naddr.equalsIgnoreCase(Configure.NONE)) callTo=null;
			else callTo=NameAddress.parse(naddr);
			return;
		}
		if (attribute.equals("redirect_to")) {
			String naddr=par.getRemainingString().trim();
			if (naddr==null || naddr.length()==0 || naddr.equalsIgnoreCase(Configure.NONE)) redirectTo=null;
			else redirectTo=NameAddress.parse(naddr);
			return;
		}
		if (attribute.equals("transfer_to")) {
			String naddr=par.getRemainingString().trim();
			if (naddr==null || naddr.length()==0 || naddr.equalsIgnoreCase(Configure.NONE)) transferTo=null;
			else transferTo=NameAddress.parse(naddr);
			return;
		}
	}

	/**
	 * Adds settings read from command line arguments.
	 */
	protected void updateWith(Flags flags) {
		Boolean unregist=flags.getBoolean("-u",null,"unregisters the contact address with the registrar server (the same as -g 0)");
		if (unregist!=null) this.doUnregister=unregist.booleanValue();
		
		Boolean unregist_all=flags.getBoolean("-z",null,"unregisters ALL contact addresses");
		if (unregist_all!=null) this.doUnregisterAll=unregist_all.booleanValue();

		Boolean no_system_audio=flags.getBoolean("--no-audio",null,"do not use system audio");
		if (no_system_audio!=null) this.noSystemAudio=no_system_audio.booleanValue();

		String call_to=flags.getString("-c","<call_to>",null,"calls a remote user");      
		if (call_to!=null) this.callTo=NameAddress.parse(call_to);
		
		String redirect_to=flags.getString("-r","<uri>",null,"redirects the call to new user <uri>");
		if (redirect_to!=null) this.redirectTo=NameAddress.parse(redirect_to);
		
		String[] transfer=flags.getStringTuple("-q",2,"<uri> <secs>",null,"transfers the call to <uri> after <secs> seconds");
		
		String transfer_to=transfer!=null? transfer[0] : null; 
		if (transfer_to!=null) this.transferTo=NameAddress.parse(transfer_to);
		
		int transfer_time=transfer!=null? Integer.parseInt(transfer[1]) : -1;
		if (transfer_time>0) this.transferTime=transfer_time;
		
		int accept_time=flags.getInteger("-y","<secs>",-1,"auto answers after given seconds");      
		if (accept_time>=0) this.acceptTime=accept_time;
		
		int re_invite_time=flags.getInteger("-i","<secs>",-1,"re-invites after given seconds");
		if (re_invite_time>0) this.reinviteTime=re_invite_time;
		
		int re_call_time=flags.getInteger("--re-call-time","<time>",-1,"re-calls after given seconds");
		if (re_call_time>0) this.recallTime=re_call_time;
		
		int re_call_count=flags.getInteger("--re-call-count","<n>",-1,"number of successive automatic re-calls");
		if (re_call_count>0) this.recallCount=re_call_count;
	}		

}
