package org.mjsip.ua;


import org.mjsip.media.FlowSpec.Direction;
import org.mjsip.sip.address.GenericURI;
import org.mjsip.sip.address.NameAddress;
import org.mjsip.sip.address.SipURI;
import org.mjsip.sip.address.UnexpectedUriSchemeException;
import org.mjsip.sip.provider.SipOptions;
import org.zoolu.util.Configure;
import org.zoolu.util.Flags;
import org.zoolu.util.Parser;


/** {@link UserAgent} configuration options.
  */
public class UAConfig extends Configure implements UAOptions {
		 
	/** 
	 * Constructs a {@link UAConfig} from the given configuration file and program arguments.
	 */
	public static UAConfig init(String file, Flags flags, SipOptions sipConfig) {
		UAConfig result=new UAConfig();
		result.loadFile(file);
		result.updateWith(flags);
		result.normalize(sipConfig);
		return result;
	}

	/** 
	 * Constructs a {@link UAConfig} from configuration file values.
	 */
	public static UAConfig init(String file, SipOptions sipConfig) {
		UAConfig result=new UAConfig();
		result.loadFile(file);
		result.normalize(sipConfig);
		return result;
	}

	private String _displayName=null;

	private String _user=null;

	private String _proxy=null;

	private String _registrar=null;

	private String _uaAddress=null;

	private String _authUser=null;
	private String _authRealm=null;
	private String _authPasswd=null;

	private boolean _doRegister=false;

	private int _expires=3600;

	private long _keepaliveTime=0;

	private int _refuseTime=20;
	
	private boolean _noOffer=false;

	private boolean _noPrompt=false;

	private boolean _recvOnly=false;
	private boolean _sendOnly=false;
	
	private String _mediaAddr=null;

	private boolean _uaServer=true;

	private boolean _optionsServer=true;

	private boolean _nullServer=true;

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

	private boolean _javaxSoundSync=true;

	/** Constructs a {@link UAConfig} */
	private UAConfig() {
		super();
	}

	@Override
	public Direction getDirection() {
		if (isRecvOnly()) {
			return Direction.RECV_ONLY;
		} else if (isSendOnly()) {
			return Direction.SEND_ONLY;
		} else {
			return Direction.FULL_DUPLEX;
		}
	}

	/** Inits the UserAgentProfile. */
	private void normalize(SipOptions sipConfig) {
		if (getProxy()!=null && getProxy().equalsIgnoreCase(Configure.NONE)) setProxy(null);
		if (getRegistrar()!=null && getRegistrar().equalsIgnoreCase(Configure.NONE)) setRegistrar(null);
		if (getDisplayName()!=null && getDisplayName().equalsIgnoreCase(Configure.NONE)) setDisplayName(null);
		if (getUser()!=null && getUser().equalsIgnoreCase(Configure.NONE)) setUser(null);
		if (getAuthRealm()!=null && getAuthRealm().equalsIgnoreCase(Configure.NONE)) setAuthRealm(null);

		if (getRegistrar()==null && getProxy()!=null) setRegistrar(getProxy());
		if (getProxy()==null && getRegistrar()!=null) setProxy(getRegistrar());
		if (getAuthRealm()==null && getProxy()!=null) setAuthRealm(getProxy());
		if (getAuthRealm()==null && getRegistrar()!=null) setAuthRealm(getRegistrar());
		if (getAuthUser()==null && getUser()!=null) setAuthUser(getUser());
		if (getUaAddress()==null) {
			setUaAddress(sipConfig.getViaAddr());
			if (sipConfig.getHostPort()!=sipConfig.getDefaultPort()) setUaAddress(getUaAddress() + ":"+sipConfig.getHostPort());
		}
	}

	// ************************ public methods ************************

	@Override
	public NameAddress getUserURI() {
		if (getProxy()!=null && getUser()!=null) return new NameAddress(getDisplayName(),new SipURI(getUser(),getProxy()));
		else return new NameAddress(getDisplayName(),new SipURI(getUser(),getUaAddress()));
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
		if (getDisplayName()==null) setDisplayName(naddr.getDisplayName());
		if (getUser()==null) setUser(uri.getUserName());
		if (getProxy()==null) setProxy((uri.hasPort())? uri.getHost()+":"+uri.getPort() : uri.getHost());
		if (getRegistrar()==null) setRegistrar(getProxy());
	}

	// *********************** protected methods **********************

	/** Parses a single line (loaded from the config file)
	 * @param attribute The name of the option.
	 * @param par The {@link Parser} delivering the option value.
	  */
	@Override
	public void setOption(String attribute, Parser par) {
		if (attribute.equals("display_name"))   {  setDisplayName(par.getRemainingString().trim());  return;  }
		if (attribute.equals("user"))           {  setUser(par.getString());  return;  }
		if (attribute.equals("proxy"))          {  setProxy(par.getString());  return;  }
		if (attribute.equals("registrar"))      {  setRegistrar(par.getString());  return;  }

		if (attribute.equals("auth_user"))      {  setAuthUser(par.getString());  return;  } 
		if (attribute.equals("auth_realm"))     {  setAuthRealm(par.getRemainingString().trim());  return;  }
		if (attribute.equals("auth_passwd"))    {  setAuthPasswd(par.getRemainingString().trim());  return;  }

		if (attribute.equals("do_register"))    {  setRegister((par.getString().toLowerCase().startsWith("y")));  return;  }
		if (attribute.equals("expires"))        {  setExpires(par.getInt());  return;  } 
		if (attribute.equals("keepalive_time")) {  setKeepAliveTime(par.getInt());  return;  } 

		if (attribute.equals("refuse_time"))    {  setRefuseTime(par.getInt());  return;  }

		if (attribute.equals("no_offer"))       {  setNoOffer((par.getString().toLowerCase().startsWith("y")));  return;  }
		if (attribute.equals("no_prompt"))      {  setNoPrompt((par.getString().toLowerCase().startsWith("y")));  return;  }

		if (attribute.equals("recv_only"))      {  setRecvOnly((par.getString().toLowerCase().startsWith("y")));  return;  }
		if (attribute.equals("send_only"))      {  setSendOnly((par.getString().toLowerCase().startsWith("y")));  return;  }
		
		if (attribute.equals("media_addr"))     {  setMediaAddr(par.getString());  return;  } 

		if (attribute.equals("ua_server")) {  setUaServer((par.getString().toLowerCase().startsWith("y")));  return;  }
		if (attribute.equals("options_server")) {  setOptionsServer((par.getString().toLowerCase().startsWith("y")));  return;  }
		if (attribute.equals("null_server")) {  setNullServer((par.getString().toLowerCase().startsWith("y")));  return;  }
		if (attribute.equals("javax_sound_sync")) {  setJavaxSoundSync((par.getString().toLowerCase().startsWith("y")));  return;  }
	}

	/**
	 * Adds settings read from command line arguments.
	 */
	protected void updateWith(Flags flags) {
		Boolean no_prompt=flags.getBoolean("--no-prompt",null,"do not prompt");
		if (no_prompt!=null) this.setNoPrompt(no_prompt.booleanValue());
		
		int regist_time=flags.getInteger("-g","<time>",-1,"registers the contact address with the registrar server for a gven duration, in seconds");
		if (regist_time>=0) {  this.setRegister(true);  this.setExpires(regist_time);  }
		
		long keepalive_time=flags.getLong("--keep-alive","<msecs>",-1,"send keep-alive packets each given milliseconds");
		if (keepalive_time>=0) this.setKeepAliveTime(keepalive_time);
		
		Boolean no_offer=flags.getBoolean("-n",null,"no offer in invite (offer/answer in 2xx/ack)");
		if (no_offer!=null) this.setNoOffer(no_offer.booleanValue());
		
		String display_name=flags.getString("--display-name","<str>",null,"display name");
		if (display_name!=null) this.setDisplayName(display_name);
		
		String user=flags.getString("--user","<user>",null,"user name");
		if (user!=null) this.setUser(user);
		
		String proxy=flags.getString("--proxy","<proxy>",null,"proxy server");
		if (proxy!=null) this.setProxy(proxy);
		
		String registrar=flags.getString("--registrar","<registrar>",null,"registrar server");
		if (registrar!=null) this.setRegistrar(registrar);
		
		String auth_user=flags.getString("--auth-user","<user>",null,"user name used for authenticat");
		if (auth_user!=null) this.setAuthUser(auth_user);
		
		String auth_realm=flags.getString("--auth-realm","<realm>",null,"realm used for authentication");
		if (auth_realm!=null) this.setAuthRealm(auth_realm);
		
		String auth_passwd=flags.getString("--auth-passwd","<passwd>",null,"passwd used for authentication");
		if (auth_passwd!=null) this.setAuthPasswd(auth_passwd); 

		Boolean recv_only=flags.getBoolean("--recv-only",null,"receive only mode, no media is sent");
		if (recv_only!=null) this.setRecvOnly(recv_only.booleanValue());
		
		Boolean send_only=flags.getBoolean("--send-only",null,"send only mode, no media is received");
		if (send_only!=null) this.setSendOnly(send_only.booleanValue());
		
		// for backward compatibility
		String from_uri=flags.getString("--from-uri","<uri>",null,"user's address-of-record (AOR)");
		if (from_uri!=null) this.setUserURI(NameAddress.parse(from_uri));
	}

	/**
	 * Display name for the user. It is used in the user's AOR registered to the registrar server
	 * and used as From URI.
	 */
	public String getDisplayName() {
		return _displayName;
	}

	/** @see #getDisplayName() */
	public void setDisplayName(String displayName) {
		this._displayName = displayName;
	}

	@Override
	public String getUser() {
		return _user;
	}
	
	/** @see #getUser() */
	public void setUser(String user) {
		this._user = user;
	}

	@Override
	public String getProxy() {
		return _proxy;
	}

	/** @see #getProxy() */
	public void setProxy(String proxy) {
		this._proxy = proxy;
	}

	@Override
	public String getRegistrar() {
		return _registrar;
	}

	/** @see #getRegistrar() */
	public void setRegistrar(String registrar) {
		this._registrar = registrar;
	}

	/**
	 * UA address. It is the SIP address of the UA and is used to form the From URI if no proxy is
	 * configured.
	 */
	public String getUaAddress() {
		return _uaAddress;
	}

	/** @see #getUaAddress() */
	public void setUaAddress(String ua_address) {
		this._uaAddress = ua_address;
	}

	@Override
	public String getAuthUser() {
		return _authUser;
	}

	/** @see #getAuthUser() */
	public void setAuthUser(String authUser) {
		this._authUser = authUser;
	}

	@Override
	public String getAuthRealm() {
		return _authRealm;
	}

	/** @see #getAuthRealm() */
	public void setAuthRealm(String authRealm) {
		this._authRealm = authRealm;
	}

	@Override
	public String getAuthPasswd() {
		return _authPasswd;
	}

	/** @see #getAuthPasswd() */
	public void setAuthPasswd(String authPasswd) {
		this._authPasswd = authPasswd;
	}

	/** Whether registering with the registrar server */
	public boolean isRegister() {
		return _doRegister;
	}

	/** @see #isRegister() */
	public void setRegister(boolean doRegister) {
		this._doRegister = doRegister;
	}

	/** Expires time (in seconds). */
	public int getExpires() {
		return _expires;
	}

	/** @see #getExpires() */
	public void setExpires(int expires) {
		this._expires = expires;
	}

	/**
	 * Rate of keep-alive tokens (datagrams) sent toward the outbound proxy (if present) or toward
	 * the registrar server. Its value specifies the delta-time (in millesconds) between two
	 * keep-alive tokens.
	 * <p>
	 * Set keepalive_time=0 for not sending keep-alive datagrams.
	 * </p>
	 */
	public long getKeepAliveTime() {
		return _keepaliveTime;
	}

	/** @see #getKeepAliveTime() */
	public void setKeepAliveTime(long keepaliveTime) {
		_keepaliveTime = keepaliveTime;
	}

	@Override
	public int getRefuseTime() {
		return _refuseTime;
	}

	/** @see #getRefuseTime() */
	public void setRefuseTime(int refuseTime) {
		_refuseTime = refuseTime;
	}

	@Override
	public boolean getNoOffer() {
		return _noOffer;
	}

	/** @see #getNoOffer() */
	public void setNoOffer(boolean noOffer) {
		this._noOffer = noOffer;
	}

	/** Do not use prompt */
	public boolean isNoPrompt() {
		return _noPrompt;
	}

	/** @see #isNoPrompt() */
	public void setNoPrompt(boolean noPrompt) {
		this._noPrompt = noPrompt;
	}

	/** Whether playing in receive only mode */
	public boolean isRecvOnly() {
		return _recvOnly;
	}

	/** @see #isRecvOnly() */
	public void setRecvOnly(boolean recvOnly) {
		this._recvOnly = recvOnly;
	}

	/** Whether playing in send only mode */
	public boolean isSendOnly() {
		return _sendOnly;
	}

	/** @see #isSendOnly() */
	public void setSendOnly(boolean sendOnly) {
		this._sendOnly = sendOnly;
	}

	@Override
	public String getMediaAddr() {
		return _mediaAddr;
	}

	/** @see #getMediaAddr() */
	public void setMediaAddr(String mediaAddr) {
		_mediaAddr = mediaAddr;
	}

	@Override
	public boolean isUaServer() {
		return _uaServer;
	}

	/** @see #isUaServer() */
	public void setUaServer(boolean uaServer) {
		_uaServer = uaServer;
	}

	@Override
	public boolean isOptionsServer() {
		return _optionsServer;
	}

	/** @see #isOptionsServer() */
	public void setOptionsServer(boolean optionsServer) {
		_optionsServer = optionsServer;
	}

	@Override
	public boolean isNullServer() {
		return _nullServer;
	}

	/** @see #isNullServer() */
	public void setNullServer(boolean nullServer) {
		_nullServer = nullServer;
	}

	/**
	 * Whether enforcing time synchronization to RTP source stream. If synchronization is
	 * explicitly performed, the departure time of each RTP packet is equal to its nominal time.
	 * <p>
	 * Note that when using audio capturing, synchronization with the sample rate is implicitly
	 * performed by the audio capture device and frames are read at constant bit rate. However, an
	 * explicit re-synchronization is suggested in order to let the read() method be non-blocking
	 * (in the other case the UA audio performance seems decreasing.
	 * </p>
	 */
	public boolean isJavaxSoundSync() {
		return _javaxSoundSync;
	}

	/** @see #isJavaxSoundSync() */
	public void setJavaxSoundSync(boolean javaxSoundSync) {
		_javaxSoundSync = javaxSoundSync;
	}

}
