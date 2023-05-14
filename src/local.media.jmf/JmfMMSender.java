package local.media.jmf;



import local.net.*;
import org.zoolu.net.*;
import org.zoolu.tools.Random;
import java.util.*;

import javax.media.Format;



/** JmfMMSender is a JMF-based media sender that may send multiple RTP flows
  * through the same UDP port.
  */
public class JmfMMSender implements UdpProviderListener
{
   /** Media type */
   String media_type;

   /** Internal UDP provider for RTP */
   UdpProvider int_udp;

   /** External UDP socket for RTP */
   UdpSocket ext_socket;

   /** Remote socket address */
   SocketAddress remote_soaddr;

   /** List of local JMF senders, as vector of JmfMediaSender */
   Vector senders;

   /** Media source */
   String media_source;

   /** Local host */
   static final String LOCALHOST="127.0.0.1";



   /** Constructs a JmfMMSender.
     * @media_type media type (audio|video)
     * @media_format media format
     * @ext_udp internal UDP socket where local RTP flows are received at
     * @int_udp external UDP socket where local RTP flows are relayed from
     * @remote_soaddr remote socket address where RTP flows are sent to
     * @media_source media source, in case it differs from the standard system audio (mic) or video (cam) */
   public JmfMMSender(String media_type, Format media_format, UdpSocket int_socket, UdpSocket ext_socket, SocketAddress remote_soaddr, String media_source)
   {  this.media_type=media_type;
      this.int_udp=new UdpProvider(int_socket,this);
      this.ext_socket=ext_socket;
      this.remote_soaddr=remote_soaddr;
      this.media_source=media_source;
      senders=new Vector();
      JmfMediaSender sender=new JmfMediaSender(media_type,media_format,LOCALHOST,int_socket.getLocalPort(),media_source);
      senders.addElement(sender);
      sender.start();
   }
   

   /** Stops the JmfMMSender */
   public void halt()
   {  int_udp.halt();
      (new Thread() {  public void run()
      {  for (Enumeration e=senders.elements(); e.hasMoreElements(); )
         {  ((JmfMediaSender)e.nextElement()).stop();
         }
      }}).start();
   }
  

   /** From UdpProviderListener. When a new UDP datagram is received. */
   public void onReceivedPacket(UdpProvider udp, UdpPacket packet)
   {  packet.setIpAddress(remote_soaddr.getAddress());
      packet.setPort(remote_soaddr.getPort());
      try {  ext_socket.send(packet);  } catch (java.io.IOException e) {  e.printStackTrace();  }
   }  


   /** From UdpProviderListener. When UdpProvider terminates. */
   public void onServiceTerminated(UdpProvider udp, Exception error)
   {
   }  


   // ******************************* MAIN *******************************

   /** The main method. */
   public static void main(String[] args)
   {
      JmfMMSender sender;
      try
      {  String media_type=args[0];
         int local_port=Integer.parseInt(args[1]);
         String remote_addr=args[2];
         int remote_port=Integer.parseInt(args[3]);
         String media_src=(args.length>4)? args[4] : null;  
         sender=new JmfMMSender(media_type,null,new UdpSocket(local_port+2),new UdpSocket(local_port),new SocketAddress(remote_addr,remote_port),media_src);
      }
      catch (Exception e)
      {  System.err.println("Error creating the sender");
         System.err.println("usage:\n  java JmfMMSender audio|video <local_port> <dest_addr> <dest_port> [<media>] ");
         System.err.println("\n    with: <media> = \"file://filename\"");
         return;
      }
      System.out.println("Press 'Return' to stop");
      try { System.in.read(); } catch (java.io.IOException e) { e.printStackTrace(); }
      sender.halt();
   }

}


