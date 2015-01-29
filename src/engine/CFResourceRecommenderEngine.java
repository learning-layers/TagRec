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

import file.BookmarkReader;
import itemrecommendations.CFResourceRecommender;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import common.Bookmark;
import common.Features;
import common.Similarity;

public class CFResourceRecommenderEngine implements ResourceEngineInterface {

	private BookmarkReader reader = null;
	private CFResourceRecommender calculator = null;
	private final Map<String, Double> topResources;

	public CFResourceRecommenderEngine() {
		this.topResources = new LinkedHashMap<>();		
		this.reader = new BookmarkReader(0, false);
	}
	
	public void loadFile(String filename) throws Exception {

		BookmarkReader reader = new BookmarkReader(0, false);
		reader.readFile(filename);
		Collections.sort(reader.getBookmarks());

		CFResourceRecommender calculator = new CFResourceRecommender(reader, reader.getBookmarks().size(), false, true, false, 5, Similarity.COSINE, Features.ENTITIES);
		
		resetStructure(reader, calculator);
	}

	public synchronized Map<String, Double> getEntitiesWithLikelihood(String user, String resource, List<String> topics, Integer count, Boolean filterOwnEntities) {
		if (count == null || count.doubleValue() < 1) {
			count = 10;
		}
		if (filterOwnEntities == null) {
			filterOwnEntities = true;
		}
		
		Map<String, Double> resourceMap = new LinkedHashMap<>();
		if (this.reader == null || this.calculator == null) {
			System.out.println("No data has been loaded");
			return resourceMap;
		}
		int userID = -1;
		if (user != null) {
			userID = this.reader.getUsers().indexOf(user);
		}
		// used to filter own resources if necessary
		List<Integer> userResources = null;
		if (filterOwnEntities.booleanValue()) {
			userResources = Bookmark.getResourcesFromUser(this.reader.getBookmarks(), userID);
		}

		Map<Integer, Double> resourceIDs = this.calculator.getRankedResourcesList(userID, true, false, false, filterOwnEntities.booleanValue());
		for (Map.Entry<Integer, Double> tEntry : resourceIDs.entrySet()) {
			if (resourceMap.size() < count) {
				resourceMap.put(this.reader.getResources().get(tEntry.getKey()), tEntry.getValue());
			}
		}
		
		if (resourceMap.size() < count) {
			for (Map.Entry<String, Double> t : this.topResources.entrySet()) {
				if (resourceMap.size() < count) {
					// add MP resources if they are not already in the recommeded list or already known by this user
					if (!resourceMap.containsKey(t.getKey()) && (userResources == null || userResources.contains(t.getKey()))) {
						resourceMap.put(t.getKey(), t.getValue());
					}
				} else {
					break;
				}
			}
		}

		return resourceMap;
	}

	public synchronized void resetStructure(BookmarkReader reader, CFResourceRecommender calculator) {
		this.reader = reader;
		this.calculator = calculator;
		
		this.topResources.clear();
		this.topResources.putAll(EngineUtils.calcTopResources(this.reader));
	}
}
