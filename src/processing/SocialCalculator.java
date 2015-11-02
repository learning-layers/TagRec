package processing;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.google.common.primitives.Ints;

import common.Bookmark;
import common.DoubleMapComparator;
import common.DoubleMapComparatorKeyString;
import file.BookmarkReader;
import file.PredictionFileWriter;

/**
 * @author spujari
 *
 */
public class SocialCalculator {

    private BookmarkReader reader;
    private HashMap<String, HashMap<Integer, ArrayList<Long>>> userTagTimes ;
    private HashMap<String, List<String>> network;
    private List<String> users;
    private int sampleSize;
    private int trainSize;
    private List<String> idNameMap;
    
    /**
     * @param tweetFilename
     * @param networkFilename
     * @param userInfoPath
     * @param trainSize
     * @param sampleSize
     */
    public SocialCalculator(String tweetFilename, String networkFilename, int trainSize, int sampleSize) {
        this.sampleSize = sampleSize;
        this.trainSize = trainSize;
        reader = new BookmarkReader(trainSize, false);
        reader.readFile(tweetFilename);
        List<Bookmark> bookmarkList = reader.getBookmarks();
        this.users = reader.getUsers();
        this.sampleSize = bookmarkList.size();
        this.trainSize =  (int) ((int)bookmarkList.size() * 0.9);
        this.userTagTimes = getUserTagTime(bookmarkList.subList(0, this.trainSize));
        this.idNameMap = reader.getUsers();
        this.network = getNetwork(networkFilename, getNameIdMap(idNameMap));
    }
    
    public HashMap<String, HashMap<Integer, ArrayList<Long>>> getUserTagTimes(){
    	return userTagTimes;
    }
    
    /**
     * Takes a list of username string with user id as index of the list.
     * @param idNameMap
     * @return {@link HashMap} the map from name of user to his id in bookmark system.
     */
    private HashMap<String, Integer> getNameIdMap(List<String> idNameMap){
        HashMap< String, Integer> nameIdMap = new HashMap<String, Integer>();
        for (int i=0; i<idNameMap.size(); i++){
            nameIdMap.put(idNameMap.get(i), i);
        }
        return nameIdMap;
    }
    
    /**
     * 
     * The user tags and their timeline information.
     * @param bookmarkList
     * @return
     */
    private HashMap<String, HashMap<Integer, ArrayList<Long>>> getUserTagTime(List<Bookmark> bookmarkList){
        HashMap<String, HashMap<Integer, ArrayList<Long>>> userTagTimes = new HashMap<String, HashMap<Integer,ArrayList<Long>>>();
        for(Bookmark bookmark : bookmarkList){
            List<Integer> taglist = bookmark.getTags();
            Integer userId = bookmark.getUserID();
            // get the userName for the id.
            String userName = users.get(userId);
            //userName = this.twitterScreenNameIdMap.get(userName);
            String timestamp = bookmark.getTimestamp();
            Long timestampLong = bookmark.getTimestampAsLong();
            if (!userTagTimes.containsKey(userName)){
                userTagTimes.put(userName, new HashMap<Integer, ArrayList<Long>>());
            }
            for (Integer tag : taglist){
            	if (!userTagTimes.get(userName).containsKey(tag)){
                    userTagTimes.get(userName).put(tag, new ArrayList<Long>());
                }
                userTagTimes.get(userName).get(tag).add(timestampLong);
            }
        }
        return userTagTimes;
    }
    
    /**
     * 
     * @param filepath
     * @param nameIdMap
     * @return HashMap name and friends as 
     */
    private HashMap<String, List<String>> getNetwork(String filepath, HashMap<String, Integer> nameIdMap){
        
        HashMap<String, List<String>> network = new HashMap<String, List<String>>();
        try {
            File file = new File(filepath);
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line = "";
            while((line=br.readLine())!= null){
                String[] tokens = line.split("\t");
                String user1 = tokens[0];
                String user2 = tokens[1];
                if (!network.containsKey(user1)){
                    network.put(user1, new ArrayList<String>());
                    network.get(user1).add(user2);
                }else{
                    network.get(user1).add(user2);
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("Cannot find the network file: " + filepath + "\n");
        } catch (IOException e) {
            System.out.println("Error in reading from the network file: " + filepath + "\n");
        }
        return network;
    }
    
    /**
     * @param userID
     * @param timesString
     * @return
     */
    private Map<Integer, Double> getRankedTagList(int userID, Long timesString){
        Map<Integer, Double> rankedList = new HashMap<Integer, Double>();
        String user = this.users.get(userID);
        List<String> friendList = network.get(user);
        HashMap <Integer, Integer> tagRank = new HashMap<Integer, Integer>();
        if (friendList == null){
        	return rankedList;
        }
        for(String friend : friendList){
        	HashMap<Integer, ArrayList<Long>> tagTimestampMap = userTagTimes.get(friend);
        	if (tagTimestampMap != null){
        		for (Integer tag : tagTimestampMap.keySet()){
        			ArrayList<Long> timestampList = tagTimestampMap.get(tag);
                
        			// is there a timestamp less than the given timestamp
        			for (Long timestampLong : timestampList){
        				if(timesString > timestampLong ){
        					
        					if (tagRank.containsKey(tag)){
        						tagRank.put(tag, tagRank.get(tag)+1);
        					}else{
        						tagRank.put(tag, 1);
        					}
        				}
        			}
                
        		}
        	}
        }
        
        Map<Integer, Double> sortedResultMap = new TreeMap<Integer, Double>(new DoubleMapComparator(rankedList));
		sortedResultMap.putAll(rankedList);
		return sortedResultMap;
    }

    
    /**
     * @param sampleSize
     * @return
     */
    private List<Map<Integer, Double>> calculateSocialTagScore(int sampleSize) {
    
        List<Map<Integer, Double>> results = new ArrayList<Map<Integer, Double>>();
        for (int i = trainSize; i < sampleSize; i++) { // the test-set
            Bookmark data = reader.getBookmarks().get(i);
            Map<Integer, Double> map = getRankedTagList(data.getUserID(), data.getTimestampAsLong());
            results.add(map);
        }  
        return results;
    }
    
    
    /**
     * @return
     */
    public BookmarkReader predictSample() {
    
        List<Map<Integer, Double>> actValues = calculateSocialTagScore(this.sampleSize);
        // convert tag to int
        List<int[]> predictionValues = new ArrayList<int[]>();
        for (int i = 0; i < actValues.size(); i++) {
            Map<Integer, Double> modelVal = actValues.get(i);
            predictionValues.add(Ints.toArray(modelVal.keySet()));
        }
        reader.setTestLines(reader.getBookmarks().subList(trainSize, reader.getBookmarks().size()));
        PredictionFileWriter writer = new PredictionFileWriter(reader, predictionValues);
        //writer.writeFile(this.twee + "_social")
        return reader;
    }      
}
