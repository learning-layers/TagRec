package processing.hashtag.social;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;

import cc.mallet.util.FileUtils;

public class SocialStrengthCalculator {

    private HashMap<String, HashMap<String, ArrayList<Long>>> mapUserMentionTimestamps;
    private HashMap<String, HashMap<String, ArrayList<Long>>> mapUserRetweetTimestamps;
    private HashMap<String, HashMap<String, ArrayList<Long>>> mapUserReplyTimestamps;

    public SocialStrengthCalculator(String mentionFilename, String retweetFilename, String replyFilename) {
        populateUserRelationMap(mentionFilename, retweetFilename, replyFilename);
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

    /** Get the tag weight based on the userId, friend and weightType.
    * 
    * @param userId
    * @param friend
    * @param weightType
    * @return {@link Double} the weight which id independent of the tag and
    *         just depend on the relation strength between 2 users.
    */
    public double getTagWeight(String userId, String friend, String weightType) {
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

}
