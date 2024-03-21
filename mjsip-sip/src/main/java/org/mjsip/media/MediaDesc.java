package org.mjsip.media;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
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
	private final String _mediaType;

	/** Port */
	private int _port;

	/** Transport */
	private final String _transport;
	
	/** Vector of media specifications (as Vector<MediaSpec>) */
	private final MediaSpec[] _specs;

	/** Creates a new MediaDesc.
	  * @param media media type
	  * @param port port
	  * @param transport transport protocol
	  * @param specs array of media specifications */
	public MediaDesc(String media, int port, String transport, MediaSpec[] specs) {
		_mediaType = media;
		_port = port;
		_transport = transport;
		_specs = specs;
	}

	/**
	 * Creates a copy of this description.
	 * 
	 * <p>
	 * The copy is not deep. The final {@link MediaSpec}s are reused.
	 * </p>
	 */
	public MediaDesc copy() {
		return new MediaDesc(_mediaType, _port, _transport, _specs);
	}

	/**
	 * The media type (e.g. audio, video, message, etc.).
	 */
	public String getMediaType() {
		return _mediaType;
	}

	/**
	 * The port of the stream.
	 */
	public int getPort() {
		return _port;
	}

	/** @see #getPort() */
	public void setPort(int port) {
		this._port = port;
	}

	/** The transport protocol. */
	public String getTransport() {
		return _transport;
	}

	/** The supported media encodings. */
	public MediaSpec[] getMediaSpecs() {
		return _specs;
	}

	/** Gets the corresponding {@link MediaDescriptor} for creating SIP messages. */
	public MediaDescriptor toMediaDescriptor() {
		List<String> formats = new ArrayList<>();
		List<AttributeField> attributes = new ArrayList<>();
		if (_specs != null) {
			for (MediaSpec ms : _specs) {
				String avp = String.valueOf(ms.getAVP());
				formats.add(avp);

				StringBuilder rtpmap = new StringBuilder();
				rtpmap.append(avp);

				String codec = ms.getCodec();
				int sample_rate = ms.getSampleRate();
				if (codec != null && sample_rate > 0) {
					rtpmap.append(' ');
					rtpmap.append(codec);
					rtpmap.append('/');
					rtpmap.append(sample_rate);

					int channels = ms.getChannels();
					if (channels > 1) {
						rtpmap.append('/');
						rtpmap.append(channels);
					}
				}
				attributes.add(new AttributeField("rtpmap", rtpmap.toString()));
			}
		}
		MediaField mediaField = new MediaField(_mediaType, _port, 0, _transport, formats);
		return new MediaDescriptor(mediaField, null, attributes);
	}

	/** Gets a string representation of this object.
	  * @return a string representing this object */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(_mediaType).append(" ").append(_port).append(" ").append(_transport);
		if (_specs != null) {
			sb.append(" {");
			for (int i = 0; i < _specs.length; i++) {
				if (i>0) sb.append(",");
				sb.append(" ").append(_specs[i].toString());
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
		List<MediaSpec> specList = new ArrayList<>();
		if (par.goTo("{").hasMore()) {
			par.skipChar();
			int len=par.indexOf("}")-par.getPos();
			if (len>0) {
				par=new Parser(par.getString(len));
				char[] delim={ ';', ',' };
				while (par.skipWSP().hasMore()) {
					source = par.getWord(delim);
					if (source != null && !source.isEmpty())
						specList.add(MediaSpec.parseMediaSpec(source));
				}
			}
		}
		MediaSpec[] specs = specList.toArray(new MediaSpec[] {});
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
		MediaField mf = descriptor.getMediaField();
		String mediaType = mf.getMediaType();
		int port = mf.getPort();
		String transport = mf.getTransport();

		AttributeField[] rtpmap = descriptor.getAttributes("rtpmap");
		List<MediaSpec> specs = new ArrayList<>(rtpmap.length);

		List<String> avps = mf.getFormatList();
		for (String avp : avps) {
			MediaSpec spec = MediaSpec.getWellKnown(Integer.parseInt(avp));
			if (spec != null) {
				specs.add(spec);
			}
		}

		for (AttributeField field : rtpmap) {
			specs.add(parseMediaSpec(field.getAttributeValue()));
		}
		return new MediaDesc(mediaType, port, transport, specs.toArray(new MediaSpec[] {}));
	}

	private static MediaSpec parseMediaSpec(String source) {
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
		return new MediaDesc(getMediaType(), getPort(), getTransport(), newSpecs);
	}

	/**
	 * Creates a copy of the given descriptor array.
	 * 
	 * @see #copy()
	 */
	public static MediaDesc[] copy(MediaDesc[] media) {
		MediaDesc[] copy = new MediaDesc[media.length];
		for (int n = 0, cnt = media.length; n < cnt; n++) {
			copy[n] = media[n].copy();
		}
		return copy;
	}

	/**
	 * Retrieves the {@link MediaDesc} with the given media type from the given list of descriptors.
	 */
	public static MediaDesc findMedia(String mediaType, MediaDesc[] media_descs) {
		for (MediaDesc media : media_descs) {
			if (mediaType.equals(media.getMediaType())) {
				return media;
			}
		}
		return null;
	}

}
