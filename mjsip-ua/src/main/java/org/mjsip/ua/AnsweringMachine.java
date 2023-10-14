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



import org.mjsip.media.MediaDesc;
import org.mjsip.sip.address.GenericURI;
import org.mjsip.sip.address.NameAddress;
import org.mjsip.sip.provider.SipConfig;
import org.mjsip.sip.provider.SipProvider;
import org.mjsip.sip.provider.SipStack;
import org.slf4j.LoggerFactory;
import org.zoolu.util.Flags;



/**
 * {@link AnsweringMachine} is a VOIP server that automatically accepts incoming calls, sends an audio file and records
 * input received from the remote end.
 */
public class AnsweringMachine extends MultipleUAS {
	
	private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(AnsweringMachine.class);

	/** Maximum life time (call duration) in seconds */
	public static int MAX_LIFE_TIME=600;

	/** Media file to play when answering the call. */
	public static String DEFAULT_ANNOUNCEMENT_FILE="./announcement-8000hz-mono-a-law.wav";

	/** First media port */
	int _firstMediaPort;

	/** Last media port */
	int _lastMediaPort;

	/** Creates an {@link AnsweringMachine}. */
	public AnsweringMachine(SipProvider sip_provider, UAConfig uaConfig, int portCnt) {
		super(sip_provider,uaConfig);
		
		_firstMediaPort = uaConfig.getMediaPort();
		_lastMediaPort = _firstMediaPort + portCnt - 1;
	} 


	/** From UserAgentListener. When a new call is incoming. */
	@Override
	public void onUaIncomingCall(UserAgent ua, NameAddress callee, NameAddress caller, MediaDesc[] media_descs) {
		GenericURI address = callee.getAddress();
		LOG.info("Incomming call from: " + address);

		int current_media_port = uaConfig.getMediaPort();
		if ((current_media_port += media_descs.length) > _lastMediaPort) {
			current_media_port = _firstMediaPort;
		}
		uaConfig.setMediaPort(current_media_port, 1);

		ua.accept();
	}
	

	/** The main method. */
	public static void main(String[] args) {
		String program = AnsweringMachine.class.getSimpleName();
		LOG.info(program + " " + SipStack.version);

		Flags flags=new Flags(program, args);
		int portCnt=flags.getInteger("--ports", "<cnt>", 20, "number of available media ports");
		Boolean prompt_exit = flags.getBoolean("--prompt", false, "Whether to wait for enter to exit program.");
		String config_file=flags.getString("-f","<file>", System.getProperty("user.home") + "/.mjsip-ua" ,"loads configuration from the given file");
		SipConfig sipConfig = SipConfig.init(config_file, flags);
		UAConfig uaConfig = UAConfig.init(config_file, flags);
		flags.close();
		
		uaConfig.audio = true;
		uaConfig.video = false;
		uaConfig.sendOnly = true;
		if (uaConfig.hangupTime <= 0)
			uaConfig.hangupTime = MAX_LIFE_TIME;
		new AnsweringMachine(new SipProvider(sipConfig), uaConfig, portCnt);

		// prompt before exit
		if (prompt_exit) 
		try {
			System.out.println("press 'enter' to exit");
			(new java.io.BufferedReader(new java.io.InputStreamReader(System.in))).readLine();
			System.exit(0);
		}
		catch (Exception e) {}
	}    

}
