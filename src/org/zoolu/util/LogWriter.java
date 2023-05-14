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


import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Date;


/** Simple logger that writes log messages onto a logfile, or standard output, or {@link java.io.Writer}, or {@link java.io.OutputStream}
  * or {@link java.io.PrintStream}.
  * <p>
  * When creating a logger you can also specify a <i>priority_level</i> and a <i>maximum_size</i> for the log.
  * <p>
  * The attribute <i>priority_level</i> is used to manage different levels of verboseness.
  * When adding a log message through the methods {@link Logger#log(LogLevel,String)} or {@link Logger#log(LogLevel,Class,String)}
  * a {@link LogLevel} <i>log_level</i> for the given message is specified; only log messages with a <i>log_level</i>
  * greater or equal to the logger <i>priority_level</i> are recorded.
  * <br>
  * With priority level {@link LogLevel#OFF} no messages are logged.
  * With priority level {@link LogLevel#ALL} all messages are logged.
  * <p>
  * The attribute <i>maximum_size</i> is used to limit the size the log.
  * When the log size reaches the <i>maximum_size</i> value, no more log messages are recorded.
  */
public class LogWriter implements Logger {
	
	/** Default maximum log file size (1MB) */
	public static long DEFAULT_MAX_SIZE=1024*1024; // 1MB


	/** The log writer */
	protected Writer out;

	/** The <i>logging_level</i>.
	  * Only messages with a level greater than or equal to this <i>logging_level</i> are logged. */
	LogLevel logging_level;
	
	/** The maximum size of the log stream/file [bytes]
	  * Value 0 (or negative) indicates no maximum size */
	long max_size;
	  
	/** Whether writing a timestamp header */
	boolean timestamp=true;

	/** The char counter of the already logged data */
	long counter;



	/** Creates a new LogWriter.
	  * @param out the Writer where log messages are written to */
	public LogWriter(Writer out) {
		init(out,LogLevel.INFO,0);
	}


	/** Creates a new LogWriter.
	  * @param out the Writer where log messages are written to 
	  * @param logging_level the logging level */
	public LogWriter(Writer out, LogLevel logging_level) {
		init(out,logging_level,0);
	}


	/** Creates a new LogWriter.
	  * @param out the OutputStream where log messages are written to */
	public LogWriter(OutputStream out) {
		init(new OutputStreamWriter(out),LogLevel.INFO,0);
	}


	/** Creates a new LogWriter.
	  * @param out the OutputStream where log messages are written to
	  * @param logging_level the logging level */
	public LogWriter(OutputStream out, LogLevel logging_level) {
		init(new OutputStreamWriter(out),logging_level,0);
	}


	/** Creates a new the LogWriter.
	  * @param file_name the file where log messages are written to */
	public LogWriter(String file_name) {
		init(file_name,LogLevel.INFO,DEFAULT_MAX_SIZE,false);
	}


	/** Creates a new the LogWriter.
	  * @param file_name the file where log messages are written to
	  * @param logging_level the logging level */
	public LogWriter(String file_name, LogLevel logging_level) {
		init(file_name,logging_level,DEFAULT_MAX_SIZE,false);
	}


	/** Creates a new the LogWriter.
	  * @param file_name the file where log messages are written to
	  * @param logging_level the logging level
	  * @param max_size the maximum size for the log file, that is the maximum number of characters that can be wirtten */
	public LogWriter(String file_name, LogLevel logging_level, long max_size) {
		init(file_name,logging_level,max_size,false);
	}


	/** Creates a new the LogWriter.
	  * @param file_name the file where log messages are written to
	  * @param logging_level the logging level
	  * @param max_size the maximum size for the log file, that is the maximum number of characters that can be wirtten
	  * @param append if <i>true</i>, the file is opened in 'append' mode, that is the new messages are appended to the previously saved file (the file is not rewritten) */
	public LogWriter(String file_name, LogLevel logging_level, long max_size, boolean append) {
		init(file_name,logging_level,max_size,append);
	}


	/** Initializes the LogWriter.
	  * @param out the Writer where log messages are written to
	  * @param logging_level the logging level
	  * @param max_size the maximum size for the log, that is the maximum number of characters that can be written */
	private void init(Writer out, LogLevel logging_level, long max_size)  {
		this.out=out;
		this.logging_level=logging_level;
		this.max_size=max_size;
		counter=0;
	}


	/** Initializes the LogWriter.
	  * @param file_name the file where log messages are written to
	  * @param logging_level the logging level
	  * @param max_size the maximum size for the log file, that is the maximum number of characters that can be wirtten
	  * @param append if <i>true</i>, the file is opened in 'append' mode, that is the new messages are appended to the previously saved file (the file is not rewritten) */
	private void init(String file_name, LogLevel logging_level, long max_size, boolean append) {
		if (logging_level!=LogLevel.OFF) {
			try {
				Writer out=new OutputStreamWriter(new FileOutputStream(file_name,append));
				init(out,logging_level,max_size);
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
		else init(null,LogLevel.OFF,0);
	}


	/** Sets the logging level.
	  * @param logging_level the logging level */
	public void setLoggingLevel(LogLevel logging_level)  {
		this.logging_level=logging_level;
	}


	/** Gets the current logging level.
	  * @return the logging level */
	public LogLevel getLoggingLevel()  {
		return logging_level;
	}


	/** Enables or disables writing a timestamp header.
	  * @param timestamp true for enabling the use of timestamps */
	public void setTimestamp(boolean timestamp)  {
		this.timestamp=timestamp;
	}


	/** Closes the log writer. */
	public void close() {
		if (out!=null) try {  out.close();  } catch (IOException e) {  e.printStackTrace();  }
		out=null;
	}


	/** Adds a log message.
	  * @param message the message to be logged */
	//J5:@Override
	public void log(String message) {
		log(LogLevel.INFO,null,message);
	}


	/** Adds a log message.
	  * @param level the log level of this message; only messages with log level greater than or equal to the <i>logging_level</i> of the log writer are actually recorded
	  * @param message the message to be logged */
	//J5:@Override
	public void log(LogLevel level, String message) {
		log(level,null,message);
	}


	/** Adds a log message.
	  * @param level the log level of this message; only messages with log level greater than or equal to the <i>logging_level</i> of the log writer are actually recorded
	  * @param source_class the origin of this log message
	  * @param message the message to be logged */
	//J5:@Override
	public synchronized void log(LogLevel level, Class source_class, String message) {
		if (out!=null && level.getValue()>=logging_level.getValue() && (max_size<=0 || counter<max_size)) {
			StringBuffer sb=new StringBuffer();
			if (timestamp) sb.append(DateFormat.formatHHmmssSSS(new Date(System.currentTimeMillis()))).append(": ");
			if (level!=LogLevel.INFO) sb.append(level.getName()).append(": ");
			if (source_class!=null) sb.append(source_class.getName().substring(source_class.getPackage().getName().length()+1)).append(": ");
			message=sb.append(message).append("\r\n").toString();
			write(message);
			counter+=message.length();
			if (max_size>0 && counter>=max_size) write("\r\n----MAXIMUM LOG SIZE----\r\nSuccessive logs are lost.");
		}
	}


	/** Writes a string onto the inner writer.
	  * @param str the string to be written */
	protected synchronized void write(String str) {
		try {
			out.write(str);
			out.flush();
		}
		catch (Exception e) {}
	}


	/** Resets the char counter. */
	protected void reset() {
		counter=0;
	}

}
