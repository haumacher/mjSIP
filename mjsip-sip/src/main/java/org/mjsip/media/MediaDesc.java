package org.mjsip.media;

import java.util.Collection;
import java.util.Vector;

import org.mjsip.sdp.AttributeField;
import org.mjsip.sdp.MediaDescriptor;
import org.mjsip.sdp.SdpMessage;
import org.mjsip.sdp.field.MediaField;
import org.zoolu.util.Parser;

/**
 * RTP media stream description.
 */
public class MediaDesc {

	private static final MediaDesc[] NO_MEDIA = new MediaDesc[]{};

	private static final char[] DELIMITER = { '/' };

	/** Media type (e.g. audio, video, message, etc.) */
	private final String media;

	/** Port */
	private int port;

	/** Transport */
	private final String transport;
	
	/** Vector of media specifications (as Vector<MediaSpec>) */
	private final MediaSpec[] specs;

	/** Creates a new MediaDesc.
	  * @param media media type
	  * @param port port
	  * @param transport transport protocol
	  * @param specs array of media specifications */
	public MediaDesc(String media, int port, String transport, MediaSpec[] specs) {
		this.media=media;
		this.port=port;
		this.transport=transport;
		this.specs=specs;
	}

	/**
	 * The media type (e.g. audio, video, message, etc.).
	 */
	public String getMedia() {
		return media;
	}

	/**
	 * The port of the stream.
	 */
	public int getPort() {
		return port;
	}

	/** @see #getPort() */
	public void setPort(int port) {
		this.port=port;
	}

	/** The transport protocol. */
	public String getTransport() {
		return transport;
	}

	/** The supported media encodings. */
	public MediaSpec[] getMediaSpecs() {
		return specs;
	}

	/** Gets the corresponding {@link MediaDescriptor} for creating SIP messages. */
	public MediaDescriptor toMediaDescriptor() {
		Vector<String> formats = new Vector<>();
		Vector<AttributeField> attributes = new Vector<>();
		if (specs!=null) {
			for (int i=0; i<specs.length; i++) {
				MediaSpec ms=specs[i];
				int avp=ms.getAVP();
				String codec=ms.getCodec();
				int sample_rate=ms.getSampleRate();
				int channels=ms.getChannels();
				String avpString = String.valueOf(avp);
				formats.addElement(avpString);
				attributes.addElement(new AttributeField("rtpmap",
						avpString + ((codec != null && sample_rate > 0)
								? " " + codec + "/" + sample_rate + (channels > 1 ? "/" + channels : "")
								: "")));
			}
		}
		MediaField mediaField = new MediaField(media, port, 0, transport, formats);
		return new MediaDescriptor(mediaField, null, attributes.toArray(new AttributeField[] {}));
	}

	/** Gets a string representation of this object.
	  * @return a string representing this object */
	@Override
	public String toString() {
		StringBuffer sb=new StringBuffer();
		sb.append(media).append(" ").append(port).append(" ").append(transport);
		if (specs!=null) {
			sb.append(" {");
			for (int i=0; i<specs.length; i++) {
				if (i>0) sb.append(",");
				sb.append(" ").append(((MediaSpec)specs[i]).toString());
			}
			sb.append(" }");
		}
		return sb.toString();
	}

	/**
	 * Parses a configuration string into a {@link MediaDesc}.
	 * 
	 * @param source
	 *        Representation of a {@link MediaDesc} in a configuration file.
	 */
	public static MediaDesc parseMediaDesc(String source) {
		Parser par = new Parser(source);
		String media=par.getString();
		int port=par.getInt();
		String transport=par.getString();
		Vector<MediaSpec> spec_vector = new Vector<>();
		if (par.goTo("{").hasMore()) {
			par.skipChar();
			int len=par.indexOf("}")-par.getPos();
			if (len>0) {
				par=new Parser(par.getString(len));
				char[] delim={ ';', ',' };
				while (par.skipWSP().hasMore()) {
					source = par.getWord(delim);
					if (source != null && source.length() > 0)
						spec_vector.addElement(MediaSpec.parseMediaSpec(source));
				}
			}
		}
		MediaSpec[] specs = spec_vector.toArray(new MediaSpec[] {});
		return new MediaDesc(media,port,transport,specs);
	}

	/**
	 * Parses all media descriptors from the given {@link SdpMessage}.
	 */
	public static MediaDesc[] parseSdpDescriptors(String sdp) {
		if (sdp == null) {
			return NO_MEDIA;
		}
		return parseDescriptors(new SdpMessage(sdp).getMediaDescriptors());
	}

	/**
	 * Parses a collection of raw {@link MediaDescriptor} into a {@link MediaDesc}s internally used
	 * by mjSIP.
	 */
	public static MediaDesc[] parseDescriptors(Collection<MediaDescriptor> descriptors) {
		int i = 0;
		MediaDesc[] result = new MediaDesc[descriptors.size()];
		for (MediaDescriptor descriptor : descriptors) {
			result[i++] = parseDescriptor(descriptor);
		}
		return result;
	}

	/**
	 * Parses a raw {@link MediaDescriptor} into a {@link MediaDesc} internally used by mjSIP.
	 */
	public static MediaDesc parseDescriptor(MediaDescriptor descriptor) {
		MediaField mf = descriptor.getMedia();
		String media = mf.getMedia();
		int port = mf.getPort();
		String transport = mf.getTransport();

		AttributeField[] rtpmap = descriptor.getAttributes("rtpmap");
		Vector<MediaSpec> specs = new Vector<>(rtpmap.length);
		for (AttributeField field : rtpmap) {
			specs.addElement(parseMediaSpec(media, field.getAttributeValue()));
		}
		return new MediaDesc(media, port, transport, specs.toArray(new MediaSpec[] {}));
	}

	private static MediaSpec parseMediaSpec(String media, String source) {
		Parser parser = new Parser(source);

		int avp = parser.getInt();

		String codec = null;
		int sample_rate = 0;
		int channels = 1;
		if (parser.skipChar().hasMore()) {
			codec = parser.getWord(DELIMITER);
			sample_rate = Integer.parseInt(parser.skipChar().getWord(DELIMITER));
			if (parser.hasMore()) {
				channels = Integer.parseInt(parser.skipChar().getWord(DELIMITER));
			}
		}
		return new MediaSpec(avp, codec, sample_rate, channels, 0);
	}

	/**
	 * A copy of this {@link MediaDesc} with alternative {@link #getMediaSpecs()}.
	 */
	public MediaDesc withSpecs(MediaSpec[] newSpecs) {
		return new MediaDesc(getMedia(), getPort(), getTransport(), newSpecs);
	}

}
