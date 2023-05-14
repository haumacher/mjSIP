package local.media;



import org.zoolu.net.*;
import org.zoolu.sound.ExtendedAudioSystem;
import org.zoolu.tools.Log;
import javax.sound.sampled.*;
import java.io.*;



/** Push-To-Talk (PTT) application. 
  */
public class Push2TalkApp implements MediaApp, UdpProviderListener
{
   /** Maximum datagram size (MTU). Bigger datagrams are fragmented. */
   static final int MAX_PKT_SIZE=1012;

   /** Minimum datagram size. Smaller datagrams are silently discarded) */
   static final int MIN_PKT_SIZE=4;

   /** Time between two keepalive datagrams [millisecs] */
   static final long KEPPALIVE_TIME=4000;

   /** Log */
   Log log=null;

   /** Audio format */
   AudioFormat audio_format=new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,8000.0F,16,1,2,8000.0F,false);

   /** Audio type */
   String audio_type="audio/x-wav";

   /** Audio file type */
   AudioFileFormat.Type file_type=AudioFileFormat.Type.WAVE;
   

   /** Encoding */
   //String encoding="encoding=pcm&rate=8000&bits=16&channels=1";

   /** Captured audio input stream */
   AudioInputStream audio_input_stream;

   /** Buffer */
   ByteArrayOutputStream buffer;

   /** Whether it is recording */
   boolean is_recording=false;
   
   /** Player */
   Clip player=null;

   /** Remote address */
   IpAddress remote_addr;
   
   /** Remote port */
   int remote_port;

   /** UDP socket */
   UdpSocket udp_socket=null;

   /** UDP layer */
   UdpProvider udp=null;
   
   /** UDP keepalive */
   UdpKeepAlive udp_keepalive=null;

   /** GUI */
   Push2TalkFrame gui;

   /** Last Push2TalkApp instance */
   static Push2TalkApp INSTANCE=null;



   /** Gets last Push2TalkApp instance. */
   public static Push2TalkApp getLastInstance()
   {  return INSTANCE;
   }


   /** Creates a new Push2TalkApp. */
   public Push2TalkApp(FlowSpec flow_spec, Log log)
   {  this.log=log;
      int local_port=flow_spec.getLocalPort();
      remote_addr=new IpAddress(flow_spec.getRemoteAddress());
      remote_port=flow_spec.getRemotePort();
      try
      {  //udp=new UdpProvider(udp_socket=new UdpSocket(local_port),this);
         udp=new UdpProvider(udp_socket=new JumboUdpSocket(local_port,MAX_PKT_SIZE),this);
      }
      catch(Exception e) {  e.printStackTrace();  printLog(e.getMessage());  }

      // javax sound
      audio_input_stream=ExtendedAudioSystem.getInputStream(audio_format); 
                        
      INSTANCE=this;
      
      // DEBUG
      //org.zoolu.net.JumboUdpSocket.DEBUG=true;
      org.zoolu.net.JumboUdpSocket.DEPARTURE_TIME=10;
   }


   /** Starts media application */
   public boolean startApp()
   {  udp_keepalive=new UdpKeepAlive(udp_socket,new SocketAddress(remote_addr,remote_port),KEPPALIVE_TIME);
      gui=new Push2TalkFrame(this);
      return true;     
   }


   /** Stops media application */
   public boolean stopApp()
   {  udp_keepalive.halt();
      udp_keepalive=null;
      udp.halt();
      udp=null;
      udp_socket.close();
      gui.closeWindow();
      return true;
   }


   /** Starts recording. */
   public void record()
   {  if (!is_recording)
      {  //try
         {  buffer=new ByteArrayOutputStream();
            //AudioSystem.write(audio_input_stream,file_type,buffer)
            printLog("record");
            ExtendedAudioSystem.startAudioInputLine();      
            final int frame_size=audio_format.getFrameSize();
            (new Thread(){  public void run(){  doRecording(buffer,audio_input_stream,frame_size);  }  }).start();
            is_recording=true;
         }
         //catch (IOException e) {  e.printStackTrace();  printLog(e.getMessage());  }
      }
   }


   /** Does the actual recording coping audio frames from an AudioInputStream to a OutputStream. */
   private void doRecording(OutputStream os, AudioInputStream audio_is, int frame_size)
   {  try
      {  byte[] frame=new byte[frame_size];
         while(audio_is.read(frame)>0) os.write(frame);
      }
      catch (IOException e) {  e.printStackTrace();  printLog(e.getMessage());  }
   }


   /** Stops recording and sends captured audio. */
   public void send()
   {  if (is_recording)
      {  try
         {  ExtendedAudioSystem.stopAudioInputLine();
            is_recording=false;
            byte[] buff=buffer.toByteArray();
            ByteArrayInputStream bis=new ByteArrayInputStream(buff);
            AudioInputStream ais=new AudioInputStream(bis,audio_format,buff.length/audio_format.getFrameSize());
            ByteArrayOutputStream bos=new ByteArrayOutputStream();
            AudioSystem.write(ais,file_type,bos);
            //AudioSystem.write(ais,file_type,new File("temp.wav"));
            //play(bos.toByteArray());
            printLog("send ["+bos.size()+"B]");
            send(bos.toByteArray());
         }
         catch (IOException e) {  e.printStackTrace();  printLog(e.getMessage());  }   
      }
   }


   /** Sends the given audio buffer. */
   void send(byte[] buffer)
   {  if (udp!=null)
      try 
      {  UdpPacket packet=new UdpPacket(buffer,buffer.length,remote_addr,remote_port);
         udp.send(packet);
      }
      catch (IOException e) {  e.printStackTrace();  printLog(e.getMessage());  }   
   }


   /** Plays the given audio buffer. */
   void play(byte[] buffer, int offset, int length)
   {  if (length<MIN_PKT_SIZE) return;
      // else
      try
      {  if (player!=null)
         {  player.stop();
            player.close();
         }
         printLog("play ["+length+"B]");
         AudioInputStream ais=AudioSystem.getAudioInputStream(new ByteArrayInputStream(buffer,offset,length));
         AudioFormat format=ais.getFormat();
         DataLine.Info info=new DataLine.Info(Clip.class,format);
         player=(Clip)AudioSystem.getLine(info);
         player.open(ais);
         player.start();
      }
      catch (LineUnavailableException e) {  e.printStackTrace();  printLog(e.getMessage());  }   
      catch (UnsupportedAudioFileException e) {  e.printStackTrace();  printLog(e.getMessage());  }   
      catch (IOException e) {  e.printStackTrace();  printLog(e.getMessage());  }   
   }


   // ************************ Callback methods ***********************

   /** From UdpProvider. When a new UDP datagram is received. */
   public void onReceivedPacket(UdpProvider udp, UdpPacket packet)
   {  //System.err.println("DEBUG: PTT: DATA received");
      play(packet.getData(),packet.getOffset(),packet.getLength());
   }

   /** From UdpProvider. WWhen UdpProvider terminates. */
   public void onServiceTerminated(UdpProvider udp, Exception error)
   {
   }


   // ****************************** Logs *****************************

   /** Adds a new string to the default Log */
   private void printLog(String str)
   {  printLog(str,Log.LEVEL_HIGH);
   }


   /** Adds a new string to the default Log */
   private void printLog(String str, int level)
   {  if (log!=null) log.println("PTT: "+str,LoopbackMediaApp.LOG_OFFSET+level);  
      if (level<=Log.LEVEL_HIGH) System.out.println("Push2TalkApp: "+str);
   }


   // ****************************** Main *****************************

   /** Waits for <i>millisecs</i>. */
   private static void sleep(long millisecs)
   {  try {  Thread.sleep(millisecs);  } catch(Exception e) {}
   }


   /** Main method. */
   public static void main(String[] args)
   {  //Log log=new Log(System.out,5);
      FlowSpec flow_spec=new FlowSpec(null,4444,"127.0.0.1",4444,FlowSpec.FULL_DUPLEX);
      final Push2TalkApp ppt=new Push2TalkApp(flow_spec,null);
      ppt.startApp();
      for (int i=0; i<4; i++)
      {  ppt.record();
         sleep(4000);
         ppt.send();
      }
      sleep(4000);
      ppt.stopApp();
      
      System.exit(0);
   }
   
}
