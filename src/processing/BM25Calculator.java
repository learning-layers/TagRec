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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.common.base.Stopwatch;
import com.google.common.primitives.Ints;

import common.DoubleMapComparator;
import common.Features;
import common.Similarity;
import common.Bookmark;
import common.Utilities;

import file.PredictionFileWriter;
import file.BookmarkReader;

public class BM25Calculator {
	
	public static int MAX_NEIGHBORS = 20;
	private final static double K1 = 1.2;
	private final static double K3 = 1.2;
	private final static double B = 0.8;
	
	private BookmarkReader reader;
	private boolean userBased;
	private boolean resBased;
	private double beta;
	Similarity sim;
	private List<Bookmark> trainList;
	private List<Bookmark> testList;
	private List<Map<Integer, Double>> userMaps;
	private Map<Integer, Double> allUsers;
	private List<Map<Integer, Double>> resMaps;
	private Map<Integer, Double> allResources;
	
	public BM25Calculator(BookmarkReader reader, int trainSize, boolean predictTags, boolean userBased, boolean resBased, int beta, Similarity sim, Features features) {		
		this.reader = reader;
		this.userBased = userBased;
		this.resBased = resBased;
		this.beta = (double)beta / 10.0;
		this.sim = sim;
		//this.trainList = this.reader.getUserLines().subList(0, predictTags ? trainSize : reader.getUserLines().size()); // TODO
		this.trainList = this.reader.getBookmarks().subList(0, trainSize);
		this.testList = this.reader.getBookmarks().subList(trainSize, this.reader.getBookmarks().size());
		if (this.userBased || !predictTags) {
			if (features == Features.ENTITIES) {
				this.userMaps = Utilities.getUsedEntities(this.trainList, false, null);
			} else if (features == Features.TOPICS) {
				this.userMaps = Utilities.getRelativeTopicMaps(this.trainList, false);//Utilities.getUserTopics(this.trainList);
			} else if (features == Features.TAGS) {
				this.userMaps = Utilities.getRelativeTagMaps(this.trainList, false);//Utilities.getUserMaps(this.trainList);
			} else if (features == Features.TAG_ENTITIES) {
				this.userMaps = Utilities.getUsedEntities(this.trainList, false, Utilities.getRelativeTagMaps(this.trainList, false));
			}
			this.allUsers = Utilities.getAllEntities(this.trainList, false);
		}
		if (this.resBased) {
			if (features == Features.ENTITIES) {
				this.resMaps = Utilities.getUsedEntities(this.trainList, true, null);
			} else if (features == Features.TOPICS) {
				this.resMaps = Utilities.getRelativeTopicMaps(this.trainList, true);//Utilities.getResTopics(this.trainList);
			} else if (features == Features.TAGS) {
				this.resMaps = Utilities.getRelativeTagMaps(this.trainList, true);//Utilities.getResMaps(this.trainList);
			} else if (features == Features.TAG_ENTITIES) {
				this.resMaps = Utilities.getUsedEntities(this.trainList, true, Utilities.getRelativeTagMaps(this.trainList, true));
			}
			this.allResources = Utilities.getAllEntities(this.trainList, true);
		}
	}
		
	private Map<Integer, Double> getRankedResourcesList(int userID, boolean sorting, boolean allResources, boolean bll) {
		List<Integer> userResources = null;
		Map<Integer, Double> userBllResources = null;
		if (this.resBased) {
			// ####################################################### TODO: check log! and normalization!
			userBllResources = Bookmark.getResourcesFromUserWithRec(this.trainList, this.testList, userID, 0.5, bll);
			userResources = new ArrayList<Integer>(userBllResources.keySet());
		} else {
			userResources = Bookmark.getResourcesFromUser(this.trainList, userID);
		}
		Map<Integer, Double> rankedResources = new LinkedHashMap<Integer, Double>();
		
		int i = 0;
		double denom = 0.0;
		if (this.userBased) {
			Map<Integer, Double> sortedNeighbors = Utilities.getNeighbors(userID, -1, this.allUsers, this.userMaps, this.trainList, this.sim);
			for (Map.Entry<Integer, Double> neighbor : sortedNeighbors.entrySet()) {		
				if (i++ > MAX_NEIGHBORS) {
					break;
				}
				if (bll) {
					userBllResources = Bookmark.getResourcesFromUserWithRec(this.trainList, this.testList, neighbor.getKey(), 0.5, false);
				}
				double bm25 = neighbor.getValue();
				denom += bm25;
				if (bm25 != 0.0) {
					List<Integer> resources = Bookmark.getResourcesFromUser(this.trainList, neighbor.getKey());				
					for (Integer resID : resources) {
						if (!userResources.contains(resID)) {
							double bllVal = (bll ? userBllResources.get(resID) : 1.0);
							Double val = rankedResources.get(resID);
							double entryVal = bllVal * bm25;
							rankedResources.put(resID, (val != null ? val + entryVal : entryVal));		
							//System.out.println("add resource to list - " + resID + " " + (val != null ? val + bm25 : bm25));
						}
					}
				}
			}
			// ####################################################### TODO: check if necessary
			for (Map.Entry<Integer, Double> entry : rankedResources.entrySet()) {
				entry.setValue(entry.getValue() / denom);
			}
			// Neighbor-weighted CF
			//for (Map.Entry<Integer, Double> entry : rankedResources.entrySet()) {
			//	entry.setValue(Math.log10(1 + this.reader.getResourceCounts().get(entry.getKey())) * entry.getValue());
			//}
		}
		if (this.resBased) {
			denom = 0.0;
			// TODO: Threshold
			Map<Integer, Double> sortedResources = null;
			if (allResources) {
				sortedResources = new LinkedHashMap<Integer, Double>();
				int resCount = 0;
				for (Map.Entry<Integer, Double> res : userBllResources.entrySet()) {
					if (resCount++ > MAX_NEIGHBORS) {
						break;
					}
					int resID = res.getKey();
					i = 0;
					Double bllVal = (bll && userBllResources != null ? res.getValue() : 1.0);
					Map<Integer, Double> resources = Utilities.getSimResources(-1, resID, userResources, this.allResources, this.resMaps, this.trainList, this.sim);
					for (Map.Entry<Integer, Double> entry : resources.entrySet()) {
						if (i++ > MAX_NEIGHBORS) {
							break;
						}
						Double val = sortedResources.get(entry.getKey());
						double entryVal = (bllVal != null ? bllVal.doubleValue() : 1.0) * entry.getValue();
						sortedResources.put(entry.getKey(), val != null ? val.doubleValue() + entryVal : entryVal);
					}
				}
			} else {
				sortedResources = Utilities.getSimResourcesForUser(userID, this.allResources, this.userMaps, this.resMaps, this.trainList, userResources, this.sim);
			}
			for (Map.Entry<Integer, Double> sortedRes : sortedResources.entrySet()) {
				Double val = rankedResources.get(sortedRes.getKey());
				rankedResources.put(sortedRes.getKey(), val != null ? val.doubleValue() + sortedRes.getValue() : sortedRes.getValue());
			}
		}
		
//		denom = 0.0;
//		// normalize
//		for (double val : rankedResources.values()) {
//			denom += Math.exp(val);
//		}
//		for (Map.Entry<Integer, Double> entry : rankedResources.entrySet()) {
//			entry.setValue(Math.exp(entry.getValue()) / denom);
//		}
		if (sorting) {
			// return the sorted resources
			Map<Integer, Double> sortedRankedResources = new TreeMap<Integer, Double>(new DoubleMapComparator(rankedResources));
			sortedRankedResources.putAll(rankedResources);
			return sortedRankedResources;
		} else {
			return rankedResources;
		}
	}
	
	// Tags -------------------------------------------------------------------------------------------------------------------------------------
	// TODO: check results with no-core
	public Map<Integer, Double> getRankedTagList(int userID, int resID, boolean sorting) {
		Map<Integer, Double> resultMap = new LinkedHashMap<Integer, Double>();
		int i = 0;		
		if (this.userBased) {
			Map<Integer, Double> neighbors = Utilities.getNeighbors(userID, resID, this.allUsers, this.userMaps, this.trainList, this.sim);
			for (Map.Entry<Integer, Double> entry : neighbors.entrySet()) {
				if (i++ < MAX_NEIGHBORS && entry.getKey() != userID) {
					//neighborMaps.add(this.userMaps.get(entry.getKey()));
					Bookmark nBookmark = Bookmark.getUserData(this.trainList, entry.getKey(), resID);
					List<Integer> tags = null;
					if (nBookmark != null) {
						tags = nBookmark.getTags();
					} else {
						if (entry.getKey() < this.userMaps.size()) {
							tags = new ArrayList<Integer>(this.userMaps.get(entry.getKey()).keySet());
						} else {
							tags = new ArrayList<Integer>();
						}
					}
					double bm25 = /*this.beta * */entry.getValue();
					//if (bm25 != 0.0) {
						for (int tag : tags) {
							Double val = resultMap.get(tag);
							resultMap.put(tag, (val != null ? val + bm25 : bm25));
						}
					//}
				} else {
					break;
				}
			}		
			// Neighbor-weighted CF
			//for (Map.Entry<Integer, Double> entry : resultMap.entrySet()) {
			//	entry.setValue(Math.log10(1 + (double)getTagFrequency(entry.getKey(), neighborMaps)) * entry.getValue());
			//}
		}
		i = 0;
		if (this.resBased) {
			List<Integer> userResources = new ArrayList<Integer>();
			userResources.add(resID);
			Map<Integer, Double> resources = Utilities.getSimResources(userID, resID, userResources, this.allResources, this.resMaps, this.trainList, this.sim);
			for (Map.Entry<Integer, Double> entry : resources.entrySet()) {
				if (i++ < MAX_NEIGHBORS) {
					List<Integer> tags = Bookmark.getResData(this.trainList, userID, entry.getKey()).getTags();
					double bm25 = /*(1.0 - this.beta) * */entry.getValue();
					//if (bm25 != 0.0) {
						for (int tag : tags) {
							Double val = resultMap.get(tag);
							resultMap.put(tag, (val != null ? val + bm25 : bm25));
						}
					//}
				} else {
					break;
				}
			}	
		}
		
		if (sorting) {
			Map<Integer, Double> sortedResultMap = new TreeMap<Integer, Double>(new DoubleMapComparator(resultMap));
			sortedResultMap.putAll(resultMap);			
			Map<Integer, Double> returnMap = new LinkedHashMap<Integer, Double>(20);
			int index = 0;
			for (Map.Entry<Integer, Double> entry : sortedResultMap.entrySet()) {
				if (index++ < 20) {
					returnMap.put(entry.getKey(), entry.getValue());
				} else {
					break;
				}
			}
			return returnMap;
		}
		return resultMap;
	}
	
	// Statics -----------------------------------------------------------------------------------------------------------------------------------------------------------
	public static BookmarkReader predictResources(String filename, int trainSize, int sampleSize, int neighborSize, boolean userBased, boolean resourceBased, boolean allResources, boolean bll, Features features) {
		MAX_NEIGHBORS = neighborSize;
		return predictSample(filename, trainSize, sampleSize, false, userBased, resourceBased, allResources, 5, bll, features);
	}
	
	private static List<Map<Integer, Double>> startBM25CreationForResourcesPrediction(BookmarkReader reader, int sampleSize, boolean userBased, boolean resBased, boolean allResources, boolean bll, Features features) {
		int size = reader.getBookmarks().size();
		int trainSize = size - sampleSize;
		BM25Calculator calculator = new BM25Calculator(reader, trainSize, false, userBased, resBased, 5, Similarity.COSINE, features);
		
		List<Map<Integer, Double>> results = new ArrayList<Map<Integer, Double>>();
		for (Integer userID : reader.getUniqueUserListFromTestSet(trainSize)) {
			Map<Integer, Double> map = null;
			map = calculator.getRankedResourcesList(userID, true, allResources, bll); // TODO
			results.add(map);
		}
	
		return results;
	}
	
	// Tags -----------------------------------------------------------------------------------------------------------------------------------------------------------------
	public static BookmarkReader predictTags(String filename, int trainSize, int sampleSize, int neighbors, boolean userBased, boolean resBased, int beta) {
		MAX_NEIGHBORS = neighbors;
		return predictSample(filename, trainSize, sampleSize, true, userBased, resBased, false, beta, false, Features.TAGS);
	}
	
	public static BookmarkReader predictSample(String filename, int trainSize, int sampleSize, boolean predictTags, boolean userBased, boolean resBased, boolean allResources, int beta, boolean bll, Features features) {
		//filename += "_res";
		
		//int size = 0;
		//if (predictTags) {
		//	size = trainSize;
		//}
		BookmarkReader reader = new BookmarkReader(trainSize, false); // TODO
		reader.readFile(filename);
		
		List<Map<Integer, Double>> cfValues = null;	
		if (predictTags) {
			cfValues = startBM25CreationForTagPrediction(reader, sampleSize, userBased, resBased, beta);
		} else {
			cfValues = startBM25CreationForResourcesPrediction(reader, sampleSize, userBased, resBased, allResources, bll, features);
		}
		
		List<int[]> predictionValues = new ArrayList<int[]>();
		for (int i = 0; i < cfValues.size(); i++) {
			Map<Integer, Double> modelVal = cfValues.get(i);
			predictionValues.add(Ints.toArray(modelVal.keySet()));
		}		
		String suffix = "_cf_";
		if (!userBased) {
			suffix = "_rescf_";
		} else if (!resBased) {
			suffix = "_usercf_";
		}
		suffix += features + "_"; 
		if (predictTags) {
			reader.setUserLines(reader.getBookmarks().subList(trainSize, reader.getBookmarks().size()));
			PredictionFileWriter writer = new PredictionFileWriter(reader, predictionValues);
			String outputFile = filename + suffix + beta;
			writer.writeFile(outputFile);
			
			Utilities.writeStringToFile("./data/metrics/" + outputFile + "_TIME.txt", timeString);
		} else {
			if (!userBased && !allResources) {
				suffix += "mixed_";
			}
			if (bll) {
				suffix += "bll_";
			}
			PredictionFileWriter writer = new PredictionFileWriter(reader, predictionValues);
			writer.writeResourcePredictionsToFile(filename + suffix + beta, trainSize, MAX_NEIGHBORS);
		}
		return reader;
	}
	
	private static String timeString;
	
	private static List<Map<Integer, Double>> startBM25CreationForTagPrediction(BookmarkReader reader, int sampleSize, boolean userBased, boolean resBased, int beta) {
		timeString = "";
		int size = reader.getBookmarks().size();
		int trainSize = size - sampleSize;
		Stopwatch timer = new Stopwatch();
		timer.start();
		BM25Calculator calculator = new BM25Calculator(reader, trainSize, true, userBased, resBased, beta, Similarity.JACCARD, Features.TAGS);
		timer.stop();
		long trainingTime = timer.elapsed(TimeUnit.MILLISECONDS);
		
		List<Map<Integer, Double>> results = new ArrayList<Map<Integer, Double>>();
		timer = new Stopwatch();
		timer.start();
		for (int i = trainSize; i < size; i++) {
			Bookmark data = reader.getBookmarks().get(i);
			Map<Integer, Double> map = null;
			map = calculator.getRankedTagList(data.getUserID(), data.getWikiID(), true);
			results.add(map);
			System.out.println(data.getTags() + "|" + map.keySet());
		}
		timer.stop();
		long testTime = timer.elapsed(TimeUnit.MILLISECONDS);
		timeString += ("Full training time: " + trainingTime + "\n");
		timeString += ("Full test time: " + testTime + "\n");
		timeString += ("Average test time: " + testTime / (double)sampleSize) + "\n";
		timeString += ("Total time: " + (trainingTime + testTime) + "\n");
	
		return results;
	}	
}
