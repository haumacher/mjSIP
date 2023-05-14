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

package local.conference;


import local.server.Proxy;
import local.server.ServerProfile;
import org.zoolu.sip.address.*;
import org.zoolu.sip.provider.*;
import org.zoolu.sip.header.*;
import org.zoolu.sip.message.Message;
import org.zoolu.tools.ScheduledWork;
import org.zoolu.tools.Log;

import java.io.FileOutputStream;
import java.util.Hashtable;
import java.util.HashSet;


/** Class ConferenceServer extends class Proxy implementing a SIP Conference Server.
  * <p/>
  * A ConferenceServer acts as register and location server
  * for registering new conferences. A new conference focus is created for for each
  * new registered conference. <br/>
  */
public class ConferenceServer extends Proxy
{   
   /** Whether working in debug mode. */
   public static boolean DEBUG=true;

   /** Active focuses, as table of (String)conference_id-->(Focus)focus */
   protected Hashtable focuses_db;

   /** Avaliable Focus ports (Integer) */
   protected HashSet fports;

   /** Avaliable media ports (Integer) */
   protected HashSet mports;

   /** Whether to use the same caller media port */
   protected boolean port_mirroring=false;
   
   /** Whether mixing audio streams. */
   protected boolean audio_mixing=false;

   /** Whether using symmetric RTP. */
   protected boolean symmetric_rtp=false;



   /** Costructs a new ConferenceServer that acts also as location server
     * for registered conference. */
   public ConferenceServer(SipProvider sip_provider, ServerProfile server_profile, HashSet fports, HashSet mports, boolean symmetric_rtp)
   {  //super(provider,db_class,server_profile);
      super(sip_provider,server_profile);
      this.fports=fports;
      this.mports=mports;
      this.symmetric_rtp=symmetric_rtp;
      printLog("total of available focus ports: "+fports.size(),Log.LEVEL_HIGH); 
      printLog("total of available media ports: "+mports.size(),Log.LEVEL_HIGH);
      focuses_db=new Hashtable();
   }
   
  
   /** Sets media ports equal to that used by peers. */
   public void setPortMirroring(boolean port_mirroring)
   {  this.port_mirroring=port_mirroring;
   }


   /** Sets the mixer mode for audio streams. */
   public void setAudioMixing(boolean audio_mixing)
   {  this.audio_mixing=audio_mixing;
   }


   /** When a new REGISTER request is received for the local server */
   public void processRequestToLocalServer(Message msg)
   {  printLog("inside processRequestToLocalServer(msg)",Log.LEVEL_MEDIUM);
      
      if (msg.isRegister())
      {
         // get the conference focus
         ToHeader th=msg.getToHeader();
         if (th==null)  
         {  printLog("To header field missed: message discarded",Log.LEVEL_HIGH);
            return;
         }         
         ExpiresHeader eh=msg.getExpiresHeader();
         if (eh==null)  
         {  printLog("Expires header field missed: message discarded",Log.LEVEL_HIGH);
            return;
         }         
         SipURL to_url=th.getNameAddress().getAddress();
         String conference_id=to_url.getUserName()+"@"+to_url.getHost();
         int expires=eh.getDeltaSeconds();
         if (expires>0)
         {  Focus focus;
            if (!focuses_db.containsKey(conference_id))
            {  // ADD a new conference
               focus=addConference(conference_id,expires);
            }
            else
            {  // GET the conference
               focus=(Focus)focuses_db.get(conference_id);
            }
            // set focus contact address
            NameAddress contact_url=focus.getContactURL();          
            printLog("conference '"+conference_id+"' is at "+contact_url,Log.LEVEL_HIGH);        
            msg.setContactHeader(new ContactHeader(contact_url));
         }
         else
         {  if (focuses_db.containsKey(conference_id))
            {  // CLOSE the conference
               closeConference(conference_id);
            }
            // set contact
            msg.setContactHeader(new ContactHeader());
         }
      }
      
      // register (or unregister) the conference with the location server
      super.processRequestToLocalServer(msg); 
   }


   /** Register a new conference.
     * @conference_id conference name formed as name@conference-server
     * @expires expiration time in seconds; a conference with expiration time <=0 never expires */
   public void registerConference(String conference_id, int expires)
   {  
      Focus focus=addConference(conference_id,expires);
      location_service.addUserStaticContact(conference_id,focus.getContactURL());
   }


   /** Adds a new conference. It returns the Focus of the conference. */
   private Focus addConference(String conference_id, int expires)
   {  Integer port=(Integer)fports.iterator().next();
      fports.remove(port);
      Focus focus=newFocus(sip_provider.getViaAddress(),port.intValue(),sip_provider.getTransportProtocols(),mports,symmetric_rtp); 
      focuses_db.put(conference_id,focus);
      if (port_mirroring) focus.setPortMirroring(true);  
      if (audio_mixing) focus.setAudioMixing(true);
      printLog("ADD new conference '"+conference_id+"'",Log.LEVEL_HIGH);
      if (expires>0)
      {  closeConferenceAfter(conference_id,expires);
         printLog("the conference will be teared down in "+expires+"secs",Log.LEVEL_HIGH);
      }
      else
      {  printLog("WARNING: no expiration time for this conference: it will never expire",Log.LEVEL_HIGH);
      }
      return focus;
   }


   /** Closes a conference and removes the focus. */
   private void closeConference(String conference_id)
   {  Focus focus=(Focus)focuses_db.get(conference_id);
      focus.hangup();
      fports.add(new Integer(focus.getPort()));
      focus.halt();
      focuses_db.remove(conference_id);
      printLog("CLOSE conference '"+conference_id+"'",Log.LEVEL_HIGH);
      printLog("there are still "+focuses_db.size()+" active conferences",Log.LEVEL_MEDIUM);
   }


   /** Schedules an automatic conference closing after <i>expires</i> secs. */
   private void closeConferenceAfter(final String conference_id, final int expires)
   {  new ScheduledWork(expires*1000)
      {  public void doWork()
         {  printLog("conference '"+conference_id+"' expired",Log.LEVEL_HIGH);
            closeConference(conference_id);
            location_service.removeUser(conference_id);
         }
      };
   }


   /** Creates a new Focus.
     * This method is used when registering a new conference, for creating
     * a new conference Focus. <br/>
     * It can be re-defined by a class that extends ConferenceServer in order to
     * implement new Focus functionalities.
     * @return It returns a new Focus. */
   protected Focus newFocus(String via_addr, int sip_port, String[] transport_protocols, HashSet media_ports, boolean symmetric_rtp)
   {  return new Focus(via_addr,sip_port,transport_protocols,media_ports,symmetric_rtp); 
   }


   // ****************************** Logs *****************************

   /** Default log level offset */
   static final int LOG_OFFSET=0;
   
   /** Adds a new string to the default Log */
   protected void printLog(String str, int level)
   {  if (log!=null) log.println("CS: "+str,ConferenceServer.LOG_OFFSET+level);
      if (DEBUG && level<=Log.LEVEL_HIGH) System.err.println("CS: "+str);
   }


   // ****************************** MAIN *****************************

   /** Default first focus port */
   protected static final int DEFAULT_FPORTS_BEGIN=6060;
   /** Default number of focus ports */
   protected static final int DEFAULT_FPORTS_SIZE=40;

   /** Default first media port */
   protected static final int DEFAULT_MPORTS_BEGIN=37100;
   /** Default number of media ports */
   protected static final int DEFAULT_MPORTS_SIZE=200;


   /** Main method. */
   public static void main(String[] args)
   {        
      String file=null;
      String conference_db=null;
      
      int first_focus_port=DEFAULT_FPORTS_BEGIN;
      int last_focus_port=DEFAULT_FPORTS_BEGIN+DEFAULT_FPORTS_SIZE-1;

      int first_media_port=DEFAULT_MPORTS_BEGIN;
      int last_media_port=DEFAULT_MPORTS_BEGIN+DEFAULT_MPORTS_SIZE-1;

      boolean port_mirroring=false;
      boolean audio_mixing=false;
      boolean symmetric_rtp=false;

      String add_conf=null;
               
      for (int i=0; i<args.length; i++)
      {  if (args[i].equals("-f") && args.length>(i+1))
         {  file=args[++i];
            continue;
         }
         if (args[i].equals("-c") && args.length>(i+1))
         {  conference_db=args[++i];
            continue;
         }
         if (args[i].equals("-p")) // set the local ports
         {  first_focus_port=Integer.parseInt(args[++i]);
            last_focus_port=Integer.parseInt(args[++i]);
            continue;
         }
         if (args[i].equals("-m")) // set the local media ports
         {  first_media_port=Integer.parseInt(args[++i]);
            last_media_port=Integer.parseInt(args[++i]);
            continue;
         }  
         if (args[i].equals("--port-mirror"))
         {  port_mirroring=true;
            continue;
         }
         if (args[i].equals("--audio-mixer"))
         {  audio_mixing=true;
            continue;
         }
         if (args[i].equals("--symmetric-rtp"))
         {  symmetric_rtp=true;
            continue;
         }
         if (args[i].equals("--add-conf") && args.length>(i+1))
         {  add_conf=args[++i];
            continue;
         }

         if (args[i].equals("-h"))
         {  System.out.println("usage:\n   java ConferenceServer [options]");
            System.out.println("   options:");
            System.out.println("   -f <config_file>   specifies a configuration file");
            System.out.println("   -c <conference_db> specifies a conference DB");
            System.out.println("   -p <fist_port> <last_port> interval of focus ports");
            System.out.println("   -m <fist_port> <last_port> interval of media ports");
            System.out.println("   --port-mirror      uses media port equals to the peer");
            System.out.println("   --audio-mixer      mixes audio streams");
            System.out.println("   --symmetric-rtp    uses symmetric RTP");
            System.out.println("   --add-conf <name>  add a static conference");
            System.exit(0);
         }
      }
                  
      SipStack.init(file);
      SipProvider sip_provider=new SipProvider(file);
      ServerProfile server_profile=new ServerProfile(file);
      if (conference_db!=null) server_profile.location_db=conference_db;
      
      // delete prefious registered confereces
      try { FileOutputStream aux=new FileOutputStream(server_profile.location_db); aux.close(); } catch (Exception e) { }

      HashSet focus_ports=new HashSet();
      for (int i=first_focus_port; i<=last_focus_port; i++) focus_ports.add(new Integer(i)); 

      HashSet media_ports=new HashSet();
      for (int i=first_media_port; i<=last_media_port; i+=4) media_ports.add(new Integer(i)); 

      ConferenceServer conference_server=new ConferenceServer(sip_provider,server_profile,focus_ports,media_ports,symmetric_rtp);

      if (port_mirroring) conference_server.setPortMirroring(true);
      if (audio_mixing) conference_server.setAudioMixing(true);
      if (add_conf!=null) conference_server.registerConference(add_conf,-1);
   }
  
}