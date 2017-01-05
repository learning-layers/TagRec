package processing.hashtag;

import java.util.Map;
import java.util.TreeMap;
import common.DoubleMapComparatorGeneric;

public class TagRecommendationUtil {
    /**
     * Sorted Map values ascending to descending.
     * 
     * @param resultMap
     * @return
     */
    public static <T> Map<T, Double> getSortedMap(Map<T, Double> resultMap) {
            System.out.println("result Map without sort >> " + resultMap);
            Map<T, Double> sortedResultMap = new TreeMap<T, Double>(new DoubleMapComparatorGeneric<T>(resultMap));
            sortedResultMap.putAll(resultMap);
            System.out.println("resultMap >> " + sortedResultMap);
            return sortedResultMap;
    }
}
