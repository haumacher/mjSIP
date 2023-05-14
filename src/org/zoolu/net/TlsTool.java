/*
 * Copyright (C) 2007 Luca Veltri - University of Parma - Italy
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

package org.zoolu.net;



import java.io.*;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyStore;
//import java.security.cert.Certificate;
import java.security.spec.PKCS8EncodedKeySpec;
//import sun.misc.BASE64Encoder;
//import sun.misc.BASE64Decoder;
import org.zoolu.tools.Base64;



/** Key management tool.
  */
public class TlsTool
{

   /** Begin private key */
   static final String BEGIN_PRIVATE_KEY="-----BEGIN PRIVATE KEY-----";

   /** End private key */
   static final String END_PRIVATE_KEY="-----END PRIVATE KEY-----";



   /** Imports a private key. */
   public static Key importPrivateKey(String file_name) throws Exception
   {  InputStream is=new FileInputStream(file_name);
      Key key=importPrivateKey(is);
      is.close();
      return key;
   }


   /** Imports a private key. */
   public static Key importPrivateKey(InputStream is) throws Exception
   {  byte[] buff=new byte[is.available()];
      is.read(buff);
      PKCS8EncodedKeySpec pkcs8_eks=new PKCS8EncodedKeySpec(buff);
      KeyFactory kf=KeyFactory.getInstance("RSA");
      return kf.generatePrivate(pkcs8_eks);
   }


   /** Imports a private key from base64 format. */
   public static Key importPrivateKeyBASE64(String file_name) throws Exception
   {  BufferedReader rr=new BufferedReader(new FileReader(file_name));
      Key key=importPrivateKeyBASE64(rr);
      rr.close();
      return key;
   }

   /** Imports a private key from base64 format. */
   public static Key importPrivateKeyBASE64(BufferedReader rr) throws Exception
   {  String buff="";
      for (String line=rr.readLine(); line!=null; line=rr.readLine())
      {  buff+=line;
      }
      int begin=buff.indexOf(BEGIN_PRIVATE_KEY);
      if (begin>=0) begin+=BEGIN_PRIVATE_KEY.length(); //else begin=0;
      int end=buff.indexOf(END_PRIVATE_KEY);
      //if (end<0) end=buff.length();
      String encoded=buff.substring(begin,end);
      //BASE64Decoder decoder=new BASE64Decoder();
      //PKCS8EncodedKeySpec pkcs8_eks=new PKCS8EncodedKeySpec(decoder.decodeBuffer(encoded));
      PKCS8EncodedKeySpec pkcs8_eks=new PKCS8EncodedKeySpec(Base64.decode(encoded));
      KeyFactory kf=KeyFactory.getInstance("RSA");
      return kf.generatePrivate(pkcs8_eks);
   }


   /** Exports a private key. */
   public static void exportPrivateKey(Key key, String file_name) throws IOException
   {  OutputStream os=new FileOutputStream(file_name);
      exportPrivateKey(key,os);
      os.close();
   }


   /** Exports a private key. */
   public static void exportPrivateKey(Key key, OutputStream os) throws IOException
   {  os.write(key.getEncoded());
   }


   /** Exports a private key in base64 format. */
   public static void exportPrivateKeyBASE64(Key key, String file_name) throws Exception
   {  FileWriter file=new FileWriter(file_name);
      exportPrivateKeyBASE64(key,file);
      file.close();
   }


   /** Exports a private key in base64 format. */
   public static void exportPrivateKeyBASE64(Key key, Writer wr) throws Exception
   {  //BASE64Encoder encoder=new BASE64Encoder();
      //String encoded=encoder.encode(key.getEncoded());
      String encoded=Base64.encode(key.getEncoded());
      wr.write(BEGIN_PRIVATE_KEY+"\r\n");
      wr.write(encoded);
      wr.write("\r\n"+END_PRIVATE_KEY);
   }


   /** Main method. */
   public static void main(String[] args)
   {  String store_file=null;
      char[] passwd=null;
      String alias=null;
      String key_file="key.pem";
      for (int i=0; i<args.length; i++)
      {  if (args[i].equals("-keystore"))
         {  store_file=args[++i];
         }
         else
         if (args[i].equals("-storepass"))
         {  passwd=args[++i].toCharArray();
         }
         else
         if (args[i].equals("-alias"))
         {  alias=args[++i];
         }
         else
         if (args[i].equals("-file"))
         {  key_file=args[++i];
         }
      }
      if (store_file==null || passwd==null || alias==null)
      {  System.out.println("usage:\n\n   java TlsTool -keystore <store_file> -storepass <passwd> -alias <alias> [-file <key_file>]");
         System.exit(0);
      }
      // else
      try
      {  KeyStore ks=KeyStore.getInstance(KeyStore.getDefaultType());
         ks.load(new FileInputStream(store_file),passwd); // here passwd is used only for integrity check
         //System.out.print("TlsTool: current cert aliases:");
         //for (java.util.Enumeration e=ks.aliases(); e.hasMoreElements(); ) System.out.print(" "+(String)e.nextElement());
         //System.out.println(" ");
         Key key=ks.getKey(alias,passwd);
         exportPrivateKeyBASE64(key,key_file);
         System.out.println("Private key stored in file <"+key_file+">");
      }
      catch (Exception e)
      {  e.printStackTrace();
      }
   }

}
