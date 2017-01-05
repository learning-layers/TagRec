package processing.hashtag.baseline;

import java.util.ArrayList;
import java.util.HashMap;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class PersonalisedSimilarityCalculatorTest {

    private HashMap<String, HashMap<Integer, ArrayList<Long>>> userTagTimestamps = new HashMap<String, HashMap<Integer, ArrayList<Long>>>();
    private HashMap<Integer, HashMap<String, Integer>> tagUserCount = new HashMap<Integer, HashMap<String, Integer>>();

    @Before
    public void init() {
        userTagTimestamps = initUserTagTimestamps();
        tagUserCount = HashtagUtil.getTagUserCount(userTagTimestamps);
    }

    @Test
    public void getPersonalisedSimilarityTest() {
        HashMap<String, Vector> userTFIDFVector = HashtagUtil.createAllUserTFIDFVector(userTagTimestamps, tagUserCount);
        System.out.print(" vector 1 >> " + userTFIDFVector.get("user1").getVector());
        System.out.print(" vector 2 >> " + userTFIDFVector.get("user2").getVector());
        System.out.print(" vector 3 >> " + userTFIDFVector.get("user3").getVector());
        double cosineSimilarity = CosineSimilarityCalculator.getCosineSimilarity(userTFIDFVector.get("user1"),
                userTFIDFVector.get("user2"));
        System.out.println(" cosine Similarity Score >>  " + cosineSimilarity);
    }

    @After
    public void destroy() {

    }

    HashMap<String, HashMap<Integer, ArrayList<Long>>> initUserTagTimestamps() {

        HashMap<String, HashMap<Integer, ArrayList<Long>>> userTagTimestamps = new HashMap<String, HashMap<Integer, ArrayList<Long>>>();

        // adding hashmap for user 1
        userTagTimestamps.put("user1", new HashMap<Integer, ArrayList<Long>>());
        userTagTimestamps.get("user1").put(1, new ArrayList<Long>());
        userTagTimestamps.get("user1").get(1).add(1l);
        userTagTimestamps.get("user1").get(1).add(2l);
        userTagTimestamps.get("user1").get(1).add(3l);

        // adding user map for user 2
        userTagTimestamps.get("user1").put(2, new ArrayList<Long>());
        userTagTimestamps.get("user1").get(2).add(1l);
        userTagTimestamps.get("user1").get(2).add(2l);

        userTagTimestamps.put("user2", new HashMap<Integer, ArrayList<Long>>());
        userTagTimestamps.get("user2").put(1, new ArrayList<Long>());
        userTagTimestamps.get("user2").get(1).add(2l);
        userTagTimestamps.get("user2").get(1).add(3l);

        userTagTimestamps.get("user2").put(2, new ArrayList<Long>());
        userTagTimestamps.get("user2").get(2).add(1l);
        userTagTimestamps.get("user2").get(2).add(2l);

        userTagTimestamps.get("user2").put(3, new ArrayList<Long>());
        userTagTimestamps.get("user2").get(3).add(1l);

        // adding hashmap for user 3
        userTagTimestamps.put("user3", new HashMap<Integer, ArrayList<Long>>());
        userTagTimestamps.get("user3").put(1, new ArrayList<Long>());
        userTagTimestamps.get("user3").get(1).add(1l);

        userTagTimestamps.get("user3").put(2, new ArrayList<Long>());
        userTagTimestamps.get("user3").get(2).add(1l);
        userTagTimestamps.get("user3").get(2).add(2l);

        userTagTimestamps.put("user4", new HashMap<Integer, ArrayList<Long>>());
        userTagTimestamps.get("user4").put(4, new ArrayList<Long>());
        userTagTimestamps.get("user4").get(4).add(1l);

        userTagTimestamps.get("user4").put(5, new ArrayList<Long>());
        userTagTimestamps.get("user4").get(5).add(1l);
        userTagTimestamps.get("user4").get(5).add(2l);
        
        return userTagTimestamps;
    }
}
