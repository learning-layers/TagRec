package engine;

import java.util.List;
import java.util.Map;

public class EntityRecommenderEngine implements EngineInterface {

	private EngineInterface tagRecommender;
	private EngineInterface resourceRecommender;
	private EngineInterface userRecommender;
	
	public void loadFile(String filename) throws Exception {	
		this.tagRecommender = new TagRecommenderEvalEngine();
		this.tagRecommender.loadFile(filename);
		this.resourceRecommender = new ResourceRecommenderEngine();
		this.resourceRecommender.loadFile(filename);
		this.userRecommender = new UserRecommenderEngine();
		this.userRecommender.loadFile(filename);
	}

	public Map<String, Double> getEntitiesWithLikelihood(String user, String resource, List<String> topics, Integer count, Boolean filterOwnEntities, Algorithm algorithm, EntityType type) {
		if (type == EntityType.TAG) {
			return tagRecommender.getEntitiesWithLikelihood(user, resource, topics, count, filterOwnEntities, algorithm, type);
		} else if (type == EntityType.RESOURCE) {
			return resourceRecommender.getEntitiesWithLikelihood(user, resource, topics, count, filterOwnEntities, algorithm, type);
		} else if (type == EntityType.USER) {
			return userRecommender.getEntitiesWithLikelihood(user, resource, topics, count, filterOwnEntities, algorithm, type);
		}
		return null;
	}
}
