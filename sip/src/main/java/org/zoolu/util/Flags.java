/*
 * Copyright (C) 2018 Luca Veltri - University of Parma - Italy
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

package org.zoolu.util;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;


/** Gets the values of command-line options/parameters.
 * It parses an array of strings (command-line arguments) looking for given command-line options and/or parameters.
 * <p>
 * It can be used also for generating a "Usage" message with the list and description of all program options/parameters already parsed.
 * <p>
 * Example of command-line list:
 * <br>
 * <center>val1 -a val2 -h -v --beta val3 val4</center>
* <p>
 * Strings are of four types:
 * <ul>
 * <li> 'boolean option' with only the option tag, e.g. '-h' or '--help'
 * <li> 'value option' with both option tag and value, e.g. '--url http://127.0.0.1'
 * <li> 'tuple option' with option tag and a tuple of values, e.g. '--addrport 127.0.0.1 80'
 * <li> 'parameter' with only the parameter value, e.g. 'http://127.0.0.1'
 * </ul>
 * Parameters must be taken according to their order.
 * <p>
 * In the example above, '-a val2' and '--beta val3' are options, '-h' and '-v' are flags, while 'val1' and 'val4' are parameters. 
 */
public class Flags {

	/** Parameter */
	public static final String PARAM=null;
	/** Optional parameter */
	public static final String OPTIONAL_PARAM="optional-param";

	/** First writes parameters, then options */
	public static boolean FIRST_PARAMS_THEN_OPTIONS=true;
	/** Tab string for indenting all lines following the first "Usage" line. */
	public static String TAB1="  ";
	//public static String TAB1="";
	/** Tab string for indenting all lines following "where:" and "Options:" */
	public static String TAB2=TAB1+"  ";
	//public static String TAB2=TAB1+"\t";
	/** Tab string between the 'opt-param' part and the 'description' part */
	public static String TAB3="  ";
	//public static String TAB3="\t";

	
	/** Arguments to be parsed (ArrayList&lt;String&gt;) */
	ArrayList args;

	/** Option list (ArrayList&lt;Option&gt;) */
	ArrayList options=new ArrayList();

	/** Parameter list (ArrayList&lt;Option&gt;) */
	ArrayList params=new ArrayList();
	
	/** Whether '--not' option is enabled */
	boolean not_option=false;
	
	
	/** Creates options.
	 * @param args array of string contains the options and/or parameters. */
	public Flags(String[] args) {
		this.args=new ArrayList(Arrays.asList(args));
	}
	
	/** Parses the array of string for a given 'boolean option'.
	 * The option is removed from the list of unparsed strings.
	 * @param tag option tag (e.g. '-u' or '--help')
	 * @param description a description of this option/parameter
	 * @return <i>true</i> if the option is present */
	public boolean getBoolean(String tag, String description) {
		if (description!=null) options.add(new Option(tag,null,description));
		for (int i=0; i<args.size(); i++) {
			if (args.get(i).equals(tag)) {
				args.remove(i);
				return true;
			}
		}
		return false;
	}
	
	/** Parses the array of string for a given 'boolean option'.
	 * The option is removed from the list of unparsed strings.
	 * @param tag option tag (e.g. '-u' or '!-u')
	 * @param default_value default value
	 * @param description a description of this option/parameter
	 * @return the boolean value */
	public Boolean getBoolean(String tag, Boolean default_value, String description) {
		not_option=true;
		if (description!=null) options.add(new Option(tag,null,description));
		for (int i=0; i<args.size(); i++) {
			if (args.get(i).equals('!'+tag)) {
				args.remove(i);
				return new Boolean(false);
			}
			else
			if (args.get(i).equals(tag)) {
				args.remove(i);
				if (i>0 && args.get(i-1).equals("!")) {
					args.remove(i-1);
					return new Boolean(false);
				}
				else return new Boolean(true);
			}
		}
		return default_value;
	}
	
	/** Parses the array of strings for a given value option, or parameter.
	 * The option/parameter is removed from the list of unparsed strings.
	 * @param tag option tag (e.g. '-a' or '--add'); use {@link #PARAM} or {@link #OPTIONAL_PARAM} to indicate that iti is a parameter without tag
	 * @param param parameter string used to represent the value in the help string
	 * @param description a description of this option/parameter for the help string; if it is <i>null</i>, no description is added to the help string
	 * @return the value */
	private String getValue(String tag, String param, String description) {
		if (tag==PARAM || tag==OPTIONAL_PARAM) {
			// parameter
			if (description!=null && param!=null) params.add(new Option(tag,param,description));
			if (args.size()>0) {
				String value=(String)args.get(0);
				args.remove(0);
				return value;				
			}
		}
		else {
			// option
			if (description!=null && param!=null) options.add(new Option(tag,param,description));
			for (int i=0; i<args.size(); i++) {
				if (args.get(i).equals(tag)) {
					args.remove(i);
					String value=(String)args.get(i);
					args.remove(i);
					return value;
				}
			}
		}
		return null;
	}

	/** Parses the array of strings for a given tuple option.
	 * The option is removed from the list of unparsed strings.
	 * @param tag option tag; e.g. '-v' or '--values'
	 * @param len the number of components of the tuple
	 * @param param parameter string used to represent the parameter tuple in the help string
	 * @param description a description of this option for the help string; if it is <i>null</i>, no description is added to the help string
	 * @return the tuple */
	private String[] getTuple(String tag, int len, String param, String description) {
		if (tag==null || tag==PARAM || tag==OPTIONAL_PARAM) throw new RuntimeException("tuple tag is missing; tuple can't be of type 'parameter'");
		if (description!=null && param!=null) options.add(new Option(tag,param,description));
		for (int i=0; i<args.size(); i++) {
			if (args.get(i).equals(tag)) {
				args.remove(i);
				String[] tuple=new String[len];
				for (int k=0; k<len; k++) {
					tuple[k]=(String)args.get(i);
					args.remove(i);
				}
				return tuple;
			}
		}
		return null;
	}

	/** Parses the array of string for a given string option/parameter.
	 * The option/parameter is removed from the list of unparsed strings.
	 * @param tag option tag (e.g. '-a' or '--add'); use {@link #PARAM} or {@link #OPTIONAL_PARAM} to indicate that iti is a parameter without tag
	 * @param param parameter string used to represent the value in the help string;
	 * @param default_value default value
	 * @param description a description of this option/parameter for the help string; if it is <i>null</i>, no description is added to the help string
	 * @return the string value */
	public String getString(String tag, String param, String default_value, String description) {
		String value=getValue(tag,param,description);
		return value!=null? value : default_value;
	}

	/** Parses the array of string for a given integer option/parameter.
	 * The option/parameter is removed from the list of unparsed strings.
	 * @param tag option tag (e.g. '-a' or '--add'); use {@link #PARAM} or {@link #OPTIONAL_PARAM} to indicate that iti is a parameter without tag
	 * @param param parameter string; if not <i>null</i> it is the string used to represent the parameter value
	 * @param default_value default value
	 * @param description a description
	 * @return the integer value */
	public int getInteger(String tag, String param, int default_value, String description) {
		String value=getValue(tag,param,description);
		return value!=null? Integer.parseInt(value) : default_value;
	}

	/** Parses the array of string for a given long option/parameter.
	 * The option/parameter is removed from the list of unparsed strings.
	 * @param tag option tag (e.g. '-a' or '--add'); use {@link #PARAM} or {@link #OPTIONAL_PARAM} to indicate that iti is a parameter without tag
	 * @param param parameter string used to represent the value in the help string;
	 * @param default_value default value
	 * @param description a description of this option/parameter for the help string; if it is <i>null</i>, no description is added to the help string
	 * @return the long value */
	public long getLong(String tag, String param, long default_value, String description) {
		String value=getValue(tag,param,description);
		return value!=null? Long.parseLong(value) : default_value;
	}

	/** Parses the array of string for a given option/parameter and returns a float value.
	 * The option/parameter is removed from the list of unparsed strings.
	 * @param tag option tag (e.g. '-a' or '--add'); use {@link #PARAM} or {@link #OPTIONAL_PARAM} to indicate that iti is a parameter without tag
	 * @param param parameter string used to represent the value in the help string;
	 * @param default_value default value
	 * @param description a description of this option/parameter for the help string; if it is <i>null</i>, no description is added to the help string
	 * @return the float value */
	public float getFloat(String tag, String param, float default_value, String description) {
		String value=getValue(tag,param,description);
		return value!=null? Float.parseFloat(value) : default_value;
	}

	/** Parses the array of string for a given double option/parameter.
	 * The option/parameter is removed from the list of unparsed strings.
	 * @param tag option tag (e.g. '-a' or '--add'); use {@link #PARAM} or {@link #OPTIONAL_PARAM} to indicate that iti is a parameter without tag
	 * @param param parameter string used to represent the value in the help string
	 * @param default_value default value
	 * @param description a description of this option/parameter for the help string; if it is <i>null</i>, no description is added to the help string
	 * @return the double value */
	public double getDouble(String tag, String param, double default_value, String description) {
		String value=getValue(tag,param,description);
		return value!=null? Double.parseDouble(value) : default_value;
	}

	/** Parses the array of string for a given tuple option.
	 * The option is removed from the list of unparsed strings.
	 * @param tag option tag (e.g. '-v' or '--values')
	 * @param len the number of components of the tuple
	 * @param param parameter string used to represent the value in the help string
	 * @param default_value default value
	 * @param description a description of this option for the help string; if it is <i>null</i>, no description is added to the help string
	 * @return the string tuple */
	public String[] getStringTuple(String tag, int len, String param, String[] default_value, String description) {
		String[] tuple=getTuple(tag,len,param,description);
		return tuple!=null? tuple : default_value;
	}

	/** Gets the remaining parameters.
	 * @param optional whether these parameters are optional 
	 * @param param string used to represent the parameter values
	 * @param description a description of these parameters
	 * @return the strings that has not been parsed */
	public String[] getRemainingStrings(boolean optional, String param, String description) {
		if (description!=null && param!=null) params.add(new Option(optional?OPTIONAL_PARAM:null,param,description));
		String[] ss=(String[])args.toArray(new String[]{});
		args.clear();
		return ss;
	}

	/** Gets the number of unparsed strings.
	 * @return the number of remaining strings */
	public int size() {
		return args.size();
	}

	public String toString() {
		StringBuffer sb=new StringBuffer();
		final Option nopt=new Option(" !",null,"inverts the next option");
		// compute option-param part width
		//int oplen_max=0;
		int oplen_max=nopt.getTag().length();
		for (int i=0; i<options.size(); i++) {
			Option o=(Option)options.get(i);
			int len=o.getTag().length();
			if (o.getParam()!=null) len+=1+o.getParam().length();
			if (len>oplen_max) oplen_max=len;
		}
		for (int i=0; i<params.size(); i++) {
			Option p=(Option)params.get(i);
			int len=p.getParam().length();
			if (len>oplen_max) oplen_max=len;
		}
		// params
		if (params.size()>0) {
			sb.append("\r\n").append(TAB1).append("where:");			
			for (int i=0; i<params.size(); i++) {
				Option p=(Option)params.get(i);
				sb.append("\r\n").append(TAB2).append(p.toString(oplen_max,TAB3));
			}	
		}
		// options
		if (options.size()>0) {
			sb.append("\r\n").append(TAB1).append("Options:");
			Option[] sorted_options=(Option[])options.toArray(new Option[]{});
			Arrays.sort(sorted_options,new Comparator(){
				public int compare(Object o1, Object o2) {
					return ((Option)o1).getTag().compareTo(((Option)o2).getTag());
				}
			});
			for (int i=0; i<sorted_options.length; i++) {
				Option o=sorted_options[i];
				sb.append("\r\n").append(TAB2).append(o.toString(oplen_max,TAB3));
			}
			if (not_option) {
				sb.append("\r\n").append(TAB2).append(nopt.toString(oplen_max,TAB3));
			}

		}
		return sb.toString();
	}
	
	/** Gets the usage description.
	 * @param program the main class
	 * @return a string like "Usage: java program [options] ..." */
	public String toUsageString(String program) {
		StringBuffer sb=new StringBuffer();
		if (FIRST_PARAMS_THEN_OPTIONS || options.size()==0) sb.append("Usage: java ").append(program);
		else sb.append("Usage: java ").append(program).append(" [options]");
		for (int i=0; i<params.size(); i++) {
			Option p=(Option)params.get(i);
			if (p.getTag()==OPTIONAL_PARAM) sb.append(" [").append(p.getParam()).append("]");
			else sb.append(" ").append(p.getParam());
		}
		if (FIRST_PARAMS_THEN_OPTIONS && options.size()>0) sb.append(" [options]");
		sb.append(toString());
		return sb.toString();
	}
	
	
	// ************************ INNER CLASSES ************************
	
	/** Option.
	 */
	static class Option {
		
		/** Option tag */
		String tag;
		
		/** Parameter string */
		String param;
		
		/** Description */
		String description;
		
		/** Creates an option.
		 * @param tag option tag
		 * @param param a string representing the option parameter
		 * @param description a description */
		public Option(String tag, String param, String description) {
			this.tag=tag;
			this.param=param;
			this.description=description;
		}
		
		/** Gets the option tag. */
		public String getTag() {
			return tag;
		}
		
		/** Gets the string representing the option parameter. */
		public String getParam() {
			return param;
		}
		
		/** Gets the description. */
		public String getDescription() {
			return description;	
		}
		
		/** Gets a string representation of this option, with a fixed width of the 'opt-param' part.
		 * @param oplen minimum number of character of the 'opt-param' part
		 * @param sep separator between the 'opt-param' part and the 'description' part */
		public String toString(int oplen, String sep) {
			StringBuffer sb=new StringBuffer();
			if (tag!=PARAM && tag!=OPTIONAL_PARAM) {
				sb.append(tag);
				if (param!=null) sb.append(" ");
			}
			if (param!=null) sb.append(param);
			if (oplen>0) {
				int len=sb.length();
				for (int k=oplen-len; k>0; k--) sb.append(" ");
			}
			if (sep!=null) sb.append(sep);
			if (description!=null) sb.append(description);
			return sb.toString();
		}
		
		public String toString() {
			return toString(0," : ");
		}
	}

}
