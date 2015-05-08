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

public class CFTagRecommender {
	
	public static int MAX_NEIGHBORS = 20;
	private final static double K1 = 1.2;
	private final static double K3 = 1.2;
	private final static double B = 0.8;
	
	private BookmarkReader reader;
	private boolean userBased;
	private boolean resBased;
	private double beta;
	private List<Bookmark> trainList;
	private List<Map<Integer, Integer>> userMaps;
	private List<Map<Integer, Integer>> resMaps;
	
	public CFTagRecommender(BookmarkReader reader, int trainSize, boolean userBased, boolean resBased, int beta) {
		this.reader = reader;
		this.userBased = userBased;
		this.resBased = resBased;
		this.beta = (double)beta / 10.0;
		this.trainList = this.reader.getBookmarks().subList(0, trainSize);
		//Collections.sort(this.trainList);
		if (this.userBased) {
			this.userMaps = Utilities.getUserMaps(this.trainList);
		}
		if (this.resBased) {
			this.resMaps = Utilities.getResMaps(this.trainList);
		}
	}
		
	public Map<Integer, Double> getRankedTagList(int userID, int resID, boolean sorting) {
		Map<Integer, Double> resultMap = new LinkedHashMap<Integer, Double>();
		int i = 0;
		
		if (this.userBased) {
			Map<Integer, Double> neighbors = getNeighbors(userID, resID);
			for (Map.Entry<Integer, Double> entry : neighbors.entrySet()) {
				if (i++ < MAX_NEIGHBORS) {
					//neighborMaps.add(this.userMaps.get(entry.getKey()));
					List<Integer> tags = Bookmark.getBookmark(this.trainList, entry.getKey(), resID).getTags();
					double bm25 = this.beta * entry.getValue();
					// add tags to resultMap
					for (int tag : tags) {
						Double val = resultMap.get(tag);
						resultMap.put(tag, (val != null ? val + bm25 : bm25));
					}
				} else {
					break;
				}
			}		
			//for (Map.Entry<Integer, Double> entry : resultMap.entrySet()) {
			//	entry.setValue(Math.log10(1 + (double)getTagFrequency(entry.getKey(), neighborMaps)) * entry.getValue());
			//}
		}
		if (this.resBased) {
			Map<Integer, Double> resources = getSimResources(userID, resID);
			for (Map.Entry<Integer, Double> entry : resources.entrySet()) {
				if (i++ < MAX_NEIGHBORS) {
					List<Integer> tags = Bookmark.getResData(this.trainList, userID, entry.getKey()).getTags();
					double bm25 = (1.0 - this.beta) * entry.getValue();
					// add tags to resultMap
					for (int tag : tags) {
						Double val = resultMap.get(tag);
						resultMap.put(tag, (val != null ? val + bm25 : bm25));
					}
				} else {
					break;
				}
			}	
		}
		
		if (sorting) {
			Map<Integer, Double> sortedResultMap = new TreeMap<Integer, Double>(new DoubleMapComparator(resultMap));
			sortedResultMap.putAll(resultMap);			
			Map<Integer, Double> returnMap = new LinkedHashMap<Integer, Double>(10);
			int index = 0;
			for (Map.Entry<Integer, Double> entry : sortedResultMap.entrySet()) {
				if (index++ < 10) {
					returnMap.put(entry.getKey(), entry.getValue());
				} else {
					break;
				}
			}
			return returnMap;
		}
		return resultMap;
	}
	
	private Map<Integer, Double> getNeighbors(int userID, int resID) {
		Map<Integer, Double> neighbors = new LinkedHashMap<Integer, Double>();
		//List<Map<Integer, Integer>> neighborMaps = new ArrayList<Map<Integer, Integer>>();
		// get all users that have tagged the resource
		for (Bookmark data : this.trainList) {
			if (data.getUserID() != userID) {
				if (resID == -1) {
					neighbors.put(data.getUserID(), 0.0);
				} else if (data.getResourceID() == resID) {
					neighbors.put(data.getUserID(), 0.0);
				}
			}
		}
		//boolean allUsers = false;
		// if list is empty, use all users		
		if (neighbors.size() == 0) {
			//allUsers = true;
			for (Bookmark data : this.trainList) {
				neighbors.put(data.getUserID(), 0.0);
			}
		}
		
		//for (int id : neighbors.keySet()) {
		//	neighborMaps.add(this.userMaps.get(id));
		//}
		if (userID < this.userMaps.size()) {
			Map<Integer, Integer> targetMap = this.userMaps.get(userID);
			//double lAverage = getLAverage(neighborMaps);
			for (Map.Entry<Integer, Double> entry : neighbors.entrySet()) {
				double bm25Value = /*allUsers ? */Utilities.getJaccardSim(targetMap, this.userMaps.get(entry.getKey()));// :
				//	getBM25Value(neighborMaps, lAverage, targetMap, this.userMaps.get(entry.getKey()));
				entry.setValue(bm25Value);
			}
			
			// return the sorted neighbors
			Map<Integer, Double> sortedNeighbors = new TreeMap<Integer, Double>(new DoubleMapComparator(neighbors));
			sortedNeighbors.putAll(neighbors);
			return sortedNeighbors;
		}
		return neighbors;
	}
	
	private Map<Integer, Double> getSimResources(int userID, int resID) {
		Map<Integer, Double> resources = new LinkedHashMap<Integer, Double>();
		// get all resources that have been tagged by the user
		for (Bookmark data : this.trainList) {
			if (data.getResourceID() != resID) {
				if (userID == -1) {
					resources.put(data.getResourceID(), 0.0);
				} else if (data.getUserID() == userID) {
					resources.put(data.getResourceID(), 0.0);
				}
			}
		}
		// if list is empty, use all users		
		if (resources.size() == 0) {
			for (Bookmark data : this.trainList) {
				resources.put(data.getResourceID(), 0.0);
			}
		}
		
		if (resID < this.resMaps.size()) {
			Map<Integer, Integer> targetMap = this.resMaps.get(resID);
			for (Map.Entry<Integer, Double> entry : resources.entrySet()) {
				double bm25Value = Utilities.getJaccardSim(targetMap, this.resMaps.get(entry.getKey()));
				entry.setValue(bm25Value);
			}			
			// return the sorted neighbors
			Map<Integer, Double> sortedResources = new TreeMap<Integer, Double>(new DoubleMapComparator(resources));
			sortedResources.putAll(resources);
			return sortedResources;
		}
		return resources;
	}
	
	public static double getBM25Value(List<Map<Integer, Integer>> neighborMaps, double lAverage, Map<Integer, Integer> targetMap, Map<Integer, Integer> nMap) {
		double bm25Sum = 0.0;	
		for (Map.Entry<Integer, Integer> targetVal : targetMap.entrySet()) {
			double idf = getIDF(targetVal.getKey(), neighborMaps);
			double tftd = 0.0;
			if (nMap.containsKey(targetVal.getKey())) {
				tftd = (double)nMap.get(targetVal.getKey());
			}
			double tftq = (double)targetVal.getValue();
			double ld = Utilities.getMapCount(nMap);
			double bm25Val = (idf * (K1 + 1) * tftd * (K3 + 1) * tftq) /
					((K1 * ((1 - B) + B * (ld / lAverage)) + tftd) * (K3 + tftq));
			
			if (Double.valueOf(bm25Val).isNaN()) {
				System.out.println("idf: " + idf + ", tftd: " + tftd + ", tftq: " + tftq + ", ld: " + ld + ", lAverage: " + lAverage);
			}
			
			bm25Sum += bm25Val;
		}
		return bm25Sum;
	}
	
	public static int getTagFrequency(int tagID, List<Map<Integer, Integer>> neighborMaps) {
		int count = 0;
		for (Map<Integer, Integer> map : neighborMaps) {
			if (map.containsKey(tagID)) {
				count++;
			}
		}
		
		return count;
	}
	
	public static double getIDF(int tagID, List<Map<Integer, Integer>> neighborMaps) {
		//return Math.log(((double)neighborMaps.size() - (double)count + 0.5) / ((double)count + 0.5));		
		Double idf = Math.log((double)neighborMaps.size() / (double)getTagFrequency(tagID, neighborMaps));
		return (idf.isInfinite() ? 0 : idf.doubleValue());
	}
	
	public static double getLAverage(List<Map<Integer, Integer>> neighborMaps) {
		double sum = 0.0;
		for (Map<Integer, Integer> map : neighborMaps) {
			sum += Utilities.getMapCount(map);
		}
		return sum / (double)neighborMaps.size();
	}
	
	// Statics --------------------------------------------------------------------------------------------------------
	
	private static String timeString;
	
	private static List<Map<Integer, Double>> startBM25CreationForTagPrediction(BookmarkReader reader, int sampleSize, boolean userBased, boolean resBased, int beta) {
		int size = reader.getBookmarks().size();
		int trainSize = size - sampleSize;
		Stopwatch timer = new Stopwatch();
		timer.start();
		CFTagRecommender calculator = new CFTagRecommender(reader, trainSize, userBased, resBased, beta);
		timer.stop();
		long trainingTime = timer.elapsed(TimeUnit.MILLISECONDS);
		
		List<Map<Integer, Double>> results = new ArrayList<Map<Integer, Double>>();
		timer.reset();
		timer.start();
		for (int i = trainSize; i < size; i++) {
			Bookmark data = reader.getBookmarks().get(i);
			Map<Integer, Double> map = null;
			map = calculator.getRankedTagList(data.getUserID(), data.getResourceID(), true);
			results.add(map);
			//System.out.println(data.getTags() + "|" + map.keySet());
		}
		timer.stop();
		long testTime = timer.elapsed(TimeUnit.MILLISECONDS);
		
		timeString = PerformanceMeasurement.addTimeMeasurement(timeString, true, trainingTime, testTime, sampleSize);
		return results;
	}	
	
	public static BookmarkReader predictTags(String filename, int trainSize, int sampleSize, int neighbors, boolean userBased, boolean resBased, int beta) {
		MAX_NEIGHBORS = neighbors;
		return predictSample(filename, trainSize, sampleSize, userBased, resBased, beta);
	}
	
	public static BookmarkReader predictSample(String filename, int trainSize, int sampleSize, boolean userBased, boolean resBased, int beta) {
		Timer timerThread = new Timer();
		MemoryThread memoryThread = new MemoryThread();
		timerThread.schedule(memoryThread, 0, MemoryThread.TIME_SPAN);
		
		BookmarkReader reader = new BookmarkReader(trainSize, false);
		reader.readFile(filename);
		
		List<Map<Integer, Double>> cfValues = null;	
		cfValues = startBM25CreationForTagPrediction(reader, sampleSize, userBased, resBased, beta);
		
		List<int[]> predictionValues = new ArrayList<int[]>();
		for (int i = 0; i < cfValues.size(); i++) {
			Map<Integer, Double> modelVal = cfValues.get(i);
			predictionValues.add(Ints.toArray(modelVal.keySet()));
		}		
		String suffix = "_cf_";
		if (!userBased) {
			suffix = "_rescf_";
		} else if (!resBased) {
			suffix = "_usercf_";
		}
		reader.setTestLines(reader.getBookmarks().subList(trainSize, reader.getBookmarks().size()));
		PredictionFileWriter writer = new PredictionFileWriter(reader, predictionValues);
		String outputFile = filename + suffix + beta;
		writer.writeFile(outputFile);

		timeString = PerformanceMeasurement.addMemoryMeasurement(timeString, false, memoryThread.getMaxMemory());
		timerThread.cancel();
		Utilities.writeStringToFile("./data/metrics/" + outputFile + "_TIME.txt", timeString);
		return reader;
	}
}
