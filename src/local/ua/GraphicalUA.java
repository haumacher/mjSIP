/*
 * Copyright (C) 2005 Luca Veltri - University of Parma - Italy
 * 
 * This source code is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This source code is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this source code; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 * 
 * Author(s):
 * Luca Veltri (luca.veltri@unipr.it)
 */

package local.ua;



import local.media.MediaDesc;
import org.zoolu.sip.address.*;
import org.zoolu.sip.provider.*;
import org.zoolu.tools.*;

import java.util.Vector;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.MalformedURLException;
import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import javax.swing.plaf.basic.BasicComboBoxEditor;



/** Simple GUI-based SIP user agent (UA). */
public class GraphicalUA extends JFrame implements UserAgentListener
{
   /** This application */
   final String app_name="mjUA (http://www.mjsip.org)";

   /** Log */
   protected Log log;
   
   /** SipProvider. */
   protected SipProvider sip_provider;

   /** User Agent */
   protected UserAgent ua;

   /** UserAgentProfile */
   protected UserAgentProfile ua_profile;

   /** Title */
   //String user_name=app_name;

   /** Buddy list size */
   protected static final int NMAX_CONTACTS=10;

   /** Buddy list */
   protected StringList buddy_list;

   private static final int W_Width=320; // window width
   private static final int W_Height=90; // window height
   private static final int C_Height=30; // buttons and combobox height (total)

   /** Media file path */
   //final String MEDIA_PATH="media/local/ua/";
   
   final String CALL_GIF=/*MEDIA_PATH+*/"call.gif";
   final String HANGUP_GIF=/*MEDIA_PATH+*/"hangup.gif";
   
   Icon icon_call;
   Icon icon_hangup;
   //Icon icon_call=new ImageIcon("media/ua/call.gif");
   //Icon icon_hangup=new ImageIcon("media/ua/hangup.gif");

   JPanel jPanel1 = new JPanel();
   JPanel jPanel2 = new JPanel();
   JPanel jPanel3 = new JPanel();
   JPanel jPanel4 = new JPanel();
   JComboBox jComboBox1 = new JComboBox();
   BorderLayout borderLayout1 = new BorderLayout();
   BorderLayout borderLayout2 = new BorderLayout();
   JPanel jPanel5 = new JPanel();
   GridLayout gridLayout2 = new GridLayout();
   GridLayout gridLayout3 = new GridLayout();
   JButton jButton1 = new JButton();
   JButton jButton2 = new JButton();
   ComboBoxEditor comboBoxEditor1=new BasicComboBoxEditor();
   BorderLayout borderLayout3 = new BorderLayout();

   JTextField display=new JTextField();




   // ************************* UA internal state *************************
     
   /** UA_IDLE=0 */
   protected static final String UA_IDLE="IDLE";
   /** UA_INCOMING_CALL=1 */
   protected static final String UA_INCOMING_CALL="INCOMING_CALL";
   /** UA_OUTGOING_CALL=2 */
   protected static final String UA_OUTGOING_CALL="OUTGOING_CALL";
   /** UA_ONCALL=3 */
   protected static final String UA_ONCALL="ONCALL";
   
   /** Call state: <P>UA_IDLE=0, <BR>UA_INCOMING_CALL=1, <BR>UA_OUTGOING_CALL=2, <BR>UA_ONCALL=3 */
   String call_state=UA_IDLE;
   

   /** Changes the call state */
   protected void changeStatus(String state)
   {  call_state=state;
      printLog("state: "+call_state,Log.LEVEL_MEDIUM); 
   }

   /** Checks the call state */
   protected boolean statusIs(String state)
   {  return call_state.equals(state); 
   }

   /** Gets the call state */
   protected String getStatus()
   {  return call_state; 
   }


   // *************************** Public methods **************************

   /** Creates a new GraphicalUA */
   public GraphicalUA(SipProvider sip_provider, UserAgentProfile ua_profile)
   {
      this.sip_provider=sip_provider;
      this.ua_profile=ua_profile;
      log=sip_provider.getLog();
      
      ua=new UserAgent(sip_provider,ua_profile,this);
      //ua.listen();
      changeStatus(UA_IDLE);
      String jar_file=ua_profile.ua_jar;
      if (jar_file!=null)
      {  icon_call=Archive.getImageIcon(Archive.getJarURL(jar_file,ua_profile.media_path+CALL_GIF));
         icon_hangup=Archive.getImageIcon(Archive.getJarURL(jar_file,ua_profile.media_path+HANGUP_GIF));
      }
      else
      try
      {  icon_call=Archive.getImageIcon(new URL(ua_profile.media_path+CALL_GIF));
         icon_hangup=Archive.getImageIcon(new URL(ua_profile.media_path+HANGUP_GIF));
      }
      catch (MalformedURLException e) { e.printStackTrace(); }      

      if (ua_profile.buddy_list_file!=null && (ua_profile.buddy_list_file.startsWith("http://") || ua_profile.buddy_list_file.startsWith("file:/")))
      {  try
         {  buddy_list=new StringList(new URL(ua_profile.buddy_list_file));
         }
         catch (MalformedURLException e) { e.printStackTrace(); buddy_list=new StringList((String)null); }
      }
      else buddy_list=new StringList(ua_profile.buddy_list_file);
      jComboBox1=new JComboBox(buddy_list.getElements());

      try
      {
         jbInit();
      }
      catch(Exception e) { e.printStackTrace(); }
            
      //Image image=Archive.getImage(Archive.getJarURL(jar_file,"media/local/ua/intro.gif"));
      //PopupFrame about=new PopupFrame("About",image,this);
      //try  {  Thread.sleep(3000);  } catch(Exception e) {  }
      //about.closeWindow();
      
      run();   
   }


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

      this.setTitle(sip_provider.getContactAddress(ua_profile.user).toString());
      this.addWindowListener(new java.awt.event.WindowAdapter()
      {  public void windowClosing(WindowEvent e) { exit(); }
      });
      jPanel1.setLayout(borderLayout3);
      jPanel2.setLayout(borderLayout2);
      display.setBackground(Color.black);
      display.setForeground(Color.green);
      display.setEditable(false);
      display.setText(app_name);
      jPanel4.setLayout(borderLayout1);
      jPanel5.setLayout(gridLayout2);
      jPanel3.setLayout(gridLayout3);
      gridLayout3.setRows(2);
      gridLayout3.setColumns(1);
      if (icon_call!=null && icon_call.getIconWidth()>0) jButton1.setIcon(icon_call);
      else jButton1.setText("Call");
      jButton1.addActionListener(new java.awt.event.ActionListener()
      {  public void actionPerformed(ActionEvent e) { jButton1_actionPerformed(); }
      });
      jButton1.addKeyListener(new java.awt.event.KeyAdapter()
      {  public void keyTyped(KeyEvent e) { jButton1_actionPerformed(); }
      });
      if (icon_hangup!=null && icon_hangup.getIconWidth()>0) jButton2.setIcon(icon_hangup);
      else jButton2.setText("Hungup");
      jButton2.addActionListener(new java.awt.event.ActionListener()
      {  public void actionPerformed(ActionEvent e) { jButton2_actionPerformed(); }
      });
      jButton2.addKeyListener(new java.awt.event.KeyAdapter()
      {  public void keyTyped(KeyEvent e) { jButton2_actionPerformed(); }
      });
      jComboBox1.addActionListener(new java.awt.event.ActionListener()
      {  public void actionPerformed(ActionEvent e) { jComboBox1_actionPerformed(e); }
      });
      comboBoxEditor1.addActionListener(new java.awt.event.ActionListener()
      {  public void actionPerformed(ActionEvent e) { comboBoxEditor1_actionPerformed(e); }
      });
      jButton2.setFont(new java.awt.Font("Dialog", 0, 10));
      jButton1.setFont(new java.awt.Font("Dialog", 0, 10));
      comboBoxEditor1.getEditorComponent().setBackground(Color.yellow);
      jComboBox1.setEditable(true);
      jComboBox1.setEditor(comboBoxEditor1);
      jComboBox1.setSelectedItem(null);
      jPanel3.setPreferredSize(new Dimension(0,C_Height));
      this.getContentPane().add(jPanel1, BorderLayout.CENTER);
      jPanel1.add(jPanel2, BorderLayout.CENTER);
      jPanel1.add(jPanel3, BorderLayout.SOUTH);
      jPanel2.add(display, BorderLayout.CENTER);
      jPanel3.add(jPanel4, null);
      jPanel3.add(jPanel5, null);
      jPanel4.add(jComboBox1, BorderLayout.CENTER);
      jPanel5.add(jButton1, null);
      jPanel5.add(jButton2, null);

      // show it
      this.setVisible(true);
   }



   /** Starts the UA */
   void run()
   {
      // Set the re-invite
      if (ua_profile.re_invite_time>0)
      {  reInvite(ua_profile.re_invite_time);
      }

      // Set the transfer (REFER)
      if (ua_profile.transfer_to!=null && ua_profile.transfer_time>0)
      {  callTransfer(ua_profile.transfer_to,ua_profile.transfer_time);
      }

      if (ua_profile.do_unregister_all)
      // ########## unregisters ALL contact URLs
      {  ua.printLog("UNREGISTER ALL contact URLs");
         ua.unregisterall();
      } 

      if (ua_profile.do_unregister)
      // unregisters the contact URL
      {  ua.printLog("UNREGISTER the contact URL");
         ua.unregister();
      } 

      if (ua_profile.do_register)
      // ########## registers the contact URL with the registrar server
      {  ua.printLog("REGISTRATION");
         ua.loopRegister(ua_profile.expires,ua_profile.expires/2,ua_profile.keepalive_time);
      } 

      if (ua_profile.call_to!=null)
      // ########## make a call with the remote URL
      {  ua.printLog("UAC: CALLING "+ua_profile.call_to);
         jComboBox1.setSelectedItem(null);
         comboBoxEditor1.setItem(ua_profile.call_to.toString());
         display.setText("CALLING "+ua_profile.call_to);
         ua.call(ua_profile.call_to);
         changeStatus(UA_OUTGOING_CALL);       
      } 

      if (!ua_profile.audio && !ua_profile.video) ua.printLog("ONLY SIGNALING, NO MEDIA");   
   }


   /** Exits. */
   protected void exit()
   {  // close possible active call before exiting
      jButton2_actionPerformed();
      // exit now
      System.exit(0);
   }


   /** When the call/accept button is pressed. */
   void jButton1_actionPerformed()
   {
      if (statusIs(UA_IDLE))
      {  String url=(String)comboBoxEditor1.getItem();
         if (url!=null && url.length()>0)
         {  ua.hangup();
            display.setText("CALLING "+url);
            ua.call(url);
            changeStatus(UA_OUTGOING_CALL);
            if (ua_profile.hangup_time>0) automaticHangup(ua_profile.hangup_time); 
         }
      }
      else
      if (statusIs(UA_INCOMING_CALL))
      {  ua.accept();
         display.setText("ON CALL");
         changeStatus(UA_ONCALL);
      }
   }


   /** When the refuse/hangup button is pressed. */
   void jButton2_actionPerformed()
   {
      if (!statusIs(UA_IDLE))
      {  ua.hangup();
         //ua.listen();
         changeStatus(UA_IDLE);      
 
         display.setText("HANGUP");
      }
   }


   /** When the combo-box action is performed. */
   void jComboBox1_actionPerformed(ActionEvent e)
   {  // if the edited URL is different from the selected item, copy the selected item in the editor
      /*
      String edit_name=(String)comboBoxEditor1.getItem();
      int index=jComboBox1.getSelectedIndex();
      if (index>=0)
      {  String selected_name=buddy_list.elementAt(index);
         if (!selected_name.equals(edit_name)) comboBoxEditor1.setItem(selected_name);
      }*/
   }


   /** When the combo-box text field is changed. */
   void comboBoxEditor1_actionPerformed(ActionEvent e)
   {  // if a new URL has been typed, insert it in the buddy_list and make it selected item
      // else, simply make the URL the selected item
      String name=(String)comboBoxEditor1.getItem();
      // parse separatly NameAddrresses or SipURLs
      if (name.indexOf("\"")>=0 || name.indexOf("<")>=0)
      {  // try to parse a NameAddrress
         NameAddress nameaddr=(new SipParser(name)).getNameAddress();
         if (nameaddr!=null) name=nameaddr.toString();
         else name=null;
      }
      else
      {  // try to parse a SipURL
         SipURL url=new SipURL(name);
         if (url!=null) name=url.toString();
         else name=null;
      }

      if (name==null)
      {  System.out.println("DEBUG: No sip url recognized in: "+(String)comboBoxEditor1.getItem());
         return;
      }

      // checks if the the URL is already present in the buddy_list
      if (!buddy_list.contains(name))
      {  jComboBox1.insertItemAt(name,0);
         jComboBox1.setSelectedIndex(0);
         // limit the list size
         while (buddy_list.getElements().size()>NMAX_CONTACTS) jComboBox1.removeItemAt(NMAX_CONTACTS);
         // save new contact list
         buddy_list.save();         
      }
      else
      {  int index=buddy_list.indexOf(name);
         jComboBox1.setSelectedIndex(index);
      }
 
   }


   /** Gets the UserAgent */
   /*protected UserAgent getUA()
   {  return ua;
   }*/


   // ********************** UA callback functions **********************

   /** When a new call is incoming */
   public void onUaIncomingCall(UserAgent ua, NameAddress callee, NameAddress caller, Vector media_descs)
   {  changeStatus(UA_INCOMING_CALL);
      if (ua_profile.redirect_to!=null) // redirect the call
      {  display.setText("CALL redirected to "+ua_profile.redirect_to);
         ua.redirect(ua_profile.redirect_to);
      }         
      else
      if (ua_profile.accept_time>=0) // automatically accept the call
      {  display.setText("ON CALL");
         jComboBox1.setSelectedItem(null);
         comboBoxEditor1.setItem(caller.toString());
         //accept();
         automaticAccept(ua_profile.accept_time);
      }
      else
      {  display.setText("INCOMING CALL");
         jComboBox1.setSelectedItem(null);
         comboBoxEditor1.setItem(caller.toString());
      }
   }


   /** When an ougoing call is stated to be in progress */
   public void onUaCallProgress(UserAgent ua)
   {  display.setText("PROGRESS");
   }


   /** When an ougoing call is remotly ringing */
   public void onUaCallRinging(UserAgent ua)
   {  display.setText("RINGING");
   }


   /** When an ougoing call has been accepted */
   public void onUaCallAccepted(UserAgent ua)
   {  display.setText("ON CALL");
      changeStatus(UA_ONCALL);
      if (ua_profile.hangup_time>0) automaticHangup(ua_profile.hangup_time); 
   }


   /** When an incoming call has been cancelled */
   public void onUaCallCancelled(UserAgent ua)
   {  display.setText("CANCELLED");
      //ua.listen();
      changeStatus(UA_IDLE);
   }


   /** When a call has been transferred */
   public void onUaCallTransferred(UserAgent ua)
   {  display.setText("TRASFERRED");
      //ua.listen();
      changeStatus(UA_IDLE);
   }


   /** When an ougoing call has been refused or timeout */
   public void onUaCallFailed(UserAgent ua, String reason)
   {  display.setText("FAILED"+((reason!=null)? " ("+reason+")" : ""));
      //ua.listen();
      changeStatus(UA_IDLE);
   }


   /** When a call has been locally or remotely closed */
   public void onUaCallClosed(UserAgent ua)
   {  display.setText("BYE");
      //ua.listen();
      changeStatus(UA_IDLE);
   }

   /** When a new media session is started. */
   public void onUaMediaSessionStarted(UserAgent ua, String type, String codec)
   {  //printLog(type+" started "+codec);
   }

   /** When a media session is stopped. */
   public void onUaMediaSessionStopped(UserAgent ua, String type)
   {  //printLog(type+" stopped");
   }


   /** When registration succeeded. */
   public void onUaRegistrationSucceeded(UserAgent ua, String result)
   {  this.setTitle(ua_profile.getUserURI().toString());
      printLog("REGISTRATION SUCCESS: "+result); 
   }

   /** When registration failed. */
   public void onUaRegistrationFailed(UserAgent ua, String result)
   {  this.setTitle(sip_provider.getContactAddress(ua_profile.user).toString());
      printLog("REGISTRATION FAILURE: "+result); 
   }


   // ************************ scheduled events ************************

   /** Schedules a re-inviting after <i>delay_time</i> secs. It simply changes the contact address. */
   /*void reInvite(final NameAddress contact, final int delay_time)
   {  new ScheduledWork(delay_time*1000)
      {  public void doWork()
         {  printLog("AUTOMATIC RE-INVITING/MODIFING");
            ua.modify(contact,null);
         }
      };
   }*/
   /** Schedules a re-inviting after <i>delay_time</i> secs. It simply changes the contact address. */
   void reInvite(final int delay_time)
   {  printLog("AUTOMATIC RE-INVITING/MODIFING: "+delay_time+" secs"); 
      if (delay_time==0) ua.modify(null);
      else new ScheduledWork(delay_time*1000) {  public void doWork() {  ua.modify(null);  }  };
   }


   /** Schedules a call-transfer after <i>delay_time</i> secs. */
   /*void callTransfer(final NameAddress transfer_to, final int delay_time)
   {  new ScheduledWork(delay_time*1000)
      {  public void doWork()
         {  printLog("AUTOMATIC REFER/TRANSFER");
            ua.transfer(transfer_to);
         }
      };
   }*/
   /** Schedules a call-transfer after <i>delay_time</i> secs. */
   void callTransfer(final NameAddress transfer_to, final int delay_time)
   {  printLog("AUTOMATIC REFER/TRANSFER: "+delay_time+" secs");
      if (delay_time==0) ua.transfer(transfer_to);
      else new ScheduledWork(delay_time*1000) {  public void doWork() {  ua.transfer(transfer_to);  }  };
   }


   /** Schedules an automatic answer after <i>delay_time</i> secs. */
   /*void automaticAccept(final int delay_time)
   {  new ScheduledWork(delay_time*1000)
      {  public void doWork()
         {  printLog("AUTOMATIC ANSWER");
            jButton1_actionPerformed();
         }
      };
   }*/
   /** Schedules an automatic answer after <i>delay_time</i> secs. */
   void automaticAccept(final int delay_time)
   {  printLog("AUTOMATIC ANSWER: "+delay_time+" secs");
      if (delay_time==0) jButton1_actionPerformed();
      else new ScheduledWork(delay_time*1000) {  public void doWork() {  jButton1_actionPerformed();  }  };
   }
   

   /** Schedules an automatic hangup after <i>delay_time</i> secs. */
   /*void automaticHangup(final int delay_time)
   {  new ScheduledWork(delay_time*1000)
      {  public void doWork()
         {  printLog("AUTOMATIC HANGUP");
            jButton2_actionPerformed();
         }
      };
   }*/
   /** Schedules an automatic hangup after <i>delay_time</i> secs. */
   void automaticHangup(final int delay_time)
   {  printLog("AUTOMATIC HANGUP: "+delay_time+" secs");
      if (delay_time==0) jButton2_actionPerformed();
      else new ScheduledWork(delay_time*1000) {  public void doWork() {  jButton2_actionPerformed();  }  };
   }


   // ******************************* Main ******************************

   /** The main method - It runs UA.main() with GUI enabled. */
   public static void main(String[] args)
   {
      System.out.println("MJSIP UA "+SipStack.version);

      if (!UA.init("local.ua.GraphicalUA",args)) System.exit(0);
      // else
      new GraphicalUA(UA.sip_provider,UA.ua_profile);
   }


   // ******************************* Logs ******************************

   /** Adds a new string to the default Log */
   private void printLog(String str)
   {  printLog(str,Log.LEVEL_HIGH);
   }

   /** Adds a new string to the default Log */
   private void printLog(String str, int level)
   {  if (log!=null) log.println("GraphicalUA: "+str,UserAgent.LOG_OFFSET+level);  
   }

   /** Adds the Exception message to the default Log */
   private void printException(Exception e,int level)
   {  printLog("Exception: "+ExceptionPrinter.getStackTraceOf(e),level);
   }
}
