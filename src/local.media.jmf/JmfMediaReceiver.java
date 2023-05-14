package local.media.jmf;



import javax.media.*;
import javax.media.format.*;
import java.util.Vector;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.Border;



/** JmfMediaReceiver is a JMF-based media receiver.
  */
public class JmfMediaReceiver implements ControllerListener
{
   /** Media type */
   String media_type;

   /** Local port */
   int media_port;

   /** Player */
   Player player=null;

   /** GUI */
   JmfReceiverGUI gui=null;



   /** Creates a JmfMediaReceiver. */
   public JmfMediaReceiver(String media_type, int media_port)
   {  init(media_type,media_port,media_type.equalsIgnoreCase("video"));
   }
  

   /** Creates a JmfMediaReceiver. */
   public JmfMediaReceiver(String media_type, int media_port, boolean with_gui)
   {  init(media_type,media_port,with_gui);
   }
  

   /** Inits a JmfMediaReceiver. */
   private void init(String media_type, int media_port, boolean with_gui)
   {  this.media_type=media_type;
      this.media_port=media_port;
      if (with_gui) gui=new JmfReceiverGUI(this);
      try
      {  String media_url="rtp://:"+media_port+"/"+media_type;
         System.out.println("Receiver URL= "+media_url);
         MediaLocator media_locator=new MediaLocator(media_url);
         player=Manager.createPlayer(media_locator);
         if (player!=null) player.addControllerListener(this);
         else System.out.println("Player cannot be created");
      }
      catch(Exception e) { e.printStackTrace(); }
   }


   /** Gets the media type. */
   public String GetMediaType()
   {  return media_type;
   }


   /** Gets local media port. */
   public int getPort()
   {  return media_port;
   }


   /** Starts receiving the stream. */
   public String start()
   {  String err=null;
      try 
      {  System.out.println("Trying to realize the player");
         player.realize();
         while(player.getState()!=player.Realized);
         System.out.println("Player realized");
         player.start();
      }
      catch (Exception e)
      {  e.printStackTrace();
         err="Failed trying to start the player";
      }
      return err;
   }


   /** Stops the receiver. */
   public String stop()
   {  if (player!=null)
      {  player.stop();
         player.deallocate();
         player.close();
         System.out.println("Player stopped");
         player=null;
         if (gui!=null) gui.dispose();
      }
      return null;
   }


   /** Exits the program. */
   public void exit()
   {  stop();
      System.exit(0);
   }


   /** Updates the receiver controller. */
   public synchronized void controllerUpdate(ControllerEvent event)
   {  if (gui!=null) gui.controllerUpdate(event);
   }
   
   /** Gets the visual component. */
   public java.awt.Component getVisualComponent()
   {  return player.getVisualComponent();
   }
   
   /** Gets the control component. */
   public java.awt.Component getControlPanelComponent()
   {  return player.getControlPanelComponent();
   }
   
   /*public Player getPlayer()
   {  return player;
   }*/
   

   // ******************************* MAIN *******************************

   /** The main method. */
   public static void main(String[] args)
   {
      if (args.length>=2)
      try
      {  int port=Integer.parseInt(args[1]);
         JmfMediaReceiver media_receiver=new JmfMediaReceiver(args[0],port);
         media_receiver.start();
         return;
      }
      catch (Exception e) { System.out.println("Error creating the receiver"); }
      
      System.out.println("usage:\n  java JmfMediaReceiver audio|video <local_port>");
   }

}


// *********************** class JmfReceiverGUI ***********************

/** Graphical user interface of the receiver.
  */
class JmfReceiverGUI extends Frame
{
   /** Icon */
   //static Image ICON=Toolkit.getDefaultToolkit().getImage("media/local/media/player.gif");
   static Image ICON=org.zoolu.tools.Archive.getImage(org.zoolu.tools.Archive.getJarURL("lib/ua.jar","media/local/media/player.gif"));

   /** Title */
   static String TITLE="mjPlayer";
   
   /** Panel */
   Panel panel=new Panel();

   /** Receiver */
   JmfMediaReceiver receiver;



   /** Creates the JmfReceiverGUI. */
   public JmfReceiverGUI(final JmfMediaReceiver receiver)
   {  this.receiver=receiver;
      try
      {  this.setTitle(TITLE);
         this.setIconImage(ICON);
         this.addWindowListener(new java.awt.event.WindowAdapter()
         {  public void windowClosing(WindowEvent e) {  receiver.exit();  }
         });
         panel.setLayout(new BorderLayout());
         this.add(panel);
   
         this.setSize(new Dimension(340,300));
         Dimension screenSize=Toolkit.getDefaultToolkit().getScreenSize();
         Dimension frameSize=this.getSize();
         if (frameSize.height>screenSize.height)
         frameSize.height=screenSize.height;
         if (frameSize.width>screenSize.width)
         frameSize.width=screenSize.width;
         this.setLocation((screenSize.width-frameSize.width)/2,(screenSize.height-frameSize.height)/2);
         this.setVisible(true);
         this.toFront();
      }
      catch(Exception e) { e.printStackTrace(); }
   }


   /** Updates the receiver controller. */
   public synchronized void controllerUpdate(ControllerEvent event)
   {  //System.out.println("DEBUG: controllerUpdate()");
      if (event instanceof RealizeCompleteEvent || event instanceof FormatChangeEvent)
      {  if (event instanceof RealizeCompleteEvent) System.out.println("RealizeComplete event");
         if (event instanceof FormatChangeEvent) System.out.println("FormatChange event");
         Component visual_comp=receiver.getVisualComponent();
         if (visual_comp!=null) panel.add("Center",visual_comp);
         Component control_comp=receiver.getControlPanelComponent();       
         if (control_comp!=null) panel.add("South",control_comp);
         this.setVisible(true);
         this.toFront();
         return;
      }
      else
      {  //System.out.println("Event: "+event.toString()+": Do nothing");
      }
   }

}
