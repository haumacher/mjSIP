package local.ua;



import local.media.MediaDesc;
import local.media.MediaSpec;
import org.zoolu.net.SocketAddress;
import org.zoolu.sip.address.*;
import org.zoolu.sip.provider.SipStack;
import org.zoolu.sip.provider.SipProvider;
import org.zoolu.tools.Configure;
import org.zoolu.tools.MultiTable;
import org.zoolu.tools.Parser;

import java.util.Vector;



/** UserAgentProfile maintains the user configuration.
  */
public class UserAgentProfile extends Configure
{
   /** The default configuration file */
   private static String config_file="mjsip.cfg";

       
   // ********************** user configurations *********************

   /** Display name for the user.
     * It is used in the user's AOR registered to the registrar server
     * and used as From URL. */
   public String display_name=null;

   /** User's name.
     * It is used to build the user's AOR registered to the registrar server
     * and used as From URL. */
   public String user=null;

   /** Fully qualified domain name (or address) of the proxy server.
     * It is part of the user's AOR registered to the registrar server
     * and used as From URL.
     * <p/>
     * If <i>proxy</i> is not defined, the <i>registrar</i> value is used in its place.
     * <p/>
     * If <i>registrar</i> is not defined, the <i>proxy</i> value is used in its place. */
   public String proxy=null;

   /** Fully qualified domain name (or address) of the registrar server.
     * It is used as recipient for REGISTER requests.
     * <p/>
     * If <i>registrar</i> is not defined, the <i>proxy</i> value is used in its place.
     * <p/>
     * If <i>proxy</i> is not defined, the <i>registrar</i> value is used in its place. */
   public String registrar=null;

   /** UA address.
     * It is the SIP address of the UA and is used to form the From URL if no proxy is configured. */
   public String ua_address=null;

   /** User's name used for server authentication. */
   public String auth_user=null;
   /** User's realm used for server authentication. */
   public String auth_realm=null;
   /** User's passwd used for server authentication. */
   public String auth_passwd=null;

   /** Absolute path (or complete URL) of the jar archive, where various UA media (gif, wav, etc.) are stored.
     * Use value 'NONE' for getting resources from external folders.
     * By default, the file "lib/ua.jar" is used. */
   public static String ua_jar="lib/ua.jar";

   /** Absolute path (or complete URL) of the media resources (gif, wav, etc.), used by the UA.
     * By default, the folder "media/local/ua/" is used. */
   public static String media_path="media/local/ua/";

   /** Absolute path (or complete URL) of the buddy list file where the buddy list is and loaded from (and saved to).
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
     * keep-alive tokens. <br/>
     * Set keepalive_time=0 for not sending keep-alive datagrams. */
   public long keepalive_time=0;

   /** Automatic call a remote user secified by the 'call_to' value.
     * Use value 'NONE' for manual calls (or let it undefined).  */
   public NameAddress call_to=null;
         
   /** Response time in seconds; it is the maximum time the user can wait before responding to an incoming call; after such time the call is automatically declined (refused). */
   public int refuse_time=20;
   /** Automatic answer time in seconds; time<0 corresponds to manual answer mode. */
   public int accept_time=-1;        
   /** Automatic hangup time (call duartion) in seconds; time<=0 corresponds to manual hangup mode. */
   public int hangup_time=-1;
   /** Automatic call transfer time in seconds; time<0 corresponds to no auto transfer mode. */
   public int transfer_time=-1;
   /** Automatic re-inviting time in seconds; time<0 corresponds to no auto re-invite mode.  */
   public int re_invite_time=-1;

   /** Redirect incoming call to the secified url.
     * Use value 'NONE' for not redirecting incoming calls (or let it undefined). */
   public NameAddress redirect_to=null;

   /** Transfer calls to the secified url.
     * Use value 'NONE' for not transferring calls (or let it undefined). */
   public NameAddress transfer_to=null;

   /** No offer in the invite */
   public boolean no_offer=false;
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
   public int media_port=-1;

   /** Whether using symmetric_rtp */
   public boolean symmetric_rtp=false;

   /** Vector of media descriptions (MediaDesc) */
   public Vector media_descs=new Vector();

   /** MultiTable of media specifications, as multiple-values table of (String)media-->(MediaSpec)media_spec */
   private MultiTable media_spec_mtable=new MultiTable();

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

   /** Alternative javax-sound-based audio application (currently just for tests) */
   public String javax_sound_app=null;

   /** Whether using explicit external converter (i.e. direct access to an external conversion provider) instead of that provided by javax.sound.sampled.spi. It applies only when javax sound is used, that is when no other audio apps (such as jmf or rat) are used.  */
   public boolean javax_sound_direct_convertion=false;

   /** Sender synchronization adjustment, that is the time (in milliseconds) that a frame
     * should be sent in advance by the RTP sender, before the nominal time.
     * A value less that 0 means no re-synchronization explicitely performed by the RTP sender.
     * <p/>
     * Note that when using audio capturing, synchronization with the sample rate
     * is implicitely performed by the audio capture device and frames are read at constat bit rate.
     * However, a value of this parameter >=0 (explicit re-synchronization) is suggested
     * in order to let the read() method be non-blocking (in the other case
     * the UA audio performances seem to decrease). */
   //public int javax_sound_sync_adj=2;

   /** Whether enforcing time synchronization to RTP source stream.
     * If synchronization is explicitely performed, the depature time of each RTP packet is equal to its nominal time.
     * <p/>
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


   // ********************* historical parametes *********************

   /** Default audio port */
   private int audio_port=4000;
   /** Default video port */
   private int video_port=4002;

   /** Default audio avp */
   private int audio_avp0=0;
   /** Default audio codec */
   private String audio_codec0="PCMU";
   /** Default audio sample rate */
   private int audio_sample_rate0=8000;
   /** Default audio packet size */
   private int audio_packet_size0=320;
   /** Default video avp */
   private int video_avp0=101;

   /** Audio types (as Vector of Strings) */
   private Vector audio_type_list=new Vector();
   /** Audio types (all in one String) */
   private String audio_types=null;
      
   /** Video types (as Vector of Strings) */
   private Vector video_type_list=new Vector();
   /** Video types (all in one String) */
   private String video_types=null;

   /** Whether using JMF for audio/video streaming */
   private boolean use_jmf=false;


   // ************************** costructors *************************
   
   /** Costructs a void UserAgentProfile */
   public UserAgentProfile()
   {  init();
   }

   /** Costructs a new UserAgentProfile */
   public UserAgentProfile(String file)
   {  // load configuration
      loadFile(file);
      // post-load manipulation     
      init();
   }

   /** Inits the UserAgentProfile. */
   private void init()
   {  if (proxy!=null && proxy.equalsIgnoreCase(Configure.NONE)) proxy=null;
      if (registrar!=null && registrar.equalsIgnoreCase(Configure.NONE)) registrar=null;
      if (display_name!=null && display_name.equalsIgnoreCase(Configure.NONE)) display_name=null;
      if (user!=null && user.equalsIgnoreCase(Configure.NONE)) user=null;
      if (auth_realm!=null && auth_realm.equalsIgnoreCase(Configure.NONE)) auth_realm=null;
      if (send_file!=null && send_file.equalsIgnoreCase(Configure.NONE)) send_file=null;
      if (recv_file!=null && recv_file.equalsIgnoreCase(Configure.NONE)) recv_file=null;

      // media descrptions
      // BEGIN BACKWARD COMPATIBILITY
      if (media_descs.size()==0)
      {  if (audio_types!=null)
         {  Parser par=new Parser(audio_types);
            if (par.indexOf('{')>=0) par.goTo('{').skipChar();
            while (par.hasMore())
            {  char[] delim={ ',' , ';' , '}' };
               String audio_type=par.getWord(delim).trim();
               if (audio_type.length()>0) audio_type_list.addElement(audio_type);
               par.skipChar();
            }
         }
         if (audio_type_list.size()>0)
         {  Vector audio_specs=new Vector(audio_type_list.size());
            for (int i=0; i<audio_type_list.size(); i++) audio_specs.addElement(MediaSpec.parseMediaSpec("audio "+(String)audio_type_list.elementAt(i)));
            media_descs.addElement(new MediaDesc("audio",audio_port,"RTP/AVP",audio_specs));
         }
         if (video_types!=null)
         {  Parser par=new Parser(video_types);
            if (par.indexOf('{')>=0) par.goTo('{').skipChar();
            while (par.hasMore())
            {  char[] delim={ ',' , ';' , '}' };
               String video_type=par.getWord(delim).trim();
               if (video_type.length()>0) video_type_list.addElement(video_type);
            }
         }
         if (video_type_list.size()>0)
         {  Vector video_specs=new Vector(video_type_list.size());
            for (int i=0; i<video_type_list.size(); i++) video_specs.addElement(MediaSpec.parseMediaSpec("video "+(String)video_type_list.elementAt(i)));
            media_descs.addElement(new MediaDesc("video",video_port,"RTP/AVP",video_specs));
         }
      }
      if (media_descs.size()==0)
      {  Vector audio_specs=new Vector(1);
         audio_specs.addElement(new MediaSpec("audio",audio_avp0,audio_codec0,audio_sample_rate0,audio_packet_size0));
         media_descs.addElement(new MediaDesc("audio",audio_port,"RTP/AVP",audio_specs));
         //Vector video_specs=new Vector(1);
         //video_specs.addElement(new MediaSpec("video",video_avp0,null,-1,-1));
         //media_descs.addElement(new MediaDesc("video",video_port,"rtp/avp",video_specs));
      }
      if (!use_jmf_audio && !use_jmf_video) use_jmf_audio=use_jmf_video=use_jmf;
      // END BACKWARD COMPATIBILITY

      // BEGIN PACH FOR JMF SUPPORT
      if (audio && use_jmf_audio)
      {  media_spec_mtable.remove("audio");
         media_spec_mtable.put("audio",new MediaSpec("audio",11,"L16",16000,320));
      }
      else
      if (video && use_jmf_video)
      {  media_spec_mtable.remove("video");
         media_spec_mtable.put("video",new MediaSpec("video",101,null,-1,-1));
      }
      // END PACH FOR JMF SUPPORT
      for (int i=0; i<media_descs.size(); i++)
      {  MediaDesc md=(MediaDesc)media_descs.elementAt(i);
         Vector media_specs=media_spec_mtable.get(md.getMedia());
         if (media_specs!=null) for (int j=0; j<media_specs.size(); j++) md.addMediaSpec((MediaSpec)media_specs.elementAt(j));
      }
      
      if (ua_jar!=null && ua_jar.equalsIgnoreCase(Configure.NONE)) ua_jar=null;

      setUnconfiguredAttributes(null);
   }


   // ************************ public methods ************************

   /** Gets the user's AOR (Address Of Record) registered to the registrar server
     * and used as From URL.
     * <p/>
     * In case of <i>proxy</i> and <i>user</i> parameters have been defined
     * it is formed as "<i>display_name</i>"&lt;sip:<i>user</i>@<i>proxy</i>&rt,
     * otherwhise the local UA address (obtained by the SipProvider) is used. */
   public NameAddress getUserURI()
   {  if (proxy!=null && user!=null) return new NameAddress(display_name,new SipURL(user,proxy));
      else return new NameAddress(display_name,new SipURL(user,ua_address));
   }

   /** Sets the user's AOR (Address Of Record) registered to the registrar server
     * and used as From URL.
     * <p/>
     * It actually sets the <i>display_name</i>, <i>user</i>, and <i>proxy</i> parameters.
     * <p/>
     * If <i>registrar</i> is not defined, the <i>proxy</i> value is used in its place. */
   public void setUserURI(NameAddress naddr)
   {  SipURL url=naddr.getAddress();
      if (display_name==null) display_name=naddr.getDisplayName();
      if (user==null) user=url.getUserName();
      if (proxy==null) proxy=(url.hasPort())? url.getHost()+":"+url.getPort() : url.getHost();
      if (registrar==null) registrar=proxy;
   }

   /** Sets server and authentication attributes (if not already done).
     * It actually sets <i>ua_address</i>, <i>registrar</i>, <i>proxy</i>, <i>auth_realm</i>,
     * and <i>auth_user</i> attributes.
     * <p/>
     * Note: this method sets such attributes only if they haven't still been initilized. */
   public void setUnconfiguredAttributes(SipProvider sip_provider)
   {  if (registrar==null && proxy!=null) registrar=proxy;
      if (proxy==null && registrar!=null) proxy=registrar;
      if (auth_realm==null && proxy!=null) auth_realm=proxy;
      if (auth_realm==null && registrar!=null) auth_realm=registrar;
      if (auth_user==null && user!=null) auth_user=user;
      if (ua_address==null && sip_provider!=null)
      {  ua_address=sip_provider.getViaAddress();
         if (sip_provider.getPort()!=SipStack.default_port) ua_address+=":"+sip_provider.getPort();
      }
   }

   /** Inits contact and secure contact addresses (if not already done)
     * according to the specified sip provider.
     * It actually initializes <i>contact_url</i> and <i>secure_contact_url</i> attributes.
     * <p/>
     * Note: this method sets such attributes only if they haven't still been initilized. */
   /*public void initUnconfiguredContactAddress(SipProvider sip_provider)
   {  if (contact_url==null) contact_url=getContactAddress(sip_provider);
      if (secure_contact_url==null) secure_contact_url=getSecureContactAddress(sip_provider);
   }*/

   /** Updates contact and secure contact addresses according to the specified sip provider.
     * It actually sets <i>contact_url</i> and <i>secure_contact_url</i> attributes. */
   /*public void updateContactAddress(SipProvider sip_provider)
   {  contact_url=getContactAddress(sip_provider);
      secure_contact_url=getSecureContactAddress(sip_provider);
   }*/

   // ************************ private methods ***********************

   /** Gets a valid address with username and transport information. */
   /*private NameAddress getContactAddress(SipProvider sip_provider)
   {  SipURL url=(sip_provider.getPort()!=SipStack.default_port)? new SipURL(user,sip_provider.getViaAddress(),sip_provider.getPort()) : new SipURL(user,sip_provider.getViaAddress());
      if (!sip_provider.hasTransport(SipProvider.PROTO_UDP)) url.addTransport(sip_provider.getDefaultTransport());
      return new NameAddress(url);
   }*/

   /** Gets a valid secure address with username and transport information. */
   /*private NameAddress getSecureContactAddress(SipProvider sip_provider)
   {  if (sip_provider.hasTransport(SipProvider.PROTO_TLS))
      {  SipURL url=(sip_provider.getTlsPort()!=SipStack.default_tls_port)? new SipURL(user,sip_provider.getViaAddress(),sip_provider.getTlsPort()) : new SipURL(user,sip_provider.getViaAddress()); 
         url.setSecure(true);
         return new NameAddress(url);
      }
      else return null;
   }*/

   // *********************** protected methods **********************

   /** Parses a single line (loaded from the config file) */
   protected void parseLine(String line)
   {  String attribute;
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
      if (attribute.equals("media_path"))     {  media_path=par.getStringUnquoted();  return;  }      
      if (attribute.equals("buddy_list_file")){  buddy_list_file=par.getStringUnquoted();  return;  }      

      if (attribute.equals("do_register"))    {  do_register=(par.getString().toLowerCase().startsWith("y"));  return;  }
      if (attribute.equals("do_unregister"))  {  do_unregister=(par.getString().toLowerCase().startsWith("y"));  return;  }
      if (attribute.equals("do_unregister_all")) {  do_unregister_all=(par.getString().toLowerCase().startsWith("y"));  return;  }
      if (attribute.equals("expires"))        {  expires=par.getInt();  return;  } 
      if (attribute.equals("keepalive_time")) {  keepalive_time=par.getInt();  return;  } 

      if (attribute.equals("call_to"))
      {  String naddr=par.getRemainingString().trim();
         if (naddr==null || naddr.length()==0 || naddr.equalsIgnoreCase(Configure.NONE)) call_to=null;
         else call_to=new NameAddress(naddr);
         return;
      }
      if (attribute.equals("redirect_to"))
      {  String naddr=par.getRemainingString().trim();
         if (naddr==null || naddr.length()==0 || naddr.equalsIgnoreCase(Configure.NONE)) redirect_to=null;
         else redirect_to=new NameAddress(naddr);
         return;
      }
      if (attribute.equals("transfer_to"))
      {  String naddr=par.getRemainingString().trim();
         if (naddr==null || naddr.length()==0 || naddr.equalsIgnoreCase(Configure.NONE)) transfer_to=null;
         else transfer_to=new NameAddress(naddr);
         return;
      }
      if (attribute.equals("refuse_time"))    {  refuse_time=par.getInt();  return;  }
      if (attribute.equals("accept_time"))    {  accept_time=par.getInt();  return;  }
      if (attribute.equals("hangup_time"))    {  hangup_time=par.getInt();  return;  } 
      if (attribute.equals("transfer_time"))  {  transfer_time=par.getInt();  return;  } 
      if (attribute.equals("re_invite_time")) {  re_invite_time=par.getInt();  return;  } 
      if (attribute.equals("no_offer"))       {  no_offer=(par.getString().toLowerCase().startsWith("y"));  return;  }
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
          attribute.equals("media_desc"))     {  media_descs.addElement(MediaDesc.parseMediaDesc(par.getRemainingString().trim()));  return;  }
      if (attribute.equals("media_spec"))     {  MediaSpec ms=MediaSpec.parseMediaSpec(par.getRemainingString().trim());  media_spec_mtable.put(ms.getType(),ms);  return;  }

      if (attribute.equals("use_jmf_audio"))  {  use_jmf_audio=(par.getString().toLowerCase().startsWith("y"));  return;  }
      if (attribute.equals("use_jmf_video"))  {  use_jmf_video=(par.getString().toLowerCase().startsWith("y"));  return;  }
      if (attribute.equals("use_rat"))        {  use_rat=(par.getString().toLowerCase().startsWith("y"));  return;  }
      if (attribute.equals("bin_rat"))        {  bin_rat=par.getStringUnquoted();  return;  }
      if (attribute.equals("use_vic"))        {  use_vic=(par.getString().toLowerCase().startsWith("y"));  return;  }
      if (attribute.equals("bin_vic"))        {  bin_vic=par.getStringUnquoted();  return;  }      

      if (attribute.equals("ua_server")) {  ua_server=(par.getString().toLowerCase().startsWith("y"));  return;  }
      if (attribute.equals("options_server")) {  options_server=(par.getString().toLowerCase().startsWith("y"));  return;  }
      if (attribute.equals("null_server")) {  null_server=(par.getString().toLowerCase().startsWith("y"));  return;  }
      if (attribute.equals("javax_sound_app")) {  javax_sound_app=par.getString();  return;  }
      if (attribute.equals("javax_sound_direct_convertion")) {  javax_sound_direct_convertion=(par.getString().toLowerCase().startsWith("y"));  return;  }
      if (attribute.equals("javax_sound_sync")) {  javax_sound_sync=(par.getString().toLowerCase().startsWith("y"));  return;  }
      if (attribute.equals("random_early_drop_rate")) {  random_early_drop_rate=par.getInt();  return;  }
      if (attribute.equals("audio_mcast_soaddr")) {  audio_mcast_soaddr=new SocketAddress(par.getString());  return;  } 
      if (attribute.equals("video_mcast_soaddr")) {  video_mcast_soaddr=new SocketAddress(par.getString());  return;  } 

      // for backward compatibily
      if (attribute.equals("from_url"))         {  setUserURI(new NameAddress(par.getRemainingString().trim()));  return;  } 
      if (attribute.equals("contact_user"))     {  user=par.getString();  return;  } 
      if (attribute.equals("auto_accept"))      {  accept_time=((par.getString().toLowerCase().startsWith("y")))? 0 : -1;  return;  } 
      if (attribute.equals("contacts_file"))    {  buddy_list_file=par.getStringUnquoted();  return;  }      

      if (attribute.equals("audio_port"))     {  audio_port=par.getInt();  return;  } 
      if (attribute.equals("video_port"))     {  video_port=par.getInt();  return;  } 
      if (attribute.equals("audio_type"))     {  audio_type_list.addElement(par.getRemainingString().trim());  return;  }
      if (attribute.equals("video_type"))     {  video_type_list.addElement(par.getRemainingString().trim());  return;  }
      if (attribute.equals("audio_types"))    {  audio_types=par.getRemainingString().trim();  return;  }
      if (attribute.equals("video_types"))    {  video_types=par.getRemainingString().trim();  return;  }

      if (attribute.equals("audio_avp"))        {  audio_avp0=par.getInt();  return;  } 
      if (attribute.equals("audio_codec"))      {  audio_codec0=par.getString();  return;  } 
      if (attribute.equals("audio_sample_rate")){  audio_sample_rate0=par.getInt();  return;  } 
      if (attribute.equals("audio_packet_size")){  audio_packet_size0=par.getInt();  return;  } 
      if (attribute.equals("video_avp"))        {  video_avp0=par.getInt();  return;  } 

      if (attribute.equals("audio_frame_size")) {  audio_packet_size0=par.getInt();  return;  } 
      if (attribute.equals("javax_sound_launcher")) {  javax_sound_app=par.getString();  return;  }
      if (attribute.equals("javax_sound_sync_adj")) {  javax_sound_sync=(par.getInt()>=0);  return;  }
      if (attribute.equals("use_jmf"))        {  use_jmf=(par.getString().toLowerCase().startsWith("y"));  return;  }

      // old parameters
      if (attribute.equals("contact_url")) System.err.println("WARNING: parameter 'contact_url' is no more supported.");
      if (attribute.equals("secure_contact_url")) System.err.println("WARNING: parameter 'secure_contact_url' is no more supported.");
   }


   /** Converts the entire object into lines (to be saved into the config file) */
   protected String toLines()
   {  // currently not implemented..
      return getUserURI().toString();
   }
  
}
