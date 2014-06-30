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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import com.google.common.primitives.Ints;

import common.Bookmark;
import common.DoubleMapComparator;
import common.Features;
import common.Similarity;
import common.Utilities;
import file.BookmarkReader;
import file.PredictionFileWriter;

/**
 * Class for calculating recommendations based on Zheng Tag - Time approach
 * @author elacic
 *
 */
public class HuangCalculator {
	public static int MAX_NEIGHBORS = 20;

	private Similarity similarity;

	private List<Bookmark> trainList;
	
	private HuangApproach huangApproach;

	private Map<Integer, Double> allUsersSimilarities;
	
	private Map<Integer, Map<Integer, Double>> resourceTagWeights;
	private List<Map<Integer, Double>> userTagWeights;

	/**
	 * Constructor with needed data for calculating recommendations
	 * @param reader contains train data
	 * @param sim measure which defines how to calculate similarity between two users
	 * @param trainSize size of the train set
	 */
	public HuangCalculator(BookmarkReader reader, Similarity sim, int trainSize) {
		similarity = sim;
		
		trainList = reader.getBookmarks().subList(0, trainSize);
		
		huangApproach = new HuangApproach(trainList);
		System.out.println("Constructed Huang approach class");
		
		allUsersSimilarities = Utilities.getAllEntities(trainList, false);
		
		resourceTagWeights = new HashMap<Integer, Map<Integer, Double>>();
		userTagWeights = new ArrayList<Map<Integer, Double>>();

		fillChengWeights();
		System.out.println("Filled user - resource weights.");
	}

	/**
	 * Fills for every user a map which contains weights (based on tag, time and tag-time score) for his resources
	 */
	private void fillChengWeights() {
		for (Bookmark data : trainList) {
			int user = data.getUserID();
			int resource = data.getWikiID();
			List<Integer> tags = data.getTags();
			
			Map<Integer, Double> tagUserWeights = null;

			if (user >= userTagWeights.size()) {
				tagUserWeights = new LinkedHashMap<Integer, Double>();
				userTagWeights.add(tagUserWeights);
			} else {
				tagUserWeights = userTagWeights.get(user);
			}
			
			Map<Integer, Double> tagResourceWeights = resourceTagWeights.get(resource);
			if (tagResourceWeights == null) {
				tagResourceWeights = new HashMap<Integer, Double>();
			}
			
			for (Integer tag : tags) {
				if (!tagUserWeights.containsKey(tag)) {
					tagUserWeights.put(tag, huangApproach.getUserTagWeight(user, tag));
				}
				if (!tagResourceWeights.containsKey(tag)) {
					tagResourceWeights.put(tag, huangApproach.getItemTagWeight(resource, tag));
				}
			}
			
			resourceTagWeights.put(resource, tagResourceWeights);
		}
	}
	

	public Map<Integer, Double> getRankedResourcesListByUserWeight(int userID, boolean sorting) {
		return getRankedResourcesList(userID, userTagWeights, sorting);
	}
	
	
	/**
	 * Calculates resources to recommend for a user
	 * @param userID user to recommend resources
	 * @param userResourcesWeights list containing resource weight-map for every user (index of the list is the id of the user)
	 * @param scoreCalculator calculator which calculates the weight-score for a user-id and a resource-id
	 * @param sorting should the returned recommend resource map be sorted based on the resource score
	 * @return ranked map with recommended resources
	 */
	private Map<Integer, Double> getRankedResourcesList(
			int userID,  
			List<Map<Integer, Double>> userResourcesWeights, 
			boolean sorting) {
		Map<Integer, Double> candidateResources = new LinkedHashMap<Integer, Double>();
		
		// find similar users
		int i = 0;
		Map<Integer, Double> sortedNeighbors = 
				Utilities.getNeighbors(userID, -1, allUsersSimilarities, userResourcesWeights, trainList, similarity);
		
		Set<Integer> targetUserResources = huangApproach.getUserResourceTagMaping().get(userID).keySet();
		
		// extract candidate resources
		for (Map.Entry<Integer, Double> neighbor : sortedNeighbors.entrySet()) {
			if (i++ > MAX_NEIGHBORS) {
				break;
			}
//			System.out.println("Neighbour: " + neighbor);
			double userSimVal = neighbor.getValue();
			
			if (userSimVal != 0.0) {
				List<Integer> resources = Bookmark.getResourcesFromUser(trainList, neighbor.getKey());
				
				for (Integer resID : resources) {
					if (! targetUserResources.contains(resID)) {
						Double resourceScore = candidateResources.get(resID);
						resourceScore = (resourceScore != null) ? (resourceScore + userSimVal) : userSimVal;
						candidateResources.put(resID, resourceScore);
					}
				}
			}
		}
		
		Map<Integer, Double> resourceMaxScaledTagFRD = new HashMap<Integer, Double>();
		
		// rank candidate resources with item similarity and scaled tag FRD
		for (Map.Entry<Integer, Double> candidateRes : candidateResources.entrySet()) {
			Map<Integer, Double> candidateResourceTags = resourceTagWeights.get(candidateRes.getKey());
			
			double candidateSimilarity = 0.0;
			for (Integer targetUserRes : targetUserResources) {
				// only in first iteration of target resources calculate target user's resource FDR
				if (! resourceMaxScaledTagFRD.containsKey(targetUserRes)) {
					fillMaxScaledTagFRD(userID, resourceMaxScaledTagFRD, targetUserRes);
				}
				
				Map<Integer, Double> targetResourceTags = resourceTagWeights.get(targetUserRes);
				
				Double resSimVal = Utilities.getCosineFloatSim(targetResourceTags, candidateResourceTags);
				candidateSimilarity += (HuangApproach.WEIGHT * resSimVal) + ((1 - HuangApproach.WEIGHT) * resourceMaxScaledTagFRD.get(targetUserRes));
			}
			candidateResources.put(candidateRes.getKey(), candidateSimilarity);
		}
		
		if (sorting) {
			// return the sorted resources
			Map<Integer, Double> sortedRankedResources = new TreeMap<Integer, Double>(new DoubleMapComparator(candidateResources));
			sortedRankedResources.putAll(candidateResources);
			return sortedRankedResources;
		} else {
			return candidateResources;
		}
	}

	private void fillMaxScaledTagFRD(int userID, Map<Integer, Double> resourceMaxScaledTagFRD, Integer targetUserRes) {
		List<Integer> targetResTags = huangApproach.getUserResourceTagMaping().get(userID).get(targetUserRes);
		
		double maxScaledTagFRD = Double.MIN_VALUE;
		
		for (Integer tag : targetResTags) {
			double scaledTagFRD = huangApproach.getScaledTagFRD(userID, tag);
			maxScaledTagFRD = (maxScaledTagFRD < scaledTagFRD) ? scaledTagFRD : maxScaledTagFRD;
		}
		
		resourceMaxScaledTagFRD.put(targetUserRes, maxScaledTagFRD);
	}
	
	
	// Statics -----------------------------------------------------------------------------------------------------------------------------------------------------------	

	private static List<Map<Integer, Double>> startHuangUserProfile(BookmarkReader reader, int trainSize, HuangCalculator calculator) {
		List<Map<Integer, Double>> results = new ArrayList<Map<Integer, Double>>();
		
		for (Integer userID : reader.getUniqueUserListFromTestSet(trainSize)) {
			results.add(calculator.getRankedResourcesListByUserWeight(userID, true));
		}
	
		return results;
	}
	
	public static BookmarkReader predictSample(String filename, int trainSize) {
		// read input
		//filename += "_res";
		BookmarkReader reader = new BookmarkReader(trainSize, false);
		reader.readFile(filename);
		
		HuangCalculator calculator = new HuangCalculator(reader, Similarity.COSINE, trainSize);
		// get recommendations
		List<Map<Integer, Double>> tagValues = startHuangUserProfile(reader, trainSize, calculator);
		// write results
		writeResults(filename, trainSize, "_huang_tag_user", reader, tagValues);
		
		return reader;
	}

	private static void writeResults(String filename, int trainSize, String suffix,
			BookmarkReader reader, List<Map<Integer, Double>> zhengValues) {
		List<int[]> predictionValues = new ArrayList<int[]>();
		
		for (int i = 0; i < zhengValues.size(); i++) {
			Map<Integer, Double> modelVal = zhengValues.get(i);
			predictionValues.add(Ints.toArray(modelVal.keySet()));
		}		
		
		PredictionFileWriter writer = new PredictionFileWriter(reader, predictionValues);
		writer.writeResourcePredictionsToFile(filename + suffix, trainSize, MAX_NEIGHBORS);
	}

}
