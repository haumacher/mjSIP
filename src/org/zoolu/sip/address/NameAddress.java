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

package org.zoolu.sip.address;



import org.zoolu.sip.provider.SipParser;



/** Class <i>NameAddress</i> is used to rapresent any valid SIP Name Address.
  * It contains a SIP URI and optionally a display name.
  * <BR> A  SIP Name Address is a string of the form of:
  * <BR><BLOCKQUOTE><PRE>&nbsp&nbsp [ display-name ] address
  * <BR>&nbsp&nbsp where address can be a valid SIP URL</PRE></BLOCKQUOTE>
*/
public class NameAddress
{
   /** Display name. */
   String name;

   /** URL. */
   SipURL url;


   /** Creates a new NameAddress. */
   public NameAddress(String display_name, SipURL url)
   {  this.name=display_name;
      this.url=url;
   }

   /** Creates a new NameAddress. */
   public NameAddress(SipURL url)
   {  this.name=null;
      this.url=url;
   }

   /** Creates a new NameAddress. */
   public NameAddress(NameAddress naddr)
   {  name=naddr.getDisplayName();
      url=naddr.getAddress();
   }

   /** Creates a new NameAddress. */
   public NameAddress(String str)
   {  SipParser par=new SipParser(str);
      NameAddress naddr=par.getNameAddress();
      name=naddr.name;
      url=naddr.url;
   }
   
   /** Creates a copy of this object. */
   public Object clone()
   {  return new NameAddress(this);
   }

   /** Whether object <i>obj</i> is "equal to" this. */
   public boolean equals(Object obj)
   {  try
      {  NameAddress naddr=(NameAddress)obj;
         return ((name==naddr.name) || (name.equals(naddr.name)) && url.equals(naddr.url));
      }
      catch (Exception e)
      {  return false;
      }
   }

   /** Gets address of NameAddress */
   public SipURL getAddress()
   {  return url;
   }

   /** Gets display name (returns null id display name does not exist). */
   public String getDisplayName()
   {  return name;
   }

   /** Whether there is a display name. */
   public boolean hasDisplayName()
   {  return name!=null;
   }

   /** Removes display name (if present). */
   public void removeDisplayName()
   {  name=null;
   }

   /** Sets URL. */
   public void setAddress(SipURL url)
   {  this.url=url;
   }

   /** Sets display name. */
   public void setDisplayName(String display_name)
   {  this.name=display_name;
   }

   /** Gets string representation of this object. */
   public String toString()
   {  StringBuffer sb=new StringBuffer();
      if (hasDisplayName()) sb.append('\"').append(name).append("\" <").append(url).append('>');
      else sb.append('<').append(url).append('>');
      return sb.toString();
   }

}
