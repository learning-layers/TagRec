package processing;

import java.security.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;

public class ProcessFrequencyRecencySocial {
    
    
    private HashMap<String,HashMap<Integer, ArrayList<Long>>> userTagTime;
    private HashMap<String, ArrayList<String>> network;
    
    public ProcessFrequencyRecencySocial(HashMap<String, HashMap<Integer, ArrayList<Long>>> userTagTime, HashMap<String, ArrayList<String>> network) {
        
        // for each user get the tags of the user
        
        ArrayList<Integer> durations = new ArrayList<Integer>();
        for (String user : userTagTime.keySet())
        {
            ArrayList<String> friends = network.get(user);
            ArrayList<HashMap<Integer, ArrayList<Long>>> friendHashMapList = new ArrayList<HashMap<Integer,ArrayList<Long>>>();
            for (String friend : friends){
                friendHashMapList.add(userTagTime.get(friend));
            }
            
            HashMap<Integer, ArrayList<Long>> allTagTimeMap = getAllTagsHashMap(friendHashMapList);
            
            
        
        }
        
            // for each tag find the timestamp
                // for each timestamp find friends who has used if before the given user
        // for each friend create 
    
    }
    
    private HashMap<Integer, ArrayList<Long>> getAllTagsHashMap(ArrayList<HashMap<Integer, ArrayList<Long>>> friendHashMapList){
        HashMap<Integer, ArrayList<Long>> allTagTimeMap = new HashMap<Integer, ArrayList<Long>>();
        
        for(HashMap<Integer, ArrayList<Long>> tagTimestampMap : friendHashMapList){
            for (Integer tag : tagTimestampMap.keySet()){
                ArrayList<Long> timestamps = tagTimestampMap.get(tag);
                if (allTagTimeMap.containsKey(tag)){
                    allTagTimeMap.get(tag).addAll(timestamps);
                }else{
                    allTagTimeMap.put(tag, new ArrayList<Long>());
                    allTagTimeMap.get(tag).addAll(timestamps);
                }
            }
        }
        
        return allTagTimeMap;
    }
    
    private ArrayList<Integer> createDurationList(HashMap<Integer, ArrayList<Long>> userTagTimeMap, HashMap<Integer, ArrayList<Long>> allTagTimeMap){
        ArrayList<Integer> durationList = new ArrayList<Integer>();
        for (Integer tag : userTagTimeMap.keySet()){
            ArrayList<Long> timestamps = userTagTimeMap.get(tag);
            Collections.sort(timestamps);
            
            if (allTagTimeMap.containsKey(tag)){
                ArrayList<Long> friendTimestampList = allTagTimeMap.get(tag);
                Collections.sort(friendTimestampList);
                for (Long timestamp : timestamps){
                    for (Long timestampFriend : friendTimestampList){
                        if(timestamp < timestampFriend){
                            long duration = timestampFriend - timestamp;
                        }
                    }
                 }
            }
        }
        return durationList;
    }
    
    private int durationCounts(long duration){
        
    }
    

}
