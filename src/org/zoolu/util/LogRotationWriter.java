/*
 * Copyright (C) 2013 Luca Veltri - University of Parma - Italy
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



import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Calendar;



/** It extends LogWriter providing rotation of the log file. 
  */
public class LogRotationWriter extends LogWriter {
	

	/** Month */
	public static final int MONTH=Calendar.MONTH;

	/** Day */
	public static final int DAY=Calendar.DAY_OF_MONTH;

	/** Hour */
	public static final int HOUR=Calendar.HOUR;

	/** Minute */
	public static final int MINUTE=Calendar.MINUTE;



	/** Number of log file rotations (value 0 means no rotation) */
	int num_rotations;

	/** Rotates log files */
	String file_name;
	
	/** Time scale (MONTH, DAY, HOUR, or MINUTE) */
	int time_scale;

	/** Time value when log files are rotated (time = time_scale * time_value) */
	int time_value;

	/** Date of the next rotation */
	long next_rotation;



	/** Creates a new LogRotationWriter.
	  * @param file_name the file where log messages are written to
	  * @param verbose_level the verbose level
	  * @param max_size the maximum size for the log, that is the maximum number of characters that can be written
	  * @param num_rotations the number of log file rotations (value 0 means no rotation)
	  * @param time_scale the time scale (MONTH, DAY, HOUR, or MINUTE)
	  * @param time_value the time value when log files are rotated (actual time = time_scale * time_value) */
	public LogRotationWriter(String file_name, LogLevel verbose_level, long max_size, int num_rotations, int time_scale, int time_value) {
		super(file_name,verbose_level,max_size);
		init(file_name,num_rotations,time_scale,time_value);
	}


	/** Creates a new LogRotationWriter.
	  * @param file_name the file where log messages are written to
	  * @param verbose_level the verbose level
	  * @param max_size the maximum size for the log, that is the maximum number of characters that can be written
	  * @param num_rotations the number of log file rotations (value 0 means no rotation)
	  * @param time_scale the time scale (MONTH, DAY, HOUR, or MINUTE)
	  * @param time_value the time value when log files are rotated (actual time = time_scale * time_value)
	  * @param append if <i>true</i>, it opens the existing log file in 'append' mode, without rewritting the previously saved content */
	public LogRotationWriter(String file_name, LogLevel verbose_level, long max_size, int num_rotations, int time_scale, int time_value, boolean append) {
		super(file_name,verbose_level,max_size,append);
		init(file_name,num_rotations,time_scale,time_value);
	}


	/** Inits the log rotation.
	  * @param file_name the file where log messages are written to
	  * @param num_rotations the number of log file rotations (value 0 means no rotation)
	  * @param time_scale the time scale (MONTH, DAY, HOUR, or MINUTE)
	  * @param time_value the time value when log files are rotated (actual time = time_scale * time_value) */
	private void init(String file_name, int num_rotations, int time_scale, int time_value) {
		this.file_name=file_name;
		this.num_rotations=num_rotations;
		this.time_scale=time_scale;
		this.time_value=time_value;
		updateNextRotationTime();
	}


	/** Rotates logs. */
	public synchronized LogRotationWriter rotate() {
		if (num_rotations>0) {
			for (int i=num_rotations-2; i>0; i--) {
				// rename back files
				rename(file_name+'.'+i,file_name+'.'+(i+1));
			}
			// save and close current log file
			if (out!=null) try {  out.close();  } catch (IOException e) {  e.printStackTrace();  }
			// rename current log file
			if (num_rotations>1) rename(file_name,file_name+'.'+1);
			// reset the log
			try {  out=new OutputStreamWriter(new FileOutputStream(file_name));  } catch (IOException e) {  e.printStackTrace();  }
			reset();
		}
		return this; 
	}  


	/** Writes a string onto the inner writer.
	  * @param str the string to be written */
	public synchronized void write(String str)  {
		long now=Calendar.getInstance().getTime().getTime();
		if (now>next_rotation) {
			rotate();
			updateNextRotationTime();
		}
		super.write(str);
	}

	
	/** Renames a file. */
	private static void rename(String src_file, String dst_file) {
		File src=new File(src_file);
		if (src.exists())  {
			File dst=new File(dst_file);
			if (dst.exists()) dst.delete();
			src.renameTo(dst);
		}
	}


	/** Updates the next rotation date. */
	private void updateNextRotationTime() {
		Calendar cal=Calendar.getInstance();
		cal.add(time_scale,time_value);     
		next_rotation=cal.getTime().getTime(); 
	}
		
}
