package processing.hashtag.baseline;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import net.sf.javaml.utils.MathUtils;

public class HashtagEntropyCalculator {

    /**
     * Compute the hashtag entropy of all the hashtags and create the entropy.
     * 
     * @return
     */
    public static HashMap<Integer, Double> computeAllHashtagEntropyMap(
        HashMap<String, HashMap<Integer, ArrayList<Long>>> userTagTimestamps, int numberOfIntervals) {

        // compute the duration for which the dataset is there starting from the
        // first.
        Duration globalUsageDuration = computeWholeDatasetHashtagUsageDuration(userTagTimestamps);

        // Given the global usage duration and total number of duration interval
        // find out the duration interval.
        long durationSingleInterval = computeSingleIntervalDuration(globalUsageDuration, numberOfIntervals);

        // Given the global usage duration and duration of single interval
        ArrayList<Duration> binList = getIntervalDurationList(globalUsageDuration, durationSingleInterval);

        // number of times a hashtag occur in an interval.
        HashMap<Integer, HashMap<Integer, Integer>> hashtagIntervalCount = computeIntervalHashtagCount(
                userTagTimestamps, binList);
        
        // sum of usage of hashtags in all such intervals.
        HashMap<Integer, Double> hashtagAllIntervalSumCount = computeHashtagIntervalCountSum(hashtagIntervalCount,
                binList);
        
        // probability of a hashtag for a given interval.
        HashMap<Integer, HashMap<Integer, Double>> hashtagIntervalProbabilityScores = computeHashtagIntervalProbabilityScores(                hashtagIntervalCount, hashtagAllIntervalSumCount, binList.size());
        
        // create a hashtag entropy map for the users.
        HashMap<Integer, Double> hashtagEntropyMap = computeHashtagEntropyValue(hashtagIntervalProbabilityScores, binList.size());
        return hashtagEntropyMap;
        
    }

    /**
     * 
     * Set the minimum timestamp as the start timestamp and the maximum
     * timestamp as  end timestamp in the dataset.
     * @param userTagTimestamps
     *            a hashmap mapping users to tags and ArrayList of timestamps
     * @return
     */
    private static Duration computeWholeDatasetHashtagUsageDuration(
            HashMap<String, HashMap<Integer, ArrayList<Long>>> userTagTimestamps) {
        //TODO: Efficiency
        Duration duration = new Duration();
        for (String user : userTagTimestamps.keySet()) {
            if (userTagTimestamps.get(user) != null) {
                for (Integer tag : userTagTimestamps.get(user).keySet()) {
                    if (userTagTimestamps.get(user).get(tag) != null) {
                        ArrayList<Long> hastagUsageTimestampList = userTagTimestamps.get(user).get(tag);
                        for (Long timestamp : hastagUsageTimestampList) {
                            if (timestamp < duration.getStartTime() || duration.getStartTime() == 0) {
                                duration.setStartTime(timestamp);
                            } else if (timestamp > duration.getEndTime()) {
                                duration.setEndTime(timestamp);
                            }
                        }
                    }
                }
            }
        }
        return duration;
    }

    /**
     * Compute the single interval duration given global interval and number of
     * intervals within the global duration.
     * 
     * @param duration
     * @param numberOfIntervals
     * @return
     */
    private static int computeSingleIntervalDuration(Duration duration, int numberOfIntervals) {
        int interval = 0;
        double durationInterval = duration.getEndTime() - duration.getStartTime();
        interval = (int) durationInterval / numberOfIntervals;
        if(interval == 0){
            interval = 1;
        }
        return interval;
    }

    /**
     * Get the list of interval duration starting from the start time of
     * {@code globalUsageDuration}.
     * 
     * @param globalUsageDuration
     * @param intervalDuration
     * @return
     */
    private static ArrayList<Duration> getIntervalDurationList(Duration globalUsageDuration, long intervalDuration) {
        ArrayList<Duration> durationArrayList = new ArrayList<Duration>();
        long currentTime = globalUsageDuration.getStartTime();
        Duration duration = new Duration();
        while (currentTime <= globalUsageDuration.getEndTime()) {
            duration = new Duration();
            duration.setStartTime(currentTime);
            duration.setEndTime(currentTime + intervalDuration);
            durationArrayList.add(duration);
            currentTime += intervalDuration;
        }
        return durationArrayList;
    }

    /**
     * Convert userTagTimestamp map to hashtagIntervalCount map.
     * 
     * @param userTagTimes
     * @return
     */
    private static HashMap<Integer, HashMap<Integer, Integer>> computeIntervalHashtagCount(
            HashMap<String, HashMap<Integer, ArrayList<Long>>> userTagTimes, ArrayList<Duration> binList) {
        HashMap<Integer, HashMap<Integer, Integer>> hashtagIntervalCount = new HashMap<Integer, HashMap<Integer, Integer>>();
        for (String user : userTagTimes.keySet()) {
            for (Integer tag : userTagTimes.get(user).keySet()) {
                ArrayList<Long> timestamps = userTagTimes.get(user).get(tag);
                for (Long timestamp : timestamps) {
                    int intervalIndex = getIntervalIndex(timestamp, binList);
                    if (!hashtagIntervalCount.containsKey(tag)) {
                        hashtagIntervalCount.put(tag, new HashMap<Integer, Integer>());
                    }
                    if (!hashtagIntervalCount.get(tag).containsKey(intervalIndex)) {
                        hashtagIntervalCount.get(tag).put(intervalIndex, 0);
                    }
                    hashtagIntervalCount.get(tag).put(intervalIndex,
                            hashtagIntervalCount.get(tag).get(intervalIndex) + 1);
                }
            }
        }
        return hashtagIntervalCount;
    }

    /**
     * Compute hashtag count sum for each hashtag over all intervals.
     * 
     * @param hashtagIntervalCount
     * @return
     */
    private static HashMap<Integer, Double> computeHashtagIntervalCountSum(
            HashMap<Integer, HashMap<Integer, Integer>> hashtagIntervalCount, ArrayList<Duration> binList) {

        HashMap<Integer, Double> hashtagAllIntervalCountSum = new HashMap<Integer, Double>();
        for (Integer hashtag : hashtagIntervalCount.keySet()) {
            HashMap<Integer, Integer> intervalCount = hashtagIntervalCount.get(hashtag);
            if (intervalCount != null) {
                double denominatorFactor = computeSumHashtagAllIntervalCount(intervalCount) + 0.01 * binList.size();
                if (!hashtagAllIntervalCountSum.containsKey(hashtag)) {
                    hashtagAllIntervalCountSum.put(hashtag, denominatorFactor);
                }
            }
        }
        return hashtagAllIntervalCountSum;
    }

    /**
     * Compute the sum of count for a hashtag over all the intervals.
     * 
     * @param intervalCount
     * @return
     */
    private static int computeSumHashtagAllIntervalCount(HashMap<Integer, Integer> intervalCount) {
        int sumOverInterval = 0;
        for (int interval : intervalCount.keySet()) {
            sumOverInterval += intervalCount.get(interval);
        }
        return sumOverInterval;
    }

    /**
     * Compute the probability score of a user within a given interval.
     * 
     * @param hashtagIntervalCount
     * @param hashtagAllIntervalSumCount
     * @return
     */
    private static HashMap<Integer, HashMap<Integer, Double>> computeHashtagIntervalProbabilityScores(
            HashMap<Integer, HashMap<Integer, Integer>> hashtagIntervalCount,
            HashMap<Integer, Double> hashtagAllIntervalSumCount, int numberOfInterval) {
        HashMap<Integer, HashMap<Integer, Double>> hashtagIntervalProbabilityScores = new HashMap<Integer, HashMap<Integer, Double>>();
        for (Integer tag : hashtagIntervalCount.keySet()) {
            if (hashtagIntervalCount.get(tag) != null) {
                for (Integer interval : hashtagIntervalCount.get(tag).keySet()) {
                    double probabilityScore = (hashtagIntervalCount.get(tag).get(interval) + 0.01)
                            / ((hashtagAllIntervalSumCount.get(tag) + (0.01) * numberOfInterval ));
                    if (!hashtagIntervalProbabilityScores.containsKey(tag)) {
                        hashtagIntervalProbabilityScores.put(tag, new HashMap<Integer, Double>());
                    }
                    /*if (!hashtagIntervalProbabilityScores.get(tag).containsKey(interval)) {
                        hashtagIntervalProbabilityScores.get(tag).put(interval, 0.0);
                    }*/
                    hashtagIntervalProbabilityScores.get(tag).put(interval, probabilityScore);
                }
            }
        }
        return hashtagIntervalProbabilityScores;
    }

    /**
     * Compute and get the entropy score for all the hashtags. The entropy score
     * is for a particular hashtag.
     * 
     * @param hashtagIntervalProbabilityScores
     * @return
     */
    private static HashMap<Integer, Double> computeHashtagEntropyValue(
            HashMap<Integer, HashMap<Integer, Double>> hashtagIntervalProbabilityScores, int numberOfIntervals) {
        HashMap<Integer, Double> hashtagEntropyScoreMap = new HashMap<Integer, Double>();
        for (Integer hashtag : hashtagIntervalProbabilityScores.keySet()) {
            double hashtagEntropyNumerator = 0.0;
            if (hashtagIntervalProbabilityScores.get(hashtag) != null) {
                for (Integer interval : hashtagIntervalProbabilityScores.get(hashtag).keySet()) {
                    if (hashtagIntervalProbabilityScores.get(hashtag).get(interval) != null) {
                        hashtagEntropyNumerator += hashtagIntervalProbabilityScores.get(hashtag).get(interval)
                                * MathUtils.log2(hashtagIntervalProbabilityScores.get(hashtag).get(interval));
                    }
                }
            }
            double hashtagEntropyDenominator = MathUtils.log2(numberOfIntervals);
            double hashtagEntropyScore = - (hashtagEntropyNumerator) / (hashtagEntropyDenominator);
            hashtagEntropyScoreMap.put(hashtag, hashtagEntropyScore);
        }
        //System.out.println(" >> hashtagScore >> " + hashtagEntropyScoreMap);
        return hashtagEntropyScoreMap;
    }

    /**
     * Serialize the entropy value for the dataset.
     * @param hashtagEntropyMap
     * @param filePath
     */
    public static void serializeHashtagEntropy(Map<Integer, Double> hashtagEntropyMap, String filePath) {
        OutputStream file = null;
        try {
            file = new FileOutputStream(filePath);
            OutputStream buffer = new BufferedOutputStream(file);
            ObjectOutput output = new ObjectOutputStream(buffer);
            output.writeObject(hashtagEntropyMap);
            output.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Deserialize the Entropy Map.
     * @param filePath
     * @return
     */
    public static Map<Integer, Double> deSerializeHashtagEntropy(String filePath) {
        InputStream file = null;
        Map<Integer, Double> hashtagEntropyMap = null;
        try {
            file = new FileInputStream(filePath);
            InputStream buffer = new BufferedInputStream(file);
            ObjectInput input = new ObjectInputStream(buffer);
            hashtagEntropyMap = (Map<Integer, Double>) input.readObject();
            input.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return hashtagEntropyMap;
    }
    
    /**
     * Get the index of the time interval in which timestamp falls.
     * 
     * @param timestamp
     * @param intervalList
     * @return
     */
    private static int getIntervalIndex(long timestamp, ArrayList<Duration> intervalList) {
        int intervalIndex = -1;
        for (Duration duration : intervalList) {
            if (timestamp >= duration.getStartTime() && timestamp <= duration.getEndTime()) {
                intervalIndex = intervalList.indexOf(duration);
            } else {
                continue;
            }
        }
        if (intervalIndex == -1) {
            throw new RuntimeException();
        } else {
            return intervalIndex;
        }
    }

    /**
     * 
     * @author spujari
     *
     */
    private static class Duration {
        private long startTime;
        private long endTime;

        public long getStartTime() {
            return startTime;
        }

        public void setStartTime(long startTime) {
            this.startTime = startTime;
        }

        public long getEndTime() {
            return endTime;
        }

        public void setEndTime(long endTime) {
            this.endTime = endTime;
        }

        public double getDuration() {
            return (endTime - startTime);
        }
    }
}
