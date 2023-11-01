/*
 * Copyright (c) 2023 Bernhard Haumacher et al. All Rights Reserved.
 */
package org.mjsip.ua;

import org.kohsuke.args4j.Option;
import org.mjsip.config.YesNoHandler;
import org.mjsip.sip.address.NameAddress;
import org.mjsip.sip.config.NameAddressHandler;

/**
 * Options for SIP a command-line tool or user interface.
 */
public class UIConfig {

	/** Whether unregistering the contact address */
	@Option(name = "--do-unregister", usage = "Unregisters the contact address with the registrar server.", handler = YesNoHandler.class)
	public boolean doUnregister=false;
	
	/** Whether unregistering all contacts beafore registering the contact address */
	@Option(name = "--do-unregister-all", usage = "Unregisters all contact addresses.", handler = YesNoHandler.class)
	public boolean doUnregisterAll=false;

	/** Do not use system audio  */
	@Option(name = "--no-system-audio", usage = "Do not use system audio.", handler = YesNoHandler.class)
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
	@Option(name = "--accept-time", usage = "Automatically accept calls after the given time in seconds.")
	public int acceptTime=-1;        

	/** Automatic call transfer time in seconds; time&lt;0 corresponds to no auto transfer mode. */
	@Option(name = "--transfer-time", usage = "Automatic call transfer time in seconds; A value < 0 corresponds to no auto transfer mode.")
	public int transferTime=-1;
	
	/** Automatic re-inviting time in seconds; time&lt;0 corresponds to no auto re-invite mode.  */
	@Option(name = "--reinvite-time", usage = "Re-invites after the given time in seconds.")
	public int reinviteTime=-1;
	
	/** Automatic re-call time in seconds; time&lt;0 corresponds to no auto re-call mode.  */
	@Option(name = "--recall-time", usage = "Re-calls after given time in seconds.")
	public int recallTime=-1;
	
	/** Number of successive automatic re-calls; it is used only if call_to!=null, re_call_time&gt;0, and re_call_count&gt;0.  */
	@Option(name = "--recall-count", usage = "Number of successive automatic re-calls.")
	public int recallCount=-1;
	
	/** Automatic call a remote user secified by the 'call_to' value.
	 * Use value 'NONE' for manual calls (or let it undefined).  */
	@Option(name = "--call-to", usage = "Calls a remote user.",  handler = NameAddressHandler.class)
	public NameAddress callTo=null;
	
	/** Redirect incoming call to the secified URI.
	  * Use value 'NONE' for not redirecting incoming calls (or let it undefined). */
	@Option(name = "--redirect-to", usage = "Redirects the call to new user URI.", handler = NameAddressHandler.class)
	public NameAddress redirectTo=null;

	/** Transfer calls to the secified URI.
	  * Use value 'NONE' for not transferring calls (or let it undefined). */
	@Option(name = "--transfer-to", usage = "Transfer calls to the secified URI after the specified timeout.", handler = NameAddressHandler.class)
	public NameAddress transferTo=null;

}
