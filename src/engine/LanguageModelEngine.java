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

import common.DoubleMapComparator;
import common.Utilities;
import file.BookmarkReader;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class LanguageModelEngine implements EngineInterface {

	private BookmarkReader reader;
	private final Map<String, Map<Integer, Double>> userMaps;
	private final Map<String, Map<Integer, Double>> resMaps;
	private final Map<Integer, Double> topTags;

	public LanguageModelEngine() {
		this.userMaps = new HashMap<>();
		this.resMaps = new HashMap<>();
		topTags = new LinkedHashMap<>();

		reader = null;
	}

	public void loadFile(String filename) throws Exception {
		BookmarkReader reader = EngineUtils.getSortedBookmarkReader(filename);
		
		Map<String, Map<Integer, Double>> userMaps = new HashMap<>();
		Map<String, Map<Integer, Double>> resMaps = new HashMap<>();

		List<Map<Integer, Double>> userStats = Utilities.getNormalizedMaps(reader.getBookmarks(), false);
		int i = 0;
		for (Map<Integer, Double> map : userStats) {
			//Map<Integer, Double> resultMap = new HashMap<>();
			//for (Map.Entry<Integer, Integer> entry : map.entrySet()) {
			//	resultMap.put(entry.getKey(), (double) entry.getValue() / (double) Utilities.getMapCount(map));
			//}
			userMaps.put(reader.getUsers().get(i++), map);
		}
		List<Map<Integer, Double>> resStats = Utilities.getNormalizedMaps(reader.getBookmarks(), true);
		i = 0;
		for (Map<Integer, Double> map : resStats) {
			//Map<Integer, Double> resultMap = new HashMap<>();
			//for (Map.Entry<Integer, Integer> entry : map.entrySet()) {
			//	resultMap.put(entry.getKey(), (double) entry.getValue() / (double) Utilities.getMapCount(map));
			//}
			resMaps.put(reader.getResources().get(i++), map);
		}

		Map<Integer, Double> topTags = EngineUtils.calcTopEntities(reader, EntityType.TAG);
		resetStructures(userMaps, resMaps, reader, topTags);
	}

	public synchronized Map<String, Double> getEntitiesWithLikelihood(String user, String resource, List<String> topics, Integer count, Boolean filterOwnEntities, Algorithm algorithm, EntityType type) {
		if (count == null || count.doubleValue() < 1) {
			count = 10;
		}
		Map<Integer, Double> userMap = this.userMaps.get(user);
		if (filterOwnEntities == null) {
			filterOwnEntities = true;
		}
		List<Integer> filterTags = EngineUtils.getFilterTags(filterOwnEntities, this.reader, user, resource, userMap);
		
		Map<Integer, Double> resultMap = new LinkedHashMap<Integer, Double>();
		if (algorithm == null || algorithm != Algorithm.MP) {
			Map<Integer, Double> resMap = this.resMaps.get(resource);
			if ((algorithm == null || algorithm == Algorithm.MPu || algorithm == Algorithm.MPur) && userMap != null) {
				for (Map.Entry<Integer, Double> entry : userMap.entrySet()) {
					if (!filterTags.contains(entry.getKey())) {
						resultMap.put(entry.getKey(), entry.getValue().doubleValue());
					}
				}
			}
			if ((algorithm == null || algorithm == Algorithm.MPr || algorithm == Algorithm.MPur) && resMap != null) {
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
		
		// map tag-IDs back to strings
		int i = 0;
		Map<String, Double> tagMap = new LinkedHashMap<>();
		for (Map.Entry<Integer, Double> entry : sortedResultMap.entrySet()) {
			if (i++ < count) {
				tagMap.put(this.reader.getTags().get(entry.getKey()), (double) entry.getValue());
			} else {
				break;
			}
		}
		return tagMap;
	}

	private synchronized void resetStructures(Map<String, Map<Integer, Double>> userMaps, Map<String, Map<Integer, Double>> resMaps, BookmarkReader reader, Map<Integer, Double> topTags) {

		this.reader = reader;
		
		this.userMaps.clear();
		this.userMaps.putAll(userMaps);

		this.resMaps.clear();
		this.resMaps.putAll(resMaps);

		this.topTags.clear();
		this.topTags.putAll(topTags);
	}
}
