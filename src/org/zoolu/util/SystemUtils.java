package org.zoolu.util;



/** Class that collects various system-level methods and objects.
 */
public class SystemUtils {
	
	  
	/** Causes the current thread to sleep for the specified number of milliseconds.
	 * Differently from method {@link Thread#sleep(long)}, it never throws an {@link InterruptedException}.
	 * @param millisecs the length of time to sleep in milliseconds */
	public static void sleep(long millisecs) {
		try {  Thread.sleep(millisecs);  } catch (InterruptedException e) {}
	}  
	
	
	/** Exits after a given time.
	 * <p> Note that this method is not blocking.
	 * @param millisecs the length of time before exiting, in milliseconds */
	public static void exitAfter(final long millisecs)  {
		new Thread() {
			public void run() {
				SystemUtils.sleep(millisecs);
				System.exit(0);
			}
		}.start();
	}

	
	/** Gets the simple name of a class.
	 * This method is equivalent to the method getSimpleName() of class {@link java.lang.Class} introduced in Java 1.5.
	 * <p>
	 * It is provided for backward compatibility with Java 1.4 and JavaME.  
	 * @param class_name the class name
	 * @return only the simple name of the class, without package name */
	public static String getClassSimpleName(String class_name) {
		String[] names=class_name.split("\\x2E");
		return names[names.length-1];
	}

	
	/** Default system logger */
	protected static Logger DEFAULT_LOGGER=null;

	
	/** Sets the default system logger.
	 * @param default_logger the default logger */
	public static void setDefaultLogger(Logger default_logger)  {
		DEFAULT_LOGGER=default_logger;
	}

	
	/** Gets the default system logger.
	 * @return the default logger */
	public static Logger getDefaultLogger()  {
		return DEFAULT_LOGGER;
	}

}
