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

package engine;

import processing.BLLCalculator;
import common.CooccurenceMatrix;
import common.DoubleMapComparator;
import common.Utilities;
import file.BookmarkReader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class BaseLevelLearningEngine implements EngineInterface {

	private BookmarkReader reader;
	private final Map<String, Map<Integer, Double>> userMaps;
	private final Map<String, Map<Integer, Double>> userCounts;
	private final Map<String, Map<Integer, Double>> resMaps;
	private final Map<String, Map<Integer, Double>> resCounts;
	private final Map<Integer, Double> topTags;
	private CooccurenceMatrix rMatrix;

	public BaseLevelLearningEngine() {
		userMaps = new HashMap<>();
		userCounts = new HashMap<>();
		resMaps = new HashMap<>();
		resCounts = new HashMap<>();
		topTags = new LinkedHashMap<>();
		
		reader = null;
	}

	public void loadFile(String path, String filename) throws Exception {
		BookmarkReader reader = EngineUtils.getSortedBookmarkReader(path, filename);

		Map<String, Map<Integer, Double>> userMaps = new HashMap<>();
		Map<String, Map<Integer, Double>> userCounts = new HashMap<>();
		Map<String, Map<Integer, Double>> resMaps = new HashMap<>();
		Map<String, Map<Integer, Double>> resCounts = new HashMap<>();

		List<Map<Integer, Double>> userRecencies = BLLCalculator.getArtifactMaps(reader, reader.getBookmarks(), null, false, 
				new ArrayList<Long>(), new ArrayList<Double>(), 0.5, true, null, true);
		List<Map<Integer, Double>> userFrequencies = Utilities.getRelativeTagMaps(reader.getBookmarks(), false);
		int i = 0;
		for (Map<Integer, Double> map : userRecencies) {
			userMaps.put(reader.getUsers().get(i++), map);
		}
		i = 0;
		for (Map<Integer, Double> map : userFrequencies) {
			userCounts.put(reader.getUsers().get(i++), map);
		}
		List<Map<Integer, Double>> resRecencies = BLLCalculator.getArtifactMaps(reader, reader.getBookmarks(), null, true, new ArrayList<Long>(), 
				new ArrayList<Double>(), 0.0, true, null, true);
		List<Map<Integer, Double>> resFrequencies = Utilities.getRelativeTagMaps(reader.getBookmarks(), true);
		i = 0;
		for (Map<Integer, Double> map : resRecencies) {
			resMaps.put(reader.getResources().get(i++), map);
		}
		i = 0;
		for (Map<Integer, Double> map : resFrequencies) {
			resCounts.put(reader.getResources().get(i++), map);
		}
		Map<Integer, Double> topTags = EngineUtils.calcTopEntities(reader, EntityType.TAG);
		
		//System.out.println("calculate associative component for BLLac");
		CooccurenceMatrix matrix = new CooccurenceMatrix(reader.getBookmarks(), reader.getTagCounts(), true);
		
		resetStructures(userMaps, resMaps, reader, topTags, matrix, userCounts, resCounts);
	}
	
	public synchronized Map<String, Double> getEntitiesWithLikelihood(String user, String resource, List<String> topics, Integer count, Boolean filterOwnEntities, Algorithm algorithm, EntityType type) {
		if (count == null || count.doubleValue() < 1) {
			count = 10;
		}
		Map<Integer, Double> userMap = this.userMaps.get(user);
		Map<Integer, Double> userCountMap = this.userCounts.get(user);
		if (filterOwnEntities == null) {
			filterOwnEntities = true;
		}
		List<Integer> filterTags = EngineUtils.getFilterTags(filterOwnEntities, this.reader, user, resource);
		
		// get personalized tag recommendations
		Map<Integer, Double> resultMap = new LinkedHashMap<Integer, Double>();
		if (algorithm == null || algorithm != Algorithm.MP) {
			Map<Integer, Double> resMap = this.resMaps.get(resource);
			Map<Integer, Double> resCountMap = this.resCounts.get(resource);
			// user-based and resource-based
			if (userMap != null) {
				for (Map.Entry<Integer, Double> entry : userMap.entrySet()) {
					if (!filterTags.contains(entry.getKey())) {
						resultMap.put(entry.getKey(), entry.getValue().doubleValue());
					}
				}
				if (resCountMap != null && (algorithm == null || algorithm == Algorithm.BLLac || algorithm == Algorithm.BLLacMPr)) {
					Map<Integer, Double> associativeValues = this.rMatrix.calculateAssociativeComponentsWithTagAssosiation(userCountMap, resCountMap, false, true, false);
					for (Map.Entry<Integer, Double> entry : associativeValues.entrySet()) {
						Double val = resultMap.get(entry.getKey());
						if (!filterTags.contains(entry.getKey())) {
							resultMap.put(entry.getKey(), val == null ? entry.getValue().doubleValue() : val.doubleValue() + entry.getValue().doubleValue());
						}
					}
					double denom = 0.0;
					for (Map.Entry<Integer, Double> entry : resultMap.entrySet()) {
						double val = Math.log(entry.getValue());
						denom += Math.exp(val);
					}
					for (Map.Entry<Integer, Double> entry : resultMap.entrySet()) {
						entry.setValue(Math.exp(Math.log(entry.getValue())) / denom);
					}
				}
			}
			
			if ((algorithm == null || algorithm == Algorithm.BLLacMPr) && resMap != null) {
				for (Map.Entry<Integer, Double> entry : resMap.entrySet()) {
					if (!filterTags.contains(entry.getKey())) {
						double resVal = entry.getValue().doubleValue();
						Double val = resultMap.get(entry.getKey());
						resultMap.put(entry.getKey(), val == null ? resVal : val.doubleValue() + resVal);
					}
				}
			}
		}
		
		// add MP tags if necessary
		if (resultMap.size() < count) {
			for (Map.Entry<Integer, Double> t : this.topTags.entrySet()) {
				if (resultMap.size() < count) {
					if (!resultMap.containsKey(t.getKey()) && !filterTags.contains(t.getKey())) {
						resultMap.put(t.getKey(), t.getValue());
					}
				} else {
					break;
				}
			}
		}

		// sort
		Map<Integer, Double> sortedResultMap = new TreeMap<Integer, Double>(new DoubleMapComparator(resultMap));
		sortedResultMap.putAll(resultMap);
		// map tag-ids back to strings
		Map<String, Double> tagMap = new LinkedHashMap<>();
		int i = 0;
		for (Map.Entry<Integer, Double> entry : sortedResultMap.entrySet()) {
			if (i++ < count) {
				tagMap.put(this.reader.getTags().get(entry.getKey()), (double) entry.getValue());
			} else {
				break;
			}
		}
		return tagMap;
	}

	private synchronized void resetStructures(Map<String, Map<Integer, Double>> userMaps, Map<String, Map<Integer, Double>> resMaps, BookmarkReader reader, Map<Integer, Double> topTags, CooccurenceMatrix matrix, Map<String, Map<Integer, Double>> userCounts, Map<String, Map<Integer, Double>> resCounts) {
		this.reader = reader;

		this.userMaps.clear();
		this.userMaps.putAll(userMaps);
		this.userCounts.clear();
		this.userCounts.putAll(userCounts);

		this.resMaps.clear();
		this.resMaps.putAll(resMaps);
		this.resCounts.clear();
		this.resCounts.putAll(resCounts);

		this.topTags.clear();
		this.topTags.putAll(topTags);
		
		this.rMatrix = matrix;
	}
}
