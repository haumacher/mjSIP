package local.media;



import java.awt.*;
import java.awt.event.*;
import javax.swing.*;



/** Push-To-Talk (PTT) user interface. 
 */
class Push2TalkFrame extends Frame
{
   private static final String title="Push-To-Talk"; // title
   private static final int W_Width=320; // window width
   private static final int W_Height=90; // window height
   private static final int B_Height=30; // buttons and combobox height (total)

   Button button1=new Button();
   Label label1=new Label();
   Push2TalkApp ptt_app;
   boolean is_recording=false; 



   /** Creates a new Push2TalkFrame. */
   public Push2TalkFrame(Push2TalkApp ptt_app)
   {  this.ptt_app=ptt_app;
      try  {  jbInit();  } catch(Exception e) {  e.printStackTrace();  }
      setPlaying();
   }


   /** Inits the Push2TalkFrame. */
   private void jbInit() throws Exception
   {  
      // set frame dimensions
      this.setSize(W_Width,W_Height);
      Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
      Dimension frameSize = this.getSize();
      if (frameSize.height > screenSize.height) frameSize.height = screenSize.height;
      if (frameSize.width > screenSize.width) frameSize.width = screenSize.width;
      this.setLocation((screenSize.width - frameSize.width)/2 - 40, (screenSize.height - frameSize.height)/2 - 40);
      this.setResizable(false);
   
      this.setTitle(title);
      this.addWindowListener(new java.awt.event.WindowAdapter()
      {  public void windowClosing(WindowEvent e) {  closeWindow();  }
      });
      button1.addActionListener(new java.awt.event.ActionListener()
      {  public void actionPerformed(ActionEvent e) {  push();  }
      });
      this.add(button1, BorderLayout.SOUTH);
      //label1.setFont(new java.awt.Font("Monospaced", 0, 12));
      label1.setAlignment(1);
      this.add(label1,BorderLayout.CENTER);

      this.setVisible(true);
   }


   /** Closes the frame. */
   public void closeWindow()
   {  this.dispose();
   }


   /** Pushes. */
   void push()
   {  if (is_recording)
      {  setPlaying();
         ptt_app.send();
      }
      else
      {  setRecording();
         ptt_app.record();
      }
   }


   /** Sets recording mode. */
   void setRecording()
   {  label1.setText("Recording");
      label1.setBackground(new Color(0xFF0000));
      button1.setLabel("SEND");
      is_recording=true;
   }


   /** Sets playing mode. */
   void setPlaying()
   {  label1.setText("Playing");
      //label1.setBackground(new Color(0x00C000));
      label1.setBackground(new Color(0x8080FF));
      button1.setLabel("REC");
      is_recording=false;
   }
  
}
