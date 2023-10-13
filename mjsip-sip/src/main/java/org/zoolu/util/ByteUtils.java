/*
 * Copyright (C) 2008 Luca Veltri - University of Parma - Italy
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



import java.util.Arrays;



/** Class that collects static methods for dealing with binary materials.
  */
public class ByteUtils {
	

	// Arrays of bytes:

	/** Copies an array of bytes into another array.
	 * @param src the source array to be copied
	 * @param dst the destination array */
	public static void copy(byte[] src, byte[] dst) {
		copy(src,0,dst,0,src.length);
	}

	/** Copies an array of bytes into another array.
	 * @param src the source array to be copied
	 * @param dst the destination buffer where the array has to be copied
	 * @param dst_off the offset within the buffer */
	public static void copy(byte[] src, byte[] dst, int dst_off) {
		copy(src,0,dst,dst_off,src.length);
	}

	/** Copies an array of bytes into another array.
	 * @param src the buffer containing the source array to be copied
	 * @param src_off the offset within the buffer
	 * @param dst the destination buffer where the array has to be copied
	 * @param dst_off the offset within the buffer
	 * @param len the number of bytes to be copied */
	public static void copy(byte[] src, int src_off, byte[] dst, int dst_off, int len) {
		//for (int end=src_off+len; src_off<end; ) dst[dst_off++]=src[src_off++];
		System.arraycopy(src,src_off,dst,dst_off,len);
	}

	/** Gets a new array containing a copy of an array of bytes.
	  * @param src the array to be copied
	  * @return a copy of the original array */
	public static byte[] copy(byte[] src) {
		return copy(src,0,src.length);
	}

	/** Gets a new array containing a copy of an array of bytes.
	  * @param src the buffer containing the array to be copied
	  * @param off the offset within the buffer
	  * @param len the length of the array
	  * @return a copy of the original array */
	public static byte[] copy(byte[] src, int off, int len) {
		byte[] dst=new byte[len];
		//for (int k=0; k<len; k++) dst[k]=src[off+k];
		System.arraycopy(src,off,dst,0,len);
		return dst;
	}
	
	/** Compares two arrays of bytes.
	 * @param a1 one array
	 * @param a2 the other array
	 * @return <i>true</i> if the two arrays are equal */
	public static boolean match(byte[] a1, byte[] a2) {
		return match(a1,0,a1.length,a2,0,a2.length);
	}
	
	/** Compares two arrays of bytes.
	 * @param buf1 the buffer containing the first array
	 * @param off1 the offset within the buffer
	 * @param len1 the length of the first array
	 * @param buf2 the buffer containing the second array
	 * @param off2 the offset within the buffer
	 * @param len2 the length of the second array
	 * @return <i>true</i> if the two arrays are equal */
	public static boolean match(byte[] buf1, int off1, int len1, byte[] buf2, int off2, int len2) {
		if (len1!=len2) return false;
		for (int end1=off1+len1; off1<end1; ) if (buf1[off1++]!=buf2[off2++]) return false;
		return true;
	}
	
	/** Compares two arrays of bytes.
	 * @param a1 one array
	 * @param a2 the other array
	 * @return <ul>
	 *   <li> 0 if the two arrays are equal;
	 *   <li> 1 if they are different and, starting from left, all bits are equal and the length of the first array is greater than than the second, or the first bit that differs has value '1' in the first array (and '0' in the second one);
	 *   <li> -1 if they are different and, starting from left, all bits are equal and the length of the first array is lesser than than the second, or the first bit that differs has value '0' in the first array (and '1' in the second one)
	 * </ul> */
	public static int compare(byte[] a1, byte[] a2) {
		return compare(a1,0,a1.length,a2,0,a2.length);
	}

	/** Compares two arrays of bytes.
	 * @param buf1 the buffer containing the first array
	 * @param off1 the offset within the buffer
	 * @param len1 the length of the first array
	 * @param buf2 the buffer containing the second array
	 * @param off2 the offset within the buffer
	 * @param len2 the length of the second array
	 * @return <ul>
	 *   <li> 0 if the two arrays are equal;
	 *   <li> 1 if they are different and, starting from left, all bits are equal and the length of the first array is greater than than the second, or the first bit that differs has value '1' in the first array (and '0' in the second one);
	 *   <li> -1 if they are different and, starting from left, all bits are equal and the length of the first array is lesser than than the second, or the first bit that differs has value '0' in the first array (and '1' in the second one)
	 * </ul> */
	public static int compare(byte[] buf1, int off1, int len1, byte[] buf2, int off2, int len2) {
		int len=len1<len2? len1 : len2;
		for (int i=0; i<len; i++) { 
			int diff=(buf1[off1++]&0xff)-(buf2[off2++]&0xff);
			if (diff!=0) return diff>0? 1 : -1;
		}
		// else
		return len1==len2? 0 : len1>len2? 1 : -1;
	}
	
	/** Finds the first occurrence of one array of bytes within a second array of bytes.
	 * @param a1 the array to be searched
	 * @param buf2 the buffer containing the second array 
	 * @param off2 the offset within the second array
	 * @param len2 the length of the second array
	 * @return the index within the second array of the first occurrence of the first array, or -1 if not found. */
	public static int indexOf(byte[] a1, byte[] buf2, int off2, int len2) {
		return indexOf(a1,0,a1.length,buf2,off2,len2);
	}

	/** Finds the first occurrence of one array of bytes within a second array of bytes.
	 * @param buf1 the buffer containing the array to be searched
	 * @param off1 the offset within the buffer
	 * @param len1 the length of the array to be searched
	 * @param buf2 the buffer containing the second array 
	 * @param off2 the offset within the buffer
	 * @param len2 the length of the second array
	 * @return the index within the second array of the first occurrence of the first array, or -1 if not found. */
	public static int indexOf(byte[] buf1, int off1, int len1, byte[] buf2, int off2, int len2) {
		for (int i=0; i<=(len2-len1); i++)
			if (match(buf1,off1,len1,buf2,off2+i,len1)) return i;
		return -1;
	}

	/** Assigns the specified byte value to each element of the array of bytes.
	 * @param a the array of bytes
	 * @param val the value to be assigned */
	public static void fill(byte[] a, byte val) {
		fill(a,0,a.length,val);
	}

	/** Assigns the specified byte value to each element of an array of bytes.
	 * @param buf the buffer containing the array
	 * @param off the offset within the buffer
	 * @param len the length of the array
	 * @param val the value to be assigned */
	public static void fill(byte[] buf, int off, int len, byte val) {
		//for (int end=off+len; off<end; ) buf[off++]=val;
		Arrays.fill(buf,off,off+len,val);
	}

	/** Gets two bytes from a given array.
	  * @param src the source buffer containing the two bytes
	  * @param off the offset within the buffer
	  * @return a two-byte array */
	/*public static byte[] getTwoBytes(byte[] src, int off) {
		return copy(src,off,2);
	}*/
	
	/** Gets two bytes from a given array.
	 * @param src the buffer containing the two bytes to be copied
	 * @param dst the destination buffer where the two bytes have to be copied
	 * @param dst_off the offset within the destination buffer */
	/*public static void getTwoBytes(byte[] src, byte[] dst, int dst_off) {
		copy(src,0,dst,dst_off,2);
	}*/
	
	/** Gets four bytes from a given array.
	  * @param src the source buffer containing the four bytes
	  * @param off the offset within the buffer
	  * @return a four-byte array */
	/*public static byte[] getFourBytes(byte[] src, int off) {
		return copy(src,off,4);
	}*/
	
	/** Gets four bytes from a given array.
	 * @param src the buffer containing the four bytes to be copied
	 * @param dst the destination buffer where the four bytes have to be copied
	 * @param dst_off the offset within the destination buffer */
	/*public static void getFourBytes(byte[] src, byte[] dst, int dst_off) {
		copy(src,0,dst,dst_off,4);
	}*/

	/** Concatenates two arrays of bytes.
	  * @param src1 buffer containing the 1st array
	  * @param src2 buffer containing the 2nd array
	  * @return a new byte array containing the concatenation of the two arrays. */
	public static byte[] concat(byte[] src1, byte[] src2) {
		return concat(src1,0,src1.length,src2,0,src2.length);
	}

	/** Concatenates two arrays of bytes.
	  * @param src1 buffer containing the first array
	  * @param src2 buffer containing the second array
	  * @param dst3 buffer for the resulting concatenation array
	  * @return the length of the concatenation of the two arrays. */
	public static int concat(byte[] src1, byte[] src2, byte[] dst3) {
		return concat(src1,0,src1.length,src2,0,src2.length,dst3,0);
	}

	/** Concatenates two arrays of bytes.
	  * @param src1 buffer containing the first array
	  * @param off1 offset of the first array
	  * @param len1 length of the first array
	  * @param src2 buffer containing the second array
	  * @param off2 offset of the second array
	  * @param len2 length of the second array
	  * @return a new byte array containing the concatenation of the two arrays. */
	public static byte[] concat(byte[] src1, int off1, int len1, byte[] src2, int off2, int len2) {
		byte[] dst3=new byte[len1+len2];
		concat(src1,off1,len1,src2,off2,len2,dst3,0);
		return dst3;
	}
	
	/** Concatenates two arrays of bytes.
	  * @param src1 buffer containing the first array
	  * @param off1 offset of the first array
	  * @param len1 length of the first array
	  * @param src2 buffer containing the second array
	  * @param off2 offset of the second array
	  * @param len2 length of the second array
	  * @param dst3 buffer for the resulting concatenation array
	  * @param off3 offset of the resulting array
	  * @return the length of the concatenation of the two arrays. */
	public static int concat(byte[] src1, int off1, int len1, byte[] src2, int off2, int len2, byte[] dst3, int off3) {
		copy(src1,off1,dst3,off3,len1);
		copy(src2,off2,dst3,off3+len1,len2);
		return len1+len2;
	}


	// Bit shift:

	/** Rotates the integer (32-bit word) w shifting n bits left.
	  * @param w the integer to be rotated
	  * @param n the nuber of bits to be shifted to the left */
	/*public static int rotateLeft(int w, int n) {
		return (w << n) | (w >>> (32-n));
	}*/

	/** Rotates the integer (32-bit word) w shifting n bits right.
	  * @param w the integer to be rotated
	  * @param n the nuber of bits to be shifted to the right */
	/*public static int rotateRight(int w, int n) {
		return (w >>> n) | (w << (32-n));
	}*/

	/** Rotates an array of integers (32-bit words), shifting 1 word left.
	  * @param w the array of integers to be shifted to the left */
	/*public static int[] rotateLeft(int[] w) {
		int len=w.length;
		int w1=w[len-1];
		for (int i=len-1; i>1; i--) w[i]=w[i-1];
		w[0]=w1;
		return w;
	}*/

	/** Rotates an array of integers (32-bit words), shifting 1 word right.
	  * @param w the array of integers to be shifted to the right */
	/*public static int[] rotateRight(int[] w) {
		int len=w.length;
		int w0=w[0];
		for (int i=1; i<len; i++) w[i-1]=w[i];
		w[len-1]=w0;
		return w;
	}*/

	/** Rotates an array of bytes, shifting 1 byte left.
	  * @param b the array of bytes to be shifted to the left */
	/*public static byte[] rotateLeft(byte[] b) {
		int len=b.length;
		byte b1=b[len-1];
		for (int i=len-1; i>1; i--) b[i]=b[i-1];
		b[0]=b1;
		return b;
	}*/

	/** Rotates an array of bytes, shifting 1 byte right.
	  * @param b the array of bytes to be shifted to the right */
	/*public static byte[] rotateRight(byte[] b) {
		int len=b.length;
		byte b0=b[0];
		for (int i=1; i<len; i++) b[i-1]=b[i];
		b[len-1]=b0;
		return b;
	}*/


	// Unsigned integers:

	/** Gets the unsigned representation of a byte, returned as a <b>short</b>. The same as getting <i>(short)(b&amp;0xFF)</i>.
	  * @param b the byte value
	  * @return the unsigned integer value (as <b>short</b>) of the given byte. */
	public static short uByte(byte b) {
		return (short)(b&0xFF);
	} 

	/** Gets the unsigned representation of a 32-bit integer, returned as a <b>long</b>. The same as getting <i>(long)n&amp;0x00000000FFFFFFFF</i>.
	  * @param n the integer value
	  * @return the unsigned integer value (as <b>long</b>) of the given integer. */
	public static long uInt(int n) {
		return (long)n&0xFFFFFFFFL;
	} 


	// Array of bytes to hexadecimal string, and vice versa:

	/** Array of hexadecimal digits (from '0' to 'f'). */
	//private static char[] HEX_DIGITS={'0','1','2','3','4','5','6','7','8','9','a','b','c','d','e','f'};
	//private static char[] HEX_DIGITS={'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};

	/** Array of hexadecimal values (from 0 to 15). */
	/*private static int[] HEX_VALUES=new int[256];	
	static {
		for (char c=0; c<255; c++) HEX_VALUES[c]=(c>='0' && c<='9')? c-'0' : (c>='a' && c<='f')? c-'W' : (c>='A' && c<='F')? c-'7' : -1;
	}*/

	/** Gets the hexadecimal digit.
	 * The argument should be between 0 and 15. Values outside this interval give an undefined result. 
	 * @param d an integer between 0 and 15
	 * @return the hexadecimal digit, that is one of the following characters: '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', or 'f' */
	private static char hexDigit(int d) {
		//if (d<0 || d>15) throw new RuntimeException("Invalid value "+d);
		//return (char)(d<10? '0'+d : '7'+d); // uppercase (note: '7'='A'-10)
		return (char)(d<10? '0'+d : 'W'+d); // lowercase (note: 'W'='a'-10)
	}

	/** Whether a character is a hexadecimal digit (that is '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', or 'f').
	  * @param c the character
	  * @return true of it is a valid hexadecimal digit */
	private static boolean isHexDigit(char c) {
		return (c>='0' && c<='9') || (c>='a' && c<='f') || (c>='A' && c<='F');
	}

	/** Gets the value of a hexadecimal character.
	 * Both upper and lower case characters from 'a' to 'f' are valid input symbols.
	 * No check is performed on the argument; the result for non-hexadecimal character is undefined.
	 * @param c the hexadecimal symbol, that is one of the following characters: '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f', 'A', 'B', 'C', 'D', 'E', or 'F'
	 * @return the integer value (between 0 and 15) of the hexadecimal digit */
	private static int hexValue(char c) {
		//return HEX_VALUES[c];
		return c<='9'? c-'0' : (c<='F'? c-'7': c-'W');
	}

	/** Gets the value of a hexadecimal character.
	 * Both upper and lower case characters from 'a' to 'f' are valid input symbols.
	 * A RuntimeException is thrown in case of invalid character.
	 * @param c the hexadecimal symbol, that is one of the following characters: '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f', 'A', 'B', 'C', 'D', 'E', or 'F'
	 * @return the integer value (between 0 and 15) of the hexadecimal digit */
	private static int hexValueWithException(char c) {
		//return (c>=0 && c<='9')? c-'0' : (c>='a' && c<='f')? c-'W' : (c>='A' && c<='F')? c-'7' : -1;
		if (c>='0' && c<='9') return c-'0';
		if (c>='a' && c<='f') return c-'W';
		if (c>='A' && c<='F') return c-'7';
		//if (HEX_VALUES[c]>=0) return HEX_VALUES[c];
		// else
		throw new RuntimeException("Invalid hexadecimal character: '"+c+"'");
	}

	/** Gets a hexadecimal representation of an array of bytes.
	 * @param data the byte array
	 * @return the hexadecimal string */
	public static String bytesToHexString(byte[] data) {
		if (data==null) return null;
		// else
		return bytesToHexString(data,0,data.length);
	}
	
	/** Gets a hexadecimal representation of an array of bytes.
	 * @param buf buffer containing the byte array
	 * @param off the offset within the array
	 * @param len the number of bytes
	 * @return the hexadecimal string */
	public static String bytesToHexString(byte[] buf, int off, int len) {
		char[] str=new char[len*2];
		int index=0;
		int end=off+len;
		while (off<end) {
			byte b=buf[off++];
			//str[index++]=HEX_DIGITS[(b&0xF0)>>4];
			//str[index++]=HEX_DIGITS[b&0x0F];
			str[index++]=hexDigit((b&0xF0)>>4);
			str[index++]=hexDigit(b&0x0F);
		}
		return new String(str);
	}

	/** Gets a formatted (e.g. colon-separated) hexadecimal representation of an array of bytes.
	  * The hexadecimal values are formatted in blocks of a given size, separated by a given symbol.
	  * Example, in case of two-byte blocks (block size = 2) separated by colons, the string will be formatted as "aabb:ccdd:..:eeff". 
	  * @param data the byte array
	  * @param c the block separator
	  * @param num the number of bytes within each block
	  * @return the hexadecimal string */
	public static String bytesToFormattedHexString(byte[] data, char c, int num) {
		return bytesToFormattedHexString(data,0,data.length,c,num);
	}

	/** Gets a formatted (e.g. colon-separated) hexadecimal representation of an array of bytes.
	  * The hexadecimal values are formatted in blocks of a given size, separatedby a given symbol.
	  * Example, in case of two-byte blocks (block size = 2) separated by colons, the string will be formatted as "aabb:ccdd:..:eeff". 
	  * @param buf the buffer containing the byte array
	  * @param off the offset within the array
	  * @param len the length of the array
	  * @param c the block separator
	  * @param num the number of bytes within each block
	  * @return the hexadecimal string */
	public static String bytesToFormattedHexString(byte[] buf, int off, int len, char c, int num) {
		int str_len=len*2+((len+num-1)/num)-1;
		char[] str=new char[str_len];
		int index=0;
		for (int i=0; i<len; i++) {
			byte b=buf[off+i];
			//str[index++]=HEX_DIGITS[(b&0xF0)>>4];
			//str[index++]=HEX_DIGITS[b&0x0F];
			str[index++]=hexDigit((b&0xF0)>>4);
			str[index++]=hexDigit(b&0x0F);
			if (((i+1)%num)==0 && index<str_len) str[index++]=c;
		}
		return new String(str);
	}  

	/** Gets an unformatted hexadecimal string.
	  * It removes any non-hexadecimal character from the string. 
	  * @param str the formatted hexadecimal string
	  * @return the unformatted hexadecimal string */
	public static String trimHexString(String str) {
		StringBuffer sb=new StringBuffer();
		char c;
		for (int i=0; i<str.length(); i++) if (isHexDigit(c=str.charAt(i))) sb.append(c);
		return sb.toString();
	}

	/** Converts a hexadecimal string into an array of bytes.
	 * @param str the hexadecimal string
	 * @return the array of bytes */
	public static byte[] hexStringToBytes(String str) {
		byte[] data=new byte[str.length()/2];
		hexStringToBytes(str,data,0);
		return data;
	}

	/** Converts a hexadecimal string into an array of bytes.
	 * @param str the hexadecimal string
	 * @param buf the buffer where the array of bytes will be written
	 * @param off the offset within the buffer
	 * @return the length of the array */
	public static int hexStringToBytes(String str, byte[] buf, int off) {
		int str_len=str.length();
		//for (int i=0; i<str_len; i+=2) buf[off++]=(byte)Integer.parseInt(str.substring(i,i+2),16);
		for (int index=0; index<str_len; ) {
			char c0=str.charAt(index++);
			char c1=str.charAt(index++);
			buf[off++]=(byte)((hexValueWithException(c0)<<4)|hexValueWithException(c1));
		}
		return str_len/2;
	}

	/** Converts a formatted hexadecimal string into an array of bytes.
	 * @param str the string with hexadecimal characters possibly separated by the given character
	 * @param c the separator character
	 * @return the array of bytes */
	public static byte[] formattedHexStringToBytes(String str, char c) {
		return formattedHexStringToBytes(str,c,-1);
	}

	/** Converts a formatted hexadecimal string into an array of bytes.
	 * @param str the string with hexadecimal characters possibly separated by the given character
	 * @param c the separator character
	 * @param len the length of the array of bytes, or -1 if unknown
	 * @return the array of bytes */
	public static byte[] formattedHexStringToBytes(String str, char c, int len) {
		if (len<0) {
			// count the number of hex digits
			int str_len=str.length();
			len=0;
			for (int i=0; i<str_len; i++) if (str.charAt(i)!=c) len++;
			if ((len%2)==1) throw new RuntimeException("Odd number of hexadecimal characters: "+len);
			len/=2;
		}
		// get the byte array
		byte[] data=new byte[len];
		formattedHexStringToBytes(str,c,data,0);
		return data;
	}

	/** Converts a formatted hexadecimal string into an array of bytes.
	 * @param str the string with hexadecimal characters possibly separated by the given character
	 * @param c the separator character
	 * @param buf the buffer where the array of bytes will be written
	 * @param off the offset within the buffer
	 * @return the length of the array of bytes */
	public static int formattedHexStringToBytes(String str, char c, byte[] buf, int off) {
		int index=off;
		char c0,c1;
		for (int i=0; i<str.length(); ) {
			while ((c0=str.charAt(i++))==c);
			while ((c1=str.charAt(i++))==c);
			buf[index++]=(byte)((hexValueWithException(c0)<<4)+hexValueWithException(c1));
		}
		return index-off;
	}

	/** Gets a hexadecimal representation of an array of bytes.
	 * <p>
	 * The same as {@link #bytesToHexString(byte[])}
	 * @param data the byte array
	 * @return the hexadecimal string */
	public static String asHex(byte[] data) {
		return bytesToHexString(data);
	}  
	
	/** Gets a hexadecimal representation of an array of bytes.
	 * <p>
	 * The same as {@link #bytesToHexString(byte[], int, int)}
	 * @param buf buffer containing the byte array
	 * @param off the offset within the array
	 * @param len the number of bytes
	 * @return the hexadecimal string */
	public static String asHex(byte[] buf, int off, int len) {
		return bytesToHexString(buf,off,len);
	}

	/** Converts a hexadecimal string into an array of bytes.
	 * The string may also include colon ':' characters that separate blocks of hexadecimal values (e.g. aa:bb:cc:dd:..:ff). 
	 * <p>
	 * The same as {@link #formattedHexStringToBytes(String, char, int)} with <i>c=':'</i> and <i>len=-1</i>
	 * @param str the string with hexadecimal characters possibly separated by colon
	 * @return the array of bytes */
	public static byte[] hexToBytes(String str) {
		return formattedHexStringToBytes(str,':',-1);
	}

	/** Converts a hexadecimal string into an array of bytes.
	 * The string may also include colon ':' characters that separate blocks of hexadecimal values (e.g. aa:bb:cc:dd:..:ff). 
	 * <p>
	 * The same as {@link #formattedHexStringToBytes(String, char, int)} with <i>c=':'</i>
	 * @param str the string with hexadecimal characters possibly separated by colon
	 * @param len the length of the array of bytes, or -1 if unknown
	 * @return the array of bytes */
	public static byte[] hexToBytes(String str, int len) {
		return formattedHexStringToBytes(str,':',len);
	}

	/** Converts a hexadecimal string into an array of bytes.
	 * The string may also include colon ':' characters that separate blocks of hexadecimal values (e.g. aa:bb:cc:dd:..:ff). 
	 * <p>
	 * The same as {@link #formattedHexStringToBytes(String, char, byte[], int)} with <i>c=':'</i>
	 * @param str the string with hexadecimal characters possibly separated by colon
	 * @param buf the buffer where the array of bytes will be written
	 * @param off the offset within the buffer
	 * @return the length of the array of bytes */
	public static int hexToBytes(String str, byte[] buf, int off) {
		return formattedHexStringToBytes(str,':',buf,off);
	}


	// Array of bytes to ascii string:

	/** Gets an ASCII representation of an array of bytes.
	  * Non-ASCII bytes are encoded as '.'.
	  * @param data the array of bytes
	  * @return the ASCII string */
	public static String bytesToAsciiString(byte[] data) {
		return bytesToAsciiString(data,0,data.length);
	}  
	
	/** Gets an ASCII representation of an array of bytes.
	  * Non-ASCII bytes are encoded as '.'.
	  * @param buf buffer containing the array of bytes
	  * @param off the offset within the buffer
	  * @param len the number of bytes
	  * @return the ASCII string */
	public static String bytesToAsciiString(byte[] buf, int off, int len) {
		char[] str=new char[len];
		int index=0;
		int end=off+len;
		while (off<end) {
			byte b=buf[off++];
			str[index++]=(b>=32 && b<127)? (char)b : '.';
		}
		return new String(str);
	}  

	/** Gets an ASCII representation of an array of bytes.
	  * Non-ASCII bytes are encoded as '.'.
	 * <p>
	 * The same as {@link #bytesToAsciiString(byte[])}
	  * @param data the array of bytes
	  * @return the ASCII string */
	public static String asAscii(byte[] data) {
		return bytesToAsciiString(data);
	}  
	
	/** Gets an ASCII representation of an array of bytes.
	  * Non-ASCII bytes are encoded as '.'.
	 * <p>
	 * The same as {@link #bytesToAsciiString(byte[], int, int)}
	  * @param buf buffer containing the array of bytes
	  * @param off the offset within the buffer
	  * @param len the number of bytes
	  * @return the ASCII string */
	public static String asAscii(byte[] buf, int off, int len) {
		return bytesToAsciiString(buf,off,len);
	}  


	// Array of bytes to binary string:

	/** Converts a byte array into a binary string.
	 * @param data the byte array
	 * @return the binary string */
	public static String bytesToBinString(byte[] data) {
		return bytesToBinString(data,0,data.length);
	}

	/** Converts a byte array into a binary string.
	 * @param buf buffer containing the byte array
	 * @param off the offset within the buffer
	 * @param len the length of the array
	 * @return the binary string */
	public static String bytesToBinString(byte[] buf, int off, int len) {
		StringBuffer sb=new StringBuffer();
		int end=off+len;
		for (int i=off; i<end; i++) {
			int b=buf[i];
			for (int k=7; k>=0; k--) {
				sb.append((b>>k)&0x01);
				//if (k==4) sb.append(" ");
			}
			//if (i<(end-1)) sb.append(" ");
		}
		return sb.toString();
	}
	
	/** Gets a binary representation of an array of bytes.
	 * <p>
	 * The same as {@link #bytesToBinString(byte[])}
	 * @param data the array of bytes
	 * @return the binary string */
	public static String asBinary(byte[] data) {
		return bytesToBinString(data);
	}  
	
	/** Gets a binary representation of an array of bytes.
	 * <p>
	 * The same as {@link #bytesToBinString(byte[], int, int)}
	 * @param buf buffer containing the array of bytes
	 * @param off the offset within the buffer
	 * @param len the number of bytes
	 * @return the binary string */
	public static String asBinary(byte[] buf, int off, int len) {
		return bytesToBinString(buf,off,len);
	}  

	
	// Array of bytes to integer, and vice versa:

	/** Transforms a two-byte array into a 16-bit integer; the 2-byte array is in big-endian byte order (the big end, most significant byte, is stored first, at the lowest storage address).
	  * @param src the two-byte array representing an integer in big-endian byte order
	  * @return the 16-bit integer */
	public static int twoBytesToInt(byte[] src) {
		return twoBytesToInt(src,0);
	}
	
	/** Reads a 16-bit integer from a two-byte array; the two-byte array is in big-endian byte order (the big end, most significant byte, is stored first, at the lowest storage address).
	  * @param src a buffer containing the two-byte array representing an integer in big-endian byte order
	  * @param off the offset within the buffer
	  * @return the 16-bit integer */
	public static int twoBytesToInt(byte[] src, int off) {
		return ((src[off]&0xff)<<8) | (src[off+1]&0xff);
	}

	/** Transforms a 4-byte array into a 32-bit integer; the 4-byte array is in big-endian byte order (the big end, most significant byte, is stored first, at the lowest storage address).
	  * @param src the 4-byte array representing an integer in big-endian byte order
	  * @return the 32-bit integer */
	public static long fourBytesToInt(byte[] src) {
		return fourBytesToInt(src,0);
	}
	
	/** Transforms a 4-byte array into a 32-bit integer; the 4-byte array is in big-endian byte order (the big end, most significant byte, is stored first, at the lowest storage address).
	  * @param src a buffer containing the 4-byte array representing an integer in big-endian byte order
	  * @param off the offset within the buffer
	  * @return the 32-bit integer */
	public static long fourBytesToInt(byte[] src, int off) {
		return ((((((long)(src[off]&0xff)<<8)+(src[off+1]&0xff))<<8)+(src[off+2]&0xff))<<8)+(src[off+3]&0xff);
	}

	/** Transforms a 8-byte array into a 64-bit integer; the 8-byte array is in big-endian byte order (the big end, most significant byte, is stored first, at the lowest storage address).
	  * @param src the 8-byte array representing an integer in big-endian byte order
	  * @return the 64-bit integer */
	public static long eightBytesToInt(byte[] src) {
		return eightBytesToInt(src,0);
	}
	
	/** Transforms a 8-byte array into a 64-bit integer; the 8-byte array is in big-endian byte order (the big end, most significant byte, is stored first, at the lowest storage address).
	  * @param src a buffer containing the 8-byte array representing an integer in big-endian byte order
	  * @param off the offset within the buffer
	  * @return the 64-bit integer */
	public static long eightBytesToInt(byte[] src, int off) {
		return ((((((((((long)(src[off]&0xff)<<8)+(src[off+1]&0xff))<<8)+(src[off+2]&0xff))<<8)+(src[off+3]&0xff)<<8)+(src[off+4]&0xff)<<8)+(src[off+5]&0xff)<<8)+(src[off+6]&0xff)<<8)+(src[off+7]&0xff);
	}

	/** Transforms a n-byte array into an integer; the n-byte array is in big-endian byte order (the big end, most significant byte, is stored first, at the lowest storage address).
	  * @param src the n-byte array representing an integer in big-endian byte order
	  * @return the integer */
	public static long nBytesToInt(byte[] src) {
		return nBytesToInt(src,0,src.length);
	}
	
	/** Transforms a n-byte array into an integer; the n-byte array is in big-endian byte order (the big end, most significant byte, is stored first, at the lowest storage address).
	  * @param src a buffer containing the n-byte array representing an integer in big-endian byte order
	  * @param off the offset within the buffer
	  * @param len the number of bytes
	  * @return the integer */
	public static long nBytesToInt(byte[] src, int off, int len) {
		long val=0;
		for (int i=0; i<len; i++) {
			val=(val<<8) | (src[off+i]&0xff);
		}
		return val;
	}

	/** Transforms a 16-bit integer into a two-byte array in big-endian byte order (the big end, most significant byte, is stored first, at the lowest storage address).
	  * @param val the 16-bit integer
	  * @return a two-byte array representing the given integer, in big-endian byte order */
	public static byte[] intToTwoBytes(int val) {
		byte[] b=new byte[2];
		intToTwoBytes(val,b,0);
		return b;
	}

	/** Writes a 16-bit integer into a two-byte array in big-endian byte order (the big end, most significant byte, is stored first, at the lowest storage address).
	  * @param val the 16-bit integer
	  * @param dst a buffer for the two-byte big-endian representation of the given integer
	  * @param off the offset within the buffer */
	public static void intToTwoBytes(int val, byte[] dst, int off) {
		dst[off]=(byte)(val>>8);
		dst[off+1]=(byte)(val&0xFF);
	}

	/** Transforms a 32-bit integer into a four-byte array in big-endian byte order (the big end, most significant byte, is stored first, at the lowest storage address).
	  * @param val the 32-bit integer
	  * @return a four-byte array representing the given integer, in big-endian byte order */
	public static byte[] intToFourBytes(long val) {
		byte[] dst=new byte[4];
		intToFourBytes(val,dst,0);
		return dst;
	}

	/** Transforms a 32-bit integer into a four-byte array in big-endian byte order (the big end, most significant byte, is stored first, at the lowest storage address).
	  * @param val the 32-bit integer
	  * @param dst a buffer for the four-byte big-endian representation of the given integer
	  * @param off the offset within the buffer */
	public static void intToFourBytes(long val, byte[] dst, int off) {
		dst[off]=(byte)(val>>24);
		dst[off+1]=(byte)((val>>16)&0xFF);
		dst[off+2]=(byte)((val>>8)&0xFF);
		dst[off+3]=(byte)(val&0xFF);
	}

	/** Transforms a 48-bit integer into a six-byte array in big-endian byte order (the big end, most significant byte, is stored first, at the lowest storage address).
	  * @param val the 48-bit integer
	  * @return a six-byte array representing the given integer, in big-endian byte order */
	public static byte[] intToSixBytes(long val) {
		byte[] dst=new byte[6];
		intToSixBytes(val,dst,0);
		return dst;
	}

	/** Transforms a 48-bit integer into a six-byte array in big-endian byte order (the big end, most significant byte, is stored first, at the lowest storage address).
	  * @param val the 48-bit integer
	  * @param dst a buffer for the six-byte big-endian representation of the given integer
	  * @param off the offset within the buffer */
	public static void intToSixBytes(long val, byte[] dst, int off) {
		dst[off]=(byte)(val>>40);
		dst[off+1]=(byte)((val>>32)&0xFF);
		dst[off+2]=(byte)((val>>24)&0xFF);
		dst[off+3]=(byte)((val>>16)&0xFF);
		dst[off+4]=(byte)((val>>8)&0xFF);
		dst[off+5]=(byte)(val&0xFF);
	}

	/** Transforms a 64-bit signed integer (as long) into an eight-byte array in big-endian byte order (the big end, most significant byte, is stored first, at the lowest storage address).
	  * @param val the 64-bit integer
	  * @return an eight-byte array representing the given integer, in big-endian byte order */
	public static byte[] intToEightBytes(long val) {
		byte[] dst=new byte[8];
		intToEightBytes(val,dst,0);
		return dst;
	}

	/** Transforms a 64-bit signed integer (as long) into an eight-byte array in big-endian byte order (the big end, most significant byte, is stored first, at the lowest storage address).
	  * @param val the 64-bit integer
	  * @param dst a buffer for the eight-byte big-endian representation of the given integer
	  * @param off the offset within the buffer */
	public static void intToEightBytes(long val, byte[] dst, int off) {
		dst[off]=(byte)((val>>56)&0xFF);
		dst[off+1]=(byte)((val>>48)&0xFF);
		dst[off+2]=(byte)((val>>40)&0xFF);
		dst[off+3]=(byte)((val>>32)&0xFF);
		dst[off+4]=(byte)((val>>24)&0xFF);
		dst[off+5]=(byte)((val>>16)&0xFF);
		dst[off+6]=(byte)((val>>8)&0xFF);
		dst[off+7]=(byte)(val&0xFF);
	}

	/** Transforms an integer into an n-byte array in big-endian byte order (the big end, most significant byte, is stored first, at the lowest storage address).
	  * @param val the integer
	  * @param len the number of bytes
	  * @return an n-byte array representing the given integer, in big-endian byte order */
	public static byte[] intToNBytes(long val, int len) {
		byte[] dst=new byte[len];
		intToNBytes(val,dst,0,len);
		return dst;
	}

	/** Transforms an integer into an n-byte array in big-endian byte order (the big end, most significant byte, is stored first, at the lowest storage address).
	  * @param val the integer
	  * @param dst a buffer for the n-byte big-endian representation of the given integer
	  * @param off the offset within the buffer
	  * @param len the number of bytes */
	public static void intToNBytes(long val, byte[] dst, int off, int len) {
		for (int i=0; i<len; i++) {
			dst[off+i]=(byte)((val>>((len-1-i)*8))&0xFF);
		}
	}

	/** Transforms a four-byte array into a 32-bit integer; the four-byte array is in little-endian byte order (the little end, least significant byte, is stored first, at the lowest storage address).
	  * @param src the four-byte array representing an integer in little-endian byte order
	  * @return the 32-bit integer */
	public static long fourBytesToIntLittleEndian(byte[] src) {
		return fourBytesToIntLittleEndian(src,0);
	}
	
	/** Transforms a four-byte array into a 32-bit integer; the four-byte array is in little-endian byte order (the little end, least significant byte, is stored first, at the lowest storage address).
	  * @param src a buffer containing the four-byte array representing an integer in little-endian byte order
	  * @param off the offset within the buffer
	  * @return the 32-bit integer */
	public static long fourBytesToIntLittleEndian(byte[] src, int off) {
		return ((((((long)(src[off+3]&0xff)<<8)+(src[off+2]&0xff))<<8)+(src[off+1]&0xff))<<8)+(src[off]&0xff);
	}

	/** Transforms a 32-bit integer into a four-byte array in little-endian byte order (the little end, least significant byte, is stored first, at the lowest storage address).
	  * @param n the 32-bit integer
	  * @return a four-byte array representing the given integer, in little-endian byte order */
	public static byte[] intToFourBytesLittleEndian(long n) {
		byte[] dst=new byte[4];
		intToFourBytesLittleEndian(n,dst,0);
		return dst;
	}

	/** Transforms a 32-bit integer into a four-byte array copied into a given buffer; the 4-bytes array is in little-endian byte order (the little end, least significant byte, is stored first, at the lowest storage address).
	  * @param n the 32-bit integer
	  * @param dst a buffer for the four-byte little-endian representation of the given integer
	  * @param off the offset within the buffer */
	public static void intToFourBytesLittleEndian(long n, byte[] dst, int off) {
		dst[off+3]=(byte)(n>>24);
		dst[off+2]=(byte)((n>>16)&0xFF);
		dst[off+1]=(byte)((n>>8)&0xFF);
		dst[off]=(byte)(n&0xFF);
	}

	/** Modifies the given array inverting the byte order.
	  * It transforms little-endian to big-endian n-bytes array (and vice versa).
	  * @param buf the buffer containing the array */
	public static void invertByteOrder(byte[] buf) {
		//byte[] b1=new byte[buf.length];
		//for (int i=0,j=buf.length-1; i<b1.length; i++,j--) b1[i]=buf[j];
		//return b1;
		for (int i=0,j=buf.length-1; i<buf.length/2; i++,j--) {
			byte b_i=buf[i];
			buf[i]=buf[j];
			buf[j]=b_i;
		}
	}

	/** Adds 1 to the number represented by the given array of bytes, module 2^n, where n is the length (in bits) of the array.
	  * @param buf the buffer containing the array */
	public static void inc(byte[] buf) {
		for (int i=buf.length-1; i>=0; i--) {
			if ((buf[i]=(byte)(((buf[i]&0xFF)+1)&0xFF))!=0) break;
		}
	}

	/** Gets a 16-bit integer from two bytes.
	  * @param b_high high byte
	  * @param b_low low byte
	  * @return the 16-bit integer value */
	public static int twoBytesToInt(byte b_high, byte b_low) {
		return ((((int)b_high)&0xFF)<<8) | (((int)b_low)&0xFF);
	}

	/** Gets a 32-bit integer (as long) from four bytes.
	  * @param b3 the fourth byte
	  * @param b2 the third byte
	  * @param b1 the second byte
	  * @param b0 the first byte
	  * @return the 32-bit integer value */
	public static long fourBytesToLong(byte b3, byte b2, byte b1, byte b0) {
		return ((((long)b3)&0xFF)<<24) | ((((long)b2)&0xFF)<<16) | ((((long)b1)&0xFF)<<8) | (((long)b0)&0xFF);
	}

	/** Gets the first byte of an integer (the lowest byte).
	  * @param val the integer value
	  * @return the value of the first byte */
	/*public static byte byte0(long val) {
		return (byte)(val&0xFF);
	}*/

	/** Gets the second byte of an integer (the highest byte in case of a 16-bit integer).
	  * @param val the integer value
	  * @return the value of the second byte */
	/*public static byte byte1(long val) {
		return (byte)((val&0xFF00)>>8);
	}*/

	/** Gets the third byte of an integer.
	  * @param val the integer value
	  * @return the value of the third byte */
	/*public static byte byte2(long val) {
		return (byte)((val&0xFF0000)>>16);
	}*/

	/** Gets the fourth byte of an integer (the highest byte in case of a 32-bit integer).
	  * @param val the integer value
	  * @return the value of the fourth byte */
	/*public static byte byte3(long val) {
		return (byte)((val&0xFF000000)>>24);
	}*/

}
