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


import org.zoolu.tools.Parser;


/** SIP Replaces header (RFC 3891).
  * <p>
  * Replaces header is used to logically replace an existing SIP dialog
  * with a new SIP dialog.  This primitive can be used to enable a
  * variety of features, for example: "Attended Transfer" and "Call
  * Pickup".  Note that the definition of these example features is non-
  * normative.
  */
public class ReplacesHeader extends ParametricHeader
{

   /** Creates a new ReplacesHeader. */
   /*public ReplacesHeader(String call_id)
   {  super(SipHeaders.Replaces,call_id);
   }*/

   /** Creates a new ReplacesHeader. */
   public ReplacesHeader(String call_id, String to_tag, String from_tag)
   {  super(SipHeaders.Replaces,call_id);
      if (to_tag!=null) setParameter("to-tag",to_tag);
      if (from_tag!=null) setParameter("from-tag",from_tag);
   }

   /** Creates a new ReplacesHeader. */
   public ReplacesHeader(String call_id, String to_tag, String from_tag, boolean early_only)
   {  super(SipHeaders.Replaces,call_id);
      if (to_tag!=null) setParameter("to-tag",to_tag);
      if (from_tag!=null) setParameter("from-tag",from_tag);
      if (early_only) setParameter("early-only",null);
   }

   /** Creates a new ReplacesHeader. */
   public ReplacesHeader(Header hd)
   {  super(hd);
   }

   
   /** Gets call_id. */
   public String getCallId()
   {  char[] delim={ ' ', ';', '\t', '\r', '\n' };
      return (new Parser(value)).getWord(delim);
   }


   /** Whether it has 'to-tag' parameter. */
   public boolean hasToTag()
   {  return hasParameter("to-tag");
   }
   /** Gets 'to-tag' parameter. */
   public String getToTag()
   {  return getParameter("to-tag");
   }
   /** Sets 'to-tag' parameter. */
   /*public void setToTag(String to_tag)
   {  this.setParameter("to-tag",to_tag);
   }*/


   /** Whether it has 'from-tag' parameter. */
   public boolean hasFromTag()
   {  return hasParameter("from-tag");
   }
   /** Gets 'from-tag' parameter. */
   public String getFromTag()
   {  return getParameter("from-tag");
   }
   /** Sets 'from-tag' parameter. */
   /*public void setFromTag(String from_tag)
   {  this.setParameter("from-tag",from_tag);
   }*/


   /** Whether it has 'early-only' flag. */
   public boolean hasEarlyFlag()
   {  return hasParameter("early-only");
   }
   /** Sets 'early-only' parameter. */
   public void setEarlyFlag(boolean early_only)
   {  if (early_only) this.setParameter("early-only",null);
      else removeParameter("early-only");
   } 
}

