package org.zoolu.sound;




/** Audio codec.
  * This class exhibits unambiguous codec names.
  * <p>
  * For each codec, the default AVP payload type, frame size, and number of samples per frame
  * are also provided.
  */
public class CodecType {
	
	/** PCM linear */
	public static final CodecType PCM_LINEAR=new CodecType("PCM_LINEAR",96,2,1);

	/** G711 (PCM) u-law */
	public static final CodecType G711_ULAW=new CodecType("G711_ULAW",0,1,1);
	
	/** G711 (PCM) A-law */
	public static final CodecType G711_ALAW=new CodecType("G711_ALAW",8,1,1);
  
	/** G726_24 */
	public static final CodecType G726_24=new CodecType("G726_24",101,3,8);

	/** G726_32 */
	public static final CodecType G726_32=new CodecType("G726_32",101,4,8);

	/** G726_40 */
	public static final CodecType G726_40=new CodecType("G726_40",101,5,8);

	/** GSM0610 */
	public static final CodecType GSM0610=new CodecType("GSM0610",3,33,160); // = 50 frames/sec in case of sample rate = 8000 Hz

	/** AMR narrow band (AMR-NB), from 4.75 to 12.2 kbps (4.75 kbps, 5.15 kbps, 5.9 kbps, 6.7 kbps, 7.4 kbps, 7.95 kbps, 10.2 kbps, or 12.2 kbps ) */
	public static final CodecType AMR_NB=new CodecType("AMR_NB",-1,-1,160);

	/** AMR 4.75 kbps */
	public static final CodecType AMR_0475=new CodecType("AMR_0475",96,13,160);

	/** AMR 5.15 kbps */
	public static final CodecType AMR_0515=new CodecType("AMR_0515",96,14,160);

	/** AMR 5.9 kbps */
	public static final CodecType AMR_0590=new CodecType("AMR_0590",96,16,160);

	/** AMR 6.7 kbps */
	public static final CodecType AMR_0670=new CodecType("AMR_0670",96,18,160);

	/** AMR 7.4 kbps */
	public static final CodecType AMR_0740=new CodecType("AMR_0740",96,20,160);

	/** AMR 7.95 kbps */
	public static final CodecType AMR_0795=new CodecType("AMR_0795",96,21,160);

	/** AMR 10.2 kbps */
	public static final CodecType AMR_1020=new CodecType("AMR_1020",96,27,160);

	/** AMR 12.2 kbps */
	public static final CodecType AMR_1220=new CodecType("AMR_1220",96,32,160);

	/** Clear mode */
	public static final CodecType Clear_Mode=new CodecType("Clear_Mode",96,1,1);



	/** Name */
	String name;

	/** Default AudioFormat.Encoding */
	//AudioFormat.Encoding encoding;

	/** Default AVP payload type */
	int payload_type;

	/** Frame size in bytes */
	int frame_size;

	/** Number of samples per frame */
	int samples;



	/** Creates a new Codec. */
	protected CodecType(String name/*, AudioFormat.Encoding encoding*/, int payload_type, int frame_size, int samples) {
		this.name=name;
		//this.encoding=encoding;
		this.payload_type=payload_type;
		this.frame_size=frame_size;
		this.samples=samples;
	}



	/** Gets codec name. */
	public String getName() {
		return name;
	}

	/** Gets AVP payload type. */
	public int getPayloadType() {
		return payload_type;
	}

	/** Gets the frame size in bytes. */
	public int getFrameSize() {
		return frame_size;
	}

	/** Gets the number of samples per frame. */
	public int getSamplesPerFrame() {
		return samples;
	}

	/** Gets a String representation of this object. */
	public String toString() {
		return getName();
	}


	
	/** Gets Codec from name. */
	public static CodecType getByName(String name) {
		if (name.equalsIgnoreCase("PCM_SIGNED") || name.equalsIgnoreCase("PCM_LINEAR") || name.equalsIgnoreCase("linear")) return PCM_LINEAR;
		else
		if (name.equalsIgnoreCase("PCMU") || name.equalsIgnoreCase("ULAW") || name.equalsIgnoreCase("PCM_ULAW") || name.equalsIgnoreCase("G711_ULAW") || name.equalsIgnoreCase("PCM-ulaw") || name.equalsIgnoreCase("G711-ulaw")) return G711_ULAW;
		else
		if (name.equalsIgnoreCase("PCMA") || name.equalsIgnoreCase("ALAW") || name.equalsIgnoreCase("PCM_ALAW") || name.equalsIgnoreCase("G711_ALAW") || name.equalsIgnoreCase("PCM-alaw") || name.equalsIgnoreCase("G711-alaw")) return G711_ALAW;
		else
		if (name.equalsIgnoreCase("G726_24") || name.equalsIgnoreCase("G726-24")) return G726_24;
		else
		if (name.equalsIgnoreCase("G726_32") || name.equalsIgnoreCase("G726-32")) return G726_32;
		else
		if (name.equalsIgnoreCase("G726_40") || name.equalsIgnoreCase("G726-40")) return G726_40;
		else
		if (name.equalsIgnoreCase("GSM0610") || name.equalsIgnoreCase("GSM")) return GSM0610;
		else
		if (name.equalsIgnoreCase("AMR_NB") || name.equalsIgnoreCase("AMR-NB")) return AMR_NB;
		else
		if (name.equalsIgnoreCase("AMR") || name.equalsIgnoreCase("AMR_0475") || name.equalsIgnoreCase("AMR-4.75")) return AMR_0475;
		else
		if (name.equalsIgnoreCase("AMR_0515") || name.equalsIgnoreCase("AMR-5.15")) return AMR_0515;
		else
		if (name.equalsIgnoreCase("AMR_0590") || name.equalsIgnoreCase("AMR-5.9")) return AMR_0590;
		else
		if (name.equalsIgnoreCase("AMR_0670") || name.equalsIgnoreCase("AMR-6.7")) return AMR_0670;
		else
		if (name.equalsIgnoreCase("AMR_0740") || name.equalsIgnoreCase("AMR-7.4")) return AMR_0740;
		else
		if (name.equalsIgnoreCase("AMR_0795") || name.equalsIgnoreCase("AMR-7.95")) return AMR_0795;
		else
		if (name.equalsIgnoreCase("AMR_1020") || name.equalsIgnoreCase("AMR-10.2")) return AMR_1020;
		else
		if (name.equalsIgnoreCase("AMR_1220") || name.equalsIgnoreCase("AMR-12.2")) return AMR_1220;
		else
		if (name.equalsIgnoreCase("Clear_Mode") || name.equalsIgnoreCase("Clear")) return Clear_Mode;
		// else
		return null;
	}

	/** Whether it is equals to the given object. */
	public boolean equals(Object obj) {
		try {  return ((CodecType)obj).name.equals(name);  }  catch (Exception e)  {  return false;  }
	}

}
