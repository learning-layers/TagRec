package itemrecommendations;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.google.common.primitives.Ints;
import common.Bookmark;
import common.IntMapComparator;
import common.Utilities;

import file.BookmarkReader;
import file.PredictionFileWriter;

public class MPResourceCalculator {
	
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
	
	public static BookmarkReader predictPopularResources(String filename, int trainSize) {
		BookmarkReader reader = new BookmarkReader(trainSize, false);
		reader.readFile(filename);

		List<int[]> values = getPopularResources(reader, 20, trainSize);
		PredictionFileWriter writer = new PredictionFileWriter(reader, values);
		writer.writeResourcePredictionsToFile(filename + "_pop", trainSize, 0);
		return reader;
	}
	
	public static BookmarkReader predictRandomResources(String filename, int trainSize) {
		BookmarkReader reader = new BookmarkReader(trainSize, false);
		reader.readFile(filename);
		
		List<int[]> values = getRandomResources(reader, 20, trainSize);
		PredictionFileWriter writer = new PredictionFileWriter(reader, values);
		writer.writeResourcePredictionsToFile(filename + "_rand", trainSize, 0);
		return reader;
	}
}
