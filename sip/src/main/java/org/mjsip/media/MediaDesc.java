package org.mjsip.media;



import java.util.Vector;

import org.mjsip.sdp.AttributeField;
import org.mjsip.sdp.MediaDescriptor;
import org.mjsip.sdp.field.MediaField;
import org.zoolu.util.Parser;



/** Media description.
  */
public class MediaDesc {
	

	/** Media type (e.g. audio, video, message, etc.) */
	String media;

	/** Port */
	int port;

	/** Transport */
	String transport;
	
	/** Vector of media specifications (as Vector<MediaSpec>) */
	MediaSpec[] specs;


	
	/** Creates a new MediaDesc.
	  * @param media Media type
	  * @param port Port
	  * @param transport Transport protocol */
	/*public MediaDesc(String media, int port, String transport) {
		init(media,port,transport,specs);
	}*/


	/** Creates a new MediaDesc.
	  * @param media media type
	  * @param port port
	  * @param transport transport protocol
	  * @param specs array of media specifications */
	public MediaDesc(String media, int port, String transport, MediaSpec[] specs) {
		init(media,port,transport,specs);
	}


	/** Creates a new MediaDesc.
	  * @param md media descriptor (as {@link org.mjsip.sdp.MediaDescriptor}) */
	public MediaDesc(MediaDescriptor md) {
		MediaField mf=md.getMedia();
		String media=mf.getMedia();
		int port=mf.getPort();
		String transport=mf.getTransport();

		AttributeField[] attributes=md.getAttributes("rtpmap");
		Vector spec_vector=new Vector(attributes.length);
		for (int i=0; i<attributes.length; i++) {
			Parser par=new Parser((attributes[i]).getAttributeValue());
			int avp=par.getInt();
			String codec=null;
			int sample_rate=0;
			int channels=1;
			if (par.skipChar().hasMore()) {
				char[] delim={'/'};
				codec=par.getWord(delim);
				sample_rate=Integer.parseInt(par.skipChar().getWord(delim));
				if (par.hasMore()) {
					channels=Integer.parseInt(par.skipChar().getWord(delim));
				}
			}
			spec_vector.addElement(new MediaSpec(media,avp,codec,sample_rate,channels,0));
		}
		specs=(MediaSpec[])spec_vector.toArray(new MediaSpec[]{});
		init(media,port,transport,specs);
	}


	/** Inits the MediaDesc. */
	private void init(String media, int port, String transport, MediaSpec[] specs) {
		this.media=media;
		this.port=port;
		this.transport=transport;
		this.specs=specs;
	}


	/** Gets media type.
	  * @return the media type */
	public String getMedia() {
		return media;
	}


	/** Gets port.
	  * @return the transport port */
	public int getPort() {
		return port;
	}


	/** Sets port.
	  * @param port the transport port */
	public void setPort(int port) {
		this.port=port;
	}


	/** Gets transport protocol.
	  * @return the transport protocol */
	public String getTransport() {
		return transport;
	}


	/** Gets media specifications.
	  * @return array of media specifications */
	public MediaSpec[] getMediaSpecs() {
		return specs;
	}


	/** Sets media specifications.
	  * @param media_specs array of media specifications */
	/*public void setMediaSpecs(MediaSpec[] media_specs) {
		this.specs=media_specs;
	}*/


	/** Adds a new media specification.
	  * @param media_spec media specifications */
	/*public void addMediaSpec(MediaSpec media_spec) {
		if (specs==null) specs=new Vector();
		specs.addElement(media_spec);
	}*/


	/** Gets the corresponding MediaDescriptor.
	  * @return media description (as {@link org.mjsip.sdp.MediaDescriptor}) of this MediaDesc */
	public MediaDescriptor toMediaDescriptor() {
		Vector formats=new Vector();
		Vector av=new Vector();
		if (specs!=null) {
			for (int i=0; i<specs.length; i++) {
				MediaSpec ms=specs[i];
				int avp=ms.getAVP();
				String codec=ms.getCodec();
				int sample_rate=ms.getSampleRate();
				int channels=ms.getChannels();
				formats.addElement(String.valueOf(avp));
				av.addElement(new AttributeField("rtpmap",String.valueOf(avp)+((codec!=null && sample_rate>0)? " "+codec+"/"+sample_rate+(channels>1? "/"+channels : "") : "")));
			}
		}
		return new MediaDescriptor(new MediaField(media,port,0,transport,formats),null,(AttributeField[])av.toArray(new AttributeField[]{}));
	}


	/** Gets a string representation of this object.
	  * @return a string representing this object */
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


	/** Gets a String compact representation of this object (only media, port, and transport are included). */
	/*public String toStringCompact() {
		StringBuffer sb=new StringBuffer();
		sb.append(media).append(" ").append(port).append(" ").append(transport);
		return sb.toString();
	}*/


	/** Parses a String and gets a new MediaDesc.
	  * @param str a string containing the representation of a MediaDesc */
	public static MediaDesc parseMediaDesc(String str) {
		//System.out.println("MediaDesc: parsing: "+str);
		Parser par=new Parser(str);
		String media=par.getString();
		int port=par.getInt();
		String transport=par.getString();
		Vector spec_vector=new Vector();
		if (par.goTo("{").hasMore()) {
			par.skipChar();
			int len=par.indexOf("}")-par.getPos();
			if (len>0) {
				par=new Parser(par.getString(len));
				char[] delim={ ';', ',' };
				while (par.skipWSP().hasMore()) {
					str=par.getWord(delim);
					if (str!=null && str.length()>0) spec_vector.addElement(MediaSpec.parseMediaSpec(str));
				}
			}
		}
		MediaSpec[] specs=(MediaSpec[])spec_vector.toArray(new MediaSpec[]{});
		return new MediaDesc(media,port,transport,specs);
	}

}
