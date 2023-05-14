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

package local.sbc;



import org.zoolu.net.SocketAddress;
import org.zoolu.tools.Timer;
import org.zoolu.tools.TimerListener;
import java.util.Hashtable;
import java.util.Enumeration;
import java.util.Date;
import java.util.Vector;
// logs
import org.zoolu.sip.provider.SipStack;
import org.zoolu.tools.Log;



/** Class AddressResolver maintains address binding.
  * It maps socket addresses into other socket addresses.
  * <p/>
  * Class AddressResolver can be used to maintain a reference to the actual address
  * to be used to reach a remote node, against to the address optained in other ways.
  * For example, it can be used to maintain correct remote address mapping
  * for symmetric NAT traversal.
  */
public class AddressResolver implements TimerListener
{

   /** Refresh time [millisecs].
     * When expired bindings are removed. */
   long refresh_time;

   /** Expire time [millisecs].
     * The maximum time that a binding is considered active. */
   long expire_time;

   /** Log. */
   Log log=null;

   /** Binding table */
   Hashtable binding_table;

   /** Time table */
   Hashtable time_table;

   /** Refresh timer */
   Timer timer;
   
   

   /** Costructs an empty AddressResolver */
   public AddressResolver(long refresh_time, Log log)
   {  this.refresh_time=refresh_time;
      expire_time=refresh_time/2;
      this.log=log;
      binding_table=new Hashtable();
      time_table=new Hashtable();
      timer=new Timer(refresh_time,this);
      timer.start();
   }

   
   /** Gets the size of the resolver's db */
   public int size()
   {  return binding_table.size();
   }


   /** Gets list of all reference SocketAddresses */
   public Enumeration getAllSocketAddresses()
   {  return binding_table.keys();
   }


   /** Whether there is a mapping for the selected SocketAddress */
   public boolean contains(SocketAddress refer_soaddr)
   {  if (refer_soaddr!=null)
      {  return binding_table.containsKey(refer_soaddr.toString());
      }
      return false;
   }
   
   
   /** Adds or updates a new SocketAddress mapping */
   public void updateBinding(SocketAddress refer_soaddr, SocketAddress actual_soaddr)
   {  if (refer_soaddr!=null)
      {  String key=refer_soaddr.toString();
         Long expire=new Long((new Date()).getTime()+expire_time);
         if (binding_table.containsKey(key))
         {  if (!((SocketAddress)binding_table.get(key)).equals(actual_soaddr))
            {  printLog("change BINDING "+refer_soaddr+" >> "+actual_soaddr,Log.LEVEL_HIGH);
               binding_table.remove(key);
               binding_table.put(key,actual_soaddr);
            }
            else
            {  printLog("update BINDING "+refer_soaddr+" >> "+actual_soaddr,Log.LEVEL_MEDIUM);
               // do not change binding_table
            }
            time_table.remove(key);
            time_table.put(key,expire);
         }
         else
         {  printLog("add BINDING "+refer_soaddr+" >> "+actual_soaddr,Log.LEVEL_HIGH);
            binding_table.put(key,actual_soaddr);
            time_table.put(key,expire);
         }
      }
   }


   /** Removes a SocketAddress mapping */
   public void removeBinding(SocketAddress refer_soaddr)
   {  if (refer_soaddr!=null)
      {  String key=refer_soaddr.toString();
         if (binding_table.containsKey(key))
         {  printLog("remove BINDING for "+refer_soaddr,Log.LEVEL_HIGH);
            binding_table.remove(key);
            time_table.remove(key);
         }
      }
   }


   /** Gets the actual SocketAddress for the selected SocketAddress */
   public SocketAddress getSocketAddress(SocketAddress refer_soaddr)
   {  if (refer_soaddr!=null)
      {  String key=refer_soaddr.toString();
         if (binding_table.containsKey(key)) return (SocketAddress)binding_table.get(key);
      }
      return null;
   }


   /** When the refresh timeout fires */
   public void onTimeout(Timer t)
   {  // enumerate expired binding
      printLog("refresh all address bindings:",Log.LEVEL_MEDIUM);         
      long now=(new Date()).getTime();
      Vector aux=new Vector();
      for (Enumeration e=time_table.keys(); e.hasMoreElements(); )
      {  String key=(String)e.nextElement();
         long expire=((Long)time_table.get(key)).longValue();
         if (expire<now) aux.addElement(key);
      }
      // remove expired binding
      for (int i=0; i<aux.size(); i++)
      {  String key=(String)aux.elementAt(i);
         printLog("remove BINDING for "+key,Log.LEVEL_HIGH);         
         binding_table.remove(key);
         time_table.remove(key);
      }
      printLog("done.",Log.LEVEL_MEDIUM);         

      // start a new refresh timer
      timer=new Timer(refresh_time,this);
      timer.start();
   }

   // ****************************** Logs *****************************

   /** Adds a new string to the default Log */
   protected void printLog(String str, int level)
   {  if (log!=null) log.println("AddressResolver: "+str,SessionBorderController.LOG_OFFSET+level);
      if (level==Log.LEVEL_HIGH) System.out.println("IP: "+str);
   }

}