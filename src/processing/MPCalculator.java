/*
 TagRecommender:
 A framework to implement and evaluate algorithms for the recommendation
 of tags.
 Copyright (C) 2013 Dominik Kowald
 
 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU Affero General Public License as
 published by the Free Software Foundation, either version 3 of the
 License, or (at your option) any later version.
 
 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU Affero General Public License for more details.
 
 You should have received a copy of the GNU Affero General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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

import common.IntMapComparator;
import common.Bookmark;
import common.MemoryThread;
import common.PerformanceMeasurement;
import common.Utilities;
import file.PredictionFileWriter;
import file.BookmarkReader;

public class MPCalculator {

	private static String timeString;
	
	private static List<int[]> getPerfectTags(BookmarkReader reader, int sampleSize, int limit) {
		List<int[]> tags = new ArrayList<int[]>();
		int trainSize = reader.getBookmarks().size() - sampleSize;
		
		for (Bookmark data : reader.getBookmarks().subList(trainSize, trainSize + sampleSize)) {
			List<Integer> t = new ArrayList<Integer>(data.getTags());
			//while (t.size() < limit) {
			//	t.add(-1);
			//}
			tags.add(Ints.toArray(t));
		}
		return tags;
	}
	
	private static int[] getPopularTagList(BookmarkReader reader, int size) {
		Map<Integer, Integer> countMap = new LinkedHashMap<Integer, Integer>();
		for (int i = 0; i < reader.getTagCounts().size(); i++) {
			countMap.put(i, reader.getTagCounts().get(i));
		}
		Map<Integer, Integer> sortedCountMap = new TreeMap<Integer, Integer>(new IntMapComparator(countMap));
		sortedCountMap.putAll(countMap);
		int[] tagIDs = new int[size];
		int i = 0;
		for (Integer key : sortedCountMap.keySet()) {
			if (i < size) {
				tagIDs[i++] = key;
			} else {
				break;
			}
		}
		return tagIDs;
	}
	
	private static List<int[]> getPopularTags(BookmarkReader reader, int sampleSize, int limit) {
		List<int[]> tags = new ArrayList<int[]>();
		Stopwatch timer = new Stopwatch();
		timer.start();

		int[] tagIDs = getPopularTagList(reader, limit);
		
		timer.stop();
		long trainingTime = timer.elapsed(TimeUnit.MILLISECONDS);
		timer.reset();
		timer.start();
		for (int j = 0; j < sampleSize; j++) {
			tags.add(tagIDs);
		}
		timer.stop();
		long testTime = timer.elapsed(TimeUnit.MILLISECONDS);
		
		timeString = PerformanceMeasurement.addTimeMeasurement(timeString, true, trainingTime, testTime, sampleSize);
		return tags;
	}
	
	// public statics --------------------------------------------------------------------------------------------
	public static BookmarkReader predictPopularTags(String filename, int trainSize, int sampleSize, boolean mp) {
		Timer timerThread = new Timer();
		MemoryThread memoryThread = new MemoryThread();
		timerThread.schedule(memoryThread, 0, MemoryThread.TIME_SPAN);
		
		BookmarkReader reader = new BookmarkReader(trainSize, false);
		reader.readFile(filename);

		List<int[]> values = null;
		if (mp) {
			values = getPopularTags(reader, sampleSize, Utilities.REC_LIMIT);
		} else {
			values = getPerfectTags(reader, sampleSize, Utilities.REC_LIMIT);
		}
		
		reader.setTestLines(reader.getBookmarks().subList(trainSize, reader.getBookmarks().size()));
		PredictionFileWriter writer = new PredictionFileWriter(reader, values);
		writer.writeFile(filename + "_mp");
		
		timeString = PerformanceMeasurement.addMemoryMeasurement(timeString, false, memoryThread.getMaxMemory());
		timerThread.cancel();
		Utilities.writeStringToFile("./data/metrics/" + filename + "_mp" + "_TIME.txt", timeString);
		return reader;
	}
}
