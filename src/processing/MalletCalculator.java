/*
 TagRecommender:
 A framework to implement and evaluate algorithms for the recommendation
 of tags.
 Copyright (C) 2013 Dominik Kowald
 
 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU Affero General Public License as
 published by the Free Software Foundation, either version 3 of the
 License, or (at your option) any later version.
 
 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU Affero General Public License for more details.
 
 You should have received a copy of the GNU Affero General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package processing;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import com.google.common.base.Stopwatch;
import com.google.common.primitives.Doubles;
import com.google.common.primitives.Ints;

import common.DoubleMapComparator;
import common.Bookmark;
import common.Utilities;
import cc.mallet.pipe.Array2FeatureVector;
import cc.mallet.pipe.CharSequence2TokenSequence;
import cc.mallet.pipe.CharSequenceArray2TokenSequence;
import cc.mallet.pipe.CharSequenceLowercase;
import cc.mallet.pipe.Pipe;
import cc.mallet.pipe.PrintInputAndTarget;
import cc.mallet.pipe.SerialPipes;
import cc.mallet.pipe.StringList2FeatureSequence;
import cc.mallet.pipe.TokenSequence2FeatureSequence;
import cc.mallet.pipe.TokenSequenceLowercase;
import cc.mallet.topics.ParallelTopicModel;
import cc.mallet.types.Alphabet;
import cc.mallet.types.FeatureSequence;
import cc.mallet.types.IDSorter;
import cc.mallet.types.Instance;
import cc.mallet.types.InstanceList;
import cc.mallet.types.TokenSequence;
import file.PredictionFileWriter;
import file.BookmarkReader;
import file.BookmarkSplitter;

public class MalletCalculator {

	private final static int MAX_RECOMMENDATIONS = 10;
	private final static int MAX_TERMS = 100;
	private final static int NUM_THREADS = 10;
	private final static int NUM_ITERATIONS = 2000;
	private final static double ALPHA = 0.01;
	private final static double BETA = 0.01;
	private final static double TOPIC_THRESHOLD = 0.001;
	
	private int numTopics;
	private List<Map<Integer, Integer>> maps;
	private InstanceList instances;
	private List<Map<Integer, Double>> docList;
	private List<Map<Integer, Double>> topicList;
	
	public MalletCalculator(List<Map<Integer, Integer>> maps, int numTopics) {
		this.numTopics = numTopics;
		this.maps = maps;		
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
	
	private List<Map<Integer, Double>> getMaxTopicsByDocs(ParallelTopicModel LDA, int maxTopicsPerDoc) {
		List<Map<Integer, Double>> docList = new ArrayList<Map<Integer, Double>>();
        int numDocs = this.instances.size();
        for (int doc = 0; doc < numDocs; ++doc) {
        	Map<Integer, Double> topicList = new LinkedHashMap<Integer, Double>();
        	double[] topicProbs = LDA.getTopicProbabilities(doc);
        	//double probSum = 0.0;
        	for (int topic = 0; topic < topicProbs.length && topic < maxTopicsPerDoc; topic++) {
        		//if (topicProbs[topic] > 0.01) { // TODO
        			topicList.put(topic, topicProbs[topic]);
        			//probSum += topicProbs[topic];
        		//}
        	}
			//System.out.println("Topic Sum: " + probSum);
        	Map<Integer, Double> sortedTopicList = new TreeMap<Integer, Double>(new DoubleMapComparator(topicList));
        	sortedTopicList.putAll(topicList);
        	docList.add(sortedTopicList);
        }       
		return docList;
	}
	
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
    			if (entry.getWeight() > 0.0) { // TODO
    				//if (i++ < limit) { // TODO
    					int tag = Integer.parseInt(alphabet.lookupObject(entry.getID()).toString());
    					termList.put(tag, entry.getWeight());
    					weightSum += entry.getWeight();
    				//} else {
    				//	break;
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
	
	public void predictValuesProbs() {
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
		this.topicList = getMaxTermsByTopics(LDA, MAX_TERMS);
		System.out.println("Fetched Topic-List");
	}
	
	public Map<Integer, Double> getValueProbsForID(int id, boolean topicCreation) {
		Map<Integer, Double> terms = null;
		if (id < this.docList.size()) {
			terms = new LinkedHashMap<Integer, Double>();
			Map<Integer, Double> docVals = this.docList.get(id);
			for (Map.Entry<Integer, Double> topic : docVals.entrySet()) { // look at each assigned topic
				Set<Entry<Integer, Double>> entrySet = this.topicList.get(topic.getKey()).entrySet();
				double topicProb = topic.getValue();
				for (Map.Entry<Integer, Double> entry : entrySet) { // and its terms
					if (topicCreation) {
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
	
	// Statics -------------------------------------------------------------------------------------------------------------------------	
	
	private static List<Double> getDenoms(List<Map<Integer, Double>> maps) {
		List<Double> denoms = new ArrayList<Double>();
		for (Map<Integer, Double> map : maps) {
			double denom = 0.0;
			for (Map.Entry<Integer, Double> entry : map.entrySet()) {
				denom += Math.exp(entry.getValue());
			}
			denoms.add(denom);
		}
		
		return denoms;
	}
	
	private static Map<Integer, Double> getRankedTagList(BookmarkReader reader, Map<Integer, Double> userMap, double userDenomVal, 
			Map<Integer, Double> resMap, double resDenomVal, boolean sorting, boolean smoothing, boolean topicCreation) {
		
		Map<Integer, Double> resultMap = new LinkedHashMap<Integer, Double>();
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
		/*
		double size = (double)reader.getTagAssignmentsCount();
		double tagSize = (double)reader.getTags().size();
		Map<Integer, Double> resultMap = new LinkedHashMap<Integer, Double>();
		for (int i = 0; i < tagSize; i++) {
			double pt = (double)reader.getTagCounts().get(i) / size;
			Double userVal = 0.0;
			if (userMap != null && userMap.containsKey(i)) {
				userVal = userMap.get(i);//Math.exp(userMap.get(i)) / userDenomVal;
			}
			Double resVal = 0.0;
			if (resMap != null && resMap.containsKey(i)) {
				resVal = resMap.get(i);//Math.exp(resMap.get(i)) / resDenomVal;
			}
			if (userVal > 0.0 || resVal > 0.0) { // TODO
				if (smoothing) {
					resultMap.put(i, Utilities.getSmoothedTagValue(userVal, userDenomVal, resVal, resDenomVal, pt));
				} else {
					resultMap.put(i, userVal + resVal);
				}
			}
		}
		
		if (sorting) {
			Map<Integer, Double> sortedResultMap = new TreeMap<Integer, Double>(new DoubleMapComparator(resultMap));
			sortedResultMap.putAll(resultMap);
			if (topicCreation) {
				return sortedResultMap;
			} else { // otherwise filter
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
		}
		return resultMap;
		*/
	}
	
	private static String timeString;
	
	public static List<Map<Integer, Double>> startLdaCreation(BookmarkReader reader, int sampleSize, boolean sorting, int numTopics, boolean userBased, boolean resBased, boolean topicCreation, boolean smoothing) {
		timeString = "";
		int size = reader.getBookmarks().size();
		int trainSize = size - sampleSize;
		
		Stopwatch timer = new Stopwatch();
		timer.start();
		MalletCalculator userCalc = null;
		List<Map<Integer, Integer>> userMaps = null;
		//List<Double> userDenoms = null;
		if (userBased) {
			userMaps = Utilities.getUserMaps(reader.getBookmarks().subList(0, trainSize));
			userCalc = new MalletCalculator(userMaps, numTopics);
			userCalc.predictValuesProbs();
			//userDenoms = getDenoms(userPredictionValues);
			System.out.println("User-Training finished");
		}
		MalletCalculator resCalc = null;
		List<Map<Integer, Integer>> resMaps = null;
		//List<Double> resDenoms = null;
		if (resBased) {
			resMaps = Utilities.getResMaps(reader.getBookmarks().subList(0, trainSize));
			resCalc = new MalletCalculator(resMaps, numTopics);
			resCalc.predictValuesProbs();
			//resDenoms = getDenoms(resPredictionValues);
			System.out.println("Res-Training finished");
		}
		List<Map<Integer, Double>> results = new ArrayList<Map<Integer, Double>>();
		if (trainSize == size) {
			trainSize = 0;
		}
        timer.stop();
        long trainingTime = timer.elapsed(TimeUnit.MILLISECONDS);
        
		timer = new Stopwatch();
		timer.start();
		for (int i = trainSize; i < size; i++) { // the test set
			Bookmark data = reader.getBookmarks().get(i);
			int userID = data.getUserID();
			int resID = data.getWikiID();
			//Map<Integer, Integer> userMap = null;
			//if (userBased && userMaps != null && userID < userMaps.size()) {
			//	userMap = userMaps.get(userID);
			//}
			//Map<Integer, Integer> resMap = null;
			//if (resBased && resMaps != null && resID < resMaps.size()) {
			//	resMap = resMaps.get(resID);
			//}
			double userTagCount = 0.0;//Utilities.getMapCount(userMap);
			double resTagCount = 0.0;//Utilities.getMapCount(resMap);
			/*
			double userDenomVal = 0.0;
			if (userDenoms != null && userID < userDenoms.size()) {
				userDenomVal = userDenoms.get(userID);
			}
			double resDenomVal = 0.0;
			if (resDenoms != null && resID < resDenoms.size()) {
				resDenomVal = resDenoms.get(resID);
			}
			*/
			Map<Integer, Double> userPredMap = null;
			if (userCalc != null) {
				userPredMap = userCalc.getValueProbsForID(userID, topicCreation);
			}
			Map<Integer, Double> resPredMap = null;
			if (resCalc != null) {
				resPredMap = resCalc.getValueProbsForID(resID, topicCreation);
			}
			Map<Integer, Double> map = getRankedTagList(reader, userPredMap, userTagCount, resPredMap, resTagCount, sorting, smoothing, topicCreation);
			results.add(map);
		}
		timer.stop();
		long testTime = timer.elapsed(TimeUnit.MILLISECONDS);
		timeString += ("Full training time: " + trainingTime + "\n");
		timeString += ("Full test time: " + testTime + "\n");
		timeString += ("Average test time: " + testTime / (double)sampleSize) + "\n";
		timeString += ("Total time: " + (trainingTime + testTime) + "\n");
		return results;
	}
    
	
	public static void predictSample(String filename, int trainSize, int sampleSize, int numTopics, boolean userBased, boolean resBased) {
		BookmarkReader reader = new BookmarkReader(trainSize, false);
		reader.readFile(filename);

		List<Map<Integer, Double>> ldaValues = startLdaCreation(reader, sampleSize, true, numTopics, userBased, resBased, false, true);
		
		List<int[]> predictionValues = new ArrayList<int[]>();
		for (int i = 0; i < ldaValues.size(); i++) {
			Map<Integer, Double> ldaVal = ldaValues.get(i);
			predictionValues.add(Ints.toArray(ldaVal.keySet()));
		}
		reader.setUserLines(reader.getBookmarks().subList(trainSize, reader.getBookmarks().size()));
		PredictionFileWriter writer = new PredictionFileWriter(reader, predictionValues);
		writer.writeFile(filename + "_lda_" + numTopics);
		
		Utilities.writeStringToFile("./data/metrics/" + filename + "_lda_" + numTopics + "_TIME.txt", timeString);
	}
	
	public static void createSample(String filename, int sampleSize, short numTopics, boolean userBased, boolean resBased) {
		String outputFile = new String(filename) + "_lda_" + numTopics;

		//filename += "_res";
		//outputFile += "_res";

		BookmarkReader reader = new BookmarkReader(0, false);
		reader.readFile(filename);

		int trainSize = reader.getBookmarks().size() - sampleSize;	
		List<Map<Integer, Double>> ldaValues = startLdaCreation(reader, 0, true, numTopics, userBased, resBased, true, true);
		
		List<int[]> predictionValues = new ArrayList<int[]>();
		// TODO: make argument for the probValues
		List<double[]> probValues = new ArrayList<double[]>();
		for (int i = 0; i < ldaValues.size(); i++) {
			Map<Integer, Double> ldaVal = ldaValues.get(i);
			predictionValues.add(Ints.toArray(ldaVal.keySet()));
			probValues.add(Doubles.toArray(ldaVal.values()));
			/*
			int[] values = new int[MAX_RECOMMENDATIONS];
			int j = 0;
			for (Integer val : ldaVal.keySet()) {
				if (j < MAX_RECOMMENDATIONS) {
					values[j++] = val;
				} else {
					break;
				}
			}
			predictionValues.add(values);
			*/
		}

		List<Bookmark> trainUserSample = reader.getBookmarks().subList(0, trainSize);
		List<Bookmark> testUserSample = reader.getBookmarks().subList(trainSize, trainSize + sampleSize);
		List<Bookmark> userSample = reader.getBookmarks().subList(0, trainSize + sampleSize);		
		BookmarkSplitter.writeWikiSample(reader, trainUserSample, outputFile + "_train", predictionValues);
		BookmarkSplitter.writeWikiSample(reader, testUserSample, outputFile + "_test", predictionValues);
		BookmarkSplitter.writeWikiSample(reader, userSample, outputFile, predictionValues);
	}
}
