/*
 * Copyright (c) 2023 Bernhard Haumacher et al. All Rights Reserved.
 */
package org.mjsip.ua.streamer;

import java.util.concurrent.Executor;

import org.mjsip.media.FlowSpec;
import org.mjsip.media.MediaStreamer;
import org.mjsip.media.NativeMediaStreamer;
import org.zoolu.net.SocketAddress;

/**
 * {@link StreamerFactory} that creates streamers base on a native program for streaming.
 */
public class NativeStreamerFactory implements StreamerFactory {

	private final SocketAddress _mcastAddr;
	private final String _executable; 
	
	/** 
	 * Creates a {@link NativeStreamerFactory}.
	 */
	public NativeStreamerFactory(SocketAddress mcastAddr, String executable) {
		_mcastAddr = mcastAddr;
		_executable = executable;
	}

	@Override
	public MediaStreamer createMediaStreamer(Executor executor, FlowSpec flow_spec) {
		String remote_addr=(_mcastAddr!=null)? _mcastAddr.getAddress().toString() : flow_spec.getRemoteAddress();
		int remote_port=(_mcastAddr!=null)? _mcastAddr.getPort() : flow_spec.getRemotePort();
		int local_port=(_mcastAddr!=null)? _mcastAddr.getPort() : flow_spec.getLocalPort();
		String[] args=new String[]{(remote_addr+"/"+remote_port)};
		return new NativeMediaStreamer(_executable, args, local_port, remote_port);
	}

}
