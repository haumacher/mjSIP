/*
 * Copyright (C) 2005 Luca Veltri - University of Parma - Italy
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

package local.net;


import org.zoolu.tools.Random;


/** RtcpPacket implements a RTCP packet. 
 */
public class RtcpPacket
{
   /* RTCP packet buffer containing both the RTCP header and payload */   
   byte[] packet;

   /* RTCP packet length */   
   int packet_len;

   /* RTCP header length */   
   //int header_len;

   /** Gets the RTCP packet */
   public byte[] getPacket()
   {  return packet;
   }

   /** Gets the RTCP packet length */
   public int getLength()
   {  return packet_len;
   }

   // version (V): 2 bits
   // padding (P): 1 bit
   // extension (X): 1 bit
   // CSRC count (CC): 4 bits
   // marker (M): 1 bit
   // RTCP report type (200=SR, 201=RR, 202=SDES): 7 bits


   /** Creates a new RTP packet */ 
   public RtcpPacket(byte[] buffer, int packet_len)
   {  packet=buffer;
      if (packet_len<8 && packet.length>=8) packet_len=8;
      if (packet_len>packet.length) packet_len=packet.length;
      this.packet_len=packet_len;
   }

   /** Gets the version (V) */
   public int getVersion()
   {  if (packet_len>=8) return (packet[0]>>6 & 0x03);
      else return 0; // broken packet 
   }

   /** Sets the version (V) */
   public void setVersion(int v)
   {  if (packet.length>=8) packet[0]=(byte)((packet[0] & 0x3F) | ((v & 0x03)<<6));
   }

   /** Whether has padding (P) */
   public boolean hasPadding()
   {  if (packet_len>=8) return RtpPacket.getBit(packet[0],5);
      else return false; // broken packet 
   }

   /** Set padding (P) */
   public void setPadding(boolean p)
   {  if (packet.length>=8) RtpPacket.setBit(p,packet[0],5);
   }

   /** Whether has extension (X) */
   public boolean hasExtension()
   {  if (packet_len>=8) return RtpPacket.getBit(packet[0],4);
      else return false; // broken packet 
   }

   /** Set extension (X) */
   public void setExtension(boolean x)
   {  if (packet.length>=8) RtpPacket.setBit(x,packet[0],4);
   }

   /** Gets the reception report count (RC) */
   public int getReceptionRecordCount()
   {  if (packet_len>=8) return (packet[0] & 0x0F);
      else return 0; // broken packet
   }

   /** Sets the reception report count (RC) */
   public void setReceptionRecordCount(int pt)
   {  if (packet.length>=8) packet[0]=(byte)((packet[0] & 0xF0) | (pt & 0x0F));
   }

   /** Gets the RTCP report type (PT) */
   public int getReportType()
   {  if (packet_len>=8) return packet[1];
      else return -1; // broken packet
   }

   /** Sets the RTCP report type (PT) */
   public void setReportType(int pt)
   {  if (packet.length>=8) packet[1]=(byte)(pt & 0xFF);
   }

   /** Gets the RTCP packet length */
   /*public int getLength()
   {  if (packet_len>=8) return (getInt(packet,2,4)+1)*4;
      else return 0; // broken packet
   }*/

   /** Sets the RTCP packet length */
   public void setLength(int len)
   {  if (packet.length>=8)
      {  if (packet.length<len) len=packet.length;
         RtpPacket.setInt(len,packet,2,4);
         packet_len=len;
      }
   }

   /** Gets the SSCR */
   public long getSscr()
   {  if (packet_len>=8) return RtpPacket.getLong(packet,4,8);
      else return 0; // broken packet
   }

   /** Sets the SSCR */
   public void setSscr(long ssrc)
   {  if (packet.length>=8) RtpPacket.setLong(ssrc,packet,4,8);
   }

}
