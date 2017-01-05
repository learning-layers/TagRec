package processing.hashtag.baseline;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import common.DoubleMapComparator;
import common.DoubleMapComparatorGeneric;

public class HashtagUtil {

    /**
     * Convert User-tag-timestamps Map to tag-user-count Map.
     * 
     * @param userTagTimestamps
     * @return
     */
    public static HashMap<Integer, HashMap<String, Integer>> getTagUserCount(
            HashMap<String, HashMap<Integer, ArrayList<Long>>> userTagTimestamps) {
        HashMap<Integer, HashMap<String, Integer>> tagUserCount = new HashMap<Integer, HashMap<String, Integer>>();
        for (String user : userTagTimestamps.keySet()) {
            if (userTagTimestamps.get(user) != null) {
                for (Integer tag : userTagTimestamps.get(user).keySet()) {
                    if (userTagTimestamps.get(user).get(tag) != null) {
                        if (!tagUserCount.containsKey(tag)) {
                            tagUserCount.put(tag, new HashMap<String, Integer>());
                        }
                        tagUserCount.get(tag).put(user, userTagTimestamps.get(user).get(tag).size());
                    }
                }
            }
        }
        return tagUserCount;
    }
    
    /**
     * Get All user TFIDF score.
     * @return {@link HashMap} of the user and
     */
    public static HashMap<String, Vector> createAllUserTFIDFVector(
            HashMap<String, HashMap<Integer, ArrayList<Long>>> userTagTimestamps,
            HashMap<Integer, HashMap<String, Integer>> tagUserCount) {

        System.out.print("All user TFIDF calculation starts >> ");
        HashMap<String, Vector> allUserTFIDFVectorMap = new HashMap<String, Vector>();
        for (String userName : userTagTimestamps.keySet()) {
            System.out.println("TFIDF calculation going for user >> " + userName);
            Vector hashtagTFIDFVector = UserTFIDFVectorCalculator.createUserTFIDFVector(userTagTimestamps, tagUserCount,
                    userName);
            System.out.println("TFDIDF vector for the user as >> " + hashtagTFIDFVector.getVector());
            allUserTFIDFVectorMap.put(userName, hashtagTFIDFVector);
        }
        System.out.println("All user TFIDF calculation ends >> ");
        return allUserTFIDFVectorMap;
    }
    
    public static Map<Integer, Double> getSortedMap( Map<Integer, Double> resultMap) {
        Map<Integer, Double> sortedResultMap = new TreeMap<Integer, Double>(new DoubleMapComparator(resultMap));
        sortedResultMap.putAll(resultMap);
        return sortedResultMap;
    }
    
    public static Map<String, Double> getSortedMapString( Map<String, Double> resultMap) {
        Map<String, Double> sortedResultMap = new TreeMap<String, Double>(new DoubleMapComparatorGeneric<String>(resultMap));
        sortedResultMap.putAll(resultMap);
        return sortedResultMap;
    }
}
