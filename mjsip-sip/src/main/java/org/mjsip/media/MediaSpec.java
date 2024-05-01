package org.mjsip.media;

import java.util.HashMap;
import java.util.Map;

import org.zoolu.sound.CodecType;
import org.zoolu.util.Parser;

/**
 * Media encoding specification.
 */
public class MediaSpec {

	// See https://en.wikipedia.org/wiki/RTP_payload_formats
	private static final MediaSpec[] WELL_KNOWN = {
			new MediaSpec(0, 	"PCMU",	 /*audio*/ 	     8000,  1, 0),
			new MediaSpec(3, 	"GSM", 	 /*audio*/ 	     8000,  1, 0),
			new MediaSpec(4, 	"G723",	 /*audio*/ 	     8000,  1, 0),
			new MediaSpec(5, 	"DVI4",	 /*audio*/ 	     8000,  1, 0),
			new MediaSpec(6, 	"DVI4",	 /*audio*/ 	     16000, 1, 0),
			new MediaSpec(7, 	"LPC", 	 /*audio*/ 	     8000,  1, 0),
			new MediaSpec(8, 	"PCMA",	 /*audio*/ 	     8000,  1, 0),
			new MediaSpec(9, 	"G722",	 /*audio*/ 	     16000, 1, 0),
			new MediaSpec(10, 	"L16", 	 /*audio*/ 	     44100, 2, 0),
			new MediaSpec(11, 	"L16", 	 /*audio*/ 	     44100, 1, 0),
			new MediaSpec(12, 	"QCELP", /*audio*/ 	     8000,  1, 0),
			new MediaSpec(13, 	"CN", 	 /*audio*/ 	     8000,  1, 0),
			new MediaSpec(14, 	"MPA", 	 /*audio*/ 	     90000, 1, 0),
			new MediaSpec(15, 	"G728",	 /*audio*/ 	     8000,  1, 0),
			new MediaSpec(16, 	"DVI4",	 /*audio*/ 	     11025, 1, 0),
			new MediaSpec(17, 	"DVI4",	 /*audio*/ 	     22050, 1, 0),
			new MediaSpec(18, 	"G729",	 /*audio*/ 	     8000,  1, 0),
			new MediaSpec(25, 	"CELLB", /*video*/ 	     90000, 0, 0),
			new MediaSpec(26, 	"JPEG",	 /*video*/ 	     90000, 0, 0),
			new MediaSpec(28, 	"nv", 	 /*video*/ 	     90000, 0, 0),
			new MediaSpec(31, 	"H261",	 /*video*/ 	     90000, 0, 0),
			new MediaSpec(32, 	"MPV", 	 /*video*/ 	     90000, 0, 0),
			new MediaSpec(33, 	"MP2T",	 /*audio/video*/ 90000, 0, 0),
			new MediaSpec(34, 	"H263",	 /*video*/ 	     90000, 0, 0)
	};
	
	private static final Map<Integer, MediaSpec> WELL_KNOWN_BY_AVP;

	static {
		WELL_KNOWN_BY_AVP = new HashMap<>();
		for (MediaSpec spec : WELL_KNOWN) {
			WELL_KNOWN_BY_AVP.put(Integer.valueOf(spec.getAVP()), spec);
		}
	}
	
	/**
	 * The well-known MediaSpec by payload type code.
	 * 
	 * @see "https://en.wikipedia.org/wiki/RTP_payload_formats"
	 */
	public static final MediaSpec getWellKnown(int avp) {
		return WELL_KNOWN_BY_AVP.get(Integer.valueOf(avp));
	}

	/** AVP code */
	private final int avp;

	/** Codec */
	private final String codec;

	/** Sample rate [samples/sec] */
	private final int sample_rate;

	/** Number of channels (e.g. in case of audio, 1 for mono, 2 for stereo) */
	private final int channels;

	/** Packet size [bytes] */
	private final int packet_size;
	
	/**
	 * Creates a new MediaSpec.
	 * @param avp
	 *        AVP code
	 * @param codec
	 *        codec type
	 * @param sample_rate
	 *        sample rate
	 * @param channels
	 *        number of channels (e.g. in case of audio, 1 for mono, 2 for stereo)
	 * @param packet_size
	 *        packet size
	 */
	public MediaSpec(int avp, String codec, int sample_rate, int channels, int packet_size) {
		this.avp=avp;
		this.codec=codec;
		this.sample_rate=sample_rate;
		this.channels=channels;
		this.packet_size=packet_size;
	}

	/** The AVP code. */
	public int getAVP() {
		return avp;
	}

	/** The codec name. */
	public String getCodec() {
		return codec;
	}

	/** The sample rate of the stream. */
	public int getSampleRate() {
		return sample_rate;
	}
	
	/** The number of channels. */
	public int getChannels() {
		return channels;
	}

	/** The packet size. */
	public int getPacketSize() {
		return packet_size;
	}

	@Override
	public String toString() {
		StringBuilder sb=new StringBuilder();
		sb.append(avp);
		if (codec!=null) sb.append(" ").append(codec).append(" ").append(sample_rate).append(" ").append(packet_size).append(" ").append(channels);
		return sb.toString();
	}

	/**
	 * {@link CodecType} used.
	 */
	public CodecType getCodecType() {
		String codecName = getCodec();
		if (codecName == null) {
			codecName = AudioStreamer.DEFAULT_CODEC_NAME;
		}
		return CodecType.getByName(codecName.toUpperCase());
	}

	/** Parses a string and gets a new MediaSpec. */
	public static MediaSpec parseMediaSpec(String str) {
		Parser par=new Parser(str);
		int avp=par.getInt();
		String codec=(par.skipWSP().hasMore())? par.getString() : null;
		int sample_rate=(par.skipWSP().hasMore())? par.getInt() : 0;
		int packet_size=(par.skipWSP().hasMore())? par.getInt() : 0;
		int channels=(par.skipWSP().hasMore())? par.getInt() : 1;
		return new MediaSpec(avp, codec, sample_rate, channels, packet_size);
	}

}
