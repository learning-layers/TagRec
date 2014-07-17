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

import processing.ThreeLayersCalculator;
import file.BookmarkReader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ThreeLayersEngine implements EngineInterface {

	private BookmarkReader reader = null;
	private ThreeLayersCalculator calculator = null;
	private final Map<String, Double> topTags;

	public ThreeLayersEngine() {
		topTags = new LinkedHashMap<>();
		
		reader = new BookmarkReader(0, false);
	}
	
	public void loadFile(String filename) throws Exception {

		BookmarkReader reader = new BookmarkReader(0, false);

		// CHANGED dtheiler: throwing exception; re-implemented if exception
		// handling is necessary here
		reader.readFile(filename);

		Collections.sort(reader.getBookmarks());
		System.out.println("read in and sorted file");

		ThreeLayersCalculator calculator = new ThreeLayersCalculator(reader, reader
				.getBookmarks().size(), 5, 5, true, true, false);

		resetStructure(reader, calculator);
	}

	public synchronized Map<String, Double> getTagsWithLikelihood(String user, String resource, List<String> topics, Integer count) {
		if (count == null || count.doubleValue() < 1) {
			count = 10;
		}
		Map<String, Double> tagMap = new LinkedHashMap<>();
		if (this.reader == null || this.calculator == null) {
			return tagMap;
		}
		int userID = this.reader.getUsers().indexOf(user);
		int resID = this.reader.getResources().indexOf(resource);
		List<Integer> topicIDs = new ArrayList<>();
		if (topics != null) {
			for (String t : topics) {
				int tID = this.reader.getCategories().indexOf(t);
				if (tID != -1) {
					topicIDs.add(tID);
				}
			}
		}

		Map<Integer, Double> tagIDs = this.calculator.getRankedTagList(userID,
				resID, topicIDs, System.currentTimeMillis() / 1000.0, count,
				this.reader.hasTimestamp(), false);
		for (Map.Entry<Integer, Double> tEntry : tagIDs.entrySet()) {
			tagMap.put(this.reader.getTags().get(tEntry.getKey()), tEntry.getValue());
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

	public synchronized void resetStructure(BookmarkReader reader,
			ThreeLayersCalculator calculator) {
		this.reader = reader;
		this.calculator = calculator;
		
		this.topTags.clear();
		this.topTags.putAll(EngineUtils.calcTopTags(this.reader));
	}
}
