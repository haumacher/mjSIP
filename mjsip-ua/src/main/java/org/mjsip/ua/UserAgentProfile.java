package org.mjsip.ua;


import java.util.Vector;

import org.mjsip.media.MediaDesc;
import org.mjsip.media.MediaSpec;
import org.mjsip.sip.address.GenericURI;
import org.mjsip.sip.address.NameAddress;
import org.mjsip.sip.address.SipURI;
import org.mjsip.sip.address.UnexpectedUriSchemeException;
import org.mjsip.sip.provider.SipProvider;
import org.zoolu.net.SocketAddress;
import org.zoolu.util.Configure;
import org.zoolu.util.MultiTable;
import org.zoolu.util.Parser;
import org.zoolu.util.VectorUtils;


/** UserAgentProfile maintains the user configuration.
  */
public class UserAgentProfile extends Configure {
		 
	// ********************** user configurations *********************

	/** Display name for the user.
	  * It is used in the user's AOR registered to the registrar server
	  * and used as From URI. */
	public String displayName=null;

	/** User's name.
	  * It is used to build the user's AOR registered to the registrar server
	  * and used as From URI. */
	public String user=null;

	/** Fully qualified domain name (or address) of the proxy server.
	  * It is part of the user's AOR registered to the registrar server
	  * and used as From URI.
	  * <p>
	  * If <i>proxy</i> is not defined, the <i>registrar</i> value is used in its place.
	  * <p>
	  * If <i>registrar</i> is not defined, the <i>proxy</i> value is used in its place. */
	public String proxy=null;

	/** Fully qualified domain name (or address) of the registrar server.
	  * It is used as recipient for REGISTER requests.
	  * <p>
	  * If <i>registrar</i> is not defined, the <i>proxy</i> value is used in its place.
	  * <p>
	  * If <i>proxy</i> is not defined, the <i>registrar</i> value is used in its place. */
	public String registrar=null;

	/** UA address.
	  * It is the SIP address of the UA and is used to form the From URI if no proxy is configured. */
	public String ua_address=null;

	/** User's name used for server authentication. */
	public String authUser=null;
	/** User's realm used for server authentication. */
	public String authRealm=null;
	/** User's passwd used for server authentication. */
	public String authPasswd=null;

	/** Relative path of UA media resources (gif, wav, etc.) within the UA jar file or within the resources folder. 
	  * By default, the folder "media/org/mjsip/ua" is used. */
	public static String mediaPath="media/org/mjsip/ua";

	/** Absolute path (or complete URI) of the buddy list file where the buddy list is and loaded from (and saved to).
	  * By default, the file "buddy.lst" is used. */
	public static String buddyListFile="buddy.lst";

	/** Whether registering with the registrar server */
	public boolean doRegister=false;
	/** Whether unregistering the contact address */
	public boolean doUnregister=false;
	/** Whether unregistering all contacts beafore registering the contact address */
	public boolean doUnregisterAll=false;
	/** Expires time (in seconds). */
	public int expires=3600;

	/** Rate of keep-alive tokens (datagrams) sent toward the outbound proxy
	  * (if present) or toward the registrar server.
	  * Its value specifies the delta-time (in millesconds) between two
	  * keep-alive tokens. <br>
	  * Set keepalive_time=0 for not sending keep-alive datagrams. */
	public long keepaliveTime=0;

	/** Automatic call a remote user secified by the 'call_to' value.
	  * Use value 'NONE' for manual calls (or let it undefined).  */
	public NameAddress callTo=null;
			
	/** Response time in seconds; it is the maximum time the user can wait before responding to an incoming call; after such time the call is automatically declined (refused). */
	public int refuseTime=20;
	/** Automatic answer time in seconds; time&lt;0 corresponds to manual answer mode. */
	public int acceptTime=-1;        
	/** Automatic hangup time (call duartion) in seconds; time&lt;=0 corresponds to manual hangup mode. */
	public int hangupTime=-1;
	/** Automatic call transfer time in seconds; time&lt;0 corresponds to no auto transfer mode. */
	public int transferTime=-1;
	/** Automatic re-inviting time in seconds; time&lt;0 corresponds to no auto re-invite mode.  */
	public int reinviteTime=-1;
	/** Automatic re-call time in seconds; time&lt;0 corresponds to no auto re-call mode.  */
	public int recallTime=-1;
	/** Number of successive automatic re-calls; it is used only if call_to!=null, re_call_time&gt;0, and re_call_count&gt;0.  */
	public int recallCount=-1;

	/** Redirect incoming call to the secified URI.
	  * Use value 'NONE' for not redirecting incoming calls (or let it undefined). */
	public NameAddress redirectTo=null;

	/** Transfer calls to the secified URI.
	  * Use value 'NONE' for not transferring calls (or let it undefined). */
	public NameAddress transferTo=null;

	/** No offer in the invite */
	public boolean noOffer=false;
	/** Do not use system audio  */
	public boolean noSystemAudio=false;
	/** Do not use prompt */
	public boolean noPrompt=false;
	
	/** Whether using audio */
	public boolean audio=true;
	/** Whether using video */
	public boolean video=false;

	/** Whether looping the received media streams back to the sender. */
	public boolean loopback=false;
	/** Whether playing in receive only mode */
	public boolean recvOnly=false;
	/** Whether playing in send only mode */
	public boolean sendOnly=false;
	/** Whether playing a test tone in send only mode */
	public boolean sendTone=false;
	/** Audio file to be streamed */
	public String sendFile=null;
	/** Audio file to be recorded */
	public String recvFile=null;
	/** Video file to be streamed */
	public String sendVideoFile=null;
	/** Video file to be recorded */
	public String recvVideoFile=null;

	/** Media address (use it if you want to use a media address different from the via address) */
	public String mediaAddr=null;
	/** First media port (use it if you want to use media ports different from those specified in media_descs) */
	private int mediaPort=-1;

	/** Whether using symmetric_rtp */
	public boolean symmetricRtp=false;

	/** Array of media descriptions */
	public MediaDesc[] mediaDescs=new MediaDesc[]{};
	/** Vector of media descriptions */
	private Vector mediaDescVector=new Vector();
	/** Table of media specifications, as multiple-values table of (String)media-->(MediaSpec)media_spec */
	private MultiTable mediaSpecTable=new MultiTable();

	/** Whether using JMF for audio streaming */
	public boolean useJmfAudio=false;
	/** Whether using JMF for video streaming */
	public boolean useJmfVideo=true;
	/** Whether using RAT (Robust Audio Tool) as audio sender/receiver */
	public boolean useRat=false;
	/** Whether using VIC (Video Conferencing Tool) as video sender/receiver */
	public boolean useVic=false;
	/** RAT command-line executable */
	public String binRat="rat";
	/** VIC command-line executable */
	public String binVic="vic";


	// ******************** undocumented parametes ********************

	/** Whether running the UAS (User Agent Server), or acting just as UAC (User Agent Client). In the latter case only outgoing calls are supported. */
	public boolean uaServer=true;
	/** Whether running an Options Server, that automatically responds to OPTIONS requests. */
	public boolean optionsServer=true;
	/** Whether running an Null Server, that automatically responds to not-implemented requests. */
	public boolean nullServer=true;

	/** Alternative javax-sound-based audio streamer (currently just for tests) */
	public String javaxSoundStreamer=null;

	/** Whether using explicit external converter (i.e. direct access to an external conversion provider)
	  * instead of that provided by javax.sound.sampled.spi.
	  * It applies only when javax sound is used, that is when no other audio apps (such as jmf or rat) are used. */
	public boolean javaxSoundDirectConversion=false;

	/** Sender synchronization adjustment, that is the time (in milliseconds) that a frame
	  * should be sent in advance by the RTP sender, before the nominal time.
	  * A value less that 0 means no re-synchronization explicitely performed by the RTP sender.
	  * <p>
	  * Note that when using audio capturing, synchronization with the sample rate
	  * is implicitely performed by the audio capture device and frames are read at constat bit rate.
	  * However, a value of this parameter >=0 (explicit re-synchronization) is suggested
	  * in order to let the read() method be non-blocking (in the other case
	  * the UA audio performances seem to decrease). */
	//public int javax_sound_sync_adj=2;

	/** Whether enforcing time synchronization to RTP source stream.
	  * If synchronization is explicitely performed, the depature time of each RTP packet is equal to its nominal time.
	  * <p>
	  * Note that when using audio capturing, synchronization with the sample rate
	  * is implicitely performed by the audio capture device and frames are read at constat bit rate.
	  * However, an explicit re-synchronization is suggested
	  * in order to let the read() method be non-blocking (in the other case
	  * the UA audio performance seems decreasing. */
	public boolean javaxSoundSync=true;

	/** Receiver random early drop (RED) rate. Actually it is the inverse of packet drop rate.
	  * It can used to prevent long play back delay. 
	  * A value less or equal to 0 means that no packet dropping is explicitely
	  * performed at the RTP receiver. */
	public int randomEarlyDropRate=20;

	/** Fixed audio multicast socket address; if defined, it forces the use of this maddr+port for audio session */
	public SocketAddress audioMcastSoAddr=null;
	/** Fixed video multicast socket address; if defined, it forces the use of this maddr+port for video session */
	public SocketAddress videoMcastSoAddr=null;


	// ************************** costructors *************************
	
	/** Costructs a void UserAgentProfile */
	public UserAgentProfile() {
		init();
	}

	/** Costructs a new UserAgentProfile
	  * @param file the name of the configuration file */
	public UserAgentProfile(String file) {
		// load configuration
		loadFile(file);
		// post-load manipulation     
		init();
	}

	/** Inits the UserAgentProfile. */
	private void init() {
		if (proxy!=null && proxy.equalsIgnoreCase(Configure.NONE)) proxy=null;
		if (registrar!=null && registrar.equalsIgnoreCase(Configure.NONE)) registrar=null;
		if (displayName!=null && displayName.equalsIgnoreCase(Configure.NONE)) displayName=null;
		if (user!=null && user.equalsIgnoreCase(Configure.NONE)) user=null;
		if (authRealm!=null && authRealm.equalsIgnoreCase(Configure.NONE)) authRealm=null;
		if (sendFile!=null && sendFile.equalsIgnoreCase(Configure.NONE)) sendFile=null;
		if (recvFile!=null && recvFile.equalsIgnoreCase(Configure.NONE)) recvFile=null;

		// BEGIN PATCH FOR JMF SUPPORT
		if (audio && useJmfAudio) {
			mediaSpecTable.remove("audio");
			mediaSpecTable.put("audio",new MediaSpec("audio",11,"L16",16000,1,320));
		}
		else
		if (video && useJmfVideo) {
			mediaSpecTable.remove("video");
			mediaSpecTable.put("video",new MediaSpec("video",101,null,-1,1,-1));
		}
		// END PATCH FOR JMF SUPPORT
		
		// media descriptions
		if (mediaDescVector.size()==0 && audio) {
			// add default auido support
			mediaDescVector.addElement(MediaDesc.parseMediaDesc("audio 4080 RTP/AVP { audio 0 PCMU 8000 160, audio 8 PCMA 8000 160 }"));
		}
		mediaDescs=new MediaDesc[mediaDescVector.size()];
		for (int i=0; i<mediaDescVector.size(); i++) {
			MediaDesc md=(MediaDesc)mediaDescVector.elementAt(i);
			Vector media_spec_vector=new Vector();
			MediaSpec[] ms_array=md.getMediaSpecs();
			if (ms_array.length>0) {
				//media_spec_vector.addAll(Arrays.asList(ms_array));
				VectorUtils.addArray(media_spec_vector,ms_array);
			}
			Vector ms_vector=mediaSpecTable.get(md.getMedia());
			if (ms_vector!=null) {
				//media_spec_vector.addAll(ms_vector);
				VectorUtils.addVector(media_spec_vector,ms_vector);
			}
			//MediaSpec[] media_specs=(MediaSpec[])media_spec_vector.toArray(new MediaSpec[]{});
			MediaSpec[] media_specs=(MediaSpec[])VectorUtils.vectorToArray(media_spec_vector,new MediaSpec[media_spec_vector.size()]);
			mediaDescs[i]=new MediaDesc(md.getMedia(),md.getPort(),md.getTransport(),media_specs);
		}
		
		if (mediaPort>0) setMediaPort(mediaPort);
		
		setUnconfiguredAttributes(null);
	}


	// ************************ public methods ************************

	/** Gets the user's AOR (Address Of Record) registered to the registrar server
	  * and used as From URI.
	  * <p>
	  * In case of <i>proxy</i> and <i>user</i> parameters have been defined
	  * it is formed as "<i>display_name</i>" &lt;sip:<i>user</i>@<i>proxy</i>&gt;,
	  * otherwhise the local UA address (obtained by the SipProvider) is used.
	  * @return the user's name address */
	public NameAddress getUserURI() {
		if (proxy!=null && user!=null) return new NameAddress(displayName,new SipURI(user,proxy));
		else return new NameAddress(displayName,new SipURI(user,ua_address));
	}

	/** Sets the user's AOR (Address Of Record) registered to the registrar server
	  * and used as From URI.
	  * <p>
	  * It actually sets the <i>display_name</i>, <i>user</i>, and <i>proxy</i> parameters.
	  * <p>
	  * If <i>registrar</i> is not defined, the <i>proxy</i> value is used in its place.
	  * @param naddr the user's name address formed by the user's display name (optional) and URI */
	public void setUserURI(NameAddress naddr) {
		GenericURI naddr_uri=naddr.getAddress();
		if (!naddr_uri.isSipURI()) throw new UnexpectedUriSchemeException(naddr_uri.getScheme());
		// else
		SipURI uri=new SipURI(naddr_uri);
		if (displayName==null) displayName=naddr.getDisplayName();
		if (user==null) user=uri.getUserName();
		if (proxy==null) proxy=(uri.hasPort())? uri.getHost()+":"+uri.getPort() : uri.getHost();
		if (registrar==null) registrar=proxy;
	}

	/** Sets server and authentication attributes (if not already done).
	  * It actually sets <i>ua_address</i>, <i>registrar</i>, <i>proxy</i>, <i>auth_realm</i>,
	  * and <i>auth_user</i> attributes.
	  * <p>
	  * Note: this method sets such attributes only if they haven't still been initilized.
	  * @param sip_provider the SIP provider used for initializing these attributes */
	public void setUnconfiguredAttributes(SipProvider sip_provider) {
		if (registrar==null && proxy!=null) registrar=proxy;
		if (proxy==null && registrar!=null) proxy=registrar;
		if (authRealm==null && proxy!=null) authRealm=proxy;
		if (authRealm==null && registrar!=null) authRealm=registrar;
		if (authUser==null && user!=null) authUser=user;
		if (ua_address==null && sip_provider!=null) {
			ua_address=sip_provider.getViaAddress();
			if (sip_provider.getPort()!=sip_provider.sipConfig.defaultPort) ua_address+=":"+sip_provider.getPort();
		}
	}


	/** Sets the transport port for each medium.
	  * The media ports are set incrementally starting from the given value and incremented by 2 for each medium.
	  * Example, if two media are present, e.g. audio, video, text, and <i>media_port</i> is 4000,
	  * then the audio port will be 4000, the video port will be 4002, and the text port will be 4004. 
	  * @param media_port the port number for the first medium */
	public void setMediaPort(int media_port) {
		setMediaPort(media_port,2);
	}


	/** Sets the transport port for each medium.
	  * The media ports are set incrementally starting from the given value <i>media_port</i> and incremented by the given value <i>diff</i>.
	  * The assigned ports will be <i>media_port</i>, <i>media_port</i>+<i>diff</i>, <i>media_port</i>+2*<i>diff</i>, etc.
	  * Example, if two media are present, e.g. audio, video, text, <i>media_port</i> is 4000, and <i>diff</i> is 2,
	  * then the audio port will be 4000+<i>inc</i>, the video port will be 4000+2*<i>inc</i>, and the text port will be 4004. 
	  * @param media_port the port number for the first medium
	  * @param diff the incremented value for successive media ports */
	public void setMediaPort(int media_port, int diff) {
		for (int i=0; i<mediaDescs.length; i++) {
			MediaDesc md=(MediaDesc)mediaDescs[i];
			md.setPort(media_port);
			media_port+=diff;
		}
	}


	/** Gets the transport port of the first medium.
	  * @return the port number of the first medium,if any, otherwise -1 */
	public int getMediaPort() {
		if (mediaDescs!=null && mediaDescs.length>0) return ((MediaDesc)mediaDescs[0]).getPort();
		else return -1;
	}


	// *********************** protected methods **********************

	/** Parses a single line (loaded from the config file)
	  * @param line a string containing the pair attribute name and attribute value, separated by a "=" */
	@Override
	public void setOption(String attribute, Parser par) {
		if (attribute.equals("display_name"))   {  displayName=par.getRemainingString().trim();  return;  }
		if (attribute.equals("user"))           {  user=par.getString();  return;  }
		if (attribute.equals("proxy"))          {  proxy=par.getString();  return;  }
		if (attribute.equals("registrar"))      {  registrar=par.getString();  return;  }

		if (attribute.equals("auth_user"))      {  authUser=par.getString();  return;  } 
		if (attribute.equals("auth_realm"))     {  authRealm=par.getRemainingString().trim();  return;  }
		if (attribute.equals("auth_passwd"))    {  authPasswd=par.getRemainingString().trim();  return;  }

		if (attribute.equals("media_path"))     {  mediaPath=par.getStringUnquoted();  return;  }      
		if (attribute.equals("buddy_list_file")){  buddyListFile=par.getStringUnquoted();  return;  }      

		if (attribute.equals("do_register"))    {  doRegister=(par.getString().toLowerCase().startsWith("y"));  return;  }
		if (attribute.equals("do_unregister"))  {  doUnregister=(par.getString().toLowerCase().startsWith("y"));  return;  }
		if (attribute.equals("do_unregister_all")) {  doUnregisterAll=(par.getString().toLowerCase().startsWith("y"));  return;  }
		if (attribute.equals("expires"))        {  expires=par.getInt();  return;  } 
		if (attribute.equals("keepalive_time")) {  keepaliveTime=par.getInt();  return;  } 

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
		if (attribute.equals("refuse_time"))    {  refuseTime=par.getInt();  return;  }
		if (attribute.equals("accept_time"))    {  acceptTime=par.getInt();  return;  }
		if (attribute.equals("hangup_time"))    {  hangupTime=par.getInt();  return;  } 
		if (attribute.equals("transfer_time"))  {  transferTime=par.getInt();  return;  } 
		if (attribute.equals("re_invite_time")) {  reinviteTime=par.getInt();  return;  } 
		if (attribute.equals("re_call_time"))   {  recallTime=par.getInt();  return;  } 
		if (attribute.equals("re_call_count"))  {  recallCount=par.getInt();  return;  } 
		if (attribute.equals("no_offer"))       {  noOffer=(par.getString().toLowerCase().startsWith("y"));  return;  }
		if (attribute.equals("no_system_audio")){  noSystemAudio=(par.getString().toLowerCase().startsWith("y"));  return;  }
		if (attribute.equals("no_prompt"))      {  noPrompt=(par.getString().toLowerCase().startsWith("y"));  return;  }

		if (attribute.equals("loopback"))       {  loopback=(par.getString().toLowerCase().startsWith("y"));  return;  }
		if (attribute.equals("recv_only"))      {  recvOnly=(par.getString().toLowerCase().startsWith("y"));  return;  }
		if (attribute.equals("send_only"))      {  sendOnly=(par.getString().toLowerCase().startsWith("y"));  return;  }
		if (attribute.equals("send_tone"))      {  sendTone=(par.getString().toLowerCase().startsWith("y"));  return;  }
		if (attribute.equals("send_file"))      {  sendFile=par.getRemainingString().trim();  return;  }
		if (attribute.equals("recv_file"))      {  recvFile=par.getRemainingString().trim();  return;  }
		if (attribute.equals("send_video_file")){  sendVideoFile=par.getRemainingString().trim();  return;  }
		if (attribute.equals("recv_video_file")){  recvVideoFile=par.getRemainingString().trim();  return;  }

		if (attribute.equals("audio"))          {  audio=(par.getString().toLowerCase().startsWith("y"));  return;  }
		if (attribute.equals("video"))          {  video=(par.getString().toLowerCase().startsWith("y"));  return;  }

		if (attribute.equals("media_addr"))     {  mediaAddr=par.getString();  return;  } 
		if (attribute.equals("media_port"))     {  mediaPort=par.getInt();  return;  } 
		if (attribute.equals("symmetric_rtp"))  {  symmetricRtp=(par.getString().toLowerCase().startsWith("y"));  return;  } 
		if (attribute.equals("media") ||
			 attribute.equals("media_desc"))    {  mediaDescVector.addElement(MediaDesc.parseMediaDesc(par.getRemainingString().trim()));  return;  }
		if (attribute.equals("media_spec"))     {  MediaSpec ms=MediaSpec.parseMediaSpec(par.getRemainingString().trim());  mediaSpecTable.put(ms.getType(),ms);  return;  }

		if (attribute.equals("use_jmf_audio"))  {  useJmfAudio=(par.getString().toLowerCase().startsWith("y"));  return;  }
		if (attribute.equals("use_jmf_video"))  {  useJmfVideo=(par.getString().toLowerCase().startsWith("y"));  return;  }
		if (attribute.equals("use_rat"))        {  useRat=(par.getString().toLowerCase().startsWith("y"));  return;  }
		if (attribute.equals("bin_rat"))        {  binRat=par.getStringUnquoted();  return;  }
		if (attribute.equals("use_vic"))        {  useVic=(par.getString().toLowerCase().startsWith("y"));  return;  }
		if (attribute.equals("bin_vic"))        {  binVic=par.getStringUnquoted();  return;  }      

		if (attribute.equals("ua_server")) {  uaServer=(par.getString().toLowerCase().startsWith("y"));  return;  }
		if (attribute.equals("options_server")) {  optionsServer=(par.getString().toLowerCase().startsWith("y"));  return;  }
		if (attribute.equals("null_server")) {  nullServer=(par.getString().toLowerCase().startsWith("y"));  return;  }
		if (attribute.equals("javax_sound_streamer")) {  javaxSoundStreamer=par.getString();  return;  }
		if (attribute.equals("javax_sound_direct_convertion")) {  javaxSoundDirectConversion=(par.getString().toLowerCase().startsWith("y"));  return;  }
		if (attribute.equals("javax_sound_sync")) {  javaxSoundSync=(par.getString().toLowerCase().startsWith("y"));  return;  }
		if (attribute.equals("random_early_drop_rate")) {  randomEarlyDropRate=par.getInt();  return;  }
		if (attribute.equals("audio_mcast_soaddr")) {  audioMcastSoAddr=new SocketAddress(par.getString());  return;  } 
		if (attribute.equals("video_mcast_soaddr")) {  videoMcastSoAddr=new SocketAddress(par.getString());  return;  }
	}

}
