/*
 * Copyright (C) 2005 Luca Veltri - University of Parma - Italy
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

package org.mjsip.phone.gui;


import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.ComboBoxEditor;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.plaf.basic.BasicComboBoxEditor;

import org.mjsip.config.OptionParser;
import org.mjsip.media.FlowSpec.Direction;
import org.mjsip.media.MediaDesc;
import org.mjsip.media.StreamerOptions;
import org.mjsip.media.rx.AudioReceiver;
import org.mjsip.media.rx.JavaxAudioOutput;
import org.mjsip.media.tx.AudioTransmitter;
import org.mjsip.media.tx.JavaxAudioInput;
import org.mjsip.pool.PortConfig;
import org.mjsip.pool.PortPool;
import org.mjsip.sip.address.NameAddress;
import org.mjsip.sip.address.SipURI;
import org.mjsip.sip.provider.SipConfig;
import org.mjsip.sip.provider.SipParser;
import org.mjsip.sip.provider.SipProvider;
import org.mjsip.sip.provider.SipStack;
import org.mjsip.time.ConfiguredScheduler;
import org.mjsip.time.SchedulerConfig;
import org.mjsip.ua.MediaAgent;
import org.mjsip.ua.MediaConfig;
import org.mjsip.ua.MediaOptions;
import org.mjsip.ua.RegisteringUserAgent;
import org.mjsip.ua.UAConfig;
import org.mjsip.ua.UAOptions;
import org.mjsip.ua.UIConfig;
import org.mjsip.ua.UserAgent;
import org.mjsip.ua.UserAgentListener;
import org.mjsip.ua.UserAgentListenerAdapter;
import org.mjsip.ua.clip.ClipPlayer;
import org.mjsip.ua.streamer.DefaultStreamerFactory;
import org.mjsip.ua.streamer.DispatchingStreamerFactory;
import org.mjsip.ua.streamer.NativeStreamerFactory;
import org.mjsip.ua.streamer.StreamerFactory;
import org.slf4j.LoggerFactory;
import org.zoolu.net.AddressType;
import org.zoolu.util.Archive;


/** Simple SIP user agent GUI. */
public class UserAgentGui extends JFrame implements UserAgentListenerAdapter {
	
	private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(UserAgentGui.class);

	/** This application */
	final String app_name="mjUA (http://www.mjsip.org)";

	/** SipProvider. */
	protected SipProvider sip_provider;

	/** User Agent */
	protected RegisteringUserAgent ua;

	/** UserAgentProfile */
	protected UAConfig _uaConfig;

	/** Title */
	//String user_name=app_name;

	/** Buddy list size */
	protected static final int NMAX_CONTACTS=10;

	/** Buddy list */
	protected StringList buddy_list;

	private static final int W_Width=320; // window width
	private static final int W_Height=90; // window height
	private static final int C_Height=30; // buttons and combobox height (total)

	/** Media file path */
	//final String MEDIA_PATH="media/media/org/mjsip/ua/";
	
	final String CALL_GIF=/*MEDIA_PATH+*/"call.gif";
	final String HANGUP_GIF=/*MEDIA_PATH+*/"hangup.gif";
	
	Icon icon_call;
	Icon icon_hangup;
	//Icon icon_call=new ImageIcon("media/media/org/mjsip/ua/call.gif");
	//Icon icon_hangup=new ImageIcon("media/media/org/mjsip/ua/hangup.gif");

	JPanel jPanel1 = new JPanel();
	JPanel jPanel2 = new JPanel();
	JPanel jPanel3 = new JPanel();
	JPanel jPanel4 = new JPanel();
	JComboBox<String> jComboBox1 = new JComboBox<>();
	BorderLayout borderLayout1 = new BorderLayout();
	BorderLayout borderLayout2 = new BorderLayout();
	JPanel jPanel5 = new JPanel();
	GridLayout gridLayout2 = new GridLayout();
	GridLayout gridLayout3 = new GridLayout();
	JButton jButton1 = new JButton();
	JButton jButton2 = new JButton();
	ComboBoxEditor comboBoxEditor1=new BasicComboBoxEditor();
	BorderLayout borderLayout3 = new BorderLayout();

	JTextField display=new JTextField();




	// ************************* UA internal state *************************
	  
	/** UA_IDLE=0 */
	protected static final String UA_IDLE="IDLE";
	/** UA_INCOMING_CALL=1 */
	protected static final String UA_INCOMING_CALL="INCOMING_CALL";
	/** UA_OUTGOING_CALL=2 */
	protected static final String UA_OUTGOING_CALL="OUTGOING_CALL";
	/** UA_ONCALL=3 */
	protected static final String UA_ONCALL="ONCALL";
	
	/** Call state: <P>UA_IDLE=0, <BR>UA_INCOMING_CALL=1, <BR>UA_OUTGOING_CALL=2, <BR>UA_ONCALL=3 */
	String call_state=UA_IDLE;

	private MediaOptions _mediaConfig;

	private UIConfig _uiConfig;

	private StreamerFactory _streamerFactory;
	

	/** Changes the call state */
	protected void changeStatus(String state) {
		call_state=state;
		LOG.debug("state: "+call_state); 
	}

	/** Checks the call state */
	protected boolean statusIs(String state) {
		return call_state.equals(state); 
	}

	/** Gets the call state */
	protected String getStatus() {
		return call_state; 
	}


	// *************************** Public methods **************************

	/** Creates a new UA. 
	 * @param portPool */
	public UserAgentGui(SipProvider sip_provider, PortPool portPool, UAConfig uaConfig, UIConfig uiConfig, MediaOptions mediaConfig) {
		this.sip_provider=sip_provider;
		_uaConfig=uaConfig;
		_uiConfig = uiConfig;
		_mediaConfig = mediaConfig;

		ua=new RegisteringUserAgent(sip_provider, portPool,_uaConfig, this.andThen(clipPlayer()));
		_streamerFactory = createStreamerFactory(_mediaConfig, _uaConfig);
		//ua.listen();
		changeStatus(UA_IDLE);

		initGraphics();                  
		run();   
	}

	/**
	 * Creates a {@link StreamerFactory} based on configuration options.
	 */
	public StreamerFactory createStreamerFactory(MediaOptions mediaConfig, UAOptions uaConfig) {
		DispatchingStreamerFactory factory = new DispatchingStreamerFactory();
		if (mediaConfig.isAudio()) {
			if (mediaConfig.isUseRat()) {
				factory.addFactory("audio", new NativeStreamerFactory(mediaConfig.getAudioMcastSoAddr(), mediaConfig.getBinRat()));
			} else {
				Direction dir = uaConfig.getDirection();

				AudioTransmitter tx;
				if (dir.doSend()) {
					tx = new JavaxAudioInput(true, mediaConfig.isJavaxSoundDirectConversion());
				} else {
					tx = null;
				}

				AudioReceiver rx;
				if (dir.doReceive()) {
					rx = new JavaxAudioOutput(mediaConfig.isJavaxSoundDirectConversion());
				} else {
					rx = null;
				}

				// standard javax-based audio streamer
				StreamerOptions options = StreamerOptions.builder()
						.setRandomEarlyDrop(mediaConfig.getRandomEarlyDropRate())
						.setSymmetricRtp(mediaConfig.isSymmetricRtp())
						.build();
				
				factory.addFactory("audio", new DefaultStreamerFactory(options, rx, tx));
			}
		}
		if (mediaConfig.isVideo()) {
			if (mediaConfig.isUseVic()) {
				factory.addFactory("video", new NativeStreamerFactory(mediaConfig.getVideoMcastSoAddr(), mediaConfig.getBinVic()));
			}
		}
		return factory;
	}
	
	private UserAgentListener clipPlayer() {
		if (!_mediaConfig.isUseRat() && !_uiConfig.noSystemAudio) {
			return new ClipPlayer(_uiConfig.mediaPath);
		}
		return null;
	}
	
	protected void initGraphics() {
		
		// load icons
		try {
			icon_call=getImageIcon(_uiConfig.mediaPath+"/"+CALL_GIF);
			icon_hangup=getImageIcon(_uiConfig.mediaPath+"/"+HANGUP_GIF);
		}
		catch (IOException e) {
			e.printStackTrace();
			LOG.info("Exception", e);
		}
		
		// load buddy list
		if (_uiConfig.buddyListFile!=null && (_uiConfig.buddyListFile.startsWith("http://") || _uiConfig.buddyListFile.startsWith("file:/"))) {
			try {
				buddy_list=new StringList(new URL(_uiConfig.buddyListFile));
			}
			catch (MalformedURLException e) {
				e.printStackTrace();
				LOG.info("Exception.", e);
				buddy_list=new StringList((File)null);
			}
		}
		else buddy_list=new StringList(new File(_uiConfig.buddyListFile));
		jComboBox1=new JComboBox<>(buddy_list.getElements());

		// init frame
		try {
			// set frame dimensions
			this.setSize(W_Width,W_Height);
			Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
			Dimension frameSize = this.getSize();
			if (frameSize.height > screenSize.height) frameSize.height = screenSize.height;
			if (frameSize.width > screenSize.width) frameSize.width = screenSize.width;
			this.setLocation((screenSize.width - frameSize.width)/2 - 40, (screenSize.height - frameSize.height)/2 - 40);
			this.setResizable(false);
	
			this.setTitle(sip_provider.getContactAddress(_uaConfig.getUser(), AddressType.DEFAULT).toString());
			this.addWindowListener(new java.awt.event.WindowAdapter() {
				@Override
				public void windowClosing(WindowEvent e) { exit(); }
			});
			jPanel1.setLayout(borderLayout3);
			jPanel2.setLayout(borderLayout2);
			display.setBackground(Color.black);
			display.setForeground(Color.green);
			display.setEditable(false);
			display.setText(app_name);
			jPanel4.setLayout(borderLayout1);
			jPanel5.setLayout(gridLayout2);
			jPanel3.setLayout(gridLayout3);
			gridLayout3.setRows(2);
			gridLayout3.setColumns(1);
			if (icon_call!=null && icon_call.getIconWidth()>0) jButton1.setIcon(icon_call);
			else jButton1.setText("Call");
			jButton1.addActionListener(new java.awt.event.ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) { jButton1_actionPerformed(); }
			});
			jButton1.addKeyListener(new java.awt.event.KeyAdapter() {
				@Override
				public void keyTyped(KeyEvent e) { jButton1_actionPerformed(); }
			});
			if (icon_hangup!=null && icon_hangup.getIconWidth()>0) jButton2.setIcon(icon_hangup);
			else jButton2.setText("Hungup");
			jButton2.addActionListener(new java.awt.event.ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) { jButton2_actionPerformed(); }
			});
			jButton2.addKeyListener(new java.awt.event.KeyAdapter() {
				@Override
				public void keyTyped(KeyEvent e) { jButton2_actionPerformed(); }
			});
			jComboBox1.addActionListener(new java.awt.event.ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) { jComboBox1_actionPerformed(e); }
			});
			comboBoxEditor1.addActionListener(new java.awt.event.ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) { comboBoxEditor1_actionPerformed(e); }
			});
			jButton2.setFont(new java.awt.Font("Dialog", 0, 10));
			jButton1.setFont(new java.awt.Font("Dialog", 0, 10));
			comboBoxEditor1.getEditorComponent().setBackground(Color.yellow);
			jComboBox1.setEditable(true);
			jComboBox1.setEditor(comboBoxEditor1);
			jComboBox1.setSelectedItem(null);
			jPanel3.setPreferredSize(new Dimension(0,C_Height));
			this.getContentPane().add(jPanel1, BorderLayout.CENTER);
			jPanel1.add(jPanel2, BorderLayout.CENTER);
			jPanel1.add(jPanel3, BorderLayout.SOUTH);
			jPanel2.add(display, BorderLayout.CENTER);
			jPanel3.add(jPanel4, null);
			jPanel3.add(jPanel5, null);
			jPanel4.add(jComboBox1, BorderLayout.CENTER);
			jPanel5.add(jButton1, null);
			jPanel5.add(jButton2, null);
	
			// show it
			this.setVisible(true);
			
			//Image image=Archive.getImage(Archive.getJarURL(jar_file,"media/org/mjsip/ua/intro.gif"));
			//PopupFrame about=new PopupFrame("About",image,this);
			//try  {  Thread.sleep(3000);  } catch(Exception e) {  }
			//about.closeWindow();
		}
		catch(Exception e) { e.printStackTrace(); }

	}



	/** Starts the UA */
	protected void run() {
		
		// Set the re-invite
		if (_uiConfig.reinviteTime>0) {
			reInvite(_uiConfig.reinviteTime);
		}

		// Set the transfer (REFER)
		if (_uiConfig.transferTo!=null && _uiConfig.transferTime>0) {
			callTransfer(_uiConfig.transferTo,_uiConfig.transferTime);
		}

		if (_uiConfig.doUnregisterAll) {
			// ########## unregisters ALL contact URIs
			LOG.info("Unregister all contact URIs");
			ua.unregisterall();
		} 

		if (_uiConfig.doUnregister) {
			// unregisters the contact URI
			LOG.info("Unregister the contact URI");
			ua.unregister();
		} 

		if (_uaConfig.isRegister()) {
			// ########## registers the contact URI with the registrar server
			LOG.info("Starting registration");
			ua.loopRegister(_uaConfig.getExpires(),_uaConfig.getExpires()/2,_uaConfig.getKeepAliveTime());
		} 

		if (_uiConfig.callTo!=null) {
			// ########## make a call with the remote URI
			LOG.info("UAC: CALLING " + _uiConfig.callTo);
			jComboBox1.setSelectedItem(null);
			comboBoxEditor1.setItem(_uiConfig.callTo.toString());
			display.setText("CALLING "+_uiConfig.callTo);
			ua.call(_uiConfig.callTo, mediaAgent());
			changeStatus(UA_OUTGOING_CALL);       
		} 

		if (!_mediaConfig.isAudio() && !_mediaConfig.isVideo())
			LOG.info("ONLY SIGNALING, NO MEDIA");
	}

	private MediaAgent mediaAgent() {
		return new MediaAgent(_mediaConfig.getMediaDescs(), _streamerFactory);
	}

	/** Exits. */
	protected void exit() {
		// close possible active call before exiting
		jButton2_actionPerformed();
		// exit now
		System.exit(0);
	}


	/** When the call/accept button is pressed. */
	void jButton1_actionPerformed() {
		
		if (statusIs(UA_IDLE)) {
			String uri=(String)comboBoxEditor1.getItem();
			if (uri!=null && uri.length()>0) {
				ua.hangup();
				display.setText("CALLING "+uri);
				ua.call(uri, mediaAgent());
				changeStatus(UA_OUTGOING_CALL);
			}
		}
		else
		if (statusIs(UA_INCOMING_CALL)) {
			ua.accept(mediaAgent());
			display.setText("ON CALL");
			changeStatus(UA_ONCALL);
		}
	}


	/** When the refuse/hangup button is pressed. */
	void jButton2_actionPerformed() {
		
		if (!statusIs(UA_IDLE)) {
			ua.hangup();
			//ua.listen();
			changeStatus(UA_IDLE);      
 
			display.setText("HANGUP");
		}
	}


	/** When the combo-box action is performed. */
	void jComboBox1_actionPerformed(ActionEvent e) {
		// if the edited URI is different from the selected item, copy the selected item in the editor
		/*
		String edit_name=(String)comboBoxEditor1.getItem();
		int index=jComboBox1.getSelectedIndex();
		if (index>=0) {
			String selected_name=buddy_list.elementAt(index);
			if (!selected_name.equals(edit_name)) comboBoxEditor1.setItem(selected_name);
		}*/
	}


	/** When the combo-box text field is changed. */
	void comboBoxEditor1_actionPerformed(ActionEvent e) {
		// if a new URI has been typed, insert it in the buddy_list and make it selected item
		// else, simply make the URI the selected item
		String name=(String)comboBoxEditor1.getItem();
		// parse separatly NameAddrresses or SipURIs
		if (name.indexOf("\"")>=0 || name.indexOf("<")>=0) {
			// try to parse a NameAddrress
			NameAddress nameaddr=(new SipParser(name)).getNameAddress();
			if (nameaddr!=null) name=nameaddr.toString();
			else name=null;
		}
		else {
			// try to parse a SipURI
			SipURI uri=SipURI.parseSipURI(name);
			if (uri!=null) name=uri.toString();
			else name=null;
		}

		if (name==null) {
			LOG.debug("No SIP URI recognized in: "+(String)comboBoxEditor1.getItem());
			return;
		}

		// checks if the the URI is already present in the buddy_list
		if (!buddy_list.contains(name)) {
			jComboBox1.insertItemAt(name,0);
			jComboBox1.setSelectedIndex(0);
			// limit the list size
			while (buddy_list.getElements().size()>NMAX_CONTACTS) jComboBox1.removeItemAt(NMAX_CONTACTS);
			// save new contact list
			buddy_list.save();         
		}
		else {
			int index=buddy_list.indexOf(name);
			jComboBox1.setSelectedIndex(index);
		}
 
	}


	/** Gets the UserAgent */
	/*protected UserAgent getUA() {
		return ua;
	}*/


	// ********************** UA callback functions **********************

	/** When a new call is incoming */
	@Override
	public void onUaIncomingCall(UserAgent ua, NameAddress callee, NameAddress caller, MediaDesc[] media_descs) {
		changeStatus(UA_INCOMING_CALL);
		if (_uiConfig.redirectTo!=null) {
			// redirect the call
			display.setText("CALL redirected to "+_uiConfig.redirectTo);
			ua.redirect(_uiConfig.redirectTo);
		}         
		else
		if (_uiConfig.acceptTime>=0) {
			// automatically accept the call
			display.setText("ON CALL");
			jComboBox1.setSelectedItem(null);
			comboBoxEditor1.setItem(caller.toString());
			//accept();
			automaticAccept(_uiConfig.acceptTime);
		}
		else {
			display.setText("INCOMING CALL");
			jComboBox1.setSelectedItem(null);
			comboBoxEditor1.setItem(caller.toString());
		}
	}


	/** When an ougoing call is stated to be in progress */
	@Override
	public void onUaCallProgress(UserAgent ua) {
		display.setText("PROGRESS");
	}


	/** When an ougoing call is remotly ringing */
	@Override
	public void onUaCallRinging(UserAgent ua) {
		display.setText("RINGING");
	}


	/** When an ougoing call has been accepted */
	@Override
	public void onUaCallAccepted(UserAgent ua) {
		display.setText("ON CALL");
		changeStatus(UA_ONCALL);
	}


	/** When an incoming call has been cancelled */
	@Override
	public void onUaCallCancelled(UserAgent ua) {
		display.setText("CANCELLED");
		//ua.listen();
		changeStatus(UA_IDLE);
	}


	/** When a call has been transferred */
	@Override
	public void onUaCallTransferred(UserAgent ua) {
		display.setText("TRASFERRED");
		//ua.listen();
		changeStatus(UA_IDLE);
	}


	/** When an ougoing call has been refused or timeout */
	@Override
	public void onUaCallFailed(UserAgent ua, String reason) {
		display.setText("FAILED"+((reason!=null)? " ("+reason+")" : ""));
		//ua.listen();
		changeStatus(UA_IDLE);
	}


	/** When a call has been locally or remotely closed */
	@Override
	public void onUaCallClosed(UserAgent ua) {
		display.setText("BYE");
		//ua.listen();
		changeStatus(UA_IDLE);
	}

	/** When a new media session is started. */
	@Override
	public void onUaMediaSessionStarted(UserAgent ua, String type, String codec) {
		//log(type+" started "+codec);
	}

	/** When a media session is stopped. */
	@Override
	public void onUaMediaSessionStopped(UserAgent ua, String type) {
		//log(type+" stopped");
	}


	/** When registration succeeded. */
	@Override
	public void onUaRegistrationSucceeded(UserAgent ua, String result) {
		this.setTitle(_uaConfig.getUserURI().toString());
	}

	/** When registration failed. */
	@Override
	public void onUaRegistrationFailed(UserAgent ua, String result) {
		this.setTitle(sip_provider.getContactAddress(_uaConfig.getUser(), AddressType.DEFAULT).toString());
	}


	// ************************ scheduled events ************************

	/** Schedules a re-inviting after <i>delay_time</i> secs. It simply changes the contact address. */
	/*void reInvite(final NameAddress contact, final int delay_time) {
		new ScheduledWork(delay_time*1000) {
			public void doWork() {
				log("AUTOMATIC RE-INVITING/MODIFING");
				ua.modify(contact,null);
			}
		};
	}*/
	/** Schedules a re-inviting after <i>delay_time</i> secs. It simply changes the contact address. */
	void reInvite(final int delay_time) {
		LOG.info("AUTOMATIC RE-INVITING/MODIFING: "+delay_time+" secs"); 
		if (delay_time==0) ua.modify(null);
		else
			sip_provider.scheduler().schedule((long) (delay_time*1000), ()->ua.modify(null));
	}


	/** Schedules a call-transfer after <i>delay_time</i> secs. */
	/*void callTransfer(final NameAddress transfer_to, final int delay_time) {
		new ScheduledWork(delay_time*1000) {
			public void doWork() {
				log("AUTOMATIC REFER/TRANSFER");
				ua.transfer(transfer_to);
			}
		};
	}*/
	/** Schedules a call-transfer after <i>delay_time</i> secs. */
	void callTransfer(final NameAddress transfer_to, final int delay_time) {
		LOG.info("AUTOMATIC REFER/TRANSFER: "+delay_time+" secs");
		if (delay_time==0) ua.transfer(transfer_to);
		else
			sip_provider.scheduler().schedule((long) (delay_time*1000), () -> ua.transfer(transfer_to));
	}


	/** Schedules an automatic answer after <i>delay_time</i> secs. */
	/*void automaticAccept(final int delay_time) {
		new ScheduledWork(delay_time*1000) {
			public void doWork() {
				log("AUTOMATIC ANSWER");
				jButton1_actionPerformed();
			}
		};
	}*/
	/** Schedules an automatic answer after <i>delay_time</i> secs. */
	void automaticAccept(final int delay_time) {
		LOG.info("AUTOMATIC ANSWER: "+delay_time+" secs");
		if (delay_time==0) jButton1_actionPerformed();
		else
			sip_provider.scheduler().schedule((long) (delay_time*1000), this::jButton1_actionPerformed);
	}
	

	/** Schedules an automatic hangup after <i>delay_time</i> secs. */
	/*void automaticHangup(final int delay_time) {
		new ScheduledWork(delay_time*1000) {
			public void doWork() {
				printLog("AUTOMATIC HANGUP");
				jButton2_actionPerformed();
			}
		};
	}*/
	/** Schedules an automatic hangup after <i>delay_time</i> secs. */
	void automaticHangup(final int delay_time) {
		LOG.info("AUTOMATIC HANGUP: "+delay_time+" secs");
		if (delay_time==0) jButton2_actionPerformed();
		else
			sip_provider.scheduler().schedule((long) (delay_time*1000), this::jButton2_actionPerformed);
	}


	// ****************************** Static ******************************

	private static ImageIcon getImageIcon(String image_file) throws java.io.IOException {
		return Archive.getImageIcon(UserAgent.class.getResource("/" + image_file));
	}

	/** The main method. */
	public static void main(String[] args) {
		println("MJSIP UserAgent "+SipStack.version);

		SipConfig sipConfig = new SipConfig();
		UAConfig uaConfig = new UAConfig();
		SchedulerConfig schedulerConfig = new SchedulerConfig();
		MediaConfig mediaConfig = new MediaConfig();
		PortConfig portConfig = new PortConfig();
		UIConfig uiConfig = new UIConfig();

		OptionParser.parseOptions(args, ".mjsip-ua", sipConfig, uaConfig, schedulerConfig, mediaConfig, portConfig, uiConfig);
		
		sipConfig.normalize();
		uaConfig.normalize(sipConfig);

		new UserAgentGui(new SipProvider(sipConfig, new ConfiguredScheduler(schedulerConfig)), portConfig.createPool(), uaConfig, uiConfig, mediaConfig);
	}
	
	/** Prints a message to standard output. */
	protected static void println(String str) {
		System.out.println(str);
	}
	
}
