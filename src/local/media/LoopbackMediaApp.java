package local.media;



import local.net.UdpRelay;
import org.zoolu.tools.Log;
import org.zoolu.tools.ExceptionPrinter;



/** LoopbackMediaApp.
  */
public class LoopbackMediaApp implements MediaApp
{  
   /** Log */
   Log log=null;

   /** UdpRelay */
   UdpRelay udp_relay=null;
   

   /** Creates a new LoopbackMediaApp */
   public LoopbackMediaApp(FlowSpec flow_spec, Log log)
   {  this.log=log;  
      try
      {  udp_relay=new UdpRelay(flow_spec.getLocalPort(),flow_spec.getRemoteAddress(),flow_spec.getRemotePort(),null);
         printLog("relay "+udp_relay.toString()+" started",Log.LEVEL_LOW);
      }
      catch (Exception e) {  printException(e,Log.LEVEL_HIGH);  }
   }


   /** Starts media application */
   public boolean startApp()
   {  // do nothing, already started..  
      return true;      
   }


   /** Stops media application */
   public boolean stopApp()
   {  if (udp_relay!=null)
      {  udp_relay.halt();
         udp_relay=null;
         printLog("relay halted",Log.LEVEL_LOW);
      }      
      return true;
   }


   // *************************** Callbacks ***************************

   /** From UdpRelayListener. When the remote source address changes. */
   public void onUdpRelaySourceChanged(UdpRelay udp_relay, String remote_src_addr, int remote_src_port)
   {  printLog("UDP relay: remote address changed: "+remote_src_addr+":"+remote_src_port,Log.LEVEL_HIGH);
   }

   /** From UdpRelayListener. When UdpRelay stops relaying UDP datagrams. */
   public void onUdpRelayTerminated(UdpRelay udp_relay)
   {  printLog("UDP relay: terminated.",Log.LEVEL_HIGH);
   } 


   // ****************************** Logs *****************************

   /** Default log level offset */
   static final int LOG_OFFSET=0;
   

   /** Adds a new string to the default Log */
   private void printLog(String str)
   {  printLog(str,Log.LEVEL_HIGH);
   }


   /** Adds a new string to the default Log */
   private void printLog(String str, int level)
   {  if (log!=null) log.println("LoopbackMediaApp: "+str,LoopbackMediaApp.LOG_OFFSET+level);  
      if (level<=Log.LEVEL_HIGH) System.out.println("LoopbackMediaApp: "+str);
   }


   /** Adds the Exception message to the default Log */
   private void printException(Exception e,int level)
   {  printLog("Exception: "+ExceptionPrinter.getStackTraceOf(e),level);
      if (level<=Log.LEVEL_HIGH) e.printStackTrace();
   }

}