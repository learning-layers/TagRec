package processing.hashtag.social;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import common.DoubleMapComparator;

public class SocialFrequencyCalculator {
	
	private HashMap<String, HashMap<Integer, ArrayList<Long>>> userTagTimes;
	private HashMap<String, ArrayList<String>> network;
	private List<String> users;
	
	public SocialFrequencyCalculator(HashMap<String, HashMap<Integer, ArrayList<Long>>> userTagTimes, 
			HashMap<String, ArrayList<String>> network, List<String> users) {
		this.userTagTimes = userTagTimes;
		this.network = network;
		this.users = users;
	}

	/**
	 * The tag weight is calculated based on how frequently a tag have been used by a user.
	 * 
	 * @param userID
	 *            {@link Integer}
	 * @param timesString
	 *            {@link Long}
	 * @param sort
	 *            {@link Boolean}
	 */
	public Map<Integer, Double> getRankedTagListSocialFrequency(int userID, Long timesString, boolean sort) {
		String user = this.users.get(userID);
		List<String> friendList = network.get(user);
		HashMap<Integer, Double> tagRank = new HashMap<Integer, Double>();
		if (friendList == null) {
			return tagRank;
		}
		for (String friend : friendList) {
			HashMap<Integer, ArrayList<Long>> tagTimestampMap = userTagTimes.get(friend);
			if (tagTimestampMap != null) {
				for (Integer tag : tagTimestampMap.keySet()) {
					ArrayList<Long> timestampList = tagTimestampMap.get(tag);
					// is there a timestamp less than the given timestamp
					for (Long timestampLong : timestampList) {

						if (timesString > timestampLong) {
							long duration = timesString - timestampLong;

							if (tagRank.containsKey(tag)) {
								tagRank.put(tag, tagRank.get(tag) + Math.pow(duration, 1.0));
							} else {
								tagRank.put(tag, 1.0);
							}
						}
					}

				}
			}
		}

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

		if (sort) {
			Map<Integer, Double> sortedResultMap = new TreeMap<Integer, Double>(new DoubleMapComparator(tagRank));
			sortedResultMap.putAll(tagRank);
			return sortedResultMap;
		} else {
			return tagRank;
		}
	}
}
