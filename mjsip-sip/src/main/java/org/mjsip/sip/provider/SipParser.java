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

package org.mjsip.sip.provider;



import java.util.Date;
import java.util.Vector;

import org.mjsip.sip.address.GenericURI;
import org.mjsip.sip.address.NameAddress;
import org.mjsip.sip.address.SipURI;
import org.mjsip.sip.header.Header;
import org.mjsip.sip.header.RequestLine;
import org.mjsip.sip.header.StatusLine;
import org.zoolu.util.DateFormat;
import org.zoolu.util.Parser;

//import java.text.DateFormat;
//import java.text.SimpleDateFormat;



/** Class SipParser extends class Parser for parsing of SIP messages.
 */
public class SipParser extends Parser {
	
	/** Creates a new SipParser based on String <i>s</i> */ 
	public SipParser(String s) {
		super(s);
	}

	/** Creates a new SipParser based on String <i>s</i> and starting from position <i>i</i> */ 
	public SipParser(String s, int i) {
		super(s,i);
	}
	
	/** Creates a new SipParser based on StringBuffer <i>sb</i> */ 
	public SipParser(StringBuffer sb) {
		super(sb);
	}

	/** Creates a new SipParser based on StringBuffer <i>sb</i> and starting from position <i>i</i> */ 
	public SipParser(StringBuffer sb, int i) {
		super(sb,i);
	}

	/** Creates a new SipParser starting from the current position. */ 
	public SipParser(Parser p) {
		super(p.getWholeString(),p.getPos());
	}

	/** MARK char[], composed by: '-' , '_' , '.' , '!' , '~' , '*' , '\'' , '|' */
	public static char[] MARK={'-','_','.','!','~','*','\'','|'};
	
	/** SEPARATOR char[], composed by: ' ','\t','\r','\n','(',')','&lt;','&gt;',',',';','\','"','/','[',']','?','=','{','}' */
	public static char[] SEPARATOR={' ','\t','\r','\n','(',')','<','>',',',';','\\','"','/','[',']','?','=','{','}'};

	/** Checks whether a char is any MARK */
	public static boolean isMark(char c) {
		//return (c=='-' || c=='_' || c=='.' || c=='!' || c=='~' || c=='*' || c=='\'' || c=='|');
		return isAnyOf(MARK,c);
	}
	
	/** Unreserved char; that is an alphanum or a mark*/
	public static boolean isUnreserved(char c) {
		return (isAlphanum(c) || isMark(c));
	}
	
	/** Separator; differently form RFC2543, do not include '@' and ':', while include '\r' and '\n'*/
	public static boolean isSeparator(char c) {
		//return (isSpace(c) || isCRLF(c) || c=='(' || c==')' || c=='<' || c=='>' || c==',' || c==';' || c=='\\' || c=='"' || c=='/' || c=='[' || c==']' || c=='?' || c=='=' || c=='{' || c=='}');
		return isAnyOf(SEPARATOR,c);
	}
	
	/** Returns the first occurence of a separator or the end of the string*/
	public int indexOfSeparator() {
		int begin=index;
		while(begin<str.length() && !isSeparator(str.charAt(begin))) begin++;
		return begin;
	}  
	
	/** Index of the end of the header (EOH) */
	public int indexOfEOH() {
		SipParser par=new SipParser(str,index);
		while (true) {
			par.goTo(CRLF); // find the first CR or LF
			if (!par.hasMore()) return str.length(); // if no CR/LF found return the end of string
			int end=par.getPos();
			par.goToNextLine(); // skip the CR/LF chars
			if (!par.hasMore() || !isWSP(par.nextChar())) return end;
		}
	}

	/** Returns the begin of next header */
	public int indexOfNextHeader() {
		SipParser par=new SipParser(str,index);
		par.goToNextHeader();
		return par.getPos();
	}
	
	/** Returns the index of the begin of the first occurence of the Header <i>hname</i> */
	public int indexOfHeader(String hname) {
		if (str.startsWith(hname,index)) return index;
		String[] target={'\n'+hname, '\r'+hname};
		SipParser par=new SipParser(str,index);   
		//par.goTo(target);
		par.goToIgnoreCase(target);
		if (par.hasMore()) par.skipChar();
		return par.getPos();
	}

	/** Goes to the begin of next header */
	public SipParser goToNextHeader() {
		index=indexOfEOH();
		goToNextLine();
		return this;
	}
	
	/** Go to the end of the last header.
	  * The final empty line delimiter is not considered as header */
	public SipParser goToEndOfLastHeader() {
		String[] delimiters={"\r\n\r\n","\n\n"}; // double newline
		goTo(delimiters);
		if (!hasMore()) {
			// no double newline found
			if (str.startsWith("\r\n",str.length()-2)) index=str.length()-2;
				else if (str.charAt(str.length()-1)=='\n') index=str.length()-1;
					  else index=str.length();
		}
		return this;
	}
	
	/** Go to the begin (first char of) SipMessage Body */
	public SipParser goToBody() {
		goToEndOfLastHeader();
		goTo('\n').skipChar();
		goTo('\n').skipChar();
		return this;
	}
	
	/** Returns the first header and goes to the next line. */
	public Header getHeader()     {
		if (!hasMore()) return null;
		int begin=getPos();
		int end=indexOfEOH();
		String header_str=getString(end-begin);
		goToNextLine();
		int colon=header_str.indexOf(':');
		if (colon<0) return null;
		String hname=header_str.substring(0,colon).trim();
		String hvalue=header_str.substring(++colon).trim();
		return new Header(hname,hvalue);       
	}

	/** Returns the first occurence of Header <i>hname</i>. */
	public Header getHeader(String hname)     {
		SipParser par=new SipParser(str,indexOfHeader(hname));
		if (!par.hasMore()) return null;
		par.skipN(hname.length());
		int begin=par.indexOf(':')+1;
		int end=par.indexOfEOH();
		if (begin>end) return null;
		String hvalue=str.substring(begin,end).trim();
		index=end;
		return new Header(hname,hvalue);
	}
	

	//************************ first-line ************************

	/** Returns the request-line. */
	public RequestLine getRequestLine()     {
		String method=getString();
		skipWSP();
		GenericURI uri=(new SipParser(getString(indexOfEOH()-getPos()))).getURI();
		goToNextLine();
		return new RequestLine(method,uri);
	}

	/** Returns the status-line or null (if it doesn't start with "SIP/"). */
	public StatusLine getStatusLine() {
		String version=getString(4);     
		if (!version.equalsIgnoreCase("SIP/")) {  index=str.length(); return null;  } 
		skipString().skipWSP(); // "SIP/2.0 "
		int code=getInt();
		String reason=getString(indexOfEOH()-getPos()).trim();
		goToNextLine();
		return new StatusLine(code,reason);
	}


	//*************************** URIs ***************************

	public static final char[] uri_separators={' ','>','\n','\r'};
	
	public static final String[] uri_schemes={"sip:","sips:","tel:"};

	public static final String[] naddr_uri_schemes={"<sip:","<sips:","<tel:"};

	/** Returns the first SIP-compatible URI.
	  * If no valid SIP-compatible URI is found, it returns <b>null</b> */
	public GenericURI getURI() {
		goTo(uri_schemes);
		if (!hasMore()) return null;
		int begin=getPos();
		int end=indexOf(uri_separators);
		if (end<0) end=str.length();
		String uri=getString(end-begin);
		if (hasMore()) skipChar();
		return new GenericURI(uri);
	}
/* 
	public static SipURI parseSipURI(String s) {
		SipParser par=new SipParser(s);
		return par.parseSipURI();
	}
*/
	/** Returns the first NameAddress in the string <i>str</i>.
	  * If no NameAddress is found, it returns <b>null</b>.  
	  * A NameAddress is a string of the form of:
	  * <BR><BLOCKQUOTE><PRE>&nbsp;&nbsp; "user's name" &lt;sip uri&gt; </PRE></BLOCKQUOTE> */
	public NameAddress getNameAddress() {
		String text=null;
		GenericURI uri=null;
		int begin=getPos();
		int uri_begin=indexOf(naddr_uri_schemes);
		//System.out.println("DEBUG: SipParser: parseNameAddress(): str="+this.getRemainingString());
		//System.out.println("DEBUG: SipParser: parseNameAddress(): index="+uri_begin);
		if (uri_begin<0)  {
			uri=getURI();
			//if (uri==null) return null;
			if (uri==null) {
				setPos(begin);
				uri=new SipURI(getString());
			}
			return new NameAddress(uri);
		}
		else {
			text=getString(uri_begin-begin).trim();
			uri=getURI();
			if (text.length()>0 && text.charAt(0)=='\"' && text.charAt(text.length()-1)=='\"') {
				text=text.substring(1,text.length()-1); 
				// now you should eliminate escape chars ('\')..
			}
			if (text.length()==0) return new NameAddress(uri);
			else return new NameAddress(text,uri); 
		}
	}

	/** Skips the first NameAddress.
	  * If no NameAddress is found, it goes to the end of the string. */
/*
	public SipParser skipNameAddress() {
		int uri_begin=indexOf("<sip:");
		if (uri_begin<0) uri_begin=indexOf("sip:");
		if (uri_begin<0) index=str.length();
		else {
			goTo(uri_separators);
			skipChars(uri_separators);
		}
		return this;
	}
*/

	/** Returns the first quoted string in the string <i>str</i>; if no NameAddress is found, it returns <b>null</b>*/
/*public String parseQuotedString() {
		int begin=str.indexOf('\"',index);
		if (begin<0) return null;
		begin++;
		int end=str.indexOf('\"',begin);
		String quotedtext=str.substring(begin,end);
		index=end;
		return quotedtext;
	}
*/

	//*************************** DATE ***************************


	/** Returns a Date object according with the SIP standard date format */
	public Date getDate() {
		
		//DateFormat df=new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss 'GMT'",Locale.US);
		try {
			//Date d=df.parse(str,new ParsePosition(index));
			Date d=DateFormat.parseEEEddMMMyyyyhhmmss(str,index);
			index=str.indexOf("GMT",index)+3;
			return d;
		}
		catch (Exception e) { e.printStackTrace(); index=str.length(); return null; }
	}   
	

	//*************************** PARAMETERS ***************************

	public static char[] param_separators={ ' ', '=', ';', ',', '\n', '\r' };   

	/** Gets the value of specified parameter.
	  * @param name the parameter name
	  * @return the parameter value or null if parameter does not exist or doesn't have a value (i.e. in case of flag parameter). */
	public String getParameter(String name)  {
		while (hasMore()) {
			if (getWord(param_separators).equals(name)) {
				skipWSP();
				if (nextChar()=='=') {
					skipChar(); 
					return getWordSkippingQuoted(param_separators);
				}
				else return null;
			}
			goToSkippingQuoted(';');
			if (hasMore()) skipChar(); // skip ';'
		}
		return null;
	}
		
	/** Gets a String Vector of parameter names.
	  * @return Returns a String Vector of all parameter names or null if no parameter is present. */
	public Vector getParameterNames()  {
		String name;
		Vector params=new Vector();
		while (hasMore()) {
			name=getWord(param_separators);
			if (name.length()>0) params.addElement(new String(name));
			goToSkippingQuoted(';');
			if (hasMore()) skipChar(); // skip ';'
		}
		return params;       
	}
	
	/** Whether there is the specified parameter */
	public boolean hasParameter(String name) {
		while (hasMore()) {
			if (getWord(param_separators).equals(name)) return true;
			goToSkippingQuoted(';');
			if (hasMore()) skipChar(); // skip ';'
		}
		return false;
	}
	

	//************************ MULTIPLE HEADERS ************************
	
	/** Finds the first comma-separator. Return -1 if no comma is found. */
	public int indexOfCommaHeaderSeparator() {
		boolean inside_quoted_string=false;
		for (int i=index; i<str.length(); i++) {
			char c=str.charAt(i); 
			if (c=='"') inside_quoted_string=!inside_quoted_string;
			if (!inside_quoted_string && c==',') return i;
		}
		return -1;
	}   

	/** Goes to the first comma-separator. Goes to the end of string if no comma is found. */
	public SipParser goToCommaHeaderSeparator() {
		int comma=indexOfCommaHeaderSeparator();
		if (comma<0) index=str.length();
		else index=comma;
		return this;
	}   


	//************************** SIP MESSAGE ***************************
	
	/** Gets the first SIP message (all bytes until the first end of SIP message),
	  * if a SIP message delimiter is found.
	  * <p>The message begins from the first non-CRLF char. */
	/*public SipMessage getSipMessage() {
		// skip any CRLF sequence
		skipCRLF();
		// Get content length; if no Content-Length header is found, return null
		String text=(getPos()==0)? str : getRemainingString();
		SipMessage msg=new SipMessage(text);
		if (!msg.hasContentLengthHeader()) return null;
		int body_len=msg.getContentLengthHeader().getContentLength();

		// gets the message (and go ahead), or returns null
		int begin=getPos();
		goToEndOfLastHeader();
		if (!hasMore()) return null;
		goTo('\n');
		if (!hasMore()) return null;
		skipChar().goTo('\n'); // skip the LF of last header and go the the new line
		if (!hasMore()) return null;
		int body_pos=skipChar().getPos(); // skip the LF of the empty line and go the the body
		
		int end=body_pos+body_len;
		if (end<=str.length()) {
			index=end;
			return new SipMessage(str.substring(begin,end));
		}
		else return null;
	}*/

}
