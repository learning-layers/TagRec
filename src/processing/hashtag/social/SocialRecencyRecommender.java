package processing.hashtag.social;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import common.DoubleMapComparator;

public class SocialRecencyRecommender {

	private HashMap<String, HashMap<Integer, ArrayList<Long>>> userTagTimes;
	private HashMap<String, ArrayList<String>> network;
	private List<String> users;

	/**
	 * Constructor 
	 * @param userTagTimes the map of users to the tags to the timestamp list.
	 * @param network the underlying user network.
	 * @param users list of userids.
	 * */
	public SocialRecencyRecommender(HashMap<String, HashMap<Integer, ArrayList<Long>>> userTagTimes, 
			HashMap<String, ArrayList<String>> network, List<String> users){
			this.userTagTimes = userTagTimes;
			this.network = network;
			this.users = users;
	}
	
	/**
	 * Get the weighted map of the tags for a given userid and timestamp. 
	 * @param userId integer user id of the user for which we want to get the recommendation.
	 * @param timestamp corresponding to which we want to get the recommendation.
	 * @return {@link Map} A map of tag to the weight of the tag for given users and timestamp.
	 **/
	public Map<Integer, Double> getRankedTagListSocialRecency(int userId, long timestamp, boolean sort) {
		HashMap<Integer, ArrayList<Long>> tagTimestampMapAllFriend = getAllFriendTagTimestampsMapSocialRecency(userId);
		HashMap<Integer, Long> mostRecentTimestampMap = getMostRecentTagTimestampSocialRecency(timestamp,
				tagTimestampMapAllFriend);
		Long maxValue = getMaxValue(mostRecentTimestampMap);
		HashMap<Integer, Double> resultMap = getNormalisedTagWeightSocialRecency(mostRecentTimestampMap, maxValue);		
		if (sort) {
			Map<Integer, Double> sortedResultMap = new TreeMap<Integer, Double>(new DoubleMapComparator(resultMap));
			sortedResultMap.putAll(resultMap);
			return sortedResultMap;
		} else {
			return resultMap;
		}
	}
	
	/**
	 * Merge all the tag timestamps for a users in a single Hashmap.
	 * @param userId user id of user for whom we want to get the tag timestamp map for all her friend.
	 * @return {@link HashMap} of tags and timestamp list of their usage.
	 * */
	private HashMap<Integer, ArrayList<Long>> getAllFriendTagTimestampsMapSocialRecency(int userId) {
		HashMap<Integer, ArrayList<Long>> tagTimestampMapAllFriend = new HashMap<Integer, ArrayList<Long>>();
		String user = this.users.get(userId);
		List<String> friendList = network.get(user);
		if (friendList != null) {
			for (String friend : friendList) {
				HashMap<Integer, ArrayList<Long>> tagTimestmapMapSingleFriend = userTagTimes.get(friend);
				if (tagTimestmapMapSingleFriend != null) {
					for (Integer tag : tagTimestmapMapSingleFriend.keySet()) {
						if (!tagTimestampMapAllFriend.containsKey(tag)) {
							tagTimestampMapAllFriend.put(tag, new ArrayList<Long>());
						} else {
							tagTimestampMapAllFriend.get(tag).addAll(tagTimestmapMapSingleFriend.get(tag));
						}
					}
				}
			}
		}
		sortHashMapListSocialRecency(tagTimestampMapAllFriend);
		return tagTimestampMapAllFriend;
	}

	/**
	 * From the list of tag and their timestamp, get the Hashmap of tag mapped to it most recent timestamp 
	 * since the occurence of the given timestamp.
	 * @param timestamp the timestamp when the given 
	 * @param tagTimestampMapAllFriend get the tag to timestamps map for all the friend of a user.
	 * @return {@link HashMap} of tag to the most recent timestamp
	 **/
	private HashMap<Integer, Long> getMostRecentTagTimestampSocialRecency(long timestamp,
			HashMap<Integer, ArrayList<Long>> tagTimestampMapAllFriend) {
		HashMap<Integer, Long> mostRecentTimestampMap = new HashMap<Integer, Long>();
		for (Integer tag : tagTimestampMapAllFriend.keySet()) {
			ArrayList<Long> tagTimestampList = tagTimestampMapAllFriend.get(tag);
			for (Long tagTimestamp : tagTimestampMapAllFriend.get(tag)) {
				if (tagTimestamp > timestamp) {
					mostRecentTimestampMap.put(tag, tagTimestamp - timestamp);
					break;
				} else {
					continue;
				}
			}
		}
		return mostRecentTimestampMap;
	}

	/**
	 * Sort the {@link ArrayList} in the value part of the {@link HashMap}
	 * @param tagTimestampMapAllFriend a map containing the tag and timestamps list.
	 * @return {@link Void}
	 ***/
	private void sortHashMapListSocialRecency(HashMap<Integer, ArrayList<Long>> tagTimestampMapAllFriend) {
		for (Integer tag : tagTimestampMapAllFriend.keySet()) {
			Collections.sort(tagTimestampMapAllFriend.get(tag));
		}
	}

	/**
	 * Get the normalised values of the corresponding to the maximum value provided as the parameter.
	 * @param mostRecentTimestampMap most recent timestamp value when a hashtag is used corresponding to the given timestamp.
	 * @param maxValue maxValue of the recent timestamp.
	 * @return {@link HashMap} A hashmap which contains the normalised weights of the tags.
	 * **/
	private HashMap<Integer, Double> getNormalisedTagWeightSocialRecency(HashMap<Integer, Long> mostRecentTimestampMap,
			Long maxValue) {
		HashMap<Integer, Double> resultMap = new HashMap<Integer, Double>();
		for (Integer tag : mostRecentTimestampMap.keySet()) {
			Double weight = (double) maxValue - mostRecentTimestampMap.get(tag);
			resultMap.put(tag, weight);
		}
		return resultMap;
	}

	/**
	 * Get the maximum value among all the values in a HashMap of comparable values.
	 * @param mostRecentTimestampMap {@link HashMap} the hashmap with each value corresponding 
	 * the timestamp when the hashmap was last used.
	 * @return the maximum value among all the Long values in value list.
	 * */
	private Long getMaxValue(HashMap<Integer, Long> mostRecentTimestampMap) {
		Long maxValue = new Long(0);
		for (Integer tag : mostRecentTimestampMap.keySet()) {
			if (maxValue < mostRecentTimestampMap.get(tag)) {
				maxValue = mostRecentTimestampMap.get(tag);
			} else {
				continue;
			}
		}
		return maxValue;
	}
}
