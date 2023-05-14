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

package local.sbc;


import org.zoolu.net.SocketAddress;


/** Masquerade maintains soaddr mapping for a single flow. 
  */
public class Masquerade
{
   /** Peer SocketAddress */
   private SocketAddress peer_soaddr;
   /** Masq SocketAddress */
   private SocketAddress masq_soaddr;

   /** Creates a new Masquerade */
   public Masquerade(SocketAddress peer_soaddr, SocketAddress masq_soaddr)
   {  this.peer_soaddr=peer_soaddr;
      this.masq_soaddr=masq_soaddr;
   }
   
   /** Gets remote peer's address */
   public SocketAddress getPeerSoaddr()
   {  return peer_soaddr;
   }
   
   /** Gets masquerating address */
   public SocketAddress getMasqSoaddr()
   {  return masq_soaddr;
   }  

}