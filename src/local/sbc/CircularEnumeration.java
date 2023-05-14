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


import java.util.Vector;


/** CircularEnumeration collects a set of Objects that can be accessed in a sequencial
  * and cyclic manner.
  * <p/>
  * A CircularEnumeration is created from a Vector of Objects. It implements only one
  * the nextElement() method.
  */
public class CircularEnumeration
{
   Vector v;
   int i;

   public CircularEnumeration(Vector vector)
   {  v=vector;
      i=0;
   }  

   public Object nextElement()
   {  if (v==null || v.size()==0) return null;
      if (i==v.size()) i=0;
      return v.elementAt(i++);
   }

}
