/*
 * Copyright (C) 2014 Luca Veltri - University of Parma - Italy
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




/** A generic message logger.
  */
public interface Logger {
	
	/** Adds a log message.
	  * @param message the message to be logged */
	public void log(String message);

	/** Adds a log message.
	  * @param level the log level of this message
	  * @param message the message to be logged */
	public void log(LogLevel level, String message);

	/** Adds a log message.
	  * @param level the log level of this message
	  * @param source_class the origin of this log message
	  * @param message the message to be logged */
	public void log(LogLevel level, Class source_class, String message);

}
