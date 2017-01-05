package processing.hashtag.baseline;

import java.io.File;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.solr.common.util.Hash;

import common.DoubleMapComparator;
import common.Similarity;
import net.sf.javaml.utils.MathUtils;

/**
 * @author spujari This tag recommendation is based on the method explained in
 * @see <a href=
 *      "http://link.springer.com/chapter/10.1007%2F978-3-319-16354-3_65#page-1">
 *      with paper title @see
 *      "Long time no tweets, time aware personalised Hashtag Suggestion".
 **/
public class ContentPersonalTemporalCalculator {
    
    
    private HashMap<String, HashMap<Integer, ArrayList<Long>>> userTagTimestamps;
    private HashMap<String, ArrayList<String>> network;
    private List<String> users;
    private HashMap<Integer, HashSet<String>> tagUserMap;
    private HashMap<Integer, ArrayList<Long>> tagTimestamps;
    private Map<Integer, Map<Integer, Double>> resultMapTweetSimilarity;
    private PersonalisedTFIDFCalculator personalisedTFIDFCalculator;
    private Map<Integer, Double> tagEntropyScore;
    private int numberOfIntervals;
    private double eta_l;
    private double eta_h;

    /**
     * {@link Constructor}
     * 
     * @param userTagTimes
     *            user-Tag-Timestamp map
     * @param network
     *            user mapped to userfriend list
     * @param users
     *            {@link List} of users which are mapped to the index and
     *            userId.
     */
    public ContentPersonalTemporalCalculator(HashMap<String, HashMap<Integer, ArrayList<Long>>> userTagTimes,
            HashMap<String, ArrayList<String>> network, List<String> users, List<String> tags, String solrUrl,
            String solrCore, String sampleDir, Map<Integer, Map<Integer, Double>> resultMapTweetSimilarity) {
        this.userTagTimestamps = userTagTimes;
        this.network = network;
        this.users = users;
        this.tagUserMap = createUserHashtagVector(userTagTimes);
        this.resultMapTweetSimilarity = resultMapTweetSimilarity;
        this.tagTimestamps = getTagTimestamps(userTagTimes);
        this.numberOfIntervals = 10000;
        
        String serialFilePath = "./data/results/" + sampleDir + "/" + solrCore + "_hashtag_entropy_" + numberOfIntervals + ".ser";
        if(new File(serialFilePath).exists()){
            System.out.println("serializer file exists .. the entropy values will be loaded from the serialized file");
            this.tagEntropyScore = HashtagEntropyCalculator.deSerializeHashtagEntropy(serialFilePath);
        }else{
            System.out.println("serializer file not exists ... entropy will be calculated and stored into the serialized file");
            this.tagEntropyScore = HashtagEntropyCalculator.computeAllHashtagEntropyMap(userTagTimestamps, this.numberOfIntervals);
            HashtagEntropyCalculator.serializeHashtagEntropy(this.tagEntropyScore, serialFilePath);
        }
        // init the constructor for personalised TFIDF vector
        /*personalisedTFIDFCalculator = new PersonalisedTFIDFCalculator(userTagTimestamps,
                HashtagUtil.getTagUserCount(userTagTimestamps));*/
    }
    
    public HashMap<Integer, ArrayList<Long>> getTagTimestamps(HashMap<String, HashMap<Integer, ArrayList<Long>>> userTagTimestamps){
        HashMap<Integer, ArrayList<Long>> tagTimestamps = new HashMap<Integer, ArrayList<Long>>();
        for(String user : userTagTimestamps.keySet()){
            for(Integer hashtag : userTagTimestamps.get(user).keySet()){
                ArrayList<Long> timestamps = userTagTimestamps.get(user).get(hashtag);
                if(!tagTimestamps.containsKey(hashtag)){
                    tagTimestamps.put(hashtag, new ArrayList<Long>());
                }
                tagTimestamps.get(hashtag).addAll(timestamps);
            }
        }
        for (Integer hashtag : tagTimestamps.keySet()){
            Collections.sort(tagTimestamps.get(hashtag), Collections.reverseOrder());
        }
        return tagTimestamps;
    }

    /**
     * Get Similarity score based on hybrid of Text Based Similarity,
     * Personalised Similarity and Temporal factor.
     * 
     * @param user
     *            userId of user for whom tweet is getting recommended.
     * @param time
     *            time of tweet for temporal factor
     * @param targetTweetId
     *            target tweetId
     * @param sort
     *            whether to sort or not
     * @return {@link Map} of tag and similarity score.
     */
    public Map<Integer, Double> getSimilarityScore(int userId, long time, boolean sort) {
        String user = users.get(userId);
        HashMap<Integer, Double> resultMap = new HashMap<Integer, Double>();
        Map<Integer, Double> userTweetSimilarityHashtagScore = this.resultMapTweetSimilarity.get(userId);
        for (int hashTag : tagUserMap.keySet()) {
            double hashtagScore = getHybridTextPersonalisedScore(user, hashTag, userTweetSimilarityHashtagScore);
            resultMap.put(hashTag, hashtagScore);
        }
        return getSortedMap(getNormalisedMap(resultMap));
    }

    /**
     * Get Similarity score based on hybrid of Text Based Similarity, Personalised Similarity 
     * in which each user is represented by a TFIDF vector. 
     * In which case each Hashtag represent the vector element and the vector 
     * similarity is calculated based on taking the cosine similarity into account.
     * @param userId
     * @param time
     * @param sort
     * @return
     */
    public Map<Integer, Double> getSimilarityScoreVersion2(int userId, long time, boolean sort) {
        System.out.println("running inside version2");
        HashMap<Integer, Double> resultMap = new HashMap<Integer, Double>();
        String user = users.get(userId);
        // get the Tweet Similarity result map for a user which contains the score for each hashtag.
        Map<Integer, Double> userTweetSimilarityHashtagScore = this.resultMapTweetSimilarity.get(userId);
        
        // get the User similarity result map for a user. 
        Map<Integer, Double> userUserSimilarityHashtagScore = personalisedTFIDFCalculator.getHashTagScoreResultMap(user,
                network.get(user), personalisedTFIDFCalculator.getAllUserTFIDFVector());
        
        // for each hashtag tha the user hash used
        for (int hashTag : tagUserMap.keySet()) {
            double userScoreTweetSimilarity = 0;
            double userScorePersonalisedSimilarity = 0;

            
            if (userTweetSimilarityHashtagScore.containsKey(hashTag)) {
                userScoreTweetSimilarity = userTweetSimilarityHashtagScore.get(hashTag);
            }
            
            if (userUserSimilarityHashtagScore.containsKey(hashTag)) {
                userScorePersonalisedSimilarity = userUserSimilarityHashtagScore.get(hashTag);
            }
            
            // make a hybrid linear combination of 2 approaches
            double hashtagScore = combineLinear(userScoreTweetSimilarity, userScorePersonalisedSimilarity, 0.6);
            
            if (hashtagScore > 0) {
                resultMap.put(hashTag, hashtagScore);
            }
        }
        return getSortedMap(getNormalisedMap(resultMap));
    }

    /**
     * Get Similarity score based on hybrid of Text Based Similarity, Personalised Similarity 
     * in which each user is represented by a TFIDF vector. 
     * In which case each Hashtag represent the vector element and the vector 
     * similarity is calculated based on taking the cosine similarity into account.
     * @param userId
     * @param time
     * @param sort
     * @return
     */
    public Map<Integer, Double> getSimilarityScoreVersion3(int userId, long time, boolean sort) {
        System.out.println("running inside version3");
        System.out.println(" ContentePersonalTemporalCalculator >> eta_h " + eta_h + " >> eta_l >> " + eta_l);
        HashMap<Integer, Double> resultMap = new HashMap<Integer, Double>();
        Map<Integer, Double> userTweetSimCollabFiltHashtagScore = this.resultMapTweetSimilarity.get(userId);
        //System.out.println(" unsorted map >> " + userTweetSimCollabFiltHashtagScore);
        userTweetSimCollabFiltHashtagScore = getTopItems(userTweetSimCollabFiltHashtagScore, 1000);
        //System.out.println(" chosen map >> " + userTweetSimCollabFiltHashtagScore);
        
        for (int hashTag : userTweetSimCollabFiltHashtagScore.keySet()) {
            double userScoreTweetSimilarity = 0.0;
            double lastUserDiffDays = getLastUsedDiffInDays(time, hashTag);
            if (userTweetSimCollabFiltHashtagScore.containsKey(hashTag)) {
                userScoreTweetSimilarity = userTweetSimCollabFiltHashtagScore.get(hashTag);
            }
            double temporalFactor = 1.0;
            if(tagEntropyScore.get(hashTag)!= null){
                double entropyScore = tagEntropyScore.get(hashTag);
                if(entropyScore < 0.5){
                    double power = -1 * eta_l * lastUserDiffDays;
                    temporalFactor = Math.exp(power);
                     //System.out.println(" power factor lower entropy >> " + power);
                     //System.out.println(" temporal Factor lower entropy >> " + temporalFactor);
                }else{
                    double power = -1 * eta_h * lastUserDiffDays;
                    temporalFactor = Math.exp(power);
                     //System.out.println(" power factor high entropy >> " + power);
                     //System.out.println(" temporal Factor high entropy >> " + temporalFactor);
                }
                 //System.out.println("temporal factor with entropy value >> " + temporalFactor);
                temporalFactor += 0.5;

            }
            double hashtagScore = userScoreTweetSimilarity * temporalFactor; 
            //if (hashtagScore > 0.0) {
            resultMap.put(hashTag, hashtagScore);
            //}
        }
        return getSortedMap(resultMap);
    }

    private double getLastUsedDiffInDays(long time, int hashTag) {
        double lastUsedDiffSeconds = 0;
        ArrayList<Long> tagTimestampsList = this.tagTimestamps.get(hashTag);
        for(Long timestamp : tagTimestampsList){
            if (time > timestamp){
                lastUsedDiffSeconds = time - timestamp;
                break;
            }
        }
        double lastUsedDiffDays = lastUsedDiffSeconds / 86400;
        return lastUsedDiffDays;
    }
    
    /**
     * Create a map of hashtag to User set from a userTagTimestamps map.
     * 
     * @param userTagTimes
     * @return
     */
    private HashMap<Integer, HashSet<String>> createUserHashtagVector(
            HashMap<String, HashMap<Integer, ArrayList<Long>>> userTagTimes) {
        // a map of all the hashtags and List of users who have used those hash maps
        HashMap<Integer, HashSet<String>> tagUserMap = new HashMap<Integer, HashSet<String>>();
        for (String user : userTagTimes.keySet()) {
            HashMap<Integer, ArrayList<Long>> tagTimes = userTagTimes.get(user);
            for (Integer tag : tagTimes.keySet()) {
                if (!tagUserMap.containsKey(tag)) {
                    tagUserMap.put(new Integer(tag), new HashSet<String>());
                } else {
                    tagUserMap.get(tag).add(new String(user));
                }
            }
        }
        return tagUserMap;
    }

    /**
     * Hybrid Text Personalised Score.
     * 
     * @param user
     * @param hashTag
     * @param resultMap
     * @param userTweetSimilarityHashtagScore
     * @return
     */
    private double getHybridTextPersonalisedScore(String user, int hashTag,
            Map<Integer, Double> userTweetSimilarityHashtagScore) {

        double hashtagScore = 0;

        if (userTweetSimilarityHashtagScore.containsKey(hashTag)) {
            hashtagScore = combineLinear(userTweetSimilarityHashtagScore.get(hashTag), PersonalisedSimilarityCalculator
                    .getPersonalisedSimilarity(user, network.get(user), tagUserMap.get(hashTag), userTagTimestamps),
                    0.5);
        } else {
            hashtagScore = combineLinear(0, PersonalisedSimilarityCalculator.getPersonalisedSimilarity(user,
                    network.get(user), tagUserMap.get(hashTag), userTagTimestamps), 0.5);
        }

        if (hashtagScore > 0.0) {
            double temporalFactor = 1d;
            hashtagScore = hashtagScore * temporalFactor;
        }
        return hashtagScore;
    }

    /**
     * Combine 2 values lineary with a constant factor.
     * 
     * @param score1
     * @param score2
     * @param lambda
     *            constant factor for linear combination.
     * @return
     */
    private double combineLinear(double score1, double score2, double lambda) {
        double hashtagScore = lambda * score1 + (1 - lambda) * score2;
        return hashtagScore;
    }

    /**
     * Sorted Map values ascending to descending.
     * 
     * @param sort
     * @param resultMap
     * @return
     */
    private Map<Integer, Double> getSortedMap( Map<Integer, Double> resultMap) {
        Map<Integer, Double> sortedResultMap = new TreeMap<Integer, Double>(new DoubleMapComparator(resultMap));
            sortedResultMap.putAll(resultMap);
            System.out.println("resultMap >> " + sortedResultMap);
            return sortedResultMap;
    }

    /**
     * Normalize the values in the map.
     * 
     * @param resultMap
     * @return
     */
    private Map<Integer, Double> getNormalisedMap(Map<Integer, Double> resultMap) {
        double denom = 0.0;
        if (resultMap != null) {
            for (Map.Entry<Integer, Double> entry : resultMap.entrySet()) {
                if (entry != null) {
                    double actVal = Math.log(entry.getValue());
                    denom += Math.exp(actVal);
                    entry.setValue(actVal);
                }
            }
            for (Map.Entry<Integer, Double> entry : resultMap.entrySet()) {
                if (entry != null) {
                    double actVal = Math.exp(entry.getValue());
                    entry.setValue(actVal / denom);
                }
            }
        }
        return resultMap;
    }

    private Map<Integer, Double> getTopItems(Map<Integer, Double> completeMap, int numberOfTopItems){
        Map<Integer, Double> chosenMap = new HashMap<Integer, Double>();
        int itemCount = 0;
        for (Integer hashtag : completeMap.keySet()){
            // System.out.println("hastag >> " + itemCount + " >> "+ hashtag) ;
            itemCount += 1;
            chosenMap.put(hashtag, completeMap.get(hashtag));
            if(itemCount > numberOfTopItems){
                break;
            }
        }
        return chosenMap;
    }

    public double getEta_l() {
        return eta_l;
    }

    public void setEta_l(double eta_l) {
        this.eta_l = eta_l;
    }

    public double getEta_h() {
        return eta_h;
    }

    public void setEta_h(double eta_h) {
        this.eta_h = eta_h;
    }

}
