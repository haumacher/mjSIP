package org.mjsip.ua;


import org.kohsuke.args4j.Option;
import org.mjsip.config.YesNoHandler;
import org.mjsip.media.FlowSpec.Direction;
import org.mjsip.sip.address.NameAddress;
import org.mjsip.sip.address.SipURI;
import org.mjsip.sip.config.SipURIHandler;
import org.mjsip.sip.provider.SipOptions;
import org.zoolu.util.Configure;


/** {@link UserAgent} configuration options.
  */
public class UAConfig implements UAOptions {

	@Option(name = "--display-name", metaVar = "<name>", usage = "The display name of the user.")
	private String _displayName=null;

	@Option(name = "--sip-user", metaVar = "<name>", usage = "The user ID to register at the registrar.")
	private String _sipUser="alice";

	@Option(name = "--proxy", usage = "Proxy server to use.")
	private String _proxy=null;

	@Option(name = "--registrar", usage = "Registrar server.", handler = SipURIHandler.class)
	private SipURI _registrar=null;

	@Option(name = "--route", usage = "Additional URI for routing the registration messages.", handler = SipURIHandler.class)
	private SipURI _route=null;
	
	@Option(name = "--address")
	private String _uaAddress=null;

	@Option(name = "--auth-user", usage = "User name used for authentication.")
	private String _authUser=null;

	@Option(name = "--auth-realm", usage = "Realm used for authentication.")
	private String _authRealm=null;

	@Option(name = "--auth-passwd", usage = "Password used for authentication.")
	private String _authPasswd=null;

	@Option(name = "--do-register", handler = YesNoHandler.class)
	private boolean _doRegister=true;

	@Option(name = "--expires", usage = "Registers the contact address with the registrar server for a gven duration in seconds.")
	private int _expires=3600;

	@Option(name = "--keep-alive", usage = "Send keep-alive packets each given milliseconds.")
	private long _keepaliveTime=0;

	@Option(name = "--refuse-after")
	private int _refuseTime=20;
	
	@Option(name = "--no-offer", handler = YesNoHandler.class, usage = "Send no offer in response to invite (offer/answer in 2xx/ack).")
	private boolean _noOffer=false;

	@Option(name = "--no-prompt", handler = YesNoHandler.class, usage = "Do not prompt.")
	private boolean _noPrompt=false;

	@Option(name = "--receive-only", handler = YesNoHandler.class, usage = "Receive only mode, no media is sent.")
	private boolean _recvOnly=false;
	
	@Option(name = "--send-only", handler = YesNoHandler.class, usage = "Send only mode, no media is received.")
	private boolean _sendOnly=false;
	
	@Option(name = "--media-address")
	private String _mediaAddr=null;

	@Option(name = "--accept-invite", handler = YesNoHandler.class)
	private boolean _uaServer=true;

	@Option(name = "--options-server", handler = YesNoHandler.class)
	private boolean _optionsServer=true;

	@Option(name = "--null-server", handler = YesNoHandler.class)
	private boolean _nullServer=true;

	/** Sender synchronization adjustment, that is the time (in milliseconds) that a frame
	  * should be sent in advance by the RTP sender, before the nominal time.
	  * A value less that 0 means no re-synchronization explicitly performed by the RTP sender.
	  * <p>
	  * Note that when using audio capturing, synchronization with the sample rate
	  * is implicitly performed by the audio capture device and frames are read at constant bit rate.
	  * However, a value of this parameter >=0 (explicit re-synchronization) is suggested
	  * in order to let the read() method be non-blocking (in the other case
	  * the UA audio performances seem to decrease). */
	//public int javax_sound_sync_adj=2;

	@Option(name = "--sound-sync")
	private boolean _javaxSoundSync=true;

	/** Constructs a {@link UAConfig} */
	public UAConfig() {
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
	public void normalize(SipOptions sipConfig) {
		if (getProxy()!=null && getProxy().equalsIgnoreCase(Configure.NONE)) setProxy(null);
		if (getDisplayName()!=null && getDisplayName().equalsIgnoreCase(Configure.NONE)) setDisplayName(null);
		if (getSipUser()!=null && getSipUser().equalsIgnoreCase(Configure.NONE)) setSipUser(null);
		if (getAuthRealm()!=null && getAuthRealm().equalsIgnoreCase(Configure.NONE)) setAuthRealm(null);

		if (getRegistrar()==null && getProxy()!=null) setRegistrar(new SipURI(getProxy()));
		if (getProxy()==null && getRegistrar()!=null) setProxy(getRegistrar().getHost());
		if (getAuthRealm()==null && getProxy()!=null) setAuthRealm(getProxy());
		if (getAuthRealm()==null && getRegistrar()!=null) setAuthRealm(getRegistrar().getHost());
		if (getAuthUser()==null && getSipUser()!=null) setAuthUser(getSipUser());

		if (isRegister() && getRegistrar() == null) {
			throw new IllegalArgumentException("Registrar is required, when registering is enabled.");
		}
		
		String uaAddress = getUaAddress();
		if (uaAddress==null) {
			if (sipConfig.getHostPort() > 0 && sipConfig.getHostPort() != sipConfig.getDefaultPort()) {
				setUaAddress(new SipURI(sipConfig.getViaAddr(), sipConfig.getHostPort()).toString());
			} else {
				setUaAddress(sipConfig.getViaAddr());
			}
		}
	}

	// ************************ public methods ************************

	@Override
	public NameAddress getUserURI() {
		if (getProxy()!=null && getSipUser()!=null) {
			return new NameAddress(getDisplayName(),new SipURI(getSipUser(),getProxy()));
		} else {
			return new NameAddress(getDisplayName(),new SipURI(getSipUser(),getUaAddress()));
		}
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
	public String getSipUser() {
		return _sipUser;
	}
	
	/** @see #getUser() */
	public void setSipUser(String sipUser) {
		this._sipUser = sipUser;
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
	public SipURI getRegistrar() {
		return _registrar;
	}

	/** @see #getRegistrar() */
	public void setRegistrar(SipURI registrar) {
		this._registrar = registrar;
	}

	@Override
	public SipURI getRoute() {
		return _route;
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

	@Override
	public boolean isRegister() {
		return _doRegister;
	}

	/** @see #isRegister() */
	public void setRegister(boolean doRegister) {
		this._doRegister = doRegister;
	}

	@Override
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
