package processing.hashtag.social;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import common.DoubleMapComparator;

public class SocialHybridCalculator {
	
	private HashMap<String, HashMap<Integer, ArrayList<Long>>> userTagTimes;
	private HashMap<String, ArrayList<String>> network;
	private List<String> users;
	private List<Map<Integer, Double>> resultMapPersonalBLLAllUsers;
	private List<Map<Integer, Double>> resultMapPersonalFreqAllUsers;
	private SocialStrengthCalculator socialStrengthCalculator;
	
    /**
	 * Social Hybrid Calculator. 
	 * 
	 * @param userTagTimes
	 * @param network
	 * @param users
	 * @param resultMapPersonalBLLAllUsers
	 * @param resultMapPersonalFreqAllUsers
	 */
	public SocialHybridCalculator(HashMap<String, HashMap<Integer, ArrayList<Long>>> userTagTimes, 
			HashMap<String, ArrayList<String>> network, List<String> users, 
			List<Map<Integer, Double>> resultMapPersonalBLLAllUsers, List<Map<Integer, Double>> resultMapPersonalFreqAllUsers) {
		this.userTagTimes = userTagTimes;
		this.network = network;
		this.users = users;
		this.resultMapPersonalBLLAllUsers = resultMapPersonalBLLAllUsers;
		this.resultMapPersonalFreqAllUsers = resultMapPersonalFreqAllUsers;
	}
	
	/**
	 * Social Frequency Hybrid: A combination of social frequency and Personal frequency usage of the tags.
	 * 
	 * @param userID
	 * @param timeString
	 * @param beta
	 * @return {@link Map} a map of tags to the weight.
	 */
	public Map<Integer, Double> getRankedTagListSocialFrequencyHybrid(int userID, Long timeString, double beta) {
		Map<Integer, Double> resultMapSocialFreq = new SocialFrequencyCalculator(userTagTimes, network, users)
				.getRankedTagListSocialFrequency(userID, timeString, false);
		Map<Integer, Double> resultMapPersonalFreq = this.resultMapPersonalFreqAllUsers.get(userID);
		for (Map.Entry<Integer, Double> entry : resultMapPersonalFreq.entrySet()) {
			Double val = resultMapSocialFreq.get(entry.getKey());
			resultMapSocialFreq.put(entry.getKey(), val == null ? (beta) * entry.getValue().doubleValue()
					: (1 - beta) * val.doubleValue() + (beta) * entry.getValue().doubleValue());
		}
		Map<Integer, Double> sortedResultMap = new TreeMap<Integer, Double>(
				new DoubleMapComparator(resultMapSocialFreq));
		sortedResultMap.putAll(resultMapSocialFreq);
		return sortedResultMap;
	}

	/**
	 * Social BLL Hybrid: A combination of Social BLL and Personal BLL scores of tag weights.
	 * 
	 * @param userID
	 * @param timeString
	 * @param beta
	 * @param exponentSocial
	 * @param sort
	 * @return {@link Map} a map of tags to the weight.
	 */
	public Map<Integer, Double> getRankedTagListSocialBLLHybrid(int userID, Long timesString, double beta,
			double exponentSocial, boolean sort, boolean isLinkEnable) {
		String user = this.users.get(userID);
		List<String> friendList = network.get(user);
		HashMap<Integer, Double> tagRank = new LinkedHashMap<Integer, Double>();
		if (friendList != null) {
			for (String friend : friendList) {
				HashMap<Integer, ArrayList<Long>> tagTimestampMap = userTagTimes.get(friend);
				if (tagTimestampMap != null) {
					for (Integer tag : tagTimestampMap.keySet()) {
						ArrayList<Long> timestampList = tagTimestampMap.get(tag);
						for (Long timestampLong : timestampList) {
							if (timesString > timestampLong) {
								long duration = timesString - timestampLong;
								if (tagRank.containsKey(tag)) {
								    tagRank.put(tag, tagRank.get(tag) + getScoreBLL(user, friend, duration, exponentSocial, isLinkEnable));
								} else {
									tagRank.put(tag, getScoreBLL(user, friend, duration, exponentSocial, isLinkEnable));
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

		}
		
		Map<Integer, Double> resultMap = this.resultMapPersonalBLLAllUsers.get(userID);
		for (Map.Entry<Integer, Double> entry : tagRank.entrySet()) {
			Double val = resultMap.get(entry.getKey());
			resultMap.put(entry.getKey(), val == null ? /* (beta) **/ entry.getValue().doubleValue()
					: /* (1-beta) **/ val.doubleValue() + /* (beta) **/ entry.getValue().doubleValue());
		}

		if (sort) {
			Map<Integer, Double> sortedResultMap = new TreeMap<Integer, Double>(new DoubleMapComparator(resultMap));
			sortedResultMap.putAll(resultMap);
			return sortedResultMap;
		} else {
			return resultMap;
		}
	}
	
	private double getScoreBLL(String user, String friend, long duration, double exponentSocial, boolean isLinkEnable){
	    double linkWeightBLL = 0.0;
	    if(isLinkEnable){
	        double socialStrength = 1.0 + socialStrengthCalculator.getTagWeight(user, friend, "hybrid");
	        System.out.println(" social Strength >> " + socialStrength);
	        linkWeightBLL = Math.pow(duration, (-1) * (exponentSocial)) * socialStrength;
	    }else{
	        linkWeightBLL = Math.pow(duration, (-1) * (exponentSocial));
	    }
	    return linkWeightBLL;
	}
	
	public SocialStrengthCalculator getSocialStrengthCalculator() {
        return socialStrengthCalculator;
    }

    public void setSocialStrengthCalculator(SocialStrengthCalculator socialStrengthCalculator) {
        this.socialStrengthCalculator = socialStrengthCalculator;
    }	
}
