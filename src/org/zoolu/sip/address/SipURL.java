/*
 * Copyright (C) 2007 Luca Veltri - University of Parma - Italy
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
import org.zoolu.tools.Parser;
import java.util.Vector;


/** Class <i>SipURL</i> implements SIP and SIPS URLs.
  * <P> A SIP URL is a string of the form of:
  * <BR><BLOCKQUOTE><PRE>&nbsp&nbsp sip:[user@]hostname[:port][;parameters] </PRE></BLOCKQUOTE>
  * <P> If <i>port</i> number is ommitted, -1 is returned
  */
public class SipURL
{

   /** SIP scheme */
   protected static final String SIP_SCHEME="sip"; 

   /** SIPS scheme */
   protected static final String SIPS_SCHEME="sips"; 

   /** Transport param name */
   protected static final String PARAM_TRANSPORT="transport"; 

   /** Maddr param name */
   protected static final String PARAM_MADDR="maddr"; 

   /** TTL param name */
   protected static final String PARAM_TTL="ttl"; 

   /** Lr param name */
   protected static final String PARAM_LR="lr"; 


   /** Whether has SIPS scheme */
   protected boolean secure=false;

   /** SIP URL */
   protected String url;



   /** Creates a new SipURL. */
   public SipURL(SipURL u)
   {  url=u.url;
      secure=u.secure;
   }

   /** Creates a new SipURL.
     * @param url hostname or the complete SIP (or SIPS) URL. */
   public SipURL(String url)
   {  if (url.startsWith(SIP_SCHEME+":")) this.url=url;
      else
      if(url.startsWith(SIPS_SCHEME+":"))
      {  this.url=url;
         this.secure=true;
      }
      else this.url=SIP_SCHEME+":"+url;
   }

   /** Creates a new SipURL. */
   public SipURL(String username, String hostname)
   {  init(username,hostname,-1);
   }

   /** Creates a new SipURL. */
   public SipURL(String hostname, int portnumber)
   {  init(null,hostname,portnumber);
   }

   /** Creates a new SipURL. */
   public SipURL(String username, String hostname, int portnumber)
   {  init(username,hostname,portnumber);
   }

   /** Inits the SipURL. */
   private void init(String username, String hostname, int portnumber)
   {  StringBuffer sb=new StringBuffer(getScheme());
      sb.append(':');
      if (username!=null) sb.append(username).append('@');
      sb.append(hostname);
      if (portnumber>0) sb.append(":"+portnumber);
      url=sb.toString();
   }

   /** Creates and returns a copy of the URL. */
   public Object clone()
   {  return new SipURL(this);
   }

   /** Indicates whether some other Object is "equal to" this URL. */
   public boolean equals(Object obj)
   {  try
      {  return equals((SipURL)obj);
      }
      catch (Exception e)
      {  return false;  
      }
   }

   /** Whether two SipURLs are equals. */
   public boolean equals(SipURL sip_url)
   {  return url.equals(sip_url.url);
   }

   /** Gets scheme ("sip" or "sips"). */
   private String getScheme()
   {  return ((secure)? SIPS_SCHEME : SIP_SCHEME);
   }

   /** Gets user name of SipURL (Returns null if user name does not exist). */
   public String getUserName()
   {  int begin=getScheme().length()+1; // skip "sip:"
      int end=url.indexOf('@',begin);
      if (end<0) return null;
         else return url.substring(begin,end);
   }

   /** Gets host of SipURL. */
   public String getHost()
   {  char[] host_terminators={':',';','?'};
      Parser par=new Parser(url);
      int begin=par.indexOf('@'); // skip "sip:user@"
      if (begin<0) begin=getScheme().length()+1; // skip "sip:"
         else begin++; // skip "@"
      par.setPos(begin);
      int end=par.indexOf(host_terminators);
      if (end<0) return url.substring(begin);
         else return url.substring(begin,end);
   }

   /** Gets port of SipURL; returns -1 if port is not specidfied. */
   public int getPort()
   {  char[] port_terminators={';','?'};
      Parser par=new Parser(url,getScheme().length()+1); // skip "sip:"
      int begin=par.indexOf(':');
      if (begin<0) return -1;
      else
      {  begin++;
         par.setPos(begin);
         int end=par.indexOf(port_terminators);
         if (end<0) return Integer.parseInt(url.substring(begin));
         else return Integer.parseInt(url.substring(begin,end));
      }
   }

   /** Gets boolean value to indicate if SipURL has user name. */
   public boolean hasUserName()
   {  return getUserName()!=null;
   }

   /** Gets boolean value to indicate if SipURL has port. */
   public boolean hasPort()
   {  return getPort()>=0;
   }

   /** Gets string representation of URL. */
   public String toString()
   {  return url;
   }

   /** Gets the string of all parameters.
     * @return Returns a string of all parameters or null if no parameter is present. */
   public String getParameters() 
   {  if (url!=null)
      {  int index=url.indexOf(';');
         if (index>=0) return url.substring(index+1);
      }
      // else
      return null;
   }
   
   /** Gets the value of specified parameter.
     * @return Returns the value of the specified parameter or null if not present. */
   public String getParameter(String name) 
   {  SipParser par=new SipParser(url);
      return ((SipParser)par.goTo(';').skipChar()).getParameter(name);
   }
   
   /** Gets a String Vector of parameter names.
     * @return Returns a String Vector of all parameter names or null if no parameter is present. */
   public Vector getParameterNames() 
   {  SipParser par=new SipParser(url);
      return ((SipParser)par.goTo(';').skipChar()).getParameterNames();
   }
   
   /** Whether there is the specified parameter. */
   public boolean hasParameter(String name)
   {  SipParser par=new SipParser(url);
      return ((SipParser)par.goTo(';').skipChar()).hasParameter(name);
   }
   
   /** Whether there are any parameters. */
   public boolean hasParameters()
   {  if (url!=null && url.indexOf(';')>=0) return true;
      else return false;
   }
   
   /** Adds a new parameter without a value. */
   public void addParameter(String name) 
   {  url=url+";"+name;       
   }
   
   /** Adds a new parameter with value. */
   public void addParameter(String name, String value) 
   {  if (value!=null) url=url+";"+name+"="+value;
      else url=url+";"+name;       
   }

   /** Removes all parameters (if any). */
   public void removeParameters() 
   {  int index=url.indexOf(';');
      if (index>=0) url=url.substring(0,index);      
   }

   /** Removes specified parameter (if present). */
   public void removeParameter(String name) 
   {  int index=url.indexOf(';');
      if (index<0) return;
      Parser par=new Parser(url,index);
      while (par.hasMore())
      {  int begin_param=par.getPos();
         par.skipChar();
         if (par.getWord(SipParser.param_separators).equals(name))
         {  String top=url.substring(0,begin_param); 
            par.goToSkippingQuoted(';');
            String bottom="";
            if (par.hasMore()) bottom=url.substring(par.getPos()); 
            url=top.concat(bottom);
            return;
         }
         par.goTo(';');
      }
   }

   /** Gets the value of transport parameter.
     * @return null if no transport parameter is present. */
   public String getTransport() 
   {  return getParameter(PARAM_TRANSPORT);
   }  

   /** Whether transport parameter is present. */
   public boolean hasTransport()
   {  return hasParameter(PARAM_TRANSPORT);
   }

   /** Adds transport parameter. */
   public void addTransport(String proto) 
   {  addParameter(PARAM_TRANSPORT,proto.toLowerCase());
   }

   /** Gets the value of maddr parameter.
     * @return null if no maddr parameter is present. */
   public String getMaddr() 
   {  return getParameter(PARAM_MADDR);
   }  

   /** Whether maddr parameter is present. */
   public boolean hasMaddr()
   {  return hasParameter(PARAM_MADDR);
   }

   /** Adds maddr parameter. */
   public void addMaddr(String maddr) 
   {  addParameter(PARAM_MADDR,maddr);
   }

   /** Gets the value of ttl parameter.
     * @return 1 if no ttl parameter is present. */
   public int getTtl() 
   {  try {  return Integer.parseInt(getParameter(PARAM_TTL));  } catch (Exception e) {  return 1;  }
   }  

   /** Whether ttl parameter is present. */
   public boolean hasTtl()
   {  return hasParameter(PARAM_TTL);
   }

   /** Adds ttl parameter. */
   public void addTtl(int ttl) 
   {  addParameter(PARAM_TTL,Integer.toString(ttl));
   }

   /** Whether lr (loose-route) parameter is present. */
   public boolean hasLr()
   {  return hasParameter(PARAM_LR);
   }

   /** Adds lr parameter. */
   public void addLr() 
   {  addParameter(PARAM_LR);
   }

  /** Whether is SIPS. */
   public boolean isSecure()
   {  return secure;
   }

   /** Sets scheme to SIPS. */
   public void setSecure(boolean secure) 
   {  if (this.secure!=secure)
      {  this.secure=secure;
         url=getScheme()+url.substring(url.indexOf(':'));
      }
   }

}
