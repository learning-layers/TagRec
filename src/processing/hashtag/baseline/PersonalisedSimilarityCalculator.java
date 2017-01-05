package processing.hashtag.baseline;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class PersonalisedSimilarityCalculator {

    /**
     * Personalised Similarity: Sum of similarity score of a users who have used
     * the hashtag to the target user, more the similarity score between the
     * users who have used the same hashtag, higher is the chance of use of
     * hashtag.
     * 
     * @param hashtag
     *            the hashtag for which we want to calculate the similarity
     *            score
     * @param user
     *            target user who is creating the target tweet.
     * @return
     */
    public static double getPersonalisedSimilarity(String user, ArrayList<String> friendsArrayList, HashSet<String> friends, HashMap<String, 
                                                              HashMap<Integer, ArrayList<Long>>> userTagTimes) {
        //ArrayList<String> friendsArrayList = network.get(user);
        HashSet<String> intersectedUsers = new HashSet<String>(friendsArrayList);
        //HashSet<String> friends = new HashSet<String>(tagUserMap.get(targetHashtag));
        intersectedUsers.retainAll(friends);
        double simScore = findSumOfSimilarityScore(user, new ArrayList<String>(intersectedUsers), userTagTimes);
        return simScore;
    }

    /**
     * Sum of similarity score in case of personalised recommendation.
     * 
     * @param targetUser
     * @param candidateUsers
     * @return
     */
    private static double findSumOfSimilarityScore(String targetUser, ArrayList<String> candidateUsers,
            HashMap<String, HashMap<Integer, ArrayList<Long>>> userTagTimes) {
        double similarityScoreValue = 0d;
        Set<Integer> targetUserHashtag;
        Set<Integer> candidateUserHashtag;
        Set<Integer> intersection;
        for (String candidateUser : candidateUsers) {
            targetUserHashtag = userTagTimes.get(targetUser).keySet();
            if (userTagTimes.containsKey(candidateUser)) {
                candidateUserHashtag = new HashSet<Integer>(userTagTimes.get(candidateUser).keySet());
                intersection = new HashSet<Integer>(targetUserHashtag);
                intersection.retainAll(candidateUserHashtag);

                if (targetUserHashtag.size() > 0 && candidateUserHashtag.size() > 0) {
                    similarityScoreValue += computeSimilarityScore(intersection.size(), targetUserHashtag.size(),
                            candidateUserHashtag.size());
                }
            }
        }
        return similarityScoreValue;
    }

    /**
     * Compute similarity score.
     * 
     * @param intersectionSize
     * @param set1Size
     * @param set2Size
     * @return
     */
    private static double computeSimilarityScore(int intersectionSize, int set1Size, int set2Size) {
        return (double) intersectionSize / (double) (set1Size * set2Size);
    }

}
