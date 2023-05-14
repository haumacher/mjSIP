package org.mjsip.media;



import org.zoolu.util.Parser;



/** Media specification.
  */
public class MediaSpec {
	

	/** Media type (e.g. audio, video, message, etc.) */
	String type;

	/** AVP code */
	int avp;

	/** Codec */
	String codec;

	/** Sample rate [samples/sec] */
	int sample_rate;

	/** Number of channels (e.g. in case of audio, 1 for mono, 2 for stereo) */
	int channels;

	/** Packet size [bytes] */
	int packet_size;
	

	
	/** Creates a new MediaSpec.
	  * @param type edia type
	  * @param avp AVP code
	  * @param codec codec type
	  * @param sample_rate sample rate
	  * @param channels number of channels (e.g. in case of audio, 1 for mono, 2 for stereo)
	  * @param packet_size packet size */
	public MediaSpec(String type, int avp, String codec, int sample_rate, int channels, int packet_size) {
		init(type,avp,codec,sample_rate,channels,packet_size);
	}


	/** Inits the MediaSpec. */
	private void init(String type, int avp, String codec, int sample_rate, int channels, int packet_size) {
		this.type=type;
		this.avp=avp;
		this.codec=codec;
		this.sample_rate=sample_rate;
		this.channels=channels;
		this.packet_size=packet_size;
	}


	/** Gets media type. */
	public String getType() {
		return type;
	}


	/** Gets AVP code. */
	public int getAVP() {
		return avp;
	}


	/** Gets codec. */
	public String getCodec() {
		return codec;
	}


	/** Gets sample rate. */
	public int getSampleRate() {
		return sample_rate;
	}
	
	
	/** Gets number of channels. */
	public int getChannels() {
		return channels;
	}


	/** Gets packet size. */
	public int getPacketSize() {
		return packet_size;
	}


	/** Gets a string representation of this object. */
	public String toString() {
		StringBuffer sb=new StringBuffer();
		sb.append(type).append(" ").append(avp);
		if (codec!=null) sb.append(" ").append(codec).append(" ").append(sample_rate).append(" ").append(packet_size).append(" ").append(channels);
		return sb.toString();
	}

	/** Parses a string and gets a new MediaSpec. */
	public static MediaSpec parseMediaSpec(String str) {
		//System.out.println("MediaSpec: parsing: "+str);
		Parser par=new Parser(str);
		String type=par.getString();
		int avp=par.getInt();
		String codec=(par.skipWSP().hasMore())? par.getString() : null;
		int sample_rate=(par.skipWSP().hasMore())? par.getInt() : 0;
		int packet_size=(par.skipWSP().hasMore())? par.getInt() : 0;
		int channels=(par.skipWSP().hasMore())? par.getInt() : 1;
		return new MediaSpec(type,avp,codec,sample_rate,channels,packet_size);
	}

}
