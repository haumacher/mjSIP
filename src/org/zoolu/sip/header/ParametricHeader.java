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


import org.zoolu.sip.provider.SipParser;
import org.zoolu.tools.Parser;
import java.lang.*;
import java.util.*;


/** Abstract ParametricHeader is the base class for all SIP Headers that include parameters */
public abstract class ParametricHeader extends Header
{
   //public ParametricHeader(String hname)
   //{  super(hname);
   //}	

   /** Costructs the abstract ParametricHeader. */
   protected ParametricHeader(String hname, String hvalue)
   {  super(hname,hvalue);
   }

   /** Costructs the abstract ParametricHeader. */
   protected ParametricHeader(Header hd)
   {  super(hd);
   }
   
   /** Gets the first word.
     * @return the first word or null if no value is present beafore parameters. */
   /*protected String getFirstWord() 
   {  int index=indexOfFirstSemi();
      if (index<0) return value;
      else return value.substring(0,index).trim();
   }*/

   /** Returns the index of the first semicolon before the first parameter.
     * @return the index of the semicolon before the first parameter, or -1 if no parameter is present. */
   protected int indexOfFirstSemi()
   {  //int index=(new Parser(value)).goToSkippingQuoted(';').skipChar().skipWSP().getPos();
      int index=(new Parser(value)).goToSkippingQuoted(';').getPos();
      return (index>=value.length())? -1 : index;
   }

   /** Gets the string of all parameters.
     * @return Returns a string of all parameters or null if no parameter is present. */
   public String getParameters() 
   {  int index=indexOfFirstSemi();
      if (index<0) return null;
      return getValue().substring(index+1);
   }
   
   /** Gets the value of specified parameter.
     * @return Returns the value of the specified parameter or null if not present. */
   public String getParameter(String pname) 
   {  int index=indexOfFirstSemi();
      if (index<0) return null;
      return (new SipParser((new Parser(getValue(),index)).skipChar().skipWSP())).getParameter(pname);
   }
    
   /** Gets a String Vector of parameter names.
     * @return Returns a String Vector of all parameter names or null if no parameter is present. */
   public Vector getParameterNames() 
   {  int index=indexOfFirstSemi();
      if (index<0) return new Vector();
      return (new SipParser((new Parser(getValue(),index)).skipChar().skipWSP())).getParameterNames();
   }

   /** Whether there is the specified parameter */
   public boolean hasParameter(String pname)
   {  int index=indexOfFirstSemi();
      if (index<0) return false;
      return (new SipParser((new Parser(getValue(),index)).skipChar().skipWSP())).hasParameter(pname);
   }

   /** Whether there are any parameters */
   public boolean hasParameters()
   {  return indexOfFirstSemi()>=0;
   }

   /** Removes all parameters (if any) */
   public void removeParameters() 
   {  if (!hasParameters()) return;
      String header=getValue();
      //System.out.println(header);
      int i=header.indexOf(';');
      header=header.substring(0,i);      
      //System.out.println(header);
      setValue(header);
   }

   /** Removes specified parameter (if present) */
   public void removeParameter(String pname) 
   {  int index=indexOfFirstSemi();
      if (index<0) return;
      String header=getValue();
      Parser par=new Parser(header,index);
      while (par.hasMore())
      {  int begin_param=par.getPos();
         par.skipChar();
         if (par.getWord(SipParser.param_separators).equals(pname))
         {  String top=header.substring(0,begin_param); 
            par.goToSkippingQuoted(';');
            String bottom="";
            if (par.hasMore()) bottom=header.substring(par.getPos()); 
            header=top.concat(bottom);
            setValue(header);
            return;
            //par=new Parser(header,par.getPos());
         }
         par.goTo(';');
      }
   }

   /** Sets the value of a specified parameter.
     * Zero-length String is returned in case of flag parameter (without value). */
   public void setParameter(String pname, String pvalue) 
   {  if (getValue()==null) setValue("");
      if (hasParameter(pname)) removeParameter(pname);
      String header=getValue();
      header=header.concat(";"+pname);
      if (pvalue!=null) header=header.concat("="+pvalue);
      setValue(header);          
   }
}
