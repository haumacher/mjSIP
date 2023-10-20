/*
 * Copyright (c) 2023 Bernhard Haumacher et al. All Rights Reserved.
 */
package org.mjsip.ua;

import java.util.HashMap;
import java.util.Map;

import org.mjsip.media.MediaDesc;
import org.mjsip.media.MediaSpec;
import org.zoolu.util.Configure;
import org.zoolu.util.Flags;
import org.zoolu.util.Parser;

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
	
	/** Temporary mapping of media type to {@link MediaDesc}. */
	private Map<String, MediaDesc> _descByType=new HashMap<>();

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
		if (attribute.equals("media") || attribute.equals("media_desc"))    {
			MediaDesc desc = MediaDesc.parseMediaDesc(par.getRemainingString().trim());
			_descByType.put(desc.getMedia(), desc);  
		}
	}
	
	private void normalize(UAConfig uaConfig) {
		// BEGIN PATCH FOR JMF SUPPORT
		if (uaConfig.audio && uaConfig.useJmfAudio) {
			MediaDesc desc = _descByType.remove("audio");
			_descByType.put("audio", desc.withSpecs(new MediaSpec[] {new MediaSpec(11,"L16",16000,1,320)}));
		}
		else
		if (uaConfig.video && uaConfig.useJmfVideo) {
			MediaDesc desc = _descByType.remove("video");
			_descByType.put("video",desc.withSpecs(new MediaSpec[] {new MediaSpec(101,null,-1,1,-1)}));
		}
		// END PATCH FOR JMF SUPPORT
		
		// media descriptions
		if (_descByType.size()==0 && uaConfig.audio) {
			// add default auido support
			_descByType.put("audio",MediaDesc.parseMediaDesc("audio 4080 RTP/AVP { 0 PCMU 8000 160, 8 PCMA 8000 160 }"));
		}
		
		int i = 0;
		mediaDescs=new MediaDesc[_descByType.size()];
		for (MediaDesc md : _descByType.values()) {
			mediaDescs[i++]=new MediaDesc(md.getMedia(),md.getPort(),md.getTransport(),md.getMediaSpecs());
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
			MediaSpec[] specs = descriptor.getMediaSpecs();
			result[n] = descriptor.withSpecs(specs);
		}
		return result;
	}	

}
