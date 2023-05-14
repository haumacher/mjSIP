/*
 * Copyright (C) 2012 Luca Veltri - University of Parma - Italy
 * 
 * This source code is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This source code is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this source code; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 * 
 * Author(s):
 * Luca Veltri (luca.veltri@unipr.it)
 */

package org.mjsip.media;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.zoolu.util.ByteUtils;


/** Reads and wirtes AU file header.
  */
public class AuFileHeader {
	
	/** Magic number (four ASCII characters ".snd") */
	static final long MAGIC_NUMBER=0x2e736e64;

	/** AU header length in bytes (i.e. data offset) */
	int hdr_len=24; // basic header

	/** Data size in bytes (0xffffffff = unknown) */
	long data_size=0xffffffff; // unknown
	
	/** Data encoding format */
	int encoding_format=1; // G711 PCM ULAW
	
	/** Number of samples per second */
	int sample_rate=8000; // 8000 Hz
	
	/** Number of interleaved channels, e.g., 1 for mono, 2 for stereo */
	int channels=1; // mono
	

	
	/** Creates a new AuFileHeader.
	  * @param encoding_format data encoding format
	  * @param sample_rate number of samples per second */
	public AuFileHeader(int encoding_format, int sample_rate) {
		this.encoding_format=encoding_format;
		this.sample_rate=sample_rate;
	}


	/** Creates a new AuFileHeader.
	  * @param encoding_format data encoding format
	  * @param sample_rate number of samples per second
	  * @param channels number of interleaved channels, e.g., 1 for mono, 2 for stereo
	  * @param data_size data size in bytes (-1 = unknown) */
	public AuFileHeader(int encoding_format, int sample_rate, int channels, long data_size) {
		this.encoding_format=encoding_format;
		this.sample_rate=sample_rate;
		this.channels=channels;
		this.data_size=(data_size==-1)? 0xffffffff : data_size;
	}


	/** Creates a new AuFileHeader.
	  * @param is the InputStream where the AU header is read form */
	public AuFileHeader(InputStream is) throws IOException {
		byte[] word=new byte[4];
		is.read(word);
		if (ByteUtils.fourBytesToInt(word)!=MAGIC_NUMBER) throw new IOException("AU starting magic number not found.");
		// else
		is.read(word); // header length
		hdr_len=(int)ByteUtils.fourBytesToInt(word);
		is.read(word); // data size
		data_size=ByteUtils.fourBytesToInt(word);
		is.read(word); // encoding format
		encoding_format=(int)ByteUtils.fourBytesToInt(word);
		is.read(word); // sample rate
		sample_rate=(int)ByteUtils.fourBytesToInt(word);
		is.read(word); // channels
		channels=(int)ByteUtils.fourBytesToInt(word);
		is.skip(hdr_len-24);
	}

	/** Writes the AuFileHeader.
	  * @param os the OutputStream where the AU header is written to */
	public void writeTo(OutputStream os) throws IOException {
		os.write(ByteUtils.intToFourBytes(MAGIC_NUMBER));
		os.write(ByteUtils.intToFourBytes(hdr_len=24));
		os.write(ByteUtils.intToFourBytes(data_size));
		os.write(ByteUtils.intToFourBytes(encoding_format));
		os.write(ByteUtils.intToFourBytes(sample_rate));
		os.write(ByteUtils.intToFourBytes(channels));
	}
	
	/** Gets data size.
	  * @return data size in bytes (-1 = unknown) */
	public long getDataSize() {
		return (data_size==0xffffffff)? -1 : data_size;
	}

	/** Gets encoding format.
	  * @return data encoding format */
	public int getEncodingFormat() {
		return encoding_format;
	}

	/** Gets sample rate.
	  * @return number of samples per second */
	public int getSampleRate() {
		return sample_rate;
	}

	/** Gets channels.
	  * @return number of interleaved channels, e.g., 1 for mono, 2 for stereo */
	public int getChannels() {
		return channels;
	}

}
