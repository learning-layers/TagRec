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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.common.base.Stopwatch;
import com.google.common.primitives.Ints;

import common.DoubleMapComparator;
import common.UserData;
import common.Utilities;

import file.PredictionFileWriter;
import file.BookmarkReader;

public class BM25Calculator {
	
	public static int MAX_NEIGHBORS = 20;
	private final static double K1 = 1.2;
	private final static double K3 = 1.2;
	private final static double B = 0.8;
	
	private BookmarkReader reader;
	private int trainSize;
	private boolean userBased;
	private boolean resBased;
	private double beta;
	private List<UserData> trainList;
	private List<Map<Integer, Integer>> userMaps;
	private List<Map<Integer, Integer>> resMaps;
	
	public BM25Calculator(BookmarkReader reader, int trainSize, boolean predictTags, boolean userBased, boolean resBased, int beta) {
		this.reader = reader;
		this.trainSize = trainSize;
		this.userBased = userBased;
		this.resBased = resBased;
		this.beta = (double)beta / 10.0;
		this.trainList = this.reader.getUserLines().subList(0, predictTags ? trainSize : reader.getUserLines().size());
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
					List<Integer> tags = UserData.getUserData(this.trainList, entry.getKey(), resID).getTags();
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
					List<Integer> tags = UserData.getResData(this.trainList, userID, entry.getKey()).getTags();
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
		for (UserData data : this.trainList) {
			if (data.getUserID() != userID) {
				if (resID == -1) {
					neighbors.put(data.getUserID(), 0.0);
				} else if (data.getWikiID() == resID) {
					neighbors.put(data.getUserID(), 0.0);
				}
			}
		}
		//boolean allUsers = false;
		// if list is empty, use all users		
		if (neighbors.size() == 0) {
			//allUsers = true;
			for (UserData data : this.trainList) {
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
		for (UserData data : this.trainList) {
			if (data.getWikiID() != resID) {
				if (userID == -1) {
					resources.put(data.getWikiID(), 0.0);
				} else if (data.getUserID() == userID) {
					resources.put(data.getWikiID(), 0.0);
				}
			}
		}
		// if list is empty, use all users		
		if (resources.size() == 0) {
			for (UserData data : this.trainList) {
				resources.put(data.getWikiID(), 0.0);
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
	
	// ----------------------------------------------------------------------------------------------------------------------------
	
	/**
	 * recommend resources for given user
	 * @param userID
	 */
	private Map<Integer, Double> getRankedResourcesList(int userID) {
		List<Integer> userResources = UserData.getResourcesFromUser(this.trainList.subList(0, trainSize), userID);
		Map<Integer, Double> sortedNeighbors = getNeighbors(userID, -1);
		Map<Integer, Double> rankedResources = new LinkedHashMap<Integer, Double>();
		
		int i=0;
		for (Map.Entry<Integer, Double> neighbor : sortedNeighbors.entrySet()) {		
			if (i++ > MAX_NEIGHBORS) {
				break;
			}	
			List<Integer> resources = UserData.getResourcesFromUser(this.trainList, neighbor.getKey());			
			double bm25 = neighbor.getValue();		
			for (Integer resID : resources) {
				if (!userResources.contains(resID)) {
					Double val = rankedResources.get(resID);
					rankedResources.put(resID, (val != null ? val + bm25 : bm25));		
					//System.out.println("add resource to list - " + resID + " " + (val != null ? val + bm25 : bm25));
				}
			}
		}
		
		for (Map.Entry<Integer, Double> entry : rankedResources.entrySet()) {
			entry.setValue(Math.log10(1 + this.reader.getResourceCounts().get(entry.getKey())) * entry.getValue());
		}	
		// return the sorted resources
		Map<Integer, Double> sortedRankedResources = new TreeMap<Integer, Double>(new DoubleMapComparator(rankedResources));
		sortedRankedResources.putAll(rankedResources);
		return sortedRankedResources;
	}
	
	public static double getBM25Value(List<Map<Integer, Integer>> neighborMaps, double lAverage,
								Map<Integer, Integer> targetMap, Map<Integer, Integer> nMap) {
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
	
	// --------------------------------------------------------------------------------------------------------
	
	public static void predictTags(String filename, int trainSize, int sampleSize, int neighbors, boolean userBased, boolean resBased, int beta) {
		MAX_NEIGHBORS = neighbors;
		predictSample(filename, trainSize, sampleSize, true, userBased, resBased, beta);
	}
	
	public static void predictSample(String filename, int trainSize, int sampleSize, boolean predictTags, boolean userBased, boolean resBased, int beta) {
		//filename += "_res";
		
		int size = 0;
		if (predictTags) {
			size = trainSize;
		}
		BookmarkReader reader = new BookmarkReader(size, false); // TODO
		reader.readFile(filename);
		
		List<Map<Integer, Double>> cfValues = null;	
		if (predictTags) {
			cfValues = startBM25CreationForTagPrediction(reader, sampleSize, userBased, resBased, beta);
		} else {
			cfValues = startBM25CreationForResourcesPrediction(reader, sampleSize);
		}
		
		List<int[]> predictionValues = new ArrayList<int[]>();
		for (int i = 0; i < cfValues.size(); i++) {
			Map<Integer, Double> modelVal = cfValues.get(i);
			predictionValues.add(Ints.toArray(modelVal.keySet()));
		}		
		if (predictTags) {
			String suffix = "_cf_";
			if (!userBased) {
				suffix = "_rescf_";
			} else if (!resBased) {
				suffix = "_usercf_";
			}
			reader.setUserLines(reader.getUserLines().subList(trainSize, reader.getUserLines().size()));
			PredictionFileWriter writer = new PredictionFileWriter(reader, predictionValues);
			String outputFile = filename + suffix + beta;
			writer.writeFile(outputFile);
			
			Utilities.writeStringToFile("./data/metrics/" + outputFile + "_TIME.txt", timeString);
		} else {
			PredictionFileWriter writer = new PredictionFileWriter(reader, predictionValues);
			writer.writeResourcePredictionsToFile(filename + "_cf", trainSize, MAX_NEIGHBORS);
		}
	}
	
	private static String timeString;
	
	private static List<Map<Integer, Double>> startBM25CreationForTagPrediction(BookmarkReader reader, int sampleSize, boolean userBased, boolean resBased, int beta) {
		timeString = "";
		int size = reader.getUserLines().size();
		int trainSize = size - sampleSize;
		Stopwatch timer = new Stopwatch();
		timer.start();
		BM25Calculator calculator = new BM25Calculator(reader, trainSize, true, userBased, resBased, beta);
		timer.stop();
		long trainingTime = timer.elapsed(TimeUnit.MILLISECONDS);
		
		List<Map<Integer, Double>> results = new ArrayList<Map<Integer, Double>>();
		timer = new Stopwatch();
		timer.start();
		for (int i = trainSize; i < size; i++) {
			UserData data = reader.getUserLines().get(i);
			Map<Integer, Double> map = null;
			map = calculator.getRankedTagList(data.getUserID(), data.getWikiID(), true);
			results.add(map);
			//System.out.println(data.getTags() + "|" + map.keySet());
		}
		timer.stop();
		long testTime = timer.elapsed(TimeUnit.MILLISECONDS);
		timeString += ("Full training time: " + trainingTime + "\n");
		timeString += ("Full test time: " + testTime + "\n");
		timeString += ("Average test time: " + testTime / (double)sampleSize) + "\n";
		timeString += ("Total time: " + (trainingTime + testTime) + "\n");
	
		return results;
	}	
	
	
	//--------------------------------------------------------------------------------------------------------------------------
	public static void predictResources(String filename, int trainSize, int sampleSize, int neighborSize) {
		MAX_NEIGHBORS = neighborSize;
		predictSample(filename, trainSize, sampleSize, false, true, false, 5);
	}
	
	private static List<Map<Integer, Double>> startBM25CreationForResourcesPrediction(BookmarkReader reader, int sampleSize) {
		int size = reader.getUserLines().size();
		int trainSize = size - sampleSize;
		BM25Calculator calculator = new BM25Calculator(reader, trainSize, false, true, true, 5); // TODO
		
		List<Map<Integer, Double>> results = new ArrayList<Map<Integer, Double>>();
		for (Integer userID : reader.getUniqueUserListFromTestSet(trainSize)) {
			Map<Integer, Double> map = null;
			map = calculator.getRankedResourcesList(userID);
			results.add(map);
		}
	
		return results;
	}
}
