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

import java.util.List;
import java.util.Map;

import file.BookmarkReader;

public class TagRecommenderEngine implements EngineInterface {

	EngineInterface lmEngine;
	EngineInterface bllEngine;
	EngineInterface threelEngine;
	
	public TagRecommenderEngine() {
		this.lmEngine = null;
		this.bllEngine = null;
		this.threelEngine = null;
	}
	
	@Override
	public void loadFile(String filename) throws Exception {
		this.lmEngine = null;
		this.bllEngine = null;
		this.threelEngine = null;
		
		BookmarkReader reader = new BookmarkReader(0, false);
		reader.readFile(filename);
		if (reader.getCategories().size() > 0) {
			this.threelEngine = new ThreeLayersEngine();
			this.threelEngine.loadFile(filename);
		}
		if (reader.hasTimestamp()) {
			this.bllEngine = new BaseLevelLearningEngine();
			this.bllEngine.loadFile(filename);
		} else {
			this.lmEngine = new LanguageModelEngine();
			this.lmEngine.loadFile(filename);
		}
	}

	@Override
	public Map<String, Double> getTagsWithLikelihood(String user, String resource, List<String> topics, Integer count) {
		if (topics != null && topics.size() > 0 && this.threelEngine != null) {
			return this.threelEngine.getTagsWithLikelihood(user, resource, topics, count);
		} else if (this.bllEngine != null) {
			return this.bllEngine.getTagsWithLikelihood(user, resource, topics, count);
		} else {
			return this.lmEngine.getTagsWithLikelihood(user, resource, topics, count);
		}
	}
	
}
