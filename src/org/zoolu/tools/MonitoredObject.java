/*
 * Copyright (C) 2006 Luca Veltri - University of Parma - Italy
 * 
 * This file is part of MjSip (http://www.mjsip.org)
 * 
 * MjSip is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * MjSip is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with MjSip; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 * 
 * Author(s):
 * Luca Veltri (luca.veltri@unipr.it)
 */

package org.zoolu.tools;



import java.util.Hashtable;
import java.util.Enumeration;
import java.util.Vector;



/** MonitoredObject is the base class for all objects that have to be monitored.
  * It keeps trace of the number of instances allocated into the heap.
  */
public class MonitoredObject
{
   /** Table of class occurences, that is: (String)class_name-->(Long)class_occurences */
   static Hashtable class_occurences=new Hashtable();

   /** Total number of thrown exceptions */
   static long exception_counter=0;


   /** Class name of the current object */
   String class_name;


   /** Creates a new MonitoredObject. */
   public MonitoredObject()
   {  try
      {  class_name=getClass().getName();
         if (class_name==null) throw new MonitoredObjectException("Unknown class name.");
         else addObject(class_name);
      }
      catch (Exception e)
      {  exception_counter++;
      }
   }


   /** Deletes the MonitoredObject. */
   public void finalize()
   {  try
      {  if (class_name!=null) removeObject(class_name);
      }
      catch (Exception e)
      {  exception_counter++;
      }
  }


   /** Adds a new MonitoredObject instance. */
   private synchronized static void addObject(String class_name)
   {  if (!class_occurences.containsKey(class_name))
      {  Long counter=new Long(1);
         class_occurences.put(class_name,counter);
      }
      else
      {  Long counter=(Long)class_occurences.get(class_name);
         counter=new Long(counter.longValue()+1);
         class_occurences.remove(class_name);
         class_occurences.put(class_name,counter);
      }
   }


   /** Removes a MonitoredObject instance. */
   private synchronized static void removeObject(String class_name)
   {  if (class_occurences.containsKey(class_name))
      {  Long counter=(Long)class_occurences.get(class_name);
         counter=new Long(counter.longValue()-1);
         class_occurences.remove(class_name);
         class_occurences.put(class_name,counter);
      }
   }


   /** Gets occurrences of all monitored classes.
     * @return It returns a Hashtable mapping class names (String) onto the number of their occurrences (Long). */
   public synchronized static Hashtable getAllClassOccurences()
   {  return new Hashtable(class_occurences);
   }


   /** Gets occurrences of a specific class. */
   public synchronized static long getClassOccurences(String class_name)
   {  if (class_occurences.containsKey(class_name)) return ((Long)class_occurences.get(class_name)).longValue();
      else return 0;
   }


   /** Gets all monitored classes.
     * @return It returns an Enumeration of all class names (String). */
   public synchronized static Enumeration getAllClasses()
   {  return (new Hashtable(class_occurences)).keys();
   }


   /** Gets the total number of thrown exceptions. */
   public static long getExceptionCounter()
   {  return exception_counter;
   }


   /** Prints dump. */
   public synchronized static String getDump()
   {  String dump="";
      for (Enumeration e=class_occurences.keys(); e.hasMoreElements(); )
      {  String name=(String)e.nextElement();
         Long counter=(Long)class_occurences.get(name);
         dump+=name+": "+counter+"\r\n";
      }
      return dump;
   }


   /** MonitoredObjectException. */
   public class MonitoredObjectException extends Exception
   {  
      /* Creates a new MonitoredObjectException. */
      public MonitoredObjectException(String name)
      {  super(name);
      }
   }
}
