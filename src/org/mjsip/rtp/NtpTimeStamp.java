/*
 * Copyright (C) 2013 Luca Veltri - University of Parma - Italy
 * 
 * This source code is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This source code is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this source code; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 * 
 * Author(s):
 * Luca Veltri (luca.veltri@unipr.it)
 */

package org.mjsip.rtp;




/** Wallclock time (absolute date and time) represented using the
  * timestamp format of the Network Time Protocol (NTP).
  * <br>
  * It is the absolute time in seconds relative to 0h UTC on January 1, 1900.
  * The full resolution NTP timestamp is a 64-bit unsigned fixed-point number with
  * the integer part in the first 32 bits and the fractional part in the
  * last 32 bits.  In some fields where a more compact representation is
  * appropriate, only the middle 32 bits are used; that is, the low 16
  * bits of the integer part and the high 16 bits of the fractional part.
  * The high 16 bits of the integer part must be determined
  * independently.
  * <p>
  * Since only 32 bits are used for the integer part, February 7, 2036, at 6h:28m:15s
  * is the last absolute time returned by the method getTime(), while at 6h:28m:16s
  * it wraps and go back to January 1, 1900.
  * <br>
  * In order to have a longer absolute period, the time returned by method getTime() is
  * within the interval from January 20, 1968, at 4h:14m:8s  GMT,
  * until February 26, 2014, at 9h:42m:23s GMT.
  * <p>
  * However, for RTP purposes, only differences between pairs of NTP
  * timestamps are used.  So long as the pairs of timestamps can be
  * assumed to be within 68 years of each other, using modular arithmetic
  * for subtractions and comparisons makes the wraparound irrelevant.
  * <p>
  * The Network Time Protocol and the timestamp format are defined in RFC 1305
  * (Network Time Protocol (Version 3) Specification, Implementation and Analysis), and
  * RFC 2030 (Simple Network Time Protocol (SNTP) Version 4 for IPv4, IPv6 and OSI).
  */
public class NtpTimeStamp implements Comparable {
	

	/* Integer part of NTP seconds (from January 1, 1900) */
	long seconds;

	/* Fractional part of NTP seconds */
	long fraction;

	/** NTP time 0 (Jan 1, 1900), in Java time format (that is from January 1, 1970) */
	public static final long NTP_TIME_0=-2208988800000L;



	/** Creates a new NtpTimeStamp.
	  * @param seconds the integer part of NTP seconds (in NTP format, that is from January 1, 1900)
	  * @param fraction the fractional part of NTP seconds (in NTP format, that is from January 1, 1900) */
	public NtpTimeStamp(long seconds, long fraction) {
		this.seconds=seconds;
		this.fraction=fraction;
	}


	/** Creates a new NtpTimeStamp.
	  * @param time the timestamp time value (in Java format, that is from Java epoch time January 1, 1970) */
	public NtpTimeStamp(long time) {
		long milisecs=time-NTP_TIME_0;
		seconds=(milisecs/1000)&0xffffffffL;
		fraction=((milisecs%1000)<<32)/1000;
	}


	/**Gets the NTP timestamp time (in NTP format).
	  * @return the NTP timestamp time value (in NTP format). */
	public long getNtpTime() {
		return (seconds<<32) | fraction;
	}
	
	
	/** Gets the integer part of NTP seconds.
	  * @return the seconds of this NTP timestamp. */
	public long getNtpSeconds() {
		return seconds;
	}
	
	
	/** Gets the fractional part of NTP seconds.
	  * @return the fractional seconds of this NTP timestamp. */
	public long getNtpFraction() {
		return fraction;
	}
	
	
	/** Gets the timestamp time (in Java format, thati is from Java epoch time January 1, 1970).
	  * It returns the correct time only until February 26, 2014, at 9h:42m:23s GMT;
	  * after this date the time wraps and goes back to January 20, 1968, at 4h:14m:8s  GMT.
	  * @return the timestamp time value (in Java format, thati is from Java epoch time January 1, 1970) */
	public long getTime() {
		return (((seconds&0x80000000L)==0x80000000L)?seconds:(seconds+0x100000000L))*1000+fraction+NTP_TIME_0;
	}


	/** From interface Comparable. Compares this object with the specified object for order.
	  * @param obj the Object to be compared
	  * @return a negative integer, zero, or a positive integer as this object is less than, equal to, or greater than the specified object */
	public int compareTo(Object obj) throws ClassCastException {
		NtpTimeStamp ts=(NtpTimeStamp)obj;
		return (int)(getNtpTime()-ts.getNtpTime());
	}


	/** Whether it is equal to the specified object.
	  * @param obj the Object to be compared
	  * @return true it is equal to the specified object */
	public boolean equals(Object obj) {
		if (obj instanceof NtpTimeStamp) {
			NtpTimeStamp ts=(NtpTimeStamp)obj;
			return seconds==ts.seconds && fraction==ts.fraction;
		}
		return false;
	}


	/** Gets a string representation of this object. */
	 public String toString()
	 {  StringBuffer sb=new StringBuffer();
		 sb.append(Long.toHexString(seconds));
		 sb.append('.');
		 sb.append(Long.toHexString(fraction));
		 return sb.toString();
	 }

}
