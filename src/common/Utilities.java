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

package common;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.lang3.StringEscapeUtils;

import com.google.common.primitives.Ints;

import file.BookmarkReader;

public class Utilities {

	private final static String REV_START = "<rev xml:space=\"preserve\">";
	private final static String REV_END = "</rev>";
	
	public static boolean isEntityEvaluated(BookmarkReader reader, int id, Integer minBookmarks, Integer maxBookmarks, boolean resource) {
		if (id == -1) {
			return true;
		}
		List<Integer> entityCounts = (resource ? reader.getResourceCounts() : reader.getUserCounts());
		if (id < entityCounts.size()) {
			int count = entityCounts.get(id);
			if (minBookmarks != null) {
				if (count < minBookmarks.intValue()) {
					return false;
				}
			} else if (maxBookmarks != null) {
				if (count > maxBookmarks.intValue()) {
					return false;
				}
			}
			return true;
		}
		return false;
	}
	
	public static List<Map<Integer, Integer>> getUserMaps(List<Bookmark> userLines) {
		List<Map<Integer, Integer>> userMaps = new ArrayList<Map<Integer, Integer>>();
		for (Bookmark data : userLines) {
			int userID = data.getUserID();
			if (userID >= userMaps.size()) {
				userMaps.add(Utilities.mergeListWithMap(data.getTags(), new LinkedHashMap<Integer, Integer>()));
			} else {
				Utilities.mergeListWithMap(data.getTags(), userMaps.get(userID));
			}
		}
		return userMaps;
	}
	
	public static List<Map<Integer, Integer>> getResMaps(List<Bookmark> userLines) {
		List<Map<Integer, Integer>> resMaps = new ArrayList<Map<Integer, Integer>>();
		for (Bookmark data : userLines) {
			int resID = data.getWikiID();
			if (resID >= resMaps.size()) {
				resMaps.add(Utilities.mergeListWithMap(data.getTags(), new LinkedHashMap<Integer, Integer>()));
			} else {
				Utilities.mergeListWithMap(data.getTags(), resMaps.get(resID));
			}
		}
		return resMaps;
	}
	
	public static List<Map<Integer, Integer>> getUserTopics(List<Bookmark> userLines) {
		List<Map<Integer, Integer>> userMaps = new ArrayList<Map<Integer, Integer>>();
		for (Bookmark data : userLines) {
			int userID = data.getUserID();
			if (userID >= userMaps.size()) {
				userMaps.add(Utilities.mergeListWithMap(data.getCategories(), new LinkedHashMap<Integer, Integer>()));
			} else {
				Utilities.mergeListWithMap(data.getCategories(), userMaps.get(userID));
			}
		}
		return userMaps;
	}
	
	public static List<Map<Integer, Integer>> getResTopics(List<Bookmark> userLines) {
		List<Map<Integer, Integer>> resMaps = new ArrayList<Map<Integer, Integer>>();
		for (Bookmark data : userLines) {
			int resID = data.getWikiID();
			if (resID >= resMaps.size()) {
				resMaps.add(Utilities.mergeListWithMap(data.getCategories(), new LinkedHashMap<Integer, Integer>()));
			} else {
				Utilities.mergeListWithMap(data.getCategories(), resMaps.get(resID));
			}
		}
		return resMaps;
	}
	
	public static List<Map<Integer, Double>> getUniqueTopicMaps(List<Bookmark> userLines, boolean resource) {
		List<Map<Integer, Double>> resMaps = new ArrayList<Map<Integer, Double>>();
		for (Bookmark data : userLines) {
			int resID = resource ? data.getWikiID() : data.getUserID();
			Map<Integer, Double> rMap = null;
			if (resID >= resMaps.size()) {
				rMap = new LinkedHashMap<Integer, Double>();
				resMaps.add(rMap);
			}/* else {
				rMap = resMaps.get(resID);
			}*/
			if (rMap != null) {
				for (int cat : data.getCategories()) {
					rMap.put(cat, 1.0);
				}
			}
		}
		return resMaps;
	}
	
	public static List<int[]> createRandomBaseline(int from, int to, int count) {
		List<int[]> baseline = new ArrayList<int[]>();
		
		for (int i = 0; i < count; i++) {
			baseline.add(Ints.toArray(getRandomIndices(from, to)));
		}
		
		return baseline;
	}
	
	public static List<Integer> getRandomIndices(int from, int to) {
		List<Integer> indices = new ArrayList<Integer>();
		for (int j = from; j <= to; j++) {
			indices.add(j);
		}
		Collections.shuffle(indices);
		return indices;
	}
	
	public static boolean writeStringToFile(String filename, String stringToWrite) {		
		try {
			FileWriter writer = new FileWriter(new File(filename));
			BufferedWriter bw = new BufferedWriter(writer);
			bw.write(stringToWrite);
			bw.flush();
			bw.close();
			return true;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	public static Map<Integer, Double> getRelativeMapFromList(List<Integer> from) {
		Map<Integer, Double> to = new LinkedHashMap<Integer, Double>();
		for (Integer value : from) {
			Double count = to.get(value);
			to.put(value, (count != null ? count + 1.0 : 1.0));
		}
		for (Map.Entry<Integer, Double> entry : to.entrySet()) {
			entry.setValue(entry.getValue() / (double)from.size());
		}
		return to;
	}
	
	public static Map<Integer, Integer> mergeListWithMap(List<Integer> from, Map<Integer, Integer> to) {
		for (Integer value : from) {
			Integer count = to.get(value);
			to.put(value, (count != null ? count + 1 : 1));
		}
		return to;
	}
	
	public static Map<Integer, Double> mergeProbMaps(BookmarkReader reader, Map<Integer, Double> from, Map<Integer, Double> to, double lambda) {
		Map<Integer, Double> resultMap = new LinkedHashMap<Integer, Double>();
		for (int i = 0; i < reader.getTags().size(); i++) {
			Double fromVal = from.get(i);
			if (fromVal == null) {
				fromVal = 0.0;
			}
			Double toVal = to.get(i);
			if (toVal == null) {
				toVal = 0.0;
			}
			if (fromVal > 0.0 || toVal > 0.0) {
				resultMap.put(i, lambda * fromVal + (1.0 - lambda) * toVal);
			}
		}
		return resultMap;
		/*
		for (Map.Entry<Integer, Double> entry : from.entrySet()) {
			Double prob = to.get(entry.getKey());
			if (prob == null) {
				prob = 0.0;
				System.out.println("Merge maps: value not found");
			}
			to.put(entry.getKey(), lambda * entry.getValue() + (1.0 - lambda) * prob);
		}
		return to;
		*/
	}
	
	public static String getWikiContent(String xmlString) {
		int startIndex = xmlString.indexOf(REV_START);
		int endIndex = xmlString.indexOf(REV_END);
		if (startIndex != -1 && endIndex != -1) {
			return StringEscapeUtils.unescapeHtml4(xmlString.substring(startIndex + REV_START.length(), endIndex));
		}
		return null;
	}
	
	public static List<String> getTagNames(List<Integer> tagIDs, BookmarkReader reader) {
		List<String> tagNames = new ArrayList<String>();
		for (int id : tagIDs) {
			tagNames.add(reader.getTags().get(id));
		}
		return tagNames;
	}
	
	public static Set<Integer> getUsersByResource(List<Bookmark> userLines, int resID) {
		Set<Integer> userList = new HashSet<Integer>();
		for (Bookmark data : userLines) {
			if (data.getWikiID() == resID) {
				userList.add(data.getUserID());
			}
		}
		return userList;
	}
	
	public static List<Set<Integer>> getUserResourceLists(List<Bookmark> userLines) {
		List<Set<Integer>> userLists = new ArrayList<Set<Integer>>();
		for (Bookmark data : userLines) {
			int userID = data.getUserID();
			Set<Integer> resList = null;
			if (userID >= userLists.size()) {
				resList = new HashSet<Integer>();
				userLists.add(resList);
			} else {
				resList = userLists.get(userID);
			}
			resList.add(data.getWikiID());
		}
		return userLists;
	}
	
	public static List<Map<Integer, Double>> getRelativeTagMaps(List<Bookmark> userLines, boolean resource) {
		List<Map<Integer, Integer>> maps = (resource ? getResMaps(userLines) : getUserMaps(userLines));
		List<Map<Integer, Double>> relMaps = new ArrayList<Map<Integer, Double>>();
		for (Map<Integer, Integer> m : maps) {
			double count = getMapCount(m);
			Map<Integer, Double> relM = new LinkedHashMap<Integer, Double>();
			for (Map.Entry<Integer, Integer> entry : m.entrySet()) {
				relM.put(entry.getKey(), (double)entry.getValue().intValue() / count);
			}
			relMaps.add(relM);
		}
		return relMaps;
	}
	
	public static List<Map<Integer, Double>> getDoubleTagMaps(List<Bookmark> userLines, boolean resource) {
		List<Map<Integer, Integer>> maps = (resource ? getResMaps(userLines) : getUserMaps(userLines));
		List<Map<Integer, Double>> relMaps = new ArrayList<Map<Integer, Double>>();
		for (Map<Integer, Integer> m : maps) {
			Map<Integer, Double> relM = new LinkedHashMap<Integer, Double>();
			for (Map.Entry<Integer, Integer> entry : m.entrySet()) {
				relM.put(entry.getKey(), entry.getValue().doubleValue());
			}
			relMaps.add(relM);
		}
		return relMaps;
	}

	
	public static List<Map<Integer, Double>> getRelativeTopicMaps(List<Bookmark> userLines, boolean resource) {
		List<Map<Integer, Integer>> maps = (resource ? getResTopics(userLines) : getUserTopics(userLines));
		List<Map<Integer, Double>> relMaps = new ArrayList<Map<Integer, Double>>();
		for (Map<Integer, Integer> m : maps) {
			double count = getMapCount(m);
			Map<Integer, Double> relM = new LinkedHashMap<Integer, Double>();
			for (Map.Entry<Integer, Integer> entry : m.entrySet()) {
				relM.put(entry.getKey(), (double)entry.getValue().intValue() / count);
			}
			relMaps.add(relM);
		}
		return relMaps;
	}
	
	public static List<Map<Integer, Double>> getNormalizedMaps(List<Bookmark> userLines, boolean resource) {
		List<Map<Integer, Integer>> maps = (resource ? getResMaps(userLines) : getUserMaps(userLines));
		List<Map<Integer, Double>> relMaps = new ArrayList<Map<Integer, Double>>();
		for (Map<Integer, Integer> m : maps) {
			double denom = getMapDenom(m);
			Map<Integer, Double> relM = new LinkedHashMap<Integer, Double>();
			for (Map.Entry<Integer, Integer> entry : m.entrySet()) {
				relM.put(entry.getKey(), Math.exp((double)entry.getValue().intValue()) / denom);
			}
			relMaps.add(relM);
		}
		return relMaps;
	}
	
	public static List<List<Bookmark>> getBookmarks(List<Bookmark> lines, boolean resource) {
		List<List<Bookmark>> bookmarks = new ArrayList<List<Bookmark>>();
		for (Bookmark data : lines) {
			int id = (resource ? data.getWikiID() : data.getUserID());
			List<Bookmark> b = null;
			if (id >= bookmarks.size()) {
				b = new ArrayList<Bookmark>();
				bookmarks.add(b);
			} else {
				b = bookmarks.get(id);
			}
			b.add(data);
		}
		return bookmarks;
	}
	
	public static List<Map<Integer, Double>> getUsedEntities(List<Bookmark> lines, boolean resource, List<Map<Integer, Double>> valueMaps) {
		List<Map<Integer, Double>> entities = new ArrayList<Map<Integer, Double>>();
		for (Bookmark data : lines) {
			int id = (resource ? data.getWikiID() : data.getUserID());
			int entityID = (resource ? data.getUserID() : data.getWikiID());
			Map<Integer, Double> values = null;
			if (valueMaps != null && id < valueMaps.size()) {
				values = valueMaps.get(id);
			}
			
			Map<Integer, Double> e = null;
			if (id >= entities.size()) {
				e = new LinkedHashMap<Integer, Double>();
				entities.add(e);
			} else {
				e = entities.get(id);
			}
			double val = 1.0;
			if (values != null) {
				val = 0.0;
				for (int t : data.getTags()) {
					Double v = values.get(t);
					if (v != null) {
						val += v.doubleValue();
					}
				}
			}
			Double oldVal = e.get(entityID);
			e.put(entityID, oldVal != null ? oldVal.doubleValue() + val : val);
		}
		return entities;
	}
	
	
	// returns a Map<TagId, frequency> for all lines in userLines
	public static Map<Integer, Integer> getTags(List<Bookmark> userLines) {
		Map<Integer, Integer> resTags = new LinkedHashMap<Integer, Integer>();
		for (Bookmark data : userLines) {
			for (Integer tag : data.getTags()){
				int count = resTags.containsKey(tag) ? resTags.get(tag) : 0;
				resTags.put(tag, count + 1);
			} 
		}
		return resTags;
	}
	
	public static double getMapCount(Map<Integer, Integer> map) {
		double sum = 0.0;
		if (map != null) {
			for (Integer val : map.values()) {
				sum += (double)val;
			}
		}
		return sum;
	}
	
	public static double getMapDenom(Map<Integer, Integer> map) {
		double sum = 0.0;
		if (map != null) {
			for (Integer val : map.values()) {
				sum += Math.exp((double)val);
			}
		}
		return sum;
	}
	
	public static double getSmoothedTagValue(double userVal, double userTagCount, double resVal, double resTagCount, double pt) {
		userVal = Math.log(userTagCount + 1.0) / Math.log(2.0) * userVal +
				  Math.log(resTagCount + 1.0) / Math.log(2.0) * pt;
		resVal = Math.log(resTagCount + 1.0) / Math.log(2.0) * resVal +
				 Math.log(userTagCount + 1.0) / Math.log(2.0) * pt;
		return userVal * resVal / pt;
	}
	
	public static double getJaccardSim(Map<Integer, Integer> targetMap, Map<Integer, Integer> nMap) {
		Set<Integer> unionSet = new HashSet<Integer>(targetMap.keySet());
		Set<Integer> intersectSet = new HashSet<Integer>(targetMap.keySet());
		unionSet.addAll(nMap.keySet());
		intersectSet.retainAll(nMap.keySet());
		if (intersectSet.size() == 0 || unionSet.size() == 0)
			return 0.0;
		
		return (double)intersectSet.size() / (double)unionSet.size();
	}
	
	public static double getJaccardSimLists(List<Integer> targetMap, List<Integer> nMap) {
		Set<Integer> unionSet = new HashSet<Integer>(targetMap);
		Set<Integer> intersectSet = new HashSet<Integer>(targetMap);
		unionSet.addAll(nMap);
		intersectSet.retainAll(nMap);
		return (double)intersectSet.size() / (double)unionSet.size();
	}
	
	public static double getJaccardFloatSim(Map<Integer, Double> targetMap, Map<Integer, Double> nMap) {
		Set<Integer> unionSet = new HashSet<Integer>(targetMap.keySet());
		Set<Integer> intersectSet = new HashSet<Integer>(targetMap.keySet());
		unionSet.addAll(nMap.keySet());
		intersectSet.retainAll(nMap.keySet());
		return (double)intersectSet.size() / (double)unionSet.size();
	}
	
	public static double getCosineSim(Map<Integer, Integer> targetMap, Map<Integer, Integer> nMap) {
        Set<Integer> both = new HashSet<Integer>(targetMap.keySet());
        both.retainAll(nMap.keySet());
        double scalar = 0.0, norm1 = 0.0, norm2 = 0.0;
        for (int k : both) scalar += (targetMap.get(k) * nMap.get(k));
        for (int k : targetMap.keySet()) norm1 += (targetMap.get(k) * targetMap.get(k));
        for (int k : nMap.keySet()) norm2 += (nMap.get(k) * nMap.get(k));
        return scalar / Math.sqrt(norm1 * norm2);
	}
	
	public static double getCosineSimList(List<Integer> targetList, List<Integer> nList) {
		Map<Integer, Integer> targetMap = getMapForList(targetList);
		Map<Integer, Integer> nMap = getMapForList(nList);
        Set<Integer> both = new HashSet<Integer>(targetMap.keySet());
        both.retainAll(nMap.keySet());
        double scalar = 0.0, norm1 = 0.0, norm2 = 0.0;
        for (int k : both) scalar += (targetMap.get(k) * nMap.get(k));
        for (int k : targetMap.keySet()) norm1 += (targetMap.get(k) * targetMap.get(k));
        for (int k : nMap.keySet()) norm2 += (nMap.get(k) * nMap.get(k));
        return scalar / Math.sqrt(norm1 * norm2);
	}
	
	public static double getCosineFloatSim(Map<Integer, Double> targetMap, Map<Integer, Double> nMap) {
        Set<Integer> both = new HashSet<Integer>(targetMap.keySet());
        both.retainAll(nMap.keySet());
        double scalar = 0.0, norm1 = 0.0, norm2 = 0.0;
        for (int k : both) scalar += (targetMap.get(k) * nMap.get(k));
        for (int k : targetMap.keySet()) norm1 += (targetMap.get(k) * targetMap.get(k));
        for (int k : nMap.keySet()) norm2 += (nMap.get(k) * nMap.get(k));
        return scalar / Math.sqrt(norm1 * norm2);
	}
	
	public static double getPearsonSim(List<Bookmark> userRatings, List<Bookmark> neighborRatings) {
		return PearsonSimilarityCalculator.getPearsonSim(userRatings, neighborRatings);
	}
	
	public static long getBaselineTimestamp(List<Bookmark> testLines, int refID, boolean resource) {
		if (testLines != null) {
			long maxTimestamp = -1;
			for (Bookmark data : testLines) {
				int id = resource ? data.getWikiID() : data.getUserID();
				if (id == refID) {
					long timestamp = Long.parseLong(data.getTimestamp());
					if (timestamp > maxTimestamp) {
						maxTimestamp = timestamp;
					}
				}
			}
			return maxTimestamp;
		}
		return System.currentTimeMillis() / 1000;
	}
	
	public static long getBaselineTimestampEff(List<Bookmark> testLines, int refID) {
		if (testLines != null) {
			for (int i = testLines.size() - 1; i >= 0; i--) {
				Bookmark data = testLines.get(i);
				if (data.getUserID() == refID) {
					return Long.parseLong(data.getTimestamp());
				}
			}
			return -1;
		}
		return System.currentTimeMillis() / 1000;
	}
	
	public static Map<Integer, Integer> getMapForList(List<Integer> list) {
		Map<Integer, Integer> map = new LinkedHashMap<Integer, Integer>();
		for (Integer val : list) {
			map.put(val, 1);
		}
		return map;
	}
	
	public static Map<Integer, Double> getAllEntities(List<Bookmark> trainList, boolean resource) {
		Map<Integer, Double> allEntities = new LinkedHashMap<Integer, Double>();
		for (Bookmark data : trainList) {
			if (resource) {
				allEntities.put(data.getWikiID(), 0.0);
			} else {
				allEntities.put(data.getUserID(), 0.0);
			}
		}
		return allEntities;
	}
	
	public static Map<Integer, Double> getNeighbors(int userID, int resID, Map<Integer, Double> allNeighbors, List<Map<Integer, Double>> userMaps, List<Bookmark> trainList, Similarity sim) {
		Map<Integer, Double> neighbors = new LinkedHashMap<Integer, Double>();
		//List<Map<Integer, Integer>> neighborMaps = new ArrayList<Map<Integer, Integer>>();
		Map<Integer, Double> targetMap = null;	
		
		// get all users that have tagged the resource
		if (resID != -1) {
			for (Bookmark data : trainList) {
				if (data.getUserID() != userID) {
					if (data.getWikiID() == resID) {
						neighbors.put(data.getUserID(), 0.0);
					}
				}
			}
		}
		// if list is empty, use all users		
		if (neighbors.size() == 0) {
			//for (Bookmark data : trainList) {
			//	if (data.getUserID() != userID) {
			//		neighbors.put(data.getUserID(), 0.0);
			//	}
			//}
			neighbors.putAll(allNeighbors);
		}
		
		if (userID < userMaps.size()) {
			targetMap = userMaps.get(userID);
		} else {
			return neighbors;
		}
		//for (int id : neighbors.keySet()) {
		//	neighborMaps.add(this.userMaps.get(id));
		//}
		// double lAverage = getLAverage(neighborMaps);
		for (Map.Entry<Integer, Double> entry : neighbors.entrySet()) {
			Map<Integer, Double> nMap = userMaps.get(entry.getKey());
			//if (userID != entry.getKey() && !nMap.isEmpty()) {
				Double bm25Value = (sim == Similarity.JACCARD ? Utilities.getJaccardFloatSim(targetMap, nMap) : Utilities.getCosineFloatSim(targetMap, nMap));
				//if (resID == -1) {
				//	bm25Value = Math.pow(bm25Value.doubleValue(), 3);
				//}
				// getBM25Value(neighborMaps, lAverage, targetMap,
				// this.userMaps.get(entry.getKey()));
				if (!bm25Value.isInfinite() && !bm25Value.isNaN()) {
					entry.setValue(bm25Value);
				}
			//}
		}

		// return the sorted neighbors
		Map<Integer, Double> sortedNeighbors = new TreeMap<Integer, Double>(new DoubleMapComparator(neighbors));
		sortedNeighbors.putAll(neighbors);
		return sortedNeighbors;
	}
	
	public static Map<Integer, Double> getSimResources(int userID, int resID, List<Integer> userResources, Map<Integer, Double> allResources, List<Map<Integer, Double>> resMaps, List<Bookmark> trainList, Similarity sim) {
		Map<Integer, Double> resources = new LinkedHashMap<Integer, Double>();
		Map<Integer, Double> targetMap = null;	
		if (resID < resMaps.size()) {
			targetMap = resMaps.get(resID);
		}
		if (targetMap == null || targetMap.isEmpty()) {
			resources.putAll(allResources);
			return resources;
		}
		
		// get all resources that have been tagged by the user
		if (userID != -1) {
			for (Bookmark data : trainList) {
				if (data.getWikiID() != resID) {
					if (data.getUserID() == userID) {
						resources.put(data.getWikiID(), 0.0);
					}
				}
			}
		}
		// if list is empty, use all resources		
		if (resources.size() == 0) {
			resources.putAll(allResources);
		}
		
		for (Map.Entry<Integer, Double> entry : resources.entrySet()) {
			Map<Integer, Double> rMap = resMaps.get(entry.getKey());
			if (!userResources.contains(entry.getKey()) && !rMap.isEmpty()) {
				double bm25Value = (sim == Similarity.JACCARD ? Utilities.getJaccardFloatSim(targetMap, rMap) :
					Utilities.getCosineFloatSim(targetMap, rMap));
				entry.setValue(bm25Value);
			}
		}

		// return the sorted resources
		Map<Integer, Double> sortedResources = new TreeMap<Integer, Double>(new DoubleMapComparator(resources));
		sortedResources.putAll(resources);
		return sortedResources;
	}
	
	public static Map<Integer, Double> getSimResourcesForUser(int userID, Map<Integer, Double> allResources, List<Map<Integer, Double>> userMaps, List<Map<Integer, Double>> resMaps, 
			List<Bookmark> trainList, List<Integer> userResources, Similarity sim) {
		Map<Integer, Double> resources = new LinkedHashMap<Integer, Double>();
		Map<Integer, Double> targetMap = null;	
		if (userID < userMaps.size()) {
			targetMap = userMaps.get(userID);
		}
		if (targetMap == null || targetMap.isEmpty()) {
			return resources;
		}
		resources.putAll(allResources);
		
		for (Map.Entry<Integer, Double> entry : resources.entrySet()) {
			Map<Integer, Double> rMap = resMaps.get(entry.getKey());
			if (!rMap.isEmpty() && !userResources.contains(entry.getKey())) {
				double bm25Value = (sim == Similarity.JACCARD ? Utilities.getJaccardFloatSim(targetMap, rMap) :
					Utilities.getCosineFloatSim(targetMap, rMap));
				entry.setValue(bm25Value);
			}
		}
		
		// return the sorted resources
		Map<Integer, Double> sortedResources = new TreeMap<Integer, Double>(new DoubleMapComparator(resources));
		sortedResources.putAll(resources);
		return sortedResources;
	}
}
