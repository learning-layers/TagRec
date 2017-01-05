package itemrecommendations;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import com.google.common.base.Stopwatch;
import com.google.common.primitives.Ints;

import common.Bookmark;
import common.IntMapComparator;
import common.MemoryThread;
import common.PerformanceMeasurement;
import common.Utilities;
import file.BookmarkReader;
import file.PredictionFileWriter;

public class MPResourceCalculator {
	
	private static String timeString;
	
	private static List<int[]> getPopularResources(BookmarkReader reader, int count, int trainSize) {
		List<int[]> resources = new ArrayList<int[]>();
		Map<Integer, Integer> countMap = new LinkedHashMap<Integer, Integer>();
		for (int i = 0; i < reader.getResources().size(); i++) {
			countMap.put(i, reader.getResourceCounts().get(i));
		}
		Map<Integer, Integer> sortedCountMap = new TreeMap<Integer, Integer>(new IntMapComparator(countMap));
		sortedCountMap.putAll(countMap);
		
		for (int userID : reader.getUniqueUserListFromTestSet(trainSize)) {
			List<Integer> userResources = Bookmark.getResourcesFromUser(reader.getBookmarks().subList(0, trainSize), userID);
			//System.out.println(userResources.size());
			List<Integer> resIDs = new ArrayList<Integer>();
			int i = 0;
			for (Integer key : sortedCountMap.keySet()) {
				if (i < count) {
					if (!userResources.contains(key)) {
						resIDs.add(key);
						i++;
					}
				} else {
					break;
				}
			}
			resources.add(Ints.toArray(resIDs));
		}
		return resources;
	}
	
	private static List<int[]> getRandomResources(BookmarkReader reader, int count, int trainSize) {
		List<int[]> resources = new ArrayList<int[]>();
		int resCount = reader.getResources().size();
	
		for (int userID : reader.getUniqueUserListFromTestSet(trainSize)) {
			List<Integer> userResources = Bookmark.getResourcesFromUser(reader.getBookmarks().subList(0, trainSize), userID);
			
			List<Integer> resIDs = new ArrayList<Integer>();
			int i = 0;
			for (Integer res : Utilities.getRandomIndices(0, resCount - 1)) {
				if (i < count) {
					if (!userResources.contains(res)) {
						resIDs.add(res);
						i++;
					}
				} else {
					break;
				}
			}
			resources.add(Ints.toArray(resIDs));
		}
		return resources;
	}
	
	public static BookmarkReader predictPopularResources(String filename, int trainSize, boolean writeTime) {
		Timer timerThread = new Timer();
		MemoryThread memoryThread = new MemoryThread();
		timerThread.schedule(memoryThread, 0, MemoryThread.TIME_SPAN);
		
		BookmarkReader reader = new BookmarkReader(trainSize, false);
		reader.readFile(filename);
		Stopwatch timer = new Stopwatch();
		timer.start();
		
		List<int[]> values = getPopularResources(reader, 20, trainSize);
		
		timer.stop();
		long trainingTime = timer.elapsed(TimeUnit.MILLISECONDS);
		timer.reset();
		timer.start();
		PredictionFileWriter writer = new PredictionFileWriter(reader, values);
		writer.writeResourcePredictionsToFile(filename + "_mp", trainSize, 0);
		timer.stop();
		long testTime = timer.elapsed(TimeUnit.MILLISECONDS);
		timeString = PerformanceMeasurement.addTimeMeasurement(timeString, true, trainingTime, testTime, reader.getBookmarks().size() - trainSize);
		
		timeString = PerformanceMeasurement.addMemoryMeasurement(timeString, false, memoryThread.getMaxMemory());
		timerThread.cancel();
		if (writeTime) {
			Utilities.writeStringToFile("./data/metrics/" + filename + "_mp_TIME.txt", timeString);
		}
		return reader;
	}
	
	public static BookmarkReader predictRandomResources(String filename, int trainSize, boolean writeTime) {
		BookmarkReader reader = new BookmarkReader(trainSize, false);
		reader.readFile(filename);
		
		List<int[]> values = getRandomResources(reader, 20, trainSize);
		PredictionFileWriter writer = new PredictionFileWriter(reader, values);
		writer.writeResourcePredictionsToFile(filename + "_rand", trainSize, 0);
		return reader;
	}
}
