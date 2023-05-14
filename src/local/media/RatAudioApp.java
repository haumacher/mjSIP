package local.media;



import local.net.UdpRelay;
import org.zoolu.tools.Log;



/** RAT application.
  */
public class RatAudioApp implements MediaApp
{
   /** Log */
   Log log=null;

   /** Runtime media process (RAT application) */
   Process media_process=null;
   
   int local_port;
   int remote_port;
   String remote_addr;
   
   /** Media application command */
   String command;

   /** Creates a new RatAudioApp. */
   public  RatAudioApp(String rat_comm, FlowSpec flow_spec, Log log)
   {  this.log=log;
      this.command=rat_comm;
      this.local_port=flow_spec.getLocalPort();
      this.remote_addr=flow_spec.getRemoteAddress();
      this.remote_port=flow_spec.getRemotePort();
   }

   /** Starts media application */
   public boolean startApp()
   {  // udp flow adaptation for RAT application
      if (local_port!=remote_port) 
      {  printLog("UDP local relay: src_port="+local_port+", dest_port="+remote_port);
         printLog("UDP local relay: src_port="+(local_port+1)+", dest_port="+(remote_port+1));
         new UdpRelay(local_port,"127.0.0.1",remote_port,null);
         new UdpRelay(local_port+1,"127.0.0.1",remote_port+1,null);  
      }
      else
      {  printLog("local_port==remote_port --> no UDP relay is needed");
      }

      //debug...
      printLog("starting RAT");
    
      String cmds[] = {"","",""};
      cmds[0] = command;
      cmds[1] = remote_addr+"/"+remote_port;

      // try to start the RAT
      try
      {  media_process=Runtime.getRuntime().exec(cmds);
         return true;
      }
      catch (Exception e)
      {  e.printStackTrace();
         return false;
      }          
   }

   /** Stops media application */
   public boolean stopApp()
   {  printLog("stopping RAT");
      if (media_process!=null) media_process.destroy();
      return true;
   }


   // ****************************** Logs *****************************

   /** Adds a new string to the default Log */
   private void printLog(String str)
   {  if (log!=null) log.println("RatAudioApp: "+str,LoopbackMediaApp.LOG_OFFSET+Log.LEVEL_HIGH);  
      //if (LOG_LEVEL<=Log.LEVEL_HIGH) System.out.println("RatAudioApp: "+str);
      System.out.println("RatAudioApp: "+str);
   }
      
}