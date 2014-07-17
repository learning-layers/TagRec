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

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

import common.IntMapComparator;

import file.BookmarkReader;

public class EngineUtils {

	public static Map<String, Double> calcTopTags(BookmarkReader reader) {
		Map<String, Double> tagMap = new LinkedHashMap<>();
		Map<Integer, Integer> countMap = new LinkedHashMap<Integer, Integer>();

		Integer countSum = 0;

		for (int i = 0; i < reader.getTagCounts().size(); i++) {
			countMap.put(i, reader.getTagCounts().get(i));

			countSum += reader.getTagCounts().get(i);
		}

		Map<Integer, Integer> sortedCountMap = new TreeMap<Integer, Integer>(
				new IntMapComparator(countMap));
		sortedCountMap.putAll(countMap);

		for (Map.Entry<Integer, Integer> entry : sortedCountMap.entrySet()) {
			tagMap.put(reader.getTags().get(entry.getKey()),
					((double) entry.getValue()) / countSum);
		}

		return tagMap;
	}
}
