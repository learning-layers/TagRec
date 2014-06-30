/*
 TagRecommender:
 A framework to implement and evaluate algorithms for the recommendation
 of tags.
 Copyright (C) 2013 Dominik Kowald, Emanuel Lacic
 
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

package itemrecommendations;

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

import processing.ActCalculator;

import com.google.common.base.Stopwatch;
import com.google.common.primitives.Ints;

import common.DoubleMapComparator;
import common.Features;
import common.Similarity;
import common.Bookmark;
import common.Utilities;

import file.PredictionFileWriter;
import file.BookmarkReader;

public class Resource3LTCalculator {
	
	public static int MAX_NEIGHBORS = 20;
	
	private BookmarkReader reader;
	private List<Bookmark> trainList;
	private List<Bookmark> testList;	
	private Similarity sim;
	private Features features;
	private boolean userSim;
	private boolean bll;
	private boolean novelty;

	private List<Map<Integer, Double>> userMaps;
	//private List<Map<Integer, Double>> userTags;
	private List<Map<Integer, Double>> userTopics;
	private Map<Integer, Double> allUsers;
	//private List<Map<Integer, Double>> resMaps;
	//private List<Map<Integer, Double>> resTags;
	private List<Map<Integer, Double>> resTopics;
	// private Map<Integer, Double> allResources;
	
	private List<Map<Integer, Double>> bllValues;

	private boolean calculateOnTags;
	
	public Resource3LTCalculator(BookmarkReader reader, int trainSize, int sampleSize, Similarity sim, Features features, 
			boolean userSim, boolean bll, boolean novelty, boolean calculateOnTags) {
		this.reader = reader;
		this.trainList = this.reader.getBookmarks().subList(0, trainSize);
		this.testList = this.reader.getBookmarks().subList(trainSize, trainSize + sampleSize);
		this.sim = sim;
		this.features = features;
		this.userSim = userSim;
		this.bll = bll;
		this.novelty = novelty;
		this.calculateOnTags = calculateOnTags;

		if (this.features == Features.ENTITIES) {
			// TODO: try tag values for cosine!
			this.userMaps = Utilities.getUsedEntities(this.trainList, false, null);
		} else if (this.features == Features.TAGS) {
			this.userMaps = Utilities.getRelativeTagMaps(this.trainList, false);
		} else {
			this.userMaps = Utilities.getRelativeTopicMaps(this.trainList, false);
		}
		this.userTopics = Utilities.getUniqueTopicMaps(this.trainList, false);//Utilities.getRelativeTopicMaps(this.trainList, false);
		this.allUsers = Utilities.getAllEntities(this.trainList, false);
		if (this.bll) {
			this.bllValues = ActCalculator.getArtifactMaps(reader, this.trainList, this.testList, false, new ArrayList<Long>(), new ArrayList<Double>(), 0.5, true);
		}
		
		/*if (this.features == Features.ENTITIES) {
			this.resMaps = Utilities.getUsedEntities(this.trainList, true, null);
		} else if (this.features == Features.TAGS) {
			this.resMaps = Utilities.getRelativeTagMaps(this.trainList, true);
		} else {
			this.resMaps = Utilities.getRelativeTopicMaps(this.trainList, true);
		}*/		
		//this.resTopics = Utilities.getUniqueTopicMaps(this.trainList, true); //Utilities.getUsedEntities(this.trainList, true, null); // 
		this.resTopics = Utilities.getUsedEntities(this.trainList, true, null); 
		//this.resTopics = Utilities.getRelativeTagMaps(this.trainList, true);
		//this.resTopics = Utilities.getRelativeTopicMaps(this.trainList, true);
		
		//this.allResources = Utilities.getAllEntities(this.trainList, true);
	}
		
	private Map<Integer, Double> getRankedResourcesList(int userID, boolean sorting) {
		Map<Integer, Double> rankedResources = new LinkedHashMap<Integer, Double>();
		Map<Integer, Double> candidateResources = new LinkedHashMap<Integer, Double>();
		// TODO: check if Resources are modeled with BLL values of tags
		
		Map<Integer, Double> userResources = null;
		if (calculateOnTags) {
			userResources = Bookmark.getResourcesFromUserWithBLL(this.trainList, this.testList, userID, this.bllValues);
		} else {
			userResources = Bookmark.getResourcesFromUserWithRec(trainList, testList, userID, 0.5, false);
		}
		

		
		Map<Integer, Double> targetUserTopics = this.userTopics.get(userID);
		
		// get candidates
		int i = 0;
		Map<Integer, Double> sortedNeighbors = Utilities.getNeighbors(userID, -1, this.allUsers, this.userMaps, this.trainList, this.sim);
		for (Map.Entry<Integer, Double> neighbor : sortedNeighbors.entrySet()) {
			if (i++ > MAX_NEIGHBORS) {
				break;
			}
			double userSimVal = neighbor.getValue();
//			if (userSimVal != 0.0) {
				Map<Integer, Double> resources = Bookmark.getResourcesFromUserWithBLL(this.trainList, this.testList, neighbor.getKey(), this.bllValues);
//				Map<Integer, Double> resources =  Bookmark.getResourcesFromUserWithRec(trainList, testList, neighbor.getKey(), 0.5, false);
				for (Integer resID : resources.keySet()) {
					if (!userResources.containsKey(resID)) { // exclude already known resources
						Double val = candidateResources.get(resID);
						candidateResources.put(resID, (val != null ? val + userSimVal : userSimVal ));
					}
				}
//			}
		}
		
		// rank cadidates
		for (Map.Entry<Integer, Double> candidateRes : candidateResources.entrySet()) {
			Map<Integer, Double> candidateTopics = this.resTopics.get(candidateRes.getKey());
			double echoVal = 0.0;
			for (Map.Entry<Integer, Double> userRes : userResources.entrySet()) {
				Map<Integer, Double> targetTopics = this.resTopics.get(userRes.getKey());
				Double resSimVal = Utilities.getCosineFloatSim(targetTopics, candidateTopics);
				//resSimVal = Math.pow(resSimVal, 3);
				if (!resSimVal.isNaN() && !resSimVal.isInfinite() && resSimVal.doubleValue() > 0.0) {
					double bllVal = userRes.getValue().doubleValue() > 0 ? userRes.getValue().doubleValue() : 1.0;
					double currentEcho = resSimVal * bllVal;
					//if (this.novelty) {	// here?					
					//}
					echoVal += currentEcho;
				}
			}
			// TODO: use importance of this resource
			if (this.userSim) {
				echoVal *= candidateRes.getValue();
			}
			if (this.novelty) {
				double novValue = 1.0 - Utilities.getCosineFloatSim(targetUserTopics, candidateTopics);
				echoVal *= novValue;
			}
			rankedResources.put(candidateRes.getKey(), echoVal);
		}
		
		/*
		System.out.print("\"" + this.reader.getUsers().get(userID) + "\";\"");
		int count = 1;
		for (int candRes : candidateResources.keySet()) {
			System.out.print(this.reader.getResources().get(candRes));
			if (count++ < candidateResources.keySet().size()) {
				System.out.print(",");
			}
		}
		System.out.print("\"\n");
		*/
		
//		denom = 0.0;
//		// normalize
//		for (double val : rankedResources.values()) {
//			denom += Math.exp(val);
//		}
//		for (Map.Entry<Integer, Double> entry : rankedResources.entrySet()) {
//			entry.setValue(Math.exp(entry.getValue()) / denom);
//		}
		if (sorting) {
			// return the sorted resources
			Map<Integer, Double> sortedRankedResources = new TreeMap<Integer, Double>(new DoubleMapComparator(rankedResources));
			sortedRankedResources.putAll(rankedResources);
			return sortedRankedResources;
		} else {
			return rankedResources;
		}
	}
	
	// Statics -----------------------------------------------------------------------------------------------------------------------------------------------------------	
	private static List<Map<Integer, Double>> start3LTCreationForResourcesPrediction(BookmarkReader reader, int trainSize, int sampleSize, Features features, 
			boolean userSim, boolean bll, boolean novelty, boolean calculateOnTags) {
		int size = reader.getBookmarks().size();
		Resource3LTCalculator calculator = new Resource3LTCalculator(reader, trainSize, sampleSize, Similarity.BM25, features, userSim, bll, novelty, calculateOnTags);
		
		List<Map<Integer, Double>> results = new ArrayList<Map<Integer, Double>>();
		for (Integer userID : reader.getUniqueUserListFromTestSet(trainSize)) {
			Map<Integer, Double> map = null;
			map = calculator.getRankedResourcesList(userID, true);
			results.add(map);
		}
	
		return results;
	}
	
	public static BookmarkReader predictSample(String filename, int trainSize, int sampleSize, int neighborSize, Features features, 
			boolean userSim, boolean bll, boolean novelty, boolean calculateOnTags) {
		MAX_NEIGHBORS = neighborSize;
		
		// read input
		//filename += "_res";
		BookmarkReader reader = new BookmarkReader(trainSize, false);
		reader.readFile(filename);
		// get recommendations
		List<Map<Integer, Double>> cfValues = null;	
		cfValues = start3LTCreationForResourcesPrediction(reader, trainSize, sampleSize, features, userSim, bll, novelty, calculateOnTags);
		
		// write results
		List<int[]> predictionValues = new ArrayList<int[]>();
		for (int i = 0; i < cfValues.size(); i++) {
			Map<Integer, Double> modelVal = cfValues.get(i);
			predictionValues.add(Ints.toArray(modelVal.keySet()));
		}		
		String suffix = "_r3l_" + features;
		if (bll) {
			suffix += "_bll";
		}
		PredictionFileWriter writer = new PredictionFileWriter(reader, predictionValues);
		writer.writeResourcePredictionsToFile(filename + suffix, trainSize, MAX_NEIGHBORS);
			
		return reader;
	}
}
