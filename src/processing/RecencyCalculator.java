package processing;

import java.util.ArrayList;
import java.util.Collections;
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

public class RecencyCalculator {

	private List<List<Bookmark>> userBookmarks;
	
	public RecencyCalculator(BookmarkReader reader, int trainSize) {
		List<Bookmark> trainList = reader.getBookmarks().subList(0, trainSize);
		this.userBookmarks = Utilities.getBookmarks(trainList, false);
	}
	
	public Map<Integer, Double> getRankedTagList(int userID) {
		Map<Integer, Double> returnMap = new LinkedHashMap<Integer, Double>();
		if (userID >= this.userBookmarks.size()) {
			return returnMap;
		}
		List<Bookmark> bookmarks = this.userBookmarks.get(userID);
		Collections.sort(bookmarks);
		
		int count = 0;
		int index = bookmarks.size() - 1;
		while (count < Utilities.REC_LIMIT && index >= 0) {
			Bookmark b = bookmarks.get(index--);
			for (int t : b.getTags()) {
				if (!returnMap.containsKey(t)) {
					returnMap.put(t, (double)Utilities.REC_LIMIT - count++);
					if (count >= Utilities.REC_LIMIT) {
						break;
					}
				}
			}
		}	
		return returnMap;
	}
	
	// Statics ----------------------------------------------------------------------------------------------------------------------		
	public static BookmarkReader predictSample(String filename, int trainSize, int sampleSize) {		
		BookmarkReader reader = new BookmarkReader(trainSize, false);
		reader.readFile(filename);	
		List<int[]> predictionValues = new ArrayList<int[]>();
		Map<Integer, int[]> predictionValueMap = new LinkedHashMap<Integer, int[]>();
		RecencyCalculator calculator = new RecencyCalculator(reader, trainSize);
		
		for (int i = trainSize; i < trainSize + sampleSize; i++) { // the test-set
			Bookmark data = reader.getBookmarks().get(i);
			if (predictionValueMap.containsKey(data.getUserID())) {
				predictionValues.add(predictionValueMap.get(data.getUserID()));
			} else {
				Map<Integer, Double> map = calculator.getRankedTagList(data.getUserID());
				int[] predictionKeys = Ints.toArray(map.keySet());
				predictionValueMap.put(data.getUserID(), predictionKeys);
				predictionValues.add(predictionKeys);
			}
		}

		reader.setTestLines(reader.getBookmarks().subList(trainSize, reader.getBookmarks().size()));
		PredictionFileWriter writer = new PredictionFileWriter(reader, predictionValues);
		writer.writeFile(filename + "_rec");
		return reader;
	}
}
