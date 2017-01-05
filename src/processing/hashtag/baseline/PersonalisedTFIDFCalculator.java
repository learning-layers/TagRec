package processing.hashtag.baseline;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.solr.common.util.Hash;

import processing.hashtag.TagRecommendationUtil;

/**
 * This class implements a version of collaborative filtering algorithm that
 * assign score to the hashtags and rank hashtags based on this score. The main
 * steps of the algorithm can be illustrated as follows.
 * 
 * getHashTagScoreResultMap() - gives the score of all the hashtags in our
 * dataset.
 * 
 * >> computeHashtagVectorMap() compute the TFIDF vector of hashtags for each
 * user >> A TFIDF vector for a user means a vector in which hashtags are
 * elements and a tfidf score associated with each hashtag. -- Get a map of
 * TFIDF vector for each user. --
 * 
 * @author spujari
 *
 */
public class PersonalisedTFIDFCalculator {

    private HashMap<String, HashMap<Integer, ArrayList<Long>>> userTagTimestamps;
    private HashMap<Integer, HashMap<String, Integer>> tagUserCount;
    private HashMap<String, Vector> allUserTFIDFVector;
    
    /**
     * Constructor.
     * 
     * @param userTagTimestamps
     * @param tagUserCount
     */
    public PersonalisedTFIDFCalculator(HashMap<String, HashMap<Integer, ArrayList<Long>>> userTagTimestamps,
            HashMap<Integer, HashMap<String, Integer>> tagUserCount) {
        this.userTagTimestamps = userTagTimestamps;
        this.tagUserCount = tagUserCount;
        this.allUserTFIDFVector = HashtagUtil.createAllUserTFIDFVector(userTagTimestamps, tagUserCount);
    }

    public HashMap<String, Vector> getAllUserTFIDFVector() {
        return allUserTFIDFVector;
    }

    /**
     * Get the result map of hashtag scores.
     * 
     * @param user
     * @param friends
     * @return
     */
    public Map<Integer, Double> getHashTagScoreResultMap(String user, ArrayList<String> friends,
            HashMap<String, Vector> allUserTFIDFVector) {
        System.out.println("Computation going for user >> " + user + " number of friends >>  " + friends);
        Map<Integer, Double> resultMap = new HashMap<Integer, Double>();
        Vector targetUserVector = this.allUserTFIDFVector.get(user);
        Map<String, Double> userSimScoreMap = new HashMap<String, Double>();
        for (String friend : friends) {
            if (this.allUserTFIDFVector.containsKey(friend)) {
                Vector friendUserVector = this.allUserTFIDFVector.get(friend);
                double simScore = CosineSimilarityCalculator.getCosineSimilarity(targetUserVector, friendUserVector);
                if(simScore > 0){
                    userSimScoreMap.put(friend, simScore);
                }
            }
        }
        //userSimScoreMap = TagRecommendationUtil.getSortedMap(userSimScoreMap);
        //userSimScoreMap = choseKFromSortedMap(userSimScoreMap, 100);
        resultMap = computeHashtagScoreMapFromUserScoreMap(userSimScoreMap);
        //resultMap = TagRecommendationUtil.getSortedMap(resultMap);
        //System.out.println("print sorted result map >> " + resultMap);
        return resultMap;
    }

    /**
     * Compute Hashtag Score.
     * 
     * @param userSimScoreMap
     * @return
     */
    private HashMap<Integer, Double> computeHashtagScoreMapFromUserScoreMap(Map<String, Double> userSimScoreMap) {
        //System.out.println("user sim score map >> " + userSimScoreMap);
        userSimScoreMap = new HashMap<String, Double>(userSimScoreMap);
        HashMap<Integer, Double> hashTagScore = new HashMap<Integer, Double>();
        for (String user : userSimScoreMap.keySet()) {
            if (userTagTimestamps.containsKey(user)) {
                for (Integer tag : userTagTimestamps.get(user).keySet()) {
                    if (hashTagScore.containsKey(tag)) {
                        //System.out.println(" tag >> " + tag);
                        //System.out.println(" user >> " + user);
                        //System.out.println(" Hashtag score >> " + hashTagScore.get(tag));
                        //System.out.println(" userSimScoreMap score >> " + userSimScoreMap.get(user));
                        if (hashTagScore.get(tag) < userSimScoreMap.get(user) && userSimScoreMap.get(user) > 0) {
                            hashTagScore.put(tag, userSimScoreMap.get(user));
                        }
                    } else {
                        if(userSimScoreMap.get(user) > 0){
                            hashTagScore.put(tag, userSimScoreMap.get(user));
                        }
                    }
                }
            }
        }
        return hashTagScore;
    }

    /**
     * Chose highest K integers from the map.
     * 
     * @param sortedMap
     * @param k
     * @return
     */
    private Map<String, Double> choseKFromSortedMap(Map<String, Double> sortedMap, int k) {
        HashMap<String, Double> chosenKSortedMap = new HashMap<String, Double>();
        int count = 0;
        HashMap<String, Double> map = new HashMap<String, Double>(sortedMap);
        for (String key : map.keySet()) {
            double sortedValue = map.get(key);
            chosenKSortedMap.put(key, sortedValue);
            if (count >= k) {
                break;
            } else {
                count += 1;
            }
        }
        return chosenKSortedMap;
    }    
}
