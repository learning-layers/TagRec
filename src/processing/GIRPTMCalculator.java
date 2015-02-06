package processing;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import com.google.common.base.Stopwatch;
import com.google.common.primitives.Ints;

import common.DoubleMapComparator;
import common.Bookmark;
import common.MemoryThread;
import common.PerformanceMeasurement;
import common.Utilities;
import file.PredictionFileWriter;
import file.BookmarkReader;

public class GIRPTMCalculator {

	private List<Map<Integer, Double>> tagRecencies;
	private List<Map<Integer, Double>> userMaps;
	private List<Map<Integer, Double>> resMaps;
	private boolean userBased;
	private boolean resBased;
	
	public GIRPTMCalculator(BookmarkReader reader, int trainSize, boolean userBased, boolean resBased) {
		List<Bookmark> trainList = reader.getBookmarks().subList(0, trainSize);
		this.userBased = userBased;
		this.resBased = resBased;
		
		if (this.userBased) {
			this.userMaps = Utilities.getRelativeTagMaps(trainList, false);
			List<List<Bookmark>> userBookmarks = Utilities.getBookmarks(trainList, false);
			this.tagRecencies = new ArrayList<Map<Integer, Double>>();
			for (List<Bookmark> userB : userBookmarks) {
				this.tagRecencies.add(getTagRecencies(userB));
			}
		}
		if (this.resBased) {
			this.resMaps = Utilities.getRelativeTagMaps(trainList, true);
		}
	}
	
	public Map<Integer, Double> getRankedTagList(int userID, int resID) {
		Map<Integer, Double> resultMap = new LinkedHashMap<Integer, Double>();
		if (this.userMaps != null && userID < this.userMaps.size()) {
			Map<Integer, Double> userMap = this.userMaps.get(userID);
			Map<Integer, Double> recMap = this.tagRecencies.get(userID);
			for (Map.Entry<Integer, Double> entry : userMap.entrySet()) {
				resultMap.put(entry.getKey(), entry.getValue().doubleValue() * recMap.get(entry.getKey()).doubleValue());
			}
		}
		if (this.resMaps != null && resID < this.resMaps.size()) {
			Map<Integer, Double> resMap = this.resMaps.get(resID);
			for (Map.Entry<Integer, Double> entry : resMap.entrySet()) {
				Double val = resultMap.get(entry.getKey());
				resultMap.put(entry.getKey(), val == null ? entry.getValue().doubleValue() : val.doubleValue() + entry.getValue().doubleValue());
			}
		}
		
		// sort and return
		Map<Integer, Double> returnMap = new LinkedHashMap<Integer, Double>();
		Map<Integer, Double> sortedResultMap = new TreeMap<Integer, Double>(new DoubleMapComparator(resultMap));
		sortedResultMap.putAll(resultMap);
		int count = 0;
		for (Map.Entry<Integer, Double> entry : sortedResultMap.entrySet()) {
			if (count++ < 10) {
				returnMap.put(entry.getKey(), entry.getValue());
			} else {
				break;
			}
		}		
		return returnMap;
	}
	
	private Map<Integer, Double> getTagRecencies(List<Bookmark> bookmarks) {
		//Collections.sort(bookmarks); // TODO: necessary?
		int testIndex = bookmarks.size() + 1;
		Map<Integer, Integer> firstUsages = new LinkedHashMap<Integer, Integer>();
		Map<Integer, Integer> lastUsages = new LinkedHashMap<Integer, Integer>();
		Map<Integer, Double> resultMap = new LinkedHashMap<Integer, Double>();
		for (int i = 0; i < bookmarks.size(); i++) {
			for (int tag : bookmarks.get(i).getTags()) {
				Integer firstIndex = firstUsages.get(tag);
				Integer lastIndex = lastUsages.get(tag);
				if (firstIndex == null || i < firstIndex) {
					firstUsages.put(tag, i);
				};
				if (lastIndex == null || i > lastIndex) {
					lastUsages.put(tag, i);
				};
			}
		}
		
		for (Map.Entry<Integer, Integer> firstIndex : firstUsages.entrySet()) {
			double firstVal = Math.log((double)(testIndex - firstIndex.getValue()));
			double lastVal = Math.log((double)(testIndex - lastUsages.get(firstIndex.getKey())));
			Double rec = firstVal * (Math.pow(lastVal, firstVal * (-1.0)));
			if (!rec.isNaN() && !rec.isInfinite()) {
				resultMap.put(firstIndex.getKey(), rec.doubleValue());
			}
		}
		return resultMap;
	}
	
	// Statics ----------------------------------------------------------------------------------------------------------------------	
	private static String timeString;
	
	public static BookmarkReader predictSample(String filename, int trainSize, int sampleSize, boolean userBased, boolean resBased) {
		Timer timerThread = new Timer();
		MemoryThread memoryThread = new MemoryThread();
		timerThread.schedule(memoryThread, 0, MemoryThread.TIME_SPAN);
		
		BookmarkReader reader = new BookmarkReader(trainSize, false);
		reader.readFile(filename);	
		List<int[]> predictionValues = new ArrayList<int[]>();
		Stopwatch timer = new Stopwatch();
		timer.start();
		GIRPTMCalculator calculator = new GIRPTMCalculator(reader, trainSize, userBased, resBased);
		timer.stop();
		long trainingTime = timer.elapsed(TimeUnit.MILLISECONDS);
		
		timer.reset();
		timer.start();
		for (int i = trainSize; i < trainSize + sampleSize; i++) { // the test-set
			Bookmark data = reader.getBookmarks().get(i);
			Map<Integer, Double> map = calculator.getRankedTagList(data.getUserID(), data.getWikiID());
			predictionValues.add(Ints.toArray(map.keySet()));
		}
		timer.stop();
		long testTime = timer.elapsed(TimeUnit.MILLISECONDS);
		
		timeString = PerformanceMeasurement.addTimeMeasurement(timeString, true, trainingTime, testTime, sampleSize);		
		String suffix = "_rec";
		if (!userBased) {
			suffix = "_resrec";
		} else if (!resBased) {
			suffix = "_userrec";
		}
		reader.setUserLines(reader.getBookmarks().subList(trainSize, reader.getBookmarks().size()));
		PredictionFileWriter writer = new PredictionFileWriter(reader, predictionValues);
		writer.writeFile(filename + suffix);
		
		timeString = PerformanceMeasurement.addMemoryMeasurement(timeString, false, memoryThread.getMaxMemory());
		timerThread.cancel();
		Utilities.writeStringToFile("./data/metrics/" + filename + suffix + "_TIME.txt", timeString);		
		return reader;
	}
}
