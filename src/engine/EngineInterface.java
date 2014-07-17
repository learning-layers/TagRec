package engine;

import java.util.List;
import java.util.Map;

public interface EngineInterface {
	
	public void loadFile(String filename) throws Exception;
	
	public Map<String, Double> getTagsWithLikelihood(String user, String resource, List<String> topics, Integer count);
}
