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
