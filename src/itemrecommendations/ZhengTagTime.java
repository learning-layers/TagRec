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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import processing.ActCalculator;
import common.Bookmark;
import common.Utilities;
import file.BookmarkReader;

/**
 * Tag-weighted, time-weighted and combined strategy used by Zheng & Li
 * @author elacic
 *
 */
public class ZhengTagTime {
	
	Map<Integer, Map<Integer, List<Integer>>> userResourceTagMaping;
	Map<Integer, Map<Integer, Long>> userResourceTimeMaping;
	
	Map<Integer, Long> userMaxTimeMaping;
	Map<Integer, Long> userMinTimeMaping;


	public ZhengTagTime(List<Bookmark> trainList) {
		userResourceTagMaping = new HashMap<Integer, Map<Integer,List<Integer>>>();
		userResourceTimeMaping = new HashMap<Integer, Map<Integer, Long>>();
		
		userMaxTimeMaping = new HashMap<Integer, Long>();
		userMinTimeMaping = new HashMap<Integer, Long>();

		for (Bookmark trainData : trainList) {
			Integer userID = trainData.getUserID();
			Integer resID = trainData.getWikiID();
			List<Integer> tags = trainData.getTags();
			Long day = TimeUnit.SECONDS.toDays(Long.parseLong(trainData.getTimestamp()));
			
			Map<Integer, List<Integer>> resourceTagMaping = userResourceTagMaping.get(userID);
			Map<Integer, Long> resourceTimeMaping = userResourceTimeMaping.get(userID);
			
			if (resourceTagMaping == null) {
				resourceTagMaping = new HashMap<Integer, List<Integer>>();
			}
			if (resourceTimeMaping == null) {
				resourceTimeMaping = new HashMap<Integer, Long>();
			}
			
			resourceTagMaping.put(resID, tags);
			resourceTimeMaping.put(resID, day);
			
			Long maxUserTime = userMaxTimeMaping.get(userID);
			Long minUserTime = userMinTimeMaping.get(userID);
			if (maxUserTime == null) {
				maxUserTime = day;
				minUserTime = day;
			} else {
				if (maxUserTime < day) {
					maxUserTime = day;
				}
				if (minUserTime > day) {
					minUserTime = day;
				}
			}
			userMaxTimeMaping.put(userID, maxUserTime);
			userMinTimeMaping.put(userID, minUserTime);
			
			userResourceTagMaping.put(userID, resourceTagMaping);
			userResourceTimeMaping.put(userID, resourceTimeMaping);
		}
	}
	
	/**
	 * Gets the tag weight for a resource of a user
	 * @param userID user
	 * @param resourceID respource with tags from user
	 * @return tag weight
	 */
	public Double getTagWeight(Integer userID, Integer resourceID) {
		if (! userResourceTagMaping.containsKey(userID) || 
				! userResourceTagMaping.get(userID).containsKey(resourceID)) {
			return 0.0;
		}
		//get user's tag for the provided resource
		Map<Integer, List<Integer>> resourceTagMaping = userResourceTagMaping.get(userID);
		List<Integer> tags = resourceTagMaping.get(resourceID);
		
		Double resourceTagScore = 0.0;
		
		List<Integer> tagsByUser = new ArrayList<Integer>();
		Set<Integer> uniqueTagsUsedByUser = new HashSet<Integer>();
		
		for (List<Integer> tagsAtResource : resourceTagMaping.values()) {
			tagsByUser.addAll(tagsAtResource);
			uniqueTagsUsedByUser.addAll(tagsAtResource);
		}
		
		for (Integer tag : tags) {
			resourceTagScore += getTagScore(userID, tag, tagsByUser, uniqueTagsUsedByUser);
		}
		
		return resourceTagScore;
	}
	
	/**
	 * Calculates the tag score for the tag-weighted strategy
	 * @param userID the user using the provided tag
	 * @param tag the tag for which the tag score will be calculated
	 * @param uniqueTagsUsedByUser unique tags used by the user
	 * @param tagsByUser all tags used by the user (a tag can occur multiple times if it has been used at more resources)
	 * @return tag score
	 */
	private Double getTagScore(int userID, int tag, 
			List<Integer> tagsByUser, Set<Integer> uniqueTagsUsedByUser) {
		Double tagScore = (double) Collections.frequency(tagsByUser, tag);
		
		Integer frequencySum = 0;
		
		for (Integer uniqueTagUsed : uniqueTagsUsedByUser) {
			frequencySum += Collections.frequency(tagsByUser, uniqueTagUsed);
		}
		
		return tagScore / (double) frequencySum;
	}
	
	/**
	 * Calculates the time weight which denotes the degree with which a user's interests have declined
	 * @param userID the user
	 * @param resourceID
	 * @return
	 */
	public Double getTimeWeight(int userID, int resourceID) {
		
		Long minTime = userMinTimeMaping.get(userID);
		Long maxTime = userMaxTimeMaping.get(userID);
		if (minTime == null && maxTime == null) {
			minTime = 0L;
			maxTime = 1L;
		}
		if (minTime == maxTime) {
			minTime = 0L;
		}
		double hlu = maxTime - minTime; // TODO: half life of each user
		
		return Math.exp( - Math.log(2) * getTime(userID, resourceID) / hlu );
	}
	
	/**
	 * Takes the value of 0 for the last tagging day of a user, 1 for the penultimate tagging day, etc.
	 * In other words, the maximum day of the user (the last day) minus the day of tagging the resource
	 * @param userId
	 * @param resourceID
	 * @return
	 */
	private Long getTime(int userId, int resourceID) {
		Long time = 0L;
		if (userResourceTimeMaping.containsKey(userId) && userResourceTimeMaping.get(userId).containsKey(resourceID)) {
			time = userMaxTimeMaping.get(userId) - userResourceTimeMaping.get(userId).get(resourceID);
		}
		return time;
	}
	
	/**
	 * Calculates the combined tag-time weight
	 * @param userID user
	 * @param resourceID resource of the user
	 * @param lambda parameter to adjust the significance of the tag and time weights
	 * @return tag-time weight
	 */
	public Double getTagTimeWeight(int userID, int resourceID, double lambda) {
		return (lambda * getTagWeight(userID, resourceID)) + ((1 - lambda) * getTimeWeight(userID, resourceID));
	}
	
	/**
	 * Calculates the combined tag-time weight
	 * @param tagWeight 
	 * @param timeWeight
	 * @param lambda
	 * @return
	 */
	public Double getTagTimeWeight(Double tagWeight, Double timeWeight, double lambda) {
		return (lambda * tagWeight) + ((1 - lambda) * timeWeight);
	}
}
