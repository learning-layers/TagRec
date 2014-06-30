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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Calculator for the Pearson similarity between two user ratings
 * @author elacic
 *
 */
public class PearsonSimilarityCalculator {
	
	/**
	 * Calculates the Pearson-similarity based on the resource ratings of a user and the resource ratings of his neighbor
	 * @param userRatings resource ratings of the user
	 * @param neighborRatings resource ratings of the user's neighbor
	 * @return the Pearson similarity
	 */
	public static double getPearsonSim(List<Bookmark> userRatings, List<Bookmark> neighborRatings) {
		double differenceSum = 0.0;
		double userSquareDifferenceSum = 0.0;
		double neighboorSquareDifferenceSum = 0.0;
		
		Map<Integer, List<Double>> coratedItems = new HashMap<Integer, List<Double>>();
		
		double userRatingAverage = calculateAverageAndFillCoratedItems(userRatings, coratedItems);
		double neighboorRatingAverage = calculateAverageAndFillCoratedItems(neighborRatings, coratedItems);
		
		for (Integer resourceId : coratedItems.keySet()) {
			List<Double> resourceRatings = coratedItems.get(resourceId);
			
			if (resourceRatings.size() < 2) {
				continue;
			}
			
			Double userResourceRating = resourceRatings.get(0);
			Double neighboorResourceRating = resourceRatings.get(1);
			
			differenceSum += (userResourceRating - userRatingAverage) * (neighboorResourceRating - neighboorRatingAverage);
			
			userSquareDifferenceSum += Math.pow( (userResourceRating - userRatingAverage) , 2);
			neighboorSquareDifferenceSum += Math.pow( (neighboorResourceRating - neighboorRatingAverage) , 2);
		}
		
		return differenceSum / (Math.sqrt(userSquareDifferenceSum) * Math.sqrt(neighboorSquareDifferenceSum));
	}

	/**
	 * Calculates the average of a user's ratings and fills the coratedItems map
	 * @param userRatings
	 * @param coratedItems
	 * @return
	 */
	private static double calculateAverageAndFillCoratedItems(List<Bookmark> userRatings, Map<Integer, List<Double>> coratedItems) {
		double ratingSum = 0.0;
		for (Bookmark userRating : userRatings) {
			double filteredRating = filterRating(userRating.getRating());
			ratingSum += filteredRating;
			
			fillCoratedItems(coratedItems, userRating.getWikiID(), filteredRating);
		}
		return ratingSum / userRatings.size();
	}

	/**
	 * Fills the mapping on the rating for a resource
	 * @param coratedItems mapping for resource rating which will be filled
	 * @param resource resource that is rated
	 * @param filteredRating rating for the resource
	 */
	private static void fillCoratedItems(Map<Integer, List<Double>> coratedItems, Integer resource, double filteredRating) {
		List<Double> resourceRating = null;
		
		if (coratedItems.containsKey(resource)) {
			resourceRating = coratedItems.get(resource);
		} else {
			resourceRating = new ArrayList<Double>();
		}
		
		resourceRating.add(filteredRating);
		coratedItems.put(resource, resourceRating);
	}
	
	/**
	 * For read only ratings which were set to -1 by convention in the CUL dataset
	 * @param rating rating to be filtered
	 * @return filtered rating
	 */
	private static double filterRating(double rating) {
		if (rating == -1) {
			return 3;
		}
		
		return rating;
	}
}
