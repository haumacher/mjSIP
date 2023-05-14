package local.media.jmf;



import local.net.*;
import org.zoolu.net.*;
import org.zoolu.tools.Random;
import java.util.*;



/** JmfMMReceiver is a JMF-based media receiver that plays multiple RTP fows
  * received at the same UDP port (external UDP socket).
  * <p/>
  * Incoming RTP packets are received at an external UDP port (using the given external UDP socket),
  * demultimplied on the basis of the SSRC field,
  * and relayed to the corrsponding player from the internal UDP port (using the given internal UDP socket).
  */
public class JmfMMReceiver implements UdpProviderListener
{
   /** Begin of available ports */
   private static int PORT_SET_BEGIN=40000;
   /** Length of available ports */
   private static int PORT_SET_LEN=10000;

   /** Media type */
   String media_type;

   /** External UDP provider for RTP */
   UdpProvider ext_udp;

   /** Internal UDP socket for RTP */
   UdpSocket int_socket;

   /** Local JMF receivers, as table:(Long)sscr-->(JmfMediaReceiver)receivers */
   Hashtable receivers;

   /** Local host */
   static final IpAddress localhost=new IpAddress("127.0.0.1");



   /** Constructs a JmfMMReceiver.
     * @media_type media type (audio|video) 
     * @ext_udp external UDP socket where remote RTP flows are received at
     * @int_udp internal UDP socket used to relay RTP flows to local receivers */
   public JmfMMReceiver(String media_type, UdpSocket ext_socket, UdpSocket int_socket)
   {  this.media_type=media_type;
      this.int_socket=int_socket;
      ext_udp=new UdpProvider(ext_socket,this);
      receivers=new Hashtable();
   }
   

   /** Stops the JmfMMReceiver */
   public void halt()
   {  for (Enumeration e=receivers.elements(); e.hasMoreElements(); )
      {  ((JmfMediaReceiver)e.nextElement()).stop();
      }
      receivers.clear();
      ext_udp.halt();
      ext_udp=null;
   }
  

   /** From UdpProviderListener. When a new UDP datagram is received. */
   public void onReceivedPacket(UdpProvider udp, UdpPacket packet)
   {  if (packet.getLength()<12) return;
      // else
      if (udp==ext_udp)
      {  RtpPacket rtp_packet=new RtpPacket(packet.getData(),packet.getLength());
         Long sscr=new Long(rtp_packet.getSscr());
         if (!receivers.containsKey(sscr))
         {  final JmfMediaReceiver recv=new JmfMediaReceiver(media_type,pickFreePort()) {  public void exit() {  stop();  }  };
            (new Thread() {  public void run() {  recv.start();  }  }).start();
            receivers.put(sscr,recv);
         }
         JmfMediaReceiver recv=(JmfMediaReceiver)receivers.get(sscr);
         if (recv!=null)
         {  packet.setIpAddress(localhost);
            packet.setPort(recv.getPort());
            try {  int_socket.send(packet);  } catch (java.io.IOException e) {  e.printStackTrace();  }
         }
      }
   }  


   /** Picks a new free port. */
   public static int pickFreePort()
   {  return PORT_SET_BEGIN+2*Random.nextInt(PORT_SET_LEN/2);
   }  


   /** From UdpProviderListener. When UdpProvider terminates. */
   public void onServiceTerminated(UdpProvider udp, Exception error)
   {
   }  


   // ******************************* MAIN *******************************

   /** The main method. */
   public static void main(String[] args)
   {
      if (args.length>=2)
      try
      {  String media_type=args[0];
         int port=Integer.parseInt(args[1]);
         JmfMMReceiver receiver=new JmfMMReceiver(media_type,new UdpSocket(port),new UdpSocket(JmfMMReceiver.pickFreePort()));
         return;
      }
      catch (Exception e) { System.out.println("Error creating the receiver"); }

      System.out.println("usage:\n  java JmfMMReceiver audio|video <local_port>");
   }

}


