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

package org.zoolu.sdp;


import org.zoolu.tools.Parser;


/** SDP key field.
  * <p>
  * <BLOCKQUOTE><PRE>
  *    key-field = "k=" method [:encryption_key] CRLF
  * </PRE></BLOCKQUOTE>
  */
public class KeyField extends SdpField
{  
   /** Method prompt */
   public static final String METHOD_PROMPT="prompt"; 
   /** Method clear */
   public static final String METHOD_CLEAR="clear";    
   /** Method base64 */
   public static final String METHOD_BASE64="base64"; 
   /** Method uri */
   public static final String METHOD_URI="uri"; 

                    
   /** Creates a new KeyField. */
   public KeyField(String key_field)
   {  super('k',key_field);
   }

   /** Creates a new KeyField. */
   public KeyField(String method, String encryption_key)
   {  super('k',method+":"+encryption_key);
   }

   /** Creates a new KeyField. */
   public KeyField(SdpField sf)
   {  super(sf);
   }
      
   /** Gets the method to be used to obtain a usable key. */
   public String getMethod()
   {  int colon=value.indexOf(":");
      if (colon<0) return value; else return value.substring(0,colon);
   }

   /** Gets the encryption key. */
   public String getEncryptionKey()
   {  int colon=value.indexOf(":");
      if (colon<0) return null; else return value.substring(colon+1);
   }
   
}
