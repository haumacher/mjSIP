package org.mjsip.media;




/** Interface for classes that start a media streamer (e.g. for audio or video fullduplex streaming). */
public interface MediaStreamer {
	
	/** Starts media streams. */
	public boolean start();

	/** Stops media streams. */
	public boolean halt();
		
}
