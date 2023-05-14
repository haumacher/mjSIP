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
			@Override
			public void run() {
				SystemUtils.sleep(millisecs);
				System.exit(0);
			}
		}.start();
	}

}
