package processing.hashtag.social;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import cc.mallet.util.FileUtils;
import common.DoubleMapComparator;

public class SocialLinkWeightCalculator {

	private HashMap<String, HashMap<Integer, ArrayList<Long>>> userTagTimes;
	private HashMap<String, ArrayList<String>> network;
	private List<String> users;
	private HashMap<String, HashMap<String, ArrayList<Long>>> mapUserMentionTimestamps;
	private HashMap<String, HashMap<String, ArrayList<Long>>> mapUserRetweetTimestamps;
	private HashMap<String, HashMap<String, ArrayList<Long>>> mapUserReplyTimestamps;

	public SocialLinkWeightCalculator(HashMap<String, HashMap<Integer, ArrayList<Long>>> userTagTimes,
			HashMap<String, ArrayList<String>> network, List<String> users, String mentionFilename,
			String retweetFilename, String replyFilename) {
		this.userTagTimes = userTagTimes;
		this.network = network;
		this.users = users;
		populateUserRelationMap(mentionFilename, retweetFilename, replyFilename);
	}

	/**
	 * Get a ranked weight of tag and their score.
	 * 
	 * @param userId
	 *            the userId of the user for whom we want to get the
	 *            recommendation.
	 * @param timestamp
	 *            the long value of timestamp for which you want to get the
	 *            recommendation.
	 * @param whether
	 *            the returned map should be in the sorted order of values.
	 **/
	public Map<Integer, Double> getRankedTagListSocialLinkWeight(int userId, long timestamp, boolean sort) {
		String user = this.users.get(userId);
		List<String> friendList = network.get(user);
		HashMap<Integer, Double> tagRank = new HashMap<Integer, Double>();
		Map<Integer, Double> sortedResultMap = new HashMap<Integer, Double>();
		if (friendList == null) {
			System.out.println("friendlist is null >> ");
			sortedResultMap = new TreeMap<Integer, Double>(new DoubleMapComparator(tagRank));
			sortedResultMap.putAll(tagRank);
		} else {
			System.out.println("friendlist not null >> ");
			tagRank = getTagWeightMap(user, friendList, timestamp);
			tagRank = getNormalizedTagWeightMap(tagRank);
			sortedResultMap = new TreeMap<Integer, Double>(new DoubleMapComparator(tagRank));
			sortedResultMap.putAll(tagRank);
		}
		System.out.println("sorted Result Map >> " + sortedResultMap);
		return sortedResultMap;
	}

	/**
	 * Get normalised tag weight map.
	 * 
	 * @param
	 * @return {@link HashMap} of noramlised values of the tag weights.
	 **/
	private HashMap<Integer, Double> getNormalizedTagWeightMap(HashMap<Integer, Double> tagRank) {
		double denom = 0.0;
		if (tagRank != null) {
			for (Map.Entry<Integer, Double> entry : tagRank.entrySet()) {
				if (entry != null) {
					double actVal = Math.log(entry.getValue());
					denom += Math.exp(actVal);
					entry.setValue(actVal);
				}
			}
			for (Map.Entry<Integer, Double> entry : tagRank.entrySet()) {
				if (entry != null) {
					double actVal = Math.exp(entry.getValue());
					entry.setValue(actVal / denom);
				}
			}
		}
		return tagRank;
	}

	/**
	 * Get the tag weight.
	 * 
	 * @param friendList
	 *            {@link List} of friends for a given user.
	 * @param tagRank
	 *            the ran
	 **/
	private HashMap<Integer, Double> getTagWeightMap(String user, List<String> friendList,
			long timestamp) {
		HashMap<Integer, Double> tagRank = new HashMap<Integer, Double>();
		for (String friend : friendList) {
			HashMap<Integer, ArrayList<Long>> tagTimestampMap = userTagTimes.get(friend);
			if (tagTimestampMap != null) {
				for (Integer tag : tagTimestampMap.keySet()) {
					ArrayList<Long> timestampList = tagTimestampMap.get(tag);
					// if the hashtag is used by the friend before the current
					// usage then we add the weight of the hashtag to the usage.
					for (Long timestampLong : timestampList) {
						if (tagRank.containsKey(tag)) {
								tagRank.put(tag, tagRank.get(tag) + getTagWeight(user, friend, "hybrid"));
								;
							} else {
								tagRank.put(tag, getTagWeight(user, friend, "hybrid"));
							}
							break; // once the hashtag used is detected we add the weight equal to the retweet value.
					}
				}
			}
		}
		return tagRank;
	}

	/**
	 * Get the tag weight based on the userId, friend and weightType.
	 * 
	 * @param userId
	 * @param friend
	 * @param weightType
	 * @return {@link Double} the weight which id independent of the tag and
	 *         just depend on the relation strength between 2 users.
	 */
	private double getTagWeight(String userId, String friend, String weightType) {
		double weight = 0;
		if ("retweet".equals(weightType)) {
			if (mapUserRetweetTimestamps.get(userId) != null) {
				System.out.println("user timestamp not null >> ");
				if (mapUserRetweetTimestamps.get(userId).get(friend) != null) {
					weight = mapUserRetweetTimestamps.get(userId).get(friend).size();
					System.out.println("weight not zero >> " + weight);
				} else {
					weight = 0;
				}
			}
		} else if ("mention".equals(weightType)) {
			if (mapUserMentionTimestamps.get(userId) != null) {
				if (mapUserMentionTimestamps.get(userId).get(friend) != null) {
					weight = mapUserMentionTimestamps.get(userId).get(friend).size();
					System.out.println("weight not zero >> " + weight);
				} else {
					weight = 0;
				}
			}
		} else if ("reply".equals(weightType)) {
			if (mapUserReplyTimestamps.get(userId) != null) {
				if (mapUserReplyTimestamps.get(userId).get(friend) != null) {
					weight = mapUserReplyTimestamps.get(userId).get(friend).size();
				} else {
					weight = 0;
				}
			}
		} else if ("hybrid".equals(weightType)) {
			if (mapUserReplyTimestamps.get(userId) != null) {
				if (mapUserReplyTimestamps.get(userId).get(friend) != null) {
					weight += mapUserReplyTimestamps.get(userId).size();
				}
			}
			if (mapUserMentionTimestamps.get(userId) != null) {
				if (mapUserMentionTimestamps.get(userId).get(friend) != null) {
					weight += mapUserMentionTimestamps.get(userId).size();
				}
			}
			if (mapUserRetweetTimestamps.get(userId) != null) {
				if (mapUserRetweetTimestamps.get(userId).get(friend) != null) {
					weight += mapUserRetweetTimestamps.get(userId).size();
				}
			}
		}
		return weight;
	}

	/**
	 * Populate mention list for users.
	 * 
	 * @param mentionFilename
	 *            the filename of the mention list of users
	 **/
	private void populateUserMentionMap(String mentionFilename) {
		mapUserMentionTimestamps = new HashMap<String, HashMap<String, ArrayList<Long>>>();
		ArrayList<RelationItem> relations = getRelationItemList(mentionFilename);
		String initUsername = "";
		String targetUsername = "";
		for (RelationItem item : relations) {
			initUsername = item.getInitUser();
			targetUsername = item.getTargetUser();
			if (!mapUserMentionTimestamps.containsKey(initUsername)) {
				mapUserMentionTimestamps.put(initUsername, new HashMap<String, ArrayList<Long>>());
			}
			if (!mapUserMentionTimestamps.get(initUsername).containsKey(targetUsername)) {
				mapUserMentionTimestamps.get(initUsername).put(targetUsername, new ArrayList<Long>());
			}
			mapUserMentionTimestamps.get(initUsername).get(targetUsername).add(item.getCreatedAt().getTime());
		}
	}

	/**
	 * Populate retweet list of users.
	 * 
	 * @param retweetFilename
	 *            the filename of the retweet list of users
	 */
	private void populateUserRetweetMap(String retweetFilename) {
		mapUserRetweetTimestamps = new HashMap<String, HashMap<String, ArrayList<Long>>>();
		ArrayList<RelationItem> relations = getRelationItemList(retweetFilename);
		String initUsername = "";
		String targetUsername = "";
		for (RelationItem item : relations) {
			initUsername = item.getInitUser();
			targetUsername = item.getTargetUser();
			if (!mapUserRetweetTimestamps.containsKey(initUsername)) {
				mapUserRetweetTimestamps.put(initUsername, new HashMap<String, ArrayList<Long>>());
			}
			if (!mapUserRetweetTimestamps.get(initUsername).containsKey(targetUsername)) {
				mapUserRetweetTimestamps.get(initUsername).put(targetUsername, new ArrayList<Long>());
			}
			mapUserRetweetTimestamps.get(initUsername).get(targetUsername).add(item.getCreatedAt().getTime());
		}
	}

	/**
	 * Populate reply list of users.
	 * 
	 * @param replyFilename
	 *            the filename of the reply list of users
	 */
	private void populateUserReplyMap(String replyFilename) {
		mapUserReplyTimestamps = new HashMap<String, HashMap<String, ArrayList<Long>>>();
		ArrayList<RelationItem> relations = getRelationItemList(replyFilename);
		String initUsername = "";
		String targetUsername = "";
		for (RelationItem item : relations) {
			initUsername = item.getInitUser();
			targetUsername = item.getTargetUser();
			if (!mapUserReplyTimestamps.containsKey(initUsername)) {
				mapUserReplyTimestamps.put(initUsername, new HashMap<String, ArrayList<Long>>());
			}
			if (!mapUserReplyTimestamps.get(initUsername).containsKey(targetUsername)) {
				mapUserReplyTimestamps.get(initUsername).put(targetUsername, new ArrayList<Long>());
			}
			mapUserReplyTimestamps.get(initUsername).get(targetUsername).add(item.getCreatedAt().getTime());
		}
	}

	/**
	 * Populate all the list.
	 * 
	 * @param mentionFilename
	 * @param retweetFilename
	 * @param replyFilename
	 *            the filename
	 * @return void
	 */
	private void populateUserRelationMap(String mentionFilename, String retweetFilename, String replyFilename) {
		populateUserMentionMap(mentionFilename);
		populateUserRetweetMap(retweetFilename);
		populateUserReplyMap(replyFilename);
	}

	/**
	 * Get relation item list from the filename.
	 * 
	 * @param filename
	 * @return return the list of {@link RelationItem}
	 **/
	private ArrayList<RelationItem> getRelationItemList(String filename) {
		ArrayList<RelationItem> relationItems = new ArrayList<RelationItem>();
		DateFormat formater = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");
		File file = new File(filename);
		try {
			String[] lines = FileUtils.readFile(file);
			for (String line : lines) {
				String[] tokens = line.split("\t");
				RelationItem relationItem = new RelationItem();
				try {
					relationItem.setId(Integer.parseInt(tokens[0]));
				} catch (NumberFormatException exc) {
					System.out.println("error parsing relation id > " + tokens[0]);
				}

				try {
					relationItem.setInitUser(tokens[1]);
				} catch (NumberFormatException exc) {
					System.out.println("error parsing init user > " + tokens[1]);
				}

				try {
					relationItem.setTargetUser(tokens[2]);
				} catch (NumberFormatException exc) {
					System.out.println("error parsing target user > " + tokens[2]);
				}

				try {
					relationItem.setTweetId(Long.parseLong(tokens[3]));
				} catch (NumberFormatException exc) {
					System.out.println("error parsing tweet id> " + tokens[3]);
				}

				try {
					relationItem.setCreatedAt(formater.parse(tokens[4]));
				} catch (ParseException e) {
					e.printStackTrace();
				}
				relationItems.add(relationItem);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return relationItems;
	}
}
