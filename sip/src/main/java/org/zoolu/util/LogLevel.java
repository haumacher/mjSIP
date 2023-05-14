package org.zoolu.util;



/** Log level.
 */
public class LogLevel {
	
	/** Level SEVERE, for very high priority logs (e.g. errors). */
	public static final LogLevel SEVERE=new LogLevel("SEVERE",100);

	/** Level WARNING, for high priority logs. */
	public static final LogLevel WARNING=new LogLevel("WARNING",80);

	/** Level INFO, for medium priority logs. */
	public static final LogLevel INFO=new LogLevel("INFO",60);  

	/** Level DEBUG, for low priority logs. */
	public static final LogLevel DEBUG=new LogLevel("DEBUG",40); 

	/** Level DEBUG, for very low priority logs. */
	public static final LogLevel TRACE=new LogLevel("TRACE",20); 

	/** Priority level OFF, for no logs. */
	public static final LogLevel OFF=new LogLevel("OFF",Integer.MAX_VALUE); 

	/** Priority level ALL, for all logs. */
	public static final LogLevel ALL=new LogLevel("ALL",Integer.MIN_VALUE); 

	
	/** Level name */
	String name;
	
	/** Level value */
	int value;

	
	/** Creates a new log level.
	 * @param name the level name
	 * @param value the level value */
	public LogLevel(String name, int value) {
		this.name=name;
		this.value=value;
	}

	/** Whether this object equals to an other object.
	 * @param obj the other object that is compared to
	 * @return true if the object is a LogLevel and the two level values are equal */
	public boolean equals(Object obj) {
		if (this==obj) return true;
		// else
		if (obj!=null && obj instanceof LogLevel) return value==((LogLevel)obj).getValue();
		// else
		return false;
	}

	/** Gets the level value.
	 * @return the level value */
	public int getValue() {
		return value;
	}

	/** Gets the level name.
	 * @return the level name */
	public String getName() {
		return name;
	}

	/** Gets a string representation of this object.
	 * @return the level name */
	public String toString() {
		return name;
	}

}
