package common;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

public class MergeUtil {

	public static Map<Integer, Double> mergeMapsWithThreshold(Map<Integer, Double> srcMap, Map<Integer, Double> targetMap, int limit) {
        Map<Integer, Double> resultMap = new LinkedHashMap<Integer, Double>();
		Map<Integer, Double> sortedTargetMap = MapUtil.sortByValue(targetMap);
		double threshold = 0.0;
		for (Map.Entry<Integer, Double> entry : sortedTargetMap.entrySet()) {
			threshold = entry.getValue();
			break;
		}
		System.out.println(threshold);
        
        for (Map.Entry<Integer, Double> srcEntry : srcMap.entrySet()) {
        	if (srcEntry.getValue() >= threshold) {
        		resultMap.put(srcEntry.getKey(), srcEntry.getValue());
        	} else {
        		break;
        	}
        }
        for (Map.Entry<Integer, Double> targetEntry: sortedTargetMap.entrySet()) {
        	if (resultMap.size() < limit) {
        		if (!resultMap.containsKey(targetEntry.getKey())) {
        			resultMap.put(targetEntry.getKey(), targetEntry.getValue());
        		}
        	} else {
        		break;
        	}
        }
        
        return resultMap;
	}
}
