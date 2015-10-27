package processing;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

/**
 * @author spujari
 *
 */
public class ProcessFrequecyAndRecency {
    
	public ProcessFrequecyAndRecency(){
		
	}
	
    public void ProcessTagAnalytics(HashMap<Integer, HashMap<Integer, ArrayList<Long>>> userTagTimestampMap) {

        // frequency
        HashMap< Integer, Integer> tagFrequency = getTagFrequency(userTagTimestampMap);
        saveHashMap(tagFrequency, "./tagfrequency");
        
        // recency in duration
        HashMap< Integer, Integer> tagRecency = getRecencyInDuration(userTagTimestampMap);
        saveHashMap(tagRecency, "./tagrecency");
        
        // user unique tag count
        HashMap< Integer, Integer> uniqueTagCount = getUserUniqueTagCount(userTagTimestampMap);
        saveHashMap(uniqueTagCount, "./uniqueTagCount");
        // user tag count
        HashMap< Integer, Integer> userTagCount = getUserTagCount(userTagTimestampMap);
      // tag tag count
        HashMap< Integer, Integer> tagTagCount = getTagTagCount(userTagTimestampMap);
      // tag user count
        HashMap< Integer, Integer> tagUserCount = getTagUserCount(userTagTimestampMap);        
    }
    
    private void saveHashMap(HashMap<Integer, Integer> saveHashMap, String filename){
    	
    	File file = new File(filename);
    	try {
    		if (!file.exists()){
        		file.createNewFile();
        	}
			BufferedWriter bw = new  BufferedWriter(new FileWriter(file));
			for (Integer key : saveHashMap.keySet()){
				bw.write(key + "\t" + saveHashMap.get(key) + "\n");
	    	}
			bw.close();
    	} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
        
    /**
     * get how frequent is length of timestamp list.
     * @param userTagTimestampMap  user map to tag to list of timestamps of tag usage. 
     * @return a map of timestamp list lenght to number of times its occuring in the dataset.
     */
    private HashMap<Integer, Integer> getTagFrequency(HashMap<Integer, HashMap<Integer, ArrayList<Long>>> userTagTimestampMap){
    
        HashMap<Integer, Integer> hashMapInteger = new HashMap<Integer, Integer>();
        for (Integer user : userTagTimestampMap.keySet()){
            for (Integer tag : userTagTimestampMap.get(user).keySet()){
                List<Long> timestampList = userTagTimestampMap.get(user).get(tag);
                Integer timestampListLen = timestampList.size();
                if (!hashMapInteger.containsKey(timestampListLen)) {
                    hashMapInteger.put(timestampListLen, 0);
                }else{
                    
                }
                hashMapInteger.put(timestampListLen, hashMapInteger.get(timestampListLen) + 1); 
            }
        }
        return hashMapInteger;
     }
    
    /**
     * how recent the tags are used in the dataset.
     * @param userTagTimestampMap user map to tag to list of timestamps of tag usage.
     * @return the duration count between the 2 consecutive use of hastags.
     */
    private HashMap<Integer, Integer> getRecencyInDuration(HashMap<Integer, HashMap<Integer, ArrayList<Long>>> userTagTimestampMap){
        
        
        // TODO: assume that the timestamp are sorted by most recent first (decreasing order of timestamp)
        HashMap<Integer, Integer> durationCountMap = new HashMap<Integer, Integer>();
        for (Integer user : userTagTimestampMap.keySet()){
            for (Integer tag : userTagTimestampMap.get(user).keySet()){
                List<Long> timestampList = userTagTimestampMap.get(user).get(tag);
                Long lastTimestamp = new Long(0);
                for (Long currentTimestamp : timestampList){
                    if (lastTimestamp == 0){
                        continue;
                    }else{
                        int duration = (int)(lastTimestamp - currentTimestamp);
                        duration = getDurationAtGranularity(duration, 0); 
                        if (!durationCountMap.containsKey(duration)){
                            durationCountMap.put(duration, 0);
                        }else{
                        
                        }
                        durationCountMap.put(duration, durationCountMap.get(duration) + 1);
                        lastTimestamp = currentTimestamp;
                    }
                }
            }
        }
        return durationCountMap;
    }
        
    /**
     * How many unique tags does a user use.
     * @param userTagTimestampMap
     * @return
     */
    private HashMap<Integer, Integer> getUserUniqueTagCount(HashMap<Integer, HashMap<Integer, ArrayList<Long>>> userTagTimestampMap){        
        
        HashMap<Integer, Integer> userUniqueTagCount = new HashMap<Integer, Integer>();
        for (Integer user : userTagTimestampMap.keySet()){
            int numberUniqueTags = userTagTimestampMap.get(user).size();
            if (!userUniqueTagCount.containsKey(numberUniqueTags)){
                userUniqueTagCount.put(numberUniqueTags, 0);
            }else{
                
            }
            userUniqueTagCount.put(numberUniqueTags, userUniqueTagCount.get(numberUniqueTags) + 1);
        }
        return userUniqueTagCount;
    }
    
    /**
     * The total number of tags used by the user 
     * @param userTagTimestampMap
     * @return
     */
    private HashMap<Integer, Integer> getUserTagCount(HashMap<Integer, HashMap<Integer, ArrayList<Long>>> userTagTimestampMap){
        
        HashMap<Integer, Integer> userTotalTagCountMap = new HashMap<Integer, Integer>();
        for (Integer user : userTagTimestampMap.keySet()){
            int userTotalTagCount = 0;
            for (Integer tag : userTagTimestampMap.get(user).keySet()){
                List<Long> timestampList = userTagTimestampMap.get(user).get(tag);
                int lenTimestampList = timestampList.size();
                userTotalTagCount = userTotalTagCount + lenTimestampList;
            }
            if (!userTotalTagCountMap.containsKey(userTotalTagCount)){
                userTotalTagCountMap.put(userTotalTagCount, 0);
            }
            userTotalTagCountMap.put(userTotalTagCount, userTotalTagCountMap.get(userTotalTagCount) + 1);
        }
        return userTotalTagCountMap;
    }
    
    private HashMap<Integer, Integer> getTagTagCount(HashMap<Integer, HashMap<Integer, ArrayList<Long>>> userTagTimestampMap){

        /*
         * Count of tags in the system. Some tags are frequent the others.
         * */
        HashMap<Integer, Integer> tagTagCount = new HashMap<Integer, Integer>();
        for (Integer user : userTagTimestampMap.keySet()){
            for (Integer tag : userTagTimestampMap.get(user).keySet()){
                List<Long> timestampList = userTagTimestampMap.get(user).get(tag);
                if (!tagTagCount.containsKey(tag)){
                    tagTagCount.put(tag, 0);
                }else{
                    
                }
                tagTagCount.put(tag, tagTagCount.get(tag) + timestampList.size());
            }
        }
        // get the count of tags that are used particular number of time.
        HashMap<Integer, Integer> tagCountCountMap = new HashMap<Integer, Integer>();
        for (Integer tag : tagTagCount.keySet()){
            Integer count = tagTagCount.get(tag);
            if (!tagCountCountMap.containsKey(count)){
                tagCountCountMap.put(count, 0);
            }else{
                
            }
            tagCountCountMap.put(count,tagCountCountMap.get(count) + 1);
        }
        return tagCountCountMap;
    }
    
    private HashMap<Integer, Integer> getTagUserCount(HashMap<Integer, HashMap<Integer, ArrayList<Long>>> userTagTimestampMap){

        /**
         * Some tags have more users then the others.
         * */
        HashMap<Integer, Integer> tagUserCount = new HashMap<Integer, Integer>();
        for (Integer user : userTagTimestampMap.keySet()){
            for (Integer tag : userTagTimestampMap.get(user).keySet()){
                if(!tagUserCount.containsKey(tag)){
                    tagUserCount.put(tag, 0);
                }else{
                    
                }
                tagUserCount.put(tag, tagUserCount.get(tag));
            }
        }
        HashMap<Integer, Integer> tagUserCountCount = new HashMap<Integer, Integer>(); 
        for (Integer tag : tagUserCount.keySet()){
            Integer count = tagUserCount.get(tag);
            if (!tagUserCountCount.containsKey(count)){
                tagUserCountCount.put(count, 0);
            }else{
                
            }
            tagUserCountCount.put(count,tagUserCountCount.get(count) + 1);
        }
        return tagUserCountCount;
    }
    
    private void getSocialExternalHashTagCount(HashMap<Integer, HashMap<Integer, List<Long>>> userTagTimestampMap, 
                                                                            HashMap<Integer, HashSet<Integer>> network){
        /**
         * get the first use of hashtag was external or social.
         * */
        for (Integer user : userTagTimestampMap.keySet()){
            for (Integer tag : userTagTimestampMap.get(user).keySet()){
                List<Long> timestampList = userTagTimestampMap.get(user).get(tag);
            }
        }
        return;
    }
    
    private int getDurationAtGranularity(int duration, int granularityLevel){
        int granularity = 0;
        int millisInHour = 24 * 60 * 60 * 1000;
        int millisInDay = 24 * 60 * 60 * 1000;
        int millisInWeek = 24 * 60 * 60 * 1000;
        switch(granularityLevel){
            case 0:
                granularity = duration / millisInHour;
                break;
            case 1:
                granularity = duration / millisInDay;
                break;
            case 2:
                granularity = duration / millisInWeek;
                break;
        }
        return granularity;
    }
}
