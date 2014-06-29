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
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import com.google.common.base.Stopwatch;
import com.google.common.primitives.Ints;

import common.DoubleMapComparator;
import common.IntMapComparator;
import common.Bookmark;
import common.Utilities;

import file.PredictionFileWriter;
import file.BookmarkReader;
import processing.folkrank.*;

public class FolkRankCalculator {
	
	private static List<int[]> startFolkRankCreationForResources(BookmarkReader reader, int sampleSize) {
		
		int size = reader.getBookmarks().size();
		int trainSize = size - sampleSize;
		List<Map<Integer, Integer>> userMaps = Utilities.getUserMaps(reader.getBookmarks());	
		System.out.println("\nStart FolkRank Calculation for Resources");
		// TODO: should not use whole size!
		//LeavePostOutFolkRankDataDuplicator dupl = new LeavePostOutFolkRankDataDuplicator();
		FactReader factReader = new WikipediaFactReader(reader, size, 3);
		FactPreprocessor prep = new FactReaderFactPreprocessor(factReader);
		prep.process();
		FolkRankData facts = prep.getFolkRankData();
		
        FolkRankParam param = new FolkRankParam();
        FolkRankPref pref = new FolkRankPref(new double[] {1.0, 1.0, 1.0});
        int tagCounts = facts.getCounts()[0].length;
        System.out.println("Tags: " + tagCounts);
        int usrCounts = facts.getCounts()[1].length;
        System.out.println("Users: " + usrCounts);
        int resCounts = facts.getCounts()[2].length;
        System.out.println("Resources: " + resCounts);
        double[] countVals = new double[]{usrCounts, usrCounts, usrCounts, usrCounts, usrCounts};
        double[][] prefWeights = new double[][]{new double[]{}, countVals, new double[]{}};
        
        // start FolkRank        
		List<int[]> results = new ArrayList<int[]>();		
		for (int userID : reader.getUniqueUserListFromTestSet(trainSize)) {
			List<Integer> topResources = new ArrayList<Integer>();
	        int u = userID;
	        int[] tPrefs = new int[]{};
	        int[] uPrefs = getBestUsers(userMaps, u, 5);
	        int[] rPrefs = new int[]{};
	        pref.setPreference(new int[][]{tPrefs, uPrefs, rPrefs}, prefWeights);
	        FolkRankAlgorithm folk = new FolkRankAlgorithm(param);
	        FolkRankResult result = folk.computeFolkRank(facts, pref);
	        SortedSet<ItemWithWeight> topKTags = ItemWithWeight.getTopK(facts, result.getWeights(), 100, 2); // TODO
	        int count = 0;
	        List<Integer> userResources = Bookmark.getResourcesFromUser(reader.getBookmarks().subList(0, trainSize), userID);
	        for (ItemWithWeight item : topKTags) {
	        	if (count < 10) {
	        		if (!userResources.contains(item.getItem())) {
	        			topResources.add(item.getItem());
	        			count++;
	        		}
	        	} else {
	        		break;
	        	}
	        }
	        results.add(Ints.toArray(topResources));
		}
		return results;
	}
	
	private static List<int[]> frResults;
	private static List<int[]> prResults;
	private static String timeString;
	
	private static void startFolkRankCreation(BookmarkReader reader, int sampleSize) {
		timeString = "";
		System.out.println("\nStart FolkRank Calculation for Tags");
		frResults = new ArrayList<int[]>();
		prResults = new ArrayList<int[]>();
		int size = reader.getBookmarks().size();
		int trainSize = size - sampleSize;
		Stopwatch timer = new Stopwatch();
		timer.start();
		FactReader factReader = new WikipediaFactReader(reader, trainSize, 3);
		FactPreprocessor prep = new FactReaderFactPreprocessor(factReader);
		prep.process();
		FolkRankData facts = prep.getFolkRankData();
		
        FolkRankParam param = new FolkRankParam();
        FolkRankPref pref = new FolkRankPref(new double[] {1.0, 1.0, 1.0});
        int usrCounts = facts.getCounts()[1].length;
        System.out.println("Users: " + usrCounts);
        int resCounts = facts.getCounts()[2].length;
        System.out.println("Resources: " + resCounts);
        double[][] prefWeights = new double[][]{new double[]{}, new double[]{usrCounts}, new double[]{resCounts}};      
        FolkRankAlgorithm folk = new FolkRankAlgorithm(param);
        timer.stop();
        long trainingTime = timer.elapsed(TimeUnit.MILLISECONDS);
        
		timer = new Stopwatch();
        // start FolkRank        
		for (int i = trainSize; i < size; i++) {
			timer.start();
			Bookmark data = reader.getBookmarks().get(i);
	        int u = data.getUserID();
	        int[] uPrefs = (u < usrCounts ? new int[]{u} : new int[]{});
	        int r = data.getWikiID();
	        int[] rPrefs = (r < resCounts ? new int[]{r} : new int[]{});
	        pref.setPreference(new int[][]{new int[]{}, uPrefs, rPrefs}, prefWeights);
	        FolkRankResult result = folk.computeFolkRank(facts, pref);
	        
			int[] topTags = new int[10];
	        SortedSet<ItemWithWeight> topKTags = ItemWithWeight.getTopK(facts, result.getWeights(), 10, 0);
	        int count = 0;
	        for (ItemWithWeight item : topKTags) {
	            topTags[count++] = item.getItem();
	        }
	        frResults.add(topTags);
	        timer.stop();
	        
	        int[] topTagsPr = new int[10];
	        SortedSet<ItemWithWeight> topKTagsPr = ItemWithWeight.getTopK(facts, result.getAPRWeights(), 10, 0);
	        count = 0;
	        for (ItemWithWeight item : topKTagsPr) {
	            topTagsPr[count++] = item.getItem();
	        }
	        prResults.add(topTagsPr);
	        //System.out.println(u + "|" + data.getTags().toString().replace("[", "").replace("]", "") + 
	        //					   "|" + Arrays.toString(topTags).replace("[", "").replace("]", "") + 
	        //					   "|" + Arrays.toString(topTagsPr).replace("[", "").replace("]", ""));
		}
		long testTime = timer.elapsed(TimeUnit.MILLISECONDS);
		timeString += ("Full training time: " + trainingTime + "\n");
		timeString += ("Full test time: " + testTime + "\n");
		timeString += ("Average test time: " + testTime / (double)sampleSize) + "\n";
		timeString += ("Total time: " + (trainingTime + testTime) + "\n");
	}
	
	public static void predictSample(String filename, int trainSize, int sampleSize, boolean predictTags) {
		//filename += "_res";
		
		int size = 0;
		if (predictTags) {
			size = trainSize;
		}
		BookmarkReader reader = new BookmarkReader(size, false);
		reader.readFile(filename);
		List<int[]> predictionValues = null;
		List<int[]> prPredictionValues = null;
		if (predictTags) {
			startFolkRankCreation(reader, sampleSize);
			predictionValues = frResults;
			prPredictionValues = prResults;
		} else {
			predictionValues = startFolkRankCreationForResources(reader, sampleSize);
		}
		
		if (predictTags) {
			reader.setUserLines(reader.getBookmarks().subList(trainSize, reader.getBookmarks().size()));
			PredictionFileWriter writer = new PredictionFileWriter(reader, predictionValues);
			writer.writeFile(filename + "_fr");
			PredictionFileWriter prWriter = new PredictionFileWriter(reader, prPredictionValues);
			prWriter.writeFile(filename + "_apr");
			
			Utilities.writeStringToFile("./data/metrics/" + filename + "_fr" + "_TIME.txt", timeString);
		} else {
			PredictionFileWriter writer = new PredictionFileWriter(reader, predictionValues);
			writer.writeResourcePredictionsToFile(filename + "_fr", trainSize, 0);
		}
	}
	
	//-------------------------------------------------------------------------------------------------------------
	private static int getBestTag(List<Map<Integer, Integer>> userMaps, int userID) {
		if (userID < userMaps.size()) {
			Map<Integer, Integer> tags = userMaps.get(userID);
			Map<Integer, Integer> sortedTags = new TreeMap<Integer, Integer>(new IntMapComparator(tags));
			sortedTags.putAll(tags);
			for (int t : sortedTags.keySet()) {
				return t;
			}
		}
		return 0;
	}
	
	// TODO: try BM25
	private static double getOccurences(Map<Integer, Integer> targetMap, Map<Integer, Integer> nMap) {
		double count = 0.0;
		for (int tTag : targetMap.keySet()) {
			if (nMap.containsKey(tTag)) {
				count += nMap.get(tTag);
			}
		}
		return count;
	}
	
	private static int[] getBestUsers(List<Map<Integer, Integer>> userMaps, int userID, int size) {
		int[] retVals = new int[size];
		Map<Integer, Integer> nSimMap = new LinkedHashMap<Integer, Integer>();
		if (userID < userMaps.size()) {
			Map<Integer, Integer> tags = userMaps.get(userID);
			for (int i = 0; i < userMaps.size(); i++) {
				if (i != userID) {
					Map<Integer, Integer> nTags = userMaps.get(i);
					int count = (int)getOccurences(tags, nTags);
					nSimMap.put(i, count);
				}
			}
			Map<Integer, Integer> sortedResults = new TreeMap<Integer, Integer>(new IntMapComparator(nSimMap));
			sortedResults.putAll(nSimMap);
			int j = 0;
			for (int id : sortedResults.keySet()) {
				if (j < size) {
					retVals[j++] = id;
				}
			}
			return retVals;
		}
		return null;
	}
}
