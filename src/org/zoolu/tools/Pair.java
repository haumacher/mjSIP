/*
 * Copyright (C) 2009 Luca Veltri - University of Parma - Italy
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

package org.zoolu.tools;




/** Pair of two object.
  */
public class Pair
{
   /** First object */
   Object o1;
   
   /** Second object */
   Object o2;
   
   
   /** creates a new Pair. */
   public Pair(Object o1, Object o2)
   {  this.o1=o1;
      this.o2=o2;
   }

   /** creates a new Pair. */
   public Pair(Pair p)
   {  o1=p.o1;
      o2=p.o2;
   }

   /** Gets the first object. */
   public Object getFirst()
   {  return o1;
   }
  
   /** Gets the second object. */
   public Object getSecond()
   {  return o2;
   }

   /** Sets the first object. */
   public void setFirst(Object o1)
   {  this.o1=o1;
   }
  
   /** Sets the second object. */
   public void setSecond(Object o2)
   {  this.o2=o2;
   }

   /** Whether two objects are equals. */
   public static boolean same(Object obj1, Object obj2)
   {  return (obj1==null)? obj2==null : obj1.equals(obj2);
   }

   /** Whether it equals to a given object. */
   public boolean equals(Object obj)
   {  if(!(obj instanceof Pair)) return false;
      // else
      Pair p=(Pair)obj;
      return same(o1,p.o1) && same(o2,p.o2);
   }

   /** gets a String representation of this object. */
   public String toString()
   {  return "{"+o1+","+o2+"}";
   }
   
}
