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
public class ZhengCalculator {
	public static int MAX_NEIGHBORS = 20;
	private static final double LAMBDA = 0.5;

	private List<Bookmark> bookmarks;
	private Similarity similarity;

	private List<Bookmark> trainList;
	private ZhengTagTime zhengApproach;

	private Map<Integer, Double> allUsersSimilarities;
	private List<Map<Integer, Double>> userResourcesTagWeight;
	private List<Map<Integer, Double>> userResourcesTimeWeight;
	private List<Map<Integer, Double>> userResourcesTagTimeWeight;

	/**
	 * Constructor with needed data for calculating recommendations
	 * @param reader contains train data
	 * @param sim measure which defines how to calculate similarity between two users
	 * @param trainSize size of the train set
	 */
	public ZhengCalculator(BookmarkReader reader, Similarity sim, int trainSize) {
		bookmarks = reader.getBookmarks();
		similarity = sim;
		
		trainList = bookmarks.subList(0, trainSize);
		
		zhengApproach = new ZhengTagTime(trainList);
		System.out.println("Constructed Zheng approach class");
		
		allUsersSimilarities = Utilities.getAllEntities(trainList, false);
		
		userResourcesTagWeight = new ArrayList<Map<Integer, Double>>();
		userResourcesTimeWeight = new ArrayList<Map<Integer, Double>>();
		userResourcesTagTimeWeight = new ArrayList<Map<Integer, Double>>();
		
		fillZhengWeights();
		System.out.println("Filled user - resource weights.");
	}
	
	public static List<Map<Integer, Double>> createTagTimeMapping(List<Bookmark> trainList){
		List<Map<Integer, Double>> userResourcesTagTimeWeight = new ArrayList<Map<Integer, Double>>();
		ZhengTagTime zhengApproach = new ZhengTagTime(trainList);
		for (Bookmark data : trainList) {
			int user = data.getUserID();
			int resource = data.getWikiID();
			
			Map<Integer, Double> resourceTagTimeWeights = null;
			
			if (user >= userResourcesTagTimeWeight.size()) {
				resourceTagTimeWeights = new LinkedHashMap<Integer, Double>();
				userResourcesTagTimeWeight.add(resourceTagTimeWeights);
			} else {
				resourceTagTimeWeights = userResourcesTagTimeWeight.get(user);
			}
			
			Double tagTimeWeight = zhengApproach.getTagTimeWeight(user, resource, LAMBDA);

			resourceTagTimeWeights.put(resource, tagTimeWeight);
		}
		
		return userResourcesTagTimeWeight;
	}
	
	public static Map<Integer, Double> createUserTagTimeMapping(int userID, List<Bookmark> trainList){
		Map<Integer, Double> userResourcesTagTimeWeight = new LinkedHashMap<Integer, Double>();
		ZhengTagTime zhengApproach = new ZhengTagTime(trainList);
		for (Bookmark data : trainList) {
			int user = data.getUserID();
			int resource = data.getWikiID();
			
			if (user == userID) {
				Double tagTimeWeight = zhengApproach.getTagTimeWeight(user, resource, LAMBDA);
				userResourcesTagTimeWeight.put(resource, tagTimeWeight);
			}
		}
		
		return userResourcesTagTimeWeight;
	}

	/**
	 * Fills for every user a map which contains weights (based on tag, time and tag-time score) for his resources
	 */
	private void fillZhengWeights() {
		for (Bookmark data : trainList) {
			int user = data.getUserID();
			int resource = data.getWikiID();
			
			Map<Integer, Double> resourceTagWeights = null;
			Map<Integer, Double> resourceTimeWeights = null;
			Map<Integer, Double> resourceTagTimeWeights = null;
			
			if (user >= userResourcesTagWeight.size()) {
				resourceTagWeights = new LinkedHashMap<Integer, Double>();
				resourceTimeWeights = new LinkedHashMap<Integer, Double>();
				resourceTagTimeWeights = new LinkedHashMap<Integer, Double>();
				userResourcesTagWeight.add(resourceTagWeights);
				userResourcesTimeWeight.add(resourceTimeWeights);
				userResourcesTagTimeWeight.add(resourceTagTimeWeights);
			} else {
				resourceTagWeights = userResourcesTagWeight.get(user);
				resourceTimeWeights = userResourcesTimeWeight.get(user);
				resourceTagTimeWeights = userResourcesTagTimeWeight.get(user);
			}
			
			Double tagWeight = zhengApproach.getTagWeight(user, resource);
			Double timeWeight = zhengApproach.getTimeWeight(user, resource);
			Double tagTimeWeight = zhengApproach.getTagTimeWeight(tagWeight, timeWeight, LAMBDA);

			resourceTagWeights.put(resource, tagWeight);
			resourceTimeWeights.put(resource, timeWeight);
			resourceTagTimeWeights.put(resource, tagTimeWeight);
			
		}
	}
	


	/**
	 * Calculates results based on the tag approach from Zheng
	 * @param userID id of the user to generate recommendations
	 * @param sorting if the map should be sorted by ranking score
	 * @return ranked map with recommended resources
	 */
	public Map<Integer, Double> getRankedResourcesListByTag(int userID, boolean sorting) {
		return getRankedResourcesList(userID, userResourcesTagWeight, sorting);
	}
	
	/**
	 * Calculates results based on the time approach from Zheng
	 * @param userID id of the user to generate recommendations
	 * @param sorting if the map should be sorted by ranking score
	 * @return ranked map with recommended resources
	 */
	public Map<Integer, Double> getRankedResourcesListByTime(int userID, boolean sorting) {
		return getRankedResourcesList(userID, userResourcesTimeWeight, sorting);
	}
	
	/**
	 * Calculates results based on the tag - time approach from Zheng
	 * @param userID id of the user to generate recommendations
	 * @param sorting if the map should be sorted by ranking score
	 * @return ranked map with recommended resources
	 */
	public Map<Integer, Double> getRankedResourcesListByTagTime(int userID, boolean sorting) {
		return getRankedResourcesList(userID, userResourcesTagTimeWeight, sorting);
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
		
		// get candidates
		int i = 0;
		Map<Integer, Double> sortedNeighbors = 
				Utilities.getNeighbors(userID, -1, allUsersSimilarities, userResourcesWeights, trainList, similarity);
		
		Double userSimiliaritySum = 0.0;
		
		for (Map.Entry<Integer, Double> neighbor : sortedNeighbors.entrySet()) {
			if (i++ > MAX_NEIGHBORS) {
				break;
			}
//			System.out.println("Neighbour: " + neighbor);
			double userSimVal = neighbor.getValue();
			userSimiliaritySum += userSimVal;
			
			if (userSimVal != 0.0) {
				List<Integer> resources = Bookmark.getResourcesFromUser(trainList, neighbor.getKey());
				
				for (Integer resID : resources) {
					if (! userResourcesTagWeight.get(userID).containsKey(resID)) {
						Double resourceScore = candidateResources.get(resID);
						
						Double resourceWeight = userResourcesWeights.get(neighbor.getKey()).get(resID) * userSimVal;
//								scoreCalculator.getScore(neighbor.getKey(), resID) * userSimVal;
						
						resourceScore = (resourceScore != null) ? (resourceScore + resourceWeight) : resourceWeight;

						candidateResources.put(resID, resourceScore);
					}
				}
			}
		}
		
		for (Integer resource : candidateResources.keySet()) {
			double resourceScore = candidateResources.get(resource) / Math.abs(userSimiliaritySum);
			candidateResources.put(resource, resourceScore);
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
	
	
	// Statics -----------------------------------------------------------------------------------------------------------------------------------------------------------	

	private static List<Map<Integer, Double>> startZhengTagCreationForResourcesPrediction(
			BookmarkReader reader, int trainSize, ZhengCalculator calculator) {
		List<Map<Integer, Double>> results = new ArrayList<Map<Integer, Double>>();
		
		for (Integer userID : reader.getUniqueUserListFromTestSet(trainSize)) {
			results.add(calculator.getRankedResourcesListByTag(userID, true));
		}
	
		return results;
	}
	
	private static List<Map<Integer, Double>> startZhengTimeCreationForResourcesPrediction(
			BookmarkReader reader, int trainSize, ZhengCalculator calculator) {
		List<Map<Integer, Double>> results = new ArrayList<Map<Integer, Double>>();
		
		for (Integer userID : reader.getUniqueUserListFromTestSet(trainSize)) {
			results.add(calculator.getRankedResourcesListByTime(userID, true));
		}
	
		return results;
	}
	
	private static List<Map<Integer, Double>> startZhengTagTimeCreationForResourcesPrediction(
			BookmarkReader reader, int trainSize, ZhengCalculator calculator) {
		List<Map<Integer, Double>> results = new ArrayList<Map<Integer, Double>>();
		
		for (Integer userID : reader.getUniqueUserListFromTestSet(trainSize)) {
			results.add(calculator.getRankedResourcesListByTagTime(userID, true));
		}
	
		return results;
	}
	
	public static BookmarkReader predictSample(String filename, int trainSize) {
		// read input
		//filename += "_res";
		BookmarkReader reader = new BookmarkReader(trainSize, false);
		reader.readFile(filename);
		
		ZhengCalculator calculator = new ZhengCalculator(reader, Similarity.COSINE, trainSize);

//		// get recommendations
//		List<Map<Integer, Double>> tagValues = startZhengTagCreationForResourcesPrediction(reader, trainSize, calculator);
//		// write results
//		writeResults(filename, trainSize, "_zheng_tag", reader, tagValues);
//		
//		// get recommendations
//		List<Map<Integer, Double>> timeValues = startZhengTimeCreationForResourcesPrediction(reader, trainSize, calculator);
//		// write results
//		writeResults(filename, trainSize, "_zheng_time", reader, timeValues);

		// get recommendations
		List<Map<Integer, Double>> tagTimeValues = startZhengTagTimeCreationForResourcesPrediction(reader, trainSize, calculator);
		// write results
		writeResults(filename, trainSize, "_zheng_tagtime", reader, tagTimeValues);
			
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
