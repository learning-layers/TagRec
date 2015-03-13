package common;

import java.util.TimerTask;

public class MemoryThread extends TimerTask {

	public final static int TIME_SPAN = 5000;
	
	private long maxMemory = -1;
	
	public MemoryThread() {
		Runtime.getRuntime().gc();
	}
	
	private void trackMaxMemory() {
	    // Get the Java runtime
	    Runtime runtime = Runtime.getRuntime();
	    // Run the garbage collector
	    //runtime.gc();
	    // Calculate the used memory
	    long memory = runtime.totalMemory() - runtime.freeMemory();
	    if (memory > this.maxMemory) {
	    	this.maxMemory = memory;
	    }
	}
	
	public void run() {
		trackMaxMemory();
	}
	
	public long getMaxMemory() {
		trackMaxMemory();
		return this.maxMemory;
	}
}
