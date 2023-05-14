/*
 * Copyright (C) 2008 Luca Veltri - University of Parma - Italy
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

package local.ua;



import local.media.*;
import org.zoolu.sip.call.*;
import org.zoolu.sip.address.*;
import org.zoolu.sip.provider.*;
import org.zoolu.sip.message.Message;
import org.zoolu.sdp.*;
import org.zoolu.net.SocketAddress;
import org.zoolu.tools.*;

import java.util.Enumeration;
import java.util.Vector;



/** Simple SIP call agent (signaling and media).
  * It supports both audio and video sessions, by means of embedded media applications
  * that can use the default Java sound support (javax.sound.sampled.AudioSystem)
  * and/or the Java Multimedia Framework (JMF).
  * <p>
  * As media applications it can also use external audio/video tools.
  * Currently only support for RAT (Robust Audio Tool) and VIC has been implemented.
  */
public class UserAgent extends CallListenerAdapter implements CallWatcherListener, RegistrationClientListener, TimerListener
{

   /** On wav file */
   static final String CLIP_ON="on.wav";
   /** Off wav file */
   static final String CLIP_OFF="off.wav";
   /** Ring wav file */
   static final String CLIP_RING="ring.wav";
   /** Progress wav file */
   static final String CLIP_PROGRESS="progress.wav";


   // ***************************** attributes ****************************

   /** Log */
   Log log;
   
   /** UserAgentProfile */
   protected UserAgentProfile ua_profile;

   /** SipProvider */
   protected SipProvider sip_provider;

   /** RegistrationClient */
   protected RegistrationClient rc=null;

   /** SipKeepAlive daemon */
   protected SipKeepAlive keep_alive;

   /** Call */
   protected ExtendedCall call;
   /** Call transfer */
   protected ExtendedCall call_transfer;

   /** UAS */
   protected CallWatcher ua_server;

   /** OptionsServer */
   protected OptionsServer options_server;

   /** NotImplementedServer */
   protected NotImplementedServer null_server;

   /** MediaAgent */
   MediaAgent media_agent;
   
   /** List of active media sessions */
   protected Vector media_sessions=new Vector();

   /** Current local media descriptions, as Vector of MediaDesc */
   protected Vector media_descs=null;

   /** UserAgent listener */
   protected UserAgentListener listener=null;

   /** Response timeout */
   Timer response_to=null;

   /** Whether the outgoing call is already in progress */
   boolean progress;   
   /** Whether the outgoing call is already ringing */
   boolean ringing;

   /** On sound */
   AudioClipPlayer clip_on;
   /** Off sound */
   AudioClipPlayer clip_off;
   /** Ring sound */
   AudioClipPlayer clip_ring;
   /** Progress sound */
   AudioClipPlayer clip_progress;

   // for JSE
   /** On volume gain */
   float clip_on_volume_gain=(float)0.0; // not changed
   /** Off volume gain */
   float clip_off_volume_gain=(float)0.0; // not changed
   /** Ring volume gain */
   float clip_ring_volume_gain=(float)0.0; // not changed
   /** Progress volume gain */
   float clip_progress_volume_gain=(float)0.0; // not changed
   // for JME
   /** On volume (in the range [0-100]) */
   int clip_on_volume=5;
   /** Off volume (in the range [0-100]) */
   int clip_off_volume=5;
   /** Ring volume (in the range [0-100]) */
   int clip_ring_volume=30;
   /** Progress volume (in the range [0-100]) */
   int clip_progress_volume=30;


   // **************************** constructors ***************************

   /** Creates a new UserAgent. */
   public UserAgent(SipProvider sip_provider, UserAgentProfile ua_profile, UserAgentListener listener)
   {  init(sip_provider,ua_profile,listener);
   } 


   // ************************** private methods **************************

   /** Inits the UserAgent */
   private void init(SipProvider sip_provider, UserAgentProfile ua_profile, UserAgentListener listener)
   {  this.sip_provider=sip_provider;
      log=sip_provider.getLog();
      this.listener=listener;
      this.ua_profile=ua_profile;
      // update user profile information
      ua_profile.setUnconfiguredAttributes(sip_provider);

      // log main config parameters
      printLog("ua_address: "+ua_profile.ua_address,Log.LEVEL_MEDIUM);
      printLog("user's uri: "+ua_profile.getUserURI(),Log.LEVEL_MEDIUM);
      printLog("proxy: "+ua_profile.proxy,Log.LEVEL_MEDIUM);
      printLog("registrar: "+ua_profile.registrar,Log.LEVEL_MEDIUM);
      printLog("auth_realm: "+ua_profile.auth_realm,Log.LEVEL_MEDIUM);
      printLog("auth_user: "+ua_profile.auth_user,Log.LEVEL_MEDIUM);
      printLog("auth_passwd: ******",Log.LEVEL_MEDIUM);
      printLog("audio: "+ua_profile.audio,Log.LEVEL_MEDIUM);
      printLog("video: "+ua_profile.video,Log.LEVEL_MEDIUM);
      for (int i=0; i<ua_profile.media_descs.size(); i++)
      {  printLog("media: "+((MediaDesc)ua_profile.media_descs.elementAt(i)).toString());
      }
      // log other config parameters
      printLog("loopback: "+ua_profile.loopback,Log.LEVEL_LOWER);
      printLog("send_only: "+ua_profile.send_only,Log.LEVEL_LOWER);
      printLog("recv_only: "+ua_profile.recv_only,Log.LEVEL_LOWER);
      printLog("send_file: "+ua_profile.send_file,Log.LEVEL_LOWER);
      printLog("recv_file: "+ua_profile.recv_file,Log.LEVEL_LOWER);
      printLog("send_tone: "+ua_profile.send_tone,Log.LEVEL_LOWER);

      // start call server (that corrisponds to the UAS part)
      if (ua_profile.ua_server) ua_server=new CallWatcher(sip_provider,this);
      
      // start OPTIONS server
      if (ua_profile.options_server) options_server=new OptionsServer(sip_provider,"INVITE, ACK, CANCEL, OPTIONS, BYE","application/sdp");

      // start "Not Implemented" server
      if (ua_profile.null_server) null_server=new NotImplementedServer(sip_provider);

      // init media agent
      media_agent=new MediaAgent(ua_profile,log);

      // load sounds
      // ################# patch to make rat working.. #################
      // in case of rat, do not load and play audio clips
      if (!ua_profile.use_rat)
      {  try
         {  String jar_file=ua_profile.ua_jar;
            if (jar_file!=null)
            {  clip_on=new AudioClipPlayer(Archive.getJarURL(jar_file,ua_profile.media_path+CLIP_ON),null);
               clip_off=new AudioClipPlayer(Archive.getJarURL(jar_file,ua_profile.media_path+CLIP_OFF),null);
               clip_ring=new AudioClipPlayer(Archive.getJarURL(jar_file,ua_profile.media_path+CLIP_RING),null);
               clip_progress=new AudioClipPlayer(Archive.getJarURL(jar_file,ua_profile.media_path+CLIP_PROGRESS),null);
            }
            else
            {  clip_on=new AudioClipPlayer(Archive.getURL(ua_profile.media_path+CLIP_ON),null);
               clip_off=new AudioClipPlayer(Archive.getURL(ua_profile.media_path+CLIP_OFF),null);
               clip_ring=new AudioClipPlayer(Archive.getURL(ua_profile.media_path+CLIP_RING),null);
               clip_progress=new AudioClipPlayer(Archive.getURL(ua_profile.media_path+CLIP_PROGRESS),null);
            }
            clip_ring.setLoop();
            clip_progress.setLoop();
            // for JSE
            clip_on.setVolumeGain(clip_on_volume_gain);
            clip_off.setVolumeGain(clip_off_volume_gain);
            clip_ring.setVolumeGain(clip_ring_volume_gain);
            clip_progress.setVolumeGain(clip_progress_volume_gain);
            // for JME
            //clip_on.setVolume(clip_on_volume);
            //clip_off.setVolume(clip_off_volume);
            //clip_ring.setVolume(clip_ring_volume);
            //clip_progress.setVolume(clip_progress_volume);
         }
         catch (Exception e)
         {  printException(e,Log.LEVEL_HIGH);
         }
      }
   }


   /** Inits the RegistrationClient */
   private void initRegistrationClient()
   {  rc=new RegistrationClient(sip_provider,new SipURL(ua_profile.registrar),ua_profile.getUserURI(),ua_profile.getUserURI(),ua_profile.auth_user,ua_profile.auth_realm,ua_profile.auth_passwd,this);
   }


   /** Gets SessionDescriptor from Vector of MediaSpec. */
   private SessionDescriptor getSessionDescriptor(Vector media_descs)
   {  String owner=ua_profile.user;
      String media_addr=(ua_profile.media_addr!=null)? ua_profile.media_addr : sip_provider.getViaAddress();
      int media_port=ua_profile.media_port;
      SessionDescriptor sd=new SessionDescriptor(owner,media_addr);
      for (int i=0; i<media_descs.size(); i++)
      {  MediaDesc md=(MediaDesc)media_descs.elementAt(i);
         // check if audio or video have been disabled
         if (md.getMedia().equalsIgnoreCase("audio") && !ua_profile.audio) continue;
         if (md.getMedia().equalsIgnoreCase("video") && !ua_profile.video) continue;
         // else
         if (media_port>0)
         {  // override the media_desc port
            md.setPort(media_port);
            media_port+=2;
         }
         sd.addMediaDescriptor(md.toMediaDescriptor());
      }
      return sd;
   }


   /** Creates a new SessionDescriptor from owner, address, and Vector of MediaDesc. */
   /*private static SessionDescriptor newSessionDescriptor(String owner, String media_addr, Vector media_descs)
   {  SessionDescriptor sd=new SessionDescriptor(owner,media_addr);
      for (int i=0; i<media_descs.size(); i++) sd.addMediaDescriptor(((MediaDesc)media_descs.elementAt(i)).toMediaDescriptor());
      return sd;
   }*/


   /** Sets a new media description (Vector of MediaDesc). */
   public void setMediaDescription(Vector media_descs)
   {  this.media_descs=media_descs;
   }


   /** Gets a NameAddress based on an input string.
     * The input string can be a:
     * <br/> - user name,
     * <br/> - an address of type <i>user@address</i>,
     * <br/> - a complete address in the form of <i>"Name" &lt;sip:user@address&gt;</i>,
     * <p/>
     * In the former case, a SIP URL is costructed using the proxy address
     * if available. */
   private NameAddress completeNameAddress(String str)
   {  if (str.indexOf("<sip:")>=0 || str.indexOf("<sips:")>=0) return new NameAddress(str);
      else
      {  SipURL url=completeSipURL(str);
         return new NameAddress(url);
      }
   }


   /** Gets a SipURL based on an input string. */
   private SipURL completeSipURL(String str)
   {  // in case it is passed only the user field, add "@" + proxy address
      if (ua_profile.proxy!=null && !str.startsWith("sip:") && !str.startsWith("sips:") && str.indexOf("@")<0 && str.indexOf(".")<0 && str.indexOf(":")<0)
      {  // may be it is just the user name..
         return new SipURL(str,ua_profile.proxy);
      }
      else return new SipURL(str);
   }


   // *************************** public methods **************************

   /** Sets the automatic answer time (default is -1 that means no auto accept mode) */
   /*public void setAcceptTime(int accept_time)
   {  ua_profile.accept_time=accept_time; 
   }*/

   /** Sets the automatic hangup time (default is 0, that corresponds to manual hangup mode) */
   /*public void setHangupTime(int time)
   {  ua_profile.hangup_time=time; 
   }*/

   /** Sets the redirection url (default is null, that is no redircetion) */
   /*public void setRedirection(NameAddress url)
   {  ua_profile.redirect_to=url; 
   }*/

   /** Sets the no offer mode for the invite (default is false) */
   /*public void setNoOfferMode(boolean nooffer)
   {  ua_profile.no_offer=nooffer;
   }*/

   /** Enables audio */
   /*public void setAudio(boolean enable)
   {  ua_profile.audio=enable;
   }*/

   /** Enables video */
   /*public void setVideo(boolean enable)
   {  ua_profile.video=enable;
   }*/

   /** Sets the receive only mode */
   /*public void setReceiveOnlyMode(boolean r_only)
   {  ua_profile.recv_only=r_only;
   }*/

   /** Sets the send only mode */
   /*public void setSendOnlyMode(boolean s_only)
   {  ua_profile.send_only=s_only;
   }*/

   /** Sets the send tone mode */
   /*public void setSendToneMode(boolean s_tone)
   {  ua_profile.send_tone=s_tone;
   }*/

   /** Sets the send file */
   /*public void setSendFile(String file_name)
   {  ua_profile.send_file=file_name;
   }*/

   /** Sets the recv file */
   /*public void setRecvFile(String file_name)
   {  ua_profile.recv_file=file_name;
   }*/

   /** Gets the local SDP */
   /*public String getLocalSDP()
   {  return session_descriptor.toString();
   }*/  

   /** Sets the local SDP */
   /*public void setLocalSDP(String sdp)
   {  session_descriptor=new SessionDescriptor(sdp);
   }*/


   /** Register with the registrar server
     * @param expire_time expiration time in seconds */
   public void register(int expire_time)
   {  if (rc.isRegistering()) rc.halt();
      rc.register(expire_time);
   }


   /** Periodically registers the contact address with the registrar server.
     * @param expire_time expiration time in seconds
     * @param renew_time renew time in seconds
     * @param keepalive_time keep-alive packet rate (inter-arrival time) in milliseconds */
   public void loopRegister(int expire_time, int renew_time, long keepalive_time)
   {  // create registration client
      if (rc==null) initRegistrationClient();
      // stop previous operation
      if (rc.isRegistering()) rc.halt();
      // start registering
      rc.loopRegister(expire_time,renew_time);
      // keep-alive
      if (keepalive_time>0)
      {  SipURL target_url=(sip_provider.hasOutboundProxy())? sip_provider.getOutboundProxy() : rc.getTarget().getAddress();
         String target_host=target_url.getHost();
         int target_port=target_url.getPort();
         if (target_port<0) target_port=SipStack.default_port;
         SocketAddress target_soaddr=new SocketAddress(target_host,target_port);
         if (keep_alive!=null && keep_alive.isRunning()) keep_alive.halt();
         keep_alive=new SipKeepAlive(sip_provider,target_soaddr,null,keepalive_time);
      }
   }


   /** Unregisters with the registrar server */
   public void unregister()
   {  // create registration client
      if (rc==null) initRegistrationClient();
      // stop registering
      if (keep_alive!=null && keep_alive.isRunning()) keep_alive.halt();
      if (rc.isRegistering()) rc.halt();
      // unregister
      rc.unregister();
   }


   /** Unregister all contacts with the registrar server */
   public void unregisterall()
   {  // create registration client
      if (rc==null) initRegistrationClient();
      // stop registering
      if (keep_alive!=null && keep_alive.isRunning()) keep_alive.halt();
      if (rc.isRegistering()) rc.halt();
      // unregister
      rc.unregisterall();
   }


   /** Makes a new call (acting as UAC). */
   public void call(String callee)
   {  call(callee,null);
   }


   /** Makes a new call (acting as UAC) with specific media description (Vector of MediaDesc). */
   public void call(String callee, Vector media_descs)
   {  // in case of incomplete url (e.g. only 'user' is present), try to complete it
      call(completeNameAddress(callee),media_descs);
   }


   /** Makes a new call (acting as UAC). */
   public void call(NameAddress callee)
   {  call(callee,null);
   }


   /** Makes a new call (acting as UAC) with specific media description (Vector of MediaDesc). */
   public void call(NameAddress callee, Vector media_descs)
   {  // new media description
      if (media_descs==null) media_descs=ua_profile.media_descs;
      this.media_descs=media_descs;
      // new call
      printLog("DEBUG: auth_user="+ua_profile.auth_user+"@"+ua_profile.auth_realm,Log.LEVEL_HIGH);
      call=new ExtendedCall(sip_provider,ua_profile.getUserURI(),ua_profile.auth_user,ua_profile.auth_realm,ua_profile.auth_passwd,this);      
      if (ua_profile.no_offer) call.call(callee);
      else
      {  SessionDescriptor local_sdp=getSessionDescriptor(media_descs);
         call.call(callee,local_sdp.toString());
      }
      progress=false;
      ringing=false;
   }


   /** Waits for an incoming call (acting as UAS). */
   /*public void listen()
   {  new CallWatcher(sip_provider,ua_profile.contact_url,this);
   }*/


   /** Closes an ongoing, incoming, or pending call. */
   public void hangup()
   {  // sound
      if (clip_progress!=null) clip_progress.stop();
      if (clip_ring!=null) clip_ring.stop();
      // response timeout
      if (response_to!=null) response_to.halt();

      closeMediaSessions();
      if (call!=null) call.hangup();
      call=null;
   } 


   /** Accepts an incoming call. */
   public void accept()
   {  accept(null);
   }


   /** Accepts an incoming call with specific media description (Vector of MediaDesc). */
   public void accept(Vector media_descs)
   {  // sound
      if (clip_ring!=null) clip_ring.stop();
      // response timeout
      if (response_to!=null) response_to.halt();
      // return if no active call
      if (call==null) return;
      // else
      // new media description
      if (media_descs==null) media_descs=ua_profile.media_descs;
      this.media_descs=media_descs;
      // new sdp
      SessionDescriptor local_sdp=getSessionDescriptor(media_descs);
      SessionDescriptor remote_sdp=new SessionDescriptor(call.getRemoteSessionDescriptor());
      SessionDescriptor new_sdp=new SessionDescriptor(local_sdp.getOrigin(),remote_sdp.getSessionName(),local_sdp.getConnection(),remote_sdp.getTime());
      new_sdp.addMediaDescriptors(local_sdp.getMediaDescriptors());
      new_sdp=OfferAnswerModel.makeSessionDescriptorProduct(new_sdp,remote_sdp);
      // accept
      call.accept(new_sdp.toString());
   }


   /** Redirects an incoming call. */
   public void redirect(String redirect_to)
   {  // in case of incomplete url (e.g. only 'user' is present), try to complete it
      redirect(completeNameAddress(redirect_to));
   }


   /** Redirects an incoming call. */
   public void redirect(NameAddress redirect_to)
   {  // sound
      if (clip_ring!=null) clip_ring.stop();
      // response timeout
      if (response_to!=null) response_to.halt();
      
      if (call!=null) call.redirect(redirect_to);
   }   


   /** Modifies the current session. It re-invites the remote party changing the contact URL and SDP. */
   public void modify(String body)
   {  if (call!=null && call.isActive())
      {  printLog("RE-INVITING/MODIFING");
         call.modify(body);
      }
   }


   /** Transfers the current call to a remote UA. */
   public void transfer(String transfer_to)
   {  // in case of incomplete url (e.g. only 'user' is present), try to complete it
      transfer(completeNameAddress(transfer_to));
   }


   /** Transfers the current call to a remote UA. */
   public void transfer(NameAddress transfer_to)
   {  if (call!=null && call.isActive())
      {  printLog("REFER/TRANSFER");
         call.transfer(transfer_to);
      }
   }


   // ********************** protected methods **********************

   /** Starts media sessions (audio and/or video). */
   protected void startMediaSessions()
   {
      // exit if the media application is already running  
      if (media_sessions.size()>0)
      {  printLog("DEBUG: media sessions already active",Log.LEVEL_HIGH);
         return;
      }
      // get local and remote rtp addresses and ports
      SessionDescriptor local_sdp=new SessionDescriptor(call.getLocalSessionDescriptor());
      SessionDescriptor remote_sdp=new SessionDescriptor(call.getRemoteSessionDescriptor());
      String local_address=local_sdp.getConnection().getAddress();
      String remote_address=remote_sdp.getConnection().getAddress();
      // calculate media descriptor product
      Vector md_list=OfferAnswerModel.makeMediaDescriptorProduct(local_sdp.getMediaDescriptors(),remote_sdp.getMediaDescriptors());
      // select the media direction (send_only, recv_ony, fullduplex)
      FlowSpec.Direction dir=FlowSpec.FULL_DUPLEX;
      if (ua_profile.recv_only) dir=FlowSpec.RECV_ONLY;
      else
      if (ua_profile.send_only) dir=FlowSpec.SEND_ONLY;
      // for each media
      for (Enumeration ei=md_list.elements(); ei.hasMoreElements(); )
      {  MediaField md=((MediaDescriptor)ei.nextElement()).getMedia();
         String media=md.getMedia();
         // local and remote ports
         int local_port=md.getPort();
         int remote_port=remote_sdp.getMediaDescriptor(media).getMedia().getPort();
         remote_sdp.removeMediaDescriptor(media);
         // media and flow specifications
         String transport=md.getTransport();
         String format=(String)md.getFormatList().elementAt(0);
         int avp=Integer.parseInt(format);
         MediaSpec media_spec=null;
         for (int i=0; i<media_descs.size() && media_spec==null; i++)
         {  MediaDesc media_desc=(MediaDesc)media_descs.elementAt(i);
            if (media_desc.getMedia().equalsIgnoreCase(media))
            {  Vector media_specs=media_desc.getMediaSpecs();
               for (int j=0; j<media_specs.size() && media_spec==null; j++)
               {  MediaSpec ms=(MediaSpec)media_specs.elementAt(j);
                  if (ms.getAVP()==avp) media_spec=ms;
               }
            }
         }
         if (local_port!=0 && remote_port!=0 && media_spec!=null)
         {  FlowSpec flow_spec=new FlowSpec(media_spec,local_port,remote_address,remote_port,dir);
            printLog(media+" format: "+flow_spec.getMediaSpec().getCodec());
            boolean success=media_agent.startMediaSession(flow_spec);           
            if (success)
            {  media_sessions.addElement(media);
               if (listener!=null) listener.onUaMediaSessionStarted(this,media,format);
            }
         }
         else
         {  printLog("DEBUG: media session cannot be started (local_port="+local_port+", remote_port="+remote_port+", media_spec="+media_spec+").");
         }
      }
   }
 
   
   /** Closes media sessions.  */
   protected void closeMediaSessions()
   {  for (int i=0; i<media_sessions.size(); i++)
      {  String media=(String)media_sessions.elementAt(i);
         media_agent.stopMediaSession(media);
         if (listener!=null) listener.onUaMediaSessionStopped(this,media);
      }
      media_sessions.removeAllElements();
   }


   // ************************* RA callbacks ************************

   /** From RegistrationClientListener. When it has been successfully (un)registered. */
   public void onRegistrationSuccess(RegistrationClient rc, NameAddress target, NameAddress contact, String result)
   {  printLog("Registration success: "+result,Log.LEVEL_HIGH);
      if (listener!=null) listener.onUaRegistrationSucceeded(this,result);   
   }

   /** From RegistrationClientListener. When it failed on (un)registering. */
   public void onRegistrationFailure(RegistrationClient rc, NameAddress target, NameAddress contact, String result)
   {  printLog("Registration failure: "+result,Log.LEVEL_HIGH);
      if (listener!=null) listener.onUaRegistrationFailed(this,result);
   }


   // ************************ Call callbacks ***********************
   
   /** From CallWatcherListener. When the CallWatcher receives a new invite request that creates a new Call. */
   public void onNewIncomingCall(CallWatcher call_watcher, ExtendedCall call, NameAddress callee, NameAddress caller, String sdp, Message invite)
   {  printLog("onNewIncomingCall()",Log.LEVEL_LOW);
      if (this.call!=null && !this.call.isClosed())
      {  printLog("LOCALLY BUSY: INCOMING CALL REFUSED",Log.LEVEL_HIGH);
         call.refuse();
         return;
      }
      // else   
      printLog("INCOMING",Log.LEVEL_HIGH);
      this.call=call;
      call.ring();
      // sound
      if (clip_ring!=null) clip_ring.play();
      // response timeout
      if (ua_profile.refuse_time>=0) response_to=new Timer(ua_profile.refuse_time*1000,this);
      response_to.start();
      
      Vector media_descs=null;
      if (sdp!=null)
      {  Vector md_list=(new SessionDescriptor(sdp)).getMediaDescriptors();
         media_descs=new Vector(md_list.size());
         for (int i=0; i<md_list.size(); i++) media_descs.addElement(new MediaDesc((MediaDescriptor)md_list.elementAt(i)));
      }
      if (listener!=null) listener.onUaIncomingCall(this,callee,caller,media_descs);
   }  


   /** From CallListener. Callback function called when arriving a new INVITE method (incoming call) */
   public void onCallInvite(Call call, NameAddress callee, NameAddress caller, String sdp, Message invite)
   {  printLog("onCallInvite()",Log.LEVEL_LOW);
      if (call!=this.call) {  printLog("NOT the current call",Log.LEVEL_LOW);  return;  }
      // never called (the method onNewInocomingCall() is called instead): do nothing.
   }


   /** From CallListener. Callback function called when arriving a new Re-INVITE method (re-inviting/call modify) */
   public void onCallModify(Call call, String sdp, Message invite)
   {  printLog("onCallModify()",Log.LEVEL_LOW);
      if (call!=this.call) {  printLog("NOT the current call",Log.LEVEL_LOW);  return;  }
      printLog("RE-INVITE/MODIFY",Log.LEVEL_HIGH);
      // to be implemented.
      // currently it simply accepts the session changes (see method onCallModify() in CallListenerAdapter)
      super.onCallModify(call,sdp,invite);
   }


   /** From CallListener. Callback function called when arriving a 183 Session Progress */
   public void onCallProgress(Call call, Message resp)
   {  printLog("onCallProgress()",Log.LEVEL_LOW);
      if (call!=this.call && call!=call_transfer) {  printLog("NOT the current call",Log.LEVEL_LOW);  return;  }
      if (!progress)
      {  printLog("PROGRESS",Log.LEVEL_HIGH);
         progress=true;
         // sound
         if (clip_progress!=null) clip_progress.play();
         
         if (listener!=null) listener.onUaCallProgress(this);
      }
   }


   /** From CallListener. Callback function that may be overloaded (extended). Called when arriving a 180 Ringing */
   public void onCallRinging(Call call, Message resp)
   {  printLog("onCallRinging()",Log.LEVEL_LOW);
      if (call!=this.call && call!=call_transfer) {  printLog("NOT the current call",Log.LEVEL_LOW);  return;  }
      if (!ringing)
      {  printLog("RINGING",Log.LEVEL_HIGH);
         ringing=true;
         // sound
         if (clip_progress!=null) clip_progress.play();
         
         if (listener!=null) listener.onUaCallRinging(this);
      }
   }


   /** From CallListener. Callback function called when arriving a 2xx (call accepted) */
   public void onCallAccepted(Call call, String sdp, Message resp)
   {  printLog("onCallAccepted()",Log.LEVEL_LOW);
      if (call!=this.call && call!=call_transfer) {  printLog("NOT the current call",Log.LEVEL_LOW);  return;  }
      printLog("ACCEPTED/CALL",Log.LEVEL_HIGH);
      if (ua_profile.no_offer)
      {  // new sdp
         SessionDescriptor local_sdp=getSessionDescriptor(media_descs);
         SessionDescriptor remote_sdp=new SessionDescriptor(sdp);
         SessionDescriptor new_sdp=new SessionDescriptor(local_sdp.getOrigin(),remote_sdp.getSessionName(),local_sdp.getConnection(),remote_sdp.getTime());
         new_sdp.addMediaDescriptors(local_sdp.getMediaDescriptors());
         new_sdp=OfferAnswerModel.makeSessionDescriptorProduct(new_sdp,remote_sdp);         
         // answer with the local sdp
         call.ackWithAnswer(new_sdp.toString());
      }
      // sound
      if (clip_progress!=null) clip_progress.stop();
      if (clip_on!=null) clip_on.play();
      
      if (listener!=null) listener.onUaCallAccepted(this);

      startMediaSessions();
      
      if (call==call_transfer)
      {  this.call.notify(resp);
      }
   }


   /** From CallListener. Callback function called when arriving an ACK method (call confirmed) */
   public void onCallConfirmed(Call call, String sdp, Message ack)
   {  printLog("onCallConfirmed()",Log.LEVEL_LOW);
      if (call!=this.call) {  printLog("NOT the current call",Log.LEVEL_LOW);  return;  }
      printLog("CONFIRMED/CALL",Log.LEVEL_HIGH);
      // sound
      if (clip_on!=null) clip_on.play();
      
      startMediaSessions();
   }


   /** From CallListener. Callback function called when arriving a 2xx (re-invite/modify accepted) */
   public void onCallReInviteAccepted(Call call, String sdp, Message resp)
   {  printLog("onCallReInviteAccepted()",Log.LEVEL_LOW);
      if (call!=this.call) {  printLog("NOT the current call",Log.LEVEL_LOW);  return;  }
      printLog("RE-INVITE-ACCEPTED/CALL",Log.LEVEL_HIGH);
   }


   /** From CallListener. Callback function called when arriving a 4xx (re-invite/modify failure) */
   public void onCallReInviteRefused(Call call, String reason, Message resp)
   {  printLog("onCallReInviteRefused()",Log.LEVEL_LOW);
      if (call!=this.call) {  printLog("NOT the current call",Log.LEVEL_LOW);  return;  }
      printLog("RE-INVITE-REFUSED ("+reason+")/CALL",Log.LEVEL_HIGH);
      if (listener!=null) listener.onUaCallFailed(this,reason);
   }


   /** From CallListener. Callback function called when arriving a 4xx (call failure) */
   public void onCallRefused(Call call, String reason, Message resp)
   {  printLog("onCallRefused()",Log.LEVEL_LOW);
      if (call!=this.call && call!=call_transfer) {  printLog("NOT the current call",Log.LEVEL_LOW);  return;  }
      printLog("REFUSED ("+reason+")",Log.LEVEL_HIGH);
      if (call==call_transfer)
      {  this.call.notify(resp);
         call_transfer=null;
      }
      else this.call=null;
      // sound
      if (clip_progress!=null) clip_progress.stop();
      if (clip_off!=null) clip_off.play();
      
      if (listener!=null) listener.onUaCallFailed(this,reason);
   }


   /** From CallListener. Callback function called when arriving a 3xx (call redirection) */
   public void onCallRedirected(Call call, String reason, Vector contact_list, Message resp)
   {  printLog("onCallRedirected()",Log.LEVEL_LOW);
      if (call!=this.call) {  printLog("NOT the current call",Log.LEVEL_LOW);  return;  }
      printLog("REDIRECTION ("+reason+")",Log.LEVEL_HIGH);
      NameAddress first_contact=new NameAddress((String)contact_list.elementAt(0));
      call.call(first_contact); 
   }


   /** From CallListener. Callback function called when arriving a CANCEL request */
   public void onCallCancel(Call call, Message cancel)
   {  printLog("onCallCancel()",Log.LEVEL_LOW);
      if (call!=this.call) {  printLog("NOT the current call",Log.LEVEL_LOW);  return;  }
      printLog("CANCEL",Log.LEVEL_HIGH);
      this.call=null;
      // sound
      if (clip_ring!=null) clip_ring.stop();
      if (clip_off!=null) clip_off.play();
      // response timeout
      if (response_to!=null) response_to.halt();
      
      if (listener!=null) listener.onUaCallCancelled(this);
   }


   /** From CallListener. Callback function called when arriving a BYE request */
   public void onCallBye(Call call, Message bye)
   {  printLog("onCallBye()",Log.LEVEL_LOW);
      if (call!=this.call && call!=call_transfer) {  printLog("NOT the current call",Log.LEVEL_LOW);  return;  }
      if (call!=call_transfer && call_transfer!=null)
      {  printLog("CLOSE PREVIOUS CALL",Log.LEVEL_HIGH);
         this.call=call_transfer;
         call_transfer=null;
         return;
      }
      // else
      printLog("CLOSE",Log.LEVEL_HIGH);
      this.call=null;
      closeMediaSessions();
      // sound
      if (clip_off!=null) clip_off.play();
      
      if (listener!=null) listener.onUaCallClosed(this);
   }


   /** From CallListener. Callback function called when arriving a response after a BYE request (call closed) */
   public void onCallClosed(Call call, Message resp)
   {  printLog("onCallClosed()",Log.LEVEL_LOW);
      if (call!=this.call) {  printLog("NOT the current call",Log.LEVEL_LOW);  return;  }
      printLog("CLOSE/OK",Log.LEVEL_HIGH);
      if (listener!=null) listener.onUaCallClosed(this);
   }

   /** Callback function called when the invite expires */
   public void onCallTimeout(Call call)
   {  printLog("onCallTimeout()",Log.LEVEL_LOW);
      if (call!=this.call) {  printLog("NOT the current call",Log.LEVEL_LOW);  return;  }
      printLog("NOT FOUND/TIMEOUT",Log.LEVEL_HIGH);
      int code=408;
      String reason="Request Timeout";
      if (call==call_transfer)
      {  this.call.notify(code,reason);
         call_transfer=null;
      }
      // sound
      if (clip_off!=null) clip_off.play();
      
      if (listener!=null) listener.onUaCallFailed(this,reason);
   }


   // ******************* ExtendedCall callbacks ********************

   /** From ExtendedCallListener. Callback function called when arriving a new REFER method (transfer request) */
   public void onCallTransfer(ExtendedCall call, NameAddress refer_to, NameAddress refered_by, Message refer)
   {  printLog("onCallTransfer()",Log.LEVEL_LOW);
      if (call!=this.call) {  printLog("NOT the current call",Log.LEVEL_LOW);  return;  }
      printLog("transfer to "+refer_to.toString(),Log.LEVEL_HIGH);
      call.acceptTransfer();
      call_transfer=new ExtendedCall(sip_provider,ua_profile.getUserURI(),this);
      call_transfer.call(refer_to,getSessionDescriptor(media_descs).toString());
   }

   /** From ExtendedCallListener. Callback function called when a call transfer is accepted. */
   public void onCallTransferAccepted(ExtendedCall call, Message resp)
   {  printLog("onCallTransferAccepted()",Log.LEVEL_LOW);
      if (call!=this.call) {  printLog("NOT the current call",Log.LEVEL_LOW);  return;  }
      printLog("transfer accepted",Log.LEVEL_HIGH);
   }

   /** From ExtendedCallListener. Callback function called when a call transfer is refused. */
   public void onCallTransferRefused(ExtendedCall call, String reason, Message resp)
   {  printLog("onCallTransferRefused()",Log.LEVEL_LOW);
      if (call!=this.call) {  printLog("NOT the current call",Log.LEVEL_LOW);  return;  }
      printLog("transfer refused",Log.LEVEL_HIGH);
   }

   /** From ExtendedCallListener. Callback function called when a call transfer is successfully completed */
   public void onCallTransferSuccess(ExtendedCall call, Message notify)
   {  printLog("onCallTransferSuccess()",Log.LEVEL_LOW);
      if (call!=this.call) {  printLog("NOT the current call",Log.LEVEL_LOW);  return;  }
      printLog("transfer successed",Log.LEVEL_HIGH);
      call.hangup();
      if (listener!=null) listener.onUaCallTransferred(this);
   }

   /** From ExtendedCallListener. Callback function called when a call transfer is NOT sucessfully completed */
   public void onCallTransferFailure(ExtendedCall call, String reason, Message notify)
   {  printLog("onCallTransferFailure()",Log.LEVEL_LOW);
      if (call!=this.call) {  printLog("NOT the current call",Log.LEVEL_LOW);  return;  }
      printLog("transfer failed",Log.LEVEL_HIGH);
   }


   // *********************** Timer callbacks ***********************

   /** When the Timer exceeds. */
   public void onTimeout(Timer t)
   {  if (response_to==t)
      {  printLog("response time expired: incoming call declined",Log.LEVEL_HIGH);
         if (call!=null) call.refuse();
         // sound
         if (clip_ring!=null) clip_ring.stop();
      }
   }


   // ***************************** logs ****************************

   /** Default log level offset */
   static final int LOG_OFFSET=0;
   
   /** Adds a new string to the default Log */
   public void printLog(String str)
   {  printLog(str,Log.LEVEL_HIGH);
   }

   /** Adds a new string to the default Log */
   private void printLog(String str, int level)
   {  if (log!=null) log.println("UA: "+str,UserAgent.LOG_OFFSET+level);  
      else if ((ua_profile==null || !ua_profile.no_prompt) && level<=Log.LEVEL_HIGH) System.out.println("UA: "+str);
   }

   /** Adds the Exception message to the default Log */
   private final void printException(Exception e, int level)
   {  printLog("Exception: "+ExceptionPrinter.getStackTraceOf(e),level);
   }

}
