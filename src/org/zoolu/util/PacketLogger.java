package org.zoolu.util;


import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;


/** Packet logger.
  */
public class PacketLogger {
	
	/** Default logger */
	protected static PacketLogger DEFAULT_LOGGER=null;

	/** The writer */
	protected Writer out;

	/** Start time */
	long start_time=System.currentTimeMillis();



	/** Creates a new PacketLogger.
	  * @param out the Writer where log messages are written to */
	public PacketLogger(Writer out) {
		this.out=out;
	}


	/** Creates a new PacketLogger.
	  * @param out the OutputStream where log messages are written to */
	public PacketLogger(OutputStream out) {
		this.out=new OutputStreamWriter(out);
	}


	/** Creates a new the PacketLogger.
	  * @param file_name the file where log messages are written to */
	public PacketLogger(String file_name) {
		try {
			out=new OutputStreamWriter(new FileOutputStream(file_name));
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}


	/** Sets the deafult packet logger. */
	public static void setDefaultPacketLogger(PacketLogger default_logger)  {
		DEFAULT_LOGGER=default_logger;
	}


	/** Gets the deafult packet logger. */
	public static PacketLogger getDefaultPacketLogger()  {
		return DEFAULT_LOGGER;
	}


	/** From class Writer. Close the stream, flushing it first. */
	public void close() throws IOException {
		if (out!=null) out.close();
		out=null;
	}


	/** Adds a packet. */
	public void append(String src_address, String dest_address, String description, byte[] data) {
		append(src_address,dest_address,description,data,0,data.length);
	}

	/** Adds a packet. */
	public synchronized void append(String src_address, String dest_address, String description, byte[] buf, int off, int len) {
		try {
			out.write(String.valueOf(System.currentTimeMillis()-start_time));
			out.write('\t');
			out.write(src_address);
			out.write('\t');
			out.write(dest_address);
			out.write('\t');
			out.write(description);
			out.write('\t');
			out.write(String.valueOf(len));
			out.write('\t');
			out.write(ByteUtils.asHex(buf,off,len));
			out.write('\n');
			out.flush();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

}
