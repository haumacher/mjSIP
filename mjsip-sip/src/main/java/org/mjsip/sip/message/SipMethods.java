/*
 * Copyright (C) 2013 Luca Veltri - University of Parma - Italy
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

package org.mjsip.sip.message;




/** All SIP method names, according to the IANA SIP registry.
  */
public class SipMethods {
	

	/** Method name ACK, defined in RFC3261 */
	public static final String ACK="ACK";

	/** Method name BYE, defined in RFC3261 */
	public static final String BYE="BYE";

	/** Method name CANCEL, defined in RFC3261 */
	public static final String CANCEL="CANCEL";

	/** Method name INFO, defined in RFC6086 */
	public static final String INFO="INFO";

	/** Method name INVITE, defined in RFC3261 */
	public static final String INVITE="INVITE";

	/** Method name MESSAGE, defined in RFC3428 */
	public static final String MESSAGE="MESSAGE";

	/** Method name NOTIFY, defined in RFC6665 */
	public static final String NOTIFY="NOTIFY";

	/** Method name OPTIONS, defined in RFC3261 */
	public static final String OPTIONS="OPTIONS";

	/** Method name PRACK, defined in RFC3262 */
	public static final String PRACK="PRACK";

	/** Method name PUBLISH, defined in RFC3903 */
	public static final String PUBLISH="PUBLISH";

	/** Method name REFER, defined in RFC3515 */
	public static final String REFER="REFER";

	/** Method name REGISTER, defined in RFC3261 */
	public static final String REGISTER="REGISTER";

	/** Method name SUBSCRIBE, defined in RFC6665 */
	public static final String SUBSCRIBE="SUBSCRIBE";

	/** Method name UPDATE, defined in RFC3311 */
	public static final String UPDATE="UPDATE";



	/** Whether the given method is ACK.
	  * @param name the method name
	  * @return true if the method is ACK */
	public static boolean isAck(String name) { return same(name,ACK); }

	/** Whether the given method is BYE.
	  * @param name the method name
	  * @return true if the method is BYE */
	public static boolean isBye(String name) { return same(name,BYE); }

	/** Whether the given method is CANCEL.
	  * @param name the method name
	  * @return true if the method is CANCEL */
	public static boolean isCancel(String name) { return same(name,CANCEL); }

	/** Whether the given method is INFO.
	  * @param name the method name
	  * @return true if the method is INFO */
	public static boolean isInfo(String name) { return same(name,INFO); }

	/** Whether the given method is INVITE.
	  * @param name the method name
	  * @return true if the method is INVITE */
	public static boolean isInvite(String name) { return same(name,INVITE); }

	/** Whether the given method is MESSAGE.
	  * @param name the method name
	  * @return true if the method is MESSAGE */
	public static boolean isMessage(String name) { return same(name,MESSAGE); }

	/** Whether the given method is NOTIFY.
	  * @param name the method name
	  * @return true if the method is NOTIFY */
	public static boolean isNotify(String name) { return same(name,NOTIFY); }

	/** Whether the given method is OPTIONS.
	  * @param name the method name
	  * @return true if the method is OPTIONS */
	public static boolean isOptions(String name) { return same(name,OPTIONS); }

	/** Whether the given method is PRACK.
	  * @param name the method name
	  * @return true if the method is PRACK */
	public static boolean isPrack(String name) { return same(name,PRACK); }

	/** Whether the given method is PUBLISH.
	  * @param name the method name
	  * @return true if the method is PUBLISH */
	public static boolean isPublish(String name) { return same(name,PUBLISH); }

	/** Whether the given method is REFER.
	  * @param name the method name
	  * @return true if the method is REFER */
	public static boolean isRefer(String name) { return same(name,REFER); }

	/** Whether the given method is REGISTER.
	  * @param name the method name
	  * @return true if the method is REGISTER */
	public static boolean isRegister(String name) { return same(name,REGISTER); }

	/** Whether the given method is SUBSCRIBE.
	  * @param name the method name
	  * @return true if the method is SUBSCRIBE */
	public static boolean isSubscribe(String name) { return same(name,SUBSCRIBE); }

	/** Whether the given method is UPDATE.
	  * @param name the method name
	  * @return true if the method is UPDATE */
	public static boolean isUpdate(String name) { return same(name,UPDATE); }




	/** Array of all standard methods, defined by RFC3261 and successive extensions (RFC 2976, RFC3262, RFC3311, RFC3428, etc.) */
	//static final String[] methods={ INVITE,ACK,OPTIONS,BYE,CANCEL,REGISTER,INFO,PRACK,UPDATE,SUBSCRIBE,NOTIFY,MESSAGE,REFER,PUBLISH };
	protected static String[] methods=null;
	
	/** Array of all methods that create a dialog */
	protected static final String[] dialog_methods={ INVITE,SUBSCRIBE };



	/** Gets all standard SIP methods.
	  * @return array of all standard SIP methods, defined by RFC3261 and successive extensions (RFC 2976, RFC3262, RFC3311, RFC3428, etc.) */
	public static String[] getAllSipMethods() {
		if (methods==null) {
			//java.lang.reflect.Field[] fields=Class.forName("org.mjsip.sip.message.SipMethods").getFields();
			java.lang.reflect.Field[] fields=new SipMethods().getClass().getFields();
			//String[] names=new String[fields.length];
			methods=new String[fields.length];
			for (int i=0; i<fields.length; i++) methods[i]=fields[i].getName();
		}
		return methods;
	}


	/** Whether the given method is a standard SIP method.
	  * @param name the method name
	  * @return true if it is a standard SIP method */
	public static boolean isSipMethod(String name) {
		String[] methods=getAllSipMethods();
		for (int i=0; i<methods.length; i++) if (same(name,methods[i])) return true;
		return false;
	}


	/** Gets standard methods that create a SIP dialog.
	  * @return array of all standard methods that create a SIP dialog */
	public static String[] getCreateDialogMethods() {
		return dialog_methods;
	}


	/** Whether the given method creates a SIP dialog.
	  * @param name the method name
	  * @return true if it is a standard method creates a SIP dialog */
	public static boolean isCreateDialogMethod(String name) {
		for (int i=0; i<dialog_methods.length; i++) if (same(name,dialog_methods[i])) return true;
		return false;
	}


	/** Whether the two names are case-unsensitive-equal. */
	protected static boolean same(String s1, String s2) {
		return s1.equalsIgnoreCase(s2);
	} 
	
}
