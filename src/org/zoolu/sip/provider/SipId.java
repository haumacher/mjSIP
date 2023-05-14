/*
 * Copyright (C) 2005 Luca Veltri - University of Parma - Italy
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

package org.zoolu.sip.provider;




/** Generic identifier used to address SipProvider-related entities.
  */
public class SipId
{
   /** The actual id */   
   String id;
   
   /** Costructs a new void SipId. */
   SipId()
   {
   }

   /** Costructs a new SipId. */
   SipId(String id)
   {  this.id=id;
   }

   /** Costructs a new SipId. */
   SipId(SipId i)
   {  this.id=i.id;
   }

   /** Whether the SipId equals to <i>obj</i>. */
   public boolean equals(Object obj)
   {  try
      {  SipId i=(SipId)obj;
         return id.equals(i.id);
      }
      catch (Exception e) {  return false;  }
   }

   /** Gets an int hashCode for the SipId. */
   public int hashCode()
   {  return id.hashCode();
   }

   /** Gets a String value for the SipId */ 
   public String toString()
   {  return id;
   }
}
