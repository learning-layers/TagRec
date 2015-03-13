package common;

public class PerformanceMeasurement {

	private static final long MEGABYTE = 1024L * 1024L;
	
	public static String addTimeMeasurement(String performance, boolean clearString, long trainingTime, long testTime, int sampleSize) {
		if (clearString) {
			performance = "";
		}
		performance += ("Full training time: " + trainingTime + "\n");
		performance += ("Full test time: " + testTime + "\n");
		performance += ("Average test time: " + (testTime / (double)sampleSize)) + "\n";
		performance += ("Total time: " + (trainingTime + testTime) + "\n");
		return performance;
	}
	
	public static String addMemoryMeasurement(String performance, boolean clearString, long memory) {
		if (clearString) {
			performance = "";
		}
		performance += ("Memory in bytes: " + memory + "\n");
		performance += ("Memory in mBytes: " + bytesToMegabytes(memory) + "\n");
		return performance;
	}
	
	public static String addCurrentMemoryMeasurement(String performance, boolean clearString, boolean gc) {
		if (clearString) {
			performance = "";
		}
	    // Get the Java runtime
	    Runtime runtime = Runtime.getRuntime();
	    if (gc) {
	    	// Run the garbage collector
	    	runtime.gc();
	    }
	    // Calculate the used memory
	    long memory = runtime.totalMemory() - runtime.freeMemory();
		performance += ("Memory in bytes: " + memory + "\n");
		performance += ("Memory in mBytes: " + bytesToMegabytes(memory) + "\n");
		return performance;
	}
	
	private static long bytesToMegabytes(long bytes) {
		return bytes / MEGABYTE;
	}
}
