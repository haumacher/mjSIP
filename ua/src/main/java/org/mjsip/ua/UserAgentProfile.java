package org.mjsip.ua;


import java.io.File;
import java.util.Vector;

import org.mjsip.media.MediaDesc;
import org.mjsip.media.MediaSpec;
import org.mjsip.sip.address.GenericURI;
import org.mjsip.sip.address.NameAddress;
import org.mjsip.sip.address.SipURI;
import org.mjsip.sip.address.UnexpectedUriSchemeException;
import org.mjsip.sip.provider.SipProvider;
import org.mjsip.sip.provider.SipStack;
import org.zoolu.net.SocketAddress;
import org.zoolu.util.Configure;
import org.zoolu.util.MultiTable;
import org.zoolu.util.Parser;
import org.zoolu.util.VectorUtils;


/** UserAgentProfile maintains the user configuration.
  */
public class UserAgentProfile extends Configure {
	
	/** The default configuration file */
	private static String config_file="mjsip.cfg";

		 
	// ********************** user configurations *********************

	/** Display name for the user.
	  * It is used in the user's AOR registered to the registrar server
	  * and used as From URI. */
	public String display_name=null;

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
	public String auth_user=null;
	/** User's realm used for server authentication. */
	public String auth_realm=null;
	/** User's passwd used for server authentication. */
	public String auth_passwd=null;

	/** Absolute path (or complete URI) of the jar archive, where various UA media (gif, wav, etc.) are stored.
	  * Use value 'NONE' for getting resources from external folders.
	  * By default, the file "lib/ua.jar" or "lib/mjua.jar" is used. */
	public static String ua_jar=null;

	/** Absolute path (or complete URI) of the folder containing UA resources.
	 * By default, the local folder "resources" is used. */
  public static String res_path="resources";

	/** Relative path of UA media resources (gif, wav, etc.) within the UA jar file or within the resources folder. 
	  * By default, the folder "media/org/mjsip/ua" is used. */
	public static String media_path="media/org/mjsip/ua";

	/** Absolute path (or complete URI) of the buddy list file where the buddy list is and loaded from (and saved to).
	  * By default, the file "buddy.lst" is used. */
	public static String buddy_list_file="buddy.lst";

	/** Whether registering with the registrar server */
	public boolean do_register=false;
	/** Whether unregistering the contact address */
	public boolean do_unregister=false;
	/** Whether unregistering all contacts beafore registering the contact address */
	public boolean do_unregister_all=false;
	/** Expires time (in seconds). */
	public int expires=3600;

	/** Rate of keep-alive tokens (datagrams) sent toward the outbound proxy
	  * (if present) or toward the registrar server.
	  * Its value specifies the delta-time (in millesconds) between two
	  * keep-alive tokens. <br>
	  * Set keepalive_time=0 for not sending keep-alive datagrams. */
	public long keepalive_time=0;

	/** Automatic call a remote user secified by the 'call_to' value.
	  * Use value 'NONE' for manual calls (or let it undefined).  */
	public NameAddress call_to=null;
			
	/** Response time in seconds; it is the maximum time the user can wait before responding to an incoming call; after such time the call is automatically declined (refused). */
	public int refuse_time=20;
	/** Automatic answer time in seconds; time&lt;0 corresponds to manual answer mode. */
	public int accept_time=-1;        
	/** Automatic hangup time (call duartion) in seconds; time&lt;=0 corresponds to manual hangup mode. */
	public int hangup_time=-1;
	/** Automatic call transfer time in seconds; time&lt;0 corresponds to no auto transfer mode. */
	public int transfer_time=-1;
	/** Automatic re-inviting time in seconds; time&lt;0 corresponds to no auto re-invite mode.  */
	public int re_invite_time=-1;
	/** Automatic re-call time in seconds; time&lt;0 corresponds to no auto re-call mode.  */
	public int re_call_time=-1;
	/** Number of successive automatic re-calls; it is used only if call_to!=null, re_call_time&gt;0, and re_call_count&gt;0.  */
	public int re_call_count=-1;

	/** Redirect incoming call to the secified URI.
	  * Use value 'NONE' for not redirecting incoming calls (or let it undefined). */
	public NameAddress redirect_to=null;

	/** Transfer calls to the secified URI.
	  * Use value 'NONE' for not transferring calls (or let it undefined). */
	public NameAddress transfer_to=null;

	/** No offer in the invite */
	public boolean no_offer=false;
	/** Do not use system audio  */
	public boolean no_system_audio=false;
	/** Do not use prompt */
	public boolean no_prompt=false;
	
	/** Whether using audio */
	public boolean audio=true;
	/** Whether using video */
	public boolean video=false;

	/** Whether looping the received media streams back to the sender. */
	public boolean loopback=false;
	/** Whether playing in receive only mode */
	public boolean recv_only=false;
	/** Whether playing in send only mode */
	public boolean send_only=false;
	/** Whether playing a test tone in send only mode */
	public boolean send_tone=false;
	/** Audio file to be streamed */
	public String send_file=null;
	/** Audio file to be recorded */
	public String recv_file=null;
	/** Video file to be streamed */
	public String send_video_file=null;
	/** Video file to be recorded */
	public String recv_video_file=null;

	/** Media address (use it if you want to use a media address different from the via address) */
	public String media_addr=null;
	/** First media port (use it if you want to use media ports different from those specified in media_descs) */
	private int media_port=-1;

	/** Whether using symmetric_rtp */
	public boolean symmetric_rtp=false;

	/** Array of media descriptions */
	public MediaDesc[] media_descs=new MediaDesc[]{};
	/** Vector of media descriptions */
	private Vector media_desc_vector=new Vector();
	/** Table of media specifications, as multiple-values table of (String)media-->(MediaSpec)media_spec */
	private MultiTable media_spec_table=new MultiTable();

	/** Whether using JMF for audio streaming */
	public boolean use_jmf_audio=false;
	/** Whether using JMF for video streaming */
	public boolean use_jmf_video=true;
	/** Whether using RAT (Robust Audio Tool) as audio sender/receiver */
	public boolean use_rat=false;
	/** Whether using VIC (Video Conferencing Tool) as video sender/receiver */
	public boolean use_vic=false;
	/** RAT command-line executable */
	public String bin_rat="rat";
	/** VIC command-line executable */
	public String bin_vic="vic";


	// ******************** undocumented parametes ********************

	/** Whether running the UAS (User Agent Server), or acting just as UAC (User Agent Client). In the latter case only outgoing calls are supported. */
	public boolean ua_server=true;
	/** Whether running an Options Server, that automatically responds to OPTIONS requests. */
	public boolean options_server=true;
	/** Whether running an Null Server, that automatically responds to not-implemented requests. */
	public boolean null_server=true;

	/** Alternative javax-sound-based audio streamer (currently just for tests) */
	public String javax_sound_streamer=null;

	/** Whether using explicit external converter (i.e. direct access to an external conversion provider)
	  * instead of that provided by javax.sound.sampled.spi.
	  * It applies only when javax sound is used, that is when no other audio apps (such as jmf or rat) are used. */
	public boolean javax_sound_direct_convertion=false;

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
	public boolean javax_sound_sync=true;

	/** Receiver random early drop (RED) rate. Actually it is the inverse of packet drop rate.
	  * It can used to prevent long play back delay. 
	  * A value less or equal to 0 means that no packet dropping is explicitely
	  * performed at the RTP receiver. */
	public int random_early_drop_rate=20;

	/** Fixed audio multicast socket address; if defined, it forces the use of this maddr+port for audio session */
	public SocketAddress audio_mcast_soaddr=null;
	/** Fixed video multicast socket address; if defined, it forces the use of this maddr+port for video session */
	public SocketAddress video_mcast_soaddr=null;


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
		if (display_name!=null && display_name.equalsIgnoreCase(Configure.NONE)) display_name=null;
		if (user!=null && user.equalsIgnoreCase(Configure.NONE)) user=null;
		if (auth_realm!=null && auth_realm.equalsIgnoreCase(Configure.NONE)) auth_realm=null;
		if (send_file!=null && send_file.equalsIgnoreCase(Configure.NONE)) send_file=null;
		if (recv_file!=null && recv_file.equalsIgnoreCase(Configure.NONE)) recv_file=null;

		// BEGIN PATCH FOR JMF SUPPORT
		if (audio && use_jmf_audio) {
			media_spec_table.remove("audio");
			media_spec_table.put("audio",new MediaSpec("audio",11,"L16",16000,1,320));
		}
		else
		if (video && use_jmf_video) {
			media_spec_table.remove("video");
			media_spec_table.put("video",new MediaSpec("video",101,null,-1,1,-1));
		}
		// END PATCH FOR JMF SUPPORT
		
		// media descriptions
		if (media_desc_vector.size()==0 && audio) {
			// add default auido support
			media_desc_vector.addElement(MediaDesc.parseMediaDesc("audio 4080 RTP/AVP { audio 0 PCMU 8000 160, audio 8 PCMA 8000 160 }"));
		}
		media_descs=new MediaDesc[media_desc_vector.size()];
		for (int i=0; i<media_desc_vector.size(); i++) {
			MediaDesc md=(MediaDesc)media_desc_vector.elementAt(i);
			Vector media_spec_vector=new Vector();
			MediaSpec[] ms_array=md.getMediaSpecs();
			if (ms_array.length>0) {
				//media_spec_vector.addAll(Arrays.asList(ms_array));
				VectorUtils.addArray(media_spec_vector,ms_array);
			}
			Vector ms_vector=media_spec_table.get(md.getMedia());
			if (ms_vector!=null) {
				//media_spec_vector.addAll(ms_vector);
				VectorUtils.addVector(media_spec_vector,ms_vector);
			}
			//MediaSpec[] media_specs=(MediaSpec[])media_spec_vector.toArray(new MediaSpec[]{});
			MediaSpec[] media_specs=(MediaSpec[])VectorUtils.vectorToArray(media_spec_vector,new MediaSpec[media_spec_vector.size()]);
			media_descs[i]=new MediaDesc(md.getMedia(),md.getPort(),md.getTransport(),media_specs);
		}
		
		if (media_port>0) setMediaPort(media_port);
		
		if (ua_jar==null) {
			if (new File("ua.jar").canRead()) ua_jar="ua.jar";
			else
			if (new File("lib/ua.jar").canRead()) ua_jar="lib/ua.jar";
			else
			if (new File("mjua.jar").canRead()) ua_jar="mjua.jar";
			else
			if (new File("lib/mjua.jar").canRead()) ua_jar="lib/mjua.jar";
		}
		else {
			if (ua_jar!=null && ua_jar.equalsIgnoreCase(Configure.NONE)) ua_jar=null;			
		}

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
		if (proxy!=null && user!=null) return new NameAddress(display_name,new SipURI(user,proxy));
		else return new NameAddress(display_name,new SipURI(user,ua_address));
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
		if (display_name==null) display_name=naddr.getDisplayName();
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
		if (auth_realm==null && proxy!=null) auth_realm=proxy;
		if (auth_realm==null && registrar!=null) auth_realm=registrar;
		if (auth_user==null && user!=null) auth_user=user;
		if (ua_address==null && sip_provider!=null) {
			ua_address=sip_provider.getViaAddress();
			if (sip_provider.getPort()!=SipStack.default_port) ua_address+=":"+sip_provider.getPort();
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
		for (int i=0; i<media_descs.length; i++) {
			MediaDesc md=(MediaDesc)media_descs[i];
			md.setPort(media_port);
			media_port+=diff;
		}
	}


	/** Gets the transport port of the first medium.
	  * @return the port number of the first medium,if any, otherwise -1 */
	public int getMediaPort() {
		if (media_descs!=null && media_descs.length>0) return ((MediaDesc)media_descs[0]).getPort();
		else return -1;
	}


	// *********************** protected methods **********************

	/** Parses a single line (loaded from the config file)
	  * @param line a string containing the pair attribute name and attribute value, separated by a "=" */
	protected void parseLine(String line) {
		String attribute;
		Parser par;
		int index=line.indexOf("=");
		if (index>0) {  attribute=line.substring(0,index).trim(); par=new Parser(line,index+1);  }
		else {  attribute=line; par=new Parser("");  }
				  
		if (attribute.equals("display_name"))   {  display_name=par.getRemainingString().trim();  return;  }
		if (attribute.equals("user"))           {  user=par.getString();  return;  }
		if (attribute.equals("proxy"))          {  proxy=par.getString();  return;  }
		if (attribute.equals("registrar"))      {  registrar=par.getString();  return;  }

		if (attribute.equals("auth_user"))      {  auth_user=par.getString();  return;  } 
		if (attribute.equals("auth_realm"))     {  auth_realm=par.getRemainingString().trim();  return;  }
		if (attribute.equals("auth_passwd"))    {  auth_passwd=par.getRemainingString().trim();  return;  }

		if (attribute.equals("ua_jar"))         {  ua_jar=par.getStringUnquoted();  return;  }      
		if (attribute.equals("res_path"))       {  res_path=par.getStringUnquoted();  return;  }      
		if (attribute.equals("media_path"))     {  media_path=par.getStringUnquoted();  return;  }      
		if (attribute.equals("buddy_list_file")){  buddy_list_file=par.getStringUnquoted();  return;  }      

		if (attribute.equals("do_register"))    {  do_register=(par.getString().toLowerCase().startsWith("y"));  return;  }
		if (attribute.equals("do_unregister"))  {  do_unregister=(par.getString().toLowerCase().startsWith("y"));  return;  }
		if (attribute.equals("do_unregister_all")) {  do_unregister_all=(par.getString().toLowerCase().startsWith("y"));  return;  }
		if (attribute.equals("expires"))        {  expires=par.getInt();  return;  } 
		if (attribute.equals("keepalive_time")) {  keepalive_time=par.getInt();  return;  } 

		if (attribute.equals("call_to")) {
			String naddr=par.getRemainingString().trim();
			if (naddr==null || naddr.length()==0 || naddr.equalsIgnoreCase(Configure.NONE)) call_to=null;
			else call_to=new NameAddress(naddr);
			return;
		}
		if (attribute.equals("redirect_to")) {
			String naddr=par.getRemainingString().trim();
			if (naddr==null || naddr.length()==0 || naddr.equalsIgnoreCase(Configure.NONE)) redirect_to=null;
			else redirect_to=new NameAddress(naddr);
			return;
		}
		if (attribute.equals("transfer_to")) {
			String naddr=par.getRemainingString().trim();
			if (naddr==null || naddr.length()==0 || naddr.equalsIgnoreCase(Configure.NONE)) transfer_to=null;
			else transfer_to=new NameAddress(naddr);
			return;
		}
		if (attribute.equals("refuse_time"))    {  refuse_time=par.getInt();  return;  }
		if (attribute.equals("accept_time"))    {  accept_time=par.getInt();  return;  }
		if (attribute.equals("hangup_time"))    {  hangup_time=par.getInt();  return;  } 
		if (attribute.equals("transfer_time"))  {  transfer_time=par.getInt();  return;  } 
		if (attribute.equals("re_invite_time")) {  re_invite_time=par.getInt();  return;  } 
		if (attribute.equals("re_call_time"))   {  re_call_time=par.getInt();  return;  } 
		if (attribute.equals("re_call_count"))  {  re_call_count=par.getInt();  return;  } 
		if (attribute.equals("no_offer"))       {  no_offer=(par.getString().toLowerCase().startsWith("y"));  return;  }
		if (attribute.equals("no_system_audio")){  no_system_audio=(par.getString().toLowerCase().startsWith("y"));  return;  }
		if (attribute.equals("no_prompt"))      {  no_prompt=(par.getString().toLowerCase().startsWith("y"));  return;  }

		if (attribute.equals("loopback"))       {  loopback=(par.getString().toLowerCase().startsWith("y"));  return;  }
		if (attribute.equals("recv_only"))      {  recv_only=(par.getString().toLowerCase().startsWith("y"));  return;  }
		if (attribute.equals("send_only"))      {  send_only=(par.getString().toLowerCase().startsWith("y"));  return;  }
		if (attribute.equals("send_tone"))      {  send_tone=(par.getString().toLowerCase().startsWith("y"));  return;  }
		if (attribute.equals("send_file"))      {  send_file=par.getRemainingString().trim();  return;  }
		if (attribute.equals("recv_file"))      {  recv_file=par.getRemainingString().trim();  return;  }
		if (attribute.equals("send_video_file")){  send_video_file=par.getRemainingString().trim();  return;  }
		if (attribute.equals("recv_video_file")){  recv_video_file=par.getRemainingString().trim();  return;  }

		if (attribute.equals("audio"))          {  audio=(par.getString().toLowerCase().startsWith("y"));  return;  }
		if (attribute.equals("video"))          {  video=(par.getString().toLowerCase().startsWith("y"));  return;  }

		if (attribute.equals("media_addr"))     {  media_addr=par.getString();  return;  } 
		if (attribute.equals("media_port"))     {  media_port=par.getInt();  return;  } 
		if (attribute.equals("symmetric_rtp"))  {  symmetric_rtp=(par.getString().toLowerCase().startsWith("y"));  return;  } 
		if (attribute.equals("media") ||
			 attribute.equals("media_desc"))    {  media_desc_vector.addElement(MediaDesc.parseMediaDesc(par.getRemainingString().trim()));  return;  }
		if (attribute.equals("media_spec"))     {  MediaSpec ms=MediaSpec.parseMediaSpec(par.getRemainingString().trim());  media_spec_table.put(ms.getType(),ms);  return;  }

		if (attribute.equals("use_jmf_audio"))  {  use_jmf_audio=(par.getString().toLowerCase().startsWith("y"));  return;  }
		if (attribute.equals("use_jmf_video"))  {  use_jmf_video=(par.getString().toLowerCase().startsWith("y"));  return;  }
		if (attribute.equals("use_rat"))        {  use_rat=(par.getString().toLowerCase().startsWith("y"));  return;  }
		if (attribute.equals("bin_rat"))        {  bin_rat=par.getStringUnquoted();  return;  }
		if (attribute.equals("use_vic"))        {  use_vic=(par.getString().toLowerCase().startsWith("y"));  return;  }
		if (attribute.equals("bin_vic"))        {  bin_vic=par.getStringUnquoted();  return;  }      

		if (attribute.equals("ua_server")) {  ua_server=(par.getString().toLowerCase().startsWith("y"));  return;  }
		if (attribute.equals("options_server")) {  options_server=(par.getString().toLowerCase().startsWith("y"));  return;  }
		if (attribute.equals("null_server")) {  null_server=(par.getString().toLowerCase().startsWith("y"));  return;  }
		if (attribute.equals("javax_sound_streamer")) {  javax_sound_streamer=par.getString();  return;  }
		if (attribute.equals("javax_sound_direct_convertion")) {  javax_sound_direct_convertion=(par.getString().toLowerCase().startsWith("y"));  return;  }
		if (attribute.equals("javax_sound_sync")) {  javax_sound_sync=(par.getString().toLowerCase().startsWith("y"));  return;  }
		if (attribute.equals("random_early_drop_rate")) {  random_early_drop_rate=par.getInt();  return;  }
		if (attribute.equals("audio_mcast_soaddr")) {  audio_mcast_soaddr=new SocketAddress(par.getString());  return;  } 
		if (attribute.equals("video_mcast_soaddr")) {  video_mcast_soaddr=new SocketAddress(par.getString());  return;  } 
	}


	/** Converts the entire object into lines (to be saved into the config file)
	  * @return just the user's URI */
	//  * @return a string representation of this object containing a sequence of lines, each of them formed by pairs of attribute name and attribute value separated by a "=" */
	protected String toLines() {
		// currently not implemented..
		return getUserURI().toString();
	}
  
}
