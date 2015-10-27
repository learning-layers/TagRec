package processing;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.concurrent.TimeUnit;

import com.google.common.base.Stopwatch;
import com.google.common.primitives.Ints;

import common.Bookmark;
import common.CalculationType;
import common.CooccurenceMatrix;
import common.MemoryThread;
import common.PerformanceMeasurement;
import common.Utilities;
import file.BookmarkReader;
import file.PredictionFileWriter;

/**
 * @author spujari
 *
 */
/**
 * @author spujari
 *
 */
/**
 * @author spujari
 *
 */
/**
 * @author spujari
 *
 */
public class SocialCalculator {

    private BookmarkReader reader;
    private double dVal;
    private List<Bookmark> trainList;
    private static String timeString;
    private HashMap<Integer, HashMap<Integer, ArrayList<Long>>> userTagTimes ;
    private HashMap<String, List<String>> network;
    private int sampleSize;
    private int trainSize;
    private String userInfoPath;
    private List<String> idNameMap;
    
    public HashMap<Integer, HashMap<Integer, ArrayList<Long>>> getUserTagTimes(){
    	return userTagTimes;
    }
    
    public SocialCalculator(String tweetFilename, String networkFilename, String userInfoPath, int trainSize, int sampleSize) {
        this.sampleSize = sampleSize;
        this.trainSize = trainSize;
        this.userInfoPath = userInfoPath;
        reader = new BookmarkReader(trainSize, false);
        reader.readFile(tweetFilename);
        List<Bookmark> bookmarkList = reader.getBookmarks();
        this.userTagTimes = getUserTagTime(bookmarkList);
        this.idNameMap = reader.getUsers();
        this.network = getNetwork(networkFilename, getNameIdMap(idNameMap));
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
     * Takes a filename with userinformation that maps the username to the twitter ids.
     * @param userInfoMapPath
     * @return
     */
    private HashMap<String, String> getTwitterIdScreenNameMap(String userInfoMapPath){
        HashMap<String, String> twitterIdNameMap = new HashMap<String, String>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(new File(userInfoMapPath)));
            String line = "";
            while((line=br.readLine())!=null){
                String[] tokens = line.split("\t");
                String id = tokens[0];
                String name = tokens[1];
                twitterIdNameMap.put(id, name);
            }
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return twitterIdNameMap;
    }   
    
    /**
     * 
     * The user tags and their timeline information.
     * @param bookmarkList
     * @return
     */
    private HashMap<Integer, HashMap<Integer, ArrayList<Long>>> getUserTagTime(List<Bookmark> bookmarkList){
        
        HashMap<Integer, HashMap<Integer, ArrayList<Long>>> userTagTimes = new HashMap<Integer, HashMap<Integer,ArrayList<Long>>>();
        for(Bookmark bookmark : bookmarkList){
            List<Integer> taglist = bookmark.getTags();
            Integer user = bookmark.getUserID();
            String timestamp = bookmark.getTimestamp();
            Long timestampLong = bookmark.getTimestampAsLong();
            if (!userTagTimes.containsKey(user)){
                userTagTimes.put(user, new HashMap<Integer, ArrayList<Long>>());
            }
            
            for (Integer tag : taglist){
                if (!userTagTimes.get(user).containsKey(tag)){
                    userTagTimes.get(user).put(tag, new ArrayList<Long>());
                }
                userTagTimes.get(user).get(tag).add(timestampLong);
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
        HashMap<String, String> twitterIdScreenNameMap = getTwitterIdScreenNameMap(this.userInfoPath);
        try {
            File file = new File(filepath);
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line = "";
            while((line=br.readLine())!= null){
                String[] tokens = line.split("\t");
                String user1 = twitterIdScreenNameMap.get(tokens[0]);
                String user2 = twitterIdScreenNameMap.get(tokens[1]);
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
    
    private Map<Integer, Double> getRankedTagList(int userID, Long timesString){
        
        Map<Integer, Double> rankedList = new HashMap<Integer, Double>();
        // implementing for most used hashtags
        List<String> friendList = network.get(userID);
        for(String friend : friendList){
            int friendIndex = this.idNameMap.indexOf(friend);
            HashMap<Integer, ArrayList<Long>> tagTimestampMap = userTagTimes.get(friendIndex);
            for (Integer tag : tagTimestampMap.keySet()){
                ArrayList<Long> timestampList = tagTimestampMap.get(tag);
                
                // is there a timestamp less than the given timestamp
                for (Long timestampLong : timestampList){
                    
                }
                
                if (!rankedList.containsKey(tag)){
                    rankedList.put(tag, 0.0);
                }
                rankedList.put(tag, rankedList.get(tag) + 1);
            }
        }
        
        return rankedList;
    }
    
    private List<Map<Integer, Double>> startActCreation(int sampleSize) {
        int size = this.reader.getBookmarks().size();
        int trainSize = size - sampleSize;
        List<Map<Integer, Double>> results = new ArrayList<Map<Integer, Double>>();
        if (trainSize == size) {
            trainSize = 0;
        }
        for (int i = trainSize; i < size; i++) { // the test-set
            Bookmark data = reader.getBookmarks().get(i);
            Map<Integer, Double> map = getRankedTagList(data.getUserID(), data.getTimestampAsLong());
            results.add(map);
        }  
        return results;
    }
    
    public BookmarkReader predictSample(int trainSize, int sampleSize) {
        List<Map<Integer, Double>> actValues = startActCreation(sampleSize);
        List<int[]> predictionValues = new ArrayList<int[]>();
        for (int i = 0; i < actValues.size(); i++) {
            Map<Integer, Double> modelVal = actValues.get(i);
            predictionValues.add(Ints.toArray(modelVal.keySet()));
        }
        reader.setTestLines(reader.getBookmarks().subList(trainSize, reader.getBookmarks().size()));
        PredictionFileWriter writer = new PredictionFileWriter(reader, predictionValues);
        return reader;
    }      
}
