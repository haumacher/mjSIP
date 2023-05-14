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

package org.zoolu.tools;


import java.io.*;
import org.zoolu.tools.Random;


/** OTP (One Time Pad) encryption algorithm based on MD5 hash function.
  * It uses a PRG (Pseudo-Random-Generator) function in OFB (Output Feadback) 
  * to genarate a byte-stream (the OTP) that is XORed with the plaintext. <br/>
  * The PRG is based on MD5.
  * <p/>
  * The OTP is calculated starting from a key and an IV, as follows: <br/>
  * h_0=hash(skey|iv) <br/>
  * h_i=hash(skey|h_i-1)  
  * <p/>
  * where: <br/>
  * hash(.)==MD5(.) <br/>
  * skey==key
  * <p/>
  * while the ciphertext is calculated as follows: <br/>
  * c_0=iv <br/>
  * c_i=m_i XOR h_i   with i=1,2,..
  * <p/>
  * Note that optionally it could be modified initializing the skey as follows: <br/>
  * skey==hash(key|iv) <br/>
  * in order to do not keep in memory the secret key for long time.
  */
public class MD5OTP
{
   /** Block size in bytes */
   static int size;
   /** the OTP stream key */
   byte[] skey;
   /** pseudorandom-stream (OTP) block */
   byte[] h;
   /** index within a single block */
   int index;

   
   // *********************** Public methods ***********************

   /** Creates a new MD5OTP */
   /*public MD5OTP(int bsize, byte[] key, byte[] iv)
   {  init(bsize,key,iv);
   }*/


   /** Creates a new MD5OTP */
   public MD5OTP(byte[] skey, byte[] iv)
   {  init(16,skey,iv);
   }


   /** Creates a new MD5OTP with IV=0 */
   public MD5OTP(byte[] skey)
   {  init(16,skey,null);
   }


   /** Inits the MD5OTP algorithm */
   private void init(int size, byte[] skey, byte[] iv)
   {  this.size=size;
      if (iv==null) { iv=new byte[size]; for (int i=0; i<size; i++) iv[i]=0; }
      this.skey=skey;
      //skey=hash(cat(key,iv));
      h=clone(iv);
      index=size-1;
   }


   /** Encodes a block of bytes */
   public int update(byte[] in_buff, int in_offset, int in_len, byte[] out_buff, int out_offset)
   {  int in_end=in_offset+in_len;
      int j=out_offset;
      for (int i=in_offset; i<in_end; i++)
      {  if ((++index)==size)
         {  // calculate new h_block
            h=hash(cat(skey,h));
            index=0;
         }
         out_buff[j++]=(byte)(in_buff[i]^h[index]);
      }
      return in_len;
   }


   /** Encodes a block of bytes */
   public byte[] update(byte[] in_buff)
   {  byte[] out_buff=new byte[in_buff.length];
      update(in_buff,0,in_buff.length,out_buff,0);
      return out_buff;
   }


   /** Encodes a byte stream */
   public void update(InputStream in, OutputStream out)
   {  byte[] in_buff=new byte[2048];
      byte[] out_buff=new byte[2048];
      int len;
      try
      {  while ((len=in.read(in_buff))>0)
         {  update(in_buff,0,len,out_buff,0);
            out.write(out_buff,0,len);
         }
      }
      catch (IOException e) { e.printStackTrace(); }
   }


   /** Encrypts an array of bytes. An IV is chosen and written at the top. */
   public static int encrypt(byte[] in_buff, int in_offset, int in_len, byte[] out_buff, int out_offset, byte[] key)
   {  // choose a random IV
      byte[] iv=Random.nextBytes(16);
      copy(iv,0,iv.length,out_buff,out_offset);
      out_offset+=iv.length;
      // do encryption
      (new MD5OTP(key,iv)).update(in_buff,in_offset,in_len,out_buff,out_offset);
      return iv.length+in_len;
   }


   /** Encrypts an array of bytes. An IV is chosen and written at the top. */
   public static byte[] encrypt(byte[] m, byte[] key)
   {  // choose a random IV
      byte[] iv=Random.nextBytes(16);
      // do encryption    
      byte[] c=(new MD5OTP(key,iv)).update(m);
      return cat(iv,c);
   }


   /** Decrypts an array of bytes with the IV at top. */
   public static int decrypt(byte[] in_buff, int in_offset, int in_len, byte[] out_buff, int out_offset, byte[] key)
   {  // read the IV
      byte[] iv=sub(in_buff,in_offset,16);
      in_offset+=16;
      in_len-=16;
      // do encryption
      (new MD5OTP(key,iv)).update(in_buff,in_offset,in_len,out_buff,out_offset);
      return in_len;
   }


   /** Decrypts an array of bytes with the IV at top. */
   public static byte[] decrypt(byte[] c, byte[] key)
   {  // read the IV
      byte[] iv=sub(c,0,16);
      byte[] buf=sub(c,16,c.length-16); 
      return (new MD5OTP(key,iv)).update(buf);
   } 


   // *********************** Private methods **********************

   /** Copies a byte array */
   private static int copy(byte[] in_buff, int in_offset, int in_len, byte[] out_buff, int out_offset)
   {  int in_end=in_offset+in_len;
      int j=out_offset;
      for (int i=in_offset; i<in_end; i++) out_buff[j++]=in_buff[i];
      return in_len;
   }


   /** Makes a clone of a byte array */
   private static byte[] clone(byte[] bb)
   {  byte[] cc=new byte[bb.length];
      for (int i=0; i<bb.length; i++) cc[i]=bb[i];
      return cc;
   }    


   /** Concatenates two byte arrays */
   private static byte[] cat(byte[] aa, byte[] bb)
   {  byte[] cc=new byte[aa.length+bb.length];
      for (int i=0; i<aa.length; i++) cc[i]=aa[i];
      for (int i=0; i<bb.length; i++) cc[i+aa.length]=bb[i];
      return cc;
   }   


   /** Returns a sub array */
   private static byte[] sub(byte[] bb, int offset, int len)
   {  byte[] cc=new byte[len];
      int end=offset+len;
      int j=0;
      for (int i=offset; i<end; i++) cc[j++]=bb[i];
      return cc;
   }   


   /** Return an hash of the byte array */
   private static byte[] hash(byte[] bb)
   {  return MD5.digest(bb);
   }


   /** Returns an hex representation of the byte array */
   private static String asHex(byte[] bb)
   {  StringBuffer buf=new StringBuffer(bb.length*2);
      for (int i=0; i<bb.length; i++)
      {  if (((int)bb[i] & 0xff) < 0x10) buf.append("0");
         //buf.append(Long.toString((int)bb[i] & 0xff, 16));
         buf.append(Integer.toHexString((int)bb[i] & 0xff));
      }
      return buf.toString();
   }


   // **************************** Main ****************************

   /** Main method. */
   public static void main(String[] args)
   {  
      if (args.length<2)
      {  System.out.println("Usage:\n\n   java MD5OTP <message> <pass_phrase> [<iv>]");
         System.exit(0);
      } 
   
      byte[] msg=args[0].getBytes();     
      byte[] key=args[1].getBytes();
      byte[] iv=null;
      if (args.length>2) iv=args[2].getBytes();
      
      System.out.println("m= "+asHex(msg)+" ("+new String(msg)+")");
      byte[] cip=(new MD5OTP(key,iv)).update(msg);
      System.out.println("c= "+asHex(cip));
      cip=(new MD5OTP(key,iv)).update(cip);
      System.out.println("m= "+asHex(cip)+" ("+new String(cip)+")");
      
      System.out.println("");
      //System.out.println("m= "+asHex(msg)+" ("+new String(msg)+")");
      cip=MD5OTP.encrypt(msg,key);
      System.out.println("c= "+asHex(cip));
      cip=MD5OTP.decrypt(cip,key);
      System.out.println("m= "+asHex(cip)+" ("+new String(cip)+")");

      System.out.println("");
      //System.out.println("m= "+asHex(msg)+" ("+new String(msg)+")");
      cip=new byte[16+msg.length];
      MD5OTP.encrypt(msg,0,msg.length,cip,0,key);
      System.out.println("c= "+asHex(cip));
      MD5OTP.decrypt(cip,0,cip.length,msg,0,key);
      System.out.println("m= "+asHex(msg)+" ("+new String(msg)+")");
   } 
   
}
