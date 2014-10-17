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

import processing.BM25Calculator;
import file.BookmarkReader;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import common.Features;
import common.Similarity;

public class CFResourceRecommenderEngine implements EngineInterface {

	private BookmarkReader reader = null;
	private BM25Calculator calculator = null;
	private final Map<String, Double> topResources;

	public CFResourceRecommenderEngine() {
		this.topResources = new LinkedHashMap<>();		
		this.reader = new BookmarkReader(0, false);
	}
	
	public void loadFile(String filename) throws Exception {

		BookmarkReader reader = new BookmarkReader(0, false);
		reader.readFile(filename);

		Collections.sort(reader.getBookmarks());
		System.out.println("read in and sorted file");

		BM25Calculator calculator = new BM25Calculator(reader, reader.getBookmarks().size(), false, true, false, 5, Similarity.COSINE, Features.ENTITIES);
		
		resetStructure(reader, calculator);
	}

	public synchronized Map<String, Double> getEntitiesWithLikelihood(String user, String resource, List<String> topics, Integer count) {
		if (count == null || count.doubleValue() < 1) {
			count = 10;
		}
		Map<String, Double> resourceMap = new LinkedHashMap<>();
		if (this.reader == null || this.calculator == null) {
			return resourceMap;
		}
		int userID = -1;
		if (user != null) {
			userID = this.reader.getUsers().indexOf(user);
		}

		Map<Integer, Double> resourceIDs = this.calculator.getRankedResourcesList(userID, true, false, false);
		for (Map.Entry<Integer, Double> tEntry : resourceIDs.entrySet()) {
			if (resourceMap.size() < count) {
				resourceMap.put(this.reader.getResources().get(tEntry.getKey()), tEntry.getValue());
			}
		}
		
		if (resourceMap.size() < count) {
			for (Map.Entry<String, Double> t : this.topResources.entrySet()) {
				if (resourceMap.size() < count) {
					if (!resourceMap.containsKey(t.getKey())) {
						resourceMap.put(t.getKey(), t.getValue());
					}
				} else {
					break;
				}
			}
		}

		return resourceMap;
	}

	public synchronized void resetStructure(BookmarkReader reader, BM25Calculator calculator) {
		this.reader = reader;
		this.calculator = calculator;
		
		this.topResources.clear();
		this.topResources.putAll(EngineUtils.calcTopResources(this.reader));
	}
}
