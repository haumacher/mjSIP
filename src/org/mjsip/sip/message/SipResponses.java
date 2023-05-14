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
	

	/** Gets the reason phrase of a given response code.
	  * @param code the response code
	  * @return the reason phrase of a given response code */
	public static String reasonOf(int code) {
		switch (code) {
			// 1xx Provisional
			case 100 : return "Trying";
			case 180 : return "Ringing";
			case 181 : return "Call Is Being Forwarded";
			case 182 : return "Queued";
			case 183 : return "Session Progress";
			case 199 : return "Early Dialog Terminated";
			
			// 2xx Successful
			case 200 : return "OK";
			case 202 : return "Accepted (Deprecated)";
			case 204 : return "No Notification";
			
			// 3xx Redirection
			case 300 : return "Multiple Choices";
			case 301 : return "Moved Permanently";
			case 302 : return "Moved Temporarily";
			case 305 : return "Use Proxy";
			case 380 : return "Alternative Service";

			// 4xx Request Failure
			case 400 : return "Bad Request";
			case 401 : return "Unauthorized";
			case 402 : return "Payment Required";
			case 403 : return "Forbidden";
			case 404 : return "Not Found";
			case 405 : return "Method Not Allowed";
			case 406 : return "Not Acceptable";
			case 407 : return "Proxy Authentication Required";
			case 408 : return "Request Timeout";
			case 410 : return "Gone";
			case 412 : return "Conditional Request Failed";
			case 413 : return "Request Entity Too Large";
			case 414 : return "Request-URI Too Long";
			case 415 : return "Unsupported Media Type";
			case 416 : return "Unsupported URI Scheme";
			case 417 : return "Unknown Resource-Priority";
			case 420 : return "Bad Extension";
			case 421 : return "Extension Required";
			case 422 : return "Session Interval Too Small";
			case 423 : return "Interval Too Brief";
			case 424 : return "Bad Location Information";
			case 428 : return "Use Identity Header";
			case 429 : return "Provide Referrer Identity";
			case 430 : return "Flow Failed";
			case 433 : return "Anonymity Disallowed";
			case 436 : return "Bad Identity-Info";
			case 437 : return "Unsupported Certificate";
			case 438 : return "Invalid Identity Header";
			case 439 : return "First Hop Lacks Outbound Support";
			case 440 : return "Max-Breadth Exceeded";
			case 469 : return "Bad Info Package";
			case 470 : return "Consent Needed";
			case 480 : return "Temporarily Unavailable";
			case 481 : return "Call/Transaction Does Not Exist";
			case 482 : return "Loop Detected";
			case 483 : return "Too Many Hops";
			case 484 : return "Address Incomplete";
			case 485 : return "Ambiguous";
			case 486 : return "Busy Here";
			case 487 : return "Request Terminated";
			case 488 : return "Not Acceptable Here";
			case 489 : return "Bad Event";
			case 491 : return "Request Pending";
			case 493 : return "Undecipherable";
			case 494 : return "Security Agreement Required";

			// 5xx Server Failure
			case 500 : return "Server Internal Error";
			case 501 : return "Not Implemented";
			case 502 : return "Bad Gateway";
			case 503 : return "Service Unavailable";
			case 504 : return "Server Time-out";
			case 505 : return "Version Not Supported";
			case 513 : return "Message Too Large";
			case 580 : return "Precondition Failure";

			// 6xx Global Failures
			case 600 : return "Busy Everywhere";
			case 603 : return "Decline";
			case 604 : return "Does Not Exist Anywhere";
			case 606 : return "Not Acceptable";
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
