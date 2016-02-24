package processing;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

/**
 * @author spujari
 *
 */
public class ProcessFrequencyRecency {
    
	private String sampleDir;
	
    public void ProcessTagAnalytics(String sampleDir, HashMap<String, HashMap<Integer, ArrayList<Long>>> userTagTimestampMap) {
    	this.sampleDir = sampleDir;
    	
        // frequency
        //HashMap<Integer, Integer> tagFrequency = getTagFrequency(userTagTimestampMap);
        //saveHashMap(tagFrequency, "./tagfrequency");
        // recency in duration
        //HashMap< Integer, Integer> tagRecency = getRecencyInDuration(userTagTimestampMap);

        //saveHashMap(getRecencyInDuration(userTagTimestampMap, TimeUtil.SECOND), "./tagrecency" + "_" + "Seconds");
        //saveHashMap(getRecencyInDuration(userTagTimestampMap, TimeUtil.MINUTE), "./tagrecency" + "_" + "MINUTE");
        saveHashMap(getRecencyInDuration(userTagTimestampMap, TimeUtil.HOUR), "recencyHours" );
        //saveHashMap(getRecencyInDuration(userTagTimestampMap, TimeUtil.DAY), "./tagrecency" + "_" + "DAY" );
        //saveHashMap(getRecencyInDuration(userTagTimestampMap, TimeUtil.FIFTEEN_DAYS), "./tagrecency" + "_" + "FIFTEEN_DAYS");
        //saveHashMap(getRecencyInDuration(userTagTimestampMap, TimeUtil.MONTH), "./tagrecency" + "_" + "MONTH");
        // user unique tag count
        //HashMap< Integer, Integer> uniqueTagCount = getUserUniqueTagCount(userTagTimestampMap);
        //saveHashMap(uniqueTagCount, "./uniqueTagCount");
        // user tag count
        //HashMap< Integer, Integer> userTagCount = getUserTagCount(userTagTimestampMap);
      // tag tag count
       // HashMap< Integer, Integer> tagTagCount = getTagTagCount(userTagTimestampMap);
      // tag user count
        //HashMap< Integer, Integer> tagUserCount = getTagUserCount(userTagTimestampMap);        
    }
    
    private void saveHashMap(HashMap<Integer, Integer> saveHashMap, String filename){
    	File file = new File("./data/metrics/" + this.sampleDir + "/" + filename);
    	try {
    		if (!file.exists()){
        		file.createNewFile();
        	}
			BufferedWriter bw = new  BufferedWriter(new FileWriter(file));
			for (Integer key : saveHashMap.keySet()){
				for(int i=0; i < saveHashMap.get(key) ; i++){
				    bw.write(key + "\n");
				}
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
    private HashMap<Integer, Integer> getTagFrequency(HashMap<String, HashMap<Integer, ArrayList<Long>>> userTagTimestampMap){
    
        HashMap<Integer, Integer> hashMapInteger = new HashMap<Integer, Integer>();
        for (String user : userTagTimestampMap.keySet()){
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
    private HashMap<Integer, Integer> getRecencyInDuration(HashMap<String, HashMap<Integer, ArrayList<Long>>> userTagTimestampMap,
                                                            int durationType){
        
        HashMap<Integer, Integer> durationCountMap = new HashMap<Integer, Integer>();
        for (String user : userTagTimestampMap.keySet()){
            for (Integer tag : userTagTimestampMap.get(user).keySet()){
                Long lastTimestamp = new Long(0);
                List<Long> timestampList = userTagTimestampMap.get(user).get(tag);
                Collections.sort(timestampList);
                
                for (Long currentTimestamp : timestampList){
                    if (lastTimestamp == 0){
                        lastTimestamp = currentTimestamp;
                        continue;
                    }else{
                        int duration = (int)(currentTimestamp - lastTimestamp);
                        duration = TimeUtil.getDurationAtGranularity(duration, durationType); 
                        duration++;
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
    private HashMap<Integer, Integer> getUserUniqueTagCount(HashMap<String, HashMap<Integer, ArrayList<Long>>> userTagTimestampMap){        
        
        HashMap<Integer, Integer> userUniqueTagCount = new HashMap<Integer, Integer>();
        for (String user : userTagTimestampMap.keySet()){
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
    private HashMap<Integer, Integer> getUserTagCount(HashMap<String, HashMap<Integer, ArrayList<Long>>> userTagTimestampMap){
        
        HashMap<Integer, Integer> userTotalTagCountMap = new HashMap<Integer, Integer>();
        for (String user : userTagTimestampMap.keySet()){
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
    
    private HashMap<Integer, Integer> getTagTagCount(HashMap<String, HashMap<Integer, ArrayList<Long>>> userTagTimestampMap){

        /*
         * Count of tags in the system. Some tags are frequent the others.
         * */
        HashMap<Integer, Integer> tagTagCount = new HashMap<Integer, Integer>();
        for (String user : userTagTimestampMap.keySet()){
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
    
    private HashMap<Integer, Integer> getTagUserCount(HashMap<String, HashMap<Integer, ArrayList<Long>>> userTagTimestampMap){

        /**
         * Some tags have more users then the others.
         * */
        HashMap<Integer, Integer> tagUserCount = new HashMap<Integer, Integer>();
        for (String user : userTagTimestampMap.keySet()){
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
    }
