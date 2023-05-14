package local.sbc;


import org.zoolu.sip.address.*;
import org.zoolu.sip.provider.SipStack;
import org.zoolu.net.IpAddress;
import org.zoolu.tools.Configure;
import org.zoolu.tools.Parser;

import java.io.*;
import java.util.Vector;


/**
 * GwProfile maintains the SessionBorderController configuration.
 */
public class GwProfile extends Configure
{
       
   // ********************* SipGW configurations *******************

   /** Maximum time that the UDP relay remains active without receiving UDP datagrams (in milliseconds). */
   long relay_timeout=60000; // 1min

   /** Refresh time of address-binding cache (in milliseconds) */
   long binding_timeout=3600000;

   /** Minimum time between two changes of peer address (in milliseconds) */
   int handover_time=0; //5000;

   /** Rate of keep-alive datagrams sent toward all registered UAs (in milliseconds).
     * Set keepalive_time=0 to disable the sending of keep-alive datagrams */
   public long keepalive_time=0;

   /** Whether sending keepalive datagram only to UAs that explicitely request it through 'keep-alive' parameter. */
   //public boolean keepalive_selective=false;

   /** Whether sending keepalive datagram to all contacted UAs (also toward non-registered UAs) */
   public boolean keepalive_aggressive=false;

   /** Whether implementing symmetric RTP for NAT traversal. */
   //boolean symmetric_rtp=false;
   
   /** Whether intercepting media traffics. */
   public boolean do_interception=false;

   /** Whether injecting new media flows. */
   public boolean do_active_interception=false;

   /** Sink address for media traffic interception. */
   public String sink_addr="127.0.0.1";

   /** Sink port for media traffic interception. */
   public int sink_port=0;

   /** Media address. */
   public String media_addr="0.0.0.0";

   /** Available media ports (default interval=[37000:37399]). */
   public Vector media_ports=null;

   /** First Available media port. */
   private int first_port=37000;
   /** Last Available media port. */
   private int last_port=37399;


   // ************************** costructors *************************
   
   /** Costructs a new GwProfile */
   public GwProfile()
   {  init(null);
   }

   /** Costructs a new GwProfile */
   public GwProfile(String file)
   {  init(file);
   }

   /** Inits the GwProfile */
   private void init(String file)
   {  loadFile(file);
      media_ports=new Vector();
      for (int i=first_port; i<=last_port; i+=2) media_ports.addElement(new Integer(i)); 
   }


   // **************************** methods ***************************

   /** Parses a single line (loaded from the config file) */
   protected void parseLine(String line)
   {  String attribute;
      Parser par;
      int index=line.indexOf("=");
      if (index>0) {  attribute=line.substring(0,index).trim(); par=new Parser(line,index+1);  }
      else {  attribute=line; par=new Parser("");  }

      if (attribute.equals("relay_timeout")) { relay_timeout=par.getInt(); return; }
      if (attribute.equals("binding_timeout")) { binding_timeout=par.getInt(); return; }
      if (attribute.equals("handover_time")) { handover_time=par.getInt(); return; }
      if (attribute.equals("keepalive_time")) { keepalive_time=par.getInt(); return; }
      //if (attribute.equals("keepalive_selective")) { keepalive_selective=(par.getString().toLowerCase().startsWith("y")); return; }
      if (attribute.equals("keepalive_aggressive")) { keepalive_aggressive=(par.getString().toLowerCase().startsWith("y")); return; }
      //if (attribute.equals("symmetric_rtp")) { symmetric_rtp=(par.getString().toLowerCase().startsWith("y")); return; }
      if (attribute.equals("do_interception")) { do_interception=(par.getString().toLowerCase().startsWith("y")); return; }
      if (attribute.equals("do_active_interception")) { do_active_interception=(par.getString().toLowerCase().startsWith("y")); return; }
      if (attribute.equals("sink_addr")) { sink_addr=par.getString(); return; }
      if (attribute.equals("sink_port")) { sink_port=par.getInt(); return; }
      if (attribute.equals("media_addr")) { media_addr=par.getString(); return; }
      if (attribute.equals("media_ports"))
      {  char[] delim={' ','-',':'};
         first_port=Integer.parseInt(par.getWord(delim));
         last_port=Integer.parseInt(par.getWord(delim));
         return;
      }
   }  
 
      
   /** Converts the entire object into lines (to be saved into the config file) */
   protected String toLines()
   {  // currently not implemented..
      return toString();
   }
   
}
