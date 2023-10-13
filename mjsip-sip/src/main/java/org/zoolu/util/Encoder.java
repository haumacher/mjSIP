/*
 * Copyright (C) 2006 Luca Veltri - University of Parma - Italy
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



/** Generic interface for an encoding algorithm.
  */
public interface Encoder {
	
	/** Encodes an input chunk of data.
	  * @param in_buf buffer containing the input data
	  * @param in_off offset within the input buffer
	  * @param in_len length of the input data
	  * @param out_buf buffer for the output data (where the encoded data is written)
	  * @param out_off offset within the output buffer
	  * @return the length of the output data */
	public int encode(byte[] in_buf, int in_off, int in_len, byte[] out_buf, int out_off);
}
