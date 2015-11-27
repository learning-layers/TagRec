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
import common.Utilities;
import file.BookmarkReader;
import file.PredictionFileWriter;

/**
 * @author spujari
 *
 */
public class SocialCalculator {

	private String sampleDir;
    private String filename;
    private BookmarkReader reader;
    
    private HashMap<String, HashMap<Integer, ArrayList<Long>>> userTagTimes ;
    private HashMap<String, ArrayList<String>> network;
    private List<String> users;
    private int trainSize;
    private List<String> idNameMap;
    private List<Map<Integer, Double>> resultMapPersonalBLLAllUsers;
    private List<Map<Integer, Double>> resultMapPersonalFreqAllUsers;
    
    
    public SocialCalculator(String sampleDir, String userTweetFilename, String networkFilename, int trainSize, int testSize) {
        this.sampleDir = sampleDir;
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
        resultMapPersonalBLLAllUsers = BLLCalculator.getArtifactMaps(reader, trainList, testList, false, new ArrayList<Long>(), new ArrayList<Double>(), 0.5, true);
        this.resultMapPersonalFreqAllUsers = Utilities.getNormalizedMaps(this.reader.getBookmarks().subList(0, trainSize), false);
        System.out.println("bll values list size>> " + resultMapPersonalBLLAllUsers.size());
    }
    
    public HashMap<String, HashMap<Integer, ArrayList<Long>>> getUserTagTimes(){
    	return userTagTimes;
    }
    
    public HashMap<String, ArrayList<String>> getNetwork(){
        return this.network;
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
    private HashMap<String, ArrayList<String>> getNetwork(String filepath, HashMap<String, Integer> nameIdMap){
        
        HashMap<String, ArrayList<String>> network = new HashMap<String, ArrayList<String>>();
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
    
    private Map<Integer, Double> getRankedTagListSocialFrequency(int userID, Long timesString, boolean sort) {
        
        String user = this.users.get(userID);
        List<String> friendList = network.get(user);
        HashMap <Integer, Double> tagRank = new HashMap<Integer, Double>();
        if (friendList == null){
            return tagRank;
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
                            
                            if (tagRank.containsKey(tag)){
                                tagRank.put(tag, tagRank.get(tag)+ Math.pow(duration,1.0));
                            }else{
                                tagRank.put(tag, 1.0);
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
        
        if (sort) {
	        Map<Integer, Double> sortedResultMap = new TreeMap<Integer, Double>(new DoubleMapComparator(tagRank));
	        sortedResultMap.putAll(tagRank);
	        return sortedResultMap;
        } else {
        	return tagRank;
        }
    }
    
    private Map<Integer, Double> getRankedTagListSocial(int userID, Long timesString, double exponentSocial){
        String user = this.users.get(userID);
        List<String> friendList = network.get(user);
        HashMap <Integer, Double> tagRank = new HashMap<Integer, Double>();
        
        if (friendList == null){
            return tagRank;
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
                            
                            if (tagRank.containsKey(tag)){
                                tagRank.put(tag, tagRank.get(tag)+ Math.pow(duration,(-1)*(exponentSocial)));
                            }else{
                                tagRank.put(tag, Math.pow(duration,(-1)*(exponentSocial)));
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
        
        Map<Integer, Double> sortedResultMap = new TreeMap<Integer, Double>(new DoubleMapComparator(tagRank));
        sortedResultMap.putAll(tagRank);
        return sortedResultMap;

    }
    
    private Map<Integer, Double> getRankedTagListSocialFrequencyHybrid(int userID, Long timeString, double beta){
    	
    	Map<Integer, Double> resultMapSocialFreq = getRankedTagListSocialFrequency(userID, timeString, false);
    	
    	// get frequency based recommendation score
    	
    	
    	// combine two scores
    	Map<Integer, Double> resultMapPersonalFreq = this.resultMapPersonalFreqAllUsers.get(userID);
        for (Map.Entry<Integer, Double> entry : resultMapPersonalFreq.entrySet()) {
            Double val = resultMapSocialFreq.get(entry.getKey());    
            resultMapSocialFreq.put(entry.getKey(), val == null ? (beta) * entry.getValue().doubleValue() : (1-beta) * val.doubleValue() + (beta) * entry.getValue().doubleValue());
        }
        
        Map<Integer, Double>sortedResultMap = new TreeMap<Integer, Double>(new DoubleMapComparator(resultMapSocialFreq));
        sortedResultMap.putAll(resultMapSocialFreq);
        return sortedResultMap;
    } 
    
    private Map<Integer, Double> getRankedTagListSocialBLLHybrid(int userID, Long timesString, double beta, double exponentSocial){
        Map<Integer, Double> rankedList = new HashMap<Integer, Double>();
        String user = this.users.get(userID);
        List<String> friendList = network.get(user);
        HashMap <Integer, Double> tagRank = new HashMap<Integer, Double>();
        if (friendList != null){
            for(String friend : friendList){
                HashMap<Integer, ArrayList<Long>> tagTimestampMap = userTagTimes.get(friend);
                if (tagTimestampMap != null){
                    for (Integer tag : tagTimestampMap.keySet()){
                        ArrayList<Long> timestampList = tagTimestampMap.get(tag);
                        for (Long timestampLong : timestampList){
                            if(timesString > timestampLong ){
                                long duration = timesString - timestampLong;
                                if (tagRank.containsKey(tag)){
                                   tagRank.put(tag, tagRank.get(tag)+ Math.pow(duration,(-1)*(exponentSocial)));
                                }else{
                                   tagRank.put(tag, Math.pow(duration,(-0.5)));
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
            resultMap.put(entry.getKey(), val == null ? (beta) * entry.getValue().doubleValue() : (1-beta) * val.doubleValue() + (beta) * entry.getValue().doubleValue());
        }
        Map<Integer, Double>sortedResultMap = new TreeMap<Integer, Double>(new DoubleMapComparator(resultMap));
        sortedResultMap.putAll(resultMap);
        return sortedResultMap;
    }
    
    /**
     * @param sampleSize
     * @return
     */
    private List<Map<Integer, Double>> calculateSocialTagScore(double beta, double exponentSocial, String algorithm) {
        
        List<Map<Integer, Double>> results = new ArrayList<Map<Integer, Double>>();
        for (int i = trainSize; i < reader.getBookmarks().size(); i++) { // the test-set
            Bookmark data = reader.getBookmarks().get(i);
            Map<Integer, Double> map = new HashMap<Integer, Double>();
            if(algorithm.equals("social_freq")){
                map = getRankedTagListSocialFrequency(data.getUserID(), data.getTimestampAsLong(), true);
            }else if(algorithm.equals("social")){
                map = getRankedTagListSocial(data.getUserID(), data.getTimestampAsLong(), exponentSocial);
            }else if (algorithm.equals("hybrid")) {
                map = getRankedTagListSocialBLLHybrid(data.getUserID(), data.getTimestampAsLong(), beta, exponentSocial);
            }else if (algorithm.equals("hybrid_freq")){
            	map = getRankedTagListSocialFrequencyHybrid(data.getUserID(), data.getTimestampAsLong(), beta);
            }
            results.add(map);
        }
        return results;
    }
    
    /**
     * @return
     */
    public BookmarkReader predictSample(double exponentSocial, double beta, String algorithm) {
        List<Map<Integer, Double>> actValues = calculateSocialTagScore(beta, exponentSocial, algorithm);
        List<int[]> predictionValues = new ArrayList<int[]>();
        for (int i = 0; i < actValues.size(); i++) {
            Map<Integer, Double> modelVal = actValues.get(i);
            predictionValues.add(Ints.toArray(modelVal.keySet()));
        }
        reader.setTestLines(reader.getBookmarks().subList(trainSize, reader.getBookmarks().size()));
        PredictionFileWriter writer = new PredictionFileWriter(reader, predictionValues);
        writer.writeFile(this.filename + "_social" + exponentSocial + "_" + beta + "_" + algorithm);
        return reader;
    }      
}
