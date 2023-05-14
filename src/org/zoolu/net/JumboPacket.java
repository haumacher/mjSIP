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

package org.zoolu.net;


import org.zoolu.tools.BinTools;
import java.util.Vector;


/** Class JumboPacket provides methods for managing large datagrams
  * providing fragmentation operation over a generic
  * datagram-oriented and connection-less communication protocol.
  * <p/>
  * JumboPacket could be used for example to add fragmentation operation
  * to the UDP transfer protocol.
  * <p/>
  * Each fragment has a header and payload; the header is composed by
  * the following fields:
  * <ul>
  *   <li> packet identifier, (4 bytes)
  *   <li> original packet length, (4 bytes)
  *   <li> offset of the current packet within the original payload. (4 bytes)
  * </ul>
  */
public class JumboPacket
{

   // *************************** constants ***************************

   /** Current protocol version */
   //static final int VERSION=1;

   /** Header length */
   static final int HLEN=12;

   /** Seem for id generation */
   static int id_generator=0;

   /** Seem for id generation */
   static int pickId()
   {  return id_generator++;
   }


   // ************************* packet header *************************

   /** Protocol version (1 byte) */
   //int version;

   /** Unique identifier of the original datagram (4 bytes) */   
   int packet_id;

   /** Length of the original datagram (4 bytes) */
   int packet_length;

   /** Offset within the original datagram (4 bytes) */
   int packet_offset;


   // ************************* packet payload ************************

   /** Data buffer */   
   byte[] data;

   /** Offset within the data buffer */   
   int data_offset;

   /** Length of data */   
   int data_length;


   // ************************* public methods ************************

   /** Creates a new JumboPacket */ 
   public JumboPacket(byte[] data)
   {  init(data,0,data.length,-1,-1,-1);
   }

   /** Creates a new JumboPacket */ 
   public JumboPacket(byte[] data, int data_offset, int data_length)
   {  init(data,data_offset,data_length,-1,-1,-1);
   }

   /** Creates a new JumboPacket fragment */ 
   public JumboPacket(byte[] data, int data_offset, int data_length, int packet_id, int packet_length, int packet_offset)
   {  init(data,data_offset,data_length,packet_id,packet_length,packet_offset);
   }

   /** Inits the JumboPacket */ 
   private void init(byte[] data, int data_offset, int data_length, int packet_id, int packet_length, int packet_offset)
   {  this.data=data;
      this.data_offset=data_offset;
      this.data_length=data_length;  
      if (packet_id>=0)
      {  this.packet_id=packet_id;
         this.packet_length=packet_length;
         this.packet_offset=packet_offset;
      }
      else
      {  this.packet_id=pickId();
         this.packet_length=data_length;
         this.packet_offset=0;
      }
   }

   /** Gets packet id. */
   public int getPacketId()
   {  return packet_id;
   }

   /** Gets the length of the original packet. */
   public int getOriginalPacketLength()
   {  return packet_length;
   }

   /** Gets the fragment offset within the original packet. */
   public int getOriginalPacketOffset()
   {  return packet_offset;
   }

   /** Gets the data received or the data to be sent. */
   public byte[] getData()
   {  return data;
   }

   /** Gets the length of the data. */
   public int getDataLength()
   {  return data_length;
   }

   /** Gets the offset of the data. */
   public int getDataOffset()
   {  return data_offset;
   }

   /** Whether this is a fragment of a bigger datagram. */
   public boolean isFragment()
   {  return packet_length!=data_length;
   }

   /** Whether this is the first fragment of the datagram. */
   public boolean isFirstFragment()
   {  return packet_offset==0;
   }

   /** Whether this is the last fragment of the datagram. */
   public boolean isLastFragment()
   {  return (packet_offset+data_length)==packet_length;
   }

   /** Gets the raw packet containing the JumboPacket. */
   public byte[] getBytes()
   {  byte[] pdu=new byte[HLEN+data_length];
      int i=0;
      // version
      //pdu[i++]=(byte)version;
      // packet id
      pdu[i++]=BinTools.getByte3(packet_id);
      pdu[i++]=BinTools.getByte2(packet_id);
      pdu[i++]=BinTools.getByte1(packet_id);
      pdu[i++]=BinTools.getByte0(packet_id);
      // packet length
      pdu[i++]=BinTools.getByte3(packet_length);
      pdu[i++]=BinTools.getByte2(packet_length);
      pdu[i++]=BinTools.getByte1(packet_length);
      pdu[i++]=BinTools.getByte0(packet_length);
      // packet offset
      pdu[i++]=BinTools.getByte3(packet_offset);
      pdu[i++]=BinTools.getByte2(packet_offset);
      pdu[i++]=BinTools.getByte1(packet_offset);
      pdu[i++]=BinTools.getByte0(packet_offset);
      // payload (here it should be i==HLEN, however force it for robusteness)
      i=HLEN;
      int end=data_offset+data_length;
      for (int j=data_offset; j<end; j++) pdu[i++]=data[j];
      return pdu;
   }

   /** Gets a JumboPacket from a array of bytes. */
   public static JumboPacket parsePacket(byte[] buff)
   {  return parsePacket(buff,0,buff.length);
   }

   /** Gets a JumboPacket from a array of bytes. */
   public static JumboPacket parsePacket(byte[] buff, int off, int len)
   {  int k=off;
      int packet_id=(int)BinTools.bytesToLong(buff[k],buff[k+1],buff[k+2],buff[k+3]);
      k+=4;
      int packet_length=(int)BinTools.bytesToLong(buff[k],buff[k+1],buff[k+2],buff[k+3]);
      k+=4;
      int packet_offset=(int)BinTools.bytesToLong(buff[k],buff[k+1],buff[k+2],buff[k+3]);

      JumboPacket jumbo=new JumboPacket(buff,off+HLEN,len-HLEN,packet_id,packet_length,packet_offset);
      return jumbo;
   }

   /** Gets an array of JumboPackets from a single JumboPacket with the constrain of a specified MTU size. */
   public JumboPacket[] doFragment(int mtu)
   {  int max_len=mtu-HLEN;
      int n=packet_length/max_len;
      if ((packet_length%max_len)!=0) n++;
      //System.err.println("DEBUG: n: "+n);
      JumboPacket[] fragments=new JumboPacket[n];
      for (int i=0; i<n; i++)
      {  int frag_len=(i<(n-1))? max_len : packet_length-i*max_len;
         fragments[i]=new JumboPacket(data,data_offset+i*max_len,frag_len,packet_id,packet_length,packet_offset+i*max_len);
      }
      return fragments;
   }

   /** Whether an array of JumboPackets forms a complete JumboPacket. */
   public static boolean isComplete(JumboPacket[] fragments)
   {  int packet_length=fragments[0].packet_length;
      int packet_id=fragments[0].packet_id;
      int total_length=fragments[0].data_length;
      for (int i=1; i<fragments.length; i++)
      {  if (fragments[i].packet_id==packet_id) total_length+=fragments[i].data_length;
      }
      return total_length>=packet_length;
   }

   /** Whether a Vector of JumboPackets forms a complete JumboPacket. */
   public static boolean isComplete(Vector fragments)
   {  JumboPacket frag0=(JumboPacket)fragments.elementAt(0);
      int packet_length=frag0.packet_length;
      int packet_id=frag0.packet_id;
      int total_length=frag0.data_length;
      for (int i=1; i<fragments.size(); i++)
      {  JumboPacket frag=(JumboPacket)fragments.elementAt(i);
         if (frag.packet_id==packet_id) total_length+=frag.data_length;
      }
      return total_length>=packet_length;
   }

   /** Gets the whole original packet from an array of JumboPackets. */
   public static JumboPacket doReassemble(JumboPacket[] fragments)
   {  if (!isComplete(fragments)) return null;
      //else
      int packet_length=fragments[0].packet_length;
      int packet_id=fragments[0].packet_id;
      byte[] data=new byte[packet_length];
      for (int i=0; i<fragments.length; i++)
      {  JumboPacket frag=fragments[i];
         BinTools.copyBytes(frag.data,frag.data_offset,data,frag.packet_offset,frag.data_length);
      }
      return new JumboPacket(data,0,packet_length,packet_id,packet_length,0);
   }

   /** Gets the whole original packet from a Vector of JumboPackets. */
   public static JumboPacket doReassemble(Vector fragments)
   {  if (!isComplete(fragments)) return null;
      //else
      JumboPacket frag0=(JumboPacket)fragments.elementAt(0);
      int packet_length=frag0.packet_length;
      int packet_id=frag0.packet_id;
      byte[] data=new byte[packet_length];
      for (int i=0; i<fragments.size(); i++)
      {  JumboPacket frag=(JumboPacket)fragments.elementAt(i);
         BinTools.copyBytes(frag.data,frag.data_offset,data,frag.packet_offset,frag.data_length);
      }
      return new JumboPacket(data,0,packet_length,packet_id,packet_length,0);
   }

   /** Gets the data as String. */
   public String getStringData()
   {  return new String(data,data_offset,data_length);
   }

   /** Gets the data as hex String. */
   public String getHexData()
   {  return BinTools.asHex(data,data_offset,data_length);
   }

   /** Converts this object to a String. */
   public String toString()
   {  String str="PKT="+packet_id+",HLEN="+HLEN+",DLEN="+packet_length;
      //if (isFragment())
      str+=",FRAG="+packet_offset+":"+(packet_offset+data_length-1);
      return str;
   }

   /** Converts this object to a hex String. */
   public String toHexString()
   {  return BinTools.asHex(getBytes());
   }


   // ******************************* main *******************************
 
   /** Prints out a String. */
   static void println(String str)
   {  System.out.println(str);
   }

   /** Prints out a JumboPacket. */
   static void println(JumboPacket packet)
   {  if (packet==null) System.out.println("NULL PACKET");
      else
      {  System.out.println(packet.toString()+",DATA=\""+packet.getStringData()+"\"");
         System.out.println("["+packet.toHexString()+"]");
      }
   }

   /** Test program. */
   public static void main(String[] args)
   {
      String text="Hello world!";
      int MTU=16;
      // original packet
      JumboPacket packet=new JumboPacket(text.getBytes());
      println("Original packet:");
      println(packet);
      // fragments
      JumboPacket[] fragments=packet.doFragment(MTU);      
      // permutation
      JumboPacket[] fragments_permut=new JumboPacket[fragments.length];    
      for (int i=0; i<fragments.length; i++)
      {  fragments_permut[i]=fragments[fragments.length-1-i];
      }
      fragments=fragments_permut;
      println("Fragments:");
      for (int i=0; i<fragments.length; i++)
      {  JumboPacket frag=fragments[i];
         println(frag);
      }
      packet=JumboPacket.doReassemble(fragments);
      println("Reassembled packet:");
      println(packet);
      // do error
      JumboPacket[] fragments_err=new JumboPacket[fragments.length-1];      
      for (int i=0; i<fragments_err.length; i++)
      {  fragments_err[i]=fragments[(i>=2)?i+1:i];
      }
      packet=JumboPacket.doReassemble(fragments_err);
      println("Erroneous packet:");
      println(packet);
   }
   
}
