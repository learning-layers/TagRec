package processing.hashtag;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.security.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;

import common.TimeUtil;

public class ProcessFrequencyRecencySocial {
    
	private String sampleDir;
    
    public ProcessFrequencyRecencySocial(String sampleDir, HashMap<String, HashMap<Integer, ArrayList<Long>>> userTagTime, HashMap<String, ArrayList<String>> network) {
        this.sampleDir = sampleDir;
        
        //ArrayList<Integer> durationSeconds = new ArrayList<Integer>();
        //ArrayList<Integer> durationMinutes = new ArrayList<Integer>();
        ArrayList<Integer> durationHours = new ArrayList<Integer>();
        
        for (String user : userTagTime.keySet())
        {
            ArrayList<String> friends = network.get(user);
            ArrayList<HashMap<Integer, ArrayList<Long>>> friendHashMapList = new ArrayList<HashMap<Integer,ArrayList<Long>>>();
            
            if (friends == null){
                continue;
            }
            
            for (String friend : friends){
                if (userTagTime.containsKey(friend)){
                    friendHashMapList.add(userTagTime.get(friend));
                }
            }
            HashMap<Integer, ArrayList<Long>> allTagTimeMap = getAllTagsHashMap(friendHashMapList);        
            //durationSeconds.addAll(createDurationList(userTagTime.get(user), allTagTimeMap, TimeUtil.SECOND));
            //durationMinutes.addAll(createDurationList(userTagTime.get(user), allTagTimeMap, TimeUtil.MINUTE));
            durationHours.addAll(createDurationList(userTagTime.get(user), allTagTimeMap, TimeUtil.HOUR));
        }
        //saveDurationList(durationSeconds, "recencySocialSeconds");
        //saveDurationList(durationMinutes, "recencySocialMinutes");
        saveDurationList(durationHours, "recencySocialHours");
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
    
    private ArrayList<Integer> createDurationList(HashMap<Integer, ArrayList<Long>> userTagTimeMap, HashMap<Integer, ArrayList<Long>> allTagTimeMap, int granularity){
        ArrayList<Integer> durationList = new ArrayList<Integer>();
        for (Integer tag : userTagTimeMap.keySet()){
            
            ArrayList<Long> timestamps = userTagTimeMap.get(tag);
            Collections.sort(timestamps);
            Collections.reverse(timestamps);
            
            if (allTagTimeMap.containsKey(tag)){
                
                ArrayList<Long> friendTimestampList = allTagTimeMap.get(tag);
                Collections.sort(friendTimestampList);
                Collections.reverse(friendTimestampList);
                
                for (Long timestamp : timestamps){
                    for (Long timestampFriend : friendTimestampList){
                        if(timestamp > timestampFriend){
                            int duration = (int)(timestamp - timestampFriend);
                            int count = TimeUtil.getDurationAtGranularity(duration, granularity);
                            count++;
                            durationList.add(count);
                            break;
                        }else{
                            continue;
                        }
                    }
                 }
            }
            
        }
        return durationList;
    }

    private void saveDurationList(ArrayList<Integer> durationList, String filename){
        Collections.sort(durationList);
        File file = new File("./data/metrics/" + this.sampleDir + "/" + filename);
        if (!file.exists()){
            try {
                file.createNewFile();
            } catch (IOException e) {
                
                System.out.println("create a new file failed!!");
            }
        }
        try {
            BufferedWriter fileWriter = new BufferedWriter(new FileWriter(file));
            for (Integer duration : durationList){
                fileWriter.write( duration + "\n");
            }
            fileWriter.close();
        } catch (IOException e) {
            System.out.println("Error! while creating a new file reader");
        }
    }
}
