package processing.hashtag.baseline;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import com.google.common.base.Stopwatch;
import com.google.common.primitives.Ints;

import cc.mallet.pipe.StringList2FeatureSequence;
import cc.mallet.topics.ParallelTopicModel;
import cc.mallet.types.Alphabet;
import cc.mallet.types.IDSorter;
import cc.mallet.types.Instance;
import cc.mallet.types.InstanceList;
import common.Bookmark;
import common.DoubleMapComparator;
import common.MemoryThread;
import common.PerformanceMeasurement;
import common.Utilities;
import file.BookmarkReader;
import file.BookmarkWriter;
import file.PredictionFileWriter;
import processing.MalletCalculator;

public class MalletCalculatorTweet {

    private final static int MAX_RECOMMENDATIONS = 10;
    private final static int NUM_ITERATIONS = 2000;
    private final static int MAX_TERMS = 100;

    private static double TOPIC_THRESHOLD = 0.001;
    private final static double ALPHA = 0.01;
    private final static double BETA = 0.01;

    
    private int numTopics;
    private Map<Integer, Double> mostPopularTopics;
    private List<Map<Integer, Double>> docList;
    private List<Map<Integer, Double>> topicList;
    private List<Map<Integer, Integer>> maps;
    private InstanceList instances;
    
    public MalletCalculatorTweet(List<Map<Integer, Integer>> maps, int numTopics) {
        this.numTopics = numTopics;
        this.maps = maps;
        this.mostPopularTopics = new LinkedHashMap<Integer, Double>();
        initializeDataStructures();
    }
    
    private void initializeDataStructures() {
        this.instances = new InstanceList(new StringList2FeatureSequence());
        for (Map<Integer, Integer> map : this.maps) {
            List<String> tags = new ArrayList<String>();
            for (Map.Entry<Integer, Integer> entry : map.entrySet()) {
                for (int i = 0; i < entry.getValue(); i++) {
                    tags.add(entry.getKey().toString());
                }               
            }
            Instance inst = new Instance(tags, null, null, null);
            inst.setData(tags);
            this.instances.addThruPipe(inst);
        }
    }
    
    /**
     * What does this function returns.
     * @param LDA
     * @param maxTopicsPerDoc
     * @return
     */
    private List<Map<Integer, Double>> getMaxTopicsByDocs(ParallelTopicModel LDA, int maxTopicsPerDoc){

        List<Map<Integer, Double>> docList = new ArrayList<Map<Integer, Double>>();
        Map<Integer, Double> unsortedMostPopularTopics = new LinkedHashMap<Integer, Double>();
        int numDocs = this.instances.size();
        for (int doc = 0; doc < numDocs; ++doc) {
            Map<Integer, Double> topicList = new LinkedHashMap<Integer, Double>();
            double[] topicProbs = LDA.getTopicProbabilities(doc);
            //double probSum = 0.0;
            for (int topic = 0; topic < topicProbs.length && topic < maxTopicsPerDoc; topic++) {
                if (topicProbs[topic] > TOPIC_THRESHOLD) { // TODO
                    double newTopicProb = topicProbs[topic];
                    topicList.put(topic, newTopicProb);
                    Double oldTopicProb = unsortedMostPopularTopics.get(topic);
                    unsortedMostPopularTopics.put(topic, oldTopicProb == null ? newTopicProb : oldTopicProb.doubleValue() + newTopicProb);
                    //probSum += topicProbs[topic];
                }
            }
            //System.out.println("Topic Sum: " + probSum);
            Map<Integer, Double> sortedTopicList = new TreeMap<Integer, Double>(new DoubleMapComparator(topicList));
            sortedTopicList.putAll(topicList);
            docList.add(sortedTopicList);
        }
        
        
        Map<Integer, Double> sortedMostPopularTopics = new TreeMap<Integer, Double>(new DoubleMapComparator(unsortedMostPopularTopics));
        sortedMostPopularTopics.putAll(unsortedMostPopularTopics);
        for (Map.Entry<Integer, Double> entry : sortedMostPopularTopics.entrySet()) {
            if (this.mostPopularTopics.size() < MAX_RECOMMENDATIONS) {
                this.mostPopularTopics.put(entry.getKey(), entry.getValue());
            }
        }
        
        return docList;
    }
    
    /**
     * The created model is passed to this method, this method gives maximum terms by topic.
     * @param LDA
     * @param limit
     * @return
     */
    private List<Map<Integer, Double>> getMaxTermsByTopics(ParallelTopicModel LDA, int limit) {
        
        
        Alphabet alphabet = LDA.getAlphabet();
        List<Map<Integer, Double>> topicList = new ArrayList<Map<Integer, Double>>();
        int numTopics = LDA.getNumTopics();
        List<TreeSet<IDSorter>> sortedWords = LDA.getSortedWords();
        for (int topic = 0; topic < numTopics; ++topic) {
            Map<Integer, Double> termList = new LinkedHashMap<Integer, Double>();
            TreeSet<IDSorter> topicWords = sortedWords.get(topic);
            //int i = 0;
            double weightSum = 0.0;
            for (IDSorter entry : topicWords) {
                if (entry.getWeight() > 0.0) {
                    //if (i++ < limit) { 
                        int tag = Integer.parseInt(alphabet.lookupObject(entry.getID()).toString());
                        termList.put(tag, entry.getWeight());
                        weightSum += entry.getWeight();
                    //} else {
                    //  break;
                    //}
                }
            }
            // relative values
            //double relSum = 0.0;
            for (Map.Entry<Integer, Double> entry : termList.entrySet()) {
                //relSum += (entry.getValue() / weightSum);
                entry.setValue(entry.getValue() / weightSum);
            }
            //System.out.println("RelSum: " + relSum);
            topicList.add(termList);
        }   
        return topicList;
    }
    
    /**
     * What does this boolean value signify.
     * @param topicCreation
     */
    public void predictValuesProbs(boolean topicCreation) {
        
        ParallelTopicModel LDA = new ParallelTopicModel(this.numTopics, ALPHA * this.numTopics, BETA); // TODO
        LDA.addInstances(this.instances);
        LDA.setNumThreads(1);
        LDA.setNumIterations(NUM_ITERATIONS);
        LDA.setRandomSeed(43);
        try {
            LDA.estimate();
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.docList = getMaxTopicsByDocs(LDA, this.numTopics);
        System.out.println("Fetched Doc-List");
        this.topicList = !topicCreation ? getMaxTermsByTopics(LDA, MAX_TERMS) : null;
        System.out.println("Fetched Topic-List");
    }

    /**
     * What does this method do?
     * @param id
     * @param topicCreation
     * @return
     */
    public Map<Integer, Double> getValueProbsForID(int id, boolean topicCreation) {
        
        Map<Integer, Double> terms = null;
        if (id < this.docList.size()) {
            Map<Integer, Double> docVals = this.docList.get(id);
            if (this.topicList == null) {
                return docVals;
            }
            terms = new LinkedHashMap<Integer, Double>();

            for (Map.Entry<Integer, Double> topic : docVals.entrySet()) { // look at each assigned topic
                Set<Entry<Integer, Double>> entrySet = this.topicList.get(topic.getKey()).entrySet();
                double topicProb = topic.getValue();
                for (Map.Entry<Integer, Double> entry : entrySet) { // and its terms
                    if (topicCreation) {
                        // DEPRECATED
                        if (topicProb > TOPIC_THRESHOLD) {
                            terms.put(entry.getKey(), topicProb);
                            break; // only use first tag as topic-name with the topic probability
                        }
                    } else {
                        double wordProb = entry.getValue();
                        Double val = terms.get(entry.getKey());
                        terms.put(entry.getKey(), val == null ? wordProb * topicProb : val + wordProb * topicProb);
                    }
                }
            }
        }
        return terms;
    }
    
    public Map<Integer, Double> getMostPopularTopics() {
        return this.mostPopularTopics;
    }
    
    
    /***************************************************** Get Ranked Tag List *******************************************************/
    
    private static String timeString;
    
    private static Map<Integer, Double> getRankedTagList(BookmarkReader reader, Map<Integer, Double> userMap, Map<Integer, Double> resMap, boolean sorting) {
        
        Map<Integer, Double> resultMap = new LinkedHashMap<Integer, Double>();
        
        // either of they userMap or resMap is null. Based on which of them is null calculate the resultMap.
        if (userMap != null) {
            for (Map.Entry<Integer, Double> entry : userMap.entrySet()) {
                resultMap.put(entry.getKey(), entry.getValue().doubleValue());
            }
        }
        if (resMap != null) {
            for (Map.Entry<Integer, Double> entry : resMap.entrySet()) {
                double resVal = entry.getValue().doubleValue();
                Double val = resultMap.get(entry.getKey());
                resultMap.put(entry.getKey(), val == null ? resVal : val.doubleValue() + resVal);
            }
        }
                
        // sort the result map.
        if (sorting) {
            Map<Integer, Double> sortedResultMap = new TreeMap<Integer, Double>(new DoubleMapComparator(resultMap));
            sortedResultMap.putAll(resultMap);
            
            Map<Integer, Double> returnMap = new LinkedHashMap<Integer, Double>(MAX_RECOMMENDATIONS);
            int i = 0;
            for (Map.Entry<Integer, Double> entry : sortedResultMap.entrySet()) {
                if (i++ < MAX_RECOMMENDATIONS) {
                    returnMap.put(entry.getKey(), entry.getValue());
                } else {
                    break;
                }
            }
            return returnMap;
        }
        return resultMap;
    }
    
    /**
     * Start the creation of LDA model.
     * @param reader
     * @param sampleSize
     * @param sorting
     * @param numTopics
     * @param userBased
     * @param resBased
     * @param topicCreation
     * @return
     */
    private static List<Map<Integer, Double>> startLdaCreation(BookmarkReader reader, int sampleSize, boolean sorting, int numTopics, boolean userBased, boolean resBased, boolean topicCreation) {
        int size = reader.getBookmarks().size();
        int trainSize = size - sampleSize;
        //int oldTrainSize = trainSize;
        
        Stopwatch timer = new Stopwatch();
        timer.start();
        MalletCalculatorTweet userCalc = null;
        List<Map<Integer, Integer>> userMaps = null;
        
        // what is meant by the user based maps
        if (userBased) {
            userMaps = Utilities.getUserMaps(reader.getBookmarks().subList(0, trainSize));
            userCalc = new MalletCalculatorTweet(userMaps, numTopics);
            userCalc.predictValuesProbs(topicCreation);
            System.out.println("User-Training finished");
        }
        MalletCalculator resCalc = null;
        List<Map<Integer, Integer>> resMaps = null;
        
        // what is meant by the resource based maps
        if (resBased) {
            resMaps = Utilities.getResMaps(reader.getBookmarks().subList(0, trainSize));
            resCalc = new MalletCalculator(resMaps, numTopics);
            resCalc.predictValuesProbs(topicCreation);
            System.out.println("Res-Training finished");
        }
        List<Map<Integer, Double>> results = new ArrayList<Map<Integer, Double>>();
        if (topicCreation) {
            trainSize = 0;
        }
        timer.stop();
        long trainingTime = timer.elapsed(TimeUnit.MILLISECONDS);
        timer.reset();
        timer.start();
        int mpCount = 0;
        for (int i = trainSize; i < size; i++) { // the test set
            Bookmark data = reader.getBookmarks().get(i);
            int userID = data.getUserID();
            int resID = data.getResourceID();

            Map<Integer, Double> userPredMap = null;
            if (userCalc != null) {
                // create the user prediction map fopr the given user
                userPredMap = userCalc.getValueProbsForID(userID, topicCreation);
            }
            Map<Integer, Double> resPredMap = null;
            if (resCalc != null) {
                //if (i > oldTrainSize) {
                //  System.out.println("Test-Set");
                //}
                // if res specific lda call, the create resource specific map
                resPredMap = resCalc.getValueProbsForID(resID, topicCreation);
                if (topicCreation && resPredMap == null) {
                    resPredMap = resCalc.getMostPopularTopics();
                    mpCount++;
                }
            }
            
            // get ranked tag list for the current user
            Map<Integer, Double> map = getRankedTagList(reader, userPredMap, resPredMap, sorting);
            
            
            results.add(map);
        }
        timer.stop();
        long testTime = timer.elapsed(TimeUnit.MILLISECONDS);
        
        timeString = PerformanceMeasurement.addTimeMeasurement(timeString, true, trainingTime, testTime, (topicCreation ? size : sampleSize));
        System.out.println("MpCount: " + mpCount);
        return results;
    }
    
    public static BookmarkReader predictSample(String filename, int trainSize, int sampleSize, int numTopics, boolean userBased, boolean resBased) {
        Timer timerThread = new Timer();
        MemoryThread memoryThread = new MemoryThread();
        timerThread.schedule(memoryThread, 0, MemoryThread.TIME_SPAN);
        
        BookmarkReader reader = new BookmarkReader(trainSize, false);
        reader.readFile(filename);

        List<Map<Integer, Double>> ldaValues = startLdaCreation(reader, sampleSize, true, numTopics, userBased, resBased, false);
        
        List<int[]> predictionValues = new ArrayList<int[]>();
        for (int i = 0; i < ldaValues.size(); i++) {
            Map<Integer, Double> ldaVal = ldaValues.get(i);
            predictionValues.add(Ints.toArray(ldaVal.keySet()));
        }
        reader.setTestLines(reader.getBookmarks().subList(trainSize, reader.getBookmarks().size()));
        PredictionFileWriter writer = new PredictionFileWriter(reader, predictionValues);
        writer.writeFile(filename + "_lda_" + numTopics);
        
        timeString = PerformanceMeasurement.addMemoryMeasurement(timeString, false, memoryThread.getMaxMemory());
        timerThread.cancel();
        Utilities.writeStringToFile("./data/metrics/" + filename + "_lda_" + numTopics + "_TIME.txt", timeString);
        return reader;
    }
    
    public static void createSample(String filename, short numTopics, boolean tagRec, int trainSize, boolean personalizedTopicCreation) {
        Timer timerThread = new Timer();
        MemoryThread memoryThread = new MemoryThread();
        timerThread.schedule(memoryThread, 0, MemoryThread.TIME_SPAN);
         
        String outputFile = new String(filename) + "_lda_" + numTopics;

        if (tagRec) {
            TOPIC_THRESHOLD = 0.001;
        } else {
            TOPIC_THRESHOLD = 0.01;
        }
        Integer creationTrainSize = (personalizedTopicCreation ? trainSize : null);
        
        BookmarkReader reader = new BookmarkReader(creationTrainSize == null ? 0 : creationTrainSize.intValue(), false);
        reader.readFile(filename);
        int size = reader.getBookmarks().size();
        
        List<Map<Integer, Double>> ldaValues = startLdaCreation(reader, creationTrainSize == null ? 0 : size - creationTrainSize.intValue(), true, numTopics, false, true, true);
        
        List<int[]> predictionValues = new ArrayList<int[]>();
        //List<double[]> probValues = new ArrayList<double[]>();
        for (int i = 0; i < ldaValues.size(); i++) {
            Map<Integer, Double> ldaVal = ldaValues.get(i);
            predictionValues.add(Ints.toArray(ldaVal.keySet()));
            //probValues.add(Doubles.toArray(ldaVal.values()));
        }
        List<Bookmark> userSample = reader.getBookmarks().subList(0, size);     
        BookmarkWriter.writeSample(reader, userSample, outputFile, predictionValues, false);
        //if (creationTrainSize != null) {
            List<Bookmark> trainUserSample = reader.getBookmarks().subList(0, trainSize);
            List<int[]> trainPredictionValues = predictionValues.subList(0, trainSize);
            List<Bookmark> testUserSample = reader.getBookmarks().subList(trainSize, size);
            List<int[]> testPredictionValues = predictionValues.subList(trainSize, size);
            BookmarkWriter.writeSample(reader, trainUserSample, outputFile + "_train", trainPredictionValues, false);
            BookmarkWriter.writeSample(reader, testUserSample, outputFile + "_test", testPredictionValues, false);
        //}
                
        timeString = PerformanceMeasurement.addMemoryMeasurement(timeString, false, memoryThread.getMaxMemory());
        timerThread.cancel();
        Utilities.writeStringToFile("./data/metrics/" + filename + "_lda_creation_" + numTopics + "_TIME.txt", timeString);
    }

    
    
   
}
