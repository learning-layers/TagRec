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
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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
public class HuangApproach {
	
	public static final double WEIGHT = 0.5;
	public static final double ALPHA = 1.0;
	
	Map<Integer, Map<Integer, List<Integer>>> userResourceTagMaping;
	Map<Integer, Map<Integer, List<Integer>>> resourceUserTagMaping;

	
	Map<Integer, Long> userFirstMapping;
	Map<Integer, Long> userLastMapping;

	Map<Integer, Map<Integer, Long>> userTagFirstMapping;
	Map<Integer, Map<Integer, Long>> userTagLastMapping;
	
	
	public Map<Integer, Map<Integer, List<Integer>>> getUserResourceTagMaping() {
		return userResourceTagMaping;
	}

	public Map<Integer, Map<Integer, List<Integer>>> getResourceUserTagMaping() {
		return resourceUserTagMaping;
	}

	public HuangApproach(List<Bookmark> trainList) {
		userResourceTagMaping = new HashMap<Integer, Map<Integer,List<Integer>>>();
		resourceUserTagMaping = new HashMap<Integer, Map<Integer,List<Integer>>>();

		userFirstMapping = new HashMap<Integer, Long>();
		userLastMapping = new HashMap<Integer, Long>();

		userTagFirstMapping = new HashMap<Integer, Map<Integer, Long>>();
		userTagLastMapping = new HashMap<Integer, Map<Integer, Long>>();

		
		for (Bookmark trainData : trainList) {
			Integer userID = trainData.getUserID();
			Integer resID = trainData.getWikiID();
			List<Integer> tags = trainData.getTags();
			Long day = TimeUnit.SECONDS.toDays(Long.parseLong(trainData.getTimestamp()));
			
			Map<Integer, List<Integer>> resourceTagMaping = userResourceTagMaping.get(userID);
			Map<Integer, List<Integer>> userTagMaping = resourceUserTagMaping.get(resID);

			
			if (resourceTagMaping == null) {
				resourceTagMaping = new HashMap<Integer, List<Integer>>();
			}
			if (userTagMaping == null) {
				userTagMaping = new HashMap<Integer, List<Integer>>();
			}
			
			fillUserFirst(userID, day);
			fillUserLast(userID, day);

			fillTagFirst(userID, tags, day);
			fillTagLast(userID, tags, day);
			
			resourceTagMaping.put(resID, tags);
			userTagMaping.put(userID, tags);
			
			userResourceTagMaping.put(userID, resourceTagMaping);
			resourceUserTagMaping.put(resID, userTagMaping);
		}
	}

	private void fillUserFirst(Integer userID, Long day) {
		if (! userFirstMapping.containsKey(userID) || day < userFirstMapping.get(userID)) {
			userFirstMapping.put(userID, day);
		}
	}
	
	private void fillUserLast(Integer userID, Long day) {
		if (! userLastMapping.containsKey(userID) || day > userLastMapping.get(userID)) {
			userLastMapping.put(userID, day);
		}
	}

	private void fillTagFirst(Integer userID, List<Integer> tags, Long day) {
		Map<Integer, Long> tagMap = userTagFirstMapping.get(userID);
		
		if (tagMap == null){
			tagMap = new HashMap<Integer, Long>();
		}
		
		for (Integer tag : tags) {
			if( ! tagMap.containsKey(tag) || day < tagMap.get(tag)) {
				tagMap.put(tag, day);
			}
		}
		
		userTagFirstMapping.put(userID, tagMap);
	}
	
	private void fillTagLast(Integer userID, List<Integer> tags, Long day) {
		Map<Integer, Long> tagMap = userTagLastMapping.get(userID);
		
		if (tagMap == null){
			tagMap = new HashMap<Integer, Long>();
		}
		
		for (Integer tag : tags) {
			if( ! tagMap.containsKey(tag) || day > tagMap.get(tag)) {
				tagMap.put(tag, day);
			}
		}
		
		userTagLastMapping.put(userID, tagMap);
	}
	

	
	/**
	 * Gets the tag weight for a given user and tag
	 * @param userID id of the user
	 * @param tag
	 * @return TF-IUF model calculated weight
	 */
	public Double getUserTagWeight(Integer userID, Integer tag) {
		return getUserTagTF(userID, tag) * getTagIUF(tag);
	}
	
	/**
	 * Gets the tag weight for a given resource and tag
	 * @param resourceID if of the resource
	 * @param tag
	 * @return TF-IIF model calculated weight
	 */
	public Double getItemTagWeight(Integer resourceID, Integer tag) {
		return getItemTagTF(resourceID, tag) * getTagIIF(tag);
	}
	

	private Double getUserTagTF(Integer userID, Integer tag) {
		return getTagTF(userID, tag, userResourceTagMaping);
	}
	private Double getTagIUF(Integer tag) {
		return getTagIIF(tag, userResourceTagMaping);
	}
	
	
	private Double getItemTagTF(Integer resourceID, Integer tag) {
		return getTagTF(resourceID, tag, resourceUserTagMaping);
	}
	private Double getTagIIF(Integer tag) {
		return getTagIIF(tag, resourceUserTagMaping);
	}
	
	/**
	 * Calculates the tag TF for an entity mapping
	 * @param entityId
	 * @param tag
	 * @param entityMaping
	 * @return
	 */
	private Double getTagTF(Integer entityId, Integer tag, Map<Integer, Map<Integer, List<Integer>>> entityMaping) {
		if (! entityMaping.containsKey(entityId)) {
			return 0.0;
		}
		
		List<Integer> tagsByEntity = getTagsByEntity(entityId, entityMaping);
		
		return Collections.frequency(tagsByEntity, tag) / (double) maximumFreqency(tagsByEntity);
	}

	private List<Integer> getTagsByEntity(Integer entityId, Map<Integer, Map<Integer, List<Integer>>> entityMaping) {
		Map<Integer, List<Integer>> entityTagMaping = entityMaping.get(entityId);
		
		List<Integer> tagsByEntity = new ArrayList<Integer>();
		
		for (List<Integer> tagsAtEntity : entityTagMaping.values()) {
			tagsByEntity.addAll(tagsAtEntity);
		}
		
		return tagsByEntity;
	}
	
	/**
	 * Calculates IIF for a tag in a entity mapping
	 * @param tag
	 * @param entityMaping
	 * @return
	 */
	private Double getTagIIF(Integer tag, Map<Integer, Map<Integer, List<Integer>>> entityMaping) {
		Set<Integer> allResources = entityMaping.keySet();
		
		Integer totalNumberOfResources = allResources.size();
		Integer numberOfTaggedResources = 0;
		
		for (Integer resID : allResources) {
			for (List<Integer> tags : entityMaping.get(resID).values()) {
				if (tags.contains(tag)) {
					numberOfTaggedResources++;
					break;
				}
			}
		}
		
		return Math.log(totalNumberOfResources / (double) numberOfTaggedResources);
	}
	
	
	/**
	 * Maximum frequency of items in the list
	 * @param list
	 * @return
	 */
	private static <T> Integer maximumFreqency(List<T> list) {
	    Map<T, Integer> map = new HashMap<>();

	    for (T t : list) {
	        Integer val = map.get(t);
	        map.put(t, val == null ? 1 : val + 1);
	    }

	    Entry<T, Integer> max = null;

	    for (Entry<T, Integer> e : map.entrySet()) {
	        if (max == null || e.getValue() > max.getValue())
	            max = e;
	    }

	    return max.getValue();
	}
	
	private double getScaledRecency(Integer user, Integer tag) {
		long current = TimeUnit.SECONDS.toDays(System.currentTimeMillis());
		
		long tagLast = 0; 
		if (userTagLastMapping.containsKey(user) && userTagLastMapping.get(user).containsKey(tag)) {
			tagLast = userTagLastMapping.get(user).get(tag);
		}
		
		long userFirst = userFirstMapping.get(user);
		
		return  1 - ((current - tagLast) / (double) (current - userFirst));
	}
	
	private double getScaledDuration(Integer user, Integer tag) {
		long tagFirst = 0; 
		if (userTagFirstMapping.containsKey(user) && userTagFirstMapping.get(user).containsKey(tag)) {
			tagFirst = userTagFirstMapping.get(user).get(tag);
		}
		
		long tagLast = 0; 
		if (userTagLastMapping.containsKey(user) && userTagLastMapping.get(user).containsKey(tag)) {
			tagLast = userTagLastMapping.get(user).get(tag);
		}
		
		long userFirst = userFirstMapping.get(user);
		
		long userLast = userLastMapping.get(user);
		
		
		return (tagLast - tagFirst) / (double) (userLast - userFirst);
	}
	
	private double getTagFRD(Integer user, Integer tag, List<Integer> tagsByEntity) {
		return Collections.frequency(tagsByEntity, tag) * 
				( ALPHA + WEIGHT * getScaledRecency(user, tag) + (1 - WEIGHT) * getScaledDuration(user, tag) );
	}
	
	public double getScaledTagFRD(Integer user, Integer tag) {
		
		List<Integer> tagsByEntity = getTagsByEntity(user, userResourceTagMaping);
		Set<Integer> uniqueTagsByEntity = new HashSet<Integer>(tagsByEntity);
		
		double maxTagFRD = Integer.MIN_VALUE;
		for (Integer userTag : uniqueTagsByEntity) {
			double uniqueTagFRD = getTagFRD(user, userTag, tagsByEntity);
			maxTagFRD = (uniqueTagFRD > maxTagFRD) ? uniqueTagFRD : maxTagFRD;
		}
		
		return getTagFRD(user, tag, tagsByEntity) / maxTagFRD;
	}
	
	
}