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

/** All raeson-phrases of SIP response codes, according to the IANA SIP registry.
  */
public class SipResponses {
	
	public static final int TRYING = 100;

	public static final int RINGING = 180;

	public static final int CALL_BEING_FORWARDED = 181;

	public static final int QUEUED = 182;

	public static final int SESSION_PROGRESS = 183;

	public static final int EARLY_DIALOG_TERMINATED = 199;


	public static final int OK = 200;

	public static final int ACCEPTED = 202;

	public static final int NO_NOTIFICATION = 204;

	public static final int MULTIPLE_CHOICES = 300;

	public static final int MOVED_PERMANENTLY = 301;

	public static final int MOVED_TEMPORARILY = 302;

	public static final int USE_PROXY = 305;

	public static final int ALTERNATIVE_SERVICE = 380;


	public static final int BAD_REQUEST = 400;

	public static final int UNAUTHORIZED = 401;

	public static final int PAYMENT_REQUIRED = 402;

	public static final int FORBIDDEN = 403;

	public static final int NOT_FOUND = 404;

	public static final int METHOD_NOT_ALLOWED = 405;

	public static final int NOT_ACCEPTABLE = 406;

	public static final int PROXY_AUTHENTICATION_REQUIRED = 407;

	public static final int REQUEST_TIMEOUT = 408;

	public static final int GONE = 410;

	public static final int CONDITIONAL_REQUEST_FAILED = 412;

	public static final int REQUEST_ENTITY_TOO_LARGE = 413;

	public static final int REQUEST_URI_TOO_LONG = 414;

	public static final int UNSUPPORTED_MEDIA_TYPE = 415;

	public static final int UNSUPPORTED_URI_SCHEME = 416;

	public static final int UNKNOWN_RESOURCE_PRIORITY = 417;

	public static final int BAD_EXTENSION = 420;

	public static final int EXTENSION_REQUIRED = 421;

	public static final int SESSION_INTERVAL_TOO_SMALL = 422;

	public static final int INTERVAL_TOO_BRIEF = 423;

	public static final int BAD_LOCATION_INFORMATION = 424;

	public static final int USE_IDENTITY_HEADER = 428;

	public static final int PROVIDE_REFERRER_IDENTITY = 429;

	public static final int FLOW_FAILED = 430;

	public static final int ANONYMITY_DISALLOWED = 433;

	public static final int BAD_IDENTITY_INFO = 436;

	public static final int UNSUPPORTED_CERTIFICATE = 437;

	public static final int INVALID_IDENTITY_HEADER = 438;

	public static final int FIRST_HOP_LACKS_OUTBOUND_SUPPORT = 439;

	public static final int MAX_BREADTH_EXCEEDED = 440;

	public static final int BAD_INFO_PACKAGE = 469;

	public static final int CONSENT_NEEDED = 470;

	public static final int TEMPORARILY_UNAVAILABLE = 480;

	public static final int CALL_TRANSACTION_DOES_NOT_EXIST = 481;

	public static final int LOOP_DETECTED = 482;

	public static final int TOO_MANY_HOPS = 483;

	public static final int ADDRESS_INCOMPLETE = 484;

	public static final int AMBIGUOUS = 485;

	public static final int BUSY_HERE = 486;

	public static final int REQUEST_TERMINATED = 487;

	public static final int NOT_ACCEPTABLE_HERE = 488;

	public static final int BAD_EVENT = 489;

	public static final int REQUEST_PENDING = 491;

	public static final int UNDECIPHERABLE = 493;

	public static final int SECURITY_AGREEMENT_REQUIRED = 494;


	public static final int SERVER_INTERNAL_ERROR = 500;

	public static final int NOT_IMPLEMENTED = 501;

	public static final int BAD_GATEWAY = 502;

	public static final int SERVICE_UNAVAILABLE = 503;

	public static final int SERVER_TIME_OUT = 504;

	public static final int VERSION_NOT_SUPPORTED = 505;

	public static final int MESSAGE_TOO_LARGE = 513;

	public static final int PRECONDITION_FAILURE = 580;


	public static final int BUSY_EVERYWHERE = 600;

	public static final int DECLINE = 603;

	public static final int DOES_NOT_EXIST_ANYWHERE = 604;

	public static final int NOT_ACCEPTABLE_GLOBAL = 606;


	/** Gets the reason phrase of a given response code.
	  * @param code the response code
	  * @return the reason phrase of a given response code */
	public static String reasonOf(int code) {
		switch (code) {
			// 1xx Provisional
			case TRYING:
				return "Trying";
			case RINGING:
				return "Ringing";
			case CALL_BEING_FORWARDED:
				return "Call Is Being Forwarded";
			case QUEUED:
				return "Queued";
			case SESSION_PROGRESS:
				return "Session Progress";
			case EARLY_DIALOG_TERMINATED:
				return "Early Dialog Terminated";
			
			// 2xx Successful
			case OK:
				return "OK";
			case ACCEPTED:
				return "Accepted (Deprecated)";
			case NO_NOTIFICATION:
				return "No Notification";
			
			// 3xx Redirection
			case MULTIPLE_CHOICES:
				return "Multiple Choices";
			case MOVED_PERMANENTLY:
				return "Moved Permanently";
			case MOVED_TEMPORARILY:
				return "Moved Temporarily";
			case USE_PROXY:
				return "Use Proxy";
			case ALTERNATIVE_SERVICE:
				return "Alternative Service";

			// 4xx Request Failure
			case BAD_REQUEST:
				return "Bad Request";
			case UNAUTHORIZED:
				return "Unauthorized";
			case PAYMENT_REQUIRED:
				return "Payment Required";
			case FORBIDDEN:
				return "Forbidden";
			case NOT_FOUND:
				return "Not Found";
			case METHOD_NOT_ALLOWED:
				return "Method Not Allowed";
			case NOT_ACCEPTABLE:
				return "Not Acceptable";
			case PROXY_AUTHENTICATION_REQUIRED:
				return "Proxy Authentication Required";
			case REQUEST_TIMEOUT:
				return "Request Timeout";
			case GONE:
				return "Gone";
			case CONDITIONAL_REQUEST_FAILED:
				return "Conditional Request Failed";
			case REQUEST_ENTITY_TOO_LARGE:
				return "Request Entity Too Large";
			case REQUEST_URI_TOO_LONG:
				return "Request-URI Too Long";
			case UNSUPPORTED_MEDIA_TYPE:
				return "Unsupported Media Type";
			case UNSUPPORTED_URI_SCHEME:
				return "Unsupported URI Scheme";
			case UNKNOWN_RESOURCE_PRIORITY:
				return "Unknown Resource-Priority";
			case BAD_EXTENSION:
				return "Bad Extension";
			case EXTENSION_REQUIRED:
				return "Extension Required";
			case SESSION_INTERVAL_TOO_SMALL:
				return "Session Interval Too Small";
			case INTERVAL_TOO_BRIEF:
				return "Interval Too Brief";
			case BAD_LOCATION_INFORMATION:
				return "Bad Location Information";
			case USE_IDENTITY_HEADER:
				return "Use Identity Header";
			case PROVIDE_REFERRER_IDENTITY:
				return "Provide Referrer Identity";
			case FLOW_FAILED:
				return "Flow Failed";
			case ANONYMITY_DISALLOWED:
				return "Anonymity Disallowed";
			case BAD_IDENTITY_INFO:
				return "Bad Identity-Info";
			case UNSUPPORTED_CERTIFICATE:
				return "Unsupported Certificate";
			case INVALID_IDENTITY_HEADER:
				return "Invalid Identity Header";
			case FIRST_HOP_LACKS_OUTBOUND_SUPPORT:
				return "First Hop Lacks Outbound Support";
			case MAX_BREADTH_EXCEEDED:
				return "Max-Breadth Exceeded";
			case BAD_INFO_PACKAGE:
				return "Bad Info Package";
			case CONSENT_NEEDED:
				return "Consent Needed";
			case TEMPORARILY_UNAVAILABLE:
				return "Temporarily Unavailable";
			case CALL_TRANSACTION_DOES_NOT_EXIST:
				return "Call/Transaction Does Not Exist";
			case LOOP_DETECTED:
				return "Loop Detected";
			case TOO_MANY_HOPS:
				return "Too Many Hops";
			case ADDRESS_INCOMPLETE:
				return "Address Incomplete";
			case AMBIGUOUS:
				return "Ambiguous";
			case BUSY_HERE:
				return "Busy Here";
			case REQUEST_TERMINATED:
				return "Request Terminated";
			case NOT_ACCEPTABLE_HERE:
				return "Not Acceptable Here";
			case BAD_EVENT:
				return "Bad Event";
			case REQUEST_PENDING:
				return "Request Pending";
			case UNDECIPHERABLE:
				return "Undecipherable";
			case SECURITY_AGREEMENT_REQUIRED:
				return "Security Agreement Required";

			// 5xx Server Failure
			case SERVER_INTERNAL_ERROR:
				return "Server Internal Error";
			case NOT_IMPLEMENTED:
				return "Not Implemented";
			case BAD_GATEWAY:
				return "Bad Gateway";
			case SERVICE_UNAVAILABLE:
				return "Service Unavailable";
			case SERVER_TIME_OUT:
				return "Server Time-out";
			case VERSION_NOT_SUPPORTED:
				return "Version Not Supported";
			case MESSAGE_TOO_LARGE:
				return "Message Too Large";
			case PRECONDITION_FAILURE:
				return "Precondition Failure";

			// 6xx Global Failures
			case BUSY_EVERYWHERE:
				return "Busy Everywhere";
			case DECLINE:
				return "Decline";
			case DOES_NOT_EXIST_ANYWHERE:
				return "Does Not Exist Anywhere";
			case NOT_ACCEPTABLE_GLOBAL:
				return "Not Acceptable";
		}
		
		// else     
		switch (code/100) {
			// 1xx Provisional
			case 1 : return "Provisional";
			// 2xx Successful
			case 2 : return "Successful";
			// 3xx Redirection
			case 3 : return "Redirection";
			// 4xx Request Failure
			case 4 : return "Request Failure";
			// 5xx Server Failure
			case 5 : return "Server Failure";
			// 6xx Global Failures
			case 6 : return "Global Failures";
		}

		// else, it is not valid
		return null;
	}

	
}
