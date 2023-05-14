package local.media.jmf;



import local.media.*;
import org.zoolu.net.*;
import org.zoolu.tools.Log;
import org.zoolu.tools.Archive;

import javax.media.Format;



/** Media (audio|video) appication.
  */
public class JmfMediaApp implements MediaApp
{
   /** Log */
   Log log=null;
   
   /** Media type */
   String media_type;

   /** Media type */
   Format media_format=null;

   /** Media source */
   String media_source;

   /** Local port */
   int local_port;
   /** Remote socket address */
   SocketAddress remote_soaddr;
   
   /** Stream direction */
   FlowSpec.Direction dir;

   /** Runtime media process */
   Process media_process=null;

   /** Sender */
   JmfMMSender sender=null;   
   /** Receiver */
   JmfMMReceiver receiver=null;

   /** Internal UDP socket */
   UdpSocket int_socket=null;
   /** External UDP socket */
   UdpSocket ext_socket=null;



   /** Creates a new JmfMediaApp. */
   public JmfMediaApp(FlowSpec flow_spec, String media_source, Log log)
   {  this.log=log;
      this.media_source=media_source;
      media_type=flow_spec.getMediaSpec().getType();
      local_port=flow_spec.getLocalPort();
      remote_soaddr=new SocketAddress(flow_spec.getRemoteAddress(),flow_spec.getRemotePort());
      dir=flow_spec.getDirection();
      String debug_dir=(dir==FlowSpec.SEND_ONLY)? "SEND_ONLY" : ((dir==FlowSpec.RECV_ONLY)? "RECV_ONLY" : "FULL_DUPLEX");
      String debug_arrow=(dir==FlowSpec.SEND_ONLY)? "-->" : ((dir==FlowSpec.RECV_ONLY)? "<--" : "<-->");
      printLog(debug_dir+": "+local_port+debug_arrow+remote_soaddr.toString());
      // Patch for working with JMF with local streams
      //if (remote_addr.startsWith("127."))
      //{  printLog("Patch for JMF: replaced local destination address "+remote_addr+" with 255.255.255.255");
      //   remote_addr="255.255.255.255";
      //}
   }


   /** Starts media application */
   public boolean startApp()
   {  printLog("starting JMF "+media_type);   
      try
      {  int_socket=new UdpSocket(JmfMMReceiver.pickFreePort());
         ext_socket=new UdpSocket(local_port);
         if (dir==FlowSpec.SEND_ONLY || dir==FlowSpec.FULL_DUPLEX) sender=new JmfMMSender(media_type,media_format,int_socket,ext_socket,remote_soaddr,media_source);
         if (dir==FlowSpec.RECV_ONLY || dir==FlowSpec.FULL_DUPLEX) receiver=new JmfMMReceiver(media_type,ext_socket,int_socket);
      }
      catch(java.net.SocketException e)
      {  printLog(e.getMessage());
         e.printStackTrace();
         return false;
      }
      return true;
   }


   /** Stops media application */
   public boolean stopApp()
   {  printLog("stopping JMF "+media_type);

      if (sender!=null) sender.halt();
      if (receiver!=null) receiver.halt();
      
      if (int_socket!=null) int_socket.close();
      if (ext_socket!=null) ext_socket.close();
      int_socket=null;
      ext_socket=null;
            
      return true;      
   }



   // ****************************** Logs *****************************

   /** Default log level offset */
   static final int LOG_OFFSET=0;


   /** Adds a new string to the default Log */
   private void printLog(String str)
   {  if (log!=null) log.println("JmfMediaApp<"+media_type+">: "+str,LOG_OFFSET+Log.LEVEL_HIGH);  
      //if (LOG_LEVEL<=Log.LEVEL_HIGH) System.out.println("JmfMediaApp: "+str);
      System.out.println("JmfMediaApp<"+media_type+">: "+str);
   }

}