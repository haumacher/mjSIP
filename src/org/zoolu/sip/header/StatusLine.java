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

package org.zoolu.sip.header;




/** SIP Status-line, i.e. the first line of a response message.
  */
public class StatusLine
{
   /** Status code */
   protected int code;

   /** Status reason */
   protected String reason;

   /** Construct StatusLine  */
   public StatusLine(int c, String r)
   {  code=c;
      reason=r;
   }

   /** Creates a new copy of the request-line. */
   public Object clone()
   {  return new StatusLine(getCode(),getReason());
   }

   /** Whether Object <i>obj</i> is "equal to" this StatusLine. */
   public boolean equals(Object obj)
   {  //if (o.getClass().getSuperclass()!=this.getClass().getSuperclass()) return false;
      try
      {  StatusLine r=(StatusLine)obj;
         if (r.getCode()==(this.getCode()) && r.getReason().equals(this.getReason())) return true;
         else return false;
      }
      catch (Exception e) {  return false;  }
   }

   /** Gets String value of this Object. */
   public String toString()
   {  return "SIP/2.0 "+code+" "+reason+"\r\n";
   }

   /** Gets status code. */
   public int getCode()
   {  return code;
   }

   /** Gets status reason. */
   public String getReason()
   {  return reason;
   }
}
