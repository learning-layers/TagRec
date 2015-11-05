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
import java.util.Map.Entry;
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

    private String filename;
    private BookmarkReader reader;
    
    private HashMap<String, HashMap<Integer, ArrayList<Long>>> userTagTimes ;
    private HashMap<String, List<String>> network;
    private List<String> users;
    private int trainSize;
    private List<String> idNameMap;
    private List<Map<Integer, Double>> bllMapTagValues;
    
    public SocialCalculator(String userTweetFilename, String networkFilename, int trainSize, int testSize) {
        this.filename = userTweetFilename;
        this.trainSize = trainSize;
        reader = new BookmarkReader(trainSize, false);
        reader.readFile(userTweetFilename);
        this.users = reader.getUsers();
        
        this.idNameMap = reader.getUsers();
        // initialise the predictor with the basic training dataset
        List<Bookmark> socialTestList = reader.getBookmarks().subList(0, this.trainSize);
        System.out.println("socialTestList.size() >>" + socialTestList.size());
        this.addBookmarks(socialTestList);
        this.network = getNetwork(networkFilename, getNameIdMap(idNameMap));
        List<Bookmark> trainList =  this.reader.getBookmarks().subList(0, this.trainSize);
        List<Bookmark> testList = this.reader.getBookmarks().subList(this.trainSize, this.reader.getBookmarks().size());
        System.out.println("total size >>" + this.reader.getBookmarks().size());
        System.out.println("users list size>> " + this.users.size());
        System.out.println("trainList size>> " + trainList.size());
        System.out.println("testList size>> " + testList.size());
        bllMapTagValues = BLLCalculator.getArtifactMaps(reader, trainList, testList, false, new ArrayList<Long>(), new ArrayList<Double>(), 0.5, true);
        System.out.println("bll values list size>> " + bllMapTagValues.size());
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
     * The user tags and their timeline information.
     * @param bookmarkList
     * @return
     */
    private void addBookmarks(List<Bookmark> bookmarkList){
    	
    	if (this.userTagTimes == null){
        	this.userTagTimes = new HashMap<String, HashMap<Integer,ArrayList<Long>>>();
        }
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
    }
    
    /**
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
    private Map<Integer, Double> getRankedTagList(int userID, Long timesString, double alpha){
        Map<Integer, Double> rankedList = new HashMap<Integer, Double>();
        String user = this.users.get(userID);
        List<String> friendList = network.get(user);
        HashMap <Integer, Double> tagRank = new HashMap<Integer, Double>();
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
        				    long duration = timesString - timestampLong;
        					
        				    //if (duration < (5*24*60*60)){        				    
        				        if (tagRank.containsKey(tag)){
        						    tagRank.put(tag, tagRank.get(tag)+ Math.pow(duration,(-0.5)));
        					    }else{
        					        tagRank.put(tag, Math.pow(duration,(-0.5)));
        					    }
        				    //}
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
        
        
        Map<Integer, Double> resultMap = this.bllMapTagValues.get(userID);
        
        
        Map<Integer, Double> sortedResultMap = new TreeMap<Integer, Double>(new DoubleMapComparator(resultMap));
        sortedResultMap.putAll(resultMap);
        if (userID == 0)
            System.out.println(sortedResultMap);
        
        sortedResultMap = new TreeMap<Integer, Double>(new DoubleMapComparator(tagRank));
        sortedResultMap.putAll(tagRank);
        
        if (userID == 0)
            System.out.println(sortedResultMap);
        
        for (Map.Entry<Integer, Double> entry : tagRank.entrySet()) {
            Double val = resultMap.get(entry.getKey());    
            resultMap.put(entry.getKey(), val == null ? (alpha) * entry.getValue().doubleValue() : (1-alpha) * val.doubleValue() + (alpha) * entry.getValue().doubleValue());
        }
        sortedResultMap = new TreeMap<Integer, Double>(new DoubleMapComparator(resultMap));
		sortedResultMap.putAll(resultMap);
		if (userID == 0){
		    System.out.println(sortedResultMap);
		    System.out.println("ends >> ");
		}
		return sortedResultMap;
    }
  
    
    /**
     * @param sampleSize
     * @return
     */
    private List<Map<Integer, Double>> calculateSocialTagScore(double alpha) {
        
        List<Map<Integer, Double>> results = new ArrayList<Map<Integer, Double>>();
        for (int i = trainSize; i < reader.getBookmarks().size(); i++) { // the test-set
            Bookmark data = reader.getBookmarks().get(i);
            Map<Integer, Double> map = getRankedTagList(data.getUserID(), data.getTimestampAsLong(), alpha);
            results.add(map);
        }
        
        
        
        return results;
    }
        
    /**
     * @return
     */
    public BookmarkReader predictSample() {
        List<Map<Integer, Double>> actValues = calculateSocialTagScore(0.5);
        // convert tag to int
        List<int[]> predictionValues = new ArrayList<int[]>();
        for (int i = 0; i < actValues.size(); i++) {
            Map<Integer, Double> modelVal = actValues.get(i);
            predictionValues.add(Ints.toArray(modelVal.keySet()));
        }
        reader.setTestLines(reader.getBookmarks().subList(trainSize, reader.getBookmarks().size()));
        PredictionFileWriter writer = new PredictionFileWriter(reader, predictionValues);
        writer.writeFile(this.filename + "_social");
        return reader;
    }      
}
