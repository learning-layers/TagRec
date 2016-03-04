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

import common.CalculationType;
import common.CooccurenceMatrix;
import common.DoubleMapComparator;
import common.Bookmark;
import common.MemoryThread;
import common.PerformanceMeasurement;
import common.Utilities;
import file.PredictionFileWriter;
import file.BookmarkReader;

public class ThreeLTCalculator {

	private BookmarkReader reader;
	private List<Bookmark> trainList;
	private double beta; // used for user - res combination
	private double dValue; // used for time
	private boolean userBased;
	private boolean resBased;
	private boolean bookmarkBLL;
	
	private List<List<Bookmark>> userBookmarks;
	List<Map<Integer, Double>> resMaps;
	
	private CooccurenceMatrix rMatrix;
	private CalculationType cType;
	private List<Map<Integer, Double>> userCounts;
	private List<Map<Integer, Double>> resCounts;
	
	public List<Map<Integer, Double>> getUserMaps() {
		return this.userCounts;
	}
	
	public ThreeLTCalculator(BookmarkReader reader, int trainSize, int dValue, int beta, boolean userBased, boolean resBased, boolean bookmarkBLL, CalculationType cType) {
		this.reader = reader;
		this.trainList = this.reader.getBookmarks().subList(0, trainSize);
		this.userBookmarks = Utilities.getBookmarks(this.trainList, false);
		this.beta = (double)beta / 10.0;
		this.dValue = (double)dValue / 10.0;
		this.userBased = userBased;
		this.resBased = resBased;		
		this.bookmarkBLL = bookmarkBLL;		
		this.cType = cType;
		
		this.resMaps = BLLCalculator.getArtifactMaps(reader, this.trainList, null, true, new ArrayList<Long>(), new ArrayList<Double>(), 0, true);
		this.userCounts = Utilities.getRelativeTagMaps(this.trainList, false);
		this.resCounts = Utilities.getRelativeTagMaps(this.trainList, true);
		if (this.cType == CalculationType.USER_TO_RESOURCE) {
			this.rMatrix = new CooccurenceMatrix(this.trainList, this.reader.getTagCounts(), true);
		}
	}
	
	private Map<Integer, Double> getLastUsages(List<Bookmark> bookmarks, double timestamp, boolean categories) {
		Map<Integer, Double> usageMap = new LinkedHashMap<Integer, Double>();
		for (Bookmark data : bookmarks) {
			List<Integer> keys = (categories ? data.getCategories() : data.getTags());
			double targetTimestamp = Double.parseDouble(data.getTimestamp());
			for (int key : keys) {
				Double val = usageMap.get(key);
				if (val == null || targetTimestamp > val.doubleValue()) {
					usageMap.put(key, targetTimestamp);
				}
			}
		}
		for (Map.Entry<Integer, Double> entry : usageMap.entrySet()) {
			Double rec = Math.pow(timestamp - entry.getValue() + 1.0, this.dValue * (-1.0));
			//Double rec = Math.exp((timestamp - entry.getValue() + 1.0) * -1.0);
			if (!rec.isInfinite() && !rec.isNaN()) {
				entry.setValue(rec.doubleValue());
			} else {
				System.out.println("BLL - NAN");
				entry.setValue(0.0);
			}
		}
		return usageMap;
	}
	
	public Map<Integer, Double> getRankedTagList(int userID, int resID, List<Integer> testCats, double testTimestamp, int limit, boolean tagBLL, boolean topicBLL, boolean sorting) {	
		Map<Integer, Double> userResultMap = null;
		if (this.userBased) {
			List<Bookmark> userB = null;
			Map<Integer, Double> userTagMap = null;
			Map<Integer, Double> userCatMap = null;
			Map<Integer, Double> userCount = null;
			Map<Integer, Double> resCount = null;
			if (userID != -1 && userID < this.userBookmarks.size()) {
				userB = this.userBookmarks.get(userID);
				userCount = this.userCounts.get(userID);
				if (tagBLL) {
					userTagMap = getLastUsages(userB, testTimestamp, false);
				}
				if (topicBLL) {
					userCatMap = getLastUsages(userB, testTimestamp, true);
				}
			} else {
				userB = new ArrayList<Bookmark>();
				userCount = new LinkedHashMap<Integer, Double>();
			}
			userResultMap = getResultMap(userB, testCats, userTagMap, userCatMap, testTimestamp, topicBLL);
			if (this.cType == CalculationType.USER_TO_RESOURCE && resID < this.resCounts.size()) {
				resCount = this.resCounts.get(resID);
				Map<Integer, Double> associativeValues = this.rMatrix.calculateAssociativeComponentsWithTagAssosiation(userCount, resCount, false, true, false);
				for (Map.Entry<Integer, Double> entry : associativeValues.entrySet()) {
					Double val = userResultMap.get(entry.getKey());				
					userResultMap.put(entry.getKey(), val == null ? entry.getValue().doubleValue() : val.doubleValue() + entry.getValue().doubleValue());
				}
				double denom = 0.0;
				for (Map.Entry<Integer, Double> entry : userResultMap.entrySet()) {
					double val = Math.log(entry.getValue());
					denom += Math.exp(val);
				}
				for (Map.Entry<Integer, Double> entry : userResultMap.entrySet()) {
					entry.setValue(Math.exp(Math.log(entry.getValue())) / denom);
				}
			}
		} else {
			userResultMap = new LinkedHashMap<Integer, Double>();
		}
		
		Map<Integer, Double> resResultMap = null;
		if (this.resBased) {
			if (this.resMaps != null) {
				if (resID != -1 && resID < this.resMaps.size()) {
					resResultMap = this.resMaps.get(resID);
				} else {
					resResultMap = new LinkedHashMap<Integer, Double>();
				}
			}
		}

		// merge user and resource results
		Map<Integer, Double> resultMap = new LinkedHashMap<Integer, Double>();
		for (int i = 0; i < this.reader.getTags().size(); i++) {
			double userVal = 0.0;
			if (userResultMap != null && userResultMap.containsKey(i)) {
				userVal = userResultMap.get(i);
			}
			double resVal = 0.0;
			if (resResultMap != null && resResultMap.containsKey(i)) {
				resVal = resResultMap.get(i);
			}
			if (userVal != 0.0 || resVal != 0.0) {
				resultMap.put(i, this.beta * userVal + (1.0 - this.beta) * resVal);
				//resultMap.put(i, userVal + resVal);
			}
		}
		
		// sort and return
		Map<Integer, Double> returnMap = new LinkedHashMap<Integer, Double>();
		Map<Integer, Double> sortedResultMap = null;
		if (sorting) {
			sortedResultMap = new TreeMap<Integer, Double>(new DoubleMapComparator(resultMap));
			sortedResultMap.putAll(resultMap);
		} else {
			sortedResultMap = resultMap;
		}
		int count = 0;
		for (Map.Entry<Integer, Double> entry : sortedResultMap.entrySet()) {
			if (count++ < limit) {
				returnMap.put(entry.getKey(), entry.getValue());
			} else {
				break;
			}
		}		
		return returnMap;
	}
	
	public Map<Integer, Double> getCollectiveRankedTagList(List<Integer> testCats, double testTimestamp, int limit, boolean tagBLL, boolean topicBLL) {
		Map<Integer, Double> collectiveTagMap = new LinkedHashMap<Integer, Double>();
		
		List<Bookmark> bookmarks = this.reader.getBookmarks();
		List<Map<Integer, Integer>> resTopics = Utilities.getResTopics(bookmarks);
		for (Bookmark b : bookmarks) {
			if (b.getResourceID() < resTopics.size()) {
				double sim = Utilities.getCosineSimList(testCats, new ArrayList<Integer>(resTopics.get(b.getResourceID()).keySet()));
				Double ajhid = Math.pow(sim, 3);
				if (ajhid.isNaN() || ajhid.isInfinite()) {
					ajhid = 0.0;
					System.out.println("Cos - NAN");
				}
				for (int t : b.getTags()) {
					Double tVal = collectiveTagMap.get(t);
					collectiveTagMap.put(t, tVal == null ? ajhid : tVal.doubleValue() + ajhid);
				}
			}
		}
				
		Map<Integer, Double> sortedResultMap = new TreeMap<Integer, Double>(new DoubleMapComparator(collectiveTagMap));
		sortedResultMap.putAll(collectiveTagMap);
		Map<Integer, Double> returnMap = new LinkedHashMap<Integer, Double>();
		for (Map.Entry<Integer, Double> entry : sortedResultMap.entrySet()) {
			//if (returnMap.size() < limit) {
				returnMap.put(entry.getKey(), entry.getValue());
			//} else {
			//	break;
			//}
		}
		
		return returnMap;
	}

	private Map<Integer, Double> getResultMap(List<Bookmark> bookmarks, List<Integer> testCats, Map<Integer, Double> userTagMap, Map<Integer, Double> userCatMap, double testTimestamp, boolean topicBLL) {
		Map<Integer, Double> resultMap = new LinkedHashMap<Integer, Double>();
		Map<Integer, Double> testCatsMap = getMapFromList(testCats, null);
		for (Bookmark data : bookmarks) {
			// old version for topicBLL
			Map<Integer, Double> catsMap = getMapFromList(data.getCategories(), null/*userCatMap*/);
			
			//Double ajhid = Math.exp((1.0 - Utilities.getCosineFloatSim(catsMap, testCatsMap)) * (-1.0));
			Double sim = Utilities.getCosineFloatSim(catsMap, testCatsMap);
			Double ajhid = Math.pow(sim, 3);
			if (ajhid.isNaN() || ajhid.isInfinite()) {
				ajhid = 0.0;
				System.out.println("Cos - NAN");
			}
			// new version for topicBLL
			if (topicBLL) {
				double topicRecSum = 0.0;
				Map<Integer, Double> catsRecMap = getMapFromList(data.getCategories(), userCatMap);
				for (double catRec : catsRecMap.values()) {
					topicRecSum += catRec;
 				}
				ajhid *= topicRecSum;
			}
			if (this.bookmarkBLL) {
				ajhid *= getBookmarkBLL(data, testTimestamp);
			}
			
			Map<Integer, Double> tagsMap = getMapFromList(data.getTags(), userTagMap);			
			for (Map.Entry<Integer, Double> entry : tagsMap.entrySet()) {
				Double akout = ajhid.doubleValue() * entry.getValue().doubleValue();
				Double value = resultMap.get(entry.getKey());
				resultMap.put(entry.getKey(), value == null ? akout.doubleValue() : value.doubleValue() + akout.doubleValue());
			}
		}
		
		// normalize and return
		double denom = 0.0;
		for (Map.Entry<Integer, Double> entry : resultMap.entrySet()) {
			double val = 0.0;
			if (entry.getValue() != 0.0) {
				val = Math.log(entry.getValue());
			}
			denom += Math.exp(val);
			entry.setValue(val);
		}
		for (Map.Entry<Integer, Double> entry : resultMap.entrySet()) {
			entry.setValue(Math.exp(entry.getValue()) / denom);
		}
		return resultMap;
	}
	
	private double getBookmarkBLL(Bookmark data, double testTimestamp) {
		Double rec = Math.pow(testTimestamp - Double.parseDouble(data.getTimestamp()) + 1.0, this.dValue * (-1.0));
		if (!rec.isInfinite() && !rec.isNaN()) {
			return Math.log(rec + 1.0);
		}
		
		System.out.println("Bookmark-BLL - NAN");
		return Math.log(1.0);
	}

	private Map<Integer, Double> getMapFromList(List<Integer> keys, Map<Integer, Double> values) {
		Map<Integer, Double> map = new LinkedHashMap<Integer, Double>();
		for (int key : keys) {
			if (values != null && values.containsKey(key)) {
				Double val = Math.log(values.get(key) + 1.0);
				if (!val.isNaN() && !val.isInfinite()) {
					map.put(key, Math.log(values.get(key) + 1.0));
				} else {
					map.put(key, Math.log(1.0 + 1.0));
				}
				//map.put(key, values.get(key));
			} else {
				map.put(key, Math.log(1.0 + 1.0));
				//map.put(key, 1.0);
			}
		}
		return map;
	}
	
	/*
	private Map<Integer, Double> getAllUsages(List<Bookmark> bookmarks, double timestamp, boolean categories) {
		Map<Integer, Double> usageMap = new LinkedHashMap<Integer, Double>();
		for (Bookmark data : bookmarks) {
			List<Integer> keys = (categories ? data.getCategories() : data.getTags());
			double targetTimestamp = Double.parseDouble(data.getTimestamp());
			Double rec = Math.pow(timestamp - targetTimestamp + 1.0, this.dValue * (-1.0));
			if (!rec.isInfinite() && !rec.isNaN()) {
				for (int key : keys) {
					Double oldVal = usageMap.get(key);
					usageMap.put(key, (oldVal != null ? oldVal + rec : rec));
				}
			} else {
				System.out.println("BLL - NAN");
			}
		}
		return usageMap;
	}
	*/
		
	// Statics -----------------------------------------------------------------------------------------------------------------------		
	private static String timeString;
	
	public static BookmarkReader predictSample(String filename, int trainSize, int sampleSize, int d, int beta, boolean userBased, boolean resBased,
			boolean tagBLL, boolean topicBLL, CalculationType cType) {
		
		Timer timerThread = new Timer();
		MemoryThread memoryThread = new MemoryThread();
		timerThread.schedule(memoryThread, 0, MemoryThread.TIME_SPAN);
		
		BookmarkReader reader = new BookmarkReader(trainSize, false);
		reader.readFile(filename);
		
		List<int[]> predictionValues = new ArrayList<int[]>();
		Stopwatch timer = new Stopwatch();
		timer.start();
		ThreeLTCalculator calculator = new ThreeLTCalculator(reader, trainSize, d, beta, userBased, resBased, false, cType);
		timer.stop();
		long trainingTime = timer.elapsed(TimeUnit.MILLISECONDS);
		
		timer.reset();
		timer.start();
		for (int i = trainSize; i < trainSize + sampleSize; i++) { // the test-set
			Bookmark data = reader.getBookmarks().get(i);
			long timestamp = Long.parseLong((data.getTimestamp()));
			Map<Integer, Double> map = calculator.getRankedTagList(data.getUserID(), data.getResourceID(), data.getCategories(), timestamp, 10, tagBLL, topicBLL, true);
			predictionValues.add(Ints.toArray(map.keySet()));
		}
		timer.stop();
		long testTime = timer.elapsed(TimeUnit.MILLISECONDS);
		
		timeString = PerformanceMeasurement.addTimeMeasurement(timeString, true, trainingTime, testTime, sampleSize);		
		String suffix = "_layers";
		if (!userBased) {
			suffix = "_reslayers";
		} else if (!resBased) {
			suffix = "_userlayers";
		}
		if (tagBLL && topicBLL) {
			suffix += "bll";
		} else if (tagBLL) {
			suffix += "tagbll";
		} else if (topicBLL) {
			suffix += "topicbll";
		}
		if (cType == CalculationType.USER_TO_RESOURCE) {
			suffix += "ac";
		}
		
		String outputFile = filename + suffix + "_" + beta + "_" + d;	
		reader.setTestLines(reader.getBookmarks().subList(trainSize, reader.getBookmarks().size()));
		PredictionFileWriter writer = new PredictionFileWriter(reader, predictionValues);
		writer.writeFile(outputFile);
		
		timeString = PerformanceMeasurement.addMemoryMeasurement(timeString, false, memoryThread.getMaxMemory());
		timerThread.cancel();
		Utilities.writeStringToFile("./data/metrics/" + outputFile + "_TIME.txt", timeString);	
		return reader;
	}
}
