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
    private HashMap<String, HashMap<String, ArrayList<Long>>> userTagTimes ;
    private HashMap<String, List<String>> network;
    private List<String> users;
    private List<String> tags;
    private int sampleSize;
    private int trainSize;
    private String userInfoPath;
    private List<String> idNameMap;
    private HashMap<String, Integer> nameIdMap;
    private HashMap<String, String> twitterIdScreenNameMap;
    private HashMap<String, String> twitterScreenNameIdMap;
    
    public SocialCalculator(String tweetFilename, String networkFilename, String userInfoPath, int trainSize, int sampleSize) {
        this.sampleSize = sampleSize;
        this.trainSize = trainSize;
        this.userInfoPath = userInfoPath;
        reader = new BookmarkReader(trainSize, false);
        reader.readFile(tweetFilename);
        List<Bookmark> bookmarkList = reader.getBookmarks();
        this.users = reader.getUsers();
        this.tags = reader.getTags();
        this.sampleSize = bookmarkList.size();
        this.trainSize =  (int) ((int)bookmarkList.size() * 0.9);
        this.userTagTimes = getUserTagTime(bookmarkList.subList(0, this.trainSize));
        this.idNameMap = reader.getUsers();
        this.nameIdMap = this.getNameIdMap(idNameMap);
        this.network = getNetwork(networkFilename, getNameIdMap(idNameMap));
        this.twitterIdScreenNameMap = getTwitterIdScreenNameMap(userInfoPath);
        this.twitterScreenNameIdMap = getTwitterScreenNameIdMap(userInfoPath);
        /*
         * Agenda:
         * 
         * >> read the twitter list of bookmarks and create a bookmark list out of it
         * 
         * >> in case of bookmarks they are mapped to the integer values mapped to screen anem and tags
         * */
    }
    
    public HashMap<String, HashMap<String, ArrayList<Long>>> getUserTagTimes(){
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
    
    
    private HashMap<String, String> getTwitterScreenNameIdMap(String userInfoPath){
    	HashMap<String, String> twitterScreenNameIdMap = new HashMap<String, String>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(new File(userInfoPath)));
            String line = "";
            while((line=br.readLine())!=null){
                String[] tokens = line.split("\t");
                String id = tokens[0];
                String name = tokens[1];
                twitterScreenNameIdMap.put(name, id);
            }
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return twitterScreenNameIdMap;
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
    private HashMap<String, HashMap<String, ArrayList<Long>>> getUserTagTime(List<Bookmark> bookmarkList){
        HashMap<String, HashMap<String, ArrayList<Long>>> userTagTimes = new HashMap<String, HashMap<String,ArrayList<Long>>>();
        for(Bookmark bookmark : bookmarkList){
            List<Integer> taglist = bookmark.getTags();
            Integer userId = bookmark.getUserID();
            // get the userName for the id.
            String userName = users.get(userId);
            //userName = this.twitterScreenNameIdMap.get(userName);
            String timestamp = bookmark.getTimestamp();
            Long timestampLong = bookmark.getTimestampAsLong();
            if (!userTagTimes.containsKey(userName)){
                userTagTimes.put(userName, new HashMap<String, ArrayList<Long>>());
            }
            for (Integer tag : taglist){
            	String tagName = this.tags.get(tag);
            	if (!userTagTimes.get(userName).containsKey(tag)){
                    userTagTimes.get(userName).put(tagName, new ArrayList<Long>());
                }
                userTagTimes.get(userName).get(tagName).add(timestampLong);
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
    
    private Map<String, Double> getRankedTagList(int userID, Long timesString){
        Map<String, Double> rankedList = new HashMap<String, Double>();
        String user = this.users.get(userID);
        List<String> friendList = network.get(user);
        HashMap <String, Integer> tagRank = new HashMap<String, Integer>();
        if (friendList == null){
        	return rankedList;
        }
        for(String friend : friendList){
        	HashMap<String, ArrayList<Long>> tagTimestampMap = userTagTimes.get(friend);
        	if (tagTimestampMap != null){
        		for (String tag : tagTimestampMap.keySet()){
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
        
        Map<String, Double> sortedResultMap = new TreeMap<String, Double>(new DoubleMapComparatorKeyString(rankedList));
		sortedResultMap.putAll(sortedResultMap);
		return rankedList;
    }
    
    public LinkedHashMap sortHashMapByValuesD(HashMap passedMap) {
    	   List mapKeys = new ArrayList(passedMap.keySet());
    	   List mapValues = new ArrayList(passedMap.values());
    	   Collections.sort(mapValues);
    	   Collections.sort(mapKeys);

    	   LinkedHashMap sortedMap = new LinkedHashMap();

    	   java.util.Iterator valueIt = mapValues.iterator();
    	   while (valueIt.hasNext()) {
    	       Object val = valueIt.next();
    	       Iterator keyIt = mapKeys.iterator();

    	       while (keyIt.hasNext()) {
    	           Object key = keyIt.next();
    	           String comp1 = passedMap.get(key).toString();
    	           String comp2 = val.toString();

    	           if (comp1.equals(comp2)){
    	               passedMap.remove(key);
    	               mapKeys.remove(key);
    	               sortedMap.put((String)key, (Integer)val);
    	               break;
    	           }

    	       }

    	   }
    	   return sortedMap;
   }
    
    private List<Map<String, Double>> startActCreation(int sampleSize) {
        List<Map<String, Double>> results = new ArrayList<Map<String, Double>>();
        for (Map<String, Double> map : results) {
			double denom = 0.0;
			if (map != null) {
				for (Map.Entry<String, Double> entry : map.entrySet()) {
					if (entry != null) {
						double actVal = Math.log(entry.getValue());
						denom += Math.exp(actVal);
						entry.setValue(actVal);
					}
				}
				//denomList.add(denom);
				for (Map.Entry<String, Double> entry : map.entrySet()) {
						if (entry != null) {
							double actVal = Math.exp(entry.getValue());
							entry.setValue(actVal / denom);
						}
				}
			}
		}
        for (int i = trainSize; i < sampleSize; i++) { // the test-set
            Bookmark data = reader.getBookmarks().get(i);
            Map<String, Double> map = getRankedTagList(data.getUserID(), data.getTimestampAsLong());
            results.add(map);
        }  
        return results;
    }
    
    public BookmarkReader predictSample() {
        List<Map<String, Double>> actValues = startActCreation(this.sampleSize);
        // convert tag to int
        List<int[]> predictionValues = new ArrayList<int[]>();
        for (int i = 0; i < actValues.size(); i++) {
            Map<String, Double> modelVal = actValues.get(i);
            //predictionValues.add(Ints.toArray(modelVal.keySet()));
        }
        reader.setTestLines(reader.getBookmarks().subList(trainSize, reader.getBookmarks().size()));
        PredictionFileWriter writer = new PredictionFileWriter(reader, predictionValues);
        return reader;
    }      
}
