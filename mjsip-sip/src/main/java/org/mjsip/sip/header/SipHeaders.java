/*
 * Copyright (C) 2012 Luca Veltri - University of Parma - Italy
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

package org.mjsip.sip.header;




/** SipHeaders extends class {@link CoreSipHeaders} by adding new SIP header names.
  */
public class SipHeaders extends CoreSipHeaders {
	

	//****************************** Extensions *******************************/

	/** String "Allow-Events" */
	public static final String Allow_Events="Allow-Events";

	/** String "Event" */
	public static final String Event="Event";
	/** String "o" */
	public static final String Event_short="o";

	/** String "Info-Package" for Info-Package header field defined in RRC 6086 */
	public static final String Info_Package="Info-Package";

	/** String "Min-SE" */
	public static final String Min_SE="Min-SE";

	/** String "RAck" for RAck header field defined in RRC 3262 */
	public static final String RAck="RAck";  

	/** String "Reason" */
	public static final String Reason="Reason";  
	
	/** String "Recv-Info" for Recv-Info header field defined in RRC 6086 */
	public static final String Recv_Info="Recv-Info";

	/** String "Refer-To" */
	public static final String Refer_To="Refer-To";  

	/** String "Referred-By" */
	public static final String Referred_By="Referred-By"; 

	/** String "Replaces" */
	public static final String Replaces="Replaces"; 

	/** String "RSeq" for RSeq header field defined in RRC 3262 */
	public static final String RSeq="RSeq";  

	/** String "Service-Route" for Service-Route header field defined in RRC 3608 */
	public static final String ServiceRoute="Service-Route";  

	/** String "Session-Expires" */
	public static final String Session_Expires="Session-Expires";

	/** String "Subscription-State" */
	public static final String Subscription_State="Subscription-State";

}
