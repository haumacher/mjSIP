package local.media.jmf;



import java.io.*;

import javax.media.*;
import javax.media.format.*;
import javax.media.protocol.*;
import javax.media.control.TrackControl;

import java.util.Vector;



/** JmfMediaSender is a JMF-based media sender.
  */
public class JmfMediaSender
{
   /** Processor */
   Processor processor=null;
   /** MediaLocator */
   MediaLocator dest;
   /** DataSink */
   DataSink sink;


   /** Creates a JmfMediaSender. */
   public JmfMediaSender(String media_type, Format media_format, String dest_addr, int dest_port, String media_src)
   {  init(media_type,media_format,dest_addr,dest_port,media_src);  
   }


   /** Inits a JmfMediaSender. */
   private void init(String media_type, Format media_format, String dest_addr, int dest_port, String media_src)
   {  String result;
      
      // #### 1) set the media-locator
      MediaLocator media_locator;
      //try
      {  if (media_src==null) media_locator=getMediaLocator(media_type,media_format);
         else media_locator=new MediaLocator(media_src);
      }
      //catch (Exception e) { e.printStackTrace(); }  
      System.out.println("MediaLocator: "+media_locator.toString());
      
      // #### 2) create the processor
      result=createProcessor(media_locator);
      if (result!=null)
      {  System.out.println(result);
         //System.exit(0);
         return;
      }
      //else       
      System.out.println("Processor created");
               
      // #### 3) configure the processor
      processor.configure();
      while(processor.getState()<processor.Configured)
      {  // wait..
         //synchronized (getStateLock())
         //{  try { getStateLock().wait(); } catch (InterruptedException ie) { return; }
         //}
      }
      System.out.println("Processor configured");
      
      
      // now do step (3a) or step (3b)

      // I decided to do step 3a (only) in case of direct audio or video
      if (media_src==null)
      //if (false)
      {
      // #### 3a) set the output ContentDescriptor
         //System.out.println("Supported output formats: ");
         //Format[] formats=processor.getSupportedContentDescriptors();
         //for (int i=0; i<formats.length; i++) System.out.println(formats[i].toString());    
         processor.setContentDescriptor(new ContentDescriptor(ContentDescriptor.RAW_RTP));
         System.out.println("ContentDescriptor="+processor.getContentDescriptor().getContentType());
      }
      else
      {         
      // #### 3b.1) set the output ContentDescriptor
         processor.setContentDescriptor(new ContentDescriptor(ContentDescriptor.RAW));
         System.out.println("ContentDescriptor="+processor.getContentDescriptor().getContentType());
      
      // #### 3b.2) handle multiple tracks
         TrackControl[] tracks=processor.getTrackControls();
         System.out.println("Number of tracks="+tracks.length);
         boolean enabled=false;   
         for (int i=0; i<tracks.length; i++)
         {  TrackControl track_control=tracks[i];
            Format format=track_control.getFormat();
            System.out.println("track#"+i+" format:"+format.toString());
            if (!enabled)
            {  if (format.toString().indexOf("Stereo")>0 || format.toString().indexOf("Mono")>0)
               {  if (media_type.equals("audio")) enabled=track_control.setFormat(new AudioFormat(AudioFormat.GSM_RTP))!=null;
                  else enabled=false;
               }
               else
               {  if (media_type.equals("video")) enabled=track_control.setFormat(new VideoFormat(VideoFormat.H263_RTP))!=null;
                  else enabled=false;
               }
               track_control.setEnabled(enabled);
               System.out.println("track#"+i+" enabled="+enabled);
            }
            else
            {  track_control.setEnabled(false);
               System.out.println("track#"+i+" disabled");
            }
         }
      }      
      // #### 4) realize the processor
      processor.realize();
      while(processor.getState()<processor.Realized) ; // wait..
      System.out.println("Processor realized");
        
      String media_url="rtp://"+dest_addr+":"+dest_port+"/"+media_type+"/1";     
      dest=new MediaLocator(media_url);
   } 
 

   /** Starts sending the stream */
   public String start()
   {  // #### 5) create and start the DataSink
      try
      {  sink=Manager.createDataSink(processor.getDataOutput(),dest);
      }
      catch (NoDataSinkException e)
      {  e.printStackTrace();
         return "Failed creating DataSink";
      }
      System.out.println("DataSink created");
      System.out.println("Dest= "+sink.getOutputLocator()+" , "+sink.getContentType());
      try
      {  sink.open();
         System.out.println("DataSink opened");      
         sink.start();
      }
      catch (IOException e)
      {  e.printStackTrace();
         return "Failed starting DataSink";
      } 
      System.out.print("Start sending.. ");
      processor.start();
      System.out.println("OK");
      
      return null;
   }
   
   
   /** Stops the stream */
   public String stop()
   {  try
      {  sink.stop();
         sink.close();
      }
      catch (IOException e)
      {  e.printStackTrace();
         return "Failed closing DataSink";
      }
      System.out.print("Stop sending.. ");   
      processor.stop();
      processor.deallocate();
      processor.close();
      System.out.println("OK");
         
      return null;
   }

   /** Creates the processor */
   private String createProcessor(MediaLocator locator)
   {  if (locator==null) return "MediaLocator is null";
      // else 
      DataSource datasrc;  
      try
      {  datasrc=Manager.createDataSource(locator);
      }
      catch (Exception e)
      {  e.printStackTrace();
         return "Couldn't create DataSource";
      }
      try
      {  processor=Manager.createProcessor(datasrc);
      }
      catch (NoProcessorException ne)
      {  ne.printStackTrace();
         return "Couldn't create processor";
      }
      catch (IOException ie)
      {  ie.printStackTrace();
         return "IOException creating processor";
      }
      return null;
   }   


   /** Gets MediaLocator of the system audio. */
   //private MediaLocator getAudioLocator()
   //{  return getMediaLocator(new AudioFormat(AudioFormat.LINEAR,8000,8,1));
   //}
  
  
   /** Gets MediaLocator of the system video. */
   //private MediaLocator getVideoLocator()
   //{  return getMediaLocator(null);
   //}
 
  
   /** Gets MediaLocator for a system media device */
   private MediaLocator getMediaLocator(String media_type, Format media_format)
   {  if (media_format!=null) System.out.println("Selected format: "+media_format.toString());
      else
      {  System.out.println("Selected format: none");
         if (media_type.equalsIgnoreCase("audio"))
         {  media_format=new AudioFormat(AudioFormat.LINEAR,8000,16,1);
            System.out.println("Default audio format: "+media_format.toString());
         }
      }
      // get the CaptureDeviceInfo for the live audio or video capture device
      Vector deviceList=CaptureDeviceManager.getDeviceList(media_format);
      System.out.println("List of devices: "+deviceList.size());
      if (deviceList.size()==0)
      {  System.out.println("No device found supporting such format");
         return null;
      }
      for (int i=0; i<deviceList.size();i++)
      {  CaptureDeviceInfo di=(CaptureDeviceInfo)deviceList.elementAt(i);
         System.out.println("device "+i+":"+di.getName());
      }
      
      CaptureDeviceInfo di=(CaptureDeviceInfo)deviceList.elementAt(deviceList.size()-1);
      //System.out.println("Supported media formats: ");
      //Format[] formats=di.getFormats();
      //for (int i=0; i<formats.length; i++) System.out.println(formats[i].toString());
      MediaLocator media_locator=di.getLocator();
      //System.out.println("MediaLocator: "+media_locator);
      return media_locator;
   }
  
  
  
  
  // ******************************* MAIN *******************************

   /** The main method. */
   public static void main(String[] args)
   {
      if (args.length<3)
      {
         System.out.println("usage:\n  java JmfMediaSender audio|video <dest_addr> <dest_port> [<media>]");
         System.out.println("\n    with: <media> = \"file://filename\"");
      }
      else
      {  
         String media_type=args[0];
         String dest_addr=args[1];
         int dest_port=Integer.parseInt(args[2]);
         String media_src=(args.length>3)? args[3]: null;         
         JmfMediaSender sender=new JmfMediaSender(media_type,null,dest_addr,dest_port,media_src);

         String result=sender.start();
         if (result!=null)
         {  System.out.println("ERROR: "+result); 
            System.exit(0);
         }
         
         System.out.println("Press 'Return' to stop");
         try { System.in.read(); } catch (IOException e) { e.printStackTrace(); }

         result=sender.stop();
         if (result!=null)
         {  System.out.println("ERROR: "+result); 
            System.exit(0);
         }
      }
   }

}