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

import org.apache.commons.lang3.StringEscapeUtils;

import com.google.common.primitives.Ints;

import file.BookmarkReader;

public class Utilities {

	private final static String REV_START = "<rev xml:space=\"preserve\">";
	private final static String REV_END = "</rev>";
	
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
	
	public static Set<Integer> getUsersByResource(List<UserData> userLines, int resID) {
		Set<Integer> userList = new HashSet<Integer>();
		for (UserData data : userLines) {
			if (data.getWikiID() == resID) {
				userList.add(data.getUserID());
			}
		}
		return userList;
	}
	
	public static List<Set<Integer>> getUserResourceLists(List<UserData> userLines) {
		List<Set<Integer>> userLists = new ArrayList<Set<Integer>>();
		for (UserData data : userLines) {
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
	
	public static List<Map<Integer, Integer>> getUserMaps(List<UserData> userLines) {
		List<Map<Integer, Integer>> userMaps = new ArrayList<Map<Integer, Integer>>();
		for (UserData data : userLines) {
			int userID = data.getUserID();
			if (userID >= userMaps.size()) {
				userMaps.add(Utilities.mergeListWithMap(data.getTags(), new LinkedHashMap<Integer, Integer>()));
			} else {
				Utilities.mergeListWithMap(data.getTags(), userMaps.get(userID));
			}
		}
		return userMaps;
	}
	
	public static List<Map<Integer, Double>> getRelativeMaps(List<UserData> userLines, boolean resource) {
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
	
	public static List<Map<Integer, Double>> getNormalizedMaps(List<UserData> userLines, boolean resource) {
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
	
	public static List<List<UserData>> getBookmarks(List<UserData> lines, boolean resource) {
		List<List<UserData>> bookmarks = new ArrayList<List<UserData>>();
		for (UserData data : lines) {
			int id = (resource ? data.getWikiID() : data.getUserID());
			List<UserData> b = null;
			if (id >= bookmarks.size()) {
				b = new ArrayList<UserData>();
				bookmarks.add(b);
			} else {
				b = bookmarks.get(id);
			}
			b.add(data);
		}
		return bookmarks;
	}
	
	public static List<Map<Integer, Integer>> getResMaps(List<UserData> userLines) {
		List<Map<Integer, Integer>> resMaps = new ArrayList<Map<Integer, Integer>>();
		for (UserData data : userLines) {
			int resID = data.getWikiID();
			if (resID >= resMaps.size()) {
				resMaps.add(Utilities.mergeListWithMap(data.getTags(), new LinkedHashMap<Integer, Integer>()));
			} else {
				Utilities.mergeListWithMap(data.getTags(), resMaps.get(resID));
			}
		}
		return resMaps;
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
	
	public static double getCosineFloatSim(Map<Integer, Double> targetMap, Map<Integer, Double> nMap) {
        Set<Integer> both = new HashSet<Integer>(targetMap.keySet());
        both.retainAll(nMap.keySet());
        double scalar = 0.0, norm1 = 0.0, norm2 = 0.0;
        for (int k : both) scalar += (targetMap.get(k) * nMap.get(k));
        for (int k : targetMap.keySet()) norm1 += (targetMap.get(k) * targetMap.get(k));
        for (int k : nMap.keySet()) norm2 += (nMap.get(k) * nMap.get(k));
        return scalar / Math.sqrt(norm1 * norm2);
	}
	
	public static long getBaselineTimestamp(List<UserData> testLines, int refID) {
		if (testLines != null) {
			long maxTimestamp = -1;
			for (UserData data : testLines) {
				if (data.getUserID() == refID) {
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
	
	public static long getBaselineTimestampEff(List<UserData> testLines, int refID) {
		if (testLines != null) {
			for (int i = testLines.size() - 1; i >= 0; i--) {
				UserData data = testLines.get(i);
				if (data.getUserID() == refID) {
					return Long.parseLong(data.getTimestamp());
				}
			}
			return -1;
		}
		return System.currentTimeMillis() / 1000;
	}
}
