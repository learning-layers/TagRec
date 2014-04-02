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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import com.google.common.base.Stopwatch;
import com.google.common.primitives.Ints;

import common.DoubleMapComparator;
import common.UserData;
import common.Utilities;

import file.PredictionFileWriter;
import file.BookmarkReader;

public class LayersCalculator {

	private BookmarkReader reader;
	private List<UserData> trainList;
	private List<List<UserData>> userBookmarks;
	private List<List<UserData>> resBookmarks;
	private double beta;
	
	private List<Map<Integer, Double>> categoryCombs;
	
	private List<Map<Integer, Double>> userMaps;
	private List<Map<Integer, Double>> resMaps;
	
	public LayersCalculator(BookmarkReader reader, int trainSize, int beta) {
		this.reader = reader;
		this.trainList = this.reader.getUserLines().subList(0, trainSize);
		List<UserData> testLines = this.reader.getUserLines().subList(trainSize, this.reader.getUserLines().size());
		this.userBookmarks = Utilities.getBookmarks(this.trainList, false);
		this.resBookmarks = Utilities.getBookmarks(this.trainList, true);
		this.beta = (double)beta / 10.0;
		
		//this.userMaps = Utilities.getRelativeMaps(this.trainList, false);
		this.userMaps = ActCalculator.getArtifactMaps(this.trainList, testLines, false, new ArrayList<Long>(), new ArrayList<Double>(), 0.2, true);
		//this.resMaps = Utilities.getRelativeMaps(this.trainList, true);
		this.resMaps = ActCalculator.getArtifactMaps(this.trainList, testLines, true, new ArrayList<Long>(), new ArrayList<Double>(), 0.2, true);
		
		this.categoryCombs = new ArrayList<Map<Integer, Double>>();
		for (UserData data : this.trainList) {
			this.categoryCombs.add(Utilities.getRelativeMapFromList(data.getCategories()));
		}
	}
	
	public Map<Integer, Double> getRankedTagList(int userID, int resID, List<Integer> testCats) {
		List<Map<Integer, Double>> categories = new ArrayList<Map<Integer, Double>>();
		List<Map<Integer, Double>> tags = new ArrayList<Map<Integer, Double>>();
		List<UserData> bookmarks = new ArrayList<UserData>();
		Map<Integer, Double> testCatsMap = Utilities.getRelativeMapFromList(testCats);
		
		List<UserData> userB = null;
		if (userID < this.userBookmarks.size()) {
			userB = this.userBookmarks.get(userID);
			bookmarks.addAll(userB);
		} else {
			userB = new ArrayList<UserData>();
		}
		List<UserData> resB = null;
		if (resID < this.resBookmarks.size()) {
			resB = this.resBookmarks.get(resID);
			bookmarks.addAll(resB);
		} else {
			resB = new ArrayList<UserData>();
		}
		// get examples
		for (int i = 0; i < bookmarks.size(); i++) {
			UserData data = bookmarks.get(i);
			// TODO: performance!
			Map<Integer, Double> catMap = Utilities.getRelativeMapFromList(data.getCategories());
			categories.add(catMap);
			tags.add(Utilities.getRelativeMapFromList(getTagsForCategoryMap(catMap)));		
		}
		
		Map<Integer, Double> userResultMap = null;
		if (userID < this.userMaps.size()) {
			userResultMap = getResultMap(categories, tags, this.userMaps.get(userID), testCatsMap, 0, userB.size());
		}
		Map<Integer, Double> resResultMap = null;
		if (resID < this.resMaps.size()) {
			resResultMap = getResultMap(categories, tags, this.resMaps.get(resID), testCatsMap, userB.size() + 1, bookmarks.size());
		}
		Map<Integer, Double> resultMap = new LinkedHashMap<Integer, Double>();
		// TODO: merge results
		for (int i = 0; i < this.reader.getTags().size(); i++) {
			Double userVal = 0.0;
			if (userResultMap != null && userResultMap.containsKey(i)) {
				userVal = userResultMap.get(i);
			}
			Double resVal = 0.0;
			if (resResultMap != null && resResultMap.containsKey(i)) {
				resVal = resResultMap.get(i);
			}
			resultMap.put(i, this.beta * userVal.doubleValue() + (1.0 - this.beta) * resVal.doubleValue());
		}
		
		Map<Integer, Double> returnMap = new LinkedHashMap<Integer, Double>();
		Map<Integer, Double> sortedResultMap = new TreeMap<Integer, Double>(new DoubleMapComparator(resultMap));
		sortedResultMap.putAll(resultMap);
		int count = 0;
		for (Map.Entry<Integer, Double> entry : sortedResultMap.entrySet()) {
			if (count++ < 10) {
				returnMap.put(entry.getKey(), entry.getValue());
			} else {
				break;
			}
		}
		
		//System.out.println(returnMap.values().toString());
		return returnMap;
	}

	private Map<Integer, Double> getResultMap(List<Map<Integer, Double>> categories, List<Map<Integer, Double>> tags, Map<Integer, Double> countMap, Map<Integer, Double> testCatsMap, int start, int end) {
		Map<Integer, Double> resultMap = new LinkedHashMap<Integer, Double>();
		for (int i = start; i < end; i++) {
			Map<Integer, Double> catsMap = categories.get(i);
			Double sim = 1.0 - Utilities.getCosineFloatSim(catsMap, testCatsMap);
			Double ajhid = Math.exp(sim.doubleValue() * (-1.0));
			if (ajhid.isNaN() || ajhid.isInfinite()) {
				ajhid = 0.0;
			}
			Map<Integer, Double> tagsMap = tags.get(i);		
			for (Map.Entry<Integer, Double> entry : tagsMap.entrySet()) {
				Double akout = ajhid.doubleValue() * entry.getValue().doubleValue();
				Double value = resultMap.get(entry.getKey());
				resultMap.put(entry.getKey(), value == null ? akout.doubleValue() : value.doubleValue() + akout.doubleValue());
			}
		}
		
		// normalize values
		double denom = 0.0;
		for (double val : resultMap.values()) {
			denom += Math.exp(val);
		}
		for (Map.Entry<Integer, Double> entry : resultMap.entrySet()) {
			// add BLL value to existing entries
			Double count = 0.0;
			if (countMap != null) {
				count = countMap.get(entry.getKey());
			}
			entry.setValue(Math.exp(entry.getValue()) / denom  * 0.1 + (count != null ? count.doubleValue() : 0.0) * 0.9);
		}
		// add new BLL values
		for (Map.Entry<Integer, Double> entry : countMap.entrySet()) {
			if (!resultMap.containsKey(entry.getKey())) {
				resultMap.put(entry.getKey(), entry.getValue() * 0.9);
			}
		}
		
		return resultMap;
	}
	
	private List<Integer> getTagsForCategoryMap(Map<Integer, Double> cats) {
		List<Integer> tags = new ArrayList<Integer>();
		int i = 0;
		for (Map<Integer, Double> catComb : this.categoryCombs) {
			//if (cats.size() == catComb.size() && cats.entrySet().containsAll(catComb.entrySet()) && cats.values().containsAll(catComb.values())) {
			if (isSameCategoryComb(cats, catComb)) {
				tags.addAll(this.trainList.get(i).getTags());
			}
			i++;
		}
		return tags;
	}
	
	private boolean isSameCategoryComb(Map<Integer, Double> targetCats, Map<Integer, Double> cats) {
		if (targetCats.size() != cats.size()) {
			return false;
		} else {
			for (Map.Entry<Integer, Double> targetEntry : targetCats.entrySet()) {
				Double val = cats.get(targetEntry.getKey());
				if (val == null || val.doubleValue() != targetEntry.getValue().doubleValue()) {
					return false;
				}
			}
		}
		return true;
	}
	
	// Statics -----------------------------------------------------------------------------------------------------------------------
	
	
	
	private static String timeString = "";
	
	public static void predictSample(String filename, int trainSize, int sampleSize, int beta) {
		//filename += "_res";
		BookmarkReader reader = new BookmarkReader(trainSize, false);
		reader.readFile(filename);
		
		List<int[]> predictionValues = new ArrayList<int[]>();
		Stopwatch timer = new Stopwatch();
		timer.start();
		LayersCalculator calculator = new LayersCalculator(reader, trainSize, beta);
		timer.stop();
		long trainingTime = timer.elapsed(TimeUnit.MILLISECONDS);
		
		timer = new Stopwatch();
		timer.start();
		for (int i = trainSize; i < trainSize + sampleSize; i++) { // the test-set
			UserData data = reader.getUserLines().get(i);
			Map<Integer, Double> map = calculator.getRankedTagList(data.getUserID(), data.getWikiID(), data.getCategories());
			predictionValues.add(Ints.toArray(map.keySet()));
		}
		timer.stop();
		long testTime = timer.elapsed(TimeUnit.MILLISECONDS);
		timeString += ("Full training time: " + trainingTime + "\n");
		timeString += ("Full test time: " + testTime + "\n");
		timeString += ("Average test time: " + testTime / (double)sampleSize) + "\n";
		timeString += ("Total time: " + (trainingTime + testTime) + "\n");
		String outputFile = filename + "_3layers";
		Utilities.writeStringToFile("./data/metrics/" + outputFile + "_TIME.txt", timeString);
		
		reader.setUserLines(reader.getUserLines().subList(trainSize, reader.getUserLines().size()));
		PredictionFileWriter writer = new PredictionFileWriter(reader, predictionValues);
		writer.writeFile(outputFile);
	}
}
