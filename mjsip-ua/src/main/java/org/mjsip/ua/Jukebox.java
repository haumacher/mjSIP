/*
 * Copyright (C) 2007 Luca Veltri - University of Parma - Italy
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

package org.mjsip.ua;



import java.io.File;

import org.mjsip.media.MediaDesc;
import org.mjsip.sip.address.NameAddress;
import org.mjsip.sip.provider.SipConfig;
import org.mjsip.sip.provider.SipProvider;
import org.mjsip.sip.provider.SipStack;
import org.zoolu.util.Flags;



/** Jukebox is a simple audio server.
  * It automatically responds to incoming calls and sends the audio file
  * as selected by the caller through the request-line parameter 'audiofile'.
  */
public class Jukebox extends MultipleUAS {
	

	/** URI resource parameter */
	public static String PARAM_RESOURCE="resource";
	
	/** Default available ports */
	public static int MEDIA_PORTS=20;

	/** Maximum life time (call duration) in seconds */
	public static int MAX_LIFE_TIME=600;

	/** Media file path */
	public static String MEDIA_PATH=".";

	/** First media port */
	int first_media_port;

	/** Last media port */
	int last_media_port;

	/** Creates a new Jukebox. */
	public Jukebox(SipProvider sip_provider, UAConfig uaConfig, int num_of_ports) {
		super(sip_provider,uaConfig);
		
		this.first_media_port=uaConfig.getMediaPort();
		this.last_media_port=first_media_port+num_of_ports-1;
	} 

	/** From UserAgentListener. When a new call is incoming. */
	@Override
	public void onUaIncomingCall(UserAgent ua, NameAddress callee, NameAddress caller, MediaDesc[] media_descs) {
		String audio_file=MEDIA_PATH+"/"+callee.getAddress().getParameter(PARAM_RESOURCE);
		if (audio_file!=null) if (new File(audio_file).isFile()) uaConfig.sendFile=audio_file;
		if (uaConfig.sendFile!=null) ua.accept(); else ua.hangup();
		int current_media_port=uaConfig.getMediaPort();
		if ((current_media_port+=media_descs.length)>last_media_port) current_media_port=first_media_port;
		uaConfig.setMediaPort(current_media_port,1);
	}
	

	/** The main method. */
	public static void main(String[] args) {
		System.out.println("Jukebox "+SipStack.version);

		int media_ports=MEDIA_PORTS;
		boolean prompt_exit=false;

		for (int i=0; i<args.length; i++) {
			if (args[i].equals("--mports")) {
				try {
					media_ports=Integer.parseInt(args[i+1]);
					args[i]="--skip";
					args[++i]="--skip";
				}
				catch (Exception e) {  e.printStackTrace();  }
			}
			else
			if (args[i].equals("--mpath")) {
				MEDIA_PATH=args[i+1];
				args[i]="--skip";
				args[++i]="--skip";
			}
			else
			if (args[i].equals("--prompt")) {
				prompt_exit=true;
				args[i]="--skip";
			}
		}
		Flags flags=new Flags("Jukebox", args);
		String config_file=flags.getString("-f","<file>", System.getProperty("user.home") + "/.mjsip-ua" ,"loads configuration from the given file");
		SipConfig sipConfig = SipConfig.init(config_file, flags);
		UAConfig uaConfig = UAConfig.init(config_file, flags);
		flags.close();

		uaConfig.audio=true;
		uaConfig.video=false;
		uaConfig.sendOnly=true;
		if (uaConfig.hangupTime<=0) uaConfig.hangupTime=MAX_LIFE_TIME;
		new Jukebox(new SipProvider(sipConfig),uaConfig,media_ports);
		
		// promt before exit
		if (prompt_exit) 
		try {
			System.out.println("press 'enter' to exit");
			(new java.io.BufferedReader(new java.io.InputStreamReader(System.in))).readLine();
			System.exit(0);
		}
		catch (Exception e) {}
	}    

}
