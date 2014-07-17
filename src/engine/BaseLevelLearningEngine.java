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

import processing.ActCalculator;
import common.DoubleMapComparator;
import file.BookmarkReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class BaseLevelLearningEngine implements EngineInterface {

	private BookmarkReader reader;
	private final Map<String, Map<Integer, Double>> userMaps;
	private final Map<String, Map<Integer, Double>> resMaps;
	private final Map<String, Double> topTags;

	public BaseLevelLearningEngine() {

		userMaps = new HashMap<>();
		resMaps = new HashMap<>();
		topTags = new LinkedHashMap<>();

		reader = new BookmarkReader(0, false);
	}

	public void loadFile(String filename) throws Exception {
		Map<String, Map<Integer, Double>> userMaps = new HashMap<>();
		Map<String, Map<Integer, Double>> resMaps = new HashMap<>();
		BookmarkReader reader = new BookmarkReader(0, false);

		reader.readFile(filename);
		Collections.sort(reader.getBookmarks());
		System.out.println("read in and sorted file");
		List<Map<Integer, Double>> userRecencies = ActCalculator
				.getArtifactMaps(reader, reader.getBookmarks(), null, false,
						new ArrayList<Long>(), new ArrayList<Double>(), 0.5,
						true);
		int i = 0;
		for (Map<Integer, Double> map : userRecencies) {
			userMaps.put(reader.getUsers().get(i++), map);
		}
		List<Map<Integer, Double>> resRecencies = ActCalculator
				.getArtifactMaps(reader, reader.getBookmarks(), null, true,
						new ArrayList<Long>(), new ArrayList<Double>(), 0.0,
						true);
		i = 0;
		for (Map<Integer, Double> map : resRecencies) {
			resMaps.put(reader.getResources().get(i++), map);
		}

		resetStructures(userMaps, resMaps, reader);
	}

	public synchronized Map<String, Double> getTagsWithLikelihood(String user, String resource, List<String> topics, Integer count) {
		if (count == null || count.doubleValue() < 1) {
			count = 10;
		}
		Map<Integer, Double> resultMap = new LinkedHashMap<Integer, Double>();
		Map<Integer, Double> userMap = this.userMaps.get(user);
		Map<Integer, Double> resMap = this.resMaps.get(resource);
		// user-based and resource-based
		if (userMap != null) {
			for (Map.Entry<Integer, Double> entry : userMap.entrySet()) {
				resultMap.put(entry.getKey(), entry.getValue().doubleValue());
			}
		}
		if (resMap != null) {
			for (Map.Entry<Integer, Double> entry : resMap.entrySet()) {
				double resVal = entry.getValue().doubleValue();
				Double val = resultMap.get(entry.getKey());
				resultMap.put(entry.getKey(),
						val == null ? resVal : val.doubleValue() + resVal);
			}
		}
		// sort and add MP tags if necessary
		Map<Integer, Double> sortedResultMap = new TreeMap<Integer, Double>(
				new DoubleMapComparator(resultMap));

		sortedResultMap.putAll(resultMap);
		int i = 0;
		Map<String, Double> tagMap = new LinkedHashMap<>();
		for (Map.Entry<Integer, Double> entry : sortedResultMap.entrySet()) {

			if (i++ < count) {
				tagMap.put(this.reader.getTags().get(entry.getKey()),
						(double) entry.getValue());
			} else {
				break;
			}
		}

		if (tagMap.size() < count) {
			for (Map.Entry<String, Double> t : this.topTags.entrySet()) {
				if (tagMap.size() < count) {
					if (!tagMap.containsKey(t.getKey())) {
						tagMap.put(t.getKey(), t.getValue());
					}
				} else {
					break;
				}
			}
		}

		return tagMap;
	}

	private synchronized void resetStructures(
			Map<String, Map<Integer, Double>> userMaps,
			Map<String, Map<Integer, Double>> resMaps, BookmarkReader reader) {

		this.reader = reader;

		this.userMaps.clear();
		this.userMaps.putAll(userMaps);

		this.resMaps.clear();
		this.resMaps.putAll(resMaps);

		this.topTags.clear();
		this.topTags.putAll(EngineUtils.calcTopTags(this.reader));
	}
}
