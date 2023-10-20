/*
 * Copyright (c) 2023 Bernhard Haumacher et al. All Rights Reserved.
 */
package org.mjsip.ua;

import java.util.Vector;

import org.mjsip.media.MediaDesc;
import org.mjsip.media.MediaSpec;
import org.zoolu.util.Configure;
import org.zoolu.util.Flags;
import org.zoolu.util.MultiTable;
import org.zoolu.util.Parser;
import org.zoolu.util.VectorUtils;

/**
 * Definition of static media files to serve.
 */
public class MediaConfig extends Configure {

	/** 
	 * Constructs a {@link UAConfig} from the given configuration file and program arguments.
	 */
	public static MediaConfig init(String file, Flags flags, UAConfig uaConfig) {
		MediaConfig result=new MediaConfig();
		result.loadFile(file);
		result.normalize(uaConfig);
		return result;
	}

	/** Array of media descriptions */
	public MediaDesc[] mediaDescs=new MediaDesc[]{};
	/** Vector of media descriptions */
	private Vector<MediaDesc> mediaDescVector=new Vector<>();
	/** Table of media specifications, as multiple-values table of (String)media-->(MediaSpec)media_spec */
	private MultiTable<String,MediaSpec> mediaSpecTable=new MultiTable<>();

	/**
	 * Sets the transport port for each medium.
	 * 
	 * @param portPool
	 *        The pool to take free ports from.
	 */
	public void allocateMediaPorts(PortPool portPool) {
		for (int i=0; i<mediaDescs.length; i++) {
			MediaDesc md=mediaDescs[i];
			md.setPort(portPool.allocate());
		}
	}

	/**
	 * Releases ports previously allocated using {@link #allocateMediaPorts(PortPool)}.
	 * 
	 * @param portPool The pool to put ports back to.
	 */
	public void releaseMediaPorts(PortPool portPool) {
		for (int i=0; i<mediaDescs.length; i++) {
			MediaDesc md=mediaDescs[i];
			portPool.release(md.getPort());
			md.setPort(0);
		}
	}
	
	@Override
	public void setOption(String attribute, Parser par) {
		if (attribute.equals("media") ||
				 attribute.equals("media_desc"))    {  mediaDescVector.addElement(MediaDesc.parseMediaDesc(par.getRemainingString().trim()));  return;  }
		if (attribute.equals("media_spec"))     {  MediaSpec ms=MediaSpec.parseMediaSpec(par.getRemainingString().trim());  mediaSpecTable.put(ms.getType(),ms);  return;  }
	}
	
	private void normalize(UAConfig uaConfig) {
		// BEGIN PATCH FOR JMF SUPPORT
		if (uaConfig.audio && uaConfig.useJmfAudio) {
			mediaSpecTable.remove("audio");
			mediaSpecTable.put("audio",new MediaSpec("audio",11,"L16",16000,1,320));
		}
		else
		if (uaConfig.video && uaConfig.useJmfVideo) {
			mediaSpecTable.remove("video");
			mediaSpecTable.put("video",new MediaSpec("video",101,null,-1,1,-1));
		}
		// END PATCH FOR JMF SUPPORT
		
		// media descriptions
		if (mediaDescVector.size()==0 && uaConfig.audio) {
			// add default auido support
			mediaDescVector.addElement(MediaDesc.parseMediaDesc("audio 4080 RTP/AVP { audio 0 PCMU 8000 160, audio 8 PCMA 8000 160 }"));
		}
		mediaDescs=new MediaDesc[mediaDescVector.size()];
		for (int i=0; i<mediaDescVector.size(); i++) {
			MediaDesc md=mediaDescVector.elementAt(i);
			Vector<MediaSpec> media_spec_vector=new Vector<MediaSpec>();
			MediaSpec[] ms_array=md.getMediaSpecs();
			if (ms_array.length>0) {
				//media_spec_vector.addAll(Arrays.asList(ms_array));
				VectorUtils.addArray(media_spec_vector,ms_array);
			}
			Vector<MediaSpec> ms_vector=mediaSpecTable.get(md.getMedia());
			if (ms_vector!=null) {
				//media_spec_vector.addAll(ms_vector);
				VectorUtils.addVector(media_spec_vector,ms_vector);
			}
			//MediaSpec[] media_specs=(MediaSpec[])media_spec_vector.toArray(new MediaSpec[]{});
			MediaSpec[] media_specs=(MediaSpec[])VectorUtils.vectorToArray(media_spec_vector,new MediaSpec[media_spec_vector.size()]);
			mediaDescs[i]=new MediaDesc(md.getMedia(),md.getPort(),md.getTransport(),media_specs);
		}
	}

	/** 
	 * Creates a {@link MediaConfig} from existing descriptors.
	 */
	public static MediaConfig from(MediaDesc[] media_descs) {
		MediaConfig result = new MediaConfig();
		result.mediaDescs = copyDescriptory(media_descs);
		return result;
	}

	private static MediaDesc[] copyDescriptory(MediaDesc[] descriptors) {
		MediaDesc[] result = new MediaDesc[descriptors.length];
		for (int n = 0, cnt = descriptors.length; n < cnt; n++) {
			MediaDesc descriptor = descriptors[n];
			result[n] = new MediaDesc(descriptor.getMedia(), descriptor.getPort(), descriptor.getTransport(), descriptor.getMediaSpecs());
		}
		return result;
	}	

}
