package org.mjsip.net;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;

import org.zoolu.net.SocketAddress;
import org.zoolu.net.UdpPacket;
import org.zoolu.net.UdpProvider;
import org.zoolu.net.UdpProviderListener;
import org.zoolu.net.UdpSocket;
import org.zoolu.util.ByteUtils;


/** Simple UDP packet generator, sender, and/or receiver.
  */
public class UdpTool {
	
	/** UDP provider */
	UdpProvider udp=null;

	/** Local input for outgoing packets */
	//BufferedReader input=null;

	/** Local output for incoming packets */
	Writer output=null;

	/** Time between packet departures (in milliseconds) */
	long inter_time=1000;

	/** Whether using ASCII mode */
	boolean ascii_mode=false;



	/** Creates a new UdpTool.
	  * @param local_port UDP local port
	  * @param output output for writing incoming data */
	public UdpTool(int local_port, Writer output) {
		this.output=output;
		try {
			UdpProviderListener udp_listener=new UdpProviderListener() {
				public void onReceivedPacket(UdpProvider udp, UdpPacket packet) {
					processUdpReceivedPacket(udp,packet);
				}
				public void onServiceTerminated(UdpProvider udp, Exception error) {
					processUdpServiceTerminated(udp,error);
				}
			};
			udp=new UdpProvider(new UdpSocket(local_port),udp_listener);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}


	/** Sets the time between packet departures.
	  * @param inter_time inter-time value (in milliseconds) */
	public void setInterTime(long inter_time) {
		this.inter_time=inter_time;
	}


	/** Sets ASCII mode.
	  * @param ascii_mode whether using ASCII inputs/outputs (true=ascii, false=hex) */
	public void setAsciiMode(boolean ascii_mode) {
		this.ascii_mode=ascii_mode;
	}


	/** Sends a packet to the given destination.
	  * @param data UDP packet payload
	  * @param dest_soaddr destination socket address */
	public void send(byte[] data, SocketAddress dest_soaddr) {
		try {
			UdpPacket packet=new UdpPacket(data);
			packet.setIpAddress(dest_soaddr.getAddress());
			packet.setPort(dest_soaddr.getPort());
			udp.send(packet);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}


	/** Sends a sequence of packets to the given destination.
	  * @param input trace input where packets are read from
	  * @param dest_soaddr destination socket address */
	public void sendTrace(BufferedReader input, SocketAddress dest_soaddr) {
		try {
			String line;
			while ((line=input.readLine())!=null) {
				try {  Thread.sleep(inter_time);  } catch (Exception e) {}
				UdpPacket packet=new UdpPacket((ascii_mode)? line.getBytes() : ByteUtils.hexToBytes(line));
				packet.setIpAddress(dest_soaddr.getAddress());
				packet.setPort(dest_soaddr.getPort());
				udp.send(packet);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}


	/** Sends a sequence of UDP packets with incremental sequence number as payload.
	  * @param num the number of packets to be sent
	  * @param dest_soaddr destination socket address */
	public void sendTrace(long num, SocketAddress dest_soaddr) {
		try {
			for (long i=0; i<num; i++) {
				try {  Thread.sleep(inter_time);  } catch (Exception e) {}
				send(Long.toString(i).getBytes(),dest_soaddr);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}


	/** Stops running. */
	public void halt() {
		if (udp!=null) udp.halt();
	}


	/** When a new UDP datagram is received. */
	private void processUdpReceivedPacket(UdpProvider udp, UdpPacket packet) {
		try {
			if (output!=null) {
				output.write((ascii_mode)? ByteUtils.asAscii(packet.getData(),packet.getOffset(),packet.getLength()) : ByteUtils.asHex(packet.getData(),packet.getOffset(),packet.getLength()));
				output.write('\n');
				output.flush();
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}  

	/** When UdpProvider terminates. */
	private void processUdpServiceTerminated(UdpProvider udp, Exception error) {
		try {
			if (output!=null) output.close();
			udp.getUdpSocket().close();
			udp=null;
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}  



  //**************************** MAIN ****************************


	/** Prints a new string message. */
	private static void println(String str) {
		System.out.println(str);
	}


	/** Prints out a help. */
	private static void printHelp() {
		final String CLASS_NAME=UdpTool.class.getName();
		println("\nUsage:\n   java "+CLASS_NAME+" [options]");     
		println("   options:");
		println("   -h                 prints this help");
		println("   -p <local_port>    UDP local port (defalut is 4000)");
		println("   -s <dest_soaddr>   UDP socket address where packet are sent to");
		println("   -i <input_file>    file where incoming packets are read from");
		println("   -o <output_file>   file where outgoing packets are written to");
		println("   -t <millisecs>     time between packet departures (in milliseconds)");
		println("   --data <payload>   packet payload to send");
		println("   --gen <num>        generate num packets with the payload equal to the counter (as string)");
		println("   --ascii            uses ASCII inputs/outputs instead of HEX");
		println("   -v                 runs in verbose/debug mode");
	}


	/** Main method. */
	public static void main(String[] args) {
		
		if (args.length==0) printHelp(); 

		int local_port=4000;
		SocketAddress dest_soaddr=null;
		BufferedReader input=null;
		Writer output=null;
		long inter_time=1000;
		String data=null;
		long gen_num=0;
		boolean ascii_mode=false;

		try {
			for (int i=0; i<args.length; i++) {
				
				String option;
				if (args[i].equals("-h")) {
					printHelp();
					System.exit(0);
				}
				// else
				if (args[i].startsWith(option="-p"))  {
					String str=(args[i].length()==option.length())? args[++i] : args[i].substring(option.length());
					local_port=Integer.parseInt(str);
					continue;
				}
				// else
				if (args[i].startsWith(option="-s"))  {
					String str=(args[i].length()==option.length())? args[++i] : args[i].substring(option.length());
					dest_soaddr=new SocketAddress(str);
					continue;
				}
				// else
				if (args[i].startsWith(option="-i"))  {
					String str=(args[i].length()==option.length())? args[++i] : args[i].substring(option.length());
					input=new BufferedReader(new FileReader(str));
					continue;
				}
				// else
				if (args[i].startsWith(option="-o"))  {
					String str=(args[i].length()==option.length())? args[++i] : args[i].substring(option.length());
					output=new BufferedWriter(new FileWriter(str));
					continue;
				}
				// else
				if (args[i].startsWith(option="-t"))  {
					String str=(args[i].length()==option.length())? args[++i] : args[i].substring(option.length());
					inter_time=Long.parseLong(str);
					continue;
				}
				// else
				if (args[i].startsWith(option="--data"))  {
					String str=(args[i].length()==option.length())? args[++i] : args[i].substring(option.length());
					data=str;
					continue;
				}
				// else
				if (args[i].startsWith(option="--gen"))  {
					String str=(args[i].length()==option.length())? args[++i] : args[i].substring(option.length());
					gen_num=Long.parseLong(str);
					continue;
				}
				// else
				if (args[i].equalsIgnoreCase("--ascii"))  {
					ascii_mode=true;
					continue;
				}
				// else
				if (args[i].equalsIgnoreCase("-v"))  {
					// TODO
					continue;
				}
			}
			
			if (output==null) output=new BufferedWriter(new OutputStreamWriter(System.out));
		}
		catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}

		UdpTool udp_tool=new UdpTool(local_port,output);
		udp_tool.setAsciiMode(ascii_mode);
		udp_tool.setInterTime(inter_time);
		
		if (data!=null) udp_tool.send((ascii_mode)? data.getBytes() : ByteUtils.hexToBytes(data),dest_soaddr);
		else
		if (input!=null) udp_tool.sendTrace(input,dest_soaddr);
		else
		if (gen_num>0) udp_tool.sendTrace(gen_num,dest_soaddr);
		else
		try {
			input=new BufferedReader(new InputStreamReader(System.in));
			println("Type a message to send or press 'Return' to exit");
			String line;
			while ((line=input.readLine())!=null && line.length()>0) {
				udp_tool.send((ascii_mode)? line.getBytes() : ByteUtils.hexToBytes(line),dest_soaddr);
				println("Type a message to send or press 'Return' to exit");
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		//println("Press 'Return' to stop");
		//try {  System.in.read();  } catch (Exception e) {}
		udp_tool.halt();
		println("exiting");
	}
  
}
