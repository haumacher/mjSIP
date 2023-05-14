package org.mjsip.media;



import java.io.IOException;

import org.zoolu.net.SocketAddress;
import org.zoolu.sound.CodecType;
import org.zoolu.util.LogWriter;



/** Simple audio application for sending and/or receiving audio streams.
 */
public class AudioApp {

	
	/** Prints a new string message. */
	protected static void println(String str) {
		System.out.println(str);
	}

	
	/** Creates a new audio streamer. */
	protected AudioStreamer createAudioStreamer(FlowSpec flow_spec, String audiofile_in, String audiofile_out, boolean direct_convertion, Codec additional_encoding, boolean do_sync, int random_early_drop, boolean symmetric_rtp, boolean rtcp, LogWriter log) {
		return new AudioStreamer(flow_spec,audiofile_in,audiofile_out,direct_convertion,additional_encoding,do_sync,random_early_drop,symmetric_rtp,rtcp,log);
	}


	/** Prints out a help. */
	protected void printHelp() {
		println("\nUsage:\n   java "+getClass().getName()+" [options]");     
		println("   options:");
		println("   -h                prints this help");
		println("   -v                verbose mode");
		println("   -s <ipaddr:port>  sends audio to the given remote ipaddr:port");
		println("   -r <port>         receives audio on the given port");
		println("   -i <audio-in>     uses the given source as auidio input (default is system mic)");
		println("   -o <audio-out>    uses the given destination as audio output (default is system speaker)");
		println("   -c <codec>        uses the given codec, e.g. ULAW (default), ALAW, GSM, etc.");
		println("   --srate <rate>    uses the given sample rate (deafault is 8000 sample/s)");
		println("   --psize <size>    uses the given packet payload size (deafault is 320 B)");
		println("   --adj <time>      sets the difference between the actual inter-packet sending time respect to the nominal value (in milliseconds)");
		println("   --red <num>       sets packet random early drop value, for avoiding packet starvation at receiver; sets the number of packets that separates two drops; 0 means no drop");
		println("   --tone <freq>     generates a tone as input, with a given frequency [Hz]");
		println("   --ampl <ampl>     uses the given value as tone amplitude (between 0 and 1, default=0.5)");
		println("   --rtcp            uses RTCP");
		println("   --be              uses Bandwidth-Efficient mode");
		println("   --sqn-check       receiver discards out-of-sequence and duplicated packets");
		println("   --silence-pad     receiver fills silence periods with void audio");
		println("");
		println("   --debug-drop-rate <time> sender drops packets every <time> millisecs");
		println("   --debug-drop-time <time> sender drops packets for a duration of <time> millisecs");
		println("   --stereo          whether using stereo");
	}


	/** Creates a SimpleAudioApp and starts it. */
	public void run(String[] args) {
		
		if (args.length==0) printHelp(); 

		boolean direct_convertion=false;
		boolean do_sync=true;
		//int random_early_drop=20;
		int random_early_drop=0;
		boolean symmetric_rtp=false;
		long sync_adj=0;

		String codec_name=AudioStreamer.DEFAULT_CODEC_NAME;
		int sample_rate=AudioStreamer.DEFAULT_SAMPLE_RATE;
		int packet_size=-1;

		int tone_freq=0;
		double tone_ampl=0.5;

		SocketAddress remote_soaddr=null;
		int local_port=0;
		String audio_in=null;
		String audio_out=null;
		int channels=1;
		
		boolean rtcp=false;
				
		try {
			for (int i=0; i<args.length; i++) {
				String param;
				int param_len;
				
				if (args[i].equals("-h")) {
					printHelp();
					System.exit(0);
				}
				// else
				if (args[i].equals("-v"))  {
					AudioStreamer.VERBOSE_DEBUG=true;
					continue;
				}
				// else
				if (args[i].startsWith("-s"))  {
					String str=(args[i].length()==2)? args[++i] : args[i].substring(2);
					remote_soaddr=new SocketAddress(str);
					continue;
				}
				// else
				if (args[i].startsWith("-r"))  {
					String str=(args[i].length()==2)? args[++i] : args[i].substring(2);
					local_port=Integer.parseInt(str);
					continue;
				}
				// else
				if (args[i].startsWith("-i"))  {
					String str=(args[i].length()==2)? args[++i] : args[i].substring(2);
					audio_in=str;
					continue;
				}
				// else
				if (args[i].startsWith("-o"))  {
					String str=(args[i].length()==2)? args[++i] : args[i].substring(2);
					audio_out=str;
					continue;
				}
				// else
				if (args[i].startsWith("-c"))  {
					String str=(args[i].length()==2)? args[++i] : args[i].substring(2);
					codec_name=str;
					continue;
				}
				// else
				if (args[i].startsWith("--srate"))  {
					String str=(args[i].length()==7)? args[++i] : args[i].substring(7);
					sample_rate=Integer.parseInt(str);
					continue;
				}
				// else
				if (args[i].startsWith("--psize"))  {
					String str=(args[i].length()==7)? args[++i] : args[i].substring(7);
					packet_size=Integer.parseInt(str);
					continue;
				}
				// else
				if (args[i].startsWith("--adj"))  {
					String str=(args[i].length()==5)? args[++i] : args[i].substring(5);
					sync_adj=Long.parseLong(str);
					continue;
				}
				// else
				if (args[i].startsWith("--red"))  {
					String str=(args[i].length()==5)? args[++i] : args[i].substring(5);
					random_early_drop=Integer.parseInt(str);
					continue;
				}
				// else
				if (args[i].startsWith("--tone"))  {
					String str=(args[i].length()==6)? args[++i] : args[i].substring(6);
					tone_freq=Integer.parseInt(str);
					continue;
				}
				// else
				if (args[i].startsWith("--ampl"))  {
					String str=(args[i].length()==6)? args[++i] : args[i].substring(6);
					tone_ampl=Double.parseDouble(str);
					continue;
				}
				// else
				if (args[i].startsWith("--rtcp"))  {
					rtcp=true;
					continue;
				}
				// else
				if (args[i].startsWith("--sqn-check"))  {
					AudioStreamer.SEQUENCE_CHECK=true;
					continue;
				}
				// else
				if (args[i].startsWith("--silence-pad"))  {
					AudioStreamer.SILENCE_PADDING=true;
					continue;
				}
				// else
				if (args[i].startsWith("--be"))  {
					AudioStreamer.RTP_BANDWIDTH_EFFICIENT_MODE=true;
					continue;
				}
				// else
				if (args[i].startsWith(param="--debug-drop-rate"))  {
					String str=(args[i].length()==(param_len=param.length()))? args[++i] : args[i].substring(param_len);
					RtpStreamSender.DEBUG_DROP_RATE=(int)(Long.parseLong(str)/20);
					continue;
				}
				// else
				if (args[i].startsWith(param="--debug-drop-time"))  {
					String str=(args[i].length()==(param_len=param.length()))? args[++i] : args[i].substring(param_len);
					RtpStreamSender.DEBUG_DROP_TIME=(int)(Long.parseLong(str)/20);
					continue;
				}
				// else
				if (args[i].startsWith(param="--stereo"))  {
					channels=2;
					continue;
				}
				// else
				println("\nUnknown option: "+args[i]);
				println("Use -h for a complete list of options.");
				System.exit(0);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			//printHelp();
			System.exit(0);
		}

		if (local_port==0 && remote_soaddr==null) {
			//printHelp();
			System.exit(0);
		}
		
		FlowSpec.Direction dir=(remote_soaddr==null)? FlowSpec.RECV_ONLY: ((local_port==0)? FlowSpec.SEND_ONLY : FlowSpec.FULL_DUPLEX);
		String remote_ipaddr=(remote_soaddr!=null)? remote_soaddr.getAddress().toString() : null;
		int remote_port=(remote_soaddr!=null)? remote_soaddr.getPort() : 0;
		
		if (tone_freq>0) {
			audio_in=AudioStreamer.TONE;
			AudioStreamer.TONE_FREQ=tone_freq;
			AudioStreamer.TONE_AMPL=tone_ampl;
		}

		CodecType codec=CodecType.getByName(codec_name);
		int avp=codec.getPayloadType();
		int frame_size=codec.getFrameSize();
		int frame_rate=sample_rate/codec.getSamplesPerFrame();
		if (packet_size<=0) packet_size=(int)(frame_rate*frame_size*AudioStreamer.DEFAULT_PACKET_TIME/1000);
		//int packet_rate=frame_rate*frame_size/packet_size;
		int packet_time=(int)(packet_size*1000/(frame_rate*frame_size));
		println("Codec: "+avp+" "+codec);
		println("Frame rate: "+frame_rate+" frame/s");
		println("Frame size: "+frame_size+" B");
		println("Packet time: "+packet_time+" ms");
		println("Packet rate: "+(1000/packet_time)+" pkt/s");
		println("Packet size: "+packet_size+" B");
		if (random_early_drop>0) println("Random early drop at receiver: 1 packet out of "+random_early_drop);
		if (sync_adj!=0) println("Inter-packet time adjustment at sender: "+sync_adj+" ms every "+packet_time+" ms");
		MediaSpec mspec=new MediaSpec("audio",avp,codec_name,sample_rate,channels,packet_size);      
		FlowSpec fspec=new FlowSpec(mspec,local_port,remote_ipaddr,remote_port,dir);

		AudioStreamer audio_streamer=createAudioStreamer(fspec,audio_in,audio_out,direct_convertion,null,do_sync,random_early_drop,symmetric_rtp,rtcp,null);

		//if (random_early_drop>0) aapp.setRED(random_early_drop);
		if (sync_adj!=0) audio_streamer.setSyncAdj(sync_adj);
		audio_streamer.start();
		println("Press 'Return' to stop.");
		try {  System.in.read();  } catch (IOException e) {};
		audio_streamer.halt();
		try {  Thread.sleep(1000);  } catch (Exception e) {};
		System.exit(0);
	}



	// ****************************** Main *****************************

	/** The main method. */
	public static void main(String[] args) {
		new AudioApp().run(args);
	}
	
	
}
