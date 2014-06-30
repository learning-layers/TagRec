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
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import com.google.common.base.Stopwatch;
import com.google.common.primitives.Ints;

import common.IntMapComparator;
import common.Bookmark;
import common.Utilities;
import file.PredictionFileWriter;
import file.BookmarkReader;

public class BaselineCalculator {

	private static String timeString = "";
	
	public static int[] getPopularTagList(BookmarkReader reader, int size) {
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
	
	private static List<int[]> getPerfectTags(BookmarkReader reader, int sampleSize, int limit) {
		List<int[]> tags = new ArrayList<int[]>();
		int trainSize = reader.getBookmarks().size() - sampleSize;
		
		for (Bookmark data : reader.getBookmarks().subList(trainSize, trainSize + sampleSize)) {
			List<Integer> t = data.getTags();
			while (t.size() < limit) {
				t.add(-1);
			}
			tags.add(Ints.toArray(t));
		}
		return tags;
	}
	
	private static List<int[]> getPopularTags(BookmarkReader reader, int sampleSize, int limit) {
		timeString = "";
		List<int[]> tags = new ArrayList<int[]>();
		Stopwatch timer = new Stopwatch();
		timer.start();

		int[] tagIDs = getPopularTagList(reader, limit);
		
		timer.stop();
		long trainingTime = timer.elapsed(TimeUnit.MILLISECONDS);
		timer = new Stopwatch();
		timer.start();
		for (int j = 0; j < sampleSize; j++) {
			tags.add(tagIDs);
		}
		timer.stop();
		long testTime = timer.elapsed(TimeUnit.MILLISECONDS);
		timeString += ("Full training time: " + trainingTime + "\n");
		timeString += ("Full test time: " + testTime + "\n");
		timeString += ("Average test time: " + testTime / sampleSize) + "\n";
		timeString += ("Total time: " + (trainingTime + testTime) + "\n");
		return tags;
	}
	
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
	
	public static void predictPopularTags(String filename, int trainSize, int sampleSize) {
		//filename += "_res";
		
		BookmarkReader reader = new BookmarkReader(trainSize, false);
		reader.readFile(filename);

		List<int[]> values = getPopularTags(reader, sampleSize, 10);
		//List<int[]> values = getPerfectTags(reader, sampleSize, 10);
		
		reader.setUserLines(reader.getBookmarks().subList(trainSize, reader.getBookmarks().size()));
		PredictionFileWriter writer = new PredictionFileWriter(reader, values);
		writer.writeFile(filename + "_mp");
		Utilities.writeStringToFile("./data/metrics/" + filename + "_mp" + "_TIME.txt", timeString);
	}
	
	public static BookmarkReader predictPopularResources(String filename, int trainSize) {
		//filename += "_res";

		BookmarkReader reader = new BookmarkReader(trainSize, false); // TODO: size
		reader.readFile(filename);

		List<int[]> values = getPopularResources(reader, 20, trainSize);
		PredictionFileWriter writer = new PredictionFileWriter(reader, values);
		writer.writeResourcePredictionsToFile(filename + "_pop", trainSize, 0);
		return reader;
	}
	
	public static BookmarkReader predictRandomResources(String filename, int trainSize) {
		//filename += "_res";

		BookmarkReader reader = new BookmarkReader(trainSize, false); // TODO: size?
		reader.readFile(filename);
		List<int[]> values = getRandomResources(reader, 20, trainSize);
		PredictionFileWriter writer = new PredictionFileWriter(reader, values);
		writer.writeResourcePredictionsToFile(filename + "_rand", trainSize, 0);
		return reader;
	}
}
