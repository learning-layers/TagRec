package processing.hashtag.baseline;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * @author spujari
 *
 */
public class UserTFIDFVectorCalculator{
        /**
         * Create a TFIDF vector for a user.
         * @param user
         * @return
         */
    public static Vector createUserTFIDFVector(HashMap<String, HashMap<Integer, ArrayList<Long>>> userTagTimestamps,
            HashMap<Integer, HashMap<String, Integer>> tagUserCount, String userName) {
        Vector vector = new Vector();
        for (Integer tag : userTagTimestamps.get(userName).keySet()) {
            int totalNumberOfUsers = userTagTimestamps.size();
            if (userTagTimestamps.get(userName).containsKey(tag)) {
                int tagUsageCountOfUser = userTagTimestamps.get(userName).get(tag).size();
                System.out.println(" number of times tag used by user >> " + tagUsageCountOfUser + " >> " + " user using the tags >> " + tagUserCount.get(tag).keySet().size() + " total Number of Users >> " + totalNumberOfUsers);
                vector.getVector().put(tag,
                        getHashTagTFIDFValue(tagUsageCountOfUser, tagUserCount.get(tag).keySet().size(), totalNumberOfUsers));
            }
        }
        return vector;
    }

        /**
         * Get TFIDF value for a Hashtag.
         * @param userName
         * @param hashtag
         * @return
         */
        private static double getHashTagTFIDFValue(int tagUsageCountOfUser, int numberOfUsersOfTag, int numberOfUsersDataset) {
            double tfIdf = 0d;
            double idfScore = getHashtagIDF(numberOfUsersOfTag, numberOfUsersDataset);
            System.out.println(" idfScore >> " + idfScore);
            tfIdf = (double)tagUsageCountOfUser * idfScore;
            System.out.println(" tfidf score >> " + tfIdf);
            return tfIdf;
        }

        
        /**
         * Get the IDF value of the hashtag.
         * @param numberOfUsersOfTag
         * @param numberOfUsers
         * @return
         */
        private static double getHashtagIDF(double numberOfUsersOfTag, double numberOfUsers) {
            double idfUser = 0d;
            if(numberOfUsersOfTag!=0){
                idfUser = numberOfUsers / numberOfUsersOfTag;
            }
            if(idfUser != 0){
                double logidfUser = Math.log(idfUser);
                return logidfUser;
            }else{
                return idfUser;
            }
        }
}
