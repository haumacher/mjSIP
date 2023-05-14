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

package org.zoolu.tools;



/** Class that collects static methods for dealing with binary materials.
  */
public class BinTools
{  
   /** Transforms an array of bytes into a hex string. */
   public static String asHex(byte[] buf)
   {  return asHex(buf,0,buf.length);
   }  
   
   /** Transforms the first <i>len</i> bytes of an array into a hex string. */
   public static String asHex(byte[] buf, int len)
   {  return asHex(buf,0,len);
   }  
   
   /** Transforms <i>len</i> bytes (from <i>offset</i>) of an array into a hex string. */
   public static String asHex(byte[] buf, int offset, int len)
   {  StringBuffer sb=new StringBuffer();
      int end=offset+len;
      for (int i=offset; i<end; i++)
      {  sb.append(Integer.toHexString((buf[i]>>>4)&0x0F));
         sb.append(Integer.toHexString(buf[i]&0x0F));
      }
      return sb.toString();
   }  
   
   /** Compares two arrays of bytes. */
   public static boolean compare(byte[] a, byte[] b)
   {  //if (a.length!=b.length) return false;
      //for (int i=0; i<a.length; i++) if (a[i]!=b[i]) return false;
      //return true;
      return compare(a,0,a.length,b,0,b.length);
   }
   
   /** Compares two arrays of bytes. */
   public static boolean compare(byte[] a, int a_off, int a_len, byte[] b, int b_off, int b_len)
   {  if (a_len!=b_len) return false;
      for (int a_end=a_off+a_len; a_off<a_end; ) if (a[a_off++]!=b[b_off++]) return false;
      return true;
   }
   
   /** Initalizes a byte array with value <i>value</i>. */
   public static byte[] initBytes(byte[] b, int value)
   {  for (int i=0; i<b.length; i++) b[i]=(byte)value;
      return b;
   }

   /** Gets the unsigned representatin of a byte, returned as a <b>short</b>. The same as getting <i>(int)b&0xFF</i>. */
   public static short uByte(byte b)
   {  //return (short)(((short)b+256)&0xFF);
      return (short)((int)b&0xFF);
   } 

   /** 0x00000000FFFFFFFF */
   public static final long OxFFFFFFFF=((long)1<<32)-1;

   /** Gets the unsigned representatin of a 32-bit word , returned as a <b>long</b>. The same as getting <i>(long)n&0x00000000FFFFFFFF</i>. */
   public static long uWord(int n)
   {  //long Ox1OOOOOOOO=(long)1<<32;
      //return (long)(((long)n+Ox1OOOOOOOO)&(Ox1OOOOOOOO-1));
      return (long)n&OxFFFFFFFF;
   } 

   /** Rotates w left n bits. */
   private static int rotateLeft(int w, int n)
   {  return (w << n) | (w >>> (32-n));
   }

   /** Rotates w right n bits. */
   private static int rotateRight(int w, int n)
   {  return (w >>> n) | (w << (32-n));
   }

   /** Rotates an array of int (words), shifting 1 word left. */
   private static int[] rotateLeft(int[] w)
   {  int len=w.length;
      int w1=w[len-1];
      for (int i=len-1; i>1; i--) w[i]=w[i-1];
      w[0]=w1;
      return w;
   }

   /** Rotates an array of int (words), shifting 1 word right. */
   private static int[] rotateRight(int[] w)
   {  int len=w.length;
      int w0=w[0];
      for (int i=1; i<len; i++) w[i-1]=w[i];
      w[len-1]=w0;
      return w;
   }

   /** Rotates an array of bytes, shifting 1 byte left. */
   private static byte[] rotateLeft(byte[] b)
   {  int len=b.length;
      byte b1=b[len-1];
      for (int i=len-1; i>1; i--) b[i]=b[i-1];
      b[0]=b1;
      return b;
   }

   /** Rotates an array of bytes, shifting 1 byte right. */
   private static byte[] rotateRight(byte[] b)
   {  int len=b.length;
      byte b0=b[0];
      for (int i=1; i<len; i++) b[i-1]=b[i];
      b[len-1]=b0;
      return b;
   }

   /** Returns a copy of an array of bytes <i>b</i>. */
   public static byte[] clone(byte[] b)
   {  return getBytes(b,0,b.length);
   }

   /** Returns a <i>len</i>-byte array from array <i>b</i> with offset <i>offset</i>. */
   public static byte[] getBytes(byte[] b, int offset, int len)
   {  byte[] bb=new byte[len];
      for (int k=0; k<len; k++) bb[k]=b[offset+k];
      return bb;
   }
   
   /** Returns a 2-byte array from array <i>b</i> with offset <i>offset</i>. */
   public static byte[] get2Bytes(byte[] b, int offset)
   {  return getBytes(b,offset,2);
   }
   
   /** Returns a 4-byte array from array <i>b</i> with offset <i>offset</i>. */
   public static byte[] get4Bytes(byte[] b, int offset)
   {  return getBytes(b,offset,4);
   }
   
   /** Copies all bytes of array <i>src</i> into array <i>dst</i> with offset <i>offset</i>. */
   public static int copyBytes(byte[] src, byte[] dst, int offset)
   {  for (int k=0; k<src.length; k++) dst[offset+k]=src[k];
      return src.length;
   }

   /** Copies the first <i>len</i> bytes of array <i>src</i> into array <i>dst</i> with offset <i>offset</i>. */
   public static int copyBytes(byte[] src, byte[] dst, int offset, int len)
   {  for (int k=0; k<len; k++) dst[offset+k]=src[k];
      return len;
   }
   
   /** Copies the first <i>len</i> bytes starting from <i>src_off</i> of array <i>src</i> into array <i>dst</i> with offset <i>dst_off</i>. */
   public static int copyBytes(byte[] src, int src_off, byte[] dst, int dst_off, int len)
   {  for (int k=0; k<len; k++) dst[dst_off+k]=src[src_off+k];
      return len;
   }
   
   /** Copies the first 2 bytes of array <i>src</i> into array <i>dst</i> with offset <i>offset</i>. */
   public static void copy2Bytes(byte[] src, byte[] dst, int offset)
   {  copyBytes(src,dst,offset,2);
   }
   
   /** Copies a the first 4 bytes of array <i>src</i> into array <i>dst</i> with offset <i>index</i>. */
   public static void copy4Bytes(byte[] src, byte[] dst, int offset)
   {  copyBytes(src,dst,offset,4);
   }


   /** Transforms a byte array into a string of hex values.
     * The same as asHex(byte[]). */
   public static String bytesToHexString(byte[] b)
   {  return asHex(b,b.length);
   }

   /** Transforms the first <i>len</i> bytes of an array into a string of hex values.
     * The same as asHex(byte[],int). */
   public static String bytesToHexString(byte[] b, int len)
   {  return asHex(b,len);
   }

   /** Transforms <i>len</i> bytes of an array into a string of hex values.
     * The same as asHex(byte[],int,int). */
   public static String bytesToHexString(byte[] b, int off, int len)
   {  return asHex(b,off,len);
   }

   /** Transforms a string of hex values into an array of bytes.
     * The string may include ':' chars. */
   public static byte[] hexStringToBytes(String str)
   {  return hexStringToBytes(str,-1); 
   }

   /** Transforms a string of hex values into an array of bytes of max length <i>len</i>.
     * The string may include ':' chars between hex values (e.g. aa:bb:cc:dd:..). 
     * If <i>len</i> is set to -1, all string is converted. */
   public static byte[] hexStringToBytes(String str, int len)
   {  // if the string is of the form xx:yy:zz:ww.., remove all ':' first
      if (str.indexOf(":")>=0)
      {  String aux="";
         char c;
         for (int i=0; i<str.length(); i++) if ((c=str.charAt(i))!=':') aux+=c;
         str=aux;
      }
      // if len=-1, set the len value
      if (len<0) len=str.length()/2; 
      byte[] b=new byte[len];
      for (int i=0; i<len; i++)
      {  if (i<str.length()/2) b[i]=(byte)Integer.parseInt(str.substring(i*2,i*2+2),16);
         else b[i]=0;
      }
      return b;
   }
   
   /** Transforms a four-bytes array into a dotted four-decimals string. */
   public static String bytesToIpv4addr(byte[] b)
   {  return bytesToIpv4addr(b,0);
   }
   
   /** Transforms a four-bytes array into a dotted four-decimals string. */
   public static String bytesToIpv4addr(byte[] b, int off)
   {  return Integer.toString(uByte(b[off++]))+"."+Integer.toString(uByte(b[off++]))+"."+Integer.toString(uByte(b[off++]))+"."+Integer.toString(uByte(b[off]));
   }

   /** Transforms a dotted four-decimals string (ipv4 address) into a four-bytes array. */
   public static byte[] ipv4addrToBytes(String addr)
   {  byte[] b=new byte[4];
      /*int begin=0;
      int end;
      for (int i=0; i<3; i++)
      {  end=addr.indexOf('.',begin);
         b[i]=(byte)Integer.parseInt(addr.substring(begin,end));
         begin=end+1;
      }
      b[3]=(byte)Integer.parseInt(addr.substring(begin));*/
      ipv4addrToBytes(addr,b,0);
      return b;
   } 
   
   /** Transforms a dotted four-decimals string (ipv4 address) into a four-bytes array. */
   public static void ipv4addrToBytes(String addr, byte[] buf, int off)
   {  int begin=0;
      int end;
      for (int i=0; i<3; i++)
      {  end=addr.indexOf('.',begin);
         buf[off+i]=(byte)Integer.parseInt(addr.substring(begin,end));
         begin=end+1;
      }
      buf[off+3]=(byte)Integer.parseInt(addr.substring(begin));
   } 
   
   /** Transforms a 4-bytes array into a 32-bit int */
   public static long fourBytesToInt(byte[] b)
   {  return fourBytesToInt(b,0);
   }
   
   /** Transforms a 4-bytes array into a 32-bit int */
   public static long fourBytesToInt(byte[] buf, int off)
   {  return ((((((long)uByte(buf[off])<<8)+uByte(buf[off+1]))<<8)+uByte(buf[off+2]))<<8)+uByte(buf[off+3]);
   }
   
   /** Transforms a 2-bytes array into a 16-bit int */
   public static int twoBytesToInt(byte[] b)
   {  return twoBytesToInt(b,0);
   }
   
   /** Transforms a 2-bytes array into a 16-bit int */
   public static int twoBytesToInt(byte[] buf, int off)
   {  return (((int)uByte(buf[off])<<8)+uByte(buf[off+1]));
   }
   
   /** Transforms a 32-bit int into a 4-bytes array */
   public static byte[] intTo4Bytes(long n)
   {  byte[] b=new byte[4];
      intTo4Bytes(n,b,0);
      return b;
   }

   /** Transforms a 32-bit int to 4-bytes copied into a given byte array */
   public static void intTo4Bytes(long n, byte[] buf, int off)
   {  buf[off]=(byte)(n>>24);
      buf[off+1]=(byte)((n>>16)&0xFF);
      buf[off+2]=(byte)((n>>8)&0xFF);
      buf[off+3]=(byte)(n&0xFF);
   }

   /** Transforms a 16-bit int into a 2-bytes array */
   public static byte[] intTo2Bytes(int n)
   {  byte[] b=new byte[2];
      intTo2Bytes(n,b,0);
      return b;
   }

   /** Transforms a 16-bit int to 2-bytes copied into a given byte array */
   public static void intTo2Bytes(int n, byte[] buf, int off)
   {  buf[off]=(byte)(n>>8);
      buf[off+1]=(byte)(n&0xFF);
   }

   /** Transforms a 4-bytes array into a 32-bit word (with the more significative byte at left). */
   public static long bytesToWord(byte[] b, int offset)
   {  return ((((((long)uByte(b[offset+3])<<8)+uByte(b[offset+2]))<<8)+uByte(b[offset+1]))<<8)+uByte(b[offset+0]);
   }

   /** Transforms a 4-bytes array into a 32-bit word (with the more significative byte at left). */
   public static long bytesToWord(byte[] b)
   {  return ((((((long)uByte(b[3])<<8)+uByte(b[2]))<<8)+uByte(b[1]))<<8)+uByte(b[0]);
   }
   
   /** Transforms a 32-bit word (with the more significative byte at left) into a 4-bytes array. */
   public static byte[] wordToBytes(long n)
   {  byte[] b=new byte[4];
      b[3]=(byte)(n>>24);
      b[2]=(byte)((n>>16)&0xFF);
      b[1]=(byte)((n>>8)&0xFF);
      b[0]=(byte)(n&0xFF);
      return b;
   }


   /** Gets an int from two bytes. */
   public static int bytesToInt(byte b_high, byte b_low)
   {  return ((((int)b_high)&0xFF)<<8) | (((int)b_low)&0xFF);
   }

   /** Gets the low byte of a int. */
   public static byte getLowByte(int i)
   {  return (byte)(i&0xFF);
   }

   /** Gets the high byte of a int. */
   public static byte getHighByte(int i)
   {  return (byte)((i&0xFF00)>>8);
   }
 
   /** Gets a long from four bytes. */
   public static long bytesToLong(byte b3, byte b2, byte b1, byte b0)
   {  return ((((long)b3)&0xFF)<<24) | ((((long)b2)&0xFF)<<16) | ((((long)b1)&0xFF)<<8) | (((long)b0)&0xFF);
   }

   /** Gets the byte #0 of an int (i.e. the lower byte). */
   public static byte getByte0(long i)
   {  return (byte)(i&0xFF);
   }

   /** Gets the byte #1 of an int. */
   public static byte getByte1(long i)
   {  return (byte)((i&0xFF00)>>8);
   }

   /** Gets the byte #2 of an int. */
   public static byte getByte2(long i)
   {  return (byte)((i&0xFF0000)>>16);
   }

   /** Gets the byte #3 of an int (i.e. the higher byte). */
   public static byte getByte3(long i)
   {  return (byte)((i&0xFF000000)>>24);
   }

}
